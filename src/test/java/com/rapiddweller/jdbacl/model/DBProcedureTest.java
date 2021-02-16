package com.rapiddweller.jdbacl.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

/**
 * The type Db procedure test.
 */
public class DBProcedureTest {
  /**
   * Test constructor.
   */
  @Test
  public void testConstructor() {
    DBProcedure actualDbProcedure = new DBProcedure("Name", new DBPackage("Name", null));
    assertEquals("procedure", actualDbProcedure.getObjectType());
    assertEquals("Name", actualDbProcedure.toString());
    CompositeDBObject<?> expectedOwner = actualDbProcedure.owner;
    assertSame(expectedOwner, actualDbProcedure.getOwner());
  }

  /**
   * Test set object id.
   */
  @Test
  public void testSetObjectId() {
    DBProcedure dbProcedure = new DBProcedure("Name", new DBPackage("Name", null));
    dbProcedure.setObjectId("42");
    assertEquals("42", dbProcedure.getObjectId());
  }

  /**
   * Test set sub program id.
   */
  @Test
  public void testSetSubProgramId() {
    DBProcedure dbProcedure = new DBProcedure("Name", new DBPackage("Name", null));
    dbProcedure.setSubProgramId("42");
    assertEquals("42", dbProcedure.getSubProgramId());
  }

  /**
   * Test set overload.
   */
  @Test
  public void testSetOverload() {
    DBProcedure dbProcedure = new DBProcedure("Name", new DBPackage("Name", null));
    dbProcedure.setOverload("Overload");
    assertEquals("Overload", dbProcedure.getOverload());
  }

  /**
   * Test is identical.
   */
  @Test
  public void testIsIdentical() {
    DBProcedure dbProcedure = new DBProcedure("Name", new DBPackage("Name", null));
    assertFalse(dbProcedure.isIdentical(new DBCatalog()));
  }

  /**
   * Test is identical 2.
   */
  @Test
  public void testIsIdentical2() {
    assertFalse((new DBProcedure("Name", new DBPackage("Name", null))).isIdentical(null));
  }
}

