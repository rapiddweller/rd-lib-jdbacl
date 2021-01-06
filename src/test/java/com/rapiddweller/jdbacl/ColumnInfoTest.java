package com.rapiddweller.jdbacl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ColumnInfoTest {
    @Test
    public void testToString() {
        assertEquals("Name: class java.lang.Object", (new ColumnInfo("Name", 1, Object.class)).toString());
    }
}

