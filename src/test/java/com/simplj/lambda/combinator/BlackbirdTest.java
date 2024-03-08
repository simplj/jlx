package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.QuadFunction;
import org.junit.Assert;
import org.junit.Test;

public class BlackbirdTest {

    @Test
    public void blackBirdTest() {
        QuadFunction<Function<Integer, Integer>, BiFunction<Integer, Integer, Integer>, Integer, Integer, Integer> blackbird = new BlackbirdImpl().build();

        Function<Integer, Integer> id = a -> a;
        BiFunction<Integer, Integer, Integer> sum = (a,b) -> a+b;

        int res = blackbird.apply(id, sum, 2, 3);

        Assert.assertEquals(res, 5);

        System.out.println("[✅] blackbirdTest Passed");
    }
}

class BlackbirdImpl implements Blackbird<Integer, Integer, Integer, Integer> {}