/*
 * (c) Copyright 2008-2011 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.common.ArrayUtil;
import com.rapiddweller.common.ConfigurationError;
import com.rapiddweller.common.Encodings;
import com.rapiddweller.common.ErrorHandler;
import com.rapiddweller.jdbacl.dialect.HSQLUtil;
import com.rapiddweller.jdbacl.model.DBPrimaryKeyConstraint;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.model.DBUniqueConstraint;
import com.rapiddweller.jdbacl.model.TableContainerSupport;
import org.apache.derby.diag.ErrorLogReader;
import org.apache.derby.diag.ErrorMessages;
import org.apache.derby.iapi.jdbc.BrokeredConnection30;
import org.h2.tools.SimpleResultSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link DBUtil} class.<br/><br/>
 * Created at 03.05.2008 15:53:49
 *
 * @author Volker Bergmann
 * @since 0.5.3
 */
public class DBUtilTest {

  /**
   * The Thrown.
   */
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /**
   * The Script file.
   */
  final String SCRIPT_FILE = "com/rapiddweller/jdbacl/create_tables.hsql.sql";


  /**
   * Test exists environment.
   */
  @Test
  public void testExistsEnvironment() {
    assertFalse(DBUtil.existsEnvironment("Environment"));
    assertTrue(DBUtil.existsEnvironment("string://"));
    assertFalse(DBUtil.existsEnvironment("http://"));
  }

  /**
   * Test get environment data.
   *
   * @throws IOException the io exception
   */
  @Test
  public void testGetEnvironmentData() throws IOException {
    thrown.expect(ConfigurationError.class);
    DBUtil.getEnvironmentData("Environment");
  }

  /**
   * Test get environment data 2.
   *
   * @throws IOException the io exception
   */
  @Test
  public void testGetEnvironmentData2() throws IOException {
    assertTrue(DBUtil.getEnvironmentData("string://").isEmpty());
  }

  /**
   * Test get connect data.
   */
  @Test
  public void testGetConnectData() {
    thrown.expect(ConfigurationError.class);
    DBUtil.getConnectData("Environment");
  }

  /**
   * Test get connect data 2.
   */
  @Test
  public void testGetConnectData2() {
    JDBCConnectData actualConnectData = DBUtil.getConnectData("string://");
    assertFalse(actualConnectData.readOnly);
    assertNull(actualConnectData.catalog);
    assertNull(actualConnectData.driver);
    assertNull(actualConnectData.password);
    assertNull(actualConnectData.schema);
    assertNull(actualConnectData.url);
    assertNull(actualConnectData.user);
  }

  /**
   * Test environment file name.
   *
   * @throws IOException the io exception
   */
  @Test
  public void testEnvironmentFileName() throws IOException {
    thrown.expect(ConfigurationError.class);
    DBUtil.environmentFileName("Environment");
  }

  /**
   * Test environment file name 2.
   *
   * @throws IOException the io exception
   */
  @Test
  public void testEnvironmentFileName2() throws IOException {
    assertEquals("string://.env.properties", DBUtil.environmentFileName("string://"));
  }

  /**
   * Test available.
   */
  @Test
  public void testAvailable() {
    assertFalse(DBUtil.available("https://example.org/example", "Driver Class", "User", "iloveyou"));
    assertFalse(DBUtil.available("https://example.org/example", null, "User", "iloveyou"));
    assertFalse(DBUtil.available("https://example.org/example", "com.rapiddweller.jdbacl.DBUtil", "User", "iloveyou"));
    assertFalse(DBUtil.available("https://example.org/example", "java.sql.Connection", "User", "iloveyou"));
  }

  /**
   * Test get open connection count.
   */
  @Test
  public void testGetOpenConnectionCount() {
    assertEquals(0, DBUtil.getOpenConnectionCount());
  }

  /**
   * Test get open prepared statement count.
   */
  @Test
  public void testGetOpenPreparedStatementCount() {
    assertEquals(0, DBUtil.getOpenPreparedStatementCount());
  }

  /**
   * Test get statement.
   */
  @Test
  public void testGetStatement() {
    thrown.expect(RuntimeException.class);
    DBUtil.getStatement(new ErrorLogReader());
  }

  /**
   * Test get statement 2.
   */
  @Test
  public void testGetStatement2() {
    assertNull(DBUtil.getStatement(new SimpleResultSet()));
  }

