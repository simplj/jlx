package com.simplj.lambda.data;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;
import com.simplj.lambda.util.Expr;

import java.util.*;
import java.util.stream.Stream;

import static com.simplj.lambda.util.Expr.let;

abstract class FArray<E, A extends FArray<E, A>> implements Iterable<E> {
    final E[] newArray;

    FArray() {
        newArray = Util.cast(new Object[0]);
    }

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
        List<E> match = new LinkedList<>();
        List<E> rest = new LinkedList<>();
        for (E e : arr) {
            if (c.evaluate(e)) {
                match.add(e);
            } else {
                rest.add(e);
            }
        }
        E[] a = Util.cast(new Object[match.size()]);
        E[] b = Util.cast(new Object[rest.size()]);
        return Tuple.of(unit(match.toArray(a)), unit(rest.toArray(b)));
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

    public List<E> toList() {
        return Arrays.asList(array());
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

    /**
     * @return <code>true</code> if all the lazy functions (if any) are applied otherwise <code>false</code>
     */
    public abstract boolean isApplied();

    public abstract A applied();

    @Override
    public String toString() {
        return isApplied() ? Arrays.toString(array()) : "[?]";
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array());
    }

    @Override
    public boolean equals(Object obj) {
        boolean res;
        if (obj == null) {
            res = null == array();
        } else {
            if (obj instanceof FArray) {
                FArray<?, ?> fList = Util.cast(obj);
                res = Arrays.equals(array(), fList.array());
            } else if (obj.getClass().isArray()) {
                if (obj.getClass().getComponentType().isPrimitive()) {
                    res = primEq(obj);
                } else {
                    res = Arrays.equals(array(), Util.cast(obj));
                }
            } else {
                res = false;
            }
        }
        return res;
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

    /*int, byte, short, long, float, double, boolean and char*/
    private boolean primEq(Object obj) {
        boolean res;
        String primType = obj.getClass().getComponentType().getSimpleName();
        E[] arr;
        switch (primType) {
            case "int":
                int[] iArr = Util.cast(obj);
                arr = array();
                res = iArr.length == arr.length;
                for (int i = 0; res && i < iArr.length; i++) {
                    res = arr[i].equals(iArr[i]);
                }
                break;
            case "byte":
                byte[] bArr = Util.cast(obj);
                arr = array();
                res = bArr.length == arr.length;
                for (int i = 0; res && i < bArr.length; i++) {
                    res = arr[i].equals(bArr[i]);
                }
                break;
            case "short":
                short[] sArr = Util.cast(obj);
                arr = array();
                res = sArr.length == arr.length;
                for (int i = 0; res && i < sArr.length; i++) {
                    res = arr[i].equals(sArr[i]);
                }
                break;
            case "long":
                long[] lArr = Util.cast(obj);
                arr = array();
                res = lArr.length == arr.length;
                for (int i = 0; res && i < lArr.length; i++) {
                    res = arr[i].equals(lArr[i]);
                }
                break;
            case "float":
                float[] fArr = Util.cast(obj);
                arr = array();
                res = fArr.length == arr.length;
                for (int i = 0; res && i < fArr.length; i++) {
                    res = arr[i].equals(fArr[i]);
                }
                break;
            case "double":
                double[] dArr = Util.cast(obj);
                arr = array();
                res = dArr.length == arr.length;
                for (int i = 0; res && i < dArr.length; i++) {
                    res = arr[i].equals(dArr[i]);
                }
                break;
            case "boolean":
                boolean[] binArr = Util.cast(obj);
                arr = array();
                res = binArr.length == arr.length;
                for (int i = 0; res && i < binArr.length; i++) {
                    res = arr[i].equals(binArr[i]);
                }
                break;
            case "char":
                char[] cArr = Util.cast(obj);
                arr = array();
                res = cArr.length == arr.length;
                for (int i = 0; res && i < cArr.length; i++) {
                    res = arr[i].equals(cArr[i]);
                }
                break;
            default:
                throw new IllegalArgumentException("Not supported primitive data type '" + primType + "'!");
        }
        return res;
    }
}
