package com.rapiddweller.jdbacl;

import com.rapiddweller.jdbacl.model.DBCatalog;
import com.rapiddweller.jdbacl.model.DBSchema;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.model.TableType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The type Database tree model test.
 */
public class DatabaseTreeModelTest {
  /**
   * Test get parent.
   */
  @Test
  public void testGetParent() {
    DatabaseTreeModel databaseTreeModel = new DatabaseTreeModel(null);
    assertNull(databaseTreeModel.getParent(new DBCatalog()));
  }

  /**
   * Test is leaf.
   */
  @Test
  public void testIsLeaf() {
    DatabaseTreeModel databaseTreeModel = new DatabaseTreeModel(null);
    assertFalse(databaseTreeModel.isLeaf(new DBCatalog()));
  }

  /**
   * Test is leaf 2.
   */
  @Test
  public void testIsLeaf2() {
    assertTrue((new DatabaseTreeModel(null)).isLeaf(null));
  }

  /**
   * Test get child count.
   */
  @Test
  public void testGetChildCount() {
    DatabaseTreeModel databaseTreeModel = new DatabaseTreeModel(null);
    assertEquals(0, databaseTreeModel.getChildCount(new DBCatalog()));
  }

  /**
   * Test get child count 2.
   */
  @Test
  public void testGetChildCount2() {
    DatabaseTreeModel databaseTreeModel = new DatabaseTreeModel(null);
    DBTable dbTable = new DBTable("Name");
    assertEquals(0, databaseTreeModel.getChildCount(dbTable));
    assertTrue(dbTable.isPKImported());
    assertEquals(0, dbTable.getColumnNames().length);
  }

  /**
   * Test get child count 3.
   */
  @Test
  public void testGetChildCount3() {
    DatabaseTreeModel databaseTreeModel = new DatabaseTreeModel(null);
    DBTable dbTable = new DBTable("Name", TableType.TABLE, new DBSchema("Name"));
    assertEquals(0, databaseTreeModel.getChildCount(dbTable));
    assertTrue(dbTable.isPKImported());
    assertEquals(0, dbTable.getColumnNames().length);
  }

  /**
   * Test get index of child.
   */
  @Test
  public void testGetIndexOfChild() {
    DatabaseTreeModel databaseTreeModel = new DatabaseTreeModel(null);
    DBCatalog parent = new DBCatalog();
    assertEquals(-1, databaseTreeModel.getIndexOfChild(parent, new DBCatalog()));
  }

  /**
   * Test get index of child 2.
   */
  @Test
  public void testGetIndexOfChild2() {
    DatabaseTreeModel databaseTreeModel = new DatabaseTreeModel(null);
    DBTable dbTable = new DBTable("Name");
    assertEquals(-1, databaseTreeModel.getIndexOfChild(dbTable, new DBCatalog()));
    assertTrue(dbTable.isPKImported());
    assertEquals(0, dbTable.getColumnNames().length);
  }

  /**
   * Test get index of child 3.
   */
  @Test
  public void testGetIndexOfChild3() {
    DatabaseTreeModel databaseTreeModel = new DatabaseTreeModel(null);
    DBTable dbTable = new DBTable("Name", TableType.TABLE, new DBSchema("Name"));
    assertEquals(-1, databaseTreeModel.getIndexOfChild(dbTable, new DBCatalog()));
    assertTrue(dbTable.isPKImported());
    assertEquals(0, dbTable.getColumnNames().length);
  }
}

