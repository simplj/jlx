package com.simplj.lambda.tuples;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Quadruple<?, ?, ?, ?> that = (Quadruple<?, ?, ?, ?>) o;

        return Objects.equals(this.first, that.first) && Objects.equals(this.second, that.second)
                && Objects.equals(this.third, that.third) && Objects.equals(this.fourth, that.fourth);
    }

    @Override
    public int hashCode() {
        int result = first == null ? 0 : first.hashCode();
        result = 31 * result + (second == null ? 0 : second.hashCode());
        result = 31 * result + (third == null ? 0 : third.hashCode());
        result = 31 * result + (fourth == null ? 0 : fourth.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ", " + fourth + ')';
    }
}
