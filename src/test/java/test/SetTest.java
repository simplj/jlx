package test;

import com.simplj.lambda.data.ImmutableSet;
import com.simplj.lambda.data.MutableSet;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class SetTest {
    public static void main(String[] args) {
        int count = 1_000_000;
        testSet(count);
        testMutableSet(count);
        testImmutableSet(count);
    }

    private static void testSet(int count) {
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < count; i++) {
            set.add(i);
        }

        long s = System.currentTimeMillis();
        long c = set.stream().map(x -> x + 1).flatMap(x -> Stream.of(-x, x)).filter(x -> x > 0).count();
        long e = System.currentTimeMillis();
        System.out.println("List: " + (e - s) + " ms | Result: " + c);
    }

    private static void testMutableSet(int count) {
        MutableSet<Integer> mSet = MutableSet.unit(HashSet::new);
        for (int i = 0; i < count; i++) {
            mSet.add(i);
        }

        long s = System.currentTimeMillis();
        long c = mSet.map(x -> x + 1).flatmap(x -> set(-x, x)).filter(x -> x > 0).size();
        long e = System.currentTimeMillis();
        System.out.println("mSet: " + (e - s) + " ms | Result: " + c);
    }

    private static void testImmutableSet(int count) {
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < count; i++) {
            set.add(i);
        }
        ImmutableSet<Integer> iSet = ImmutableSet.of(set, HashSet::new);

        long s = System.currentTimeMillis();
        long c = iSet.map(x -> x + 1).flatmap(x -> set(-x, x)).filter(x -> x > 0).applied().size();
        long e = System.currentTimeMillis();
        System.out.println("iSet: " + (e - s) + " ms | Result: " + c);
    }

    private static Set<Integer> set(int y, int x) {
        Set<Integer> s = new HashSet<>();
        s.add(y);
        s.add(x);
        return s;
    }
}
