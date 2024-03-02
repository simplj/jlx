package test;

import com.simplj.lambda.data.IArray;
import com.simplj.lambda.data.IList;
import com.simplj.lambda.data.MArray;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.util.Expr;
import com.simplj.lambda.util.Try;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.simplj.lambda.util.Expr.let;

public class Test {
    public static void main(String[] args) {
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


        Function<Object, String> dummy = Function.returning("dummy");
        Stream.of(1, 2, 3).map(dummy).collect(Collectors.toList()).forEach(System.out::println);

//        Try.execute(() -> Thread.sleep(5)).log(System.out::println).mapException(RuntimeException::new).resultOrThrow();
    }
}
