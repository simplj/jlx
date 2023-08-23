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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Try<A> {
    private final Executable<AutoCloseableMarker, A> func;
    private Consumer<Exception> logger;
    private Function<Exception, Either<Exception, A>> recovery;
    private final Map<String, Function<? extends Exception, A>> handlers;

    private Try(Executable<AutoCloseableMarker, A> f, Consumer<Exception> logger, Function<Exception, Either<Exception, A>> recovery) {
        this.func = f;
        this.logger = logger;
        this.recovery = recovery;
        this.handlers = new HashMap<>();
    }

    public static <R> Try<R> execute(Provider<R> f) {
        return execute(f.toExecutable());
    }
    public static Try<Void> execute(Snippet f) {
        return execute(f.toExecutable());
    }
    public static Try<Void> execute(Receiver<AutoCloseableMarker> f) {
        return execute(f.toExecutable());
    }
    public static <R> Try<R> execute(Executable<AutoCloseableMarker, R> f) {
        return new Try<>(f, Try::noOp, Either::<Exception, R>left);
    }

    public Try<A> log(Consumer<Exception> f) {
        this.logger = f;
        return this;
    }

    public Try<A> recover(Function<Exception, A> f) {
        this.recovery = f.andThen(Either::right);
        return this;
    }

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

    public Either<Exception, A> result() {
        Either<Exception, A> res = null;
        AutoCloseableMarker m = new AutoCloseableMarker();
        try {
            res = Either.right(func.execute(m));
        } catch (Exception ex) {
            logger.consume(ex);
            res = handleException(ex);
        } finally {
            if (!m.closeableList.isEmpty()) {
                List<Couple<AutoCloseable, Exception>> l = new LinkedList<>();
                for (AutoCloseable ac : m.closeableList) {
                    try {
                        ac.close();
                    } catch (Exception ex) {
                        l.add(Tuple.of(ac, ex));
                    }
                }
                if (!l.isEmpty()) {
                    res = Either.left(new AutoCloseException(res, l));
                }
            }
        }
        return res;
    }

    public void run() {
        result();
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
    }

    public static class AutoCloseException extends Exception {
        private final Object result;
        private final List<Couple<AutoCloseable, Exception>> closeableList;

        public AutoCloseException(Object result, List<Couple<AutoCloseable, Exception>> closeableList) {
            this.result = result;
            this.closeableList = closeableList;
        }

        public Object result() {
            return result;
        }

        public List<Couple<AutoCloseable, Exception>> failedCloseableList() {
            return closeableList;
        }
    }
}
