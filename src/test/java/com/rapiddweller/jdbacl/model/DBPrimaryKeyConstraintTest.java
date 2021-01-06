package com.rapiddweller.jdbacl.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DBPrimaryKeyConstraintTest {
    @Test
    public void testConstructor() {
        DBPrimaryKeyConstraint actualDbPrimaryKeyConstraint = new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true,
                "foo", "foo", "foo");
        assertEquals("unique constraint", actualDbPrimaryKeyConstraint.getObjectType());
        assertTrue(actualDbPrimaryKeyConstraint.isNameDeterministic());
        assertEquals(3, actualDbPrimaryKeyConstraint.getColumnNames().length);
        CompositeDBObject<?> expectedTable = actualDbPrimaryKeyConstraint.owner;
        DBTable table = actualDbPrimaryKeyConstraint.getTable();
        assertSame(expectedTable, table);
        assertEquals("Name", actualDbPrimaryKeyConstraint.getName());
        assertTrue(table.isPKImported());
        assertEquals(3, table.getPKColumnNames().length);
        assertEquals(0, table.getColumnNames().length);
    }

    @Test
    public void testConstructor2() {
        DBPrimaryKeyConstraint actualDbPrimaryKeyConstraint = new DBPrimaryKeyConstraint(null, "Name", true, "foo", "foo",
                "foo");
        assertEquals("unique constraint", actualDbPrimaryKeyConstraint.getObjectType());
        assertTrue(actualDbPrimaryKeyConstraint.isNameDeterministic());
        assertEquals(3, actualDbPrimaryKeyConstraint.getColumnNames().length);
        assertNull(actualDbPrimaryKeyConstraint.getTable());
        assertEquals("Name", actualDbPrimaryKeyConstraint.getName());
    }

    @Test
    public void testConstructor3() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(null);
        DBPrimaryKeyConstraint actualDbPrimaryKeyConstraint = new DBPrimaryKeyConstraint(dbTable, "Name", true, "foo",
                "foo", "foo");
        assertEquals("unique constraint", actualDbPrimaryKeyConstraint.getObjectType());
        assertTrue(actualDbPrimaryKeyConstraint.isNameDeterministic());
        assertEquals(3, actualDbPrimaryKeyConstraint.getColumnNames().length);
        CompositeDBObject<?> expectedTable = actualDbPrimaryKeyConstraint.owner;
        DBTable table = actualDbPrimaryKeyConstraint.getTable();
        assertSame(expectedTable, table);
        assertEquals("Name", actualDbPrimaryKeyConstraint.getName());
        assertEquals(3, table.getPKColumnNames().length);
    }

    @Test
    public void testToString() {
        assertEquals("CONSTRAINT Name PRIMARY KEY (foo, foo, foo)",
                (new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo")).toString());
    }
}

