package com.simplj.lambda.util;

import com.simplj.lambda.function.Function;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class TestRetryContext {
    @Test
    public void testCountRetry() throws Exception {
        Mutable<Integer> m = Mutable.of(0);
        RetryContext ctx = RetryContext.builder(100, 1.5, 3).build();
        assertEquals(1.5, ctx.multiplier(), 0.0);
        assertEquals(-1, ctx.maxDelay());
        assertEquals(3, ctx.maxAttempt());
        assertEquals(-1, ctx.maxDuration());
        assertTrue(ctx.retryNeededFor(new Exception()));
        assertNotNull(ctx.logger());
        int n = ctx.retry(() -> retry(3, m));
        assertEquals(3, n);
    }

    @Test
    public void testTimeBoxedRetry() {
        Mutable<Integer> m = Mutable.of(0);
        List<String> l = new LinkedList<>();
        RetryContext ctx = RetryContext.builder(100, 1.5, 200L).logger(l::add).build();
        assertEquals(-1, ctx.maxAttempt());
        assertEquals(200L, ctx.maxDuration());
        assertThrows(IllegalStateException.class, () -> ctx.retry(() -> {
                    retry(4, m);
                }));
        assertEquals(2, l.size());
    }

    @Test
    public void testCountAndTimeBoxedRetry() {
        Mutable<Integer> m = Mutable.of(0);
        List<String> l = new LinkedList<>();
        RetryContext ctx = RetryContext.builder(100, 1.5, 3, 200L).logger(l::add).build();
        assertEquals(3, ctx.maxAttempt());
        assertEquals(200L, ctx.maxDuration());
        assertThrows(IllegalStateException.class, () -> ctx.retry(() -> {
                    retry(4, m);
                }));
        assertEquals(2, l.size());
    }

    @Test
    public void testNegativeParams() {
        assertThrows(IllegalArgumentException.class, () -> RetryContext.builder(-1, 1.0, 1));
        assertThrows(IllegalArgumentException.class, () -> RetryContext.builder(1, 1.0, -1));
        assertThrows(IllegalArgumentException.class, () -> RetryContext.builder(-1, 1.0, 100L));
        assertThrows(IllegalArgumentException.class, () -> RetryContext.builder(1, 1.0, -100L));
        assertThrows(IllegalArgumentException.class, () -> RetryContext.builder(-1, 1.0, 1, 100L));
        assertThrows(IllegalArgumentException.class, () -> RetryContext.builder(1, 1.0, -1, 100L));
        assertThrows(IllegalArgumentException.class, () -> RetryContext.builder(1, 1.0, 1, -100L));
    }

    @Test
    public void testResettableRetryContext() {
        ResettableRetryContext<Integer> r = RetryContext.builder(100, 1.5, 3).build().resettableContext(Function.id());
        assertNotNull(r.retryInputResetF());
        assertEquals(1, r.retryInputResetF().apply(1).intValue());
    }

    private int retry(int n, Mutable<Integer> m) throws Exception {
        if (m.mutate(v -> v + 1).get() < n) {
            throw new IllegalStateException("Needs Retry!");
        }
        return n;
    }
}
