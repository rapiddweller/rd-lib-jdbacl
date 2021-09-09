/*
 * (c) Copyright 2010-2014 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.model.xml;

import com.rapiddweller.common.ArrayUtil;
import com.rapiddweller.common.Assert;
import com.rapiddweller.common.ConfigurationError;
import com.rapiddweller.common.IOUtil;
import com.rapiddweller.common.ImportFailedException;
import com.rapiddweller.common.ParseUtil;
import com.rapiddweller.common.StringUtil;
import com.rapiddweller.common.SyntaxError;
import com.rapiddweller.common.xml.XMLUtil;
import com.rapiddweller.jdbacl.model.DBCatalog;
import com.rapiddweller.jdbacl.model.DBCheckConstraint;
import com.rapiddweller.jdbacl.model.DBColumn;
import com.rapiddweller.jdbacl.model.DBForeignKeyConstraint;
import com.rapiddweller.jdbacl.model.DBMetaDataImporter;
import com.rapiddweller.jdbacl.model.DBNonUniqueIndex;
import com.rapiddweller.jdbacl.model.DBPackage;
import com.rapiddweller.jdbacl.model.DBPrimaryKeyConstraint;
import com.rapiddweller.jdbacl.model.DBProcedure;
import com.rapiddweller.jdbacl.model.DBSchema;
import com.rapiddweller.jdbacl.model.DBSequence;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.model.DBTrigger;
import com.rapiddweller.jdbacl.model.DBUniqueConstraint;
import com.rapiddweller.jdbacl.model.DBUniqueIndex;
import com.rapiddweller.jdbacl.model.Database;
import com.rapiddweller.jdbacl.model.FKChangeRule;
import com.rapiddweller.jdbacl.model.TableType;
import com.rapiddweller.jdbacl.model.jdbc.JDBCDBImporter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Set;

/**
 * Imports a meta data model from an XML file.<br/><br/>
 * Created: 28.11.2010 15:18:55
 *
 * @author Volker Bergmann
 * @since 0.6.4
 */
