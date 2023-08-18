package com.simplj.lambda.executable;

import com.simplj.lambda.function.Function;
import com.simplj.lambda.util.Either;

import java.util.Objects;

@FunctionalInterface
public interface Executable<I, O> {
    O execute(I input) throws Exception;

    default Function<I, Either<Exception, O>> pure() {
        return (I i) -> {
            Either<Exception, O> res;
            try {
                res = Either.right(execute(i));
            } catch (Exception ex) {
                res = Either.left(ex);
            }
            return res;
        };
    }

    default <T> Executable<T, O> compose(Executable<T, I> before) {
        Objects.requireNonNull(before);
        return before.andThen(this);
    }
    default <R> Executable<I, R> andThen(Executable<O, R> after) {
        Objects.requireNonNull(after);
        return (I i) -> after.execute(this.execute(i));
    }

    static <T> Executable<T, T> id() {
        return t -> t;
    }
}
