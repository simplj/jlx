package com.simplj.lambda.util.retry;

import com.simplj.lambda.function.Consumer;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.util.Mutable;

import java.util.concurrent.TimeUnit;

class Retry<A> {
    static final Consumer<String> DEFAULT_LOGGER = l -> System.out.println("jlx.default.logger: " + l);
    final long initialDelay;
    final Function<Long, Long> delayF;
    final RetryCondition<A> retryCondition;
    final Consumer<String> logger;

    Retry(long initialDelay, Function<Long, Long> delayF, RetryCondition<A> retryCondition, Consumer<String> logger) {
        this.initialDelay = initialDelay;
        this.delayF = delayF;
        this.retryCondition = retryCondition;
        this.logger = logger;
    }

    boolean complementRetry(Mutable<Integer> count, long elapsedMillis, Either<Exception, ? extends A> e, Mutable<Long> delay) {
        boolean res = retryCondition.isRetryNeeded(count.get(), elapsedMillis, e);
        if (res) {
            count.mutate(n -> n + 1);
            long currDelay = delay.get();
            logger.consume(String.format("Retrying %s after delay of %s ms...", count, currDelay));
            delay.mutate(this::sleep);
        }
        return res;
    }

    private long sleep(long delay) {
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {
            logger.consume("Sleep interrupted! Reason: " + e.getMessage());
        }
        return delayF.apply(delay);
    }
}
