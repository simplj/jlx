package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class MutableSet<T> extends FunctionalSet<T, MutableSet<T>> implements Set<T> {
    Set<T> set;

    private MutableSet(Set<T> set, Producer<Set<?>> constructor) {
        super(constructor);
        this.set = set;
    }

    public static <A> MutableSet<A> unit() {
        return unit(HashSet::new);
    }

    public static <A> MutableSet<A> unit(Producer<Set<?>> constructor) {
        return of(Util.cast(constructor.produce()), constructor);
    }

    public static <A> MutableSet<A> of(Set<A> set) {
        return of(set, HashSet::new);
    }

    public static <A> MutableSet<A> of(Set<A> set, Producer<Set<?>> constructor) {
        return new SetFunctor<>(set, constructor, Data::new, set);
    }

    /**
     * Function application is <i>eager</i> i.e. it applies all the lazy functions (if any) to set elements
     * @return the underlying <code>set</code> with all the lazy functions (if any) applied
     */
    @Override
    public final Set<T> set() {
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
    public abstract <R> MutableSet<R> map(Function<T, R> f);

    /**
     * Applies the function `f` of type <i>(T -&gt; set&lt;R&gt;)</i> to all the elements in the set and returns the resultant flattened set. Function application is <i>lazy</i><br>
     * Detailed Description: <i>flatmap</i>-ing `f` on set <code>[1, 2, 3]</code> will return a set <code>[f(1), f(2), f(3)]</code>.
     * As it can be seen that the function `f` is not applied immediately which makes <code>flatmap</code> a <i>lazy</i> implementation.
     * The function `f` is not applied to the elements until a <i>eager</i> api is called. Therefore, calling <code>flatmap</code> has no effect until a <i>eager</i> api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant set after applying `f` to all the set elements
     */
    public abstract <R> MutableSet<R> flatmap(Function<T, ? extends Set<R>> f);
    /* ------------------- END: Lazy methods ------------------- */

    /**
     * @return <code>true</code> if all the lazy functions (if any) are applied otherwise <code>false</code>
     */
    @Override
    public boolean isApplied() {
        return set != null;
    }

    @Override
    public MutableSet<T> applied() {
        apply();
        return this;
    }

    @Override
    public boolean add(T t) {
        apply();
        return set.add(t);
    }

    @Override
    public MutableSet<T> include(T val) {
        add(val);
        return this;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        apply();
        return set.addAll(c);
    }

    @Override
    public MutableSet<T> include(Collection<? extends T> c) {
        addAll(c);
        return this;
    }

    @Override
    public boolean remove(Object o) {
        apply();
        return set.remove(o);
    }

    @Override
    public MutableSet<T> delete(T val) {
        remove(val);
        return this;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        apply();
        return set.removeAll(c);
    }

    @Override
    public MutableSet<T> delete(Collection<? extends T> c) {
        removeAll(c);
        return this;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        apply();
        return set.retainAll(c);
    }

    @Override
    public MutableSet<T> preserve(Collection<? extends T> c) {
        retainAll(c);
        return this;
    }

    @Override
    public void clear() {
        apply();
        set.clear();
    }

    @Override
    public MutableSet<T> empty() {
        clear();
        return this;
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        apply();
        return set.removeIf(filter);
    }

    public MutableSet<T> deleteIf(Condition<? super T> c) {
        removeIf(c::evaluate);
        return this;
    }

    private void apply() {
        if (!isApplied()) {
            set = appliedSet().set;
        }
    }

    abstract MutableSet<T> appliedSet();

    private static final class SetFunctor<A, T> extends MutableSet<T> implements Functor<A, T> {
        private final Set<A> src;
        private final Function<A, Data<T>> func;

        SetFunctor(Set<A> set, Producer<Set<?>> constructor, Function<A, Data<T>> f, Set<T> applied) {
            super(applied, constructor);
            this.src = set;
            this.func = f;
        }

        @Override
        MutableSet<T> instantiate(Producer<Set<?>> constructor) {
            return new SetFunctor<>(set, constructor, Data::new, set);
        }

        @Override
        public <R> MutableSet<R> map(Function<T, R> f) {
            return new SetFunctor<>(src, constructor, map(func, f), null);
        }

        @Override
        public <R> MutableSet<R> flatmap(Function<T, ? extends Set<R>> f) {
            return new SetFunctor<>(src, constructor, flatmap(func, f), null);
        }

        @Override
        public MutableSet<T> filter(Condition<T> c) {
            return new SetFunctor<>(src, constructor, filter(func, c), null);
        }

        final SetFunctor<T, T> appliedSet() {
            SetFunctor<T, T> res;
            if (set == null) {
                Set<T> r = apply(src, func, Util.cast(constructor.produce()));
                res = new SetFunctor<>(r, constructor, Data::new, r);
            } else {
                res = new SetFunctor<>(set, constructor, Data::new, set);
            }
            return res;
        }
    }
}
