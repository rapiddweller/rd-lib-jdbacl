/*
 * (c) Copyright 2011 by Volker Bergmann. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted under the terms of the
 * GNU General Public License (GPL).
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED CONDITIONS,
 * REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE
 * HEREBY EXCLUDED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.rapiddweller.jdbacl.model;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Types;

import org.junit.Test;

/**
 * Tests the {@link DBDataType}.<br/><br/>
 * Created: 14.06.2011 16:40:11
 *
 * @author Volker Bergmann
 * @since 0.6.8
 */
public class DBDataTypeTest {

    @Test
    public void testGetInstanceByDescriptor() {
        DBDataType type1 = DBDataType.getInstance(Types.INTEGER, "INTEGER");
        DBDataType type2 = DBDataType.getInstance(Types.INTEGER, "INTEGER");
        assertTrue(type1 == type2);
    }

    @Test
    public void testGetInstanceByName() {
        DBDataType type1 = DBDataType.getInstance("INTEGER");
        DBDataType type2 = DBDataType.getInstance("INTEGER");
        assertTrue(type1 == type2);
    }

    @Test
    public void testJdbcTypeFor() {
        assertEquals(Types.INTEGER, DBDataType.jdbcTypeFor("INTEGER"));
        assertEquals(Types.VARCHAR, DBDataType.jdbcTypeFor("VARCHAR"));
        assertEquals(Types.VARCHAR, DBDataType.jdbcTypeFor("VARCHAR2"));
        assertEquals(Types.NVARCHAR, DBDataType.jdbcTypeFor("NVARCHAR"));
        assertEquals(2004, DBDataType.jdbcTypeFor("BLOB"));
    }

    @Test
    public void testIsLOB() {
        assertTrue(DBDataType.getInstance("BLOB").isLOB());
        assertTrue(DBDataType.getInstance("CLOB").isLOB());
        assertFalse(DBDataType.getInstance("DATE").isLOB());
        assertTrue(DBDataType.getInstance("NCLOB").isLOB());
    }

    @Test
    public void testIsVarChar() {
        assertFalse(DBDataType.getInstance("BLOB").isVarChar());
        assertTrue(DBDataType.getInstance("VARCHAR2").isVarChar());
    }

    @Test
    public void testIsAlpha() {
        assertFalse(DBDataType.getInstance("BLOB").isAlpha());
        assertTrue(DBDataType.getInstance("CLOB").isAlpha());
    }

    @Test
    public void testIsNumber() {
        assertFalse(DBDataType.getInstance("BLOB").isNumber());
    }

    @Test
    public void testIsInteger() {
        assertFalse(DBDataType.getInstance("BLOB").isInteger());
    }

    @Test
    public void testIsDecimal() {
        assertFalse(DBDataType.getInstance("BLOB").isDecimal());
    }

    @Test
    public void testIsTemporal() {
        assertFalse(DBDataType.getInstance("BLOB").isTemporal());
        assertTrue(DBDataType.getInstance("DATE").isTemporal());
        assertTrue(DBDataType.getInstance("TIME").isTemporal());
    }

    @Test
    public void testEquals() {
        assertFalse(DBDataType.getInstance("BLOB").equals("o"));
        assertFalse(DBDataType.getInstance("BLOB").equals(null));
    }

    @Test
    public void testHashCode() {
        assertEquals(2041757, DBDataType.getInstance("BLOB").hashCode());
    }

}
