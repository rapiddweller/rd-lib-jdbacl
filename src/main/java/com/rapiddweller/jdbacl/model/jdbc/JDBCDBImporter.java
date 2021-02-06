/*
 * (c) Copyright 2012-2014 by Volker Bergmann. All rights reserved.
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
import com.rapiddweller.common.ErrorHandler;
import com.rapiddweller.common.Escalator;
import com.rapiddweller.common.Filter;
import com.rapiddweller.common.ImportFailedException;
import com.rapiddweller.common.Level;
import com.rapiddweller.common.LoggerEscalator;
import com.rapiddweller.common.ObjectNotFoundException;
import com.rapiddweller.common.ProgrammerError;
import com.rapiddweller.common.StringUtil;
import com.rapiddweller.common.collection.OrderedNameMap;
import com.rapiddweller.common.version.VersionNumber;
import com.rapiddweller.contiperf.StopWatch;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.DatabaseDialectManager;
import com.rapiddweller.jdbacl.JDBCConnectData;
import com.rapiddweller.jdbacl.dialect.OracleDialect;
import com.rapiddweller.jdbacl.model.DBCatalog;
import com.rapiddweller.jdbacl.model.DBCheckConstraint;
import com.rapiddweller.jdbacl.model.DBDataType;
import com.rapiddweller.jdbacl.model.DBForeignKeyConstraint;
import com.rapiddweller.jdbacl.model.DBMetaDataImporter;
import com.rapiddweller.jdbacl.model.DBPackage;
import com.rapiddweller.jdbacl.model.DBSchema;
import com.rapiddweller.jdbacl.model.DBSequence;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.model.Database;
import com.rapiddweller.jdbacl.model.FKChangeRule;
import com.rapiddweller.jdbacl.model.TableType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Abstract parent class for JDBC meta data importers.<br/><br/>
 * Created: 29.01.2012 21:52:06
 *
 * @author Volker Bergmann
 * @since 0.8.0
 */
public class JDBCDBImporter implements DBMetaDataImporter {

  private static final String TEMPORARY_ENVIRONMENT = "___temp";

  protected final static Logger LOGGER = LogManager.getLogger(JDBCDBImporter.class);

  protected final String environment;
  protected String url;
  protected String driver;
  protected String password;
  protected String user;
  protected String catalogName;
  protected String schemaName;
  protected String tableInclusionPattern;
  protected String tableExclusionPattern;

  Connection _connection;
  DatabaseDialect dialect;
  String databaseProductName;
  private VersionNumber databaseProductVersion;

  final Escalator escalator = new LoggerEscalator();
  ErrorHandler errorHandler;
  TableNameFilter tableNameFilter;

  DatabaseMetaData metaData;

  public JDBCDBImporter(String environment) {
    this._connection = null;
    this.environment = environment;
    this.tableInclusionPattern = ".*";
    this.tableExclusionPattern = null;
    this.errorHandler = new ErrorHandler(getClass().getName(), Level.error);
    init();
  }

  public JDBCDBImporter(String url, String driver, String user, String password, String catalog, String schema) {
    this._connection = null;
    this.environment = TEMPORARY_ENVIRONMENT;
    this.url = url;
    this.driver = driver;
    this.user = user;
    this.password = password;
    this.catalogName = catalog;
    this.schemaName = schema;
    this.tableInclusionPattern = ".*";
    this.errorHandler = new ErrorHandler(getClass().getName(), Level.error);
    init();
  }

  public JDBCDBImporter(Connection connection, String user, String schemaName) {
    this.environment = TEMPORARY_ENVIRONMENT;
    this._connection = connection;
    this.user = user;
    this.schemaName = schemaName;
    this.errorHandler = new ErrorHandler(getClass().getName(), Level.error);
    init();
  }

  // properties ------------------------------------------------------------------------------------------------------

  public String getDatabaseProductName() {
    return databaseProductName;
  }

  public VersionNumber getDatabaseProductVersion() {
    return databaseProductVersion;
  }

  public void setFaultTolerant(boolean faultTolerant) {
    this.errorHandler = new ErrorHandler(getClass().getName(), (faultTolerant ? Level.warn : Level.error));
  }

