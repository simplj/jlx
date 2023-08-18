package test;

import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.monadic.Functor;

public class FunctorMonadTest {
    private final Function<Integer, Functor<Integer>> unitF = Functor::arg;
    private final Executable<Integer, Functor<Integer>> incrF = x -> unitF.apply(x + 1);
    private final Executable<Integer, Functor<Integer>> doubleF = x -> unitF.apply(x + x);

    public static void main(String[] args) throws Exception {
        FunctorMonadTest fmt = new FunctorMonadTest();
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
        return unitF.apply(0).flatmap(incrF).result().right().equals(incrF.execute(0).result().right());
    }

    /*
    Monad Law 2: Right Identity
    M `bind` unit = M, where M is a Monadic Value
    i.e. `bind`ing a monadic value `M` to `unit` will result in the original monadic value
     */
    public boolean testRightIdentity() throws Exception {
        Functor<Integer> m = unitF.apply(0);
        return m.flatmap(unitF::apply).result().right().equals(m.result().right());
    }

    /*
    Monad Law 3: Associativity
    (M `bind` f) `bind` g = M `bind` (f `bind` g), where M is a Monadic Value
    i.e. chain of monadic function application using `bind` does not matter how they are nested
     */
    public boolean testAssociativity() throws Exception {
        Functor<Integer> m = unitF.apply(0);
        return m.flatmap(incrF).flatmap(doubleF).result().right().equals(m.flatmap(incrF.andThen(v -> v.flatmap(doubleF))).result().right());
    }
}
