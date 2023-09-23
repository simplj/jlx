package com.simplj.lambda.executable;

import com.simplj.lambda.function.Producer;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.util.retry.RetryContext;
import com.simplj.lambda.util.Try;

import java.util.Objects;

@FunctionalInterface
public interface Provider<R> {
    R provide() throws Exception;

    default Producer<Either<Exception, R>> pure() {
        return () -> Try.execute(this).result();
    }

    /**
     * Attempts retry as per the given RetryContext when an exception is occurred during execution
     * @param ctx RetryContext containing max retry attempt, exceptions to retry (inclusive/exclusive) along with other supporting attributes.
     * @return Provider with retrying behavior as per the given RetryContext.
     */
    default Provider<R> withRetry(RetryContext ctx) {
        Objects.requireNonNull(ctx);
        return () -> ctx.retry(this);
    }

    default <A> Provider<A> andThen(Executable<R, A> after) {
        Objects.requireNonNull(after);
        return () -> after.execute(provide());
    }

    default <X> Executable<X, R> toExecutable() {
        return x -> provide();
    }

    static <R> Provider<R> of(Provider<R> f) {
        return f;
    }

    static <T> Provider<T> defer(T val) {
        return () -> val;
    }
}
