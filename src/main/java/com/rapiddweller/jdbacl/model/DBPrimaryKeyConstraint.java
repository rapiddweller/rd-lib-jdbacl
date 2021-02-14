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

import com.rapiddweller.jdbacl.NameSpec;
import com.rapiddweller.jdbacl.SQLUtil;

/**
 * Represents a primary key constraint in a database.<br/><br/>
 * Created: 08.01.2007 23:53:56
 *
 * @author Volker Bergmann
 */
public class DBPrimaryKeyConstraint extends DBUniqueConstraint {

  private static final long serialVersionUID = 2403324107962405097L;

  /**
   * Instantiates a new Db primary key constraint.
   *
   * @param table             the table
   * @param name              the constraint name - it may be null
   * @param nameDeterministic the name deterministic
   * @param columnNames       the names of the columns to which the constraint is applied
   */
  public DBPrimaryKeyConstraint(DBTable table, String name, boolean nameDeterministic, String... columnNames) {
    super(table, name, nameDeterministic, columnNames);
    if (table != null) {
      table.setPrimaryKey(this);
    }
  }

  @Override
  public String toString() {
    return SQLUtil.pkSpec(this, NameSpec.ALWAYS);
  }

}
