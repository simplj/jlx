package com.simplj.lambda.sequence;

import com.simplj.lambda.data.SlidingWindow;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.util.Mutable;

public class CircularSeries<T extends Comparable<T>> extends Series<T> {
    private final Function<T, T> resetF;

    CircularSeries(Serial<T> serialF, Condition<T> filter, Mutable<Integer> idx, SlidingWindow<T> pred, Mutable<T> val, Mutable<T> succ, Function<T, T> resetF) {
        super(serialF, filter, idx, pred, val, succ);
        this.resetF = resetF;
    }

    public CircularSeries<T> copy() {
        return new CircularSeries<>(serialF, filter, idx.copy(), pred.copy(), val.copy(), succ.copy(), resetF);
    }

    void populateNext() {
        super.populateNext();
        succ.set(resetF.apply(succ.get()));
    }
}
