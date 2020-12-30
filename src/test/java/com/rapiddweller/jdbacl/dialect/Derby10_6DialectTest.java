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

package com.rapiddweller.jdbacl.dialect;

import static org.junit.Assert.*;

import java.sql.Connection;

import com.rapiddweller.common.NameUtil;
import com.rapiddweller.common.StringUtil;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.model.DBSequence;
import org.junit.Test;

/**
 * Tests the {@link Derby10_6Dialect}.<br/><br/>
 * Created: 24.10.2011 10:33:53
 * @since 0.6.13
 * @author Volker Bergmann
 */
public class Derby10_6DialectTest extends DatabaseDialectTest<Derby10_6Dialect> {
	
	private static final String ENVIRONMENT = "derby-embedded";

	public Derby10_6DialectTest() {
		super(new Derby10_6Dialect());
	}

	@Test
	public void testRenderCreateSequence() {
		assertEquals("CREATE SEQUENCE my_seq AS BIGINT", dialect.renderCreateSequence(new DBSequence("my_seq", null)));
		assertEquals("CREATE SEQUENCE my_seq AS BIGINT START WITH 10 INCREMENT BY 2 MAXVALUE 999 MINVALUE 5 CYCLE", 
				dialect.renderCreateSequence(createConfiguredSequence()));
	}
	
	@Test
	public void testRenderDropSequence() {
		assertEquals("DROP SEQUENCE my_seq RESTRICT", dialect.renderDropSequence("my_seq"));
	}
		
	@Test
	public void testSequencesOnline() throws Exception {
		if (!DBUtil.existsEnvironment(ENVIRONMENT)) {
			System.out.println("Skipping test: testSequencesOnline()");
			return;
		}
		String sequenceName = getClass().getSimpleName();
		Connection connection = DBUtil.connect(ENVIRONMENT, false);
		try {
			dialect.createSequence(sequenceName, 23, connection);
			DBSequence[] sequences = dialect.querySequences(connection);
			String[] sequenceNames = NameUtil.getNames(sequences);
			assertTrue(StringUtil.containsIgnoreCase(sequenceName, sequenceNames));
			assertEquals(23L, DBUtil.queryLong(dialect.renderFetchSequenceValue(sequenceName), connection).longValue());
			dialect.setNextSequenceValue(sequenceName, 123, connection);
			String seqValQuery = dialect.renderFetchSequenceValue(sequenceName);
			assertEquals(123L, DBUtil.queryScalar(seqValQuery, connection));
		} finally {
			DBUtil.executeUpdate("DROP SEQUENCE " + sequenceName + " RESTRICT", connection);
			DBUtil.close(connection);
		}
	}
	
	@Test
	public void testRenderFetchSequenceValue() {
		assertEquals("VALUES (NEXT VALUE FOR my_seq)", dialect.renderFetchSequenceValue("my_seq"));
	}
	
}
