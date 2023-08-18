package test;

import com.simplj.lambda.data.ImmutableList;
import com.simplj.lambda.data.MutableList;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class ListTest {
    public static void main(String[] args) {
        int count = 1_000_000;
        testList(count);
        testMutableList(count);
        testImmutableList(count);
    }

    private static void testList(int count) {
        List<Integer> list = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            list.add(i);
        }

        long s = System.currentTimeMillis();
        long c = list.stream().map(x -> x + 1).flatMap(x -> Stream.of(-x, x)).filter(x -> x > 0).count();
        long e = System.currentTimeMillis();
        System.out.println("List: " + (e - s) + " ms | Result: " + c);
    }

    private static void testMutableList(int count) {
        MutableList<Integer> mList = MutableList.unit(LinkedList::new);
        for (int i = 0; i < count; i++) {
            mList.add(i);
        }

        long s = System.currentTimeMillis();
        long c = mList.map(x -> x + 1).flatmap(x -> Arrays.asList(-x, x)).filter(x -> x > 0).size();
        long e = System.currentTimeMillis();
        System.out.println("mList: " + (e - s) + " ms | Result: " + c);
    }

    private static void testImmutableList(int count) {
        List<Integer> list = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            list.add(i);
        }
        ImmutableList<Integer> iList = ImmutableList.of(list, LinkedList::new);

        long s = System.currentTimeMillis();
        long c = iList.map(x -> x + 1).flatmap(x -> Arrays.asList(-x, x)).filter(x -> x > 0).applied().size();
        long e = System.currentTimeMillis();
        System.out.println("iList: " + (e - s) + " ms | Result: " + c);
    }
}
