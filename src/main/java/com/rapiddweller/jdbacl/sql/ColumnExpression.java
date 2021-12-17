/*
 * (c) Copyright 2011 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.sql;

import com.rapiddweller.common.Expression;
import com.rapiddweller.script.expression.ConstantExpression;

/**
 * {@link Expression} which represents a database column.<br/><br/>
 * Created: 08.06.2011 13:37:59
 *
 * @author Volker Bergmann
 * @since 0.1
 */
public class ColumnExpression extends ConstantExpression<String> {

  /**
   * The Quoted.
   */
  protected final boolean quoted;

  /**
   * Instantiates a new Column expression.
   *
   * @param name   the name
   * @param quoted the quoted
   */
  public ColumnExpression(String name, boolean quoted) {
    super(name);
    this.quoted = quoted;
  }

  /**
   * Gets column name.
   *
   * @return the column name
   */
  public String getColumnName() {
    return getValue();
  }

  /**
   * Is quoted boolean.
   *
   * @return the boolean
   */
  public boolean isQuoted() {
    return quoted;
  }

  @Override
  public String toString() {
    return (quoted ? "\"" + getValue() + "\"" : getValue());
  }

}
