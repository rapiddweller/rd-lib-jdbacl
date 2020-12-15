/*
 * (c) Copyright 2010 by Volker Bergmann. All rights reserved.
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
 * In-memory implementation of the mapping functionality needed for source database tables.<br/><br/>
 * Created: 24.08.2010 10:59:40
 * @since 0.6.4
 * @author Volker Bergmann
 */
public class SourceTableMapper extends AbstractTableMapper {
	
	Map<Object, Object> sourcePkToTargetPk;
	
	public SourceTableMapper(KeyMapper root, Connection connection, String dbId, IdentityModel table, Database database) {
		super(root, connection, dbId, table, database);
	    this.sourcePkToTargetPk = new HashMap<Object, Object>(1000);
    }
	
	public void store(Object sourcePK, String naturalKey, Object targetPK) {
		super.store(sourcePK, naturalKey);
		if (targetPK != null)
			sourcePkToTargetPk.put(sourcePK, targetPK);
	}
	
	public Object getTargetPK(Object sourcePK) {
		assureInitialized();
		return sourcePkToTargetPk.get(sourcePK);
	}

}
