package com.simplj.lambda.executable;

@FunctionalInterface
public interface BiExecutable<A, B, R> {
    R execute(A inpA, B inpB) throws Exception;

    default <T> BiExecutable<T, B, R> composeFirst(Executable<T, A> f) {
        return (t, b) -> execute(f.execute(t), b);
    }
    default <T> BiExecutable<A, T, R> composeSecond(Executable<T, B> f) {
        return (a, t) -> execute(a, f.execute(t));
    }

    default <T> BiExecutable<A, B, T> andThen(Executable<R, T> f) {
        return (a, b) -> f.execute(execute(a, b));
    }

    default Executable<B, R> partial(A a) {
        return b -> execute(a, b);
    }

    default Executable<A, Executable<B, R>> curried() {
        return a -> b -> execute(a, b);
    }

    static <T, U> BiExecutable<T, U, T> first() {
        return (a, b) -> a;
    }
    static <T, U> BiExecutable<T, U, U> second() {
        return (a, b) -> b;
    }
}
