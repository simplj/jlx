package com.simplj.lambda.monadic;

import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.executable.Receiver;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.monadic.exception.FilteredOutException;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.util.Try;

/**
 * Applying functions lazily on a Producer (or an initial value)
 * @param <A> Type of the resultant value
 */
public class Thunk<A> {
    private final Producer<Either<Exception, A>> func;
    private final Either<Exception, A> result;

    private Thunk(Producer<Either<Exception, A>> func, Either<Exception, A> r) {
        this.func = func;
        this.result = r;
    }

    public static <T> Thunk<T> init(T val) {
        return of(Producer.defer(val));
    }
    public static <T> Thunk<T> of(Producer<T> f) {
        return new Thunk<>(f.andThen(Either::right), null);
    }

    public <R> Thunk<R> map(Executable<A, R> f) {
        Producer<Either<Exception, R>> next = func.andThen(e -> {
            Either<Exception, R> res;
            if (e.isRight()) {
                res = Try.execute(() -> f.execute(e.right())).result();
            } else {
                res = Either.left(e.left());
            }
            return res;
        });
        return new Thunk<>(next, null);
    }

    public <R> Thunk<R> flatmap(Executable<A, Thunk<R>> f) {
        Producer<Either<Exception, R>> next = func.andThen(e -> {
            Either<Exception, R> res;
            if (e.isRight()) {
                res = Try.execute(() -> f.execute(e.right())).result().flatmap(Thunk::result);
            } else {
                res = Either.left(e.left());
            }
            return res;
        });
        return new Thunk<>(next, null);
    }

    public Thunk<A> filter(Condition<A> f) {
        Producer<Either<Exception, A>> next = func.andThen(e -> {
            Either<Exception, A> res;
            if (e.isRight() && !f.evaluate(e.right())) {
                res = Either.left(new FilteredOutException(e.right()));
            } else {
                res = e;
            }
            return res;
        });
        return new Thunk<>(next, null);
    }

    public Thunk<A> record(Receiver<A> r) {
        return map(r.yield());
    }

    public Thunk<A> recover(Function<Exception, A> recovery) {
        Producer<Either<Exception, A>> recoveryF = func.andThen(e -> e.isLeft() ? Either.right(recovery.apply(e.left())) : e);
        return new Thunk<>(recoveryF, null);
    }

    public Thunk<A> recoverWhen(Condition<Exception> condition, Function<Exception, A> recovery) {
        Producer<Either<Exception, A>> recoveryF = func.andThen(e -> e.isLeft() && condition.evaluate(e.left()) ? Either.right(recovery.apply(e.left())) : e);
        return new Thunk<>(recoveryF, null);
    }
    public Thunk<A> recoverOn(Class<? extends Exception> clazz, Function<Exception, A> recovery) {
        Producer<Either<Exception, A>> recoveryF = func.andThen(e -> e.isLeft() && clazz.isAssignableFrom(e.left().getClass()) ? Either.right(recovery.apply(e.left())) : e);
        return new Thunk<>(recoveryF, null);
    }

    public Either<Exception, A> result() {
        return applied().result;
    }

    /**
     * Executes the thunk (attempting retry if provided) and returns result if succeeds or throws Exception if occurred during the execution.
     * @return Resultant value if succeeds
     * @throws Exception if occurred during the execution
     */
    public A resultOrThrow() throws Exception {
        Either<Exception, A> res = result();
        if (res.isLeft()) {
            throw res.left();
        }
        return res.right();
    }

    public Thunk<A> applied() {
        if (result == null) {
            Either<Exception, A> e = func.produce();
            return new Thunk<>(e.isRight() ? Producer.defer(e) : func, e);
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

        Thunk<?> that = (Thunk<?>) o;
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
