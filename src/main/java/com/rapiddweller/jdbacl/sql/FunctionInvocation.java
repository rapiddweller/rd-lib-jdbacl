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

import com.rapiddweller.common.Context;
import com.rapiddweller.common.Expression;
import com.rapiddweller.script.expression.CompositeExpression;

/**
 * Represents the invocation of a function.<br/><br/>
 * Created: 08.06.2011 11:58:37
 *
 * @author Volker Bergmann
 * @since 0.1
 */
public class FunctionInvocation extends CompositeExpression<Object, Object> {

  /**
   * The Name.
   */
  final String name;

  /**
   * Instantiates a new Function invocation.
   *
   * @param name      the name
   * @param arguments the arguments
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public FunctionInvocation(String name, Expression... arguments) {
    super(arguments);
    this.name = name;
  }

  /**
   * Gets function name.
   *
   * @return the function name
   */
  public String getFunctionName() {
    return name;
  }

  @Override
  public Object evaluate(Context context) {
    return null; // the class is just a space holder
  }

}
