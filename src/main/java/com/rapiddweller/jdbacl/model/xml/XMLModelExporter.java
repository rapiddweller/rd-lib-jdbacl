/*
 * (c) Copyright 2010-2021 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.common.Encodings;
import com.rapiddweller.common.xml.SimpleXMLWriter;
import com.rapiddweller.jdbacl.SQLUtil;
import com.rapiddweller.jdbacl.model.DBCatalog;
import com.rapiddweller.jdbacl.model.DBCheckConstraint;
import com.rapiddweller.jdbacl.model.DBColumn;
import com.rapiddweller.jdbacl.model.DBForeignKeyConstraint;
import com.rapiddweller.jdbacl.model.DBIndex;
import com.rapiddweller.jdbacl.model.DBMetaDataExporter;
import com.rapiddweller.jdbacl.model.DBPackage;
import com.rapiddweller.jdbacl.model.DBPrimaryKeyConstraint;
import com.rapiddweller.jdbacl.model.DBProcedure;
import com.rapiddweller.jdbacl.model.DBSchema;
import com.rapiddweller.jdbacl.model.DBSequence;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.model.DBTrigger;
import com.rapiddweller.jdbacl.model.DBUniqueConstraint;
import com.rapiddweller.jdbacl.model.Database;
import com.rapiddweller.jdbacl.model.FKChangeRule;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import static com.rapiddweller.common.xml.SimpleXMLWriter.addAttribute;
import static com.rapiddweller.common.xml.SimpleXMLWriter.createAttributes;

/**
 * Saves a database meta data model as XML file.<br/><br/>
 * Created: 28.11.2010 06:30:25
 * @author Volker Bergmann
 * @since 0.6.4
 */
public class XMLModelExporter implements DBMetaDataExporter {

  private static final String COLUMNS = "columns";
  private static final String COLUMN = "column";
  private static final String NAME = "name";
  private static final String FALSE = "false";

  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private final File file;
  private final String encoding;
  private final boolean lazy;

  // constructors ----------------------------------------------------------------------------------------------------

  public XMLModelExporter(File file) {
    this(file, true);
  }

  public XMLModelExporter(File file, boolean lazy) {
    this(file, Encodings.UTF_8, lazy);
  }

  public XMLModelExporter(File file, String encoding, boolean lazy) {
    this.file = file;
    this.encoding = encoding;
    this.lazy = lazy;
  }

  // interface -------------------------------------------------------------------------------------------------------

  @Override
  public void export(Database database) throws IOException {
    OutputStream out = new FileOutputStream(file);
    try (SimpleXMLWriter writer = new SimpleXMLWriter(out, encoding, true)) {
      writer.startDocument();
      exportDatabase(database, writer);
      writer.endDocument();
    } catch (SAXException e) {
      throw new RuntimeException("Error exporting database " + database, e);
    }
  }

  // private helper methods ------------------------------------------------------------------------------------------

  private void exportDatabase(Database database, SimpleXMLWriter writer)
      throws SAXException {
    AttributesImpl attribs = createAttributes(NAME, database.getName());
    if (database.getDatabaseProductName() != null) {
      addAttribute("databaseProductName", database.getDatabaseProductName(), attribs);
    }
    if (database.getDatabaseProductVersion() != null) {
      addAttribute("databaseProductVersion", database.getDatabaseProductVersion().toString(), attribs);
    }
    if (database.getImportDate() != null) {
      addAttribute("importDate", sdf.format(database.getImportDate()), attribs);
    }
    addAttribute("user", database.getUser(), attribs);
    addAttribute("tableInclusionPattern", database.getTableInclusionPattern(), attribs);
    addAttribute("tableExclusionPattern", database.getTableExclusionPattern(), attribs);
    addAttribute("checksImported", String.valueOf(database.isChecksImported()), attribs);
    addAttribute("sequencesImported", String.valueOf(database.isSequencesImported()), attribs);
    addAttribute("triggersImported", String.valueOf(database.isTriggersImported()), attribs);
    addAttribute("packagesImported", String.valueOf(database.isSequencesImported()), attribs);
    writer.startElement("database", attribs);
    for (DBCatalog catalog : database.getCatalogs()) {
      exportCatalog(catalog, writer);
    }
    writer.endElement("database");
  }

