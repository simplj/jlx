package com.simplj.lambda.sequence;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static com.simplj.lambda.util.Expr.let;
import static org.junit.Assert.*;

public class SeriesTest {

    @Test
    public void testSeries() {
        Series<Integer> s = Series.of(0, n -> n + 1);
        assertEquals(0, s.current().intValue());
        assertEquals(1, s.successor().intValue());
        assertEquals(1, s.next().intValue());
        assertEquals(2, s.successor().intValue());
        assertEquals(0, s.predecessor().intValue());
        assertEquals("0 < [1] < 2", s.toString());
        assertEquals(2, s.count());

        assertThrows(NoSuchElementException.class, () -> Series.of(0, n -> n + 1).predecessor());

        s = Series.of(0, Serial.withoutIdx((p, c) -> p.limit() + c), n -> n > 5);
        assertEquals(6, s.next().intValue());
        assertEquals(6, s.current().intValue());
        s = Series.of(0, n -> n + 1, Condition.equal(3).negate());
        assertEquals(Arrays.asList(1, 2, 4, 5), s.toList(4));
    }

    @Test
    public void testBoundedSeries() {
        assertThrows(NoSuchElementException.class, () -> Series.of(5, n -> n + 1).bounded(Bound.onCurrent(Condition.lesser(5))).next());
        Series<Integer> bounded = Series.of(0, Serial.withoutPred(Integer::sum), Condition.equal(10).negate()).bounded(Bound.onIdx(Condition.lesser(5)));
        assertEquals(Arrays.asList(0, 1, 3, 6, 14), bounded.toList());
        assertTrue(Bound.withoutIdx((p, c) -> p == null || c == null).satisfy(0, null, null));
        assertTrue(Bound.withoutPred((i, c) -> i == 0 || c == null).satisfy(0, null, null));
    }

    @Test
    public void testCircularSeries() {
        Series<Integer> binary = Series.of(-1, n -> n + 1).circular(Condition.greater(1));
        assertEquals(Arrays.asList(0, 1, 0, 1, 0, 1, 0, 1, 0, 1), binary.toList(10));
    }

    @Test
    public void testFibonacciSeries() {
        Serial<Integer> fibb = (i, p, c) -> let(i).pure().when(Condition.greater(1)).then(() -> p.last() + c).otherwise(Function.id());
        Series<Integer> fibonacci = Series.of(0, fibb).bounded(Bound.onCurrent(Condition.lesser(100)));
        assertEquals(Arrays.asList(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89), fibonacci.toList());
        fibonacci = Series.of(0, fibb);
        assertEquals(Arrays.asList(0, 1, 1, 2, 3, 5, 8, 13, 21, 34), fibonacci.toList(10));
    }

    @Test
    public void testFactorial() {
        Series<Integer> fact = Series.of(1, (i, p, c) -> i == 0 ? 1 : i * c);
        assertEquals(Arrays.asList(1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880), fact.toList(10));
        int val = 1;
        for (int i = 0; i < 5; i++) {
            if (i > 0) {
                val *= i;
            }
            assertEquals(val, fact.next().intValue());
        }
        assertEquals(120, fact.next().intValue());
    }
}
