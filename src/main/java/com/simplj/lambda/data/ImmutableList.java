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

public class ImmutableList<T> extends FunctionalList<T, ImmutableList<T>> {
    private final List<?> src;
    private final List<T> list;
    private final Producer<List<?>> constructor;
    private final Function<Object, ? extends List<T>> func;
    private final boolean applied;

    private ImmutableList(List<T> list, Producer<List<?>> cons) {
        this(Collections.emptyList(), list, cons, null, true);
    }
    private ImmutableList(List<?> src, Producer<List<?>> cons, Function<Object, ? extends List<T>> func) {
        this(src, Util.cast(cons.produce()), cons, func, false);
    }
    private ImmutableList(List<?> src, List<T> list, Producer<List<?>> cons, Function<Object, ? extends List<T>> func, boolean flag) {
        this.src = src;
        this.list = list;
        this.constructor = cons;
        this.func = func;
        this.applied = flag;
    }

    public static <A> ImmutableList<A> unit(Producer<List<?>> constructor) {
        return new ImmutableList<>(Util.cast(constructor.produce()), constructor);
    }

    public static <A> ImmutableList<A> of(List<A> list, Producer<List<?>> constructor) {
        return new ImmutableList<>(list, constructor);
    }

    @Override
    final List<T> list() {
        alertIfNotApplied();
        return list;
    }

    /* ------------------- START: Lazy methods ------------------- */
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
    public <R> ImmutableList<R> map(Function<T, R> f) {
        return flatmap(f.andThen(Collections::singletonList));
    }

    /**
     * Applies the function `f` of type &lt;i&gt;(T -&gt; List&lt;R&gt;)&lt;/i&gt; to all the elements in the list and returns the resultant flattened list. Function application is &lt;b&gt;lazy&lt;/b&gt;&lt;br /&gt;
     * Detailed Description: &lt;b&gt;flatmap&lt;/b&gt;-ing `f` on list &lt;code&gt;[1, 2, 3]&lt;/code&gt; will return a list &lt;code&gt;[f(1), f(2), f(3)]&lt;/code&gt;.
     * As it can be seen that the function `f` is not applied immediately which makes &lt;code&gt;flatmap&lt;/code&gt; a &lt;b&gt;lazy&lt;/b&gt; implementation.
     * The function `f` is not applied to the elements until a &lt;b&gt;eager&lt;/b&gt; api is called. Therefore, calling &lt;code&gt;flatmap&lt;/code&gt; has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param f function to apply to each element.
     * @param <R> type returned by the function `f` application
     * @return resultant list after applying `f` to all the list elements
     */
    public <R> ImmutableList<R> flatmap(Function<T, ? extends List<R>> f) {
        ImmutableList<R> res;
        if (func == null) {
            res = new ImmutableList<>(list, constructor, f.compose(Util::cast));
        } else {
            res = new ImmutableList<>(src, constructor, func.andThen(l -> apply(l, f.compose(Util::cast))));
        }
        return res;
    }

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the list excludes elements from the list which does not satisfy `c`. Hence the resultant list of this api only contains the elements which satisfies the condition `c`. &lt;br /&gt;
     * Function application is &lt;b&gt;lazy&lt;/b&gt; which means calling this api has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param c condition to evaluate against each element
     * @return list containing elements which satisfies the condition `c`
     */
    @Override
    public ImmutableList<T> filter(Condition<T> c) {
        return flatmap(t -> c.evaluate(t) ? Collections.singletonList(t) : Collections.emptyList());
    }

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the list excludes elements from the list which satisfies `c`. Hence the resultant list of this api only contains the elements which does not satisfy the condition `c`. &lt;br /&gt;
     * Function application is &lt;b&gt;lazy&lt;/b&gt; which means calling this api has no effect until a &lt;b&gt;eager&lt;/b&gt; api is called.
     * @param c condition to evaluate against each element
     * @return list containing elements which does not satisfy the condition `c`
     */
    @Override
    public ImmutableList<T> filterOut(Condition<T> c) {
        return filter(c.negate());
    }
    /* ------------------- END: Lazy methods ------------------- */

    /**
     * @return &lt;code&gt;true&lt;/code&gt; if all the lazy functions (if any) are applied otherwise &lt;code&gt;false&lt;/code&gt;
     */
    @Override
    public boolean isApplied() {
        return applied;
    }

    /**
     * Function application is &lt;b&gt;eager&lt;/b&gt; i.e. it applies all the lazy functions (if any) to list elements
     * @return &lt;code&gt;current instance&lt;/code&gt; if already &lt;code&gt;applied&lt;/code&gt; otherwise a &lt;code&gt;new instance&lt;/code&gt; with all the lazy functions applied
     */
    @Override
    public ImmutableList<T> applied() {
        ImmutableList<T> res;
        if (isApplied()) {
            res = this;
        } else {
            res = new ImmutableList<>(apply(src, func), constructor);
        }
        return res;
    }

    /**
     * Function application is &lt;b&gt;eager&lt;/b&gt; i.e. it applies all the lazy functions (if any) to list elements
     * @return the underlying &lt;code&gt;list&lt;/code&gt; with all the lazy functions (if any) applied
     * @throws IllegalStateException if not {@link #applied() applied}
     */
    @Override
    public List<T> toList() {
        return applied().list;
    }

