package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public abstract class MSet<E> extends FSet<E, MSet<E>> implements Set<E> {
    private static final MSet<?> NONE = MSet.unit(Collections::emptySet);
    Set<E> set;

    private MSet(Set<E> set, Producer<Set<?>> constructor) {
        super(constructor);
        this.set = set;
    }

    public static <A> MSet<A> none() {
        return Util.cast(NONE);
    }

    public static <A> MSet<A> unit() {
        return unit(HashSet::new);
    }

    public static <A> MSet<A> unit(Producer<Set<?>> constructor) {
        return of(Util.cast(constructor.produce()), constructor);
    }

    @SafeVarargs
    public static <A> MSet<A> of(A...elems) {
        return of(Util.asSet(elems));
    }

    public static <A> MSet<A> of(Set<A> set) {
        return of(set, HashSet::new);
    }

    public static <A> MSet<A> of(Set<A> set, Producer<Set<?>> constructor) {
        return new SetFunctor<>(set, constructor, LinkedUnit::new, set);
    }

    public static <E> MSet<E> from(Iterable<E> iter) {
        Set<E> set = new HashSet<>();
        iter.forEach(set::add);
        return of(set, HashSet::new);
    }

    public final ISet<E> immutable() {
        return ISet.of(set());
    }

    /**
     * Function application is <i>eager</i> i.e. it applies all the lazy functions (if any) to set elements
     * @return the underlying <code>set</code> with all the lazy functions (if any) applied
     */
    @Override
    public final Set<E> set() {
        apply();
        return set;
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
    public abstract <R> MSet<R> map(Function<E, R> f);

    /**
     * Applies the function `f` of type <i>(T -&gt; set&lt;R&gt;)</i> to all the elements in the set and returns the resultant flattened set. Function application is <i>lazy</i><br>
     * Detailed Description: <i>flatmap</i>-ing `f` on set <code>[1, 2, 3]</code> will return a set <code>[f(1), f(2), f(3)]</code>.
     * As it can be seen that the function `f` is not applied immediately which makes <code>flatmap</code> a <i>lazy</i> implementation.
     * The function `f` is not applied to the elements until a <i>eager</i> api is called. Therefore, calling <code>flatmap</code> has no effect until a <i>eager</i> api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant set after applying `f` to all the set elements
     */
    public abstract <R> MSet<R> flatmap(Function<E, ? extends Set<R>> f);
    /* ------------------- END: Lazy methods ------------------- */

    /**
     * @return <code>true</code> if all the lazy functions (if any) are applied otherwise <code>false</code>
     */
    @Override
    public boolean isApplied() {
        return set != null;
    }

    @Override
    public MSet<E> applied() {
        apply();
        return this;
    }

    @Override
    public boolean add(E t) {
        apply();
        return set.add(t);
    }

    @Override
    public MSet<E> include(E val) {
        add(val);
        return this;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        apply();
        return set.addAll(c);
    }

    @Override
    public MSet<E> include(Iterable<? extends E> c) {
        apply();
        for (E e : c) {
            set.add(e);
        }
        return this;
    }

    @Override
    public boolean remove(Object o) {
        apply();
        return set.remove(o);
    }

    @Override
    public MSet<E> delete(E val) {
        remove(val);
        return this;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        apply();
        return set.removeAll(c);
    }

    @Override
    public MSet<E> delete(Iterable<? extends E> c) {
        apply();
        for (E e : c) {
            set.remove(e);
        }
        return this;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        apply();
        return set.retainAll(c);
    }

    @Override
    public MSet<E> preserve(Iterable<? extends E> c) {
        apply();
        for (E e : c) {
            if (!set.contains(e)) {
                set.remove(e);
            }
        }
        return this;
    }

    @Override
    public void clear() {
        apply();
        set.clear();
    }

    @Override
    public MSet<E> empty() {
        clear();
        return this;
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        apply();
        return set.removeIf(filter);
    }

    public MSet<E> deleteIf(Condition<? super E> c) {
        removeIf(c::evaluate);
        return this;
    }

    private void apply() {
        if (!isApplied()) {
            set = appliedSet().set;
        }
    }

    abstract MSet<E> appliedSet();

    private static final class SetFunctor<A, T> extends MSet<T> implements Functor<A, T> {
        private final Set<A> src;
        private final Function<A, LinkedUnit<T>> func;

        SetFunctor(Set<A> set, Producer<Set<?>> constructor, Function<A, LinkedUnit<T>> f, Set<T> applied) {
            super(applied, constructor);
            this.src = set;
            this.func = f;
        }

        @Override
        MSet<T> instantiate(Producer<Set<?>> constructor, Set<T> setVal) {
            return new SetFunctor<>(setVal, constructor, LinkedUnit::new, setVal);
        }

        @Override
        public <R> MSet<R> map(Function<T, R> f) {
            return new SetFunctor<>(src, constructor, map(func, f), null);
        }

        @Override
        public <R> MSet<R> flatmap(Function<T, ? extends Set<R>> f) {
            return new SetFunctor<>(src, constructor, flatmap(func, f), null);
        }

        @Override
        public MSet<T> filter(Condition<T> c) {
            return new SetFunctor<>(src, constructor, filter(func, c), null);
        }

        final SetFunctor<T, T> appliedSet() {
            SetFunctor<T, T> res;
            if (set == null) {
                Set<T> r = apply(src, func, Util.cast(constructor.produce()));
                res = new SetFunctor<>(r, constructor, LinkedUnit::new, r);
            } else {
                res = new SetFunctor<>(set, constructor, LinkedUnit::new, set);
            }
            return res;
        }
    }
}
