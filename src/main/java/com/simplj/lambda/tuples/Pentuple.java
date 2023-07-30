package com.simplj.lambda.tuples;

public final class Pentuple<A, B, C, D, E> {
    private final A first;
    private final B second;
    private final C third;
    private final D fourth;
    private final E fifth;

    Pentuple(A first, B second, C third, D fourth, E fifth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
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

    public final E fifth() {
        return fifth;
    }

    public <F> Hextuple<A, B, C, D, E, F> add(F sixth) {
        return Tuple.of(first, second, third, fourth, fifth, sixth);
    }

    public final <V> Pentuple<V, B, C, D, E> modifyFirst(V newVal) {
        return Tuple.of(newVal, second, third, fourth, fifth);
    }

    public final <V> Pentuple<A, V, C, D, E> modifySecond(V newVal) {
        return Tuple.of(first, newVal, third, fourth, fifth);
    }

    public final <V> Pentuple<A, B, V, D, E> modifyThird(V newVal) {
        return Tuple.of(first, second, newVal, fourth, fifth);
    }

    public final <V> Pentuple<A, B, C, V, E> modifyFourth(V newVal) {
        return Tuple.of(first, second, third, newVal, fifth);
    }

    public final <V> Pentuple<A, B, C, D, V> modifyFifth(V newVal) {
        return Tuple.of(first, second, third, fourth, newVal);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ", " + fourth + ", " + fifth + ')';
    }
}
