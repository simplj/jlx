package com.simplj.lambda.util;

import com.simplj.lambda.function.Condition;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class SeriesTest {

    @Test
    public void testSeries() {
        Series<Integer> s = Series.of(0, n -> n + 1);
        assertEquals(1, s.next().intValue());
        assertEquals("<1>", s.toString());

        s = Series.of(0, n -> n + 1, n -> n > 5);
        assertEquals(6, s.next().intValue());
        assertEquals(6, s.current().intValue());
    }

    @Test
    public void testBoundedSeries() {
        assertThrows(NoSuchElementException.class, () -> Series.bounded(6, n -> n + 1, n -> n < 5).next());
        Series.bounded(6, n -> n + 1, n -> n < 5).next();

        Series<Integer> s = Series.bounded(0, n -> n + 1, n -> n < 5, n -> n % 2 == 0);
        assertEquals(6, s.next().intValue());
        assertEquals(6, s.current().intValue());
    }

    @Test
    public void testFibonacciSeries() {
        Series<Integer> fibonacci = Series.bounded(0, (a, b) -> a.size() == 0 ? 1 : a.get(0) + b, Condition.lesser(100));
        System.out.println(fibonacci.toList());
        while (fibonacci.hasNext()) {
            System.out.println(fibonacci.next());
        }
        System.out.println(fibonacci);
        fibonacci = Series.of(0, (a, b) -> a.size() == 0 ? 1 : a.get(0) + b);
        System.out.println(fibonacci.toList(10));
    }
}
