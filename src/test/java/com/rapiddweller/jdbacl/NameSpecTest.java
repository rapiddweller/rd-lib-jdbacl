package com.rapiddweller.jdbacl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NameSpecTest {
    @Test
    public void testValueOf() {
        assertEquals(NameSpec.ALWAYS, NameSpec.valueOf("ALWAYS"));
    }

    @Test
    public void testValues() {
        assertEquals(3, NameSpec.values().length);
    }
}

