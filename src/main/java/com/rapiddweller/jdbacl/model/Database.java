/*
 * (c) Copyright 2006-2012 by Volker Bergmann. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted under the terms of the
 * GNU General Public License.
 *
 * For redistributing this software or a derivative work under a license other
 * than the GPL-compatible Free Software License as defined by the Free
 * Software Foundation or approved by OSI, you must first obtain a commercial
 * license to this software product from Volker Bergmann.
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

package com.rapiddweller.jdbacl.model;

import com.rapiddweller.common.ObjectNotFoundException;
import com.rapiddweller.common.StringUtil;
import com.rapiddweller.common.collection.OrderedNameMap;
import com.rapiddweller.common.version.VersionNumber;
import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.DatabaseDialectManager;
import com.rapiddweller.jdbacl.model.jdbc.JDBCDBImporter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Represents a database.<br/><br/>
 * Created: 06.01.2007 18:34:20
 *
 * @author Volker Bergmann
 */
public class Database extends AbstractCompositeDBObject<DBCatalog> implements TableHolder, SequenceHolder {

  private static final long serialVersionUID = -1975619615948817919L;

  private final String environment;

  private String productName;
  private VersionNumber productVersion;
  private Date importDate;
  private String user;
  private String tableInclusionPattern;
  private String tableExclusionPattern;

  private Set<String> reservedWords;

  private final OrderedNameMap<DBCatalog> catalogs;

  private final JDBCDBImporter importer;
  private boolean sequencesImported;
  private boolean triggersImported;
  private boolean packagesImported;
  private boolean checksImported;


  // constructors ----------------------------------------------------------------------------------------------------

  /**
   * Instantiates a new Database.
   *
   * @param environment the environment
   */
  public Database(String environment) {
    this(environment, new JDBCDBImporter(environment), true);
  }

  /**
   * Instantiates a new Database.
   *
   * @param environment    the environment
   * @param productName    the product name
   * @param productVersion the product version
   * @param importDate     the import date
   */
  public Database(String environment, String productName, String productVersion, Date importDate) {
    this(environment, null, false);
    this.productName = productName;
    this.productVersion = VersionNumber.valueOf(productVersion);
    this.importDate = importDate;
  }

