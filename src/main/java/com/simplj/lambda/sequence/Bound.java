package com.simplj.lambda.sequence;

import com.simplj.lambda.data.SlidingWindow;
import com.simplj.lambda.function.BiCondition;
import com.simplj.lambda.function.Condition;

@FunctionalInterface
public interface Bound<T> {
    boolean satisfy(int idx, SlidingWindow<T> predecessor, T current);

    static <A> Bound<A> onCurrent(Condition<A> condition) {
        return (x, y, c) -> condition.evaluate(c);
    }
    static <A> Bound<A> onIdx(Condition<Integer> condition) {
        return (i, x, y) -> condition.evaluate(i);
    }

    static <A> Bound<A> withoutIdx(BiCondition<SlidingWindow<A>, A> condition) {
        return (x, p, c) -> condition.evaluate(p, c);
    }
    static <A> Bound<A> withoutPred(BiCondition<Integer, A> condition) {
        return (i, x, c) -> condition.evaluate(i, c);
    }
}