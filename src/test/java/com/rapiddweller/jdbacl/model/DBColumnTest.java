/*
 * (c) Copyright 2006-2012 by Volker Bergmann. All rights reserved.
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

import java.sql.Types;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link DBColumn}.<br/><br/>
 * Created: 06.01.2007 10:41:46
 *
 * @author Volker Bergmann
 */
public class DBColumnTest {

  /**
   * Test constructor.
   */
  @Test
  public void testConstructor() {
    DBColumn actualDbColumn = new DBColumn("Name", new DBTable("Name"), 1, "Type And Size");
    assertFalse(actualDbColumn.isUnique());
    assertEquals("Name", actualDbColumn.getName());
    assertFalse(actualDbColumn.isVersionColumn());
    assertFalse(actualDbColumn.isPKComponent());
    assertNull(actualDbColumn.getDoc());
    assertNull(actualDbColumn.getSize());
    String[] columnNames = ((DBTable) actualDbColumn.getOwner()).getColumnNames();
    assertEquals("column", actualDbColumn.getObjectType());
    assertNull(actualDbColumn.getDefaultValue());
    assertEquals("Name : TYPE AND SIZE", actualDbColumn.toString());
    assertNull(actualDbColumn.getNotNullConstraint());
    assertNull(actualDbColumn.getFractionDigits());
    assertEquals(1, columnNames.length);
  }

  /**
   * Test constructor 2.
   */
  @Test
  public void testConstructor2() {
    DBColumn actualDbColumn = new DBColumn("Name", null, 1, "Type And Size");
    assertNull(actualDbColumn.getSize());
    assertEquals("Name : TYPE AND SIZE", actualDbColumn.toString());
    assertNull(actualDbColumn.getOwner());
    assertNull(actualDbColumn.getDoc());
    List<DBUniqueConstraint> expectedUkConstraints = actualDbColumn.ukConstraints;
    assertSame(expectedUkConstraints, actualDbColumn.getUkConstraints());
    assertEquals("column", actualDbColumn.getObjectType());
    assertTrue(actualDbColumn.isNullable());
    assertNull(actualDbColumn.getDefaultValue());
    assertEquals("Name", actualDbColumn.getName());
    assertNull(actualDbColumn.getFractionDigits());
    assertFalse(actualDbColumn.isVersionColumn());
  }

  /**
   * Test constructor 3.
   */
  @Test
  public void testConstructor3() {
    DBColumn actualDbColumn = new DBColumn("Name", new DBTable("Name"), 1, "TYPE AND SIZE");
    assertFalse(actualDbColumn.isUnique());
    assertEquals("Name", actualDbColumn.getName());
    assertFalse(actualDbColumn.isVersionColumn());
    assertFalse(actualDbColumn.isPKComponent());
    assertNull(actualDbColumn.getDoc());
    assertNull(actualDbColumn.getSize());
    String[] columnNames = ((DBTable) actualDbColumn.getOwner()).getColumnNames();
    assertEquals("column", actualDbColumn.getObjectType());
    assertNull(actualDbColumn.getDefaultValue());
    assertEquals("Name : TYPE AND SIZE", actualDbColumn.toString());
    assertNull(actualDbColumn.getNotNullConstraint());
    assertNull(actualDbColumn.getFractionDigits());
    assertEquals(1, columnNames.length);
  }

  /**
   * Test constructor 4.
   */
  @Test
  public void testConstructor4() {
    DBTable table = new DBTable("Name");
    DBColumn actualDbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
    assertFalse(actualDbColumn.isUnique());
    assertEquals("Name", actualDbColumn.getName());
    assertFalse(actualDbColumn.isVersionColumn());
    assertFalse(actualDbColumn.isPKComponent());
    assertNull(actualDbColumn.getDoc());
    assertNull(actualDbColumn.getSize());
    String[] columnNames = ((DBTable) actualDbColumn.getOwner()).getColumnNames();
    assertEquals("column", actualDbColumn.getObjectType());
    assertNull(actualDbColumn.getDefaultValue());
    assertEquals("Name : BLOB", actualDbColumn.toString());
    assertNull(actualDbColumn.getNotNullConstraint());
    assertNull(actualDbColumn.getFractionDigits());
    assertEquals(1, columnNames.length);
  }

  /**
   * Test constructor 5.
   */
  @Test
  public void testConstructor5() {
    DBColumn actualDbColumn = new DBColumn("Name", null, DBDataType.getInstance("BLOB"));
    assertNull(actualDbColumn.getSize());
    assertEquals("Name : BLOB", actualDbColumn.toString());
    assertNull(actualDbColumn.getOwner());
    assertNull(actualDbColumn.getDoc());
    List<DBUniqueConstraint> expectedUkConstraints = actualDbColumn.ukConstraints;
    assertSame(expectedUkConstraints, actualDbColumn.getUkConstraints());
    assertEquals("column", actualDbColumn.getObjectType());
    assertTrue(actualDbColumn.isNullable());
    assertNull(actualDbColumn.getDefaultValue());
    assertEquals("Name", actualDbColumn.getName());
    assertNull(actualDbColumn.getFractionDigits());
    assertFalse(actualDbColumn.isVersionColumn());
  }

