package com.simplj.lambda.util;

import com.simplj.lambda.data.Util;
import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.executable.Provider;
import com.simplj.lambda.executable.Receiver;
import com.simplj.lambda.executable.Excerpt;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Consumer;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.monadic.Thunk;
import com.simplj.lambda.monadic.exception.FilteredOutException;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;
import com.simplj.lambda.util.retry.RetryContext;

import java.util.*;

/**
 * Try can be used to execute a code which might throw an exception and recovering from the exception if thrown.
 * This is the functional style representation of try-catch block.
 * @param <A> Type of the resultant value
 */
public class Try<A> {
    private final Executable<AutoCloseableMarker, A> func;
    private Consumer<Exception> logger;
    private Excerpt finalizeF;
    private final Map<String, Function<? extends Exception, A>> handlers;

    Try(Executable<AutoCloseableMarker, A> f, Consumer<Exception> logger, Excerpt finalizeF) {
        this.func = f;
        this.logger = logger;
        this.finalizeF = finalizeF;
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
     * Sets a Provider for execution. Resultant `Either` of the Provider is flattened after execution.
     * @param t Try to be flattened (and executed)
     * @param <R> Result type of the Try.
     * @return An instance of Try flattening another Try instance
     */
    public static <R> Try<R> flatten(Try<R> t) {
        return flatExecute(t::result);
    }

    /**
     * Sets a Provider for execution. Resultant `Either` of the Provider is flattened after execution.
     * @param f Provider function to be executed
     * @param <R> Return type of the Provider. This can be get by using the `result()` API of Try.
     * @return An instance of Try with the Provider set for execution
     */
    public static <R> Try<R> flatExecute(Provider<Either<Exception, R>> f) {
        return flatExecute(f.toExecutable());
    }

    /**
     * Sets a Excerpt for execution
     * @param f Excerpt to be executed
     * @return An instance of Try with the Excerpt set for execution
     */
    public static Try<Void> execute(Excerpt f) {
        return execute(f.toExecutable());
    }

    /**
     * Sets a Receiver for execution.
     * The input argument for the Receiver is an instance of AutoCloseableMarker where an AutoCloseable can be marked for auto-closing after the execution of the Receiver.
     * This is similar to `try-with-resource`.
     * Passing a `Receiver with Retry` can result into unexpected behavior, hence, it is strongly recommended to use `Try.retry` when a retry mechanism is required.
     * @param f Consumer to be executed
     * @return An instance of Try with the Receiver set for execution
     */
    public static Try<Void> execute(Receiver<AutoCloseableMarker> f) {
        return execute(f.toExecutable());
    }

    /**
     * Sets a Executable for execution.
     * The input argument for the Executable is an instance of AutoCloseableMarker where an AutoClosable can be marked for auto-closing after the execution of the Executable.
     * This is similar to `try-with-resource`.
     * Passing a `Executable with Retry` can result into unexpected behavior, hence, it is strongly recommended to use `Try.retry` when a retry mechanism is required.
     * @param f Executable to be executed
     * @param <R> Return type of the Executable. This can be get by using the `result()` API of Try.
     * @return An instance of Try with the Executable set for execution
     */
    public static <R> Try<R> execute(Executable<AutoCloseableMarker, R> f) {
        return new Try<>(f, Consumer.noOp(), Excerpt.numb());
    }

    /**
     * Sets a Executable for execution. Resultant `Either` of the Provider is flattened after execution.
     * The input argument for the Executable is an instance of AutoCloseableMarker where an AutoClosable can be marked for auto-closing after the execution of the Executable.
     * This is similar to `try-with-resource`.
     * Passing a `Executable with Retry` can result into unexpected behavior, hence, it is strongly recommended to use `Try.retry` when a retry mechanism is required.
     * @param f Executable to be executed
     * @param <R> Return type of the Executable. This can be get by using the `result()` API of Try.
     * @return An instance of Try with the Executable set for execution
     */
    public static <R> Try<R> flatExecute(Executable<AutoCloseableMarker, Either<Exception, R>> f) {
        return execute(f.andThen(e -> {
            if (e.isLeft()) {
                throw e.left();
            }
            return e.right();
        }));
    }

    /**
     * It is recommended to use `handler` and `recover` APIs after this API as all handlers and recovery will be reset by this operation.
     * <br>* This API is lazy.
     * @param f   executable to be applied on the Left value
     * @param <R> Type of resultant value of Function f
     * @return A new Try instance having the provided Executable composed with the existing one
     */
    public <R> Try<R> map(Executable<A, R> f) {
        return new Try<>(func.andThen(f), logger, finalizeF);
    }

    /**
     * It is recommended to use `handler` and `recover` APIs after this API as all handlers and recovery will be reset by this operation.
     * <br>* This API is lazy.
     * @param f   executable to be applied on the Left value
     * @param <R> Type of resultant value of Function f
     * @return A new Try instance having the provided Executable composed with the existing one
     */
    public <R> Try<R> flatmap(Executable<A, Try<R>> f) {
        return new Try<>(func.andThen(f).andThen(Try::resultOrThrow), logger, finalizeF);
    }

    public Try<A> filter(Condition<A> condition) {
        Executable<AutoCloseableMarker, A> next = func.andThen(r -> {
            if (!condition.evaluate(r)) {
                throw new FilteredOutException(r);
            }
            return r;
        });
        return new Try<>(next, logger, finalizeF);
    }
    public <X extends Exception> Try<A> filter(Condition<A> condition, Producer<X> exF) throws X {
        Executable<AutoCloseableMarker, A> next = func.andThen(r -> {
            if (!condition.evaluate(r)) {
                throw exF.produce();
            }
            return r;
        });
        return new Try<>(next, logger, finalizeF);
    }
    public <X extends Exception> Try<A> filter(Condition<A> condition, X ex) throws X {
        Executable<AutoCloseableMarker, A> next = func.andThen(r -> {
            if (!condition.evaluate(r)) {
                throw ex;
            }
            return r;
        });
        return new Try<>(next, logger, finalizeF);
    }

    public Try<A> record(Consumer<A> consumer) {
        return new Try<>(func.andThen(Receiver.of(consumer::consume).yield()), logger, finalizeF);
    }

    /**
     * Attempts retry as per the given RetryContext when an exception is occurred during execution.
     * @param ctx RetryContext containing max retry attempt, exceptions to retry (inclusive/exclusive) along with other supporting attributes.
     * @return Current instance of Try.
     */
    public Try<A> retry(RetryContext ctx) {
        return new Try<>(func.withRetry(ctx.resettableContext(AutoCloseableMarker::reset)), logger, finalizeF);
    }

    /**
     * Attempts retry as per the given arguments when an exception is occurred during execution.
     * @param initialDelay initial delay for the retry operation
     * @param multiplier   delay multiplier for the retry operation
     * @param maxAttempts  max retry attempt
     * @return Current instance of Try.
     */
    public Try<A> retry(long initialDelay, double multiplier, int maxAttempts) {
        return new Try<>(func.withRetry(RetryContext.times(initialDelay, l -> (long) (l * multiplier), maxAttempts).build().resettableContext(AutoCloseableMarker::reset)), logger, finalizeF);
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
     * @param <E> Type of `Exception` to be mapped to
     * @return Current instance of Try
     */
    public <E extends Exception> TypedTry<E, A> mapException(Function<Exception, E> f) {
        Objects.requireNonNull(f);
        Executable<AutoCloseableMarker, A> next = a -> {
            try {
                return func.execute(a);
            } catch (Exception ex) {
                logger.consume(ex);
                logger = Consumer.noOp();
                throw f.apply(ex);
            }
        };
        return new TypedTry<>(next, logger, finalizeF);
    }

    /**
     * Recovers from an exception that may occur during execution.
     * @param f If any exception occurs, then that exception is passed to this Function f to recover from it.
     * @return Current instance of Try
     */
    public Try<A> recover(Function<Exception, A> f) {
        Objects.requireNonNull(f);
        Executable<AutoCloseableMarker, A> next = a -> {
            A res;
            try {
                res = func.execute(a);
            } catch (Exception ex) {
                logger.consume(ex);
                res = f.apply(ex);
            }
            return res;
        };
        return new Try<>(next, logger, finalizeF);
    }

    public Try<A> recoverWhen(Condition<Exception> condition, Function<Exception, A> recovery) {
        Objects.requireNonNull(condition);
        Objects.requireNonNull(recovery);
        Executable<AutoCloseableMarker, A> next = a -> {
            A res;
            try {
                res = func.execute(a);
            } catch (Exception ex) {
                logger.consume(ex);
                if (condition.evaluate(ex)) {
                    res = recovery.apply(ex);
                } else {
                    throw ex;
                }
            }
            return res;
        };
        return new Try<>(next, logger, finalizeF);
    }
    public Try<A> recoverOn(Class<? extends Exception> clazz, Function<Exception, A> recovery) {
        Objects.requireNonNull(clazz);
        return recoverWhen(e -> clazz.isAssignableFrom(e.getClass()), recovery);
    }

    /**
     * Handles a specific Exception sub-type that may occur during execution.
     * @param type Specific exception sub-type to handle and recover from the exception
     * @param f    If the specific exception occurs, then that exception is passed to this Function f to recover from it.
     * @param <E>  Sub type of Exception
     * @return Current instance of Try
     */
    public <E extends Exception> Try<A> handle(Class<E> type, Function<E, A> f) {
        if (type.isAssignableFrom(Exception.class)) {
            System.out.println("[WARNING] `Try.handle` is for handling specific exception (sub) types! `Try.recover` can be used for handling generic `Exception`s.");
            return recover(e -> f.apply(Util.cast(e)));
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
     * Like `finally` in `try-catch` block, Excerpt is run post execution regardless of whether the execution is successful or not.
     * @param f Excerpt which will be run post execution
     * @return Current instance of Try.
     */
    public Try<A> finalize(Excerpt f) {
        this.finalizeF = f;
        return this;
    }

    /**
     * Executes the code (attempting retry if provided) and returns either the result of type A in right or Exception if occurred in left.
     * @return Either right with result of type A or Exception in left
     */
    public Either<Exception, A> result() {
        Either<Exception, A> res = null;
        AutoCloseableMarker m = new AutoCloseableMarker();
        try {
            res = Either.right(func.execute(m));
        } catch (Exception ex) {
            logger.consume(ex);
            res = handleException(ex);
        } finally {
            processFinally();
            List<Couple<AutoCloseable, Exception>> l = m.close();
            if (!l.isEmpty()) {
                res = Either.left(new AutoCloseException(res, l));
            }
        }
        return res;
    }

    /**
     * Executes the code (attempting retry if provided) and returns result if succeeds or throws Exception if occurred during the execution.
     * @return Resultant value if succeeds
     * @throws Exception if occurred during the execution
     */
    public A resultOrThrow() throws Exception {
        Either<Exception, A> r = result();
        if (r.isLeft()) {
            throw r.left();
        }
        return r.right();
    }

    /**
     * Executes the code silently i.e. does not return anything and any exception occurred during the execution is suppressed (or logged if set).
     */
    public void run() {
        result();
    }

    /**
     * Executes the code silently i.e. does not return anything or throws RuntimeException (wrapping actual Exception inside if occurred during the execution).
     */
    public void runOrThrowRE() throws RuntimeException {
        Either<Exception, A> r = result();
        if (r.isLeft()) {
            if (r.left() instanceof RuntimeException) {
                throw Util.<RuntimeException>cast(r.left());
            } else {
                throw new RuntimeException(r.left());
            }
        }
    }

    private Either<Exception, A> handleException(Exception ex) {
        Either<Exception, A> res;
        Function<? extends Exception, A> f = handlers.get(ex.getClass().getName());
        if (f == null) {
            res = Either.left(ex);
        } else {
            res = Either.right(f.apply(Util.cast(ex)));
        }
        return res;
    }

    private void processFinally() {
        try {
            finalizeF.execute();
        } catch (Exception e) {
            logger.consume(new FinalizingException(e));
        }
    }

    public static class TypedTry<E extends Exception, R> extends Try<R> {
        public TypedTry(Executable<AutoCloseableMarker, R> func, Consumer<Exception> logger, Excerpt finalizeF) {
            super(func, logger, finalizeF);
        }

        @Override
        public R resultOrThrow() throws E {
            Either<Exception, R> r = result();
            if (r.isLeft()) {
                throw Util.<IllegalStateException, E>tryCastOrThrow(r.left(), e -> new IllegalStateException("`E` in TypedTry is not comprehensive enough to handle " + r.left().getClass().getName(), e));
            }
            return r.right();
        }
    }

    public static class AutoCloseableMarker {
        private final List<AutoCloseable> closeableList;

        private AutoCloseableMarker() {
            closeableList = new LinkedList<>();
        }

        /**
         * Marks for closing the AutoCloseable when the execution is complete.
         * @param closeable AutoCloseable to be closed
         * @param <T>       Any class extending AutoCloseable
         * @return passed AutoCloseable object
         */
        public <T extends AutoCloseable> T markForAutoClose(T closeable) {
            this.closeableList.add(closeable);
            return closeable;
        }

        private AutoCloseableMarker reset() {
            closeableList.clear();
            return this;
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

    private static class FinalizingException extends Exception {
        private FinalizingException(Exception e) {
            super("Failed to finalize Try!", e);
        }
    }
}
