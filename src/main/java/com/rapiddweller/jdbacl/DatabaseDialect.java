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

package com.rapiddweller.jdbacl;

import com.rapiddweller.common.ArrayUtil;
import com.rapiddweller.common.ConfigurationError;
import com.rapiddweller.common.IOUtil;
import com.rapiddweller.common.ObjectNotFoundException;
import com.rapiddweller.common.StringUtil;
import com.rapiddweller.common.TimeUtil;
import com.rapiddweller.common.converter.TimestampFormatter;
import com.rapiddweller.jdbacl.model.DBPackage;
import com.rapiddweller.jdbacl.model.DBSchema;
import com.rapiddweller.jdbacl.model.DBSequence;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.sql.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.rapiddweller.jdbacl.SQLUtil.appendColumnName;
import static com.rapiddweller.jdbacl.SQLUtil.createCatSchTabString;

/**
 * Provides abstractions of concepts that are implemented differently
 * by different database vendors.<br/><br/>
 *
 * @author Volker Bergmann
 * @since 0.4.0
 */
@SuppressWarnings("checkstyle:CommentsIndentation")
public abstract class DatabaseDialect {

  private static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSSSSSSSS";
  /**
   * The Quote table names.
   */
  public final boolean quoteTableNames;
  /**
   * The Logger.
   */
  protected final Logger logger = LogManager.getLogger(getClass());
  /**
   * The Sequence supported.
   */
  protected final boolean sequenceSupported;
  private final String system;
  private final DateFormat dateFormat;
  private final DateFormat datetimeFormat;
  private final DateFormat timeFormat;
  private Set<String> reservedWords;

  /**
   * Instantiates a new Database dialect.
   *
   * @param system            the system
   * @param quoteTableNames   the quote table names
   * @param sequenceSupported the sequence supported
   * @param datePattern       the date pattern
   * @param timePattern       the time pattern
   * @param datetimePattern   the datetime pattern
   */
  public DatabaseDialect(String system, boolean quoteTableNames, boolean sequenceSupported,
                         String datePattern, String timePattern, String datetimePattern) {
    this.system = system;
    this.quoteTableNames = quoteTableNames;
    this.sequenceSupported = sequenceSupported;
    this.dateFormat = new SimpleDateFormat(datePattern);
    this.timeFormat = new SimpleDateFormat(timePattern);
    this.datetimeFormat = new SimpleDateFormat(datetimePattern);
    this.reservedWords = null;
  }

  /**
   * Is not one boolean.
   *
   * @param i the
   * @return the boolean
   */
  protected static boolean isNotOne(BigInteger i) {
    return (BigInteger.ONE.compareTo(i) != 0);
  }

  /**
   * Gets system.
   *
   * @return the system
   */
  public String getSystem() {
    return system;
  }

  /**
   * Is reserved word boolean.
   *
   * @param word       the word
   * @param connection the connection
   * @return the boolean
   * @throws SQLException the sql exception
   */
  public boolean isReservedWord(String word, Connection connection) throws SQLException {
    return (word != null && getReservedWords(connection).contains(word.toUpperCase()));
  }

  /**
   * Gets reserved words.
   *
   * @param connection the connection
   * @return the reserved words
   * @throws SQLException the sql exception
   */
  public Set<String> getReservedWords(Connection connection) throws SQLException {
    if (reservedWords == null) {
      importReservedWords(connection);
    }
    return reservedWords;
  }

  /**
   * Imports the reserved words defined in a configuration file, then adds words retrieved by
   * {@link DatabaseMetaData#getSQLKeywords()}. If no system-specific configuration file is found,
   * jdbacl falls back to the keywords defined in SQL:2003. The combination of both approaches
   * happens, since the documentation of {@link DatabaseMetaData#getSQLKeywords()} says that it
   * "Retrieves a comma-separated list of all of this database's SQL keywords that are NOT also
   * SQL:2003 keywords. By adding keywords dynamically retrieved from the JDBC driver I also get
   * the chance to automatically handle keywords introduced in a new database version without
   * having loads of different reserved-words-files.
   *
   * @param connection the connection
   * @throws SQLException the sql exception
   */
  protected void importReservedWords(Connection connection) throws SQLException {
    this.reservedWords = new HashSet<>();
    parseReservedWordsConfigFile();
    if (connection != null) {
      importReservedWordsFromDriver(connection);
    }
  }

  private void importReservedWordsFromDriver(Connection connection) throws SQLException {
    DatabaseMetaData metaData = connection.getMetaData();
    String keywordList = metaData.getSQLKeywords();
    logger.debug("Imported keywords: " + keywordList);
    String[] keywords = StringUtil.splitAndTrim(keywordList, ',');
    for (String keyword : keywords) {
      this.reservedWords.add(keyword.toUpperCase());
    }
  }

