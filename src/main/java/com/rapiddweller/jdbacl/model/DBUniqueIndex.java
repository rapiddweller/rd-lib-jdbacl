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

import java.util.Arrays;

import com.rapiddweller.common.NullSafeComparator;

/**
 * Represents a unique database index.<br/><br/>
 * Created: 11.01.2007 00:32:42
 * @author Volker Bergmann
 */
public class DBUniqueIndex extends DBIndex {

    private static final long serialVersionUID = -1758033589908866869L;
    
	private DBUniqueConstraint constraint;

    public DBUniqueIndex(String name, boolean nameDeterministic, DBUniqueConstraint constraint) {
        super(name, nameDeterministic, constraint.getTable());
        this.constraint = constraint;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public DBTable getTable() {
        return (DBTable) constraint.getOwner();
    }

    @Override
    public String[] getColumnNames() {
        return constraint.getColumnNames();
    }

	@Override
	public void addColumnName(String columnName) {
		if (constraint == null)
			constraint = new DBUniqueConstraint(getTable(), name, isNameDeterministic(), columnName);
		else
			constraint.addColumnName(columnName);
	}
    
	@Override
	public boolean isIdentical(DBObject other) {
		if (this == other)
			return true;
		if (other == null || !(other instanceof DBUniqueIndex))
			return false;
		DBUniqueIndex that = (DBUniqueIndex) other;
		return NullSafeComparator.equals(this.name, that.name)
			&& Arrays.equals(this.getColumnNames(), that.getColumnNames())
			&& NullSafeComparator.equals(getOwner().getName(), that.getOwner().getName());
	}

}
