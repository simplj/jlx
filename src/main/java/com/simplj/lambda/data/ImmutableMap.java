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

public class ImmutableMap<K, V> extends FunctionalMap<K, V, ImmutableMap<K, V>> {
    private final Map<?, ?> src;
    private final Map<K, V> map;
    private final Producer<Map<?, ?>> constructor;
    private final BiFunction<Object, Object, ? extends Map<K, V>> func;
    private final boolean applied;

    private ImmutableMap(Map<K, V> map, Producer<Map<?, ?>> cons) {
        this(Collections.emptyMap(), map, cons, null, true);
    }
    private ImmutableMap(Map<?, ?> src, Producer<Map<?, ?>> cons, BiFunction<Object, Object, ? extends Map<K, V>> func) {
        this(src, Util.cast(cons.produce()), cons, func, false);
    }
    public ImmutableMap(Map<?, ?> src, Map<K, V> map, Producer<Map<?, ?>> cons, BiFunction<Object, Object, ? extends Map<K, V>> func, boolean flag) {
        this.src = src;
        this.map = map;
        this.constructor = cons;
        this.func = func;
        this.applied = flag;
    }

    public static <A, B> ImmutableMap<A, B> unit(Producer<Map<?, ?>> constructor) {
        return new ImmutableMap<>(Util.cast(constructor.produce()), constructor);
    }

