package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;

import java.util.Collection;

interface Functor<A, T> {

    default <R> Function<A, LinkedUnit<R>> map(Function<A, LinkedUnit<T>> a, Function<T, R> b) {
        return a.andThen(d -> {
            LinkedUnit<R> r = new LinkedUnit<>();
            LinkedUnit.Node<T> n = d.head();
            while (n != null) {
                r.add(b.apply(n.val()));
                n = n.next();
            }
            return r;
        });
    }

    default <R> Function<A, LinkedUnit<R>> flatmap(Function<A, LinkedUnit<T>> a, Function<T, ? extends Iterable<R>> b) {
        return a.andThen(d -> {
            LinkedUnit<R> r = new LinkedUnit<>();
            LinkedUnit.Node<T> n = d.head();
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
    default <R> Function<A, LinkedUnit<R>> fmap(Function<A, LinkedUnit<T>> a, Function<T, ? extends R[]> b) {
        return a.andThen(d -> {
            LinkedUnit<R> r = new LinkedUnit<>();
            LinkedUnit.Node<T> n = d.head();
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

    default Function<A, LinkedUnit<T>> filter(Function<A, LinkedUnit<T>> f, Condition<T> c) {
        return f.andThen(d -> {
            LinkedUnit.Node<T> n = d.head();
            LinkedUnit.Node<T> p = null;
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

    default <R extends Collection<T>> R apply(Collection<A> src, Function<A, LinkedUnit<T>> func, R r) {
        for (A a : src) {
            LinkedUnit.Node<T> fh = func.apply(a).head();
            while (fh != null) {
                r.add(fh.val());
                fh = fh.next();
            }
        }
        return r;
    }

    default <R extends Collection<T>> R apply(A[] src, Function<A, LinkedUnit<T>> func, R r) {
        for (A a : src) {
            LinkedUnit.Node<T> fh = func.apply(a).head();
            while (fh != null) {
                r.add(fh.val());
                fh = fh.next();
            }
        }
        return r;
    }
}
