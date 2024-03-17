package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;

/**
 * Kite is a combinator which takes two arguments and return the second one
 *
 * @param <A> First argument to Kite
 * @param <B> Second argument to Kite
 */
public interface Kite<A,B> {
    default BiFunction<A,B,B> build() {
        return (a,b) -> b;
    }
}