  private void exportCatalog(DBCatalog catalog, SimpleXMLWriter writer) throws SAXException {
    writer.startElement("catalog", createAttributes(NAME, catalog.getName()));
    for (DBSchema schema : catalog.getSchemas()) {
      exportSchema(schema, writer);
    }
    writer.endElement("catalog");
  }

  private void exportSchema(DBSchema schema, SimpleXMLWriter writer) throws SAXException {
    Database db = schema.getDatabase();
    AttributesImpl atts = createAttributes(NAME, schema.getName());
    writer.startElement("schema", atts);
    for (DBTable table : schema.getTables()) {
      exportTable(table, writer);
    }
    if (db.isSequencesImported()) {
      for (DBSequence sequence : schema.getSequences(true)) {
        exportSequence(sequence, writer);
      }
    }
    if (db.isTriggersImported()) {
      for (DBTrigger trigger : schema.getTriggers()) {
        exportTrigger(trigger, writer);
      }
    }
    if (db.isPackagesImported()) {
      for (DBPackage pkg : schema.getPackages()) {
        exportPackage(pkg, writer);
      }
    }
    writer.endElement("schema");
  }

  private void exportTable(DBTable table, SimpleXMLWriter writer) throws SAXException {
    AttributesImpl atts = createAttributes(NAME, table.getName());
    if (lazy && !table.areColumnsImported()) {
      addAttribute("columnsImported", FALSE, atts);
    }
    if (lazy && !table.isPKImported()) {
      addAttribute("pkImported", FALSE, atts);
    }
    if (lazy && !table.areFKsImported()) {
      addAttribute("fksImported", FALSE, atts);
    }
    if (lazy && !table.areIndexesImported()) {
      addAttribute("indexesImported", FALSE, atts);
    }
    if (lazy && !table.areChecksImported()) {
      addAttribute("checksImported", FALSE, atts);
    }
    writer.startElement("table", atts);
    exportColumns(table, writer);
    exportPK(table, writer);
    exportFKs(table, writer);
    exportUKsAndIndexes(table, writer);
    exportChecks(table, writer);
    writer.endElement("table");
  }

  private void exportColumns(DBTable table, SimpleXMLWriter writer) throws SAXException {
    if (!lazy || table.areColumnsImported()) {
      List<DBColumn> columnsToExport = table.getColumns();
      for (DBColumn column : columnsToExport) {
        exportColumn(column, writer);
      }
    }
  }

  private static void exportColumn(DBColumn column, SimpleXMLWriter writer) throws SAXException {
    AttributesImpl atts = createAttributes(NAME, column.getName());
    addAttribute("default", column.getDefaultValue(), atts);
    addAttribute("jdbcType", String.valueOf(column.getType().getJdbcType()), atts);
    addAttribute("type", SQLUtil.renderColumnTypeWithSize(column), atts);
    addAttribute("nullable", (column.isNullable() ? null : FALSE), atts);
    writer.startElement(COLUMN, atts);
    writer.endElement(COLUMN);
  }

  private void exportPK(DBTable table, SimpleXMLWriter writer)
      throws SAXException {
    if (!lazy) {
      table.havePKImported();
    }
    if (table.isPKImported()) {
      DBPrimaryKeyConstraint pk = table.getPrimaryKeyConstraint();
      if (pk != null) {
        exportPK(pk, writer);
      }
    }
  }

  private void exportPK(DBPrimaryKeyConstraint pk, SimpleXMLWriter writer) throws SAXException {
    AttributesImpl pkAtts = createAttributes(NAME, pk.getName());
    String[] pkColumnNames = pk.getColumnNames();
    if (pkColumnNames.length == 1) {
      addAttribute(COLUMN, pkColumnNames[0], pkAtts);
    }
    writer.startElement("pk", pkAtts);
    if (pkColumnNames.length > 1) {
      writeColumnGroup(pkColumnNames, writer);
    }
    writer.endElement("pk");
  }

