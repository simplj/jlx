package com.simplj.lambda.executable;

import java.util.Objects;

@FunctionalInterface
public interface TriReceiver<A, B, C> {
    void receive(A inpA, B inpB, C inpC) throws Exception;

    default <T> TriReceiver<T, B, C> composeFirst(Executable<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b, c) -> receive(f.execute(t), b, c);
    }
    default <T> TriReceiver<A, T, C> composeSecond(Executable<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t, c) -> receive(a, f.execute(t), c);
    }
    default <T> TriReceiver<A, B, T> composeThird(Executable<T, C> f) {
        Objects.requireNonNull(f);
        return (a, b, t) -> receive(a, b, f.execute(t));
    }

    default BiReceiver<B, C> partial(A a) {
        return (b, c) -> receive(a, b, c);
    }
    default Receiver<C> partial(A a, B b) {
        return c -> receive(a, b, c);
    }

    default Executable<A, Executable<B, Receiver<C>>> curried() {
        return a -> b -> c -> receive(a, b, c);
    }

    default TriExecutable<A, B, C, A> yieldFirst() {
        return (a, b, c) -> {
            receive(a, b, c);
            return a;
        };
    }
    default TriExecutable<A, B, C, B> yieldSecond() {
        return (a, b, c) -> {
            receive(a, b, c);
            return b;
        };
    }
    default TriExecutable<A, B, C, C> yieldThird() {
        return (a, b, c) -> {
            receive(a, b, c);
            return c;
        };
    }
}
