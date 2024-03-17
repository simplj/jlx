package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Function;

/**
 * The thrush combinator takes two arguments, and calls the second with the first.
 *
 * @param <I>
 * @param <O>
 */

public interface Thrush<I,O> {
    default BiFunction<I, Function<I,O>,O> build() {
        return (a,f) -> f.apply(a);
    }
}
