package com.simplj.lambda.util;

import com.simplj.lambda.function.Function;

import java.util.Objects;

/**
 * A Data Structure which contains either a Left value or a Right value.
 * Generally below rules a followed for a Either Data Type:
 * <pre>
 *     Left value is considered to be the failure output of an operation
 *     Right value is considered to be the success output of an operation
 * </pre>
 * That means:
 * <pre>
 * when an operation/function fails, the failure output (for example: Exception) is returned as Left
 * when an operation/function succeeds, the resultant value is returned as Right.
 * </pre>
 * @param <L> Type of Left value
 * @param <R> Type of Right value
 */
public class Either<L, R> {
    private final L left;
    private final R right;
    private final int flag;

    private Either(L left, R right, int flag) {
        this.left = left;
        this.right = right;
        this.flag = flag;
    }

    /**
     * Sets Left value of Either
     * @param a   Value to be set as Left
     * @param <A> Type of Left value
     * @param <B> Type of Right value
     * @return Either instance having the given value set in it's Left
     */
    public static <A, B> Either<A, B> left(A a) {
        return new Either<>(a, null, -1);
    }

    /**
     * Sets Right value of Either
     * @param b   Value to be set as Right
     * @param <A> Type of Left value
     * @param <B> Type of Right value
     * @return Either instance having the given value set in it's Right
     */
    public static <A, B> Either<A, B> right(B b) {
        return new Either<>(null, b, 1);
    }

    /**
     * Returns whether this instance has a Left value
     * @return whether this instance has a Left value
     */
    public boolean isLeft() {
        return flag == -1;
    }

    /**
     * Returns whether this instance has a Right value
     * @return whether this instance has a Right value
     */
    public boolean isRight() {
        return flag == 1;
    }

    /**
     * Returns the Left value if `this.isLeft() == true` otherwise returns null
     * @return the Left value if `this.isLeft() == true` otherwise returns null
     */
    public L left() {
        return left;
    }

    /**
     * Returns the Right value if `this.isRight() == true` otherwise returns null
     * @return the Right value if `this.isRight() == true` otherwise returns null
     */
    public R right() {
        return right;
    }

    /**
     * Applies the Function f to the Right value of current Either instance.
     * If the current Either instance has a Left value then f is not applied.
     * <br>API is eager i.e. Function f is applied to Right value of current instance (or not applied at all if current instance has a Left value) as soon as this API is called.
     * <br>Following are true about this operation:
     * <pre>
     *     Either.left(l).map(f).left() is equal to l
     *     Either.right(r).map(f).right() is equal to f(r)
     * </pre>
     * @param f   function to be applied on the Right value
     * @param <A> Type of resultant value of Function f
     * @return new Either instance with resultant value of Function f (if applied, or the existing Left value)
     */
    public <A> Either<L, A> map(Function<R, A> f) {
        Either<L, A> res;
        if (isRight()) {
            res = right(f.apply(right));
        } else {
            res = left(left);
        }
        return res;
    }

    /**
     * Applies the Function f to the Right value of current Either instance and flattens the result. If the current Either instance has a Left value then f is not applied.
     * <br>API is eager i.e. Function f is applied to Right value of current instance (or not applied at all if current instance has a Left value) as soon as this API is called.
     * <br>Following are true about this operation:
     * <pre>
     *     Either.left(l).flatmap(f).left() is equal to l
     *     Either.right(r).flatmap(f) is equal to f(r)
     *     Either.right(r).flatmap(f).right() is equal to f(r).right()
     *     where, f returns an Either instance
     * </pre>
     * @param f   function to be applied on the Right value
     * @param <A> Type of resultant value of Function f
     * @return new Either instance with resultant value of Function f (if applied, or the existing Left value)
     */
    public <A> Either<L, A> flatmap(Function<R, Either<L, A>> f) {
        Either<L, A> res;
        if (isRight()) {
            res = f.apply(right);
        } else {
            res = left(left);
        }
        return res;
    }

    /**
     * Transforms the current Either instance to a new Either instance using the left or right transformers in from it's arguments.
     * <br>If the current either instance has a left value, then leftT is applied to it's left value.
     * <br>If the current either instance has a right value, then rightT is applied to it's right value.
     * <br>API is eager i.e. leftT or rightT is applied (based on current Either value) as soon as this API is called.
     * @param leftT  the transformer to be applied if current Either has a left value
     * @param rightT the transformer to be applied if current Either has a right value
     * @param <E>    Type of left value of the transformed Either
     * @param <A>    Type of right value of the transformed Either
     * @return new Either instance with leftT or rightT applied according to current Either value.
     */
    public <E, A> Either<E, A> transform(Function<L, E> leftT, Function<R, A> rightT) {
        Either<E, A> res;
        if (isRight()) {
            res = right(rightT.apply(right));
        } else {
            res = left(leftT.apply(left));
        }
        return res;
    }

    /**
     * Transforms the current Either instance to a new Either instance using the left or right transformers in from it's arguments.
     * <br>If the current either instance has a left value, then leftT is applied to it's left value.
     * <br>If the current either instance has a right value, then rightT is applied to it's right value and flattened.
     * <br>API is eager i.e. leftT or rightT is applied (based on current Either value) as soon as this API is called.
     * @param leftT  the transformer to be applied if current Either has a left value
     * @param rightT the transformer to be applied if current Either has a right value
     * @param <E>    Type of left value of the transformed Either
     * @param <A>    Type of right value of the transformed Either
     * @return new Either instance with leftT or rightT applied according to current Either value.
     */
    public <E, A> Either<E, A> flatTransform(Function<L, E> leftT, Function<R, Either<E, A>> rightT) {
        Either<E, A> res;
        if (isRight()) {
            res = rightT.apply(right);
        } else {
            res = left(leftT.apply(left));
        }
        return res;
    }

    /**
     * Applies the Function f to the Left value of current Either instance.
     * If the current Either instance has a Right value then f is not applied.
     * <br>API is eager i.e. Function f is applied to Left value of current instance (or not applied at all if current instance has a Right value) as soon as this API is called.
     * <br>Following are true about this operation:
     * <pre>
     *     Either.left(l).map(f).left() is equal to f(l)
     *     Either.right(r).map(f).right() is equal to r
     * </pre>
     * @param f   function to be applied on the Left value
     * @param <E> Type of resultant value of Function f
     * @return new Either instance with resultant value of Function f (if applied, or the existing Right value)
     */
    public <E> Either<E, R> mapLeft(Function<L, E> f) {
        Either<E, R> res;
        if (isLeft()) {
            res = left(f.apply(left));
        } else {
            res = right(right);
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
