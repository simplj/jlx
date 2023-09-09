package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;

import java.util.LinkedList;

public abstract class MutableArray<E> extends FunctionalArray<E, MutableArray<E>> {
    volatile E[] arr;

    private MutableArray(E[] arr) {
        super();
        this.arr = arr;
    }

    public static <A> MutableArray<A> of(int size) {
        A[] arr = Util.cast(new Object[size]);
        return of(arr);
    }
    public static <A> MutableArray<A> of(A[] arr) {
        return new ArrayFunctor<>(arr, LinkedItem::new, arr);
    }

    public static MutableArray<Integer> of(int...arr) {
        Integer[] a = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static MutableArray<Byte> of(byte...arr) {
        Byte[] a = new Byte[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static MutableArray<Short> of(short...arr) {
        Short[] a = new Short[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static MutableArray<Long> of(long...arr) {
        Long[] a = new Long[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static MutableArray<Float> of(float...arr) {
        Float[] a = new Float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static MutableArray<Double> of(double...arr) {
        Double[] a = new Double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static MutableArray<Boolean> of(boolean...arr) {
        Boolean[] a = new Boolean[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static MutableArray<Character> of(char...arr) {
        Character[] a = new Character[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }

    @Override
    public MutableArray<E> set(int idx, E val) {
        apply();
        arr[idx] = val;
        return this;
    }

    @Override
    public E get(int idx) {
        apply();
        return arr[idx];
    }

    @Override
    public E[] array() {
        apply();
        return arr;
    }

    public abstract <T> MutableArray<T> map(Function<E, T> f);

    public abstract <R> MutableArray<R> flatmap(Function<E, ? extends R[]> f);

    /**
     * @return <code>true</code> if all the lazy functions (if any) are applied otherwise <code>false</code>
     */
    public boolean isApplied() {
        return arr != null;
    }

    public MutableArray<E> applied() {
        apply();
        return this;
    }

    private void apply() {
        if (!isApplied()) {
            arr = appliedArray().arr;
        }
    }

    public abstract MutableArray<E> appliedArray();

    private static final class ArrayFunctor<A, T> extends MutableArray<T> implements Functor<A, T> {
        private final A[] src;
        private final Function<A, LinkedItem<T>> func;

        ArrayFunctor(A[] arr, Function<A, LinkedItem<T>> f, T[] applied) {
            super(applied);
            this.src = arr;
            this.func = f;
        }

        @Override
        MutableArray<T> unit(T[] arr) {
            return new ArrayFunctor<>(arr, LinkedItem::new, arr);
        }

        @Override
        public <R> MutableArray<R> map(Function<T, R> f) {
            return new ArrayFunctor<>(src, map(func, f), null);
        }

        @Override
        public <R> MutableArray<R> flatmap(Function<T, ? extends R[]> f) {
            return new ArrayFunctor<>(src, fmap(func, f), null);
        }

        @Override
        public MutableArray<T> filter(Condition<T> c) {
            return new ArrayFunctor<>(src, filter(func, c), null);
        }

        public final ArrayFunctor<T, T> appliedArray() {
            ArrayFunctor<T, T> res;
            if (arr == null) {
                T[] r = apply(src, func, new LinkedList<>()).toArray(newArray);
                res = new ArrayFunctor<>(r, LinkedItem::new, r);
            } else {
                res = new ArrayFunctor<>(arr, LinkedItem::new, arr);
            }
            return res;
        }
    }
}
