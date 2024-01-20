package com.simplj.lambda.util;

import com.simplj.lambda.executable.Provider;
import com.simplj.lambda.function.Producer;

public class Timed {
    public static <T> TimedExecution<T> execute(Provider<T> provider) throws Exception {
        long s = System.currentTimeMillis();
        T t = provider.provide();
        long e = System.currentTimeMillis();
        return new TimedExecution<>((e - s), t);
    }
    public static <T> TimedExecution<T> execute(Try<T> tryBlock) throws Exception {
        long s = System.currentTimeMillis();
        T t = tryBlock.resultOrThrow();
        long e = System.currentTimeMillis();
        return new TimedExecution<>((e - s), t);
    }
    public static <T> TimedExecution<T> apply(Producer<T> provider) {
        long s = System.currentTimeMillis();
        T t = provider.produce();
        long e = System.currentTimeMillis();
        return new TimedExecution<>((e - s), t);
    }
    public static <T> TimedExecution<Either<Exception, T>> apply(Try<T> tryBlock) {
        long s = System.currentTimeMillis();
        Either<Exception, T> t = tryBlock.result();
        long e = System.currentTimeMillis();
        return new TimedExecution<>((e - s), t);
    }

    public static class TimedExecution<R> {
        private final long duration;
        private final R result;

        private TimedExecution(long duration, R result) {
            this.duration = duration;
            this.result = result;
        }

        public long duration() {
            return duration;
        }

        public R result() {
            return result;
        }

        @Override
        public String toString() {
            return result + " #" + duration + " millis.";
        }
    }
}
