/*
 * (c) Copyright 2012-2021 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.common.exception.ConnectFailedException;
import com.rapiddweller.common.ErrorHandler;
import com.rapiddweller.common.Escalator;
import com.rapiddweller.common.Filter;
import com.rapiddweller.common.ImportFailedException;
import com.rapiddweller.common.Level;
import com.rapiddweller.common.LoggerEscalator;
import com.rapiddweller.common.NameUtil;
import com.rapiddweller.common.ObjectNotFoundException;
import com.rapiddweller.common.exception.ExceptionFactory;
import com.rapiddweller.common.StringUtil;
import com.rapiddweller.common.collection.OrderedNameMap;
import com.rapiddweller.common.version.VersionNumber;
import com.rapiddweller.contiperf.StopWatch;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.DatabaseDialectManager;
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

  final Escalator escalator = new LoggerEscalator();
  protected String url;
  protected String driver;
  protected String password;
  protected final String user;
  protected String catalogName;
  protected String schemaName;
  protected String tableInclusionPattern;
  protected String tableExclusionPattern;
  Connection connection;
  boolean connectionOwned;
  DatabaseDialect dialect;
  String databaseProductName;
  ErrorHandler errorHandler;
  TableNameFilter tableNameFilter;
  DatabaseMetaData metaData;
  private VersionNumber databaseProductVersion;

  public JDBCDBImporter(String url, String driver, String user, String password, String catalog, String schema) {
    this.url = url;
    this.driver = driver;
    this.user = user;
    this.password = password;
    this.catalogName = catalog;
    this.schemaName = schema;
    this.tableInclusionPattern = ".*";
    this.connection = null;
    this.connectionOwned = true;
    this.errorHandler = new ErrorHandler(getClass().getName(), Level.error);
    init();
  }

  public JDBCDBImporter(Connection connection, String user, String catalogName, String schemaName) {
    this.connection = connection;
    this.connectionOwned = false;
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
        throw ExceptionFactory.getInstance().programmerUnsupported("Not a supported rule: " + rule);
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

  public String getUrl() {
    return url;
  }

  public String getUser() {
    return user;
  }

  public String getCatalogName() {
    return catalogName;
  }

  public void setCatalogName(String catalogName) {
    this.catalogName = catalogName;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
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

  /** @deprecated */
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
    return new Database(url, this, true);
  }


  // catalog import --------------------------------------------------------------------------------------------------

  protected void init() {
    try {
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
    if (connectionOwned) {
      DBUtil.close(connection);
    }
  }


  // catalog import --------------------------------------------------------------------------------------------------

  public void importCatalogs(Database database) throws SQLException, ConnectFailedException {
    logger.debug("Importing catalogs from '{}'", url);
    StopWatch watch = new StopWatch("importCatalogs");
    ResultSet catalogSet = metaData.getCatalogs();
    int catalogCount = 0;
    while (catalogSet.next()) {
      String foundCatalog = catalogSet.getString(1);
      if (logger.isDebugEnabled()) {
        logger.debug("found catalog {}", StringUtil.quoteIfNotNull(foundCatalog));
      }
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


  // schema import ---------------------------------------------------------------------------------------------------

  private Set<String> getForeignSchemas(String schemaName) throws SQLException {
    Set<String> set = new HashSet<>();
    if (schemaName != null) {
      set.add(schemaName);
      if (this.dialect.getDbType().equals("h2")) {
        ResultSet resultSet = metaData.getSchemas();
        while (resultSet.next()) {
          set.add((String) resultSet.getObject("TABLE_SCHEM"));
        }
      } else if (!this.dialect.getDbType().equals("sql_server")) {
        ResultSet resultSet = metaData.getImportedKeys(null, schemaName, null);
        while (resultSet.next()) {
          set.add((String) resultSet.getObject("PKTABLE_SCHEM"));
        }
      }
    }
    return set;
  }

  public void importSchemas(Database database) throws SQLException {
    logger.debug("Importing schemas from system '{}'", url);
    StopWatch watch = new StopWatch("importSchemas");
    int schemaCount = 0;
    ResultSet schemaSet = metaData.getSchemas();
    Set<String> neededSchemas = getForeignSchemas(this.schemaName);
    while (schemaSet.next()) {
      String declaredSchemaName = schemaSet.getString(1);
      int columnCount = schemaSet.getMetaData().getColumnCount();
      String declaredCatalogName = (columnCount >= 2 ? schemaSet.getString(2) : null);
      if (neededSchemas.contains(declaredSchemaName)
          || (this.schemaName == null && dialect.isDefaultSchema(declaredSchemaName, user))) {
        debug("importing schema {}", StringUtil.quoteIfNotNull(declaredSchemaName));
        this.schemaName = declaredSchemaName; // take over capitalization used in the DB
        // PostgreSQL and SQL Server do not necessarily tell you the catalog name
        String catalogNameOfSchema = (declaredCatalogName != null ? declaredCatalogName : this.catalogName);
        DBCatalog catalogOfSchema = database.getCatalog(catalogNameOfSchema);
        if (catalogOfSchema == null) {
          throw new ObjectNotFoundException("Catalog of Schema not found: " + declaredSchemaName);
        }
        new DBSchema(declaredSchemaName, catalogOfSchema);
        importAllTables(database, declaredSchemaName);
        schemaCount++;
      } else {
        debug("ignoring schema {}", StringUtil.quoteIfNotNull(declaredSchemaName));
      }
    }
    haveAtLeastOneSchema(database, schemaCount);
    schemaSet.close();
    watch.stop();
  }

  private void haveAtLeastOneSchema(Database database, int importedSchemaCount) throws SQLException {
    if (importedSchemaCount == 0) {
      // add a default schema if none was reported (e.g. by MySQL)
      DBCatalog catalogToUse = database.getCatalog(catalogName);
      if (catalogToUse == null) {
        catalogToUse = database.getCatalogs().get(0);
      }
      catalogToUse.addSchema(new DBSchema(null));
      this.importAllTables(database);
    }
  }

  // table import ----------------------------------------------------------------------------------------------------

  public void importAllTables(Database database) throws SQLException {
    logger.info("Importing tables from environment '{}'", url);
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


  private void handleTableImport(Database database, StopWatch watch, ResultSet tableSet) throws SQLException {
    while (tableSet.next()) {

      // parsing ResultSet line
      String tableCatalogName = tableSet.getString(1);
      String tableSchemaName = tableSet.getString(2);
      String tableName = tableSet.getString(3);
      if (!tableSupported(tableName)) {
        logger.debug("ignoring table: {}, {}, {}", tableCatalogName, tableSchemaName, tableName);
        continue;
      }
      String tableTypeSpec = tableSet.getString(4);
      String tableRemarks = tableSet.getString(5);
      if (database.isReservedWord(tableName)) {
        logger.warn("Table name is a reserved word: '{}'", tableName);
      }
      logger.debug("importing table: {}, {}, {}, {}, {}",
          tableCatalogName, tableSchemaName, tableName, tableTypeSpec, tableRemarks);
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

  private boolean isOracleInternalTable(String tableName) {
    if (!isOracle()) {
      return false;
    } else if (tableName.startsWith("BIN$")) {
      // exclude Oracle BIN tables
      escalator.escalate("BIN$ table found (for improved performance " +
          "execute 'PURGE RECYCLEBIN;')", this, tableName);
      return true;
    } else {
      // exclude Oracle system tables
      return tableName.startsWith("SYS_");
    }
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
    String schemaPattern = schemaPattern(catalog, schemaName);
    debug("Importing columns for catalog {}, schemaPattern {}, tablePattern '{}'",
        StringUtil.quoteIfNotNull(catalog.getName()), StringUtil.quoteIfNotNull(schemaName),
        StringUtil.quoteIfNotNull(tablePattern));
    ResultSet columnSet = null;
    try {
      columnSet = metaData.getColumns(catalog.getName(), schemaPattern, tablePattern, null);
      ResultSetMetaData setMetaData = columnSet.getMetaData();
      if (setMetaData.getColumnCount() == 0) {
        return;
      }
      while (columnSet.next()) {
        importColumn(columnSet, catalog, schemaName, tableFilter, receiver);
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

  private void importColumn(ResultSet columnSet, DBCatalog catalog, String schemaName, Filter<String> tableFilter,
                            ColumnReceiver receiver) throws SQLException {
    String colSchemaName = columnSet.getString(2);
    String tableName = columnSet.getString(3);
    String columnName = columnSet.getString(4);
    if (ignoreColumn(tableName, tableFilter)) {
      logger.debug("ignoring column {}.{}.{}.{}", catalog.getName(), colSchemaName, tableName, columnName);
    } else {
      int sqlType = columnSet.getInt(5);
      String columnType = columnSet.getString(6);
      Integer columnSize = columnSet.getInt(7);
      if (columnSize == 0) { // this happens with INTEGER values on HSQLDB
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
          catalog.getName(), colSchemaName, tableName,
          columnName, sqlType, columnType, columnSize, decimalDigits,
          nullable, comment, defaultValue);
      // determine table
      DBTable table = catalog.getTable(tableName, false);
      if (table == null) {
        logger.debug("Ignoring column {}.{}", tableName, columnName);
        return;
      }
      DBSchema schema = catalog.getSchema(schemaName);
      if (schema != null) {
        table = schema.getTable(tableName);
      }
      // create column
      Integer fractionDigits = (decimalDigits > 0 ? decimalDigits : null);
      DBDataType dataType = DBDataType.getInstance(sqlType, columnType);
      defaultValue = parseDefaultValue(defaultValue, dataType);
      receiver.receiveColumn(columnName, dataType, columnSize, fractionDigits, nullable, defaultValue,
          comment, table);
      // not used: importVersionColumnInfo(catalogName, table, metaData)
    }
  }

  private String parseDefaultValue(String defaultValue, DBDataType dataType) {
    if (!StringUtil.isEmpty(defaultValue)) {
      if (!dataType.isAlpha()) {
        defaultValue = removeBrackets(defaultValue); // some driver adds brackets to number defaults
      }
      defaultValue = defaultValue.trim(); // oracle thin driver produces "1 "
    }
    return defaultValue;
  }

  private boolean ignoreColumn(String tableName, Filter<String> tableFilter) {
    return tableName.startsWith("BIN$") || (tableFilter != null && !tableFilter.accept(tableName));
  }

  private String schemaPattern(DBCatalog catalog, String schemaName) {
    String schemaPattern;
    if (schemaName != null) {
      schemaPattern = schemaName;
    } else if (catalog.getSchemas().size() == 1) {
      schemaPattern = catalog.getSchemas().get(0).getName();
    } else {
      schemaPattern = null;
    }
    return schemaPattern;
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
        logger.debug("found pk column {}, {}, {}", columnName, keySeq, pkName);
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


  // index import ----------------------------------------------------------------------------------------------------

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
        Boolean ascending = isAscending(ascOrDesc);
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
    mapIndexes(schema, queriedTable, receiver, indexes);
    watch.stop();
  }

  private void mapIndexes(DBSchema schema, DBTable queriedTable, IndexReceiver receiver, OrderedNameMap<DBIndexInfo> indexes) {
    for (DBIndexInfo indexInfo : indexes.values()) {
      DBTable table = (queriedTable != null ? queriedTable : schema.getTable(indexInfo.tableName));
      boolean deterministicName = dialect.isDeterministicIndexName(indexInfo.name);
      receiver.receiveIndex(indexInfo, deterministicName, table, schema);
    }
  }


  // foreign key import ----------------------------------------------------------------------------------------------

  public void importImportedKeys(DBTable table, FKReceiver receiver) {
    logger.debug("Importing imported keys for table '{}'", table.getName());
    StopWatch watch = new StopWatch("importImportedKeys");
    DBCatalog catalog = table.getCatalog();
    DBSchema schema = table.getSchema();
    String tableName = table.getName();
    ResultSet resultSet = null;
    try {
      resultSet = metaData.getImportedKeys(NameUtil.nameOrNull(catalog), NameUtil.nameOrNull(schema), tableName);
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
      buildFKConstraints(keyList, table, receiver);
    } catch (SQLException e) {
      errorHandler.handleError("Error importing foreign key constraints", e);
    } finally {
      DBUtil.close(resultSet);
    }
    watch.stop();
  }

  private void buildFKConstraints(List<ImportedKey> keyList, DBTable table, FKReceiver receiver) {
    for (ImportedKey key : keyList) {
      int n = key.getForeignKeyColumnNames().size();
      DBTable pkTable = key.getPkTable();
      DBCatalog catalog = table.getCatalog();
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
  }


  // check import ----------------------------------------------------------------------------------------------------

  public final void importAllChecks(Database database) {
    logger.info("Importing checks from environment '{}'", url);
    StopWatch watch = new StopWatch("importAllChecks");
    try {
      database.setChecksImported(true);
      if (dialect instanceof OracleDialect) {
        for (DBCatalog catalog : database.getCatalogs()) {
          for (DBSchema schema : catalog.getSchemas()) {
            importChecksOfSchema(schema);
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Error importing checks from " + url, e);
    }
    watch.stop();
  }

  private void importChecksOfSchema(DBSchema schema) throws SQLException, ConnectFailedException {
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


  // referrer table import -------------------------------------------------------------------------------------------

  public void importRefererTables(DBTable table, ReferrerReceiver receiver) {
    StopWatch watch = new StopWatch("importRefererTables");
    logger.debug("Importing exported keys for table '{}'", table);
    DBCatalog catalog = table.getCatalog();
    DBSchema schema = table.getSchema();
    String declaredCatalogName = NameUtil.nameOrNull(catalog);
    String declaredSchemaName = NameUtil.nameOrNull(schema);
    String tableName = table.getName();
    ResultSet resultSet = null;
    try {
      resultSet = metaData.getExportedKeys(declaredCatalogName, declaredSchemaName, tableName);
      while (resultSet.next()) {
        // ignoring String fktableCat = resultSet.getString(5)
        // ignoring String fktableSchem = resultSet.getString(6)
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


  // sequence import -------------------------------------------------------------------------------------------------

  public void importSequences(Database database) {
    logger.info("Importing sequences from environment '{}'", url);
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
      logger.error("Error importing sequences from environment '{}'", url, e);
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


  // trigger import --------------------------------------------------------------------------------------------------

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


  // package import --------------------------------------------------------------------------------------------------

  private void importPackagesOfSchema(DBSchema schema) throws SQLException {
    StopWatch watch = new StopWatch("importPackagesOfSchema");
    List<DBPackage> packages = dialect.queryPackages(schema, connection);
    for (DBPackage pkg : packages) {
      schema.receivePackage(pkg);
      pkg.setSchema(schema);
    }
    watch.stop();
  }

  // helper methods --------------------------------------------------------------------------------------------------
  
  protected boolean tableSupported(String tableName) {
    return (tableNameFilter.accept(tableName) && !isOracleInternalTable(tableName));
  }

  private Boolean isAscending(String ascOrDesc) {
    return ascOrDesc != null ? ascOrDesc.charAt(0) == 'A' : null;
  }

  private void debug(String format, Object... data) {
    logger.debug(format, data);
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
