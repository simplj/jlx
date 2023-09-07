package com.simplj.lambda.executable;

import com.simplj.lambda.util.RetryContext;

import java.util.Objects;

@FunctionalInterface
public interface BiReceiver<A, B> {
    void receive(A inpA, B inpB) throws Exception;

    /**
     * Applies the BiReceiver partially (that is why the name `re` depicting partial `receive`-ing)
     * @param a argument to be applied
     * @return Receiver with the other argument
     */
    default Receiver<B> re(A a) {
        return b -> receive(a, b);
    }

    /**
     * Attempts retry as per the given RetryContext when an exception is occurred during execution
     * @param ctx RetryContext containing max retry attempt, exceptions to retry (inclusive/exclusive) along with other supporting attributes.
     * @return BiReceiver with retrying behavior as per the given RetryContext.
     */
    default BiReceiver<A, B> withRetry(RetryContext ctx) {
        Objects.requireNonNull(ctx);
        return (a, b) -> ctx.retry(() -> receive(a, b));
    }

    default <T> BiReceiver<T, B> composeFirst(Executable<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b) -> receive(f.execute(t), b);
    }
    default <T> BiReceiver<A, T> composeSecond(Executable<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t) -> receive(a, f.execute(t));
    }

    default Executable<A, Receiver<B>> curried() {
        return a -> b -> receive(a, b);
    }

    default BiExecutable<A, B, A> yieldFirst() {
        return (a, b) -> {
            receive(a, b);
            return a;
        };
    }
    default BiExecutable<A, B, B> yieldSecond() {
        return (a, b) -> {
            receive(a, b);
            return b;
        };
    }
    default BiExecutable<A, B, Void> toExecutable() {
        return (a, b) -> {
            receive(a, b);
            return null;
        };
    }

    static <T, U> BiReceiver<T, U> retrying(RetryContext ctx, BiReceiver<T, U> f) {
        return f.withRetry(ctx);
    }

    static <T, P> BiReceiver<T, P> noOp() {
        return (x1, x2) -> {};
    }
}
