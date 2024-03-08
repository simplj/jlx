package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.QuadFunction;
import org.junit.Assert;
import org.junit.Test;

public class DoveTest {
    @Test
    public void doveTest() {
        QuadFunction<BiFunction<Integer, Integer, Integer>, Integer, Function<Integer, Integer>, Integer, Integer> dove = new DoveImpl().build();

        Function<Integer, Integer> addOne = a -> a+1;
        BiFunction<Integer, Integer, Integer> sum = (a,b) -> a+b;

        int res = dove.apply(sum, 2, addOne, 3);

        Assert.assertEquals(res, 6);

        System.out.println("[✅] doveTest Passed");
    }
}

class DoveImpl implements Dove<Integer, Integer, Integer, Integer> {}