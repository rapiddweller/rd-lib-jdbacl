/*
 * (c) Copyright 2010-2011 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.dialect;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the {@link SqlServerDialect}.<br/><br/>
 * Created: 09.04.2010 07:51:17
 *
 * @author Volker Bergmann
 * @since 0.6.0
 */
public class SqlServerDialectTest extends DatabaseDialectTest<SqlServerDialect> {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConstructor() {
        SqlServerDialect actualSqlServerDialect = new SqlServerDialect();
        assertEquals("sql_server", actualSqlServerDialect.getSystem());
        assertTrue(actualSqlServerDialect.quoteTableNames);
        assertFalse(actualSqlServerDialect.isSequenceSupported());
    }

    @Test
    public void testIsDefaultSchema() {
        assertFalse((new SqlServerDialect()).isDefaultSchema("Schema", "User"));
        assertTrue((new SqlServerDialect()).isDefaultSchema("DBO", "User"));
    }

    public SqlServerDialectTest() {
        super(new SqlServerDialect());
    }

    @Test
    public void testSequenceSupported() {
        assertFalse(dialect.isSequenceSupported());
    }

    @Test
    public void testFormatDate() {
        assertEquals("'1971-02-03T13:14:15'", dialect.formatValue(DATETIME_19710203131415));
    }

    @Test
    public void testFormatTime() {
        assertEquals("'13:14:15'", dialect.formatValue(TIME_131415));
    }

    @Test
    public void testIsDeterministicPKName() {
        assertFalse(dialect.isDeterministicPKName("SYS_XYZ"));
        assertTrue(dialect.isDeterministicPKName("USER_PK"));
        assertTrue((new SqlServerDialect()).isDeterministicPKName("Pk Name"));
        assertFalse((new SqlServerDialect()).isDeterministicPKName("SYS_U"));
    }

    @Test
    public void testIsDeterministicUKName() {
        assertFalse(dialect.isDeterministicUKName("SYS_XYZ"));
        assertTrue(dialect.isDeterministicUKName("USER_NAME_UK"));
        assertTrue((new SqlServerDialect()).isDeterministicUKName("Pk Name"));
        assertFalse((new SqlServerDialect()).isDeterministicUKName("SYS_U"));
    }

    @Test
    public void testIsDeterministicFKName() {
        assertFalse(dialect.isDeterministicFKName("SYS_XYZ"));
        assertTrue(dialect.isDeterministicFKName("USER_ROLE_FK"));
        assertTrue((new SqlServerDialect()).isDeterministicFKName("Pk Name"));
        assertFalse((new SqlServerDialect()).isDeterministicFKName("SYS_U"));
    }

    @Test
    public void testIsDeterministicIndexName() {
        assertFalse(dialect.isDeterministicIndexName("SYS_XYZ"));
        assertTrue(dialect.isDeterministicIndexName("USER_NAME_IDX"));
        assertTrue((new SqlServerDialect()).isDeterministicIndexName("Index Name"));
        assertFalse((new SqlServerDialect()).isDeterministicIndexName("SYS_U"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRegex() {
        assertFalse(dialect.supportsRegex());
        dialect.regexQuery("code", false, "[A-Z]{4}");
    }

    @Test
    public void testRenderCase() {
        assertEquals("col = CASE WHEN condition1 THEN result1 WHEN condition2 THEN result2 ELSE result4 END",
                dialect.renderCase("col", "result4", "condition1", "result1", "condition2", "result2"));
        assertEquals("Column Name = CASE ELSE Else Expression END",
                (new SqlServerDialect()).renderCase("Column Name", "Else Expression"));
        assertEquals("Column Name = CASE END", (new SqlServerDialect()).renderCase("Column Name", ""));
    }

    @Test
    public void testRenderCase2() {
        thrown.expect(ArrayIndexOutOfBoundsException.class);
        (new SqlServerDialect()).renderCase("Column Name", "Else Expression", "foo", "foo", "foo");
    }

}
