package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
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

    /**
     * casts to specific type if the following holds true:
     * <br><code>o instanceof R</code>
     * <br>This is a syntactic sugar for <code>(R) o</code> that avoids using <code>@SuppressWarnings("unchecked")</code>
     * <br><br>It is the developer's responsibility to use this api wisely.
     * <br>For example, the following will work:
     * <br><code>E e = cast(obj)</code> given that `obj` is an instance of `E`
     * <br>but, the following will produce a `java.lang.ClassCastException`:
     * <br><code>String s = cast(obj)</code> when `obj` is NOT an instance of `String`
     * @param o   value to cast
     * @param <R> Resultant Type
     * @return the type cast-ed value
     */
    @SuppressWarnings("unchecked")
    public static <R> R cast(Object o) {
        return (R) o;
    }
    @SuppressWarnings("unchecked")
    public static <E extends Exception, R> R tryCastOrThrow(Object o, Function<Exception, E> fX) throws E {
        try {
            return (R) o;
        } catch (Exception ex) {
            throw fX.apply(ex);
        }
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
