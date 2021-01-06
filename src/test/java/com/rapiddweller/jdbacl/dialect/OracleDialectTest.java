/*
 * (c) Copyright 2009-2011 by Volker Bergmann. All rights reserved.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import com.rapiddweller.common.TimeUtil;
import com.rapiddweller.jdbacl.model.DBSequence;
import com.rapiddweller.jdbacl.sql.Query;
import org.junit.Test;

/**
 * Tests the {@link OracleDialect}.<br/><br/>
 * Created: 10.11.2009 17:22:59
 *
 * @author Volker Bergmann
 * @since 0.6.0
 */
public class OracleDialectTest extends DatabaseDialectTest<OracleDialect> {

    @Test
    public void testConstructor() {
        OracleDialect actualOracleDialect = new OracleDialect();
        assertEquals("oracle", actualOracleDialect.getSystem());
        assertTrue(actualOracleDialect.quoteTableNames);
        assertTrue(actualOracleDialect.isSequenceSupported());
    }

    @Test
    public void testIsDefaultCatalog() {
        assertFalse((new OracleDialect()).isDefaultCatalog("Catalog", "User"));
        assertTrue((new OracleDialect()).isDefaultCatalog(null, "User"));
    }

    @Test
    public void testIsDefaultSchema() {
        assertFalse((new OracleDialect()).isDefaultSchema("Schema", "User"));
        assertTrue((new OracleDialect()).isDefaultSchema("SYS_C\\d{8}", "SYS_C\\d{8}"));
    }

    public OracleDialectTest() {
        super(new OracleDialect());
    }

    @Test
    public void testFormatDate() {
        Date date = TimeUtil.date(1971, 1, 3);
        assertEquals("to_date('1971-02-03', 'yyyy-mm-dd')", dialect.formatValue(date));
    }

    @Test
    public void testFormatDatetime() {
        Date date = TimeUtil.date(1971, 1, 3, 13, 14, 15, 0);
        assertEquals("to_date('1971-02-03 13:14:15', 'yyyy-mm-dd HH24:mi:ss')", dialect.formatValue(date));
    }

    @Test
    public void testFormatTime() {
        Time time = TimeUtil.time(13, 14, 15, 123);
        assertEquals("to_date('13:14:15', 'HH24:mi:ss')", dialect.formatValue(time));
    }

    @Test
    public void testFormatTimestamp() {
        Timestamp timestamp = TimeUtil.timestamp(1971, 1, 3, 13, 14, 15, 123456789);
        assertEquals("to_timestamp('1971-02-03 13:14:15.123456789', 'yyyy-mm-dd HH24:mi:ss.FF')",
                dialect.formatValue(timestamp));
    }

    @Test
    public void testIsDeterministicPKName() {
        assertFalse(dialect.isDeterministicPKName("SYS_C00208398"));
        assertTrue(dialect.isDeterministicPKName("USER_PK"));
        assertTrue((new OracleDialect()).isDeterministicPKName("Pk Name"));
        assertFalse((new OracleDialect()).isDeterministicPKName("SYS_C99999999"));
    }

    @Test
    public void testIsDeterministicUKName() {
        assertFalse(dialect.isDeterministicUKName("SYS_C00208396"));
        assertTrue(dialect.isDeterministicUKName("USER_NAME_UK"));
        assertTrue((new OracleDialect()).isDeterministicUKName("Uk Name"));
        assertFalse((new OracleDialect()).isDeterministicUKName("SYS_C99999999"));
    }

    @Test
    public void testIsDeterministicFKName() {
        assertFalse(dialect.isDeterministicFKName("SYS_C00208399"));
        assertTrue(dialect.isDeterministicFKName("USER_ROLE_FK"));
        assertTrue((new OracleDialect()).isDeterministicFKName("Fk Name"));
        assertFalse((new OracleDialect()).isDeterministicFKName("SYS_C99999999"));
    }

    @Test
    public void testIsDeterministicIndexName() {
        assertFalse(dialect.isDeterministicIndexName("SYS_C00208398"));
        assertTrue(dialect.isDeterministicIndexName("USER_NAME_IDX"));
        assertTrue((new OracleDialect()).isDeterministicIndexName("Index Name"));
        assertFalse((new OracleDialect()).isDeterministicIndexName("SYS_C99999999"));
    }

    @Test
    public void testSupportsRegex() {
        assertTrue((new OracleDialect()).supportsRegex());
    }

    @Test
    public void testRegexQuery() {
        assertEquals("NOT REGEXP_LIKE(Expression, 'Regex')", (new OracleDialect()).regexQuery("Expression", true, "Regex"));
        assertEquals("REGEXP_LIKE(Expression, 'Regex')", (new OracleDialect()).regexQuery("Expression", false, "Regex"));
    }

    @Test
    public void testIsDeterministicCheckName() {
        assertFalse(dialect.isDeterministicCheckName("SYS_C00208394"));
        assertTrue(dialect.isDeterministicCheckName("USER_NAME_NOT_NULL"));
        assertTrue((new OracleDialect()).isDeterministicCheckName("Check Name"));
        assertFalse((new OracleDialect()).isDeterministicCheckName("SYS_C99999999"));
    }

