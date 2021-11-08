/*
 * (c) Copyright 2008-2012 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.dialect;

import com.rapiddweller.common.ArrayBuilder;
import com.rapiddweller.common.Assert;
import com.rapiddweller.common.CollectionUtil;
import com.rapiddweller.common.NameUtil;
import com.rapiddweller.common.OrderedMap;
import com.rapiddweller.common.StringUtil;
import com.rapiddweller.common.converter.TimestampFormatter;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.model.DBCheckConstraint;
import com.rapiddweller.jdbacl.model.DBPackage;
import com.rapiddweller.jdbacl.model.DBProcedure;
import com.rapiddweller.jdbacl.model.DBSchema;
import com.rapiddweller.jdbacl.model.DBSequence;
import com.rapiddweller.jdbacl.model.DBTrigger;
import com.rapiddweller.jdbacl.sql.Query;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implements generic database concepts for Oracle.<br/><br/>
 * Created: 26.01.2008 07:05:28
 * @author Volker Bergmann
 * @since 0.4.0
 */
public class OracleDialect extends DatabaseDialect {

  private static final String DATE_PATTERN = "'to_date('''yyyy-MM-dd''', ''yyyy-mm-dd'')'";
  private static final String TIME_PATTERN = "'to_date('''HH:mm:ss''', ''HH24:mi:ss'')'";
  private static final String DATETIME_PATTERN = "'to_date('''yyyy-MM-dd HH:mm:ss''', ''yyyy-mm-dd HH24:mi:ss'')'";
  private static final String TIMESTAMP_MESSAGE = "to_timestamp(''{0}'', ''yyyy-mm-dd HH24:mi:ss.FF'')";
  private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSSSSSSSS";
  private static final Pattern SIMPLE_NOT_NULL_CHECK = Pattern.compile("\"[A-Z0-9_]+\" IS NOT NULL");

  final Pattern randomNamePattern = Pattern.compile("SYS_C\\d{8}");

  public OracleDialect() {
    super("oracle", true, true, DATE_PATTERN, TIME_PATTERN, DATETIME_PATTERN);
  }

  @Override
  public boolean isDefaultCatalog(String catalog, String user) {
    return (catalog == null);
  }

  @Override
  public boolean isDefaultSchema(String schema, String user) {
    return user.equalsIgnoreCase(schema);
  }

  @Override
  public String renderCreateSequence(DBSequence sequence) {
    //		  Oracle sequence syntax:
    //			CREATE SEQUENCE [myschema.]xyz
    //			START WITH 1
    //			INCREMENT BY 1
    //			MINVALUE 1 | NOMINVALUE
    //			MAXVALUE 999999999 | NOMAXVALUE
    //			CACHE 1 | NOCACHE
    //			CYCLE | NOCYCLE
    //			ORDER | NOORDER

    String result = super.renderCreateSequence(sequence);
    // apply cache settings
    Long cache = sequence.getCache();
    if (cache != null) {
      result += " CACHE " + cache;
    }
    // if applicable, append ORDER. This is purely oracle
    Boolean order = sequence.isOrder();
    if (order != null) {
      result += (order ? " ORDER" : "NOORDER");
    }
    return result;
  }

  @Override
  protected String renderSequenceNameAndType(DBSequence sequence) {
    String schemaName = sequence.getSchemaName();
    return (schemaName != null ? "\"" + schemaName + "\"." : "") + '"' + sequence.getName() + '"';
  }

  @Override
  public String renderFetchSequenceValue(String sequenceName) {
    return "select " + sequenceName + ".nextval from dual";
  }

  @Override
  public String formatTimestamp(Timestamp value) {
    String renderedTimestamp = new TimestampFormatter(TIMESTAMP_PATTERN).format(value);
    return MessageFormat.format(TIMESTAMP_MESSAGE, renderedTimestamp);
  }

  @Override
  public DBSequence[] querySequences(Connection connection) throws SQLException {
    Statement statement = connection.createStatement();
    ResultSet resultSet = statement.executeQuery("select sequence_name, min_value, max_value, increment_by, " +
        "cycle_flag, order_flag, cache_size, last_number from user_sequences");
    try {
      ArrayBuilder<DBSequence> builder = new ArrayBuilder<>(DBSequence.class);
      while (resultSet.next()) {
        DBSequence sequence = new DBSequence(resultSet.getString(1), null);
        sequence.setMinValue(new BigInteger(resultSet.getString(2)));
        sequence.setMaxValue(new BigInteger(resultSet.getString(3)));
        sequence.setIncrement(new BigInteger(resultSet.getString(4)));
        sequence.setCycle("Y".equals(resultSet.getString(5)));
        sequence.setOrder("Y".equals(resultSet.getString(6)));
        sequence.setCache(resultSet.getLong(7));
        sequence.setLastNumber(new BigInteger(resultSet.getString(8)));
        builder.add(sequence);
        logger.debug("Imported sequence {}", sequence.getName());
      }
      return builder.toArray();
    } finally {
      DBUtil.closeResultSetAndStatement(resultSet);
    }
  }

