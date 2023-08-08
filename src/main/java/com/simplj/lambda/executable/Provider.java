package com.simplj.lambda.executable;

import java.util.Objects;

@FunctionalInterface
public interface Provider<R> {
    R provide() throws Exception;

    default <A> Provider<A> andThen(Executable<R, A> after) {
        Objects.requireNonNull(after);
        return () -> after.execute(provide());
    }

    default <X> Executable<X, R> toExecutable() {
        return x -> provide();
    }

    static <T> Provider<T> wrap(T val) {
        return () -> val;
    }
}
