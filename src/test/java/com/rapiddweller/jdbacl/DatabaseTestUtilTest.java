package com.rapiddweller.jdbacl;

import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * The type Database test util test.
 * @author Volker Bergmann
 */
public class DatabaseTestUtilTest {
  @Test
  public void testGetConnectData() {
    assertNull(DatabaseTestUtil.getConnectData("Environment", "."));
  }
}

