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

import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.sql.Query;

import java.util.regex.Pattern;

/**
 * Implements generic database concepts for DB2<br/><br/>
 * Created: 26.01.2008 07:09:34
 *
 * @author Volker Bergmann
 * @since 0.4.0
 */
public class DB2Dialect extends DatabaseDialect {

  private static final String DATE_PATTERN = "''yyyy-MM-dd''";
  private static final String TIME_PATTERN = "''HH:mm:ss''";
  private static final String DATETIME_PATTERN = "''yyyy-MM-dd HH:mm:ss''";

  /**
   * The Random name pattern.
   */
  final Pattern randomNamePattern = Pattern.compile("SQL\\d{15}");

  /**
   * Instantiates a new Db 2 dialect.
   */
  public DB2Dialect() {
    super("db2", false, true, DATE_PATTERN, TIME_PATTERN, DATETIME_PATTERN);
  }

  @Override
  public boolean isDefaultCatalog(String catalog, String user) {
    return true;
  }

  @Override
  public boolean isDefaultSchema(String schema, String user) {
    return true;
  }

  @Override
  public String renderFetchSequenceValue(String sequenceName) {
    String table = "sysibm.sysdummy1";
    String sequence = sequenceName;
    int sep = sequenceName.lastIndexOf('.');
    if (sep > 0) {
      table = sequenceName.substring(0, sep);
      sequence = sequenceName.substring(sep + 1);
    }
    return "select nextval for " + sequence + " from " + table;
  }

  @Override
  public boolean isDeterministicPKName(String pkName) {
    return !randomNamePattern.matcher(pkName).matches();
  }

  @Override
  public boolean isDeterministicUKName(String ukName) {
    return !randomNamePattern.matcher(ukName).matches();
  }

  @Override
  public boolean isDeterministicFKName(String fkName) {
    return !randomNamePattern.matcher(fkName).matches();
  }

  @Override
  public boolean isDeterministicIndexName(String indexName) {
    return !randomNamePattern.matcher(indexName).matches();
  }

  @Override
  public void restrictRownums(int firstRowIndex, int rowCount, Query query) {
    /* TODO implement DatabaseDialect.applyRownumRestriction()
			DB2: SELECT * FROM T WHERE ID_T > 20 FETCH FIRST 10 ROWS ONLY
	*/
    throw new UnsupportedOperationException("DB2Dialect.applyRownumRestriction() is not implemented");
  }

}
