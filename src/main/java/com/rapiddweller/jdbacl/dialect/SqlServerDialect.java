/*
 * (c) Copyright 2010-2012 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.common.StringUtil;
import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.sql.Query;

import java.util.regex.Pattern;

/**
 * Implements generic database concepts for SQL Server.<br/><br/>
 * Created: 09.04.2010 07:29:32
 * @since 0.6.0
 * @author Volker Bergmann
 */
public class SqlServerDialect extends DatabaseDialect {

	private static final String DATE_PATTERN = "''yyyy-MM-dd''";
	private static final String TIME_PATTERN = "''HH:mm:ss''";
	private static final String DATETIME_PATTERN = "''yyyy-MM-dd'T'HH:mm:ss''";

	final Pattern randomNamePattern = Pattern.compile("SYS_\\w*");

	public SqlServerDialect() {
	    super("sql_server", true, false, DATE_PATTERN, TIME_PATTERN, DATETIME_PATTERN);
    }

	@Override
    public boolean isDefaultCatalog(String catalog, String user) {
	    return true;
    }

	@Override
    public boolean isDefaultSchema(String schema, String user) {
	    return "DBO".equalsIgnoreCase(schema);
    }

	@Override
	public boolean isDeterministicPKName(String pkName) {
		return !randomNamePattern.matcher(pkName).matches();
	}

	@Override
	public boolean isDeterministicUKName(String pkName) {
		return !randomNamePattern.matcher(pkName).matches();
	}

	@Override
	public boolean isDeterministicFKName(String pkName) {
		return !randomNamePattern.matcher(pkName).matches();
	}

	@Override
	public boolean isDeterministicIndexName(String indexName) {
		return !randomNamePattern.matcher(indexName).matches();
	}

	@Override
	public String renderCase(String columnName, String elseExpression, String... whenThenExpressionPairs) {
		StringBuilder builder = new StringBuilder();
		builder.append(columnName).append(" = "); // applying column name
		builder.append("CASE");
		for (int i = 0; i < whenThenExpressionPairs.length; i += 2) {
			builder.append(" WHEN ").append(whenThenExpressionPairs[i]); // when part
			builder.append(" THEN ").append(whenThenExpressionPairs[i + 1]); // then part
		}
		if (!StringUtil.isEmpty(elseExpression))
			builder.append(" ELSE ").append(elseExpression); // else part
		builder.append(" END"); // closing the case
		return builder.toString();
	}

	@Override
	public void restrictRownums(int firstRowIndex, int rowCount,
			Query query) {
	    /* TODO v0.8.2 implement DatabaseDialect.applyRownumRestriction()
			MS SQL Server: SELECT TOP 10 * FROM T
	     */
		throw new UnsupportedOperationException("SqlServerDialect.applyRownumRestriction() is not implemented");
	}
	
}
