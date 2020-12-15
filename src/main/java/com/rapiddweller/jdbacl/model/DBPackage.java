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

import java.util.List;

import com.rapiddweller.commons.NullSafeComparator;
import com.rapiddweller.commons.anno.Nullable;
import com.rapiddweller.commons.collection.OrderedNameMap;

/**
 * Represents a database packet which can hold {@link DBProcedure}s.<br/><br/>
 * Created: 07.11.2011 15:42:47
 * @since 0.7.0
 * @author Volker Bergmann
 */
public class DBPackage extends AbstractCompositeDBObject<DBProcedure> {
	
	private static final long serialVersionUID = 5001335810310474145L;
	
	private @Nullable String subObjectName;
	private String objectId;
	private @Nullable String dataObjectId;
	private String objectType;
	private String status;
	
	private OrderedNameMap<DBProcedure> procedures;

	public DBPackage(String name, DBSchema owner) {
		super(name, "package", owner);
		this.procedures = OrderedNameMap.createCaseIgnorantMap();
		if (owner != null)
			owner.addPackage(this);
	}
	
	public DBSchema getSchema() {
		return (DBSchema) getOwner();
	}
	
	public void setSchema(DBSchema schema) {
		setOwner(schema);
	}
	
	public String getSubObjectName() {
		return subObjectName;
	}

	public void setSubObjectName(String subObjectName) {
		this.subObjectName = subObjectName;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getDataObjectId() {
		return dataObjectId;
	}

	public void setDataObjectId(String dataObjectId) {
		this.dataObjectId = dataObjectId;
	}

	@Override
	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<DBProcedure> getProcedures() {
		return procedures.values();
	}

	public void setProcedures(OrderedNameMap<DBProcedure> procedures) {
		this.procedures = procedures;
	}

	@Override
	public List<DBProcedure> getComponents() {
		return procedures.values();
	}

	public void addProcedure(DBProcedure procedure) {
		this.procedures.put(procedure.getName(), procedure);
		procedure.setOwner(this);
	}

	@Override
	public boolean isIdentical(DBObject other) {
		if (this == other)
			return true;
		if (other == null || other.getClass() != this.getClass())
			return false;
		DBPackage that = (DBPackage) other;
		return NullSafeComparator.equals(this.subObjectName, that.subObjectName)
			&& this.objectId.equals(that.objectId)
			&& NullSafeComparator.equals(this.dataObjectId, that.dataObjectId)
			&& this.objectType.equals(that.objectType)
			&& this.status.equals(that.status);
	}
	
}
