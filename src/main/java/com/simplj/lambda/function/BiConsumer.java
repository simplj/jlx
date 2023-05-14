package com.simplj.lambda.function;

import java.util.Objects;

@FunctionalInterface
public interface BiConsumer<A, B> {
    void consume(A inpA, B inpB);

    default <T> BiConsumer<T, B> composeFirst(Function<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b) -> consume(f.apply(t), b);
    }
    default <T> BiConsumer<A, T> composeSecond(Function<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t) -> consume(a, f.apply(t));
    }

    default Consumer<B> partial(A a) {
        return b -> consume(a, b);
    }

    default Function<A, Consumer<B>> curried() {
        return a -> b -> consume(a, b);
    }

    default BiFunction<A, B, A> chainFirst() {
        return (a, b) -> {
            consume(a, b);
            return a;
        };
    }
    default BiFunction<A, B, B> chainSecond() {
        return (a, b) -> {
            consume(a, b);
            return b;
        };
    }
}
