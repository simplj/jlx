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
        return of(Util.cast(new Object[size]));
    }
    public static <A> MutableArray<A> of(A[] arr) {
        return new ArrayFunctor<>(arr, Data::new, arr);
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
            arr = appliedList().arr;
        }
    }

    public abstract MutableArray<E> appliedList();

    private static final class ArrayFunctor<A, T> extends MutableArray<T> implements Functor<A, T> {
        private final A[] src;
        private final Function<A, Data<T>> func;

        ArrayFunctor(A[] arr, Function<A, Data<T>> f, T[] applied) {
            super(applied);
            this.src = arr;
            this.func = f;
        }

        @Override
        MutableArray<T> unit(T[] arr) {
            return new ArrayFunctor<>(arr, Data::new, arr);
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

        public final ArrayFunctor<T, T> appliedList() {
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
