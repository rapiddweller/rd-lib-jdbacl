/*
 * (c) Copyright 2007-2011 by Volker Bergmann. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted under the terms of the
 * GNU General Public License.
 *
 * For redistributing this software or a derivative work under a license other
 * than the GPL-compatible Free Software License as defined by the Free
 * Software Foundation or approved by OSI, you must first obtain a commercial
 * license to this software product from Volker Bergmann.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED CONDITIONS,
 * REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE
 * HEREBY EXCLUDED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.rapiddweller.jdbacl.proxy;

import com.rapiddweller.common.BeanUtil;
import com.rapiddweller.common.LogCategoriesConstants;
import com.rapiddweller.common.debug.Debug;
import com.rapiddweller.common.debug.ResourceMonitor;
import com.rapiddweller.jdbacl.DBUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.ConnectionEventListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wraps a connection for logging of JDBC connection handling.<br/>
 * Created: 24.08.2008 18:47:44<br/>
 *
 * @author Volker Bergmann
 * @since 0.5.5
 */
public class PooledConnectionHandler implements InvocationHandler {

  private static final Logger jdbcLogger = LogManager.getLogger(LogCategoriesConstants.JDBC);
  private static final AtomicInteger openConnectionCount;
  private static long nextId = 0;
  private static ResourceMonitor openConnectionMonitor;

  static {
    openConnectionCount = new AtomicInteger();
    if (Debug.active()) {
      openConnectionMonitor = new ResourceMonitor();
    }
  }

  private final boolean readOnly;
  private final Connection realConnection;
  private final long id;

  // construction ----------------------------------------------------------------------------------------------------
  private final List<ConnectionEventListener> listeners;
  private boolean closed;

  // InvocationHandler implementation --------------------------------------------------------------------------------

  /**
   * Instantiates a new Pooled connection handler.
   *
   * @param realConnection the real connection
   * @param readOnly       the read only
   */
  public PooledConnectionHandler(Connection realConnection, boolean readOnly) {
    this.readOnly = readOnly;
    this.id = nextId();
    this.realConnection = realConnection;
    this.listeners = new ArrayList<>();
    this.closed = false;
    if (jdbcLogger.isDebugEnabled()) {
      jdbcLogger.debug("Created connection #" + id + ": " + realConnection);
    }
    openConnectionCount.incrementAndGet();
    if (openConnectionMonitor != null) {
      openConnectionMonitor.register(this);
    }
  }

  /**
   * Gets open connection count.
   *
   * @return the open connection count
   */
  public static int getOpenConnectionCount() {
    return openConnectionCount.get();
  }

  // PooledConnection implementation ---------------------------------------------------------------------------------

  /**
   * Reset monitors.
   */
  public static void resetMonitors() {
    openConnectionCount.set(0);
    if (openConnectionMonitor != null) {
      openConnectionMonitor.reset();
    }
  }

  /**
   * Assert all connections closed boolean.
   *
   * @param critical the critical
   * @return the boolean
   */
  public static boolean assertAllConnectionsClosed(boolean critical) {
    return openConnectionMonitor.assertNoRegistrations(critical);
  }

  private static synchronized long nextId() {
    return ++nextId;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {
    String methodName = method.getName();
    if ("close".equals(methodName)) {
      this.close();
    } else if ("getConnection".equals(methodName) && (args == null || args.length == 0)) {
      return this.getConnection();
    } else if ("addConnectionEventListener".equals(methodName)) {
      this.addConnectionEventListener((ConnectionEventListener) args[0]);
      return null;
    } else if ("removeConnectionEventListener".equals(methodName)) {
      this.removeConnectionEventListener((ConnectionEventListener) args[0]);
      return null;
    } else if ("prepareStatement".equals(methodName)) {
      switch (args.length) {
        case 1:
          return DBUtil.prepareStatement(realConnection, (String) args[0], readOnly);
        case 2:
          if (method.getParameterTypes()[1] == int.class) {
            return DBUtil.prepareStatement(realConnection, (String) args[0], readOnly,
                (Integer) args[1], ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
          } else {
            break;
          }
        case 3:
          return DBUtil.prepareStatement(realConnection, (String) args[0], readOnly,
              (Integer) args[1], (Integer) args[2], ResultSet.HOLD_CURSORS_OVER_COMMIT);
        case 4:
          return DBUtil.prepareStatement(realConnection, (String) args[0], readOnly,
              (Integer) args[1], (Integer) args[2], (Integer) args[3]);

        default:
          throw new RuntimeException(
              "Error prepareStatement wrong argument input, got "
                  + args.length
                  + " but only 1,2,3 and 4 are supported arguments");
      }
    } else if ("createStatement".equals(methodName)) {
      return createStatement(method, args);
    }
    return BeanUtil.invoke(realConnection, method, args);
  }

  private Statement createStatement(Method method, Object[] args) {
    Statement statement = (Statement) BeanUtil.invoke(realConnection, method, args);
    return DBUtil.createLoggingStatementHandler(statement, readOnly);
  }

  // connection count ------------------------------------------------------------------------------------------------

  /**
   * Close.
   *
   * @throws SQLException the sql exception
   */
  public void close() throws SQLException {
    if (closed) {
      return;
    }
    try {
      realConnection.close();
      listeners.clear();
      openConnectionCount.decrementAndGet();
      if (openConnectionMonitor != null) {
        openConnectionMonitor.unregister(this);
      }
      closed = true;
      if (jdbcLogger.isDebugEnabled()) {
        jdbcLogger.debug("Closed connection #" + id + ": " + realConnection);
      }
    } catch (SQLException e) {
      jdbcLogger.error("Error closing connection #" + id + ": " + realConnection, e);
      throw e;
    }
  }

  /**
   * Gets connection.
   *
   * @return the connection
   */
  public Connection getConnection() {
    return realConnection;
  }

  /**
   * Add connection event listener.
   *
   * @param listener the listener
   */
  public void addConnectionEventListener(ConnectionEventListener listener) {
    listeners.add(listener);
  }

  // private helpers -------------------------------------------------------------------------------------------------

  /**
   * Remove connection event listener.
   *
   * @param listener the listener
   */
  public void removeConnectionEventListener(ConnectionEventListener listener) {
    listeners.remove(listener);
  }

}
