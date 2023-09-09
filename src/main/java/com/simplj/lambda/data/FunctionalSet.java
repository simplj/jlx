package com.simplj.lambda.data;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

abstract class FunctionalSet<T, S extends FunctionalSet<T, S>> implements Iterable<T> {
    final Producer<Set<?>> constructor;

    FunctionalSet(Producer<Set<?>> constructor) {
        this.constructor = constructor;
    }

    abstract S instantiate(Producer<Set<?>> constructor);

    public abstract Set<T> set();

    /**
     * Applies the <code>Condition</code> `c` to all the elements in the set excludes elements from the set which does not satisfy `c`. Hence the resultant set of this api only contains the elements which satisfies the condition `c`.<br>
     * Function application is <i>lazy</i> which means calling this api has no effect until a <i>eager</i> api is called.
     * @param c condition to evaluate against each element
     * @return set containing elements which satisfies the condition `c`
     */
    public abstract S filter(Condition<T> c);

    public S filterOut(Condition<T> c) {
        return filter(c.negate());
    }

    public abstract boolean isApplied();

    /**
     * Function application is <i>eager</i> i.e. it applies all the lazy functions (if any) to set elements
     * @return <code>current instance</code> with all the lazy functions (if any) applied
     */
    public abstract S applied();

    /**
     * Applies the <code>Condition</code> `c` to all the elements in the {@link #applied() applied} set and returns a <code>Couple</code> of <code>ImmutableSet</code>s with satisfying elements in {@link Couple#first() first} and <i>not</i> satisfying elements in {@link Couple#second() second}
     * @param c condition based on which the elements will be segregated
     * @return <code>Couple</code> of <code>ImmutableSet</code>s with satisfying elements in {@link Couple#first() first} and <i>not</i> satisfying elements in {@link Couple#second() second}
     */
    public Couple<S, S> split(Condition<T> c) {
        S match = instantiate(constructor);
        S rest = instantiate(constructor);
        S l = applied();
        for (T t : l) {
            if (c.evaluate(t)) {
                match.include(t);
            } else {
                rest.include(t);
            }
        }
        return Tuple.of(match, rest);
    }

    public int size() {
        return set().size();
    }
    public boolean isEmpty() {
        return set().isEmpty();
    }
    public boolean contains(Object o) {
        return set().contains(o);
    }
    public boolean containsAll(Collection<?> c) {
        return set().containsAll(c);
    }
    public Object[] toArray() {
        return set().toArray();
    }
    public <T1> T1[] toArray(T1[] a) {
        return set().toArray(a);
    }

    public abstract S include(T val);

    public abstract S include(Collection<? extends T> c);

    public abstract S delete(T val);

    public abstract S delete(Collection<? extends T> c);

    public abstract S preserve(Collection<? extends T> c);

    public abstract S empty();

    public abstract S deleteIf(Condition<? super T> c);

    public Stream<T> stream() {
        return set().stream();
    }
    public Stream<T> parallelStream() {
        return set().parallelStream();
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
    public void forEach(Consumer<? super T> action) {
        set().forEach(action);
    }

    public T find(Condition<T> c) {
        return Util.find(set(), c);
    }

    public boolean none(Condition<T> c) {
        return null == find(c);
    }

    public boolean any(Condition<T> c) {
        return !none(c);
    }

    public boolean all(Condition<T> c) {
        return none(c.negate());
    }

    public <R> R fold(R identity, BiFunction<R, T, R> accumulator) {
        Set<T> s = set();
        for (T t : s) {
            identity = accumulator.apply(identity, t);
        }
        return identity;
    }

    public T reduce(BiFunction<T, T, T> accumulator) {
        T res = null;
        Set<T> s = set();
        boolean flag = false;
        if (!s.isEmpty()) {
            for (T t : s) {
                if (flag) {
                    res = accumulator.apply(res, t);
                } else {
                    res = t;
                    flag = true;
                }
            }
        }
        return res;
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

    public S copy() {
        S r = instantiate(constructor);
        r.include(set());
        return r;
    }
}
