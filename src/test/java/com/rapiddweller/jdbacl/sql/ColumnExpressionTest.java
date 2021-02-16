package com.rapiddweller.jdbacl.sql;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * The type Column expression test.
 */
public class ColumnExpressionTest {
  /**
   * Test get column name.
   */
  @Test
  public void testGetColumnName() {
    assertEquals("Name", (new ColumnExpression("Name", true)).getColumnName());
  }

  /**
   * Test to string.
   */
  @Test
  public void testToString() {
    assertEquals("\"Name\"", (new ColumnExpression("Name", true)).toString());
    assertEquals("Name", (new ColumnExpression("Name", false)).toString());
  }
}

