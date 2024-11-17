package com.simplj.lambda.executable;

import com.simplj.lambda.util.retry.RetryContext;

import java.util.Objects;

@FunctionalInterface
public interface PentaReceiver<A, B, C, D, E> {
    void receive(A inpA, B inpB, C inpC, D inpD, E inpE) throws Exception;

    /**
     * Applies the PentaReceiver partially (that is why the name `re` depicting partial `receive`-ing)
     * @param a argument to be applied
     * @return QuadReceiver with the remaining arguments
     */
    default QuadReceiver<B, C, D, E> re(A a) {
        return (b, c, d, e) -> receive(a, b, c, d, e);
    }

    /**
     * Attempts retry as per the given RetryContext when an exception is occurred during execution
     * @param ctx RetryContext containing max retry attempt, exceptions to retry (inclusive/exclusive) along with other supporting attributes.
     * @return QuadReceiver with retrying behavior as per the given RetryContext.
     */
    default PentaReceiver<A, B, C, D, E> withRetry(RetryContext ctx) {
        Objects.requireNonNull(ctx);
        return (a, b, c, d, e) -> ctx.retry(() -> receive(a, b, c, d, e));
    }

    default <T> PentaReceiver<T, B, C, D, E> composeFirst(Executable<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b, c, d, e) -> receive(f.execute(t), b, c, d, e);
    }
    default <T> PentaReceiver<A, T, C, D, E> composeSecond(Executable<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t, c, d, e) -> receive(a, f.execute(t), c, d, e);
    }
    default <T> PentaReceiver<A, B, T, D, E> composeThird(Executable<T, C> f) {
        Objects.requireNonNull(f);
        return (a, b, t, d, e) -> receive(a, b, f.execute(t), d, e);
    }
    default <T> PentaReceiver<A, B, C, T, E> composeFourth(Executable<T, D> f) {
        Objects.requireNonNull(f);
        return (a, b, c, t, e) -> receive(a, b, c, f.execute(t), e);
    }
    default <T> PentaReceiver<A, B, C, D, T> composeFifth(Executable<T, E> f) {
        Objects.requireNonNull(f);
        return (a, b, c, d, t) -> receive(a, b, c, d, f.execute(t));
    }

    default Executable<A, Executable<B, Executable<C, Executable<D, Receiver<E>>>>> curried() {
        return a -> b -> c -> d -> e -> receive(a, b, c, d, e);
    }

    default PentaExecutable<A, B, C, D, E, A> yieldFirst() {
        return (a, b, c, d, e) -> {
            receive(a, b, c, d, e);
            return a;
        };
    }
    default PentaExecutable<A, B, C, D, E, B> yieldSecond() {
        return (a, b, c, d, e) -> {
            receive(a, b, c, d, e);
            return b;
        };
    }
    default PentaExecutable<A, B, C, D, E, C> yieldThird() {
        return (a, b, c, d, e) -> {
            receive(a, b, c, d, e);
            return c;
        };
    }
    default PentaExecutable<A, B, C, D, E, D> yieldFourth() {
        return (a, b, c, d, e) -> {
            receive(a, b, c, d, e);
            return d;
        };
    }
    default PentaExecutable<A, B, C, D, E, E> yieldFifth() {
        return (a, b, c, d, e) -> {
            receive(a, b, c, d, e);
            return e;
        };
    }

    default PentaExecutable<A, B, C, D, E, Void> toExecutable() {
        return (a, b, c, d, e) -> {
            receive(a, b, c, d, e);
            return null;
        };
    }

    static <T, U, V, W, X> PentaReceiver<T, U, V, W, X> of(PentaReceiver<T, U, V, W, X> f) {
        return f;
    }

    static <T, P, Q, R, S> PentaReceiver<T, P, Q, R, S> noOp() {
        return (x1, x2, x3, x4, x5) -> {};
    }
}
