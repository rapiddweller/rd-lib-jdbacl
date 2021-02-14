package com.rapiddweller.jdbacl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * The type Column info test.
 */
public class ColumnInfoTest {
  /**
   * Test to string.
   */
  @Test
  public void testToString() {
    assertEquals("Name: class java.lang.Object", (new ColumnInfo("Name", 1, Object.class)).toString());
  }
}

