package com.simplj.lambda.data;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SlidingWindow<T> implements Iterable<T> {
    private final int limit;
    private final List<T> window;

    private SlidingWindow(int limit) {
        this.limit = limit + 1;
        this.window = new LinkedList<>();
    }

    public static <A> SlidingWindow<A> of(int limit) {
        return new SlidingWindow<>(limit);
    }

    public SlidingWindow<T> add(T val) {
        window.add(val);
        if (window.size() == limit) {
            window.remove(0);
        }
        return this;
    }

    public SlidingWindow<T> insert(int idx, T val) {
        window.add(idx, val);
        if (window.size() == limit) {
            window.remove(0);
        }
        return this;
    }

    public T get(int idx) {
        return window.get(idx);
    }

    public T first() {
        return get(0);
    }

    public T mid() {
        return get(size() / 2);
    }

    public T last() {
        return get(size() - 1);
    }

    public int limit() {
        return limit;
    }

    public int size() {
        return window.size();
    }

    public boolean isEmpty() {
        return window.isEmpty();
    }

    public <R> R fold(R unit, BiFunction<R, T, R> accumulator) {
        for (T t : window) {
            unit = accumulator.apply(unit, t);
        }
        return unit;
    }

    public T reduce(BiFunction<T, T, T> reducer) {
        T res = null;
        if (!window.isEmpty()) {
            res = first();
            for (int i = 1, lim = window.size(); i < lim; i++) {
                res = reducer.apply(res, window.get(i));
            }
        }
        return res;
    }

    public <R> SlidingWindow<R> map(Function<T, R> f) {
        SlidingWindow<R> res = new SlidingWindow<>(limit);
        for (T t : window) {
            res.add(f.apply(t));
        }
        return res;
    }

    public SlidingWindow<T> filter(Condition<T> c) {
        SlidingWindow<T> res = new SlidingWindow<>(limit);
        for (T t : window) {
            if (c.evaluate(t)) {
                res.add(t);
            }
        }
        return res;
    }
    public SlidingWindow<T> filterOut(Condition<T> c) {
        return filter(c.negate());
    }

    public SlidingWindow<T> take(int n) {
        SlidingWindow<T> res = new SlidingWindow<>(limit);
        for (int i = 0; i < n && i < window.size(); i++) {
            res.add(window.get(i));
        }
        return res;
    }

    public SlidingWindow<T> skip(int n) {
        SlidingWindow<T> res = new SlidingWindow<>(limit);
        for (int i = n; i < window.size(); i++) {
            res.add(window.get(i));
        }
        return res;
    }

    public IList<T> toIList() {
        return IList.of(window);
    }

    public List<T> toList() {
        return new LinkedList<>(window);
    }

    @Override
    public Iterator<T> iterator() {
        return window.iterator();
    }

    @Override
    public String toString() {
        return window.toString();
    }
}
