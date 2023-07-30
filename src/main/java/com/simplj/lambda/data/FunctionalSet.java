package com.simplj.lambda.data;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.tuples.Couple;

import java.util.Collection;
import java.util.Set;

abstract class FunctionalSet<T, S extends FunctionalSet<T, S>> implements Set<T> {

    public abstract S filter(Condition<T> c);

    public abstract S filterOut(Condition<T> c);

    public abstract Set<T> set();

    public abstract boolean isApplied();

    public abstract S applied();

    public abstract Couple<S, S> split(Condition<T> c);

    public abstract S include(T val);

    public abstract S include(Collection<? extends T> c);

    public abstract S delete(T val);

    public abstract S delete(Collection<? extends T> c);

    public abstract S preserve(Collection<? extends T> c);

    public T find(Condition<T> c) {
        return Util.find(set(), c);
    }

    public boolean none(Condition<T> c) {
        return null == find(c);
    }

    public boolean any(Condition<T> c) {
        return !none(c);
    }

    public boolean all(Condition<T> c) {
        return none(c.negate());
    }

    public <R> R fold(R identity, BiFunction<R, T, R> accumulator) {
        Set<T> s = set();
        for (T t : s) {
            identity = accumulator.apply(identity, t);
        }
        return identity;
    }

    public T reduce(BiFunction<T, T, T> accumulator) {
        T res = null;
        Set<T> s = set();
        boolean flag = false;
        if (!s.isEmpty()) {
            for (T t : s) {
                if (flag) {
                    res = accumulator.apply(res, t);
                } else {
                    res = t;
                    flag = true;
                }
            }
        }
        return res;
    }
}
