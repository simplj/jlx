package com.simplj.lambda.function;

import java.util.Objects;

@FunctionalInterface
public interface PentaConsumer<A, B, C, D, E> {
    void consume(A inpA, B inpB, C inpC, D inpD, E inpE);

    /**
     * Applies the PentaConsumer partially (that is why the name `cons` depicting partial `consume`-ing)
     * @param a argument to be applied
     * @return QuadConsumer with the remaining arguments
     */
    default QuadConsumer<B, C, D, E> cons(A a) {
        return (b, c, d, e) -> consume(a, b, c, d, e);
    }

    default <T> PentaConsumer<T, B, C, D, E> composeFirst(Function<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b, c, d, e) -> consume(f.apply(t), b, c, d, e);
    }
    default <T> PentaConsumer<A, T, C, D, E> composeSecond(Function<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t, c, d, e) -> consume(a, f.apply(t), c, d, e);
    }
    default <T> PentaConsumer<A, B, T, D, E> composeThird(Function<T, C> f) {
        Objects.requireNonNull(f);
        return (a, b, t, d, e) -> consume(a, b, f.apply(t), d, e);
    }
    default <T> PentaConsumer<A, B, C, T, E> composeFourth(Function<T, D> f) {
        Objects.requireNonNull(f);
        return (a, b, c, t, e) -> consume(a, b, c, f.apply(t), e);
    }
    default <T> PentaConsumer<A, B, C, D, T> composeFifth(Function<T, E> f) {
        Objects.requireNonNull(f);
        return (a, b, c, d, t) -> consume(a, b, c, d, f.apply(t));
    }

    default Function<A, Function<B, Function<C, Function<D, Consumer<E>>>>> curried() {
        return a -> b -> c -> d -> e -> consume(a, b, c, d, e);
    }

    default PentaFunction<A, B, C, D, E, A> yieldFirst() {
        return (a, b, c, d, e) -> {
            consume(a, b, c, d, e);
            return a;
        };
    }
    default PentaFunction<A, B, C, D, E, B> yieldSecond() {
        return (a, b, c, d, e) -> {
            consume(a, b, c, d, e);
            return b;
        };
    }
    default PentaFunction<A, B, C, D, E, C> yieldThird() {
        return (a, b, c, d, e) -> {
            consume(a, b, c, d, e);
            return c;
        };
    }
    default PentaFunction<A, B, C, D, E, D> yieldFourth() {
        return (a, b, c, d, e) -> {
            consume(a, b, c, d, e);
            return d;
        };
    }
    default PentaFunction<A, B, C, D, E, E> yieldFifth() {
        return (a, b, c, d, e) -> {
            consume(a, b, c, d, e);
            return e;
        };
    }

    default PentaFunction<A, B, C, D, E, Void> toFunction() {
        return (a, b, c, d, e) -> {
            consume(a, b, c, d, e);
            return null;
        };
    }

    static <T, U, V, W, X> PentaConsumer<T, U, V, W, X> of(PentaConsumer<T, U, V, W, X> f) {
        return f;
    }

    static <T, P, Q, R, S> PentaConsumer<T, P, Q, R, S> noOp() {
        return (x1, x2, x3, x4, x5) -> {};
    }
}
