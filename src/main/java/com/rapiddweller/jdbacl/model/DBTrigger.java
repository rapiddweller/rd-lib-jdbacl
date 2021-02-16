/*
 * (c) Copyright 2011-2012 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.model;

import com.rapiddweller.common.NullSafeComparator;

/**
 * Represents a database trigger.<br/><br/>
 * Created: 07.11.2011 14:46:14
 *
 * @author Volker Bergmann
 * @since 0.7.0
 */
public class DBTrigger extends AbstractDBObject implements ContainerComponent {

  private static final long serialVersionUID = -183721433730785529L;

  /**
   * Type of the trigger, e.g. "AFTER STATEMENT", "BEFORE STATEMENT", "BEFORE EACH ROW"
   */
  private String triggerType;

  /**
   * the event type which invokes the trigger, e.g. "INSERT OR UPDATE OR DELETE"
   */
  private String triggeringEvent;

  private String tableOwner;
  private String baseObjectType;
  private String tableName;
  private String columnName;
  private String referencingNames;
  private String whenClause;
  private String status;
  private String description;
  private String actionType;
  private String triggerBody;

  private Double priority;
  private Boolean staticColumn;
  private String conditionTime;

  /**
   * Instantiates a new Db trigger.
   *
   * @param name  the name
   * @param owner the owner
   */
  public DBTrigger(String name, DBSchema owner) {
    super(name, "trigger", owner);
    if (owner != null) {
      owner.addTrigger(this);
    }
  }

  /**
   * Gets trigger type.
   *
   * @return the trigger type
   */
  public String getTriggerType() {
    return triggerType;
  }

  /**
   * Sets trigger type.
   *
   * @param triggerType the trigger type
   */
  public void setTriggerType(String triggerType) {
    this.triggerType = triggerType;
  }

  /**
   * Gets triggering event.
   *
   * @return the triggering event
   */
  public String getTriggeringEvent() {
    return triggeringEvent;
  }

  /**
   * Sets triggering event.
   *
   * @param triggeringEvent the triggering event
   */
  public void setTriggeringEvent(String triggeringEvent) {
    this.triggeringEvent = triggeringEvent;
  }

  /**
   * Gets table owner.
   *
   * @return the table owner
   */
  public String getTableOwner() {
    return tableOwner;
  }

  /**
   * Sets table owner.
   *
   * @param tableOwner the table owner
   */
  public void setTableOwner(String tableOwner) {
    this.tableOwner = tableOwner;
  }

  /**
   * Gets base object type.
   *
   * @return the base object type
   */
  public String getBaseObjectType() {
    return baseObjectType;
  }

  /**
   * Sets base object type.
   *
   * @param baseObjectType the base object type
   */
  public void setBaseObjectType(String baseObjectType) {
    this.baseObjectType = baseObjectType;
  }

  /**
   * Gets table name.
   *
   * @return the table name
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * Sets table name.
   *
   * @param tableName the table name
   */
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  /**
   * Gets column name.
   *
   * @return the column name
   */
  public String getColumnName() {
    return columnName;
  }

  /**
   * Sets column name.
   *
   * @param columnName the column name
   */
  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  /**
   * Gets referencing names.
   *
   * @return the referencing names
   */
  public String getReferencingNames() {
    return referencingNames;
  }

  /**
   * Sets referencing names.
   *
   * @param referencingNames the referencing names
   */
  public void setReferencingNames(String referencingNames) {
    this.referencingNames = referencingNames;
  }

  /**
   * Gets when clause.
   *
   * @return the when clause
   */
  public String getWhenClause() {
    return whenClause;
  }

  /**
   * Sets when clause.
   *
   * @param whenClause the when clause
   */
  public void setWhenClause(String whenClause) {
    this.whenClause = whenClause;
  }

  /**
   * Gets status.
   *
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Sets status.
   *
   * @param status the status
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Gets description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets description.
   *
   * @param description the description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets action type.
   *
   * @return the action type
   */
  public String getActionType() {
    return actionType;
  }

  /**
   * Sets action type.
   *
   * @param actionType the action type
   */
  public void setActionType(String actionType) {
    this.actionType = actionType;
  }

  /**
   * Gets trigger body.
   *
   * @return the trigger body
   */
  public String getTriggerBody() {
    return triggerBody;
  }

  /**
   * Sets trigger body.
   *
   * @param triggerBody the trigger body
   */
  public void setTriggerBody(String triggerBody) {
    this.triggerBody = triggerBody;
  }

  /**
   * Gets priority.
   *
   * @return the priority
   */
  public double getPriority() {
    return priority;
  }

  /**
   * Sets priority.
   *
   * @param priority the priority
   */
  public void setPriority(double priority) {
    this.priority = priority;
  }

  /**
   * Gets static column.
   *
   * @return the static column
   */
  public Boolean getStaticColumn() {
    return staticColumn;
  }

  /**
   * Sets static column.
   *
   * @param staticColumn the static column
   */
  public void setStaticColumn(Boolean staticColumn) {
    this.staticColumn = staticColumn;
  }

  /**
   * Gets condition time.
   *
   * @return the condition time
   */
  public String getConditionTime() {
    return conditionTime;
  }

  /**
   * Sets condition time.
   *
   * @param conditionTime the condition time
   */
  public void setConditionTime(String conditionTime) {
    this.conditionTime = conditionTime;
  }

  /**
   * Gets normalized description.
   *
   * @return the normalized description
   */
  public String getNormalizedDescription() {
    String result = this.description.trim();
    if (owner != null) {
      String defaultPrefix = '"' + owner.getName().toUpperCase() + "\".";
      if (result.startsWith(defaultPrefix)) {
        result = result.substring(defaultPrefix.length());
      }
    }
    return result;
  }

  /**
   * ignores description
   */
  @Override
  public boolean isIdentical(DBObject object) {
    if (!(object instanceof DBTrigger)) {
      return false;
    }
    DBTrigger that = (DBTrigger) object;
    return NullSafeComparator.equals(this.triggerType, that.triggerType)
        && NullSafeComparator.equals(this.triggeringEvent, that.triggeringEvent)
        && NullSafeComparator.equals(this.baseObjectType, that.baseObjectType)
        && NullSafeComparator.equals(this.tableName, that.tableName)
        && NullSafeComparator.equals(this.columnName, that.columnName)
        && NullSafeComparator.equals(this.referencingNames, that.referencingNames)
        && NullSafeComparator.equals(this.whenClause, that.whenClause)
        && NullSafeComparator.equals(this.status, that.status)
        && NullSafeComparator.equals(this.actionType, that.actionType)
        && NullSafeComparator.equals(this.triggerBody.trim(), that.triggerBody.trim());
  }

}
