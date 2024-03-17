package com.simplj.lambda.combinator;

import com.simplj.lambda.function.TriFunction;

public interface Hummingbird<A,B,O> {
    default TriFunction<TriFunction<A,B,A,O>,A,B,O> build() {
        return (f,a,b) -> f.apply(a,b,a);
    }
}
