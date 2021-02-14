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
import com.rapiddweller.common.NullSafeComparator;
import com.rapiddweller.jdbacl.NameSpec;
import com.rapiddweller.jdbacl.SQLUtil;

import java.util.Arrays;

/**
 * Represents a unique constraint on one or the combination of several columns of one table.<br/>
 * <br/>
 * Created: 06.01.2007 09:00:37
 *
 * @author Volker Bergmann
 */
public class DBUniqueConstraint extends DBConstraint implements MultiColumnObject {

  private static final long serialVersionUID = -8241121848879185421L;

  private String[] columnNames;

  /**
   * Instantiates a new Db unique constraint.
   *
   * @param owner             the owner
   * @param name              the constraint name - it may be null
   * @param nameDeterministic the name deterministic
   * @param columnNames       the names of the columns to which the constraint applies
   */
  public DBUniqueConstraint(DBTable owner, String name, boolean nameDeterministic, String... columnNames) {
    super(name, nameDeterministic, "unique constraint", owner);
    this.columnNames = columnNames;
    if (getClass() == DBUniqueConstraint.class) {
      owner.addUniqueConstraint(this);
    }
  }

  @Override
  public String[] getColumnNames() {
    return columnNames.clone();
  }

  /**
   * Add column name.
   *
   * @param columnName the column name
   */
  public void addColumnName(String columnName) {
    if (!ArrayUtil.contains(columnName, columnNames)) {
      columnNames = ArrayUtil.append(columnName, columnNames);
    }
  }

  @Override
  public boolean isIdentical(DBObject other) {
    if (this == other) {
      return true;
    }
    if (other == null || !(other instanceof DBUniqueConstraint)) {
      return false;
    }
    DBUniqueConstraint that = (DBUniqueConstraint) other;
    return NullSafeComparator.equals(this.name, that.name)
        && Arrays.equals(this.columnNames, that.columnNames)
        && NullSafeComparator.equals(this.getTable().getName(), that.getTable().getName());
  }

  @Override
  public String toString() {
    return SQLUtil.ukSpec(this, NameSpec.ALWAYS);
  }

}
