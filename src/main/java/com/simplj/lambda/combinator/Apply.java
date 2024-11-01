package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Function;

public interface Apply<I,O> {
    default BiFunction<Function<I,O>, I, O> build() {
        return Function::apply;
    }
}
