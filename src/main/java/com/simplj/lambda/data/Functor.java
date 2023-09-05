package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;

import java.util.Collection;

interface Functor<A, T> {

    default <R> Function<A, Data<R>> map(Function<A, Data<T>> a, Function<T, R> b) {
        return a.andThen(d -> {
            Data<R> r = new Data<>();
            Data.Node<T> n = d.head();
            while (n != null) {
                r.add(b.apply(n.val()));
                n = n.next();
            }
            return r;
        });
    }

    default <R> Function<A, Data<R>> flatmap(Function<A, Data<T>> a, Function<T, ? extends Iterable<R>> b) {
        return a.andThen(d -> {
            Data<R> r = new Data<>();
            Data.Node<T> n = d.head();
            while (n != null) {
                Iterable<R> l = b.apply(n.val());
                for (R e : l) {
                    r.add(e);
                }
                n = n.next();
            }
            return r;
        });
    }
    default <R> Function<A, Data<R>> fmap(Function<A, Data<T>> a, Function<T, ? extends R[]> b) {
        return a.andThen(d -> {
            Data<R> r = new Data<>();
            Data.Node<T> n = d.head();
            while (n != null) {
                R[] l = b.apply(n.val());
                for (R e : l) {
                    r.add(e);
                }
                n = n.next();
            }
            return r;
        });
    }

    default Function<A, Data<T>> filter(Function<A, Data<T>> f, Condition<T> c) {
        return f.andThen(d -> {
            Data.Node<T> n = d.head();
            Data.Node<T> p = null;
            while (n != null) {
                if (c.evaluate(n.val())) {
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

    default <R extends Collection<T>> R apply(Collection<A> src, Function<A, Data<T>> func, R r) {
        for (A a : src) {
            Data.Node<T> fh = func.apply(a).head();
            while (fh != null) {
                r.add(fh.val());
                fh = fh.next();
            }
        }
        return r;
    }

    default <R extends Collection<T>> R apply(A[] src, Function<A, Data<T>> func, R r) {
        for (A a : src) {
            Data.Node<T> fh = func.apply(a).head();
            while (fh != null) {
                r.add(fh.val());
                fh = fh.next();
            }
        }
        return r;
    }
}
