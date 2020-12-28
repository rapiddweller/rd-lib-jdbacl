/*
 * (c) Copyright 2010-2011 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.identity.mem;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import com.rapiddweller.jdbacl.identity.IdentityModel;
import com.rapiddweller.jdbacl.identity.KeyMapper;
import com.rapiddweller.jdbacl.model.Database;

/**
 * In-memory implementation of the mapping functionality needed for source databases.<br/><br/>
 * Created: 24.08.2010 11:28:44
 * @since 0.6.4
 * @author Volker Bergmann
 */
public class SourceDatabaseMapper {

	final KeyMapper root;
	final Connection connection;
	final String dbId;
	final Map<String, SourceTableMapper> tableMappers;
	final Database database;
	
	public SourceDatabaseMapper(KeyMapper root, Connection connection, String dbId, Database database) {
		this.root = root;
		this.connection = connection;
		this.dbId = dbId;
		this.database = database;
	    this.tableMappers = new HashMap<>(500);
    }
	
	// interface -------------------------------------------------------------------------------------------------------
	
	public void store(IdentityModel table, Object sourcePK, String naturalKey, Object targetPK) {
		getOrCreateTableMapper(table).store(sourcePK, naturalKey, targetPK);
	}
	
	public Object getTargetPK(IdentityModel table, Object sourcePK) {
		return getOrCreateTableMapper(table).getTargetPK(sourcePK);
	}

	public String getNaturalKey(IdentityModel identity, Object sourcePK) {
		return getOrCreateTableMapper(identity).getNaturalKey(sourcePK);
    }
	
	// helper methods --------------------------------------------------------------------------------------------------

	private SourceTableMapper getOrCreateTableMapper(IdentityModel identity) {
		String tableName = identity.getTableName();
	    SourceTableMapper mapper = tableMappers.get(tableName);
	    if (mapper == null) {
	    	mapper = new SourceTableMapper(root, connection, dbId, identity, database);
	    	tableMappers.put(tableName, mapper);
	    }
	    return mapper;
    }

}
