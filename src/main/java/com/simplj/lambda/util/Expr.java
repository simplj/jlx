package com.simplj.lambda.util;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;

import java.util.Objects;

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

    public <T> Expr<T> map(Function<A, T> f) {
        return new Expr<>(f.apply(val));
    }

    public When<A> when(A match) {
        return new When<>(val, v -> Objects.equals(v, match));
    }
    public When<A> when(Condition<A> condition) {
        return new When<>(val, condition);
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

    public static class When<T> {
        private final T val;
        private final int flag;

        private When(T val, Condition<T> condition) {
            this.val = val;
            this.flag = condition.evaluate(val) ? 1 : 0;
        }

        public <R> Then<T, R> then(Function<T, R> f) {
            Then<T, R> res;
            if (flag == 1) {
                res = new Then<>(val, flag + 1, f.apply(val));
            } else {
                res = new Then<>(val, flag, null);
            }
            return res;
        }
    }
    public static class TypedWhen<T, R> {
        private final T val;
        private final int flag;
        private final R r;

        private TypedWhen(T val, int flag, R r) {
            this.val = val;
            this.flag = flag;
            this.r = r;
        }

        public Then<T, R> then(Function<T, R> f) {
            Then<T, R> res;
            if (flag == 1) {
                res = new Then<>(val, flag + 1, f.apply(val));
            } else {
                res = new Then<>(val, flag, r);
            }
            return res;
        }
    }
    public static class Then<T, R> {
        private final T val;
        private final int flag;
        private final R r;

        private Then(T val, int flag, R r) {
            this.val = val;
            this.flag = flag;
            this.r = r;
        }

        public TypedWhen<T, R> when(T match) {
            return when(v -> Objects.equals(v, match));
        }
        public TypedWhen<T, R> when(Condition<T> condition) {
            TypedWhen<T, R> res;
            if (flag == 2) {
                res = new TypedWhen<>(val, flag, r);
            } else {
                res = new TypedWhen<>(val, condition.evaluate(val) ? 1 : 0, r);
            }
            return res;
        }
        public R otherwise(Function<T, R> f) {
            R res;
            if (flag == 2) {
                res = r;
            } else {
                res = f.apply(val);
            }
            return res;
        }
        public R otherwiseNull() {
            return otherwise(x -> null);
        }
    }
}
