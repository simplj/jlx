package com.simplj.lambda.function;

@FunctionalInterface
public interface Snippet {
    void apply();

    default <X> Function<X, Void> toFunction() {
        return x -> {
            apply();
            return null;
        };
    }
    default <X> Function<X, X> yield() {
        return x -> {
            apply();
            return x;
        };
    }

    default <X> Consumer<X> toConsumer() {
        return x -> apply();
    }

    default <X> Producer<X> toProducer() {
        return () -> {
            apply();
            return null;
        };
    }
}
