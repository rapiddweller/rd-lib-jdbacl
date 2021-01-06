package com.rapiddweller.jdbacl.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

public class DBSequenceTest {
    @Test
    public void testConstructor() {
        DBSequence actualDbSequence = new DBSequence("Name", null);
        assertEquals("Name", actualDbSequence.getName());
        BigInteger increment = actualDbSequence.getIncrement();
        BigInteger sqrtResult = increment.sqrt();
        String actualToStringResult = actualDbSequence.getLastNumber().toString();
        assertNull(actualDbSequence.getOwner());
        assertEquals("sequence", actualDbSequence.getObjectType());
        assertNull(actualDbSequence.getStartIfNotDefault());
        assertNull(actualDbSequence.getMinValue());
        assertNull(actualDbSequence.getMaxValueIfNotDefault());
        assertNull(actualDbSequence.getIncrementIfNotDefault());
        assertEquals("0", actualToStringResult);
        assertEquals("1", increment.toString());
        assertEquals("1", sqrtResult.toString());
        BigInteger sqrtResult1 = sqrtResult.sqrt();
        assertEquals("1", sqrtResult1.toString());
        BigInteger sqrtResult2 = sqrtResult1.sqrt();
        assertEquals("1", sqrtResult2.toString());
        BigInteger sqrtResult3 = sqrtResult2.sqrt();
        assertEquals("1", sqrtResult3.toString());
        BigInteger sqrtResult4 = sqrtResult3.sqrt();
        assertEquals("1", sqrtResult4.toString());
        assertEquals("1", sqrtResult4.sqrt().toString());
    }

    @Test
    public void testConstructor2() {
        DBSequence actualDbSequence = new DBSequence("Name",
                new DBSchema("Name", new DBCatalog("Name", AbstractModelTest.createTestModel())));
        assertEquals("Name", actualDbSequence.getName());
        BigInteger increment = actualDbSequence.getIncrement();
        BigInteger sqrtResult = increment.sqrt();
        String actualToStringResult = actualDbSequence.getLastNumber().toString();
        CompositeDBObject<?> expectedOwner = actualDbSequence.owner;
        assertSame(expectedOwner, actualDbSequence.getOwner());
        assertEquals("sequence", actualDbSequence.getObjectType());
        assertEquals("Name", actualDbSequence.getCatalogName());
        assertEquals("Name", actualDbSequence.getSchemaName());
        assertNull(actualDbSequence.getStartIfNotDefault());
        assertNull(actualDbSequence.getMaxValue());
        assertNull(actualDbSequence.getIncrementIfNotDefault());
        assertNull(actualDbSequence.getMinValueIfNotDefault());
        String actualToStringResult1 = sqrtResult.toString();
        assertEquals("1", increment.toString());
        assertEquals("0", actualToStringResult);
        assertEquals("1", actualToStringResult1);
    }

    @Test
    public void testConstructor3() {
        DBSequence actualDbSequence = new DBSequence("Name", "Catalog Name", "Schema Name");
        assertEquals("Name", actualDbSequence.getName());
        BigInteger increment = actualDbSequence.getIncrement();
        BigInteger sqrtResult = increment.sqrt();
        String actualToStringResult = actualDbSequence.getLastNumber().toString();
        assertNull(actualDbSequence.getOwner());
        assertEquals("sequence", actualDbSequence.getObjectType());
        assertEquals("Catalog Name", actualDbSequence.getCatalogName());
        assertEquals("Schema Name", actualDbSequence.getSchemaName());
        assertNull(actualDbSequence.getStartIfNotDefault());
        assertNull(actualDbSequence.getMinValue());
        assertNull(actualDbSequence.getMaxValueIfNotDefault());
        assertNull(actualDbSequence.getIncrementIfNotDefault());
        assertEquals("0", actualToStringResult);
        assertEquals("1", increment.toString());
        assertEquals("1", sqrtResult.toString());
        BigInteger sqrtResult1 = sqrtResult.sqrt();
        assertEquals("1", sqrtResult1.toString());
        BigInteger sqrtResult2 = sqrtResult1.sqrt();
        assertEquals("1", sqrtResult2.toString());
        BigInteger sqrtResult3 = sqrtResult2.sqrt();
        assertEquals("1", sqrtResult3.toString());
        BigInteger sqrtResult4 = sqrtResult3.sqrt();
        assertEquals("1", sqrtResult4.toString());
        assertEquals("1", sqrtResult4.sqrt().toString());
    }

