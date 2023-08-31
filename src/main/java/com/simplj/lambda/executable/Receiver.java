package com.simplj.lambda.executable;

import com.simplj.lambda.util.RetryContext;

import java.util.Objects;

@FunctionalInterface
public interface Receiver<A> {
    void receive(A input) throws Exception;

    /**
     * Applies the Receiver partially (that is why the name `re` depicting partial `receive`-ing)
     * @param a argument to be applied
     * @return Excerpt with the Receiver functionality wrapped
     */
    default Excerpt re(A a) {
        return () -> receive(a);
    }

    /**
     * Attempts retry as per the given RetryContext when an exception is occurred during execution
     * @param ctx RetryContext containing max retry attempt, exceptions to retry (inclusive/exclusive) along with other supporting attributes.
     * @return Receiver with retrying behavior as per the given RetryContext.
     */
    default Receiver<A> withRetry(RetryContext ctx) {
        Objects.requireNonNull(ctx);
        return a -> ctx.retry(() -> receive(a));
    }

    default <T> Receiver<T> compose(Executable<T, A> before) {
        Objects.requireNonNull(before);
        return t -> receive(before.execute(t));
    }

    default Executable<A, A> yield() {
        return a -> {
            receive(a);
            return a;
        };
    }
    default Executable<A, Void> toExecutable() {
        return a -> {
            receive(a);
            return null;
        };
    }

    static <T> Receiver<T> retrying(RetryContext ctx, Receiver<T> f) {
        return f.withRetry(ctx);
    }
}
