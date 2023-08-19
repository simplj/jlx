package com.simplj.lambda.util;

import com.simplj.lambda.executable.Provider;
import com.simplj.lambda.executable.Snippet;
import com.simplj.lambda.function.Consumer;
import com.simplj.lambda.function.Function;

public class Try<A> {
    private final Provider<A> func;
    private Consumer<Exception> logger;
    private Function<Exception, Either<Exception, A>> recovery;

    private Try(Provider<A> f, Consumer<Exception> logger, Function<Exception, Either<Exception, A>> recovery) {
        this.func = f;
        this.logger = logger;
        this.recovery = recovery;
    }

    public static <R> Try<R> guard(Provider<R> f) {
        return new Try<>(f, Try::noOp, Either::<Exception, R>left);
    }
    public static Try<Void> guard(Snippet f) {
        return new Try<>(f.toProvider(), Try::noOp, Either::<Exception, Void>left);
    }

    public Try<A> log(Consumer<Exception> f) {
        this.logger = f;
        return this;
    }

    public Try<A> recover(Function<Exception, A> f) {
        this.recovery = f.andThen(Either::right);
        return this;
    }

    public Either<Exception, A> result() {
        Either<Exception, A> res;
        try {
            res = Either.right(func.provide());
        } catch (Exception ex) {
            logger.consume(ex);
            res = recovery.apply(ex);
        }
        return res;
    }
    public void execute() {
        result();
    }

    private static void noOp(Exception ex) {
        //No Op
    }
}
