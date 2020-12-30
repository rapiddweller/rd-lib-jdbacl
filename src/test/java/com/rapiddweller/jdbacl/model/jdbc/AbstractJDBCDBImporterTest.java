/*
 * (c) Copyright 2012 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.model.jdbc;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.rapiddweller.common.ConnectFailedException;
import com.rapiddweller.common.ErrorHandler;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.dialect.HSQLUtil;
import com.rapiddweller.jdbacl.model.DBIndex;
import com.rapiddweller.jdbacl.model.DBNonUniqueIndex;
import com.rapiddweller.jdbacl.model.DBSchema;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.model.DBUniqueIndex;
import com.rapiddweller.jdbacl.model.Database;

/**
 * Abstract parent class for tests that relate to child classes of {@link JDBCDBImporter}.<br/><br/>
 * Created: 02.02.2012 14:02:05
 * @since 0.8.0
 * @author Volker Bergmann
 */
public abstract class AbstractJDBCDBImporterTest {

	protected Connection setupDatabase() throws ConnectFailedException, IOException {
		Connection connection = HSQLUtil.connectInMemoryDB(getClass().getSimpleName());
		DBUtil.executeScriptFile("com/rapiddweller/jdbacl/model/jdbc/create_tables.hsql.sql", "ISO-8859-1", connection, true, new ErrorHandler(getClass()));
		return connection;
	}

	protected void dropDatabaseTables(Connection connection) throws SQLException {
		DBUtil.executeUpdate("drop table t1;", connection);
		connection.close();
	}

	protected static DBSchema checkSchema(Database db) {
		DBSchema schema = db.getCatalog(null).getSchema("public");
		assertNotNull(schema);
		return schema;
	}

	protected static DBTable checkTables(DBSchema schema) {
		assertEquals(1, schema.getTables().size());
        return schema.getTable("T1");
	}

	protected static void checkIndexes(DBTable table) {
		List<DBIndex> indexes = table.getIndexes();
		assertEquals(3, indexes.size());
		for (DBIndex index : indexes) {
			if (index instanceof DBNonUniqueIndex) {
				// non-unique nickname index
				assertEquals(1, index.getColumnNames().length);
				assertTrue("NICKNAME".equalsIgnoreCase(index.getColumnNames()[0]));
			} else if (index instanceof DBUniqueIndex) {
				if (index.getColumnNames().length == 1) {
					// PK index
					assertTrue("ID".equalsIgnoreCase(index.getColumnNames()[0]));
				} else {
					// unique composite index (namespace,name)
					assertEquals(2, index.getColumnNames().length);
					assertTrue("NAMESPACE".equalsIgnoreCase(index.getColumnNames()[0]));
					assertTrue("NAME".equalsIgnoreCase(index.getColumnNames()[1]));
				}
			} else
				fail("Unexpected index type: " + index.getClass() + '(' + index + ')');
		}
	}

}
