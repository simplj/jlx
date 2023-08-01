package com.simplj.lambda.data;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.tuples.Couple;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

abstract class FunctionalList<T, L extends FunctionalList<T, L>> implements Iterable<T> {

    abstract List<T> list();

    public abstract L filter(Condition<T> c);

    public abstract L filterOut(Condition<T> c);

    public abstract boolean isApplied();

    public abstract L applied();

    public abstract List<T> toList();

    public abstract Couple<L, L> split(Condition<T> c);

    public abstract int size();
    public abstract boolean isEmpty();
    public abstract boolean contains(Object o);
    public abstract boolean containsAll(Collection<?> c);
    public abstract Object[] toArray();
    public abstract <T1> T1[] toArray(T1[] a);

    public abstract L append(T val);

    public abstract L append(Collection<? extends T> c);

    public abstract L insert(int index, T val);

    public abstract L insert(int index, Collection<? extends T> c);

    public abstract L replace(int index, T val);

    public abstract L delete(int index);

    public abstract L delete(T val);

    public abstract L delete(Collection<? extends T> c);

    public abstract L preserve(Collection<? extends T> c);

    public abstract L empty();

    public abstract T get(int index);
    public abstract int indexOf(Object o);
    public abstract int lastIndexOf(Object o);
    public abstract ListIterator<T> listIterator();
    public abstract ListIterator<T> listIterator(int index);
    public abstract List<T> subList(int fromIndex, int toIndex);
    public abstract L sorted(Comparator<? super T> c);
    public abstract L replacingAll(UnaryOperator<T> operator);
    public abstract L deleteIf(Predicate<? super T> filter);

    public abstract Stream<T> stream();
    public abstract Stream<T> parallelStream();

    public T find(Condition<T> c) {
        return Util.find(list(), c);
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

    public <R> R foldl(R identity, BiFunction<R, T, R> accumulator) {
        List<T> l = list();
        for (T t : l) {
            identity = accumulator.apply(identity, t);
        }
        return identity;
    }

    public <R> R foldr(R origin, BiFunction<T, R, R> accumulator) {
        List<T> l = list();
        int idx = l.size() - 1;
        while (idx >= 0) {
            origin = accumulator.apply(l.get(idx), origin);
            idx--;
        }
        return origin;
    }

    public T reduceL(BiFunction<T, T, T> accumulator) {
        T res = null;
        List<T> l = list();
        if (!l.isEmpty()) {
            res = l.get(0);
            for (int idx = l.size(), i = 1; i < idx; i++) {
                res = accumulator.apply(res, l.get(i));
            }
        }
        return res;
    }

    public T reduceR(BiFunction<T, T, T> accumulator) {
        T res = null;
        List<T> l = list();
        if (!l.isEmpty()) {
            int idx = l.size() - 1;
            res = l.get(idx);
            idx -= 1;
            while (idx >= 0) {
                res = accumulator.apply(l.get(idx), res);
                idx--;
            }
        }
        return res;
    }

    public abstract L copy();
}
