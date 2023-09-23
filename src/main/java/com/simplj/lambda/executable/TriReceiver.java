package com.simplj.lambda.executable;

import com.simplj.lambda.util.retry.RetryContext;

import java.util.Objects;

@FunctionalInterface
public interface TriReceiver<A, B, C> {
    void receive(A inpA, B inpB, C inpC) throws Exception;

    /**
     * Applies the TriReceiver partially (that is why the name `re` depicting partial `receive`-ing)
     * @param a argument to be applied
     * @return BiReceiver with the remaining arguments
     */
    default BiReceiver<B, C> re(A a) {
        return (b, c) -> receive(a, b, c);
    }

    /**
     * Attempts retry as per the given RetryContext when an exception is occurred during execution
     * @param ctx RetryContext containing max retry attempt, exceptions to retry (inclusive/exclusive) along with other supporting attributes.
     * @return TriReceiver with retrying behavior as per the given RetryContext.
     */
    default TriReceiver<A, B, C> withRetry(RetryContext ctx) {
        Objects.requireNonNull(ctx);
        return (a, b, c) -> ctx.retry(() -> receive(a, b, c));
    }

    default <T> TriReceiver<T, B, C> composeFirst(Executable<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b, c) -> receive(f.execute(t), b, c);
    }
    default <T> TriReceiver<A, T, C> composeSecond(Executable<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t, c) -> receive(a, f.execute(t), c);
    }
    default <T> TriReceiver<A, B, T> composeThird(Executable<T, C> f) {
        Objects.requireNonNull(f);
        return (a, b, t) -> receive(a, b, f.execute(t));
    }

    default Executable<A, Executable<B, Receiver<C>>> curried() {
        return a -> b -> c -> receive(a, b, c);
    }

    default TriExecutable<A, B, C, A> yieldFirst() {
        return (a, b, c) -> {
            receive(a, b, c);
            return a;
        };
    }
    default TriExecutable<A, B, C, B> yieldSecond() {
        return (a, b, c) -> {
            receive(a, b, c);
            return b;
        };
    }
    default TriExecutable<A, B, C, C> yieldThird() {
        return (a, b, c) -> {
            receive(a, b, c);
            return c;
        };
    }
    default TriExecutable<A, B, C, Void> toExecutable() {
        return (a, b, c) -> {
            receive(a, b, c);
            return null;
        };
    }

    static <T, U, V> TriReceiver<T, U, V> of(TriReceiver<T, U, V> f) {
        return f;
    }

    static <T, P, Q> TriReceiver<T, P, Q> noOp() {
        return (x1, x2, x3) -> {};
    }
}
