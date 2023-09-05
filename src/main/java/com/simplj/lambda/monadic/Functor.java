package com.simplj.lambda.monadic;

import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.executable.Receiver;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.util.Try;

public class Functor<A> {
    private final Producer<Either<Exception, A>> func;
    private final Either<Exception, A> result;

    private Functor(Producer<Either<Exception, A>> func, Either<Exception, A> r) {
        this.func = func;
        this.result = r;
    }

    public static <T> Functor<T> arg(T val) {
        return new Functor<>(Producer.defer(Either.right(val)), null);
    }

    public <R> Functor<R> map(Executable<A, R> f) {
        Producer<Either<Exception, R>> next = func.andThen(e -> {
            Either<Exception, R> res;
            if (e.isRight()) {
                res = Try.execute(() -> f.execute(e.right())).result();
            } else {
                res = Either.left(e.left());
            }
            return res;
        });
        return new Functor<>(next, null);
    }

    public <R> Functor<R> flatmap(Executable<A, Functor<R>> f) {
        Producer<Either<Exception, R>> next = func.andThen(e -> {
            Either<Exception, R> res;
            if (e.isRight()) {
                res = Try.execute(() -> f.execute(e.right())).result().flatmap(Functor::result);
            } else {
                res = Either.left(e.left());
            }
            return res;
        });
        return new Functor<>(next, null);
    }

    public Functor<A> hook(Receiver<A> r) {
        return map(r.yield());
    }

    public Functor<A> recover(Function<Exception, A> recovery) {
        Producer<Either<Exception, A>> recoveryF = func.andThen(e -> e.isLeft() ? Either.right(recovery.apply(e.left())) : e);
        return new Functor<>(recoveryF, null);
    }

    public Either<Exception, A> result() {
        return applied().result;
    }

    public Functor<A> applied() {
        if (result == null) {
            Either<Exception, A> e = func.produce();
            return new Functor<>(e.isRight() ? Producer.defer(e) : func, e);
        }
        return this;
    }

    @Override
    public String toString() {
        return result == null ? "Functor[?]" : result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (result == null) {
            throw new IllegalStateException("Functor must be `applied` before calling `equals` method!");
        }
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Functor<?> that = (Functor<?>) o;
        return this.result.equals(that.result);
    }

    @Override
    public int hashCode() {
        if (result == null) {
            throw new IllegalStateException("Functor must be `applied` before calling `hashcode` method!");
        }
        return result.hashCode();
    }
}
