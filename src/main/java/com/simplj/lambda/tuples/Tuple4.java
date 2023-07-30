package com.simplj.lambda.tuples;

public interface Tuple4<A, B, C, D> extends Tuple3<A, B, C> {
    D fourth();

    static <P, Q, R, S> Tuple4<P, Q, R, S> of(P first, Q second, R third, S fourth) {
        return Tuple.of(first, second, third, fourth);
    }
}