  /**
   * Test constructor 6.
   */
  @Test
  public void testConstructor6() {
    DBTable table = new DBTable("Name");
    DBColumn actualDbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"), 3);
    assertFalse(actualDbColumn.isUnique());
    assertEquals("Name", actualDbColumn.getName());
    assertFalse(actualDbColumn.isVersionColumn());
    assertFalse(actualDbColumn.isPKComponent());
    assertNull(actualDbColumn.getDoc());
    assertEquals(3, actualDbColumn.getSize().intValue());
    String[] columnNames = ((DBTable) actualDbColumn.getOwner()).getColumnNames();
    assertEquals("column", actualDbColumn.getObjectType());
    assertNull(actualDbColumn.getDefaultValue());
    assertEquals("Name : BLOB", actualDbColumn.toString());
    assertNull(actualDbColumn.getNotNullConstraint());
    assertNull(actualDbColumn.getFractionDigits());
    assertEquals(1, columnNames.length);
  }

  /**
   * Test constructor 7.
   */
  @Test
  public void testConstructor7() {
    DBColumn actualDbColumn = new DBColumn("Name", null, DBDataType.getInstance("BLOB"), 3);
    assertEquals(3, actualDbColumn.getSize().intValue());
    assertEquals("Name : BLOB", actualDbColumn.toString());
    assertNull(actualDbColumn.getOwner());
    assertNull(actualDbColumn.getDoc());
    List<DBUniqueConstraint> expectedUkConstraints = actualDbColumn.ukConstraints;
    assertSame(expectedUkConstraints, actualDbColumn.getUkConstraints());
    assertEquals("column", actualDbColumn.getObjectType());
    assertTrue(actualDbColumn.isNullable());
    assertNull(actualDbColumn.getDefaultValue());
    assertEquals("Name", actualDbColumn.getName());
    assertNull(actualDbColumn.getFractionDigits());
    assertFalse(actualDbColumn.isVersionColumn());
  }

  /**
   * Test constructor 8.
   */
  @Test
  public void testConstructor8() {
    DBTable table = new DBTable("Name");
    DBColumn actualDbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"), 3, 1);
    assertFalse(actualDbColumn.isUnique());
    assertEquals("Name", actualDbColumn.getName());
    assertFalse(actualDbColumn.isVersionColumn());
    assertFalse(actualDbColumn.isPKComponent());
    assertNull(actualDbColumn.getDoc());
    assertEquals(3, actualDbColumn.getSize().intValue());
    String[] columnNames = ((DBTable) actualDbColumn.getOwner()).getColumnNames();
    assertEquals("column", actualDbColumn.getObjectType());
    assertNull(actualDbColumn.getDefaultValue());
    assertEquals("Name : BLOB", actualDbColumn.toString());
    assertNull(actualDbColumn.getNotNullConstraint());
    assertEquals(1, actualDbColumn.getFractionDigits().intValue());
    assertEquals(1, columnNames.length);
  }

  /**
   * Test constructor 9.
   */
  @Test
  public void testConstructor9() {
    DBColumn actualDbColumn = new DBColumn("Name", null, DBDataType.getInstance("BLOB"), 3, 1);
    assertEquals(3, actualDbColumn.getSize().intValue());
    assertEquals("Name : BLOB", actualDbColumn.toString());
    assertNull(actualDbColumn.getOwner());
    assertNull(actualDbColumn.getDoc());
    List<DBUniqueConstraint> expectedUkConstraints = actualDbColumn.ukConstraints;
    assertSame(expectedUkConstraints, actualDbColumn.getUkConstraints());
    assertEquals("column", actualDbColumn.getObjectType());
    assertTrue(actualDbColumn.isNullable());
    assertNull(actualDbColumn.getDefaultValue());
    assertEquals("Name", actualDbColumn.getName());
    assertEquals(1, actualDbColumn.getFractionDigits().intValue());
    assertFalse(actualDbColumn.isVersionColumn());
  }

  /**
   * Test set size.
   */
  @Test
  public void testSetSize() {
    DBTable table = new DBTable("Name");
    DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
    dbColumn.setSize(3);
    assertEquals(3, dbColumn.getSize().intValue());
  }

