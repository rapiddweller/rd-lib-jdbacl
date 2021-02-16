package com.rapiddweller.jdbacl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * The type Name spec test.
 */
public class NameSpecTest {
  /**
   * Test value of.
   */
  @Test
  public void testValueOf() {
    assertEquals(NameSpec.ALWAYS, NameSpec.valueOf("ALWAYS"));
  }

  /**
   * Test values.
   */
  @Test
  public void testValues() {
    assertEquals(3, NameSpec.values().length);
  }
}

