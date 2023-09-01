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

public abstract class ImmutableMap<K, V> extends FunctionalMap<K, V, ImmutableMap<K, V>> {
    final Map<K, V> map;
    final Producer<Map<?, ?>> constructor;

    public ImmutableMap(Map<K, V> map, Producer<Map<?, ?>> constructor) {
        this.map = map;
        this.constructor = constructor;
    }

    public static <A, B> ImmutableMap<A, B> unit() {
        return unit(HashMap::new);
    }

    public static <A, B> ImmutableMap<A, B> unit(Producer<Map<?, ?>> constructor) {
        return of(Util.cast(constructor.produce()), constructor);
    }

    public static <A, B> ImmutableMap<A, B> of(Map<A, B> map) {
        return of(map, HashMap::new);
    }

    public static <A, B> ImmutableMap<A, B> of(Map<A, B> map, Producer<Map<?, ?>> constructor) {
        return new MapFunctor<>(map, constructor, Pair::new, map);
    }

    /**
     * Function application is &lt;b&gt;eager&lt;/b&gt; i.e. it applies all the lazy functions (if any) to map elements
     * @return the underlying &lt;code&gt;map&lt;/code&gt; with all the lazy functions (if any) applied
     */
    @Override
    public final Map<K, V> map() {
        return applied().map;
    }

    /* ------------------- START: Lazy methods ------------------- */
    /**
     * Applies the function `f` of type &lt;i&gt;(T -&gt; R)&lt;/i&gt; to all the elements in the map and returns the resultant map. Function application is &lt;b&gt;lazy&lt;/b&gt;&lt;br /&gt;
     * Detailed Description: &lt;b&gt;map&lt;/b&gt;-ing `f` on map &lt;code&gt;[1, 2, 3]&lt;/code&gt; will return a map &lt;code&gt;[f(1), f(2), f(3)]&lt;/code&gt;.
     * As it can be seen that the function `f` is not applied immediately which makes &lt;code&gt;map&lt;/code&gt; a &lt;b&gt;lazy&lt;/b&gt; implementation.
     * The function `f` is not applied to the elements until a &lt;b&gt;eager&lt;/b&gt; api is called. Therefore, calling &lt;code&gt;map&lt;/code&gt; has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param f function to apply to each element.
     * @param <A> Type of the resultant key
     * @param <B> Type of the resultant value
     * @return resultant map after applying `f` to all the map elements
     */
    public abstract <A, B> ImmutableMap<A, B> map(BiFunction<K, V, Tuple2<A, B>> f);
    public abstract <R> ImmutableMap<R, V> mapK(Function<K, R> f);
    public abstract <R> ImmutableMap<K, R> mapV(Function<V, R> f);

