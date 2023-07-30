package com.simplj.lambda.data;

import com.simplj.lambda.function.*;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

abstract class FunctionalMap<K, V, M extends FunctionalMap<K, V, M>> implements Map<K, V> {

    public abstract M filter(BiFunction<K, V, Boolean> c);
    public abstract M filterOut(BiFunction<K, V, Boolean> c);
    public abstract M filterByKey(Condition<K> c);
    public abstract M filterOutByKey(Condition<K> c);
    public abstract M filterByValue(Condition<V> c);
    public abstract M filterOutByValue(Condition<V> c);

    public abstract Map<K, V> map();
    public abstract boolean isApplied();
    public abstract M applied();

    public Couple<K, V> find(BiFunction<K, V, Boolean> c) {
        Couple<K, V> res = null;
        Map<K, V> map = map();
        for (Entry<K, V> e : map.entrySet()) {
            if (c.apply(e.getKey(), e.getValue())) {
                res = Tuple.of(e.getKey(), e.getValue());
            }
        }
        return res;
    }
    public Couple<K, V> findByKey(Condition<K> c) {
        return find((a, x) -> c.evaluate(a));
    }
    public Couple<K, V> findByValue(Condition<V> c) {
        return find((x, b) -> c.evaluate(b));
    }

    public boolean none(BiFunction<K, V, Boolean> c) {
        return null == find(c);
    }
    public boolean any(BiFunction<K, V, Boolean> c) {
        return !none(c);
    }
    public boolean all(BiFunction<K, V, Boolean> c) {
        return none((a, b) -> !c.apply(a, b));
    }
    public boolean noKeys(Condition<K> c) {
        return null == findByKey(c);
    }
    public boolean anyKey(Condition<K> c) {
        return !noKeys(c);
    }
    public boolean allKeys(Condition<K> c) {
        return noKeys(c.negate());
    }
    public boolean noValues(Condition<V> c) {
        return null == findByValue(c);
    }
    public boolean anyValue(Condition<V> c) {
        return !noValues(c);
    }
    public boolean allValues(Condition<V> c) {
        return noValues(c.negate());
    }

    public <R> R fold(R origin, TriFunction<R, K, V, R> accumulator) {
        Map<K, V> map = map();
        for (Entry<K, V> e : map.entrySet()) {
            origin = accumulator.apply(origin, e.getKey(), e.getValue());
        }
        return origin;
    }

    public Couple<K, V> reduce(TriFunction<Couple<K, V>, K, V, Couple<K, V>> accumulator) {
        Couple<K, V> res = null;
        Map<K, V> map = map();
        boolean flag = false;
        if (!map.isEmpty()) {
            for (Entry<K, V> t : map.entrySet()) {
                if (flag) {
                    res = accumulator.apply(res, t.getKey(), t.getValue());
                } else {
                    res = Tuple.of(t.getKey(), t.getValue());
                    flag = true;
                }
            }
        }
        return res;
    }
}