    /**
     * Applies the &lt;code&gt;Condition&lt;/code&gt; `c` to all the elements in the {@link #applied() applied} list and returns a &lt;code&gt;Couple&lt;/code&gt; of &lt;code&gt;ImmutableList&lt;/code&gt;s with satisfying elements in {@link Couple#first() first} and &lt;b&gt;not&lt;/b&gt; satisfying elements in {@link Couple#second() second}
     * @param c condition based on which the elements will be segregated
     * @return &lt;code&gt;Couple&lt;/code&gt; of &lt;code&gt;ImmutableList&lt;/code&gt;s with satisfying elements in {@link Couple#first() first} and &lt;b&gt;not&lt;/b&gt; satisfying elements in {@link Couple#second() second}
     * @throws IllegalStateException if not {@link #applied() applied}
     */
    @Override
    public Couple<ImmutableList<T>, ImmutableList<T>> split(Condition<T> c) {
        alertIfNotApplied();
        ImmutableList<T> match = ImmutableList.wrap(constructor);
        ImmutableList<T> rest = ImmutableList.wrap(constructor);
        for (T t : list) {
            if (c.evaluate(t)) {
                match.list.add(t);
            } else {
                rest.list.add(t);
            }
        }
        return Tuple.of(match, rest);
    }

    public ImmutableList<Couple<Integer, T>> indexedList() {
        return foldl(Tuple.of(0, ImmutableList.<Couple<Integer, T>>wrap(constructor)), (c, v) -> Tuple.of(c.first() + 1, c.second().append(Tuple.of(c.first(), v)))).second();
    }

    @Override
    public int size() {
        alertIfNotApplied();
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        alertIfNotApplied();
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        alertIfNotApplied();
        return list.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        alertIfNotApplied();
        return list.containsAll(c);
    }

    @Override
    public Iterator<T> iterator() {
        alertIfNotApplied();
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        alertIfNotApplied();
        return list.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        alertIfNotApplied();
        return list.toArray(a);
    }

    @Override
    public ImmutableList<T> append(T val) {
        ImmutableList<T> res = applied();
        res.list.add(val);
        return res;
    }

    @Override
    public ImmutableList<T> append(Collection<? extends T> c) {
        ImmutableList<T> res = applied();
        res.list.addAll(c);
        return res;
    }

    @Override
    public ImmutableList<T> insert(int index, T val) {
        ImmutableList<T> res = applied();
        res.list.add(index, val);
        return res;
    }

    @Override
    public ImmutableList<T> insert(int index, Collection<? extends T> c) {
        ImmutableList<T> res = applied();
        res.list.addAll(index, c);
        return res;
    }

    @Override
    public ImmutableList<T> replace(int index, T val) {
        ImmutableList<T> res = applied();
        res.list.set(index, val);
        return res;
    }

    @Override
    public ImmutableList<T> delete(int index) {
        ImmutableList<T> res = applied();
        res.list.remove(index);
        return res;
    }

    @Override
    public ImmutableList<T> delete(T val) {
        ImmutableList<T> res = applied();
        res.list.remove(val);
        return res;
    }

    @Override
    public ImmutableList<T> delete(Collection<? extends T> c) {
        ImmutableList<T> res = applied();
        res.list.removeAll(c);
        return res;
    }

    @Override
    public ImmutableList<T> preserve(Collection<? extends T> c) {
        ImmutableList<T> res = applied();
        res.list.retainAll(c);
        return res;
    }

    @Override
    public ImmutableList<T> empty() {
        alertIfNotApplied();
        return new ImmutableList<>(src, constructor, func);
    }

    @Override
    public T get(int index) {
        alertIfNotApplied();
        return list.get(index);
    }

    @Override
    public int indexOf(Object o) {
        alertIfNotApplied();
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        alertIfNotApplied();
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        alertIfNotApplied();
        return list.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        alertIfNotApplied();
        return list.listIterator(index);
    }

    @Override
    public Spliterator<T> spliterator() {
        alertIfNotApplied();
        return list.spliterator();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        alertIfNotApplied();
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public ImmutableList<T> sorted(Comparator<? super T> c) {
        ImmutableList<T> res = applied();
        res.list.sort(c);
        return res;
    }

    @Override
    public ImmutableList<T> replacingAll(UnaryOperator<T> operator) {
        ImmutableList<T> res = applied();
        res.list.replaceAll(operator);
        return res;
    }

    @Override
    public ImmutableList<T> deleteIf(Predicate<? super T> filter) {
        ImmutableList<T> res = applied();
        res.list.removeIf(filter);
        return res;
    }

    @Override
    public Stream<T> stream() {
        alertIfNotApplied();
        return list.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        alertIfNotApplied();
        return list.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        alertIfNotApplied();
        list.forEach(action);
    }

    @Override
    public String toString() {
        return isApplied() ? list.toString() : "[?]";
    }

    @Override
    public int hashCode() {
        alertIfNotApplied();
        return list.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        alertIfNotApplied();
        if (obj instanceof FunctionalList) {
            FunctionalList<?, ?> fList = Util.cast(obj);
            obj = fList.list();
        }
        return list.equals(obj);
    }

    @Override
    public ImmutableList<T> copy() {
        alertIfNotApplied();
        ImmutableList<T> r = new ImmutableList<>(src, constructor, func);
        r.list.addAll(list);
        return r;
    }

    private void alertIfNotApplied() {
        if (!isApplied()) {
            throw new IllegalStateException("Immutable List not yet `applied`! Consider calling `applied()` before this api");
        }
    }

    private <R> List<R> apply(List<?> l, Function<Object, ? extends List<R>> f) {
        List<R> r = Util.cast(constructor.produce());
        for (Object o : l) {
            r.addAll(f.apply(o));
        }
        return r;
    }

    @SuppressWarnings("unchecked")
    public static <A> ImmutableList<A> wrap(Producer<List<?>> constructor) {
        return of((List<A>) constructor.produce(), constructor);
    }
}
