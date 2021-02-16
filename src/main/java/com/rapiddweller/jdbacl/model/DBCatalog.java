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

package com.rapiddweller.jdbacl.model;

import com.rapiddweller.common.Named;
import com.rapiddweller.common.ObjectNotFoundException;
import com.rapiddweller.common.collection.OrderedNameMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a JDBC catalog.<br/><br/>
 * Created: 06.01.2007 08:57:57
 *
 * @author Volker Bergmann
 */
public class DBCatalog extends AbstractCompositeDBObject<DBSchema> implements Named, Serializable {

  private static final long serialVersionUID = 3956827426638393655L;

  /**
   * The Schemas.
   */
  final OrderedNameMap<DBSchema> schemas;

  // constructors ----------------------------------------------------------------------------------------------------

  /**
   * Instantiates a new Db catalog.
   */
  public DBCatalog() {
    this(null);
  }

  /**
   * Instantiates a new Db catalog.
   *
   * @param name the name
   */
  public DBCatalog(String name) {
    this(name, null);
  }

  /**
   * Instantiates a new Db catalog.
   *
   * @param name  the name
   * @param owner the owner
   */
  public DBCatalog(String name, Database owner) {
    super(name, "catalog", owner);
    if (owner != null) {
      owner.addCatalog(this);
    }
    this.schemas = OrderedNameMap.createCaseIgnorantMap();
  }

  // properties ------------------------------------------------------------------------------------------------------

  /**
   * Gets database.
   *
   * @return the database
   */
  public Database getDatabase() {
    return (Database) getOwner();
  }

  /**
   * Sets database.
   *
   * @param database the database
   */
  public void setDatabase(Database database) {
    this.owner = database;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getDoc() {
    return doc;
  }

  @Override
  public void setDoc(String doc) {
    this.doc = doc;
  }

  // CompositeDBObject implementation --------------------------------------------------------------------------------

  @Override
  public List<DBSchema> getComponents() {
    return schemas.values();
  }

  // schema operations -----------------------------------------------------------------------------------------------

  /**
   * Gets schemas.
   *
   * @return the schemas
   */
  public List<DBSchema> getSchemas() {
    return getComponents();
  }

  /**
   * Gets schema.
   *
   * @param schemaName the schema name
   * @return the schema
   */
  public DBSchema getSchema(String schemaName) {
    return schemas.get(schemaName);
  }

  /**
   * Add schema.
   *
   * @param schema the schema
   */
  public void addSchema(DBSchema schema) {
    schemas.put(schema.getName(), schema);
    schema.setOwner(this);
  }

  /**
   * Remove schema.
   *
   * @param schema the schema
   */
  public void removeSchema(DBSchema schema) {
    schemas.remove(schema.getName());
  }

  // table operations ------------------------------------------------------------------------------------------------

  /**
   * Gets tables.
   *
   * @return the tables
   */
  public List<DBTable> getTables() {
    List<DBTable> tables = new ArrayList<>();
    for (DBSchema schema : getSchemas()) {
      tables.addAll(schema.getTables());
    }
    return tables;
  }

  /**
   * Gets table.
   *
   * @param name the name
   * @return the table
   */
  public DBTable getTable(String name) {
    return getTable(name, true);
  }

  /**
   * Gets table.
   *
   * @param name     the name
   * @param required the required
   * @return the table
   */
  public DBTable getTable(String name, boolean required) {
    for (DBSchema schema : getSchemas()) {
      for (DBTable table : schema.getTables()) {
        if (table.getName().equals(name)) {
          return table;
        }
      }
    }
    if (required) {
      throw new ObjectNotFoundException("Table '" + name + "'");
    } else {
      return null;
    }
  }

  /**
   * Remove table.
   *
   * @param tableName the table name
   */
  public void removeTable(String tableName) {
    DBTable table = getTable(tableName);
    table.getSchema().removeTable(table);
  }

  /**
   * Gets sequences.
   *
   * @return the sequences
   */
  public List<DBSequence> getSequences() {
    List<DBSequence> sequences = new ArrayList<>();
    for (DBSchema schema : getSchemas()) {
      sequences.addAll(schema.getSequences(true));
    }
    return sequences;
  }

}
