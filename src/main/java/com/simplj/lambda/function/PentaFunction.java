package com.simplj.lambda.function;

import java.util.Objects;

@FunctionalInterface
public interface PentaFunction<A, B, C, D, E, R> {
    R apply(A a, B b, C c, D d, E e);

    default QuadFunction<B, C, D, E, R> ap(A a) {
        return (b, c, d, e) -> apply(a, b, c, d, e);
    }

    default <T> PentaFunction<T, B, C, D, E, R> composeFirst(Function<T, A> after) {
        Objects.requireNonNull(after);
        return (t, b, c, d, e) -> apply(after.apply(t), b, c, d, e);
    }
    default <T> PentaFunction<A, T, C, D, E, R> composeSecond(Function<T, B> after) {
        Objects.requireNonNull(after);
        return (a, t, c, d, e) -> apply(a, after.apply(t), c, d, e);
    }
    default <T> PentaFunction<A, B, T, D, E, R> composeThird(Function<T, C> after) {
        Objects.requireNonNull(after);
        return (a, b, t, d, e) -> apply(a, b, after.apply(t), d, e);
    }
    default <T> PentaFunction<A, B, C, T, E, R> composeFourth(Function<T, D> after) {
        Objects.requireNonNull(after);
        return (a, b, c, t, e) -> apply(a, b, c, after.apply(t), e);
    }
    default <T> PentaFunction<A, B, C, D, T, R> composeFifth(Function<T, E> after) {
        Objects.requireNonNull(after);
        return (a, b, c, d, t) -> apply(a, b, c, d, after.apply(t));
    }

    default <T> PentaFunction<A, B, C, D, E, T> andThen(Function<R, T> after) {
        Objects.requireNonNull(after);
        return (a, b, c, d, e) -> after.apply(apply(a, b, c, d, e));
    }

    default Function<A, Function<B, Function<C, Function<D, Function<E, R>>>>> curried() {
        return a -> b -> c -> d -> e -> apply(a, b, c, d, e);
    }

    static <T, U, V, W, X, R> PentaFunction<T, U, V, W, X, R> of(PentaFunction<T, U, V, W, X, R> f) {
        return f;
    }

    static <P, Q, R, S, T> PentaFunction<P, Q, R, S, T, P> first() {
        return (a, b, c, d, e) -> a;
    }
    static <P, Q, R, S, T> PentaFunction<P, Q, R, S, T, Q> second() {
        return (a, b, c, d, e) -> b;
    }
    static <P, Q, R, S, T> PentaFunction<P, Q, R, S, T, R> third() {
        return (a, b, c, d, e) -> c;
    }
    static <P, Q, R, S, T> PentaFunction<P, Q, R, S, T, S> fourth() {
        return (a, b, c, d, e) -> d;
    }
    static <P, Q, R, S, T> PentaFunction<P, Q, R, S, T, T> fifth() {
        return (a, b, c, d, e) -> e;
    }

    static <X1, X2, X3, X4, X5, R> PentaFunction<X1, X2, X3, X4, X5, R> returning(R r) {
        return (x1, x2, x3, x4, x5) -> r;
    }
}
