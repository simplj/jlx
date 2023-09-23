package com.simplj.lambda.util;

import com.simplj.lambda.function.Function;

public class Expr<A> {
    private final A val;

    public Expr(A val) {
        this.val = val;
    }

    public static <T> Expr<T> let(T val) {
        return new Expr<>(val);
    }

    public <B> B in(Function<A, B> f) {
        return f.apply(val);
    }

    @Override
    public boolean equals(Object o) {
        throw new IllegalStateException("Expr should not be `equal`ed!");
    }

    @Override
    public int hashCode() {
        throw new IllegalStateException("hashCode should not be called on Expr!");
    }

    @Override
    public String toString() {
        return String.format("%s => ?", val);
    }
}