  public DBCheckConstraint[] queryCheckConstraints(Connection connection, String schemaName) throws SQLException {
    Statement statement = connection.createStatement();
    statement.setFetchSize(300);
    String query = "select owner, constraint_name, table_name, search_condition " +
        "from user_constraints where constraint_type = 'C'";
    if (schemaName != null) {
      query += " and owner = '" + schemaName.toUpperCase() + "'";
    }
    ResultSet resultSet = statement.executeQuery(query);
    ArrayBuilder<DBCheckConstraint> builder = new ArrayBuilder<>(DBCheckConstraint.class);
    try {
      while (resultSet.next()) {
        String ownerName = resultSet.getString("owner");
        if (schemaName == null || StringUtil.equalsIgnoreCase(schemaName, ownerName)) {
          String constraintName = resultSet.getString("constraint_name");
          String tableName = resultSet.getString("table_name");
          String condition = resultSet.getString("search_condition");
          if (!SIMPLE_NOT_NULL_CHECK.matcher(condition).matches()) {
            try {
              DBCheckConstraint constraint = new DBCheckConstraint(
                  constraintName, !isDeterministicCheckName(constraintName), tableName, condition);
              builder.add(constraint);
            } catch (Exception e) {
              logger.error("Error parsing check constraint ", e);
            }
          }
          logger.debug("Imported check for table {}: {}", tableName, condition);
        }
      }
    } finally {
      DBUtil.closeResultSetAndStatement(resultSet);
    }
    return builder.toArray();
  }

  public boolean isDeterministicCheckName(String checkName) {
    return !randomNamePattern.matcher(checkName).matches();
  }

  @Override
  public boolean isDeterministicPKName(String pkName) {
    return !randomNamePattern.matcher(pkName).matches();
  }

  @Override
  public boolean isDeterministicUKName(String ukName) {
    return !randomNamePattern.matcher(ukName).matches();
  }

  @Override
  public boolean isDeterministicFKName(String fkName) {
    return !randomNamePattern.matcher(fkName).matches();
  }

  @Override
  public boolean isDeterministicIndexName(String indexName) {
    return !randomNamePattern.matcher(indexName).matches();
  }

  @Override
  public boolean supportsRegex() {
    return true;
  }

  @Override
  public String regexQuery(String expression, boolean not, String regex) {
    return (not ? "NOT " : "") + "REGEXP_LIKE(" + expression + ", '" + regex + "')";
  }


  //  @Override
  //  public List<DBIndex> queryIndexes(DBSchema schema, Connection connection) throws SQLException {
  //    String query = "SELECT INDEX_NAME, INDEX_TYPE, TABLE_OWNER, TABLE_NAME, TABLE_TYPE, UNIQUENESS" +
  //        " FROM USER_INDEXES";
  //    if (schema != null) {
  //      query += " WHERE TABLE_OWNER = '" + schema.getName().toUpperCase() + "'";
  //    }
  //    List<Object[]> indexInfos = DBUtil.query(query, connection);
  //    OrderedMap<String, DBIndex> indexes = new OrderedMap<String, DBIndex>();
  //    for (int i = 0; i < indexInfos.size(); i++) {
  //      Object[] indexInfo = indexInfos.get(i);
  //      String ownerName = (String) indexInfo[2];
  //      if (schema == null || schema.getName().equals(ownerName)) {
  //        boolean unique = "UNIQUE".equalsIgnoreCase(indexInfo[5].toString());
  //        String name = (String) indexInfo[0];
  //        String tableName = (String) indexInfo[3];
  //        boolean deterministicName = isDeterministicIndexName(name);
  //        DBTable table = schema.getTable(tableName);
  //        DBIndex index;
  //        if (unique) {
  //          DBUniqueConstraint uk = table.getUniqueConstraint(name);
  //          index = new DBUniqueIndex(name, deterministicName, uk);
  //        } else {
  //          index = new DBNonUniqueIndex(name, deterministicName, table);
  //        }
  //        indexes.put(index.getName(), index);
  //        LOGGER.debug("Imported index {}", index);
  //      }
  //    }
  //
  //    // query package procedures
  //    query = "SELECT INDEX_NAME, TABLE_NAME, COLUMN_NAME, COLUMN_POSITION FROM USER_IND_COLUMNS";
  //    if (schema != null) {
  //      query += " AND OWNER = '" + schema.getName().toUpperCase() + "'";
  //    }
  //    query += " ORDER BY INDEX_NAME, COLUMN_POSITION";
  //    List<Object[]> colInfos = DBUtil.query(query, connection);
  //    for (int i = 0; i < colInfos.size(); i++) {
  //      Object[] colInfo = colInfos.get(i);
  //      DBIndex index = indexes.get((String) colInfo[0]);
  //      String columnName = (String) colInfo[2];
  //      index.addColumnName(columnName);
  //      LOGGER.debug("Imported index column {}.{}", index.getName(), columnName);
  //    }
  //    return indexes.values();
  //  }


