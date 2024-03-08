package com.simplj.lambda.sequence;

import com.simplj.lambda.data.SlidingWindow;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.util.Mutable;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class Series<T extends Comparable<T>> {
    final Serial<T> serialF;
    final Condition<T> filter;
    final Mutable<Integer> idx;
    final SlidingWindow<T> pred;
    final Mutable<T> val;
    final Mutable<T> succ;

    private Series(Serial<T> serialF, Condition<T> filter, int h, T initial) {
        this.serialF = serialF;
        this.filter = filter;
        this.idx = Mutable.of(0);
        this.pred = SlidingWindow.of(h);
        this.val = Mutable.of(initial);
        T v = serialF.generate(idx.get(), pred, val.get());
        while (!filter.evaluate(v)) {
            v = serialF.generate(idx.get(), pred, v);
        }
        this.succ = Mutable.of(v);
    }

    Series(Serial<T> serialF, Condition<T> filter, Mutable<Integer> idx, SlidingWindow<T> pred, Mutable<T> val, Mutable<T> succ) {
        this.serialF = serialF;
        this.filter = filter;
        this.idx = idx;
        this.pred = pred;
        this.val = val;
        this.succ = succ;
    }

    public static <A extends Comparable<A>> Series<A> of(A initial, Function<A, A> seriesF) {
        return of(initial, (x, y, c) -> seriesF.apply(c));
    }
    public static <A extends Comparable<A>> Series<A> of(A initial, Function<A, A> seriesF, int predCount) {
        return of(initial, (x, y, c) -> seriesF.apply(c), predCount);
    }
    public static <A extends Comparable<A>> Series<A> of(A initial, Serial<A> seriesF) {
        return of(initial, seriesF, Condition.always());
    }
    public static <A extends Comparable<A>> Series<A> of(A initial, Serial<A> seriesF, int predCount) {
        return of(initial, seriesF, Condition.always(), predCount);
    }
    public static <A extends Comparable<A>> Series<A> of(A initial, Function<A, A> seriesF, Condition<A> filter) {
        return of(initial, (x, y, c) -> seriesF.apply(c), filter);
    }
    public static <A extends Comparable<A>> Series<A> of(A initial, Function<A, A> seriesF, Condition<A> filter, int predCount) {
        return of(initial, (x, y, c) -> seriesF.apply(c), filter, predCount);
    }
    public static <A extends Comparable<A>> Series<A> of(A initial, Serial<A> seriesF, Condition<A> filter) {
        return of(initial, seriesF, filter, 1);
    }
    public static <A extends Comparable<A>> Series<A> of(A initial, Serial<A> seriesF, Condition<A> filter, int predCount) {
        return new Series<>(seriesF, filter, predCount, initial);
    }
    public Series<T> bounded(Bound<T> bound) {
        return new BoundedSeries<>(serialF, filter, idx, pred, val, succ, bound);
    }
    public Series<T> circular(Condition<T> resetCondition) {
        T initial = succ.get();
        return new CircularSeries<>(serialF, filter, idx, pred, val, succ, a -> resetCondition.evaluate(a) ? initial : a);
    }

    public boolean hasNext() {
        return true;
    }

    public T next() {
        pred.add(val.get());
        val.set(succ.get());
        idx.mutate(n -> n + 1);
        populateNext();
        return val.get();
    }

    public int count() {
        return idx.get() + 1;
    }

    public T predecessor() {
        return predecessor(0);
    }
    public T predecessor(int i) {
        if (pred.isEmpty()) {
            throw new NoSuchElementException("Series not yet started, hence no predecessor yet!");
        }
        return pred.get(i);
    }

    public T current() {
        return val.get();
    }

    public T successor() {
        return succ.get();
    }
    public List<T> toList() {
        return toList(Integer.MAX_VALUE);
    }
    public List<T> toList(int limit) {
        List<T> res = new LinkedList<>();
        Series<T> s = copy();
        while (s.hasNext() && limit > 0) {
            limit--;
            res.add(s.next());
        }
        return res;
    }

    public Series<T> copy() {
        return new Series<>(serialF, filter, idx.copy(), pred.copy(), val.copy(), succ.copy());
    }

    @Override
    public String toString() {
        return String.format("%s < [%s] < %s", String.join(" < ", pred.map(String::valueOf)), current(), successor());
    }

    void populateNext() {
        T v = serialF.generate(idx.get(), pred, val.get());
        while (!filter.evaluate(v)) {
            v = serialF.generate(idx.get(), pred, v);
        }
        succ.set(v);
    }
}
