package com.simplj.lambda.combinator;

import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.TriFunction;

/**
 * The starling combinator takes two functions and a single argument compatible with the first input of both of them.
 * It then proceeds to apply the second function to its argument, then apply the argument and the output of the
 * second function to the first function to give a result
 *
 * @param <A>
 * @param <B>
 * @param <O>
 */

public interface Starling<A,B,O> {
    default TriFunction<BiFunction<A,B,O>, Function<A,B>, A, O> build() {
        return (f,g,a) -> f.apply(a, g.apply(a));
    }
}
