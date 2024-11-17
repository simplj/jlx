package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class MList<T> extends FList<T, MList<T>> implements List<T> {
    private static final MList<?> NONE = MList.unit(Collections::emptyList);
    volatile List<T> list;

    MList(List<T> list, Producer<List<?>> constructor) {
        super(constructor);
        this.list = list;
    }

    public static <A> MList<A> none() {
        return Util.cast(NONE);
    }

    public static <E> MList<E> unit() {
        return unit(LinkedList::new);
    }

    public static <E> MList<E> unit(Producer<List<?>> constructor) {
        return of(Util.cast(constructor.produce()), constructor);
    }

    @SafeVarargs
    public static <E> MList<E> of(E...elems) {
        return of(Stream.of(elems).collect(Collectors.toList()));
    }

    public static <E> MList<E> of(List<E> list) {
        return of(list, LinkedList::new);
    }

    public static <E> MList<E> of(List<E> list, Producer<List<?>> constructor) {
        return new ListFunctor<>(list, constructor, LinkedUnit::new, list);
    }

    public static <E> MList<E> from(Iterable<E> iter) {
        List<E> list = new LinkedList<>();
        iter.forEach(list::add);
        return of(list, LinkedList::new);
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
    public abstract <R> MList<R> map(Function<T, R> f);

    /**
     * Applies the function `f` of type <i>(T -&gt; List&lt;R&gt;)</i> to all the elements in the list and returns the resultant flattened list. Function application is <i>lazy</i><br>
     * Detailed Description: <i>flatmap</i>-ing `f` on list <code>[1, 2, 3]</code> will return a list <code>[f(1), f(2), f(3)]</code>.
     * As it can be seen that the function `f` is not applied immediately which makes <code>flatmap</code> a <i>lazy</i> implementation.
     * The function `f` is not applied to the elements until a <i>eager</i> api is called. Therefore, calling <code>flatmap</code> has no effect until a <i>eager</i> api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant list after applying `f` to all the list elements
     */
    public abstract <R> MList<R> flatmap(Function<T, ? extends List<R>> f);
    /* ------------------- END: Lazy methods ------------------- */

    public IList<T> immutable() {
        return IList.of(list());
    }

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
    public MList<T> applied() {
        apply();
        return this;
    }

    public MList<Couple<Integer, T>> indexed() {
        return foldl(Tuple.of(0, MList.<Couple<Integer, T>>unit(constructor)), (c, v) -> Tuple.of(c.first() + 1, c.second().append(Tuple.of(c.first(), v)))).second();
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
    public MList<T> append(T val) {
        add(val);
        return this;
    }

    @Override
    public void add(int index, T element) {
        apply();
        list.add(index, element);
    }

    @Override
    public MList<T> insert(int index, T val) {
        add(index, val);
        return this;
    }

    @Override
    public T set(int index, T element) {
        apply();
        return list.set(index, element);
    }

    @Override
    public MList<T> replace(int index, T val) {
        set(index, val);
        return this;
    }

    @Override
    public T remove(int index) {
        apply();
        return list.remove(index);
    }

    @Override
    public MList<T> delete(int index) {
        remove(index);
        return this;
    }

    @Override
    public boolean remove(Object o) {
        apply();
        return list.remove(o);
    }

    @Override
    public MList<T> delete(T val) {
        remove(val);
        return this;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        apply();
        return list.addAll(c);
    }

    @Override
    public MList<T> append(Iterable<? extends T> c) {
        apply();
        for (T t : c) {
            list.add(t);
        }
        return this;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        apply();
        return list.addAll(index, c);
    }

    @Override
    public MList<T> insert(int index, Iterable<? extends T> c) {
        apply();
        int i = index;
        for (T t : c) {
            list.add(i, t);
            i++;
        }
        return this;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        apply();
        return list.removeAll(c);
    }

    @Override
    public MList<T> delete(Iterable<? extends T> val) {
        apply();
        for (T t : val) {
            list.remove(t);
        }
        return this;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        apply();
        return list.retainAll(c);
    }

    @Override
    public MList<T> preserve(Iterable<? extends T> c) {
        apply();
        for (T t : c) {
            if (!list.contains(t)) {
                list.remove(t);
            }
        }
        return this;
    }

    @Override
    public void clear() {
        apply();
        list.clear();
    }

    @Override
    public void sort(Comparator<? super T> c) {
        apply();
        list.sort(c);
    }

    public MList<T> sorted(Comparator<? super T> c) {
        sort(c);
        return this;
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        apply();
        list.replaceAll(operator);
    }

    public MList<T> replacingAll(UnaryOperator<T> operator) {
        replaceAll(operator);
        return this;
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        apply();
        return list.removeIf(filter);
    }

    public MList<T> deleteIf(Condition<? super T> c) {
        removeIf(c::evaluate);
        return this;
    }

    private void apply() {
        if (!isApplied()) {
            list = appliedList().list;
        }
    }

    abstract MList<T> appliedList();

    private static final class ListFunctor<A, T> extends MList<T> implements Functor<A, T> {
        private final List<A> src;
        private final Function<A, LinkedUnit<T>> func;

        ListFunctor(List<A> list, Producer<List<?>> constructor, Function<A, LinkedUnit<T>> f, List<T> applied) {
            super(applied, constructor);
            this.src = list;
            this.func = f;
        }

        @Override
        MList<T> instantiate(Producer<List<?>> constructor, List<T> listVal) {
            return new ListFunctor<>(listVal, constructor, LinkedUnit::new, listVal);
        }

        @Override
        public <R> MList<R> map(Function<T, R> f) {
            return new ListFunctor<>(src, constructor, map(func, f), null);
        }

        @Override
        public <R> MList<R> flatmap(Function<T, ? extends List<R>> f) {
            return new ListFunctor<>(src, constructor, flatmap(func, f), null);
        }

        @Override
        public MList<T> filter(Condition<T> c) {
            return new ListFunctor<>(src, constructor, filter(func, c), null);
        }

        final ListFunctor<T, T> appliedList() {
            ListFunctor<T, T> res;
            if (list == null) {
                List<T> r = apply(src, func, Util.cast(constructor.produce()));
                res = new ListFunctor<>(r, constructor, LinkedUnit::new, r);
            } else {
                res = new ListFunctor<>(list, constructor, LinkedUnit::new, list);
            }
            return res;
        }
    }
}
