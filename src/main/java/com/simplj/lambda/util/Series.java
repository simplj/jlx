package com.simplj.lambda.util;

import com.simplj.lambda.data.SlidingWindow;
import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class Series<T extends Comparable<T>> {
    private final BiFunction<SlidingWindow<T>, T, T> serialF;
    private final Condition<T> filterOut;
    private final Condition<T> bound;
    private final Function<T, T> resetF;
    private final SlidingWindow<T> pred;
    private volatile T val;
    private final Mutable<T> succ;
    private final Mutable<Integer> idx;

    private Series(BiFunction<SlidingWindow<T>, T, T> serialF, Condition<T> filter, Condition<T> bound, Function<T, T> resetF, int h, T initial) {
        this.serialF = serialF;
        this.filterOut = filter.negate();
        this.bound = bound;
        this.resetF = resetF;
        this.pred = SlidingWindow.of(h);
        this.succ = Mutable.of(initial);
        this.idx = Mutable.of(0);
    }

    public static <A extends Comparable<A>> Series<A> of(A initial, Function<A, A> seriesF) {
        return of(initial, (a, b) -> seriesF.apply(b));
    }
    public static <A extends Comparable<A>> Series<A> of(A initial, BiFunction<SlidingWindow<A>, A, A> seriesF) {
        return new Series<>(seriesF, Condition.always(), Condition.always(), Function.id(), 1, initial);
    }
    public static <A extends Comparable<A>> Series<A> of(A initial, Function<A, A> seriesF, Condition<A> filter) {
        return of(initial, (a, b) -> seriesF.apply(b), filter);
    }
    public static <A extends Comparable<A>> Series<A> of(A initial, BiFunction<SlidingWindow<A>, A, A> seriesF, Condition<A> filter) {
        return new Series<>(seriesF, filter, Condition.always(), Function.id(), 1, initial);
    }
    public static <A extends Comparable<A>> Series<A> bounded(A initial, Function<A, A> seriesF, Condition<A> bound) {
        return bounded(initial, (a, b) -> seriesF.apply(b), bound);
    }
    public static <A extends Comparable<A>> Series<A> bounded(A initial, BiFunction<SlidingWindow<A>, A, A> seriesF, Condition<A> bound) {
        return new Series<>(seriesF, Condition.always(), bound, Function.id(), 1, initial);
    }
    public static <A extends Comparable<A>> Series<A> bounded(A initial, Function<A, A> seriesF, Condition<A> filter, Condition<A> bound) {
        return bounded(initial, (a, b) -> seriesF.apply(b), filter, bound);
    }
    public static <A extends Comparable<A>> Series<A> bounded(A initial, BiFunction<SlidingWindow<A>, A, A> seriesF, Condition<A> filter, Condition<A> bound) {
        return new Series<>(seriesF, filter, bound, Function.id(), 1, initial);
    }
    public static <A extends Comparable<A>> Series<A> circular(A initial, Function<A, A> seriesF, Condition<A> resetCondition) {
        return circular(initial, (a, b) -> seriesF.apply(b), resetCondition);
    }
    public static <A extends Comparable<A>> Series<A> circular(A initial, BiFunction<SlidingWindow<A>, A, A> seriesF, Condition<A> resetCondition) {
        return new Series<>(seriesF, Condition.always(), Condition.always(), a -> resetCondition.evaluate(a) ? initial : a, 1, initial);
    }
    public static <A extends Comparable<A>> Series<A> circular(A initial, Function<A, A> seriesF, Condition<A> filter, Condition<A> resetCondition) {
        return circular(initial, (a, b) -> seriesF.apply(b), filter, resetCondition);
    }
    public static <A extends Comparable<A>> Series<A> circular(A initial, BiFunction<SlidingWindow<A>, A, A> seriesF, Condition<A> filter, Condition<A> resetCondition) {
        return new Series<>(seriesF, filter, Condition.always(), a -> resetCondition.evaluate(a) ? initial : a, 1, initial);
    }

    public boolean hasNext() {
        return bound.evaluate(succ.get());
    }

    public T next() {
        if (hasNext()) {
            if (idx.get() != 0) {
                pred.add(val);
            }
            val = succ.get();
            populateNext();
            idx.mutate(n -> n + 1);
        } else {
            val = null;
            throw new NoSuchElementException("End of series!");
        }
        return val;
    }

    public T predecessor() {
        return pred.get(0);
    }
    public T predecessor(int i) {
        return pred.get(i);
    }

    public T current() {
        return val;
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
        return new Series<>(serialF, filterOut.negate(), bound, resetF, pred.limit(), succ.get());
    }

    @Override
    public String toString() {
        return String.format("%s < [%s] < %s", String.join(" < ", pred.map(String::valueOf)), current(), successor());
    }

    private void populateNext() {
        T v = serialF.apply(pred, val);
        while (bound.evaluate(v) && filterOut.evaluate(v)) {
            v = serialF.apply(pred, v);
        }
        succ.set(resetF.apply(v));
    }
}
