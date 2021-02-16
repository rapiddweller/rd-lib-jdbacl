/*
 * (c) Copyright 2007-2010 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.common.ArrayUtil;
import com.rapiddweller.common.NullSafeComparator;
import com.rapiddweller.common.bean.HashCodeBuilder;
import com.rapiddweller.jdbacl.SQLUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a database column.<br/><br/>
 * Created: 06.01.2007 08:58:49
 *
 * @author Volker Bergmann
 */
public class DBColumn extends AbstractDBTableComponent {

  private static final long serialVersionUID = 130665821777405940L;

  /**
   * The Type.
   */
  protected DBDataType type;
  /**
   * The Size.
   */
  protected Integer size;
  /**
   * The Fraction digits.
   */
  protected Integer fractionDigits;
  /**
   * The Default value.
   */
  protected String defaultValue;
  /**
   * The Version column.
   */
  protected boolean versionColumn;

  /**
   * The Uk constraints.
   */
  protected final List<DBUniqueConstraint> ukConstraints; // constraints may be unnamed, so a Map does not make sense
  /**
   * The Not null constraint.
   */
  protected DBNotNullConstraint notNullConstraint;

  // constructors ----------------------------------------------------------------------------------------------------

  /**
   * Instantiates a new Db column.
   *
   * @param name  the name
   * @param table the table
   * @param type  the type
   */
  public DBColumn(String name, DBTable table, DBDataType type) {
    this(name, table, type, null);
  }

  /**
   * Instantiates a new Db column.
   *
   * @param name        the name
   * @param table       the table
   * @param jdbcType    the jdbc type
   * @param typeAndSize the type and size
   */
  public DBColumn(String name, DBTable table, int jdbcType, String typeAndSize) {
    this(name, table, null, null);
    Object[] tokens = SQLUtil.parseColumnTypeAndSize(typeAndSize);
    if (tokens.length > 0) {
      this.type = DBDataType.getInstance(jdbcType, (String) tokens[0]);
    }
    if (tokens.length > 1) {
      this.size = (Integer) tokens[1];
    }
    if (tokens.length > 2) {
      this.fractionDigits = (Integer) tokens[2];
    }
  }

  /**
   * Instantiates a new Db column.
   *
   * @param name  the name
   * @param table the table
   * @param type  the type
   * @param size  the size
   */
  public DBColumn(String name, DBTable table, DBDataType type, Integer size) {
    this(name, table, type, size, null);
  }

  /**
   * Instantiates a new Db column.
   *
   * @param name           the name
   * @param table          the table
   * @param type           the type
   * @param size           the size
   * @param fractionDigits the fraction digits
   */
  public DBColumn(String name, DBTable table, DBDataType type, Integer size, Integer fractionDigits) {
    super(name, "column");
    if (table != null) {
      table.receiveColumn(this);
    }
    this.name = name;
    this.type = type;
    this.size = size;
    this.fractionDigits = fractionDigits;
    this.doc = null;
    this.defaultValue = null;
    this.ukConstraints = new ArrayList<>();
    this.notNullConstraint = null;
    this.versionColumn = false;
  }

  // properties ------------------------------------------------------------------------------------------------------

  /**
   * Gets type.
   *
   * @return the type
   */
  public DBDataType getType() {
    return type;
  }

  /**
   * Sets type.
   *
   * @param type the type
   */
  public void setType(DBDataType type) {
    this.type = type;
  }

  /**
   * Gets size.
   *
   * @return the size
   */
  public Integer getSize() {
    return size;
  }

  /**
   * Sets size.
   *
   * @param size the size
   */
  public void setSize(Integer size) {
    this.size = size;
  }

  /**
   * Gets fraction digits.
   *
   * @return the fraction digits
   */
  public Integer getFractionDigits() {
    return fractionDigits;
  }

  /**
   * Sets fraction digits.
   *
   * @param fractionDigits the fraction digits
   */
  public void setFractionDigits(Integer fractionDigits) {
    this.fractionDigits = fractionDigits;
  }

  /**
   * Gets default value.
   *
   * @return the default value
   */
  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * Sets default value.
   *
   * @param defaultValue the default value
   */
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  /**
   * Is unique boolean.
   *
   * @return the boolean
   */
  public boolean isUnique() {
    getTable().getUniqueConstraints(true); // make sure lazy data is fetched before
    for (DBUniqueConstraint constraint : ukConstraints) {
      if (constraint.getColumnNames().length == 1) {
        return true;
      }
    }
    return false;
  }

