package com.rapiddweller.jdbacl.dialect;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class H2UtilTest {
    @Test
    public void testGetInMemoryURL() {
        assertEquals("jdbc:h2:mem:https://example.org/example", H2Util.getInMemoryURL("https://example.org/example"));
    }
}

