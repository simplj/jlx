package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.QuadFunction;

/**
 * Dove
 *
 * @param <A>
 * @param <B>
 * @param <C>
 * @param <O>
 */

public interface Dove<A,B,O> {
    default <C> QuadFunction<BiFunction<A,C,O>, A, Function<B,C>, B, O> build() {
        return (f,a,g,b) -> f.apply(a, g.apply(b));
    }
}
