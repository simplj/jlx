package com.simplj.lambda.util;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;

import java.util.Objects;

/**
 * Wraps a value of type A and provides functionalities to mutate/change the underlying value
 * @param <A> Type of the underlying value
 */
public class Mutable<A> {
    private volatile A value;

    private Mutable(A v) {
        this.value = v;
    }

    /**
     * Wraps a value of type T in the Mutable instance
     * @param val The value to be wrapped
     * @param <T> Type of the underlying value
     * @return Mutable instance with the provided value
     */
    public static <T> Mutable<T> of(T val) {
        return new Mutable<>(val);
    }

    /**
     * Returns the underlying value
     * @return The underlying value
     */
    public A get() {
        return value;
    }

    /**
     * Sets a new value of type A
     * @param val value which will replace the existing value
     * @return Mutable Instance with the new value
     */
    public Mutable<A> set(A val) {
        this.value = val;
        return this;
    }
    /**
     * Sets a new value of type A
     * @param val value which will replace the existing value
     * @return Mutable Instance with the new value
     */
    public Mutable<A> set(Condition<A> condition, A val) {
        if (condition.evaluate(value)) {
            set(val);
        }
        return this;
    }

    /**
     * Applies the Function f to the underlying value.
     * API is eager i.e. Function f is applied as soon as this API is called.
     * @param f Function to be applied to the underlying value
     * @return Mutable instance with the resultant value of function application
     */
    public Mutable<A> mutate(Function<A, A> f) {
        return set(f.apply(value));
    }
    /**
     * Applies the Function f to the underlying value.
     * API is eager i.e. Function f is applied as soon as this API is called.
     * @param f Function to be applied to the underlying value
     * @return Mutable instance with the resultant value of function application
     */
    public Mutable<A> mutate(Condition<A> condition, Function<A, A> f) {
        return set(condition, f.apply(value));
    }

    /**
     * Applies the Function f to the underlying value.
     * API is eager i.e. Function f is applied as soon as this API is called.
     * @param f   Function to be applied to the underlying value
     * @param <R> Type of the function's resultant value
     * @return Mutable instance with the resultant value of functional application
     */
    public <R> Mutable<R> change(Function<A, R> f) {
        return new Mutable<>(f.apply(value));
    }

    public Mutable<A> copy() {
        return Mutable.of(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mutable<?> that = (Mutable<?>) o;
        return Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }
}
