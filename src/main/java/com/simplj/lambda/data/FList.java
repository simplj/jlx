package com.simplj.lambda.data;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

abstract class FList<T, L extends FList<T, L>> implements Iterable<T> {
    final Producer<List<?>> constructor;

    FList(Producer<List<?>> constructor) {
        this.constructor = constructor;
    }

    abstract L instantiate(Producer<List<?>> constructor, List<T> listVal);

    /**
     * Function application is <i>eager</i> i.e. it applies all the lazy functions (if any) to list elements
     * @return the underlying <code>list</code> with all the lazy functions (if any) applied
     * @throws IllegalStateException if not {@link #applied() applied}
     */
    public abstract List<T> list();

    /**
     * Applies the <code>Condition</code> `c` to all the elements in the list excludes elements from the list which does not satisfy `c`. Hence the resultant list of this api only contains the elements which satisfies the condition `c`.<br>
     * Function application is <i>lazy</i> which means calling this api has no effect until a <i>eager</i> api is called.
     * @param c condition to evaluate against each element
     * @return list containing elements which satisfies the condition `c`
     */
    public abstract L filter(Condition<T> c);

    /**
     * Applies the <code>Condition</code> `c` to all the elements in the list excludes elements from the list which satisfies `c`. Hence the resultant list of this api only contains the elements which does not satisfy the condition `c`.<br>
     * Function application is <i>lazy</i> which means calling this api has no effect until a <i>eager</i> api is called.
     * @param c condition to evaluate against each element
     * @return list containing elements which does not satisfy the condition `c`
     */
    public L filterOut(Condition<T> c) {
        return filter(c.negate());
    }

    /**
     * @return <code>true</code> if all the lazy functions (if any) are applied otherwise <code>false</code>
     */
    public abstract boolean isApplied();

    /**
     * Function application is <i>eager</i> i.e. it applies all the lazy functions (if any) to list elements
     * @return <code>current instance</code> if already <code>applied</code> otherwise a <code>new instance</code> with all the lazy functions applied
     */
    public abstract L applied();

    /**
     * Applies the <code>Condition</code> `c` to all the elements in the {@link #applied() applied} list and returns a <code>Couple</code> of <code>ImmutableList</code>s with satisfying elements in {@link Couple#first() first} and <i>not</i> satisfying elements in {@link Couple#second() second}
     * @param c condition based on which the elements will be segregated
     * @return <code>Couple</code> of <code>ImmutableList</code>s with satisfying elements in {@link Couple#first() first} and <i>not</i> satisfying elements in {@link Couple#second() second}
     * @throws IllegalStateException if not {@link #applied() applied}
     */
    public Couple<L, L> split(Condition<T> c) {
        List<T> match = Util.cast(constructor.produce());
        List<T> rest = Util.cast(constructor.produce());
        List<T> l = list();
        for (T t : l) {
            if (c.evaluate(t)) {
                match.add(t);
            } else {
                rest.add(t);
            }
        }
        return Tuple.of(instantiate(constructor, match), instantiate(constructor, rest));
    }

    /**
     * Returns the first element from the current list or throws `IndexOutOfBoundsException` if empty.
     * @return the first element from the current list or throws `IndexOutOfBoundsException` if empty.
     */
    public T first() {
        List<T> l = list();
        if (l.isEmpty()) {
            throw new IndexOutOfBoundsException("List is empty!");
        }
        return l.get(0);
    }

    /**
     * Returns the middle element from the current list or throws `IndexOutOfBoundsException` if empty.
     * @return the middle element from the current list or throws `IndexOutOfBoundsException` if empty.
     */
    public T mid() {
        List<T> l = list();
        if (l.isEmpty()) {
            throw new IndexOutOfBoundsException("List is empty!");
        }
        return l.get(size() / 2);
    }

    /**
     * Returns the last element from the current list or throws `IndexOutOfBoundsException` if empty.
     * @return the last element from the current list or throws `IndexOutOfBoundsException` if empty.
     */
    public T last() {
        List<T> l = list();
        if (l.isEmpty()) {
            throw new IndexOutOfBoundsException("List is empty!");
        }
        return l.get(l.size() - 1);
    }

    public int size() {
        return list().size();
    }
    public boolean isEmpty() {
        return list().isEmpty();
    }
    public boolean contains(Object o) {
        return list().contains(o);
    }
    public boolean containsAll(Collection<?> c) {
        return list().containsAll(c);
    }
    public Object[] toArray() {
        return list().toArray();
    }
    public <T1> T1[] toArray(T1[] a) {
        return list().toArray(a);
    }

    public boolean hasAll(Iterable<? extends T> c) {
        List<T> l = list();
        for (T e : c) {
            if (!l.contains(e)) {
                return false;
            }
        }
        return true;
    }

    public abstract L append(T val);

    public abstract L append(Iterable<? extends T> c);

    public abstract L insert(int index, T val);

    public abstract L insert(int index, Iterable<? extends T> c);

    public abstract L replace(int index, T val);

    public abstract L delete(int index);

    public abstract L delete(T val);

    public abstract L delete(Iterable<? extends T> c);

    public abstract L preserve(Iterable<? extends T> c);

    public L empty() {
        return instantiate(constructor, Collections.emptyList());
    }

