/*
 * (c) Copyright 2006-2010 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.model.jdbc;

import com.rapiddweller.common.ArrayFormat;
import com.rapiddweller.common.ArrayUtil;

/**
 * Represents a database index.<br/><br/>
 * Created: 13.01.2007 23:40:52
 *
 * @author Volker Bergmann
 */
public class DBIndexInfo {

  /**
   * The Name.
   */
  public final String name;
  /**
   * The Table name.
   */
  public final String tableName;
  /**
   * The Unique.
   */
  public final boolean unique;
  /**
   * The Catalog name.
   */
  public final String catalogName;
  /**
   * The Type.
   */
  public final short type;
  /**
   * The Ascending.
   */
  public final Boolean ascending;
  /**
   * The Cardinality.
   */
  public final int cardinality;
  /**
   * The Pages.
   */
  public final int pages;
  /**
   * The Filter condition.
   */
  public final String filterCondition;

  /**
   * The Column names.
   */
  public String[] columnNames;

  /**
   * Instantiates a new Db index info.
   *
   * @param name            the name
   * @param tableName       the table name
   * @param type            the type
   * @param catalogName     the catalog name
   * @param unique          the unique
   * @param ordinalPosition the ordinal position
   * @param columnName      the column name
   * @param ascending       the ascending
   * @param cardinality     the cardinality
   * @param pages           the pages
   * @param filterCondition the filter condition
   */
  public DBIndexInfo(String name, String tableName, short type, String catalogName, boolean unique, short ordinalPosition, String columnName,
                     Boolean ascending, int cardinality, int pages, String filterCondition) {
    this.name = name;
    this.tableName = tableName;
    this.unique = unique;
    this.catalogName = catalogName;
    this.type = type;
    this.ascending = ascending;
    this.cardinality = cardinality;
    this.pages = pages;
    this.filterCondition = filterCondition;
    this.columnNames = new String[] {columnName};
    if (ordinalPosition != 1) {
      throw new IllegalArgumentException("ordinalPosition is expected to be 1, found: " + ordinalPosition);
    }
  }

  /**
   * Add column.
   *
   * @param ordinalPosition the ordinal position
   * @param columnName      the column name
   */
  public void addColumn(short ordinalPosition, String columnName) {
    int expectedPosition = columnNames.length + 1;
    if (ordinalPosition == expectedPosition) {
      columnNames = ArrayUtil.append(columnName, columnNames);
    } else {
      if (ordinalPosition > expectedPosition || !columnNames[ordinalPosition - 1].equals(columnName)) {
        throw new IllegalArgumentException("ordinalPosition is expected to be " + expectedPosition + ", " +
            "found: " + ordinalPosition);
      }
    }
  }

  @Override
  public String toString() {
    return "[" + ArrayFormat.format(columnNames) + "]";
  }

}
