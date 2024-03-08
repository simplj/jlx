package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Function;

public interface Apply<A,O> {
    default BiFunction<Function<A,O>,A,O> build() {
        return Function::apply;
    }
}
