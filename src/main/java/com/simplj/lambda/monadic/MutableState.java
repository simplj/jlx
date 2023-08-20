package com.simplj.lambda.monadic;

import com.simplj.lambda.function.Function;

public class MutableState<A> {
    private volatile A value;

    MutableState(A v) {
        this.value = v;
    }

    public static <T> MutableState<T> arg(T val) {
        return new MutableState<>(val);
    }

    public A get() {
        return value;
    }

    public MutableState<A> set(A val) {
        this.value = val;
        return this;
    }

    public MutableState<A> mutate(Function<A, A> f) {
        return set(f.apply(value));
    }

    public MutableState<A> flatmap(Function<A, MutableState<A>> f) {
        return set(f.apply(value).value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
