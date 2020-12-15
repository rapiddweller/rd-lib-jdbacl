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

import com.rapiddweller.commons.ArrayUtil;
import com.rapiddweller.commons.ConfigurationError;
import com.rapiddweller.commons.iterator.TabularIterator;
import com.rapiddweller.jdbacl.model.Database;

/**
 * {@link IdentityModel} implementation for tables which have a natural key as primary key.<br/><br/>
 * Created: 12.12.2010 12:23:14
 * @since 0.6.4
 * @author Volker Bergmann
 */
public class NaturalPkIdentity extends IdentityModel {
	
	public NaturalPkIdentity(String tableName) {
		super(tableName);
	}

	@Override
	public TabularIterator createNkPkIterator(
			Connection connection, String dbId, KeyMapper mapper, Database database) {
		String[] pkColumnNames = database.getTable(tableName).getPKColumnNames();
		if (ArrayUtil.isEmpty(pkColumnNames))
			throw new ConfigurationError("Table '" + tableName + "' has no primary key");
		StringBuilder builder = new StringBuilder("select ");
		builder.append(pkColumnNames[0]);
		for (int i = 1; i < pkColumnNames.length; i++)
			builder.append(" || '|' || ").append(pkColumnNames[i]);
		for (String pkColumnName : pkColumnNames)
			builder.append(", ").append(pkColumnName);
		builder.append(" from ").append(tableName);
		String query = builder.toString();
		return query(query, connection);
	}

	@Override
	public String getDescription() {
		return tableName + " identity by primary key";
	}

}
