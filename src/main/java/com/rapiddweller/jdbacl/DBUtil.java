/*
 * (c) Copyright 2007-2012 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl;

import com.rapiddweller.common.ArrayBuilder;
import com.rapiddweller.common.ArrayFormat;
import com.rapiddweller.common.ArrayUtil;
import com.rapiddweller.common.BeanUtil;
import com.rapiddweller.common.ConfigUtil;
import com.rapiddweller.common.ErrorHandler;
import com.rapiddweller.common.HF;
import com.rapiddweller.common.HeavyweightIterator;
import com.rapiddweller.common.IOUtil;
import com.rapiddweller.common.LogCategoriesConstants;
import com.rapiddweller.common.ReaderLineIterator;
import com.rapiddweller.common.StringUtil;
import com.rapiddweller.common.SystemInfo;
import com.rapiddweller.common.converter.AnyConverter;
import com.rapiddweller.common.converter.ToStringConverter;
import com.rapiddweller.common.debug.Debug;
import com.rapiddweller.common.depend.DependencyModel;
import com.rapiddweller.common.exception.ExceptionFactory;
import com.rapiddweller.common.iterator.ConvertingIterator;
import com.rapiddweller.jdbacl.model.DBConstraint;
import com.rapiddweller.jdbacl.model.DBPrimaryKeyConstraint;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.model.DBUniqueConstraint;
import com.rapiddweller.jdbacl.model.TableHolder;
import com.rapiddweller.jdbacl.proxy.LoggingPreparedStatementHandler;
import com.rapiddweller.jdbacl.proxy.LoggingResultSetHandler;
import com.rapiddweller.jdbacl.proxy.LoggingStatementHandler;
import com.rapiddweller.jdbacl.proxy.PooledConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.PooledConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.rapiddweller.jdbacl.SQLUtil.createCatSchTabString;

/**
 * Provides database related utility methods.<br/><br/>
 * Created: 06.01.2007 19:27:02
 * @author Volker Bergmann
 */
public class DBUtil {

