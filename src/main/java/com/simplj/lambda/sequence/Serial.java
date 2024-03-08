package com.simplj.lambda.sequence;

import com.simplj.lambda.data.SlidingWindow;
import com.simplj.lambda.function.BiFunction;

@FunctionalInterface
public interface Serial<T> {
    T generate(int idx, SlidingWindow<T> predecessor, T current);

    static <A> Serial<A> withoutIdx(BiFunction<SlidingWindow<A>, A, A> f) {
        return (x, p, c) -> f.apply(p, c);
    }
    static <A> Serial<A> withoutPred(BiFunction<Integer, A, A> f) {
        return (i, x, c) -> f.apply(i, c);
    }
}