  private void parseReservedWordsConfigFile() {
    String resourceName = "com/rapiddweller/jdbacl/dialect/" + system + "-reserved_words.txt";
    if (IOUtil.isURIAvailable(resourceName)) {
      parseReservedWords(resourceName);
    } else {
      logger.debug("Configuration file not found: " + resourceName + ". Falling back to SQL:2003 keywords");
      parseReservedWords("com/rapiddweller/jdbacl/dialect/SQL2003-reserved_words.txt");
    }
  }

  private void parseReservedWords(String resourceName) {
    logger.debug("reading reserved word from config file " + resourceName);
    try {
      for (String word : IOUtil.readTextLines(resourceName, false)) {
        reservedWords.add(word.trim());
      }
    } catch (IOException e) {
      throw new ConfigurationError("Error reading file " + resourceName, e);
    }
  }

  /**
   * Is default catalog boolean.
   *
   * @param catalog the catalog
   * @param user    the user
   * @return the boolean
   */
  public abstract boolean isDefaultCatalog(String catalog, String user);

  /**
   * Is default schema boolean.
   *
   * @param schema the schema
   * @param user   the user
   * @return the boolean
   */
  public abstract boolean isDefaultSchema(String schema, String user);

  /**
   * Is sequence supported boolean.
   *
   * @return the boolean
   */
  public boolean isSequenceSupported() {
    return sequenceSupported;
  }

  /**
   * Is sequence boundary supported boolean.
   *
   * @return the boolean
   */
  public boolean isSequenceBoundarySupported() {
    return sequenceSupported;
  }

  /**
   * Query sequences db sequence [ ].
   *
   * @param connection the connection
   * @return the db sequence [ ]
   * @throws SQLException the sql exception
   */
  public DBSequence[] querySequences(Connection connection) throws SQLException {
    throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support querying sequences");
  }

  /**
   * Create sequence.
   *
   * @param name         the name
   * @param initialValue the initial value
   * @param connection   the connection
   * @throws SQLException the sql exception
   */
  public void createSequence(String name, long initialValue, Connection connection) throws SQLException {
    if (sequenceSupported) {
      DBUtil.executeUpdate("create sequence " + name + " start with " + initialValue, connection);
    } else {
      throw checkSequenceSupport("createSequence");
    }
  }

  /**
   * Render create sequence string.
   *
   * @param sequence the sequence
   * @return the string
   */
  public String renderCreateSequence(DBSequence sequence) {
    StringBuilder builder = new StringBuilder("CREATE SEQUENCE ");
    builder.append(renderSequenceNameAndType(sequence));
    BigInteger start = sequence.getStart();
    if (start != null && isNotOne(start)) {
      builder.append(" START WITH ").append(start);
    }
    BigInteger increment = sequence.getIncrement();
    if (increment != null && isNotOne(increment)) {
      builder.append(" INCREMENT BY ").append(increment);
    }
    if (isSequenceBoundarySupported()) {
      BigInteger maxValue = sequence.getMaxValue();
      if (maxValue != null) {
        builder.append(" MAXVALUE ").append(maxValue);
      }
      BigInteger minValue = sequence.getMinValue();
      if (minValue != null) {
        builder.append(" MINVALUE ").append(minValue);
      }
    }
    Boolean cycle = sequence.isCycle();
    if (cycle != null) {
      builder.append(cycle ? " CYCLE" : " " + sequenceNoCycle());
    }
    return builder.toString();
  }

  /**
   * Render sequence name and type string.
   *
   * @param sequence the sequence
   * @return the string
   */
  protected String renderSequenceNameAndType(DBSequence sequence) {
    return sequence.getName();
  }

  /**
   * Sequence no cycle string.
   *
   * @return the string
   */
  protected String sequenceNoCycle() {
    return "NOCYCLE";
  }

  /**
   * Render fetch sequence value string.
   *
   * @param sequenceName the sequence name
   * @return the string
   */
  public String renderFetchSequenceValue(String sequenceName) {
    throw checkSequenceSupport("nextSequenceValue");
  }

  /**
   * Sets next sequence value.
   *
   * @param sequenceName the sequence name
   * @param value        the value
   * @param connection   the connection
   * @throws SQLException the sql exception
   */
  public void setNextSequenceValue(String sequenceName, long value, Connection connection) throws SQLException {
    if (sequenceSupported) {
      long old = DBUtil.queryLong(renderFetchSequenceValue(sequenceName), connection);
      long increment = value - old - 1;
      if (increment != 0) {
        BigInteger formerIncrement = getSequence(sequenceName, connection).getIncrement();
        DBUtil.executeUpdate("alter sequence " + sequenceName + " increment by " + increment, connection);
        DBUtil.queryLong(renderFetchSequenceValue(sequenceName), connection);
        DBUtil.executeUpdate("alter sequence " + sequenceName + " increment by " + formerIncrement, connection);
      }
    } else {
      throw checkSequenceSupport("incrementSequence");
    }
  }

