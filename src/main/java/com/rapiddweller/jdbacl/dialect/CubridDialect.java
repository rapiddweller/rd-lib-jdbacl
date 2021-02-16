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

package com.rapiddweller.jdbacl.dialect;

import com.rapiddweller.common.ArrayBuilder;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.model.DBSchema;
import com.rapiddweller.jdbacl.model.DBSequence;
import com.rapiddweller.jdbacl.model.DBTrigger;
import com.rapiddweller.jdbacl.sql.Query;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link DatabaseDialect} implementation for the CUBRID database.<br/><br/>
 * Created: 13.04.2012 06:53:40
 *
 * @author Volker Bergmann
 * @since 0.8.2
 */
public class CubridDialect extends DatabaseDialect {

  /**
   * Instantiates a new Cubrid dialect.
   */
  public CubridDialect() {
    super("cubrid", true, true, "''yyyy-MM-dd''", "''HH:mm:ss''", "''yyyy-MM-dd HH:mm:ss''");
  }

  @Override
  public boolean isDefaultCatalog(String catalog, String user) {
    return true;
  }

  @Override
  public boolean isDefaultSchema(String schema, String user) {
    return true;
  }


  // sequence support ------------------------------------------------------------------------------------------------

  @Override
  public DBSequence[] querySequences(Connection connection) throws SQLException {
    String query = "select name, owner, current_val, increment_val, max_val, min_val, cyclic, " +
        "class_name, att_name, cached_num from db_serial";
    ResultSet resultSet = DBUtil.executeQuery(query, connection);
    ArrayBuilder<DBSequence> builder = new ArrayBuilder<>(DBSequence.class);
    while (resultSet.next()) {
      DBSequence sequence = new DBSequence(resultSet.getString(1), null);
      sequence.setLastNumber(new BigInteger(resultSet.getString(3)));
      sequence.setIncrement(new BigInteger(resultSet.getString(4)));
      sequence.setMaxValue(new BigInteger(resultSet.getString(5)));
      sequence.setMinValue(new BigInteger(resultSet.getString(6)));
      sequence.setCycle(resultSet.getInt(7) != 0);
      sequence.setCache(resultSet.getLong(10));
    }
    return builder.toArray();
  }

  @Override
  public void createSequence(String name, long initialValue, Connection connection) throws SQLException {
    DBUtil.executeQuery("create serial " + name + " start with " + initialValue, connection);
  }

  @Override
  public String renderCreateSequence(DBSequence sequence) {
		/*
		CREATE SERIAL serial_name
		[ START WITH initial ]
		[ INCREMENT BY interval]
		[ MINVALUE min | NOMINVALUE ]
		[ MAXVALUE max | NOMAXVALUE ]
		[ CACHE integer | NOCACHE ]
		[ CYCLE | NOCYCLE ]
	 */
    StringBuilder builder = new StringBuilder("CREATE SERIAL ");
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
    // apply cache settings
    Long cache = sequence.getCache();
    if (cache != null) {
      builder.append(" CACHE ").append(cache);
    }
    // apply cycle settings
    Boolean cycle = sequence.isCycle();
    if (cycle != null) {
      builder.append(cycle ? " CYCLE" : " " + sequenceNoCycle());
    }
    return builder.toString();
  }

  @Override
  public String renderFetchSequenceValue(String sequenceName) {
    return "SELECT " + sequenceName + ".NEXT_VALUE";
  }

  @Override
  public void setNextSequenceValue(String sequenceName, long value, Connection connection) throws SQLException {
    DBUtil.executeUpdate("ALTER SERIAL " + sequenceName + " START WITH " + value, connection);
  }


  // querying triggers -----------------------------------------------------------------------------------------------

