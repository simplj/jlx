package com.simplj.lambda.util.retry;

import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.executable.Provider;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.util.Mutable;
import com.simplj.lambda.util.Try;

/**
 * Resets the input using the provided reset-er function before attempting for retry.
 * @param <T> Type of the value to reset (which is same as the return type of function that will be retried)
 */
public final class CustomResettableRetryContext<T, R> extends Retryable<R> {
    private final Function<T, T> retryInputResetF;

    CustomResettableRetryContext(Retry<R> retry, Function<T, T> retryInputResetF) {
        super(retry);
        this.retryInputResetF = retryInputResetF;
    }

    /**
     * Executes the Executable f with input T and attempts retry if needed as per the current RetryContext when an exception is occurred during execution.
     * <br>While retrying, it applies the resetting function `retryInputResetF` on `input` to reset the value before passing to the executable again.
     * @param e     Executable to execute (and retry if needed)).
     * @return The resultant value (of type R) if the execution succeeded.
     * @throws Exception when the retry exceeds, throws the exception occurred during execution.
     */
    public R retry(Executable<T, R> e, T input) throws Exception {
        Provider<R> f = e.exec(input);
        Mutable<Integer> count = Mutable.of(0);
        Mutable<Long> delay = Mutable.of(initialDelay());
        Either<Exception, R> res;
        boolean flag;
        long startTs = System.currentTimeMillis();
        do {
            res = Try.execute(f).result();
            flag = retry.complementRetry(count, System.currentTimeMillis() - startTs, res, delay);
            if (flag) {
                input = retryInputResetF.apply(input);
            }
        } while (flag);
        if (res.isLeft()) {
            throw res.left();
        }
        return res.right();
    }
}
