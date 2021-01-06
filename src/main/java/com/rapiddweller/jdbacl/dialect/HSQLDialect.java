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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import com.rapiddweller.common.ArrayBuilder;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.model.DBSequence;
import com.rapiddweller.jdbacl.sql.Query;

/**
 * Implements generic database concepts for HSQL<br/><br/>
 * Created: 26.01.2008 07:04:45
 * @since 0.4.0
 * @author Volker Bergmann
 */
public class HSQLDialect extends DatabaseDialect {
    
	private static final String DATE_PATTERN = "''yyyy-MM-dd''";
	private static final String TIME_PATTERN = "''HH:mm:ss''";
	private static final String DATETIME_PATTERN = "''yyyy-MM-dd HH:mm:ss''";

	final Pattern randomPKNamePattern = Pattern.compile("SYS_IDX_\\w+");
	final Pattern randomUKNamePattern = Pattern.compile("SYS_IDX_SYS_\\w+");
	final Pattern randomFKNamePattern = Pattern.compile("SYS_FK_\\w+");
	final Pattern randomIndexNamePattern = Pattern.compile("SYS_IDX_\\w+");

	public HSQLDialect() {
	    super("hsql", true, true, DATE_PATTERN, TIME_PATTERN, DATETIME_PATTERN);
    }

	@Override
    public boolean isDefaultCatalog(String catalog, String user) {
	    return (catalog == null);
    }

	@Override
    public boolean isDefaultSchema(String schema, String user) {
	    return "PUBLIC".equalsIgnoreCase(schema);
    }

	@Override
    public DBSequence[] querySequences(Connection connection) throws SQLException {
        String query = "select SEQUENCE_CATALOG, SEQUENCE_SCHEMA, SEQUENCE_NAME, START_WITH, INCREMENT, MINIMUM_VALUE, MAXIMUM_VALUE, CYCLE_OPTION from information_schema.system_sequences";
        ArrayBuilder<DBSequence> builder = new ArrayBuilder<>(DBSequence.class);
        ResultSet resultSet = DBUtil.executeQuery(query, connection);
        try {
	        while (resultSet.next()) {
	        	String name = resultSet.getString("SEQUENCE_NAME");
	        	DBSequence sequence = new DBSequence(name, resultSet.getString("SEQUENCE_CATALOG"), resultSet.getString("SEQUENCE_SCHEMA"));
	        	sequence.setStart(new BigInteger(resultSet.getString("START_WITH")));
	        	sequence.setIncrement(new BigInteger(resultSet.getString("INCREMENT")));
	        	sequence.setMinValue(new BigInteger(resultSet.getString("MINIMUM_VALUE")));
	        	sequence.setMaxValue(new BigInteger(resultSet.getString("MAXIMUM_VALUE")));
	        	sequence.setCycle(resultSet.getBoolean("CYCLE_OPTION"));
	        	builder.add(sequence);
	        }
        } finally {
        	DBUtil.closeResultSetAndStatement(resultSet);
        }
		return builder.toArray();
	}

	@Override
    public String renderFetchSequenceValue(String sequenceName) {
        return "call next value for " + sequenceName;
    }
	
	@Override
	public void setNextSequenceValue(String sequenceName, long value, Connection connection) throws SQLException {
	    DBUtil.executeUpdate(renderSequenceValue(sequenceName, value), connection);
	}
	
	public String renderSequenceValue(String sequenceName, long value) {
	    return "alter sequence " + sequenceName + " restart with " + value;
    }
	
	@Override
	public String renderDropSequence(String name) {
		return "drop sequence " + name;
	}

	@Override
	public boolean isDeterministicPKName(String pkName) {
		return !randomPKNamePattern.matcher(pkName).matches();
	}

	@Override
	public boolean isDeterministicUKName(String ukName) {
		return !randomUKNamePattern.matcher(ukName).matches();
	}

	@Override
	public boolean isDeterministicFKName(String fkName) {
		return !randomFKNamePattern.matcher(fkName).matches();
	}

	@Override
	public boolean isDeterministicIndexName(String indexName) {
		return !randomIndexNamePattern.matcher(indexName).matches();
	}

	@Override
	public boolean supportsRegex() {
		return false;
	}
	
	@Override
	public String trim(String expression) {
		return "LTRIM(RTRIM(" + expression + "))";
	}

	/** restricts the query result set to a certain number of rows, optionally starting from an offset. 
	 *  @param rowOffset the number of rows to skip from the beginning of the result set; 
	 *  	use 0 for not skipping any.
	 *  @param rowCount the number of rows to read; 
	 *  	use 0 for unlimited access
	 */
	@Override
	public void restrictRownums(int rowOffset, int rowCount, Query query) {
		if (rowOffset == 0)
			query.addSelectCondition("TOP " + rowCount);
		else
			query.addSelectCondition("LIMIT " + rowOffset + " " + rowCount);
	}
	
}
