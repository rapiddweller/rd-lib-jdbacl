/*
 * (c) Copyright 2008-2009 by Volker Bergmann. All rights reserved.
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import com.rapiddweller.common.BeanUtil;
import com.rapiddweller.common.CollectionUtil;
import com.rapiddweller.common.ConfigurationError;
import com.rapiddweller.common.LogCategories;
import com.rapiddweller.common.debug.Debug;
import com.rapiddweller.common.debug.ResourceMonitor;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.profile.Profiler;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * {@link InvocationHandler} implementation for a proxy to the {@link Statement} interface
 * which logs relevant JDBC or SQL calls to the log categories <code>com.rapiddweller.JDBC</code>
 * and <code>com.rapiddweller.SQL</code>.<br/>
 * <br/>
 * Created at 26.12.2008 04:48:38
 * @since 0.5.7
 * @author Volker Bergmann
 */

public class LoggingStatementHandler implements InvocationHandler {

    private static final Logger sqlLogger = LogManager.getLogger(LogCategories.SQL);
    private static final Logger jdbcLogger = LogManager.getLogger(LogCategories.JDBC);

	private static final AtomicInteger openStatementCount;
    private static ResourceMonitor openStatementMonitor;

    // attributes ------------------------------------------------------------------------------------------------------

	private final Statement realStatement;
	private final boolean readOnly;
	private String sql;
	private boolean closed;
	
	
	// constructor -----------------------------------------------------------------------------------------------------

    static {
    	openStatementCount = new AtomicInteger();
    	if (Debug.active())
    		openStatementMonitor = new ResourceMonitor();
    }
    
	public LoggingStatementHandler(Statement realStatement, boolean readOnly) {
		this.realStatement = realStatement;
		this.readOnly = readOnly;
		this.closed = false;
		openStatementCount.incrementAndGet();
		if (openStatementMonitor != null)
			openStatementMonitor.register(this);
	}
	
	// InvocationHandler interface implementation ----------------------------------------------------------------------

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		try {
			String methodName = method.getName();
			Method localMethod = BeanUtil.findMethod(this.getClass(), methodName, method.getParameterTypes());
			Object result;
			boolean profile = methodName.startsWith("execute") && "true".equals(System.getProperty("profile"));
			long startTime = 0;
			if (profile)
				startTime = System.nanoTime();
			if (localMethod != null)
				result = BeanUtil.invoke(this, localMethod, args);
			else
				result = BeanUtil.invoke(realStatement, method, args);
			if (result instanceof ResultSet)
				result = DBUtil.createLoggingResultSet((ResultSet) result, (Statement) proxy);
			if (profile) {
				long duration = (System.nanoTime() - startTime) / 1000000;
				Profiler.defaultInstance().addSample(CollectionUtil.toList("SQL", sql), duration);
			}
			return result;
		} catch (ConfigurationError e) {
			if (e.getCause() instanceof InvocationTargetException && e.getCause().getCause() instanceof SQLException)
				throw e.getCause().getCause();
			else
				throw e;
		}
	}

	// execute methods -------------------------------------------------------------------------------------------------

	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		logAll("execute", sql);
		DBUtil.checkReadOnly(sql, readOnly);
		this.sql = sql;
		return realStatement.execute(sql, autoGeneratedKeys);
	}

	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		logAll("execute", sql);
		DBUtil.checkReadOnly(sql, readOnly);
		this.sql = sql;
		return realStatement.execute(sql, columnIndexes);
	}

	public boolean execute(String sql, String[] columnNames) throws SQLException {
		logAll("execute", sql);
		DBUtil.checkReadOnly(sql, readOnly);
		this.sql = sql;
		return realStatement.execute(sql, columnNames);
	}

	public boolean execute(String sql) throws SQLException {
		logAll("execute", sql);
		DBUtil.checkReadOnly(sql, readOnly);
		this.sql = sql;
		return realStatement.execute(sql);
	}

	public int[] executeBatch() throws SQLException {
		jdbcLogger.debug("executeBatch()");
		return realStatement.executeBatch();
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		logAll("executeQuery", sql);
		DBUtil.checkReadOnly(sql, readOnly);
		this.sql = sql;
		return realStatement.executeQuery(sql);
	}
	
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		logAll("executeUpdate", sql);
		DBUtil.checkReadOnly(sql, readOnly);
		this.sql = sql;
		return realStatement.executeUpdate(sql, autoGeneratedKeys);
	}

	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		logAll("executeUpdate", sql);
		DBUtil.checkReadOnly(sql, readOnly);
		this.sql = sql;
		return realStatement.executeUpdate(sql, columnIndexes);
	}

	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		logAll("executeUpdate", sql);
		DBUtil.checkReadOnly(sql, readOnly);
		this.sql = sql;
		return realStatement.executeUpdate(sql, columnNames);
	}

	public int executeUpdate(String sql) throws SQLException {
		logAll("executeUpdate", sql);
		DBUtil.checkReadOnly(sql, readOnly);
		this.sql = sql;
		return realStatement.executeUpdate(sql);
	}
	
	public void close() throws SQLException {
		if (closed)
			return;
		jdbcLogger.debug("close: " + sql);
		this.closed = true;
		openStatementCount.decrementAndGet();
		if (openStatementMonitor != null)
			openStatementMonitor.unregister(this);
		realStatement.close();
	}
	
    public static int getOpenStatementCount() {
    	return openStatementCount.get();
    }
    
	public static void resetMonitors() {
		openStatementCount.set(0);
		if (openStatementMonitor != null)
			openStatementMonitor.reset();
	}
    
	public static boolean assertAllStatementsClosed(boolean critical) {
		return openStatementMonitor.assertNoRegistrations(critical);
	}

	// private helpers -------------------------------------------------------------------------------------------------
	
	private static void logAll(String method, String sql) {
		if (jdbcLogger.isDebugEnabled())
			jdbcLogger.debug(method + ": " + sql);
		sqlLogger.debug(sql);
	}

	// java.lang.Object overrides --------------------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return "Statement (" + sql + ")";
	}

}