  public void exportFKs(DBTable table, SimpleXMLWriter writer) throws SAXException {
    if (!lazy || table.areFKsImported()) {
      for (DBForeignKeyConstraint fk : table.getForeignKeyConstraints()) {
        exportFk(fk, writer);
      }
    }
  }

  private static void exportFk(DBForeignKeyConstraint fk, SimpleXMLWriter writer) throws SAXException {
    AttributesImpl atts = createAttributes(NAME, fk.getName());
    String[] columnNames = fk.getColumnNames();
    if (columnNames.length == 1) {
      addAttribute(COLUMN, columnNames[0], atts);
    }
    addAttribute("refereeTable", fk.getRefereeTable().getName(), atts);
    String[] refereeColumns = fk.getRefereeColumnNames();
    if (refereeColumns.length == 1) {
      addAttribute("refereeColumn", refereeColumns[0], atts);
    }
    if (fk.getUpdateRule() != FKChangeRule.NO_ACTION) {
      addAttribute("updateRule", fk.getUpdateRule().name(), atts);
    }
    if (fk.getDeleteRule() != FKChangeRule.NO_ACTION) {
      addAttribute("deleteRule", fk.getDeleteRule().name(), atts);
    }
    writer.startElement("fk", atts);
    if (columnNames.length > 1) {
      writer.startElement(COLUMNS);
      for (String columnName : columnNames) {
        AttributesImpl colAtts = createAttributes(NAME, columnName);
        addAttribute("refereeColumn", fk.columnReferencedBy(columnName), colAtts);
        writer.startElement(COLUMN, colAtts);
        writer.endElement(COLUMN);
      }
      writer.endElement(COLUMNS);
    }
    writer.endElement("fk");
  }

  public void exportUKsAndIndexes(DBTable table, SimpleXMLWriter writer) throws SAXException {
    if (!lazy || table.areIndexesImported()) {
      exportUKs(table.getUniqueConstraints(false), writer);
      exportIndexes(table.getIndexes(), writer);
    }
  }

  private void exportUKs(Set<DBUniqueConstraint> uks, SimpleXMLWriter writer) throws SAXException {
    for (DBUniqueConstraint uk : uks) {
      if (uk instanceof DBPrimaryKeyConstraint) {
        continue;
      }
      AttributesImpl atts = createAttributes(NAME, uk.getName());
      String[] columnNames = uk.getColumnNames();
      if (columnNames.length == 1) {
        addAttribute(COLUMN, columnNames[0], atts);
      }
      writer.startElement("uk", atts);
      if (columnNames.length > 1) {
        writeColumnGroup(columnNames, writer);
      }
      writer.endElement("uk");
    }
  }

  public void exportChecks(DBTable table, SimpleXMLWriter writer) throws SAXException {
    if (!lazy || table.areChecksImported()) {
      exportChecks(table.getCheckConstraints(), writer);
    }
  }

  private static void exportChecks(List<DBCheckConstraint> checks, SimpleXMLWriter writer)
      throws SAXException {
    for (DBCheckConstraint check : checks) {
      AttributesImpl atts = createAttributes(NAME, check.getName());
      addAttribute("definition", check.getConditionText(), atts);
      writer.startElement("check", atts);
      writer.endElement("check");
    }
  }

  private void exportIndexes(List<DBIndex> indexes, SimpleXMLWriter writer) throws SAXException {
    for (DBIndex index : indexes) {
      AttributesImpl atts = createAttributes(NAME, index.getName());
      addAttribute("unique", (index.isUnique() ? "true" : null), atts);
      addAttribute("nameDeterministic", (index.isNameDeterministic() ? null : FALSE), atts);
      String[] columnNames = index.getColumnNames();
      if (columnNames.length == 1) {
        addAttribute(COLUMN, columnNames[0], atts);
      }
      writer.startElement("index", atts);
      if (columnNames.length > 1) {
        writeColumnGroup(columnNames, writer);
      }
      writer.endElement("index");
    }
  }

