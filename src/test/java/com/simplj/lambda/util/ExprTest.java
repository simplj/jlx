package com.simplj.lambda.util;

import com.simplj.lambda.data.IArray;
import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.executable.Receiver;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Consumer;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.tuples.Couple;
import org.junit.Test;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Set;

import static com.simplj.lambda.util.Expr.let;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ExprTest {
    @Test
    public void testExpr() throws Exception {
        assertEquals(1, let(0).pure().record(Consumer.noOp()).recordIf(Condition.always(), Consumer.noOp()).recordIf(Condition.never(), Consumer.noOp())
                .map(Function.id()).mapIf(Condition.always(), Function.id()).mapIf(Condition.never(), n -> n + 10).in(n -> n + 1).intValue());
        assertEquals(1, let(0).record(Receiver.noOp()).recordIf(Condition.always(), Receiver.noOp()).recordIf(Condition.never(), Receiver.noOp())
                .map(Executable.id()).mapIf(Condition.always(), Executable.id()).mapIf(Condition.never(), n -> n + 10).in(n -> n + 1).intValue());
        Set<Integer> set = new HashSet<>();
        assertEquals(1, let(set.add(1)).pure().yield(() -> set).size());
        assertEquals(1, let(set.add(1)).pure().returning(set).size());
        assertEquals(1, let(set.add(1)).yield(() -> set).size());
        assertEquals(1, let(set.add(1)).returning(set).size());
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
            assertEquals(c.second(), toDayOfWeek1(c.first() + 1));
            assertEquals(c.second(), toDayOfWeek2(c.first() + 1));
        }
    }

    private DayOfWeek toDayOfWeek1(int i) throws Exception {
        return let(i).pure().when(Condition.negate(Condition.between(1, 7))).<DayOfWeek>err(x -> new Exception("Invalid number " + x))
                .when(1).then(DayOfWeek.MONDAY)
                .when(2).then(DayOfWeek.TUESDAY)
                .when(3).then(DayOfWeek.WEDNESDAY)
                .when(4).then(DayOfWeek.THURSDAY)
                .when(5).then(DayOfWeek.FRIDAY)
                .when(6).then(DayOfWeek.SATURDAY)
                .when(7).then(DayOfWeek.SUNDAY)
                .otherwiseNull();
    }
    private DayOfWeek toDayOfWeek2(int i) throws Exception {
        return let(i).when(Condition.negate(Condition.between(1, 7))).<DayOfWeek>err(x -> new Exception("Invalid number " + x))
                .when(1).then(DayOfWeek.MONDAY)
                .when(2).then(DayOfWeek.TUESDAY)
                .when(3).then(DayOfWeek.WEDNESDAY)
                .when(4).then(DayOfWeek.THURSDAY)
                .when(5).then(DayOfWeek.FRIDAY)
                .when(6).then(DayOfWeek.SATURDAY)
                .when(7).then(DayOfWeek.SUNDAY)
                .otherwiseErr(n -> new IllegalArgumentException("Invalid index #" + n + "for day  of week!"));
    }
}
