package com.simplj.lambda.util.retry;

import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.executable.Provider;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.util.*;
import com.simplj.lambda.util.Timed.TimedExecution;

/**
 * Resets the input using the provided reset-er function before attempting for retry.
 * @param <T> Type of the value to reset (which is same as the return type of function that will be retried)
 */
public final class ResettableRetryContext<T> extends Retryable<Object> {
    private final Function<T, T> retryInputResetF;

    ResettableRetryContext(Retry<Object> retry, Function<T, T> retryInputResetF) {
        super(retry);
        this.retryInputResetF = retryInputResetF;
    }

    /**
     * Executes the Executable f with input T and attempts retry if needed as per the current RetryContext when an exception is occurred during execution.
     * <br>While retrying, it applies the resetting function `retryInputResetF` on `input` to reset the value before passing to the executable again.
     * @param e     Executable to execute (and retry if needed)).
     * @param input value to be passed in the Executable. This will be reset during retry using the provided resetting function.
     * @param <R>   Type of the resultant value of Provider f.
     * @return The resultant value (of type R) if the execution succeeded.
     * @throws Exception when the retry exceeds, throws the exception occurred during execution.
     */
    public <R> R retry(Executable<T, R> e, T input) throws Exception {
        Provider<R> f = e.exec(input);
        Mutable<Integer> count = Mutable.of(0);
        Mutable<Long> delay = Mutable.of(initialDelay());
        TimedExecution<Either<Exception, R>> res;
        boolean flag;
        do {
            res = Timed.apply(Try.execute(f));
            flag = retry.complementRetry(count, res.duration(), res.result(), delay);
            if (flag) {
                input = retryInputResetF.apply(input);
            }
        } while (flag);
        if (res.result().isLeft()) {
            throw res.result().left();
        }
        return res.result().right();
    }
}