  public Connection getConnection() throws ConnectFailedException {
    if (this._connection == null) {
      StopWatch watch = new StopWatch("connect");
      this._connection = DBUtil.connect(url, driver, user, password, true);
      watch.stop();
    }
    return this._connection;
  }

  @Deprecated
  public void setTablePattern(String tablePattern) {
    this.tableInclusionPattern = tablePattern;
  }

  public void setTableInclusionPattern(String tableInclusionPattern) {
    this.tableInclusionPattern = tableInclusionPattern;
  }

  public void setTableExclusionPattern(String tableExclusionPattern) {
    this.tableExclusionPattern = tableExclusionPattern;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public void setCatalogName(String catalogName) {
    this.catalogName = catalogName;
  }

  private boolean isOracle() {
    return databaseProductName.toLowerCase().startsWith("oracle");
  }


  // database import -------------------------------------------------------------------------------------------------

  @Override
  public Database importDatabase() throws ConnectFailedException, ImportFailedException {
    return new Database(environment, this, true);
  }

  protected void init() {
    try {
      if (!TEMPORARY_ENVIRONMENT.equals(environment)) {
        JDBCConnectData cd = DBUtil.getConnectData(environment);
        if (this.url == null) {
          this.url = cd.url;
        }
        if (this.driver == null) {
          this.driver = cd.driver;
        }
        if (this.user == null) {
          this.user = cd.user;
        }
        if (this.password == null) {
          this.password = cd.password;
        }
        if (this.catalogName == null) {
          this.catalogName = cd.catalog;
        }
        if (this.schemaName == null) {
          this.schemaName = cd.schema;
        }
      }
      tableNameFilter = new TableNameFilter(tableInclusionPattern, tableExclusionPattern);
      StopWatch watch = new StopWatch("getMetaData");
      metaData = getConnection().getMetaData();
      watch.stop();
      databaseProductName = metaData.getDatabaseProductName();
      databaseProductVersion = VersionNumber.valueOf(metaData.getDatabaseProductVersion());
      LOGGER.debug("Product: {} {}", databaseProductName, databaseProductVersion);
      dialect = DatabaseDialectManager.getDialectForProduct(databaseProductName, databaseProductVersion);
      if (isOracle()) // fix for Oracle varchar column size, see http://kr.forums.oracle.com/forums/thread.jspa?threadID=554236
      {
        DBUtil.executeUpdate("ALTER SESSION SET NLS_LENGTH_SEMANTICS=CHAR", getConnection());
      }
    } catch (Exception e) {
      throw new RuntimeException("Error initializing " + getClass(), e);
    }
  }

  @Override
  public void close() {
    DBUtil.close(_connection);
  }


  // catalog import --------------------------------------------------------------------------------------------------

  public void importCatalogs(Database database) throws SQLException, ConnectFailedException {
    LOGGER.debug("Importing catalogs from environment '{}'", database.getEnvironment());
    StopWatch watch = new StopWatch("importCatalogs");
    ResultSet catalogSet = metaData.getCatalogs();
    int catalogCount = 0;
    while (catalogSet.next()) {
      String foundCatalog = catalogSet.getString(1);
      LOGGER.debug("found catalog {}", StringUtil.quoteIfNotNull(foundCatalog));
      if (StringUtil.equalsIgnoreCase(foundCatalog, this.catalogName) // this is the configured catalog
          || (StringUtil.isEmpty(this.catalogName) && ( // no catalog configured but...
          dialect.isDefaultCatalog(foundCatalog, user) // ...the one found is the default for the database
              || foundCatalog.equalsIgnoreCase(getConnection().getCatalog()) // or for the connection
      ))) {
        this.catalogName = foundCatalog;
        database.addCatalog(new DBCatalog(foundCatalog));
        catalogCount++;
      }
    }
    if (catalogCount == 0) {
      database.addCatalog(new DBCatalog(null));
    }
    catalogSet.close();
    watch.stop();
  }


  // schema import ---------------------------------------------------------------------------------------------------

  public void importSchemas(Database database) throws SQLException {
    LOGGER.debug("Importing schemas from environment '{}'", database.getEnvironment());
    StopWatch watch = new StopWatch("importSchemas");
    int schemaCount = 0;
    ResultSet schemaSet = metaData.getSchemas();
    while (schemaSet.next()) {
      String schemaName = schemaSet.getString(1);
      String catalogName = null;
      int columnCount = schemaSet.getMetaData().getColumnCount();
      if (columnCount >= 2) {
        catalogName = schemaSet.getString(2);
      }
      if (schemaName.equalsIgnoreCase(this.schemaName)
          || (this.schemaName == null && dialect.isDefaultSchema(schemaName, user)) || Objects.equals(this.tableInclusionPattern, "#all")) {
        LOGGER.debug("importing schema {}", StringUtil.quoteIfNotNull(schemaName));
        this.schemaName = schemaName; // take over capitalization used in the DB
        String catalogNameOfSchema = (columnCount >= 2 && catalogName != null ? catalogName :
            this.catalogName); // PostgreSQL and SQL Server do not necessarily tell you the catalog name
        DBCatalog catalogOfSchema = database.getCatalog(catalogNameOfSchema);
        if (catalogOfSchema == null) {
          throw new ObjectNotFoundException("Catalog not found: " + catalogOfSchema);
        }
        new DBSchema(schemaName, catalogOfSchema);
        schemaCount++;
      } else {
        LOGGER.debug("ignoring schema {}", StringUtil.quoteIfNotNull(schemaName));
      }
    }
    if (schemaCount == 0) {
      // add a default schema if none was reported (e.g. by MySQL)
      DBCatalog catalogToUse = database.getCatalog(catalogName);
      if (catalogToUse == null) {
        catalogToUse = database.getCatalogs().get(0);
      }
      catalogToUse.addSchema(new DBSchema(null));
    }
    schemaSet.close();
    watch.stop();
  }


  // table import ----------------------------------------------------------------------------------------------------

  public void importAllTables(Database database) throws SQLException {
    LOGGER.info("Importing tables from environment '{}'", database.getEnvironment());
    if (tableExclusionPattern != null) {
      LOGGER.debug("excluding tables: {}", tableExclusionPattern);
    }
    if (tableInclusionPattern != null && !".*".equals(tableInclusionPattern)) {
      LOGGER.debug("including tables: {}", tableInclusionPattern);
    }
    StopWatch watch = new StopWatch("importAllTables");
    ResultSet tableSet;
    if (Objects.equals(this.tableInclusionPattern, "#all")) {
      tableSet = metaData.getTables(null, null, null, new String[] {"TABLE", "VIEW"});
    } else {
      tableSet = metaData.getTables(catalogName, schemaName, null, new String[] {"TABLE", "VIEW"});
    }

    while (tableSet.next()) {

      // parsing ResultSet line
      String tCatalogName = tableSet.getString(1);
      String tSchemaName = tableSet.getString(2);
      String tableName = tableSet.getString(3);
      if (tableName.startsWith("BIN$")) {
        if (isOracle() && tableName.startsWith("BIN$")) {
          escalator.escalate("BIN$ table found (for improved performance " +
              "execute 'PURGE RECYCLEBIN;')", this, tableName);
        }
        continue;
      }
      if (!tableSupported(tableName)) {
        LOGGER.debug("ignoring table: {}, {}, {}", new Object[] {tCatalogName, tSchemaName, tableName});
        continue;
      }
      String tableTypeSpec = tableSet.getString(4);
      String tableRemarks = tableSet.getString(5);
      if (database.isReservedWord(tableName)) {
        LOGGER.warn("Table name is a reserved word: '{}'", tableName);
      }
      LOGGER.debug("importing table: {}, {}, {}, {}, {}",
          new Object[] {tCatalogName, tSchemaName, tableName, tableTypeSpec, tableRemarks});
      TableType tableType = tableType(tableTypeSpec, tableName);
      DBCatalog catalog = database.getCatalog(tCatalogName);
      DBSchema schema;
      if (catalog != null) {
        // that's the expected way
        schema = catalog.getSchema(tSchemaName);
      } else {
        // postgres returns no catalog info, so we need to search for the schema in the whole database
        schema = database.getSchema(tSchemaName);
      }
      if (schema != null) {
        DBTable table = new DBTable(tableName, tableType, tableRemarks, schema, this);
        table.setDoc(tableRemarks);
      } else {
        LOGGER.warn("No schema specified. Ignoring table '{}'", tableName);
      }
    }
    tableSet.close();
    watch.stop();
  }

  private TableType tableType(String tableTypeSpec, String tableName) {
    // Typical types are "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM"
    if (StringUtil.isEmpty(tableTypeSpec)) {
      return TableType.TABLE;
    }
    try {
      return TableType.valueOf(tableTypeSpec.replace(' ', '_'));
    } catch (Exception e) {
      escalator.escalate("Unknown table type '" + tableTypeSpec + "', assuming standard table", this, tableName);
      return TableType.TABLE;
    }
  }


  // column import ---------------------------------------------------------------------------------------------------

  public void importColumnsOfTable(DBTable table, ColumnReceiver receiver) {
    importColumns(table.getCatalog(), table.getSchema().getName(), table.getName(), tableNameFilter, receiver, errorHandler);
  }

  protected void importColumns(DBCatalog catalog, String schemaName, String tablePattern,
                               Filter<String> tableFilter, ColumnReceiver receiver, ErrorHandler errorHandler) {
    StopWatch watch = new StopWatch("importColumns");
    String catalogName = catalog.getName();
    String schemaPattern = (schemaName != null ? schemaName : (catalog.getSchemas().size() == 1 ? catalog.getSchemas().get(0).getName() : null));
    LOGGER.debug("Importing columns for catalog {}, schemaPattern {}, tablePattern '{}'",
        new Object[] {StringUtil.quoteIfNotNull(catalogName), StringUtil.quoteIfNotNull(schemaName),
            StringUtil.quoteIfNotNull(tablePattern)});
    ResultSet columnSet = null;
    try {
      columnSet = metaData.getColumns(catalogName, schemaPattern, tablePattern, null);
      ResultSetMetaData setMetaData = columnSet.getMetaData();
      if (setMetaData.getColumnCount() == 0) {
        return;
      }
      while (columnSet.next()) {
        String colSchemaName = columnSet.getString(2);
        String tableName = columnSet.getString(3);
        String columnName = columnSet.getString(4);
        if (tableName.startsWith("BIN$") || (tableFilter != null && !tableFilter.accept(tableName))) {
          LOGGER.debug("ignoring column {}.{}.{}.{}", new Object[] {catalogName, colSchemaName, tableName, columnName});
          continue;
        }
        int sqlType = columnSet.getInt(5);
        String columnType = columnSet.getString(6);
        Integer columnSize = columnSet.getInt(7);
        if (columnSize == 0) // happens with INTEGER values on HSQLDB
        {
          columnSize = null;
        }
        int decimalDigits = columnSet.getInt(9);
        boolean nullable = columnSet.getBoolean(11);
        String comment = columnSet.getString(12);
        String defaultValue = columnSet.getString(13);

        // Bug fix 3075401: boolean value generation problem in postgresql 8.4
        if (sqlType == Types.BIT && "bool".equalsIgnoreCase(columnType) && databaseProductName.toLowerCase().startsWith("postgres")) {
          sqlType = Types.BOOLEAN;
        }

        LOGGER.debug("found column: {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}",
            catalogName, colSchemaName, tableName,
            columnName, sqlType, columnType, columnSize, decimalDigits,
            nullable, comment, defaultValue);
        // determine table
        DBTable table = catalog.getTable(tableName, false);
        if (table == null) {
          LOGGER.debug("Ignoring column {}.{}", tableName, columnName);
          continue; // PostgreSQL returns the columns of indexes, too
        }
        DBSchema schema = catalog.getSchema(schemaName);
        if (schema != null) {
          table = schema.getTable(tableName);
        }
        // create column
        Integer fractionDigits = (decimalDigits > 0 ? decimalDigits : null);
        DBDataType dataType = DBDataType.getInstance(sqlType, columnType);
        if (!StringUtil.isEmpty(defaultValue)) {
          if (!dataType.isAlpha()) {
            defaultValue = removeBrackets(defaultValue); // some driver adds brackets to number defaults
          }
          defaultValue = defaultValue.trim(); // oracle thin driver produces "1 "
        }
        receiver.receiveColumn(columnName, dataType, columnSize, fractionDigits, nullable, defaultValue,
            comment, table);
        // not used: importVersionColumnInfo(catalogName, table, metaData);
      }
    } catch (SQLException e) {
      // possibly we try to access a catalog to which we do not have access rights
      if (errorHandler == null) {
        errorHandler = new ErrorHandler(getClass());
      }
      errorHandler.handleError("Error in parsing columns for table pattern " + tablePattern, e);
    } finally {
      DBUtil.close(columnSet);
    }
    watch.stop();
  }

/*
    private void importVersionColumnInfo(DBCatalog catalogName, DBTable table, DatabaseMetaData metaData) throws SQLException {
        ResultSet versionColumnSet = metaData.getVersionColumns(catalogName.getName(), null, table.getName());
//        DBUtil.print(versionColumnSet);
        while (versionColumnSet.next()) {
            // short scope = versionColumnSet.getString(1);
            String columnName = versionColumnSet.getString(2);
            //int dataType = versionColumnSet.getInt(3);
            //String typeName = versionColumnSet.getString(4);
            //int columnSize = versionColumnSet.getInt(5);
            //int bufferLength = versionColumnSet.getInt(6);
            //short decimalDigits = versionColumnSet.getShort(7);
            //short pseudoColumn = versionColumnSet.getShort(8);
            DBColumn column = table.getColumn(columnName);
            column.setVersionColumn(true);
        }
    }
*/


  // primary key import ----------------------------------------------------------------------------------------------

  public void importPrimaryKeyOfTable(DBTable table, PKReceiver receiver) {
    LOGGER.debug("Importing primary keys for table '{}'", table);
    StopWatch watch = new StopWatch("importPrimaryKeyOfTable");
    ResultSet pkset = null;
    try {
      if (this.tableInclusionPattern != null && this.tableInclusionPattern.contains("#all")) {
        // removed filter catalog and schema to get all primary keys for matching tables
        pkset = metaData.getPrimaryKeys(null, null, table.getName());
      } else {
        pkset = metaData.getPrimaryKeys(catalogName, schemaName, table.getName());
      }
      TreeMap<Short, String> pkComponents = new TreeMap<>();
      String pkName = null;
      while (pkset.next()) {
        String tableName = pkset.getString(3);
        if (!tableName.equals(table.getName())) // Bug fix for Firebird:
        {
          continue;                            // When querying X, it returns the pks of XY too
        }

        String columnName = pkset.getString(4);
        short keySeq = pkset.getShort(5);
        pkComponents.put(keySeq, columnName);
        pkName = pkset.getString(6);
        LOGGER.debug("found pk column {}, {}, {}", new Object[] {columnName, keySeq, pkName});
      }
      if (pkComponents.size() > 0) {
        String[] columnNames = pkComponents.values().toArray(new String[pkComponents.size()]);
        receiver.receivePK(pkName, dialect.isDeterministicPKName(pkName), columnNames, table);
      }
    } catch (SQLException e) {
      errorHandler.handleError("Error importing primary key of table " + table.getName());
    } finally {
      DBUtil.close(pkset);
    }
    watch.stop();
  }


  // index import ----------------------------------------------------------------------------------------------------

  public void importIndexesOfTable(DBTable table, boolean uniquesOnly, IndexReceiver receiver) {
    StopWatch watch = new StopWatch("importIndexesOfTable");
    if (table.getTableType() == TableType.TABLE) {
      LOGGER.debug("Importing indexes of table '{}'", table.getName());
    } else {
      LOGGER.debug("Skipping indexes of table '{}' with type '{}'", table.getName(), table.getTableType());
    }
    ResultSet indexSet = null;
    try {
      indexSet = metaData.getIndexInfo(table.getCatalog().getName(), table.getSchema().getName(), table.getName(), uniquesOnly, true);
      parseIndexSet(indexSet, table.getSchema(), table, receiver);
    } catch (SQLException e) {
      // possibly we try to query a catalog to which we do not have access rights
      errorHandler.handleError("Error parsing index data of table " + table.getName(), e);
    } finally {
      DBUtil.close(indexSet);
    }
    watch.stop();
  }

  public void parseIndexSet(ResultSet indexSet, DBSchema schema, DBTable queriedTable, IndexReceiver receiver) throws SQLException {
    StopWatch watch = new StopWatch("parseIndexSet");
    OrderedNameMap<DBIndexInfo> indexes = new OrderedNameMap<>();
    while (indexSet.next()) {
      String indexName = null;
      try {
        String tableName = indexSet.getString(3);
        if (!tableSupported(tableName) || (queriedTable != null && !queriedTable.getName().equalsIgnoreCase(tableName))) {
          continue; // table name is filtered out or a super string of the specified table name
        }
        boolean unique = !indexSet.getBoolean(4);
        String indexCatalogName = indexSet.getString(5);
        indexName = indexSet.getString(6);
        short indexType = indexSet.getShort(7);
        // not used:
        // tableIndexStatistic - this identifies table statistics that are returned in conjunction with a table's index descriptions
        // tableIndexClustered - this is a clustered index
        // tableIndexHashed - this is a hashed index
        // tableIndexOther - this is some other style of index
        //
        short ordinalPosition = indexSet.getShort(8);
        if (ordinalPosition == 0) {
          continue; // then indexType (7) is tableIndexStatistic
        }
        String columnName = indexSet.getString(9);
        String ascOrDesc = indexSet.getString(10);
        Boolean ascending = (ascOrDesc != null ? ascOrDesc.charAt(0) == 'A' : null);
        int cardinality = indexSet.getInt(11);
        int pages = indexSet.getInt(12);
        String filterCondition = indexSet.getString(13);
        LOGGER.debug("found " + (unique ? "unique index " : "index ") + indexName + ", "
            + indexCatalogName + ", " + indexType + ", "
            + ordinalPosition + ", " + columnName + ", " + ascOrDesc + ", "
            + cardinality + ", " + pages + ", " + filterCondition);
        DBIndexInfo index = indexes.get(indexName);
        if (index == null) {
          index = new DBIndexInfo(indexName, tableName, indexType, indexCatalogName, unique,
              ordinalPosition, columnName, ascending, cardinality, pages, filterCondition);
          indexes.put(indexName, index);
        } else {
          index.addColumn(ordinalPosition, columnName);
        }
      } catch (Exception e) {
        errorHandler.handleError("Error importing index " + indexName);
      }
    }
    for (DBIndexInfo indexInfo : indexes.values()) {
      DBTable table = (queriedTable != null ? queriedTable : schema.getTable(indexInfo.tableName));
      boolean deterministicName = dialect.isDeterministicIndexName(indexInfo.name);
      receiver.receiveIndex(indexInfo, deterministicName, table, schema);
    }
    watch.stop();
  }


  // foreign key import ----------------------------------------------------------------------------------------------

  public void importImportedKeys(DBTable table, FKReceiver receiver) {
    LOGGER.debug("Importing imported keys for table '{}'", table.getName());
    StopWatch watch = new StopWatch("importImportedKeys");
    DBCatalog catalog = table.getCatalog();
    DBSchema schema = table.getSchema();
    String catalogName = (catalog != null ? catalog.getName() : null);
    String tableName = table.getName();
    String schemaName = (schema != null ? schema.getName() : null);
    ResultSet resultSet = null;
    try {
      if (this.tableInclusionPattern != null && this.tableInclusionPattern.contains("#all")) {
        // removed filter catalog and schema to get all primary keys for matching tables
        resultSet = metaData.getImportedKeys(null, null, tableName);
      } else {
        resultSet = metaData.getImportedKeys(catalogName, schemaName, tableName);
      }
      List<ImportedKey> keyList = new ArrayList<>();
      Map<String, ImportedKey> keysByName = OrderedNameMap.createCaseIgnorantMap();
      ImportedKey recent = null;
      while (resultSet.next()) {
        ImportedKey cursor = ImportedKey.parse(resultSet, catalog, schema, table);
        if (cursor == null) {
          continue;
        }
        if (cursor.key_seq == 1) {
          if (cursor.fk_name != null) {
            keysByName.put(cursor.fk_name, cursor);
          }
          keyList.add(cursor);
        } else {
          // additional column for a composite FK with columns defined before
          if (cursor.fk_name != null) {
            keysByName.get(cursor.fk_name).addForeignKeyColumn(cursor.fkcolumn_name, cursor.pkcolumn_name);
          } else // some systems may not report an fk constraint name
          {
            recent.addForeignKeyColumn(cursor.fkcolumn_name, cursor.pkcolumn_name);
          }
        }
        recent = cursor;
      }
      // build DBForeignKeyConstraint objects from the gathered information
      for (ImportedKey key : keyList) {
        int n = key.getForeignKeyColumnNames().size();
        String[] columnNames = new String[n];
        String[] refereeColumnNames = new String[n];
        for (int i = 0; i < n; i++) {
          columnNames[i] = key.getForeignKeyColumnNames().get(i);
          refereeColumnNames[i] = key.getRefereeColumnNames().get(i);
        }
        DBForeignKeyConstraint foreignKeyConstraint = new DBForeignKeyConstraint(
            key.fk_name, dialect.isDeterministicFKName(key.fk_name), null, columnNames, key.getPkTable(), refereeColumnNames);
        foreignKeyConstraint.setUpdateRule(parseRule(key.update_rule));
        foreignKeyConstraint.setDeleteRule(parseRule(key.delete_rule));
        receiver.receiveFK(foreignKeyConstraint, table);
        LOGGER.debug("Imported foreign key {}", foreignKeyConstraint);
      }
    } catch (SQLException e) {
      errorHandler.handleError("Error importing foreign key constraints", e);
    } finally {
      DBUtil.close(resultSet);
    }
    watch.stop();
  }

  private static FKChangeRule parseRule(short rule) {
    switch (rule) {
      case DatabaseMetaData.importedKeyNoAction:
        return FKChangeRule.NO_ACTION;
      case DatabaseMetaData.importedKeyCascade:
        return FKChangeRule.CASCADE;
      case DatabaseMetaData.importedKeySetNull:
        return FKChangeRule.SET_NULL;
      case DatabaseMetaData.importedKeySetDefault:
        return FKChangeRule.SET_DEFAULT;
      case DatabaseMetaData.importedKeyRestrict:
        return FKChangeRule.NO_ACTION;
      default:
        throw new ProgrammerError("Not a supported rule: " + rule);
    }
  }


  // check import ----------------------------------------------------------------------------------------------------

  public final void importAllChecks(Database database) {
    LOGGER.info("Importing checks from environment '{}'", database.getEnvironment());
    StopWatch watch = new StopWatch("importAllChecks");
    try {
      database.setChecksImported(true);
      if (dialect instanceof OracleDialect) {
        for (DBCatalog catalog : database.getCatalogs()) {
          for (DBSchema schema : catalog.getSchemas()) {
            OracleDialect oraDialect = (OracleDialect) dialect;
            DBCheckConstraint[] newChecks = oraDialect.queryCheckConstraints(getConnection(), schema.getName());
            for (DBCheckConstraint newCheck : newChecks) {
              if (!tableSupported(newCheck.getTableName())) {
                continue;
              }
              DBTable table = schema.getTable(newCheck.getTableName());
              table.receiveCheckConstraint(newCheck);
              newCheck.setTable(table);
            }
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Error importing checks from " + database.getEnvironment(), e);
    }
    watch.stop();
  }


  // referrer table import -------------------------------------------------------------------------------------------

  public void importRefererTables(DBTable table, ReferrerReceiver receiver) {
    StopWatch watch = new StopWatch("importRefererTables");
    LOGGER.debug("Importing exported keys for table '{}'", table);
    DBCatalog catalog = table.getCatalog();
    DBSchema schema = table.getSchema();
    String catalogName = (catalog != null ? catalog.getName() : null);
    String tableName = table.getName();
    String schemaName = (schema != null ? schema.getName() : null);
    ResultSet resultSet = null;
    try {
      resultSet = metaData.getExportedKeys(catalogName, schemaName, tableName);
      while (resultSet.next()) {
        String fktable_cat = resultSet.getString(5);
        String fktable_schem = resultSet.getString(6);
        String fktable_name = resultSet.getString(7);
        if (tableSupported(fktable_name)) {
          LOGGER.debug("Importing referrer: {}", fktable_name);
          receiver.receiveReferrer(fktable_name, table);
        }
      }
    } catch (SQLException e) {
      errorHandler.handleError("Error importing foreign key constraints for table " + table, e);
    } finally {
      DBUtil.close(resultSet);
    }
    watch.stop();
  }


  // sequence import -------------------------------------------------------------------------------------------------

  public void importSequences(Database database) {
    LOGGER.info("Importing sequences from environment '{}'", database.getEnvironment());
    StopWatch watch = new StopWatch("importSequences");
    try {
      if (dialect.isSequenceSupported()) {
        DBSequence[] sequences = dialect.querySequences(getConnection());
        for (DBSequence sequence : sequences) {
          DBCatalog catalog = database.getCatalog(sequence.getCatalogName());
          if (catalog != null) {
            DBSchema schema = catalog.getSchema(sequence.getSchemaName());
            if (schema == null) {
              schema = catalog.getSchema(this.schemaName);
            }
            schema.receiveSequence(sequence);
            sequence.setOwner(schema);
          }
        }
      }
    } catch (Exception e) {
      LOGGER.error("Error importing sequences from environment '{}'", database.getEnvironment(), e);
    }
    watch.stop();
  }


  // trigger import --------------------------------------------------------------------------------------------------

  public void importTriggers(Database database) throws SQLException {
    for (DBCatalog catalog : database.getCatalogs()) {
      for (DBSchema schema : catalog.getSchemas()) {
        importTriggersForSchema(schema);
      }
    }
  }

  private void importTriggersForSchema(DBSchema schema) throws SQLException {
    StopWatch watch = new StopWatch("importTriggersForSchema");
    dialect.queryTriggers(schema, _connection);
    watch.stop();
  }


  // package import --------------------------------------------------------------------------------------------------

  public void importPackages(Database database) throws SQLException {
    for (DBCatalog catalog : database.getCatalogs()) {
      for (DBSchema schema : catalog.getSchemas()) {
        importPackagesOfSchema(schema);
      }
    }
  }

  private void importPackagesOfSchema(DBSchema schema) throws SQLException {
    StopWatch watch = new StopWatch("importPackagesOfSchema");
    List<DBPackage> packages = dialect.queryPackages(schema, _connection);
    for (DBPackage pkg : packages) {
      schema.receivePackage(pkg);
      pkg.setSchema(schema);
    }
    watch.stop();
  }


  // helper methods --------------------------------------------------------------------------------------------------

  protected static String removeBrackets(String defaultValue) {
    if (StringUtil.isEmpty(defaultValue)) {
      return defaultValue;
    }
    if (!defaultValue.startsWith("(") || !defaultValue.endsWith(")")) {
      return defaultValue;
    }
    return removeBrackets(defaultValue.substring(1, defaultValue.length() - 1));
  }

  protected boolean tableSupported(String tableName) {
    return tableNameFilter.accept(tableName);
  }


  // java.lang.Object overrides --------------------------------------------------------------------------------------

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  public interface ColumnReceiver {
    void receiveColumn(String columnName, DBDataType dataType, Integer columnSize, Integer fractionDigits,
                       boolean nullable, String defaultValue, String comment, DBTable table);
  }

  public interface PKReceiver {
    void receivePK(String pkName, boolean deterministicName, String[] columnNames, DBTable table);
  }

  public interface FKReceiver {
    void receiveFK(DBForeignKeyConstraint fk, DBTable table);
  }

  public interface ReferrerReceiver {
    void receiveReferrer(String fktable_name, DBTable table);
  }

  public interface IndexReceiver {
    void receiveIndex(DBIndexInfo indexInfo, boolean deterministicName, DBTable table, DBSchema schema);
  }

}
