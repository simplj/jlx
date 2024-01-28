package com.simplj.lambda.function;

import com.simplj.lambda.data.IArray;

import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface Condition<A> extends Predicate<A> {
    boolean evaluate(A input);

    default boolean test(A a) {
        return evaluate(a);
    }

    default Condition<A> negate() {
        return a -> !evaluate(a);
    }

    default <R> Condition<R> compose(Function<R, A> f) {
        Objects.requireNonNull(f);
        return r -> evaluate(f.apply(r));
    }

    default <B> BiCondition<A, B> and(Condition<B> c) {
        return BiCondition.both(this, c);
    }
    default <B> BiCondition<A, B> or(Condition<B> c) {
        return BiCondition.any(this, c);
    }

    static <T> Condition<T> of(Condition<T> c) {
        return c;
    }

    static <T> Condition<T> negate(Condition<T> c) {
        return c.negate();
    }

    static <T> Condition<T> always() {
        return a -> true;
    }
    static <T> Condition<T> never() {
        return a -> false;
    }

    static <T extends Comparable<T>> Condition<T> lesser(T n) {
        return x -> x.compareTo(n) < 0;
    }
    static <T extends Comparable<T>> Condition<T> lesserOrEqual(T n) {
        return x -> x.compareTo(n) <= 0;
    }
    static <T extends Comparable<T>> Condition<T> equal(T n) {
        return x -> x.compareTo(n) == 0;
    }
    static <T extends Comparable<T>> Condition<T> greater(T n) {
        return x -> x.compareTo(n) > 0;
    }
    static <T extends Comparable<T>> Condition<T> greaterOrEqual(T n) {
        return x -> x.compareTo(n) >= 0;
    }

    /**
     * Constructs a condition which evaluates the between condition
     * @param from floor value (inclusive) of the range to evaluate `between`
     * @param to   ceil value (inclusive) of the range to evaluate `between`
     * @param <T>  Type of the values
     * @return A condition which evaluates the between condition
     */
    static <T extends Comparable<T>> Condition<T> between(T from, T to) {
        return n -> n.compareTo(from) >= 0 && n.compareTo(to) <= 0;
    }

    @SafeVarargs
    static <T> Condition<T> in(T...values) {
        return v -> IArray.of(values).any(val -> Objects.equals(v, val));
    }
}
