package com.simplj.lambda.util.retry;

import com.simplj.lambda.executable.Provider;
import com.simplj.lambda.function.Consumer;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.util.Mutable;
import com.simplj.lambda.util.Try;

/**
 * Retries until the provided custom retry logic satisfies.
 * @param <R> Type of the value to reset (which is same as the return type of function that will be retried)
 */
public final class CustomRetryContext<R> extends Retryable<R> {

    private CustomRetryContext(Retry<R> retry) {
        super(retry);
    }

    /**
     * Returns ResettableRetryContext instance with the resetting function. ResettableRetryContext resets the input using the resetF before attempting retry.
     * @param retryInputResetF Function to reset the input while retrying
     * @param <T>              Type of the value to reset (which is same as the return type of function that will be retried)
     * @return {@link ResettableRetryContext} with the resetting function.
     */
    public <T> CustomResettableRetryContext<T, R> resettableContext(Function<T, T> retryInputResetF) {
        return new CustomResettableRetryContext<>(retry, retryInputResetF);
    }

    /**
     * Executes the Provider f and attempts retry if needed as per the current RetryContext when an exception is occurred during execution.
     * @param f   Provider to execute (and retry if needed)).
     * @return The resultant value (of type R) if the execution succeeded.
     * @throws Exception when the retry exceeds, throws the exception occurred during execution.
     */
    public R retry(Provider<R> f) throws Exception {
        Mutable<Integer> count = Mutable.of(0);
        Mutable<Long> delay = Mutable.of(initialDelay());
        Either<Exception, R> res;
        long startTs = System.currentTimeMillis();
        do {
            res = Try.execute(f).result();
        } while (retry.complementRetry(count, System.currentTimeMillis() - startTs, res, delay));
        if (res.isLeft()) {
            throw res.left();
        }
        return res.right();
    }

    public static class CustomRetryContextBuilder<T> {
        private final RetryCondition<T> condition;
        private long initialDelay;
        private Function<Long, Long> delayF;
        private Consumer<String> logger;

        public CustomRetryContextBuilder(RetryCondition<T> condition) {
            this.condition = condition;
            this.initialDelay = 0;
            this.delayF = Function.id();
            this.logger = Retry.DEFAULT_LOGGER;
        }

        public CustomRetryContextBuilder<T> delay(long initialDelay, Function<Long, Long> delayCalculator) {
            this.initialDelay = initialDelay;
            this.delayF = delayCalculator;
            return this;
        }

        /**
         * Sets the application logger to log retry attempts. Default is `System.out.println()`.
         * @param f logger function
         * @return Current instance of {@link RetryContext.RetryContextBuilder}
         */
        public CustomRetryContextBuilder<T> logger(Consumer<String> f) {
            if (f != null) {
                this.logger = f;
            }
            return this;
        }

        public CustomRetryContext<T> build() {
            return new CustomRetryContext<>(new Retry<>(initialDelay, delayF, condition, logger));
        }
    }
}
