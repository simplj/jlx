package com.simplj.lambda.util;

import com.simplj.lambda.TestUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestMutable {
    @Test
    public void testMutable() {
        Mutable<Integer> m0 = Mutable.of(0);
        assertEquals(0, m0.get().intValue());
        assertEquals(1, m0.set(1).get().intValue());

        TestUtil.testEqualsAndHashCode(m0.set(0), Mutable.of(0));

        assertEquals("0", m0.toString());
    }

    @Test
    public void testMutableMutate() {
        assertEquals(1, Mutable.of(0).mutate(n -> n + 1).get().intValue());
    }

    @Test
    public void testMutableChange() {
        assertEquals("1", Mutable.of(1).change(Object::toString).get());
    }
}
