package com.simplj.lambda.combinator;

import com.simplj.lambda.function.Function;

/**
 * The <b>idiot</b> (a.k.a. <b>identity</b>) is a combinator which takes an identifier as an argument and returns the same argument as result
 *
 * @param <I> Input argument to the combinator, Idiot
 */

public interface Idiot<I> {
    default Function<I,I> build() {
        return a -> a;
    }
}
