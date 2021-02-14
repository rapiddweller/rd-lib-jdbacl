/*
 * (c) Copyright 2011 by Volker Bergmann. All rights reserved.
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link MySQLDialect}.<br/><br/>
 * Created: 10.07.2011 10:06:28
 *
 * @author Volker Bergmann
 * @since 0.6.10
 */
public class MySQLDialectTest extends DatabaseDialectTest<MySQLDialect> {

  /**
   * Test constructor.
   */
  @Test
  public void testConstructor() {
    MySQLDialect actualMySQLDialect = new MySQLDialect();
    assertEquals("mysql", actualMySQLDialect.getSystem());
    assertFalse(actualMySQLDialect.quoteTableNames);
    assertFalse(actualMySQLDialect.isSequenceBoundarySupported());
  }

  /**
   * Instantiates a new My sql dialect test.
   */
  public MySQLDialectTest() {
    super(new MySQLDialect());
  }

  /**
   * Test sequence supported.
   */
  @Test
  public void testSequenceSupported() {
    assertFalse(dialect.isSequenceSupported());
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
    assertTrue(dialect.isDeterministicPKName("USER_PK"));
  }

  /**
   * Test is deterministic uk name.
   */
  @Test
  public void testIsDeterministicUKName() {
    assertTrue(dialect.isDeterministicUKName("USER_NAME_UK"));
  }

  /**
   * Test is deterministic fk name.
   */
  @Test
  public void testIsDeterministicFKName() {
    assertTrue(dialect.isDeterministicFKName("USER_ROLE_FK"));
  }

  /**
   * Test is deterministic index name.
   */
  @Test
  public void testIsDeterministicIndexName() {
    assertTrue(dialect.isDeterministicIndexName("USER_NAME_IDX"));
  }

  /**
   * Test supports regex.
   */
  @Test
  public void testSupportsRegex() {
    assertTrue((new MySQLDialect()).supportsRegex());
  }

  /**
   * Test regex query.
   */
  @Test
  public void testRegexQuery() {
    assertEquals("Expression NOT REGEXP 'Regex'", (new MySQLDialect()).regexQuery("Expression", true, "Regex"));
    assertEquals("Expression REGEXP 'Regex'", (new MySQLDialect()).regexQuery("Expression", false, "Regex"));
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
   * Test render case.
   */
  @Test
  public void testRenderCase() {
    assertEquals("CASE WHEN condition1 THEN result1 WHEN condition2 THEN result2 ELSE result4 END AS col",
        dialect.renderCase("col", "result4", "condition1", "result1", "condition2", "result2"));
  }

}
