package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

import java.util.LinkedList;

public abstract class IArray<E> extends FArray<E, IArray<E>> {
    final E[] arr;

    private IArray(E[] arr) {
        super();
        this.arr = arr;
    }

    public static <A> IArray<A> of(int size) {
        A[] arr = Util.cast(new Object[size]);
        return of(arr);
    }
    public static <A> IArray<A> of(A[] arr) {
        return new ArrayFunctor<>(arr, LinkedUnit::new, arr);
    }

    public static IArray<Integer> of(int...arr) {
        Integer[] a = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static IArray<Byte> of(byte...arr) {
        Byte[] a = new Byte[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static IArray<Short> of(short...arr) {
        Short[] a = new Short[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static IArray<Long> of(long...arr) {
        Long[] a = new Long[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static IArray<Float> of(float...arr) {
        Float[] a = new Float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static IArray<Double> of(double...arr) {
        Double[] a = new Double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static IArray<Boolean> of(boolean...arr) {
        Boolean[] a = new Boolean[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static IArray<Character> of(char...arr) {
        Character[] a = new Character[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }

    @Override
    public IArray<E> set(int idx, E val) {
        IArray<E> res = applied();
        res.arr[idx] = val;
        return res;
    }

    @Override
    public E get(int idx) {
        return applied().arr[idx];
    }

    @Override
    E[] array() {
        return applied().arr;
    }

    public abstract <T> IArray<T> map(Function<E, T> f);

    public abstract <R> IArray<R> flatmap(Function<E, ? extends R[]> f);

    public IArray<Couple<Integer, E>> indexed() {
        return foldl(Tuple.of(0, IArray.<Couple<Integer, E>>of(arr.length)), (c, v) -> Tuple.of(c.first() + 1, c.second().set(c.first(), Tuple.of(c.first(), v)))).second();
    }

    /**
     * @return <code>true</code> if all the lazy functions (if any) are applied otherwise <code>false</code>
     */
    public boolean isApplied() {
        return arr != null;
    }

    public final IArray<E> applied() {
        IArray<E> res;
        if (isApplied()) {
            res = this;
        } else {
            res = appliedArray();
        }
        return res;
    }

    public abstract IArray<E> appliedArray();

    private static final class ArrayFunctor<A, T> extends IArray<T> implements Functor<A, T> {
        private final A[] src;
        private final Function<A, LinkedUnit<T>> func;

        ArrayFunctor(A[] arr, Function<A, LinkedUnit<T>> f, T[] applied) {
            super(applied);
            this.src = arr;
            this.func = f;
        }

        @Override
        IArray<T> unit(T[] arr) {
            return new ArrayFunctor<>(arr, LinkedUnit::new, arr);
        }

        @Override
        public <R> IArray<R> map(Function<T, R> f) {
            return new ArrayFunctor<>(src, map(func, f), null);
        }

        @Override
        public <R> IArray<R> flatmap(Function<T, ? extends R[]> f) {
            return new ArrayFunctor<>(src, fmap(func, f), null);
        }

        @Override
        public IArray<T> filter(Condition<T> c) {
            return new ArrayFunctor<>(src, filter(func, c), null);
        }

        public final ArrayFunctor<T, T> appliedArray() {
            ArrayFunctor<T, T> res;
            if (arr == null) {
                T[] r = apply(src, func, new LinkedList<>()).toArray(newArray);
                res = new ArrayFunctor<>(r, LinkedUnit::new, r);
            } else {
                res = new ArrayFunctor<>(arr, LinkedUnit::new, arr);
            }
            return res;
        }
    }
}
