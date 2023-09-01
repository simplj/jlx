package com.simplj.lambda.executable;

import com.simplj.lambda.util.RetryContext;

import java.util.Objects;

@FunctionalInterface
public interface Excerpt {
    void execute() throws Exception;

    /**
     * Attempts retry as per the given RetryContext when an exception is occurred during execution
     * @param ctx RetryContext containing max retry attempt, exceptions to retry (inclusive/exclusive) along with other supporting attributes.
     * @return Excerpt with retrying behavior as per the given RetryContext.
     */
    default Excerpt withRetry(RetryContext ctx) {
        Objects.requireNonNull(ctx);
        return () -> ctx.retry(this);
    }

    default <X> Executable<X, Void> toExecutable() {
        return x -> {
            execute();
            return null;
        };
    }
    default <X> Executable<X, X> yield() {
        return x -> {
            execute();
            return x;
        };
    }

    default <X> Receiver<X> toReceiver() {
        return x -> execute();
    }

    default <X> Provider<X> toProvider() {
        return () -> {
            execute();
            return null;
        };
    }

    static Excerpt retrying(RetryContext ctx, Excerpt f) {
        return f.withRetry(ctx);
    }
}
