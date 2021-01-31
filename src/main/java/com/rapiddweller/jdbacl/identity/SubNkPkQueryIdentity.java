/*
 * (c) Copyright 2010-2014 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.identity;

import com.rapiddweller.common.ArrayFormat;
import com.rapiddweller.common.HeavyweightIterator;
import com.rapiddweller.common.IOUtil;
import com.rapiddweller.common.SystemInfo;
import com.rapiddweller.common.iterator.ConvertingIterator;
import com.rapiddweller.common.iterator.TabularIterator;
import com.rapiddweller.jdbacl.*;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.model.Database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Iterator;

/**
 * {@link IdentityModel} for tables which are owned by another table but have a sub identity 
 * (resulting in a one-to-many relationship between parent and child). 
 * Their natural key is composed from the owner row's natural key and a sub key for the row itself.<br/><br/>
 * Created: 01.09.2010 09:24:26
 * @since 0.6.4
 * @author Volker Bergmann
 */
public class SubNkPkQueryIdentity extends IdentityModel {

	private final String[] parentTableNames; // TODO v1.0 support multiple 'parent' and 'parentColumns' property
	private String subNkPkQuery;
	private final IdentityProvider identityProvider;

	public SubNkPkQueryIdentity(String tableName, String[] parentTableNames, IdentityProvider identityProvider) {
	    super(tableName);
	    this.parentTableNames = parentTableNames;
	    this.identityProvider = identityProvider;
    }
	
	// properties ------------------------------------------------------------------------------------------------------

	public void setSubNkPkQuery(String subNkPkQuery) {
	    this.subNkPkQuery = subNkPkQuery;
    }

	@Override
	public String getDescription() {
		return "Sub identity of (" + ArrayFormat.format(parentTableNames) + "):" + 
			SystemInfo.getLineSeparator() + subNkPkQuery;
	}

	// implementation --------------------------------------------------------------------------------------------------
	
    @Override
	public TabularIterator createNkPkIterator(Connection connection, String dbId, KeyMapper mapper, Database database) {
		return new RecursiveIterator(connection, dbId, mapper, database);
    }
    
    // helper class for recursive iteration ----------------------------------------------------------------------------

    public class RecursiveIterator implements TabularIterator {
    	
    	final Connection connection;
    	final String dbId;
    	final KeyMapper mapper;
    	final HeavyweightIterator<Object> ownerPkIterator;
    	String ownerNK;
    	TabularIterator subNkPkIterator;
    	final DatabaseDialect dialect;

	    public RecursiveIterator(Connection connection, String dbId, KeyMapper mapper, Database database) {
	        this.connection = connection;
	        this.dbId = dbId;
	        this.mapper = mapper;
	        this.dialect = DatabaseDialectManager.getDialectForProduct(
	        		database.getDatabaseProductName(), database.getDatabaseProductVersion());
	        ownerPkIterator = createParentPkIterator(connection, database); // TODO v1.0 support multiple parents
	        createSubNkPkIterator(connection, dbId);
        }

		protected HeavyweightIterator<Object> createParentPkIterator(Connection connection, Database database) {
			DBTable parentTable = database.getTable(parentTableNames[0]);
			StringBuilder query = new StringBuilder("select ");
			query.append(ArrayFormat.format(parentTable.getPKColumnNames()));
			query.append(" from ").append(parentTable);
	    	Iterator<ResultSet> rawIterator = new QueryIterator(query.toString(), connection, 100);
	        ResultSetConverter<Object> converter = new ResultSetConverter<>(Object.class, true);
	    	return new ConvertingIterator<>(rawIterator, converter);
		}

		@Override
		public boolean hasNext() {
			if (subNkPkIterator.hasNext())
				return true;
	    	while (subNkPkIterator != null && !subNkPkIterator.hasNext() && ownerPkIterator.hasNext()) {
	    		IOUtil.close(subNkPkIterator);
	    		createSubNkPkIterator(connection, dbId);
	    	}
	    	return (subNkPkIterator != null && subNkPkIterator.hasNext());
	    }

	    @Override
		public Object[] next() {
	    	Object[] result = (Object[]) subNkPkIterator.next();
	    	result[0] = ownerNK + '|' + result[0];
	    	return result;
	    }

		@Override
		public String[] getColumnNames() {
			return subNkPkIterator.getColumnNames();
		}

	    @Override
		public void remove() {
		    throw new UnsupportedOperationException(getClass() + " does not support removal");
	    }

	    @Override
		public void close() {
	    	IOUtil.close(subNkPkIterator);
		    IOUtil.close(ownerPkIterator);
	    }

		private void createSubNkPkIterator(Connection connection, String dbId) {
	        if (ownerPkIterator.hasNext()) {
	        	Object ownerPk = ownerPkIterator.next();
	        	ownerNK = mapper.getNaturalKey(dbId, identityProvider.getIdentity(parentTableNames[0]), ownerPk); // TODO v1.0 support multiple owners
	        	if (ownerNK == null)
	        		throw new InvalidIdentityDefinitionError(tableName + " row with PK " + ownerPk + 
	        				" cannot be found. Most likely this is a subsequent fault of a parent's identity" +
	        				" definition: " + ArrayFormat.format(parentTableNames));
	        	String query = SQLUtil.substituteMarkers(subNkPkQuery, "?", ownerPk, dialect);
	        	subNkPkIterator = new ArrayResultSetIterator(connection, query);
	        } else
	        	subNkPkIterator = null;
        }

    }

}