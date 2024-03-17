package com.simplj.lambda.data;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public abstract class MMap<K, V> extends FMap<K, V, MMap<K, V>> implements Map<K, V> {
    private static final MMap<?, ?> NONE = MMap.unit(Collections::emptyMap);
    Map<K, V> map;

    private MMap(Map<K, V> map, Producer<Map<?, ?>> constructor) {
        super(constructor);
        this.map = map;
    }

    public static <A, B> MMap<A, B> none() {
        return Util.cast(NONE);
    }

    public static <A, B> MMap<A, B> unit() {
        return unit(HashMap::new);
    }

    public static <A, B> MMap<A, B> unit(Producer<Map<?, ?>> constructor) {
        return of(Util.cast(constructor.produce()), constructor);
    }

    @SafeVarargs
    public static <A, B> MMap<A, B> of(Couple<A, B>...elems) {
        return of(Util.asMap(elems));
    }

    public static <A, B> MMap<A, B> of(Map<A, B> map) {
        return of(map, HashMap::new);
    }

    public static <A, B> MMap<A, B> of(Map<A, B> map, Producer<Map<?, ?>> constructor) {
        return new MapFunctor<>(map, constructor, LinkedPair::new, map);
    }

    public final IMap<K, V> immutable() {
        return IMap.of(map());
    }

    /**
     * Function application is <i>eager</i> i.e. it applies all the lazy functions (if any) to map elements
     * @return the underlying <code>map</code> with all the lazy functions (if any) applied
     */
    @Override
    public final Map<K, V> map() {
        apply();
        return map;
    }

    /* ------------------- START: Lazy methods ------------------- */
    /**
     * Applies the function `f` of type <i>(T -&gt; R)</i> to all the elements in the map and returns the resultant map. Function application is <i>lazy</i><br>
     * Detailed Description: <i>map</i>-ing `f` on map <code>[1, 2, 3]</code> will return a map <code>[f(1), f(2), f(3)]</code>.
     * As it can be seen that the function `f` is not applied immediately which makes <code>map</code> a <i>lazy</i> implementation.
     * The function `f` is not applied to the elements until a <i>eager</i> api is called. Therefore, calling <code>map</code> has no effect until a <i>eager</i> api is called.
     * @param f function to apply to each element.
     * @param <A> Type of the resultant key
     * @param <B> Type of the resultant value
     * @return resultant map after applying `f` to all the map elements
     */
    public abstract <A, B> MMap<A, B> map(BiFunction<K, V, Tuple2<A, B>> f);
    public abstract <R> MMap<R, V> mapK(Function<K, R> f);
    public abstract <R> MMap<K, R> mapV(Function<V, R> f);

    /**
     * Applies the function `f` of type <i>(T -&gt; map&lt;R&gt;)</i> to all the elements in the map and returns the resultant flattened map. Function application is <i>lazy</i><br>
     * Detailed Description: <i>flatmap</i>-ing `f` on map <code>[1, 2, 3]</code> will return a map <code>[f(1), f(2), f(3)]</code>.
     * As it can be seen that the function `f` is not applied immediately which makes <code>flatmap</code> a <i>lazy</i> implementation.
     * The function `f` is not applied to the elements until a <i>eager</i> api is called. Therefore, calling <code>flatmap</code> has no effect until a <i>eager</i> api is called.
     * @param f function to apply to each element.
     * @param <A> Type of the resultant key
     * @param <B> Type of the resultant value
     * @return resultant map after applying `f` to all the map elements
     */
    public abstract <A, B> MMap<A, B> flatmap(BiFunction<K, V, ? extends Map<A, B>> f);
    public abstract <R> MMap<R, V> flatmapK(Function<K, ? extends Set<R>> f);
    /* ------------------- END: Lazy methods ------------------- */

    /**
     * @return <code>true</code> if all the lazy functions (if any) are applied otherwise <code>false</code>
     */
    @Override
    public boolean isApplied() {
        return map != null;
    }

    @Override
    public MMap<K, V> applied() {
        apply();
        return this;
    }

    @Override
    public V put(K key, V value) {
        apply();
        return map.put(key, value);
    }

    @Override
    public MMap<K, V> include(K key, V val) {
        put(key, val);
        return this;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        apply();
        return map.putIfAbsent(key, value);
    }

    @Override
    public MMap<K, V> includeIfAbsent(K key, V val) {
        putIfAbsent(key, val);
        return this;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        apply();
        map.putAll(m);
    }

    @Override
    public MMap<K, V> include(Map<K, V> that) {
        putAll(that);
        return this;
    }

    @Override
    public V remove(Object key) {
        apply();
        return map.remove(key);
    }

    @Override
    public MMap<K, V> delete(K key) {
        remove(key);
        return this;
    }

    @Override
    public boolean remove(Object key, Object value) {
        apply();
        return map.remove(key, value);
    }

    @Override
    public MMap<K, V> delete(K key, V value) {
        remove(key, value);
        return this;
    }

    @Override
    public V replace(K key, V value) {
        apply();
        return map.replace(key, value);
    }

    @Override
    public MMap<K, V> replacing(K key, V value) {
        replace(key, value);
        return this;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        apply();
        return map.replace(key, oldValue, newValue);
    }

    @Override
    public MMap<K, V> replacing(K key, V oldValue, V newValue) {
        replace(key, oldValue, newValue);
        return this;
    }

    @Override
    public void clear() {
        apply();
        map.clear();
    }

    @Override
    public MMap<K, V> empty() {
        clear();
        return this;
    }

    @Override
    public void replaceAll(java.util.function.BiFunction<? super K, ? super V, ? extends V> function) {
        apply();
        map.replaceAll(function);
    }

    @Override
    public MMap<K, V> replacingAll(java.util.function.BiFunction<? super K, ? super V, ? extends V> function) {
        replaceAll(function);
        return this;
    }

    @Override
    public void forEach(Consumer<? super Entry<K, V>> action) {
        apply();
        super.forEach(action);
    }

    private void apply() {
        if (!isApplied()) {
            map = appliedMap().map;
        }
    }

    abstract MMap<K, V> appliedMap();

    public static <A, B> MMap<A, B> newInstance(Producer<Map<?, ?>> constructor) {
        Map<A, B> map = Util.cast(constructor.produce());
        return new MapFunctor<>(map, constructor, LinkedPair::new, map);
    }

    private static final class MapFunctor<T, R, A, B> extends MMap<A, B> implements BiFunctor<T, R, A, B> {
        private final Map<T, R> src;
        private final BiFunction<T, R, LinkedPair<A, B>> func;

        MapFunctor(Map<T, R> map, Producer<Map<?, ?>> constructor, BiFunction<T, R, LinkedPair<A, B>> f, Map<A, B> applied) {
            super(applied, constructor);
            this.src = map;
            this.func = f;
        }

        @Override
        MMap<A, B> instantiate(Producer<Map<?, ?>> constructor, Map<A, B> mapVal) {
            return new MapFunctor<>(mapVal, constructor, LinkedPair::new, mapVal);
        }

        public <C, D> MMap<C, D> map(BiFunction<A, B, Tuple2<C, D>> f) {
            return new MapFunctor<>(src, constructor, map(func, f), null);
        }

        @Override
        public <C> MMap<C, B> mapK(Function<A, C> f) {
            return new MapFunctor<>(src, constructor, mapK(func, f), null);
        }

        @Override
        public <D> MMap<A, D> mapV(Function<B, D> f) {
            return new MapFunctor<>(src, constructor, mapV(func, f), null);
        }

        public <C, D> MMap<C, D> flatmap(BiFunction<A, B, ? extends Map<C, D>> f) {
            return new MapFunctor<>(src, constructor, flatmap(func, f), null);
        }

        public MMap<A, B> filter(BiFunction<A, B, Boolean> c) {
            return new MapFunctor<>(src, constructor, filter(func, c), null);
        }

        @Override
        public <C> MMap<C, B> flatmapK(Function<A, ? extends Set<C>> f) {
            return new MapFunctor<>(src, constructor, flatmapK(func, f), null);
        }

        final MapFunctor<A, B, A, B> appliedMap() {
            MapFunctor<A, B, A, B> res;
            if (map == null) {
                Map<A, B> r = apply(src, func, Util.cast(constructor.produce()));
                res = new MapFunctor<>(r, constructor, LinkedPair::new, r);
            } else {
                res = new MapFunctor<>(map, constructor, LinkedPair::new, map);
            }
            return res;
        }
    }
}
