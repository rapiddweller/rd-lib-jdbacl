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

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.rapiddweller.common.ArrayBuilder;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.model.DBSequence;

/**
 * Handles theses changes from PostgresSQL 10:
 *
 * <ul>
 *     <li> sequence handling (see https://github.com/rails/rails/pull/28864)</li>
 * </ul>
 *
 * @since 1.1.12
 */
public class PostgreSQL10Dialect extends PostgreSQLDialect {

  @Override
  public DBSequence[] querySequences(Connection connection) throws SQLException {
    // query sequence names
    List<Object[]> rows = DBUtil.query("select relname from pg_class where relkind = 'S'", connection);
    ArrayBuilder<DBSequence> builder = new ArrayBuilder<>(DBSequence.class, rows.size());
    for (Object[] row : rows) {
      String name = (String) row[0];
      // query sequence details
      Object[] details = DBUtil.querySingleRow("select sequencename, start_value, increment_by, " +
          "max_value, min_value, cycle, cache_size, last_value from pg_sequences WHERE sequencename = '" + name + "'", connection);
      DBSequence sequence = new DBSequence(name, null);
      sequence.setStart(new BigInteger(details[1].toString()));
      sequence.setIncrement(new BigInteger(details[2].toString()));
      sequence.setMaxValue(new BigInteger(details[3].toString()));
      sequence.setMinValue(new BigInteger(details[4].toString()));
      sequence.setCycle(Boolean.valueOf(details[5].toString()));
      sequence.setCache(Long.parseLong(details[6].toString()));
      sequence.setLastNumber(details[7] != null ? new BigInteger(details[7].toString()) : BigInteger.ZERO);
      builder.add(sequence);
    }
    return builder.toArray();
  }
}
