package com.simplj.lambda.util.retry;

import com.simplj.lambda.util.Either;

abstract class Retryable<R> {
    final Retry<R> retry;

    Retryable(Retry<R> retry) {
        this.retry = retry;
    }

    public long initialDelay() {
        return retry.initialDelay;
    }

    public long computeDelay(long delay) {
        return retry.delayF.apply(delay);
    }

    public boolean isRetryNeeded(int attempt, long timeElapsed, Either<Exception, ? extends R> e) {
        return retry.retryCondition.isRetryNeeded(attempt, timeElapsed, e);
    }
}
