/*
 * (c) Copyright 2012 by Volker Bergmann. All rights reserved.
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

import java.util.regex.Pattern;

import com.rapiddweller.commons.Context;
import com.rapiddweller.commons.converter.ToStringConverter;
import com.rapiddweller.script.Expression;
import com.rapiddweller.script.expression.BinaryExpression;

/**
 * {@link Expression} which evaluates a SQL 'LIKE' operation.<br/><br/>
 * Created: 05.06.2012 11:45:34
 * @since 0.8.3
 * @author Volker Bergmann
 */
public class LikeExpression extends BinaryExpression<Boolean> {

	public LikeExpression(Expression<?> value, Expression<?> pattern) {
		super(value, pattern);
	}

	@Override
	public Boolean evaluate(Context context) {
		String value = ToStringConverter.convert(term1.evaluate(context), null);
		String pattern = ToStringConverter.convert(term2.evaluate(context), null);
		pattern = pattern.replace("%", ".*");
		return Pattern.matches(pattern, value);
	}
	
	@Override
	public String toString() {
		return "(" + term1 + " LIKE '" + term2 + "')";
	}
	
}
