package com.simplj.lambda.util;

import com.simplj.lambda.function.Condition;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class SeriesTest {

//    @Test
//    public void testSeries() {
//        Series<Integer> s = Series.of(0, n -> n + 1);
//        assertEquals(1, s.next().intValue());
//        assertEquals("<1>", s.toString());
//
//        s = Series.of(0, n -> n + 1, n -> n > 5);
//        assertEquals(6, s.next().intValue());
//        assertEquals(6, s.current().intValue());
//    }

//    @Test
//    public void testBoundedSeries() {
//        assertThrows(NoSuchElementException.class, () -> Series.bounded(6, n -> n + 1, n -> n < 5).next());
//        Series.bounded(6, n -> n + 1, n -> n < 5).next();
//
//        Series<Integer> s = Series.bounded(0, n -> n + 1, n -> n < 5, n -> n % 2 == 0);
//        assertEquals(6, s.next().intValue());
//        assertEquals(6, s.current().intValue());
//    }

//    @Test
//    public void testFibonacciSeries() {
//        Series<Integer> fibonacci = Series.bounded(0, (a, b) -> a.size() == 0 ? 1 : a.get(0) + b, Condition.lesser(100));
//        System.out.println(fibonacci.toList());
//        while (fibonacci.hasNext()) {
//            System.out.println(fibonacci.next());
//        }
//        System.out.println(fibonacci);
//        fibonacci = Series.of(0, (a, b) -> a.size() == 0 ? 1 : a.get(0) + b);
//        System.out.println(fibonacci.toList(10));
//    }

    @Test
    public void testSquareNumberSeries() {
        /*
         * output: 1,4,9,16,...
         */
        Series<Integer> sqNums = Series.of(1, (a,b) -> a.size() == 0 ? 1 : a.first()*a.first());
        System.out.println(sqNums.toList(10));
    }

    @Test
    public void testNaturalNumberSeries() {
//        Series<Integer> naturalNums = Series.of(1, (a, b) -> a.size() == 0 ? 1 : (a.first()*(a.first()+1))/2);
        Series<Integer> naturalNums = Series.of(3, n -> n*(n+1)/2);
        System.out.println(naturalNums.toList(10));
    }

    @Test
    public void testCatalanNumberSeries() {
        // ref: https://en.wikipedia.org/wiki/Catalan_number
        Series<Integer> catalans = Series.of(1, (a,b) -> a.size() == 0 ? 2 : factorial(2*a.first())/(factorial(a.first())*factorial(a.first()+1)));
        System.out.println(catalans.toList(10));
    }

    public void fact() {
        Series<Integer> facts = Series.of(5, (n) -> n*(n-1));
    }

    private int factorial(int n) {
        if (n == 0 || n == 1) {
            return 1;
        } else {
            return n * factorial(n-1);
        }
    }
}