public class XMLModelImporter implements DBMetaDataImporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(XMLModelImporter.class);

  private final String uri;
  private final JDBCDBImporter realImporter;

  /**
   * Instantiates a new Xml model importer.
   *
   * @param file         the file
   * @param realImporter the real importer
   */
  public XMLModelImporter(File file, JDBCDBImporter realImporter) {
    this(file.getAbsolutePath(), realImporter);
  }

  /**
   * Instantiates a new Xml model importer.
   *
   * @param uri          the uri
   * @param realImporter the real importer
   */
  public XMLModelImporter(String uri, JDBCDBImporter realImporter) {
    this.uri = uri;
    this.realImporter = realImporter;
  }

  @Override
  public Database importDatabase() throws ImportFailedException {
    InputStream in = null;
    try {
      in = IOUtil.getInputStreamForURI(uri);
      Document doc = XMLUtil.parse(in);
      return parseDatabase(doc.getDocumentElement());
    } catch (IOException e) {
      throw new ImportFailedException(e);
    } finally {
      IOUtil.close(in);
    }
  }

  private Database parseDatabase(Element e) {
    String environment = e.getAttribute("environment");
    if (StringUtil.isEmpty(environment)) {
      environment = e.getAttribute("name");
    }
    if (StringUtil.isEmpty(environment)) {
      throw new ConfigurationError("No environment defined in cache file");
    }
    Database db = new Database(environment, realImporter, false);
    db.setUser(e.getAttribute("user"));
    db.setTableInclusionPattern(e.getAttribute("tableInclusionPattern"));
    db.setTableExclusionPattern(e.getAttribute("tableExclusionPattern"));

    db.setSequencesImported(XMLUtil.getBooleanAttribute(e, "sequencesImported", true));
    db.setTriggersImported(XMLUtil.getBooleanAttribute(e, "triggersImported", true));
    db.setPackagesImported(XMLUtil.getBooleanAttribute(e, "packagesImported", true));
    db.setChecksImported(XMLUtil.getBooleanAttribute(e, "checksImported", true));

    // import catalogs
    for (Element child : XMLUtil.getChildElements(e)) {
      String childName = child.getNodeName();
      if ("catalog".equals(childName)) {
        parseCatalog(child, db);
      } else {
        throw new UnsupportedOperationException("Not an allowed element within <database>: " + childName);
      }
    }
    scanReferers(db);
    return db;
  }

  private void parseCatalog(Element e, Database db) {
    String name = StringUtil.emptyToNull(e.getAttribute("name"));
    DBCatalog catalog = new DBCatalog(name, db);
    for (Element child : XMLUtil.getChildElements(e)) {
      String childName = child.getNodeName();
      if ("schema".equals(childName)) {
        parseSchema(child, catalog);
      } else {
        throw new UnsupportedOperationException("Not an allowed element within <catalog>: " + childName);
      }
    }
  }

  private void parseSchema(Element e, DBCatalog catalog) {
    String name = e.getAttribute("name");
    DBSchema schema = new DBSchema(name, catalog);
    Element[] children = XMLUtil.getChildElements(e);

    // First parse elements without details in order to have all referenced tables available after
    for (Element child : children) {
      String childName = child.getNodeName();
      if ("table".equals(childName)) {
        parseTableName(child, schema);
      } else if (!"sequence".equals(childName) && !"trigger".equals(childName) && !"package".equals(childName)) {
        throw new UnsupportedOperationException("Not an allowed element within <schema>: " + childName);
      }
    }

    // finally parse the details of each db object and resolve references
    for (Element child : children) {
      String childName = child.getNodeName();
      if ("table".equals(childName)) {
        parseTableStructure(child, schema);
      } else if ("sequence".equals(childName)) {
        parseSequence(child, schema);
      } else if ("trigger".equals(childName)) {
        parseTrigger(child, schema);
      } else if ("package".equals(childName)) {
        parsePackage(child, schema);
      } else {
        throw new UnsupportedOperationException("Not an allowed element within <schema>: " + childName);
      }
    }
  }

  private static void parseTableName(Element e, DBSchema schema) {
    String name = e.getAttribute("name");
    String typeSpec = e.getAttribute("type");
    TableType type = (StringUtil.isEmpty(typeSpec) ? TableType.TABLE : TableType.valueOf(typeSpec));
    new DBTable(name, type, null, schema, schema.getDatabase().getImporter());
  }

  private void parseTableStructure(Element e, DBSchema schema) {
    String name = e.getAttribute("name");
    DBTable table = schema.getTable(name);
    boolean columnsImported = XMLUtil.getBooleanAttributeWithDefault(e, "columnsImported", true);
    table.setColumnsImported(columnsImported);
    boolean pkImported = XMLUtil.getBooleanAttributeWithDefault(e, "pkImported", true);
    table.setPKImported(pkImported);
    boolean fksImported = XMLUtil.getBooleanAttributeWithDefault(e, "fksImported", true);
    table.setFKsImported(fksImported);
    boolean indexesImported = XMLUtil.getBooleanAttributeWithDefault(e, "indexesImported", true);
    table.setIndexesImported(indexesImported);
    boolean checksImported = XMLUtil.getBooleanAttributeWithDefault(e, "checksImported", true);
    table.setChecksImported(checksImported);
    for (Element child : XMLUtil.getChildElements(e)) {
      String childName = child.getNodeName();
      if ("column".equals(childName)) {
        parseColumn(child, table);
      } else if ("pk".equals(childName)) {
        parsePK(child, table);
      } else if ("uk".equals(childName)) {
        parseUK(child, table);
      } else if ("fk".equals(childName)) {
        parseFK(child, table, schema);
      } else if ("check".equals(childName)) {
        parseCheck(child, table);
      } else if ("index".equals(childName)) {
        parseIndex(child, table);
      } else {
        throw new SyntaxError("Not an allowed element within <table>", XMLUtil.format(child));
      }
    }
  }

  private static void parseColumn(Element e, DBTable table) {
    String name = e.getAttribute("name");
    String typeAndSizeSpec = e.getAttribute("type");
    int jdbcType = Integer.parseInt(e.getAttribute("jdbcType"));
    DBColumn column = new DBColumn(name, table, jdbcType, typeAndSizeSpec);
    String defaultValue = e.getAttribute("default");
    if (!StringUtil.isEmpty(defaultValue)) {
      column.setDefaultValue(defaultValue);
    }
    String nullableSpec = e.getAttribute("nullable");
    boolean nullable = (!"false".equals(nullableSpec));
    column.setNullable(nullable);
  }

  private void parsePK(Element e, DBTable table) {
    boolean autoNamed = false;
    if (e.getAttribute("autoNamed") != null) {
      autoNamed = Boolean.parseBoolean(e.getAttribute("autoNamed"));
    }
    new DBPrimaryKeyConstraint(table, e.getAttribute("name"), autoNamed, parseColumnNames(e));
  }

  private void parseUK(Element e, DBTable table) {
    boolean autoNamed = false;
    if (e.getAttribute("autoNamed") != null) {
      autoNamed = Boolean.parseBoolean(e.getAttribute("autoNamed"));
    }
    new DBUniqueConstraint(table, e.getAttribute("name"), autoNamed, parseColumnNames(e));
  }

  private static void parseFK(Element e, DBTable owner, DBSchema schema) {
    String name = e.getAttribute("name");
    String refereeTableName = e.getAttribute("refereeTable");
    DBTable refereeTable = schema.getTable(refereeTableName);
    Assert.notNull(refereeTable, "refereeTable");
    String colAttr = e.getAttribute("column");
    String[] columnNames = null;
    String[] refereeColumnNames = null;
    if (!StringUtil.isEmpty(colAttr)) {
      columnNames = new String[] {colAttr};
      refereeColumnNames = new String[] {e.getAttribute("refereeColumn")};
    } else {
      Element colsElement = XMLUtil.getChildElement(e, false, true, "columns");
      Element[] colElements = XMLUtil.getChildElements(colsElement, false, "column");
      for (Element colElement : colElements) {
        columnNames = ArrayUtil.append(colElement.getAttribute("name"), columnNames);
        refereeColumnNames = ArrayUtil.append(colElement.getAttribute("refereeColumn"), refereeColumnNames);
      }
    }
    boolean autoNamed = false;
    if (e.getAttribute("autoNamed") != null) {
      autoNamed = Boolean.parseBoolean(e.getAttribute("autoNamed"));
    }
    DBForeignKeyConstraint fk = new DBForeignKeyConstraint(name, autoNamed, owner, columnNames, refereeTable, refereeColumnNames);
    // parse rules
    String updateRule = XMLUtil.getAttribute(e, "updateRule", false);
    if (!StringUtil.isEmpty(updateRule)) {
      fk.setUpdateRule(FKChangeRule.valueOf(updateRule));
    }
    String deleteRule = XMLUtil.getAttribute(e, "deleteRule", false);
    if (!StringUtil.isEmpty(deleteRule)) {
      fk.setDeleteRule(FKChangeRule.valueOf(deleteRule));
    }
  }

  private static void parseCheck(Element e, DBTable table) {
    try {
      table.getCatalog().getDatabase().setChecksImported(true);
      boolean autoNamed = false;
      if (e.getAttribute("autoNamed") != null) {
        autoNamed = Boolean.parseBoolean(e.getAttribute("autoNamed"));
      }
      new DBCheckConstraint(e.getAttribute("name"), autoNamed, table, e.getAttribute("definition"));
    } catch (Exception ex) {
      LOGGER.error("Error parsing check constraint", ex);
    }
  }

  private void parseIndex(Element e, DBTable table) {
    String name = e.getAttribute("name");
    String uniqueSpec = e.getAttribute("unique");
    boolean unique = ("true".equals(uniqueSpec));
    String nameDeterministicSpec = e.getAttribute("nameDeterministic");
    boolean nameDeterministic = (nameDeterministicSpec == null || "true".equals(nameDeterministicSpec));
    String[] columnNames = parseColumnNames(e);
    if (unique) {
      new DBUniqueIndex(name, nameDeterministic, table.getUniqueConstraint(columnNames));
    } else {
      new DBNonUniqueIndex(name, nameDeterministic, table, columnNames);
    }
  }

  /**
   * Parse column names string [ ].
   *
   * @param e the e
   * @return the string [ ]
   */
  public String[] parseColumnNames(Element e) {
    String colAttr = e.getAttribute("column");
    String[] columnNames = null;
    if (!StringUtil.isEmpty(colAttr)) {
      columnNames = new String[] {colAttr};
    } else {
      Element colsElement = XMLUtil.getChildElement(e, false, true, "columns");
      Element[] colElements = XMLUtil.getChildElements(colsElement, false, "column");
      for (Element colElement : colElements) {
        columnNames = ArrayUtil.append(colElement.getAttribute("name"), columnNames);
      }
    }
    return columnNames;
  }

  private static void scanReferers(Database database) {
    for (DBTable table : database.getTables()) {
      Set<DBForeignKeyConstraint> fks = table.getForeignKeyConstraints();
      for (DBForeignKeyConstraint fk : fks) {
        fk.getRefereeTable().receiveReferrer(table);
      }
    }
    for (DBTable table : database.getTables()) {
      if (table.areReferrersImported()) {
        table.setReferrersImported(true);
      }
    }
  }

  private static void parseSequence(Element e, DBSchema schema) {
    DBSequence sequence = new DBSequence(e.getAttribute("name"), schema);
    String start = e.getAttribute("start");
    if (!StringUtil.isEmpty(start)) {
      sequence.setStart(new BigInteger(start));
    }
    String increment = e.getAttribute("increment");
    if (!StringUtil.isEmpty(increment)) {
      sequence.setIncrement(new BigInteger(increment));
    }
    String maxValue = e.getAttribute("maxValue");
    if (!StringUtil.isEmpty(maxValue)) {
      sequence.setMaxValue(new BigInteger(maxValue));
    }
    String minValue = e.getAttribute("minValue");
    if (!StringUtil.isEmpty(minValue)) {
      sequence.setMinValue(new BigInteger(minValue));
    }
    String cycle = e.getAttribute("cycle");
    if (!StringUtil.isEmpty(cycle)) {
      sequence.setCycle(ParseUtil.parseBoolean(cycle));
    }
    String cache = e.getAttribute("cache");
    if (!StringUtil.isEmpty(cache)) {
      sequence.setCache(Long.parseLong(cache));
    }
    String order = e.getAttribute("order");
    if (!StringUtil.isEmpty(order)) {
      sequence.setOrder(ParseUtil.parseBoolean(order));
    }
  }

  private static void parseTrigger(Element e, DBSchema schema) {
    DBTrigger trigger = new DBTrigger(e.getAttribute("name"), null);
    schema.receiveTrigger(trigger);
    trigger.setOwner(schema);
    String triggerType = e.getAttribute("triggerType");
    if (!StringUtil.isEmpty(triggerType)) {
      trigger.setTriggerType(triggerType);
    }
    String triggeringEvent = e.getAttribute("triggeringEvent");
    if (!StringUtil.isEmpty(triggeringEvent)) {
      trigger.setTriggeringEvent(triggeringEvent);
    }
    String tableOwner = e.getAttribute("tableOwner");
    if (!StringUtil.isEmpty(tableOwner)) {
      trigger.setTableOwner(tableOwner);
    }
    String baseObjectType = e.getAttribute("baseObjectType");
    if (!StringUtil.isEmpty(baseObjectType)) {
      trigger.setBaseObjectType(baseObjectType);
    }
    String tableName = e.getAttribute("tableName");
    if (!StringUtil.isEmpty(tableName)) {
      trigger.setTableName(tableName);
    }
    String columnName = e.getAttribute("columnName");
    if (!StringUtil.isEmpty(columnName)) {
      trigger.setColumnName(columnName);
    }
    String referencingNames = e.getAttribute("referencingNames");
    if (!StringUtil.isEmpty(referencingNames)) {
      trigger.setReferencingNames(referencingNames);
    }
    String whenClause = e.getAttribute("whenClause");
    if (!StringUtil.isEmpty(whenClause)) {
      trigger.setWhenClause(whenClause);
    }
    String status = e.getAttribute("status");
    if (!StringUtil.isEmpty(status)) {
      trigger.setStatus(status);
    }
    String description = e.getAttribute("description");
    if (!StringUtil.isEmpty(description)) {
      trigger.setDescription(description);
    }
    String actionType = e.getAttribute("actionType");
    if (!StringUtil.isEmpty(actionType)) {
      trigger.setActionType(actionType);
    }
    String triggerBody = e.getAttribute("triggerBody");
    if (!StringUtil.isEmpty(triggerBody)) {
      trigger.setTriggerBody(triggerBody);
    }
  }

  private static void parsePackage(Element e, DBSchema schema) {
    DBPackage pkg = new DBPackage(e.getAttribute("name"), null);
    pkg.setSchema(schema);
    schema.receivePackage(pkg);
    String subObjectName = e.getAttribute("subObjectName");
    if (!StringUtil.isEmpty(subObjectName)) {
      pkg.setSubObjectName(subObjectName);
    }
    String objectId = e.getAttribute("objectId");
    if (!StringUtil.isEmpty(objectId)) {
      pkg.setObjectId(objectId);
    }
    String dataObjectId = e.getAttribute("dataObjectId");
    if (!StringUtil.isEmpty(dataObjectId)) {
      pkg.setDataObjectId(dataObjectId);
    }
    String objectType = e.getAttribute("objectType");
    if (!StringUtil.isEmpty(objectType)) {
      pkg.setObjectType(objectType);
    }
    String status = e.getAttribute("status");
    if (!StringUtil.isEmpty(status)) {
      pkg.setStatus(status);
    }
    parsePackageProcedures(e, pkg);
  }

  private static void parsePackageProcedures(Element pkgElement, DBPackage pkg) {
    for (Element e : XMLUtil.getChildElements(pkgElement)) {
      String nodeName = e.getNodeName();
      if ("procedure".equals(nodeName)) {
        DBProcedure procedure = new DBProcedure(e.getAttribute("name"), pkg);
        String objectId = e.getAttribute("objectId");
        if (!StringUtil.isEmpty(objectId)) {
          procedure.setObjectId(objectId);
        }
        String subProgramId = e.getAttribute("subProgramId");
        if (!StringUtil.isEmpty(subProgramId)) {
          procedure.setSubProgramId(subProgramId);
        }
        String overload = e.getAttribute("overload");
        if (!StringUtil.isEmpty(overload)) {
          procedure.setOverload(overload);
        }
      } else {
        throw new SyntaxError("Illegal child element of <package>", XMLUtil.format(e));
      }
    }
  }

  @Override
  public void close() {
    // nothing special to do
  }

}
