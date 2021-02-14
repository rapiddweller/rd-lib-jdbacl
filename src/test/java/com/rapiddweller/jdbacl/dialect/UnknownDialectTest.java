package com.rapiddweller.jdbacl.dialect;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * The type Unknown dialect test.
 */
public class UnknownDialectTest {
  /**
   * Test constructor.
   */
  @Test
  public void testConstructor() {
    UnknownDialect actualUnknownDialect = new UnknownDialect("System");
    assertEquals("System", actualUnknownDialect.getSystem());
    assertFalse(actualUnknownDialect.quoteTableNames);
    assertFalse(actualUnknownDialect.isSequenceBoundarySupported());
  }
}

