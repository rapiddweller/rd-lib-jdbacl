package com.rapiddweller.jdbacl.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The type Db tree model test.
 */
public class DBTreeModelTest {
  /**
   * Test get parent.
   */
  @Test
  public void testGetParent() {
    DBTreeModel dbTreeModel = new DBTreeModel(new DBSchema("Name"));
    assertNull(dbTreeModel.getParent(new DBCatalog()));
  }

  /**
   * Test is leaf.
   */
  @Test
  public void testIsLeaf() {
    DBTreeModel dbTreeModel = new DBTreeModel(new DBSchema("Name"));
    assertFalse(dbTreeModel.isLeaf(new DBCatalog()));
  }

  /**
   * Test is leaf 2.
   */
  @Test
  public void testIsLeaf2() {
    assertTrue((new DBTreeModel(new DBSchema("Name"))).isLeaf(null));
  }

  /**
   * Test get child count.
   */
  @Test
  public void testGetChildCount() {
    DBTreeModel dbTreeModel = new DBTreeModel(new DBSchema("Name"));
    assertEquals(0, dbTreeModel.getChildCount(new DBCatalog()));
  }

  /**
   * Test get child count 2.
   */
  @Test
  public void testGetChildCount2() {
    DBTreeModel dbTreeModel = new DBTreeModel(new DBSchema("Name"));
    DBTable dbTable = new DBTable("Name");
    assertEquals(0, dbTreeModel.getChildCount(dbTable));
    assertTrue(dbTable.isPKImported());
    assertEquals(0, dbTable.getColumnNames().length);
  }

  /**
   * Test get child count 3.
   */
  @Test
  public void testGetChildCount3() {
    DBTreeModel dbTreeModel = new DBTreeModel(new DBSchema("Name"));
    DBTable dbTable = new DBTable("Name", TableType.TABLE, new DBSchema("Name"));
    assertEquals(0, dbTreeModel.getChildCount(dbTable));
    assertTrue(dbTable.isPKImported());
    assertEquals(0, dbTable.getColumnNames().length);
  }

  /**
   * Test get index of child.
   */
  @Test
  public void testGetIndexOfChild() {
    DBTreeModel dbTreeModel = new DBTreeModel(new DBSchema("Name"));
    DBCatalog parent = new DBCatalog();
    assertEquals(-1, dbTreeModel.getIndexOfChild(parent, new DBCatalog()));
  }

  /**
   * Test get index of child 2.
   */
  @Test
  public void testGetIndexOfChild2() {
    DBTreeModel dbTreeModel = new DBTreeModel(new DBSchema("Name"));
    DBTable dbTable = new DBTable("Name");
    assertEquals(-1, dbTreeModel.getIndexOfChild(dbTable, new DBCatalog()));
    assertTrue(dbTable.isPKImported());
    assertEquals(0, dbTable.getColumnNames().length);
  }

  /**
   * Test get index of child 3.
   */
  @Test
  public void testGetIndexOfChild3() {
    DBTreeModel dbTreeModel = new DBTreeModel(new DBSchema("Name"));
    DBTable dbTable = new DBTable("Name", TableType.TABLE, new DBSchema("Name"));
    assertEquals(-1, dbTreeModel.getIndexOfChild(dbTable, new DBCatalog()));
    assertTrue(dbTable.isPKImported());
    assertEquals(0, dbTable.getColumnNames().length);
  }
}