  /**
   * Instantiates a new Database.
   *
   * @param environment the environment
   * @param importer    the importer
   * @param prepopulate the prepopulate
   */
  public Database(String environment, JDBCDBImporter importer, boolean prepopulate) {
    super(environment, "database");
    try {
      this.environment = environment;
      this.reservedWords = null;
      this.catalogs = OrderedNameMap.createCaseIgnorantMap();
      this.sequencesImported = false;
      this.triggersImported = false;
      this.packagesImported = false;
      this.checksImported = false;
      this.importer = importer;
      if (importer != null) {
        this.productName = importer.getDatabaseProductName();
        this.productVersion = importer.getDatabaseProductVersion();
        this.importDate = new Date();
        if (prepopulate) {
          importer.importCatalogs(this);
          importer.importSchemas(this);
          importer.importAllTables(this);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Database import failed for environment " + environment, e);
    }
  }

  // properties ------------------------------------------------------------------------------------------------------

  /**
   * Gets environment.
   *
   * @return the environment
   */
  public String getEnvironment() {
    return environment;
  }

  /**
   * Gets database product name.
   *
   * @return the database product name
   */
  public String getDatabaseProductName() {
    return productName;
  }

  /**
   * Gets database product version.
   *
   * @return the database product version
   */
  public VersionNumber getDatabaseProductVersion() {
    return productVersion;
  }

  /**
   * Gets import date.
   *
   * @return the import date
   */
  public Date getImportDate() {
    return importDate;
  }

  /**
   * Sets import date.
   *
   * @param importDate the import date
   */
  public void setImportDate(Date importDate) {
    this.importDate = importDate;
  }

  /**
   * Gets user.
   *
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * Sets user.
   *
   * @param user the user
   */
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * Gets table inclusion pattern.
   *
   * @return the table inclusion pattern
   */
  public String getTableInclusionPattern() {
    return tableInclusionPattern;
  }

  /**
   * Sets table inclusion pattern.
   *
   * @param tableInclusionPattern the table inclusion pattern
   */
  public void setTableInclusionPattern(String tableInclusionPattern) {
    this.tableInclusionPattern = tableInclusionPattern;
  }

  /**
   * Gets table exclusion pattern.
   *
   * @return the table exclusion pattern
   */
  public String getTableExclusionPattern() {
    return tableExclusionPattern;
  }

  /**
   * Sets table exclusion pattern.
   *
   * @param tableExclusionPattern the table exclusion pattern
   */
  public void setTableExclusionPattern(String tableExclusionPattern) {
    this.tableExclusionPattern = tableExclusionPattern;
  }

  /**
   * Is reserved word boolean.
   *
   * @param word the word
   * @return the boolean
   */
  public boolean isReservedWord(String word) {
    return getReservedWords().contains(word);
  }

  private Set<String> getReservedWords() {
    if (reservedWords == null) {
      try {
        Connection connection = (importer != null ? importer.getConnection() : null);
        DatabaseDialect dialect = DatabaseDialectManager.getDialectForProduct(productName, productVersion);
        reservedWords = dialect.getReservedWords(connection);
      } catch (Exception e) {
        throw new RuntimeException("Error fetching reserved words", e);
      }
    }
    return reservedWords;
  }

  /**
   * Gets importer.
   *
   * @return the importer
   */
  public JDBCDBImporter getImporter() {
    return importer;
  }

  // CompositeDBObject implementation --------------------------------------------------------------------------------

  @Override
  public List<DBCatalog> getComponents() {
    return catalogs.values();
  }

  // catalog operations ----------------------------------------------------------------------------------------------

  /**
   * Gets catalogs.
   *
   * @return the catalogs
   */
  public List<DBCatalog> getCatalogs() {
    return getComponents();
  }

  /**
   * Gets catalog.
   *
   * @param catalogName the catalog name
   * @return the catalog
   */
  public DBCatalog getCatalog(String catalogName) {
    return catalogs.get(catalogName);
  }

  /**
   * Add catalog.
   *
   * @param catalog the catalog
   */
  public void addCatalog(DBCatalog catalog) {
    catalog.setDatabase(this);
    catalogs.put(catalog.getName(), catalog);
  }

  /**
   * Remove catalog.
   *
   * @param catalog the catalog
   */
  public void removeCatalog(DBCatalog catalog) {
    catalogs.remove(catalog.getName());
    catalog.setOwner(null);
  }

  // schema operations -----------------------------------------------------------------------------------------------

  /**
   * Gets schema.
   *
   * @param schemaName the schema name
   * @return the schema
   */
  public DBSchema getSchema(String schemaName) {
    for (DBCatalog catalog : getCatalogs()) {
      DBSchema schema = catalog.getSchema(schemaName);
      if (schema != null) {
        return schema;
      }
    }
    throw new ObjectNotFoundException("Schema '" + name + "'");
  }

  // table operations ------------------------------------------------------------------------------------------------

  @Override
  public List<DBTable> getTables() {
    return getTables(true);
  }

  @Override
  public List<DBTable> getTables(boolean recursive) {
    if (!recursive) {
      return new ArrayList<>();
    }
    List<DBTable> tables = new ArrayList<>();
    for (DBCatalog catalog : getCatalogs()) {
      tables.addAll(catalog.getTables());
    }
    return tables;
  }

  @Override
  public DBTable getTable(String name) {
    return getTable(name, true);
  }

  /**
   * Gets table.
   *
   * @param name     the name
   * @param required the required
   * @return the table
   */
  public DBTable getTable(String name, boolean required) {
    for (DBCatalog catalog : getCatalogs()) {
      for (DBTable table : catalog.getTables()) {
        if (StringUtil.equalsIgnoreCase(table.getName(), name)) {
          return table;
        }
      }
    }
    if (required) {
      throw new ObjectNotFoundException("Table '" + name + "'");
    } else {
      return null;
    }
  }

  /**
   * Remove table.
   *
   * @param tableName the table name
   */
  public void removeTable(String tableName) {
    DBTable table = getTable(tableName, true);
    table.getSchema().removeTable(table);
  }


  // sequences -------------------------------------------------------------------------------------------------------

  /**
   * Gets sequences.
   *
   * @return the sequences
   */
  public List<DBSequence> getSequences() {
    haveSequencesImported();
    return getSequences(true);
  }

  @Override
  public List<DBSequence> getSequences(boolean recursive) {
    haveSequencesImported();
    if (!recursive) {
      return new ArrayList<>();
    }
    List<DBSequence> sequences = new ArrayList<>();
    for (DBCatalog catalog : getCatalogs()) {
      sequences.addAll(catalog.getSequences());
    }
    return sequences;
  }

  /**
   * Have sequences imported.
   */
  public synchronized void haveSequencesImported() {
    if (!sequencesImported) {
      if (importer != null) {
        importer.importSequences(this);
      }
      this.sequencesImported = true;
    }
  }

  /**
   * Is sequences imported boolean.
   *
   * @return the boolean
   */
  public boolean isSequencesImported() {
    return sequencesImported;
  }

  /**
   * Sets sequences imported.
   *
   * @param sequencesImported the sequences imported
   */
  public void setSequencesImported(boolean sequencesImported) {
    this.sequencesImported = sequencesImported;
  }


  // triggers --------------------------------------------------------------------------------------------------------

  /**
   * Gets triggers.
   *
   * @return the triggers
   */
  public List<DBTrigger> getTriggers() {
    haveTriggersImported();
    List<DBTrigger> triggers = new ArrayList<>();
    for (DBCatalog catalog : getCatalogs()) {
      for (DBSchema schema : catalog.getSchemas()) {
        triggers.addAll(schema.getTriggers());
      }
    }
    return triggers;
  }

  /**
   * Have triggers imported.
   */
  public synchronized void haveTriggersImported() {
    if (!triggersImported) {
      try {
        if (importer != null) {
          importer.importTriggers(this);
        }
        triggersImported = true;
      } catch (SQLException e) {
        throw new RuntimeException("Import of database triggers failed: " + getName(), e);
      }
    }
  }

  /**
   * Is triggers imported boolean.
   *
   * @return the boolean
   */
  public boolean isTriggersImported() {
    return triggersImported;
  }

  /**
   * Sets triggers imported.
   *
   * @param triggersImported the triggers imported
   */
  public void setTriggersImported(boolean triggersImported) {
    this.triggersImported = triggersImported;
  }


  // packages --------------------------------------------------------------------------------------------------------

  /**
   * Gets packages.
   *
   * @return the packages
   */
  public List<DBPackage> getPackages() {
    havePackagesImported();
    List<DBPackage> packages = new ArrayList<>();
    for (DBCatalog catalog : getCatalogs()) {
      for (DBSchema schema : catalog.getSchemas()) {
        packages.addAll(schema.getPackages());
      }
    }
    return packages;
  }

  /**
   * Have packages imported.
   */
  public synchronized void havePackagesImported() {
    if (!packagesImported) {
      try {
        packagesImported = true;
        if (importer != null) {
          importer.importPackages(this);
        }
      } catch (SQLException e) {
        throw new RuntimeException("Import of database packages failed: " + getName(), e);
      }
    }
  }

  /**
   * Is packages imported boolean.
   *
   * @return the boolean
   */
  public boolean isPackagesImported() {
    return packagesImported;
  }

  /**
   * Sets packages imported.
   *
   * @param packagesImported the packages imported
   */
  public void setPackagesImported(boolean packagesImported) {
    this.packagesImported = packagesImported;
  }


  // check constraints -----------------------------------------------------------------------------------------------

  /**
   * Is checks imported boolean.
   *
   * @return the boolean
   */
  public boolean isChecksImported() {
    return this.checksImported;
  }

  /**
   * Sets checks imported.
   *
   * @param checksImported the checks imported
   */
  public void setChecksImported(boolean checksImported) {
    this.checksImported = checksImported;
  }

  /**
   * Have checks imported.
   */
  public synchronized void haveChecksImported() {
    if (!isChecksImported()) {
      if (importer != null) {
        importer.importAllChecks(this);
      }
    }
  }

}
