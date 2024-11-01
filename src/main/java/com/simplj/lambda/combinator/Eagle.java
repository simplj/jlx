package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.PentaFunction;

public interface Eagle<A,B,C,D,O> {
    default PentaFunction<BiFunction<A,B,O>, A, BiFunction<C,D,B>, C, D, O> build() {
        return (a,b,c,d,e) -> a.apply(b, c.apply(d, e));
    }
}
