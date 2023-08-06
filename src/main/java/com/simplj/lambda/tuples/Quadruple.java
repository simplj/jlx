package com.simplj.lambda.tuples;

public final class Quadruple<A, B, C, D> implements Tuple4<A, B, C, D> {
    private final A first;
    private final B second;
    private final C third;
    private final D fourth;

    Quadruple(A first, B second, C third, D fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
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

    public final D fourth() {
        return fourth;
    }

    public <E> Pentuple<A, B, C, D, E> add(E fifth) {
        return Tuple.of(first, second, third, fourth, fifth);
    }

    public final <V> Quadruple<V, B, C, D> modifyFirst(V newVal) {
        return new Quadruple<>(newVal, second, third, fourth);
    }

    public final <V> Quadruple<A, V, C, D> modifySecond(V newVal) {
        return new Quadruple<>(first, newVal, third, fourth);
    }

    public final <V> Quadruple<A, B, V, D> modifyThird(V newVal) {
        return new Quadruple<>(first, second, newVal, fourth);
    }

    public final <V> Quadruple<A, B, C, V> modifyFourth(V newVal) {
        return new Quadruple<>(first, second, third, newVal);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ", " + fourth + ')';
    }
}
