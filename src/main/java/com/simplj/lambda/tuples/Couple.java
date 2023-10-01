package com.simplj.lambda.tuples;

import java.util.Objects;

public final class Couple<A, B> implements Tuple2<A, B> {
    private final A first;
    private final B second;

    Couple(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public final A first() {
        return first;
    }

    public final B second() {
        return second;
    }

    public <C> Triple<A, B, C> add(C third) {
        return Tuple.of(first, second, third);
    }

    public final <V> Couple<V, B> modifyFirst(V newVal) {
        return new Couple<>(newVal, second);
    }

    public final <V> Couple<A, V> modifySecond(V newVal) {
        return new Couple<>(first, newVal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Couple<?, ?> that = (Couple<?, ?>) o;

        return Objects.equals(this.first, that.first) && Objects.equals(this.second, that.second);
    }

    @Override
    public int hashCode() {
        int result = first == null ? 0 : first.hashCode();
        result = 31 * result + (second == null ? 0 : second.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ')';
    }
}
