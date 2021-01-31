/*
 * (c) Copyright 2012 by Volker Bergmann. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.rapiddweller.jdbacl.sql.Query;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;

import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.DatabaseTestUtil;
import com.rapiddweller.jdbacl.model.DBSequence;
import org.junit.Test;

/**
 * Tests the {@link DatabaseDialect} for CUBRID.<br/><br/>
 * Created: 16.04.2012 11:26:57
 *
 * @author Volker Bergmann
 * @since 0.8.2
 */
public class CubridDialectTest extends DatabaseDialectTest<CubridDialect> {

    @Test
    public void testConstructor() {
        CubridDialect actualCubridDialect = new CubridDialect();
        assertEquals("cubrid", actualCubridDialect.getSystem());
        assertTrue(actualCubridDialect.isSequenceBoundarySupported());
    }

    public CubridDialectTest() {
        super(new CubridDialect());
    }

    @Test
    public void testFormatDate() {
        assertEquals("'1971-02-03'", dialect.formatValue(DATE_19710203));
    }

    @Test
    public void testFormatDatetime() {
        assertEquals("'1971-02-03 13:14:15'", dialect.formatValue(DATETIME_19710203131415));
    }

    @Test
    public void testFormatTime() {
        assertEquals("'13:14:15'", dialect.formatValue(TIME_131415));
    }

    @Test
    public void testFormatTimestamp() {
        assertEquals("'1971-02-03 13:14:15.123456789'",
                dialect.formatValue(TIMESTAMP_19710203131415123456789));
    }

    @Test
    public void testIsDeterministicPKName() {
        assertTrue(dialect.isDeterministicPKName("USER_PK"));
    }

    @Test
    public void testIsDeterministicUKName() {
        assertTrue(dialect.isDeterministicUKName("USER_NAME_UK"));
    }

    @Test
    public void testIsDeterministicFKName() {
        assertTrue(dialect.isDeterministicFKName("USER_ROLE_FK"));
    }

    @Test
    public void testIsDeterministicIndexName() {
        assertTrue(dialect.isDeterministicIndexName("USER_NAME_IDX"));
    }

    @Test
    public void testTrim() {
        assertEquals("trim(Expression)", (new CubridDialect()).trim("Expression"));
    }

    @Test
    public void testRestrictRownums() {
        Query selectResult = Query.select("Selection");
        (new CubridDialect()).restrictRownums(2, 3, selectResult);
        assertEquals("SELECT Selection FROM  limit 2, 3", selectResult.toString());
    }

    @Test
    public void testRestrictRownums2() {
        Query selectResult = Query.select("Selection");
        (new CubridDialect()).restrictRownums(0, 3, selectResult);
        assertEquals("SELECT Selection FROM  limit 3", selectResult.toString());
    }

    @Test
    public void testSupportsRegex() {
        assertTrue((new CubridDialect()).supportsRegex());
    }

    @Test
    public void testRegexQuery() {
        assertEquals("Expression NOT REGEX 'Regex'", (new CubridDialect()).regexQuery("Expression", true, "Regex"));
        assertEquals("Expression REGEX 'Regex'", (new CubridDialect()).regexQuery("Expression", false, "Regex"));
    }

    @Test
    public void testRegex() {
        assertTrue(dialect.supportsRegex());
        assertEquals("code REGEX '[A-Z]{5}'", dialect.regexQuery("code", false, "[A-Z]{5}"));
        assertEquals("code NOT REGEX '[A-Z]{5}'", dialect.regexQuery("code", true, "[A-Z]{5}"));
    }

    @Test
    public void testRenderCreateSequence() {
        assertEquals("CREATE SERIAL my_seq", dialect.renderCreateSequence(new DBSequence("my_seq", null)));
        assertEquals("CREATE SERIAL my_seq START WITH 10 INCREMENT BY 2 MAXVALUE 999 MINVALUE 5 CACHE 3 CYCLE",
                dialect.renderCreateSequence(createConfiguredSequence()));
    }

