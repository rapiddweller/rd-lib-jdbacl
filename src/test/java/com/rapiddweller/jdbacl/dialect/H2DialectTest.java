/*
 * (c) Copyright 2010-2012 by Volker Bergmann. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted under the terms of the
 * GNU General Public License (GPL).
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

import com.rapiddweller.common.ConnectFailedException;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.model.DBSequence;
import com.rapiddweller.jdbacl.sql.Query;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link H2Dialect}.<br/><br/>
 * Created: 28.03.2010 11:44:38
 *
 * @author Volker Bergmann
 * @since 0.6.0
 */
public class H2DialectTest extends DatabaseDialectTest<H2Dialect> {

  /**
   * Test constructor.
   */
  @Test
  public void testConstructor() {
    H2Dialect actualH2Dialect = new H2Dialect();
    assertEquals("h2", actualH2Dialect.getSystem());
    assertTrue(actualH2Dialect.isSequenceSupported());
  }

  /**
   * Test is default catalog.
   */
  @Test
  public void testIsDefaultCatalog() {
    assertFalse((new H2Dialect()).isDefaultCatalog("Catalog", "User"));
    assertTrue((new H2Dialect()).isDefaultCatalog(null, "User"));
  }

  /**
   * Test is default schema.
   */
  @Test
  public void testIsDefaultSchema() {
    assertFalse((new H2Dialect()).isDefaultSchema("Schema", "User"));
    assertTrue((new H2Dialect()).isDefaultSchema("PUBLIC", "User"));
  }

  /**
   * Instantiates a new H 2 dialect test.
   */
  public H2DialectTest() {
    super(new H2Dialect());
  }

  /**
   * Test format date.
   */
  @Test
  public void testFormatDate() {
    assertEquals("'1971-02-03'", dialect.formatValue(DATE_19710203));
  }

  /**
   * Test format datetime.
   */
  @Test
  public void testFormatDatetime() {
    assertEquals("'1971-02-03 13:14:15'", dialect.formatValue(DATETIME_19710203131415));
  }

  /**
   * Test format time.
   */
  @Test
  public void testFormatTime() {
    assertEquals("'13:14:15'", dialect.formatValue(TIME_131415));
  }

  /**
   * Test format timestamp.
   */
  @Test
  public void testFormatTimestamp() {
    assertEquals("'1971-02-03 13:14:15.123456789'",
        dialect.formatValue(TIMESTAMP_19710203131415123456789));
  }

  /**
   * Test is deterministic pk name.
   */
  @Test
  public void testIsDeterministicPKName() {
    assertFalse(dialect.isDeterministicPKName("CONSTRAINT_6D"));
    assertTrue(dialect.isDeterministicPKName("USER_PK"));
    assertTrue((new H2Dialect()).isDeterministicPKName("Pk Name"));
    assertFalse((new H2Dialect()).isDeterministicPKName("CONSTRAINT_U"));
  }

  /**
   * Test is deterministic uk name.
   */
  @Test
  public void testIsDeterministicUKName() {
    assertFalse(dialect.isDeterministicUKName("CONSTRAINT_INDEX_6"));
    assertTrue(dialect.isDeterministicUKName("USER_NAME_UK"));
    assertTrue((new H2Dialect()).isDeterministicUKName("Uk Name"));
    assertFalse((new H2Dialect()).isDeterministicUKName("CONSTRAINT_INDEX_U"));
  }

  /**
   * Test is deterministic fk name.
   */
  @Test
  public void testIsDeterministicFKName() {
    assertFalse(dialect.isDeterministicFKName("CONSTRAINT_34"));
    assertTrue(dialect.isDeterministicFKName("USER_ROLE_FK"));
    assertTrue((new H2Dialect()).isDeterministicFKName("Fk Name"));
    assertFalse((new H2Dialect()).isDeterministicFKName("CONSTRAINT_U"));
  }

