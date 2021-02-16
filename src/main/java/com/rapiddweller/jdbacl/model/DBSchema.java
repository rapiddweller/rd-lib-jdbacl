/*
 * (c) Copyright 2006-2014 by Volker Bergmann. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted under the terms of the
 * GNU General License.
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

import com.rapiddweller.common.collection.OrderedNameMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a JDBC database schema.<br/><br/>
 * Created: 06.01.2007 08:57:57
 *
 * @author Volker Bergmann
 */
public class DBSchema extends AbstractCompositeDBObject<DBObject> implements TableHolder, SequenceHolder, Serializable {

  private static final long serialVersionUID = -8481832064630225273L;

  private final List<DBObject> components;
  private final OrderedNameMap<DBTable> tables;
  private final OrderedNameMap<DBSequence> sequences;
  private final OrderedNameMap<DBTrigger> triggers;
  private final OrderedNameMap<DBPackage> packages;

  // constructors ----------------------------------------------------------------------------------------------------

  /**
   * Instantiates a new Db schema.
   *
   * @param name the name
   */
  public DBSchema(String name) {
    this(name, null);
  }

  /**
   * Instantiates a new Db schema.
   *
   * @param name    the name
   * @param catalog the catalog
   */
  public DBSchema(String name, DBCatalog catalog) {
    super(name, "schema");
    if (catalog != null) {
      catalog.addSchema(this);
    }
    this.components = new ArrayList<>();
    this.tables = OrderedNameMap.createCaseIgnorantMap();
    this.sequences = OrderedNameMap.createCaseIgnorantMap();
    this.triggers = OrderedNameMap.createCaseIgnorantMap();
    this.packages = OrderedNameMap.createCaseIgnorantMap();
  }

  // properties ------------------------------------------------------------------------------------------------------

  @Override
  public String getName() {
    return name;
  }

  /**
   * Gets database.
   *
   * @return the database
   */
  public Database getDatabase() {
    return getCatalog().getDatabase();
  }

  // catalog operations ----------------------------------------------------------------------------------------------

  /**
   * Gets catalog.
   *
   * @return the catalog
   */
  public DBCatalog getCatalog() {
    return (DBCatalog) owner;
  }

  /**
   * Sets catalog.
   *
   * @param catalog the catalog
   */
  public void setCatalog(DBCatalog catalog) {
    this.owner = catalog;
  }

  // CompositeDBObject implementation --------------------------------------------------------------------------------

  @Override
  public List<DBObject> getComponents() {
    return components;
  }

  // table operations ------------------------------------------------------------------------------------------------

  @Override
  public List<DBTable> getTables() {
    return tables.values();
  }

  @Override
  public List<DBTable> getTables(boolean recursive) {
    return getTables();
  }

  @Override
  public DBTable getTable(String tableName) {
    if (tableName != null) {
      return tables.get(tableName);
    } else {
      return null;
    }
  }

  /**
   * Add table.
   *
   * @param table the table
   */
  public void addTable(DBTable table) {
    tables.put(table.getName(), table);
    components.add(table);
  }

  /**
   * Remove table.
   *
   * @param table the table
   */
  public void removeTable(DBTable table) {
    tables.remove(table.getName());
    components.remove(table);
  }

  // sequence operations ---------------------------------------------------------------------------------------------

  @Override
  public List<DBSequence> getSequences(boolean recursive) {
    getDatabase().haveSequencesImported();
    return sequences.values();
  }

  /**
   * Add sequence.
   *
   * @param sequence the sequence
   */
  public void addSequence(DBSequence sequence) {
    getDatabase().haveSequencesImported();
    receiveSequence(sequence);
  }

  /**
   * Receive sequence.
   *
   * @param sequence the sequence
   */
  public void receiveSequence(DBSequence sequence) {
    this.sequences.put(sequence.getName(), sequence);
    components.add(sequence);
  }

  // trigger operations ----------------------------------------------------------------------------------------------

  /**
   * Gets triggers.
   *
   * @return the triggers
   */
  public List<DBTrigger> getTriggers() {
    getDatabase().haveTriggersImported();
    return triggers.values();
  }

  /**
   * Add trigger.
   *
   * @param trigger the trigger
   */
  public void addTrigger(DBTrigger trigger) {
    getDatabase().haveTriggersImported();
    receiveTrigger(trigger);
  }

  /**
   * Receive trigger.
   *
   * @param trigger the trigger
   */
  public void receiveTrigger(DBTrigger trigger) {
    this.triggers.put(trigger.getName(), trigger);
    components.add(trigger);
  }

  // package operations ----------------------------------------------------------------------------------------------

  /**
   * Gets packages.
   *
   * @return the packages
   */
  public List<DBPackage> getPackages() {
    getDatabase().havePackagesImported();
    return packages.values();
  }

  /**
   * Add package.
   *
   * @param pkg the pkg
   */
  public void addPackage(DBPackage pkg) {
    getDatabase().havePackagesImported();
    receivePackage(pkg);
  }

  /**
   * Receive package.
   *
   * @param pkg the pkg
   */
  public void receivePackage(DBPackage pkg) {
    packages.put(pkg.getName(), pkg);
    components.add(pkg);
  }

}
