package com.rapiddweller.jdbacl.model;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The type Table container test.
 */
public class TableContainerTest {
  /**
   * Test constructor.
   */
  @Test
  public void testConstructor() {
    TableContainer actualTableContainer = new TableContainer("Name");
    assertEquals("container", actualTableContainer.getObjectType());
    assertEquals("Name", actualTableContainer.getName());
    assertNull(actualTableContainer.getSchema());
  }

  /**
   * Test constructor 2.
   */
  @Test
  public void testConstructor2() {
    TableContainer actualTableContainer = new TableContainer("Name", new DBSchema("Name"));
    assertEquals("container", actualTableContainer.getObjectType());
    assertEquals("Name", actualTableContainer.getName());
    assertNull(actualTableContainer.getSchema());
  }

  /**
   * Test constructor 3.
   */
  @Test
  public void testConstructor3() {
    TableContainer actualTableContainer = new TableContainer("Name", new TableContainer("Name"));
    assertEquals("container", actualTableContainer.getObjectType());
    assertEquals("Name", actualTableContainer.getName());
    assertNull(actualTableContainer.getSchema());
  }

  /**
   * Test get schema.
   */
  @Test
  public void testGetSchema() {
    assertNull((new TableContainer("Name")).getSchema());
  }

  /**
   * Test get components.
   */
  @Test
  public void testGetComponents() {
    assertTrue((new TableContainer("Name")).getComponents().isEmpty());
  }

  /**
   * Test get tables.
   */
  @Test
  public void testGetTables() {
    assertTrue((new TableContainer("Name")).getTables().isEmpty());
    assertTrue((new TableContainer("Name")).getTables(true).isEmpty());
  }

  /**
   * Test get tables 2.
   */
  @Test
  public void testGetTables2() {
    TableContainer tableContainer = new TableContainer("Name");
    ArrayList<DBTable> dbTableList = new ArrayList<>();
    tableContainer.getTables(true, dbTableList);
    assertTrue(dbTableList.isEmpty());
  }

  /**
   * Test get table.
   */
  @Test
  public void testGetTable() {
    assertNull((new TableContainer("Name")).getTable("Table Name"));
  }

  /**
   * Test get sequences.
   */
  @Test
  public void testGetSequences() {
    assertTrue((new TableContainer("Name")).getSequences(true).isEmpty());
  }

  /**
   * Test get sequences 2.
   */
  @Test
  public void testGetSequences2() {
    TableContainer tableContainer = new TableContainer("Name");
    ArrayList<DBSequence> dbSequenceList = new ArrayList<>();
    tableContainer.getSequences(true, dbSequenceList);
    assertTrue(dbSequenceList.isEmpty());
  }
}

