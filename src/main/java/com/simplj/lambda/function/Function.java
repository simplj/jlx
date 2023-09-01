package com.simplj.lambda.function;

import java.util.Objects;

@FunctionalInterface
public interface Function<I, O> {
    O apply(I input);

    /**
     * Applies the Function partially (that is why the name `ap` depicting partial `apply`-ing)
     * @param i argument to be applied
     * @return Producer having this `Function`'s output
     */
    default Producer<O> ap(I i) {
        return () -> apply(i);
    }

    default <T> Function<T, O> compose(Function<T, I> before) {
        Objects.requireNonNull(before);
        return before.andThen(this);
    }
    default <R> Function<I, R> andThen(Function<O, R> after) {
        Objects.requireNonNull(after);
        return (I i) -> after.apply(this.apply(i));
    }

    static <T> Function<T, T> id() {
        return t -> t;
    }
}
