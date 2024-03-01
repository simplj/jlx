package com.simplj.lambda.function;

import java.util.Objects;

/**
 * This Function interface brings additional functional programming concepts to the plate along with
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
     * In other words, it executes the function's logic on the given input
     * For example, applying a function on an input x means executing f(x)
     *
     * @param input the input argument to be processed
     * @return the `Function` result after processing the input
     */
    O apply(I input);

    /**
     * Applies the Function partially (that is why the name `ap` depicting partial `apply`-ing)
     *
     * @param i argument to be applied
     * @return Producer having this `Function`'s output
     */
    default Producer<O> ap(I i) {
        return () -> apply(i);
    }

    /**
     * Composes two different `Function`s together to create a new `Function`.
     *
     * It first applies the Function passed as argument to the input and then applies the current Function on the
     * output and returns the result. In other words, composing two functions g and f over an input x would achieve
     * the same result as using the output of f(x) as an argument of g(x)
     * <pre>(g∘f) ≡ g(f(x))</pre>
     * For example, g.compose(f) => f.andThen(g) => g(f(x))
     *
     * @param <T> The type of input argument for the composed `Function`
     * @param before The `Function` to be applied before the calling `Function`. This `Function` argument should accept
     *               a type `T` as input and product an output of type `I` making it compatible with the input of this
     *               Function, which is to be applied later.
     * @return A new `Function` after applying both Functions on the input
     *
     * @see #andThen(Function)
     */
    default <T> Function<T, O> compose(Function<T, I> before) {
        Objects.requireNonNull(before);
        return before.andThen(this);
    }

    /**
     * Applies current `Function` to the input "and then" applies the `Function` provided as argument to the output
     * to create a new Function.
     *
     * The current `Function` is applied to the input first and then the `Function` passed as argument is applied
     * to the output.
     * For example, g.andThen(f) => f(g(x))
     *
     * @param <R> The type of the output argument of the new `Function`
     * @param after The function to be applied latter, has to be NON-NULL
     * @return A new `Function` after applying both Functions on the input
     *
     * @see #apply(Object)
     */
    default <R> Function<I, R> andThen(Function<O, R> after) {
        Objects.requireNonNull(after);
        return (I i) -> after.apply(this.apply(i));
    }

    static <T> Function<T, T> id() {
        return t -> t;
    }

    static <T, R> Function<T, R> of(Function<T, R> f) {
        return f;
    }

    static <T, R> Function<T, R> returning(R r) {
        return x -> r;
    }
}
