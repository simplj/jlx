package com.simplj.lambda.util;

import com.simplj.lambda.executable.BiExecutable;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.util.retry.ResettableRetryContext;
import com.simplj.lambda.util.retry.RetryContext;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class TestRetryContext {
    @Test
    public void testCountRetry() throws Exception {
        Mutable<Integer> m = Mutable.of(0);
        RetryContext ctx = RetryContext.times(100, d -> (long) (d * 1.5), 3).build();
        int n = ctx.retry(() -> retry(3, m));
        assertEquals(3, n);
    }

    @Test
    public void testTimeBoxedRetry() {
        Mutable<Integer> m = Mutable.of(0);
        List<String> l = new LinkedList<>();
        AtomicInteger ai = new AtomicInteger();
        RetryContext ctx = RetryContext.duration(100, d -> (long) (d * 1.5), 200L).registerPreRetryHook(ai::incrementAndGet).logger(l::add).build();
        assertThrows(IllegalStateException.class, () -> ctx.retry(() -> {
                    retry(4, m);
                }));
        assertEquals(2, l.size());
        assertEquals(2, ai.get());
    }

    @Test
    public void testCountAndTimeBoxedRetry() {
        Mutable<Integer> m = Mutable.of(0);
        List<String> l = new LinkedList<>();
        RetryContext ctx = RetryContext.builder(100, d -> (long) (d * 1.5), 3, 200L).logger(l::add).build();
        assertThrows(IllegalStateException.class, () -> ctx.retry(() -> {
                    retry(4, m);
                }));
        assertEquals(2, l.size());
    }

    @Test
    public void testNegativeParams() {
        assertThrows(IllegalArgumentException.class, () -> RetryContext.times(-1, Function.id(), 1));
        assertThrows(IllegalArgumentException.class, () -> RetryContext.times(1, Function.id(), -1));
        assertThrows(IllegalArgumentException.class, () -> RetryContext.duration(-1, Function.id(), 100L));
        assertThrows(IllegalArgumentException.class, () -> RetryContext.duration(1, Function.id(), -100L));
        assertThrows(IllegalArgumentException.class, () -> RetryContext.builder(-1, Function.id(), 1, 100L));
        assertThrows(IllegalArgumentException.class, () -> RetryContext.builder(1, Function.id(), -1, 100L));
        assertThrows(IllegalArgumentException.class, () -> RetryContext.builder(1, Function.id(), 1, -100L));
    }

    @Test
    public void testResettableRetryContext() throws Exception {
        ResettableRetryContext<Mutable<Integer>> r = RetryContext.times(100, d -> (long) (d * 1.5), 3).build().resettableContext(Function.id());
        assertEquals(1, r.retry(BiExecutable.of(this::retry).exec(1), Mutable.of(0)).intValue());
    }

    private int retry(int n, Mutable<Integer> m) {
        if (m.mutate(v -> v + 1).get() < n) {
            throw new IllegalStateException("Needs Retry!");
        }
        return n;
    }
}
