package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.TriFunction;

public interface Finch<A,B,O> {
    default TriFunction<A, B, BiFunction<A,B,O>, O> build() {
        return (a,b,f) -> f.apply(a,b);
    }
}
