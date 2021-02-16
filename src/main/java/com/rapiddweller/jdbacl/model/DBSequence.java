/*
 * (c) Copyright 2011 by Volker Bergmann. All rights reserved.
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

import java.math.BigInteger;

/**
 * Represents a database sequence.<br/><br/>
 * Created: 31.05.2011 17:55:05
 *
 * @author Volker Bergmann
 * @since 0.6.8
 */
public class DBSequence extends AbstractDBObject implements ContainerComponent {

  private static final long serialVersionUID = 8602052311285255364L;

  private String catalogName;
  private String schemaName;
  private BigInteger start = BigInteger.ONE;
  private BigInteger increment = BigInteger.ONE;
  private BigInteger maxValue = null;
  private BigInteger minValue = null;
  private Boolean cycle;
  private Long cache;
  private Boolean order;
  private BigInteger lastNumber = BigInteger.ZERO;

  /**
   * Instantiates a new Db sequence.
   *
   * @param name  the name
   * @param owner the owner
   */
  public DBSequence(String name, DBSchema owner) {
    super(name, "sequence", owner);
    if (owner != null) {
      owner.addSequence(this);
    }
  }

  /**
   * Instantiates a new Db sequence.
   *
   * @param name        the name
   * @param catalogName the catalog name
   * @param schemaName  the schema name
   */
  public DBSequence(String name, String catalogName, String schemaName) {
    super(name, "sequence", null);
    this.catalogName = catalogName;
    this.schemaName = schemaName;
  }

  @Override
  public void setOwner(CompositeDBObject<?> owner) {
    super.setOwner(owner);
    if (owner != null) {
      DBSchema schema = (DBSchema) owner;
      this.catalogName = schema.getCatalog().getName();
      this.schemaName = schema.getName();
    }
  }

  /**
   * Gets catalog name.
   *
   * @return the catalog name
   */
  public String getCatalogName() {
    return catalogName;
  }

  /**
   * Gets schema name.
   *
   * @return the schema name
   */
  public String getSchemaName() {
    return schemaName;
  }

  /**
   * Gets start.
   *
   * @return the start
   */
  public BigInteger getStart() {
    return start;
  }

  /**
   * Gets start if not default.
   *
   * @return the start if not default
   */
  public BigInteger getStartIfNotDefault() {
    return (BigInteger.ONE.equals(this.start) ? null : this.start);
  }

  /**
   * Sets start.
   *
   * @param start the start
   */
  public void setStart(BigInteger start) {
    this.start = start;
  }

  /**
   * Gets increment.
   *
   * @return the increment
   */
  public BigInteger getIncrement() {
    return increment;
  }

  /**
   * Gets increment if not default.
   *
   * @return the increment if not default
   */
  public BigInteger getIncrementIfNotDefault() {
    return (BigInteger.ONE.equals(increment) ? null : increment);
  }

  /**
   * Sets increment.
   *
   * @param increment the increment
   */
  public void setIncrement(BigInteger increment) {
    this.increment = increment;
  }

  /**
   * Gets max value.
   *
   * @return the max value
   */
  public BigInteger getMaxValue() {
    return maxValue;
  }

  /**
   * Gets max value if not default.
   *
   * @return the max value if not default
   */
  public BigInteger getMaxValueIfNotDefault() {
    return maxValue;
  }

  /**
   * Sets max value.
   *
   * @param maxValue the max value
   */
  public void setMaxValue(BigInteger maxValue) {
    this.maxValue = maxValue;
  }

  /**
   * Gets min value.
   *
   * @return the min value
   */
  public BigInteger getMinValue() {
    return minValue;
  }

  /**
   * Gets min value if not default.
   *
   * @return the min value if not default
   */
  public BigInteger getMinValueIfNotDefault() {
    return minValue;
  }

  /**
   * Sets min value.
   *
   * @param minValue the min value
   */
  public void setMinValue(BigInteger minValue) {
    this.minValue = minValue;
  }

  /**
   * Is cycle boolean.
   *
   * @return the boolean
   */
  public Boolean isCycle() {
    return cycle;
  }

  /**
   * Sets cycle.
   *
   * @param cycle the cycle
   */
  public void setCycle(Boolean cycle) {
    this.cycle = cycle;
  }

  /**
   * Gets cache.
   *
   * @return the cache
   */
  public Long getCache() {
    return cache;
  }

  /**
   * Sets cache.
   *
   * @param cache the cache
   */
  public void setCache(Long cache) {
    this.cache = cache;
  }

  /**
   * Is order boolean.
   *
   * @return the boolean
   */
  public Boolean isOrder() {
    return order;
  }

  /**
   * Sets order.
   *
   * @param order the order
   */
  public void setOrder(Boolean order) {
    this.order = order;
  }

  /**
   * Gets last number.
   *
   * @return the last number
   */
  public BigInteger getLastNumber() {
    return lastNumber;
  }

  /**
   * Sets last number.
   *
   * @param lastNumber the last number
   */
  public void setLastNumber(BigInteger lastNumber) {
    this.lastNumber = lastNumber;
  }

  @Override
  public boolean isIdentical(DBObject other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof DBSequence)) {
      return false;
    }
    DBSequence that = (DBSequence) other;
    return this.name.equals(that.getName()) &&
        this.start == that.getStart() &&
        this.increment == that.getIncrement() &&
        NullSafeComparator.equals(this.maxValue, that.getMaxValue()) &&
        NullSafeComparator.equals(this.minValue, that.getMinValue()) &&
        this.cycle == that.isCycle() &&
        this.cache == that.getCache() &&
        this.order == that.isOrder();
  }

  /**
   * Drop ddl string.
   *
   * @return the string
   */
  public String dropDDL() {
    return "drop sequence " + name;
  }

}
