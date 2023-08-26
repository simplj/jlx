package com.simplj.lambda.util;

import com.simplj.lambda.data.Util;
import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.executable.Provider;
import com.simplj.lambda.executable.Receiver;
import com.simplj.lambda.executable.Snippet;
import com.simplj.lambda.function.Consumer;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Try can be used to execute a code which might throw an exception and recovering from the exception if thrown.
 * This is the functional style representation of try-catch block.
 * @param <A>
 */
public class Try<A> {
    private final Executable<AutoCloseableMarker, A> func;
    private RetryContext retryCtx;
    private Consumer<Exception> logger;
    private Function<Exception, Either<Exception, A>> recovery;
    private Function<Exception, Exception> exF;
    private final Map<String, Function<? extends Exception, A>> handlers;

    private Try(Executable<AutoCloseableMarker, A> f, Consumer<Exception> logger, Function<Exception, Either<Exception, A>> recovery) {
        this.func = f;
        this.logger = logger;
        this.recovery = recovery;
        this.exF = Function.id();
        this.handlers = new HashMap<>();
    }

    /**
     * Sets a Provider for execution
     * @param f Provider function to be executed
     * @param <R> Return type of the Provider. This can be get by using the `result()` API of Try.
     * @return An instance of Try with the Provider set for execution
     */
    public static <R> Try<R> execute(Provider<R> f) {
        return execute(f.toExecutable());
    }

    /**
     * Sets a Snippet for execution
     * @param f Snippet to be executed
     * @return An instance of Try with the Snippet set for execution
     */
    public static Try<Void> execute(Snippet f) {
        return execute(f.toExecutable());
    }

    /**
     * Sets a Receiver for execution.
     * The input argument for the Receiver is an instance of AutoCloseableMarker where an AutoCloseable can be marked for auto-closing after the execution of the Receiver.
     * This is similar to `try-with-resource`
     * @param f Consumer to be executed
     * @return An instance of Try with the Receiver set for execution
     */
    public static Try<Void> execute(Receiver<AutoCloseableMarker> f) {
        return execute(f.toExecutable());
    }

    /**
     * Sets a Executable for execution.
     * The input argument for the Executable is an instance of AutoCloseableMarker where an AutoClosable can be marked for auto-closing after the execution of the Executable.
     * This is similar to `try-with-resource`
     * @param f Executable to be executed
     * @param <R> Return type of the Executable. This can be get by using the `result()` API of Try.
     * @return An instance of Try with the Executable set for execution
     */
    public static <R> Try<R> execute(Executable<AutoCloseableMarker, R> f) {
        return new Try<>(f, Try::noOp, Either::<Exception, R>left);
    }

    /**
     * Attempts retry as per the given RetryContext when an exception is occurred during execution
     * @param ctx RetryContext containing max retry attempt, exceptions to retry (inclusive/exclusive) along with other supporting attributes.
     * @return Current instance of Try.
     */
    public Try<A> retry(RetryContext ctx) {
        this.retryCtx = ctx;
        return this;
    }

    /**
     * Logs the exception that may occur during execution.
     * @param f If any exception occurs, then that exception is passed to this Consumer f to be logged
     * @return Current instance of Try.
     */
    public Try<A> log(Consumer<Exception> f) {
        this.logger = f;
        return this;
    }

    /**
     * Takes a Function f which transforms one exception to another.
     * This function is applied on the exception if occurred during the execution.
     * @param f Function which converts one exception to another
     * @return Current instance of Try
     */
    public Try<A> mapException(Function<Exception, Exception> f) {
        Objects.requireNonNull(f);
        this.exF = f;
        return this;
    }

    /**
     * Recovers from an exception that may occur during execution
     * @param f If any exception occurs, then that exception is passed to this Function f to recover from it.
     * @return Current instance of Try
     */
    public Try<A> recover(Function<Exception, A> f) {
        this.recovery = f.andThen(Either::right);
        return this;
    }

    /**
     * Handles a specific Exception sub-type that may occur during execution
     * @param type Specific exception sub-type to handle and recover from the exception
     * @param f    If the specific exception occurs, then that exception is passed to this Function f to recover from it.
     * @param <E>  Sub type of Exception
     * @return Current instance of Try
     */
    public <E extends Exception> Try<A> handle(Class<E> type, Function<E, A> f) {
        if (type.isAssignableFrom(Exception.class)) {
            System.out.println("[WARNING] `Try.handle` is for handling specific exception (sub) types! `Try.recover` can be used for handling generic `Exception`s.");
            this.recovery = e -> Either.right(f.apply(Util.cast(e)));
        } else {
            String name = type.getName();
            if (handlers.containsKey(name)) {
                System.out.println("[WARNING] Recovery is already set for '" + name + "'! Setting recovery is ignored for Exceptions which already has recovery set.");
            } else {
                handlers.put(name, f);
            }
        }
        return this;
    }

