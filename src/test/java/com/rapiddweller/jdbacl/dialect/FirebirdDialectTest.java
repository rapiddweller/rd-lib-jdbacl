/*
 * (c) Copyright 2009-2021 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.common.exception.IllegalOperationError;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.DatabaseTestUtil;
import com.rapiddweller.jdbacl.model.DBSequence;
import org.junit.Test;

import java.math.BigInteger;
import java.sql.Connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link FirebirdDialect}.<br/><br/>
 * Created: 10.11.2009 18:18:04
 * @author Volker Bergmann
 * @since 0.6.0
 */
public class FirebirdDialectTest extends DatabaseDialectTest<FirebirdDialect> {

  @Test
  public void testConstructor() {
    FirebirdDialect actualFirebirdDialect = new FirebirdDialect();
    assertEquals("firebird", actualFirebirdDialect.getDbType());
    assertTrue(actualFirebirdDialect.isSequenceSupported());
  }

  public FirebirdDialectTest() {
    super(new FirebirdDialect());
  }

  @Test
  public void testFormatDate() {
    assertEquals("'1971-02-03'", dialect.formatValue(DATE_19710203));
  }

  @Test
  public void testFormatDatetime() {
    assertEquals("'1971-02-03 13:14:15'", dialect.formatValue(DATETIME_19710203131415));
  }

  @Test
  public void testFormatTime() {
    assertEquals("'13:14:15'", dialect.formatValue(TIME_131415));
  }

  @Test
  public void testFormatTimestamp() {
    assertEquals("'1971-02-03 13:14:15.123456789'",
        dialect.formatValue(TIMESTAMP_19710203131415123456789));
  }

  @Test
  public void testIsDeterministicPKName() {
    assertFalse(dialect.isDeterministicPKName("INTEG_3486"));
    assertTrue(dialect.isDeterministicPKName("USER_PK"));
    assertTrue((new FirebirdDialect()).isDeterministicPKName("Pk Name"));
    assertFalse((new FirebirdDialect()).isDeterministicPKName("INTEG_9"));
  }

  @Test
  public void testIsDeterministicUKName() {
    assertFalse(dialect.isDeterministicUKName("RDB$749"));
    assertTrue(dialect.isDeterministicUKName("USER_NAME_UK"));
    assertTrue((new FirebirdDialect()).isDeterministicUKName("Uk Name"));
    assertFalse((new FirebirdDialect()).isDeterministicUKName("RDB$U"));
  }

  @Test
  public void testIsDeterministicFKName() {
    assertFalse(dialect.isDeterministicFKName("INTEG_3487"));
    assertTrue(dialect.isDeterministicFKName("USER_ROLE_FK"));
    assertTrue((new FirebirdDialect()).isDeterministicFKName("Fk Name"));
    assertFalse((new FirebirdDialect()).isDeterministicFKName("INTEG_9"));
  }

  @Test
  public void testIsDeterministicIndexName() {
    assertFalse(dialect.isDeterministicIndexName("RDB$749"));
    assertTrue(dialect.isDeterministicIndexName("USER_NAME_IDX"));
    assertTrue((new FirebirdDialect()).isDeterministicIndexName("Index Name"));
    assertFalse((new FirebirdDialect()).isDeterministicIndexName("RDB$U"));
  }

  @Test
  public void testSupportsRegex() {
    assertFalse(dialect.supportsRegex());
  }

  @Test(expected = IllegalOperationError.class)
  public void testRegex() {
    dialect.regexQuery("code", false, "[A-Z]{4}");
  }

  @Test
  public void testRenderCreateSequence() {
    assertEquals("CREATE GENERATOR my_seq", dialect.renderCreateSequence(new DBSequence("my_seq", null)));
    assertEquals("CREATE GENERATOR my_seq; SET GENERATOR my_seq TO 9;",
        dialect.renderCreateSequence(createConfiguredSequence()));
    assertEquals("CREATE GENERATOR Name", (new FirebirdDialect()).renderCreateSequence("Name"));
  }

  @Test
  public void testRenderCreateSequence2() {
    DBSequence sequence = new DBSequence("Name", null);
    assertEquals("CREATE GENERATOR Name", (new FirebirdDialect()).renderCreateSequence(sequence));
  }

  @Test
  public void testRenderCreateSequence3() {
    DBSequence dbSequence = new DBSequence("Name", null);
    dbSequence.setStart(null);
    assertEquals("CREATE GENERATOR Name", (new FirebirdDialect()).renderCreateSequence(dbSequence));
  }

  @Test
  public void testRenderCreateSequence4() {
    DBSequence dbSequence = new DBSequence("Name", null);
    dbSequence.setStart(BigInteger.valueOf(42L));
    assertEquals("CREATE GENERATOR Name; SET GENERATOR Name TO 41;",
        (new FirebirdDialect()).renderCreateSequence(dbSequence));
  }

  @Test
  public void testRenderDropSequence() {
    assertEquals("drop generator Sequence Name", (new FirebirdDialect()).renderDropSequence("Sequence Name"));
  }

  @Test
  public void testRenderFetchSequenceValue() {
    assertEquals("select gen_id(Sequence Name, 1) from RDB$DATABASE;",
        (new FirebirdDialect()).renderFetchSequenceValue("Sequence Name"));
  }

  @Test
  public void testSequencesOnline() throws Exception {
    testSequencesOnline("firebird");
  }

  @Test // requires a Firebird installation configured as environment named 'firebird'
  public void testSetNextSequenceValue() throws Exception {
    if (DatabaseTestUtil.getConnectData("firebird", ".") == null) {
      logger.warn("Skipping test " + getClass() + ".testSetNextSequenceValue() since there is no 'firebird' environment defined or online");
      return;
    }
    Connection connection = DBUtil.connect("firebird", ".", false);
    String sequenceName = getClass().getSimpleName();
    DBUtil.executeUpdate("create sequence " + sequenceName, connection);
    dialect.setNextSequenceValue(sequenceName, 123, connection);
    String seqValQuery = dialect.renderFetchSequenceValue(sequenceName);
    assertEquals(123L, DBUtil.queryScalar(seqValQuery, connection));
    DBUtil.executeUpdate("drop sequence " + sequenceName, connection);
  }

  @Test
  public void testRenderSetSequenceValue() {
    assertEquals("SET GENERATOR Sequence Name TO 41",
        (new FirebirdDialect()).renderSetSequenceValue("Sequence Name", 42L));
  }

  @Test
  public void testRenderCase() {
    assertEquals("CASE WHEN condition1 THEN result1 WHEN condition2 THEN result2 ELSE result4 END AS col",
        dialect.renderCase("col", "result4", "condition1", "result1", "condition2", "result2"));
  }

}
