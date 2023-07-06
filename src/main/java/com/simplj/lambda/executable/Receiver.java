package com.simplj.lambda.executable;

import java.util.Objects;

@FunctionalInterface
public interface Receiver<A> {
    void receive(A input) throws Exception;

    default <T> Receiver<T> compose(Executable<T, A> before) {
        Objects.requireNonNull(before);
        return t -> receive(before.execute(t));
    }

    default Executable<A, A> yield() {
        return a -> {
            receive(a);
            return a;
        };
    }
}
