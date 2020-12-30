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

import com.rapiddweller.common.ArrayFormat;
import com.rapiddweller.common.bean.HashCodeBuilder;
import com.rapiddweller.common.bean.ObjectOrArray;

/**
 * Global technical identifier for a database table rows 
 * which aggregates database id, table name and primary key.<br/><br/>
 * Created: 31.08.2010 16:19:41
 * @since 1.0
 * @author Volker Bergmann
 */
public class GlobalRowId {

	private final String schemaId;
	private final String tableName;
	private final ObjectOrArray pk;

	public GlobalRowId(String schemaId, String tableName, Object pk) {
	    this.schemaId = schemaId;
	    this.tableName = tableName;
	    this.pk = (pk instanceof ObjectOrArray ? (ObjectOrArray) pk : new ObjectOrArray(pk));
    }

	@Override
    public int hashCode() {
		return HashCodeBuilder.hashCode(schemaId, tableName, pk);
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null || getClass() != obj.getClass())
		    return false;
	    GlobalRowId that = (GlobalRowId) obj;
	    return (this.schemaId.equals(that.schemaId) 
	    		&& this.tableName.equals(that.tableName)
	    		&& this.pk.equals(that.pk));
    }
	
	@Override
	public String toString() {
	    return schemaId + '.' + tableName + '#' + renderPK(pk);
	}

	private static String renderPK(Object pk) {
		if (pk.getClass().isArray())
			return ArrayFormat.format((Object[]) pk);
		else
			return String.valueOf(pk);
    }
	
}
