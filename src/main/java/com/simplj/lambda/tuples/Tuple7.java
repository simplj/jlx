package com.simplj.lambda.tuples;

public interface Tuple7<A, B, C, D, E, F, G> extends Tuple6<A, B, C, D, E, F> {
    G seventh();

    static <P, Q, R, S, T, U, V> Tuple7<P, Q, R, S, T, U, V> of(P first, Q second, R third, S fourth, T fifth, U sixth, V seventh) {
        return new Septuple<>(first, second, third, fourth, fifth, sixth, seventh);
    }
}
