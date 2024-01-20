package com.simplj.lambda.util.retry;

import com.simplj.lambda.executable.Excerpt;
import com.simplj.lambda.executable.Provider;
import com.simplj.lambda.function.Consumer;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.util.Mutable;
import com.simplj.lambda.util.Timed;
import com.simplj.lambda.util.Timed.TimedExecution;
import com.simplj.lambda.util.Try;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Holds following attributes for retrying an execution:
 * <pre>
 *     Maximum retry (attempt or duration or both)
 *     Initial delay
 *     Delay calculator
 *     Specific exceptions (inclusive/exclusive) to retry
 * </pre>
 */
public class RetryContext extends Retryable<Object> {

    private RetryContext(RetryContextBuilder builder) {
        super(new Retry<>(builder.initialDelay, builder.delayF, buildRetryCondition(builder), builder.preRetryHook, builder.logger));
    }

    /**
     * Executes the Excerpt f and attempts retry if needed as per the current RetryContext when an exception is occurred during execution.
     * @param f Excerpt to execute (and retry if needed)).
     * @throws Exception when the retry exceeds, throws the exception occurred during execution.
     */
    public void retry(Excerpt f) throws Exception {
        Objects.requireNonNull(f);
        retry(f.toProvider());
    }

    /**
     * Executes the Provider f and attempts retry if needed as per the current RetryContext when an exception is occurred during execution.
     * @param f   Provider to execute (and retry if needed)).
     * @param <R> Type of the resultant value of Provider f.
     * @return The resultant value (of type R) if the execution succeeded.
     * @throws Exception when the retry exceeds, throws the exception occurred during execution.
     */
    public <R> R retry(Provider<R> f) throws Exception {
        Mutable<Integer> count = Mutable.of(0);
        Mutable<Long> delay = Mutable.of(initialDelay());
        TimedExecution<Either<Exception, R>> res;
        do {
            res = Timed.apply(Try.execute(f));
        } while (retry.complementRetry(count, res.duration(), res.result(), delay));
        if (res.result().isLeft()) {
            throw res.result().left();
        }
        return res.result().right();
    }

    /**
     * Returns {@link RetryContextBuilder} with initial delay, delayCalculator and max retry attempt values set.
     * @param initialDelay initial delay for the retry operation
     * @param delayCalculator   delay delayCalculator for the retry operation
     * @param maxAttempts  max retry attempt
     * @return {@link RetryContextBuilder} with initial delay, delay delayCalculator and max retry attempt values set
     */
    public static RetryContextBuilder times(long initialDelay, Function<Long, Long> delayCalculator, int maxAttempts) {
        if (initialDelay < 0 || maxAttempts < 0) {
            throw new IllegalArgumentException("Initial Delay or Max Attempts cannot be Negative!");
        }
        return new RetryContextBuilder(initialDelay, delayCalculator, maxAttempts, -1L, 1);
    }

    /**
     * Returns {@link RetryContextBuilder} with initial delay, delayCalculator and max retry attempt values set.
     * @param initialDelay initial delay for the retry operation
     * @param delayCalculator   delay delayCalculator for the retry operation
     * @param maxDuration  maximum time (in milliseconds) to keep retrying
     * @return {@link RetryContextBuilder} with initial delay, delay delayCalculator and max retry attempt values set
     */
    public static RetryContextBuilder duration(long initialDelay, Function<Long, Long> delayCalculator, long maxDuration) {
        if (initialDelay < 0 || maxDuration < 0) {
            throw new IllegalArgumentException("Initial Delay or Max Duration cannot be Negative!");
        }
        return new RetryContextBuilder(initialDelay, delayCalculator, -1, maxDuration + 1, 2);
    }

    /**
     * Returns {@link RetryContextBuilder} with initial delay, delayCalculator and max retry attempt values set.
     * @param initialDelay initial delay for the retry operation
     * @param delayCalculator   delay delayCalculator for the retry operation
     * @param maxAttempts  max retry attempt
     * @param maxDuration  maximum time (in milliseconds) to keep retrying
     * @return {@link RetryContextBuilder} with initial delay, delay delayCalculator and max retry attempt values set
     */
    public static RetryContextBuilder builder(long initialDelay, Function<Long, Long> delayCalculator, int maxAttempts, long maxDuration) {
        if (initialDelay < 0 || maxAttempts < 0 || maxDuration < 0) {
            throw new IllegalArgumentException("Initial Delay or Max Attempts or Max Duration cannot be Negative!");
        }
        return new RetryContextBuilder(initialDelay, delayCalculator, maxAttempts, maxDuration + 1, 3);
    }

    /**
     * Sets a custom retry condition for retrying.
     * @param condition custom condition for retrying.
     * @param <T>       Type of resultant value of the retry operation
     * @return CustomRetryContextBuilder
     */
    public static <T> CustomRetryContext.CustomRetryContextBuilder<T> custom(RetryCondition<T> condition) {
        return new CustomRetryContext.CustomRetryContextBuilder<>(condition);
    }

