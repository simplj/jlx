package com.simplj.lambda.function;

import com.simplj.lambda.data.IArray;

import java.util.Objects;

@FunctionalInterface
public interface BiCondition<A, B> {
    boolean evaluate(A a, B b);

    /**
     * Applies the BiCondition partially (that is why the name `eval` depicting partial `evaluate`-ing)
     * @param a the first argument to be applied
     * @return Condition with the other argument
     */
    default Condition<B> eval(A a) {
        return b -> evaluate(a, b);
    }

    default BiCondition<A, B> negate() {
        return (a, b) -> !evaluate(a, b);
    }

    default <T> BiCondition<T, B> composeFirst(Function<T, A> f) {
        Objects.requireNonNull(f);
        return (t, b) -> evaluate(f.apply(t), b);
    }
    default <T> BiCondition<A, T> composeSecond(Function<T, B> f) {
        Objects.requireNonNull(f);
        return (a, t) -> evaluate(a, f.apply(t));
    }

    static <T, R> BiCondition<T, R> of(BiCondition<T, R> c) {
        return c;
    }

    static <T, R> BiCondition<T, R> ofFirst(Condition<T> c) {
        return (a, x) -> c.evaluate(a);
    }
    static <T, R> BiCondition<T, R> ofSecond(Condition<R> c) {
        return (x, b) -> c.evaluate(b);
    }

    static <T, R> BiCondition<T, R> negate(BiCondition<T, R> c) {
        return c.negate();
    }

    static <T, R> BiCondition<T, R> always() {
        return (a, b) -> true;
    }
    static <T, R> BiCondition<T, R> never() {
        return (a, b) -> false;
    }

    static <T, R> BiCondition<T, R> any(Condition<T> c1, Condition<R> c2) {
        return (a, b) -> c1.evaluate(a) || c2.evaluate(b);
    }
    static <T, R> BiCondition<T, R> both(Condition<T> c1, Condition<R> c2) {
        return (a, b) -> c1.evaluate(a) && c2.evaluate(b);
    }
}
