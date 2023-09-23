package com.simplj.lambda.function;

import com.simplj.lambda.executable.Executable;

import java.util.Objects;

@FunctionalInterface
public interface Consumer<A> {
    void consume(A input);

    /**
     * Applies the Consumer partially (that is why the name `cons` depicting partial `consume`-ing)
     * @param a argument to be applied
     * @return Snippet with the Consumer functionality wrapped
     */
    default Snippet cons(A a) {
        return () -> consume(a);
    }

    default <T> Consumer<T> compose(Function<T, A> before) {
        Objects.requireNonNull(before);
        return t -> consume(before.apply(t));
    }

    default Function<A, A> yield() {
        return a -> {
            consume(a);
            return a;
        };
    }
    default Function<A, Void> toFunction() {
        return a -> {
            consume(a);
            return null;
        };
    }

    static <T> Consumer<T> of(Consumer<T> f) {
        return f;
    }

    static <T> Consumer<T> noOp() {
        return x -> {};
    }
}
