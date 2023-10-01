package com.simplj.lambda.tuples;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Septuple<?, ?, ?, ?, ?, ?, ?> that = (Septuple<?, ?, ?, ?, ?, ?, ?>) o;

        return Objects.equals(this.first, that.first) && Objects.equals(this.second, that.second)
                && Objects.equals(this.third, that.third) && Objects.equals(this.fourth, that.fourth)
                && Objects.equals(this.fifth, that.fifth) && Objects.equals(this.sixth, that.sixth)
                && Objects.equals(this.seventh, that.seventh);
    }

    @Override
    public int hashCode() {
        int result = first == null ? 0 : first.hashCode();
        result = 31 * result + (second == null ? 0 : second.hashCode());
        result = 31 * result + (third == null ? 0 : third.hashCode());
        result = 31 * result + (fourth == null ? 0 : fourth.hashCode());
        result = 31 * result + (fifth == null ? 0 : fifth.hashCode());
        result = 31 * result + (sixth == null ? 0 : sixth.hashCode());
        result = 31 * result + (seventh == null ? 0 : seventh.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ", " + fourth + ", " + fifth + ", " + sixth + ", " + seventh + ')';
    }
}
