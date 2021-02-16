package com.rapiddweller.jdbacl.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * The type Abstract db object test.
 */
public class AbstractDBObjectTest {
  /**
   * Test set name.
   */
  @Test
  public void testSetName() {
    DBCatalog dbCatalog = new DBCatalog();
    dbCatalog.setName("Name");
    assertEquals("Name", dbCatalog.toString());
  }

  /**
   * Test set doc.
   */
  @Test
  public void testSetDoc() {
    DBCatalog dbCatalog = new DBCatalog();
    dbCatalog.setDoc("Doc");
    assertEquals("Doc", dbCatalog.getDoc());
  }

  /**
   * Test set owner.
   */
  @Test
  public void testSetOwner() {
    DBSchema dbSchema = new DBSchema("Name");
    DBCatalog dbCatalog = new DBCatalog();
    dbCatalog.setOwner(dbSchema);
    assertSame(dbSchema, dbCatalog.getOwner());
  }
}

