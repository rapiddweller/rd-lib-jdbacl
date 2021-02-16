/*
 * (c) Copyright 2008-2010 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.dialect;

import com.rapiddweller.common.ConfigurationError;
import com.rapiddweller.common.ConnectFailedException;
import com.rapiddweller.jdbacl.DBUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Provides utility methods for using HSQLDB.<br/><br/>
 * Created at 02.05.2008 19:56:35
 *
 * @author Volker Bergmann
 * @since 0.4.3
 */
public class HSQLUtil {

  /**
   * The constant IN_MEMORY_URL_PREFIX.
   */
  public static final String IN_MEMORY_URL_PREFIX = "jdbc:hsqldb:mem:";
  /**
   * The constant DRIVER.
   */
  public static final String DRIVER = "org.hsqldb.jdbcDriver";
  /**
   * The constant DEFAULT_USER.
   */
  public static final String DEFAULT_USER = "sa";
  /**
   * The constant DEFAULT_PASSWORD.
   */
  public static final String DEFAULT_PASSWORD = "";
  /**
   * The constant DEFAULT_SCHEMA.
   */
  public static final String DEFAULT_SCHEMA = "PUBLIC";
  /**
   * The constant DEFAULT_PORT.
   */
  public static final int DEFAULT_PORT = 9001;

  /**
   * Connect in memory db connection.
   *
   * @param dbName the db name
   * @param port   the port
   * @return the connection
   * @throws ConnectFailedException the connect failed exception
   */
  public static Connection connectInMemoryDB(String dbName, int port) throws ConnectFailedException {
    return connectInMemoryDB(dbName + ":" + port);
  }

  /**
   * Connect in memory db connection.
   *
   * @param dbName the db name
   * @return the connection
   * @throws ConnectFailedException the connect failed exception
   */
  public static Connection connectInMemoryDB(String dbName) throws ConnectFailedException {
    String driver = DRIVER;
    try {
      Class.forName(driver);
      String url = getInMemoryURL(dbName);
      return DBUtil.connect(url, DRIVER, DEFAULT_USER, DEFAULT_PASSWORD, false);
    } catch (ClassNotFoundException e) {
      throw new ConfigurationError("JDBC driver not found: " + driver, e);
    }
  }

  /**
   * Gets in memory url.
   *
   * @param dbName the db name
   * @return the in memory url
   */
  public static String getInMemoryURL(String dbName) {
    return IN_MEMORY_URL_PREFIX + dbName;
  }

  /**
   * Shutdown statement.
   *
   * @param url      the url
   * @param user     the user
   * @param password the password
   * @return the statement
   * @throws ClassNotFoundException the class not found exception
   * @throws SQLException           the sql exception
   */
  public static Statement shutdown(String url, String user, String password)
      throws ClassNotFoundException, SQLException {
    Statement statement;
    Class.forName("org.hsqldb.jdbcDriver");
    Connection con = DriverManager.getConnection(url, user, password);
    statement = con.createStatement();
    statement.executeUpdate("SHUTDOWN");
    return statement;
  }

}
