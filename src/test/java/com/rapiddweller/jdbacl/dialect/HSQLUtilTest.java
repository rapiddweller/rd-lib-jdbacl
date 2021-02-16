package com.rapiddweller.jdbacl.dialect;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * The type Hsql util test.
 */
public class HSQLUtilTest {
  /**
   * Test get in memory url.
   */
  @Test
  public void testGetInMemoryURL() {
    assertEquals("jdbc:hsqldb:mem:https://example.org/example", HSQLUtil.getInMemoryURL("https://example.org/example"));
  }
}

