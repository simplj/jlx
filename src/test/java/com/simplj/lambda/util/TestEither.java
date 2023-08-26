package com.simplj.lambda.util;

import com.simplj.lambda.TestUtil;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestEither {
    @Test
    public void testLeft() {
        Either<Integer, String> left = Either.left(0);
        assertTrue(left.isLeft());
        assertFalse(left.isRight());
        assertEquals(0, left.left().intValue());
        assertEquals("Left[0]", left.toString());

        TestUtil.testEqualsAndHashCode(left, Either.left(0));
    }

    @Test
    public void testRight() {
        Either<String, Integer> right = Either.right(0);
        assertFalse(right.isLeft());
        assertTrue(right.isRight());
        assertEquals(0, right.right().intValue());
        assertEquals("Right[0]", right.toString());

        TestUtil.testEqualsAndHashCode(right, Either.right(0));
    }

    @Test
    public void testFlatmap() {
        assertEquals(1, Either.right(0).flatmap(n -> Either.right(n + 1)).right().intValue());
        assertEquals(0, Either.<Integer, Integer>left(0).flatmap(n -> Either.left(1)).left().intValue());
    }

    @Test
    public void testMap() {
        assertEquals(1, Either.right(0).map(n -> n + 1).right().intValue());
        assertEquals(0, Either.<Integer, Integer>left(0).map(n -> n + 1).left().intValue());
    }
}
