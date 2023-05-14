package com.simplj.lambda.executable;

import java.util.Objects;

@FunctionalInterface
public interface Absorber<A> {
    void absorb(A input) throws Exception;

    default <T> Absorber<T> compose(Executable<T, A> before) {
        Objects.requireNonNull(before);
        return t -> absorb(before.execute(t));
    }

    default Executable<A, A> chain() {
        return a -> {
            absorb(a);
            return a;
        };
    }
}