  /**
   * Test is deterministic index name.
   */
  @Test
  public void testIsDeterministicIndexName() {
    assertFalse(dialect.isDeterministicIndexName("PRIMARY_KEY_6"));
    assertFalse(dialect.isDeterministicIndexName("CONSTRAINT_INDEX_6"));
    assertTrue(dialect.isDeterministicIndexName("USER_NAME_IDX"));
    assertTrue((new H2Dialect()).isDeterministicIndexName("Index Name"));
    assertFalse((new H2Dialect()).isDeterministicIndexName("CONSTRAINT_INDEX_U"));
  }

  /**
   * Test supports regex.
   */
  @Test
  public void testSupportsRegex() {
    assertTrue((new H2Dialect()).supportsRegex());
  }

  /**
   * Test regex query.
   */
  @Test
  public void testRegexQuery() {
    assertEquals("Expression NOT REGEXP 'Regex'", (new H2Dialect()).regexQuery("Expression", true, "Regex"));
    assertEquals("Expression REGEXP 'Regex'", (new H2Dialect()).regexQuery("Expression", false, "Regex"));
  }

  /**
   * Test restrict rownums.
   */
  @Test
  public void testRestrictRownums() {
    Query selectResult = Query.select("Selection");
    (new H2Dialect()).restrictRownums(1, 3, selectResult);
    assertEquals("SELECT Selection FROM  LIMIT 3 OFFSET 1", selectResult.toString());
  }

  /**
   * Test restrict rownums 2.
   */
  @Test
  public void testRestrictRownums2() {
    Query selectResult = Query.select("Selection");
    (new H2Dialect()).restrictRownums(0, 3, selectResult);
    assertEquals("SELECT Selection FROM  LIMIT 3", selectResult.toString());
  }

  /**
   * Test regex.
   */
  @Test
  public void testRegex() {
    assertTrue(dialect.supportsRegex());
    assertEquals("code REGEXP '[A-Z]{5}'", dialect.regexQuery("code", false, "[A-Z]{5}"));
    assertEquals("code NOT REGEXP '[A-Z]{5}'", dialect.regexQuery("code", true, "[A-Z]{5}"));
  }

  /**
   * Test render create sequence.
   */
  @Test
  public void testRenderCreateSequence() {
    assertEquals("CREATE SEQUENCE my_seq", dialect.renderCreateSequence(new DBSequence("my_seq", null)));
    assertEquals("CREATE SEQUENCE my_seq START WITH 10 INCREMENT BY 2 CYCLE",
        dialect.renderCreateSequence(createConfiguredSequence()));
  }

  /**
   * Test sequences online.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSequencesOnline() throws Exception {
    testSequencesOnline("h2");
  }

  /**
   * Test render fetch sequence value.
   */
  @Test
  public void testRenderFetchSequenceValue() {
    assertEquals("select next value for SEQ", dialect.renderFetchSequenceValue("SEQ"));
    assertEquals("select next value for Sequence Name", (new H2Dialect()).renderFetchSequenceValue("Sequence Name"));
  }

  /**
   * Test set next sequence value.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSetNextSequenceValue() throws Exception {
    Connection connection = H2Util.connectInMemoryDB(getClass().getSimpleName());
    String sequenceName = getClass().getSimpleName();
    try {
      DBUtil.executeUpdate("create sequence " + sequenceName, connection);
      dialect.setNextSequenceValue(sequenceName, 123, connection);
      String seqValQuery = dialect.renderFetchSequenceValue(sequenceName);
      assertEquals(123L, DBUtil.queryScalar(seqValQuery, connection));
    } finally {
      DBUtil.executeUpdate("drop sequence " + sequenceName, connection);
    }
  }

  /**
   * Test set sequence value.
   */
  @Test
  public void testSetSequenceValue() {
    assertEquals("alter sequence Sequence Name restart with 42",
        (new H2Dialect()).setSequenceValue("Sequence Name", 42L));
  }

