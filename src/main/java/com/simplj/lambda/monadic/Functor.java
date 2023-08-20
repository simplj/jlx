package com.simplj.lambda.monadic;

import com.simplj.lambda.executable.Provider;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.executable.Receiver;
import com.simplj.lambda.function.Function;

public class Functor<A> {
    private final Provider<Either<Exception, A>> func;
    private final Either<Exception, A> result;

    private Functor(Provider<Either<Exception, A>> func, Either<Exception, A> r) {
        this.func = func;
        this.result = r;
    }

    public static <T> Functor<T> arg(T val) {
        return new Functor<>(Provider.defer(Either.right(val)), null);
    }

    public <R> Functor<R> map(Executable<A, R> f) {
        Provider<Either<Exception, R>> next = func.andThen(e -> {
            Either<Exception, R> res;
            if (e.isRight()) {
                res = Either.right(f.execute(e.right()));
            } else {
                res = Either.left(e.left());
            }
            return res;
        });
        return new Functor<>(next, null);
    }

    public <R> Functor<R> flatmap(Executable<A, Functor<R>> f) {
        Provider<Either<Exception, R>> next = func.andThen(e -> {
            Either<Exception, R> res;
            if (e.isRight()) {
                res = f.execute(e.right()).result();
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
        return result.isLeft() ? recovery.andThen(Functor::arg).apply(result.left()) : this;
    }

    public Either<Exception, A> result() {
        return applied().result;
    }

    public Functor<A> applied() {
        if (result == null) {
            Either<Exception, A> e;
            try {
                e = func.provide();
            } catch (Exception ex) {
                e = Either.left(ex);
            }
            return new Functor<>(e.isRight() ? Provider.defer(e) : func, e);
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
