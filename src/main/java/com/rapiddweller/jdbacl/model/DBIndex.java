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

import com.rapiddweller.common.ArrayFormat;
import com.rapiddweller.common.NullSafeComparator;
import com.rapiddweller.common.bean.HashCodeBuilder;

import java.util.Arrays;

/**
 * Represents a database index.<br/><br/>
 * Created: 06.01.2007 08:58:49
 * @author Volker Bergmann
 */
public abstract class DBIndex extends AbstractDBTableComponent implements MultiColumnObject {

	private static final long serialVersionUID = -1656761838194962745L;
	
    private final boolean nameDeterministic;

    public DBIndex() {
        this(null, false, null);
    }

    public DBIndex(String name, boolean nameDeterministic, DBTable table) {
        super(name, "index", table);
        this.nameDeterministic = nameDeterministic;
        table.addIndex(this);
    }

    public abstract boolean isUnique();
    
    @Override
	public abstract String[] getColumnNames();
    
    public abstract void addColumnName(String columnName);
    
    
    // properties ------------------------------------------------------------------------------------------------------

    @Override
	public String getName() {
        return name;
    }

	public boolean isNameDeterministic() {
		return nameDeterministic;
	}
	
	
	
    // java.lang.Object overrides --------------------------------------------------------------------------------------

    @Override
    public boolean equals(Object obj) {
    	if (this == obj)
    		return true;
    	if (obj == null || obj.getClass() != this.getClass())
    		return false;
    	DBIndex that = (DBIndex) obj;
    	return NullSafeComparator.equals(this.getName(), that.getName())
			&& NullSafeComparator.equals(this.getTable(), that.getTable())
			&& NullSafeComparator.equals(this.isUnique(), that.isUnique())
			&& Arrays.equals(this.getColumnNames(), that.getColumnNames());
    }
    
    @Override
    public int hashCode() {
    	return HashCodeBuilder.hashCode(getOwner(), getColumnNames());
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(name);
        builder.append(" (");
        builder.append(ArrayFormat.format(getColumnNames()));
        builder.append(')');
        builder.append(isUnique() ? " unique" : "");
        return builder.toString();
    }
    
}