    /**
     * Returns ResettableRetryContext instance with the resetting function. ResettableRetryContext resets the input using the resetF before attempting retry.
     * @param retryInputResetF Function to reset the input while retrying
     * @param <T>              Type of the value to reset (which is same as the return type of function that will be retried)
     * @return {@link ResettableRetryContext} with the resetting function.
     */
    public <T> ResettableRetryContext<T> resettableContext(Function<T, T> retryInputResetF) {
        return new ResettableRetryContext<>(retry, retryInputResetF);
    }

    private static RetryCondition<Object> buildRetryCondition(RetryContextBuilder builder) {
        RetryCondition<Object> res;
        int mA = builder.maxAttempt;
        long mD = builder.maxDuration;
        boolean isInclusive = builder.isInclusive;
        Set<Class<? extends Exception>> exceptions = builder.exceptions;
        switch (builder.spec) {
            case 1:
                res = (c, x, e) -> e.isLeft() && c < mA;
                break;
            case 2:
                res = (x, d, e) -> e.isLeft() && d < mD;
                break;
            case 3:
                res = (c, d, e) -> e.isLeft() && c < mA && d < mD;
                break;
            case 4:
                res = (x1, x2, e) -> e.isLeft() && exceptions.stream().anyMatch(ex -> isInclusive == ex.isAssignableFrom(e.left().getClass()));
                break;
            case 5:
                res = (c, x, e) -> e.isLeft() && c < mA && exceptions.stream().anyMatch(ex -> isInclusive == ex.isAssignableFrom(e.left().getClass()));
                break;
            case 6:
                res = (x, d, e) -> e.isLeft() && d < mD && exceptions.stream().anyMatch(ex -> isInclusive == ex.isAssignableFrom(e.left().getClass()));
                break;
            case 7:
                res = (c, d, e) -> e.isLeft() && c < mA && d < mD && exceptions.stream().anyMatch(ex -> isInclusive == ex.isAssignableFrom(e.left().getClass()));
                break;
            default:
                res = RetryCondition.never();
                break;
        }
        return res;
    }

    /**
     * Builder class for {@link RetryContext}
     */
    public static class RetryContextBuilder {
        private final long initialDelay;
        private final Function<Long, Long> delayF;
        private final int maxAttempt;
        private final long maxDuration;
        private Consumer<String> logger;
        private Set<Class<? extends Exception>> exceptions;
        private boolean isInclusive;
        private int spec;//attempt=1;duration=2;exception=4;
        private Excerpt preRetryHook;

        private RetryContextBuilder(long initialDelay, Function<Long, Long> delayF, int maxAttempt, long maxDuration, int spec) {
            this.initialDelay = initialDelay;
            this.delayF = delayF;
            this.maxAttempt = maxAttempt;
            this.maxDuration = maxDuration;
            this.spec = spec;
            this.logger = Retry.DEFAULT_LOGGER;
            this.preRetryHook = Excerpt.numb();
        }

        /**
         * Sets the application logger to log retry attempts. Default is `System.out.println()`.
         * @param f logger function
         * @return Current instance of {@link RetryContextBuilder}
         */
        public RetryContextBuilder logger(Consumer<String> f) {
            if (f != null) {
                this.logger = f;
            }
            return this;
        }

        /**
         * Sets an excerpt which gets executed before each retry.
         * @param hook excerpt which will be executed before each retry
         * @return Current instance of {@link RetryContextBuilder}
         */
        public RetryContextBuilder registerPreRetryHook(Excerpt hook) {
            if (hook != null) {
                this.preRetryHook = hook;
            }
            return this;
        }

        /**
         * Sets exceptions (inclusive/exclusive) for which to retry.
         * @param exceptions  exceptions for which to retry
         * @param isInclusive indicates whether the exceptions are inclusive or exclusive i.e. whether to retry if the given exception occur or the given exceptions does not occur
         * @param <T>         sub type of Exception
         * @return Current instance of {@link RetryContextBuilder}
         */
        public <T extends Exception> RetryContextBuilder exceptions(Set<Class<T>> exceptions, boolean isInclusive) {
            if (exceptions == null || exceptions.isEmpty()) {
                throw new IllegalArgumentException("Exceptions cannot be null or empty! Got: " + exceptions);
            }
            if (this.exceptions == null) {
                this.spec += 4;
            } else {
                logger.consume("[WARNING] jlx.RetryContext - Re-assignment of exceptions is detected! Existing set of " + (this.isInclusive ? "in" : "ex") + "clusive exception is overridden.");
            }
            this.isInclusive = isInclusive;
            this.exceptions = new HashSet<>();
            this.exceptions.addAll(exceptions);
            return this;
        }

        /**
         * Returns {@link RetryContext} instance with the given values
         * @return {@link RetryContext} instance with the given values
         */
        public RetryContext build() {
            return new RetryContext(this);
        }
    }
}
