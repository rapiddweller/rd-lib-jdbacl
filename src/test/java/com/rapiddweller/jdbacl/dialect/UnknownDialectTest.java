package com.rapiddweller.jdbacl.dialect;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests the {@link UnknownDialect}.
 */
public class UnknownDialectTest {
  @Test
  public void testConstructor() {
    UnknownDialect actualUnknownDialect = new UnknownDialect("System");
    assertEquals("System", actualUnknownDialect.getDbType());
    assertFalse(actualUnknownDialect.quoteTableNames);
    assertFalse(actualUnknownDialect.isSequenceBoundarySupported());
  }
}