  /**
   * Gets sequence.
   *
   * @param sequenceName the sequence name
   * @param connection   the connection
   * @return the sequence
   * @throws SQLException the sql exception
   */
  public DBSequence getSequence(String sequenceName, Connection connection) throws SQLException {
    DBSequence[] sequences = querySequences(connection);
    for (DBSequence seq : sequences) {
      if (seq.getName().equalsIgnoreCase(sequenceName)) {
        return seq;
      }
    }
    throw new ObjectNotFoundException("No sequence found with name '" + sequenceName + "'");
  }

  /**
   * Render drop sequence string.
   *
   * @param sequenceName the sequence name
   * @return the string
   */
  public String renderDropSequence(String sequenceName) {
    if (sequenceSupported) {
      return "drop sequence " + sequenceName;
    } else {
      throw checkSequenceSupport("dropSequence");
    }
  }

  /**
   * Insert string.
   *
   * @param table       the table
   * @param columnInfos the column infos
   * @return the string
   */
  public String insert(DBTable table, List<ColumnInfo> columnInfos) {
    StringBuilder builder = new StringBuilder("insert into ");
    builder.append(createCatSchTabString(table.getCatalog().getName(), table.getSchema().getName(), table.getName(), this)).append(" (");
    if (columnInfos.size() > 0) {
      appendColumnName(columnInfos.get(0).name, builder, this);
    }
    for (int i = 1; i < columnInfos.size(); i++) {
      builder.append(",");
      appendColumnName(columnInfos.get(i).name, builder, this);
    }
    builder.append(") values (");
    if (columnInfos.size() > 0) {
      builder.append("?");
    }
    builder.append(",?".repeat(Math.max(0, columnInfos.size() - 1)));
    builder.append(")");
    String sql = builder.toString();
    logger.debug("built SQL statement: " + sql);
    return sql;
  }

  /**
   * Update string.
   *
   * @param table         the table
   * @param pkColumnNames the pk column names
   * @param columnInfos   the column infos
   * @return the string
   */
  public String update(DBTable table, String[] pkColumnNames, List<ColumnInfo> columnInfos) {
    if (pkColumnNames.length == 0) {
      throw new UnsupportedOperationException("Cannot update table without primary key: " + table.getName());
    }
    StringBuilder builder = new StringBuilder("update ");
    builder.append(createCatSchTabString(table.getCatalog().getName(), table.getSchema().getName(), table.getName(), this)).append(" set");
    for (int i = 0; i < columnInfos.size(); i++) {
      if (!ArrayUtil.contains(columnInfos.get(i).name, pkColumnNames)) {
        builder.append(" ");
        appendColumnName(columnInfos.get(i).name, builder, this);
        builder.append("=?");
        if (i < columnInfos.size() - pkColumnNames.length - 1) {
          builder.append(", ");
        }
      }
    }
    builder.append(" where");
    for (int i = 0; i < pkColumnNames.length; i++) {
      builder.append(' ');
      appendColumnName(pkColumnNames[i], builder, this);
      builder.append("=?");
      if (i < pkColumnNames.length - 1) {
        builder.append(" and");
      }
    }
    String sql = builder.toString();
    logger.debug("built SQL statement: " + sql);
    return sql;
  }

  /**
   * Format value string.
   *
   * @param value the value
   * @return the string
   */
  public String formatValue(Object value) {
    if (value instanceof CharSequence || value instanceof Character) {
      return "'" + DBUtil.escape(value.toString()) + "'";
    } else if (value instanceof Timestamp) {
      return formatTimestamp((Timestamp) value);
    } else if (value instanceof Time) {
      return timeFormat.format(value);
    } else if (value instanceof Date) {
      if (TimeUtil.isMidnight((Date) value)) {
        return dateFormat.format(value);
      } else {
        return datetimeFormat.format(value);
      }
    } else {
      return String.valueOf(value);
    }
  }

  /**
   * Format timestamp string.
   *
   * @param timestamp the timestamp
   * @return the string
   */
  public String formatTimestamp(Timestamp timestamp) {
    return "'" + new TimestampFormatter(DEFAULT_TIMESTAMP_PATTERN).format(timestamp) + "'";
  }

  // private helpers for prepared statements-------------------------------------------------------------------------------------------------


  /**
   * Check sequence support unsupported operation exception.
   *
   * @param methodName the method name
   * @return the unsupported operation exception
   */
  protected UnsupportedOperationException checkSequenceSupport(String methodName) {
    if (!sequenceSupported) {
      return new UnsupportedOperationException("Sequence not supported in " + system);
    } else {
      return new UnsupportedOperationException(methodName + "() not implemented");
    }
  }

