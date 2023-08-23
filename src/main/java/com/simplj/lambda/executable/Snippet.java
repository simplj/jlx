package com.simplj.lambda.executable;

@FunctionalInterface
public interface Snippet {
    void execute() throws Exception;

    default <X> Executable<X, Void> toExecutable() {
        return x -> {
            execute();
            return null;
        };
    }
    default <X> Executable<X, X> yield() {
        return x -> {
            execute();
            return x;
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