  @Override
  public void queryTriggers(DBSchema schema, Connection connection) throws SQLException {
    String query = "SELECT t.owner, t.name, t.status, t.priority, t.event, " +
        "c.class_name as table_name, t.target_attribute, t.target_class_attribute, " +
        "t.condition_type, t.condition, t.condition_time, " +
        "t.action_type, t.action_definition, t.action_time FROM db_trigger as t " +
        "join _db_class as c on t.target_class = c.class_of";
    ResultSet resultSet = DBUtil.executeQuery(query, connection);
    List<DBTrigger> triggers = new ArrayList<>();
    try {
      while (resultSet.next()) {
        String triggerName = resultSet.getString("name");
        DBTrigger trigger = new DBTrigger(triggerName, null);
        trigger.setOwner(schema);
        schema.receiveTrigger(trigger); // use receiveTrigger(), because the DBTrigger ctor would cause a recursion in trigger import

        // parse status
        int statusFlag = resultSet.getInt("status");
        String status = (statusFlag == 2 ? "ACTIVE" : "INACTIVE");
        trigger.setStatus(status);

        // parse priority
        trigger.setPriority(resultSet.getDouble("priority"));

        // parse event
        int eventFlag = resultSet.getInt("event");
        String event;
        switch (eventFlag) {
          case 0:
            event = "UPDATE";
            break;
          case 1:
            event = "UPDATE STATEMENT";
            break;
          case 2:
            event = "DELETE";
            break;
          case 3:
            event = "DELETE STATEMENT";
            break;
          case 4:
            event = "INSERT";
            break;
          case 5:
            event = "INSERT STATEMENT";
            break;
          case 8:
            event = "COMMIT";
            break;
          case 9:
            event = "ROLLBACK";
            break;
          default:
            event = "<ERROR>";
            logger.error("Illegal event flag in trigger {}: {}", triggerName, eventFlag);
        }
        trigger.setTriggeringEvent(event);

        // parse table name
        trigger.setTableName(resultSet.getString("table_name"));

        // parse target column
        trigger.setColumnName(resultSet.getString("target_attribute"));

        // parse target_class_attribute (0/1)
        trigger.setStaticColumn(resultSet.getInt("target_class_attribute") == 1);

        // condition_type ignored (1 -> condition exists, null -> no condition exists)

        // parse condition
        trigger.setWhenClause(resultSet.getString("condition"));

        // parse condition_time
        Object conditionTimeObject = resultSet.getObject("condition_time"); // may be null
        if (conditionTimeObject != null) {
          int conditionTimeFlag = ((Number) conditionTimeObject).intValue();
          String conditionTime;
          switch (conditionTimeFlag) {
            case 1:
              conditionTime = "BEFORE";
              break;
            case 2:
              conditionTime = "AFTER";
              break;
            case 3:
              conditionTime = "DEFERRED";
              break;
            default:
              conditionTime = "<ERROR>";
              logger.error("Illegal condition time flag in trigger {}: {}", triggerName, conditionTime);
          }
          trigger.setConditionTime(conditionTime);
        }

        // parse action type
        int actionTypeFlag = resultSet.getInt("action_type");
        String actionType;
        switch (actionTypeFlag) {
          case 1:
            actionType = "INSERT OR UPDATE OR DELETE OR CALL OR EVALUATE";
            break;
          case 2:
            actionType = "REJECT";
            break;
          case 3:
            actionType = "INVALIDATE_TRANSACTION";
            break;
          case 4:
            actionType = "PRINT";
            break;
          default:
            actionType = "<ERROR>";
            logger.error("Illegal action type flag in trigger {}: {}", triggerName, actionTypeFlag);
        }
        trigger.setActionType(actionType);

        // parse action definition
        trigger.setTriggerBody(resultSet.getString("action_definition"));

        // parse action_time
        int actionTimeFlag = resultSet.getInt("action_time");
        String actionTime;
        switch (actionTimeFlag) {
          case 1:
            actionTime = "BEFORE";
            break;
          case 2:
            actionTime = "AFTER";
            break;
          case 3:
            actionTime = "DEFERRED";
            break;
          default:
            actionTime = "<ERROR>";
            logger.error("Illegal action time flag in trigger {}: {}", triggerName, actionTime);
        }
        trigger.setTriggerType(actionTime);

        triggers.add(trigger);
        logger.debug("Imported trigger: {}", trigger.getName());
      }
    } finally {
      DBUtil.closeResultSetAndStatement(resultSet);
    }
  }

  // recognition of generated names ----------------------------------------------------------------------------------

  @Override
  public boolean isDeterministicPKName(String pkName) {
    return true;
  }

  @Override
  public boolean isDeterministicUKName(String ukName) {
    return true;
  }

  @Override
  public boolean isDeterministicFKName(String fkName) {
    return true;
  }

  @Override
  public boolean isDeterministicIndexName(String indexName) {
    return true;
  }


  // other features --------------------------------------------------------------------------------------------------

  @Override
  public String trim(String expression) {
    return "trim(" + expression + ")";
  }

  @Override
  public void restrictRownums(int offset, int rowCount, Query query) {
    if (offset == 0) {
      query.addOption("limit " + rowCount);
    } else {
      query.addOption("limit " + offset + ", " + rowCount);
    }
  }

  @Override
  public boolean supportsRegex() {
    return true;
  }

  @Override
  public String regexQuery(String expression, boolean not, String regex) {
    return expression + (not ? " NOT" : "") + " REGEX '" + regex + "'";
  }

}
