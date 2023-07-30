package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ImmutableSet<T> extends FunctionalSet<T, ImmutableSet<T>> {
    private final Set<?> src;
    private final Set<T> set;
    private final Producer<Set<?>> constructor;
    private final Function<Object, ? extends Set<T>> func;
    private final boolean applied;

    private ImmutableSet(Set<T> set, Producer<Set<?>> cons) {
        this(Collections.emptySet(), set, cons, null, true);
    }
    private ImmutableSet(Set<?> src, Producer<Set<?>> cons, Function<Object, ? extends Set<T>> func) {
        this(src, Util.cast(cons.produce()), cons, func, false);
    }
    private ImmutableSet(Set<?> src, Set<T> set, Producer<Set<?>> cons, Function<Object, ? extends Set<T>> func, boolean flag) {
        this.src = src;
        this.set = set;
        this.constructor = cons;
        this.func = func;
        this.applied = flag;
    }

    public static <A> ImmutableSet<A> of(Set<A> set, Producer<Set<?>> constructor) {
        return new ImmutableSet<>(set, constructor);
    }

    /* ------------------- START: Lazy methods ------------------- */
    /**
     * Applies the function `f` of type &lt;i&gt;(T -&gt; R)&lt;/i&gt; to all the elements in the set and returns the resultant set. Function application is &lt;b&gt;lazy&lt;/b&gt;&lt;br /&gt;
     * Detailed Description: &lt;b&gt;map&lt;/b&gt;-ing `f` on set &lt;code&gt;[1, 2, 3]&lt;/code&gt; will return a set &lt;code&gt;[f(1), f(2), f(3)]&lt;/code&gt;.
     * As it can be seen that the function `f` is not applied immediately which makes &lt;code&gt;map&lt;/code&gt; a &lt;b&gt;lazy&lt;/b&gt; implementation.
     * The function `f` is not applied to the elements until a &lt;b&gt;eager&lt;/b&gt; api is called. Therefore, calling &lt;code&gt;map&lt;/code&gt; has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant set after applying `f` to all the set elements
     */
    public <R> ImmutableSet<R> map(Function<T, R> f) {
        return flatmap(f.andThen(Collections::singleton));
    }

    /**
     * Applies the function `f` of type &lt;i&gt;(T -&gt; set&lt;R&gt;)&lt;/i&gt; to all the elements in the set and returns the resultant flattened set. Function application is &lt;b&gt;lazy&lt;/b&gt;&lt;br /&gt;
     * Detailed Description: &lt;b&gt;flatmap&lt;/b&gt;-ing `f` on set &lt;code&gt;[1, 2, 3]&lt;/code&gt; will return a set &lt;code&gt;[f(1), f(2), f(3)]&lt;/code&gt;.
     * As it can be seen that the function `f` is not applied immediately which makes &lt;code&gt;flatmap&lt;/code&gt; a &lt;b&gt;lazy&lt;/b&gt; implementation.
     * The function `f` is not applied to the elements until a &lt;b&gt;eager&lt;/b&gt; api is called. Therefore, calling &lt;code&gt;flatmap&lt;/code&gt; has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant set after applying `f` to all the set elements
     */
    public <R> ImmutableSet<R> flatmap(Function<T, ? extends Set<R>> f) {
        ImmutableSet<R> res;
        if (func == null) {
            res = new ImmutableSet<>(set, constructor, f.compose(Util::cast));
        } else {
            res = new ImmutableSet<>(src, constructor, func.andThen(s -> apply(s, f.compose(Util::cast))));
        }
        return res;
    }

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the set excludes elements from the set which does not satisfy `c`. Hence the resultant set of this api only contains the elements which satisfies the condition `c`. &lt;br /&gt;
     * Function application is &lt;b&gt;lazy&lt;/b&gt; which means calling this api has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param c condition to evaluate against each element
     * @return set containing elements which satisfies the condition `c`
     */
    public ImmutableSet<T> filter(Condition<T> c) {
        return flatmap(t -> c.evaluate(t) ? Collections.singleton(t) : Collections.emptySet());
    }

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the set excludes elements from the set which satisfies `c`. Hence the resultant set of this api only contains the elements which does not satisfy the condition `c`. &lt;br /&gt;
     * Function application is &lt;b&gt;lazy&lt;/b&gt; which means calling this api has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param c condition to evaluate against each element
     * @return set containing elements which does not satisfy the condition `c`
     */
    public ImmutableSet<T> filterOut(Condition<T> c) {
        return filter(c.negate());
    }
    /* ------------------- END: Lazy methods ------------------- */

    /**
     * Function application is &lt;b&gt;eager&lt;/b&gt; i.e. it applies all the lazy functions (if any) to set elements
     * @return the underlying &lt;code&gt;set&lt;/code&gt; with all the lazy functions (if any) applied
     */
    @Override
    public Set<T> set() {
        alertIfNotApplied();
        return set;
    }

    /**
     * @return &lt;code&gt;true&lt;/code&gt; if all the lazy functions (if any) are applied otherwise &lt;code&gt;false&lt;/code&gt;
     */
    @Override
    public boolean isApplied() {
        return applied;
    }

    /**
     * Function application is &lt;b&gt;eager&lt;/b&gt; i.e. it applies all the lazy functions (if any) to set elements
     * @return &lt;code&gt;current instance&lt;/code&gt; with all the lazy functions (if any) applied
     */
    @Override
    public ImmutableSet<T> applied() {
        ImmutableSet<T> res;
        if (isApplied()) {
            res = this;
        } else {
            res = new ImmutableSet<>(apply(src, func), constructor);
        }
        return res;
    }

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the {@link #applied() applied} set and returns a &lt;code&gt;Couple&lt;/code&gt; of &lt;code&gt;ImmutableSet&lt;/code&gt;s with satisfying elements in {@link Couple#first() first} and &lt;b&gt;not&lt;/b&gt; satisfying elements in {@link Couple#second() second}
     * @param c condition based on which the elements will be segregated
     * @return &lt;code&gt;Couple&lt;/code&gt; of &lt;code&gt;ImmutableSet&lt;/code&gt;s with satisfying elements in {@link Couple#first() first} and &lt;b&gt;not&lt;/b&gt; satisfying elements in {@link Couple#second() second}
     */
    public Couple<ImmutableSet<T>, ImmutableSet<T>> split(Condition<T> c) {
        alertIfNotApplied();
        ImmutableSet<T> match = ImmutableSet.wrap(constructor);
        ImmutableSet<T> rest = ImmutableSet.wrap(constructor);
        for (T t : set) {
            if (c.evaluate(t)) {
                match.add(t);
            } else {
                rest.add(t);
            }
        }
        return Tuple.of(match, rest);
    }

    @Override
    public int size() {
        alertIfNotApplied();
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        alertIfNotApplied();
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        alertIfNotApplied();
        return set.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        alertIfNotApplied();
        return set.containsAll(c);
    }

    @Override
    public Iterator<T> iterator() {
        alertIfNotApplied();
        return set.iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        alertIfNotApplied();
        return set.spliterator();
    }

    @Override
    public Object[] toArray() {
        alertIfNotApplied();
        return set.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        alertIfNotApplied();
        return set.toArray(a);
    }

    @Override
    public boolean add(T t) {
        alertIfNotApplied("include");
        return set.add(t);
    }

    @Override
    public ImmutableSet<T> include(T val) {
        ImmutableSet<T> res = applied();
        res.add(val);
        return res;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        alertIfNotApplied("include");
        return set.addAll(c);
    }

    @Override
    public ImmutableSet<T> include(Collection<? extends T> c) {
        ImmutableSet<T> res = applied();
        res.addAll(c);
        return this;
    }

    @Override
    public boolean remove(Object o) {
        alertIfNotApplied("delete");
        return set.remove(o);
    }

    @Override
    public ImmutableSet<T> delete(T val) {
        ImmutableSet<T> res = applied();
        res.remove(val);
        return this;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        alertIfNotApplied("delete");
        return set.removeAll(c);
    }

    @Override
    public ImmutableSet<T> delete(Collection<? extends T> c) {
        ImmutableSet<T> res = applied();
        res.removeAll(c);
        return this;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        alertIfNotApplied("preserve");
        return set.retainAll(c);
    }

    @Override
    public ImmutableSet<T> preserve(Collection<? extends T> c) {
        ImmutableSet<T> res = applied();
        res.retainAll(c);
        return this;
    }

    @Override
    public void clear() {
        alertIfNotApplied();
        set.clear();
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        alertIfNotApplied("deleteIf");
        return set.removeIf(filter);
    }

    public ImmutableSet<T> deleteIf(Predicate<? super T> filter) {
        ImmutableSet<T> res = applied();
        res.removeIf(filter);
        return res;
    }

    @Override
    public Stream<T> stream() {
        alertIfNotApplied();
        return set.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        alertIfNotApplied();
        return set.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        alertIfNotApplied();
        set.forEach(action);
    }

    @Override
    public String toString() {
        return isApplied() ? set.toString() : "[?]";
    }

    @Override
    public int hashCode() {
        alertIfNotApplied();
        return set.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        alertIfNotApplied();
        if (obj instanceof FunctionalSet) {
            FunctionalSet<?, ?> fSet = Util.cast(obj);
            obj = fSet.set();
        }
        return set.equals(obj);
    }

    public ImmutableSet<T> copy() {
        alertIfNotApplied();
        ImmutableSet<T> r = new ImmutableSet<>(src, constructor, func);
        r.addAll(set);
        return r;
    }

    private void alertIfNotApplied() {
        if (!isApplied()) {
            throw new IllegalStateException("Immutable set not yet `applied`! Consider calling `applied()` before this api");
        }
    }
    private void alertIfNotApplied(String alternateApi) {
        if (!isApplied()) {
            throw new IllegalStateException("Immutable set not yet `applied`! Consider calling `applied()` before this api or `" + alternateApi + "` can be used here as an alternate.");
        }
    }

    private <R> Set<R> apply(Set<?> s, Function<Object, ? extends Set<R>> f) {
        Set<R> r = Util.cast(constructor.produce());
        for (Object o : s) {
            r.addAll(f.apply(o));
        }
        return r;
    }

    @SuppressWarnings("unchecked")
    public static <A> ImmutableSet<A> wrap(Producer<Set<?>> constructor) {
        return of((ImmutableSet<A>) constructor.produce(), constructor);
    }
}
