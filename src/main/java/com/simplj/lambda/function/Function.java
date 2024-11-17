package com.simplj.lambda.function;

import java.util.Objects;

/**
 * This {@code Function} interface brings additional functional programming concepts to the plate along with
 * existing java {@link java.util.function.Function} functionalities
 * It serves as a building block for further functional programming concepts
 *
 * @param <I>   denotes datatype of the input argument to the function
 * @param <O>   denotes datatype of the output returned by the function
 */

@FunctionalInterface
public interface Function<I, O> extends java.util.function.Function<I, O> {
    /**
     * Applies the Function to the given input and returns an output.
     *
     * <pre>In other words, it executes the function's logic on the given input
     * For example, applying a function on an input x means executing f(x)</pre>
     *
     * @param input the input argument to be processed
     * @return the `Function` result after processing the input
     */
    O apply(I input);

    /**
     * Applies the {@code Function} partially (that is why the name {@code ap} depicting partial {@code apply}-ing)
     *
     * @param i argument to be applied
     * @return Producer having this {@code Function}'s output
     */
    default Producer<O> ap(I i) {
        return () -> apply(i);
    }

    /**
     * Composes two different {@code Function}s together to create a new {@code Function}.
     *
     * <pre>It first applies the {@code Function} passed as argument to the input and then applies the current {@code Function} on the
     * output and returns the result. In other words, composing two {@code Function}s g and f over an input {@code x} would achieve
     * the same result as using the output of {@code f(x)} as an argument of {@code g(x)}</pre>
     * <pre>(g∘f) ≡ g(f(x))</pre>
     * <pre>For example, {@code g.compose(f) => f.andThen(g) => g(f(x))}</pre>
     *
     * @param <T> The type of input argument for the composed {@code Function}
     * @param before The {@code Function} to be applied before the calling {@code Function}. This {@code Function} argument should accept
     *               a type {@code T} as input and product an output of type {@code I} making it compatible with the input of this
     *               {@code Function}, which is to be applied later.
     * @return A new {@code Function} after applying both {@code Function}s on the input
     *
     * @see #andThen(Function)
     */
    default <T> Function<T, O> compose(Function<T, I> before) {
        Objects.requireNonNull(before);
        return before.andThen(this);
    }

    /**
     * Applies current {@code Function}to the input "and then" applies the {@code Function} provided as argument to the output
     * to create a new {@code Function}.
     *
     * <pre>The current {@code Function} is applied to the input first and then the {@code Function} passed as argument is applied
     * to the output.
     * For example, {@code g.andThen(f) => f(g(x))}</pre>
     *
     * @param <R> The type of the output argument of the new {@code Function}
     * @param after The {@code Function} to be applied latter, has to be <b>NON-NULL</b>
     * @return A new {@code Function} after applying both {@code Function}s on the input
     *
     * @see #apply(Object)
     */
    default <R> Function<I, R> andThen(Function<O, R> after) {
        Objects.requireNonNull(after);
        return (I i) -> after.apply(this.apply(i));
    }

    /**
     * Returns is an identity function
     *
     * <pre>It returns a {@code Function} which returns whatever argument is passed to it</pre>
     *
     * @return The argument passed to the {@code Function}
     * @param <T> The input and output type of the {@code Function}
     */
    static <T> Function<T, T> id() {
        return t -> t;
    }

    /**
     * Returns a {@code Function} which is passed as argument to this {@code Function}
     *
     * <pre>It can be used to transform a lambda expression into a {@code Function}. Useful for writing one-liners</pre>
     *
     * @param f A {@code Function} object which gets returned (lambda expression)
     * @return A {@code Function} object which is passed as argument
     * @param <T> Input type of the resulting {@code Function}
     * @param <R> Output type of the resulting {@code Function}
     */
    static <T, R> Function<T, R> of(Function<T, R> f) {
        return f;
    }

    /**
     * Returns a {@code Function} which takes an input and produces an output of different type
     *
     * <pre>It is similar to identity function, but instead of returning whatever is passed as argument, it transforms it
     * to match the output type.</pre>
     *
     * @param r Output of type {@code R}
     * @return A {@code Function} which takes {@code T} type value as input and returns a value of type {@code R} as output
     * @param <T> Input type of the returned {@code Function}
     * @param <R> Output type of the returned {@code Function}
     */
    static <T, R> Function<T, R> returning(R r) {
        return x -> r;
    }
}
