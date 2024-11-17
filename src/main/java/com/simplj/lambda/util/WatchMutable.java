package com.simplj.lambda.util;

import com.simplj.lambda.function.Condition;

class WatchMutable<A> extends Mutable<A> {
    private final MutableWatcher<A> watcher;

    WatchMutable(A v, MutableWatcher<A> watcher) {
        super(v);
        this.watcher = watcher;
    }

    @Override
    public Mutable<A> set(A val) {
        watcher.onUpdate(value, val);
        return super.set(val);
    }

    @Override
    public Mutable<A> set(Condition<A> condition, A val) {
        if (condition.evaluate(value)) {
            watcher.onUpdate(value, val);
            super.set(val);
        }
        return this;
    }
}