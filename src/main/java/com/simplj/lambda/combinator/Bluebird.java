package com.simplj.lambda.combinator;

import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.TriFunction;

/**
 * The bluebird combinator takes three arguments, and calls the first argument with the result of calling
 * the second argument with the third.
 *
 * @param <I>
 * @param <R>
 * @param <O>
 */

public interface Bluebird<I,O> {
    default <R> TriFunction<Function<R,O>,Function<I,R>,I,O> build() {
        return (f,g,a) -> f.apply(g.apply(a));
    }
}
