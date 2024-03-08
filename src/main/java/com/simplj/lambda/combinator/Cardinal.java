package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.TriFunction;

/**
 * Cardinal is a combinator which is takes a function having two arguments as input and applies the original function to its arguments in reverse order
 *
 * @param <A> Second Argument of Cardinal combinator
 * @param <B> Third Argument of Cardinal combinator
 * @param <O> Output of Cardinal combinator
 */

public interface Cardinal<A,B,O> {
    default TriFunction<BiFunction<B,A,O>,A,B,O> build() {
        return (f,a,b) -> f.apply(b,a);
    }
}
