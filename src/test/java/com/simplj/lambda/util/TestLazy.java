package com.simplj.lambda.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestLazy {
    @Test
    public void testLazyExecution() {
        Lazy<Integer> lazyOne = Lazy.of(() -> 0).mutate(n -> n + 1);
        assertEquals(1, lazyOne.get().intValue());
        assertEquals(0, lazyOne.mutate(n -> n - 1).get().intValue());
    }

    @Test
    public void testLazyRestate() {
        Lazy<Integer> lazyOne = Lazy.restating(() -> 0, x -> x < 0, x -> 0).mutate(n -> n + 1);
        assertEquals(1, lazyOne.get().intValue());
        assertEquals(0, lazyOne.mutate(n -> n - 2).get().intValue());
    }
}
