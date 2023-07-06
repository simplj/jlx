package com.simplj.lambda.executable;

import java.util.Objects;

@FunctionalInterface
public interface BiReceiver<A, B> {
    void receive(A inpA, B inpB) throws Exception;

    default <T> BiReceiver<T, B> composeFirst(Executable<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b) -> receive(f.execute(t), b);
    }
    default <T> BiReceiver<A, T> composeSecond(Executable<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t) -> receive(a, f.execute(t));
    }

    default Receiver<B> partial(A a) {
        return b -> receive(a, b);
    }

    default Executable<A, Receiver<B>> curried() {
        return a -> b -> receive(a, b);
    }

    default BiExecutable<A, B, A> yieldFirst() {
        return (a, b) -> {
            receive(a, b);
            return a;
        };
    }
    default BiExecutable<A, B, B> yieldSecond() {
        return (a, b) -> {
            receive(a, b);
            return b;
        };
    }
}
