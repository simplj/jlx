package com.simplj.lambda.util;

import org.junit.Test;

import static com.simplj.lambda.util.Expr.let;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ExprTest {
    @Test
    public void testExpr() {
        assertEquals(1, let(0).in(n -> n + 1).intValue());
    }

    @Test
    public void testEdgeCases() {
        Expr<String> l = let("a");
        assertEquals("a => ?", l.toString());
        assertThrows(IllegalStateException.class, () -> l.equals(null));
        assertThrows(IllegalStateException.class, l::hashCode);
    }
}