    @Test
    public void testRenderCreateSequence10() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setMinValue(BigInteger.valueOf(42L));
        assertEquals("CREATE SERIAL Name MINVALUE 42", (new CubridDialect()).renderCreateSequence(dbSequence));
    }

    @Test
    public void testRenderCreateSequence11() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setMaxValue(BigInteger.valueOf(42L));
        assertEquals("CREATE SERIAL Name MAXVALUE 42", (new CubridDialect()).renderCreateSequence(dbSequence));
    }

    @Test
    public void testRenderCreateSequence2() {
        DBSequence sequence = new DBSequence("Name", null);
        assertEquals("CREATE SERIAL Name", (new CubridDialect()).renderCreateSequence(sequence));
    }

    @Test
    public void testRenderCreateSequence3() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setCycle(true);
        assertEquals("CREATE SERIAL Name CYCLE", (new CubridDialect()).renderCreateSequence(dbSequence));
    }

    @Test
    public void testRenderCreateSequence4() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setIncrement(null);
        assertEquals("CREATE SERIAL Name", (new CubridDialect()).renderCreateSequence(dbSequence));
    }

    @Test
    public void testRenderCreateSequence5() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setCache(0L);
        assertEquals("CREATE SERIAL Name CACHE 0", (new CubridDialect()).renderCreateSequence(dbSequence));
    }

    @Test
    public void testRenderCreateSequence6() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setStart(null);
        assertEquals("CREATE SERIAL Name", (new CubridDialect()).renderCreateSequence(dbSequence));
    }

    @Test
    public void testRenderCreateSequence7() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setCycle(false);
        assertEquals("CREATE SERIAL Name NOCYCLE", (new CubridDialect()).renderCreateSequence(dbSequence));
    }

    @Test
    public void testRenderCreateSequence8() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setIncrement(BigInteger.valueOf(42L));
        assertEquals("CREATE SERIAL Name INCREMENT BY 42", (new CubridDialect()).renderCreateSequence(dbSequence));
    }

    @Test
    public void testRenderCreateSequence9() {
        DBSequence dbSequence = new DBSequence("Name", null);
        dbSequence.setStart(BigInteger.valueOf(42L));
        assertEquals("CREATE SERIAL Name START WITH 42", (new CubridDialect()).renderCreateSequence(dbSequence));
    }

    @Test // requires a CUBRID installation configured as environment named 'cubrid'
    public void testSetNextSequenceValue() throws Exception {
        if (DatabaseTestUtil.getConnectData("cubrid") == null) {
            logger.warn("Skipping test " + getClass().getSimpleName() + ".testSetNextSequenceValue() " +
                    "since no 'cubrid' environment is defined or online");
            return;
        }
        Connection connection = DBUtil.connect("cubrid", false);
        String sequenceName = getClass().getSimpleName();
        try {
            DBUtil.executeUpdate("CREATE SERIAL " + sequenceName, connection);
            dialect.setNextSequenceValue(sequenceName, 123, connection);
            String seqValQuery = dialect.renderFetchSequenceValue(sequenceName);
            assertEquals(new BigDecimal("123"), DBUtil.queryScalar(seqValQuery, connection));
        } finally {
            DBUtil.executeUpdate("DROP SERIAL " + sequenceName, connection);
        }
    }

    @Test
    public void testRenderFetchSequenceValue() {
        assertEquals("SELECT seq.NEXT_VALUE", dialect.renderFetchSequenceValue("seq"));
        assertEquals("SELECT Sequence Name.NEXT_VALUE", (new CubridDialect()).renderFetchSequenceValue("Sequence Name"));
    }

    @Test
    public void testDropSequence() {
        assertEquals("drop sequence SEQ", dialect.renderDropSequence("SEQ"));
    }

    @Test
    public void testRenderCase() {
        assertEquals("CASE WHEN condition1 THEN result1 WHEN condition2 THEN result2 ELSE result4 END AS col",
                dialect.renderCase("col", "result4", "condition1", "result1", "condition2", "result2"));
    }

}