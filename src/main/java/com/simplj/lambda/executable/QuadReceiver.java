package com.simplj.lambda.executable;

import com.simplj.lambda.function.*;
import com.simplj.lambda.util.RetryContext;

import java.util.Objects;

@FunctionalInterface
public interface QuadReceiver<A, B, C, D> {
    void receive(A inpA, B inpB, C inpC, D inpD) throws Exception;

    /**
     * Applies the QuadReceiver partially (that is why the name `re` depicting partial `receive`-ing)
     * @param a argument to be applied
     * @return TriReceiver with the remaining arguments
     */
    default TriReceiver<B, C, D> re(A a) {
        return (b, c, d) -> receive(a, b, c, d);
    }

    /**
     * Attempts retry as per the given RetryContext when an exception is occurred during execution
     * @param ctx RetryContext containing max retry attempt, exceptions to retry (inclusive/exclusive) along with other supporting attributes.
     * @return QuadReceiver with retrying behavior as per the given RetryContext.
     */
    default QuadReceiver<A, B, C, D> withRetry(RetryContext ctx) {
        Objects.requireNonNull(ctx);
        return (a, b, c, d) -> ctx.retry(() -> receive(a, b, c, d));
    }

    default <T> QuadReceiver<T, B, C, D> composeFirst(Function<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b, c, d) -> receive(f.apply(t), b, c, d);
    }
    default <T> QuadReceiver<A, T, C, D> composeSecond(Function<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t, c, d) -> receive(a, f.apply(t), c, d);
    }
    default <T> QuadReceiver<A, B, T, D> composeThird(Function<T, C> f) {
        Objects.requireNonNull(f);
        return (a, b, t, d) -> receive(a, b, f.apply(t), d);
    }
    default <T> QuadReceiver<A, B, C, T> composeFourth(Function<T, D> f) {
        Objects.requireNonNull(f);
        return (a, b, c, t) -> receive(a, b, c, f.apply(t));
    }

    default Function<A, Function<B, Function<C, Receiver<D>>>> curried() {
        return a -> b -> c -> d -> receive(a, b, c, d);
    }

    default QuadExecutable<A, B, C, D, A> yieldFirst() {
        return (a, b, c, d) -> {
            receive(a, b, c, d);
            return a;
        };
    }
    default QuadExecutable<A, B, C, D, B> yieldSecond() {
        return (a, b, c, d) -> {
            receive(a, b, c, d);
            return b;
        };
    }
    default QuadExecutable<A, B, C, D, C> yieldThird() {
        return (a, b, c, d) -> {
            receive(a, b, c, d);
            return c;
        };
    }

    static <T, U, V, W> QuadReceiver<T, U, V, W> retrying(RetryContext ctx, QuadReceiver<T, U, V, W> f) {
        return f.withRetry(ctx);
    }
}
