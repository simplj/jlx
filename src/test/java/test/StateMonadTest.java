package test;

import com.simplj.lambda.monadic.State;
import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.function.Function;

public class StateMonadTest {
    private final Function<Integer, State<Integer>> unitF = State::arg;
    private final Executable<Integer, State<Integer>> incrF = x -> unitF.apply(x + 1);
    private final Executable<Integer, State<Integer>> doubleF = x -> unitF.apply(x + x);

    public static void main(String[] args) throws Exception {
        StateMonadTest fmt = new StateMonadTest();
        System.out.println("1st Law: " + fmt.testLeftIdentity());
        System.out.println("2nd Law: " + fmt.testRightIdentity());
        System.out.println("3rd Law: " + fmt.testAssociativity());
    }

    /*
    Monad Law 1: Left Identity
    unit(x) `bind` f = f x
    i.e. wrapping a value `x` into a monad and then feeding the value to a function `f` (by using bind) is same as just applying the function `f` to value `x`
     */
    public boolean testLeftIdentity() throws Exception {
        return unitF.apply(0).flatmap(incrF).get().equals(incrF.execute(0).get());
    }

    /*
    Monad Law 2: Right Identity
    M `bind` unit = M, where M is a Monadic Value
    i.e. `bind`ing a monadic value `M` to `unit` will result in the original monadic value
     */
    public boolean testRightIdentity() throws Exception {
        State<Integer> m = unitF.apply(0);
        return m.flatmap(unitF::apply).get().equals(m.get());
    }

    /*
    Monad Law 3: Associativity
    (M `bind` f) `bind` g = M `bind` (f `bind` g), where M is a Monadic Value
    i.e. chain of monadic function application using `bind` does not matter how they are nested
     */
    public boolean testAssociativity() throws Exception {
        State<Integer> m = unitF.apply(0);
        return m.flatmap(incrF).flatmap(doubleF).get().equals(m.flatmap(incrF.andThen(v -> v.flatmap(doubleF))).get());
    }
}
