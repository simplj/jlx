package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.QuadFunction;

/**
 * The blackbird combinator composes a binary function with a unary function
 *
 * @param <A>
 * @param <B>
 * @param <C>
 * @param <O>
 */

public interface Blackbird<A,B,C,O> {
    default QuadFunction<Function<C,O>, BiFunction<A,B,C>, A, B, O> build() {
        return (f,g,a,b) -> f.apply(g.apply(a,b));
    }
}
