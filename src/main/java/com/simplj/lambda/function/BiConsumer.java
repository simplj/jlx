package com.simplj.lambda.function;

import java.util.Objects;

@FunctionalInterface
public interface BiConsumer<A, B> {
    void consume(A inpA, B inpB);

    /**
     * Applies the BiConsumer partially (that is why the name `cons` depicting partial `consume`-ing)
     * @param a argument to be applied
     * @return Consumer with the other argument
     */
    default Consumer<B> cons(A a) {
        return b -> consume(a, b);
    }

    default <T> BiConsumer<T, B> composeFirst(Function<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b) -> consume(f.apply(t), b);
    }
    default <T> BiConsumer<A, T> composeSecond(Function<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t) -> consume(a, f.apply(t));
    }

    default Function<A, Consumer<B>> curried() {
        return a -> b -> consume(a, b);
    }

    default BiFunction<A, B, A> yieldFirst() {
        return (a, b) -> {
            consume(a, b);
            return a;
        };
    }
    default BiFunction<A, B, B> yieldSecond() {
        return (a, b) -> {
            consume(a, b);
            return b;
        };
    }

    static <T, U> BiConsumer<T, U> of(BiConsumer<T, U> f) {
        return f;
    }

    static <T, U> BiConsumer<T, U> noOp() {
        return (x1, x2) -> {};
    }
}
