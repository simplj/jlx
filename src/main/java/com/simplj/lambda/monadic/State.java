package com.simplj.lambda.monadic;

import com.simplj.lambda.util.Either;
import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.executable.Receiver;
import com.simplj.lambda.function.Function;

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
            try {
                res = f.execute(value.right());
            } catch (Exception ex) {
                res = new State<>(Either.left(ex));
            }
        } else {
            res = new State<>(Either.left(value.left()));
        }
        return res;
    }

    public void execute(Receiver<A> r) throws Exception {
        if (value.isRight()) {
            r.receive(value.right());
        }
    }

    public State<A> recover(Function<Exception, A> recovery) {
        return value.isLeft() ? recovery.andThen(State::arg).apply(value.left()) : this;
    }

    public Either<Exception, A> result() {
        return value;
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
