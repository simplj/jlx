package com.simplj.lambda.util;

import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.function.Function;

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
