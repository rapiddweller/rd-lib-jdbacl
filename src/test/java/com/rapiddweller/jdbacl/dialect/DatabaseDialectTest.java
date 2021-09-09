/*
 * (c) Copyright 2009-2012 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.common.NameUtil;
import com.rapiddweller.common.StringUtil;
import com.rapiddweller.common.TimeUtil;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.DatabaseTestUtil;
import com.rapiddweller.jdbacl.JDBCConnectData;
import com.rapiddweller.jdbacl.model.DBSequence;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.junit.Test;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Parent class for testing concrete {@link DatabaseDialect} implementations.<br/><br/>
 * Created: 10.11.2009 16:59:14
 *
 * @param <E> the type parameter
 * @author Volker Bergmann
 * @since 0.6.0
 */
public abstract class DatabaseDialectTest<E extends DatabaseDialect> {

  /**
   * The Logger.
   */
  protected final Logger logger;

  /**
   * The Dialect.
   */
  protected final E dialect;

  /**
   * The constant DATETIME_19710203131415.
   */
  protected final static Date DATETIME_19710203131415 = TimeUtil.date(1971, 1, 3, 13, 14, 15, 0);
  /**
   * The constant DATE_19710203.
   */
  protected final static Date DATE_19710203 = TimeUtil.date(1971, 1, 3);
  /**
   * The constant TIME_131415.
   */
  protected final static Time TIME_131415 = TimeUtil.time(13, 14, 15, 0);
  /**
   * The constant TIMESTAMP_19710203131415123456789.
   */
  protected final static Timestamp TIMESTAMP_19710203131415123456789 = TimeUtil.timestamp(1971, 1, 3, 13, 14, 15, 123456789);

  /**
   * Instantiates a new Database dialect test.
   *
   * @param dialect the dialect
   */
  public DatabaseDialectTest(E dialect) {
    this.dialect = dialect;
    this.logger = LoggerFactory.getLogger(getClass());
  }

  // common tests for all databases ----------------------------------------------------------------------------------

  /**
   * Test sequence setup consistency.
   */
  @Test
  public void testSequenceSetupConsistency() {
    boolean supported = dialect.isSequenceSupported();
    if (supported) {
      assertSequenceSupported();
    } else {
      assertSequenceNotSupported();
    }
  }

  /**
   * Test canonical reserved words.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCanonicalReservedWords() throws Exception {
    assertTrue(dialect.isReservedWord("TABLE", null));
    assertFalse(dialect.isReservedWord("", null));
    assertFalse(dialect.isReservedWord(null, null));
  }

  // helpers ---------------------------------------------------------------------------------------------------------

  private void assertSequenceSupported() {
    String sequence = "DUMMY_SEQ";
    dialect.renderFetchSequenceValue(sequence);
    dialect.renderDropSequence(sequence);
  }

  private void assertSequenceNotSupported() {
    String sequence = "DUMMY_SEQ";
    try {
      dialect.renderFetchSequenceValue(sequence);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException e) {
      // this is expected
    }
    try {
      dialect.renderDropSequence(sequence);
      fail("UnsupportedOperationException expected");
    } catch (UnsupportedOperationException e) {
      // this is expected
    }
  }

  /**
   * Test sequences online.
   *
   * @param databaseId the database id
   * @throws Exception the exception
   */
  protected void testSequencesOnline(String databaseId) throws Exception {
    JDBCConnectData data = DatabaseTestUtil.getConnectData(databaseId);
    if (data == null) {
      System.out.println("Skipping test: testSequencesOnline(" + databaseId + ")");
      return;
    }
    String sequenceName = getClass().getSimpleName();
    Connection connection = DBUtil.connect(data, false);
    try {
      dialect.createSequence(sequenceName, 23, connection);
      DBSequence[] sequences = dialect.querySequences(connection);
      String[] sequenceNames = NameUtil.getNames(sequences);
      assertTrue(StringUtil.containsIgnoreCase(sequenceName, sequenceNames));
      assertEquals(23L, DBUtil.queryLong(dialect.renderFetchSequenceValue(sequenceName), connection).longValue());
    } finally {
      DBUtil.executeUpdate(dialect.renderDropSequence(sequenceName), connection);
      DBUtil.close(connection);
    }
  }

  /**
   * Create configured sequence db sequence.
   *
   * @return the db sequence
   */
  protected DBSequence createConfiguredSequence() {
    DBSequence seq = new DBSequence("my_seq", null);
    seq.setStart(new BigInteger("10"));
    seq.setIncrement(new BigInteger("2"));
    seq.setMaxValue(new BigInteger("999"));
    seq.setMinValue(new BigInteger("5"));
    seq.setCycle(true);
    seq.setCache(3L);
    seq.setOrder(true);
    return seq;
  }

}
