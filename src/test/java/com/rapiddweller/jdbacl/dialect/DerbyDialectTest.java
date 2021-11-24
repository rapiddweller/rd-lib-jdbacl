/*
 * (c) Copyright 2010-2021 by Volker Bergmann. All rights reserved.
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
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link DerbyDialect}.<br/><br/>
 * Created: 09.04.2010 07:42:57
 * @author Volker Bergmann
 * @since 0.6.0
 */
public class DerbyDialectTest extends DatabaseDialectTest<DerbyDialect> {

  @Test
  public void testConstructor() {
    DerbyDialect actualDerbyDialect = new DerbyDialect();
    assertEquals("derby", actualDerbyDialect.getDbType());
    assertFalse(actualDerbyDialect.isSequenceSupported());
  }

  @Test
  public void testConstructor2() {
    DerbyDialect actualDerbyDialect = new DerbyDialect(true);
    assertEquals("derby", actualDerbyDialect.getDbType());
    assertTrue(actualDerbyDialect.isSequenceSupported());
  }

  @Test
  public void testIsDefaultSchema() {
    assertFalse((new DerbyDialect()).isDefaultSchema("Schema", "User"));
    assertTrue((new DerbyDialect()).isDefaultSchema("APP", "User"));
    assertTrue((new DerbyDialect()).isDefaultSchema("'DATE('''yyyy-MM-dd''')'", "'DATE('''yyyy-MM-dd''')'"));
  }

  public DerbyDialectTest() {
    super(new DerbyDialect());
  }

  @Test
  public void testSequenceSupported() {
    assertFalse(dialect.isSequenceSupported());
  }

  @Test
  public void testFormatDate() {
    assertEquals("DATE('1971-02-03')", dialect.formatValue(DATE_19710203));
  }

  @Test
  public void testFormatDatetime() {
    assertEquals("TIMESTAMP('1971-02-03 13:14:15')", dialect.formatValue(DATETIME_19710203131415));
  }

  @Test
  public void testFormatTime() {
    assertEquals("TIME('13:14:15')", dialect.formatValue(TIME_131415));
  }

  @Test
  public void testFormatTimestamp() {
    assertEquals("'1971-02-03 13:14:15.123456789'",
        dialect.formatValue(TIMESTAMP_19710203131415123456789));
  }

  @Test
  public void testIsDeterministicPKName() {
    assertFalse(dialect.isDeterministicPKName("SQL070218051913000"));
    assertTrue(dialect.isDeterministicPKName("USER_PK"));
    assertTrue((new DerbyDialect()).isDeterministicPKName("Pk Name"));
    assertFalse((new DerbyDialect()).isDeterministicPKName("SQL999999999999999"));
  }

  @Test
  public void testIsDeterministicUKName() {
    assertFalse(dialect.isDeterministicUKName("SQL070218051912711"));
    assertTrue(dialect.isDeterministicUKName("USER_NAME_UK"));
    assertTrue((new DerbyDialect()).isDeterministicUKName("Uk Name"));
    assertFalse((new DerbyDialect()).isDeterministicUKName("SQL999999999999999"));
  }

  @Test
  public void testIsDeterministicFKName() {
    assertFalse(dialect.isDeterministicFKName("FK4561DBBFB459968D"));
    assertTrue(dialect.isDeterministicFKName("USER_ROLE_FK"));
    assertTrue((new DerbyDialect()).isDeterministicFKName("Fk Name"));
    assertFalse((new DerbyDialect()).isDeterministicFKName("FK9999999999999999"));
  }

  @Test
  public void testIsDeterministicIndexName() {
    assertFalse(dialect.isDeterministicIndexName("SQL070218051915170"));
    assertTrue(dialect.isDeterministicIndexName("USER_NAME_IDX"));
    assertTrue((new DerbyDialect()).isDeterministicIndexName("Index Name"));
    assertFalse((new DerbyDialect()).isDeterministicIndexName("SQL9"));
  }

  @Test(expected = IllegalOperationError.class)
  public void testRegex() {
    assertFalse(dialect.supportsRegex());
    dialect.regexQuery("code", false, "[A-Z]{4}");
  }

  @Test
  public void testRenderCase() {
    assertEquals("CASE WHEN condition1 THEN result1 WHEN condition2 THEN result2 ELSE result4 END AS col",
        dialect.renderCase("col", "result4", "condition1", "result1", "condition2", "result2"));
  }

}
