package com.simplj.lambda.function;

import java.util.Objects;

@FunctionalInterface
public interface BiFunction<A, B, O> {
    O apply(A inpA, B inpB);

    default <T> BiFunction<T, B, O> composeFirst(Function<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b) -> apply(f.apply(t), b);
    }
    default <T> BiFunction<A, T, O> composeSecond(Function<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t) -> apply(a, f.apply(t));
    }

    default <R> BiFunction<A, B, R> andThen(Function<O, R> after) {
        Objects.requireNonNull(after);
        return (a, b) -> after.apply(apply(a, b));
    }

    default Function<B, O> partial(A a) {
        return b -> apply(a, b);
    }

    default Function<A, Function<B, O>> curried() {
        return a -> b -> apply(a, b);
    }

    static <T, R> BiFunction<T, R, T> first() {
        return (a, b) -> a;
    }
    static <T, R> BiFunction<T, R, R> second() {
        return (a, b) -> b;
    }
}
