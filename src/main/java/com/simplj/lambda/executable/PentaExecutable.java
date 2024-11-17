package com.simplj.lambda.executable;

import com.simplj.lambda.function.PentaFunction;
import com.simplj.lambda.function.QuadFunction;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.util.retry.RetryContext;

import java.util.Objects;

@FunctionalInterface
public interface PentaExecutable<A, B, C, D, E, R> {

    R execute(A a, B b, C c, D d, E e) throws Exception;

    /**
     * Applies the PentaExecutable partially (that is why the name `exec` depicting partial `execute`-ing)
     * @param a the first argument to be applied
     * @return QuadExecutable with the remaining arguments
     */
    default QuadExecutable<B, C, D, E, R> exec(A a) {
        return (b, c, d, e) -> execute(a, b, c, d, e);
    }

    default PentaFunction<A, B, C, D, E, Either<Exception, R>> pure() {
        return (A a, B b, C c, D d, E e) -> {
            Either<Exception, R> res;
            try {
                res = Either.right(execute(a, b, c, d, e));
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
    default PentaExecutable<A, B, C, D, E, R> withRetry(RetryContext ctx) {
        Objects.requireNonNull(ctx);
        return (a, b, c, d, e) -> ctx.retry(() -> execute(a, b, c, d, e));
    }

    default <T> PentaExecutable<T, B, C, D, E, R> composeFirst(Executable<T, A> after) {
        Objects.requireNonNull(after);
        return (t, b, c, d, e) -> execute(after.execute(t), b, c, d, e);
    }
    default <T> PentaExecutable<A, T, C, D, E, R> composeSecond(Executable<T, B> after) {
        Objects.requireNonNull(after);
        return (a, t, c, d, e) -> execute(a, after.execute(t), c, d, e);
    }
    default <T> PentaExecutable<A, B, T, D, E, R> composeThird(Executable<T, C> after) {
        Objects.requireNonNull(after);
        return (a, b, t, d, e) -> execute(a, b, after.execute(t), d, e);
    }
    default <T> PentaExecutable<A, B, C, T, E, R> composeFourth(Executable<T, D> after) {
        Objects.requireNonNull(after);
        return (a, b, c, t, e) -> execute(a, b, c, after.execute(t), e);
    }
    default <T> PentaExecutable<A, B, C, D, T, R> composeFifth(Executable<T, E> after) {
        Objects.requireNonNull(after);
        return (a, b, c, d, t) -> execute(a, b, c, d, after.execute(t));
    }

    default <T> PentaExecutable<A, B, C, D, E, T> andThen(Executable<R, T> after) {
        Objects.requireNonNull(after);
        return (a, b, c, d, e) -> after.execute(execute(a, b, c, d, e));
    }

    default Executable<A, Executable<B, Executable<C, Executable<D, Executable<E, R>>>>> curried() {
        return a -> b -> c -> d -> e -> execute(a, b, c, d, e);
    }

    static <T, U, V, W, X, R> PentaExecutable<T, U, V, W, X, R> of(PentaExecutable<T, U, V, W, X, R> f) {
        return f;
    }

    static <P, Q, R, S, T> PentaExecutable<P, Q, R, S, T, P> first() {
        return (a, b, c, d, e) -> a;
    }
    static <P, Q, R, S, T> PentaExecutable<P, Q, R, S, T, Q> second() {
        return (a, b, c, d, e) -> b;
    }
    static <P, Q, R, S, T> PentaExecutable<P, Q, R, S, T, R> third() {
        return (a, b, c, d, e) -> c;
    }
    static <P, Q, R, S, T> PentaExecutable<P, Q, R, S, T, S> fourth() {
        return (a, b, c, d, e) -> d;
    }
    static <P, Q, R, S, T> PentaExecutable<P, Q, R, S, T, T> fifth() {
        return (a, b, c, d, e) -> e;
    }

    static <X1, X2, X3, X4, X5, R> PentaExecutable<X1, X2, X3, X4, X5, R> returning(R r) {
        return (x1, x2, x3, x4, x5) -> r;
    }
}
