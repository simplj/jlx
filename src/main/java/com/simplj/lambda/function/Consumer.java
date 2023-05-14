package com.simplj.lambda.function;

import java.util.Objects;

@FunctionalInterface
public interface Consumer<A> {
    void consume(A input);

    default <T> Consumer<T> compose(Function<T, A> before) {
        Objects.requireNonNull(before);
        return t -> consume(before.apply(t));
    }

    default Function<A, A> chain() {
        return a -> {
            consume(a);
            return a;
        };
    }
}
