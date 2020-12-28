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

import static org.junit.Assert.*;

import com.rapiddweller.script.expression.ConstantExpression;
import org.junit.Test;

/**
 * Tests the {@link LikeExpression}.<br/><br/>
 * Created: 06.06.2012 21:46:32
 * @since 0.8.3
 * @author Volker Bergmann
 */
public class LikeExpressionTest {
	
	@Test
	public void testValid() {
		check("Alpha", "Al%",   true);
		check("Alpha", "%pha",  true);
		check("Alpha", "%",     true);
		check("Alpha", "Alpha", true);
	}

	@Test
	public void testInvalid() {
		check("Alpha", "Be%",  false);
		check("Alpha", "%ta",  false);
		check("Alpha", "Beta", false);
	}

	public void check(String value, String pattern, boolean expected) {
		LikeExpression expression = new LikeExpression(new ConstantExpression<>(value), new ConstantExpression<>(pattern));
		assertEquals(expected, expression.evaluate(null));
	}
	
}
