package com.simplj.lambda.util;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wraps a initializer (such as a constructor) and initializes lazily when needed.
 * Also, provides capability to restate a value on a certain condition.
 * @param <A>
 */
public class Lazy<A> {
    private volatile Producer<A> valueF;
    private final Function<A, A> restateF;
    private volatile A value;
    private volatile Function<A, A> func;
    private final AtomicBoolean isApplied;

    private Lazy(Producer<A> valueF, Function<A, A> restateF) {
        this.valueF = valueF;
        this.restateF = restateF;
        this.func = Function.id();
        isApplied = new AtomicBoolean(false);
    }

    /**
     * Wraps the initialization of type T in the Lazy instance
     * @param lazyVal Initialization to be wrapped
     * @param <T>     Type of value to be initialized
     * @return Lazy instance with the initializer for type T
     */
    public static <T> Lazy<T> of(Producer<T> lazyVal) {
        return new Lazy<>(lazyVal, Function.id());
    }

    /**
     * Wraps the initialization of type T in the lazy instance.
     * Also, the value T is restated by the `restateF` function when the `condition` argument satisfies.
     * @param lazyVal   Initialization to be wrapped
     * @param condition Decides when to restate the value
     * @param restateF  Applies this function to restate the underlying value when needed
     * @param <T>       Type of value to be initialized
     * @return Lazy instance with initializer and `condition` when to restate by `restateF` function
     */
    public static <T> Lazy<T> restating(Producer<T> lazyVal, Condition<T> condition, Function<T, T> restateF) {
        return new Lazy<>(lazyVal, t -> condition.evaluate(t) ? restateF.apply(t) : t);
    }

    /**
     * Applies the Function f to the underlying value.
     * API is lazy i.e. Function f is applied when the underlying value is accessed using the `get` API.
     * @param f Function to be applied to the underlying value
     * @return Current instance of Lazy
     */
    public Lazy<A> mutate(Function<A, A> f) {
        Function<A, A> temp = this.func;
        this.func = temp.andThen(f);
        this.isApplied.set(false);
        return this;
    }

    /**
     * Applies mutation function(s) if any and returns the resultant value.
     * If the resultant value meets the restating condition (if set) then the value is restated before returning.
     * @return The resultant value after applying mutation function(s) if any and restating function if needed
     */
    public A get() {
        if (!isApplied.get()) {
            value = func.apply(valueF.produce());
            valueF = () -> value;
            func = Function.id();
            isApplied.set(true);
        }
        A temp = this.value;
        this.value = restateF.apply(temp);
        return this.value;
    }
}
