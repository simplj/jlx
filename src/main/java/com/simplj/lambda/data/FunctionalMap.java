package com.simplj.lambda.data;

import com.simplj.lambda.function.*;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

import java.util.*;

abstract class FunctionalMap<K, V, M extends FunctionalMap<K, V, M>> implements Iterable<Map.Entry<K, V>> {
    final Producer<Map<?, ?>> constructor;

    FunctionalMap(Producer<Map<?, ?>> constructor) {
        this.constructor = constructor;
    }

    abstract M instantiate(Producer<Map<?, ?>> constructor);

    public abstract Map<K, V> map();

    /**
     * Applies the <code>Condition</code> `c` to all the elements in the map excludes elements from the map which does not satisfy `c`. Hence the resultant map of this api only contains the elements which satisfies the condition `c`. <br>
     * Function application is <i>lazy</i> which means calling this api has no effect until a <i>eager</i> api is called.
     * @param c condition to evaluate against each element
     * @return map containing elements which satisfies the condition `c`
     */
    public abstract M filter(BiFunction<K, V, Boolean> c);
    /**
     * Applies the <code>Condition</code> `c` to all the elements in the map excludes elements from the map which satisfies `c`. Hence the resultant map of this api only contains the elements which does not satisfy the condition `c`. <br>
     * Function application is <i>lazy</i> which means calling this api has no effect until a <i>eager</i> api is called.
     * @param c condition to evaluate against each element
     * @return map containing elements which does not satisfy the condition `c`
     */
    public M filterOut(BiFunction<K, V, Boolean> c) {
        return filter((a, b) -> !c.apply(a, b));
    }
    public M filterByKey(Condition<K> c) {
        return filter((a, x) -> c.evaluate(a));
    }
    public M filterOutByKey(Condition<K> c) {
        return filterByKey(c.negate());
    }
    public M filterByValue(Condition<V> c) {
        return filter((x, b) -> c.evaluate(b));
    }
    public M filterOutByValue(Condition<V> c) {
        return filterByValue(c.negate());
    }

    public abstract boolean isApplied();
    /**
     * Function application is <i>eager</i> i.e. it applies all the lazy functions (if any) to map elements
     * @return <code>current instance</code> with all the lazy functions (if any) applied
     */
    public abstract M applied();

    /**
     * Applies the <code>Condition</code> `c` to all the elements in the {@link #applied() applied} map and returns a <code>Couple</code> of <code>ImmutableMap</code>s with satisfying elements in {@link Couple#first() first} and <i>not</i> satisfying elements in {@link Couple#second() second}
     * @param c condition based on which the elements will be segregated
     * @return <code>Couple</code> of <code>ImmutableMap</code>s with satisfying elements in {@link Couple#first() first} and <i>not</i> satisfying elements in {@link Couple#second() second}
     */
    public Couple<M, M> split(BiFunction<K, V, Boolean> c) {
        M match = instantiate(constructor);
        M rest = instantiate(constructor);
        Map<K, V> map = map();
        for (Map.Entry<K, V> t : map.entrySet()) {
            if (c.apply(t.getKey(), t.getValue())) {
                match.include(t.getKey(), t.getValue());
            } else {
                rest.include(t.getKey(), t.getValue());
            }
        }
        return Tuple.of(match, rest);
    }
    public int size() {
        return map().size();
    }
    public boolean isEmpty() {
        return map().isEmpty();
    }
    public boolean containsKey(Object key) {
        return map().containsKey(key);
    }
    public boolean containsValue(Object value) {
        return map().containsValue(value);
    }
    public boolean containsKeys(Set<K> keys) {
        return keySet().containsAll(keys);
    }
    public boolean containsValues(Set<V> values) {
        return values().containsAll(values);
    }
    public V get(Object key) {
        return map().get(key);
    }
    public V getOrDefault(Object key, V defaultValue) {
        return map().getOrDefault(key, defaultValue);
    }
    public abstract M include(K key, V val);
    public abstract M includeIfAbsent(K key, V val);
    public abstract M include(Map<K, V> that);
    public abstract M delete(K key);
    public abstract M delete(K key, V value);
    public abstract M replacing(K key, V value);
    public abstract M replacing(K key, V oldValue, V newValue);
    public abstract M empty();
    public Set<K> keySet() {
        return map().keySet();
    }
    public Collection<V> values() {
        return map().values();
    }
    public Set<Map.Entry<K, V>> entrySet() {
        return map().entrySet();
    }

    public void forEach(java.util.function.BiConsumer<? super K, ? super V> action) {
        map().forEach(action);
    }

    public abstract M replacingAll(java.util.function.BiFunction<? super K, ? super V, ? extends V> function);
    public V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
        return map().computeIfAbsent(key, mappingFunction);
    }
    public V computeIfPresent(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return map().computeIfPresent(key, remappingFunction);
    }
    public V compute(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return map().compute(key, remappingFunction);
    }
    public V merge(K key, V value, java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return map().merge(key, value, remappingFunction);
    }

    public Couple<K, V> find(BiFunction<K, V, Boolean> c) {
        Couple<K, V> res = null;
        Map<K, V> map = map();
        for (Map.Entry<K, V> e : map.entrySet()) {
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
        for (Map.Entry<K, V> e : map.entrySet()) {
            origin = accumulator.apply(origin, e.getKey(), e.getValue());
        }
        return origin;
    }

    public Couple<K, V> reduce(TriFunction<Couple<K, V>, K, V, Couple<K, V>> accumulator) {
        Couple<K, V> res = null;
        Map<K, V> map = map();
        boolean flag = false;
        if (!map.isEmpty()) {
            for (Map.Entry<K, V> t : map.entrySet()) {
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

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return map().entrySet().iterator();
    }

    @Override
    public Spliterator<Map.Entry<K, V>> spliterator() {
        return map().entrySet().spliterator();
    }

    @Override
    public String toString() {
        return isApplied() ? map().toString() : "(?=?)";
    }

    @Override
    public int hashCode() {
        return map().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FunctionalMap) {
            FunctionalMap<?, ?, ?> fMap = Util.cast(obj);
            obj = fMap.map();
        }
        return map().equals(obj);
    }

    public M copy() {
        M r = instantiate(constructor);
        r.include(map());
        return r;
    }
}
