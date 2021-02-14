package com.rapiddweller.jdbacl.model;

import com.rapiddweller.common.ObjectNotFoundException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DBForeignKeyConstraintTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testColumnReferencedBy() {
    DBTable owner = new DBTable("Name");
    assertEquals("Referee Column Name",
        (new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"))
            .columnReferencedBy("Fk Column Name"));
  }

  @Test
  public void testColumnReferencedBy2() {
    DBTable owner = new DBTable("Name");
    thrown.expect(ObjectNotFoundException.class);
    (new DBForeignKeyConstraint("Name", true, owner, "com.rapiddweller.jdbacl.model.DBForeignKeyConstraint",
        new DBTable("Name"), "Referee Column Name")).columnReferencedBy("Fk Column Name");
  }

  @Test
  public void testColumnReferencedBy3() {
    DBTable owner = new DBTable("Name");
    assertEquals("Referee Column Name",
        (new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"))
            .columnReferencedBy("Fk Column Name", true));
  }

  @Test
  public void testColumnReferencedBy4() {
    DBTable owner = new DBTable("Name");
    thrown.expect(ObjectNotFoundException.class);
    (new DBForeignKeyConstraint("Name", true, owner, "com.rapiddweller.jdbacl.model.DBForeignKeyConstraint",
        new DBTable("Name"), "Referee Column Name")).columnReferencedBy("Fk Column Name", true);
  }

  @Test
  public void testColumnReferencedBy5() {
    DBTable owner = new DBTable("Name");
    assertNull((new DBForeignKeyConstraint("Name", true, owner, "com.rapiddweller.jdbacl.model.DBForeignKeyConstraint",
        new DBTable("Name"), "Referee Column Name")).columnReferencedBy("Fk Column Name", false));
  }

  @Test
  public void testIsIdentical() {
    DBTable owner = new DBTable("Name");
    DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
        new DBTable("Name"), "Referee Column Name");
    assertFalse(dbForeignKeyConstraint.isIdentical(new DBCatalog()));
  }

  @Test
  public void testIsIdentical2() {
    DBTable owner = new DBTable("Name");
    assertFalse(
        (new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"))
            .isIdentical(null));
  }

  @Test
  public void testSetUpdateRule() {
    DBTable owner = new DBTable("Name");
    DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
        new DBTable("Name"), "Referee Column Name");
    dbForeignKeyConstraint.setUpdateRule(FKChangeRule.NO_ACTION);
    assertEquals(FKChangeRule.NO_ACTION, dbForeignKeyConstraint.getUpdateRule());
  }

  @Test
  public void testSetDeleteRule() {
    DBTable owner = new DBTable("Name");
    DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
        new DBTable("Name"), "Referee Column Name");
    dbForeignKeyConstraint.setDeleteRule(FKChangeRule.NO_ACTION);
    assertEquals(FKChangeRule.NO_ACTION, dbForeignKeyConstraint.getDeleteRule());
  }

  @Test
  public void testEquals() {
    DBTable owner = new DBTable("Name");
    assertNotEquals("other", (new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name")));
  }

  @Test
  public void testEquals2() {
    DBTable owner = new DBTable("Name");
    assertNotEquals(null, (new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name")));
  }

  @Test
  public void testHashCode() {
    DBTable owner = new DBTable("Name");
    assertEquals(-707271838,
        (new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"))
            .hashCode());
  }
}

