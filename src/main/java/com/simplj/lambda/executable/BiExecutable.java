package com.simplj.lambda.executable;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.util.retry.RetryContext;

import java.util.Objects;

@FunctionalInterface
public interface BiExecutable<A, B, R> {
    R execute(A inpA, B inpB) throws Exception;

    /**
     * Applies the BiExecutable partially (that is why the name `exec` depicting partial `execute`-ing)
     * @param a the first argument to be applied
     * @return Executable with the other argument
     */
    default Executable<B, R> exec(A a) {
        return b -> execute(a, b);
    }

    default BiFunction<A, B, Either<Exception, R>> pure() {
        return (A a, B b) -> {
            Either<Exception, R> res;
            try {
                res = Either.right(execute(a, b));
            } catch (Exception ex) {
                res = Either.left(ex);
            }
            return res;
        };
    }

    /**
     * Attempts retry as per the given RetryContext when an exception is occurred during execution
     * @param ctx RetryContext containing max retry attempt, exceptions to retry (inclusive/exclusive) along with other supporting attributes.
     * @return BiExecutable with retrying behavior as per the given RetryContext.
     */
    default BiExecutable<A, B, R> withRetry(RetryContext ctx) {
        Objects.requireNonNull(ctx);
        return (a, b) -> ctx.retry(() -> execute(a, b));
    }

    default <T> BiExecutable<T, B, R> composeFirst(Executable<T, A> f) {
        return (t, b) -> execute(f.execute(t), b);
    }
    default <T> BiExecutable<A, T, R> composeSecond(Executable<T, B> f) {
        return (a, t) -> execute(a, f.execute(t));
    }

    default <T> BiExecutable<A, B, T> andThen(Executable<R, T> f) {
        return (a, b) -> f.execute(execute(a, b));
    }

    default Executable<A, Executable<B, R>> curried() {
        return a -> b -> execute(a, b);
    }

    static <T, U, R> BiExecutable<T, U, R> of(BiExecutable<T, U, R> f) {
        return f;
    }

    static <T, U> BiExecutable<T, U, T> first() {
        return (a, b) -> a;
    }
    static <T, U> BiExecutable<T, U, U> second() {
        return (a, b) -> b;
    }

    static <X1, X2, R> BiExecutable<X1, X2, R> returning(R r) {
        return (x1, x2) -> r;
    }
}
