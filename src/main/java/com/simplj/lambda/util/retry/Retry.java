package com.simplj.lambda.util.retry;

import com.simplj.lambda.function.Consumer;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.util.Mutable;

import java.util.concurrent.TimeUnit;

class Retry<A> {
    static final Consumer<String> DEFAULT_LOGGER = l -> System.out.println("jlx.default.logger: " + l);
    private final Mutable<Long> delay;
    private final Function<Long, Long> delayF;
    private final RetryCondition<A> retryCondition;
    private final Consumer<String> logger;

    Retry(long initialDelay, Function<Long, Long> delayF, RetryCondition<A> retryCondition, Consumer<String> logger) {
        this.delay = Mutable.of(initialDelay);
        this.delayF = delayF;
        this.retryCondition = retryCondition;
        this.logger = logger;
    }

    boolean complementRetry(Mutable<Integer> count, long elapsedMillis, Either<Exception, ? extends A> e) {
        boolean res = retryCondition.isRetryNeeded(count.get(), elapsedMillis, e);
        if (res) {
            count.mutate(n -> n + 1);
            long currDelay = delay.get();
            delay.mutate(this::sleep);
            logger.consume(String.format("Retrying %s after delay of %s ms...", count, currDelay));
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
