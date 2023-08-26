package com.simplj.lambda.util;

import com.simplj.lambda.data.ImmutableSet;
import com.simplj.lambda.function.Consumer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Holds following attributes for retrying an execution:
 * <pre>
 *     Maximum retry attempt
 *     Initial delay
 *     Delay incrementer
 *     Specific exceptions (inclusive/exclusive) to retry
 * </pre>
 */
public final class RetryContext {
    private static final Consumer<String> DEFAULT_LOGGER = l -> System.out.println("jlx.default.logger: " + l);
    private final long initialDelay;
    private final double multiplier;
    private final int maxAttempt;
    private final long maxDelay;
    private final ImmutableSet<Class<? extends Exception>> exceptions;
    private final boolean inclusive;
    private final Consumer<String> logger;

    private RetryContext(RetryContextBuilder builder) {
        this.initialDelay = builder.initialDelay;
        this.multiplier = builder.multiplier;
        this.maxAttempt = builder.maxAttempt;
        this.maxDelay = builder.maxDelay;
        this.exceptions = ImmutableSet.of(builder.exceptions());
        this.inclusive = builder.inclusive;
        this.logger = builder.logger;
    }

    public long initialDelay() {
        return initialDelay;
    }

    public double multiplier() {
        return multiplier;
    }

    public int maxAttempt() {
        return maxAttempt;
    }

    public long maxDelay() {
        return maxDelay;
    }

    public ImmutableSet<Class<? extends Exception>> exceptions() {
        return exceptions;
    }

    public boolean inclusive() {
        return inclusive;
    }

    public Consumer<String> logger() {
        return logger;
    }

    /**
     * Returns {@link RetryContextBuilder} with initial delay, delay multiplier and max retry attempt values set
     * @param initialDelay initial delay for the retry operation
     * @param multiplier   delay multiplier for the retry operation
     * @param maxAttempts  max retry attempt
     * @return {@link RetryContextBuilder} with initial delay, delay multiplier and max retry attempt values set
     */
    public static RetryContextBuilder builder(long initialDelay, double multiplier, int maxAttempts) {
        return new RetryContextBuilder(initialDelay, multiplier, maxAttempts);
    }

    /**
     * Builder class for {@link RetryContext}
     */
    public static class RetryContextBuilder {
        private final long initialDelay;
        private final double multiplier;
        private final int maxAttempt;
        private Consumer<String> logger;
        private long maxDelay = -1;
        private Set<Class<? extends Exception>> exceptions;
        private boolean inclusive;

        private RetryContextBuilder(long initialDelay, double multiplier, int maxAttempt) {
            this.initialDelay = initialDelay;
            this.multiplier = multiplier;
            this.maxAttempt = maxAttempt;
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
            this.exceptions = new HashSet<>(exceptions);
            this.inclusive = isInclusive;
            return this;
        }

        /**
         * Returns {@link RetryContext} instance with the given values
         * @return {@link RetryContext} instance with the given values
         */
        public RetryContext build() {
            return new RetryContext(this);
        }

        private Set<Class<? extends Exception>> exceptions() {
            return exceptions == null ? Collections.emptySet() : exceptions;
        }
    }
}