  /**
   * Test set fraction digits.
   */
  @Test
  public void testSetFractionDigits() {
    DBTable table = new DBTable("Name");
    DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
    dbColumn.setFractionDigits(1);
    assertEquals(1, dbColumn.getFractionDigits().intValue());
  }

  /**
   * Test set default value.
   */
  @Test
  public void testSetDefaultValue() {
    DBTable table = new DBTable("Name");
    DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
    dbColumn.setDefaultValue("Default Value");
    assertEquals("Default Value", dbColumn.getDefaultValue());
  }

  /**
   * Test is unique.
   */
  @Test
  public void testIsUnique() {
    DBTable table = new DBTable("Name");
    assertFalse((new DBColumn("Name", table, DBDataType.getInstance("BLOB"))).isUnique());
  }

  /**
   * Test is unique 2.
   */
  @Test
  public void testIsUnique2() {
    DBTable table = new DBTable("Name");
    DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
    dbColumn.addUkConstraint(new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
    assertFalse(dbColumn.isUnique());
  }

  /**
   * Test is unique 3.
   */
  @Test
  public void testIsUnique3() {
    DBTable table = new DBTable("Name");
    DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
    dbColumn.addUkConstraint(new DBUniqueConstraint(new DBTable("Name"), "Name", true, "Column Names"));
    assertTrue(dbColumn.isUnique());
  }

  /**
   * Test is pk component.
   */
  @Test
  public void testIsPKComponent() {
    DBTable table = new DBTable("Name");
    DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
    assertFalse(dbColumn.isPKComponent());
    assertTrue(((DBTable) dbColumn.getOwner()).isPKImported());
  }

  /**
   * Test is pk component 2.
   */
  @Test
  public void testIsPKComponent2() {
    DBTable dbTable = new DBTable("Name");
    DBTable owner = new DBTable("Name");
    dbTable.addForeignKey(
        new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"));
    assertFalse((new DBColumn("Name", dbTable, DBDataType.getInstance("BLOB"))).isPKComponent());
  }

  /**
   * Test is pk component 3.
   */
  @Test
  public void testIsPKComponent3() {
    DBTable dbTable = new DBTable("Name");
    dbTable.setPrimaryKey(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
    assertFalse((new DBColumn("Name", dbTable, DBDataType.getInstance("BLOB"))).isPKComponent());
  }

  /**
   * Test is pk component 4.
   */
  @Test
  public void testIsPKComponent4() {
    DBTable dbTable = new DBTable("Name");
    dbTable.setPrimaryKey(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
    assertTrue((new DBColumn("foo", dbTable, DBDataType.getInstance("BLOB"))).isPKComponent());
  }

  /**
   * Test add uk constraint.
   */
  @Test
  public void testAddUkConstraint() {
    DBTable table = new DBTable("Name");
    DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
    dbColumn.addUkConstraint(new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
    assertFalse(dbColumn.isUnique());
  }

  /**
   * Test is nullable.
   */
  @Test
  public void testIsNullable() {
    DBTable table = new DBTable("Name");
    assertTrue((new DBColumn("Name", table, DBDataType.getInstance("BLOB"))).isNullable());
  }

  /**
   * Test set nullable.
   */
  @Test
  public void testSetNullable() {
    DBTable table = new DBTable("Name");
    DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
    dbColumn.setNullable(true);
    assertNull(dbColumn.getNotNullConstraint());
  }

  /**
   * Test set nullable 2.
   */
  @Test
  public void testSetNullable2() {
    DBTable table = new DBTable("Name");
    DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
    dbColumn.setNullable(false);
    DBNotNullConstraint notNullConstraint = dbColumn.getNotNullConstraint();
    assertFalse(dbColumn.isNullable());
    assertEquals("Name_Name_NOT_NULL", notNullConstraint.getName());
    assertEquals("DBNotNullConstraint[Name[Name]]", notNullConstraint.toString());
    assertEquals("not null constraint", notNullConstraint.getObjectType());
    assertTrue(notNullConstraint.isNameDeterministic());
    assertEquals(1, notNullConstraint.getColumnNames().length);
  }

  /**
   * Test set version column.
   */
  @Test
  public void testSetVersionColumn() {
    DBTable table = new DBTable("Name");
    DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
    dbColumn.setVersionColumn(true);
    assertTrue(dbColumn.isVersionColumn());
  }

  /**
   * Test is integer type.
   */
  @Test
  public void testIsIntegerType() {
    DBTable table = new DBTable("Name");
    assertFalse((new DBColumn("Name", table, DBDataType.getInstance("BLOB"))).isIntegerType());
  }

  /**
   * Test is integer type 2.
   */
  @Test
  public void testIsIntegerType2() {
    assertFalse((new DBColumn("Name", new DBTable("Name"), 1, "Type And Size")).isIntegerType());
  }

  /**
   * Test is integer type 3.
   */
  @Test
  public void testIsIntegerType3() {
    assertTrue((new DBColumn("Name", new DBTable("Name"), 3, "Type And Size")).isIntegerType());
  }

  /**
   * Test is integer type 4.
   */
  @Test
  public void testIsIntegerType4() {
    DBColumn dbColumn = new DBColumn("Name", new DBTable("Name"), 3, "Type And Size");
    dbColumn.setFractionDigits(0);
    assertTrue(dbColumn.isIntegerType());
  }

  /**
   * Test is integer type 5.
   */
  @Test
  public void testIsIntegerType5() {
    DBColumn dbColumn = new DBColumn("Name", new DBTable("Name"), 3, "Type And Size");
    dbColumn.setFractionDigits(6);
    assertFalse(dbColumn.isIntegerType());
  }

  /**
   * Test get foreign key constraint.
   */
  @Test
  public void testGetForeignKeyConstraint() {
    DBTable table = new DBTable("Name");
    DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
    assertNull(dbColumn.getForeignKeyConstraint());
    assertTrue(((DBTable) dbColumn.getOwner()).isPKImported());
  }

  /**
   * Test get foreign key constraint 2.
   */
  @Test
  public void testGetForeignKeyConstraint2() {
    DBTable dbTable = new DBTable("Name");
    DBTable owner = new DBTable("Name");
    dbTable.addForeignKey(
        new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"));
    assertNull((new DBColumn("Name", dbTable, DBDataType.getInstance("BLOB"))).getForeignKeyConstraint());
  }

  /**
   * Test get foreign key constraint 3.
   */
  @Test
  public void testGetForeignKeyConstraint3() {
    DBTable dbTable = new DBTable("Name");
    dbTable.setPrimaryKey(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
    assertNull((new DBColumn("Name", dbTable, DBDataType.getInstance("BLOB"))).getForeignKeyConstraint());
  }

  /**
   * Test get foreign key constraint 4.
   */
  @Test
  public void testGetForeignKeyConstraint4() {
    DBTable dbTable = new DBTable("Name");
    DBTable owner = new DBTable("Name");
    DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
        new DBTable("Name"), "Referee Column Name");
    dbTable.addForeignKey(dbForeignKeyConstraint);
    assertSame(dbForeignKeyConstraint,
        (new DBColumn("Fk Column Name", dbTable, DBDataType.getInstance("BLOB"))).getForeignKeyConstraint());
  }

  /**
   * Test equals.
   */
  @Test
  public void testEquals() {
    DBTable table = new DBTable("Name");
    assertFalse((new DBColumn("Name", table, DBDataType.getInstance("BLOB"))).equals("obj"));
  }

  /**
   * Test equals 2.
   */
  @Test
  public void testEquals2() {
    DBTable table = new DBTable("Name");
    assertNotEquals(null, (new DBColumn("Name", table, DBDataType.getInstance("BLOB"))));
  }

  /**
   * Test hash code.
   */
  @Test
  public void testHashCode() {
    DBTable table = new DBTable("Name");
    assertEquals(-1146704762, (new DBColumn("Name", table, DBDataType.getInstance("BLOB"))).hashCode());
  }

  /**
   * Test to string.
   */
  @Test
  public void testToString() {
    assertEquals("Column formatting failed", "ID : NUMBER(11,2)",
        new DBColumn("ID", null, DBDataType.getInstance(Types.DECIMAL, "NUMBER"), 11, 2).toString());
  }

  /**
   * Test to string 2.
   */
  @Test
  public void testToString2() {
    DBTable table = new DBTable("Name");
    assertEquals("Name : BLOB", (new DBColumn("Name", table, DBDataType.getInstance("BLOB"))).toString());
  }

  /**
   * Test is identical.
   */
  @Test
  public void testIsIdentical() {
    DBTable table = new DBTable("Name");
    DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
    assertFalse(dbColumn.isIdentical(new DBCatalog()));
  }

  /**
   * Test is identical 2.
   */
  @Test
  public void testIsIdentical2() {
    DBTable table = new DBTable("Name");
    assertFalse((new DBColumn("Name", table, DBDataType.getInstance("BLOB"))).isIdentical(null));
  }

  /**
   * Test is equivalent.
   */
  @Test
  public void testIsEquivalent() {
    DBTable table = new DBTable("Name");
    DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
    assertFalse(dbColumn.isEquivalent(new DBCatalog()));
  }

  /**
   * Test is equivalent 2.
   */
  @Test
  public void testIsEquivalent2() {
    DBTable table = new DBTable("Name");
    assertFalse((new DBColumn("Name", table, DBDataType.getInstance("BLOB"))).isEquivalent(null));
  }

}
