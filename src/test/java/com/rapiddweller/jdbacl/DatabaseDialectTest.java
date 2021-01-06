package com.rapiddweller.jdbacl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

public class DatabaseDialectTest {
    @Test
    public void testIsNotOne() {
        assertTrue(DatabaseDialect.isNotOne(BigInteger.valueOf(42L)));
        assertFalse(DatabaseDialect.isNotOne(BigInteger.valueOf(1L)));
        assertTrue(DatabaseDialect.isNotOne(new BigInteger(
                new byte[]{88, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65})));
    }
}

