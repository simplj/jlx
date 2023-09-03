package com.simplj.lambda.data;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

import java.util.*;
import java.util.stream.Stream;

abstract class FunctionalArray<E, A extends FunctionalArray<E, A>> implements Iterable<E> {

    abstract A unit(E[] arr);

    public abstract A set(int idx, E val);

    public abstract E get(int idx);

    public abstract E[] array();

    public abstract A filter(Condition<E> c);

    public A filterOut(Condition<E> c) {
        return filter(c.negate());
    }

    public Couple<A, A> split(Condition<E> c) {
        E[] arr = array();
        List<E> first = new LinkedList<>();
        List<E> second = new LinkedList<>();
        for (E e : arr) {
            if (c.evaluate(e)) {
                first.add(e);
            } else {
                second.add(e);
            }
        }
        A a = unit(first.toArray(Util.cast(new Object[first.size()])));
        A b = unit(second.toArray(Util.cast(new Object[second.size()])));
        return Tuple.of(a, b);
    }

    public Couple<Integer, E>[] indexed() {
        E[] arr = array();
        Couple<Integer, E>[] res = Util.cast(new Object[arr.length]);
        for (int i = 0; i < arr.length; i++) {
            res[i] = Tuple.of(i, arr[i]);
        }
        return res;
    }

    public int size() {
        return array().length;
    }

    public boolean isEmpty() {
        return 0 == size();
    }

    public boolean contains(E elem) {
        E[] arr = array();
        for (E e : arr) {
            if (Objects.equals(elem, e)) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public final boolean containsAll(E...elems) {
        Set<E> set = new HashSet<>();
        Collections.addAll(set, elems);
        E[] arr = array();
        for (E e : arr) {
            set.remove(e);
            if (set.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public int indexOf(E elem) {
        E[] arr = array();
        int idx = 0;
        for (E e : arr) {
            if (Objects.equals(elem, e)) {
                return idx;
            }
            idx++;
        }
        return -1;
    }

    public int lastIndexOf(E elem) {
        E[] arr = array();
        for (int i = arr.length - 1; i >= 0; i--) {
            if (Objects.equals(elem, arr[i])) {
                return i;
            }
        }
        return -1;
    }

    public E find(Condition<E> c) {
        return Util.find(array(), c);
    }

    public boolean none(Condition<E> c) {
        return null == find(c);
    }

    public boolean any(Condition<E> c) {
        return !none(c);
    }

    public boolean all(Condition<E> c) {
        return none(c.negate());
    }

    public <R> R foldl(R identity, BiFunction<R, E, R> accumulator) {
        E[] arr = array();
        for (E e : arr) {
            identity = accumulator.apply(identity, e);
        }
        return identity;
    }

    public <R> R foldr(R origin, BiFunction<E, R, R> accumulator) {
        E[] arr = array();
        int idx = arr.length - 1;
        while (idx >= 0) {
            origin = accumulator.apply(arr[idx], origin);
            idx--;
        }
        return origin;
    }

    public E reduceL(BiFunction<E, E, E> accumulator) {
        E res = null;
        E[] arr = array();
        if (arr.length > 0) {
            res = arr[0];
            for (int idx = arr.length, i = 1; i < idx; i++) {
                res = accumulator.apply(res, arr[i]);
            }
        }
        return res;
    }

    public E reduceR(BiFunction<E, E, E> accumulator) {
        E res = null;
        E[] arr = array();
        if (arr.length > 0) {
            int idx = arr.length - 1;
            res = arr[idx];
            idx -= 1;
            while (idx >= 0) {
                res = accumulator.apply(arr[idx], res);
                idx--;
            }
        }
        return res;
    }

    public Stream<E> stream() {
        return Arrays.stream(array());
    }

    public A copy() {
        E[] arr = array();
        E[] res = Util.cast(new Object[arr.length]);
        System.arraycopy(arr, 0, res, 0, arr.length);
        return unit(res);
    }

    @Override
    public Iterator<E> iterator() {
        return Arrays.asList(array()).iterator();
    }

    /**
     * @return <code>true</code> if all the lazy functions (if any) are applied otherwise <code>false</code>
     */
    public abstract boolean isApplied();

    public abstract A applied();
}