  public void writeColumnGroup(String[] pkColumnNames, SimpleXMLWriter writer) throws SAXException {
    writer.startElement(COLUMNS);
    for (String pkColumnName : pkColumnNames) {
      AttributesImpl colAtts = createAttributes(NAME, pkColumnName);
      writer.startElement(COLUMN, colAtts);
      writer.endElement(COLUMN);
    }
    writer.endElement(COLUMNS);
  }

  private static void exportSequence(DBSequence sequence, SimpleXMLWriter writer) throws SAXException {
    AttributesImpl atts = createAttributes(NAME, sequence.getName());
    addIfNotNull("start", sequence.getStartIfNotDefault(), atts);
    addIfNotNull("increment", sequence.getIncrementIfNotDefault(), atts);
    addIfNotNull("maxValue", sequence.getMaxValueIfNotDefault(), atts);
    addIfNotNull("minValue", sequence.getMinValueIfNotDefault(), atts);
    addIfNotNull("cycle", sequence.isCycle(), atts);
    addIfNotNull("cache", sequence.getCache(), atts);
    addIfNotNull("order", sequence.isOrder(), atts);
    writer.startElement("sequence", atts);
    writer.endElement("sequence");
  }

  private static void exportTrigger(DBTrigger trigger, SimpleXMLWriter writer) throws SAXException {
    AttributesImpl atts = createAttributes(NAME, trigger.getName());
    addIfNotNull("triggerType", trigger.getTriggerType(), atts);
    addIfNotNull("triggeringEvent", trigger.getTriggeringEvent(), atts);
    addIfNotNull("tableOwner", trigger.getTableOwner(), atts);
    addIfNotNull("baseObjectType", trigger.getBaseObjectType(), atts);
    addIfNotNull("tableName", trigger.getTableName(), atts);
    addIfNotNull("columnName", trigger.getColumnName(), atts);
    addIfNotNull("referencingNames", trigger.getReferencingNames(), atts);
    addIfNotNull("whenClause", trigger.getWhenClause(), atts);
    addIfNotNull("status", trigger.getStatus(), atts);
    addIfNotNull("description", trigger.getDescription(), atts);
    addIfNotNull("actionType", trigger.getActionType(), atts);
    addIfNotNull("triggerBody", trigger.getTriggerBody(), atts);
    writer.startElement("trigger", atts);
    writer.endElement("trigger");
  }

  private static void exportPackage(DBPackage pkg, SimpleXMLWriter writer) throws SAXException {
    AttributesImpl atts = createAttributes(NAME, pkg.getName());
    addIfNotNull("subObjectName", pkg.getSubObjectName(), atts);
    addIfNotNull("objectId", pkg.getObjectId(), atts);
    addIfNotNull("dataObjectId", pkg.getDataObjectId(), atts);
    addIfNotNull("objectType", pkg.getObjectType(), atts);
    addIfNotNull("status", pkg.getStatus(), atts);
    writer.startElement("package", atts);
    exportPackageProcedures(pkg, writer);
    writer.endElement("package");
  }

  private static void exportPackageProcedures(DBPackage pkg, SimpleXMLWriter writer) throws SAXException {
    for (DBProcedure procedure : pkg.getProcedures()) {
      AttributesImpl atts = createAttributes(NAME, procedure.getName());
      addIfNotNull("objectId", procedure.getObjectId(), atts);
      addIfNotNull("subProgramId", procedure.getSubProgramId(), atts);
      addIfNotNull("overload", procedure.getOverload(), atts);
      writer.startElement("procedure", atts);
      writer.endElement("procedure");
    }
  }

  private static void addIfNotNull(String name, Object value, AttributesImpl atts) {
    if (value != null) {
      addAttribute(name, value.toString(), atts);
    }
  }

}