  /**
   * Determines if a primary key constraint name was explicitly specified on creation
   * or at least generated by the database in a deterministic (reproducible) way
   *
   * @param pkName the pk name
   * @return the boolean
   */
  public abstract boolean isDeterministicPKName(String pkName);

  /**
   * Determines if a unique key constraint name was explicitly specified on creation
   * or at least generated by the database in a deterministic (reproducible) way
   *
   * @param ukName the uk name
   * @return the boolean
   */
  public abstract boolean isDeterministicUKName(String ukName);

  /**
   * Determines if a foreign key constraint name was explicitly specified creation
   * or at least generated by the database in a deterministic (reproducible) way
   *
   * @param fkName the fk name
   * @return the boolean
   */
  public abstract boolean isDeterministicFKName(String fkName);

  /**
   * Determines if an index name was explicitly specified creation
   * or at least generated by the database in a deterministic (reproducible) way
   *
   * @param indexName the index name
   * @return the boolean
   */
  public abstract boolean isDeterministicIndexName(String indexName);

  /**
   * Tells if the database supports regular expressions
   *
   * @return the boolean
   */
  public boolean supportsRegex() {
    return false;
  }

  /**
   * Renders a query condition for a regular expression.
   *
   * @param expression a column name or a SQL value expression to be checked with a regular expression
   * @param not        if set to true, the query fits expressions which do not match the regular expression
   * @param regex      the regular expression to check with
   * @return a string with a SQL query condition.
   * @throws UnsupportedOperationException if the database does not support regular expressions
   */
  public String regexQuery(String expression, boolean not, String regex) {
    throw new UnsupportedOperationException(system + " does not support regular expressions");
  }

  /**
   * Trim string.
   *
   * @param expression the expression
   * @return the string
   */
  public String trim(String expression) {
    throw new UnsupportedOperationException(system + " does not support trimming");
  }

  /**
   * Render case string.
   *
   * @param columnName              the column name
   * @param elseExpression          the else expression
   * @param whenThenExpressionPairs the when then expression pairs
   * @return the string
   */
  public String renderCase(String columnName, String elseExpression, String... whenThenExpressionPairs) {
    StringBuilder builder = new StringBuilder();
    builder.append("CASE");
    for (int i = 0; i < whenThenExpressionPairs.length; i += 2) {
      builder.append(" WHEN ").append(whenThenExpressionPairs[i]); // when part
      builder.append(" THEN ").append(whenThenExpressionPairs[i + 1]); // then part
    }
    if (!StringUtil.isEmpty(elseExpression)) {
      builder.append(" ELSE ").append(elseExpression); // else part
    }
    builder.append(" END"); // closing the case
    if (columnName != null) {
      builder.append(" AS ").append(columnName); // applying column name
    }
    return builder.toString();
  }
	
  /* TODO v0.8.x implement queries for indexes, views, functions and procedures
    public List<DBView> queryViews(Connection connection) throws SQLException {
		return new ArrayList<DBView>();
		// ORA: select VIEW_NAME, OWNER from SYS.ALL_VIEWS order by OWNER, VIEW_NAME
	}

    public List<DBIndex> queryIndexes(DBSchema schema, Connection connection) throws SQLException {
		return new ArrayList<DBIndex>();
	}

    public List<DBFunction> queryFunctions(Connection connection) throws SQLException {
		return new ArrayList<DBTrigger>();
		// ORA: select OBJECT_NAME, OWNER from SYS.ALL_OBJECTS where upper(OBJECT_TYPE) = upper('FUNCTION') order by OWNER, OBJECT_NAME 
	}

    public List<DBProcedure> queryProcedures(Connection connection) throws SQLException {
		return new ArrayList<DBTrigger>();
		// ORA: select OBJECT_NAME, OWNER from SYS.ALL_OBJECTS where upper(OBJECT_TYPE) = upper('PROCEDURE') order by OWNER, OBJECT_NAME 
	}
  */

  /**
   * Query triggers.
   *
   * @param schema     the schema
   * @param connection the connection
   * @throws SQLException the sql exception
   */
  public void queryTriggers(DBSchema schema, Connection connection) throws SQLException {
  }

  /**
   * Query packages list.
   *
   * @param schema     the schema
   * @param connection the connection
   * @return the list
   * @throws SQLException the sql exception
   */
  public List<DBPackage> queryPackages(DBSchema schema, Connection connection) throws SQLException {
    return new ArrayList<>();
  }

  /**
   * Restrict rownums.
   *
   * @param rowOffset the row offset
   * @param rowCount  the row count
   * @param query     the query
   */
  public abstract void restrictRownums(int rowOffset, int rowCount, Query query);

}
