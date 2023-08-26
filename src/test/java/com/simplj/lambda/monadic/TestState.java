package com.simplj.lambda.monadic;

import com.simplj.lambda.TestUtil;
import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.util.Mutable;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestState {
    @Test
    public void testState() {
        State<Integer> state = State.arg(0);
        assertTrue(state.isSuccessful());
        assertEquals(0, state.get().intValue());
        assertNull(state.error());
        assertEquals("Right[0]", state.toString());
        TestUtil.testEqualsAndHashCode(state, State.arg(0));
    }

    @Test
    public void testStateMap() {
        assertTrue(State.arg(1).map(n -> 1 / n).result().isRight());
        assertTrue(State.arg(0).map(z -> 1 / z).map(Executable.id()).result().isLeft());
        assertTrue(State.arg(0).map(z -> 1 / z).recover(e -> 0).result().isRight());
    }

    @Test
    public void testExecute() throws Exception {
        Mutable<Integer> m = Mutable.of(0);
        State.arg(1).execute(m::set);
        assertEquals(1, m.get().intValue());
    }
}
