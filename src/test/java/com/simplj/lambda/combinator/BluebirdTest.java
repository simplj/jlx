package com.simplj.lambda.combinator;

import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.TriFunction;
import org.junit.Assert;
import org.junit.Test;

public class BluebirdTest {
    @Test
    public void blueBirdTest() {
        TriFunction<Function<Integer, Integer>, Function<Integer, Integer>, Integer, Integer> bluebird = new BluebirdImpl().build();

        Function<Integer, Integer> addOne = a -> a+1;
        Function<Integer, Integer> subOne = a -> a-1;

        int res = bluebird.apply(addOne, subOne, 2);

        Assert.assertEquals(res, 2);

        System.out.println("[✅] bluebirdTest Passed");
    }
}

class BluebirdImpl implements Bluebird<Integer, Integer, Integer> {}