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

import com.rapiddweller.common.Assert;
import com.rapiddweller.common.NullSafeComparator;

/**
 * Represents a database not-null constraint.<br/><br/>
 * Created: 06.01.2007 09:00:49
 *
 * @author Volker Bergmann
 */
public class DBNotNullConstraint extends DBConstraint {

  private static final long serialVersionUID = 5087538327994954133L;

  private final String columnName;

  /**
   * Instantiates a new Db not null constraint.
   *
   * @param owner             the owner
   * @param name              the name
   * @param nameDeterministic the name deterministic
   * @param columnName        the column name
   */
  public DBNotNullConstraint(DBTable owner, String name, boolean nameDeterministic, String columnName) {
    super(name, nameDeterministic, "not null constraint", owner);
    Assert.notNull(owner, "owner");
    Assert.notNull(columnName, "column name");
    this.columnName = columnName;
    if (owner != null) {
      owner.getColumn(columnName).setNotNullConstraint(this);
    }
  }

  @Override
  public String[] getColumnNames() {
    return new String[] {columnName};
  }

  @Override
  public boolean isIdentical(DBObject other) {
    if (this == other) {
      return true;
    }
    if (other == null || !(other instanceof DBNotNullConstraint)) {
      return false;
    }
    DBNotNullConstraint that = (DBNotNullConstraint) other;
    return NullSafeComparator.equals(this.name, that.name)
        && columnName.equals(that.columnName)
        && NullSafeComparator.equals(this.getTable().getName(), that.getTable().getName());
  }
}
