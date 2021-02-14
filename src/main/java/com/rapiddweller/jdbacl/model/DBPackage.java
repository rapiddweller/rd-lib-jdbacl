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

import com.rapiddweller.common.NullSafeComparator;
import com.rapiddweller.common.anno.Nullable;
import com.rapiddweller.common.collection.OrderedNameMap;

import java.util.List;

/**
 * Represents a database packet which can hold {@link DBProcedure}s.<br/><br/>
 * Created: 07.11.2011 15:42:47
 *
 * @author Volker Bergmann
 * @since 0.7.0
 */
public class DBPackage extends AbstractCompositeDBObject<DBProcedure> {

  private static final long serialVersionUID = 5001335810310474145L;

  private @Nullable
  String subObjectName;
  private String objectId;
  private @Nullable
  String dataObjectId;
  private String objectType;
  private String status;

  private OrderedNameMap<DBProcedure> procedures;

  /**
   * Instantiates a new Db package.
   *
   * @param name  the name
   * @param owner the owner
   */
  public DBPackage(String name, DBSchema owner) {
    super(name, "package", owner);
    this.procedures = OrderedNameMap.createCaseIgnorantMap();
    if (owner != null) {
      owner.addPackage(this);
    }
  }

  /**
   * Gets schema.
   *
   * @return the schema
   */
  public DBSchema getSchema() {
    return (DBSchema) getOwner();
  }

  /**
   * Sets schema.
   *
   * @param schema the schema
   */
  public void setSchema(DBSchema schema) {
    setOwner(schema);
  }

  /**
   * Gets sub object name.
   *
   * @return the sub object name
   */
  public String getSubObjectName() {
    return subObjectName;
  }

  /**
   * Sets sub object name.
   *
   * @param subObjectName the sub object name
   */
  public void setSubObjectName(String subObjectName) {
    this.subObjectName = subObjectName;
  }

  /**
   * Gets object id.
   *
   * @return the object id
   */
  public String getObjectId() {
    return objectId;
  }

  /**
   * Sets object id.
   *
   * @param objectId the object id
   */
  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  /**
   * Gets data object id.
   *
   * @return the data object id
   */
  public String getDataObjectId() {
    return dataObjectId;
  }

  /**
   * Sets data object id.
   *
   * @param dataObjectId the data object id
   */
  public void setDataObjectId(String dataObjectId) {
    this.dataObjectId = dataObjectId;
  }

  @Override
  public String getObjectType() {
    return objectType;
  }

  /**
   * Sets object type.
   *
   * @param objectType the object type
   */
  public void setObjectType(String objectType) {
    this.objectType = objectType;
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
   * Gets procedures.
   *
   * @return the procedures
   */
  public List<DBProcedure> getProcedures() {
    return procedures.values();
  }

  /**
   * Sets procedures.
   *
   * @param procedures the procedures
   */
  public void setProcedures(OrderedNameMap<DBProcedure> procedures) {
    this.procedures = procedures;
  }

  @Override
  public List<DBProcedure> getComponents() {
    return procedures.values();
  }

  /**
   * Add procedure.
   *
   * @param procedure the procedure
   */
  public void addProcedure(DBProcedure procedure) {
    this.procedures.put(procedure.getName(), procedure);
    procedure.setOwner(this);
  }

  @Override
  public boolean isIdentical(DBObject other) {
    if (this == other) {
      return true;
    }
    if (other == null || other.getClass() != this.getClass()) {
      return false;
    }
    DBPackage that = (DBPackage) other;
    return NullSafeComparator.equals(this.subObjectName, that.subObjectName)
        && this.objectId.equals(that.objectId)
        && NullSafeComparator.equals(this.dataObjectId, that.dataObjectId)
        && this.objectType.equals(that.objectType)
        && this.status.equals(that.status);
  }

}
