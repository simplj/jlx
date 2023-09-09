package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.tuples.Couple;

import java.util.*;

public final class Util {

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

    static <T> T find(T[] source, Condition<T> c) {
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
    public static <R> R cast(Object o) {
        return (R) o;
    }

    @SafeVarargs
    public static <E> Set<E> asSet(E...elems) {
        Set<E> s = new HashSet<>();
        Collections.addAll(s, elems);
        return s;
    }

    @SafeVarargs
    public static <K, V> Map<K, V> asMap(Couple<K, V>...elems) {
        Map<K, V> m = new HashMap<>();
        for (Couple<K, V> e : elems) {
            m.put(e.first(), e.second());
        }
        return m;
    }
}
