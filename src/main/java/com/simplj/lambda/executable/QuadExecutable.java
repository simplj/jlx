package com.simplj.lambda.executable;

import com.simplj.lambda.function.QuadFunction;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.util.retry.RetryContext;

import java.util.Objects;

@FunctionalInterface
public interface QuadExecutable<A, B, C, D, R> {

    R execute(A a, B b, C c, D d) throws Exception;

    /**
     * Applies the QuadExecutable partially (that is why the name `exec` depicting partial `execute`-ing)
     * @param a the first argument to be applied
     * @return TriExecutable with the remaining arguments
     */
    default TriExecutable<B, C, D, R> exec(A a) {
        return (b, c, d) -> execute(a, b, c, d);
    }

    default QuadFunction<A, B, C, D, Either<Exception, R>> pure() {
        return (A a, B b, C c, D d) -> {
            Either<Exception, R> res;
            try {
                res = Either.right(execute(a, b, c, d));
            } catch (Exception ex) {
                res = Either.left(ex);
            }
            return res;
        };
    }

    /**
     * Attempts retry as per the given RetryContext when an exception is occurred during execution
     * @param ctx RetryContext containing max retry attempt, exceptions to retry (inclusive/exclusive) along with other supporting attributes.
     * @return QuadExecutable with retrying behavior as per the given RetryContext.
     */
    default QuadExecutable<A, B, C, D, R> withRetry(RetryContext ctx) {
        Objects.requireNonNull(ctx);
        return (a, b, c, d) -> ctx.retry(() -> execute(a, b, c, d));
    }

    default <T> QuadExecutable<T, B, C, D, R> composeFirst(Executable<T, A> after) {
        Objects.requireNonNull(after);
        return (t, b, c, d) -> execute(after.execute(t), b, c, d);
    }
    default <T> QuadExecutable<A, T, C, D, R> composeSecond(Executable<T, B> after) {
        Objects.requireNonNull(after);
        return (a, t, c, d) -> execute(a, after.execute(t), c, d);
    }
    default <T> QuadExecutable<A, B, T, D, R> composeThird(Executable<T, C> after) {
        Objects.requireNonNull(after);
        return (a, b, t, d) -> execute(a, b, after.execute(t), d);
    }
    default <T> QuadExecutable<A, B, C, T, R> composeFourth(Executable<T, D> after) {
        Objects.requireNonNull(after);
        return (a, b, c, t) -> execute(a, b, c, after.execute(t));
    }

    default <T> QuadExecutable<A, B, C, D, T> andThen(Executable<R, T> after) {
        Objects.requireNonNull(after);
        return (a, b, c, d) -> after.execute(execute(a, b, c, d));
    }

    default Executable<A, Executable<B, Executable<C, Executable<D, R>>>> curried() {
        return a -> b -> c -> d -> execute(a, b, c, d);
    }

    static <T, U, V, W, R> QuadExecutable<T, U, V, W, R> of(QuadExecutable<T, U, V, W, R> f) {
        return f;
    }

    static <P, Q, R, S> QuadExecutable<P, Q, R, S, P> first() {
        return (a, b, c, d) -> a;
    }
    static <P, Q, R, S> QuadExecutable<P, Q, R, S, Q> second() {
        return (a, b, c, d) -> b;
    }
    static <P, Q, R, S> QuadExecutable<P, Q, R, S, R> third() {
        return (a, b, c, d) -> c;
    }
    static <P, Q, R, S> QuadExecutable<P, Q, R, S, S> fourth() {
        return (a, b, c, d) -> d;
    }

    static <X1, X2, X3, X4, R> QuadExecutable<X1, X2, X3, X4, R> returning(R r) {
        return (x1, x2, x3, x4) -> r;
    }
}
