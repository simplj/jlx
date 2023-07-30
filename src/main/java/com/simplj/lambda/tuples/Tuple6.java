package com.simplj.lambda.tuples;

public interface Tuple6<A, B, C, D, E, F> extends Tuple5<A, B, C, D, E> {
    F sixth();

    static <P, Q, R, S, T, U> Tuple6<P, Q, R, S, T, U> of(P first, Q second, R third, S fourth, T fifth, U sixth) {
        return Tuple.of(first, second, third, fourth, fifth, sixth);
    }
}
