package com.rapiddweller.jdbacl.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class DBSchemaTest {
    @Test
    public void testConstructor() {
        DBSchema actualDbSchema = new DBSchema("Name");
        assertEquals("schema", actualDbSchema.getObjectType());
        assertNull(actualDbSchema.getCatalog());
        assertEquals("Name", actualDbSchema.toString());
    }

    @Test
    public void testConstructor2() {
        DBSchema actualDbSchema = new DBSchema("Name", new DBCatalog());
        assertEquals("schema", actualDbSchema.getObjectType());
        assertEquals("Name", actualDbSchema.toString());
        assertNull(actualDbSchema.getDatabase());
    }

    @Test
    public void testConstructor3() {
        DBSchema actualDbSchema = new DBSchema("Name", null);
        assertEquals("schema", actualDbSchema.getObjectType());
        assertNull(actualDbSchema.getCatalog());
        assertEquals("Name", actualDbSchema.toString());
    }

    @Test
    public void testGetDatabase() {
        assertNull((new DBSchema("Name", new DBCatalog())).getDatabase());
    }

    @Test
    public void testGetCatalog() {
        assertNull((new DBSchema("Name")).getCatalog());
    }

    @Test
    public void testSetCatalog() {
        DBSchema dbSchema = new DBSchema("Name");
        dbSchema.setCatalog(new DBCatalog());
        assertNull(dbSchema.getDatabase());
    }

    @Test
    public void testGetTables() {
        assertTrue((new DBSchema("Name")).getTables().isEmpty());
        assertTrue((new DBSchema("Name")).getTables(true).isEmpty());
    }

    @Test
    public void testGetTable() {
        assertNull((new DBSchema("Name")).getTable("Table Name"));
    }

    @Test
    public void testAddTable() {
        DBSchema dbSchema = new DBSchema("Name");
        dbSchema.addTable(new DBTable("Name"));
        assertEquals(1, dbSchema.getComponents().size());
    }

    @Test
    public void testRemoveTable() {
        DBSchema dbSchema = new DBSchema("Name");
        dbSchema.removeTable(new DBTable("Name"));
        assertTrue(dbSchema.getComponents().isEmpty());
    }

    @Test
    public void testGetSequences() {
        DBSchema dbSchema = new DBSchema("Name", new DBCatalog("Name", AbstractModelTest.createTestModel()));
        assertTrue(dbSchema.getSequences(true).isEmpty());
        assertTrue(dbSchema.getDatabase().isSequencesImported());
    }

    @Test
    public void testGetTriggers() {
        DBSchema dbSchema = new DBSchema("Name", new DBCatalog("Name", AbstractModelTest.createTestModel()));
        assertTrue(dbSchema.getTriggers().isEmpty());
        assertTrue(dbSchema.getDatabase().isTriggersImported());
    }

    @Test
    public void testAddTrigger() {
        DBSchema dbSchema = new DBSchema("Name", new DBCatalog("Name", AbstractModelTest.createTestModel()));
        dbSchema.addTrigger(new DBTrigger("Name", null));
        assertEquals(1, dbSchema.getComponents().size());
    }

    @Test
    public void testReceiveTrigger() {
        DBSchema dbSchema = new DBSchema("Name");
        dbSchema.receiveTrigger(new DBTrigger("Name", null));
        assertEquals(1, dbSchema.getComponents().size());
    }

    @Test
    public void testGetPackages() {
        DBSchema dbSchema = new DBSchema("Name", new DBCatalog("Name", AbstractModelTest.createTestModel()));
        assertTrue(dbSchema.getPackages().isEmpty());
        assertTrue(dbSchema.getDatabase().isPackagesImported());
    }

    @Test
    public void testAddPackage() {
        DBSchema dbSchema = new DBSchema("Name", new DBCatalog("Name", AbstractModelTest.createTestModel()));
        dbSchema.addPackage(new DBPackage("Name", null));
        assertEquals(1, dbSchema.getComponents().size());
    }

    @Test
    public void testReceivePackage() {
        DBSchema dbSchema = new DBSchema("Name");
        dbSchema.receivePackage(new DBPackage("Name", null));
        assertEquals(1, dbSchema.getComponents().size());
    }
}

