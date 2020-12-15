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

import static org.junit.Assert.*;

import com.rapiddweller.jdbacl.sql.SQLParserUtil;
import com.rapiddweller.jdbacl.sql.parser.ANTLRNoCaseStringStream;
import com.rapiddweller.script.Expression;
import com.rapiddweller.script.expression.EqualsExpression;
import com.rapiddweller.script.expression.LogicalComplementExpression;
import org.junit.Test;

/**
 * Tests the {@link SQLParserUtil}.<br/><br/>
 * Created: 07.06.2011 15:35:51
 * @since 0.1
 * @author Volker Bergmann
 */
public class SQLParserUtilTest {

	@Test
	public void testInt() throws Exception {
		check("3", "3", 3);
		check("123", "123", 123);
	}

	@Test
	public void testNull() throws Exception {
		check("null", "null", null);
	}

	@Test
	public void testIdentifier() throws Exception {
		check("a", "a", "a");
		check("col", "col", "col");
	}

	@Test
	public void testString() throws Exception {
		check("'value'", "'value'", "'value'");
	}

	@Test
	public void testQuotedName() throws Exception {
		check("\"col\"", "\"col\"", "col");
	}

	@Test
	public void testUnaryMinus() throws Exception {
		check("-123", "-(123)", -123);
	}

	@Test
	public void testParentheses() throws Exception {
		String text = "(col = 3)";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testInvocation() throws Exception {
		String text = "sin(col)";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testNot() throws Exception {
		String text = "not (col = 3)";
		Expression<?> expression = SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
		assertEquals(LogicalComplementExpression.class, expression.getClass());
		LogicalComplementExpression complement = (LogicalComplementExpression) expression;
		assertEquals(EqualsExpression.class, complement.getSourceExpressions()[0].getClass());
	}

	@Test
	public void testStar() throws Exception {
		check("3 * 2", "(3 * 2)", 6);
	}

	@Test
	public void testSlash() throws Exception {
		check("6 / 2", "(6 / 2)", 3);
	}

	@Test
	public void testPercent() throws Exception {
		check("8 % 3", "(8 % 3)", 2);
	}

	@Test
	public void testPlus() throws Exception {
		String text = "1 + 2";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testMinus() throws Exception {
		String text = "3 - 1";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testBetween() throws Exception {
		String text = "col between 3 and 5";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testIn_false() throws Exception {
		String text = "2 in (2, 3)";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testIn_true() throws Exception {
		String text = "2 in (2, 3)";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testLT_false() throws Exception {
		String text = "6 < 5";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testLT_true() throws Exception {
		String text = "3 < 5";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testLE_false() throws Exception {
		String text = "5 <= 5";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testLE_true() throws Exception {
		String text = "5 <= 4";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testGT() throws Exception {
		String text = "5 > 3";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testGE_false() throws Exception {
		String text = "4 >= 5";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testGE_true() throws Exception {
		String text = "5 >= 5";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testIsNull() throws Exception {
		String text = "col is null";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testIsNotNull() throws Exception {
		String text = "col is not null";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testEq_false() throws Exception {
		String text = "3 = 4";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testEq_true() throws Exception {
		String text = "3 = 3";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testBangEq_true() throws Exception {
		String text = "4 != 3";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testBangEq_false() throws Exception {
		String text = "3 != 3";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testLtGt_true() throws Exception {
		String text = "4 <> 3";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testLtGt_false() throws Exception {
		String text = "3 <> 3";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testBarBar() throws Exception {
		String text = "'x' || 3";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testXor() throws Exception {
		String text = "a=1 xor b=2";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testAnd() throws Exception {
		String text = "a=1 and b=2";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}

	@Test
	public void testOr() throws Exception {
		String text = "a=1 or b=2";
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
	}
	
	@Test
	public void testCheck() {
		SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream("OBJ_NAME NOT LIKE 'JOURNAL%' AND OBJ_NAME NOT LIKE 'DEPLOY%' AND OBJ_NAME NOT LIKE 'LOG%' AND OBJ_NAME <> 'TEMP_GLOBE_SESSION'"));
	}
	
	// helper methods --------------------------------------------------------------------------------------------------

	protected void check(String text, String stringRep, int result) {
		Expression<?> expression = SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
		assertEquals(stringRep, expression.toString());
		assertEquals(result, ((Number) expression.evaluate(null)).intValue());
		System.out.println(expression);
	}
	
	protected void check(String text, String stringRep, String result) {
		Expression<?> expression = SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
		assertEquals(stringRep, expression.toString());
		assertEquals(result, expression.evaluate(null));
		System.out.println(expression);
	}
	
	protected void check(String text, String stringRep) {
		Expression<?> expression = SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
		assertEquals(stringRep, expression.toString());
		System.out.println(expression);
	}
	
}
