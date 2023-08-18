package com.simplj.lambda.function;

import java.util.Objects;

@FunctionalInterface
public interface Producer<R> {
    R produce();

    default <A> Producer<A> andThen(Function<R, A> after) {
        Objects.requireNonNull(after);
        return () -> after.apply(produce());
    }

    default Function<Void, R> toFunction() {
        return x -> produce();
    }

    static <T> Producer<T> defer(T val) {
        return () -> val;
    }
}
