/*
 * (c) Copyright 2010-2012 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl;

import com.rapiddweller.common.ArrayFormat;
import com.rapiddweller.common.CollectionUtil;
import com.rapiddweller.common.StringUtil;
import com.rapiddweller.common.SystemInfo;
import com.rapiddweller.format.script.ScriptUtil;
import com.rapiddweller.jdbacl.model.DBCheckConstraint;
import com.rapiddweller.jdbacl.model.DBColumn;
import com.rapiddweller.jdbacl.model.DBConstraint;
import com.rapiddweller.jdbacl.model.DBDataType;
import com.rapiddweller.jdbacl.model.DBForeignKeyConstraint;
import com.rapiddweller.jdbacl.model.DBNotNullConstraint;
import com.rapiddweller.jdbacl.model.DBObject;
import com.rapiddweller.jdbacl.model.DBPrimaryKeyConstraint;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.model.DBUniqueConstraint;
import com.rapiddweller.jdbacl.model.ForeignKeyPath;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Provides utility methods for creating SQL queries and commands.<br/><br/>
 * Created: 01.09.2010 09:38:46
 *
 * @author Volker Bergmann
 * @since 0.6.4
 */
public class SQLUtil {

  private static final Set<String> NO_SIZE_TYPES = CollectionUtil.toSet(
      "DATE", "BLOB", "CLOB", "NCLOB");

  private static final Set<String> DDL_STATEMENTS = CollectionUtil.toSet(
      "create table", "alter table", "drop table",
      "create unique index", "drop index", "alter index",
      "rename",
      "create materialized view", "alter materialized view", "drop materialized view"
  );

  private static final Set<String> DML_STATEMENTS = CollectionUtil.toSet(
      "insert", "update", "delete", "truncate", "select into");

  private static final Set<String> PROCEDURE_CALLS = CollectionUtil.toSet(
      "execute", "exec", "call");

  /**
   * Parse column type and size object [ ].
   *
   * @param spec the spec
   * @return the object [ ]
   */
  public static Object[] parseColumnTypeAndSize(String spec) {
    int lparen = spec.indexOf('(');
    if (lparen < 0) {
      return new Object[] {spec};
    }
    String type = spec.substring(0, lparen);
    int rparen = spec.indexOf(')', lparen);
    if (rparen < 0) {
      throw new RuntimeException("Illegal column type format: " + spec);
    }
    String[] sizeAndFractionDigits = spec.substring(lparen + 1, rparen).split(",");
    if (sizeAndFractionDigits.length == 1) {
      return new Object[] {type, Integer.parseInt(sizeAndFractionDigits[0].trim())};
    } else {
      return new Object[] {type, Integer.parseInt(sizeAndFractionDigits[0].trim()),
          Integer.parseInt(sizeAndFractionDigits[1].trim())};
    }
  }

  /**
   * Render create table.
   *
   * @param table              the table
   * @param includeForeignKeys the include foreign keys
   * @param nameSpec           the name spec
   * @param out                the out
   */
  public static void renderCreateTable(DBTable table,
                                       boolean includeForeignKeys, NameSpec nameSpec, PrintWriter out) {
    // create table <name> (
    out.print("create table ");
    out.print(table.getName());
    out.print(" (");
    // columns
    List<DBColumn> columns = table.getColumns();
    for (int i = 0; i < columns.size(); i++) {
      if (i > 0) {
        out.print(',');
      }
      out.println();
      out.print('\t');
      out.print(renderColumn(columns.get(i)));
    }
    // primary key
    DBPrimaryKeyConstraint pk = table.getPrimaryKeyConstraint();
    if (pk != null) {
      out.println(",");
      out.print('\t');
      out.print(pkSpec(pk, nameSpec));
    }
    // unique keys
    Set<DBUniqueConstraint> uks = table.getUniqueConstraints(false);
    for (DBUniqueConstraint uk : uks) {
      out.println(",");
      out.print('\t');
      out.print(ukSpec(uk, nameSpec));
    }
    // unique keys
    if (includeForeignKeys) {
      Set<DBForeignKeyConstraint> fks = table.getForeignKeyConstraints();
      for (DBForeignKeyConstraint fk : fks) {
        out.println(",");
        out.print('\t');
        out.print(fkSpec(fk, nameSpec));
      }
    }
    // checks
    List<DBCheckConstraint> checks = table.getCheckConstraints();
    for (DBCheckConstraint check : checks) {
      out.println(",");
      out.print('\t');
      out.print(checkSpec(check, nameSpec));
    }
    out.println();
    out.print(")");
  }