    @Test
    public void testSetOwner() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setOwner(null);
        assertNull(dbSequence.getOwner());
    }

    @Test
    public void testSetOwner2() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setOwner(null);
        assertNull(dbSequence.getOwner());
    }

    @Test
    public void testSetOwner3() {
        DBSequence dbSequence = new DBSequence("Name", null);
        DBSchema dbSchema = new DBSchema("Name", new DBCatalog());
        dbSequence.setOwner(dbSchema);
        assertSame(dbSchema, dbSequence.getOwner());
        assertNull(dbSequence.getCatalogName());
        assertEquals("Name", dbSequence.getSchemaName());
    }

    @Test
    public void testGetStartIfNotDefault() {
        assertNull((new DBSequence("Name", null)).getStartIfNotDefault());
    }

    @Test
    public void testGetStartIfNotDefault2() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setStart(BigInteger.valueOf(42L));
        BigInteger actualStartIfNotDefault = dbSequence.getStartIfNotDefault();
        assertEquals("42", actualStartIfNotDefault.toString());
        BigInteger sqrtResult = actualStartIfNotDefault.sqrt();
        assertEquals("6", sqrtResult.toString());
        BigInteger sqrtResult1 = sqrtResult.sqrt();
        assertEquals("2", sqrtResult1.toString());
        BigInteger sqrtResult2 = sqrtResult1.sqrt();
        assertEquals("1", sqrtResult2.toString());
        BigInteger sqrtResult3 = sqrtResult2.sqrt();
        assertEquals("1", sqrtResult3.toString());
        BigInteger sqrtResult4 = sqrtResult3.sqrt();
        assertEquals("1", sqrtResult4.toString());
        BigInteger sqrtResult5 = sqrtResult4.sqrt();
        assertEquals("1", sqrtResult5.toString());
        BigInteger sqrtResult6 = sqrtResult5.sqrt();
        assertEquals("1", sqrtResult6.toString());
        assertEquals("1", sqrtResult6.sqrt().toString());
    }

    @Test
    public void testSetStart() {
        DBSequence dbSequence = new DBSequence("Name", null);
        BigInteger valueOfResult = BigInteger.valueOf(42L);
        dbSequence.setStart(valueOfResult);
        assertSame(valueOfResult, dbSequence.getStart());
    }

    @Test
    public void testGetIncrementIfNotDefault() {
        assertNull((new DBSequence("Name", null)).getIncrementIfNotDefault());
    }

    @Test
    public void testGetIncrementIfNotDefault2() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setIncrement(BigInteger.valueOf(42L));
        BigInteger actualIncrementIfNotDefault = dbSequence.getIncrementIfNotDefault();
        assertEquals("42", actualIncrementIfNotDefault.toString());
        BigInteger sqrtResult = actualIncrementIfNotDefault.sqrt();
        assertEquals("6", sqrtResult.toString());
        BigInteger sqrtResult1 = sqrtResult.sqrt();
        assertEquals("2", sqrtResult1.toString());
        BigInteger sqrtResult2 = sqrtResult1.sqrt();
        assertEquals("1", sqrtResult2.toString());
        BigInteger sqrtResult3 = sqrtResult2.sqrt();
        assertEquals("1", sqrtResult3.toString());
        BigInteger sqrtResult4 = sqrtResult3.sqrt();
        assertEquals("1", sqrtResult4.toString());
        BigInteger sqrtResult5 = sqrtResult4.sqrt();
        assertEquals("1", sqrtResult5.toString());
        BigInteger sqrtResult6 = sqrtResult5.sqrt();
        assertEquals("1", sqrtResult6.toString());
        assertEquals("1", sqrtResult6.sqrt().toString());
    }

    @Test
    public void testSetMinValue() {
        DBSequence dbSequence = new DBSequence("Name", null);
        BigInteger valueOfResult = BigInteger.valueOf(42L);
        dbSequence.setMinValue(valueOfResult);
        assertSame(valueOfResult, dbSequence.getMinValueIfNotDefault());
    }

    @Test
    public void testSetCycle() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setCycle(true);
        assertTrue(dbSequence.isCycle());
    }

    @Test
    public void testSetCache() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setCache(1L);
        assertEquals(1L, dbSequence.getCache().longValue());
    }

    @Test
    public void testSetOrder() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setOrder(true);
        assertTrue(dbSequence.isOrder());
    }

    @Test
    public void testIsIdentical() {
        DBSequence dbSequence = new DBSequence("Name", null);
        assertFalse(dbSequence.isIdentical(new DBCatalog()));
    }

    @Test
    public void testIsIdentical10() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setStart(BigInteger.valueOf(42L));
        assertFalse(dbSequence.isIdentical(new DBSequence("Name", "Catalog Name", "Schema Name")));
    }

    @Test
    public void testIsIdentical2() {
        DBSequence dbSequence = new DBSequence("Name", null);
        assertTrue(dbSequence.isIdentical(new DBSequence("Name", "Catalog Name", "Schema Name")));
    }

    @Test
    public void testIsIdentical3() {
        DBSequence dbSequence = new DBSequence("com.rapiddweller.jdbacl.model.DBSchema", null);
        assertFalse(dbSequence.isIdentical(new DBSequence("Name", "Catalog Name", "Schema Name")));
    }

    @Test
    public void testIsIdentical4() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setMinValue(BigInteger.valueOf(42L));
        assertFalse(dbSequence.isIdentical(new DBSequence("Name", "Catalog Name", "Schema Name")));
    }

    @Test
    public void testIsIdentical5() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setCycle(true);
        assertFalse(dbSequence.isIdentical(new DBSequence("Name", "Catalog Name", "Schema Name")));
    }

    @Test
    public void testIsIdentical6() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setOrder(true);
        assertFalse(dbSequence.isIdentical(new DBSequence("Name", "Catalog Name", "Schema Name")));
    }

    @Test
    public void testIsIdentical7() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setIncrement(BigInteger.valueOf(42L));
        assertFalse(dbSequence.isIdentical(new DBSequence("Name", "Catalog Name", "Schema Name")));
    }

    @Test
    public void testIsIdentical8() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setMaxValue(BigInteger.valueOf(42L));
        assertFalse(dbSequence.isIdentical(new DBSequence("Name", "Catalog Name", "Schema Name")));
    }

    @Test
    public void testIsIdentical9() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setCache(0L);
        assertFalse(dbSequence.isIdentical(new DBSequence("Name", "Catalog Name", "Schema Name")));
    }

    @Test
    public void testDropDDL() {
        assertEquals("drop sequence Name", (new DBSequence("Name", null)).dropDDL());
    }
}

