package com.simplj.lambda.util.retry;

import com.simplj.lambda.function.TriFunction;
import com.simplj.lambda.util.Either;

@FunctionalInterface
public interface RetryCondition<A> {

    boolean isRetryNeeded(int attempt, long elapsedMillis, Either<Exception, ? extends A> result);

    static <T> RetryCondition<T> infinity() {
        return (x1, x2, x3) -> true;
    }
    static <T> RetryCondition<T> never() {
        return (x1, x2, x3) -> false;
    }
    static <T> RetryCondition<T> from(TriFunction<Integer, Long, Either<Exception, ? extends T>, Boolean> f) {
        return f::apply;
    }
}
