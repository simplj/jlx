package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.TriFunction;

/**
 * The vireo combinator takes three arguments, and calls the third with the first and second.
 *
 * @param <A>
 * @param <B>
 * @param <O>
 */

public interface Vireo<A,B,O> {
    default TriFunction<A,B, BiFunction<A,B,O>,O> build() {
        return (a,b,f) -> f.apply(a,b);
    }
}
