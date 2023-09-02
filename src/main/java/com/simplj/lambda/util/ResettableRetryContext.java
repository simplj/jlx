package com.simplj.lambda.util;

import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.function.Function;

/**
 * Resets the input using the provided reset-er function before attempting for retry.
 * @param <T> Type of the value to reset (which is same as the return type of function that will be retried)
 */
public final class ResettableRetryContext<T> {
    private final RetryContext ctx;
    private final Function<T, T> retryInputResetF;

    ResettableRetryContext(RetryContext ctx, Function<T, T> retryInputResetF) {
        this.ctx = ctx;
        this.retryInputResetF = retryInputResetF;
    }

    public Function<T, T> retryInputResetF() {
        return retryInputResetF;
    }

    /**
     * Executes the Executable f with input T and attempts retry if needed as per the current RetryContext when an exception is occurred during execution.
     * <br>While retrying, it applies the resetting function `retryInputResetF` on `input` to reset the value before passing to the executable again.
     * @param f     Executable to execute (and retry if needed)).
     * @param input value to be passed in the Executable. This will be reset during retry using the provided resetting function.
     * @param <R>   Type of the resultant value of Provider f.
     * @return The resultant value (of type R) if the execution succeeded.
     * @throws Exception when the retry exceeds, throws the exception occurred during execution.
     */
    public <R> R retry(Executable<T, R> f, T input) throws Exception {
        Mutable<Integer> count = Mutable.of(0);
        Mutable<Long> delay = Mutable.of(ctx.initialDelay());
        Lazy<Long> timeLimit = Lazy.of(() -> System.currentTimeMillis() + ctx.maxDuration());
        do {
            try {
                return f.execute(input);
            } catch (Exception ex) {
                //Think if time for applying retryInputResetF needs to be included in the delay i.e. delay - retryInputResetF application time???
                input = retryInputResetF.apply(input);
                if (!ctx.complementRetry(count, timeLimit.get(), ex, delay)) {
                    throw ex;
                }
            }
        } while (true);
    }
}
