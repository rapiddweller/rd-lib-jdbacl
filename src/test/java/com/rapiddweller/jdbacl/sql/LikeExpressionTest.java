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

import com.rapiddweller.script.expression.BitwiseOrExpression;
import com.rapiddweller.script.expression.ConstantExpression;
import com.rapiddweller.script.expression.DivisionExpression;
import com.rapiddweller.script.expression.NullExpression;
import org.junit.Test;

/**
 * Tests the {@link LikeExpression}.<br/><br/>
 * Created: 06.06.2012 21:46:32
 *
 * @author Volker Bergmann
 * @since 0.8.3
 */
public class LikeExpressionTest {

    @Test
    public void testValid() {
        check("Alpha", "Al%", true);
        check("Alpha", "%pha", true);
        check("Alpha", "%", true);
        check("Alpha", "Alpha", true);
    }

    @Test
    public void testInvalid() {
        check("Alpha", "Be%", false);
        check("Alpha", "%ta", false);
        check("Alpha", "Beta", false);
    }

    @Test
    public void testToString() {
        BitwiseOrExpression term1 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression term11 = new BitwiseOrExpression(term1, new BitwiseOrExpression(null, null));
        BitwiseOrExpression term12 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression value = new BitwiseOrExpression(term11,
                new BitwiseOrExpression(term12, new BitwiseOrExpression(null, null)));
        BitwiseOrExpression term13 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression term14 = new BitwiseOrExpression(term13, new BitwiseOrExpression(null, null));
        BitwiseOrExpression term15 = new BitwiseOrExpression(null, null);
        assertEquals(
                "((((null | null) | (null | null)) | ((null | null) | (null | null))) LIKE '(((null | null) | (null |"
                        + " null)) | ((null | null) | (null | null)))')",
                (new LikeExpression(value,
                        new BitwiseOrExpression(term14, new BitwiseOrExpression(term15, new BitwiseOrExpression(null, null)))))
                        .toString());
    }

    @Test
    public void testToString2() {
        DivisionExpression divisionExpression = new DivisionExpression();
        DivisionExpression divisionExpression1 = new DivisionExpression();
        BitwiseOrExpression term1 = new BitwiseOrExpression(
                new FunctionInvocation("Name", divisionExpression, divisionExpression1, new DivisionExpression()), null);
        BitwiseOrExpression term11 = new BitwiseOrExpression(term1, new BitwiseOrExpression(null, null));
        BitwiseOrExpression term12 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression value = new BitwiseOrExpression(term11,
                new BitwiseOrExpression(term12, new BitwiseOrExpression(null, null)));
        BitwiseOrExpression term13 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression term14 = new BitwiseOrExpression(term13, new BitwiseOrExpression(null, null));
        BitwiseOrExpression term15 = new BitwiseOrExpression(null, null);
        assertEquals(
                "(((((() null () null ()) | null) | (null | null)) | ((null | null) | (null | null))) LIKE '(((null |"
                        + " null) | (null | null)) | ((null | null) | (null | null)))')",
                (new LikeExpression(value,
                        new BitwiseOrExpression(term14, new BitwiseOrExpression(term15, new BitwiseOrExpression(null, null)))))
                        .toString());
    }

    @Test
    public void testToString3() {
        NullExpression nullExpression = new NullExpression();
        DivisionExpression divisionExpression = new DivisionExpression();
        BitwiseOrExpression term1 = new BitwiseOrExpression(
                new FunctionInvocation("Name", nullExpression, divisionExpression, new DivisionExpression()), null);
        BitwiseOrExpression term11 = new BitwiseOrExpression(term1, new BitwiseOrExpression(null, null));
        BitwiseOrExpression term12 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression value = new BitwiseOrExpression(term11,
                new BitwiseOrExpression(term12, new BitwiseOrExpression(null, null)));
        BitwiseOrExpression term13 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression term14 = new BitwiseOrExpression(term13, new BitwiseOrExpression(null, null));
        BitwiseOrExpression term15 = new BitwiseOrExpression(null, null);
        assertEquals(
                "(((((null null () null ()) | null) | (null | null)) | ((null | null) | (null | null))) LIKE '(((null"
                        + " | null) | (null | null)) | ((null | null) | (null | null)))')",
                (new LikeExpression(value,
                        new BitwiseOrExpression(term14, new BitwiseOrExpression(term15, new BitwiseOrExpression(null, null)))))
                        .toString());
    }

    public void check(String value, String pattern, boolean expected) {
        LikeExpression expression = new LikeExpression(new ConstantExpression<>(value), new ConstantExpression<>(pattern));
        assertEquals(expected, expression.evaluate(null));
    }

}
