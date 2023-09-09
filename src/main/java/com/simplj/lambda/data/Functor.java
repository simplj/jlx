package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;

import java.util.Collection;

interface Functor<A, T> {

    default <R> Function<A, LinkedItem<R>> map(Function<A, LinkedItem<T>> a, Function<T, R> b) {
        return a.andThen(d -> {
            LinkedItem<R> r = new LinkedItem<>();
            LinkedItem.Node<T> n = d.head();
            while (n != null) {
                r.add(b.apply(n.val()));
                n = n.next();
            }
            return r;
        });
    }

    default <R> Function<A, LinkedItem<R>> flatmap(Function<A, LinkedItem<T>> a, Function<T, ? extends Iterable<R>> b) {
        return a.andThen(d -> {
            LinkedItem<R> r = new LinkedItem<>();
            LinkedItem.Node<T> n = d.head();
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
    default <R> Function<A, LinkedItem<R>> fmap(Function<A, LinkedItem<T>> a, Function<T, ? extends R[]> b) {
        return a.andThen(d -> {
            LinkedItem<R> r = new LinkedItem<>();
            LinkedItem.Node<T> n = d.head();
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

    default Function<A, LinkedItem<T>> filter(Function<A, LinkedItem<T>> f, Condition<T> c) {
        return f.andThen(d -> {
            LinkedItem.Node<T> n = d.head();
            LinkedItem.Node<T> p = null;
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

    default <R extends Collection<T>> R apply(Collection<A> src, Function<A, LinkedItem<T>> func, R r) {
        for (A a : src) {
            LinkedItem.Node<T> fh = func.apply(a).head();
            while (fh != null) {
                r.add(fh.val());
                fh = fh.next();
            }
        }
        return r;
    }

    default <R extends Collection<T>> R apply(A[] src, Function<A, LinkedItem<T>> func, R r) {
        for (A a : src) {
            LinkedItem.Node<T> fh = func.apply(a).head();
            while (fh != null) {
                r.add(fh.val());
                fh = fh.next();
            }
        }
        return r;
    }
}
