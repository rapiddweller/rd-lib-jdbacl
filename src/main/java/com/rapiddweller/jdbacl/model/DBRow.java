/*
 * (c) Copyright 2010 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.common.Assert;
import com.rapiddweller.common.collection.OrderedNameMap;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a row in a database table.<br/><br/>
 * Created: 23.07.2010 07:29:14
 *
 * @author Volker Bergmann
 * @since 0.6.3
 */
public class DBRow implements Serializable {

  private static final long serialVersionUID = 644247555736773166L;

  /**
   * The Table.
   */
  DBTable table;
  /**
   * The Cells.
   */
  final OrderedNameMap<Object> cells;

  /**
   * Instantiates a new Db row.
   *
   * @param table the table
   */
  public DBRow(DBTable table) {
    this.table = table;
    this.cells = OrderedNameMap.createCaseIgnorantMap();
  }

  /**
   * Gets table.
   *
   * @return the table
   */
  public DBTable getTable() {
    return table;
  }

  /**
   * With table db row.
   *
   * @param table the table
   * @return the db row
   */
  public DBRow withTable(DBTable table) {
    this.table = table;
    return this;
  }

  /**
   * Gets cells.
   *
   * @return the cells
   */
  public Map<String, Object> getCells() {
    return cells;
  }

  /**
   * Get pk values object [ ].
   *
   * @return the object [ ]
   */
  public Object[] getPKValues() {
    return getCellValues(table.getPKColumnNames());
  }

  /**
   * Gets pk value.
   *
   * @return the pk value
   */
  public Object getPKValue() {
    String[] columnNames = table.getPKColumnNames();
    if (columnNames.length == 1) {
      return getCellValue(columnNames[0]);
    } else {
      return getCellValues(columnNames);
    }
  }

  /**
   * Gets fk value.
   *
   * @param fk the fk
   * @return the fk value
   */
  public Object getFKValue(DBForeignKeyConstraint fk) {
    String[] columnNames = fk.getColumnNames();
    if (columnNames.length == 1) {
      return getCellValue(columnNames[0]);
    } else {
      return getCellValues(columnNames);
    }
  }

  /**
   * Sets fk value.
   *
   * @param fkConstraint the fk constraint
   * @param fkValue      the fk value
   */
  public void setFKValue(DBForeignKeyConstraint fkConstraint, Object fkValue) {
    String[] columnNames = fkConstraint.getColumnNames();
    if (columnNames.length == 1) {
      setCellValue(columnNames[0], fkValue);
    } else {
      Object[] cellValues = (Object[]) fkValue;
      setCellValues(columnNames, cellValues);
    }
  }

  /**
   * Get fk components object [ ].
   *
   * @param fk the fk
   * @return the object [ ]
   */
  public Object[] getFKComponents(DBForeignKeyConstraint fk) {
    return getCellValues(fk.getColumnNames());
  }

  /**
   * Sets cell values.
   *
   * @param columnNames the column names
   * @param cellValues  the cell values
   */
  public void setCellValues(String[] columnNames, Object[] cellValues) {
    Assert.equals(columnNames.length, cellValues.length, "mismatch of column and value counts");
    for (int i = 0; i < columnNames.length; i++) {
      setCellValue(columnNames[i], cellValues[i]);
    }
  }

  private Object[] getCellValues(String[] columnNames) {
    Object[] result = new Object[columnNames.length];
    for (int i = 0; i < columnNames.length; i++) {
      result[i] = cells.get(columnNames[i]);
    }
    return result;
  }

  /**
   * Gets cell value.
   *
   * @param columnName the column name
   * @return the cell value
   */
  public Object getCellValue(String columnName) {
    return cells.get(columnName);
  }

  /**
   * Sets cell value.
   *
   * @param columnName the column name
   * @param value      the value
   */
  public void setCellValue(String columnName, Object value) {
    cells.put(columnName, value);
  }

  @Override
  public String toString() {
    return table.getName() + cells.values();
  }

  /**
   * Sets pk value.
   *
   * @param newPK the new pk
   */
  public void setPKValue(Object newPK) {
    String[] columnNames = table.getPKColumnNames();
    if (columnNames.length == 1) {
      setCellValue(columnNames[0], newPK);
    } else {
      setCellValues(columnNames, (Object[]) newPK);
    }
  }

}
