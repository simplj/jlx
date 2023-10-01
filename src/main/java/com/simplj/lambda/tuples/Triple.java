package com.simplj.lambda.tuples;

import java.util.Objects;

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
        return new Triple<>(newVal, second, third);
    }

    public final <V> Triple<A, V, C> modifySecond(V newVal) {
        return new Triple<>(first, newVal, third);
    }

    public final <V> Triple<A, B, V> modifyThird(V newVal) {
        return new Triple<>(first, second, newVal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Triple<?, ?, ?> that = (Triple<?, ?, ?>) o;

        return Objects.equals(this.first, that.first) && Objects.equals(this.second, that.second)
                && Objects.equals(this.third, that.third);
    }

    @Override
    public int hashCode() {
        int result = first == null ? 0 : first.hashCode();
        result = 31 * result + (second == null ? 0 : second.hashCode());
        result = 31 * result + (third == null ? 0 : third.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ", " + third + ')';
    }
}
