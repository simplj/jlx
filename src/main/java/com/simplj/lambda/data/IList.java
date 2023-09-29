package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

import java.util.*;
import java.util.function.UnaryOperator;

public abstract class IList<T> extends FList<T, IList<T>> {
    final List<T> list;

    IList(List<T> list, Producer<List<?>> constructor) {
        super(constructor);
        this.list = list;
    }

    public static <E> IList<E> unit() {
        return unit(LinkedList::new);
    }

    public static <E> IList<E> unit(Producer<List<?>> constructor) {
        return of(Util.cast(constructor.produce()), constructor);
    }

    @SafeVarargs
    public static <E> IList<E> of(E...elems) {
        return of(Arrays.asList(elems));
    }

    public static <E> IList<E> of(List<E> list) {
        return of(list, LinkedList::new);
    }

    public static <E> IList<E> of(List<E> list, Producer<List<?>> constructor) {
        return new ListFunctor<>(list, constructor, LinkedUnit::new, list);
    }

    /* ------------------- START: Lazy methods ------------------- */
    /**
     * Applies the function `f` of type <i>(T -&gt; R)</i> to all the elements in the list and returns the resultant applied(). Function application is <i>lazy</i><br>
     * Detailed Description: <i>map</i>-ing `f` on list <code>[1, 2, 3]</code> will return a list <code>[f(1), f(2), f(3)]</code>.
     * As it can be seen that the function `f` is not applied immediately which makes <code>map</code> a <i>lazy</i> implementation.
     * The function `f` is not applied to the elements until a <i>eager</i> api is called. Therefore, calling <code>map</code> has no effect until a <i>eager</i> api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant list after applying `f` to all the list elements
     */
    public abstract <R> IList<R> map(Function<T, R> f);

    /**
     * Applies the function `f` of type <i>(T -&gt; List&lt;R&gt;)</i> to all the elements in the list and returns the resultant flattened applied(). Function application is <i>lazy</i><br>
     * Detailed Description: <i>flatmap</i>-ing `f` on list <code>[1, 2, 3]</code> will return a list <code>[f(1), f(2), f(3)]</code>.
     * As it can be seen that the function `f` is not applied immediately which makes <code>flatmap</code> a <i>lazy</i> implementation.
     * The function `f` is not applied to the elements until a <i>eager</i> api is called. Therefore, calling <code>flatmap</code> has no effect until a <i>eager</i> api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant list after applying `f` to all the list elements
     */
    public abstract <R> IList<R> flatmap(Function<T, ? extends List<R>> f);
    /* ------------------- END: Lazy methods ------------------- */

    @Override
    public final List<T> list() {
        return applied().list;
    }

    @Override
    public boolean isApplied() {
        return list != null;
    }

    public IList<Couple<Integer, T>> indexed() {
        return foldl(Tuple.of(0, IList.<Couple<Integer, T>>unit(constructor)), (c, v) -> Tuple.of(c.first() + 1, c.second().append(Tuple.of(c.first(), v)))).second();
    }

    @Override
    public IList<T> append(T val) {
        IList<T> res = applied();
        res.list.add(val);
        return res;
    }

    @Override
    public IList<T> append(Collection<? extends T> c) {
        IList<T> res = applied();
        res.list.addAll(c);
        return res;
    }

    @Override
    public IList<T> insert(int index, T val) {
        IList<T> res = applied();
        res.list.add(index, val);
        return res;
    }

    @Override
    public IList<T> insert(int index, Collection<? extends T> c) {
        IList<T> res = applied();
        res.list.addAll(index, c);
        return res;
    }

    @Override
    public IList<T> replace(int index, T val) {
        IList<T> res = applied();
        res.list.set(index, val);
        return res;
    }

    @Override
    public IList<T> delete(int index) {
        IList<T> res = applied();
        res.list.remove(index);
        return res;
    }

    @Override
    public IList<T> delete(T val) {
        IList<T> res = applied();
        res.list.remove(val);
        return res;
    }

    @Override
    public IList<T> delete(Collection<? extends T> c) {
        IList<T> res = applied();
        res.list.removeAll(c);
        return res;
    }

    @Override
    public IList<T> preserve(Collection<? extends T> c) {
        IList<T> res = applied();
        res.list.retainAll(c);
        return res;
    }

    @Override
    public IList<T> empty() {
        return unit(constructor);
    }

    @Override
    public IList<T> sorted(Comparator<? super T> c) {
        IList<T> res = applied();
        res.list.sort(c);
        return res;
    }

    @Override
    public IList<T> replacingAll(UnaryOperator<T> operator) {
        IList<T> res = applied();
        res.list.replaceAll(operator);
        return res;
    }

    @Override
    public IList<T> deleteIf(Condition<? super T> c) {
        IList<T> res = applied();
        res.list.removeIf(c::evaluate);
        return res;
    }

    private static final class ListFunctor<A, T> extends IList<T> implements Functor<A, T> {
        private final List<A> src;
        private final Function<A, LinkedUnit<T>> func;

        ListFunctor(List<A> list, Producer<List<?>> constructor, Function<A, LinkedUnit<T>> f, List<T> applied) {
            super(applied, constructor);
            this.src = list;
            this.func = f;
        }

        @Override
        IList<T> instantiate(Producer<List<?>> constructor) {
            return new ListFunctor<>(list, constructor, LinkedUnit::new, list);
        }

        @Override
        public <R> IList<R> map(Function<T, R> f) {
            return new ListFunctor<>(src, constructor, map(func, f), null);
        }

        @Override
        public <R> IList<R> flatmap(Function<T, ? extends List<R>> f) {
            return new ListFunctor<>(src, constructor, flatmap(func, f), null);
        }

        @Override
        public IList<T> filter(Condition<T> c) {
            return new ListFunctor<>(src, constructor, filter(func, c), null);
        }

        public final ListFunctor<T, T> applied() {
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