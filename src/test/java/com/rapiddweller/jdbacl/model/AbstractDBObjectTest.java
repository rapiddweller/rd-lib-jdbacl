package com.rapiddweller.jdbacl.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class AbstractDBObjectTest {
    @Test
    public void testSetName() {
        DBCatalog dbCatalog = new DBCatalog();
        dbCatalog.setName("Name");
        assertEquals("Name", dbCatalog.toString());
    }

    @Test
    public void testSetDoc() {
        DBCatalog dbCatalog = new DBCatalog();
        dbCatalog.setDoc("Doc");
        assertEquals("Doc", dbCatalog.getDoc());
    }

    @Test
    public void testSetOwner() {
        DBSchema dbSchema = new DBSchema("Name");
        DBCatalog dbCatalog = new DBCatalog();
        dbCatalog.setOwner(dbSchema);
        assertSame(dbSchema, dbCatalog.getOwner());
    }
}