  /**
   * Is pk component boolean.
   *
   * @return the boolean
   */
  public boolean isPKComponent() {
    for (String candidate : getTable().getPKColumnNames()) {
      if (name.equals(candidate)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets uk constraints.
   *
   * @return the uk constraints
   */
  public List<DBUniqueConstraint> getUkConstraints() {
    return ukConstraints;
  }

  /**
   * Add uk constraint.
   *
   * @param constraint the constraint
   */
  public void addUkConstraint(DBUniqueConstraint constraint) {
    this.ukConstraints.add(constraint);
  }

  /**
   * Gets not null constraint.
   *
   * @return the not null constraint
   */
  public DBNotNullConstraint getNotNullConstraint() {
    return notNullConstraint;
  }

  /**
   * Sets not null constraint.
   *
   * @param notNullConstraint the not null constraint
   */
  public void setNotNullConstraint(DBNotNullConstraint notNullConstraint) {
    this.notNullConstraint = notNullConstraint;
  }

  /**
   * Is nullable boolean.
   *
   * @return the boolean
   */
  public boolean isNullable() {
    return (notNullConstraint == null);
  }

  /**
   * Sets nullable.
   *
   * @param nullable the nullable
   */
  public void setNullable(boolean nullable) {
    if (nullable) {
      // if a NotNullConstraint exists then remove it
      notNullConstraint = null;
    } else {
      // if there needs to be a NotNullConstraint, check if there exists one, first
      if (this.isNullable()) {
        String constraintName = (getTable() != null ? getTable().getName() : "_") + '_' + name + "_NOT_NULL"; // TODO v1.0 get constraint name from DB
        this.notNullConstraint = new DBNotNullConstraint(getTable(), constraintName, true, name);
      }
    }
  }

  /**
   * Is version column boolean.
   *
   * @return the boolean
   */
  public boolean isVersionColumn() {
    return versionColumn;
  }

  /**
   * Sets version column.
   *
   * @param versionColumn the version column
   */
  public void setVersionColumn(boolean versionColumn) {
    this.versionColumn = versionColumn;
  }

  /**
   * Is integer type boolean.
   *
   * @return the boolean
   */
  public boolean isIntegerType() {
    return type.isInteger() || (type.isDecimal() && (fractionDigits == null || fractionDigits == 0));
  }

  /**
   * Gets foreign key constraint.
   *
   * @return the foreign key constraint
   */
  public DBForeignKeyConstraint getForeignKeyConstraint() {
    for (DBForeignKeyConstraint fk : getTable().getForeignKeyConstraints()) {
      if (ArrayUtil.contains(name, fk.getColumnNames())) {
        return fk;
      }
    }
    return null;
  }

  // java.lang.Object overrides --------------------------------------------------------------------------------------

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !DBColumn.class.isAssignableFrom(obj.getClass())) {
      return false;
    }
    DBColumn that = (DBColumn) obj;
    return NullSafeComparator.equals(this.name, that.getName())
        && this.type.equals(that.getType())
        && NullSafeComparator.equals(this.size, that.getSize())
        && NullSafeComparator.equals(this.fractionDigits, that.getFractionDigits())
        && NullSafeComparator.equals(this.defaultValue, that.getDefaultValue())
        && this.versionColumn == that.isVersionColumn()
        && this.ukConstraints.equals(that.getUkConstraints())
        && NullSafeComparator.equals(notNullConstraint, that.getNotNullConstraint());
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.hashCode(name, type, size, fractionDigits, defaultValue, versionColumn,
        ukConstraints, notNullConstraint/*, fkConstraint*/);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(name).append(" : ");
    SQLUtil.renderColumnTypeWithSize(this, builder);
    if (!isNullable()) {
      builder.append(" NOT NULL");
    }
    return builder.toString();
  }

  @Override
  public boolean isIdentical(DBObject other) {
    if (this == other) {
      return true;
    }
    if (other == null || !(other instanceof DBColumn)) {
      return false;
    }
    return this.name.equals(other.getName()) && isEquivalent(other);
  }

  /**
   * Is equivalent boolean.
   *
   * @param other the other
   * @return the boolean
   */
  public boolean isEquivalent(DBObject other) {
    if (this == other) {
      return true;
    }
    if (other == null || !(other instanceof DBColumn)) {
      return false;
    }
    DBColumn that = (DBColumn) other;
    return this.type.equals(that.getType())
        && NullSafeComparator.equals(this.size, that.getSize())
        && NullSafeComparator.equals(this.fractionDigits, that.getFractionDigits());
  }

}
