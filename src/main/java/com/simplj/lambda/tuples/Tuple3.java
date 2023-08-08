package com.simplj.lambda.tuples;

public interface Tuple3<A, B, C> extends Tuple2<A, B> {
    C third();

    static <P, Q, R> Tuple3<P, Q, R> of(P first, Q second, R third) {
        return new Triple<>(first, second, third);
    }
}
