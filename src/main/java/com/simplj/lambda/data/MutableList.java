package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public abstract class MutableList<T> extends FunctionalList<T, MutableList<T>> implements List<T> {
    volatile List<T> list;

    MutableList(List<T> list, Producer<List<?>> constructor) {
        super(constructor);
        this.list = list;
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
     * Applies the function `f` of type <i>(T -&gt; R)</i> to all the elements in the list and returns the resultant list. Function application is <i>lazy</i><br>
     * Detailed Description: <i>map</i>-ing `f` on list <code>[1, 2, 3]</code> will return a list <code>[f(1), f(2), f(3)]</code>.
     * As it can be seen that the function `f` is not applied immediately which makes <code>map</code> a <i>lazy</i> implementation.
     * The function `f` is not applied to the elements until a <i>eager</i> api is called. Therefore, calling <code>map</code> has no effect until a <i>eager</i> api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant list after applying `f` to all the list elements
     */
    public abstract <R> MutableList<R> map(Function<T, R> f);

    /**
     * Applies the function `f` of type <i>(T -&gt; List&lt;R&gt;)</i> to all the elements in the list and returns the resultant flattened list. Function application is <i>lazy</i><br>
     * Detailed Description: <i>flatmap</i>-ing `f` on list <code>[1, 2, 3]</code> will return a list <code>[f(1), f(2), f(3)]</code>.
     * As it can be seen that the function `f` is not applied immediately which makes <code>flatmap</code> a <i>lazy</i> implementation.
     * The function `f` is not applied to the elements until a <i>eager</i> api is called. Therefore, calling <code>flatmap</code> has no effect until a <i>eager</i> api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant list after applying `f` to all the list elements
     */
    public abstract <R> MutableList<R> flatmap(Function<T, ? extends List<R>> f);

    /**
     * Applies the <code>Condition</code> `c` to all the elements in the list excludes elements from the list which does not satisfy `c`. Hence the resultant list of this api only contains the elements which satisfies the condition `c`. <br>
     * Function application is <i>lazy</i> which means calling this api has no effect until a <i>eager</i> api is called.
     * @param c condition to evaluate against each element
     * @return list containing elements which satisfies the condition `c`
     */
    @Override
    public abstract MutableList<T> filter(Condition<T> c);

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
    public MutableList<T> applied() {
        apply();
        return this;
    }

    public MutableList<Couple<Integer, T>> indexed() {
        return foldl(Tuple.of(0, MutableList.<Couple<Integer, T>>newInstance(constructor)), (c, v) -> Tuple.of(c.first() + 1, c.second().append(Tuple.of(c.first(), v)))).second();
    }

    @Override
    public Iterator<T> iterator() {
        apply();
        return list.iterator();
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
    public String toString() {
        return isApplied() ? list.toString() : "[?]";
    }

    private void apply() {
        if (!isApplied()) {
            list = appliedList().list;
        }
    }

    public abstract MutableList<T> appliedList();

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
        MutableList<T> instantiate(Producer<List<?>> constructor) {
            return new ListFunctor<>(list, constructor, Data::new, list);
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

        public final ListFunctor<T, T> appliedList() {
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