    public T get(int index) {
        return list().get(index);
    }

    /**
     * drops `n` elements from first (if n is positive) or from last (if n is negative)
     * @param n elements to drop either from first (if n is positive) or from last (if n is negative)
     * @return Resultant list with n elements dropped.
     */
    public L drop(int n) {
        List<T> list = list();
        if (n == 0) {
            return instantiate(constructor, list);
        } else if (n >= list.size()) {
            return empty();
        }
        List<T> res = Util.cast(constructor.produce());
        if (n > 0) {
            while (n < list.size()) {
                res.add(list.get(n++));
            }
        } else {
            int l = list.size() + n;
            for (int i = 0; i < l; i++) {
                res.add(list.get(i));
            }
        }
        return instantiate(constructor, res);
    }

    public L dropWhile(Condition<T> c) {
        if (isEmpty()) {
            return empty();
        }
        int n = 0;
        List<T> list = list();
        while (n < list.size() && c.evaluate(list.get(n))) {
            n++;
        }
        List<T> res = Util.cast(constructor.produce());
        while (n < list.size()) {
            res.add(list.get(n++));
        }
        return instantiate(constructor, res);
    }

    public L dropUntil(Condition<T> c) {
        return dropWhile(c.negate());
    }

    /**
     * takes `n` elements from first (if n is positive) or from last (if n is negative)
     * @param n elements to take either from first (if n is positive) or from last (if n is negative)
     * @return Resultant list with only n elements taken.
     */
    public L take(int n) {
        List<T> list = list();
        if (n == 0) {
            return empty();
        } else if (n >= list.size()) {
            return instantiate(constructor, list);
        }
        List<T> res = Util.cast(constructor.produce());
        int i = 0;
        if (n > 0) {
            while (i < n) {
                res.add(list.get(i++));
            }
        } else {
            n = list.size() + n;
            while (n < list.size()) {
                res.add(list.get(n++));
            }
        }
        return instantiate(constructor, res);
    }

    public L takeWhile(Condition<T> c) {
        if (isEmpty()) {
            return empty();
        }
        int n = 0;
        List<T> list = list();
        while (n < list.size() && c.evaluate(list.get(n))) {
            n++;
        }
        L res;
        if (n == 0) {
            res = empty();
        } else if (n < list.size()) {
            List<T> r = Util.cast(constructor.produce());
            int i = 0;
            while (i < n) {
                r.add(list.get(i++));
            }
            res = instantiate(constructor, r);
        } else {
            res = instantiate(constructor, list);
        }
        return res;
    }

    public L takeUntil(Condition<T> c) {
        return takeWhile(c.negate());
    }

    public int indexOf(Object o) {
        return list().indexOf(o);
    }
    public int lastIndexOf(Object o) {
        return list().lastIndexOf(o);
    }
    public ListIterator<T> listIterator() {
        return list().listIterator();
    }
    public ListIterator<T> listIterator(int index) {
        return list().listIterator(index);
    }
    @Override
    public Iterator<T> iterator() {
        return list().iterator();
    }
    @Override
    public Spliterator<T> spliterator() {
        return list().spliterator();
    }
    public List<T> subList(int fromIndex, int toIndex) {
        return list().subList(fromIndex, toIndex);
    }
    public abstract L sorted(Comparator<? super T> c);
    public abstract L replacingAll(UnaryOperator<T> operator);
    public abstract L deleteIf(Condition<? super T> filter);

    public Stream<T> stream() {
        return list().stream();
    }
    public Stream<T> parallelStream() {
        return list().parallelStream();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        list().forEach(action);
    }

    public T find(Condition<T> c) {
        return Util.find(list(), c);
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

    public <R> R foldl(R identity, BiFunction<R, T, R> accumulator) {
        List<T> l = list();
        for (T t : l) {
            identity = accumulator.apply(identity, t);
        }
        return identity;
    }

    public <R> R foldr(R origin, BiFunction<T, R, R> accumulator) {
        List<T> l = list();
        int idx = l.size() - 1;
        while (idx >= 0) {
            origin = accumulator.apply(l.get(idx), origin);
            idx--;
        }
        return origin;
    }

    public T reduceL(BiFunction<T, T, T> accumulator) {
        T res = null;
        List<T> l = list();
        if (!l.isEmpty()) {
            res = l.get(0);
            for (int idx = l.size(), i = 1; i < idx; i++) {
                res = accumulator.apply(res, l.get(i));
            }
        }
        return res;
    }

    public T reduceR(BiFunction<T, T, T> accumulator) {
        T res = null;
        List<T> l = list();
        if (!l.isEmpty()) {
            int idx = l.size() - 1;
            res = l.get(idx);
            idx -= 1;
            while (idx >= 0) {
                res = accumulator.apply(l.get(idx), res);
                idx--;
            }
        }
        return res;
    }

    @Override
    public String toString() {
        return isApplied() ? list().toString() : "[?]";
    }

    @Override
    public int hashCode() {
        return list().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FList) {
            FList<?, ?> fList = Util.cast(obj);
            obj = fList.list();
        }
        return list().equals(obj);
    }

    public L copy() {
        List<T> r = Util.cast(constructor.produce());
        r.addAll(list());
        return instantiate(constructor, r);
    }
}
