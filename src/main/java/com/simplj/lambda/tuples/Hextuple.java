package com.simplj.lambda.tuples;

public final class Hextuple<A, B, C, D, E, F> implements Tuple6<A, B, C, D, E, F> {
    private final A first;
    private final B second;
    private final C third;
    private final D fourth;
    private final E fifth;
    private final F sixth;

    Hextuple(A first, B second, C third, D fourth, E fifth, F sixth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
        this.sixth = sixth;
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

    public final F sixth() {
        return sixth;
    }

    public <G> Septuple<A, B, C, D, E, F, G> add(G seventh) {
        return Tuple.of(first, second, third, fourth, fifth, sixth, seventh);
    }

    public final <V> Hextuple<V, B, C, D, E, F> modifyFirst(V newVal) {
        return Tuple.of(newVal, second, third, fourth, fifth, sixth);
    }

    public final <V> Hextuple<A, V, C, D, E, F> modifySecond(V newVal) {
        return Tuple.of(first, newVal, third, fourth, fifth, sixth);
    }

    public final <V> Hextuple<A, B, V, D, E, F> modifyThird(V newVal) {
        return Tuple.of(first, second, newVal, fourth, fifth, sixth);
    }

    public final <V> Hextuple<A, B, C, V, E, F> modifyFourth(V newVal) {
        return Tuple.of(first, second, third, newVal, fifth, sixth);
    }

    public final <V> Hextuple<A, B, C, D, V, F> modifyFifth(V newVal) {
        return Tuple.of(first, second, third, fourth, newVal, sixth);
    }

    public final <V> Hextuple<A, B, C, D, E, V> modifySixth(V newVal) {
        return Tuple.of(first, second, third, fourth, fifth, newVal);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ", " + fourth + ", " + fifth + ", " + sixth + ')';
    }
}
