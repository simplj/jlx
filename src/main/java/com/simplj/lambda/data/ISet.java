package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;

import java.util.*;

public abstract class ISet<E> extends FSet<E, ISet<E>> {
    private static final ISet<?> NONE = ISet.unit(Collections::emptySet);
    final Set<E> set;

    public ISet(Set<E> set, Producer<Set<?>> constructor) {
        super(constructor);
        this.set = set;
    }

    public static <A> ISet<A> none() {
        return Util.cast(NONE);
    }

    public static <A> ISet<A> unit() {
        return unit(HashSet::new);
    }

    public static <A> ISet<A> unit(Producer<Set<?>> constructor) {
        return of(Util.cast(constructor.produce()), constructor);
    }

    @SafeVarargs
    public static <A> ISet<A> of(A...elems) {
        return of(Util.asSet(elems));
    }

    public static <A> ISet<A> of(Set<A> set) {
        return of(set, HashSet::new);
    }

    public static <A> ISet<A> of(Set<A> set, Producer<Set<?>> constructor) {
        return new SetFunctor<>(set, constructor, LinkedUnit::new, set);
    }

    public static <E> ISet<E> from(Iterable<E> iter) {
        Set<E> set = new HashSet<>();
        iter.forEach(set::add);
        return of(set, HashSet::new);
    }

    public final MSet<E> mutable() {
        return MSet.of(set());
    }

    /**
     * Function application is <i>eager</i> i.e. it applies all the lazy functions (if any) to set elements
     * @return the underlying <code>set</code> with all the lazy functions (if any) applied
     */
    @Override
    public final Set<E> set() {
        return applied().set;
    }

    /* ------------------- START: Lazy methods ------------------- */
    /**
     * Applies the function `f` of type <i>(T -&gt; R)</i> to all the elements in the set and returns the resultant set. Function application is <i>lazy</i><br>
     * Detailed Description: <i>map</i>-ing `f` on set <code>[1, 2, 3]</code> will return a set <code>[f(1), f(2), f(3)]</code>.
     * As it can be seen that the function `f` is not applied immediately which makes <code>map</code> a <i>lazy</i> implementation.
     * The function `f` is not applied to the elements until a <i>eager</i> api is called. Therefore, calling <code>map</code> has no effect until a <i>eager</i> api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant set after applying `f` to all the set elements
     */
    public abstract <R> ISet<R> map(Function<E, R> f);

    /**
     * Applies the function `f` of type <i>(T -&gt; set&lt;R&gt;)</i> to all the elements in the set and returns the resultant flattened set. Function application is <i>lazy</i><br>
     * Detailed Description: <i>flatmap</i>-ing `f` on set <code>[1, 2, 3]</code> will return a set <code>[f(1), f(2), f(3)]</code>.
     * As it can be seen that the function `f` is not applied immediately which makes <code>flatmap</code> a <i>lazy</i> implementation.
     * The function `f` is not applied to the elements until a <i>eager</i> api is called. Therefore, calling <code>flatmap</code> has no effect until a <i>eager</i> api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant set after applying `f` to all the set elements
     */
    public abstract <R> ISet<R> flatmap(Function<E, ? extends Set<R>> f);
    /* ------------------- END: Lazy methods ------------------- */

    /**
     * @return <code>true</code> if all the lazy functions (if any) are applied otherwise <code>false</code>
     */
    @Override
    public boolean isApplied() {
        return set != null;
    }

    public final ISet<E> applied() {
        return isApplied() ? this : appliedSet(false);
    }

    @Override
    public ISet<E> include(E val) {
        ISet<E> res = appliedSet(true);
        res.set.add(val);
        return res;
    }

    @Override
    public ISet<E> include(Iterable<? extends E> c) {
        ISet<E> res = appliedSet(true);
        for (E e : c) {
            res.set.add(e);
        }
        return res;
    }

    @Override
    public ISet<E> delete(E val) {
        ISet<E> res = appliedSet(true);
        res.set.remove(val);
        return res;
    }

    @Override
    public ISet<E> delete(Iterable<? extends E> c) {
        ISet<E> res = appliedSet(true);
        for (E e : c) {
            res.set.remove(e);
        }
        return res;
    }

    @Override
    public ISet<E> preserve(Iterable<? extends E> c) {
        ISet<E> res = appliedSet(true);
        for (E e : c) {
            if (!res.set.contains(e)) {
                res.set.remove(e);
            }
        }
        return res;
    }

    @Override
    public ISet<E> empty() {
        return unit(constructor);
    }

    public ISet<E> deleteIf(Condition<? super E> c) {
        ISet<E> res = appliedSet(true);
        res.set.removeIf(c::evaluate);
        return res;
    }

    abstract ISet<E> appliedSet(boolean copy);

    private static final class SetFunctor<A, T> extends ISet<T> implements Functor<A, T> {
        private final Set<A> src;
        private final Function<A, LinkedUnit<T>> func;

        SetFunctor(Set<A> set, Producer<Set<?>> constructor, Function<A, LinkedUnit<T>> f, Set<T> applied) {
            super(applied, constructor);
            this.src = set;
            this.func = f;
        }

        @Override
        ISet<T> instantiate(Producer<Set<?>> constructor, Set<T> setVal) {
            return new SetFunctor<>(setVal, constructor, LinkedUnit::new, setVal);
        }

        @Override
        public <R> ISet<R> map(Function<T, R> f) {
            return new SetFunctor<>(src, constructor, map(func, f), null);
        }

        @Override
        public <R> ISet<R> flatmap(Function<T, ? extends Set<R>> f) {
            return new SetFunctor<>(src, constructor, flatmap(func, f), null);
        }

        @Override
        public ISet<T> filter(Condition<T> c) {
            return new SetFunctor<>(src, constructor, filter(func, c), null);
        }

        public final SetFunctor<T, T> appliedSet(boolean copy) {
            SetFunctor<T, T> res;
            if (set == null) {
                Set<T> r = apply(src, func, Util.cast(constructor.produce()));
                res = new SetFunctor<>(r, constructor, LinkedUnit::new, r);
            } else if (copy) {
                Set<T> r = Util.cast(constructor.produce());
                r.addAll(set);
                res = new SetFunctor<>(r, constructor, LinkedUnit::new, r);
            } else {
                res = new SetFunctor<>(set, constructor, LinkedUnit::new, set);
            }
            return res;
        }
    }
}
