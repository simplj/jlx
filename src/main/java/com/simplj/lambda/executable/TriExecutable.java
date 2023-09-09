package com.simplj.lambda.executable;

import com.simplj.lambda.function.TriFunction;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.util.RetryContext;

import java.util.Objects;

@FunctionalInterface
public interface TriExecutable<A, B, C, R> {
    R execute(A inpA, B inpB, C inpC) throws Exception;

    /**
     * Applies the TriExecutable partially (that is why the name `exec` depicting partial `execute`-ing)
     * @param a the first argument to be applied
     * @return BiExecutable with the remaining arguments
     */
    default BiExecutable<B, C, R> exec(A a) {
        return (b, c) -> execute(a, b, c);
    }

    default TriFunction<A, B, C, Either<Exception, R>> pure() {
        return (A a, B b, C c) -> {
            Either<Exception, R> res;
            try {
                res = Either.right(execute(a, b, c));
            } catch (Exception ex) {
                res = Either.left(ex);
            }
            return res;
        };
    }

    /**
     * Attempts retry as per the given RetryContext when an exception is occurred during execution
     * @param ctx RetryContext containing max retry attempt, exceptions to retry (inclusive/exclusive) along with other supporting attributes.
     * @return TriExecutable with retrying behavior as per the given RetryContext.
     */
    default TriExecutable<A, B, C, R> withRetry(RetryContext ctx) {
        Objects.requireNonNull(ctx);
        return (a, b, c) -> ctx.retry(() -> execute(a, b, c));
    }

    default <T> TriExecutable<T, B, C, R> composeFirst(Executable<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b, c) -> execute(f.execute(t), b, c);
    }
    default <T> TriExecutable<A, T, C, R> composeSecond(Executable<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t, c) -> execute(a, f.execute(t), c);
    }
    default <T> TriExecutable<A, B, T, R> composeThird(Executable<T, C> f) {
        Objects.requireNonNull(f);
        return (a, b, t) -> execute(a, b, f.execute(t));
    }

    default <T> TriExecutable<A, B, C, T> andThen(Executable<R, T> after) {
        Objects.requireNonNull(after);
        return (a, b, c) -> after.execute(execute(a, b, c));
    }

    default Executable<A, Executable<B, Executable<C, R>>> curried() {
        return a -> b -> c -> execute(a, b, c);
    }

    static <T, U, V, R> TriExecutable<T, U, V, R> retrying(RetryContext ctx, TriExecutable<T, U, V, R> f) {
        return f.withRetry(ctx);
    }

    static <T, U, V> TriExecutable<T, U, V, T> first() {
        return (a, b, c) -> a;
    }
    static <T, U, V> TriExecutable<T, U, V, U> second() {
        return (a, b, c) -> b;
    }
    static <T, U, V> TriExecutable<T, U, V, V> third() {
        return (a, b, c) -> c;
    }

    static <T, P, Q> TriReceiver<T, P, Q> noOp() {
        return (x1, x2, x3) -> {};
    }
}
