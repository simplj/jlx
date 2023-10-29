package com.simplj.lambda.util;

import com.simplj.lambda.data.Util;
import com.simplj.lambda.executable.Excerpt;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.util.retry.RetryContext;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class TestTry {
    @Test
    public void testTryProviderExecution() throws Exception {
        assertEquals("1", Try.execute(() -> unsafe("1")).result().right());
        assertEquals("1", Try.execute(() -> unsafe("")).recover(e -> "1").result().right());
        assertEquals("IE", Try.execute(() -> unsafe(""))
                .handle(InstantiationException.class, e -> "IE")
                .handle(IllegalStateException.class, e -> "ISE").result().right());
        assertEquals("ISE", Try.execute(() -> unsafe(null))
                .handle(InstantiationException.class, e -> "IE")
                .handle(IllegalStateException.class, e -> "ISE").result().right());
        assertTrue(Try.execute(() -> unsafe(null)).handle(InstantiationException.class, e -> "IE").result().isLeft());
        assertTrue(Try.execute(() -> unsafe("")).handle(IllegalStateException.class, e -> "ISE").result().isLeft());
        assertTrue(Try.execute(() -> unsafe("")).result().isLeft());
        assertEquals("1", Try.execute(() -> unsafe("1")).resultOrThrow());
        assertThrows(InstantiationException.class, () -> Try.execute(() -> unsafe("")).resultOrThrow());
    }

    @Test
    public void testTryExcerptExecution() {
        Try.execute(this::noOp)
                .handle(IOException.class, e -> null)
                .handle(IOException.class, e -> null)
                .handle(Exception.class, e -> null)
                .run();
    }

    @Test
    public void testTryConsumerExecution() {
        Either<Exception, Void> res = Try.execute(this::noOpCloseable).log(e -> System.out.println("Exception: " + e.getMessage())).result();
        assertAutoCloseException(res, Either::isRight);
    }

    @Test
    public void testTryExecutableExecution() {
        assertEquals("2", Try.execute((Try.AutoCloseableMarker m) -> unsafe(m, 2)).result().right());
        assertEquals("2", Try.flatExecute((Try.AutoCloseableMarker m) -> Either.right(unsafe(m, 2))).result().right());
        assertTrue(Try.execute((Try.AutoCloseableMarker m) -> m.markForAutoClose(new TestAutoCloseable(2))).result().right().isClosed);
        assertEquals("1", Try.execute((Try.AutoCloseableMarker m) -> unsafe(m, 0)).recover(e -> "1").result().right());
        assertEquals("IE", Try.execute((Try.AutoCloseableMarker m) -> unsafe(m, 0))
                .handle(InstantiationException.class, e -> "IE")
                .handle(IllegalStateException.class, e -> "ISE").result().right());
        assertEquals("ISE", Try.execute((Try.AutoCloseableMarker m) -> unsafe(m, -2))
                .handle(InstantiationException.class, e -> "IE")
                .handle(IllegalStateException.class, e -> "ISE").result().right());
        assertTrue(Try.execute((Try.AutoCloseableMarker m) -> unsafe(m, -2)).handle(InstantiationException.class, e -> "IE").result().isLeft());
        assertTrue(Try.execute((Try.AutoCloseableMarker m) -> unsafe(m, 0)).handle(IllegalStateException.class, e -> "ISE").result().isLeft());
        assertTrue(Try.execute((Try.AutoCloseableMarker m) -> unsafe(m, 0)).result().isLeft());

        Either<Exception, String> r = Try.execute((Try.AutoCloseableMarker m) -> unsafe(m, 1)).result();
        assertAutoCloseException(r, e -> e.isRight() && "1".equals(e.right()));
        r = Try.execute((Try.AutoCloseableMarker m) -> unsafe(m, -1)).result();
        assertAutoCloseException(r, Either::isLeft);
    }

    @Test
    public void testMapException() {
        Either<Exception, String> r = Try.execute(() -> unsafe("")).mapException(e -> new IllegalArgumentException(e.getMessage())).result();
        assertTrue(r.isLeft());
        assertTrue(r.left() instanceof IllegalArgumentException);
    }

    @Test
    public void testFinalize() {
        Mutable<Boolean> flag = Mutable.of(false);
        Try.flatten(Try.execute(() -> unsafe("1"))).finalize(() -> flag.mutate(b -> !b)).runOrThrowRE();
        assertTrue(flag.get());
        Try.flatten(Try.execute(() -> unsafe(""))).finalize(() -> flag.mutate(b -> !b)).run();
        assertFalse(flag.get());
        Mutable<Exception> e = Mutable.of(null);
        Try.execute(Excerpt.numb()).finalize(() -> {throw new IllegalStateException("ISE");}).log(e::set).runOrThrowRE();
        assertNotNull(e.get());
        assertEquals(IllegalStateException.class, e.get().getCause().getClass());
        assertEquals("Failed to finalize Try!", e.get().getMessage());
        assertEquals("1", Try.flatten(Try.execute(() -> unsafe("1"))).result().right());
        assertEquals("1", Try.flatExecute(() -> Either.right(unsafe("1"))).result().right());
    }

    @Test
    public void testRunOrThrowRE() {
        assertThrows(IllegalStateException.class, () -> Try.flatten(Try.execute(() -> unsafe(null))).runOrThrowRE());
        assertThrows(RuntimeException.class, () -> Try.flatten(Try.execute(() -> unsafe(""))).runOrThrowRE());
    }

    @Test
    public void testReTry() {
        List<String> l = new LinkedList<>();
        IllegalStateException ise = new IllegalStateException("needs retry!");
        RetryContext.RetryContextBuilder retryCtxBuilder = RetryContext.times(100, d -> (long) (d * 1.5), 3).logger(l::add);
        Mutable<Integer> m = Mutable.of(0);
        Either<Exception, Void> r = Try.execute(() -> retry(3, m, ise)).retry(retryCtxBuilder.build()).result();
        assertTrue(r.isRight());
        assertEquals(2, l.size());
        l.clear();
        m.set(0);
        r = Try.execute(() -> retry(5, m, ise)).retry(retryCtxBuilder.build()).result();
        assertTrue(r.isLeft());
        assertTrue(r.left() instanceof IllegalStateException);
        assertEquals("needs retry!", r.left().getMessage());
        assertEquals(3, l.size());
    }

    @Test
    public void testReTryInclusion() {
        List<String> l = new LinkedList<>();
        IllegalStateException ise = new IllegalStateException("needs retry!");
        RuntimeException re = new RuntimeException("needs retry!");
        RetryContext.RetryContextBuilder retryCtxBuilder = RetryContext.times(100, d -> (long) (d * 1.5), 3).logger(l::add);
        Mutable<Integer> m = Mutable.of(0);
        Either<Exception, Void> r = Try.execute(() -> retry(5, m, ise)).retry(retryCtxBuilder.exceptions(Collections.singleton(IllegalStateException.class), true).build()).result();
        assertTrue(r.isLeft());
        assertTrue(r.left() instanceof IllegalStateException);
        assertEquals("needs retry!", r.left().getMessage());
        assertEquals(3, l.size());
        l.clear();
        m.set(0);
        r = Try.execute(() -> retry(5, m, re)).retry(retryCtxBuilder.exceptions(Collections.singleton(IllegalStateException.class), true).build()).result();
        assertTrue(r.isLeft());
        assertTrue(r.left() instanceof RuntimeException);
        assertEquals("needs retry!", r.left().getMessage());
        assertEquals(0, l.stream().filter(s -> s.startsWith("Retrying ")).count());
        l.clear();
        m.set(0);
        r = Try.execute(() -> retry(5, m, re)).retry(retryCtxBuilder.exceptions(Collections.singleton(RuntimeException.class), true).build()).result();
        assertTrue(r.isLeft());
        assertTrue(r.left() instanceof RuntimeException);
        assertEquals("needs retry!", r.left().getMessage());
        assertEquals(3, l.stream().filter(s -> s.startsWith("Retrying ")).count());
        l.clear();
        m.set(0);
        r = Try.execute(() -> retry(5, m, ise)).retry(retryCtxBuilder.exceptions(Collections.singleton(RuntimeException.class), true).build()).result();
        assertTrue(r.isLeft());
        assertTrue(r.left() instanceof IllegalStateException);
        assertEquals("needs retry!", r.left().getMessage());
        assertEquals(3, l.stream().filter(s -> s.startsWith("Retrying ")).count());
        l.clear();
        m.set(0);
        r = Try.execute(() -> retry(5, m, re)).mapException(e -> new RuntimeException(e.getMessage()))
                .retry(retryCtxBuilder.exceptions(Collections.singleton(IllegalStateException.class), true).build()).result();
        assertTrue(r.isLeft());
        assertTrue(r.left() instanceof RuntimeException);
        assertEquals("needs retry!", r.left().getMessage());
        assertEquals(0, l.stream().filter(s -> s.startsWith("Retrying ")).count());
        l.clear();
        m.set(0);
        r = Try.execute(() -> retry(5, m, re)).mapException(e -> new IllegalStateException(e.getMessage()))
                .retry(retryCtxBuilder.exceptions(Collections.singleton(IllegalStateException.class), true).build()).result();
        assertTrue(r.isLeft());
        assertTrue(r.left() instanceof IllegalStateException);
        assertEquals("needs retry!", r.left().getMessage());
        assertEquals(3, l.stream().filter(s -> s.startsWith("Retrying ")).count());
        l.clear();
        m.set(0);
        r = Try.execute(() -> retry(5, m, re)).retry(retryCtxBuilder.exceptions(Collections.singleton(RuntimeException.class), true).build())
                .mapException(e -> new IllegalStateException(e.getMessage())).result();
        assertTrue(r.isLeft());
        assertTrue(r.left() instanceof IllegalStateException);
        assertEquals("needs retry!", r.left().getMessage());
        assertEquals(3, l.stream().filter(s -> s.startsWith("Retrying ")).count());
        l.clear();
        m.set(0);
        r = Try.execute(() -> retry(5, m, re)).retry(retryCtxBuilder.exceptions(Collections.singleton(IllegalStateException.class), true).build())
                .mapException(e -> new IllegalStateException(e.getMessage())).result();
        assertTrue(r.isLeft());
        assertTrue(r.left() instanceof IllegalStateException);
        assertEquals("needs retry!", r.left().getMessage());
        assertEquals(0, l.stream().filter(s -> s.startsWith("Retrying ")).count());
    }

    @Test
    public void testReTryExclusion() {
        List<String> l = new LinkedList<>();
        IllegalStateException ise = new IllegalStateException("needs retry!");
        RuntimeException re = new RuntimeException("needs retry!");
        RetryContext.RetryContextBuilder retryCtxBuilder = RetryContext.times(100, d -> (long) (d * 1.5), 3).logger(l::add);
        Mutable<Integer> m = Mutable.of(0);
        Either<Exception, Void> r = Try.execute(() -> retry(5, m, ise)).retry(retryCtxBuilder.exceptions(Collections.singleton(IllegalStateException.class), false).build()).result();
        assertTrue(r.isLeft());
        assertTrue(r.left() instanceof IllegalStateException);
        assertEquals("needs retry!", r.left().getMessage());
        assertEquals(0, l.size());
        l.clear();
        m.set(0);
        r = Try.execute(() -> retry(5, m, re)).retry(retryCtxBuilder.exceptions(Collections.singleton(IllegalStateException.class), false).build()).result();
        assertTrue(r.isLeft());
        assertTrue(r.left() instanceof RuntimeException);
        assertEquals("needs retry!", r.left().getMessage());
        assertEquals(3, l.stream().filter(s -> s.startsWith("Retrying ")).count());
        l.clear();
        m.set(0);
        r = Try.execute(() -> retry(5, m, re)).retry(retryCtxBuilder.exceptions(Collections.singleton(RuntimeException.class), false).build()).result();
        assertTrue(r.isLeft());
        assertTrue(r.left() instanceof RuntimeException);
        assertEquals("needs retry!", r.left().getMessage());
        assertEquals(0, l.stream().filter(s -> s.startsWith("Retrying ")).count());
        l.clear();
        m.set(0);
        r = Try.execute(() -> retry(5, m, ise)).retry(retryCtxBuilder.exceptions(Collections.singleton(RuntimeException.class), false).build()).result();
        assertTrue(r.isLeft());
        assertTrue(r.left() instanceof IllegalStateException);
        assertEquals("needs retry!", r.left().getMessage());
        assertEquals(0, l.stream().filter(s -> s.startsWith("Retrying ")).count());
    }

    private <T> void assertAutoCloseException(Either<Exception, T> res, Condition<Either<Exception, ?>> resultCondition) {
        assertTrue(res.isLeft());
        assertTrue(res.left() instanceof Try.AutoCloseException);
        Try.AutoCloseException ace = Util.cast(res.left());
        assertTrue(resultCondition.evaluate(ace.result()));
        assertEquals(1, ace.failedCloseableList().size());
        Couple<AutoCloseable, Exception> c = ace.failedCloseableList().get(0);
        assertTrue(c.first() instanceof TestAutoCloseable);
        assertTrue(c.second().getClass().isAssignableFrom(Exception.class));
        assertEquals("Odd Number!", c.second().getMessage());
    }

    private String unsafe(String x) throws Exception {
        if (x == null) {
            throw new IllegalStateException("Null String");
        } else if (x.isEmpty()) {
            throw new InstantiationException("Empty String");
        }
        return x;
    }
    private void noOp() throws Exception {

    }
    private void noOpCloseable(Try.AutoCloseableMarker m) throws Exception {
        m.markForAutoClose(new TestAutoCloseable(-1));
    }
    private String unsafe(Try.AutoCloseableMarker m, int flag) throws Exception {
        m.markForAutoClose(new TestAutoCloseable(flag));
        if (flag < 0) {
            throw new IllegalStateException("Negative Number");
        } else if (flag == 0) {
            throw new InstantiationException("Number is 0");
        }
        return String.valueOf(flag);
    }

    private void retry(int n, Mutable<Integer> m, Exception e) throws Exception {
        if (m.mutate(v -> v + 1).get() < n) {
            throw e;
        }
    }

    private static class TestAutoCloseable implements AutoCloseable {
        private final int flag;
        private boolean isClosed;

        private TestAutoCloseable(int flag) {
            this.flag = flag;
        }

        @Override
        public void close() throws Exception {
            if (Math.abs(flag) % 2 == 1) {
                throw new Exception("Odd Number!");
            }
            this.isClosed = true;
        }
    }
}
