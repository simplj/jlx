package com.simplj.lambda.function;

import java.util.Objects;

@FunctionalInterface
public interface QuadConsumer<A, B, C, D> {
    void consume(A inpA, B inpB, C inpC, D inpD);

    /**
     * Applies the QuadConsumer partially (that is why the name `cons` depicting partial `consume`-ing)
     * @param a argument to be applied
     * @return TriConsumer with the remaining arguments
     */
    default TriConsumer<B, C, D> cons(A a) {
        return (b, c, d) -> consume(a, b, c, d);
    }

    default <T> QuadConsumer<T, B, C, D> composeFirst(Function<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b, c, d) -> consume(f.apply(t), b, c, d);
    }
    default <T> QuadConsumer<A, T, C, D> composeSecond(Function<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t, c, d) -> consume(a, f.apply(t), c, d);
    }
    default <T> QuadConsumer<A, B, T, D> composeThird(Function<T, C> f) {
        Objects.requireNonNull(f);
        return (a, b, t, d) -> consume(a, b, f.apply(t), d);
    }
    default <T> QuadConsumer<A, B, C, T> composeFourth(Function<T, D> f) {
        Objects.requireNonNull(f);
        return (a, b, c, t) -> consume(a, b, c, f.apply(t));
    }

    default Function<A, Function<B, Function<C, Consumer<D>>>> curried() {
        return a -> b -> c -> d -> consume(a, b, c, d);
    }

    default QuadFunction<A, B, C, D, A> yieldFirst() {
        return (a, b, c, d) -> {
            consume(a, b, c, d);
            return a;
        };
    }
    default QuadFunction<A, B, C, D, B> yieldSecond() {
        return (a, b, c, d) -> {
            consume(a, b, c, d);
            return b;
        };
    }
    default QuadFunction<A, B, C, D, C> yieldThird() {
        return (a, b, c, d) -> {
            consume(a, b, c, d);
            return c;
        };
    }
    default QuadFunction<A, B, C, D, D> yieldFourth() {
        return (a, b, c, d) -> {
            consume(a, b, c, d);
            return d;
        };
    }

    default QuadFunction<A, B, C, D, Void> toFunction() {
        return (a, b, c, d) -> {
            consume(a, b, c, d);
            return null;
        };
    }

    static <T, U, V, W> QuadConsumer<T, U, V, W> of(QuadConsumer<T, U, V, W> f) {
        return f;
    }

    static <T, P, Q, R> QuadConsumer<T, P, Q, R> noOp() {
        return (x1, x2, x3, x4) -> {};
    }
}
