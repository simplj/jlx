package com.simplj.lambda.function;

import java.util.Objects;

@FunctionalInterface
public interface QuadFunction<A, B, C, D, R> {

    R apply(A a, B b, C c, D d);

    default <T> QuadFunction<T, B, C, D, R> composeFirst(Function<T, A> after) {
        Objects.requireNonNull(after);
        return (t, b, c, d) -> apply(after.apply(t), b, c, d);
    }
    default <T> QuadFunction<A, T, C, D, R> composeSecond(Function<T, B> after) {
        Objects.requireNonNull(after);
        return (a, t, c, d) -> apply(a, after.apply(t), c, d);
    }
    default <T> QuadFunction<A, B, T, D, R> composeThird(Function<T, C> after) {
        Objects.requireNonNull(after);
        return (a, b, t, d) -> apply(a, b, after.apply(t), d);
    }
    default <T> QuadFunction<A, B, C, T, R> composeFourth(Function<T, D> after) {
        Objects.requireNonNull(after);
        return (a, b, c, t) -> apply(a, b, c, after.apply(t));
    }

    default <T> QuadFunction<A, B, C, D, T> andThen(Function<R, T> after) {
        Objects.requireNonNull(after);
        return (a, b, c, d) -> after.apply(apply(a, b, c, d));
    }

    default TriFunction<B, C, D, R> partial(A a) {
        return (b, c, d) -> apply(a, b, c, d);
    }
    default BiFunction<C, D, R> partial(A a, B b) {
        return (c, d) -> apply(a, b, c, d);
    }
    /*
    TODO: Instead of partial(a, b, c), ap(a).ap(b).ap(c)
     */
    default Function<D, R> partial(A a, B b, C c) {
        return d -> apply(a, b, c, d);
    }

    default Function<A, Function<B, Function<C, Function<D, R>>>> curried() {
        return a -> b -> c -> d -> apply(a, b, c, d);
    }

    static <P, Q, R, S> QuadFunction<P, Q, R, S, P> first() {
        return (a, b, c, d) -> a;
    }
    static <P, Q, R, S> QuadFunction<P, Q, R, S, Q> second() {
        return (a, b, c, d) -> b;
    }
    static <P, Q, R, S> QuadFunction<P, Q, R, S, R> third() {
        return (a, b, c, d) -> c;
    }
    static <P, Q, R, S> QuadFunction<P, Q, R, S, S> fourth() {
        return (a, b, c, d) -> d;
    }
}
