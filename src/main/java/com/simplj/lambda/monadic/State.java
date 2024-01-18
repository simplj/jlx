package com.simplj.lambda.monadic;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.monadic.exception.FilteredOutException;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.executable.Receiver;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.util.Try;

public class State<A> {
    private final Either<Exception, A> value;

    private State(Either<Exception, A> v) {
        this.value = v;
    }

    public static <T> State<T> arg(T val) {
        return new State<>(Either.right(val));
    }

    public boolean isSuccessful() {
        return value.isRight();
    }

    public A get() {
        return value.right();
    }

    public Exception error() {
        return value.left();
    }

    public <R> State<R> map(Executable<A, R> f) {
        return flatmap(f.andThen(State::arg));
    }

    public <R> State<R> flatmap(Executable<A, State<R>> f) {
        State<R> res;
        if (value.isRight()) {
            res = Try.execute(() -> f.execute(value.right())).recover(e -> new State<>(Either.left(e))).result().right();
        } else {
            res = new State<>(Either.left(value.left()));
        }
        return res;
    }

    public State<A> filter(Condition<A> f) {
        State<A> res;
        if (value.isRight() && !f.evaluate(value.right())) {
            res = new State<>(Either.left(new FilteredOutException(value.right())));
        } else {
            res = this;
        }
        return res;
    }

    public void record(Receiver<A> r) throws Exception {
        if (value.isRight()) {
            r.receive(value.right());
        }
    }

    public State<A> recover(Function<Exception, A> recovery) {
        return value.isLeft() ? recovery.andThen(State::arg).apply(value.left()) : this;
    }

    public State<A> recoverWhen(Condition<Exception> condition, Function<Exception, A> recovery) {
        return value.isLeft() && condition.evaluate(value.left()) ? recovery.andThen(State::arg).apply(value.left()) : this;
    }
    public State<A> recoverOn(Class<? extends Exception> clazz, Function<Exception, A> recovery) {
        return value.isLeft() && clazz.isAssignableFrom(value.left().getClass()) ? recovery.andThen(State::arg).apply(value.left()) : this;
    }

    public Either<Exception, A> result() {
        return value;
    }

    /**
     * Returns result if succeeds or throws Exception if occurred during execution.
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

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State<?> that = (State<?>) o;
        return this.value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
