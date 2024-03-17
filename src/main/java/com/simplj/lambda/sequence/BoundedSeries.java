package com.simplj.lambda.sequence;

import com.simplj.lambda.data.SlidingWindow;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.util.Mutable;

import java.util.NoSuchElementException;

public final class BoundedSeries<T extends Comparable<T>> extends Series<T> {
    private final Bound<T> bound;

    BoundedSeries(Serial<T> serialF, Condition<T> filter, Mutable<Integer> idx, SlidingWindow<T> pred, Mutable<T> val, Mutable<T> succ, Bound<T> bound) {
        super(serialF, filter, idx, pred, val, succ);
        this.bound = bound;
    }

    public boolean hasNext() {
        return bound.satisfy(idx.get(), pred, succ.get());
    }

    public T next() {
        if (hasNext()) {
            super.next();
        } else {
            val.set(null);
            throw new NoSuchElementException("End of series!");
        }
        return val.get();
    }

    public Series<T> copy() {
        return new BoundedSeries<>(serialF, filter, idx.copy(), pred.copy(), val.copy(), succ.copy(), bound);
    }

    void populateNext() {
        T v = serialF.generate(idx.get(), pred, val.get());
        while (bound.satisfy(idx.get(), pred, v) && !filter.evaluate(v)) {
            v = serialF.generate(idx.get(), pred, v);
        }
        succ.set(v);
    }
}
