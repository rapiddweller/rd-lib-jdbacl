package com.rapiddweller.jdbacl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

public class JDBCConnectDataTest {
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

