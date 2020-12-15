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

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import com.rapiddweller.commons.TimeUtil;
import com.rapiddweller.jdbacl.model.DBSequence;
import com.rapiddweller.jdbacl.sql.Query;
import org.junit.Test;

/**
 * Tests the {@link OracleDialect}.<br/><br/>
 * Created: 10.11.2009 17:22:59
 * @since 0.6.0
 * @author Volker Bergmann
 */
public class OracleDialectTest extends DatabaseDialectTest<OracleDialect> {

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
	}
	
	@Test
	public void testIsDeterministicUKName() {
		assertFalse(dialect.isDeterministicUKName("SYS_C00208396"));
		assertTrue(dialect.isDeterministicUKName("USER_NAME_UK"));
	}
	
	@Test
	public void testIsDeterministicFKName() {
		assertFalse(dialect.isDeterministicFKName("SYS_C00208399"));
		assertTrue(dialect.isDeterministicFKName("USER_ROLE_FK"));
	}
	
	@Test
	public void testIsDeterministicIndexName() {
		assertFalse(dialect.isDeterministicIndexName("SYS_C00208398"));
		assertTrue(dialect.isDeterministicIndexName("USER_NAME_IDX"));
	}
	
	@Test
	public void testIsDeterministicCheckName() {
		assertFalse(dialect.isDeterministicCheckName("SYS_C00208394"));
		assertTrue(dialect.isDeterministicCheckName("USER_NAME_NOT_NULL"));
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
	public void testRenderFetchSequenceValue() {
		assertEquals("select SEQ.nextval from dual", dialect.renderFetchSequenceValue("SEQ"));
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
	
}
