package com.simplj.lambda.executable;

import java.util.Objects;

@FunctionalInterface
public interface TriExecutable<A, B, C, R> {
    R execute(A inpA, B inpB, C inpC) throws Exception;

    default <T> TriExecutable<T, B, C, R> composeFirst(Executable<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b, c) -> execute(f.execute(t), b, c);
    }
    default <T> TriExecutable<A, T, C, R> composeSecond(Executable<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t, c) -> execute(a, f.execute(t), c);
    }
    default <T> TriExecutable<A, B, T, R> composeThird(Executable<T, C> f) {
        Objects.requireNonNull(f);
        return (a, b, t) -> execute(a, b, f.execute(t));
    }

    default <T> TriExecutable<A, B, C, T> andThen(Executable<R, T> after) {
        Objects.requireNonNull(after);
        return (a, b, c) -> after.execute(execute(a, b, c));
    }

    default BiExecutable<B, C, R> partial(A a) {
        return (b, c) -> execute(a, b, c);
    }
    default Executable<C, R> partial(A a, B b) {
        return c -> execute(a, b, c);
    }

    default Executable<A, Executable<B, Executable<C, R>>> curried() {
        return a -> b -> c -> execute(a, b, c);
    }

    static <T, U, V> TriExecutable<T, U, V, T> first() {
        return (a, b, c) -> a;
    }
    static <T, U, V> TriExecutable<T, U, V, U> second() {
        return (a, b, c) -> b;
    }
    static <T, U, V> TriExecutable<T, U, V, V> third() {
        return (a, b, c) -> c;
    }
}
