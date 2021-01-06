package com.rapiddweller.jdbacl.dialect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class UnknownDialectTest {
    @Test
    public void testConstructor() {
        UnknownDialect actualUnknownDialect = new UnknownDialect("System");
        assertEquals("System", actualUnknownDialect.getSystem());
        assertFalse(actualUnknownDialect.quoteTableNames);
        assertFalse(actualUnknownDialect.isSequenceBoundarySupported());
    }
}

