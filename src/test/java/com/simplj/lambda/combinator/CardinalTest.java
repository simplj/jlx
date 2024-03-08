package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.TriFunction;
import org.junit.Assert;
import org.junit.Test;

public class CardinalTest {
    @Test
    public void cardinalTest() {
        TriFunction<BiFunction<Integer, Integer, Integer>, Integer, Integer, Integer> cardinal = new CardinalImpl().build();

        BiFunction<Integer, Integer, Integer> sub = (a,b) -> a-b;

        int res = cardinal.apply(sub, 4, 2);

        Assert.assertEquals(res, -2);

        System.out.println("[✅] cardinalTest Passed");
    }
}

class CardinalImpl implements Cardinal<Integer, Integer, Integer> {}