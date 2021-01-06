package com.rapiddweller.jdbacl.sql;

import static org.junit.Assert.assertNull;

import com.rapiddweller.common.context.DefaultContext;
import com.rapiddweller.script.expression.DivisionExpression;
import org.junit.Test;

public class FunctionInvocationTest {
    @Test
    public void testEvaluate() {
        DivisionExpression divisionExpression = new DivisionExpression();
        DivisionExpression divisionExpression1 = new DivisionExpression();
        FunctionInvocation functionInvocation = new FunctionInvocation("Name", divisionExpression, divisionExpression1,
                new DivisionExpression());
        assertNull(functionInvocation.evaluate(new DefaultContext()));
    }
}

