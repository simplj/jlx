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

public abstract class ImmutableSet<T> extends FunctionalSet<T, ImmutableSet<T>> {
    final Set<T> set;
    final Producer<Set<?>> constructor;

    public ImmutableSet(Set<T> set, Producer<Set<?>> constructor) {
        this.set = set;
        this.constructor = constructor;
    }

    public static <A> ImmutableSet<A> unit() {
        return unit(HashSet::new);
    }

    public static <A> ImmutableSet<A> unit(Producer<Set<?>> constructor) {
        return of(Util.cast(constructor.produce()), constructor);
    }

    public static <A> ImmutableSet<A> of(Set<A> set) {
        return of(set, HashSet::new);
    }

    public static <A> ImmutableSet<A> of(Set<A> set, Producer<Set<?>> constructor) {
        return new SetFunctor<>(set, constructor, Data::new, set);
    }

    /**
     * Function application is &lt;b&gt;eager&lt;/b&gt; i.e. it applies all the lazy functions (if any) to set elements
     * @return the underlying &lt;code&gt;set&lt;/code&gt; with all the lazy functions (if any) applied
     */
    @Override
    public final Set<T> set() {
        return applied().set;
    }

    /* ------------------- START: Lazy methods ------------------- */
    /**
     * Applies the function `f` of type &lt;i&gt;(T -&gt; R)&lt;/i&gt; to all the elements in the set and returns the resultant set. Function application is &lt;b&gt;lazy&lt;/b&gt;<br>
     * Detailed Description: &lt;b&gt;map&lt;/b&gt;-ing `f` on set &lt;code&gt;[1, 2, 3]&lt;/code&gt; will return a set &lt;code&gt;[f(1), f(2), f(3)]&lt;/code&gt;.
     * As it can be seen that the function `f` is not applied immediately which makes &lt;code&gt;map&lt;/code&gt; a &lt;b&gt;lazy&lt;/b&gt; implementation.
     * The function `f` is not applied to the elements until a &lt;b&gt;eager&lt;/b&gt; api is called. Therefore, calling &lt;code&gt;map&lt;/code&gt; has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant set after applying `f` to all the set elements
     */
    public abstract <R> ImmutableSet<R> map(Function<T, R> f);

    /**
     * Applies the function `f` of type &lt;i&gt;(T -&gt; set&lt;R&gt;)&lt;/i&gt; to all the elements in the set and returns the resultant flattened set. Function application is &lt;b&gt;lazy&lt;/b&gt;<br>
     * Detailed Description: &lt;b&gt;flatmap&lt;/b&gt;-ing `f` on set &lt;code&gt;[1, 2, 3]&lt;/code&gt; will return a set &lt;code&gt;[f(1), f(2), f(3)]&lt;/code&gt;.
     * As it can be seen that the function `f` is not applied immediately which makes &lt;code&gt;flatmap&lt;/code&gt; a &lt;b&gt;lazy&lt;/b&gt; implementation.
     * The function `f` is not applied to the elements until a &lt;b&gt;eager&lt;/b&gt; api is called. Therefore, calling &lt;code&gt;flatmap&lt;/code&gt; has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant set after applying `f` to all the set elements
     */
    public abstract <R> ImmutableSet<R> flatmap(Function<T, ? extends Set<R>> f);

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the set excludes elements from the set which does not satisfy `c`. Hence the resultant set of this api only contains the elements which satisfies the condition `c`. <br>
     * Function application is &lt;b&gt;lazy&lt;/b&gt; which means calling this api has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param c condition to evaluate against each element
     * @return set containing elements which satisfies the condition `c`
     */
    public abstract ImmutableSet<T> filter(Condition<T> c);

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the set excludes elements from the set which satisfies `c`. Hence the resultant set of this api only contains the elements which does not satisfy the condition `c`. <br>
     * Function application is &lt;b&gt;lazy&lt;/b&gt; which means calling this api has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param c condition to evaluate against each element
     * @return set containing elements which does not satisfy the condition `c`
     */
    public ImmutableSet<T> filterOut(Condition<T> c) {
        return filter(c.negate());
    }
    /* ------------------- END: Lazy methods ------------------- */

