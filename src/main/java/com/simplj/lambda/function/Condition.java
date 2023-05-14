package com.simplj.lambda.function;

@FunctionalInterface
public interface Condition<A> {
    boolean evaluate(A input);

    default Condition<A> negate() {
        return a -> !evaluate(a);
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
}
