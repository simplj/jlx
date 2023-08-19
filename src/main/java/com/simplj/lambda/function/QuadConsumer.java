package com.simplj.lambda.function;

import java.util.Objects;

@FunctionalInterface
public interface QuadConsumer<A, B, C, D> {
    void consume(A inpA, B inpB, C inpC, D inpD);

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

    default TriConsumer<B, C, D> partial(A a) {
        return (b, c, d) -> consume(a, b, c, d);
    }
    default BiConsumer<C, D> partial(A a, B b) {
        return (c, d) -> consume(a, b, c, d);
    }
    default Consumer<D> partial(A a, B b, C c) {
        return d -> consume(a, b, c, d);
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
}
