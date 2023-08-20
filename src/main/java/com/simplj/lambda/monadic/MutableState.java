package com.simplj.lambda.monadic;

import com.simplj.lambda.function.Function;

public class MutableState<A> {
    private volatile A value;

    private MutableState(A v) {
        this.value = v;
    }

    public static <T> MutableState<T> of(T val) {
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

    public <R> MutableState<R> change(Function<A, R> f) {
        return new MutableState<>(f.apply(value));
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
