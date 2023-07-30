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

public class MutableSet<T> extends FunctionalSet<T, MutableSet<T>> {
    private Set<?> src;
    private Set<T> set;
    private final Producer<Set<?>> constructor;
    private Function<Object, ? extends Set<T>> func;
    private final AtomicBoolean applied;

    private MutableSet(Set<T> set, Producer<Set<?>> cons) {
        this(Collections.emptySet(), set, cons, null, new AtomicBoolean(true));
    }
    private MutableSet(Set<?> src, Producer<Set<?>> cons, Function<Object, ? extends Set<T>> func) {
        this(src, Util.cast(cons.produce()), cons, func, new AtomicBoolean(false));
    }
    private MutableSet(Set<?> src, Set<T> set, Producer<Set<?>> cons, Function<Object, ? extends Set<T>> func, AtomicBoolean flag) {
        this.src = src;
        this.set = set;
        this.constructor = cons;
        this.func = func;
        this.applied = flag;
    }

    public static <A> MutableSet<A> of(Set<A> set, Producer<Set<?>> constructor) {
        return new MutableSet<>(set, constructor);
    }

    /* ------------------- START: Lazy methods ------------------- */
    /**
     * Applies the function `f` of type <i>(T -> R)</i> to all the elements in the set and returns the resultant set. Function application is <b>lazy</b><br />
     * Detailed Description: <b>map</b>-ing `f` on set <code>[1, 2, 3]</code> will return a set <code>[f(1), f(2), f(3)]</code>.
     * As it can be seen that the function `f` is not applied immediately which makes <code>map</code> a <b>lazy</b> implementation.
     * The function `f` is not applied to the elements until a <b>eager</b> api is called. Therefore, calling <code>map</code> has no effect until a <b>eager</b> api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant set after applying `f` to all the set elements
     */
    public <R> MutableSet<R> map(Function<T, R> f) {
        return flatmap(f.andThen(Collections::singleton));
    }

    /**
     * Applies the function `f` of type <i>(T -> set&lt;R&gt;)</i> to all the elements in the set and returns the resultant flattened set. Function application is <b>lazy</b><br />
     * Detailed Description: <b>flatmap</b>-ing `f` on set <code>[1, 2, 3]</code> will return a set <code>[f(1), f(2), f(3)]</code>.
     * As it can be seen that the function `f` is not applied immediately which makes <code>flatmap</code> a <b>lazy</b> implementation.
     * The function `f` is not applied to the elements until a <b>eager</b> api is called. Therefore, calling <code>flatmap</code> has no effect until a <b>eager</b> api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant set after applying `f` to all the set elements
     */
    public <R> MutableSet<R> flatmap(Function<T, ? extends Set<R>> f) {
        MutableSet<R> res;
        if (func == null) {
            res = new MutableSet<>(set, constructor, f.compose(Util::cast));
        } else {
            Function<Object, Set<R>> that = func.andThen(s -> {
                Set<R> r = Util.cast(constructor.produce());
                for (T t : s) {
                    r.addAll(f.apply(t));
                }
                return r;
            });
            res = new MutableSet<>(src, constructor, that);
        }
        return res;
    }

    /**
     * Applies the <code>Condition</code> `c` to all the elements in the set excludes elements from the set which does not satisfy `c`. Hence the resultant set of this api only contains the elements which satisfies the condition `c`. <br />
     * Function application is <b>lazy</b> which means calling this api has no effect until a <b>eager</b> api is called.
     * @param c condition to evaluate against each element
     * @return set containing elements which satisfies the condition `c`
     */
    public MutableSet<T> filter(Condition<T> c) {
        if (func == null) {
            func = o -> {
                T t = Util.cast(o);
                return c.evaluate(t) ? Collections.singleton(t) : Collections.emptySet();
            };
        } else {
            func = func.andThen(l -> {
                Set<T> r = Util.cast(constructor.produce());
                for (T t : l) {
                    if (c.evaluate(t)) {
                        r.add(t);
                    }
                }
                return r;
            });
        }
        applied.set(false);
        return this;
    }