    @Test
    public void testRegex() {
        assertTrue(dialect.supportsRegex());
        assertEquals("REGEXP_LIKE(code, '[A-Z]{5}')", dialect.regexQuery("code", false, "[A-Z]{5}"));
        assertEquals("NOT REGEXP_LIKE(code, '[A-Z]{5}')", dialect.regexQuery("code", true, "[A-Z]{5}"));
    }

    @Test
    public void testRenderCreateSequence() {
        assertEquals("CREATE SEQUENCE \"my_seq\"", dialect.renderCreateSequence(new DBSequence("my_seq", null)));
        assertEquals("CREATE SEQUENCE \"my_seq\" START WITH 10 INCREMENT BY 2 MAXVALUE 999 MINVALUE 5 CYCLE CACHE 3 ORDER",
                dialect.renderCreateSequence(createConfiguredSequence()));
    }

    @Test
    public void testRenderCreateSequence2() {
        DBSequence sequence = new DBSequence("Name", null);
        assertEquals("CREATE SEQUENCE \"Name\"", (new OracleDialect()).renderCreateSequence(sequence));
    }

    @Test
    public void testRenderCreateSequence3() {
        DBSequence sequence = new DBSequence("Name", "Catalog Name", "Schema Name");
        assertEquals("CREATE SEQUENCE \"Schema Name\".\"Name\"", (new OracleDialect()).renderCreateSequence(sequence));
    }

    @Test
    public void testRenderCreateSequence4() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setOrder(true);
        assertEquals("CREATE SEQUENCE \"Name\" ORDER", (new OracleDialect()).renderCreateSequence(dbSequence));
    }

    @Test
    public void testRenderCreateSequence5() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setCache(0L);
        assertEquals("CREATE SEQUENCE \"Name\" CACHE 0", (new OracleDialect()).renderCreateSequence(dbSequence));
    }

    @Test
    public void testRenderCreateSequence6() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setOrder(false);
        assertEquals("CREATE SEQUENCE \"Name\"NOORDER", (new OracleDialect()).renderCreateSequence(dbSequence));
    }

    @Test
    public void testRenderSequenceNameAndType() {
        DBSequence sequence = new DBSequence("Name", null);
        assertEquals("\"Name\"", (new OracleDialect()).renderSequenceNameAndType(sequence));
    }

    @Test
    public void testRenderSequenceNameAndType2() {
        DBSequence sequence = new DBSequence("Name", "Catalog Name", "Schema Name");
        assertEquals("\"Schema Name\".\"Name\"", (new OracleDialect()).renderSequenceNameAndType(sequence));
    }

    @Test
    public void testRenderFetchSequenceValue() {
        assertEquals("select SEQ.nextval from dual", dialect.renderFetchSequenceValue("SEQ"));
        assertEquals("select Sequence Name.nextval from dual",
                (new OracleDialect()).renderFetchSequenceValue("Sequence Name"));
    }

    @Test
    public void testFormatTimestamp2() {
        Timestamp value = new Timestamp(10L);
        assertEquals("to_timestamp('1970-01-01 01:00:00.010000000', 'yyyy-mm-dd HH24:mi:ss.FF')",
                (new OracleDialect()).formatTimestamp(value));
    }

    @Test
    public void testRenderDropSequence() {
        assertEquals("drop sequence SEQ", dialect.renderDropSequence("SEQ"));
    }

    @Test
    public void testRenderCase() {
        assertEquals("CASE WHEN condition1 THEN result1 WHEN condition2 THEN result2 ELSE result4 END AS col",
                dialect.renderCase("col", "result4", "condition1", "result1", "condition2", "result2"));
    }

    @Test
    public void testRestrictRowNums() {
        // test <=
        Query query = Query.select("x").from("TEST");
        dialect.restrictRownums(1, 100, query);
        assertEquals("SELECT x FROM TEST WHERE ROWNUM <= 100", query.toString());
        // test between
        query = Query.select("x").from("TEST");
        dialect.restrictRownums(100, 50, query);
        assertEquals("SELECT x FROM TEST WHERE ROWNUM BETWEEN 100 AND 150", query.toString());
    }

    @Test
    public void testRestrictRownums() {
        Query selectResult = Query.select("Selection");
        (new OracleDialect()).restrictRownums(1, 3, selectResult);
        assertEquals("SELECT Selection FROM  WHERE ROWNUM <= 3", selectResult.toString());
    }

    @Test
    public void testRestrictRownums2() {
        Query selectResult = Query.select("Selection");
        (new OracleDialect()).restrictRownums(2, 3, selectResult);
        assertEquals("SELECT Selection FROM  WHERE ROWNUM BETWEEN 2 AND 5", selectResult.toString());
    }

    @Test
    public void testTrim() {
        assertEquals("TRIM(Expression)", (new OracleDialect()).trim("Expression"));
    }

}
