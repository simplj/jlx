package com.simplj.lambda.tuples;

public final class Octuple<A, B, C, D, E, F, G, H> {
    private final A first;
    private final B second;
    private final C third;
    private final D fourth;
    private final E fifth;
    private final F sixth;
    private final G seventh;
    private final H eighth;

    Octuple(A first, B second, C third, D fourth, E fifth, F sixth, G seventh, H eighth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
        this.sixth = sixth;
        this.seventh = seventh;
        this.eighth = eighth;
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

    public final G seventh() {
        return seventh;
    }

    public final H eighth() {
        return eighth;
    }

    public final <V> Octuple<V, B, C, D, E, F, G, H> modifyFirst(V newVal) {
        return Tuple.of(newVal, second, third, fourth, fifth, sixth, seventh, eighth);
    }

    public final <V> Octuple<A, V, C, D, E, F, G, H> modifySecond(V newVal) {
        return Tuple.of(first, newVal, third, fourth, fifth, sixth, seventh, eighth);
    }

    public final <V> Octuple<A, B, V, D, E, F, G, H> modifyThird(V newVal) {
        return Tuple.of(first, second, newVal, fourth, fifth, sixth, seventh, eighth);
    }

    public final <V> Octuple<A, B, C, V, E, F, G, H> modifyFourth(V newVal) {
        return Tuple.of(first, second, third, newVal, fifth, sixth, seventh, eighth);
    }

    public final <V> Octuple<A, B, C, D, V, F, G, H> modifyFifth(V newVal) {
        return Tuple.of(first, second, third, fourth, newVal, sixth, seventh, eighth);
    }

    public final <V> Octuple<A, B, C, D, E, V, G, H> modifySixth(V newVal) {
        return Tuple.of(first, second, third, fourth, fifth, newVal, seventh, eighth);
    }

    public final <V> Octuple<A, B, C, D, E, F, V, H> modifySeventh(V newVal) {
        return Tuple.of(first, second, third, fourth, fifth, sixth, newVal, eighth);
    }

    public final <V> Octuple<A, B, C, D, E, F, G, V> modifyEighth(V newVal) {
        return Tuple.of(first, second, third, fourth, fifth, sixth, seventh, newVal);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ", " + fourth + ", " + fifth + ", " + sixth + ", " + seventh + ", " + eighth + ')';
    }
}
