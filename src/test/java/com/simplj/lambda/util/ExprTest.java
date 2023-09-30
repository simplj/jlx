package com.simplj.lambda.util;

import com.simplj.lambda.data.IArray;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.tuples.Couple;
import org.junit.Test;

import java.time.DayOfWeek;

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

    @Test
    public void testLetWhenThen() throws Exception {
        for (Couple<Integer, DayOfWeek> c : IArray.of(DayOfWeek.values()).indexed()) {
            assertEquals(c.second(), toDayOfWeek(c.first() + 1));
        }
    }

    private DayOfWeek toDayOfWeek(int i) throws Exception {
        return let(i).when(Condition.negate(Condition.between(1, 7))).<DayOfWeek>err(x -> new Exception("Invalid number " + x))
                .when(1).then(DayOfWeek.MONDAY)
                .when(2).then(DayOfWeek.TUESDAY)
                .when(3).then(DayOfWeek.WEDNESDAY)
                .when(4).then(DayOfWeek.THURSDAY)
                .when(5).then(DayOfWeek.FRIDAY)
                .when(6).then(DayOfWeek.SATURDAY)
                .otherwise(DayOfWeek.SUNDAY);
    }
}
