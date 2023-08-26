package com.simplj.lambda.monadic;

import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.function.Function;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StateMonadTest {
    private final Function<Integer, State<Integer>> unitF = State::arg;
    private final Executable<Integer, State<Integer>> incrF = x -> unitF.apply(x + 1);
    private final Executable<Integer, State<Integer>> doubleF = x -> unitF.apply(x + x);

    /*
    Monad Law 1: Left Identity
    unit(x) `bind` f = f x
    i.e. wrapping a value `x` into a monad and then feeding the value to a function `f` (by using bind) is same as just applying the function `f` to value `x`
     */
    @Test
    public void testLeftIdentity() throws Exception {
        assertEquals(incrF.execute(0).get(), unitF.apply(0).flatmap(incrF).get());
    }

    /*
    Monad Law 2: Right Identity
    M `bind` unit = M, where M is a Monadic Value
    i.e. `bind`ing a monadic value `M` to `unit` will result in the original monadic value
     */
    @Test
    public void testRightIdentity() throws Exception {
        State<Integer> m = unitF.apply(0);
        assertEquals(m.get(), m.flatmap(unitF::apply).get());
    }

    /*
    Monad Law 3: Associativity
    (M `bind` f) `bind` g = M `bind` (f `bind` g), where M is a Monadic Value
    i.e. chain of monadic function application using `bind` does not matter how they are nested
     */
    @Test
    public void testAssociativity() throws Exception {
        State<Integer> m = unitF.apply(0);
        assertEquals(m.flatmap(incrF.andThen(v -> v.flatmap(doubleF))).get(), m.flatmap(incrF).flatmap(doubleF).get());
    }
}
