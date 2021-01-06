package com.rapiddweller.jdbacl;

import static org.junit.Assert.assertNull;

import org.junit.Test;

public class DatabaseTestUtilTest {
    @Test
    public void testGetConnectData() {
        assertNull(DatabaseTestUtil.getConnectData("Environment"));
    }
}

