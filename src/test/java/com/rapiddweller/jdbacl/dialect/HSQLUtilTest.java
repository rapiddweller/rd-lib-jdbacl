package com.rapiddweller.jdbacl.dialect;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HSQLUtilTest {
    @Test
    public void testGetInMemoryURL() {
        assertEquals("jdbc:hsqldb:mem:https://example.org/example", HSQLUtil.getInMemoryURL("https://example.org/example"));
    }
}

