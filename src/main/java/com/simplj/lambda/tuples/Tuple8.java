package com.simplj.lambda.tuples;

public interface Tuple8<A, B, C, D, E, F, G, H> extends Tuple7<A, B, C, D, E, F, G> {
    H eighth();

    static <P, Q, R, S, T, U, V, W> Tuple8<P, Q, R, S, T, U, V, W> of(P first, Q second, R third, S fourth, T fifth, U sixth, V seventh, W eighth) {
        return new Octuple<>(first, second, third, fourth, fifth, sixth, seventh, eighth);
    }
}
