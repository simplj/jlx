package com.simplj.lambda.util;

import com.simplj.lambda.executable.Provider;
import com.simplj.lambda.function.Function;

public class Try<A> {
    private final Provider<A> func;
    private final Function<Exception, Either<Exception, A>> recovery;

    private Try(Provider<A> f, Function<Exception, Either<Exception, A>> recovery) {
        this.func = f;
        this.recovery = recovery;
    }

    public static <R> Try<R> execute(Provider<R> f) {
        return new Try<>(f, Either::<Exception, R>left);
    }

    public Try<A> handle(Function<Exception, A> f) {
        return new Try<>(func, f.andThen(Either::right));
    }

    public Either<Exception, A> result() {
        Either<Exception, A> res;
        try {
            res = Either.right(func.provide());
        } catch (Exception ex) {
            res = recovery.apply(ex);
        }
        return res;
    }
}
