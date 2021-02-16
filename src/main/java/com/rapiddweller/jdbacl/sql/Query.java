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

package com.rapiddweller.jdbacl.sql;

import com.rapiddweller.common.CollectionUtil;
import com.rapiddweller.jdbacl.SQLUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for constructing SQL queries.
 * General structure:
 * [selectConditions] SELECT selections
 * FROM tablesWithAliases [[LEFT | RIGHT | OUTER | INNER] JOIN joins]
 * [WHERE whereClause] [options]
 * <br/><br/>
 * Created: 09.04.2012 10:16:54
 *
 * @author Volker Bergmann
 * @since 0.8.1
 */
public class Query {

  private final List<String> selectConditions;
  private final List<String> selections;
  private final List<String> tablesWithAliases;
  private final List<String> joins;
  private final StringBuilder whereClause;
  private final List<String> options;

  /**
   * Instantiates a new Query.
   *
   * @param selection the selection
   * @param table     the table
   */
  public Query(String selection, String table) {
    this(selection, table, null);
  }

  /**
   * Instantiates a new Query.
   *
   * @param selection   the selection
   * @param table       the table
   * @param whereClause the where clause
   */
  public Query(String selection, String table, String whereClause) {
    this.selectConditions = new ArrayList<>();
    this.selections = CollectionUtil.toList(selection);
    this.tablesWithAliases = new ArrayList<>();
    this.joins = new ArrayList<>();
    if (table != null) {
      this.tablesWithAliases.add(table);
    }
    this.whereClause = new StringBuilder();
    if (whereClause != null) {
      this.whereClause.append(whereClause);
    }
    this.options = new ArrayList<>();
  }

  /**
   * Select query.
   *
   * @param selection the selection
   * @return the query
   */
  public static Query select(String selection) {
    return new Query(selection, null);
  }

  /**
   * Add select condition.
   *
   * @param selectCondition the select condition
   */
  public void addSelectCondition(String selectCondition) {
    selectConditions.add(selectCondition);
  }

  /**
   * From query.
   *
   * @param tableName the table name
   * @return the query
   */
  public Query from(String tableName) {
    return from(tableName, null);
  }

  /**
   * From query.
   *
   * @param tableName the table name
   * @param alias     the alias
   * @return the query
   */
  public Query from(String tableName, String alias) {
    if (tableName.indexOf(' ') >= 0) {
      throw new IllegalArgumentException("Tbale name must not contain spaces: '" + tableName + "'");
    }
    String term = tableName + (alias != null ? " " + alias : "");
    this.tablesWithAliases.add(term);
    return this;
  }

  /**
   * Left join query.
   *
   * @param leftAlias    the left alias
   * @param leftColumns  the left columns
   * @param rightTable   the right table
   * @param rightAlias   the right alias
   * @param rightColumns the right columns
   * @return the query
   */
  public Query leftJoin(String leftAlias, String[] leftColumns,
                        String rightTable, String rightAlias, String[] rightColumns) {
    joins.add(SQLUtil.leftJoin(leftAlias, leftColumns, rightTable, rightAlias, rightColumns));
    return this;
  }

  /**
   * Where query.
   *
   * @param where the where
   * @return the query
   */
  public Query where(String where) {
    if (this.whereClause.length() > 0) {
      throw new IllegalArgumentException("Tried to set where clause to '" + where + "' " +
          "but there already exists one: " + this.whereClause);
    }
    whereClause.append(where);
    return this;
  }

  /**
   * And.
   *
   * @param condition the condition
   */
  public void and(String condition) {
    if (whereClause.length() > 0) {
      whereClause.append(" AND ");
    }
    whereClause.append(condition);
  }

  /**
   * Add option.
   *
   * @param option the option
   */
  public void addOption(String option) {
    options.add(option);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("SELECT ");
    for (String selectCondition : selectConditions) {
      builder.append(selectCondition).append(' ');
    }
    for (int i = 0; i < selections.size(); i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(selections.get(i));
    }
    builder.append(" FROM ");
    for (int i = 0; i < tablesWithAliases.size(); i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(tablesWithAliases.get(i));
    }
    for (String join : joins) {
      builder.append(" ").append(join);
    }
    if (whereClause.length() > 0) {
      builder.append(" WHERE ").append(whereClause);
    }
    for (String option : options) {
      builder.append(' ').append(option);
    }
    return builder.toString();
  }

}