    public static <A, B> ImmutableMap<A, B> of(Map<A, B> set, Producer<Map<?, ?>> constructor) {
        return new ImmutableMap<>(set, constructor);
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
    public <A, B> ImmutableMap<A, B> map(BiFunction<K, V, Couple<A, B>> f) {
        return flatmap(f.andThen(c -> Collections.singletonMap(c.first(), c.second())));
    }
    public <R> ImmutableMap<R, V> mapK(Function<K, R> f) {
        return flatmapK(f.andThen(Collections::singleton));
    }
    public <R> ImmutableMap<K, R> mapV(Function<V, R> f) {
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
    public <A, B> ImmutableMap<A, B> flatmap(BiFunction<K, V, ? extends Map<A, B>> f) {
        ImmutableMap<A, B> res;
        if (func == null) {
            res = new ImmutableMap<>(map, constructor, f.composeFirst(Util::cast).composeSecond(Util::cast));
        } else {
            res = new ImmutableMap<>(src, constructor, func.andThen(m -> apply(m, f.composeFirst(Util::cast).composeSecond(Util::cast))));
        }
        return res;
    }
    public <R> ImmutableMap<R, V> flatmapK(Function<K, ? extends Set<R>> f) {
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
    public ImmutableMap<K, V> filter(BiFunction<K, V, Boolean> c) {
        return flatmap((k, v) -> c.apply(k, v) ? Collections.singletonMap(k, v) : Collections.emptyMap());
    }
    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the map excludes elements from the map which satisfies `c`. Hence the resultant map of this api only contains the elements which does not satisfy the condition `c`. &lt;br /&gt;
     * Function application is &lt;b&gt;lazy&lt;/b&gt; which means calling this api has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param c condition to evaluate against each element
     * @return map containing elements which does not satisfy the condition `c`
     */
    public ImmutableMap<K, V> filterOut(BiFunction<K, V, Boolean> c) {
        return filter((a, b) -> !c.apply(a, b));
    }
    public ImmutableMap<K, V> filterByKey(Condition<K> c) {
        return filter((a, x) -> c.evaluate(a));
    }
    public ImmutableMap<K, V> filterOutByKey(Condition<K> c) {
        return filterByKey(c.negate());
    }
    public ImmutableMap<K, V> filterByValue(Condition<V> c) {
        return filter((x, b) -> c.evaluate(b));
    }
    public ImmutableMap<K, V> filterOutByValue(Condition<V> c) {
        return filterByValue(c.negate());
    }
    /* ------------------- END: Lazy methods ------------------- */

    /**
     * Function application is &lt;b&gt;eager&lt;/b&gt; i.e. it applies all the lazy functions (if any) to map elements
     * @return the underlying &lt;code&gt;map&lt;/code&gt; with all the lazy functions (if any) applied
     */
    @Override
    Map<K, V> map() {
        alertIfNotApplied();
        return map;
    }

    /**
     * @return &lt;code&gt;true&lt;/code&gt; if all the lazy functions (if any) are applied otherwise &lt;code&gt;false&lt;/code&gt;
     */
    @Override
    public boolean isApplied() {
        return applied;
    }

    /**
     * Function application is &lt;b&gt;eager&lt;/b&gt; i.e. it applies all the lazy functions (if any) to map elements
     * @return &lt;code&gt;current instance&lt;/code&gt; with all the lazy functions (if any) applied
     */
    @Override
    public ImmutableMap<K, V> applied() {
        ImmutableMap<K, V> res;
        if (isApplied()) {
            res = this;
        } else {
            res = new ImmutableMap<>(apply(src, func), constructor);
        }
        return res;
    }

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the {@link #applied() applied} map and returns a &lt;code&gt;Couple&lt;/code&gt; of &lt;code&gt;ImmutableMap&lt;/code&gt;s with satisfying elements in {@link Couple#first() first} and &lt;b&gt;not&lt;/b&gt; satisfying elements in {@link Couple#second() second}
     * @param c condition based on which the elements will be segregated
     * @return &lt;code&gt;Couple&lt;/code&gt; of &lt;code&gt;ImmutableMap&lt;/code&gt;s with satisfying elements in {@link Couple#first() first} and &lt;b&gt;not&lt;/b&gt; satisfying elements in {@link Couple#second() second}
     */
    @Override
    public Couple<ImmutableMap<K, V>, ImmutableMap<K, V>> split(BiFunction<K, V, Boolean> c) {
        ImmutableMap<K, V> match = ImmutableMap.wrap(constructor);
        ImmutableMap<K, V> rest = ImmutableMap.wrap(constructor);
        alertIfNotApplied();
        for (Map.Entry<K, V> t : map.entrySet()) {
            if (c.apply(t.getKey(), t.getValue())) {
                match.map.put(t.getKey(), t.getValue());
            } else {
                rest.map.put(t.getKey(), t.getValue());
            }
        }
        return Tuple.of(match, rest);
    }

    @Override
    public int size() {
        alertIfNotApplied();
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        alertIfNotApplied();
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        alertIfNotApplied();
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        alertIfNotApplied();
        return map.containsValue(value);
    }

    @Override
    public boolean containsKeys(Set<K> keys) {
        return keySet().containsAll(keys);
    }

    @Override
    public boolean containsValues(Set<V> values) {
        return values().containsAll(values);
    }

    @Override
    public V get(Object key) {
        alertIfNotApplied();
        return map.get(key);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        alertIfNotApplied();
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public ImmutableMap<K, V> include(K key, V val) {
        ImmutableMap<K, V> res = applied();
        res.map.put(key, val);
        return res;
    }

    @Override
    public ImmutableMap<K, V> includeIfAbsent(K key, V val) {
        ImmutableMap<K, V> res = applied();
        res.map.putIfAbsent(key, val);
        return res;
    }

    @Override
    public ImmutableMap<K, V> include(Map<K, V> that) {
        ImmutableMap<K, V> res = applied();
        res.map.putAll(that);
        return res;
    }

    @Override
    public ImmutableMap<K, V> delete(K key) {
        ImmutableMap<K, V> res = applied();
        res.map.remove(key);
        return res;
    }

    @Override
    public ImmutableMap<K, V> delete(K key, V value) {
        ImmutableMap<K, V> res = applied();
        res.map.remove(key, value);
        return res;
    }

    @Override
    public ImmutableMap<K, V> replacing(K key, V value) {
        ImmutableMap<K, V> res = applied();
        res.map.replace(key, value);
        return res;
    }

    @Override
    public ImmutableMap<K, V> replacing(K key, V oldValue, V newValue) {
        ImmutableMap<K, V> res = applied();
        res.map.replace(key, oldValue, newValue);
        return res;
    }

    public ImmutableMap<K, V> empty() {
        alertIfNotApplied();
        return new ImmutableMap<>(src, constructor, func);
    }

    @Override
    public Set<K> keySet() {
        alertIfNotApplied();
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        alertIfNotApplied();
        return map.values();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        alertIfNotApplied();
        return map.entrySet();
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        alertIfNotApplied();
        map.forEach(action);
    }

    @Override
    public ImmutableMap<K, V> replacingAll(java.util.function.BiFunction<? super K, ? super V, ? extends V> function) {
        ImmutableMap<K, V> res = applied();
        res.map.replaceAll(function);
        return res;
    }

    @Override
    public V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
        alertIfNotApplied();
        return map.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        alertIfNotApplied();
        return map.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        alertIfNotApplied();
        return map.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        alertIfNotApplied();
        return map.merge(key, value, remappingFunction);
    }

    @Override
    public String toString() {
        return isApplied() ? map.toString() : "(?=?)";
    }

    @Override
    public int hashCode() {
        alertIfNotApplied();
        return map.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        alertIfNotApplied();
        if (obj instanceof FunctionalMap) {
            FunctionalMap<?, ?, ?> fSet = Util.cast(obj);
            obj = fSet.map();
        }
        return map.equals(obj);
    }

    @Override
    public ImmutableMap<K, V> copy() {
        alertIfNotApplied();
        ImmutableMap<K, V> r = new ImmutableMap<>(src, constructor, func);
        r.map.putAll(map);
        return r;
    }

    private void alertIfNotApplied() {
        if (!isApplied()) {
            throw new IllegalStateException("Immutable set not yet `applied`! Consider calling `applied()` before this api");
        }
    }
    private void alertIfNotApplied(String alternateApi) {
        if (!isApplied()) {
            throw new IllegalStateException("Immutable set not yet `applied`! Consider calling `applied()` before this api or `" + alternateApi + "` can be used here as an alternate.");
        }
    }

    private <A, B> Map<A, B> apply(Map<?, ?> m, BiFunction<Object, Object, ? extends Map<A, B>> f) {
        Map<A, B> r = Util.cast(constructor.produce());
        for (Map.Entry<?, ?> e : m.entrySet()) {
            r.putAll(f.apply(e.getKey(), e.getValue()));
        }
        return r;
    }

    @SuppressWarnings("unchecked")
    public static <A, B> ImmutableMap<A, B> wrap(Producer<Map<?, ?>> constructor) {
        return of((Map<A, B>) constructor.produce(), constructor);
    }
}
