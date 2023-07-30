package com.simplj.lambda.data.monadic;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;

import java.util.Optional;

/**
 * MList&lt;T&gt; depicts the List Monad from Functional Programming.
 * &lt;br /&gt;&lt;b&gt;&lt;u&gt;WARNING:&lt;/u&gt;&lt;/b&gt; This list follows functional paradigm and the methods are recursive,
 *  hence may throw &lt;b&gt;StackOverFlow&lt;/b&gt; exception when the size grows large!
 *  (Threshold size depends on the JVM memory)
 * @param <T> Type of underlying value
 */
public class MList<T> {
    private final T head;
    private final Producer<MList<T>> tail;

    private MList() {
        this(null, null);
    }
    private MList(T value) {
        this(value, MList::new);
    }
    private MList(T value, Producer<MList<T>> tail) {
        this.head = value;
        this.tail = tail;
    }

    //Monad's unit/return
    public static <A> MList<A> unit(A v) {
        return new MList<>(v);
    }

    //Monad's bind
    public <R> MList<R> fmap(Function<T, MList<R>> f) {
        return foldr((e, acc) -> f.apply(e).append(acc), new MList<>());
    }

    public <R> MList<R> map(Function<T, R> f) {
        return foldr((e, b) -> b.cons(f.apply(e)), new MList<>());
    }

    public MList<T> filter(Function<T, Boolean> f) {
        return foldr((e, acc) -> f.apply(e) ? acc.cons(e) : acc, new MList<>());
    }

    public <R> R foldr(BiFunction<T, R, R> f, R r) {
        if (isEmpty()) return r;
        return f.apply(head, tail().foldr(f, r));
    }

    public <R> R foldl(BiFunction<R, T, R> f, R r) {
        if (isEmpty()) return r;
        return tail().foldl(f, f.apply(r, head));
    }

    public MList<T> cons(T v) {
        return v == null ? this : new MList<>(v, () -> this);
    }

    public MList<T> append(MList<T> l) {
        return l == null || l.isEmpty() ? this : foldr((e, acc) -> acc.cons(e), l);
    }

    public MList<T> reverse() {
        return foldl(MList::cons, new MList<>());
    }

    public boolean isEmpty() {
        return head == null;
    }

    public Optional<T> find(Condition<T> c) {
        if (isEmpty()) return Optional.empty();
        if (c.evaluate(head)) return Optional.of(head);
        return tail().find(c);
    }

    public MList<T> takeWhile(Condition<T> c) {
        if (isEmpty() || c.negate().evaluate(head)) return this;
        return tail().takeWhile(c);
    }

    public MList<T> dropWhile(Condition<T> c) {
        if (isEmpty() || c.negate().evaluate(head)) return this;
        return tail().dropWhile(c);
    }

    public int size() {
        if (isEmpty()) return 0;
        return 1 + tail().size();
    }

    public T head() {
        if (head == null) throw new IllegalStateException("head called on empty list!");
        return head;
    }
    public Optional<T> headSafe() {
        if (head == null) return Optional.empty();
        return Optional.of(head);
    }
    public MList<T> tail() {
        if (tail == null) return new MList<>();
        return tail.produce();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        MList<T> l = this;
        while (l.head != null) {
            sb.append(l.head);
            l = l.tail();
        }
        return sb.subSequence(0, sb.length()) + "]";
    }

    @Override
    public boolean equals(Object obj) {
        boolean res = false;
        if (obj instanceof MList) {
            MList<?> that = (MList<?>) obj;
            MList<T> current = this;
            res = current.size() == that.size();
            while (res && that.head != null && current.head != null) {
                res = current.head.equals(that.head);
                current = current.tail();
                that = that.tail();
            }
        }
        return res;
    }
}
