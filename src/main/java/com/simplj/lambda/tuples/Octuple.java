package com.simplj.lambda.tuples;

import java.util.Objects;

public final class Octuple<A, B, C, D, E, F, G, H> implements Tuple8<A, B, C, D, E, F, G, H> {
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
        return new Octuple<>(newVal, second, third, fourth, fifth, sixth, seventh, eighth);
    }

    public final <V> Octuple<A, V, C, D, E, F, G, H> modifySecond(V newVal) {
        return new Octuple<>(first, newVal, third, fourth, fifth, sixth, seventh, eighth);
    }

    public final <V> Octuple<A, B, V, D, E, F, G, H> modifyThird(V newVal) {
        return new Octuple<>(first, second, newVal, fourth, fifth, sixth, seventh, eighth);
    }

    public final <V> Octuple<A, B, C, V, E, F, G, H> modifyFourth(V newVal) {
        return new Octuple<>(first, second, third, newVal, fifth, sixth, seventh, eighth);
    }

    public final <V> Octuple<A, B, C, D, V, F, G, H> modifyFifth(V newVal) {
        return new Octuple<>(first, second, third, fourth, newVal, sixth, seventh, eighth);
    }

    public final <V> Octuple<A, B, C, D, E, V, G, H> modifySixth(V newVal) {
        return new Octuple<>(first, second, third, fourth, fifth, newVal, seventh, eighth);
    }

    public final <V> Octuple<A, B, C, D, E, F, V, H> modifySeventh(V newVal) {
        return new Octuple<>(first, second, third, fourth, fifth, sixth, newVal, eighth);
    }

    public final <V> Octuple<A, B, C, D, E, F, G, V> modifyEighth(V newVal) {
        return new Octuple<>(first, second, third, fourth, fifth, sixth, seventh, newVal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Octuple<?, ?, ?, ?, ?, ?, ?, ?> that = (Octuple<?, ?, ?, ?, ?, ?, ?, ?>) o;

        return Objects.equals(this.first, that.first) && Objects.equals(this.second, that.second)
                && Objects.equals(this.third, that.third) && Objects.equals(this.fourth, that.fourth)
                && Objects.equals(this.fifth, that.fifth) && Objects.equals(this.sixth, that.sixth)
                && Objects.equals(this.seventh, that.seventh) && Objects.equals(this.eighth, that.eighth);
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
        result = 31 * result + (eighth == null ? 0 : eighth.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ", " + fourth + ", " + fifth + ", " + sixth + ", " + seventh + ", " + eighth + ')';
    }
}