    /**
     * Applies the function `f` of type &lt;i&gt;(T -&gt; map&lt;R&gt;)&lt;/i&gt; to all the elements in the map and returns the resultant flattened map. Function application is &lt;b&gt;lazy&lt;/b&gt;&lt;br /&gt;
     * Detailed Description: &lt;b&gt;flatmap&lt;/b&gt;-ing `f` on map &lt;code&gt;[1, 2, 3]&lt;/code&gt; will return a map &lt;code&gt;[f(1), f(2), f(3)]&lt;/code&gt;.
     * As it can be seen that the function `f` is not applied immediately which makes &lt;code&gt;flatmap&lt;/code&gt; a &lt;b&gt;lazy&lt;/b&gt; implementation.
     * The function `f` is not applied to the elements until a &lt;b&gt;eager&lt;/b&gt; api is called. Therefore, calling &lt;code&gt;flatmap&lt;/code&gt; has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param f function to apply to each element.
     * @param <A> Type of the resultant key
     * @param <B> Type of the resultant value
     * @return resultant map after applying `f` to all the map elements
     */
    public abstract <A, B> ImmutableMap<A, B> flatmap(BiFunction<K, V, ? extends Map<A, B>> f);
    public abstract <R> ImmutableMap<R, V> flatmapK(Function<K, ? extends Set<R>> f);

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the map excludes elements from the map which does not satisfy `c`. Hence the resultant map of this api only contains the elements which satisfies the condition `c`. &lt;br /&gt;
     * Function application is &lt;b&gt;lazy&lt;/b&gt; which means calling this api has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param c condition to evaluate against each element
     * @return map containing elements which satisfies the condition `c`
     */
    public abstract ImmutableMap<K, V> filter(BiFunction<K, V, Boolean> c);
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
     * @return &lt;code&gt;true&lt;/code&gt; if all the lazy functions (if any) are applied otherwise &lt;code&gt;false&lt;/code&gt;
     */
    @Override
    public boolean isApplied() {
        return map != null;
    }

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the {@link #applied() applied} map and returns a &lt;code&gt;Couple&lt;/code&gt; of &lt;code&gt;ImmutableMap&lt;/code&gt;s with satisfying elements in {@link Couple#first() first} and &lt;b&gt;not&lt;/b&gt; satisfying elements in {@link Couple#second() second}
     * @param c condition based on which the elements will be segregated
     * @return &lt;code&gt;Couple&lt;/code&gt; of &lt;code&gt;ImmutableMap&lt;/code&gt;s with satisfying elements in {@link Couple#first() first} and &lt;b&gt;not&lt;/b&gt; satisfying elements in {@link Couple#second() second}
     */
    @Override
    public Couple<ImmutableMap<K, V>, ImmutableMap<K, V>> split(BiFunction<K, V, Boolean> c) {
        ImmutableMap<K, V> match = ImmutableMap.newInstance(constructor);
        ImmutableMap<K, V> rest = ImmutableMap.newInstance(constructor);
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
        return map().size();
    }

    @Override
    public boolean isEmpty() {
        return map().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map().containsValue(value);
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
        return map().get(key);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return map().getOrDefault(key, defaultValue);
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
        return newInstance(constructor);
    }

    @Override
    public Set<K> keySet() {
        return map().keySet();
    }

    @Override
    public Collection<V> values() {
        return map().values();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return map().entrySet();
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        map().forEach(action);
    }

    @Override
    public ImmutableMap<K, V> replacingAll(java.util.function.BiFunction<? super K, ? super V, ? extends V> function) {
        ImmutableMap<K, V> res = applied();
        res.map.replaceAll(function);
        return res;
    }

    @Override
    public V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
        return map().computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return map().computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return map().compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return map().merge(key, value, remappingFunction);
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return map().entrySet().iterator();
    }

    @Override
    public void forEach(Consumer<? super Map.Entry<K, V>> action) {
        super.forEach(action);
    }

    @Override
    public Spliterator<Map.Entry<K, V>> spliterator() {
        return map().entrySet().spliterator();
    }

    @Override
    public String toString() {
        return isApplied() ? map.toString() : "(?=?)";
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

    @Override
    public ImmutableMap<K, V> copy() {
        ImmutableMap<K, V> r = newInstance(constructor);
        r.map.putAll(map);
        return r;
    }

    public static <A, B> ImmutableMap<A, B> newInstance(Producer<Map<?, ?>> constructor) {
        Map<A, B> map = Util.cast(constructor.produce());
        return new MapFunctor<>(map, constructor, Pair::new, map);
    }

    private static final class MapFunctor<T, R, A, B> extends ImmutableMap<A, B> implements BiFunctor<T, R, A, B> {
        private final Map<T, R> src;
        private final BiFunction<T, R, Pair<A, B>> func;

        MapFunctor(Map<T, R> map, Producer<Map<?, ?>> constructor, BiFunction<T, R, Pair<A, B>> f, Map<A, B> applied) {
            super(applied, constructor);
            this.src = map;
            this.func = f;
        }

        public <C, D> ImmutableMap<C, D> map(BiFunction<A, B, Tuple2<C, D>> f) {
            return new MapFunctor<>(src, constructor, map(func, f), null);
        }

        @Override
        public <C> ImmutableMap<C, B> mapK(Function<A, C> f) {
            return new MapFunctor<>(src, constructor, mapK(func, f), null);
        }

        @Override
        public <D> ImmutableMap<A, D> mapV(Function<B, D> f) {
            return new MapFunctor<>(src, constructor, mapV(func, f), null);
        }

        public <C, D> ImmutableMap<C, D> flatmap(BiFunction<A, B, ? extends Map<C, D>> f) {
            return new MapFunctor<>(src, constructor, flatmap(func, f), null);
        }

        public ImmutableMap<A, B> filter(BiFunction<A, B, Boolean> c) {
            return new MapFunctor<>(src, constructor, filter(func, c), null);
        }

        @Override
        public <C> ImmutableMap<C, B> flatmapK(Function<A, ? extends Set<C>> f) {
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
