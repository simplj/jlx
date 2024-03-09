package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.QuadFunction;

// TODO: validate this implementation
// Jay is defined as: λabcd.ab(adc) which can be interpreted as: a => b => c => d => a(b)(a(d)(c))

public interface Jay<I,O> {
    default QuadFunction<BiFunction<I,O,O>, I, O, I, O> build() {
        return (a,b,c,d) -> a.apply(b, a.apply(d,c));
    }
}
