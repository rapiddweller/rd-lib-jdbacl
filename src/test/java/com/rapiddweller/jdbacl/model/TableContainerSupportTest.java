package com.rapiddweller.jdbacl.model;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

public class TableContainerSupportTest {
    @Test
    public void testConstructor() {
        TableContainerSupport actualTableContainerSupport = new TableContainerSupport();
        Collection<TableContainer> subContainers = actualTableContainerSupport.getSubContainers();
        assertTrue(subContainers instanceof java.util.ArrayList);
        List<ContainerComponent> components = actualTableContainerSupport.getComponents();
        assertTrue(components instanceof java.util.ArrayList);
        assertTrue(subContainers.isEmpty());
        assertTrue(components.isEmpty());
        List<DBTable> tables = actualTableContainerSupport.getTables();
        assertTrue(tables instanceof java.util.ArrayList);
        assertTrue(tables.isEmpty());
    }

    @Test
    public void testGetSubContainers() {
        assertTrue((new TableContainerSupport()).getSubContainers().isEmpty());
    }

    @Test
    public void testGetTables() {
        assertTrue((new TableContainerSupport()).getTables().isEmpty());
        assertTrue((new TableContainerSupport()).getTables(true).isEmpty());
        assertTrue((new TableContainerSupport()).getTables(false).isEmpty());
    }

    @Test
    public void testGetTables2() {
        TableContainer subContainer = new TableContainer("Name");
        TableContainerSupport tableContainerSupport = new TableContainerSupport();
        tableContainerSupport.addSubContainer(subContainer);
        assertTrue(tableContainerSupport.getTables(true).isEmpty());
    }

    @Test
    public void testGetTables3() {
        TableContainerSupport tableContainerSupport = new TableContainerSupport();
        ArrayList<DBTable> dbTableList = new ArrayList<>();
        List<DBTable> actualTables = tableContainerSupport.getTables(true, dbTableList);
        assertSame(dbTableList, actualTables);
        assertTrue(actualTables.isEmpty());
    }

    @Test
    public void testGetTables4() {
        TableContainerSupport tableContainerSupport = new TableContainerSupport();
        ArrayList<DBTable> dbTableList = new ArrayList<>();
        List<DBTable> actualTables = tableContainerSupport.getTables(false, dbTableList);
        assertSame(dbTableList, actualTables);
        assertTrue(actualTables.isEmpty());
    }

    @Test
    public void testGetTables5() {
        TableContainer subContainer = new TableContainer("Name");
        TableContainerSupport tableContainerSupport = new TableContainerSupport();
        tableContainerSupport.addSubContainer(subContainer);
        ArrayList<DBTable> dbTableList = new ArrayList<>();
        List<DBTable> actualTables = tableContainerSupport.getTables(true, dbTableList);
        assertSame(dbTableList, actualTables);
        assertTrue(actualTables.isEmpty());
    }

    @Test
    public void testGetTable() {
        assertNull((new TableContainerSupport()).getTable("Table Name"));
    }

    @Test
    public void testGetSequences() {
        assertTrue((new TableContainerSupport()).getSequences(true).isEmpty());
        assertTrue((new TableContainerSupport()).getSequences(false).isEmpty());
    }

    @Test
    public void testGetSequences2() {
        TableContainer subContainer = new TableContainer("Name");
        TableContainerSupport tableContainerSupport = new TableContainerSupport();
        tableContainerSupport.addSubContainer(subContainer);
        assertTrue(tableContainerSupport.getSequences(true).isEmpty());
    }

    @Test
    public void testGetSequences3() {
        TableContainerSupport tableContainerSupport = new TableContainerSupport();
        ArrayList<DBSequence> dbSequenceList = new ArrayList<>();
        List<DBSequence> actualSequences = tableContainerSupport.getSequences(true, dbSequenceList);
        assertSame(dbSequenceList, actualSequences);
        assertTrue(actualSequences.isEmpty());
    }

    @Test
    public void testGetSequences4() {
        TableContainerSupport tableContainerSupport = new TableContainerSupport();
        ArrayList<DBSequence> dbSequenceList = new ArrayList<>();
        List<DBSequence> actualSequences = tableContainerSupport.getSequences(false, dbSequenceList);
        assertSame(dbSequenceList, actualSequences);
        assertTrue(actualSequences.isEmpty());
    }

    @Test
    public void testGetSequences5() {
        TableContainer subContainer = new TableContainer("Name");
        TableContainerSupport tableContainerSupport = new TableContainerSupport();
        tableContainerSupport.addSubContainer(subContainer);
        ArrayList<DBSequence> dbSequenceList = new ArrayList<>();
        List<DBSequence> actualSequences = tableContainerSupport.getSequences(true, dbSequenceList);
        assertSame(dbSequenceList, actualSequences);
        assertTrue(actualSequences.isEmpty());
    }

    @Test
    public void testGetComponents() {
        assertTrue((new TableContainerSupport()).getComponents().isEmpty());
    }
}

