package com.rapiddweller.jdbacl.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * The type Table container support test.
 */
public class TableContainerSupportTest {
  /**
   * Test constructor.
   */
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

  /**
   * Test get sub containers.
   */
  @Test
  public void testGetSubContainers() {
    assertTrue((new TableContainerSupport()).getSubContainers().isEmpty());
  }

  /**
   * Test get tables.
   */
  @Test
  public void testGetTables() {
    assertTrue((new TableContainerSupport()).getTables().isEmpty());
    assertTrue((new TableContainerSupport()).getTables(true).isEmpty());
    assertTrue((new TableContainerSupport()).getTables(false).isEmpty());
  }

  /**
   * Test get tables 2.
   */
  @Test
  public void testGetTables2() {
    TableContainer subContainer = new TableContainer("Name");
    TableContainerSupport tableContainerSupport = new TableContainerSupport();
    tableContainerSupport.addSubContainer(subContainer);
    assertTrue(tableContainerSupport.getTables(true).isEmpty());
  }

  /**
   * Test get tables 3.
   */
  @Test
  public void testGetTables3() {
    TableContainerSupport tableContainerSupport = new TableContainerSupport();
    ArrayList<DBTable> dbTableList = new ArrayList<>();
    List<DBTable> actualTables = tableContainerSupport.getTables(true, dbTableList);
    assertSame(dbTableList, actualTables);
    assertTrue(actualTables.isEmpty());
  }

  /**
   * Test get tables 4.
   */
  @Test
  public void testGetTables4() {
    TableContainerSupport tableContainerSupport = new TableContainerSupport();
    ArrayList<DBTable> dbTableList = new ArrayList<>();
    List<DBTable> actualTables = tableContainerSupport.getTables(false, dbTableList);
    assertSame(dbTableList, actualTables);
    assertTrue(actualTables.isEmpty());
  }

  /**
   * Test get tables 5.
   */
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

  /**
   * Test get table.
   */
  @Test
  public void testGetTable() {
    assertNull((new TableContainerSupport()).getTable("Table Name"));
  }

  /**
   * Test get sequences.
   */
  @Test
  public void testGetSequences() {
    assertTrue((new TableContainerSupport()).getSequences(true).isEmpty());
    assertTrue((new TableContainerSupport()).getSequences(false).isEmpty());
  }

  /**
   * Test get sequences 2.
   */
  @Test
  public void testGetSequences2() {
    TableContainer subContainer = new TableContainer("Name");
    TableContainerSupport tableContainerSupport = new TableContainerSupport();
    tableContainerSupport.addSubContainer(subContainer);
    assertTrue(tableContainerSupport.getSequences(true).isEmpty());
  }

  /**
   * Test get sequences 3.
   */
  @Test
  public void testGetSequences3() {
    TableContainerSupport tableContainerSupport = new TableContainerSupport();
    ArrayList<DBSequence> dbSequenceList = new ArrayList<>();
    List<DBSequence> actualSequences = tableContainerSupport.getSequences(true, dbSequenceList);
    assertSame(dbSequenceList, actualSequences);
    assertTrue(actualSequences.isEmpty());
  }

  /**
   * Test get sequences 4.
   */
  @Test
  public void testGetSequences4() {
    TableContainerSupport tableContainerSupport = new TableContainerSupport();
    ArrayList<DBSequence> dbSequenceList = new ArrayList<>();
    List<DBSequence> actualSequences = tableContainerSupport.getSequences(false, dbSequenceList);
    assertSame(dbSequenceList, actualSequences);
    assertTrue(actualSequences.isEmpty());
  }

  /**
   * Test get sequences 5.
   */
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

  /**
   * Test get components.
   */
  @Test
  public void testGetComponents() {
    assertTrue((new TableContainerSupport()).getComponents().isEmpty());
  }
}

