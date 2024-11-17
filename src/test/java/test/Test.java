package test;

import com.simplj.lambda.data.IArray;
import com.simplj.lambda.data.IList;
import com.simplj.lambda.function.Condition;

public class Test {
    public static void main(String[] args) {
        /*Mutable<Integer> m = Mutable.of(0, (a, b) -> System.out.println(a + " -> " + b));
        IntStream.range(0, 5).forEach(x -> m.mutate(i -> i + 1));*/
        IList<Integer> arr = IList.of(1, 2, 3, 4, 5);
        System.out.println(arr.drop(3));
        System.out.println(arr.drop(-3));
        System.out.println(arr.drop(0));
        System.out.println(arr.drop(5));
        System.out.println("----------------");
        System.out.println(arr.dropWhile(Condition.equal(0)));
        System.out.println(arr.dropWhile(Condition.never()));
        System.out.println(arr.dropWhile(Condition.always()));
        System.out.println(arr.dropWhile(Condition.lesser(3)));
        System.out.println("=================");
        System.out.println(arr.take(3));
        System.out.println(arr.take(-3));
        System.out.println(arr.take(0));
        System.out.println(arr.take(5));
        System.out.println("----------------");
        System.out.println(arr.takeWhile(Condition.equal(0)));
        System.out.println(arr.takeWhile(Condition.never()));
        System.out.println(arr.takeWhile(Condition.always()));
        System.out.println(arr.takeWhile(Condition.lesser(3)));
//        String s = "a/b";
//        List<String> l = IList.of(s.split("/")).map(String::trim).list();
//        System.out.println(l);
//        l = IArray.of(s.split("/")).map(String::trim).toList();
//        System.out.println(l);
//        String s1 = IArray.of(s.split("/")).map(String::trim).get(0);
//        System.out.println(s1);
//        s1 = MArray.of(s.split("/")).map(String::trim).get(1);
//        System.out.println(s1);
//        Stream<String> stream = IArray.of(s.split("/")).map(String::trim).stream();
//        String[] arr = IArray.of(s.split("/")).map(String::trim).toArray(new String[0]);
//        System.out.println(Arrays.toString(arr));
//        arr = MArray.of(s.split("/")).map(String::trim).toArray(new String[0]);
//        System.out.println(Arrays.toString(arr));
//
//        int n = 1;
//        let(n).pure()
//                .when(1).then(i -> String.valueOf(n))
//                .when(2).then(String::valueOf)
//                .otherwise(Function.returning("invalid"));


//        Function<Object, String> dummy = Function.returning("dummy");
//        Stream.of(1, 2, 3).map(dummy).collect(Collectors.toList()).forEach(System.out::println);

//        Try.execute(() -> Thread.sleep(5)).log(System.out::println).mapException(RuntimeException::new).resultOrThrow();

//        BiFunction<Integer, Integer, Integer> div = (a,b) -> a/b;
//
//        Function<Integer, Integer> half = div.flip().ap(2);
//        System.out.println(half.apply(6));

//        Function<Integer, Integer> partial = div.ap(6);
//        Integer partialRes = partial.apply(2);
//        System.out.println(partialRes);

//        Integer res = div.flip().apply(2, 4);
//        System.out.println(res);
    }
}