    /**
     * Applies the <code>Condition</code> `c` to all the elements in the set excludes elements from the set which satisfies `c`. Hence the resultant set of this api only contains the elements which does not satisfy the condition `c`. <br />
     * Function application is <b>lazy</b> which means calling this api has no effect until a <b>eager</b> api is called.
     * @param c condition to evaluate against each element
     * @return set containing elements which does not satisfy the condition `c`
     */
    public MutableSet<T> filterOut(Condition<T> c) {
        return filter(c.negate());
    }
    /* ------------------- END: Lazy methods ------------------- */

    /**
     * Function application is <b>eager</b> i.e. it applies all the lazy functions (if any) to set elements
     * @return the underlying <code>set</code> with all the lazy functions (if any) applied
     */
    @Override
    public Set<T> set() {
        apply();
        return set;
    }

    /**
     * @return <code>true</code> if all the lazy functions (if any) are applied otherwise <code>false</code>
     */
    @Override
    public boolean isApplied() {
        return applied.get();
    }

    /**
     * Function application is <b>eager</b> i.e. it applies all the lazy functions (if any) to set elements
     * @return <code>current instance</code> with all the lazy functions (if any) applied
     */
    @Override
    public MutableSet<T> applied() {
        apply();
        return this;
    }

    /**
     * Applies the <code>Condition</code> `c` to all the elements in the {@link #applied() applied} set and returns a <code>Couple</code> of <code>MutableSet</code>s with satisfying elements in {@link Couple#first() first} and <b>not</b> satisfying elements in {@link Couple#second() second}
     * @param c condition based on which the elements will be segregated
     * @return <code>Couple</code> of <code>MutableSet</code>s with satisfying elements in {@link Couple#first() first} and <b>not</b> satisfying elements in {@link Couple#second() second}
     */
    public Couple<MutableSet<T>, MutableSet<T>> split(Condition<T> c) {
        MutableSet<T> match = MutableSet.wrap(constructor);
        MutableSet<T> rest = MutableSet.wrap(constructor);
        apply();
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
        apply();
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        apply();
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        apply();
        return set.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        apply();
        return set.containsAll(c);
    }

    @Override
    public Iterator<T> iterator() {
        apply();
        return set.iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        apply();
        return set.spliterator();
    }

    @Override
    public Object[] toArray() {
        apply();
        return set.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        apply();
        return set.toArray(a);
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
    public boolean removeIf(Predicate<? super T> filter) {
        apply();
        return set.removeIf(filter);
    }

    public MutableSet<T> deleteIf(Predicate<? super T> filter) {
        removeIf(filter);
        return this;
    }

    @Override
    public Stream<T> stream() {
        apply();
        return set.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        apply();
        return set.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        apply();
        set.forEach(action);
    }

    @Override
    public String toString() {
        return isApplied() ? set.toString() : "[?]";
    }

    @Override
    public int hashCode() {
        apply();
        return set.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        apply();
        if (obj instanceof FunctionalSet) {
            FunctionalSet<?, ?> fSet = Util.cast(obj);
            obj = fSet.set();
        }
        return set.equals(obj);
    }

    public MutableSet<T> copy() {
        apply();
        MutableSet<T> r = new MutableSet<>(src, constructor, func);
        r.addAll(set);
        return r;
    }

    private void apply() {
        if (!isApplied()) {
            Set<T> r = Util.cast(constructor.produce());
            for (Object o : src) {
                r.addAll(func.apply(o));
            }
            set = r;
            src = Collections.emptySet();
            func = null;
            applied.set(true);
        }
    }

    @SuppressWarnings("unchecked")
    public static <A> MutableSet<A> wrap(Producer<Set<?>> constructor) {
        return of((MutableSet<A>) constructor.produce(), constructor);
    }
}
