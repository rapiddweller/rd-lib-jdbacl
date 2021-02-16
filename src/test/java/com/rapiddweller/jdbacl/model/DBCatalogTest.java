package com.rapiddweller.jdbacl.model;

import com.rapiddweller.common.ObjectNotFoundException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * The type Db catalog test.
 */
public class DBCatalogTest {
  /**
   * The Thrown.
   */
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /**
   * Test constructor.
   */
  @Test
  public void testConstructor() {
    DBCatalog actualDbCatalog = new DBCatalog();
    assertEquals("catalog", actualDbCatalog.getObjectType());
    assertTrue(actualDbCatalog.schemas.isEmpty());
    assertNull(actualDbCatalog.toString());
    assertNull(actualDbCatalog.getOwner());
  }

  /**
   * Test constructor 2.
   */
  @Test
  public void testConstructor2() {
    DBCatalog actualDbCatalog = new DBCatalog("Name");
    assertEquals("catalog", actualDbCatalog.getObjectType());
    assertTrue(actualDbCatalog.schemas.isEmpty());
    assertEquals("Name", actualDbCatalog.toString());
    assertNull(actualDbCatalog.getOwner());
  }

  /**
   * Test constructor 3.
   */
  @Test
  public void testConstructor3() {
    DBCatalog actualDbCatalog = new DBCatalog("Name", AbstractModelTest.createTestModel());
    assertEquals("catalog", actualDbCatalog.getObjectType());
    assertTrue(actualDbCatalog.schemas.isEmpty());
    assertEquals("Name", actualDbCatalog.toString());
    CompositeDBObject<?> expectedOwner = actualDbCatalog.owner;
    assertSame(expectedOwner, actualDbCatalog.getOwner());
  }

  /**
   * Test constructor 4.
   */
  @Test
  public void testConstructor4() {
    DBCatalog actualDbCatalog = new DBCatalog("Name", null);
    assertEquals("catalog", actualDbCatalog.getObjectType());
    assertTrue(actualDbCatalog.schemas.isEmpty());
    assertEquals("Name", actualDbCatalog.toString());
    assertNull(actualDbCatalog.getOwner());
  }

  /**
   * Test get database.
   */
  @Test
  public void testGetDatabase() {
    assertNull((new DBCatalog()).getDatabase());
  }

  /**
   * Test set database.
   */
  @Test
  public void testSetDatabase() {
    DBCatalog dbCatalog = new DBCatalog();
    Database createTestModelResult = AbstractModelTest.createTestModel();
    dbCatalog.setDatabase(createTestModelResult);
    assertSame(createTestModelResult, dbCatalog.getOwner());
  }

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
   * Test get components.
   */
  @Test
  public void testGetComponents() {
    assertTrue((new DBCatalog()).getComponents().isEmpty());
  }

  /**
   * Test get schemas.
   */
  @Test
  public void testGetSchemas() {
    assertTrue((new DBCatalog()).getSchemas().isEmpty());
  }

  /**
   * Test get schema.
   */
  @Test
  public void testGetSchema() {
    assertNull((new DBCatalog()).getSchema("Schema Name"));
  }

  /**
   * Test add schema.
   */
  @Test
  public void testAddSchema() {
    DBSchema dbSchema = new DBSchema("Name");
    (new DBCatalog()).addSchema(dbSchema);
    assertNull(dbSchema.getDatabase());
  }

  /**
   * Test get tables.
   */
  @Test
  public void testGetTables() {
    assertTrue((new DBCatalog()).getTables().isEmpty());
  }

  /**
   * Test get tables 2.
   */
  @Test
  public void testGetTables2() {
    DBSchema schema = new DBSchema("Name");
    DBCatalog dbCatalog = new DBCatalog();
    dbCatalog.addSchema(schema);
    assertTrue(dbCatalog.getTables().isEmpty());
  }

  /**
   * Test get table.
   */
  @Test
  public void testGetTable() {
    thrown.expect(ObjectNotFoundException.class);
    (new DBCatalog()).getTable("Name", true);
  }

  /**
   * Test get table 2.
   */
  @Test
  public void testGetTable2() {
    assertNull((new DBCatalog()).getTable("Name", false));
  }

  /**
   * Test get table 3.
   */
  @Test
  public void testGetTable3() {
    DBSchema schema = new DBSchema("Name");
    DBCatalog dbCatalog = new DBCatalog();
    dbCatalog.addSchema(schema);
    thrown.expect(ObjectNotFoundException.class);
    dbCatalog.getTable("Name", true);
  }

  /**
   * Test get sequences.
   */
  @Test
  public void testGetSequences() {
    assertTrue((new DBCatalog()).getSequences().isEmpty());
  }

  /**
   * Test get sequences 2.
   */
  @Test
  public void testGetSequences2() {
    DBCatalog dbCatalog = new DBCatalog("Name", AbstractModelTest.createTestModel());
    dbCatalog.addSchema(new DBSchema("Name", new DBCatalog()));
    assertTrue(dbCatalog.getSequences().isEmpty());
    assertTrue(((Database) dbCatalog.getOwner()).isSequencesImported());
  }
}

