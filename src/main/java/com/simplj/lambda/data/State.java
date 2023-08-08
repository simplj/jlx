package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;

public class State<T> {
    private volatile T value;

    private State(T val) {
        this.value = val;
    }

    public static <A> State<A> of(A value) {
        return new State<>(value);
    }

    public static <R> State<R> empty() {
        return new State<>(null);
    }

    public State<T> changeTo(T newVal) {
        this.value = newVal;
        return this;
    }

    public <R> State<R> map(Function<T, R> f) {
        return flatmap(f.andThen(State::new));
    }

    public <R> State<R> flatmap(Function<T, State<R>> f) {
        return isEmpty() ? empty() : f.apply(value);
    }

    public State<T> satisfy(Condition<T> c) {
        return flatmap(t -> c.evaluate(t) ? this : empty());
    }

    public boolean isEmpty() {
        return value == null;
    }

    public T getOrDefault(T defaultValue) {
        return isEmpty() ? defaultValue : value;
    }
}
