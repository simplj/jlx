package com.simplj.lambda;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TestUtil {

    public static <T> void testEqualsAndHashCode(T t, T other) {
        assertEquals(t, t);
        assertNotEquals(null, t);
        assertEquals(t, other);

        assertEquals(t.hashCode(), other.hashCode());
    }
}
