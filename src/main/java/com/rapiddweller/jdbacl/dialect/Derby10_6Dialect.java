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

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.rapiddweller.commons.ArrayBuilder;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.model.DBSequence;

/**
 * {@link DatabaseDialect} for Derby 10.6+ which supports sequences.<br/><br/>
 * Created: 24.10.2011 10:09:04
 * @since 0.6.13
 * @author Volker Bergmann
 */
public class Derby10_6Dialect extends DerbyDialect {

	public Derby10_6Dialect() {
		super(true);
	}

	@Override
	public String renderCreateSequence(DBSequence sequence) {
		/*
			CREATE SEQUENCE [ schemaName. ] SQL92Identifier
			AS dataType 
			| START WITH signedInteger 
			| INCREMENT BY signedInteger 
			| MAXVALUE signedInteger | NO MAXVALUE 
			| MINVALUE signedInteger | NO MINVALUE 
			| CYCLE | NO CYCLE 
		 */
		return super.renderCreateSequence(sequence);
	}
	
	@Override
	protected String renderSequenceNameAndType(DBSequence sequence) {
		String schemaName = sequence.getSchemaName();
		return (schemaName != null ? schemaName + '.' : "") + sequence.getName() + " AS BIGINT";
	}
	
	@Override
	protected String sequenceNoCycle() {
		return "NO CYCLE";
	}
	
	@Override
	public String renderDropSequence(String sequenceName) {
		return "DROP SEQUENCE " + sequenceName + " RESTRICT";
	}

    @Override
	public DBSequence[] querySequences(Connection connection) throws SQLException {
    	List<Object[]> rows = DBUtil.query("SELECT SEQUENCENAME, STARTVALUE, INCREMENT, MAXIMUMVALUE, MINIMUMVALUE, " +
    			"CYCLEOPTION, CURRENTVALUE FROM SYS.SYSSEQUENCES", connection);
    	ArrayBuilder<DBSequence> builder = new ArrayBuilder<>(DBSequence.class, rows.size());
    	for (Object[] row : rows) {
    		DBSequence sequence = new DBSequence(row[0].toString(), null);
    		sequence.setStart(new BigInteger(row[1].toString()));
    		sequence.setIncrement(new BigInteger(row[2].toString()));
    		sequence.setMaxValue(new BigInteger(row[3].toString()));
    		sequence.setMinValue(new BigInteger(row[4].toString()));
    		sequence.setCycle("Y".equals(row[5].toString()));
    		sequence.setLastNumber(new BigInteger(row[6].toString()));
    		builder.add(sequence);
    	}
    	return builder.toArray();
	}
    
    @Override
	public String renderFetchSequenceValue(String sequenceName) {
    	// see http://db.apache.org/derby/docs/10.7/ref/rrefsqljnextvaluefor.html
		// see http://stackoverflow.com/questions/5729063/how-to-use-sequence-in-apache-derby
		return "VALUES (NEXT VALUE FOR " + sequenceName + ")";
	}
	
    @Override
    public void setNextSequenceValue(String sequenceName, long nextValue, Connection connection) throws SQLException {
    	DBSequence sequence = getSequence(sequenceName, connection);
    	BigInteger lastNumber = sequence.getLastNumber();
    	long defaultNext = lastNumber.add(BigInteger.ONE).longValue();
    	if (nextValue != defaultNext) {
    		DBUtil.executeUpdate(renderDropSequence(sequenceName), connection);
    		sequence.setStart(BigInteger.valueOf(nextValue));
    		DBUtil.executeUpdate(renderCreateSequence(sequence), connection);
    	}
    }
    
}
