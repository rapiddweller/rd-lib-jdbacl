package com.rapiddweller.jdbacl.sql;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ColumnExpressionTest {
    @Test
    public void testGetColumnName() {
        assertEquals("Name", (new ColumnExpression("Name", true)).getColumnName());
    }

    @Test
    public void testToString() {
        assertEquals("\"Name\"", (new ColumnExpression("Name", true)).toString());
        assertEquals("Name", (new ColumnExpression("Name", false)).toString());
    }
}

