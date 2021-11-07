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

import java.io.IOException;
import java.sql.Connection;

/**
 * Utility class for JDBC meta data retrieval.<br/><br/>
 * Created: 02.02.2012 13:20:12
 * @author Volker Bergmann
 * @since 0.8.0
 */
public class JDBCMetaDataUtil {

  private JDBCMetaDataUtil() {
    // Private constructor to prevent instantiation of this utility class
  }

  public static Database getMetaData(String url, String driver, String user, String password, String catalogName, String schemaName,
                                   String tableInclusionPattern, String tableExclusionPattern)
    throws ConnectFailedException, IOException {
  try (DBMetaDataImporter importer = createJDBCDBImporter(url, driver, user, password, catalogName, schemaName,
      tableInclusionPattern, tableExclusionPattern)) {
    return importer.importDatabase();
  }
}

  public static Database getMetaData(Connection connection, String user, String catalogName, String schemaName,
                                     String tableInclusionPattern, String tableExclusionPattern)
      throws ConnectFailedException, ImportFailedException {
    DBMetaDataImporter importer = createJDBCDBImporter(connection, user, catalogName, schemaName,
        tableInclusionPattern, tableExclusionPattern);
    return importer.importDatabase();
  }

  public static JDBCDBImporter createJDBCDBImporter(
      String url, String driver, String user, String password, String catalogName, String schemaName,
      String tableInclusionPattern, String tableExclusionPattern) {
    JDBCDBImporter importer = new JDBCDBImporter(url, driver, user, password, catalogName, schemaName);
    importer.setTableInclusionPattern(tableInclusionPattern);
    importer.setTableExclusionPattern(tableExclusionPattern);
    return importer;
  }

  public static JDBCDBImporter createJDBCDBImporter(
      Connection connection, String user, String catalogName, String schemaName,
      String tableInclusionPattern, String tableExclusionPattern) {
    JDBCDBImporter importer = new JDBCDBImporter(connection, user, catalogName, schemaName);
    importer.setTableInclusionPattern(tableInclusionPattern);
    importer.setTableExclusionPattern(tableExclusionPattern);
    return importer;
  }

}
