package com.simplj.lambda.executable;

import java.util.Objects;

@FunctionalInterface
public interface Executable<I, O> {
    O execute(I input) throws Exception;

    default <T> Executable<T, O> compose(Executable<T, I> before) {
        Objects.requireNonNull(before);
        return before.andThen(this);
    }
    default <R> Executable<I, R> andThen(Executable<O, R> after) {
        Objects.requireNonNull(after);
        return (I i) -> after.execute(this.execute(i));
    }

    static <T> Executable<T, T> id() {
        return t -> t;
    }
}