  private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);

  private static final Logger jdbcLogger = LoggerFactory.getLogger(LogCategoriesConstants.JDBC);
  private static final Logger sqlLogger = LoggerFactory.getLogger(LogCategoriesConstants.SQL);

  public static final String ENV_PROPERTIES_SUFFIX = ".env.properties";
  public static final String DATABASE_QUERY_FAILED_COLON = "Database query failed: ";

  private DBUtil() {
    // private constructor for preventing instantiation.
  }


  // connection handling ---------------------------------------------------------------------------------------------

  public static String[] getEnvironmentNames() {
    File rapiddwellerFolder = new File(SystemInfo.getUserHome(), "rapiddweller");
    String[] fileNames = rapiddwellerFolder.list((dir, name) -> (name.toLowerCase().endsWith(ENV_PROPERTIES_SUFFIX)));
    String[] result = new String[Objects.requireNonNull(fileNames).length];
    for (int i = 0; i < fileNames.length; i++) {
      String fileName = fileNames[i];
      result[i] = fileName.substring(0, fileName.length() - ENV_PROPERTIES_SUFFIX.length());
    }
    return result;
  }

  /** Determines if a configuration exists for the specified environment.
   *  Read {@link #environmentFilePath(String, String)} to find out where the
   *  configuration file is searched. */
  public static boolean existsEnvironment(String environment, String folder) {
    try {
      getConnectData(environment, folder);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static Map<String, String> getEnvironmentData(String environment, String folder) {
    return IOUtil.readProperties(environmentFilePath(environment, folder));
  }

  public static JDBCConnectData getConnectData(String environment, String folder) {
    try {
      String path = environmentFilePath(environment, folder);
      return JDBCConnectData.parseSingleDbProperties(path);
    } catch (IOException e) {
      throw ExceptionFactory.getInstance().configurationError("Error reading environment data for '" + environment + "'");
    }
  }

  /**
   * Determines the name and location of an environment properties file and provides it as
   * canonical path (as defined in java.io.{@link File}).
   * It builds the file name and checks, in which folders it is present and
   * returns the first match. The search order is
   * <ol>
   *   <li>the directory specified by the 'folder' parameter (which can be absolute or relative)</li>
   *   <li>a sub directory 'conf' of the directory specified by the 'folder' parameter</li>
   *   <li>the current working directory</li>
   *   <li>the directory ${USER_HOME}/rapiddweller</li>
   * </ol>
   */
  public static String environmentFilePath(String environment, String folder) {
    String filename = environment + ENV_PROPERTIES_SUFFIX;
    return ConfigUtil.configFilePathDefaultLocations(filename, folder);
  }

  public static Connection connect(String environment, String folder, boolean readOnly) {
    JDBCConnectData connectData = DBUtil.getConnectData(environment, folder);
    return connect(connectData, readOnly);
  }

  public static Connection connect(JDBCConnectData data, boolean readOnly) {
    if (StringUtil.isEmpty(data.url)) {
      throw ExceptionFactory.getInstance().configurationError("No JDBC URL specified");
    }
    if (StringUtil.isEmpty(data.driver)) {
      throw ExceptionFactory.getInstance().configurationError("No JDBC driver class name specified");
    }
    if (!readOnly && data.readOnly) {
      throw ExceptionFactory.getInstance().configurationError("Environment is configured to be read only but was connected for read/write access");
    }
    return connect(data.url, data.driver, data.user, data.password, readOnly);
  }

  public static Connection connect(String url, String driverClassName, String user, String password, boolean readOnly) {
    try {
      if (driverClassName == null) {
        throw ExceptionFactory.getInstance().configurationError("No JDBC driver class name provided");
      }

      // Wrap connection properties
      java.util.Properties info = new java.util.Properties();
      if (user != null) {
        info.put("user", user);
      }
      if (password != null) {
        info.put("password", password);
      }

      // Instantiate driver
      Class<Driver> driverClass = BeanUtil.forName(driverClassName);
      Driver driver = driverClass.getDeclaredConstructor().newInstance();

      // connect
      jdbcLogger.debug("opening connection to {}", url);
      Connection connection = driver.connect(url, info);
      if (connection == null) {
        throw ExceptionFactory.getInstance().connectFailed("Connecting the database failed silently - " +
            "probably due to wrong driver (" + driverClassName + ") or wrong URL format (" + url + ")", null);
      }
      connection = wrapWithPooledConnection(connection, readOnly);
      return connection;
    } catch (Exception e) {
      throw ExceptionFactory.getInstance().connectFailed("Connect to database at " + url + " failed", e);
    }
  }

  public static boolean available(String url, String driverClass, String user, String password) {
    try {
      Connection connection = connect(url, driverClass, user, password, false);
      close(connection);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static void close(Connection connection) {
    if (connection == null) {
      return;
    }
    try {
      connection.close();
    } catch (SQLException e) {
      logger.error("Error closing connection", e);
    }
  }

  public static Connection wrapWithPooledConnection(Connection connection, boolean readOnly) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    return (Connection) Proxy.newProxyInstance(classLoader,
        new Class[] {Connection.class, PooledConnection.class},
        new PooledConnectionHandler(connection, readOnly));
  }

  public static int getOpenConnectionCount() {
    return PooledConnectionHandler.getOpenConnectionCount();
  }

  public static void resetMonitors() {
    LoggingPreparedStatementHandler.resetMonitors();
    LoggingResultSetHandler.resetMonitors();
    LoggingStatementHandler.resetMonitors();
    PooledConnectionHandler.resetMonitors();
  }

  // statement handling ----------------------------------------------------------------------------------------------

  public static Statement createLoggingStatementHandler(Statement statement, boolean readOnly) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    statement = (Statement) Proxy.newProxyInstance(classLoader,
        new Class[] {Statement.class},
        new LoggingStatementHandler(statement, readOnly));
    return statement;
  }

  public static PreparedStatement prepareStatement(Connection connection, String sql, boolean readOnly) throws SQLException {
    return prepareStatement(connection, sql, readOnly,
        ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
  }

  public static PreparedStatement prepareStatement(
      Connection connection,
      String sql,
      boolean readOnly,
      int resultSetType,
      int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
    jdbcLogger.debug("preparing statement: {}", sql);
    checkReadOnly(sql, readOnly);
    if (connection instanceof PooledConnection) {
      connection = ((PooledConnection) connection).getConnection();
    }
    int _resultSetHoldability = connection.getMetaData().getResultSetHoldability();
    PreparedStatement statement = connection.prepareStatement(
        sql, resultSetType, resultSetConcurrency, _resultSetHoldability);
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if (sqlLogger.isDebugEnabled() || jdbcLogger.isDebugEnabled()) {
      statement = (PreparedStatement) Proxy.newProxyInstance(classLoader,
          new Class[] {PreparedStatement.class},
          new LoggingPreparedStatementHandler(statement, sql));
    }
    return statement;
  }

  public static void close(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException e) {
        throw ExceptionFactory.getInstance().configurationError("Closing statement failed", e);
      }
    }
  }

  public static int getOpenStatementCount() {
    return LoggingStatementHandler.getOpenStatementCount();
  }

  public static int getOpenPreparedStatementCount() {
    return LoggingPreparedStatementHandler.getOpenStatementCount();
  }

  // ResultSet handling ----------------------------------------------------------------------------------------------

  public static ResultSet createLoggingResultSet(ResultSet realResultSet, Statement statement) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    return (ResultSet) Proxy.newProxyInstance(classLoader,
        new Class[] {ResultSet.class},
        new LoggingResultSetHandler(realResultSet, statement));
  }

  public static Statement getStatement(ResultSet resultSet) {
    try {
      return resultSet.getStatement();
    } catch (SQLException e) {
      throw ExceptionFactory.getInstance().operationFailed("Error getting statement from result set", e);
    }
  }

  public static void close(ResultSet resultSet) {
    if (resultSet != null) {
      try {
        resultSet.close();
      } catch (SQLException e) {
        throw ExceptionFactory.getInstance().operationFailed("Closing statement failed", e);
      }
    }
  }

  public static void closeResultSetAndStatement(ResultSet resultSet) {
    if (resultSet != null) {
      closeResultSetAndStatement(resultSet, getStatement(resultSet));
    }
  }

  public static void closeResultSetAndStatement(ResultSet resultSet, Statement statement) {
    if (resultSet != null) {
      try {
        close(resultSet);
      } finally {
        close(statement);
      }
    } else {
      close(statement);
    }
  }

  public static int getOpenResultSetCount() {
    return LoggingResultSetHandler.getOpenResultSetCount();
  }

  public static Object parseAndSimplifyResultSet(ResultSet resultSet) throws SQLException {
    List<Object[]> rows = parseResultSet(resultSet);
    if (rows.size() == 1 && rows.get(0).length == 1) {
      return rows.get(0)[0];
    } else {
      Object[][] array = new Object[rows.size()][];
      return rows.toArray(array);
    }
  }

  public static List<Object[]> parseResultSet(ResultSet resultSet) throws SQLException {
    List<Object[]> rows = new ArrayList<>();
    while (resultSet.next()) {
      rows.add(parseResultRow(resultSet));
    }
    return rows;
  }

  protected static Object[] parseResultRow(ResultSet resultSet) throws SQLException {
    int columnCount = columnCount(resultSet);
    Object[] cells = new Object[columnCount];
    for (int i = 0; i < columnCount; i++) {
      cells[i] = resultSet.getObject(i + 1);
    }
    return cells;
  }


  public static int columnCount(ResultSet resultSet) throws SQLException {
    return resultSet.getMetaData().getColumnCount();
  }

  public static long countRows(DBTable table, Connection connection) {
    return DBUtil.queryLong(
        "SELECT COUNT(*) FROM " + createCatSchTabString(table.getCatalog().getName(), table.getSchema().getName(), table.getName()) + table.getName(),
        connection);
  }

  public static String format(ResultSet resultSet) throws SQLException {
    StringBuilder builder = new StringBuilder();
    // format column names
    ResultSetMetaData metaData = resultSet.getMetaData();
    int columnCount = metaData.getColumnCount();
    for (int i = 1; i <= columnCount; i++) {
      builder.append(metaData.getColumnName(i)).append(i < columnCount ? ", " : SystemInfo.getLineSeparator());
    }
    // format cells
    Object parsed = parseAndSimplifyResultSet(resultSet);
    if (parsed instanceof Object[][]) {
      for (Object[] row : (Object[][]) parsed) {
        builder.append(ArrayFormat.format(", ", row)).append(SystemInfo.getLineSeparator());
      }
    } else {
      builder.append(ToStringConverter.convert(parsed, "null"));
    }
    return builder.toString();
  }

  public static String queryString(PreparedStatement statement) {
    ResultSet resultSet = null;
    try {
      resultSet = statement.executeQuery();
      if (!resultSet.next()) {
        throw ExceptionFactory.getInstance().unexpectedQueryResult("Expected a row.", null);
      }
      String value = resultSet.getString(1);
      if (resultSet.next()) {
        throw ExceptionFactory.getInstance().unexpectedQueryResult("Expected exactly one row, found more.", null);
      }
      return value;
    } catch (SQLException e) {
      throw ExceptionFactory.getInstance().unexpectedQueryResult("Database query failed", e);
    } finally {
      close(resultSet);
    }
  }

  public static Long queryLong(String query, Connection connection) {
    return AnyConverter.convert(queryScalar(query, connection), Long.class);
  }

  public static Integer queryInt(String query, Connection connection) {
    return AnyConverter.convert(queryScalar(query, connection), Integer.class);
  }

  public static Object queryScalar(String query, Connection connection) {
    Statement statement = null;
    ResultSet resultSet = null;
    try {
      statement = connection.createStatement();
      resultSet = statement.executeQuery(query);
      if (!resultSet.next()) {
        throw ExceptionFactory.getInstance().unexpectedQueryResult("Query has an empty result: " + query, null);
      }
      Object value = resultSet.getObject(1);
      if (resultSet.next()) {
        throw ExceptionFactory.getInstance().unexpectedQueryResult("Expected exactly one row, but found more for query: " + query, null);
      }
      return value;
    } catch (SQLException e) {
      throw ExceptionFactory.getInstance().unexpectedQueryResult(DATABASE_QUERY_FAILED_COLON + query, e);
    } finally {
      closeResultSetAndStatement(resultSet, statement);
    }
  }

  public static DBExecutionResult executeScriptFile(
      String scriptUri, String encoding, Connection connection, boolean ignoreComments, ErrorHandler errorHandler) {
    return executeScriptFile(scriptUri, encoding, ';', connection, ignoreComments, errorHandler);
  }

  public static DBExecutionResult executeScriptFile(
      String scriptUri, String encoding, char separator, Connection connection, boolean ignoreComments,
      ErrorHandler errorHandler) {
    BufferedReader reader = IOUtil.getReaderForURI(scriptUri, encoding);
    return runScript(reader, separator, connection, ignoreComments, errorHandler);
  }

  public static DBExecutionResult executeScript(String scriptText, Connection connection, boolean ignoreComments,
                                                ErrorHandler errorHandler) {
    return executeScript(scriptText, ';', connection, ignoreComments, errorHandler);
  }

  public static DBExecutionResult executeScript(String scriptText, char separator, Connection connection,
                                                boolean ignoreComments, ErrorHandler errorHandler) {
    StringReader reader = new StringReader(scriptText);
    return runScript(reader, separator, connection, ignoreComments, errorHandler);
  }

  private static DBExecutionResult runScript(
      Reader reader, char separator, Connection connection, boolean ignoreComments, ErrorHandler errorHandler) {
    ReaderLineIterator iterator = new ReaderLineIterator(reader);
    SQLScriptException exception = null;
    Object result = null;
    boolean changedStructure = false;
    try {
      StringBuilder cmd = new StringBuilder();
      while (iterator.hasNext()) {
        String line = iterator.next().trim();
        if (line.startsWith("--")) {
          continue;
        }
        if (cmd.length() > 0) {
          cmd.append('\n');
        }
        cmd.append(line);
        boolean lineEndsWithSeparator = (line.length() > 0 && StringUtil.lastChar(line) == separator);
        if (lineEndsWithSeparator || !iterator.hasNext()) {
          if (lineEndsWithSeparator) {
            cmd.delete(cmd.length() - 1, cmd.length()); // delete trailing separators
          }
          String sql = cmd.toString().trim();
          if (sql.length() > 0 && (!ignoreComments || !StringUtil.startsWithIgnoreCase(sql, "COMMENT"))) {
            try {
              if (SQLUtil.isQuery(sql)) {
                result = queryAndSimplify(sql, connection);
              } else {
                result = executeUpdate(sql, connection);
                changedStructure |= SQLUtil.mutatesStructure(sql);
              }
            } catch (SQLException e) {
              if (errorHandler == null) {
                errorHandler = new ErrorHandler(DBUtil.class);
              }
              errorHandler.handleError("Error in executing SQL: " + SystemInfo.getLineSeparator() + cmd, e);
              // if we arrive here, the ErrorHandler decided not to throw an exception
              // so we save the exception and line number and continue execution
              if (exception != null) { // only the first exception is saved
                exception = new SQLScriptException(e, iterator.lineCount());
              }
            }
          }
          cmd.delete(0, cmd.length());
        }
      }
      Object returnedValue = (exception != null ? exception : result);
      return new DBExecutionResult(returnedValue, changedStructure);
    } finally {
      IOUtil.close(iterator);
    }
  }

  public static int executeUpdate(String sql, Connection connection) throws SQLException {
    if (sql == null || sql.trim().length() == 0) {
      logger.warn("Empty SQL string in executeUpdate()");
      return 0;
    }
    int result = 0;
    Statement statement = null;
    try {
      statement = connection.createStatement();
      result = statement.executeUpdate(sql);
    } finally {
      close(statement);
      connection.commit();
    }
    return result;
  }

  public static <T> T[] queryScalarRowsAsArray(String query, Class<T> componentType, Connection connection) {
    Statement statement = null;
    ResultSet resultSet = null;
    try {
      statement = connection.createStatement();
      resultSet = statement.executeQuery(query);
      ArrayBuilder<T> builder = new ArrayBuilder<>(componentType);
      while (resultSet.next()) {
        builder.add(AnyConverter.convert(resultSet.getObject(1), componentType));
      }
      return builder.toArray();
    } catch (SQLException e) {
      throw ExceptionFactory.getInstance().dbQueryFailed(DATABASE_QUERY_FAILED_COLON + query, e);
    } finally {
      closeResultSetAndStatement(resultSet, statement);
    }
  }

  public static <T> T[] queryScalarRow(String query, Class<T> componentType, Connection connection) {
    Statement statement = null;
    ResultSet resultSet = null;
    try {
      statement = connection.createStatement();
      resultSet = statement.executeQuery(query);
      assertNext(resultSet, query);
      int columnCount = columnCount(resultSet);
      T[] result = ArrayUtil.newInstance(componentType, columnCount);
      for (int i = 0; i < columnCount; i++) {
        Array.set(result, i, AnyConverter.convert(resultSet.getObject(i + 1), componentType));
      }
      assertNoNext(resultSet, query);
      return result;
    } catch (SQLException e) {
      throw ExceptionFactory.getInstance().dbQueryFailed(DATABASE_QUERY_FAILED_COLON + query, e);
    } finally {
      closeResultSetAndStatement(resultSet, statement);
    }
  }

  public static Object queryAndSimplify(String query, Connection connection) throws SQLException {
    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(query, connection);
      return parseAndSimplifyResultSet(resultSet);
    } finally {
      closeResultSetAndStatement(resultSet);
    }
  }

  public static List<Object[]> query(String query, Connection connection) throws SQLException {
    ResultSet resultSet = executeQuery(query, connection); // note: exception handling happens in executeQuery()
    try {
      return parseResultSet(resultSet);
    } finally {
      closeResultSetAndStatement(resultSet);
    }
  }

  public static Object[] querySingleRow(String query, Connection connection) throws SQLException {
    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(query, connection);
      assertNext(resultSet, query);
      Object[] result = parseResultRow(resultSet);
      assertNoNext(resultSet, query);
      return result;
    } finally {
      closeResultSetAndStatement(resultSet);
    }
  }


  public static void assertNoNext(ResultSet resultSet, String query)
      throws SQLException {
    if (resultSet.next()) {
      throw ExceptionFactory.getInstance().assertionFailed("One-row database query returned multiple rows: " + query);
    }
  }


  public static void assertNext(ResultSet resultSet, String query)
      throws SQLException {
    if (!resultSet.next()) {
      throw ExceptionFactory.getInstance().objectNotFound("Database query did not return a result: " + query);
    }
  }

  public static HeavyweightIterator<Object[]> iterateQueryResults(String query, Connection connection) throws SQLException {
    ResultSet resultSet = connection.createStatement().executeQuery(query);
    ResultSetConverter<Object[]> converter = new ResultSetConverter<>(Object[].class);
    return new ConvertingIterator<>(new ResultSetIterator(resultSet), converter);
  }

  public static ResultSet executeQuery(String query, Connection connection) {
    Statement statement = null;
    try {
      statement = connection.createStatement();
      return statement.executeQuery(query);
    } catch (Exception e) {
      close(statement);
      throw ExceptionFactory.getInstance().dbQueryFailed("Error executing query: " + query, e);
    }
  }

  public static String escape(String text) {
    return text.replace("'", "''");
  }

  public static ResultsWithMetadata queryWithMetadata(String query, Connection connection) throws SQLException {
    Statement statement = null;
    ResultSet resultSet = null;
    try {
      statement = connection.createStatement();
      resultSet = statement.executeQuery(query);
      ResultSetMetaData metaData = resultSet.getMetaData();
      int columnCount = metaData.getColumnCount();
      String[] columnNames = new String[columnCount];
      for (int i = 1; i <= columnCount; i++) {
        columnNames[i - 1] = metaData.getColumnLabel(i);
      }
      List<Object[]> rows = new ArrayList<>();
      while (resultSet.next()) {
        String[] cells = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
          cells[i] = resultSet.getString(i + 1);
        }
        rows.add(cells);
      }
      String[][] array = new String[rows.size()][];
      return new ResultsWithMetadata(columnNames, rows.toArray(array));
    } finally {
      closeResultSetAndStatement(resultSet, statement);
    }
  }

  public static void checkReadOnly(String sql, boolean readOnly) {
    if (!readOnly) {
      return;
    }
    Boolean mutation = SQLUtil.mutatesDataOrStructure(sql);
    if (mutation == null || mutation) {
      throw ExceptionFactory.getInstance().servicePermissionDenied("Tried to mutate a database with read-only settings: " + sql);
    }
  }

  public static void logMetaData(Connection connection) {
    try {
      DatabaseMetaData metaData = connection.getMetaData();
      jdbcLogger.info("Connected to {} {}", metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion());
      jdbcLogger.info("Using driver {} {}", metaData.getDriverName(), metaData.getDriverVersion());
      jdbcLogger.info("JDBC version {}.{}", metaData.getJDBCMajorVersion(), metaData.getJDBCMinorVersion());

    } catch (SQLException e) {
      logger.error("Failed to fetch metadata from connection {}", connection);
    }
  }

  public static List<DBTable> dependencyOrderedTables(TableHolder tableHolder) {
    DependencyModel<DBTable> model = new DependencyModel<>();
    for (DBTable table : tableHolder.getTables()) {
      model.addNode(table);
    }
    return model.dependencyOrderedObjects(true);
  }

  public static boolean equivalent(DBUniqueConstraint uk, DBPrimaryKeyConstraint pk) {
    return Arrays.equals(uk.getColumnNames(), pk.getColumnNames());
  }

  public static void assertAllDbResourcesClosed(boolean critical) {
    boolean success = true;
    String message = null;
    if (Debug.active()) {
      success &= PooledConnectionHandler.assertAllConnectionsClosed(false);
      success &= LoggingPreparedStatementHandler.assertAllStatementsClosed(false);
      success &= LoggingStatementHandler.assertAllStatementsClosed(false);
      success &= LoggingResultSetHandler.assertAllResultSetsClosed(false);
      if (!success) {
        message = "There are unclosed database resources";
      }
    } else {
      message = checkOpenDbResources();
      success = (message == null);
    }
    if (!success) {
      if (critical) {
        throw ExceptionFactory.getInstance().assertionFailed(message);
      } else {
        logger.warn(message);
      }
    }
  }

  private static String checkOpenDbResources() {
    int c = getOpenConnectionCount();
    int r = getOpenResultSetCount();
    int s = getOpenStatementCount();
    int p = getOpenPreparedStatementCount();
    boolean success = (c == 0 && r == 0 && s == 0 && p == 0);
    if (success) {
      return null;
    } else {
      StringBuilder builder = new StringBuilder();
      if (c != 0) {
        builder.append(HF.pluralize(c, "connection"));
      }
      if (r != 0) {
        builder.append(commaIfNotEmpty(builder)).append(HF.pluralize(r, "result set"));
      }
      if (s != 0) {
        builder.append(commaIfNotEmpty(builder)).append(HF.pluralize(s, "statement"));
      }
      if (p != 0) {
        builder.append(commaIfNotEmpty(builder)).append(HF.pluralize(p, " prepared statement(s)"));
      }
      return "There are unclosed database resources: " + builder;
    }
  }

  private static String commaIfNotEmpty(StringBuilder builder) {
    return (builder.length() > 0 ? ", " : "");
  }

  public static boolean containsMandatoryColumn(DBConstraint fk) {
    for (String columnName : fk.getColumnNames()) {
      if (!fk.getTable().getColumn(columnName).isNullable()) {
        return true;
      }
    }
    return false;
  }

  public static void insert(String table, Connection connection, DatabaseDialect dialect, Object... values) throws SQLException {
    DBUtil.executeUpdate(SQLUtil.insert(connection.getCatalog(), connection.getSchema(), table, dialect, values), connection);
  }

}
