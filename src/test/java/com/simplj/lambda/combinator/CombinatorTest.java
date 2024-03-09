package com.simplj.lambda.combinator;

import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.QuadFunction;
import com.simplj.lambda.function.TriFunction;
import org.junit.Assert;
import org.junit.Test;

public class CombinatorTest {
    Function<Integer, Integer> addOne = a -> a+1;
    Function<Integer, Integer> subOne = a -> a-1;
    BiFunction<Integer, Integer, Integer> sum = (a,b) -> a+b;
    BiFunction<Integer, Integer, Integer> sub = (a,b) -> a-b;
    TriFunction<Integer, Integer, Integer, Integer> sumThree = (a,b,c) -> a+b+c;

    @Test
    public void applyTest() {
        BiFunction<Function<Integer, Integer>, Integer, Integer> apply = new ApplyImpl().build();
        int res = apply.apply(addOne, 5);
        Assert.assertEquals(res, 6);

        System.out.println("✅ applyTest Passed");
    }

    @Test
    public void blackBirdTest() {
        QuadFunction<Function<Integer, Integer>, BiFunction<Integer, Integer, Integer>, Integer, Integer, Integer> blackbird = new BlackbirdImpl().build();
        Function<Integer, Integer> id = a -> a;
        int res = blackbird.apply(id, sum, 2, 3);
        Assert.assertEquals(res, 5);

        System.out.println("✅ blackbirdTest Passed");
    }

    @Test
    public void blueBirdTest() {
        TriFunction<Function<Integer, Integer>, Function<Integer, Integer>, Integer, Integer> bluebird = new BluebirdImpl().build();
        int res = bluebird.apply(addOne, subOne, 2);
        Assert.assertEquals(res, 2);

        System.out.println("✅ bluebirdTest Passed");
    }

    @Test
    public void cardinalTest() {
        TriFunction<BiFunction<Integer, Integer, Integer>, Integer, Integer, Integer> cardinal = new CardinalImpl().build();
        int res = cardinal.apply(sub, 4, 2);
        Assert.assertEquals(res, -2);

        System.out.println("✅ cardinalTest Passed");
    }

    @Test
    public void doveTest() {
        QuadFunction<BiFunction<Integer, Integer, Integer>, Integer, Function<Integer, Integer>, Integer, Integer> dove = new DoveImpl().build();
        int res = dove.apply(sum, 2, addOne, 3);
        Assert.assertEquals(res, 6);

        System.out.println("✅ doveTest Passed");
    }

    @Test
    public void finchTest() {
        TriFunction<Integer, Integer, BiFunction<Integer, Integer, Integer>, Integer> finch = new FinchImpl().build();
        int res = finch.apply(2,3,sum);
        Assert.assertEquals(res, 5);

        System.out.println("✅ finchTest Passed");
    }

    @Test
    public void goldFinchTest() {
        QuadFunction<BiFunction<Integer, Integer, Integer>, Function<Integer, Integer>, Integer, Integer, Integer> goldfinch = new GoldFinchImpl().build();
        int res = goldfinch.apply(sum, addOne, 2, 3);
        Assert.assertEquals(res, 6);

        System.out.println("✅ goldFinchTest Passed");
    }

    @Test
    public void hummingBirdTest() {
        TriFunction<TriFunction<Integer, Integer, Integer, Integer>, Integer, Integer, Integer> hummingbird = new HummingbirdImpl().build();
        int res = hummingbird.apply(sumThree, 2, 3);
        Assert.assertEquals(res, 7);

        System.out.println("✅ hummingBirdTest Passed");
    }

    @Test
    public void idiotTest() {
        Function<Integer, Integer> idiot = new IdiotImpl().build();
        int res = idiot.apply(2);
        Assert.assertEquals(res, 2);

        System.out.println("✅ idiotTest Passed");
    }

    @Test
    public void kestrelTest() {
        BiFunction<Integer, Integer, Integer> kestrel = new KestrelImpl().build();
        int res = kestrel.apply(2, 3);
        Assert.assertEquals(res, 2);

        System.out.println("✅ kestrelTest Passed");
    }

    @Test
    public void kiteTest() {
        BiFunction<Integer, Integer, Integer> kite = new KiteImpl().build();
        int res = kite.apply(2, 3);
        Assert.assertEquals(res, 3);

        System.out.println("✅ kiteTest Passed");
    }

    @Test
    public void starlingTest() {
        TriFunction<BiFunction<Integer, Integer, Integer>, Function<Integer, Integer>, Integer, Integer> starling = new StarlingImpl().build();
        int res = starling.apply(sum, addOne, 4);
        Assert.assertEquals(res, 9);

        System.out.println("✅ starlingTest Passed");
    }

    @Test
    public void thrushTest() {
        BiFunction<Integer, Function<Integer, Integer>, Integer> thrush = new ThrushImpl().build();
        int res = thrush.apply(1, addOne);
        Assert.assertEquals(res, 2);

        System.out.println("✅ thrushTest Passed");
    }

    @Test
    public void vireoTest() {
        TriFunction<Integer, Integer, BiFunction<Integer, Integer, Integer>, Integer> vireo = new VireoImpl().build();
        int res = vireo.apply(2, 3, sum);
        Assert.assertEquals(res, 5);

        System.out.println("✅ vireoTest Passed");
    }
}
class ApplyImpl implements Apply<Integer, Integer> {}
class BlackbirdImpl implements Blackbird<Integer, Integer, Integer> {}
class BluebirdImpl implements Bluebird<Integer, Integer> {}
class CardinalImpl implements Cardinal<Integer, Integer, Integer> {}
class DoveImpl implements Dove<Integer, Integer, Integer> {}
class FinchImpl implements Finch<Integer, Integer, Integer> {}
class GoldFinchImpl implements GoldFinch<Integer, Integer, Integer> {}
class HummingbirdImpl implements Hummingbird<Integer, Integer, Integer> {}
class IdiotImpl implements Idiot<Integer> {}
class KestrelImpl implements Kestrel<Integer, Integer> {}
class KiteImpl implements Kite<Integer, Integer> {}
class StarlingImpl implements Starling<Integer, Integer> {}
class ThrushImpl implements Thrush<Integer, Integer> {}
class VireoImpl implements Vireo<Integer, Integer, Integer> {}