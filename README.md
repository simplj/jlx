# jlx [![License](https://img.shields.io/badge/License-BSD_3--Clause-blue.svg)](https://opensource.org/licenses/BSD-3-Clause) [![Maven Central](https://img.shields.io/maven-central/v/com.simplj.lambda/jlx.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.simplj.lambda%22%20AND%20a:%22jlx%22) [![javadoc](https://javadoc.io/badge2/com.simplj.lambda/jlx/javadoc.svg)](https://javadoc.io/doc/com.simplj.lambda/jlx) [![Build](https://github.com/simplj/jlx/actions/workflows/maven.yml/badge.svg)](https://github.com/simplj/jlx/actions/workflows/maven.yml) [![Code Coverage](https://github.com/simplj/jlx/actions/workflows/jacoco.yml/badge.svg)](https://github.com/simplj/jlx/actions/workflows/jacoco.yml)
Java Lambda eXpressions

## Key Highlights
Let's see the key hightlights of JLX.

* ### Exectuable\<I, O>
  A piece of code which can execute. It takes an input of type `I` and produces an output of type `O`. This is a `FunctionalInterface`, hence can be written as `lambda functions`. Now, you may ask that we already have `java.util.function.Function<I, O>` in java and why we need another `FunctionalInterface`? Well, the answer is, we cannot pass unsafe code (i.e. code which can throw `Exception`) directly into a `Function` without handling it but we can do this with an `Executable`. Java's `Function` does not work with lambda throwing `Exception` because `Function`s are meant to be pure, but as we all know "Nothing pure is ever useful". `Executable` can also be converted to a "pure function" by calling it's `pure` API. `pure` returns an `Either<Exception, R>` which either contains `Exception` (if occurred during execution) in the left or the resultant value in the right. Enough talking, now, let's do some coding to understand the difference:
  > ```java
  > class CannotDivideByZeroException extends Exception {...}
  > 
  > //an unsafe method which throws a checked exception when input number is odd
  > public int unsafe(int num) throws IllegalArgumentException {
  >   if (num % 2 != 0) throw new IllegalArgumentException();
  >   return someProcessWithNum(num);
  > }
  > 
  > //Using Function:
  > Function<Integer, Integer> dividerF = n -> {
  >   int res;
  >   try {
  >     res = unsafe(n);
  >   } catch (IllegalArgumentException ex) {
  >     res = 0;
  >   }
  >   return res;
  > }
  > 
  > //Using Executable:
  > Executable<Integer, Integer> dividerE = n -> unsafe(n); //or using method reference: this::divide
  > //pure
  > Function<Integer, Either<Exception, Integer>> dividerP = dividerE.pure();
  > ```
  > In the above example the method `divide` throws a checked exception and to use it in a `Function` we have to wrap it inside `try-catch` and on the other hand we can simply use the unsafe method `divide` with no exception handling using `Executable`.
  > 
  > Okay, the next question could be, how is the exception handled then in case of using `Executable`? The answer is, at the time of execution i.e. when I am executing the exectuable I need to handle the exception. This makes sense because exception occurs while executing not while defining. Following code explains execution of `Executable` vs `Function`.
  > ```java
  > int a = //user input
  > 
  > //Using Function
  > int res = divididerF.apply(a); //We don't know if exception occurred or not (I know we can handle using better design but that will add extra complexity to the simple code)
  > 
  > //Using Executable
  > try {
  >   int res = dividerE.execute(a);
  > } catch (Exception ex) {
  >   //Boom, exception occured while exeucuting, we got that information and can handle that here.
  > }
  > ```
  Similar to Executable, there are,
    - **BiExecutable:** Takes 2 arguments and returns a result
    - **TriExecutable:** Takes 3 arguments and returns a result
    - **QuadExecutable:** Takes 4 arguments and returns a result
    - **Receiver:** Takes 1 argument and does not return anything (Much like Consumer except that Receiver works on a lambda which can throw Exception)
    - **BiReceiver:** Takes 2 arguments and does not return anything
    - **TriReceiver:** Takes 3 arguments and does not return anything
    - **QuadReceiver:** Takes 4 arguments and does not return anything
    - **Provider:** Does not take any argument but returns a value (Much like Producer (or Supplier) except that Provider works on a lambda which can throw Exception)
    - **Excerpt:** Neither takes any argument or returns any value. It is like executing an excerpt of code lazily.


* ### package "com.simplj.lambda.functions"
  `com.simplj.lambda.functions` contains the pure functions similar to java `Function`s except jlx functions provide more comprehensive *lambda* functionalities like *'partial application of a function', 'currying', 'composing individual arguments'* etc.

__TO BE UPDATED ...__
