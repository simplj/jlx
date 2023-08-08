package com.simplj.lambda.data;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.tuples.Tuple2;

import java.util.Map;
import java.util.Set;

interface BiFunctor<K, V, A, B> {

    default <T, R> BiFunction<K, V, Pair<T, R>> map(BiFunction<K, V, Pair<A, B>> a, BiFunction<A, B, Tuple2<T, R>> b) {
        return a.andThen(d -> {
            Pair<T, R> r = new Pair<>();
            Pair.Node<A, B> n = d.head();
            while (n != null) {
                r.add(b.apply(n.key(), n.val()));
                n = n.next();
            }
            return r;
        });
    }
    default <T> BiFunction<K, V, Pair<T, B>> mapK(BiFunction<K, V, Pair<A, B>> a, Function<A, T> b) {
        return a.andThen(d -> {
            Pair<T, B> r = new Pair<>();
            Pair.Node<A, B> n = d.head();
            while (n != null) {
                r.add(b.apply(n.key()), n.val());
                n = n.next();
            }
            return r;
        });
    }
    default <R> BiFunction<K, V, Pair<A, R>> mapV(BiFunction<K, V, Pair<A, B>> a, Function<B, R> b) {
        return a.andThen(d -> {
            Pair<A, R> r = new Pair<>();
            Pair.Node<A, B> n = d.head();
            while (n != null) {
                r.add(n.key(), b.apply(n.val()));
                n = n.next();
            }
            return r;
        });
    }

    default <T, R> BiFunction<K, V, Pair<T, R>> flatmap(BiFunction<K, V, Pair<A, B>> a, BiFunction<A, B, ? extends Map<T, R>> b) {
        return a.andThen(d -> {
            Pair<T, R> r = new Pair<>();
            Pair.Node<A, B> n = d.head();
            while (n != null) {
                Map<T, R> l = b.apply(n.key(), n.val());
                l.forEach(r::add);
                n = n.next();
            }
            return r;
        });
    }
    default <T> BiFunction<K, V, Pair<T, B>> flatmapK(BiFunction<K, V, Pair<A, B>> a, Function<A, ? extends Set<T>> b) {
        return a.andThen(d -> {
            Pair<T, B> r = new Pair<>();
            Pair.Node<A, B> n = d.head();
            while (n != null) {
                Set<T> l = b.apply(n.key());
                for (T t : l) {
                    r.add(t, n.val());
                }
                n = n.next();
            }
            return r;
        });
    }

    default BiFunction<K, V, Pair<A, B>> filter(BiFunction<K, V, Pair<A, B>> f, BiFunction<A, B, Boolean> c) {
        return f.andThen(d -> {
            Pair.Node<A, B> n = d.head();
            Pair.Node<A, B> p = null;
            while (n != null) {
                if (c.apply(n.key(), n.val())) {
                    p = n;
                } else if (p == null) {
                    d.removeHead();
                } else {
                    d.removeNext(p);
                }
                n = n.next();
            }
            return d;
        });
    }

    default Map<A, B> apply(Map<K, V> src, BiFunction<K, V, Pair<A, B>> func, Map<A, B> r) {
        for (Map.Entry<K, V> e : src.entrySet()) {
            Pair.Node<A, B> fh = func.apply(e.getKey(), e.getValue()).head();
            while (fh != null) {
                r.put(fh.key(), fh.val());
                fh = fh.next();
            }
        }
        return r;
    }
}
