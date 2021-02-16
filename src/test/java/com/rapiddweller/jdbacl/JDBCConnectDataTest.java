package com.rapiddweller.jdbacl;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * The type Jdbc connect data test.
 */
public class JDBCConnectDataTest {
  /**
   * Test parse single db properties.
   *
   * @throws IOException the io exception
   */
  @Test
  public void testParseSingleDbProperties() throws IOException {
    JDBCConnectData actualParseSingleDbPropertiesResult = JDBCConnectData.parseSingleDbProperties("string://");
    assertFalse(actualParseSingleDbPropertiesResult.readOnly);
    assertNull(actualParseSingleDbPropertiesResult.catalog);
    assertNull(actualParseSingleDbPropertiesResult.driver);
    assertNull(actualParseSingleDbPropertiesResult.password);
    assertNull(actualParseSingleDbPropertiesResult.schema);
    assertNull(actualParseSingleDbPropertiesResult.url);
    assertNull(actualParseSingleDbPropertiesResult.user);
  }
}

