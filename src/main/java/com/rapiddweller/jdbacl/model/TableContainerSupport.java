/*
 * (c) Copyright 2011-2014 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.common.collection.OrderedNameMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helper implementation for {@link TableHolder} and {@link SequenceHolder}.<br/><br/>
 * Created: 30.05.2011 09:34:30
 *
 * @author Volker Bergmann
 * @since 0.6.8
 */
public class TableContainerSupport implements TableHolder, SequenceHolder {

  private final OrderedNameMap<TableContainer> subContainers;
  private final OrderedNameMap<DBTable> tables;
  private final OrderedNameMap<DBSequence> sequences;

  /**
   * Instantiates a new Table container support.
   */
  public TableContainerSupport() {
    this.subContainers = OrderedNameMap.createCaseIgnorantMap();
    this.tables = OrderedNameMap.createCaseIgnorantMap();
    this.sequences = OrderedNameMap.createCaseIgnorantMap();
  }

  // sub container operations ----------------------------------------------------------------------------------------

  /**
   * Add sub container.
   *
   * @param subContainer the sub container
   */
  public void addSubContainer(TableContainer subContainer) {
    subContainers.put(subContainer.getName(), subContainer);
  }

  /**
   * Gets sub containers.
   *
   * @return the sub containers
   */
  public Collection<TableContainer> getSubContainers() {
    return subContainers.values();
  }

  // table operations ------------------------------------------------------------------------------------------------

  @Override
  public List<DBTable> getTables() {
    return getTables(false);
  }

  @Override
  public List<DBTable> getTables(boolean recursive) {
    return getTables(recursive, new ArrayList<>());
  }

  /**
   * Gets tables.
   *
   * @param recursive the recursive
   * @param result    the result
   * @return the tables
   */
  public List<DBTable> getTables(boolean recursive, List<DBTable> result) {
    result.addAll(tables.values());
    if (recursive) {
      for (TableContainer subContainer : subContainers.values()) {
        subContainer.getTables(recursive, result);
      }
    }
    return result;
  }

  @Override
  public DBTable getTable(String tableName) {
    return tables.get(tableName);
  }

  /**
   * Add table.
   *
   * @param table the table
   */
  public void addTable(DBTable table) {
    tables.put(table.getName(), table);
  }

  /**
   * Remove table.
   *
   * @param table the table
   */
  public void removeTable(DBTable table) {
    tables.remove(table.getName());
  }

  // sequence operations ---------------------------------------------------------------------------------------------

  /**
   * Add sequence.
   *
   * @param sequence the sequence
   */
  public void addSequence(DBSequence sequence) {
    this.sequences.put(sequence.getName(), sequence);
  }

  @Override
  public List<DBSequence> getSequences(boolean recursive) {
    return getSequences(recursive, new ArrayList<>());
  }

  /**
   * Gets sequences.
   *
   * @param recursive the recursive
   * @param result    the result
   * @return the sequences
   */
  public List<DBSequence> getSequences(boolean recursive, List<DBSequence> result) {
    result.addAll(sequences.values());
    if (recursive) {
      for (TableContainer subContainer : subContainers.values()) {
        subContainer.getSequences(recursive, result);
      }
    }
    return result;
  }

  /**
   * Gets components.
   *
   * @return the components
   */
  public List<ContainerComponent> getComponents() {
    List<ContainerComponent> result = new ArrayList<>();
    result.addAll(getTables(false));
    result.addAll(getSubContainers());
    result.addAll(getSequences(false));
    return result;
  }

}
