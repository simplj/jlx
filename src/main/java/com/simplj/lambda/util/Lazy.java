package com.simplj.lambda.util;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;

public class Lazy<A> {
    private volatile Producer<A> valueF;
    private final Function<A, A> restateF;
    private volatile A value;
    private volatile Function<A, A> func;
    private volatile boolean isApplied;

    private Lazy(Producer<A> valueF, Function<A, A> restateF) {
        this.valueF = valueF;
        this.restateF = restateF;
        this.func = Function.id();
    }

    public static <T> Lazy<T> of(Producer<T> lazyVal) {
        return new Lazy<>(lazyVal, Function.id());
    }
    public static <T> Lazy<T> restating(Producer<T> lazyVal, Condition<T> condition, Function<T, T> restateF) {
        return new Lazy<>(lazyVal, t -> condition.evaluate(t) ? restateF.apply(t) : t);
    }

    public Lazy<A> mutate(Function<A, A> f) {
        this.func.andThen(f);
        this.isApplied = false;
        return this;
    }

    public A get() {
        if (!isApplied) {
            value = func.apply(valueF.produce());
            valueF = () -> value;
            func = Function.id();
            isApplied = true;
        }
        return restateF.apply(value);
    }
}
