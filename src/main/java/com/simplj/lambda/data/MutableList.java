package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public abstract class MutableList<T> extends FunctionalList<T, MutableList<T>> implements List<T> {
    List<T> list;
    final Producer<List<?>> constructor;

    MutableList(List<T> list, Producer<List<?>> constructor) {
        this.list = list;
        this.constructor = constructor;
    }

    public static <A> MutableList<A> unit() {
        return unit(LinkedList::new);
    }

    public static <A> MutableList<A> unit(Producer<List<?>> constructor) {
        return of(Util.cast(constructor.produce()), constructor);
    }

    public static <A> MutableList<A> of(List<A> list) {
        return of(list, LinkedList::new);
    }

    public static <A> MutableList<A> of(List<A> list, Producer<List<?>> constructor) {
        return new ListFunctor<>(list, constructor, Data::new, list);
    }

    /* ------------------- START: Lazy methods ------------------- */
    /**
     * Applies the function `f` of type &lt;i&gt;(T -&gt; R)&lt;/i&gt; to all the elements in the list and returns the resultant list. Function application is &lt;b&gt;lazy&lt;/b&gt;&lt;br /&gt;
     * Detailed Description: &lt;b&gt;map&lt;/b&gt;-ing `f` on list &lt;code&gt;[1, 2, 3]&lt;/code&gt; will return a list &lt;code&gt;[f(1), f(2), f(3)]&lt;/code&gt;.
     * As it can be seen that the function `f` is not applied immediately which makes &lt;code&gt;map&lt;/code&gt; a &lt;b&gt;lazy&lt;/b&gt; implementation.
     * The function `f` is not applied to the elements until a &lt;b&gt;eager&lt;/b&gt; api is called. Therefore, calling &lt;code&gt;map&lt;/code&gt; has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant list after applying `f` to all the list elements
     */
    public abstract <R> MutableList<R> map(Function<T, R> f);

    /**
     * Applies the function `f` of type &lt;i&gt;(T -&gt; List&lt;R&gt;)&lt;/i&gt; to all the elements in the list and returns the resultant flattened list. Function application is &lt;b&gt;lazy&lt;/b&gt;&lt;br /&gt;
     * Detailed Description: &lt;b&gt;flatmap&lt;/b&gt;-ing `f` on list &lt;code&gt;[1, 2, 3]&lt;/code&gt; will return a list &lt;code&gt;[f(1), f(2), f(3)]&lt;/code&gt;.
     * As it can be seen that the function `f` is not applied immediately which makes &lt;code&gt;flatmap&lt;/code&gt; a &lt;b&gt;lazy&lt;/b&gt; implementation.
     * The function `f` is not applied to the elements until a &lt;b&gt;eager&lt;/b&gt; api is called. Therefore, calling &lt;code&gt;flatmap&lt;/code&gt; has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant list after applying `f` to all the list elements
     */
    public abstract <R> MutableList<R> flatmap(Function<T, ? extends List<R>> f);

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the list excludes elements from the list which does not satisfy `c`. Hence the resultant list of this api only contains the elements which satisfies the condition `c`. &lt;br /&gt;
     * Function application is &lt;b&gt;lazy&lt;/b&gt; which means calling this api has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param c condition to evaluate against each element
     * @return list containing elements which satisfies the condition `c`
     */
    @Override
    public abstract MutableList<T> filter(Condition<T> c);

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the list excludes elements from the list which satisfies `c`. Hence the resultant list of this api only contains the elements which does not satisfy the condition `c`. &lt;br /&gt;
     * Function application is &lt;b&gt;lazy&lt;/b&gt; which means calling this api has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param c condition to evaluate against each element
     * @return list containing elements which does not satisfy the condition `c`
     */
    @Override
    public MutableList<T> filterOut(Condition<T> c) {
        return filter(c.negate());
    }
    /* ------------------- END: Lazy methods ------------------- */

    @Override
    public final List<T> list() {
        apply();
        return list;
    }

    @Override
    public boolean isApplied() {
        return list != null;
    }

    @Override
    public Couple<MutableList<T>, MutableList<T>> split(Condition<T> c) {
        MutableList<T> match = MutableList.newInstance(constructor);
        MutableList<T> rest = MutableList.newInstance(constructor);
        MutableList<T> l = applied();
        for (T t : l) {
            if (c.evaluate(t)) {
                match.list.add(t);
            } else {
                rest.list.add(t);
            }
        }
        return Tuple.of(match, rest);
    }

    public MutableList<Couple<Integer, T>> indexedList() {
        return foldl(Tuple.of(0, MutableList.<Couple<Integer, T>>newInstance(constructor)), (c, v) -> Tuple.of(c.first() + 1, c.second().append(Tuple.of(c.first(), v)))).second();
    }

    @Override
    public int size() {
        apply();
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        apply();
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        apply();
        return list.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        apply();
        return list.containsAll(c);
    }

    @Override
    public Iterator<T> iterator() {
        apply();
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        apply();
        return list.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        apply();
        return list.toArray(a);
    }

    @Override
    public boolean add(T t) {
        apply();
        return list.add(t);
    }

    @Override
    public MutableList<T> append(T val) {
        add(val);
        return this;
    }

    @Override
    public void add(int index, T element) {
        apply();
        list.add(index, element);
    }

    @Override
    public MutableList<T> insert(int index, T val) {
        add(index, val);
        return this;
    }

    @Override
    public T set(int index, T element) {
        apply();
        return list.set(index, element);
    }

    @Override
    public MutableList<T> replace(int index, T val) {
        set(index, val);
        return this;
    }

    @Override
    public T remove(int index) {
        apply();
        return list.remove(index);
    }

    @Override
    public MutableList<T> delete(int index) {
        remove(index);
        return this;
    }

    @Override
    public boolean remove(Object o) {
        apply();
        return list.remove(o);
    }

    @Override
    public MutableList<T> delete(T val) {
        remove(val);
        return this;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        apply();
        return list.addAll(c);
    }

    @Override
    public MutableList<T> append(Collection<? extends T> c) {
        addAll(c);
        return this;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        apply();
        return list.addAll(index, c);
    }

    @Override
    public MutableList<T> insert(int index, Collection<? extends T> c) {
        addAll(index, c);
        return this;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        apply();
        return list.removeAll(c);
    }

    @Override
    public MutableList<T> delete(Collection<? extends T> val) {
        removeAll(val);
        return this;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        apply();
        return list.retainAll(c);
    }

    @Override
    public MutableList<T> preserve(Collection<? extends T> c) {
        retainAll(c);
        return this;
    }

    @Override
    public void clear() {
        apply();
        list.clear();
    }

    @Override
    public MutableList<T> empty() {
        clear();
        return this;
    }

    @Override
    public T get(int index) {
        apply();
        return list.get(index);
    }

    @Override
    public int indexOf(Object o) {
        apply();
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        apply();
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        apply();
        return list.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        apply();
        return list.listIterator(index);
    }

    @Override
    public Spliterator<T> spliterator() {
        apply();
        return list.spliterator();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        apply();
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public void sort(Comparator<? super T> c) {
        apply();
        list.sort(c);
    }

    public MutableList<T> sorted(Comparator<? super T> c) {
        sort(c);
        return this;
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        apply();
        list.replaceAll(operator);
    }

    public MutableList<T> replacingAll(UnaryOperator<T> operator) {
        replaceAll(operator);
        return this;
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        apply();
        return list.removeIf(filter);
    }

    public MutableList<T> deleteIf(Predicate<? super T> filter) {
        removeIf(filter);
        return this;
    }

    @Override
    public Stream<T> stream() {
        apply();
        return list.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        apply();
        return list.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        apply();
        list.forEach(action);
    }

    @Override
    public String toString() {
        return isApplied() ? list.toString() : "[?]";
    }

    @Override
    public int hashCode() {
        apply();
        return list.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        apply();
        if (obj instanceof FunctionalList) {
            FunctionalList<?, ?> fList = Util.cast(obj);
            obj = fList.list();
        }
        return list.equals(obj);
    }

    @Override
    public MutableList<T> copy() {
        MutableList<T> r = newInstance(constructor);
        r.list.addAll(list);
        return r;
    }

    private void apply() {
        if (!isApplied()) {
            list = applied().list;
        }
    }

    private static <A> MutableList<A> newInstance(Producer<List<?>> constructor) {
        List<A> list = Util.cast(constructor.produce());
        return new ListFunctor<>(list, constructor, Data::new, list);
    }

    private static final class ListFunctor<A, T> extends MutableList<T> implements Functor<A, T> {
        private final List<A> src;
        private final Function<A, Data<T>> func;

        ListFunctor(List<A> list, Producer<List<?>> constructor, Function<A, Data<T>> f, List<T> applied) {
            super(applied, constructor);
            this.src = list;
            this.func = f;
        }

        @Override
        public <R> MutableList<R> map(Function<T, R> f) {
            return new ListFunctor<>(src, constructor, map(func, f), null);
        }

        @Override
        public <R> MutableList<R> flatmap(Function<T, ? extends List<R>> f) {
            return new ListFunctor<>(src, constructor, flatmap(func, f), null);
        }

        @Override
        public MutableList<T> filter(Condition<T> c) {
            return new ListFunctor<>(src, constructor, filter(func, c), null);
        }

        public final ListFunctor<T, T> applied() {
            ListFunctor<T, T> res;
            if (list == null) {
                List<T> r = apply(src, func, Util.cast(constructor.produce()));
                res = new ListFunctor<>(r, constructor, Data::new, r);
            } else {
                res = new ListFunctor<>(list, constructor, Data::new, list);
            }
            return res;
        }
    }
}