  @Override
  public void queryTriggers(DBSchema schema, Connection connection) throws SQLException {
    String query = "SELECT OWNER, TRIGGER_NAME, TRIGGER_TYPE, TRIGGERING_EVENT, TABLE_OWNER, BASE_OBJECT_TYPE, " +
        "TABLE_NAME, COLUMN_NAME, REFERENCING_NAMES, WHEN_CLAUSE, STATUS, DESCRIPTION, ACTION_TYPE, " +
        "TRIGGER_BODY FROM SYS.ALL_TRIGGERS";
    if (schema != null) {
      query += " WHERE OWNER = '" + schema.getName().toUpperCase() + "'";
    }
    ResultSet resultSet = DBUtil.executeQuery(query, connection);
    List<DBTrigger> triggers = new ArrayList<>();
    try {
      while (resultSet.next()) {
        DBTrigger trigger = new DBTrigger(resultSet.getString(2), null);
        trigger.setOwner(schema);
        Assert.isTrue(schema != null, "schema is null");
        schema.receiveTrigger(trigger); // use receiveTrigger(), because the DBTrigger ctor would cause a recursion in trigger import
        trigger.setTriggerType(resultSet.getString(3));
        trigger.setTriggeringEvent(resultSet.getString(4));
        trigger.setTableOwner(resultSet.getString(5));
        trigger.setBaseObjectType(resultSet.getString(6));
        trigger.setTableName(resultSet.getString(7));
        trigger.setColumnName(resultSet.getString(8));
        trigger.setReferencingNames(resultSet.getString(9));
        trigger.setWhenClause(resultSet.getString(10));
        trigger.setStatus(resultSet.getString(11));
        trigger.setDescription(resultSet.getString(12));
        trigger.setActionType(resultSet.getString(13));
        trigger.setTriggerBody(resultSet.getString(14));
        triggers.add(trigger);
        logger.debug("Imported trigger: {}", trigger.getName());
      }
    } finally {
      DBUtil.closeResultSetAndStatement(resultSet);
    }
  }

  @Override
  public List<DBPackage> queryPackages(DBSchema schema, Connection connection) throws SQLException {

    // query packages
    String query = "SELECT USER, OBJECT_NAME, SUBOBJECT_NAME, OBJECT_ID, OBJECT_TYPE, STATUS" +
        " FROM USER_OBJECTS WHERE UPPER(OBJECT_TYPE) = 'PACKAGE'";
    List<Object[]> pkgInfos = DBUtil.query(query, connection);
    OrderedMap<String, DBPackage> packages = new OrderedMap<>();
    for (Object[] pkgInfo : pkgInfos) {
      String ownerName = (String) pkgInfo[0];
      if (schema == null || schema.getName().equals(ownerName)) {
        String name = (String) pkgInfo[1];
        DBPackage pkg = new DBPackage(name, null);
        Assert.isTrue(schema != null, "Schema is null");
        if (schema != null) {
          schema.receivePackage(pkg);
        }
        pkg.setSchema(schema);
        pkg.setSubObjectName((String) pkgInfo[2]);
        pkg.setObjectId(pkgInfo[3].toString());
        pkg.setObjectType((String) pkgInfo[4]);
        pkg.setStatus((String) pkgInfo[5]);
        packages.put(pkg.getName(), pkg);
        logger.debug("Imported package {}", pkg);
      }
    }

    // query package procedures
    query = "SELECT OBJECT_NAME, PROCEDURE_NAME, OBJECT_ID, SUBPROGRAM_ID, OVERLOAD" +
        " FROM SYS.USER_PROCEDURES WHERE UPPER(OBJECT_TYPE) = 'PACKAGE'" +
        " AND PROCEDURE_NAME IS NOT NULL AND OBJECT_NAME IN (" +
        CollectionUtil.formatCommaSeparatedList(NameUtil.getNames(packages.values()), '\'') + ")";
    List<Object[]> procInfos = DBUtil.query(query, connection);
    for (Object[] procInfo : procInfos) {
      DBPackage owner = packages.get(procInfo[0]);
      String name = (String) procInfo[1];
      DBProcedure proc = new DBProcedure(name, owner);
      proc.setObjectId(procInfo[2].toString());
      proc.setSubProgramId(procInfo[3].toString());
      proc.setOverload((String) procInfo[4]);
      logger.debug("Imported package procedure {}.{}", owner.getName(), proc.getName());
    }
    return packages.values();
  }

  @Override
  public void restrictRownums(int firstRowIndex, int rowCount, Query query) {
    String condition;
    if (firstRowIndex > 1) {
      condition = "ROWNUM BETWEEN " + firstRowIndex + " AND " + (firstRowIndex + rowCount);
    } else {
      condition = "ROWNUM <= " + rowCount;
    }
    query.and(condition);
  }

  @Override
  public String trim(String expression) {
    return "TRIM(" + expression + ")";
  }

  @Override
  public String getSpecialType(String type) {
    if ("varchar".equals(type)) {
      return "varchar2";
    } else if ("double".equals(type)) {
      return "number";
    } else {
      return super.getSpecialType(type);
    }
  }
}