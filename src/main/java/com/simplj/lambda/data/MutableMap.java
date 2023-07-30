package com.simplj.lambda.data;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class MutableMap<K, V> extends FunctionalMap<K, V, MutableMap<K, V>> {
    private Map<?, ?> src;
    private Map<K, V> map;
    private final Producer<Map<?, ?>> constructor;
    private BiFunction<Object, Object, ? extends Map<K, V>> func;
    private final AtomicBoolean applied;

    private MutableMap(Map<K, V> map, Producer<Map<?, ?>> cons) {
        this(Collections.emptyMap(), map, cons, null, new AtomicBoolean(true));
    }
    private MutableMap(Map<?, ?> src, Producer<Map<?, ?>> cons, BiFunction<Object, Object, ? extends Map<K, V>> func) {
        this(src, Util.cast(cons.produce()), cons, func, new AtomicBoolean(false));
    }
    public MutableMap(Map<?, ?> src, Map<K, V> map, Producer<Map<?, ?>> cons, BiFunction<Object, Object, ? extends Map<K, V>> func, AtomicBoolean flag) {
        this.src = src;
        this.map = map;
        this.constructor = cons;
        this.func = func;
        this.applied = flag;
    }

    public static <A, B> MutableMap<A, B> of(Map<A, B> set, Producer<Map<?, ?>> constructor) {
        return new MutableMap<>(set, constructor);
    }

    /* ------------------- START: Lazy methods ------------------- */
    /**
     * Applies the function `f` of type &lt;i&gt;(T -&gt; R)&lt;/i&gt; to all the elements in the map and returns the resultant map. Function application is &lt;b&gt;lazy&lt;/b&gt;&lt;br /&gt;
     * Detailed Description: &lt;b&gt;map&lt;/b&gt;-ing `f` on map &lt;code&gt;[1, 2, 3]&lt;/code&gt; will return a map &lt;code&gt;[f(1), f(2), f(3)]&lt;/code&gt;.
     * As it can be seen that the function `f` is not applied immediately which makes &lt;code&gt;map&lt;/code&gt; a &lt;b&gt;lazy&lt;/b&gt; implementation.
     * The function `f` is not applied to the elements until a &lt;b&gt;eager&lt;/b&gt; api is called. Therefore, calling &lt;code&gt;map&lt;/code&gt; has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param f function to apply to each element.
     * @return resultant map after applying `f` to all the map elements
     */
    public <A, B> MutableMap<A, B> map(BiFunction<K, V, Couple<A, B>> f) {
        return flatmap(f.andThen(c -> Collections.singletonMap(c.first(), c.second())));
    }
    public <R> MutableMap<R, V> mapK(Function<K, R> f) {
        return flatmapK(f.andThen(Collections::singleton));
    }
    public <R> MutableMap<K, R> mapV(Function<V, R> f) {
        return flatmap((a, b) -> Collections.singletonMap(a, f.apply(b)));
    }

    /**
     * Applies the function `f` of type &lt;i&gt;(T -&gt; map&lt;R&gt;)&lt;/i&gt; to all the elements in the map and returns the resultant flattened map. Function application is &lt;b&gt;lazy&lt;/b&gt;&lt;br /&gt;
     * Detailed Description: &lt;b&gt;flatmap&lt;/b&gt;-ing `f` on map &lt;code&gt;[1, 2, 3]&lt;/code&gt; will return a map &lt;code&gt;[f(1), f(2), f(3)]&lt;/code&gt;.
     * As it can be seen that the function `f` is not applied immediately which makes &lt;code&gt;flatmap&lt;/code&gt; a &lt;b&gt;lazy&lt;/b&gt; implementation.
     * The function `f` is not applied to the elements until a &lt;b&gt;eager&lt;/b&gt; api is called. Therefore, calling &lt;code&gt;flatmap&lt;/code&gt; has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param f function to apply to each element.
     * @return resultant map after applying `f` to all the map elements
     */
    public <A, B> MutableMap<A, B> flatmap(BiFunction<K, V, ? extends Map<A, B>> f) {
        MutableMap<A, B> res;
        if (func == null) {
            res = new MutableMap<>(map, constructor, f.composeFirst(Util::cast).composeSecond(Util::cast));
        } else {
            BiFunction<Object, Object, Map<A, B>> that = func.andThen(m -> {
                Map<A, B> r = Util.cast(constructor.produce());
                for (Entry<K, V> e : m.entrySet()) {
                    r.putAll(f.apply(e.getKey(), e.getValue()));
                }
                return r;
            });
            res = new MutableMap<>(src, constructor, that);
        }
        return res;
    }
    public <R> MutableMap<R, V> flatmapK(Function<K, ? extends Set<R>> f) {
        BiFunction<K, V, Map<R, V>> that = (a, b) -> {
            Map<R, V> r = Util.cast(constructor.produce());
            Set<R> keys = f.compose(Util::cast).apply(a);
            for (R k : keys) {
                r.put(k, b);
            }
            return r;
        };
        return flatmap(that);
    }

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the map excludes elements from the map which does not satisfy `c`. Hence the resultant map of this api only contains the elements which satisfies the condition `c`. &lt;br /&gt;
     * Function application is &lt;b&gt;lazy&lt;/b&gt; which means calling this api has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param c condition to evaluate against each element
     * @return map containing elements which satisfies the condition `c`
     */
    public MutableMap<K, V> filter(BiFunction<K, V, Boolean> c) {
        if (func == null) {
            func = (a, b) -> {
                K k = Util.cast(a);
                V v = Util.cast(b);
                return c.apply(k, v) ? Collections.singletonMap(k, v) : Collections.emptyMap();
            };
        } else {
            func = func.andThen(m -> {
                Map<K, V> r = Util.cast(constructor.produce());
                for (Entry<K, V> e : m.entrySet()) {
                    if (c.apply(e.getKey(), e.getValue())) {
                        r.put(e.getKey(), e.getValue());
                    }
                }
                return r;
            });
        }
        applied.set(false);
        return this;
    }
    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the map excludes elements from the map which satisfies `c`. Hence the resultant map of this api only contains the elements which does not satisfy the condition `c`. &lt;br /&gt;
     * Function application is &lt;b&gt;lazy&lt;/b&gt; which means calling this api has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param c condition to evaluate against each element
     * @return map containing elements which does not satisfy the condition `c`
     */
    public MutableMap<K, V> filterOut(BiFunction<K, V, Boolean> c) {
        return filter((a, b) -> !c.apply(a, b));
    }
    public MutableMap<K, V> filterByKey(Condition<K> c) {
        return filter((a, x) -> c.evaluate(a));
    }
    public MutableMap<K, V> filterOutByKey(Condition<K> c) {
        return filterByKey(c.negate());
    }
    public MutableMap<K, V> filterByValue(Condition<V> c) {
        return filter((x, b) -> c.evaluate(b));
    }
    public MutableMap<K, V> filterOutByValue(Condition<V> c) {
        return filterByValue(c.negate());
    }
    /* ------------------- END: Lazy methods ------------------- */

    /**
     * Function application is &lt;b&gt;eager&lt;/b&gt; i.e. it applies all the lazy functions (if any) to map elements
     * @return the underlying &lt;code&gt;map&lt;/code&gt; with all the lazy functions (if any) applied
     */
    @Override
    public Map<K, V> map() {
        apply();
        return map;
    }

    /**
     * @return &lt;code&gt;true&lt;/code&gt; if all the lazy functions (if any) are applied otherwise &lt;code&gt;false&lt;/code&gt;
     */
    @Override
    public boolean isApplied() {
        return applied.get();
    }

    /**
     * Function application is &lt;b&gt;eager&lt;/b&gt; i.e. it applies all the lazy functions (if any) to map elements
     * @return &lt;code&gt;current instance&lt;/code&gt; with all the lazy functions (if any) applied
     */
    @Override
    public MutableMap<K, V> applied() {
        apply();
        return this;
    }

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the {@link #applied() applied} map and returns a &lt;code&gt;Couple&lt;/code&gt; of &lt;code&gt;MutableMap&lt;/code&gt;s with satisfying elements in {@link Couple#first() first} and &lt;b&gt;not&lt;/b&gt; satisfying elements in {@link Couple#second() second}
     * @param c condition based on which the elements will be segregated
     * @return &lt;code&gt;Couple&lt;/code&gt; of &lt;code&gt;MutableMap&lt;/code&gt;s with satisfying elements in {@link Couple#first() first} and &lt;b&gt;not&lt;/b&gt; satisfying elements in {@link Couple#second() second}
     */
    public Couple<MutableMap<K, V>, MutableMap<K, V>> split(BiFunction<K, V, Boolean> c) {
        MutableMap<K, V> match = MutableMap.wrap(constructor);
        MutableMap<K, V> rest = MutableMap.wrap(constructor);
        apply();
        for (Entry<K, V> t : map.entrySet()) {
            if (c.apply(t.getKey(), t.getValue())) {
                match.put(t.getKey(), t.getValue());
            } else {
                rest.put(t.getKey(), t.getValue());
            }
        }
        return Tuple.of(match, rest);
    }

    @Override
    public int size() {
        apply();
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        apply();
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        apply();
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        apply();
        return map.containsValue(value);
    }

    public boolean containsKeys(Set<K> keys) {
        return keySet().containsAll(keys);
    }

    public boolean containsValues(Set<V> values) {
        return values().containsAll(values);
    }

    @Override
    public V get(Object key) {
        apply();
        return map.get(key);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        apply();
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public V put(K key, V value) {
        apply();
        return map.put(key, value);
    }

    public MutableMap<K, V> include(K key, V val) {
        put(key, val);
        return this;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        apply();
        return map.putIfAbsent(key, value);
    }

    public MutableMap<K, V> includeIfAbsent(K key, V val) {
        putIfAbsent(key, val);
        return this;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        apply();
        map.putAll(m);
    }

    public MutableMap<K, V> include(Map<K, V> that) {
        putAll(that);
        return this;
    }

    @Override
    public V remove(Object key) {
        apply();
        return map.remove(key);
    }

    public MutableMap<K, V> delete(K key) {
        remove(key);
        return this;
    }

    @Override
    public boolean remove(Object key, Object value) {
        apply();
        return map.remove(key, value);
    }

    public MutableMap<K, V> delete(K key, V value) {
        remove(key, value);
        return this;
    }

    @Override
    public V replace(K key, V value) {
        apply();
        return map.replace(key, value);
    }

    public MutableMap<K, V> replacing(K key, V value) {
        replace(key, value);
        return this;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        apply();
        return map.replace(key, oldValue, newValue);
    }

    public MutableMap<K, V> replacing(K key, V oldValue, V newValue) {
        replace(key, oldValue, newValue);
        return this;
    }

    @Override
    public void clear() {
        apply();
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        apply();
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        apply();
        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        apply();
        return map.entrySet();
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        apply();
        map.forEach(action);
    }

    @Override
    public void replaceAll(java.util.function.BiFunction<? super K, ? super V, ? extends V> function) {
        apply();
        map.replaceAll(function);
    }

    public MutableMap<K, V> replacingAll(java.util.function.BiFunction<? super K, ? super V, ? extends V> function) {
        replaceAll(function);
        return this;
    }

    @Override
    public V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
        apply();
        return map.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        apply();
        return map.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        apply();
        return map.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        apply();
        return map.merge(key, value, remappingFunction);
    }

    @Override
    public String toString() {
        return isApplied() ? map.toString() : "(?=?)";
    }

    @Override
    public int hashCode() {
        apply();
        return map.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        apply();
        if (obj instanceof FunctionalMap) {
            FunctionalMap<?, ?, ?> fSet = Util.cast(obj);
            obj = fSet.map();
        }
        return map.equals(obj);
    }

    public MutableMap<K, V> copy() {
        apply();
        MutableMap<K, V> r = new MutableMap<>(src, constructor, func);
        r.putAll(map);
        return r;
    }

    private void apply() {
        if (!isApplied()) {
            Map<K, V> r = Util.cast(constructor.produce());
            for (Entry<?, ?> e : src.entrySet()) {
                r.putAll(func.apply(e.getKey(), e.getValue()));
            }
            map = r;
            src = Collections.emptyMap();
            func = null;
            applied.set(true);
        }
    }

    @SuppressWarnings("unchecked")
    public static <A, B> MutableMap<A, B> wrap(Producer<Map<?, ?>> constructor) {
        return of((MutableMap<A, B>) constructor.produce(), constructor);
    }
}
