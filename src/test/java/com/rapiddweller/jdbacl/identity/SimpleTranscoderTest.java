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

import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.DatabaseDialectManager;
import com.rapiddweller.jdbacl.dialect.HSQLUtil;
import com.rapiddweller.jdbacl.identity.mem.MemKeyMapper;
import com.rapiddweller.jdbacl.model.DBRow;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.model.Database;
import org.junit.Test;

/**
 * Tests the {@link SimpleTranscoder}.<br/><br/>
 * Created: 03.12.2010 11:39:45
 * @since 0.4
 * @author Volker Bergmann
 */
public class SimpleTranscoderTest extends AbstractIdentityTest {
	
	@Test
	public void test() throws Exception {
		Connection source = connectDB("s", HSQLUtil.DEFAULT_PORT + 1);
		createTables(source);
		insertData(source);
		
		Connection target = connectDB("t", HSQLUtil.DEFAULT_PORT + 2);
		createTables(target);

		Database database = importDatabase(target);
		DatabaseDialect dialect = DatabaseDialectManager.getDialectForProduct(
				database.getDatabaseProductName(), database.getDatabaseProductVersion());
		DBTable countryTable = database.getTable("COUNTRY");
		DBTable stateTable = database.getTable("STATE");

		IdentityProvider identityProvider = createIdentities(database);

		MemKeyMapper mapper = new MemKeyMapper(source, "s", target, "t", identityProvider, database);
		
		// country
		DBRow country = countryTable.queryByPK("DE", source, dialect);
		checkCountry("DE", "GERMANY", country);
		SimpleTranscoder.transcode(country, "DE", "DX", "s", identityProvider, mapper);
		checkCountry("DX", "GERMANY", country);

		// state
		DBRow state = stateTable.queryByPK(1, source, dialect);
		checkState(1, "DE", "BY", state);
		SimpleTranscoder.transcode(state, "DE|BY", 1001, "s", identityProvider, mapper);
		checkState(1001, "DX", "BY", state);
	}

	private static void checkCountry(String code, String name, DBRow country) {
		assertEquals(code, country.getCellValue("code"));
		assertEquals(name, country.getCellValue("name"));
	}
	
	private static void checkState(int id, String countryCode, String stateCode, DBRow state) {
		assertEquals(id, state.getCellValue("id"));
		assertEquals(countryCode, state.getCellValue("country"));
		assertEquals(stateCode, state.getCellValue("code"));
	}

}
