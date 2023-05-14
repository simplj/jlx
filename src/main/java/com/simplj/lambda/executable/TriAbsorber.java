package com.simplj.lambda.executable;

import java.util.Objects;

@FunctionalInterface
public interface TriAbsorber<A, B, C> {
    void absorb(A inpA, B inpB, C inpC) throws Exception;

    default <T> TriAbsorber<T, B, C> composeFirst(Executable<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b, c) -> absorb(f.execute(t), b, c);
    }
    default <T> TriAbsorber<A, T, C> composeSecond(Executable<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t, c) -> absorb(a, f.execute(t), c);
    }
    default <T> TriAbsorber<A, B, T> composeThird(Executable<T, C> f) {
        Objects.requireNonNull(f);
        return (a, b, t) -> absorb(a, b, f.execute(t));
    }

    default BiAbsorber<B, C> partial(A a) {
        return (b, c) -> absorb(a, b, c);
    }
    default Absorber<C> partial(A a, B b) {
        return c -> absorb(a, b, c);
    }

    default Executable<A, Executable<B, Absorber<C>>> curried() {
        return a -> b -> c -> absorb(a, b, c);
    }

    default TriExecutable<A, B, C, A> chainFirst() {
        return (a, b, c) -> {
            absorb(a, b, c);
            return a;
        };
    }
    default TriExecutable<A, B, C, B> chainSecond() {
        return (a, b, c) -> {
            absorb(a, b, c);
            return b;
        };
    }
    default TriExecutable<A, B, C, C> chainThird() {
        return (a, b, c) -> {
            absorb(a, b, c);
            return c;
        };
    }
}
