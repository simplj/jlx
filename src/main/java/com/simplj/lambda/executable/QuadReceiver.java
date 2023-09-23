package com.simplj.lambda.executable;

import com.simplj.lambda.util.retry.RetryContext;

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

    default <T> QuadReceiver<T, B, C, D> composeFirst(Executable<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b, c, d) -> receive(f.execute(t), b, c, d);
    }
    default <T> QuadReceiver<A, T, C, D> composeSecond(Executable<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t, c, d) -> receive(a, f.execute(t), c, d);
    }
    default <T> QuadReceiver<A, B, T, D> composeThird(Executable<T, C> f) {
        Objects.requireNonNull(f);
        return (a, b, t, d) -> receive(a, b, f.execute(t), d);
    }
    default <T> QuadReceiver<A, B, C, T> composeFourth(Executable<T, D> f) {
        Objects.requireNonNull(f);
        return (a, b, c, t) -> receive(a, b, c, f.execute(t));
    }

    default Executable<A, Executable<B, Executable<C, Receiver<D>>>> curried() {
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

    static <T, U, V, W> QuadReceiver<T, U, V, W> of(QuadReceiver<T, U, V, W> f) {
        return f;
    }

    static <T, P, Q, R> QuadReceiver<T, P, Q, R> noOp() {
        return (x1, x2, x3, x4) -> {};
    }
}
