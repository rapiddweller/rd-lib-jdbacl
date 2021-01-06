package com.rapiddweller.jdbacl.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DBTreeModelTest {
    @Test
    public void testGetParent() {
        DBTreeModel dbTreeModel = new DBTreeModel(new DBSchema("Name"));
        assertNull(dbTreeModel.getParent(new DBCatalog()));
    }

    @Test
    public void testIsLeaf() {
        DBTreeModel dbTreeModel = new DBTreeModel(new DBSchema("Name"));
        assertFalse(dbTreeModel.isLeaf(new DBCatalog()));
    }

    @Test
    public void testIsLeaf2() {
        assertTrue((new DBTreeModel(new DBSchema("Name"))).isLeaf(null));
    }

    @Test
    public void testGetChildCount() {
        DBTreeModel dbTreeModel = new DBTreeModel(new DBSchema("Name"));
        assertEquals(0, dbTreeModel.getChildCount(new DBCatalog()));
    }

    @Test
    public void testGetChildCount2() {
        DBTreeModel dbTreeModel = new DBTreeModel(new DBSchema("Name"));
        DBTable dbTable = new DBTable("Name");
        assertEquals(0, dbTreeModel.getChildCount(dbTable));
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetChildCount3() {
        DBTreeModel dbTreeModel = new DBTreeModel(new DBSchema("Name"));
        DBTable dbTable = new DBTable("Name", TableType.TABLE, new DBSchema("Name"));
        assertEquals(0, dbTreeModel.getChildCount(dbTable));
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetIndexOfChild() {
        DBTreeModel dbTreeModel = new DBTreeModel(new DBSchema("Name"));
        DBCatalog parent = new DBCatalog();
        assertEquals(-1, dbTreeModel.getIndexOfChild(parent, new DBCatalog()));
    }

    @Test
    public void testGetIndexOfChild2() {
        DBTreeModel dbTreeModel = new DBTreeModel(new DBSchema("Name"));
        DBTable dbTable = new DBTable("Name");
        assertEquals(-1, dbTreeModel.getIndexOfChild(dbTable, new DBCatalog()));
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetIndexOfChild3() {
        DBTreeModel dbTreeModel = new DBTreeModel(new DBSchema("Name"));
        DBTable dbTable = new DBTable("Name", TableType.TABLE, new DBSchema("Name"));
        assertEquals(-1, dbTreeModel.getIndexOfChild(dbTable, new DBCatalog()));
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
    }
}

