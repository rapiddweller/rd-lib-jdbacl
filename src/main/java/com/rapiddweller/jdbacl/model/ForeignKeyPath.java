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

package com.rapiddweller.jdbacl.model;

import com.rapiddweller.common.ArrayFormat;
import com.rapiddweller.common.ArrayUtil;
import com.rapiddweller.common.CollectionUtil;
import com.rapiddweller.common.ObjectNotFoundException;
import com.rapiddweller.common.StringUtil;
import com.rapiddweller.jdbacl.SQLUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a sequence of consecutively navigable foreign key references.<br/><br/>
 * Created: 22.03.2012 21:00:34
 *
 * @author Volker Bergmann
 * @since 0.8.1
 */
public class ForeignKeyPath {

  private String startTable;
  private final List<DBForeignKeyConstraint> edges;

  /**
   * Instantiates a new Foreign key path.
   *
   * @param edges the edges
   */
  public ForeignKeyPath(DBForeignKeyConstraint... edges) {
    this.startTable = (ArrayUtil.isEmpty(edges) ? null : edges[0].getTable().getName());
    this.edges = CollectionUtil.toList(edges);
  }

  /**
   * Instantiates a new Foreign key path.
   *
   * @param startTable the start table
   */
  public ForeignKeyPath(String startTable) {
    this.startTable = startTable;
    this.edges = new ArrayList<>();
  }

  /**
   * Instantiates a new Foreign key path.
   *
   * @param prototype the prototype
   */
  public ForeignKeyPath(ForeignKeyPath prototype) {
    this.startTable = prototype.startTable;
    this.edges = new ArrayList<>(prototype.edges);
  }

  /**
   * Gets start table.
   *
   * @return the start table
   */
  public String getStartTable() {
    return startTable;
  }

  /**
   * Gets edges.
   *
   * @return the edges
   */
  public List<DBForeignKeyConstraint> getEdges() {
    return edges;
  }

  /**
   * Add edge.
   *
   * @param fk the fk
   */
  public void addEdge(DBForeignKeyConstraint fk) {
    if (edges.size() == 0) {
      if (startTable == null) {
        startTable = fk.getTable().getName();
      } else if (!startTable.equals(fk.getTable().getName())) {
        throw new IllegalArgumentException("Expected reference from " + startTable + ", " +
            "but found one to " + fk.getTable());
      }
    }
    edges.add(fk);
  }

  /**
   * Gets target table.
   *
   * @return the target table
   */
  public String getTargetTable() {
    if (edges.size() > 0) {
      return edges.get(edges.size() - 1).getRefereeTable().getName();
    } else {
      return startTable;
    }
  }

  /**
   * Derive path foreign key path.
   *
   * @param fk the fk
   * @return the foreign key path
   */
  public ForeignKeyPath derivePath(DBForeignKeyConstraint fk) {
    ForeignKeyPath result = new ForeignKeyPath(this);
    result.addEdge(fk);
    return result;
  }

  /**
   * Gets intermediates.
   *
   * @return the intermediates
   */
  public List<DBTable> getIntermediates() {
    List<DBTable> result = new ArrayList<>(edges.size() - 1);
    for (int i = 0; i < edges.size() - 1; i++) {
      result.add(edges.get(i).getRefereeTable());
    }
    return result;
  }

  /**
   * Has intermediate boolean.
   *
   * @param intermediate the intermediate
   * @return the boolean
   */
  public boolean hasIntermediate(DBTable intermediate) {
    for (int i = 0; i < edges.size() - 1; i++) {
      if (edges.get(i).getRefereeTable().equals(intermediate)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get end column names string [ ].
   *
   * @return the string [ ]
   */
  public String[] getEndColumnNames() {
    return CollectionUtil.lastElement(edges).getRefereeColumnNames();
  }

  /**
   * Parse foreign key path.
   *
   * @param spec     the spec
   * @param database the database
   * @return the foreign key path
   */
  public static ForeignKeyPath parse(String spec, Database database) {
    String[] nodes = spec.split(" \\->");
    ForeignKeyPath path = new ForeignKeyPath();
    for (int i = 0; i < nodes.length - 1; i++) {
      path.addEdge(parseFK(nodes[i], database));
    }
    return path;
  }

  private static DBForeignKeyConstraint parseFK(String spec, Database database) {
    spec = spec.trim();
    int iBracket = spec.indexOf('(');
    String tableName = spec.substring(0, iBracket);
    String columnList = spec.substring(iBracket + 1, spec.length() - 1);
    String[] columns = StringUtil.splitAndTrim(columnList, ',');
    DBTable refererTable = database.getTable(tableName, true);
    DBForeignKeyConstraint fk = refererTable.getForeignKeyConstraint(columns);
    if (fk == null) {
      throw new ObjectNotFoundException("Foreign ke constraint not found: " + tableName +
          '(' + ArrayFormat.format(columns) + ')');
    }
    return fk;
  }

  /**
   * Gets table path.
   *
   * @return the table path
   */
  public String getTablePath() {
    StringBuilder builder = new StringBuilder();
    for (DBForeignKeyConstraint edge : edges) {
      builder.append(edge.getTable().getName()).append(", ");
    }
    if (edges.size() > 0) {
      DBForeignKeyConstraint endEdge = CollectionUtil.lastElement(edges);
      builder.append(endEdge.getRefereeTable().getName());
    }
    return builder.toString();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (DBForeignKeyConstraint edge : edges) {
      builder.append(edge.getTable().getName())
          .append(SQLUtil.renderColumnNames(edge.getColumnNames()))
          .append(" -> ");
    }
    if (edges.size() > 0) {
      DBForeignKeyConstraint endEdge = CollectionUtil.lastElement(edges);
      builder.append(endEdge.getRefereeTable().getName()).append(SQLUtil.renderColumnNames(endEdge.getRefereeColumnNames()));
    }
    return builder.toString();
  }

}
