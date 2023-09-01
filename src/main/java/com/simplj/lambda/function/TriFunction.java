package com.simplj.lambda.function;

import java.util.Objects;

@FunctionalInterface
public interface TriFunction<A, B, C, R> {

    R apply(A a, B b, C c);

    /**
     * Applies the TriFunction partially (that is why the name `ap` depicting partial `apply`-ing)
     * @param a the first argument to be applied
     * @return BiFunction with the remaining arguments
     */
    default BiFunction<B, C, R> ap(A a) {
        return (b, c) -> apply(a, b, c);
    }

    default <T> TriFunction<T, B, C, R> composeFirst(Function<T, A> after) {
        Objects.requireNonNull(after);
        return (t, b, c) -> apply(after.apply(t), b, c);
    }
    default <T> TriFunction<A, T, C, R> composeSecond(Function<T, B> after) {
        Objects.requireNonNull(after);
        return (a, t, c) -> apply(a, after.apply(t), c);
    }
    default <T> TriFunction<A, B, T, R> composeThird(Function<T, C> after) {
        Objects.requireNonNull(after);
        return (a, b, t) -> apply(a, b, after.apply(t));
    }

    default <T> TriFunction<A, B, C, T> andThen(Function<R, T> after) {
        Objects.requireNonNull(after);
        return (a, b, c) -> after.apply(apply(a, b, c));
    }

    default Function<A, Function<B, Function<C, R>>> curried() {
        return a -> b -> c -> apply(a, b, c);
    }

    static <P, Q, R> TriFunction<P, Q, R, P> first() {
        return (a, b, c) -> a;
    }
    static <P, Q, R> TriFunction<P, Q, R, Q> second() {
        return (a, b, c) -> b;
    }
    static <P, Q, R> TriFunction<P, Q, R, R> third() {
        return (a, b, c) -> c;
    }
}