  /**
   * Render add foreign key.
   *
   * @param fk       the fk
   * @param nameSpec the name spec
   * @param printer  the printer
   */
  public static void renderAddForeignKey(DBForeignKeyConstraint fk, NameSpec nameSpec, PrintWriter printer) {
    printer.println("ALTER TABLE " + fk.getTable().getName() + " ADD ");
    printer.print('\t');
    printer.print(SQLUtil.fkSpec(fk, nameSpec));
  }

  /**
   * Prepend alias string [ ].
   *
   * @param tableAlias  the table alias
   * @param columnNames the column names
   * @return the string [ ]
   */
  public static String[] prependAlias(String tableAlias, String[] columnNames) {
    if (tableAlias != null) {
      for (int i = 0; i < columnNames.length; i++) {
        columnNames[i] = tableAlias + '.' + columnNames[i];
      }
    }
    return columnNames;
  }

  /**
   * Render column names string.
   *
   * @param columns the columns
   * @return the string
   */
  public static String renderColumnNames(DBColumn[] columns) {
    StringBuilder builder = new StringBuilder(columns[0].getName());
    for (int i = 1; i < columns.length; i++) {
      builder.append(", ").append(columns[i].getName());
    }
    return builder.toString();
  }

  /**
   * Render column names string.
   *
   * @param columns the columns
   * @return the string
   */
  public static String renderColumnNames(List<DBColumn> columns) {
    StringBuilder builder = new StringBuilder(columns.get(0).getName());
    for (int i = 1; i < columns.size(); i++) {
      builder.append(", ").append(columns.get(i).getName());
    }
    return builder.toString();
  }

  /**
   * Render column names string.
   *
   * @param columnNames the column names
   * @return the string
   */
  public static String renderColumnNames(String[] columnNames) {
    return '(' + ArrayFormat.format(columnNames) + ')';
  }

  /**
   * Render column string.
   *
   * @param column the column
   * @return the string
   */
  public static String renderColumn(DBColumn column) {
    StringBuilder builder = new StringBuilder();

    // column name
    builder.append(column.getName());

    // column type & size
    builder.append(' ');
    renderColumnTypeWithSize(column, builder);

    // default
    if (column.getDefaultValue() != null) {
      builder.append(" DEFAULT ").append(column.getDefaultValue());
    }

    // nullability
    if (!column.isNullable()) {
      builder.append(" NOT");
    }
    builder.append(" NULL");

    return builder.toString();
  }

  /**
   * Render column type with size string.
   *
   * @param column the column
   * @return the string
   */
  public static String renderColumnTypeWithSize(DBColumn column) {
    StringBuilder builder = new StringBuilder();
    renderColumnTypeWithSize(column, builder);
    return builder.toString();
  }

  /**
   * Render column type with size.
   *
   * @param column  the column
   * @param builder the builder
   */
  public static void renderColumnTypeWithSize(DBColumn column, StringBuilder builder) {
    DBDataType columnType = column.getType();
    String typeName = (columnType != null ? columnType.getName() : null);
    builder.append(typeName);
    if (column.getSize() != null && !NO_SIZE_TYPES.contains(typeName)) {
      builder.append("(").append(column.getSize());
      if (column.getFractionDigits() != null) {
        builder.append(",").append(column.getFractionDigits());
      }
      builder.append(")");
    }
  }

