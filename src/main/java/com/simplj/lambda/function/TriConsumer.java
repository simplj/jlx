package com.simplj.lambda.function;

import java.util.Objects;

@FunctionalInterface
public interface TriConsumer<A, B, C> {
    void consume(A inpA, B inpB, C inpC);

    /**
     * Applies the TriConsumer partially (that is why the name `cons` depicting partial `consume`-ing)
     * @param a argument to be applied
     * @return BiConsumer with the remaining arguments
     */
    default BiConsumer<B, C> cons(A a) {
        return (b, c) -> consume(a, b, c);
    }

    default <T> TriConsumer<T, B, C> composeFirst(Function<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b, c) -> consume(f.apply(t), b, c);
    }
    default <T> TriConsumer<A, T, C> composeSecond(Function<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t, c) -> consume(a, f.apply(t), c);
    }
    default <T> TriConsumer<A, B, T> composeThird(Function<T, C> f) {
        Objects.requireNonNull(f);
        return (a, b, t) -> consume(a, b, f.apply(t));
    }

    default Function<A, Function<B, Consumer<C>>> curried() {
        return a -> b -> c -> consume(a, b, c);
    }

    default TriFunction<A, B, C, A> yieldFirst() {
        return (a, b, c) -> {
            consume(a, b, c);
            return a;
        };
    }
    default TriFunction<A, B, C, B> yieldSecond() {
        return (a, b, c) -> {
            consume(a, b, c);
            return b;
        };
    }
    default TriFunction<A, B, C, C> yieldThird() {
        return (a, b, c) -> {
            consume(a, b, c);
            return c;
        };
    }

    static <T, P, Q> TriConsumer<T, P, Q> noOp() {
        return (x1, x2, x3) -> {};
    }
}
