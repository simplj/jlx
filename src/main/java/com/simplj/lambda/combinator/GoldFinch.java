package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.QuadFunction;

public interface GoldFinch<A,B,C,O> {
    default QuadFunction<BiFunction<B,C,O>, Function<A, C>, A, B, O> build() {
        return (f,g,a,b) -> f.apply(b, g.apply(a));
    }
}
