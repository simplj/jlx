package com.simplj.lambda.tuples;

public final class Septuple<A, B, C, D, E, F, G> implements Tuple7<A, B, C, D, E, F, G> {
    private final A first;
    private final B second;
    private final C third;
    private final D fourth;
    private final E fifth;
    private final F sixth;
    private final G seventh;

    Septuple(A first, B second, C third, D fourth, E fifth, F sixth, G seventh) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
        this.fifth = fifth;
        this.sixth = sixth;
        this.seventh = seventh;
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

    public <H> Octuple<A, B, C, D, E, F, G, H> add(H eighth) {
        return Tuple.of(first, second, third, fourth, fifth, sixth, seventh, eighth);
    }

    public final <V> Septuple<V, B, C, D, E, F, G> modifyFirst(V newVal) {
        return new Septuple<>(newVal, second, third, fourth, fifth, sixth, seventh);
    }

    public final <V> Septuple<A, V, C, D, E, F, G> modifySecond(V newVal) {
        return new Septuple<>(first, newVal, third, fourth, fifth, sixth, seventh);
    }

    public final <V> Septuple<A, B, V, D, E, F, G> modifyThird(V newVal) {
        return new Septuple<>(first, second, newVal, fourth, fifth, sixth, seventh);
    }

    public final <V> Septuple<A, B, C, V, E, F, G> modifyFourth(V newVal) {
        return new Septuple<>(first, second, third, newVal, fifth, sixth, seventh);
    }

    public final <V> Septuple<A, B, C, D, V, F, G> modifyFifth(V newVal) {
        return new Septuple<>(first, second, third, fourth, newVal, sixth, seventh);
    }

    public final <V> Septuple<A, B, C, D, E, V, G> modifySixth(V newVal) {
        return new Septuple<>(first, second, third, fourth, fifth, newVal, seventh);
    }

    public final <V> Septuple<A, B, C, D, E, F, V> modifySeventh(V newVal) {
        return new Septuple<>(first, second, third, fourth, fifth, sixth, newVal);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ", " + fourth + ", " + fifth + ", " + sixth + ", " + seventh + ')';
    }
}
