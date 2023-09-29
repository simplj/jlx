package com.simplj.lambda.monadic;

import com.simplj.lambda.TestUtil;
import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.util.Mutable;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestFunctor {
    @Test
    public void testFunctor() {
        Thunk<Integer> f = Thunk.init(0).applied();
        assertEquals(0, f.result().right().intValue());
        assertNull(f.result().left());
        assertEquals("Functor[?]", Thunk.init(0).toString());
        assertEquals("Right[0]", f.toString());
        TestUtil.testEqualsAndHashCode(f, Thunk.init(0).applied());
    }

    @Test
    public void testFunctorFlatmap() {
        assertTrue(Thunk.init(1).flatmap(n -> Thunk.init(1 / n)).result().isRight());
        assertTrue(Thunk.init(0).flatmap(z -> Thunk.init(1 / z)).flatmap(Thunk::init).result().isLeft());
        assertTrue(Thunk.init(0).flatmap(z -> Thunk.init(1 / z)).recover(e -> 0).result().isRight());
    }

    @Test
    public void testFunctorMap() {
        assertTrue(Thunk.init(1).map(n -> 1 / n).result().isRight());
        assertTrue(Thunk.init(0).map(z -> 1 / z).map(Executable.id()).result().isLeft());
        assertTrue(Thunk.init(0).map(z -> 1 / z).recover(e -> 0).result().isRight());
    }

    @Test
    public void testExecute() {
        Mutable<Integer> m = Mutable.of(0);
        Thunk.init(1).record(m::set).applied();
        assertEquals(1, m.get().intValue());
    }

    @Test
    public void testFunctorNotApplied() {
        assertThrows(IllegalStateException.class, () -> Thunk.init(0).equals(null));
        assertThrows(IllegalStateException.class, () -> Thunk.init(0).hashCode());
    }
}
