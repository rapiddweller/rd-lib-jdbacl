/*
 * (c) Copyright 2011-2012 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.sql.Query;

/**
 * {@link DatabaseDialect} implementation for the MySQL database.<br/><br/>
 * Created: 24.06.2011 07:29:20
 *
 * @author Volker Bergmann
 * @since 0.6.9
 */
public class MySQLDialect extends DatabaseDialect {

  private static final String DATE_PATTERN = "''yyyy-MM-dd''";
  private static final String TIME_PATTERN = "''HH:mm:ss''";
  private static final String DATETIME_PATTERN = "''yyyy-MM-dd HH:mm:ss''";

  /**
   * Instantiates a new My sql dialect.
   */
  public MySQLDialect() {
    super("mysql", false, false, DATE_PATTERN, TIME_PATTERN, DATETIME_PATTERN);
  }

  @Override
  public boolean isDefaultCatalog(String catalog, String user) {
    // MySQL does not have a default catalog, so jdbacl has to retrieve it from Connection.getCatalog()
    return false;
  }

  @Override
  public boolean isDefaultSchema(String schema, String user) {
    return false; // MySQL does not make use of schemas at all
  }

  @Override
  public boolean isDeterministicPKName(String pkName) {
    return true; // MySQL always creates deterministic names
  }

  @Override
  public boolean isDeterministicUKName(String ukName) {
    return true; // MySQL always creates deterministic names
  }

  @Override
  public boolean isDeterministicFKName(String fkName) {
    return true; // MySQL always creates deterministic names
  }

  @Override
  public boolean isDeterministicIndexName(String indexName) {
    return true; // MySQL always creates deterministic names
  }

  @Override
  public boolean supportsRegex() {
    return true;
  }

  @Override
  public String regexQuery(String expression, boolean not, String regex) {
    return expression + (not ? " NOT" : "") + " REGEXP '" + regex + "'";
  }

  @Override
  public void restrictRownums(int firstRowIndex, int rowCount, Query query) {
	    /* TODO implement DatabaseDialect.applyRownumRestriction()
			MySQL, PostgreSQL, H2: SELECT * FROM T LIMIT 10 OFFSET 20
	     */
    throw new UnsupportedOperationException(
        "MySQLDialect.applyRownumRestriction() is not implemented"); // TODO implement DatabaseDialect.applyRownumRestriction()
  }

  @Override
  public String getSpecialType(String type) {
    if ("long".equals(type)) {
      return "bigint";
    }
    return super.getSpecialType(type);
  }
}
