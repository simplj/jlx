package com.simplj.lambda.executable;

import com.simplj.lambda.function.Snippet;
import com.simplj.lambda.util.RetryContext;

import java.util.Objects;

@FunctionalInterface
public interface Excerpt {
    void execute() throws Exception;

    /**
     * Attempts retry as per the given RetryContext when an exception is occurred during execution
     * @param ctx RetryContext containing max retry attempt, exceptions to retry (inclusive/exclusive) along with other supporting attributes.
     * @return Excerpt with retrying behavior as per the given RetryContext.
     */
    default Excerpt withRetry(RetryContext ctx) {
        Objects.requireNonNull(ctx);
        return () -> ctx.retry(this);
    }

    default <X> Executable<X, Void> toExecutable() {
        return x -> {
            execute();
            return null;
        };
    }
    default <X> Executable<X, X> yield() {
        return x -> {
            execute();
            return x;
        };
    }

    default <X> Receiver<X> toReceiver() {
        return x -> execute();
    }

    default <X> Provider<X> toProvider() {
        return () -> {
            execute();
            return null;
        };
    }

    static Excerpt retrying(RetryContext ctx, Excerpt f) {
        return f.withRetry(ctx);
    }

    /**
     * """I'm tired of being what you want me to be<br>
     * Feeling so faithless, lost under the surface<br>
     * Don't know what you're expecting of me<br>
     * Put under the pressure of walking in your shoes""" - Linkin Park
     * <br><br>
     * @return Excerpt instance that is numb
     */
    static Excerpt numb() {
        return () -> {};
    }
}
