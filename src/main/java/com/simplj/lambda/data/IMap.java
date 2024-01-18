package com.simplj.lambda.data;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public abstract class IMap<K, V> extends FMap<K, V, IMap<K, V>> {
    final Map<K, V> map;

    public IMap(Map<K, V> map, Producer<Map<?, ?>> constructor) {
        super(constructor);
        this.map = map;
    }

    public static <A, B> IMap<A, B> unit() {
        return unit(HashMap::new);
    }

    public static <A, B> IMap<A, B> unit(Producer<Map<?, ?>> constructor) {
        return of(Util.cast(constructor.produce()), constructor);
    }

    @SafeVarargs
    public static <A, B> IMap<A, B> of(Couple<A, B>...elems) {
        return of(Util.asMap(elems));
    }

    public static <A, B> IMap<A, B> of(Map<A, B> map) {
        return of(map, HashMap::new);
    }

    public static <A, B> IMap<A, B> of(Map<A, B> map, Producer<Map<?, ?>> constructor) {
        return new MapFunctor<>(map, constructor, LinkedPair::new, map);
    }

    /**
     * Function application is <i>eager</i> i.e. it applies all the lazy functions (if any) to map elements
     * @return the underlying <code>map</code> with all the lazy functions (if any) applied
     */
    @Override
    public final Map<K, V> map() {
        return applied().map;
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
    public abstract <A, B> IMap<A, B> map(BiFunction<K, V, Tuple2<A, B>> f);
    public abstract <R> IMap<R, V> mapK(Function<K, R> f);
    public abstract <R> IMap<K, R> mapV(Function<V, R> f);

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
    public abstract <A, B> IMap<A, B> flatmap(BiFunction<K, V, ? extends Map<A, B>> f);
    public abstract <R> IMap<R, V> flatmapK(Function<K, ? extends Set<R>> f);
    /* ------------------- END: Lazy methods ------------------- */

    public abstract <E> ISet<E> merge(BiFunction<K, V, E> f);
    public abstract <E> ISet<E> flatMerge(BiFunction<K, V, ? extends Collection<E>> f);

    /**
     * @return <code>true</code> if all the lazy functions (if any) are applied otherwise <code>false</code>
     */
    @Override
    public boolean isApplied() {
        return map != null;
    }

    public final IMap<K, V> applied() {
        return appliedMap(false);
    }

    @Override
    public IMap<K, V> include(K key, V val) {
        IMap<K, V> res = appliedMap(true);
        res.map.put(key, val);
        return res;
    }

    @Override
    public IMap<K, V> includeIfAbsent(K key, V val) {
        IMap<K, V> res = appliedMap(true);
        res.map.putIfAbsent(key, val);
        return res;
    }

    @Override
    public IMap<K, V> include(Map<K, V> that) {
        IMap<K, V> res = appliedMap(true);
        res.map.putAll(that);
        return res;
    }

    @Override
    public IMap<K, V> delete(K key) {
        IMap<K, V> res = appliedMap(true);
        res.map.remove(key);
        return res;
    }

    @Override
    public IMap<K, V> delete(K key, V value) {
        IMap<K, V> res = appliedMap(true);
        res.map.remove(key, value);
        return res;
    }

    @Override
    public IMap<K, V> replacing(K key, V value) {
        IMap<K, V> res = appliedMap(true);
        res.map.replace(key, value);
        return res;
    }

    @Override
    public IMap<K, V> replacing(K key, V oldValue, V newValue) {
        IMap<K, V> res = appliedMap(true);
        res.map.replace(key, oldValue, newValue);
        return res;
    }

    public IMap<K, V> empty() {
        return unit(constructor);
    }

    @Override
    public IMap<K, V> replacingAll(java.util.function.BiFunction<? super K, ? super V, ? extends V> function) {
        IMap<K, V> res = appliedMap(true);
        res.map.replaceAll(function);
        return res;
    }

    @Override
    public void forEach(Consumer<? super Map.Entry<K, V>> action) {
        super.forEach(action);
    }

    abstract IMap<K, V> appliedMap(boolean copy);

    private static final class MapFunctor<T, R, A, B> extends IMap<A, B> implements BiFunctor<T, R, A, B> {
        private final Map<T, R> src;
        private final BiFunction<T, R, LinkedPair<A, B>> func;

        MapFunctor(Map<T, R> map, Producer<Map<?, ?>> constructor, BiFunction<T, R, LinkedPair<A, B>> f, Map<A, B> applied) {
            super(applied, constructor);
            this.src = map;
            this.func = f;
        }

        @Override
        IMap<A, B> instantiate(Producer<Map<?, ?>> constructor, Map<A, B> mapVal) {
            return new MapFunctor<>(mapVal, constructor, LinkedPair::new, mapVal);
        }

        public <C, D> IMap<C, D> map(BiFunction<A, B, Tuple2<C, D>> f) {
            return new MapFunctor<>(src, constructor, map(func, f), null);
        }

        @Override
        public <C> IMap<C, B> mapK(Function<A, C> f) {
            return new MapFunctor<>(src, constructor, mapK(func, f), null);
        }

        @Override
        public <D> IMap<A, D> mapV(Function<B, D> f) {
            return new MapFunctor<>(src, constructor, mapV(func, f), null);
        }

        public <C, D> IMap<C, D> flatmap(BiFunction<A, B, ? extends Map<C, D>> f) {
            return new MapFunctor<>(src, constructor, flatmap(func, f), null);
        }

        @Override
        public <C> IMap<C, B> flatmapK(Function<A, ? extends Set<C>> f) {
            return new MapFunctor<>(src, constructor, flatmapK(func, f), null);
        }

        @Override
        public <E> ISet<E> merge(BiFunction<A, B, E> f) {
            return null;
        }

        @Override
        public <E> ISet<E> flatMerge(BiFunction<A, B, ? extends Collection<E>> f) {

            return null;
        }

        public IMap<A, B> filter(BiFunction<A, B, Boolean> c) {
            return new MapFunctor<>(src, constructor, filter(func, c), null);
        }

        public final MapFunctor<A, B, A, B> appliedMap(boolean copy) {
            MapFunctor<A, B, A, B> res;
            if (map == null) {
                Map<A, B> r = apply(src, func, Util.cast(constructor.produce()));
                res = new MapFunctor<>(r, constructor, LinkedPair::new, r);
            } else if (copy) {
                Map<A, B> r = Util.cast(constructor.produce());
                r.putAll(map);
                res = new MapFunctor<>(r, constructor, LinkedPair::new, r);
            } else {
                res = new MapFunctor<>(map, constructor, LinkedPair::new, map);
            }
            return res;
        }
    }
}
