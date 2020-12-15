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

package com.rapiddweller.jdbacl.identity;

import java.sql.Connection;

import com.rapiddweller.commons.iterator.TabularIterator;
import com.rapiddweller.jdbacl.model.Database;

/**
 * {@link IdentityModel} for a flat, non-recursive and independent table.<br/><br/>
 * Created: 01.09.2010 07:07:00
 * @since 0.6.4
 * @author Volker Bergmann
 */
public class NkPkQueryIdentity extends IdentityModel {
	
	private String nkPkQuery;

	public NkPkQueryIdentity(String tableName, String nkPkQuery) {
		super(tableName);
		setNkPkQuery(nkPkQuery);
    }

	public void setNkPkQuery(String nkPkQuery) {
		this.nkPkQuery = nkPkQuery;
    }

	@Override
	public String getDescription() {
		return "Identity definition by NK-PK query: " + nkPkQuery;
	}

	@Override
	public TabularIterator createNkPkIterator(Connection connection, String dbId, KeyMapper mapper, Database database) {
		return query(nkPkQuery, connection);
    }

}