    /**
     * Executes the code and returns either the result of type A in right or Exception if occurred in left.
     * @return Either right with result of type A or Exception in left
     */
    public Either<Exception, A> result() {
        Either<Exception, A> res = null;
        AutoCloseableMarker m = new AutoCloseableMarker();
        try {
            res = Either.right(func.execute(m));
        } catch (Exception ex) {
            res = retry(m, ex);
            if (res.isLeft()) {
                ex = res.left();
                ex = exF.apply(ex);
                logger.consume(ex);
                res = handleException(ex);
            }
        } finally {
            List<Couple<AutoCloseable, Exception>> l = m.close();
            if (!l.isEmpty()) {
                res = Either.left(new AutoCloseException(res, l));
            }
        }
        return res;
    }

    /**
     * Executes the code silently i.e. does not return anything and any exception occurred during the execution is suppressed (or logged if set)
     */
    public void run() {
        result();
    }

    private Either<Exception, A> retry(AutoCloseableMarker m, Exception ex) {
        if (retryCtx != null) {
            int count = 0;
            long delay = retryCtx.initialDelay();
            Mutable<Class<? extends Exception>> mE = Mutable.of(ex.getClass());
            while (count < retryCtx.maxAttempt()) {
                //`none` is used instead of `any` to handle empty exception list scenario as well
                if (retryCtx.exceptions().none(e -> retryCtx.inclusive() != e.isAssignableFrom(mE.get()))) {
                    count++;
                    delay = sleep(delay);
                    m.reset();
                    retryCtx.logger().consume("Retrying " + count + " / " + retryCtx.maxAttempt() + "for " + ex.getClass().getName() + " ...");
                    try {
                        return Either.right(func.execute(m));
                    } catch (Exception e) {
                        ex = e;
                        mE.set(ex.getClass());
                    }
                } else {
                    count = retryCtx.maxAttempt();
                }
            }
        }
        return Either.left(ex);
    }

    private long sleep(long delay) {
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {
            retryCtx.logger().consume("Sleep interrupted! Reason: " + e.getMessage());
        }
        return Math.max(retryCtx.maxDelay(), (long) (delay * retryCtx.multiplier()));
    }

    private Either<Exception, A> handleException(Exception ex) {
        Either<Exception, A> res;
        Function<? extends Exception, A> f = handlers.get(ex.getClass().getName());
        if (f == null) {
            res = recovery.apply(ex);
        } else {
            res = Either.right(f.apply(Util.cast(ex)));
        }
        return res;
    }

    private static void noOp(Exception ex) {
        //No Op
    }

    public static class AutoCloseableMarker {
        private final List<AutoCloseable> closeableList;

        private AutoCloseableMarker() {
            closeableList = new LinkedList<>();
        }

        public <T extends AutoCloseable> T markForAutoClose(T closeable) {
            this.closeableList.add(closeable);
            return closeable;
        }

        private void reset() {
            closeableList.clear();
        }

        private List<Couple<AutoCloseable, Exception>> close() {
            List<Couple<AutoCloseable, Exception>> failures;
            if (closeableList.isEmpty()) {
                failures = Collections.emptyList();
            } else {
                failures = new LinkedList<>();
                for (AutoCloseable ac : closeableList) {
                    try {
                        ac.close();
                    } catch (Exception ex) {
                        failures.add(Tuple.of(ac, ex));
                    }
                }
            }
            return failures;
        }
    }

    public static class AutoCloseException extends Exception {
        private final Either<Exception, ?> result;
        private final List<Couple<AutoCloseable, Exception>> closeableList;

        public AutoCloseException(Either<Exception, ?> result, List<Couple<AutoCloseable, Exception>> closeableList) {
            this.result = result;
            this.closeableList = closeableList;
        }

        public Either<Exception, ?> result() {
            return result;
        }

        public List<Couple<AutoCloseable, Exception>> failedCloseableList() {
            return closeableList;
        }
    }
}
