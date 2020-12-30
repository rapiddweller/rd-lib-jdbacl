/*
 * (c) Copyright 2009-2012 by Volker Bergmann. All rights reserved.
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
 * {@link DatabaseDialect} implementation for the Firebird database.<br/>
 * <br/>
 * Created at 09.03.2009 07:13:35
 * @since 0.5.8
 * @author Volker Bergmann
 */

public class FirebirdDialect extends DatabaseDialect {

	private static final String DATE_PATTERN = "''yyyy-MM-dd''";
	private static final String TIME_PATTERN = "''HH:mm:ss''";
	private static final String DATETIME_PATTERN = "''yyyy-MM-dd HH:mm:ss''";

	final Pattern randomPKNamePattern = Pattern.compile("INTEG_\\d+");
	final Pattern randomUKNamePattern = Pattern.compile("RDB\\$\\w+");
	final Pattern randomFKNamePattern = Pattern.compile("INTEG_\\d+");
	final Pattern randomIndexNamePattern = Pattern.compile("RDB\\$\\w+");

    public FirebirdDialect() {
	    super("firebird", true, true, DATE_PATTERN, TIME_PATTERN, DATETIME_PATTERN);
    }

    public String getJDBCDriverClass() {
    	return "org.firebirdsql.jdbc.FBDriver";
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
	public boolean isSequenceBoundarySupported() {
		return false;
	}
	
    @Override
    public void createSequence(String name, long initialValue, Connection connection) throws SQLException {
    	DBUtil.executeUpdate(renderCreateSequence(name), connection);
    	DBUtil.executeUpdate(renderSetSequenceValue(name, initialValue), connection);
    }

    @Override
    public String renderCreateSequence(DBSequence sequence) {
    	String result = renderCreateSequence(sequence.getName());
    	BigInteger start = sequence.getStart();
		if (start != null && isNotOne(start))
    		result += "; " + renderSetSequenceValue(sequence.getName(), start.longValue()) + ";";
		return result;
    }
    
    public String renderCreateSequence(String name) {
        return "CREATE GENERATOR " + name;
    }
    
    @Override
    public String renderDropSequence(String sequenceName) {
        return "drop generator " + sequenceName;
    }
    
    @Override
    public String renderFetchSequenceValue(String sequenceName) {
        return "select gen_id(" + sequenceName + ", 1) from RDB$DATABASE;";
    }
    
    @Override
    public DBSequence[] querySequences(Connection connection) throws SQLException {
        String query = "select RDB$GENERATOR_NAME, RDB$GENERATOR_ID, RDB$SYSTEM_FLAG, RDB$DESCRIPTION " +
        		"from RDB$GENERATORS where RDB$GENERATOR_NAME NOT LIKE '%$%'";
        ResultSet resultSet = null;
        try {
        	resultSet = DBUtil.executeQuery(query, connection);
        	ArrayBuilder<DBSequence> builder = new ArrayBuilder<>(DBSequence.class);
        	while (resultSet.next())
        		builder.add(new DBSequence(resultSet.getString(1).trim(), null));
    		return builder.toArray();
        } finally {
        	DBUtil.closeResultSetAndStatement(resultSet);
        }
    }
    
    @Override
    public void setNextSequenceValue(String sequenceName, long value, Connection connection) throws SQLException {
    	DBUtil.executeUpdate(renderSetSequenceValue(sequenceName, value), connection);
    }
    
    public String renderSetSequenceValue(String sequenceName, long value) {
        return "SET GENERATOR " + sequenceName + " TO " + (value - 1);
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
	public void restrictRownums(int firstRowIndex, int rowCount, Query query) {
	    /* TODO v0.8.2 implement DatabaseDialect.applyRownumRestriction()
			Firebird: SELECT FIRST 10 SKIP 20 * FROM T
	     */
		throw new UnsupportedOperationException("FirebirdDialect.applyRownumRestriction() is not implemented"); // TODO v0.8.2 implement DatabaseDialect.applyRownumRestriction()
	}

}
