package com.rapiddweller.jdbacl.dialect;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * The type H 2 util test.
 */
public class H2UtilTest {
  /**
   * Test get in memory url.
   */
  @Test
  public void testGetInMemoryURL() {
    assertEquals("jdbc:h2:mem:https://example.org/example", H2Util.getInMemoryURL("https://example.org/example"));
  }
}

