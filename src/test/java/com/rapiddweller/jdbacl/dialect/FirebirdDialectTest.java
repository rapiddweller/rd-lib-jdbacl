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

import java.sql.Connection;

import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.DatabaseTestUtil;
import com.rapiddweller.jdbacl.model.DBSequence;
import org.junit.Test;

/**
 * Tests the {@link FirebirdDialect}.<br/><br/>
 * Created: 10.11.2009 18:18:04
 * @since 0.6.0
 * @author Volker Bergmann
 */
public class FirebirdDialectTest extends DatabaseDialectTest<FirebirdDialect> {
	
	public FirebirdDialectTest() {
	    super(new FirebirdDialect());
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
		assertFalse(dialect.isDeterministicPKName("INTEG_3486"));
		assertTrue(dialect.isDeterministicPKName("USER_PK"));
	}
	
	@Test
	public void testIsDeterministicUKName() {
		assertFalse(dialect.isDeterministicUKName("RDB$749"));
		assertTrue(dialect.isDeterministicUKName("USER_NAME_UK"));
	}
	
	@Test
	public void testIsDeterministicFKName() {
		assertFalse(dialect.isDeterministicFKName("INTEG_3487"));
		assertTrue(dialect.isDeterministicFKName("USER_ROLE_FK"));
	}
	
	@Test
	public void testIsDeterministicIndexName() {
		assertFalse(dialect.isDeterministicIndexName("RDB$749"));
		assertTrue(dialect.isDeterministicIndexName("USER_NAME_IDX"));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testRegex() {
		assertFalse(dialect.supportsRegex());
		dialect.regexQuery("code", false, "[A-Z]{4}");
	}
	
	@Test
	public void testRenderCreateSequence() {
		assertEquals("CREATE GENERATOR my_seq", dialect.renderCreateSequence(new DBSequence("my_seq", null)));
		assertEquals("CREATE GENERATOR my_seq; SET GENERATOR my_seq TO 9;", 
				dialect.renderCreateSequence(createConfiguredSequence()));
	}
	
	@Test
	public void testSequencesOnline() throws Exception {
		testSequencesOnline("firebird");
	}
	
	@Test // requires a Firebird installation configured as environment named 'firebird'
	public void testSetNextSequenceValue() throws Exception {
		if (DatabaseTestUtil.getConnectData("firebird") == null) {
			logger.warn("Skipping test " + getClass() + ".testSetNextSequenceValue() since there is no 'firebird' environment defined or online");
			return;
		}
		Connection connection = DBUtil.connect("firebird", false);
		String sequenceName = getClass().getSimpleName();
		DBUtil.executeUpdate("create sequence " + sequenceName, connection);
		dialect.setNextSequenceValue(sequenceName, 123, connection);
		String seqValQuery = dialect.renderFetchSequenceValue(sequenceName);
		assertEquals(123L, DBUtil.queryScalar(seqValQuery, connection));
		DBUtil.executeUpdate("drop sequence " + sequenceName, connection);
	}
	
	@Test
	public void testRenderCase() {
		assertEquals("CASE WHEN condition1 THEN result1 WHEN condition2 THEN result2 ELSE result4 END AS col", 
				dialect.renderCase("col", "result4", "condition1", "result1", "condition2", "result2"));
	}
	
}
