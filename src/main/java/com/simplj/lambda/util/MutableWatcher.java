package com.simplj.lambda.util;

@FunctionalInterface
public interface MutableWatcher<A> {
    void onUpdate(A previous, A current);
}
