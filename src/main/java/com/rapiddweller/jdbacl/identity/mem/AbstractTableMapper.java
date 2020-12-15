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

import com.rapiddweller.commons.HeavyweightIterator;
import com.rapiddweller.commons.bean.ObjectOrArray;
import com.rapiddweller.jdbacl.identity.IdentityModel;
import com.rapiddweller.jdbacl.identity.KeyMapper;
import com.rapiddweller.jdbacl.model.Database;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Parent for classes that map the primary key values of the rows of one table in one database 
 * to their natural keys.<br/><br/>
 * Created: 07.09.2010 14:11:16
 * @since 0.6.4
 * @author Volker Bergmann
 */
public abstract class AbstractTableMapper {
	
	private static final Logger LOGGER = LogManager.getLogger(AbstractTableMapper.class);

	protected KeyMapper root;
	protected Connection connection;
	protected String dbId;
	protected IdentityModel identity;
	private   Map<ObjectOrArray, String> pkToNk;
	private   MapperState state;
	Database database;

	public AbstractTableMapper(KeyMapper root, Connection connection, String dbId, IdentityModel identity, Database database) {
		this.root = root;
		this.connection = connection;
		this.dbId = dbId;
		this.identity = identity;
	    this.database = database;
	    this.pkToNk = new HashMap<ObjectOrArray, String>(1000);
	    this.state = MapperState.CREATED;
    }
	
	// interface -------------------------------------------------------------------------------------------------------

	public Object store(Object pk, String naturalKey) {
		if (state == MapperState.CREATED)
			state = MapperState.PASSIVE;
		ObjectOrArray globalRowId = new ObjectOrArray(pk);
		return pkToNk.put(globalRowId, naturalKey);
	}

	public String getNaturalKey(Object pk) {
		assureInitialized();
		return pkToNk.get(new ObjectOrArray(pk));
    }
	
	// helpers ---------------------------------------------------------------------------------------------------------
	
	private void populate() {
		this.state = MapperState.POPULATING;
		LOGGER.debug("Populating key mapper for table {} on database {}", identity.getTableName(), dbId);
	    HeavyweightIterator<Object[]> iterator = identity.createNkPkIterator(connection, dbId, root, database);
	    while (iterator.hasNext()) {
	    	Object[] nkPkTuple = iterator.next();
	    	Object pk = identity.extractPK(nkPkTuple);
	    	String nk = identity.extractNK(nkPkTuple);
	    	store(pk, nk);
	    }
		this.state = MapperState.POPULATED;
    }

	protected void assureInitialized() {
	    if (state == MapperState.CREATED)
			populate();
    }

}