  /**
   * Test close result set and statement.
   */
  @Test
  public void testCloseResultSetAndStatement() {
    thrown.expect(RuntimeException.class);
    DBUtil.closeResultSetAndStatement(new ErrorLogReader());
  }

  /**
   * Test close result set and statement 2.
   */
  @Test
  public void testCloseResultSetAndStatement2() {
    SimpleResultSet simpleResultSet = new SimpleResultSet();
    DBUtil.closeResultSetAndStatement(simpleResultSet);
    assertTrue(simpleResultSet.isClosed());
    assertEquals(0, simpleResultSet.getRow());
  }

  /**
   * Test parse and simplify result set.
   *
   * @throws SQLException the sql exception
   */
  @Test
  public void testParseAndSimplifyResultSet() throws SQLException {
    SimpleResultSet simpleResultSet = new SimpleResultSet();
    assertEquals(0, ((Object[][]) DBUtil.parseAndSimplifyResultSet(simpleResultSet)).length);
    assertTrue(simpleResultSet.isClosed());
    assertEquals(0, simpleResultSet.getRow());
  }

  /**
   * Test parse and simplify result set 2.
   *
   * @throws SQLException the sql exception
   */
  @Test
  public void testParseAndSimplifyResultSet2() throws SQLException {
    SimpleResultSet simpleResultSet = new SimpleResultSet();
    simpleResultSet.addRow("foo");
    assertEquals(1, ((Object[][]) DBUtil.parseAndSimplifyResultSet(simpleResultSet)).length);
    assertTrue(simpleResultSet.isClosed());
    assertEquals(0, simpleResultSet.getRow());
  }

  /**
   * Test parse result set.
   *
   * @throws SQLException the sql exception
   */
  @Test
  public void testParseResultSet() throws SQLException {
    SimpleResultSet simpleResultSet = new SimpleResultSet();
    assertTrue(DBUtil.parseResultSet(simpleResultSet).isEmpty());
    assertTrue(simpleResultSet.isClosed());
    assertEquals(0, simpleResultSet.getRow());
  }

  /**
   * Test parse result set 2.
   *
   * @throws SQLException the sql exception
   */
  @Test
  public void testParseResultSet2() throws SQLException {
    SimpleResultSet simpleResultSet = new SimpleResultSet();
    simpleResultSet.addRow("foo");
    assertEquals(1, DBUtil.parseResultSet(simpleResultSet).size());
    assertTrue(simpleResultSet.isClosed());
    assertEquals(0, simpleResultSet.getRow());
  }

  /**
   * Test parse result row.
   *
   * @throws SQLException the sql exception
   */
  @Test
  public void testParseResultRow() throws SQLException {
    assertEquals(0, DBUtil.parseResultRow(new SimpleResultSet()).length);
  }

  /**
   * Test column count.
   *
   * @throws SQLException the sql exception
   */
  @Test
  public void testColumnCount() throws SQLException {
    assertEquals(7, DBUtil.columnCount(new ErrorLogReader()));
  }

  /**
   * Test format.
   *
   * @throws SQLException the sql exception
   */
  @Test
  public void testFormat() throws SQLException {
    SimpleResultSet simpleResultSet = new SimpleResultSet();
    assertEquals("", DBUtil.format(simpleResultSet));
    assertTrue(simpleResultSet.isClosed());
    assertEquals(0, simpleResultSet.getRow());
  }

  /**
   * Test format 2.
   *
   * @throws SQLException the sql exception
   */
  @Test
  public void testFormat2() throws SQLException {
    SimpleResultSet simpleResultSet = new SimpleResultSet();
    simpleResultSet.addRow("foo");
    assertEquals("\n", DBUtil.format(simpleResultSet));
    assertTrue(simpleResultSet.isClosed());
    assertEquals(0, simpleResultSet.getRow());
  }

  /**
   * Test execute script file.
   *
   * @throws IOException the io exception
   */
  @Test
  public void testExecuteScriptFile() throws IOException {
    BrokeredConnection30 connection = new BrokeredConnection30(null);
    DBExecutionResult actualExecuteScriptFileResult = DBUtil.executeScriptFile("string://", "UTF-8", 'A', connection,
        true, new ErrorHandler(Object.class));
    assertFalse(actualExecuteScriptFileResult.changedStructure);
    assertNull(actualExecuteScriptFileResult.result);
  }

