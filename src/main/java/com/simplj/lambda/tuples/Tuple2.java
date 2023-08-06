package com.simplj.lambda.tuples;

public interface Tuple2<A, B> {
    A first();
    B second();

    static <P, Q> Tuple2<P, Q> of(P first, Q second) {
        return new Couple<>(first, second);
    }
}
