package com.simplj.lambda.executable;

@FunctionalInterface
public interface Snippet {
    void execute() throws Exception;

    default <X, Y> Executable<X, Y> toExecutable() {
        return x -> {
            execute();
            return null;
        };
    }

    default <X> Receiver<X> toReceiver() {
        return x -> execute();
    }

    default <X> Provider<X> toProvider() {
        return () -> {
            execute();
            return null;
        };
    }
}