  /**
   * Test render drop sequence.
   */
  @Test
  public void testRenderDropSequence() {
    assertEquals("drop sequence Name", (new H2Dialect()).renderDropSequence("Name"));
  }

  /**
   * Test drop sequence.
   */
  @Test
  public void testDropSequence() {
    assertEquals("drop sequence SEQ", dialect.renderDropSequence("SEQ"));
  }

  /**
   * Test render case.
   */
  @Test
  public void testRenderCase() {
    assertEquals("CASE WHEN condition1 THEN result1 WHEN condition2 THEN result2 ELSE result4 END AS col",
        dialect.renderCase("col", "result4", "condition1", "result1", "condition2", "result2"));
  }

  /**
   * Test offset and row count restriction.
   *
   * @throws ConnectFailedException the connect failed exception
   * @throws SQLException           the sql exception
   */
  @Test
  public void testOffsetAndRowCountRestriction() throws ConnectFailedException, SQLException {
    Connection connection = H2Util.connectInMemoryDB(getClass().getSimpleName());
    String tableName = getClass().getSimpleName();
    try {
      createAndFillSimpleTable(connection, tableName);
      Query query = Query.select("x").from(tableName);
      dialect.restrictRownums(4, 2, query);
      String sql = query.toString();
      assertEquals("SELECT x FROM " + tableName + " LIMIT 2 OFFSET 4", sql);
      Integer[] result = DBUtil.queryScalarRowsAsArray(query.toString(), Integer.class, connection);
      assertArrayEquals(new Integer[] {4, 5}, result);
    } finally {
      DBUtil.executeUpdate("drop table " + tableName, connection);
    }
  }

  /**
   * Test offset restriction.
   *
   * @throws ConnectFailedException the connect failed exception
   * @throws SQLException           the sql exception
   */
  @Test
  public void testOffsetRestriction() throws ConnectFailedException, SQLException {
    Connection connection = H2Util.connectInMemoryDB(getClass().getSimpleName());
    String tableName = getClass().getSimpleName();
    try {
      createAndFillSimpleTable(connection, tableName);
      Query query = Query.select("x").from(tableName);
      dialect.restrictRownums(7, 3, query);
      String sql = query.toString();
      assertEquals("SELECT x FROM " + tableName + " LIMIT 3 OFFSET 7", sql);
      Integer[] result = DBUtil.queryScalarRowsAsArray(sql, Integer.class, connection);
      assertArrayEquals(new Integer[] {7, 8, 9}, result);
    } finally {
      DBUtil.executeUpdate("drop table " + tableName, connection);
    }
  }

  /**
   * Test row count restriction.
   *
   * @throws ConnectFailedException the connect failed exception
   * @throws SQLException           the sql exception
   */
  @Test
  public void testRowCountRestriction() throws ConnectFailedException, SQLException {
    Connection connection = H2Util.connectInMemoryDB(getClass().getSimpleName());
    String tableName = getClass().getSimpleName();
    try {
      createAndFillSimpleTable(connection, tableName);
      Query query = Query.select("x").from(tableName);
      dialect.restrictRownums(0, 4, query);
      String sql = query.toString();
      assertEquals("SELECT x FROM " + tableName + " LIMIT 4", sql);
      Integer[] result = DBUtil.queryScalarRowsAsArray(query.toString(), Integer.class, connection);
      assertArrayEquals(new Integer[] {0, 1, 2, 3}, result);
    } finally {
      DBUtil.executeUpdate("drop table " + tableName, connection);
    }
  }

  private static void createAndFillSimpleTable(Connection connection, String tableName) throws SQLException {
    DBUtil.executeUpdate("create table " + tableName + " ( x int )", connection);
    for (int i = 0; i < 10; i++) {
      DBUtil.executeUpdate("insert into " + tableName + " values (" + i + ")", connection);
    }
  }

}
