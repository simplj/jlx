package com.simplj.lambda.util;

import com.simplj.lambda.function.Function;

import java.util.Objects;

public class Either<L, R> {
    private final L left;
    private final R right;
    private final int flag;

    private Either(L left, R right, int flag) {
        this.left = left;
        this.right = right;
        this.flag = flag;
    }

    public static <A, B> Either<A, B> left(A a) {
        return new Either<>(a, null, -1);
    }
    public static <A, B> Either<A, B> right(B b) {
        return new Either<>(null, b, 1);
    }

    public boolean isLeft() {
        return flag == -1;
    }
    public boolean isRight() {
        return flag == 1;
    }

    public L left() {
        return left;
    }
    public R right() {
        return right;
    }

    public <A> Either<L, A> map(Function<R, A> f) {
        return flatmap(f.andThen(Either::right));
    }
    public <A> Either<L, A> flatmap(Function<R, Either<L, A>> f) {
        Either<L, A> res;
        if (isRight()) {
            res = f.apply(right);
        } else {
            res = left(left);
        }
        return res;
    }

    @Override
    public String toString() {
        return (isLeft() ? "Left[" + left : "Right[" + right) + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Either<?, ?> that = (Either<?, ?>) o;
        return this.flag == that.flag && ((this.isLeft() && Objects.equals(this.left, that.left))
                || (this.isRight() && Objects.equals(this.right, that.right)) || this.flag == 0);
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : (right != null ? right.hashCode() : 0);
        result = 31 * result + flag;
        return result;
    }
}