  /**
   * Test execute script file 2.
   *
   * @throws IOException the io exception
   */
  @Test
  public void testExecuteScriptFile2() throws IOException {
    BrokeredConnection30 connection = new BrokeredConnection30(null);
    DBExecutionResult actualExecuteScriptFileResult = DBUtil.executeScriptFile("string://", "UTF-8", connection, true,
        new ErrorHandler(Object.class));
    assertFalse(actualExecuteScriptFileResult.changedStructure);
    assertNull(actualExecuteScriptFileResult.result);
  }

  /**
   * Test execute script.
   */
  @Test
  public void testExecuteScript() {
    BrokeredConnection30 connection = new BrokeredConnection30(null);
    DBExecutionResult actualExecuteScriptResult = DBUtil.executeScript("--", 'A', connection, true,
        new ErrorHandler(Object.class));
    assertFalse(actualExecuteScriptResult.changedStructure);
    assertNull(actualExecuteScriptResult.result);
  }

  /**
   * Test execute script 2.
   */
  @Test
  public void testExecuteScript2() {
    BrokeredConnection30 connection = new BrokeredConnection30(null);
    DBExecutionResult actualExecuteScriptResult = DBUtil.executeScript("COMMENT", 'A', connection, true,
        new ErrorHandler(Object.class));
    assertFalse(actualExecuteScriptResult.changedStructure);
    assertNull(actualExecuteScriptResult.result);
  }

  /**
   * Test execute script 3.
   */
  @Test
  public void testExecuteScript3() {
    BrokeredConnection30 connection = new BrokeredConnection30(null);
    DBExecutionResult actualExecuteScriptResult = DBUtil.executeScript("--", connection, true,
        new ErrorHandler(Object.class));
    assertFalse(actualExecuteScriptResult.changedStructure);
    assertNull(actualExecuteScriptResult.result);
  }

  /**
   * Test execute script 4.
   */
  @Test
  public void testExecuteScript4() {
    BrokeredConnection30 connection = new BrokeredConnection30(null);
    DBExecutionResult actualExecuteScriptResult = DBUtil.executeScript("COMMENT", connection, true,
        new ErrorHandler(Object.class));
    assertFalse(actualExecuteScriptResult.changedStructure);
    assertNull(actualExecuteScriptResult.result);
  }

  /**
   * Test execute update.
   *
   * @throws SQLException the sql exception
   */
  @Test
  public void testExecuteUpdate() throws SQLException {
    assertEquals(0, DBUtil.executeUpdate(null, new BrokeredConnection30(null)));
    assertEquals(0, DBUtil.executeUpdate("", new BrokeredConnection30(null)));
  }

  /**
   * Test assert no next.
   *
   * @throws IOException  the io exception
   * @throws SQLException the sql exception
   */
  @Test
  public void testAssertNoNext() throws IOException, SQLException {
    thrown.expect(IllegalStateException.class);
    DBUtil.assertNoNext(new ErrorMessages(), "Query");
  }

  /**
   * Test assert no next 2.
   *
   * @throws SQLException the sql exception
   */
  @Test
  public void testAssertNoNext2() throws SQLException {
    SimpleResultSet simpleResultSet = new SimpleResultSet();
    DBUtil.assertNoNext(simpleResultSet, "Query");
    assertTrue(simpleResultSet.isClosed());
    assertEquals(0, simpleResultSet.getRow());
  }

  /**
   * Test escape.
   */
  @Test
  public void testEscape() {
    assertEquals("Text", DBUtil.escape("Text"));
  }

  /**
   * Test dependency ordered tables.
   */
  @Test
  public void testDependencyOrderedTables() {
    assertTrue(DBUtil.dependencyOrderedTables(new TableContainerSupport()).isEmpty());
  }

  /**
   * Test dependency ordered tables 2.
   */
  @Test
  public void testDependencyOrderedTables2() {
    DBTable table = new DBTable("Name");
    TableContainerSupport tableContainerSupport = new TableContainerSupport();
    tableContainerSupport.addTable(table);
    assertEquals(1, DBUtil.dependencyOrderedTables(tableContainerSupport).size());
    assertTrue(((DBTable) tableContainerSupport.getComponents().get(0)).isPKImported());
    assertEquals(0, ((DBTable) tableContainerSupport.getComponents().get(0)).getColumnNames().length);
  }

