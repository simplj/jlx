package com.simplj.lambda.tuples;

public interface Tuple5<A, B, C, D, E> extends Tuple4<A, B, C, D> {
    E fifth();

    static <P, Q, R, S, T> Tuple5<P, Q, R, S, T> of(P first, Q second, R third, S fourth, T fifth) {
        return Tuple.of(first, second, third, fourth, fifth);
    }
}
