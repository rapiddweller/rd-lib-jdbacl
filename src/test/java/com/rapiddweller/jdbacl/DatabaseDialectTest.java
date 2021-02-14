package com.rapiddweller.jdbacl;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The type Database dialect test.
 */
public class DatabaseDialectTest {
  /**
   * Test is not one.
   */
  @Test
  public void testIsNotOne() {
    assertTrue(DatabaseDialect.isNotOne(BigInteger.valueOf(42L)));
    assertFalse(DatabaseDialect.isNotOne(BigInteger.valueOf(1L)));
    assertTrue(DatabaseDialect.isNotOne(new BigInteger(
        new byte[] {88, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65, 65})));
  }
}

