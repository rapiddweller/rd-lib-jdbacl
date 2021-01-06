package com.rapiddweller.jdbacl.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.rapiddweller.common.ObjectNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Rule;

import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DBForeignKeyConstraintTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConstructor() {
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint actualDbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner,
                "Fk Column Name", new DBTable("Name"), "Referee Column Name");
        assertEquals("CONSTRAINT Name FOREIGN KEY (Fk Column Name) REFERENCES Name(Referee Column Name)",
                actualDbForeignKeyConstraint.toString());
        assertEquals("foreign key constraint", actualDbForeignKeyConstraint.getObjectType());
        assertEquals(1, actualDbForeignKeyConstraint.getForeignKeyColumnNames().length);
        assertTrue(actualDbForeignKeyConstraint.isNameDeterministic());
        assertEquals(FKChangeRule.NO_ACTION, actualDbForeignKeyConstraint.getUpdateRule());
        assertEquals(FKChangeRule.NO_ACTION, actualDbForeignKeyConstraint.getDeleteRule());
        assertEquals(1, actualDbForeignKeyConstraint.getRefereeColumnNames().length);
        CompositeDBObject<?> expectedTable = actualDbForeignKeyConstraint.owner;
        DBTable table = actualDbForeignKeyConstraint.getTable();
        assertSame(expectedTable, table);
        assertEquals("Name", actualDbForeignKeyConstraint.getName());
        assertTrue(table.isPKImported());
        assertEquals(0, table.getColumnNames().length);
    }

    @Test
    public void testConstructor2() {
        DBForeignKeyConstraint actualDbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, null,
                "Fk Column Name", new DBTable("Name"), "Referee Column Name");
        assertEquals("CONSTRAINT Name FOREIGN KEY (Fk Column Name) REFERENCES Name(Referee Column Name)",
                actualDbForeignKeyConstraint.toString());
        assertEquals("foreign key constraint", actualDbForeignKeyConstraint.getObjectType());
        assertEquals(1, actualDbForeignKeyConstraint.getForeignKeyColumnNames().length);
        assertTrue(actualDbForeignKeyConstraint.isNameDeterministic());
        assertEquals(FKChangeRule.NO_ACTION, actualDbForeignKeyConstraint.getUpdateRule());
        assertEquals(FKChangeRule.NO_ACTION, actualDbForeignKeyConstraint.getDeleteRule());
        assertEquals(1, actualDbForeignKeyConstraint.getRefereeColumnNames().length);
        assertNull(actualDbForeignKeyConstraint.getTable());
        assertEquals("Name", actualDbForeignKeyConstraint.getName());
    }

    @Test
    public void testConstructor4() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(null);
        DBForeignKeyConstraint actualDbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, dbTable,
                "Fk Column Name", new DBTable("Name"), "Referee Column Name");
        assertEquals("CONSTRAINT Name FOREIGN KEY (Fk Column Name) REFERENCES Name(Referee Column Name)",
                actualDbForeignKeyConstraint.toString());
        assertEquals("foreign key constraint", actualDbForeignKeyConstraint.getObjectType());
        assertEquals(1, actualDbForeignKeyConstraint.getForeignKeyColumnNames().length);
        assertTrue(actualDbForeignKeyConstraint.isNameDeterministic());
        assertEquals(FKChangeRule.NO_ACTION, actualDbForeignKeyConstraint.getUpdateRule());
        assertEquals(FKChangeRule.NO_ACTION, actualDbForeignKeyConstraint.getDeleteRule());
        assertEquals(1, actualDbForeignKeyConstraint.getRefereeColumnNames().length);
        CompositeDBObject<?> expectedTable = actualDbForeignKeyConstraint.owner;
        assertSame(expectedTable, actualDbForeignKeyConstraint.getTable());
        assertEquals("Name", actualDbForeignKeyConstraint.getName());
    }

    @Test
    public void testConstructor5() {
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint actualDbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner,
                new String[]{"foo", "foo", "foo"}, new DBTable("Name"), new String[]{"foo", "foo", "foo"});
        assertEquals("CONSTRAINT Name FOREIGN KEY (foo, foo, foo) REFERENCES Name(foo, foo, foo)",
                actualDbForeignKeyConstraint.toString());
        assertEquals("foreign key constraint", actualDbForeignKeyConstraint.getObjectType());
        assertEquals(3, actualDbForeignKeyConstraint.getForeignKeyColumnNames().length);
        assertTrue(actualDbForeignKeyConstraint.isNameDeterministic());
        assertEquals(FKChangeRule.NO_ACTION, actualDbForeignKeyConstraint.getUpdateRule());
        assertEquals(FKChangeRule.NO_ACTION, actualDbForeignKeyConstraint.getDeleteRule());
        assertEquals(3, actualDbForeignKeyConstraint.getRefereeColumnNames().length);
        CompositeDBObject<?> expectedTable = actualDbForeignKeyConstraint.owner;
        DBTable table = actualDbForeignKeyConstraint.getTable();
        assertSame(expectedTable, table);
        assertEquals("Name", actualDbForeignKeyConstraint.getName());
        assertTrue(table.isPKImported());
        assertEquals(0, table.getColumnNames().length);
    }

    @Test
    public void testConstructor6() {
        DBForeignKeyConstraint actualDbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, null,
                new String[]{"foo", "foo", "foo"}, new DBTable("Name"), new String[]{"foo", "foo", "foo"});
        assertEquals("CONSTRAINT Name FOREIGN KEY (foo, foo, foo) REFERENCES Name(foo, foo, foo)",
                actualDbForeignKeyConstraint.toString());
        assertEquals("foreign key constraint", actualDbForeignKeyConstraint.getObjectType());
        assertEquals(3, actualDbForeignKeyConstraint.getForeignKeyColumnNames().length);
        assertTrue(actualDbForeignKeyConstraint.isNameDeterministic());
        assertEquals(FKChangeRule.NO_ACTION, actualDbForeignKeyConstraint.getUpdateRule());
        assertEquals(FKChangeRule.NO_ACTION, actualDbForeignKeyConstraint.getDeleteRule());
        assertEquals(3, actualDbForeignKeyConstraint.getRefereeColumnNames().length);
        assertNull(actualDbForeignKeyConstraint.getTable());
        assertEquals("Name", actualDbForeignKeyConstraint.getName());
    }

    @Test
    public void testConstructor8() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(null);
        DBForeignKeyConstraint actualDbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, dbTable,
                new String[]{"foo", "foo", "foo"}, new DBTable("Name"), new String[]{"foo", "foo", "foo"});
        assertEquals("CONSTRAINT Name FOREIGN KEY (foo, foo, foo) REFERENCES Name(foo, foo, foo)",
                actualDbForeignKeyConstraint.toString());
        assertEquals("foreign key constraint", actualDbForeignKeyConstraint.getObjectType());
        assertEquals(3, actualDbForeignKeyConstraint.getForeignKeyColumnNames().length);
        assertTrue(actualDbForeignKeyConstraint.isNameDeterministic());
        assertEquals(FKChangeRule.NO_ACTION, actualDbForeignKeyConstraint.getUpdateRule());
        assertEquals(FKChangeRule.NO_ACTION, actualDbForeignKeyConstraint.getDeleteRule());
        assertEquals(3, actualDbForeignKeyConstraint.getRefereeColumnNames().length);
        CompositeDBObject<?> expectedTable = actualDbForeignKeyConstraint.owner;
        assertSame(expectedTable, actualDbForeignKeyConstraint.getTable());
        assertEquals("Name", actualDbForeignKeyConstraint.getName());
    }

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
        assertFalse(
                (new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"))
                        .equals("other"));
    }

    @Test
    public void testEquals2() {
        DBTable owner = new DBTable("Name");
        assertFalse(
                (new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"))
                        .equals(null));
    }

    @Test
    public void testHashCode() {
        DBTable owner = new DBTable("Name");
        assertEquals(-707271838,
                (new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"))
                        .hashCode());
    }

    @Test
    public void testToString() {
        DBTable owner = new DBTable("Name");
        assertEquals("CONSTRAINT Name FOREIGN KEY (Fk Column Name) REFERENCES Name(Referee Column Name)",
                (new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"))
                        .toString());
    }
}

