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
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Abstract parent class for JDBC meta data importers.<br/><br/>
 * Created: 29.01.2012 21:52:06
 * @author Volker Bergmann
 * @since 0.8.0
 */
public class JDBCDBImporter implements DBMetaDataImporter {

  protected static final Logger logger = LoggerFactory.getLogger(JDBCDBImporter.class);

  private static final String TEMPORARY_ENVIRONMENT = "___temp";

  protected final String environment;
  protected final String folder;
  final Escalator escalator = new LoggerEscalator();
  protected String url;
  protected String driver;
  protected String password;
  protected String user;
  protected String catalogName;
  protected String schemaName;
  protected String tableInclusionPattern;
  protected String tableExclusionPattern;
  Connection connection;
  DatabaseDialect dialect;
  String databaseProductName;
  ErrorHandler errorHandler;
  TableNameFilter tableNameFilter;
  DatabaseMetaData metaData;
  private VersionNumber databaseProductVersion;

  public JDBCDBImporter(String environment, String folder) {
    this.connection = null;
    this.environment = environment;
    this.folder = folder;
    this.tableInclusionPattern = ".*";
    this.tableExclusionPattern = null;
    this.errorHandler = new ErrorHandler(getClass().getName(), Level.error);
    init();
  }

  public JDBCDBImporter(String url, String driver, String user, String password, String catalog, String schema) {
    this.connection = null;
    this.environment = TEMPORARY_ENVIRONMENT;
    this.folder = ".";
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

  public JDBCDBImporter(Connection connection, String environment, String folder, String user, String catalogName, String schemaName) {
    this.environment = (environment != null ? environment : TEMPORARY_ENVIRONMENT);
    this.folder = folder;
    this.connection = connection;
    this.user = user;
    this.catalogName = catalogName;
    this.schemaName = schemaName;
    this.errorHandler = new ErrorHandler(getClass().getName(), Level.error);
    init();
  }

  // properties ------------------------------------------------------------------------------------------------------

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

  protected static String removeBrackets(String defaultValue) {
    if (StringUtil.isEmpty(defaultValue)) {
      return defaultValue;
    }
    if (!defaultValue.startsWith("(") || !defaultValue.endsWith(")")) {
      return defaultValue;
    }
    return removeBrackets(defaultValue.substring(1, defaultValue.length() - 1));
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public String getCatalogName() {
    return catalogName;
  }

  public void setCatalogName(String catalogName) {
    this.catalogName = catalogName;
  }

  public String getUrl() {
    if (url == null) {
      url = "no url used";
    }
    return url;
  }

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
    if (this.connection == null) {
      StopWatch watch = new StopWatch("connect");
      this.connection = DBUtil.connect(url, driver, user, password, true);
      watch.stop();
    }
    return this.connection;
  }

  @Deprecated
  public void setTablePattern(String tablePattern) {
    this.tableInclusionPattern = tablePattern;
  }

  public void setTableInclusionPattern(String tableInclusionPattern) {
    this.tableInclusionPattern = tableInclusionPattern;
  }


  // database import -------------------------------------------------------------------------------------------------

  public void setTableExclusionPattern(String tableExclusionPattern) {
    this.tableExclusionPattern = tableExclusionPattern;
  }

  private boolean isOracle() {
    return databaseProductName.toLowerCase().startsWith("oracle");
  }

  @Override
  public Database importDatabase() throws ConnectFailedException, ImportFailedException {
    return new Database(environment, this, true);
  }


  // catalog import --------------------------------------------------------------------------------------------------

  protected void init() {
    try {
      if (!TEMPORARY_ENVIRONMENT.equals(environment)) {
        JDBCConnectData cd = DBUtil.getConnectData(environment, folder);
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
      logger.debug("Product: {} {}", databaseProductName, databaseProductVersion);
      dialect = DatabaseDialectManager.getDialectForProduct(databaseProductName, databaseProductVersion);
      if (isOracle()) { // fix for Oracle varchar column size, see http://kr.forums.oracle.com/forums/thread.jspa?threadID=554236
        DBUtil.executeUpdate("ALTER SESSION SET NLS_LENGTH_SEMANTICS=CHAR", getConnection());
      }
    } catch (Exception e) {
      throw new RuntimeException("Error initializing " + getClass(), e);
    }
  }

  @Override
  public void close() {
    DBUtil.close(connection);
  }

  public void importCatalogs(Database database) throws SQLException, ConnectFailedException {
    logger.debug("Importing catalogs from environment '{}'", database.getEnvironment());
    StopWatch watch = new StopWatch("importCatalogs");
    ResultSet catalogSet = metaData.getCatalogs();
    int catalogCount = 0;
    while (catalogSet.next()) {
      String foundCatalog = catalogSet.getString(1);
      logger.debug("found catalog {}", StringUtil.quoteIfNotNull(foundCatalog));
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
      database.addCatalog(new DBCatalog(this.catalogName));
    }
    catalogSet.close();
    watch.stop();
  }


  // table import ----------------------------------------------------------------------------------------------------

  // schema import ---------------------------------------------------------------------------------------------------
  // TODO refactor to support all dialects properly
  private Set<String> getForeignSchemas(String schemaName) throws SQLException {
    Set<String> set = new HashSet<>();
    if (schemaName != null) {
      set.add(schemaName);
      if (this.dialect.getSystem().equals("h2")) {
        ResultSet resultSet = metaData.getSchemas();
        while (resultSet.next()) {
          set.add((String) resultSet.getObject("TABLE_SCHEM"));
        }
      } else if (!this.dialect.getSystem().equals("sql_server")) {
        ResultSet resultSet = metaData.getImportedKeys(null, schemaName, null);
        while (resultSet.next()) {
          set.add((String) resultSet.getObject("PKTABLE_SCHEM"));
        }
      }
    }
    return set;
  }

  public void importSchemas(Database database) throws SQLException {
    logger.debug("Importing schemas from environment '{}'", database.getEnvironment());
    StopWatch watch = new StopWatch("importSchemas");
    int schemaCount = 0;
    ResultSet schemaSet = metaData.getSchemas();
    Set<String> neededSchemas = getForeignSchemas(this.schemaName);
    while (schemaSet.next()) {
      String schemaName = schemaSet.getString(1);
      String catalogName = null;
      int columnCount = schemaSet.getMetaData().getColumnCount();
      if (columnCount >= 2) {
        catalogName = schemaSet.getString(2);
      }
      if (neededSchemas.contains(schemaName)
          || (this.schemaName == null && dialect.isDefaultSchema(schemaName, user))) {
        logger.debug("importing schema {}", StringUtil.quoteIfNotNull(schemaName));
        this.schemaName = schemaName; // take over capitalization used in the DB
        String catalogNameOfSchema = (columnCount >= 2 && catalogName != null ? catalogName :
            this.catalogName); // PostgreSQL and SQL Server do not necessarily tell you the catalog name
        DBCatalog catalogOfSchema = database.getCatalog(catalogNameOfSchema);
        if (catalogOfSchema == null) {
          throw new ObjectNotFoundException("Catalog of Schema not found: " + schemaName);
        }
        new DBSchema(schemaName, catalogOfSchema);
        importAllTables(database, schemaName);
        schemaCount++;
      } else {
        logger.debug("ignoring schema {}", StringUtil.quoteIfNotNull(schemaName));
      }
    }
    if (schemaCount == 0) {
      // add a default schema if none was reported (e.g. by MySQL)
      DBCatalog catalogToUse = database.getCatalog(catalogName);
      if (catalogToUse == null) {
        catalogToUse = database.getCatalogs().get(0);
      }
      catalogToUse.addSchema(new DBSchema(null));
      this.importAllTables(database);
    }
    schemaSet.close();
    watch.stop();
  }

  public void importAllTables(Database database) throws SQLException {
    logger.info("Importing tables from environment '{}'", database.getEnvironment());
    if (tableExclusionPattern != null) {
      logger.debug("excluding tables: {}", tableExclusionPattern);
    }
    if (tableInclusionPattern != null && !".*".equals(tableInclusionPattern)) {
      logger.debug("including tables: {}", tableInclusionPattern);
    }
    StopWatch watch = new StopWatch("importAllTables");
    ResultSet tableSet;
    tableSet = metaData.getTables(this.catalogName, this.schemaName, null, new String[] {"TABLE", "VIEW"});

    handleTableImport(database, watch, tableSet);
  }

  public void importAllTables(Database database, String schemaName) throws SQLException {
    logger.info("Importing tables from schema '{}'", schemaName);
    if (tableExclusionPattern != null) {
      logger.debug("excluding tables: {}", tableExclusionPattern);
    }
    if (tableInclusionPattern != null && !".*".equals(tableInclusionPattern)) {
      logger.debug("including tables: {}", tableInclusionPattern);
    }
    StopWatch watch = new StopWatch("importAllTables");
    ResultSet tableSet;
    tableSet = metaData.getTables(this.catalogName, schemaName, null, new String[] {"TABLE", "VIEW"});

    handleTableImport(database, watch, tableSet);
  }


  // column import ---------------------------------------------------------------------------------------------------

  private void handleTableImport(Database database, StopWatch watch, ResultSet tableSet) throws SQLException {
    while (tableSet.next()) {

      // parsing ResultSet line
      String tableCatalogName = tableSet.getString(1);
      String tableSchemaName = tableSet.getString(2);
      String tableName = tableSet.getString(3);
      if (tableName.startsWith("BIN$")) {
        if (isOracle() && tableName.startsWith("BIN$")) {
          escalator.escalate("BIN$ table found (for improved performance " +
              "execute 'PURGE RECYCLEBIN;')", this, tableName);
        }
        continue;
      }
      // exclude oracle system tables
      if (tableName.startsWith("SYS_") && isOracle()) {
        continue;
      }
      if (!tableSupported(tableName)) {
        logger.debug("ignoring table: {}, {}, {}", new Object[] {tableCatalogName, tableSchemaName, tableName});
        continue;
      }
      String tableTypeSpec = tableSet.getString(4);
      String tableRemarks = tableSet.getString(5);
      if (database.isReservedWord(tableName)) {
        logger.warn("Table name is a reserved word: '{}'", tableName);
      }
      logger.debug("importing table: {}, {}, {}, {}, {}",
          new Object[] {tableCatalogName, tableSchemaName, tableName, tableTypeSpec, tableRemarks});
      TableType tableType = tableType(tableTypeSpec, tableName);
      DBCatalog catalog = database.getCatalog(tableCatalogName);
      DBSchema schema;
      if (catalog != null) {
        // that's the expected way
        schema = catalog.getSchema(tableSchemaName);
      } else {
        // postgres returns no catalog info, so we need to search for the schema in the whole database
        schema = database.getSchema(tableSchemaName);
      }
      if (schema != null) {
        DBTable table = new DBTable(tableName, tableType, tableRemarks, schema, this);
        table.setDoc(tableRemarks);
      } else {
        logger.warn("No schema specified. Ignoring table '{}'", tableName);
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

  public void importColumnsOfTable(DBTable table, ColumnReceiver receiver) {
    importColumns(table.getCatalog(), table.getSchema().getName(), table.getName(), tableNameFilter, receiver, errorHandler);
  }


  // index import ----------------------------------------------------------------------------------------------------

  protected void importColumns(DBCatalog catalog, String schemaName, String tablePattern,
                               Filter<String> tableFilter, ColumnReceiver receiver, ErrorHandler errorHandler) {
    StopWatch watch = new StopWatch("importColumns");
    String catalogName = catalog.getName();
    String schemaPattern = (schemaName != null ? schemaName : (catalog.getSchemas().size() == 1 ? catalog.getSchemas().get(0).getName() : null));
    logger.debug("Importing columns for catalog {}, schemaPattern {}, tablePattern '{}'",
        StringUtil.quoteIfNotNull(catalogName), StringUtil.quoteIfNotNull(schemaName),
            StringUtil.quoteIfNotNull(tablePattern));
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
          logger.debug("ignoring column {}.{}.{}.{}", new Object[] {catalogName, colSchemaName, tableName, columnName});
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

        logger.debug("found column: {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}",
            catalogName, colSchemaName, tableName,
            columnName, sqlType, columnType, columnSize, decimalDigits,
            nullable, comment, defaultValue);
        // determine table
        DBTable table = catalog.getTable(tableName, false);
        if (table == null) {
          logger.debug("Ignoring column {}.{}", tableName, columnName);
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

  public void importPrimaryKeyOfTable(DBTable table, PKReceiver receiver) {
    logger.debug("Importing primary keys for table '{}'", table);
    StopWatch watch = new StopWatch("importPrimaryKeyOfTable");
    ResultSet pkset = null;
    try {
      pkset = metaData.getPrimaryKeys(catalogName, table.getSchema().getName(), table.getName());
      TreeMap<Short, String> pkComponents = new TreeMap<>();
      String pkName = null;
      while (pkset.next()) {
        String tableName = pkset.getString(3);
        if (!tableName.equals(table.getName())) { // Bug fix for Firebird:
          continue;                            // When querying X, it returns the pks of XY too
        }

        String columnName = pkset.getString(4);
        short keySeq = pkset.getShort(5);
        pkComponents.put(keySeq, columnName);
        pkName = pkset.getString(6);
        logger.debug("found pk column {}, {}, {}", new Object[] {columnName, keySeq, pkName});
      }
      if (pkComponents.size() > 0) {
        String[] columnNames = pkComponents.values().toArray(new String[0]);
        receiver.receivePK(pkName, dialect.isDeterministicPKName(pkName), columnNames, table);
      }
    } catch (SQLException e) {
      errorHandler.handleError("Error importing primary key of table " + table.getName());
    } finally {
      DBUtil.close(pkset);
    }
    watch.stop();
  }


  // foreign key import ----------------------------------------------------------------------------------------------

  public void importIndexesOfTable(DBTable table, boolean uniquesOnly, IndexReceiver receiver) {
    StopWatch watch = new StopWatch("importIndexesOfTable");
    if (table.getTableType() == TableType.TABLE) {
      logger.debug("Importing indexes of table '{}'", table.getName());
    } else {
      logger.debug("Skipping indexes of table '{}' with type '{}'", table.getName(), table.getTableType());
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
        logger.debug("found {} {}, {}, {}, {}, {}, {}, {}, {}, {}",
            (unique ? "unique index" : "index"), indexName, indexCatalogName, indexType,
            ordinalPosition, columnName, ascOrDesc, cardinality, pages, filterCondition
        );

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


  // check import ----------------------------------------------------------------------------------------------------

  public void importImportedKeys(DBTable table, FKReceiver receiver) {
    logger.debug("Importing imported keys for table '{}'", table.getName());
    StopWatch watch = new StopWatch("importImportedKeys");
    DBCatalog catalog = table.getCatalog();
    DBSchema schema = table.getSchema();
    String catalogName = (catalog != null ? catalog.getName() : null);
    String tableName = table.getName();
    String schemaName = (schema != null ? schema.getName() : null);
    ResultSet resultSet = null;
    try {
      resultSet = metaData.getImportedKeys(catalogName, schemaName, tableName);

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
          } else { // some systems may not report an fk constraint name
            Objects.requireNonNull(recent).addForeignKeyColumn(cursor.fkcolumn_name,
                cursor.pkcolumn_name);
          }
        }
        recent = cursor;
      }
      // build DBForeignKeyConstraint objects from the gathered information
      for (ImportedKey key : keyList) {
        int n = key.getForeignKeyColumnNames().size();
        DBTable pkTable = key.getPkTable();
        if (pkTable == null && catalog != null) {
          DBSchema pkSchema = catalog.getSchema(key.getPkSchemaName());
          if (pkSchema != null) {
            pkTable = pkSchema.getTable(key.getPkTableName());
          } else {
            logger.warn("build DBForeignKeyConstraint objects from the gathered information could not get the proper Schema, " +
                "there might be an Error with this Database Dialect implementation!");
          }
        }
        String[] columnNames = new String[n];
        String[] refereeColumnNames = new String[n];
        for (int i = 0; i < n; i++) {
          columnNames[i] = key.getForeignKeyColumnNames().get(i);
          refereeColumnNames[i] = key.getRefereeColumnNames().get(i);
        }
        DBForeignKeyConstraint foreignKeyConstraint = new DBForeignKeyConstraint(
            key.fk_name, dialect.isDeterministicFKName(key.fk_name),
            table,
            columnNames,
            pkTable,
            refereeColumnNames);
        foreignKeyConstraint.setUpdateRule(parseRule(key.update_rule));
        foreignKeyConstraint.setDeleteRule(parseRule(key.delete_rule));
        receiver.receiveFK(foreignKeyConstraint, table);
        logger.debug("Imported foreign key {}", foreignKeyConstraint);
      }
    } catch (SQLException e) {
      errorHandler.handleError("Error importing foreign key constraints", e);
    } finally {
      DBUtil.close(resultSet);
    }
    watch.stop();
  }


  // referrer table import -------------------------------------------------------------------------------------------

  public final void importAllChecks(Database database) {
    logger.info("Importing checks from environment '{}'", database.getEnvironment());
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


  // sequence import -------------------------------------------------------------------------------------------------

  public void importRefererTables(DBTable table, ReferrerReceiver receiver) {
    StopWatch watch = new StopWatch("importRefererTables");
    logger.debug("Importing exported keys for table '{}'", table);
    DBCatalog catalog = table.getCatalog();
    DBSchema schema = table.getSchema();
    String catalogName = (catalog != null ? catalog.getName() : null);
    String tableName = table.getName();
    String schemaName = (schema != null ? schema.getName() : null);
    ResultSet resultSet = null;
    try {
      resultSet = metaData.getExportedKeys(catalogName, schemaName, tableName);
      while (resultSet.next()) {
        String fktableCat = resultSet.getString(5);
        String fktableSchem = resultSet.getString(6);
        String fktableName = resultSet.getString(7);
        if (tableSupported(fktableName)) {
          logger.debug("Importing referrer: {}", fktableName);
          receiver.receiveReferrer(fktableName, table);
        }
      }
    } catch (SQLException e) {
      errorHandler.handleError("Error importing foreign key constraints for table " + table, e);
    } finally {
      DBUtil.close(resultSet);
    }
    watch.stop();
  }


  // trigger import --------------------------------------------------------------------------------------------------

  public void importSequences(Database database) {
    logger.info("Importing sequences from environment '{}'", database.getEnvironment());
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
      logger.error("Error importing sequences from environment '{}'", database.getEnvironment(), e);
    }
    watch.stop();
  }

  public void importTriggers(Database database) throws SQLException {
    for (DBCatalog catalog : database.getCatalogs()) {
      for (DBSchema schema : catalog.getSchemas()) {
        importTriggersForSchema(schema);
      }
    }
  }


  // package import --------------------------------------------------------------------------------------------------

  private void importTriggersForSchema(DBSchema schema) throws SQLException {
    StopWatch watch = new StopWatch("importTriggersForSchema");
    dialect.queryTriggers(schema, connection);
    watch.stop();
  }

  public void importPackages(Database database) throws SQLException {
    for (DBCatalog catalog : database.getCatalogs()) {
      for (DBSchema schema : catalog.getSchemas()) {
        importPackagesOfSchema(schema);
      }
    }
  }


  // helper methods --------------------------------------------------------------------------------------------------

  private void importPackagesOfSchema(DBSchema schema) throws SQLException {
    StopWatch watch = new StopWatch("importPackagesOfSchema");
    List<DBPackage> packages = dialect.queryPackages(schema, connection);
    for (DBPackage pkg : packages) {
      schema.receivePackage(pkg);
      pkg.setSchema(schema);
    }
    watch.stop();
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
    void receiveReferrer(String fktableName, DBTable table);
  }

  public interface IndexReceiver {
    void receiveIndex(DBIndexInfo indexInfo, boolean deterministicName, DBTable table, DBSchema schema);
  }

}
