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

    /**
     * """I'm tired of being what you want me to be
     * Feeling so faithless, lost under the surface
     * Don't know what you're expecting of me
     * Put under the pressure of walking in your shoes"""
     * - Linkin Park
     * @return Snippet instance that is numb
     */
    static Snippet numb() {
        return () -> {};
    }
}
