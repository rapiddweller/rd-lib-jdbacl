/*
 * (c) Copyright 2006-2011 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.common.ArrayUtil;
import com.rapiddweller.common.Assert;
import com.rapiddweller.common.NullSafeComparator;
import com.rapiddweller.common.ObjectNotFoundException;
import com.rapiddweller.common.bean.HashCodeBuilder;
import com.rapiddweller.jdbacl.NameSpec;
import com.rapiddweller.jdbacl.SQLUtil;

import java.util.Arrays;

/**
 * Represents a foreign key constraint.<br/><br/>
 * Created: 06.01.2007 09:00:59
 * @author Volker Bergmann
 */
public class DBForeignKeyConstraint extends DBConstraint implements MultiColumnObject {

    private static final long serialVersionUID = -7488054587082654132L;
    
    private final String[] fkColumnNames;
    
    private final DBTable refereeTable;
    private final String[] refereeColumnNames;
    private FKChangeRule updateRule;
    private FKChangeRule deleteRule;
    
    public DBForeignKeyConstraint(String name, boolean nameDeterministic, DBTable owner, String fkColumnName, 
    		DBTable refereeTable, String refereeColumnName) {
        this(name, nameDeterministic, owner, new String[] { fkColumnName }, 
        		refereeTable, new String[] { refereeColumnName });
    }

    public DBForeignKeyConstraint(String name, boolean nameDeterministic, DBTable owner, String[] fkColumnNames, 
    		DBTable refereeTable, String[] refereeColumnNames) {
        super(name, nameDeterministic, "foreign key constraint", owner);
        Assert.notNull(refereeTable, "refereeTable");
        this.fkColumnNames = fkColumnNames;
        this.refereeTable = refereeTable;
        this.refereeColumnNames = refereeColumnNames;
        this.updateRule = FKChangeRule.NO_ACTION;
        this.deleteRule = FKChangeRule.NO_ACTION;
        if (owner != null)
        	owner.addForeignKey(this);
    }

    public String[] getForeignKeyColumnNames() {
        return fkColumnNames;
    }

    public String columnReferencedBy(String fkColumnName) {
    	return columnReferencedBy(fkColumnName, true);
    }

    public String columnReferencedBy(String fkColumnName, boolean required) {
    	int index = ArrayUtil.indexOf(fkColumnName, fkColumnNames);
    	if (index < 0) {
    		if (required)
    			throw new ObjectNotFoundException("foreign key '" + name + "' does not have a column '" + fkColumnName + "'");
    		else
    			return null;
    	}
    	return refereeColumnNames[index];
    }

    public DBTable getRefereeTable() {
        return refereeTable;
    }

    @Override
    public String[] getColumnNames() {
    	return fkColumnNames;
    }

	public String[] getRefereeColumnNames() {
		return refereeColumnNames;
    }
    
	@Override
	public boolean isIdentical(DBObject other) {
		if (this == other)
			return true;
		if (other == null || !(other instanceof DBForeignKeyConstraint))
			return false;
		DBForeignKeyConstraint that = (DBForeignKeyConstraint) other;
		return NullSafeComparator.equals(this.name, that.name)
			&& Arrays.equals(fkColumnNames, that.fkColumnNames)
			&& Arrays.equals(refereeColumnNames, that.refereeColumnNames)
			&& NullSafeComparator.equals(refereeTable.getName(), that.refereeTable.getName());
	}

	public FKChangeRule getUpdateRule() {
		return updateRule;
	}
	
	public void setUpdateRule(FKChangeRule updateRule) {
		this.updateRule = updateRule;
	}
	
	public FKChangeRule getDeleteRule() {
		return deleteRule;
	}
	
	public void setDeleteRule(FKChangeRule deleteRule) {
		this.deleteRule = deleteRule;
	}
	
	
	
	// java.lang.Object overrides --------------------------------------------------------------------------------------
	
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null || getClass() != other.getClass())
			return false;
		DBForeignKeyConstraint that = (DBForeignKeyConstraint) other;
		return this.isIdentical(that) && NullSafeComparator.equals(refereeTable, that.refereeTable);
	}

    @Override
	public int hashCode() {
		return HashCodeBuilder.hashCode(
				super.hashCode(), 
				Arrays.hashCode(fkColumnNames), 
				Arrays.hashCode(refereeColumnNames), 
				refereeTable.hashCode());
	}

	@Override
    public String toString() {
		return SQLUtil.fkSpec(this, NameSpec.ALWAYS);
    }

}