  /**
   * Substitute markers string.
   *
   * @param sql          the sql
   * @param marker       the marker
   * @param substitution the substitution
   * @param dialect      the dialect
   * @return the string
   */
  public static String substituteMarkers(String sql, String marker, Object substitution, DatabaseDialect dialect) {
    return sql.replace(marker, dialect.formatValue(substitution));
  }

  /**
   * Render simple select all query string.
   *
   * @param table   the table
   * @param dialect the dialect
   * @return the string
   */
  public static String renderSimpleSelectAllQuery(DBTable table, DatabaseDialect dialect) {
    StringBuilder builder = new StringBuilder("SELECT * FROM ");
    appendCatSchTabToBuilder(table.getCatalog().getName(), table.getSchema().getName(), table.getName(), builder, dialect);
    return builder.toString();
  }


  /**
   * Render query string.
   *
   * @param catalog     the catalog
   * @param schema      the schema
   * @param table       the table
   * @param columnNames the column names
   * @param dialect     the dialect
   * @return the string
   */
  public static String renderQuery(String catalog, String schema, String table, String[] columnNames, DatabaseDialect dialect) {
    StringBuilder builder = new StringBuilder("SELECT ");
    for (int i = 0; i < columnNames.length; i++) {
      if (i > 0) {
        builder.append(" ,");
      }
      builder.append(quoteIfNecessary(columnNames[i], dialect.quoteTableNames));
    }
    builder.append(" FROM ");
    appendCatSchTabToBuilder(catalog, schema, table, builder, dialect);
    return builder.toString();
  }

  /**
   * Render query string.
   *
   * @param catalog    the catalog
   * @param schema     the schema
   * @param table      the table
   * @param columnName the column name
   * @param selector   the selector
   * @param dialect    the dialect
   * @return the string
   */
  public static String renderQuery(String catalog, String schema, String table, String columnName, String selector, DatabaseDialect dialect) {
    StringBuilder builder = new StringBuilder("SELECT ");
    builder.append(Objects.requireNonNullElse(columnName, " * "));
    builder.append(" FROM ");
    appendCatSchTabToBuilder(catalog, schema, table, builder, dialect);
    if (selector != null) {
      return ScriptUtil.combineScriptableParts(builder.toString(), " WHERE ", selector);
    } else {
      return builder.toString();
    }
  }

