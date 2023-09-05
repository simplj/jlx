package com.simplj.lambda.util;

import com.simplj.lambda.executable.Provider;
import com.simplj.lambda.executable.Excerpt;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Consumer;
import com.simplj.lambda.function.Function;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Holds following attributes for retrying an execution:
 * <pre>
 *     Maximum retry attempt
 *     Initial delay
 *     Delay incrementer
 *     Specific exceptions (inclusive/exclusive) to retry
 * </pre>
 */
public class RetryContext {
    private static final Consumer<String> DEFAULT_LOGGER = l -> System.out.println("jlx.default.logger: " + l);
    private final long initialDelay;
    private final double multiplier;
    private final long maxDelay;
    private final int maxAttempt;
    private final long maxDuration;
    private final Condition<Exception> exceptionF;
    private final boolean isInclusive;
    private final Consumer<String> logger;
    private final String notification;

    RetryContext(RetryContextBuilder builder) {
        this.initialDelay = builder.initialDelay;
        this.multiplier = builder.multiplier;
        this.maxDelay = builder.maxDelay;
        this.maxAttempt = builder.maxAttempt;
        this.maxDuration = builder.maxDuration;
        this.exceptionF = builder.exceptionF == null ? e -> true : builder.exceptionF;
        this.isInclusive = builder.isInclusive;
        this.logger = builder.logger;
        if (maxAttempt < 0) {
            this.notification = "Retrying %s after delay of %s ms for %s ...";
        } else {
            this.notification = "Retrying [%s / " + maxAttempt + "] after delay of %s ms for %s ...";
        }
    }

    public long initialDelay() {
        return initialDelay;
    }

    public double multiplier() {
        return multiplier;
    }

    public long maxDelay() {
        return maxDelay;
    }

    public int maxAttempt() {
        return maxAttempt;
    }

    public long maxDuration() {
        return maxDuration;
    }

    public boolean retryNeededFor(Exception ex) {
        return exceptionF.evaluate(ex);
    }

    public boolean isInclusive() {
        return isInclusive;
    }

    public Consumer<String> logger() {
        return logger;
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
        Mutable<Long> delay = Mutable.of(initialDelay);
        Lazy<Long> timeLimit = Lazy.of(() -> System.currentTimeMillis() + maxDuration);
        do {
            try {
                return f.provide();
            } catch (Exception ex) {
                if (!complementRetry(count, timeLimit.get(), ex, delay)) {
                    throw ex;
                }
            }
        } while (true);
    }

    /**
     * Returns {@link RetryContextBuilder} with initial delay, delay multiplier and max retry attempt values set.
     * @param initialDelay initial delay for the retry operation
     * @param multiplier   delay multiplier for the retry operation
     * @param maxAttempts  max retry attempt
     * @return {@link RetryContextBuilder} with initial delay, delay multiplier and max retry attempt values set
     */
    public static RetryContextBuilder builder(long initialDelay, double multiplier, int maxAttempts) {
        if (initialDelay < 0 || maxAttempts < 0) {
            throw new IllegalArgumentException("Initial Delay or Max Attempts cannot be Negative!");
        }
        return new RetryContextBuilder(initialDelay, multiplier, maxAttempts, -1L);
    }

    /**
     * Returns {@link RetryContextBuilder} with initial delay, delay multiplier and max retry attempt values set.
     * @param initialDelay initial delay for the retry operation
     * @param multiplier   delay multiplier for the retry operation
     * @param maxDuration  maximum time (in milliseconds) to keep retrying
     * @return {@link RetryContextBuilder} with initial delay, delay multiplier and max retry attempt values set
     */
    public static RetryContextBuilder builder(long initialDelay, double multiplier, long maxDuration) {
        if (initialDelay < 0 || maxDuration < 0) {
            throw new IllegalArgumentException("Initial Delay or Max Duration cannot be Negative!");
        }
        return new RetryContextBuilder(initialDelay, multiplier, -1, maxDuration);
    }

    /**
     * Returns {@link RetryContextBuilder} with initial delay, delay multiplier and max retry attempt values set.
     * @param initialDelay initial delay for the retry operation
     * @param multiplier   delay multiplier for the retry operation
     * @param maxAttempts  max retry attempt
     * @param maxDuration  maximum time (in milliseconds) to keep retrying
     * @return {@link RetryContextBuilder} with initial delay, delay multiplier and max retry attempt values set
     */
    public static RetryContextBuilder builder(long initialDelay, double multiplier, int maxAttempts, long maxDuration) {
        if (initialDelay < 0 || maxAttempts < 0 || maxDuration < 0) {
            throw new IllegalArgumentException("Initial Delay or Max Attempts or Max Duration cannot be Negative!");
        }
        return new RetryContextBuilder(initialDelay, multiplier, maxAttempts, maxDuration);
    }

    /**
     * Returns ResettableRetryContext instance with the resetting function. ResettableRetryContext resets the input using the resetF before attempting retry.
     * @param retryInputResetF Function to reset the input while retrying
     * @param <T>              Type of the value to reset (which is same as the return type of function that will be retried)
     * @return {@link ResettableRetryContext} with the resetting function.
     */
    public <T> ResettableRetryContext<T> resettableContext(Function<T, T> retryInputResetF) {
        return new ResettableRetryContext<>(this, retryInputResetF);
    }

    boolean complementRetry(Mutable<Integer> count, long timeLimitTs, Exception ex, Mutable<Long> delay) {
        boolean res = (maxAttempt < 0 || count.get() < maxAttempt) && (maxDuration < 0 || System.currentTimeMillis() < timeLimitTs) && exceptionF.evaluate(ex);
        if (res) {
            count.mutate(n -> n + 1);
            long currDelay = delay.get();
            delay.mutate(this::sleep);
            logger.consume(String.format(notification, count, currDelay, ex.getClass().getName()));
        }
        return res;
    }

    private long sleep(long delay) {
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {
            logger.consume("Sleep interrupted! Reason: " + e.getMessage());
        }
        return Math.max(maxDelay, (long) (delay * multiplier));
    }

    /**
     * Builder class for {@link RetryContext}
     */
    public static class RetryContextBuilder {
        private final long initialDelay;
        private final double multiplier;
        private final int maxAttempt;
        private final long maxDuration;
        private Consumer<String> logger;
        private long maxDelay = -1;
        private Condition<Exception> exceptionF;
        private boolean isInclusive;

        private RetryContextBuilder(long initialDelay, double multiplier, int maxAttempt, long maxDuration) {
            this.initialDelay = initialDelay;
            this.multiplier = multiplier;
            this.maxAttempt = maxAttempt;
            this.maxDuration = maxDuration;
            this.logger = DEFAULT_LOGGER;
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
         * Sets max delay between retry attempts. Calculates delay time between retry attempts like below:
         * <pre>
         *     max(maxDelay, calculated delay using delay multiplier)
         * </pre>
         * @param maxDelay max delay value
         * @return Current instance of {@link RetryContextBuilder}
         */
        public RetryContextBuilder maxDelay(long maxDelay) {
            this.maxDelay = maxDelay;
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
            this.isInclusive = isInclusive;
            this.exceptionF = ex -> exceptions.stream().anyMatch(e -> isInclusive == e.isAssignableFrom(ex.getClass()));
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
