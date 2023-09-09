package com.simplj.lambda.data;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.tuples.Tuple2;

import java.util.Map;
import java.util.Set;

interface BiFunctor<K, V, A, B> {

    default <T, R> BiFunction<K, V, LinkedPair<T, R>> map(BiFunction<K, V, LinkedPair<A, B>> a, BiFunction<A, B, Tuple2<T, R>> b) {
        return a.andThen(d -> {
            LinkedPair<T, R> r = new LinkedPair<>();
            LinkedPair.Node<A, B> n = d.head();
            while (n != null) {
                r.add(b.apply(n.key(), n.val()));
                n = n.next();
            }
            return r;
        });
    }
    default <T> BiFunction<K, V, LinkedPair<T, B>> mapK(BiFunction<K, V, LinkedPair<A, B>> a, Function<A, T> b) {
        return a.andThen(d -> {
            LinkedPair<T, B> r = new LinkedPair<>();
            LinkedPair.Node<A, B> n = d.head();
            while (n != null) {
                r.add(b.apply(n.key()), n.val());
                n = n.next();
            }
            return r;
        });
    }
    default <R> BiFunction<K, V, LinkedPair<A, R>> mapV(BiFunction<K, V, LinkedPair<A, B>> a, Function<B, R> b) {
        return a.andThen(d -> {
            LinkedPair<A, R> r = new LinkedPair<>();
            LinkedPair.Node<A, B> n = d.head();
            while (n != null) {
                r.add(n.key(), b.apply(n.val()));
                n = n.next();
            }
            return r;
        });
    }

    default <T, R> BiFunction<K, V, LinkedPair<T, R>> flatmap(BiFunction<K, V, LinkedPair<A, B>> a, BiFunction<A, B, ? extends Map<T, R>> b) {
        return a.andThen(d -> {
            LinkedPair<T, R> r = new LinkedPair<>();
            LinkedPair.Node<A, B> n = d.head();
            while (n != null) {
                Map<T, R> l = b.apply(n.key(), n.val());
                l.forEach(r::add);
                n = n.next();
            }
            return r;
        });
    }
    default <T> BiFunction<K, V, LinkedPair<T, B>> flatmapK(BiFunction<K, V, LinkedPair<A, B>> a, Function<A, ? extends Set<T>> b) {
        return a.andThen(d -> {
            LinkedPair<T, B> r = new LinkedPair<>();
            LinkedPair.Node<A, B> n = d.head();
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

    default BiFunction<K, V, LinkedPair<A, B>> filter(BiFunction<K, V, LinkedPair<A, B>> f, BiFunction<A, B, Boolean> c) {
        return f.andThen(d -> {
            LinkedPair.Node<A, B> n = d.head();
            LinkedPair.Node<A, B> p = null;
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

    default Map<A, B> apply(Map<K, V> src, BiFunction<K, V, LinkedPair<A, B>> func, Map<A, B> r) {
        for (Map.Entry<K, V> e : src.entrySet()) {
            LinkedPair.Node<A, B> fh = func.apply(e.getKey(), e.getValue()).head();
            while (fh != null) {
                r.put(fh.key(), fh.val());
                fh = fh.next();
            }
        }
        return r;
    }
}