  /**
   * Render where clause string.
   *
   * @param columnNames the column names
   * @param values      the values
   * @param dialect     the dialect
   * @return the string
   */
  public static String renderWhereClause(String[] columnNames, Object[] values, DatabaseDialect dialect) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < columnNames.length; i++) {
      if (i > 0) {
        builder.append(" AND ");
      }
      builder.append(columnNames[i]).append(" = ").append(dialect.formatValue(values[i]));
    }
    return builder.toString();
  }

  /**
   * Mutates data or structure boolean.
   *
   * @param sql the sql
   * @return the boolean
   */
  public static Boolean mutatesDataOrStructure(String sql) {
    sql = normalizeSQL(sql);
    // ALTER SESSION does not change data or structure
    if (sql.trim().startsWith("alter session")) {
      return false;
    }

    // check if structure is changed...
    if (Boolean.TRUE.equals(mutatesStructure(sql))) {
      return true;
    } else if (isQuery(sql)) {
      return false;
    } else if (isDML(sql)) {
      return true;
    }
    return null;
  }

  /**
   * Mutates structure boolean.
   *
   * @param sql the sql
   * @return the boolean
   */
  public static Boolean mutatesStructure(String sql) {
    if (isDDL(sql)) {
      return true;
    }
    isProcedureCall(sql);
    return false;
  }

  /**
   * Is ddl boolean.
   *
   * @param sql the sql
   * @return the boolean
   */
  public static boolean isDDL(String sql) {
    sql = normalizeSQL(sql);
    for (String ddl : DDL_STATEMENTS) {
      if (sql.startsWith(ddl)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Is dml boolean.
   *
   * @param sql the sql
   * @return the boolean
   */
  public static boolean isDML(String sql) {
    sql = normalizeSQL(sql);
    for (String ddl : DML_STATEMENTS) {
      if (sql.startsWith(ddl)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Is procedure call boolean.
   *
   * @param sql the sql
   * @return the boolean
   */
  public static boolean isProcedureCall(String sql) {
    sql = normalizeSQL(sql);
    for (String call : PROCEDURE_CALLS) {
      if (sql.startsWith(call)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Is query boolean.
   *
   * @param sql the sql
   * @return the boolean
   */
  public static boolean isQuery(String sql) {
    sql = normalizeSQL(sql);
    // anything else than SELECT or WITH must be a mutation...
    if (!sql.startsWith("select") && !sql.startsWith("with")) {
      return false;
    }
    // ... but a 'select' statement might be a 'select into'
    StringTokenizer t = new StringTokenizer(sql);
    while (t.hasMoreTokens()) {
      if ("into".equals(t.nextToken())) {
        return false;
      }
    }
    // it is a plain select statement
    return true;
  }

  private static String normalizeSQL(String sql) {
    sql = StringUtil.normalizeSpace(sql.trim().toLowerCase());
    while (sql.contains("/*")) {
      sql = StringUtil.removeSection(sql, "/*", "*/").trim();
    }
    return sql;
  }

  /**
   * Constraint spec string.
   *
   * @param constraint the constraint
   * @param nameSpec   the name spec
   * @return the string
   */
  public static String constraintSpec(DBConstraint constraint, NameSpec nameSpec) {
    if (constraint instanceof DBPrimaryKeyConstraint) {
      return pkSpec((DBPrimaryKeyConstraint) constraint, nameSpec);
    } else if (constraint instanceof DBUniqueConstraint) {
      return ukSpec((DBUniqueConstraint) constraint, nameSpec);
    } else if (constraint instanceof DBForeignKeyConstraint) {
      return fkSpec((DBForeignKeyConstraint) constraint, nameSpec);
    } else if (constraint instanceof DBNotNullConstraint) {
      return notNullSpec((DBNotNullConstraint) constraint);
    } else if (constraint instanceof DBCheckConstraint) {
      return checkSpec((DBCheckConstraint) constraint, nameSpec);
    } else {
      throw new UnsupportedOperationException("Unknown constraint type: " +
          constraint.getClass());
    }
  }

  private static String checkSpec(DBCheckConstraint check, NameSpec nameSpec) {
    StringBuilder builder = createConstraintSpecBuilder(check, nameSpec);
    builder.append("CHECK ").append(check.getConditionText());
    return builder.toString();
  }

  private static String notNullSpec(DBNotNullConstraint constraint) {
    return constraint.getColumnNames()[0] + " NOT NULL";
  }

  /**
   * Pk spec string.
   *
   * @param pk       the pk
   * @param nameSpec the name spec
   * @return the string
   */
  public static String pkSpec(DBPrimaryKeyConstraint pk, NameSpec nameSpec) {
    StringBuilder builder = createConstraintSpecBuilder(pk, nameSpec);
    builder.append("PRIMARY KEY ").append(renderColumnNames(pk.getColumnNames()));
    return builder.toString();
  }

  /**
   * Uk spec string.
   *
   * @param uk       the uk
   * @param nameSpec the name spec
   * @return the string
   */
  public static String ukSpec(DBUniqueConstraint uk, NameSpec nameSpec) {
    StringBuilder builder = createConstraintSpecBuilder(uk, nameSpec);
    builder.append("UNIQUE ").append(renderColumnNames(uk.getColumnNames()));
    return builder.toString();
  }

  /**
   * Fk spec string.
   *
   * @param fk       the fk
   * @param nameSpec the name spec
   * @return the string
   */
  public static String fkSpec(DBForeignKeyConstraint fk, NameSpec nameSpec) {
    StringBuilder builder = createConstraintSpecBuilder(fk, nameSpec);
    builder.append("FOREIGN KEY ").append(renderColumnNames(fk.getColumnNames()));
    builder.append(" REFERENCES ")
        .append(fk.getRefereeTable().getSchema().getName())
        .append('.')
        .append(fk.getRefereeTable()).append(renderColumnNames(fk.getRefereeColumnNames()));
    return builder.toString();
  }

  /**
   * Create constraint spec builder string builder.
   *
   * @param constraint the constraint
   * @param nameSpec   the name spec
   * @return the string builder
   */
  protected static StringBuilder createConstraintSpecBuilder(DBConstraint constraint, NameSpec nameSpec) {
    StringBuilder builder = new StringBuilder();
    return appendConstraintName(constraint, builder, nameSpec);
  }

  /**
   * Insert string.
   *
   * @param catalog the catalog
   * @param schema  the schema
   * @param table   the table
   * @param dialect the dialect
   * @param values  the values
   * @return the string
   */
  public static String insert(String catalog, String schema, String table, DatabaseDialect dialect, Object... values) {
    StringBuilder builder = new StringBuilder("insert into ");
    appendCatSchTabToBuilder(catalog, schema, table, builder, dialect);
    builder.append(" values (");
    for (int i = 0; i < values.length; i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(dialect.formatValue(values[i]));
    }
    return builder.append(")").toString();
  }

  /**
   * Join fk path string.
   *
   * @param route                 the route
   * @param join_Type             the join type
   * @param startAlias            the start alias
   * @param endAlias              the end alias
   * @param intermediateAliasBase the intermediate alias base
   * @return the string
   */
  public static String joinFKPath(ForeignKeyPath route, String join_Type,
                                  String startAlias, String endAlias, String intermediateAliasBase) {
    return joinFKPath(route, join_Type, startAlias, endAlias, intermediateAliasBase, null);
  }

  /**
   * Join fk path string.
   *
   * @param route                 the route
   * @param join_Type             the join type
   * @param startAlias            the start alias
   * @param endAlias              the end alias
   * @param intermediateAliasBase the intermediate alias base
   * @param indent                the indent
   * @return the string
   */
  public static String joinFKPath(ForeignKeyPath route, String join_Type,
                                  String startAlias, String endAlias, String intermediateAliasBase, String indent) {
    StringBuilder builder = new StringBuilder();
    List<DBForeignKeyConstraint> edges = route.getEdges();
    // render intermediate joins
    String currentReferrer = startAlias;
    for (int i = 0; i < edges.size() - 1; i++) {
      String refereeAlias = intermediateAliasBase + "_" + (i + 1) + "__";
      DBForeignKeyConstraint fk = edges.get(i);
      builder.append(joinFK(fk, join_Type, currentReferrer, refereeAlias));
      currentReferrer = refereeAlias;
      builder.append(' ');
      if (indent != null) {
        builder.append(SystemInfo.getLineSeparator()).append('\t');
      }
    }
    // render final join with endAlias
    DBForeignKeyConstraint fk = CollectionUtil.lastElement(edges);
    builder.append(joinFK(fk, join_Type, currentReferrer, endAlias));
    // done, return result as string
    return builder.toString();
  }

  /**
   * Join fk string.
   *
   * @param fk           the fk
   * @param joinType     the join type
   * @param refererAlias the referer alias
   * @param refereeAlias the referee alias
   * @return the string
   */
  public static String joinFK(DBForeignKeyConstraint fk, String joinType, String refererAlias, String refereeAlias) {
    return join(joinType, refererAlias, fk.getColumnNames(),
        fk.getRefereeTable().getName(), refereeAlias, fk.getRefereeColumnNames());
  }

  /**
   * Left join string.
   *
   * @param leftAlias    the left alias
   * @param leftColumns  the left columns
   * @param rightTable   the right table
   * @param rightAlias   the right alias
   * @param rightColumns the right columns
   * @return the string
   */
  public static String leftJoin(String leftAlias, String[] leftColumns,
                                String rightTable, String rightAlias, String[] rightColumns) {
    return join("LEFT", leftAlias, leftColumns, rightTable, rightAlias, rightColumns);
  }

  /**
   * Inner join string.
   *
   * @param leftAlias    the left alias
   * @param leftColumns  the left columns
   * @param rightTable   the right table
   * @param rightAlias   the right alias
   * @param rightColumns the right columns
   * @return the string
   */
  public static String innerJoin(String leftAlias, String[] leftColumns,
                                 String rightTable, String rightAlias, String[] rightColumns) {
    return join("INNER", leftAlias, leftColumns, rightTable, rightAlias, rightColumns);
  }

  /**
   * Join string.
   *
   * @param type         the type
   * @param leftAlias    the left alias
   * @param leftColumns  the left columns
   * @param rightTable   the right table
   * @param rightAlias   the right alias
   * @param rightColumns the right columns
   * @return the string
   */
  public static String join(String type, String leftAlias, String[] leftColumns,
                            String rightTable, String rightAlias, String[] rightColumns) {
    if (leftColumns.length != rightColumns.length) {
      throw new IllegalArgumentException("The join partners' column count does not match: " +
          leftColumns.length + " vs. " + rightColumns.length);
    }
    StringBuilder builder = new StringBuilder();
    if (!StringUtil.isEmpty(type) && !"INNER".equalsIgnoreCase(type)) {
      builder.append(type).append(' ');
    }
    builder.append("JOIN ");
    builder.append(rightTable).append(" ").append(rightAlias).append(" ON ");
    for (int i = 0; i < leftColumns.length; i++) {
      if (i > 0) {
        builder.append(" AND ");
      }
      builder.append(leftAlias).append('.').append(leftColumns[i]);
      builder.append(" = ").append(rightAlias).append('.').append(rightColumns[i]);
    }
    return builder.toString();
  }

  /**
   * Add required condition string builder.
   *
   * @param condition the condition
   * @param builder   the builder
   * @return the string builder
   */
  public static StringBuilder addRequiredCondition(String condition, StringBuilder builder) {
    if (builder.length() > 0) {
      builder.append(" AND ");
    }
    return builder.append(condition);
  }

  /**
   * Add optional condition string builder.
   *
   * @param condition the condition
   * @param builder   the builder
   * @return the string builder
   */
  public static StringBuilder addOptionalCondition(String condition, StringBuilder builder) {
    if (builder.length() > 0) {
      builder.append(" OR ");
    }
    return builder.append(condition);
  }

  /**
   * Owner dot component string.
   *
   * @param object the object
   * @return the string
   */
  public static String ownerDotComponent(DBObject object) {
    return (object.getOwner() != null ? object.getOwner() + "." : "") + object.getName();
  }

  /**
   * Append constraint name string builder.
   *
   * @param constraint the constraint
   * @param builder    the builder
   * @param nameSpec   the name spec
   * @return the string builder
   */
  public static StringBuilder appendConstraintName(DBConstraint constraint, StringBuilder builder, NameSpec nameSpec) {
    if (constraint.getName() != null &&
        (nameSpec == NameSpec.ALWAYS || (nameSpec == NameSpec.IF_REPRODUCIBLE && constraint.isNameDeterministic()))) {
      builder.append("CONSTRAINT ").append(quoteNameIfNullOrSpaces(constraint.getName())).append(' ');
    }
    return builder;
  }

  /**
   * Append constraint name.
   *
   * @param constraint the constraint
   * @param builder    the builder
   */
  public static void appendConstraintName(DBConstraint constraint, StringBuilder builder) {
    if (constraint.getName() != null) {
      builder.append("CONSTRAINT ").append(quoteNameIfNullOrSpaces(constraint.getName())).append(' ');
    }
  }

  /**
   * Constraint name string.
   *
   * @param constraint the constraint
   * @return the string
   */
  public static String constraintName(DBConstraint constraint) {
    return (constraint.getName() != null ?
        "CONSTRAINT " + quoteNameIfNullOrSpaces(constraint.getName()) + ' ' :
        "");
  }

  /**
   * Type and name string.
   *
   * @param dbObject the db object
   * @return the string
   */
  public static String typeAndName(DBObject dbObject) {
    if (dbObject == null) {
      return null;
    }
    String name = dbObject.getName();
    if (name == null && dbObject instanceof DBConstraint) {
      name = "CONSTRAINT";
    }
    return dbObject.getObjectType() + ' ' + name;
  }

  /**
   * Remove comments string.
   *
   * @param sql the sql
   * @return the string
   */
  public static String removeComments(String sql) {
    String result = sql;
    String tmp;
    do {
      tmp = result;
      result = StringUtil.removeSection(tmp, "/*", "*/");
    } while (!result.equals(tmp));
    return result;
  }

  /**
   * Normalize string.
   *
   * @param sql            the sql
   * @param removeComments the remove comments
   * @return the string
   */
  @SuppressWarnings("checkstyle:Indentation")
  public static String normalize(String sql, boolean removeComments) {
    if (removeComments) {
      sql = sql.replace("--", "//");
    }
    StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(sql));
    tokenizer.resetSyntax();
    tokenizer.wordChars('A', 'Z');
    tokenizer.wordChars('a', 'z');
    tokenizer.wordChars('0', '9');
    tokenizer.wordChars('_', '_');
    tokenizer.whitespaceChars(' ', ' ');
    tokenizer.whitespaceChars('\n', '\n');
    tokenizer.whitespaceChars('\r', '\r');
    tokenizer.whitespaceChars('\t', '\t');
    tokenizer.quoteChar('\'');
    tokenizer.quoteChar('"');
    if (removeComments) {
      tokenizer.slashStarComments(true);
      tokenizer.slashSlashComments(true);
    }
    StringBuilder builder = new StringBuilder();
    int lastTtype = StreamTokenizer.TT_EOF;
    try {
      while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
        int ttype = tokenizer.ttype;
        if (builder.length() > 0 // insert space if this is not the beginning of the string
            && ttype != ')' && ttype != ',' && lastTtype != '(' // no space for brackets and lists
            && lastTtype != '.' && ttype != '.' // no space around '.'
            && !(lastTtype == '/' && ttype == '*') // preserve /* if it has not been filtered out
            && !(lastTtype == '-' && ttype == '-') // preserve -- if it has not been filtered out
            && !(lastTtype == '*' && ttype == '/')) // preserve */ if it has not been filtered out
        {
          builder.append(' ');
        }
        switch (ttype) {
          case StreamTokenizer.TT_WORD:
            builder.append(tokenizer.sval);
            break;
          case StreamTokenizer.TT_NUMBER:
            builder.append(renderNumber(tokenizer));
            break;
          case '"':
            builder.append('"').append(tokenizer.sval).append('"');
            break;
          case '\'':
            builder.append('\'').append(tokenizer.sval).append('\'');
            break;
          default:
            builder.append((char) ttype);
        }
        lastTtype = ttype;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return builder.toString();
  }

  /**
   * Render number string.
   *
   * @param tokenizer the tokenizer
   * @return the string
   */
  protected static String renderNumber(StreamTokenizer tokenizer) {
    double value = tokenizer.nval;
    if (Math.floor(value) == value) {
      return renderLong((long) value);
    } else {
      return renderDouble(value);
    }
  }

  /**
   * Render column list with table name string.
   *
   * @param table   the table
   * @param columns the columns
   * @return the string
   */
  public static String renderColumnListWithTableName(String table, String... columns) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < columns.length; i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(table).append('.').append(columns[i]);
    }
    return builder.toString();
  }

  /**
   * Equals string.
   *
   * @param tableAlias1 the table alias 1
   * @param colNames1   the col names 1
   * @param tableAlias2 the table alias 2
   * @param colNames2   the col names 2
   * @return the string
   */
  public static String equals(String tableAlias1, String[] colNames1, String tableAlias2, String[] colNames2) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < colNames1.length; i++) {
      if (i > 0) {
        builder.append(" AND ");
      }
      if (tableAlias1 != null) {
        builder.append(tableAlias1).append('.');
      }
      builder.append(colNames1[i]).append(" = ");
      if (tableAlias2 != null) {
        builder.append(tableAlias2).append('.');
      }
      builder.append(colNames2[i]);
    }
    return builder.toString();
  }

  /**
   * All null string.
   *
   * @param columns    the columns
   * @param tableAlias the table alias
   * @return the string
   */
  public static String allNull(String[] columns, String tableAlias) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < columns.length; i++) {
      if (i > 0) {
        builder.append(" AND ");
      }
      if (tableAlias != null) {
        builder.append(tableAlias).append('.');
      }
      builder.append(columns[i]).append(" IS NULL");
    }
    return builder.toString();
  }

  // private helpers -------------------------------------------------------------------------------------------------

  private static String renderLong(long value) {
    if (value > 0) {
      return String.valueOf(value);
    } else {
      return "- " + Math.abs(value);
    }
  }

  private static String renderDouble(double value) {
    if (value > 0) {
      return String.valueOf(value);
    } else {
      return "- " + Math.abs(value);
    }
  }

  private static String quoteNameIfNullOrSpaces(String name) {
    return (name != null && name.indexOf(' ') >= 0 ? '"' + name + '"' : name);
  }


  private static String quoteIfNecessary(String name, Boolean quoteTableNames) {
    return (quoteTableNames ? '"' + name + '"' : name);
  }

  /**
   * Append cat sch tab to builder.
   *
   * @param catalog the catalog
   * @param schema  the schema
   * @param table   the table
   * @param builder the builder
   * @param dialect the dialect
   */
  public static void appendCatSchTabToBuilder(String catalog, String schema, String table, StringBuilder builder, DatabaseDialect dialect) {
    checkCatSchTab(catalog, schema, table, dialect, builder);
  }

  /**
   * Create cat sch tab string string.
   *
   * @param catalog the catalog
   * @param schema  the schema
   * @param table   the table
   * @param dialect the dialect
   * @return the string
   */
  public static String createCatSchTabString(String catalog, String schema, String table, DatabaseDialect dialect) {
    StringBuilder builder = new StringBuilder();
    checkCatSchTab(catalog, schema, table, dialect, builder);
    return builder.toString();
  }


  // helper
  private static void checkCatSchTab(String catalog, String schema, String table, DatabaseDialect dialect, StringBuilder builder) {
    if (catalog != null && !catalog.equals("") && !dialect.getDbType().equalsIgnoreCase("oracle")) {
      builder.append(quoteIfNecessary(catalog, dialect.quoteTableNames)).append('.');
    }
    if (schema != null && !schema.equals("")) {
      builder.append(quoteIfNecessary(schema, dialect.quoteTableNames)).append('.');
    }
    if (table != null && !table.equals("")) {
      builder.append(quoteIfNecessary(table, dialect.quoteTableNames));
    } else {
      throw new RuntimeException("Table is missing");
    }
  }

  /**
   * Create cat sch tab string string.
   *
   * @param catalog the catalog
   * @param schema  the schema
   * @param table   the table
   * @return the string
   */
  public static String createCatSchTabString(String catalog, String schema, String table) {
    StringBuilder builder = new StringBuilder();
    if (catalog != null && !catalog.equals("")) {
      builder.append(schema).append('.').append(table);
    } else if (schema != null && !schema.equals("")) {
      builder.append(schema).append('.').append(table);
    } else {
      builder.append(table);
    }
    return builder.toString();
  }

  /**
   * Format value list string.
   *
   * @param values  the values
   * @param dialect the dialect
   * @return the string
   */
  public static String formatValueList(List<String> values, DatabaseDialect dialect) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < values.size(); i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(dialect.formatValue(values.get(i)));
    }
    return builder.toString();
  }

  public static void appendColumnName(String columnName, StringBuilder builder, DatabaseDialect dialect) {
    appendQuoted(columnName, builder, dialect);
  }

  private static void appendQuoted(String name, StringBuilder builder, DatabaseDialect dialect) {
    if (dialect.quoteTableNames) {
      builder.append('"').append(name).append('"');
    } else {
      builder.append(name);
    }
  }

}
