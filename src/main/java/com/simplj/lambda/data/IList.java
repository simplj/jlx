package com.simplj.lambda.data;

import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

import java.util.*;
import java.util.function.UnaryOperator;

public abstract class IList<E> extends FList<E, IList<E>> {
    private static final IList<?> NONE = IList.unit(Collections::emptyList);
    final List<E> list;

    IList(List<E> list, Producer<List<?>> constructor) {
        super(constructor);
        this.list = list;
    }

    public static <A> IList<A> none() {
        return Util.cast(NONE);
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
    public abstract <R> IList<R> map(Function<E, R> f);

    /**
     * Applies the function `f` of type <i>(T -&gt; List&lt;R&gt;)</i> to all the elements in the list and returns the resultant flattened applied(). Function application is <i>lazy</i><br>
     * Detailed Description: <i>flatmap</i>-ing `f` on list <code>[1, 2, 3]</code> will return a list <code>[f(1), f(2), f(3)]</code>.
     * As it can be seen that the function `f` is not applied immediately which makes <code>flatmap</code> a <i>lazy</i> implementation.
     * The function `f` is not applied to the elements until a <i>eager</i> api is called. Therefore, calling <code>flatmap</code> has no effect until a <i>eager</i> api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant list after applying `f` to all the list elements
     */
    public abstract <R> IList<R> flatmap(Function<E, ? extends List<R>> f);
    /* ------------------- END: Lazy methods ------------------- */

    @Override
    public final List<E> list() {
        return applied().list;
    }

    @Override
    public boolean isApplied() {
        return list != null;
    }

    public final IList<E> applied() {
        return appliedList(false);
    }

    public IList<Couple<Integer, E>> indexed() {
        return foldl(Tuple.of(0, IList.<Couple<Integer, E>>unit(constructor)), (c, v) -> Tuple.of(c.first() + 1, c.second().append(Tuple.of(c.first(), v)))).second();
    }

    @Override
    public IList<E> append(E val) {
        IList<E> res = appliedList(true);
        res.list.add(val);
        return res;
    }

    @Override
    public IList<E> append(Iterable<? extends E> c) {
        IList<E> res = appliedList(true);
        for (E e : c) {
            res.list.add(e);
        }
        return res;
    }

    @Override
    public IList<E> insert(int index, E val) {
        IList<E> res = appliedList(true);
        res.list.add(index, val);
        return res;
    }

    @Override
    public IList<E> insert(int index, Iterable<? extends E> c) {
        IList<E> res = appliedList(true);
        int i = index;
        for (E e : c) {
            res.list.add(i, e);
            i++;
        }
        return res;
    }

    @Override
    public IList<E> replace(int index, E val) {
        IList<E> res = appliedList(true);
        res.list.set(index, val);
        return res;
    }

    @Override
    public IList<E> delete(int index) {
        IList<E> res = appliedList(true);
        res.list.remove(index);
        return res;
    }

    @Override
    public IList<E> delete(E val) {
        IList<E> res = appliedList(true);
        res.list.remove(val);
        return res;
    }

    @Override
    public IList<E> delete(Iterable<? extends E> c) {
        IList<E> res = appliedList(true);
        for (E e : c) {
            res.list.remove(e);
        }
        return res;
    }

    @Override
    public IList<E> preserve(Iterable<? extends E> c) {
        IList<E> res = appliedList(true);
        for (E e : c) {
            if (!res.list.contains(e)) {
                res.list.remove(e);
            }
        }
        return res;
    }

    @Override
    public IList<E> empty() {
        return unit(constructor);
    }

    @Override
    public IList<E> sorted(Comparator<? super E> c) {
        IList<E> res = appliedList(true);
        res.list.sort(c);
        return res;
    }

    @Override
    public IList<E> replacingAll(UnaryOperator<E> operator) {
        IList<E> res = appliedList(true);
        res.list.replaceAll(operator);
        return res;
    }

    @Override
    public IList<E> deleteIf(Condition<? super E> c) {
        IList<E> res = appliedList(true);
        res.list.removeIf(c::evaluate);
        return res;
    }

    abstract IList<E> appliedList(boolean copy);

    private static final class ListFunctor<A, T> extends IList<T> implements Functor<A, T> {
        private final List<A> src;
        private final Function<A, LinkedUnit<T>> func;

        ListFunctor(List<A> list, Producer<List<?>> constructor, Function<A, LinkedUnit<T>> f, List<T> applied) {
            super(applied, constructor);
            this.src = list;
            this.func = f;
        }

        @Override
        IList<T> instantiate(Producer<List<?>> constructor, List<T> listVal) {
            return new ListFunctor<>(listVal, constructor, LinkedUnit::new, listVal);
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

        public final ListFunctor<T, T> appliedList(boolean copy) {
            ListFunctor<T, T> res;
            if (list == null) {
                List<T> r = apply(src, func, Util.cast(constructor.produce()));
                res = new ListFunctor<>(r, constructor, LinkedUnit::new, r);
            } else if (copy) {
                List<T> r = Util.cast(constructor.produce());
                r.addAll(list);
                res = new ListFunctor<>(r, constructor, LinkedUnit::new, r);
            } else {
                res = new ListFunctor<>(list, constructor, LinkedUnit::new, list);
            }
            return res;
        }
    }
}
