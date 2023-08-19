package com.simplj.lambda.executable;

import com.simplj.lambda.function.Producer;
import com.simplj.lambda.util.Either;

import java.util.Objects;

@FunctionalInterface
public interface Provider<R> {
    R provide() throws Exception;

    default Producer<Either<Exception, R>> pure() {
        return () -> {
            Either<Exception, R> res;
            try {
                res = Either.right(provide());
            } catch (Exception ex) {
                res = Either.left(ex);
            }
            return res;
        };
    }

    default <A> Provider<A> andThen(Executable<R, A> after) {
        Objects.requireNonNull(after);
        return () -> after.execute(provide());
    }

    default <X> Executable<X, R> toExecutable() {
        return x -> provide();
    }

    static <T> Provider<T> defer(T val) {
        return () -> val;
    }
}
