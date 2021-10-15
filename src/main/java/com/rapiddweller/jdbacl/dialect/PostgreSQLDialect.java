/*
 * (c) Copyright 2008-2012 by Volker Bergmann. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted under the terms of the
 * GNU General Public License.
 *
 * For redistributing this software or a derivative work under a license other
 * than the GPL-compatible Free Software License as defined by the Free
 * Software Foundation or approved by OSI, you must first obtain a commercial
 * license to this software product from Volker Bergmann.
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

import com.rapiddweller.common.ArrayBuilder;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.model.DBSequence;
import com.rapiddweller.jdbacl.sql.Query;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Implements generic database concepts for PostgreSQL.<br/><br/>
 * Created: 26.01.2008 07:11:06
 *
 * @author Volker Bergmann
 * @since 0.4.0
 */
public class PostgreSQLDialect extends DatabaseDialect {

  private static final String DATE_PATTERN = "'date '''yyyy-MM-dd''";
  private static final String TIME_PATTERN = "'time '''HH:mm:ss''";
  private static final String DATETIME_PATTERN = "'timestamp '''yyyy-MM-dd HH:mm:ss''";

  /**
   * Instantiates a new Postgre sql dialect.
   */
  public PostgreSQLDialect() {
    super("postgres", true, true, DATE_PATTERN, TIME_PATTERN, DATETIME_PATTERN);
  }

  @Override
  protected String sequenceNoCycle() {
    return "NO CYCLE";
  }

  @Override
  public String renderCreateSequence(DBSequence sequence) {
  //PostgreSQL syntax:
  //create sequence xyz
  //start [with] 1
  //increment [by] 1
  //minvalue 1 | no minvalue
  //maxvalue 999999999 | no maxvalue
  //CACHE 1
  //[NO] CYCLE

    String result = super.renderCreateSequence(sequence);
    Long cache = sequence.getCache();
    if (cache != null) {
      result += " CACHE " + cache;
    }
    return result;
  }

  @Override
  public DBSequence[] querySequences(Connection connection) throws SQLException {
    // query sequence names
    List<Object[]> rows = DBUtil.query("select relname from pg_class where relkind = 'S'", connection);
    ArrayBuilder<DBSequence> builder = new ArrayBuilder<>(DBSequence.class, rows.size());
    for (Object[] row : rows) {
      String name = (String) row[0];
      // query sequence details
      Object[] details = DBUtil.querySingleRow("select sequence_name, start_value, increment_by, " +
          "max_value, min_value, is_cycled, cache_value, last_value from " + name, connection);
      DBSequence sequence = new DBSequence(name, null);
      sequence.setStart(new BigInteger(details[1].toString()));
      sequence.setIncrement(new BigInteger(details[2].toString()));
      sequence.setMaxValue(new BigInteger(details[3].toString()));
      sequence.setMinValue(new BigInteger(details[4].toString()));
      sequence.setCycle(Boolean.valueOf(details[5].toString()));
      sequence.setCache(Long.parseLong(details[6].toString()));
      sequence.setLastNumber(new BigInteger(details[7].toString()));
      builder.add(sequence);
    }
    return builder.toArray();
  }

  @Override
  public boolean isDefaultCatalog(String catalog, String user) {
    return "".equals(catalog) || user.equalsIgnoreCase(catalog);
  }

  @Override
  public boolean isDefaultSchema(String schema, String user) {
    return "public".equalsIgnoreCase(schema);
  }

  @Override
  public String renderFetchSequenceValue(String sequenceName) {
    return "select nextval('" + sequenceName + "')";
  }

  @Override
  public String formatTimestamp(Timestamp timestamp) {
    return "timestamp " + super.formatTimestamp(timestamp);
  }

  @Override
  public boolean isDeterministicPKName(String pkName) {
    return true; // PostgreSQL generates deterministic names
  }

  @Override
  public boolean isDeterministicUKName(String ukName) {
    return true; // PostgreSQL generates deterministic names
  }

  @Override
  public boolean isDeterministicFKName(String fkName) {
    return true; // PostgreSQL generates deterministic names
  }

  @Override
  public boolean isDeterministicIndexName(String indexName) {
    return true; // PostgreSQL generates deterministic names
  }

  @Override
  public boolean supportsRegex() {
    return true;
  }

  @Override
  public String regexQuery(String expression, boolean not, String regex) {
    return (not ? "NOT " : "") + expression + " ~ '" + regex + "'";
  }

  @Override
  public void restrictRownums(int firstRowIndex, int rowCount, Query query) {
	    /* TODO v0.8.2 implement DatabaseDialect.applyRownumRestriction()
			MySQL, PostgreSQL, H2: SELECT * FROM T LIMIT 10 OFFSET 20
	     */
    throw new UnsupportedOperationException(
        "PostgreSQLDialect.applyRownumRestriction() is not implemented"); // TODO v0.8.2 implement DatabaseDialect.applyRownumRestriction()
  }

  @Override
  public String getSpecialType(String type) {
    if ("double".equals(type)) {
      return "numeric";
    } else {
      return super.getSpecialType(type);
    }
  }
}
