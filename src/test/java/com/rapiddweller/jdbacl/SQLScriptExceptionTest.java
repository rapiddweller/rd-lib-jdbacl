package com.rapiddweller.jdbacl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * The type Sql script exception test.
 */
public class SQLScriptExceptionTest {
  /**
   * Test constructor.
   */
  @Test
  public void testConstructor() {
    assertEquals(2, (new SQLScriptException(2)).getLineNo());
    assertEquals(2, (new SQLScriptException(new Throwable(), 2)).getLineNo());
  }

  /**
   * Test constructor 2.
   */
  @Test
  public void testConstructor2() {
    SQLScriptException actualSqlScriptException = new SQLScriptException(new Throwable(), "Uri", 2);
    assertEquals("com.rapiddweller.jdbacl.SQLScriptException: Error in execution of script Uri line 2: null",
        actualSqlScriptException.toString());
    assertEquals(2, actualSqlScriptException.getLineNo());
  }

  /**
   * Test with uri.
   */
  @Test
  public void testWithUri() {
    SQLScriptException sqlScriptException = new SQLScriptException(2);
    SQLScriptException actualWithUriResult = sqlScriptException.withUri("Uri");
    assertSame(sqlScriptException, actualWithUriResult);
    assertEquals("com.rapiddweller.jdbacl.SQLScriptException: Error in execution of script Uri line 2: ",
        actualWithUriResult.toString());
  }

  /**
   * Test get message.
   */
  @Test
  public void testGetMessage() {
    assertEquals("Error in execution of script line 2: ", (new SQLScriptException(2)).getMessage());
    assertEquals("Error in execution of script line 2: null",
        (new SQLScriptException(new Throwable(), 2)).getMessage());
    assertEquals("Error in execution of script Uri line 2: null",
        (new SQLScriptException(new Throwable(), "Uri", 2)).getMessage());
  }
}

