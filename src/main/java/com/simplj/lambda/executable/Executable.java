package com.simplj.lambda.executable;

import com.simplj.lambda.function.Function;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.util.RetryContext;
import com.simplj.lambda.util.ResettableRetryContext;

import java.util.Objects;

@FunctionalInterface
public interface Executable<I, O> {
    O execute(I input) throws Exception;

    /**
     * Applies the Executable partially (that is why the name `exec` depicting partial `execute`-ing)
     * @param i argument to be applied
     * @return Provider having this `Executable`'s output
     */
    default Provider<O> exec(I i) {
        return () -> execute(i);
    }

    default Function<I, Either<Exception, O>> pure() {
        return (I i) -> {
            Either<Exception, O> res;
            try {
                res = Either.right(execute(i));
            } catch (Exception ex) {
                res = Either.left(ex);
            }
            return res;
        };
    }

    /**
     * Attempts retry as per the given RetryContext when an exception is occurred during execution
     * @param ctx RetryContext containing max retry attempt, exceptions to retry (inclusive/exclusive) along with other supporting attributes.
     * @return Executable with retrying behavior as per the given RetryContext.
     */
    default Executable<I, O> withRetry(RetryContext ctx) {
        Objects.requireNonNull(ctx);
        return i -> ctx.retry(exec(i));
    }
    default Executable<I, O> withRetry(ResettableRetryContext<I> ctx) {
        Objects.requireNonNull(ctx);
        return i -> ctx.retry(this, i);
    }

    default <T> Executable<T, O> compose(Executable<T, I> before) {
        Objects.requireNonNull(before);
        return before.andThen(this);
    }
    default <R> Executable<I, R> andThen(Executable<O, R> after) {
        Objects.requireNonNull(after);
        return (I i) -> after.execute(this.execute(i));
    }

    static <T, R> Executable<T, R> retrying(RetryContext ctx, Executable<T, R> f) {
        return f.withRetry(ctx);
    }
    static <T, R> Executable<T, R> retrying(ResettableRetryContext<T> ctx, Executable<T, R> f) {
        return f.withRetry(ctx);
    }

    static <T> Executable<T, T> id() {
        return t -> t;
    }
}
