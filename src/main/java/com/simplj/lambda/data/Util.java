package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;

import java.util.Collection;

class Util {

    static <T> T find(Collection<T> source, Condition<T> c) {
        T res = null;
        for (T t : source) {
            if (c.evaluate(t)) {
                res = t;
                break;
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    static <R> R cast(Object o) {
        return (R) o;
    }
}