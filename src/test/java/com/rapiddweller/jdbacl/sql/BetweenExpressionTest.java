package com.rapiddweller.jdbacl.sql;

import com.rapiddweller.script.expression.BitwiseOrExpression;
import com.rapiddweller.script.expression.DivisionExpression;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BetweenExpressionTest {
    @Test
    public void testConstructor() {
        BitwiseOrExpression term1 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression term11 = new BitwiseOrExpression(term1, new BitwiseOrExpression(null, null));
        BitwiseOrExpression term12 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression term13 = new BitwiseOrExpression(term11,
                new BitwiseOrExpression(term12, new BitwiseOrExpression(null, null)));
        BitwiseOrExpression term14 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression term15 = new BitwiseOrExpression(term14, new BitwiseOrExpression(null, null));
        BitwiseOrExpression term16 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression value = new BitwiseOrExpression(term13,
                new BitwiseOrExpression(term15, new BitwiseOrExpression(term16, new BitwiseOrExpression(null, null))));
        BitwiseOrExpression term17 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression term18 = new BitwiseOrExpression(term17, new BitwiseOrExpression(null, null));
        BitwiseOrExpression term19 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression term110 = new BitwiseOrExpression(term18,
                new BitwiseOrExpression(term19, new BitwiseOrExpression(null, null)));
        BitwiseOrExpression term111 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression term112 = new BitwiseOrExpression(term111, new BitwiseOrExpression(null, null));
        BitwiseOrExpression term113 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression min = new BitwiseOrExpression(term110,
                new BitwiseOrExpression(term112, new BitwiseOrExpression(term113, new BitwiseOrExpression(null, null))));
        BitwiseOrExpression term114 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression term115 = new BitwiseOrExpression(term114, new BitwiseOrExpression(null, null));
        BitwiseOrExpression term116 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression term117 = new BitwiseOrExpression(term115,
                new BitwiseOrExpression(term116, new BitwiseOrExpression(null, null)));
        BitwiseOrExpression term118 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression term119 = new BitwiseOrExpression(term118, new BitwiseOrExpression(null, null));
        BitwiseOrExpression term120 = new BitwiseOrExpression(null, null);
        BetweenExpression actualBetweenExpression = new BetweenExpression(value, min, new BitwiseOrExpression(term117,
                new BitwiseOrExpression(term119, new BitwiseOrExpression(term120, new BitwiseOrExpression(null, null)))));
        assertEquals("(((((null | null) | (null | null)) | ((null | null) | (null | null))) | (((null | null) | (null |"
                + " null)) | ((null | null) | (null | null)))) BETWEEN ((((null | null) | (null | null)) | ((null | null)"
                + " | (null | null))) | (((null | null) | (null | null)) | ((null | null) | (null | null)))) AND ((((null"
                + " | null) | (null | null)) | ((null | null) | (null | null))) | (((null | null) | (null | null)) |"
                + " ((null | null) | (null | null)))))", actualBetweenExpression.toString());
        assertEquals(3, actualBetweenExpression.getSourceExpressions().length);
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
        BitwiseOrExpression min = new BitwiseOrExpression(term14,
                new BitwiseOrExpression(term15, new BitwiseOrExpression(null, null)));
        BitwiseOrExpression term16 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression term17 = new BitwiseOrExpression(term16, new BitwiseOrExpression(null, null));
        BitwiseOrExpression term18 = new BitwiseOrExpression(null, null);
        assertEquals(
                "((((null | null) | (null | null)) | ((null | null) | (null | null))) BETWEEN (((null | null) | (null"
                        + " | null)) | ((null | null) | (null | null))) AND (((null | null) | (null | null)) | ((null | null) |"
                        + " (null | null))))",
                (new BetweenExpression(value, min,
                        new BitwiseOrExpression(term17, new BitwiseOrExpression(term18, new BitwiseOrExpression(null, null)))))
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
        BitwiseOrExpression min = new BitwiseOrExpression(term14,
                new BitwiseOrExpression(term15, new BitwiseOrExpression(null, null)));
        BitwiseOrExpression term16 = new BitwiseOrExpression(null, null);
        BitwiseOrExpression term17 = new BitwiseOrExpression(term16, new BitwiseOrExpression(null, null));
        BitwiseOrExpression term18 = new BitwiseOrExpression(null, null);
        assertEquals(
                "(((((() null () null ()) | null) | (null | null)) | ((null | null) | (null | null))) BETWEEN (((null"
                        + " | null) | (null | null)) | ((null | null) | (null | null))) AND (((null | null) | (null | null)) |"
                        + " ((null | null) | (null | null))))",
                (new BetweenExpression(value, min,
                        new BitwiseOrExpression(term17, new BitwiseOrExpression(term18, new BitwiseOrExpression(null, null)))))
                        .toString());
    }
}

