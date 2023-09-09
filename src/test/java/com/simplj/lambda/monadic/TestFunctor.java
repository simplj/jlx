package com.simplj.lambda.monadic;

import com.simplj.lambda.TestUtil;
import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.util.Mutable;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestFunctor {
    @Test
    public void testFunctor() {
        Functor<Integer> f = Functor.arg(0).applied();
        assertEquals(0, f.result().right().intValue());
        assertNull(f.result().left());
        assertEquals("Functor[?]", Functor.arg(0).toString());
        assertEquals("Right[0]", f.toString());
        TestUtil.testEqualsAndHashCode(f, Functor.arg(0).applied());
    }

    @Test
    public void testFunctorFlatmap() {
        assertTrue(Functor.arg(1).flatmap(n -> Functor.arg(1 / n)).result().isRight());
        assertTrue(Functor.arg(0).flatmap(z -> Functor.arg(1 / z)).flatmap(Functor::arg).result().isLeft());
        assertTrue(Functor.arg(0).flatmap(z -> Functor.arg(1 / z)).recover(e -> 0).result().isRight());
    }

    @Test
    public void testFunctorMap() {
        assertTrue(Functor.arg(1).map(n -> 1 / n).result().isRight());
        assertTrue(Functor.arg(0).map(z -> 1 / z).map(Executable.id()).result().isLeft());
        assertTrue(Functor.arg(0).map(z -> 1 / z).recover(e -> 0).result().isRight());
    }

    @Test
    public void testExecute() {
        Mutable<Integer> m = Mutable.of(0);
        Functor.arg(1).record(m::set).applied();
        assertEquals(1, m.get().intValue());
    }

    @Test
    public void testFunctorNotApplied() {
        assertThrows(IllegalStateException.class, () -> Functor.arg(0).equals(null));
        assertThrows(IllegalStateException.class, () -> Functor.arg(0).hashCode());
    }
}
