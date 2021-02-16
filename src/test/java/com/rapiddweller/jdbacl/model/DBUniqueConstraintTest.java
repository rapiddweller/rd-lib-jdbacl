/*
 * (c) Copyright 2007-2010 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Tests the DBUniqueConstraint<br/>
 * <br/>
 * Created: 31.08.2007 09:22:25
 *
 * @author Volker Bergmann
 */
public class DBUniqueConstraintTest {

  /**
   * Test constructor.
   */
  @Test
  public void testConstructor() {
    DBUniqueConstraint actualDbUniqueConstraint = new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo",
        "foo", "foo");
    assertEquals("unique constraint", actualDbUniqueConstraint.getObjectType());
    assertTrue(actualDbUniqueConstraint.isNameDeterministic());
    assertEquals(3, actualDbUniqueConstraint.getColumnNames().length);
    CompositeDBObject<?> expectedTable = actualDbUniqueConstraint.owner;
    DBTable table = actualDbUniqueConstraint.getTable();
    assertSame(expectedTable, table);
    assertEquals("Name", actualDbUniqueConstraint.getName());
    assertEquals(0, table.getColumnNames().length);
  }

  /**
   * Test constructor 2.
   */
  @Test
  public void testConstructor2() {
    DBUniqueConstraint actualDbUniqueConstraint = new DBUniqueConstraint(new DBTable("Name"), "unique constraint", true,
        "foo", "foo", "foo");
    assertEquals("unique constraint", actualDbUniqueConstraint.getObjectType());
    assertTrue(actualDbUniqueConstraint.isNameDeterministic());
    assertEquals(3, actualDbUniqueConstraint.getColumnNames().length);
    CompositeDBObject<?> expectedTable = actualDbUniqueConstraint.owner;
    DBTable table = actualDbUniqueConstraint.getTable();
    assertSame(expectedTable, table);
    assertEquals("unique constraint", actualDbUniqueConstraint.getName());
    assertEquals(0, table.getColumnNames().length);
  }

  /**
   * Test get column names.
   */
  @Test
  public void testGetColumnNames() {
    assertEquals(3,
        (new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo")).getColumnNames().length);
  }

  /**
   * Test add column name.
   */
  @Test
  public void testAddColumnName() {
    DBUniqueConstraint dbUniqueConstraint = new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo",
        "foo");
    dbUniqueConstraint.addColumnName("Column Name");
    assertEquals(4, dbUniqueConstraint.getColumnNames().length);
  }

  /**
   * Test is identical.
   */
  @Test
  public void testIsIdentical() {
    DBUniqueConstraint dbUniqueConstraint = new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo",
        "foo");
    assertFalse(dbUniqueConstraint.isIdentical(new DBCatalog()));
  }

  /**
   * Test is identical 2.
   */
  @Test
  public void testIsIdentical2() {
    assertFalse((new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo")).isIdentical(null));
  }

  /**
   * Test to string.
   */
  @Test
  public void testToString() {
    DBTable table = new DBTable("tablename");
    DBUniqueConstraint constraint = new DBUniqueConstraint(table, "constraintname", false, "column1", "column2");
    assertEquals("CONSTRAINT constraintname UNIQUE (column1, column2)", constraint.toString());
  }

  /**
   * Test to string 2.
   */
  @Test
  public void testToString2() {
    assertEquals("CONSTRAINT Name UNIQUE (foo, foo, foo)",
        (new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo")).toString());
  }

  /**
   * Test equals.
   */
  @Test
  public void testEquals() {
    DBTable table = new DBTable("tablename");
    // simple checks
    DBUniqueConstraint uc1 = new DBUniqueConstraint(table, "uc1", false, "col1");
    assertFalse(uc1.equals(null));
    assertFalse(uc1.equals(""));
    assertTrue(uc1.equals(uc1));
    // real comparisons
    DBUniqueConstraint uc2 = new DBUniqueConstraint(table, "uc2", false, "col2");
    DBUniqueConstraint uc3 = new DBUniqueConstraint(table, "uc3", false, "col1", "col2");
    assertFalse(uc1.equals(uc2));
    assertFalse(uc1.equals(uc3));
    assertNotEquals(uc3, uc1);
  }

}
