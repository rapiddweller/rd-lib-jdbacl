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

import com.rapiddweller.jdbacl.sql.parser.ANTLRNoCaseStringStream;
import com.rapiddweller.common.Expression;
import com.rapiddweller.script.expression.EqualsExpression;
import com.rapiddweller.script.expression.LogicalComplementExpression;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link SQLParserUtil}.<br/><br/>
 * Created: 07.06.2011 15:35:51
 *
 * @author Volker Bergmann
 * @since 0.1
 */
public class SQLParserUtilTest {

  /**
   * Test int.
   */
  @Test
  public void testInt() {
    check("3", "3", 3);
    check("123", "123", 123);
  }

  /**
   * Test null.
   */
  @Test
  public void testNull() {
    check("null", "null", null);
  }

  /**
   * Test identifier.
   */
  @Test
  public void testIdentifier() {
    check("a", "a", "a");
    check("col", "col", "col");
  }

  /**
   * Test string.
   */
  @Test
  public void testString() {
    check("'value'", "'value'", "'value'");
  }

  /**
   * Test quoted name.
   */
  @Test
  public void testQuotedName() {
    check("\"col\"", "\"col\"", "col");
  }

  /**
   * Test unary minus.
   */
  @Test
  public void testUnaryMinus() {
    check("-123", "-(123)", -123);
  }

  /**
   * Test parentheses.
   */
  @Test
  public void testParentheses() {
    String text = "(col = 3)";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test invocation.
   */
  @Test
  public void testInvocation() {
    String text = "sin(col)";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test not.
   */
  @Test
  public void testNot() {
    String text = "not (col = 3)";
    Expression<?> expression = SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
    assertEquals(LogicalComplementExpression.class, Objects.requireNonNull(expression).getClass());
    LogicalComplementExpression complement = (LogicalComplementExpression) expression;
    assertEquals(EqualsExpression.class, complement.getSourceExpressions()[0].getClass());
  }

  /**
   * Test star.
   */
  @Test
  public void testStar() {
    check("3 * 2", "(3 * 2)", 6);
  }

  /**
   * Test slash.
   */
  @Test
  public void testSlash() {
    check("6 / 2", "(6 / 2)", 3);
  }

  /**
   * Test percent.
   */
  @Test
  public void testPercent() {
    check("8 % 3", "(8 % 3)", 2);
  }

  /**
   * Test plus.
   */
  @Test
  public void testPlus() {
    String text = "1 + 2";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test minus.
   */
  @Test
  public void testMinus() {
    String text = "3 - 1";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test between.
   */
  @Test
  public void testBetween() {
    String text = "col between 3 and 5";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test in false.
   */
  @Test
  public void testIn_false() {
    String text = "2 in (2, 3)";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test in true.
   */
  @Test
  public void testIn_true() {
    String text = "2 in (2, 3)";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test lt false.
   */
  @Test
  public void testLT_false() {
    String text = "6 < 5";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test lt true.
   */
  @Test
  public void testLT_true() {
    String text = "3 < 5";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test le false.
   */
  @Test
  public void testLE_false() {
    String text = "5 <= 5";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test le true.
   */
  @Test
  public void testLE_true() {
    String text = "5 <= 4";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test gt.
   */
  @Test
  public void testGT() {
    String text = "5 > 3";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test ge false.
   */
  @Test
  public void testGE_false() {
    String text = "4 >= 5";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test ge true.
   */
  @Test
  public void testGE_true() {
    String text = "5 >= 5";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test is null.
   */
  @Test
  public void testIsNull() {
    String text = "col is null";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test is not null.
   */
  @Test
  public void testIsNotNull() {
    String text = "col is not null";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test eq false.
   */
  @Test
  public void testEq_false() {
    String text = "3 = 4";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test eq true.
   */
  @Test
  public void testEq_true() {
    String text = "3 = 3";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test bang eq true.
   */
  @Test
  public void testBangEq_true() {
    String text = "4 != 3";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test bang eq false.
   */
  @Test
  public void testBangEq_false() {
    String text = "3 != 3";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test lt gt true.
   */
  @Test
  public void testLtGt_true() {
    String text = "4 <> 3";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test lt gt false.
   */
  @Test
  public void testLtGt_false() {
    String text = "3 <> 3";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test bar bar.
   */
  @Test
  public void testBarBar() {
    String text = "'x' || 3";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test xor.
   */
  @Test
  public void testXor() {
    String text = "a=1 xor b=2";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test and.
   */
  @Test
  public void testAnd() {
    String text = "a=1 and b=2";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test or.
   */
  @Test
  public void testOr() {
    String text = "a=1 or b=2";
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
  }

  /**
   * Test check.
   */
  @Test
  public void testCheck() {
    SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(
        "OBJ_NAME NOT LIKE 'JOURNAL%' AND OBJ_NAME NOT LIKE 'DEPLOY%' AND OBJ_NAME NOT LIKE 'LOG%' AND OBJ_NAME <> 'TEMP_GLOBE_SESSION'"));
  }

  // helper methods --------------------------------------------------------------------------------------------------

  /**
   * Check.
   *
   * @param text      the text
   * @param stringRep the string rep
   * @param result    the result
   */
  protected void check(String text, String stringRep, int result) {
    Expression<?> expression = SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
    assertEquals(stringRep, Objects.requireNonNull(expression).toString());
    assertEquals(result, ((Number) expression.evaluate(null)).intValue());
    System.out.println(expression);
  }

  /**
   * Check.
   *
   * @param text      the text
   * @param stringRep the string rep
   * @param result    the result
   */
  protected void check(String text, String stringRep, String result) {
    Expression<?> expression = SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
    assertEquals(stringRep, Objects.requireNonNull(expression).toString());
    assertEquals(result, expression.evaluate(null));
    System.out.println(expression);
  }

  /**
   * Check.
   *
   * @param text      the text
   * @param stringRep the string rep
   */
  protected void check(String text, String stringRep) {
    Expression<?> expression = SQLParserUtil.parseExpression(new ANTLRNoCaseStringStream(text));
    assertEquals(stringRep, Objects.requireNonNull(expression).toString());
    System.out.println(expression);
  }

}
