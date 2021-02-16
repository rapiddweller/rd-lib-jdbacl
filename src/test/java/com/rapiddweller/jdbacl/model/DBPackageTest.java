package com.rapiddweller.jdbacl.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * The type Db package test.
 */
public class DBPackageTest {
  /**
   * Test constructor.
   */
  @Test
  public void testConstructor() {
    DBPackage actualDbPackage = new DBPackage("Name", null);
    assertNull(actualDbPackage.getObjectType());
    assertNull(actualDbPackage.getSchema());
    assertEquals("Name", actualDbPackage.toString());
  }

  /**
   * Test constructor 2.
   */
  @Test
  public void testConstructor2() {
    DBPackage actualDbPackage = new DBPackage("Name",
        new DBSchema("Name", new DBCatalog("Name", AbstractModelTest.createTestModel())));
    assertNull(actualDbPackage.getObjectType());
    CompositeDBObject<?> expectedSchema = actualDbPackage.owner;
    DBSchema schema = actualDbPackage.getSchema();
    assertSame(expectedSchema, schema);
    assertEquals("Name", actualDbPackage.toString());
    assertTrue(schema.getDatabase().isPackagesImported());
  }

  /**
   * Test get schema.
   */
  @Test
  public void testGetSchema() {
    assertNull((new DBPackage("Name", null)).getSchema());
  }

  /**
   * Test set schema.
   */
  @Test
  public void testSetSchema() {
    DBPackage dbPackage = new DBPackage("Name", null);
    DBSchema dbSchema = new DBSchema("Name");
    dbPackage.setSchema(dbSchema);
    assertSame(dbSchema, dbPackage.getSchema());
  }

  /**
   * Test set sub object name.
   */
  @Test
  public void testSetSubObjectName() {
    DBPackage dbPackage = new DBPackage("Name", null);
    dbPackage.setSubObjectName("Sub Object Name");
    assertEquals("Sub Object Name", dbPackage.getSubObjectName());
  }

  /**
   * Test set object id.
   */
  @Test
  public void testSetObjectId() {
    DBPackage dbPackage = new DBPackage("Name", null);
    dbPackage.setObjectId("42");
    assertEquals("42", dbPackage.getObjectId());
  }

  /**
   * Test set data object id.
   */
  @Test
  public void testSetDataObjectId() {
    DBPackage dbPackage = new DBPackage("Name", null);
    dbPackage.setDataObjectId("42");
    assertEquals("42", dbPackage.getDataObjectId());
  }

  /**
   * Test set object type.
   */
  @Test
  public void testSetObjectType() {
    DBPackage dbPackage = new DBPackage("Name", null);
    dbPackage.setObjectType("Object Type");
    assertEquals("Object Type", dbPackage.getObjectType());
  }

  /**
   * Test set status.
   */
  @Test
  public void testSetStatus() {
    DBPackage dbPackage = new DBPackage("Name", null);
    dbPackage.setStatus("Status");
    assertEquals("Status", dbPackage.getStatus());
  }

  /**
   * Test get procedures.
   */
  @Test
  public void testGetProcedures() {
    assertTrue((new DBPackage("Name", null)).getProcedures().isEmpty());
  }

  /**
   * Test get components.
   */
  @Test
  public void testGetComponents() {
    assertTrue((new DBPackage("Name", null)).getComponents().isEmpty());
  }

  /**
   * Test add procedure.
   */
  @Test
  public void testAddProcedure() {
    DBPackage dbPackage = new DBPackage("Name", null);
    DBProcedure dbProcedure = new DBProcedure("Name", new DBPackage("Name", null));
    dbPackage.addProcedure(dbProcedure);
    CompositeDBObject<?> expectedOwner = dbProcedure.owner;
    assertSame(expectedOwner, dbProcedure.getOwner());
  }

  /**
   * Test is identical.
   */
  @Test
  public void testIsIdentical() {
    DBPackage dbPackage = new DBPackage("Name", null);
    assertFalse(dbPackage.isIdentical(new DBCatalog()));
  }

  /**
   * Test is identical 2.
   */
  @Test
  public void testIsIdentical2() {
    assertFalse((new DBPackage("Name", null)).isIdentical(null));
  }
}

