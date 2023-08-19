package com.simplj.lambda.executable;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.QuadFunction;
import com.simplj.lambda.function.TriFunction;
import com.simplj.lambda.util.Either;

import java.util.Objects;

@FunctionalInterface
public interface QuadExecutable<A, B, C, D, R> {

    R execute(A a, B b, C c, D d) throws Exception;

    default QuadFunction<A, B, C, D, Either<Exception, R>> pure() {
        return (A a, B b, C c, D d) -> {
            Either<Exception, R> res;
            try {
                res = Either.right(execute(a, b, c, d));
            } catch (Exception ex) {
                res = Either.left(ex);
            }
            return res;
        };
    }

    default <T> QuadExecutable<T, B, C, D, R> composeFirst(Function<T, A> after) {
        Objects.requireNonNull(after);
        return (t, b, c, d) -> execute(after.apply(t), b, c, d);
    }
    default <T> QuadExecutable<A, T, C, D, R> composeSecond(Function<T, B> after) {
        Objects.requireNonNull(after);
        return (a, t, c, d) -> execute(a, after.apply(t), c, d);
    }
    default <T> QuadExecutable<A, B, T, D, R> composeThird(Function<T, C> after) {
        Objects.requireNonNull(after);
        return (a, b, t, d) -> execute(a, b, after.apply(t), d);
    }
    default <T> QuadExecutable<A, B, C, T, R> composeFourth(Function<T, D> after) {
        Objects.requireNonNull(after);
        return (a, b, c, t) -> execute(a, b, c, after.apply(t));
    }

    default <T> QuadExecutable<A, B, C, D, T> andThen(Function<R, T> after) {
        Objects.requireNonNull(after);
        return (a, b, c, d) -> after.apply(execute(a, b, c, d));
    }

    default TriExecutable<B, C, D, R> partial(A a) {
        return (b, c, d) -> execute(a, b, c, d);
    }
    default BiExecutable<C, D, R> partial(A a, B b) {
        return (c, d) -> execute(a, b, c, d);
    }
    default Executable<D, R> partial(A a, B b, C c) {
        return d -> execute(a, b, c, d);
    }

    default Executable<A, Executable<B, Executable<C, Executable<D, R>>>> curried() {
        return a -> b -> c -> d -> execute(a, b, c, d);
    }

    static <P, Q, R, S> QuadExecutable<P, Q, R, S, P> first() {
        return (a, b, c, d) -> a;
    }
    static <P, Q, R, S> QuadExecutable<P, Q, R, S, Q> second() {
        return (a, b, c, d) -> b;
    }
    static <P, Q, R, S> QuadExecutable<P, Q, R, S, R> third() {
        return (a, b, c, d) -> c;
    }
    static <P, Q, R, S> QuadExecutable<P, Q, R, S, S> fourth() {
        return (a, b, c, d) -> d;
    }
}