  /**
   * Test dependency ordered tables 3.
   */
  @Test
  public void testDependencyOrderedTables3() {
    DBTable dbTable = new DBTable("Name");
    dbTable.setPrimaryKey(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
    TableContainerSupport tableContainerSupport = new TableContainerSupport();
    tableContainerSupport.addTable(dbTable);
    assertEquals(1, DBUtil.dependencyOrderedTables(tableContainerSupport).size());
  }

  /**
   * Test dependency ordered tables 4.
   */
  @Test
  public void testDependencyOrderedTables4() {
    DBTable table = new DBTable("table");
    TableContainerSupport tableContainerSupport = new TableContainerSupport();
    tableContainerSupport.addTable(table);
    assertEquals(1, DBUtil.dependencyOrderedTables(tableContainerSupport).size());
    assertTrue(((DBTable) tableContainerSupport.getComponents().get(0)).isPKImported());
    assertEquals(0, ((DBTable) tableContainerSupport.getComponents().get(0)).getColumnNames().length);
  }

  /**
   * Test equivalent.
   */
  @Test
  public void testEquivalent() {
    DBUniqueConstraint uk = new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo");
    assertTrue(
        DBUtil.equivalent(uk, new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo")));
  }

  /**
   * Test equivalent 2.
   */
  @Test
  public void testEquivalent2() {
    DBUniqueConstraint uk = new DBUniqueConstraint(new DBTable("Name"), "Name", true, "Column Names", "foo", "foo");
    assertFalse(
        DBUtil.equivalent(uk, new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo")));
  }

  /**
   * Test contains mandatory column.
   */
  @Test
  public void testContainsMandatoryColumn() {
    assertFalse(DBUtil.containsMandatoryColumn(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true)));
  }

  /**
   * Test run script.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRunScript() throws Exception {
    Connection connection = HSQLUtil.connectInMemoryDB(getClass().getSimpleName());
    ErrorHandler errorHandler = new ErrorHandler(getClass());
    DBExecutionResult result = DBUtil.executeScriptFile(SCRIPT_FILE, Encodings.ISO_8859_1, connection, true, errorHandler);
    assertTrue(result.changedStructure);
    Object[][] rows = (Object[][]) DBUtil.queryAndSimplify("select * from T1", connection);
    assertEquals(1, rows.length);
    assertArrayEquals(ArrayUtil.buildObjectArrayOfType(Object.class, 1, "R&B"), rows[0]);
    int count = (Integer) DBUtil.queryAndSimplify("select count(*) from T1", connection);
    assertEquals(1, count);
  }

  /**
   * Test connection count.
   *
   * @throws Exception the exception
   */
  @Test
  public void testConnectionCount() throws Exception {
    DBUtil.resetMonitors();
    assertEquals(0, DBUtil.getOpenConnectionCount());
    Connection connection = HSQLUtil.connectInMemoryDB(getClass().getSimpleName());
    assertEquals(1, DBUtil.getOpenConnectionCount());
    connection.close();
    assertEquals(0, DBUtil.getOpenConnectionCount());
  }

  // testing checkReadOnly() -----------------------------------------------------------------------------------------

  /**
   * Test read only false.
   */
  @Test
  public void testReadOnly_false() {
    DBUtil.checkReadOnly("insert into xyz (id) values (3)", false);
    DBUtil.checkReadOnly("update xyz set id = 3", false);
    DBUtil.checkReadOnly("select * from xyz", false);
    DBUtil.checkReadOnly("select into xyz2 from xyz", false);
  }

  /**
   * Test read only true insert.
   */
  @Test(expected = IllegalStateException.class)
  public void testReadOnly_true_insert() {
    DBUtil.checkReadOnly("insert into xyz (id) values (3)", true);
  }

  /**
   * Test read only true update.
   */
  @Test(expected = IllegalStateException.class)
  public void testReadOnly_true_update() {
    DBUtil.checkReadOnly("update xyz set id = 3", true);
  }

  /**
   * Test read only true select.
   */
  @Test
  public void testReadOnly_true_select() {
    DBUtil.checkReadOnly("select * from xyz", true);
  }

  /**
   * Test read only true select into.
   */
  @Test(expected = IllegalStateException.class)
  public void testReadOnly_true_select_into() {
    DBUtil.checkReadOnly("select into xyz2 from xyz", true);
  }

  /**
   * Test read only alter session.
   */
  @Test
  public void testReadOnly_alter_session() {
    DBUtil.checkReadOnly("ALTER SESSION SET NLS_LENGTH_SEMANTICS=CHAR", true);
  }

}
