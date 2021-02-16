package com.rapiddweller.jdbacl.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * The type Db unique index test.
 */
public class DBUniqueIndexTest {
  /**
   * Test constructor.
   */
  @Test
  public void testConstructor() {
    DBUniqueIndex actualDbUniqueIndex = new DBUniqueIndex("Name", true,
        new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
    assertEquals("index", actualDbUniqueIndex.getObjectType());
    assertTrue(actualDbUniqueIndex.isNameDeterministic());
    assertEquals(3, actualDbUniqueIndex.getColumnNames().length);
    CompositeDBObject<?> actualOwner = actualDbUniqueIndex.getOwner();
    assertSame(actualDbUniqueIndex.getTable(), actualOwner);
    assertEquals("Name", actualDbUniqueIndex.getName());
  }

  /**
   * Test get table.
   */
  @Test
  public void testGetTable() {
    DBUniqueIndex dbUniqueIndex = new DBUniqueIndex("Name", true,
        new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
    assertSame(dbUniqueIndex.owner, dbUniqueIndex.getTable());
  }

  /**
   * Test get column names.
   */
  @Test
  public void testGetColumnNames() {
    assertEquals(3, (new DBUniqueIndex("Name", true,
        new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"))).getColumnNames().length);
  }

  /**
   * Test add column name.
   */
  @Test
  public void testAddColumnName() {
    DBUniqueIndex dbUniqueIndex = new DBUniqueIndex("Name", true,
        new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
    dbUniqueIndex.addColumnName("Column Name");
    assertEquals(4, ((DBUniqueConstraint) dbUniqueIndex.getTable().getComponents().get(0)).getColumnNames().length);
  }

  /**
   * Test is identical.
   */
  @Test
  public void testIsIdentical() {
    DBUniqueIndex dbUniqueIndex = new DBUniqueIndex("Name", true,
        new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
    assertFalse(dbUniqueIndex.isIdentical(new DBCatalog()));
  }

  /**
   * Test is identical 2.
   */
  @Test
  public void testIsIdentical2() {
    assertFalse((new DBUniqueIndex("Name", true,
        new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"))).isIdentical(null));
  }
}

