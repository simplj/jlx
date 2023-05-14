package com.simplj.lambda.executable;

import java.util.Objects;

@FunctionalInterface
public interface BiAbsorber<A, B> {
    void absorb(A inpA, B inpB) throws Exception;

    default <T> BiAbsorber<T, B> composeFirst(Executable<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b) -> absorb(f.execute(t), b);
    }
    default <T> BiAbsorber<A, T> composeSecond(Executable<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t) -> absorb(a, f.execute(t));
    }

    default Absorber<B> partial(A a) {
        return b -> absorb(a, b);
    }

    default Executable<A, Absorber<B>> curried() {
        return a -> b -> absorb(a, b);
    }

    default BiExecutable<A, B, A> chainFirst() {
        return (a, b) -> {
            absorb(a, b);
            return a;
        };
    }
    default BiExecutable<A, B, B> chainSecond() {
        return (a, b) -> {
            absorb(a, b);
            return b;
        };
    }
}
