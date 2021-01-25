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

import java.sql.Connection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.rapiddweller.common.ConnectFailedException;
import com.rapiddweller.common.ImportFailedException;
import com.rapiddweller.jdbacl.model.DBMetaDataImporter;
import com.rapiddweller.jdbacl.model.Database;
import com.rapiddweller.jdbacl.model.cache.CachingDBImporter;

/**
 * Utility class for JDBC meta data retrieval.<br/><br/>
 * Created: 02.02.2012 13:20:12
 * @since 0.8.0
 * @author Volker Bergmann
 */
public class JDBCMetaDataUtil {

	public static Future<Database> getFutureMetaData(String environment, 
			boolean importUKs, boolean importIndexes, boolean importSequences, boolean importChecks,
			String tableInclusionPattern, String tableExclusionPattern, boolean lazy, boolean cached) 
				throws ConnectFailedException, ImportFailedException {
		final DBMetaDataImporter importer = getJDBCDBImporter(environment, importUKs, importIndexes, importSequences, 
				importChecks, tableInclusionPattern, tableExclusionPattern, cached);
		Callable<Database> callable = importer::importDatabase;
		return Executors.newSingleThreadExecutor().submit(callable);
	}

	public static Database getMetaData(String environment, 
			boolean importUKs, boolean importIndexes, boolean importSequences, boolean importChecks,
			String tableInclusionPattern, String tableExclusionPattern, boolean lazy, boolean cached) 
				throws ConnectFailedException, ImportFailedException {
		DBMetaDataImporter importer = getJDBCDBImporter(environment, 
				importUKs, importIndexes, importSequences, importChecks, tableInclusionPattern, tableExclusionPattern, cached);
		return importer.importDatabase();
	}

	public static DBMetaDataImporter getJDBCDBImporter(String environment, 
			boolean importUKs, boolean importIndexes, boolean importSequences, boolean importChecks, 
			String tableInclusionPattern, String tableExclusionPattern, boolean cached) {
		JDBCDBImporter dbImporter;
		dbImporter = new JDBCDBImporter(environment);
		dbImporter.setTableInclusionPattern(tableInclusionPattern);
		dbImporter.setTableExclusionPattern(tableExclusionPattern);
		DBMetaDataImporter importer = dbImporter;
		if (cached)
			importer = new CachingDBImporter((JDBCDBImporter) importer, environment);
		return importer;
	}
	
	public static Database getMetaData(Connection target, String user, String schema) 
			throws ConnectFailedException, ImportFailedException {
		return getMetaData(target, user, schema, true, true, true, true, ".*", null);
	}
	
	public static Database getMetaData(Connection connection, String user, String schemaName,
				boolean importUKs, boolean importIndexes, boolean importSequences, boolean importChecks, 
				String tableInclusionPattern, String tableExclusionPattern) 
			throws ConnectFailedException, ImportFailedException {
		DBMetaDataImporter importer = getJDBCDBImporter(connection, user, schemaName, 
				importUKs, importIndexes, importSequences, importChecks, 
				tableInclusionPattern, tableExclusionPattern);
		return importer.importDatabase();
	}

	public static JDBCDBImporter getJDBCDBImporter(Connection connection, String user, String schemaName,
			boolean importUKs, boolean importIndexes, boolean importSequences, boolean importChecks, 
			String tableInclusionPattern, String tableExclusionPattern) {
		JDBCDBImporter importer;
		importer = new JDBCDBImporter(connection, user, schemaName);
		importer.setTableInclusionPattern(tableInclusionPattern);
		importer.setTableExclusionPattern(tableExclusionPattern);
		return importer;
	}

}
