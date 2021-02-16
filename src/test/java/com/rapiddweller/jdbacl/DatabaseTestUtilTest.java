package com.rapiddweller.jdbacl;

import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * The type Database test util test.
 */
public class DatabaseTestUtilTest {
  /**
   * Test get connect data.
   */
  @Test
  public void testGetConnectData() {
    assertNull(DatabaseTestUtil.getConnectData("Environment"));
  }
}

