package com.rapiddweller.jdbacl.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class DBRowTest {
    @Test
    public void testConstructor() {
        DBRow actualDbRow = new DBRow(new DBTable("Name"));
        assertEquals("Name[]", actualDbRow.toString());
        assertEquals(0, actualDbRow.getPKValues().length);
    }

    @Test
    public void testWithTable() {
        DBRow dbRow = new DBRow(new DBTable("Name"));
        DBRow actualWithTableResult = dbRow.withTable(new DBTable("Name"));
        assertSame(dbRow, actualWithTableResult);
        assertEquals(0, actualWithTableResult.getPKValues().length);
    }

    @Test
    public void testGetPKValues() {
        DBRow dbRow = new DBRow(new DBTable("Name"));
        assertEquals(0, dbRow.getPKValues().length);
        DBTable table = dbRow.getTable();
        assertTrue(table.isPKImported());
        assertEquals(0, table.getColumnNames().length);
    }

    @Test
    public void testGetPKValues2() {
        DBRow dbRow = new DBRow(null);
        dbRow.withTable(new DBTable("Name"));
        assertEquals(0, dbRow.getPKValues().length);
        DBTable table = dbRow.getTable();
        assertTrue(table.isPKImported());
        assertEquals(0, table.getColumnNames().length);
    }

    @Test
    public void testGetPKValues3() {
        DBTable dbTable = new DBTable("Name");
        DBTable owner = new DBTable("Name");
        dbTable.addForeignKey(
                new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"));
        assertEquals(0, (new DBRow(dbTable)).getPKValues().length);
    }

    @Test
    public void testGetPKValues4() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
        assertEquals(3, (new DBRow(dbTable)).getPKValues().length);
    }

    @Test
    public void testGetPKValue() {
        DBRow dbRow = new DBRow(new DBTable("Name"));
        assertEquals(0, ((Object[]) dbRow.getPKValue()).length);
        DBTable table = dbRow.getTable();
        assertTrue(table.isPKImported());
        assertEquals(0, table.getColumnNames().length);
    }

    @Test
    public void testGetPKValue2() {
        DBRow dbRow = new DBRow(null);
        dbRow.withTable(new DBTable("Name"));
        assertEquals(0, ((Object[]) dbRow.getPKValue()).length);
        DBTable table = dbRow.getTable();
        assertTrue(table.isPKImported());
        assertEquals(0, table.getColumnNames().length);
    }

    @Test
    public void testGetPKValue3() {
        DBTable dbTable = new DBTable("Name");
        DBTable owner = new DBTable("Name");
        dbTable.addForeignKey(
                new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"));
        assertEquals(0, ((Object[]) (new DBRow(dbTable)).getPKValue()).length);
    }

    @Test
    public void testGetPKValue4() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
        assertEquals(3, ((Object[]) (new DBRow(dbTable)).getPKValue()).length);
    }

    @Test
    public void testGetPKValue5() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "Column Names"));
        assertNull((new DBRow(dbTable)).getPKValue());
    }

    @Test
    public void testGetFKValue() {
        DBRow dbRow = new DBRow(new DBTable("Name"));
        DBTable owner = new DBTable("Name");
        assertNull(dbRow.getFKValue(
                new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name")));
    }

    @Test
    public void testGetFKValue2() {
        DBRow dbRow = new DBRow(new DBTable("Name"));
        DBTable owner = new DBTable("Name");
        assertEquals(3, ((Object[]) dbRow.getFKValue(new DBForeignKeyConstraint("Name", true, owner,
                new String[]{"foo", "foo", "foo"}, new DBTable("Name"), new String[]{"foo", "foo", "foo"}))).length);
    }

    @Test
    public void testGetFKComponents() {
        DBRow dbRow = new DBRow(new DBTable("Name"));
        DBTable owner = new DBTable("Name");
        assertEquals(1, dbRow.getFKComponents(new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name")).length);
    }

    @Test
    public void testGetCellValue() {
        assertNull((new DBRow(new DBTable("Name"))).getCellValue("Column Name"));
    }

    @Test
    public void testToString() {
        assertEquals("Name[]", (new DBRow(new DBTable("Name"))).toString());
    }

    @Test
    public void testToString2() {
        DBRow dbRow = new DBRow(null);
        dbRow.withTable(new DBTable("Name"));
        assertEquals("Name[]", dbRow.toString());
    }
}

