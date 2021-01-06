package com.rapiddweller.jdbacl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class SQLScriptExceptionTest {
    @Test
    public void testConstructor() {
        assertEquals(2, (new SQLScriptException(2)).getLineNo());
        assertEquals(2, (new SQLScriptException(new Throwable(), 2)).getLineNo());
    }

    @Test
    public void testConstructor2() {
        SQLScriptException actualSqlScriptException = new SQLScriptException(new Throwable(), "Uri", 2);
        assertEquals("com.rapiddweller.jdbacl.SQLScriptException: Error in execution of script Uri line 2: null",
                actualSqlScriptException.toString());
        assertEquals(2, actualSqlScriptException.getLineNo());
    }

    @Test
    public void testWithUri() {
        SQLScriptException sqlScriptException = new SQLScriptException(2);
        SQLScriptException actualWithUriResult = sqlScriptException.withUri("Uri");
        assertSame(sqlScriptException, actualWithUriResult);
        assertEquals("com.rapiddweller.jdbacl.SQLScriptException: Error in execution of script Uri line 2: ",
                actualWithUriResult.toString());
    }

    @Test
    public void testGetMessage() {
        assertEquals("Error in execution of script line 2: ", (new SQLScriptException(2)).getMessage());
        assertEquals("Error in execution of script line 2: null",
                (new SQLScriptException(new Throwable(), 2)).getMessage());
        assertEquals("Error in execution of script Uri line 2: null",
                (new SQLScriptException(new Throwable(), "Uri", 2)).getMessage());
    }
}

