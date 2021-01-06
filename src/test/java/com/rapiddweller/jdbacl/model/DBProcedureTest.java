package com.rapiddweller.jdbacl.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class DBProcedureTest {
    @Test
    public void testConstructor() {
        DBProcedure actualDbProcedure = new DBProcedure("Name", new DBPackage("Name", null));
        assertEquals("procedure", actualDbProcedure.getObjectType());
        assertEquals("Name", actualDbProcedure.toString());
        CompositeDBObject<?> expectedOwner = actualDbProcedure.owner;
        assertSame(expectedOwner, actualDbProcedure.getOwner());
    }

    @Test
    public void testSetObjectId() {
        DBProcedure dbProcedure = new DBProcedure("Name", new DBPackage("Name", null));
        dbProcedure.setObjectId("42");
        assertEquals("42", dbProcedure.getObjectId());
    }

    @Test
    public void testSetSubProgramId() {
        DBProcedure dbProcedure = new DBProcedure("Name", new DBPackage("Name", null));
        dbProcedure.setSubProgramId("42");
        assertEquals("42", dbProcedure.getSubProgramId());
    }

    @Test
    public void testSetOverload() {
        DBProcedure dbProcedure = new DBProcedure("Name", new DBPackage("Name", null));
        dbProcedure.setOverload("Overload");
        assertEquals("Overload", dbProcedure.getOverload());
    }

    @Test
    public void testIsIdentical() {
        DBProcedure dbProcedure = new DBProcedure("Name", new DBPackage("Name", null));
        assertFalse(dbProcedure.isIdentical(new DBCatalog()));
    }

    @Test
    public void testIsIdentical2() {
        assertFalse((new DBProcedure("Name", new DBPackage("Name", null))).isIdentical(null));
    }
}