    /**
     * @return &lt;code&gt;true&lt;/code&gt; if all the lazy functions (if any) are applied otherwise &lt;code&gt;false&lt;/code&gt;
     */
    @Override
    public boolean isApplied() {
        return set != null;
    }

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the {@link #applied() applied} set and returns a &lt;code&gt;Couple&lt;/code&gt; of &lt;code&gt;ImmutableSet&lt;/code&gt;s with satisfying elements in {@link Couple#first() first} and &lt;b&gt;not&lt;/b&gt; satisfying elements in {@link Couple#second() second}
     * @param c condition based on which the elements will be segregated
     * @return &lt;code&gt;Couple&lt;/code&gt; of &lt;code&gt;ImmutableSet&lt;/code&gt;s with satisfying elements in {@link Couple#first() first} and &lt;b&gt;not&lt;/b&gt; satisfying elements in {@link Couple#second() second}
     */
    public Couple<ImmutableSet<T>, ImmutableSet<T>> split(Condition<T> c) {
        ImmutableSet<T> match = ImmutableSet.newInstance(constructor);
        ImmutableSet<T> rest = ImmutableSet.newInstance(constructor);
        for (T t : set) {
            if (c.evaluate(t)) {
                match.set.add(t);
            } else {
                rest.set.add(t);
            }
        }
        return Tuple.of(match, rest);
    }

    @Override
    public int size() {
        return set().size();
    }

    @Override
    public boolean isEmpty() {
        return set().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set().contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set().containsAll(c);
    }

    @Override
    public Iterator<T> iterator() {
        return set().iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return set().spliterator();
    }

    @Override
    public Object[] toArray() {
        return set().toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return set().toArray(a);
    }

    @Override
    public ImmutableSet<T> include(T val) {
        ImmutableSet<T> res = applied();
        res.set.add(val);
        return res;
    }

    @Override
    public ImmutableSet<T> include(Collection<? extends T> c) {
        ImmutableSet<T> res = applied();
        res.set.addAll(c);
        return this;
    }

    @Override
    public ImmutableSet<T> delete(T val) {
        ImmutableSet<T> res = applied();
        res.set.remove(val);
        return this;
    }

    @Override
    public ImmutableSet<T> delete(Collection<? extends T> c) {
        ImmutableSet<T> res = applied();
        res.set.removeAll(c);
        return this;
    }

    @Override
    public ImmutableSet<T> preserve(Collection<? extends T> c) {
        ImmutableSet<T> res = applied();
        res.set.retainAll(c);
        return this;
    }

    @Override
    public ImmutableSet<T> empty() {
        return newInstance(constructor);
    }

    public ImmutableSet<T> deleteIf(Predicate<? super T> filter) {
        ImmutableSet<T> res = applied();
        res.set.removeIf(filter);
        return res;
    }

    @Override
    public Stream<T> stream() {
        return set().stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return set().parallelStream();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        set().forEach(action);
    }

    @Override
    public String toString() {
        return isApplied() ? set().toString() : "[?]";
    }

    @Override
    public int hashCode() {
        return set().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FunctionalSet) {
            FunctionalSet<?, ?> fSet = Util.cast(obj);
            obj = fSet.set();
        }
        return set().equals(obj);
    }

    @Override
    public ImmutableSet<T> copy() {
        ImmutableSet<T> r = newInstance(constructor);
        r.set.addAll(set);
        return r;
    }

    public static <A> ImmutableSet<A> newInstance(Producer<Set<?>> constructor) {
        Set<A> set = Util.cast(constructor.produce());
        return new SetFunctor<>(set, constructor, Data::new, set);
    }

    private static final class SetFunctor<A, T> extends ImmutableSet<T> implements Functor<A, T> {
        private final Set<A> src;
        private final Function<A, Data<T>> func;

        SetFunctor(Set<A> set, Producer<Set<?>> constructor, Function<A, Data<T>> f, Set<T> applied) {
            super(applied, constructor);
            this.src = set;
            this.func = f;
        }

        @Override
        public <R> ImmutableSet<R> map(Function<T, R> f) {
            return new SetFunctor<>(src, constructor, map(func, f), null);
        }

        @Override
        public <R> ImmutableSet<R> flatmap(Function<T, ? extends Set<R>> f) {
            return new SetFunctor<>(src, constructor, flatmap(func, f), null);
        }

        @Override
        public ImmutableSet<T> filter(Condition<T> c) {
            return new SetFunctor<>(src, constructor, filter(func, c), null);
        }

        public final SetFunctor<T, T> applied() {
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
