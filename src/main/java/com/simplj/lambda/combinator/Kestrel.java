package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;

/**
 * Kestrel is a combinator which takes two arguments and return the first one
 *
 * @param <A> First argument to Kestrel
 * @param <B> Second argument to Kestrel
 */

public interface Kestrel<A,B> {
    default BiFunction<A,B,A> build() {
        return (a,b) -> a;
    }
}
