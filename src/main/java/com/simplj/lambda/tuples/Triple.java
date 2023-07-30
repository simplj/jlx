package com.simplj.lambda.tuples;

public final class Triple<A, B, C> implements Tuple3<A, B, C> {
    private final A first;
    private final B second;
    private final C third;

    Triple(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public final A first() {
        return first;
    }

    public final B second() {
        return second;
    }

    public final C third() {
        return third;
    }

    public <D> Quadruple<A, B, C, D> add(D fourth) {
        return Tuple.of(first, second, third, fourth);
    }

    public final <V> Triple<V, B, C> modifyFirst(V newVal) {
        return Tuple.of(newVal, second, third);
    }

    public final <V> Triple<A, V, C> modifySecond(V newVal) {
        return Tuple.of(first, newVal, third);
    }

    public final <V> Triple<A, B, V> modifyThird(V newVal) {
        return Tuple.of(first, second, newVal);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ')';
    }
}
