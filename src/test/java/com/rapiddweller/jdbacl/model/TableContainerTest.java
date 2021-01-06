package com.rapiddweller.jdbacl.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

public class TableContainerTest {
    @Test
    public void testConstructor() {
        TableContainer actualTableContainer = new TableContainer("Name");
        assertEquals("container", actualTableContainer.getObjectType());
        assertEquals("Name", actualTableContainer.getName());
        assertNull(actualTableContainer.getSchema());
    }

    @Test
    public void testConstructor2() {
        TableContainer actualTableContainer = new TableContainer("Name", new DBSchema("Name"));
        assertEquals("container", actualTableContainer.getObjectType());
        assertEquals("Name", actualTableContainer.getName());
        assertNull(actualTableContainer.getSchema());
    }

    @Test
    public void testConstructor3() {
        TableContainer actualTableContainer = new TableContainer("Name", new TableContainer("Name"));
        assertEquals("container", actualTableContainer.getObjectType());
        assertEquals("Name", actualTableContainer.getName());
        assertNull(actualTableContainer.getSchema());
    }

    @Test
    public void testGetSchema() {
        assertNull((new TableContainer("Name")).getSchema());
    }

    @Test
    public void testGetComponents() {
        assertTrue((new TableContainer("Name")).getComponents().isEmpty());
    }

    @Test
    public void testGetTables() {
        assertTrue((new TableContainer("Name")).getTables().isEmpty());
        assertTrue((new TableContainer("Name")).getTables(true).isEmpty());
    }

    @Test
    public void testGetTables2() {
        TableContainer tableContainer = new TableContainer("Name");
        ArrayList<DBTable> dbTableList = new ArrayList<DBTable>();
        tableContainer.getTables(true, dbTableList);
        assertTrue(dbTableList.isEmpty());
    }

    @Test
    public void testGetTable() {
        assertNull((new TableContainer("Name")).getTable("Table Name"));
    }

    @Test
    public void testGetSequences() {
        assertTrue((new TableContainer("Name")).getSequences(true).isEmpty());
    }

    @Test
    public void testGetSequences2() {
        TableContainer tableContainer = new TableContainer("Name");
        ArrayList<DBSequence> dbSequenceList = new ArrayList<DBSequence>();
        tableContainer.getSequences(true, dbSequenceList);
        assertTrue(dbSequenceList.isEmpty());
    }
}

