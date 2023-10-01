package com.simplj.lambda.tuples;

import java.util.Objects;

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
        return new Hextuple<>(newVal, second, third, fourth, fifth, sixth);
    }

    public final <V> Hextuple<A, V, C, D, E, F> modifySecond(V newVal) {
        return new Hextuple<>(first, newVal, third, fourth, fifth, sixth);
    }

    public final <V> Hextuple<A, B, V, D, E, F> modifyThird(V newVal) {
        return new Hextuple<>(first, second, newVal, fourth, fifth, sixth);
    }

    public final <V> Hextuple<A, B, C, V, E, F> modifyFourth(V newVal) {
        return new Hextuple<>(first, second, third, newVal, fifth, sixth);
    }

    public final <V> Hextuple<A, B, C, D, V, F> modifyFifth(V newVal) {
        return new Hextuple<>(first, second, third, fourth, newVal, sixth);
    }

    public final <V> Hextuple<A, B, C, D, E, V> modifySixth(V newVal) {
        return new Hextuple<>(first, second, third, fourth, fifth, newVal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Hextuple<?, ?, ?, ?, ?, ?> that = (Hextuple<?, ?, ?, ?, ?, ?>) o;

        return Objects.equals(this.first, that.first) && Objects.equals(this.second, that.second)
                && Objects.equals(this.third, that.third) && Objects.equals(this.fourth, that.fourth)
                && Objects.equals(this.fifth, that.fifth) && Objects.equals(this.sixth, that.sixth);
    }

    @Override
    public int hashCode() {
        int result = first == null ? 0 : first.hashCode();
        result = 31 * result + (second == null ? 0 : second.hashCode());
        result = 31 * result + (third == null ? 0 : third.hashCode());
        result = 31 * result + (fourth == null ? 0 : fourth.hashCode());
        result = 31 * result + (fifth == null ? 0 : fifth.hashCode());
        result = 31 * result + (sixth == null ? 0 : sixth.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ", " + fourth + ", " + fifth + ", " + sixth + ')';
    }
}
