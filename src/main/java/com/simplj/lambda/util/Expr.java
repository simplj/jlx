package com.simplj.lambda.util;

import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.executable.Provider;
import com.simplj.lambda.executable.Receiver;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;

import java.util.Objects;

public class Expr<A> {
    private final A val;

    Expr(A val) {
        this.val = val;
    }

    public static <T> Expr<T> let(T val) {
        return new Expr<>(val);
    }

    public PureExpr<A> pure() {
        return new PureExpr<>(val);
    }

    public <B> B in(Executable<A, B> f) throws Exception {
        return f.execute(val);
    }
    public <B> B yield(Provider<B> p) throws Exception {
        return p.provide();
    }
    public <B> B returning(B r) {
        return r;
    }

    public Expr<A> record(Receiver<A> consumer) throws Exception {
        consumer.receive(val);
        return this;
    }
    public Expr<A> recordIf(Condition<A> condition, Receiver<A> consumer) throws Exception {
        if (condition.evaluate(val)) {
            consumer.receive(val);
        }
        return this;
    }

    public <T> Expr<T> map(Executable<A, T> f) throws Exception {
        return new Expr<>(f.execute(val));
    }
    public Expr<A> mapIf(Condition<A> c, Executable<A, A> f) throws Exception {
        return new Expr<>(c.evaluate(val) ? f.execute(val) : val);
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

        public <R> Then<T, R> then(R r) {
            Then<T, R> res;
            if (flag == 1) {
                res = new Then<>(val, flag + 1, r);
            } else {
                res = new Then<>(val, flag, null);
            }
            return res;
        }
        public <R> Then<T, R> then(Provider<R> f) throws Exception {
            Then<T, R> res;
            if (flag == 1) {
                res = new Then<>(val, flag + 1, f.provide());
            } else {
                res = new Then<>(val, flag, null);
            }
            return res;
        }
        public <R> Then<T, R> then(Executable<T, R> f) throws Exception {
            Then<T, R> res;
            if (flag == 1) {
                res = new Then<>(val, flag + 1, f.execute(val));
            } else {
                res = new Then<>(val, flag, null);
            }
            return res;
        }
        public <R> Then<T, R> err(Function<T, ? extends Exception> ef) throws Exception {
            if (flag == 1) {
                throw ef.apply(val);
            }
            return new Then<>(val, flag, null);
        }
        public <R> Then<T, R> err(Exception ex) throws Exception {
            if (flag == 1) {
                throw ex;
            }
            return new Then<>(val, flag, null);
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

        public Then<T, R> then(R v) {
            Then<T, R> res;
            if (flag == 1) {
                res = new Then<>(val, flag + 1, v);
            } else {
                res = new Then<>(val, flag, r);
            }
            return res;
        }
        public Then<T, R> then(Provider<R> f) throws Exception {
            Then<T, R> res;
            if (flag == 1) {
                res = new Then<>(val, flag + 1, f.provide());
            } else {
                res = new Then<>(val, flag, r);
            }
            return res;
        }
        public Then<T, R> then(Executable<T, R> f) throws Exception {
            Then<T, R> res;
            if (flag == 1) {
                res = new Then<>(val, flag + 1, f.execute(val));
            } else {
                res = new Then<>(val, flag, r);
            }
            return res;
        }
        public Then<T, R> err(Function<T, ? extends Exception> ef) throws Exception {
            if (flag == 1) {
                throw ef.apply(val);
            }
            return new Then<>(val, flag, r);
        }
        public Then<T, R> err(Exception ex) throws Exception {
            if (flag == 1) {
                throw ex;
            }
            return new Then<>(val, flag, r);
        }
    }
    public static class Then<T, R> {
        private final T val;
        private final int flag;
        private final R res;

        private Then(T val, int flag, R r) {
            this.val = val;
            this.flag = flag;
            this.res = r;
        }

        public TypedWhen<T, R> when(T match) {
            return when(v -> Objects.equals(v, match));
        }
        public TypedWhen<T, R> when(Condition<T> condition) {
            TypedWhen<T, R> r;
            if (flag == 2) {
                r = new TypedWhen<>(val, flag, res);
            } else {
                r = new TypedWhen<>(val, condition.evaluate(val) ? 1 : 0, res);
            }
            return r;
        }

        public R otherwise(Provider<R> f) throws Exception {
            return otherwise(f.provide());
        }
        public R otherwise(Executable<T, R> f) throws Exception {
            return otherwise(f.execute(val));
        }
        public R otherwise(R val) {
            R r;
            if (flag == 2) {
                r = res;
            } else {
                r = val;
            }
            return r;
        }
        public R otherwiseNull() throws Exception {
            return otherwise(x -> null);
        }
        public R otherwiseErr(Function<T, ? extends Exception> ef) throws Exception {
            return otherwiseErr(ef.apply(val));
        }
        public R otherwiseErr(Exception ex) throws Exception {
            throw ex;
        }
    }
}
