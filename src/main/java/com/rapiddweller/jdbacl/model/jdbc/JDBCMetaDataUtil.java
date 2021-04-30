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

import com.rapiddweller.common.ConnectFailedException;
import com.rapiddweller.common.ImportFailedException;
import com.rapiddweller.jdbacl.model.DBMetaDataImporter;
import com.rapiddweller.jdbacl.model.Database;
import com.rapiddweller.jdbacl.model.cache.CachingDBImporter;

import java.sql.Connection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Utility class for JDBC meta data retrieval.<br/><br/>
 * Created: 02.02.2012 13:20:12
 *
 * @author Volker Bergmann
 * @since 0.8.0
 */
public class JDBCMetaDataUtil {

  /**
   * Gets future meta data.
   *
   * @param environment           the environment
   * @param importUKs             the import u ks
   * @param importIndexes         the import indexes
   * @param importSequences       the import sequences
   * @param importChecks          the import checks
   * @param tableInclusionPattern the table inclusion pattern
   * @param tableExclusionPattern the table exclusion pattern
   * @param lazy                  the lazy
   * @param cached                the cached
   * @return the future meta data
   * @throws ConnectFailedException the connect failed exception
   * @throws ImportFailedException  the import failed exception
   */
  public static Future<Database> getFutureMetaData(String environment,
                                                   boolean importUKs, boolean importIndexes, boolean importSequences, boolean importChecks,
                                                   String tableInclusionPattern, String tableExclusionPattern, boolean lazy, boolean cached)
      throws ConnectFailedException, ImportFailedException {
    final DBMetaDataImporter importer = getJDBCDBImporter(environment, importUKs, importIndexes, importSequences,
        importChecks, tableInclusionPattern, tableExclusionPattern, cached);
    Callable<Database> callable = importer::importDatabase;
    return Executors.newSingleThreadExecutor().submit(callable);
  }

  /**
   * Gets meta data.
   *
   * @param environment           the environment
   * @param importUKs             the import u ks
   * @param importIndexes         the import indexes
   * @param importSequences       the import sequences
   * @param importChecks          the import checks
   * @param tableInclusionPattern the table inclusion pattern
   * @param tableExclusionPattern the table exclusion pattern
   * @param lazy                  the lazy
   * @param cached                the cached
   * @return the meta data
   * @throws ConnectFailedException the connect failed exception
   * @throws ImportFailedException  the import failed exception
   */
  public static Database getMetaData(String environment,
                                     boolean importUKs, boolean importIndexes, boolean importSequences, boolean importChecks,
                                     String tableInclusionPattern, String tableExclusionPattern, boolean lazy, boolean cached)
      throws ConnectFailedException, ImportFailedException {
    DBMetaDataImporter importer = getJDBCDBImporter(environment,
        importUKs, importIndexes, importSequences, importChecks, tableInclusionPattern, tableExclusionPattern, cached);
    return importer.importDatabase();
  }

  /**
   * Gets jdbcdb importer.
   *
   * @param environment           the environment
   * @param importUKs             the import u ks
   * @param importIndexes         the import indexes
   * @param importSequences       the import sequences
   * @param importChecks          the import checks
   * @param tableInclusionPattern the table inclusion pattern
   * @param tableExclusionPattern the table exclusion pattern
   * @param cached                the cached
   * @return the jdbcdb importer
   */
  public static DBMetaDataImporter getJDBCDBImporter(String environment,
                                                     boolean importUKs, boolean importIndexes, boolean importSequences, boolean importChecks,
                                                     String tableInclusionPattern, String tableExclusionPattern, boolean cached) {
    JDBCDBImporter dbImporter;
    dbImporter = new JDBCDBImporter(environment);
    dbImporter.setTableInclusionPattern(tableInclusionPattern);
    dbImporter.setTableExclusionPattern(tableExclusionPattern);
    DBMetaDataImporter importer = dbImporter;
    if (cached) {
      importer = new CachingDBImporter((JDBCDBImporter) importer, environment);
    }
    return importer;
  }

  /**
   * Gets meta data.
   *
   * @param target  the target
   * @param user    the user
   * @param catalog the catalog
   * @param schema  the schema
   * @return the meta data
   * @throws ConnectFailedException the connect failed exception
   * @throws ImportFailedException  the import failed exception
   */
  public static Database getMetaData(Connection target, String user, String catalog, String schema)
      throws ConnectFailedException, ImportFailedException {
    return getMetaData(target, user, catalog, schema, true, true, true, true, ".*", null);
  }

  /**
   * Gets meta data.
   *
   * @param connection            the connection
   * @param user                  the user
   * @param catalogName           the catalog name
   * @param schemaName            the schema name
   * @param importUKs             the import u ks
   * @param importIndexes         the import indexes
   * @param importSequences       the import sequences
   * @param importChecks          the import checks
   * @param tableInclusionPattern the table inclusion pattern
   * @param tableExclusionPattern the table exclusion pattern
   * @return the meta data
   * @throws ConnectFailedException the connect failed exception
   * @throws ImportFailedException  the import failed exception
   */
  public static Database getMetaData(Connection connection, String user, String catalogName, String schemaName,
                                     boolean importUKs, boolean importIndexes, boolean importSequences, boolean importChecks,
                                     String tableInclusionPattern, String tableExclusionPattern)
      throws ConnectFailedException, ImportFailedException {
    DBMetaDataImporter importer = getJDBCDBImporter(connection, user, catalogName, schemaName,
        importUKs, importIndexes, importSequences, importChecks,
        tableInclusionPattern, tableExclusionPattern);
    return importer.importDatabase();
  }

  /**
   * Gets jdbcdb importer.
   *
   * @param connection            the connection
   * @param user                  the user
   * @param schemaName            the schema name
   * @param importUKs             the import u ks
   * @param importIndexes         the import indexes
   * @param importSequences       the import sequences
   * @param importChecks          the import checks
   * @param tableInclusionPattern the table inclusion pattern
   * @param tableExclusionPattern the table exclusion pattern
   * @return the jdbcdb importer
   */
  public static JDBCDBImporter getJDBCDBImporter(Connection connection, String user, String catalogName, String schemaName,
                                                 boolean importUKs, boolean importIndexes, boolean importSequences, boolean importChecks,
                                                 String tableInclusionPattern, String tableExclusionPattern) {
    JDBCDBImporter importer;
    importer = new JDBCDBImporter(connection, user, catalogName, schemaName);
    importer.setTableInclusionPattern(tableInclusionPattern);
    importer.setTableExclusionPattern(tableExclusionPattern);
    return importer;
  }

}
