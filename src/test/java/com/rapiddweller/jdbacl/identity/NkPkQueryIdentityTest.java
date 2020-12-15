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

package com.rapiddweller.jdbacl.identity;

import static org.junit.Assert.*;

import java.sql.Connection;

import com.rapiddweller.commons.HeavyweightIterator;
import com.rapiddweller.jdbacl.dialect.HSQLUtil;
import com.rapiddweller.jdbacl.identity.mem.MemKeyMapper;
import com.rapiddweller.jdbacl.model.Database;
import org.junit.Test;

/**
 * Tests the {@link NkPkQueryIdentity}.<br/><br/>
 * Created: 06.12.2010 06:59:50
 * @since 0.4
 * @author Volker Bergmann
 */
public class NkPkQueryIdentityTest extends AbstractIdentityTest {

	@Test
	public void test() throws Exception {
		Connection connection = connectDB("db", HSQLUtil.DEFAULT_PORT + 1);
		createTables(connection);
		insertData(connection);
		
		Database database = importDatabase(connection);
		IdentityProvider identityProvider = createIdentities(database);
		IdentityModel countryIdentity = identityProvider.getIdentity("country");

		MemKeyMapper mapper = new MemKeyMapper(connection, "db", null, null, identityProvider, database);
		HeavyweightIterator<Object[]> iterator = countryIdentity.createNkPkIterator(connection, "db", mapper, database);
		expectCountryNkPk("DE", "DE", iterator);
		expectCountryNkPk("FR", "FR", iterator);
		expectCountryNkPk("UK", "UK", iterator);
		assertFalse(iterator.hasNext());

		dropTables(connection);
	}

}
