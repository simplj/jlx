package com.simplj.lambda.data;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;
import com.simplj.lambda.tuples.Tuple2;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class MutableMap<K, V> extends FunctionalMap<K, V, MutableMap<K, V>> implements Map<K, V> {
    Map<K, V> map;
    final Producer<Map<?, ?>> constructor;

    private MutableMap(Map<K, V> map, Producer<Map<?, ?>> constructor) {
        this.map = map;
        this.constructor = constructor;
    }

    public static <A, B> MutableMap<A, B> unit() {
        return unit(HashMap::new);
    }

    public static <A, B> MutableMap<A, B> unit(Producer<Map<?, ?>> constructor) {
        return of(Util.cast(constructor.produce()), constructor);
    }

    public static <A, B> MutableMap<A, B> of(Map<A, B> map) {
        return of(map, HashMap::new);
    }

    public static <A, B> MutableMap<A, B> of(Map<A, B> map, Producer<Map<?, ?>> constructor) {
        return new MapFunctor<>(map, constructor, Pair::new, map);
    }

    /**
     * Function application is &lt;b&gt;eager&lt;/b&gt; i.e. it applies all the lazy functions (if any) to map elements
     * @return the underlying &lt;code&gt;map&lt;/code&gt; with all the lazy functions (if any) applied
     */
    @Override
    public final Map<K, V> map() {
        apply();
        return map;
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
    public abstract <A, B> MutableMap<A, B> map(BiFunction<K, V, Tuple2<A, B>> f);
    public abstract <R> MutableMap<R, V> mapK(Function<K, R> f);
    public abstract <R> MutableMap<K, R> mapV(Function<V, R> f);

    /**
     * Applies the function `f` of type &lt;i&gt;(T -&gt; map&lt;R&gt;)&lt;/i&gt; to all the elements in the map and returns the resultant flattened map. Function application is &lt;b&gt;lazy&lt;/b&gt;&lt;br /&gt;
     * Detailed Description: &lt;b&gt;flatmap&lt;/b&gt;-ing `f` on map &lt;code&gt;[1, 2, 3]&lt;/code&gt; will return a map &lt;code&gt;[f(1), f(2), f(3)]&lt;/code&gt;.
     * As it can be seen that the function `f` is not applied immediately which makes &lt;code&gt;flatmap&lt;/code&gt; a &lt;b&gt;lazy&lt;/b&gt; implementation.
     * The function `f` is not applied to the elements until a &lt;b&gt;eager&lt;/b&gt; api is called. Therefore, calling &lt;code&gt;flatmap&lt;/code&gt; has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param f function to apply to each element.
     * @return resultant map after applying `f` to all the map elements
     */
    public abstract <A, B> MutableMap<A, B> flatmap(BiFunction<K, V, ? extends Map<A, B>> f);
    public abstract <R> MutableMap<R, V> flatmapK(Function<K, ? extends Set<R>> f);

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the map excludes elements from the map which does not satisfy `c`. Hence the resultant map of this api only contains the elements which satisfies the condition `c`. &lt;br /&gt;
     * Function application is &lt;b&gt;lazy&lt;/b&gt; which means calling this api has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param c condition to evaluate against each element
     * @return map containing elements which satisfies the condition `c`
     */
    public abstract MutableMap<K, V> filter(BiFunction<K, V, Boolean> c);
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
     * @return &lt;code&gt;true&lt;/code&gt; if all the lazy functions (if any) are applied otherwise &lt;code&gt;false&lt;/code&gt;
     */
    @Override
    public boolean isApplied() {
        return map != null;
    }

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the {@link #applied() applied} map and returns a &lt;code&gt;Couple&lt;/code&gt; of &lt;code&gt;MutableMap&lt;/code&gt;s with satisfying elements in {@link Couple#first() first} and &lt;b&gt;not&lt;/b&gt; satisfying elements in {@link Couple#second() second}
     * @param c condition based on which the elements will be segregated
     * @return &lt;code&gt;Couple&lt;/code&gt; of &lt;code&gt;MutableMap&lt;/code&gt;s with satisfying elements in {@link Couple#first() first} and &lt;b&gt;not&lt;/b&gt; satisfying elements in {@link Couple#second() second}
     */
    @Override
    public Couple<MutableMap<K, V>, MutableMap<K, V>> split(BiFunction<K, V, Boolean> c) {
        MutableMap<K, V> match = MutableMap.newInstance(constructor);
        MutableMap<K, V> rest = MutableMap.newInstance(constructor);
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

    @Override
    public MutableMap<K, V> include(K key, V val) {
        put(key, val);
        return this;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        apply();
        return map.putIfAbsent(key, value);
    }

    @Override
    public MutableMap<K, V> includeIfAbsent(K key, V val) {
        putIfAbsent(key, val);
        return this;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        apply();
        map.putAll(m);
    }

    @Override
    public MutableMap<K, V> include(Map<K, V> that) {
        putAll(that);
        return this;
    }

    @Override
    public V remove(Object key) {
        apply();
        return map.remove(key);
    }

    @Override
    public MutableMap<K, V> delete(K key) {
        remove(key);
        return this;
    }

    @Override
    public boolean remove(Object key, Object value) {
        apply();
        return map.remove(key, value);
    }

    @Override
    public MutableMap<K, V> delete(K key, V value) {
        remove(key, value);
        return this;
    }

    @Override
    public V replace(K key, V value) {
        apply();
        return map.replace(key, value);
    }

    @Override
    public MutableMap<K, V> replacing(K key, V value) {
        replace(key, value);
        return this;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        apply();
        return map.replace(key, oldValue, newValue);
    }

    @Override
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
    public MutableMap<K, V> empty() {
        clear();
        return this;
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

    @Override
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
    public Iterator<Entry<K, V>> iterator() {
        apply();
        return map.entrySet().iterator();
    }

    @Override
    public void forEach(Consumer<? super Entry<K, V>> action) {
        apply();
        super.forEach(action);
    }

    @Override
    public Spliterator<Entry<K, V>> spliterator() {
        apply();
        return map.entrySet().spliterator();
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
            FunctionalMap<?, ?, ?> fMap = Util.cast(obj);
            obj = fMap.map();
        }
        return map.equals(obj);
    }

    @Override
    public MutableMap<K, V> copy() {
        apply();
        MutableMap<K, V> r = newInstance(constructor);
        r.putAll(map);
        return r;
    }

    private void apply() {
        if (!isApplied()) {
            map = applied().map;
        }
    }

    public static <A, B> MutableMap<A, B> newInstance(Producer<Map<?, ?>> constructor) {
        Map<A, B> map = Util.cast(constructor.produce());
        return new MapFunctor<>(map, constructor, Pair::new, map);
    }

    private static final class MapFunctor<T, R, A, B> extends MutableMap<A, B> implements BiFunctor<T, R, A, B> {
        private final Map<T, R> src;
        private final BiFunction<T, R, Pair<A, B>> func;

        MapFunctor(Map<T, R> map, Producer<Map<?, ?>> constructor, BiFunction<T, R, Pair<A, B>> f, Map<A, B> applied) {
            super(applied, constructor);
            this.src = map;
            this.func = f;
        }

        public <C, D> MutableMap<C, D> map(BiFunction<A, B, Tuple2<C, D>> f) {
            return new MapFunctor<>(src, constructor, map(func, f), null);
        }

        @Override
        public <C> MutableMap<C, B> mapK(Function<A, C> f) {
            return new MapFunctor<>(src, constructor, mapK(func, f), null);
        }

        @Override
        public <D> MutableMap<A, D> mapV(Function<B, D> f) {
            return new MapFunctor<>(src, constructor, mapV(func, f), null);
        }

        public <C, D> MutableMap<C, D> flatmap(BiFunction<A, B, ? extends Map<C, D>> f) {
            return new MapFunctor<>(src, constructor, flatmap(func, f), null);
        }

        public MutableMap<A, B> filter(BiFunction<A, B, Boolean> c) {
            return new MapFunctor<>(src, constructor, filter(func, c), null);
        }

        @Override
        public <C> MutableMap<C, B> flatmapK(Function<A, ? extends Set<C>> f) {
            return new MapFunctor<>(src, constructor, flatmapK(func, f), null);
        }

        public final MapFunctor<A, B, A, B> applied() {
            MapFunctor<A, B, A, B> res;
            if (map == null) {
                Map<A, B> r = apply(src, func, Util.cast(constructor.produce()));
                res = new MapFunctor<>(r, constructor, Pair::new, r);
            } else {
                res = new MapFunctor<>(map, constructor, Pair::new, map);
            }
            return res;
        }
    }
}
