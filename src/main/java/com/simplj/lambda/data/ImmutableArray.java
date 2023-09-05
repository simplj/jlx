package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;

import java.util.LinkedList;

public abstract class ImmutableArray<E> extends FunctionalArray<E, ImmutableArray<E>> {
    final E[] arr;

    private ImmutableArray(E[] arr) {
        super();
        this.arr = arr;
    }

    public static <A> ImmutableArray<A> of(int size) {
        A[] arr = Util.cast(new Object[size]);
        return of(arr);
    }
    public static <A> ImmutableArray<A> of(A[] arr) {
        return new ArrayFunctor<>(arr, Data::new, arr);
    }

    public static ImmutableArray<Integer> of(int...arr) {
        Integer[] a = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static ImmutableArray<Byte> of(byte...arr) {
        Byte[] a = new Byte[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static ImmutableArray<Short> of(short...arr) {
        Short[] a = new Short[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static ImmutableArray<Long> of(long...arr) {
        Long[] a = new Long[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static ImmutableArray<Float> of(float...arr) {
        Float[] a = new Float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static ImmutableArray<Double> of(double...arr) {
        Double[] a = new Double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static ImmutableArray<Boolean> of(boolean...arr) {
        Boolean[] a = new Boolean[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }
    public static ImmutableArray<Character> of(char...arr) {
        Character[] a = new Character[arr.length];
        for (int i = 0; i < arr.length; i++) {
            a[i] = arr[i];
        }
        return of(a);
    }

    @Override
    public ImmutableArray<E> set(int idx, E val) {
        ImmutableArray<E> res = applied();
        res.arr[idx] = val;
        return res;
    }

    @Override
    public E get(int idx) {
        return applied().arr[idx];
    }

    @Override
    public E[] array() {
        return applied().arr;
    }

    public abstract <T> ImmutableArray<T> map(Function<E, T> f);

    public abstract <R> ImmutableArray<R> flatmap(Function<E, ? extends R[]> f);

    /**
     * @return <code>true</code> if all the lazy functions (if any) are applied otherwise <code>false</code>
     */
    public boolean isApplied() {
        return arr != null;
    }

    private static final class ArrayFunctor<A, T> extends ImmutableArray<T> implements Functor<A, T> {
        private final A[] src;
        private final Function<A, Data<T>> func;

        ArrayFunctor(A[] arr, Function<A, Data<T>> f, T[] applied) {
            super(applied);
            this.src = arr;
            this.func = f;
        }

        @Override
        ImmutableArray<T> unit(T[] arr) {
            return new ArrayFunctor<>(arr, Data::new, arr);
        }

        @Override
        public <R> ImmutableArray<R> map(Function<T, R> f) {
            return new ArrayFunctor<>(src, map(func, f), null);
        }

        @Override
        public <R> ImmutableArray<R> flatmap(Function<T, ? extends R[]> f) {
            return new ArrayFunctor<>(src, fmap(func, f), null);
        }

        @Override
        public ImmutableArray<T> filter(Condition<T> c) {
            return new ArrayFunctor<>(src, filter(func, c), null);
        }

        public final ArrayFunctor<T, T> applied() {
            ArrayFunctor<T, T> res;
            if (arr == null) {
                T[] r = apply(src, func, new LinkedList<>()).toArray(newArray);
                res = new ArrayFunctor<>(r, Data::new, r);
            } else {
                res = new ArrayFunctor<>(arr, Data::new, arr);
            }
            return res;
        }
    }
}
