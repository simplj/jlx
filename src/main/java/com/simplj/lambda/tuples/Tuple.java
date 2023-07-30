package com.simplj.lambda.tuples;

public final class Tuple {
    public static <A, B> Couple<A, B> of(A first, B second) {
        return new Couple<>(first, second);
    }
    public static <A, B, C> Triple<A, B, C> of(A first, B second, C third) {
        return new Triple<>(first, second, third);
    }
    public static <A, B, C, D> Quadruple<A, B, C, D> of(A first, B second, C third, D fourth) {
        return new Quadruple<>(first, second, third, fourth);
    }
    public static <A, B, C, D, E> Pentuple<A, B, C, D, E> of(A first, B second, C third, D fourth, E fifth) {
        return new Pentuple<>(first, second, third, fourth, fifth);
    }
    public static <A, B, C, D, E, F> Hextuple<A, B, C, D, E, F> of(A first, B second, C third, D fourth, E fifth, F sixth) {
        return new Hextuple<>(first, second, third, fourth, fifth, sixth);
    }
    public static <A, B, C, D, E, F, G> Septuple<A, B, C, D, E, F, G> of(A first, B second, C third, D fourth, E fifth, F sixth, G seventh) {
        return new Septuple<>(first, second, third, fourth, fifth, sixth, seventh);
    }
    public static <A, B, C, D, E, F, G, H> Octuple<A, B, C, D, E, F, G, H> of(A first, B second, C third, D fourth, E fifth, F sixth, G seventh, H eighth) {
        return new Octuple<>(first, second, third, fourth, fifth, sixth, seventh, eighth);
    }
}
