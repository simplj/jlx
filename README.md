# JLX — Java Lambda eXpressions: Complete Developer Documentation<br>[![License](https://img.shields.io/badge/License-BSD_3--Clause-blue.svg)](https://opensource.org/licenses/BSD-3-Clause) [![Maven Central](https://img.shields.io/maven-central/v/com.simplj.lambda/jlx.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.simplj.lambda%22%20AND%20a:%22jlx%22) [![javadoc](https://javadoc.io/badge2/com.simplj.lambda/jlx/javadoc.svg)](https://javadoc.io/doc/com.simplj.lambda/jlx) [![Build](https://github.com/simplj/jlx/actions/workflows/maven.yml/badge.svg)](https://github.com/simplj/jlx/actions/workflows/maven.yml) [![Tests](https://github.com/simplj/jlx/actions/workflows/jacoco.yml/badge.svg)](https://github.com/simplj/jlx/actions/workflows/jacoco.yml) [![Code Coverage](https://github.com/simplj/jlx/blob/coverage/.github/badges/jacoco.svg)](https://github.com/simplj/jlx/)

## Table of Contents

1. [Overview](#overview)
2. [Installation & Setup](#installation--setup)
3. [Quick Start](#quick-start)
4. [Pure Functions](#pure-functions)
5. [Exception-Aware Executables](#exception-aware-executables)
6. [Functional Collections](#functional-collections)
7. [Tuples](#tuples)
8. [Either — Disjoint Union Type](#either--disjoint-union-type)
9. [Try — Functional Exception Handling](#try--functional-exception-handling)
10. [Expr — Expression Blocks & Pattern Matching](#expr--expression-blocks--pattern-matching)
11. [Lazy — Deferred Initialization](#lazy--deferred-initialization)
12. [Mutable — Tracked Mutable State](#mutable--tracked-mutable-state)
13. [Timed — Execution Timing](#timed--execution-timing)
14. [Thunk — Lazy Computation Chain](#thunk--lazy-computation-chain)
15. [Retry Mechanism](#retry-mechanism)
16. [Sequences](#sequences)
17. [Sliding Window](#sliding-window)
18. [Utility Helpers](#utility-helpers)
19. [Error Handling & Exceptions](#error-handling--exceptions)
20. [Configuration Reference](#configuration-reference)
21. [Thread Safety & Concurrency Notes](#thread-safety--concurrency-notes)
22. [FAQ / Troubleshooting](#faq--troubleshooting)

---

## Overview

JLX (Java Lambda eXpressions) is a functional programming library for Java 8+ that addresses two fundamental gaps in the standard library:

1. **Exception-safe lambdas.** Java's `java.util.function.*` interfaces cannot hold lambdas that throw checked exceptions. JLX provides `Executable`, `Receiver`, `Provider`, and `Excerpt` as exception-aware equivalents of `Function`, `Consumer`, `Supplier`, and `Runnable`.

2. **Richer functional abstractions.** JLX adds currying, partial application, argument composition, `Either`, `Try`, `Thunk`, lazy evaluation, functional collections, and infinite sequences — all without pulling in a large framework dependency.

**Design philosophy:**
- Every abstraction has a *pure* escape hatch to convert unsafe code to `Either`-returning safe code.
- Immutable (`I*`) and Mutable (`M*`) collection variants share the same rich API surface.
- The library is zero-dependency (JUnit only in test scope) and targets Java 8.

**When to use JLX:**
- You want to pass checked-exception-throwing methods as lambdas without `try/catch` boilerplate.
- You want a fluent, composable `Try`/`Either`/`Thunk` pipeline in place of nested `try-catch` blocks.
- You need currying, partial application, or argument flip for multi-argument functions.
- You want immutable-first collection operations that are chainable and lazy.

*See Also:* [Exception-Aware Executables](#exception-aware-executables), [Try — Functional Exception Handling](#try--functional-exception-handling)

---

## Installation & Setup

**Minimum Java version:** Java 8

### Maven

```xml
<dependency>
    <groupId>com.simplj.lambda</groupId>
    <artifactId>jlx</artifactId>
    <version><!-- check Maven Central for the latest release --></version>
</dependency>
```

Check the current release at [Maven Central](https://search.maven.org/search?q=g:%22com.simplj.lambda%22%20AND%20a:%22jlx%22).

### Gradle

```groovy
implementation 'com.simplj.lambda:jlx:<version>'
```

### No additional configuration required.

The library is a plain JAR with no runtime dependencies.

---

## Quick Start

A five-minute end-to-end example showing the library's core value: passing unsafe code directly as a lambda, chaining transformations, and handling exceptions cleanly.

```java
import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.util.Try;
import com.simplj.lambda.data.IList;
import com.simplj.lambda.tuples.Tuple;
import com.simplj.lambda.tuples.Couple;

public class QuickStart {

    // A method that throws a checked exception — cannot go into java.util.function.Function directly
    static int parseAndDouble(String s) throws NumberFormatException {
        return Integer.parseInt(s) * 2;
    }

    public static void main(String[] args) throws Exception {

        // 1. Wrap the unsafe method as an Executable — no try/catch needed here
        Executable<String, Integer> doubler = QuickStart::parseAndDouble;

        // 2. Convert to a pure Function that returns Either<Exception, Integer>
        Function<String, Either<Exception, Integer>> safe = doubler.pure();

        Either<Exception, Integer> result = safe.apply("21");
        System.out.println(result.isRight()); // true
        System.out.println(result.right());   // 42

        Either<Exception, Integer> failure = safe.apply("abc");
        System.out.println(failure.isLeft()); // true

        // 3. Use Try for a fluent pipeline with recovery
        Integer value = Try.execute(() -> parseAndDouble("10"))
                           .recover(ex -> 0)
                           .resultOrThrow();
        System.out.println(value); // 20

        // 4. Functional immutable list with map
        IList<String> inputs = IList.of("1", "2", "3");
        IList<Integer> doubled = inputs.map(s -> Integer.parseInt(s) * 2);
        System.out.println(doubled.list()); // [2, 4, 6]

        // 5. Tuple for grouping results
        Couple<String, Integer> pair = Tuple.of("answer", 42);
        System.out.println(pair.first() + " = " + pair.second()); // answer = 42
    }
}
```

---

## Pure Functions

**Package:** `com.simplj.lambda.function`

These interfaces extend or mirror Java's standard `java.util.function.*` types but add **partial application**, **currying**, **argument composition**, and **flipping**. None of these interfaces throw checked exceptions; for exception-throwing lambdas use the [Executable](#exception-aware-executables) hierarchy.

### `Function<I, O>`

A single-argument pure function. Extends `java.util.function.Function<I, O>`.

```java
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Producer;

// Create from a lambda
Function<String, Integer> length = String::length;

// Apply
int n = length.apply("hello"); // 5

// Partial application — returns a Producer (zero-arg supplier)
Producer<Integer> fixedLength = length.ap("world");
int m = fixedLength.produce(); // 5

// Compose: g.compose(f) == f -> g
Function<Integer, String> intToStr = i -> "value:" + i;
Function<String, String> composed = intToStr.compose(length); // String -> Integer -> String

// Chain: andThen
Function<String, String> chained = length.andThen(intToStr); // same as above

// Identity
Function<String, String> id = Function.id();

// Constant function — always returns r regardless of input
Function<String, Integer> always42 = Function.returning(42);
```

| Method | Returns | Description |
|--------|---------|-------------|
| `apply(I input)` | `O` | Applies the function to `input` |
| `ap(I i)` | `Producer<O>` | Partial application — fixes the argument, returns a zero-arg producer |
| `compose(Function<T,I> before)` | `Function<T,O>` | `before` runs first, then this function |
| `andThen(Function<O,R> after)` | `Function<I,R>` | This runs first, then `after` |
| `Function.id()` | `Function<T,T>` | Identity function |
| `Function.of(Function<T,R> f)` | `Function<T,R>` | Wraps a lambda as a `Function` (useful for method references) |
| `Function.returning(R r)` | `Function<T,R>` | Constant function — ignores input, always returns `r` |

---

### `BiFunction<A, B, O>`

Two-argument pure function. Extends `java.util.function.BiFunction<A,B,O>`.

```java
import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);

// Apply
String r = repeat.apply("ab", 3); // "ababab"

// Partial application — fix first argument
Function<Integer, String> repeatHello = repeat.ap("hello");
String s = repeatHello.apply(2); // "hellohello"

// Currying — A -> (B -> O)
Function<String, Function<Integer, String>> curried = repeat.curried();
String t = curried.apply("x").apply(4); // "xxxx"

// Flip arguments
BiFunction<Integer, String, String> flipped = repeat.flip();
String u = flipped.apply(2, "ab"); // "ababab"

// Substitute: provide both arguments via a function producing a Couple
Function<String, String> substitute = repeat.substitute(
    raw -> Tuple.of(raw, raw.length())
);
String v = substitute.apply("hi"); // "hihi"

// Compose individual arguments
BiFunction<Integer, Integer, String> composeFirst =
    repeat.composeFirst((Integer i) -> "x".repeat(i));
```

| Method | Returns | Description |
|--------|---------|-------------|
| `apply(A a, B b)` | `O` | Applies the function |
| `ap(A a)` | `Function<B,O>` | Fixes the first argument |
| `composeFirst(Function<T,A> f)` | `BiFunction<T,B,O>` | Pre-transforms the first argument |
| `composeSecond(Function<T,B> f)` | `BiFunction<A,T,O>` | Pre-transforms the second argument |
| `andThen(Function<O,R> after)` | `BiFunction<A,B,R>` | Post-transforms the result |
| `substitute(Function<I,Couple<A,B>> f)` | `Function<I,O>` | Collapses to a single-arg function by extracting both args from a `Couple` |
| `curried()` | `Function<A,Function<B,O>>` | Curries this function |
| `flip()` | `BiFunction<B,A,O>` | Swaps argument order |
| `BiFunction.first()` | `BiFunction<T,R,T>` | Returns its first argument |
| `BiFunction.second()` | `BiFunction<T,R,R>` | Returns its second argument |
| `BiFunction.returning(R r)` | `BiFunction<X1,X2,R>` | Constant bi-function |

---

### `TriFunction<A, B, C, R>`, `QuadFunction<A, B, C, D, R>`, `PentaFunction<A, B, C, D, E, R>`

Three-, four-, and five-argument pure functions. Each provides the same composition pattern as `BiFunction`: `ap`, `composeFirst/Second/Third/…`, `andThen`, `substitute`, `curried`, and static `first()`, `second()`, …, `fifth()` projection factories.

```java
import com.simplj.lambda.function.TriFunction;

TriFunction<Integer, Integer, Integer, Integer> sum3 = (a, b, c) -> a + b + c;

// Partial application — returns BiFunction<B,C,R>
com.simplj.lambda.function.BiFunction<Integer, Integer, Integer> sum2 = sum3.ap(10);
int result = sum2.apply(20, 30); // 60

// Curry: A -> (B -> (C -> R))
Function<Integer, Function<Integer, Function<Integer, Integer>>> curried = sum3.curried();
int v = curried.apply(1).apply(2).apply(3); // 6
```

---

### `Condition<A>`

A predicate that extends `java.util.function.Predicate<A>`. Provides factory methods for common comparisons.

```java
import com.simplj.lambda.function.Condition;

Condition<Integer> positive = n -> n > 0;

boolean b1 = positive.evaluate(5);  // true
boolean b2 = positive.evaluate(-1); // false

// Negate
Condition<Integer> nonPositive = positive.negate();

// Compose: pre-transform the input
Condition<String> nonEmpty = positive.compose(String::length);
boolean b3 = nonEmpty.evaluate("hi"); // true

// Built-in comparisons (requires Comparable)
Condition<Integer> between5and10 = Condition.between(5, 10);
Condition<Integer> lessThan0     = Condition.lesser(0);
Condition<Integer> greaterEq5    = Condition.greaterOrEqual(5);
Condition<String>  alwaysTrue    = Condition.always();
Condition<String>  alwaysFalse   = Condition.never();

// Membership
Condition<String> isWeekend = Condition.in("Saturday", "Sunday");
```

| Factory Method | Description |
|----------------|-------------|
| `Condition.always()` | Always returns `true` |
| `Condition.never()` | Always returns `false` |
| `Condition.lesser(T n)` | `value < n` |
| `Condition.lesserOrEqual(T n)` | `value <= n` |
| `Condition.equal(T n)` | `value.compareTo(n) == 0` |
| `Condition.greater(T n)` | `value > n` |
| `Condition.greaterOrEqual(T n)` | `value >= n` |
| `Condition.between(T from, T to)` | `from <= value <= to` |
| `Condition.in(T... values)` | `value` equals one of the listed values |

---

### `BiCondition<A, B>`

A two-argument predicate.

```java
import com.simplj.lambda.function.BiCondition;
import com.simplj.lambda.function.Condition;

BiCondition<String, Integer> longerThan = (s, n) -> s.length() > n;

boolean b = longerThan.evaluate("hello", 3); // true

// Partial application — fix first arg
Condition<Integer> longerThan5 = longerThan.eval("hello");

// Factory helpers
BiCondition<String, Integer> onFirst  = BiCondition.ofFirst(Condition.never());
BiCondition<String, Integer> onSecond = BiCondition.ofSecond(Condition.greater(0));
BiCondition<String, Integer> either   = BiCondition.any(s -> !s.isEmpty(), n -> n > 0);
BiCondition<String, Integer> both     = BiCondition.both(s -> !s.isEmpty(), n -> n > 0);
```

---

### `Consumer<A>`, `BiConsumer<A, B>`, `TriConsumer<A, B, C>`, `QuadConsumer<A, B, C, D>`, `PentaConsumer<A, B, C, D, E>`

Side-effecting functions that return `void`. Each extends or mirrors its Java standard counterpart.

```java
import com.simplj.lambda.function.Consumer;
import com.simplj.lambda.function.BiConsumer;
import com.simplj.lambda.function.Function;

Consumer<String> printer = System.out::println;
printer.consume("hello"); // prints "hello"

// Partial application — returns a Snippet (zero-arg Runnable-like)
com.simplj.lambda.function.Snippet fixed = printer.cons("world");
fixed.apply();

// Convert to a Function that yields its input (identity side-effect)
Function<String, String> yielding = printer.yield(); // prints then returns input

// Convert to a Function<String, Void>
Function<String, Void> asFunction = printer.toFunction();

// No-op
Consumer<String> noOp = Consumer.noOp();

// BiConsumer with currying
BiConsumer<String, Integer> printTimes = (s, n) -> {
    for (int i = 0; i < n; i++) System.out.println(s);
};
Function<String, Consumer<Integer>> curried = printTimes.curried();
curried.apply("hi").consume(3); // prints "hi" three times
```

---

### `Producer<R>`

A zero-argument pure function (value source). Extends `java.util.function.Supplier<R>`.

```java
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.function.Function;

Producer<String> greeting = () -> "Hello";
String s = greeting.produce(); // "Hello"

// Chain
Producer<Integer> length = greeting.andThen(String::length);
int n = length.produce(); // 5

// Constant
Producer<String> always42 = Producer.defer("fixed");
```

---

### `Snippet`

A zero-argument, void-return functional interface (like `Runnable` with no exception). The pure counterpart of `Excerpt`.

```java
import com.simplj.lambda.function.Snippet;

Snippet s = () -> System.out.println("running");
s.apply();

// Convert to Consumer, Producer, or Function
com.simplj.lambda.function.Consumer<String>  asConsumer  = s.toConsumer();
com.simplj.lambda.function.Producer<Integer> asProducer  = s.toProducer();
com.simplj.lambda.function.Function<Integer, Integer> yield = s.yield();

// No-op
Snippet noop = Snippet.numb();
```

*See Also:* [Exception-Aware Executables — Excerpt](#exception-aware-executables)

---

## Exception-Aware Executables

**Package:** `com.simplj.lambda.executable`

These interfaces are the exception-throwing counterparts of the function hierarchy. They allow you to use lambdas that declare `throws Exception` without wrapping in `try/catch` at the definition site.

| `function` type | `executable` type |
|----------------|------------------|
| `Function<I,O>` | `Executable<I,O>` |
| `BiFunction<A,B,R>` | `BiExecutable<A,B,R>` |
| `TriFunction<A,B,C,R>` | `TriExecutable<A,B,C,R>` |
| `QuadFunction<A,B,C,D,R>` | `QuadExecutable<A,B,C,D,R>` |
| `PentaFunction<A,B,C,D,E,R>` | `PentaExecutable<A,B,C,D,E,R>` |
| `Consumer<A>` | `Receiver<A>` |
| `BiConsumer<A,B>` | `BiReceiver<A,B>` |
| `TriConsumer<A,B,C>` | `TriReceiver<A,B,C>` |
| `QuadConsumer<A,B,C,D>` | `QuadReceiver<A,B,C,D>` |
| `PentaConsumer<A,B,C,D,E>` | `PentaReceiver<A,B,C,D,E>` |
| `Producer<R>` | `Provider<R>` |
| `Snippet` | `Excerpt` |

---

### `Executable<I, O>`

```java
import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.executable.Provider;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.util.Either;

// Any method that throws Exception can be wrapped directly
Executable<String, Integer> parse = Integer::parseInt;

// Execute — caller must handle Exception
try {
    int n = parse.execute("42"); // 42
} catch (Exception e) {
    // handle
}

// Partial application — returns a Provider (exception-throwing Supplier)
Provider<Integer> fixed = parse.exec("10");
int n = fixed.provide(); // may throw

// Convert to a pure, exception-free Function returning Either
Function<String, Either<Exception, Integer>> safe = parse.pure();
Either<Exception, Integer> result = safe.apply("abc");
System.out.println(result.isLeft()); // true — NumberFormatException in left

// Compose / andThen
Executable<String, String> doubled = parse.andThen(i -> String.valueOf(i * 2));

// Add retry
import com.simplj.lambda.util.retry.RetryContext;
RetryContext ctx = RetryContext.times(100L, delay -> delay * 2, 3).build();
Executable<String, Integer> withRetry = parse.withRetry(ctx);

// Static factories
Executable<Integer, Integer> identity = Executable.id();
Executable<String, Integer> constant  = Executable.returning(99);
```

| Method | Returns | Description |
|--------|---------|-------------|
| `execute(I input)` | `O` | Runs the computation; throws `Exception` on failure |
| `exec(I i)` | `Provider<O>` | Partial application — fixes the input |
| `pure()` | `Function<I, Either<Exception,O>>` | Wraps the result in `Either`; never throws |
| `withRetry(RetryContext ctx)` | `Executable<I,O>` | Retries on exception per the `RetryContext` policy |
| `withRetry(ResettableRetryContext<I> ctx)` | `Executable<I,O>` | Retries and resets the input before each retry |
| `compose(Executable<T,I> before)` | `Executable<T,O>` | Runs `before` first |
| `andThen(Executable<O,R> after)` | `Executable<I,R>` | Runs `after` on the result |
| `Executable.id()` | `Executable<T,T>` | Identity |
| `Executable.returning(R r)` | `Executable<T,R>` | Constant |

---

### `BiExecutable<A, B, R>`

```java
import com.simplj.lambda.executable.BiExecutable;
import com.simplj.lambda.executable.Executable;

BiExecutable<Integer, Integer, Integer> divide = (a, b) -> {
    if (b == 0) throw new ArithmeticException("Division by zero");
    return a / b;
};

// Execute
int result = divide.execute(10, 2); // 5

// Partial application — returns Executable<B,R>
Executable<Integer, Integer> divide10 = divide.exec(10);
int r = divide10.execute(2); // 5

// Curry: A -> Executable<B, R>
Executable<Integer, Executable<Integer, Integer>> curried = divide.curried();
int v = curried.execute(10).execute(2); // 5

// Projections
BiExecutable<String, Integer, String> first  = BiExecutable.first();
BiExecutable<String, Integer, Integer> second = BiExecutable.second();
```

All `BiExecutable` methods (`composeFirst`, `composeSecond`, `andThen`, `substitute`, `yieldFirst`, `yieldSecond`, `toExecutable`) follow the same pattern as `BiFunction` but with `Executable` types.

---

### `TriExecutable<A, B, C, R>`, `QuadExecutable<A, B, C, D, R>`, `PentaExecutable<A, B, C, D, E, R>`

Three-, four-, and five-argument exception-aware functions. Each provides `exec`, `curried`, `composeFirst/Second/Third/…`, `andThen`, `substitute`, and static `first()`, `second()`, …, `fifth()`, `returning()` factories.

---

### `Receiver<A>`

Exception-throwing consumer. The `executable` counterpart of `Consumer<A>`.

```java
import com.simplj.lambda.executable.Receiver;
import com.simplj.lambda.executable.Executable;

Receiver<String> write = line -> {
    // could throw IOException
    System.out.println(line);
};

write.receive("data"); // throws Exception

// Partial application — returns Excerpt (zero-arg side-effect)
com.simplj.lambda.executable.Excerpt excerpt = write.re("hello");

// Convert to an Executable that returns its input (yield)
Executable<String, String> yielding = write.yield();

// Convert to Executable<String, Void>
Executable<String, Void> asExec = write.toExecutable();

// No-op
Receiver<String> noOp = Receiver.noOp();
```

---

### `BiReceiver<A, B>`, `TriReceiver<A, B, C>`, `QuadReceiver<A, B, C, D>`, `PentaReceiver<A, B, C, D, E>`

Multi-argument exception-throwing consumers. Each provides `re` (partial application), `composeFirst/Second/…`, `curried`, `yieldFirst/yieldSecond/…`, `toExecutable`, and `noOp`.

---

### `Provider<R>`

Exception-throwing zero-argument value source. The `executable` counterpart of `Producer<R>`.

```java
import com.simplj.lambda.executable.Provider;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.util.Either;

Provider<String> readLine = () -> {
    // may throw IOException
    return new java.io.BufferedReader(new java.io.InputStreamReader(System.in)).readLine();
};

// Produce (may throw)
String line = readLine.provide();

// Convert to pure — returns Either<Exception, R>
Producer<Either<Exception, String>> safe = readLine.pure();

// Chain with an Executable
Provider<Integer> lineLength = readLine.andThen(String::length);

// Constant provider
Provider<String> constant = Provider.defer("fixed");
```

---

### `Excerpt`

Exception-throwing zero-argument side-effect. The `executable` counterpart of `Snippet`.

```java
import com.simplj.lambda.executable.Excerpt;
import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.executable.Receiver;
import com.simplj.lambda.executable.Provider;

Excerpt e = () -> {
    // may throw
    System.out.println("side effect");
};

e.execute(); // may throw

// Convert to other types
Executable<String, Void> asExec    = e.toExecutable();
Executable<String, String> yield   = e.yield();       // runs side-effect, returns input
Receiver<String> asReceiver        = e.toReceiver();  // runs side-effect, ignores input
Provider<String> asProvider        = e.toProvider();  // runs side-effect, provides null

// No-op
Excerpt noop = Excerpt.numb();
```

*See Also:* [Try — Functional Exception Handling](#try--functional-exception-handling), [Retry Mechanism](#retry-mechanism)

---

## Functional Collections

**Package:** `com.simplj.lambda.data`

JLX provides immutable (`I*`) and mutable (`M*`) wrappers around Java's standard collection types. Both variants share the same rich functional API. The `I*` types apply transformations lazily and return a new instance; the `M*` types mutate in place for standard `java.util.Collection` methods while also providing fluent, chainable `M*`-returning overloads.

**Key concept — lazy application:** `map`, `flatmap`, and `filter` on `I*` types do not execute immediately. Call `.applied()` to force evaluation or `.list()`/`.set()`/`.map()` to materialise the underlying Java collection (which also forces evaluation).

### `IList<E>` and `MList<E>`

Immutable and mutable list wrappers backed by `java.util.List`.

```java
import com.simplj.lambda.data.IList;
import com.simplj.lambda.data.MList;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.tuples.Couple;

// --- Construction ---
IList<Integer> empty   = IList.unit();                    // empty ArrayList-backed
IList<Integer> nums    = IList.of(1, 2, 3, 4, 5);
IList<String>  strs    = IList.of(java.util.Arrays.asList("a", "b", "c"));
IList<Integer> linked  = IList.unit(() -> new java.util.LinkedList<>());

// --- Transformation (lazy) ---
IList<Integer> doubled  = nums.map(n -> n * 2);           // lazy
IList<Integer> flat     = nums.flatmap(n -> IList.of(n, -n).list()); // lazy
IList<Integer> evens    = nums.filter(n -> n % 2 == 0);   // lazy

// Force evaluation
IList<Integer> applied  = doubled.applied();              // evaluates now
java.util.List<Integer> javaList = doubled.list();        // also forces evaluation

// --- Structural operations (each returns a new IList) ---
IList<Integer> appended = nums.append(6);
IList<Integer> inserted = nums.insert(0, 0);
IList<Integer> replaced = nums.replace(2, 99);            // replace at index 2
IList<Integer> deleted  = nums.delete(3);                 // delete at index 3
IList<Integer> sorted   = nums.sorted(java.util.Comparator.reverseOrder());

// --- Query operations ---
boolean anyEven = nums.any(n -> n % 2 == 0);   // true
boolean allPos  = nums.all(n -> n > 0);          // true
boolean noneNeg = nums.none(n -> n < 0);         // true
Integer found   = nums.find(n -> n > 3);         // 4 (first match)

// --- Indexed access ---
IList<Couple<Integer, Integer>> indexed = nums.indexed(); // [(0,1),(1,2),...]

// --- Element access ---
Integer first  = nums.first();  // 1
Integer last   = nums.last();   // 5
Integer middle = nums.mid();    // 3

// --- Slicing ---
IList<Integer> taken    = nums.filter(Condition.lesserOrEqual(3)); // keep ≤ 3
IList<Integer> dropped  = nums.filter(Condition.greater(2));

// --- Splitting ---
Couple<IList<Integer>, IList<Integer>> halves = nums.split(n -> n <= 3);
IList<Integer> leftHalf  = halves.first();  // [1, 2, 3]
IList<Integer> rightHalf = halves.second(); // [4, 5]

// --- Folding ---
int sum  = nums.foldl(0, (acc, n) -> acc + n);       // 15
int sumR = nums.foldr(0, (n, acc) -> n + acc);        // 15
int prod = nums.reduceL((a, b) -> a * b);             // 120

// --- Mutable list ---
MList<Integer> mutable = nums.mutable();
mutable.append(6).append(7);                // fluent append
mutable.delete(0);                          // fluent delete at index 0
IList<Integer> backToImmutable = mutable.immutable();
```

**Construction methods:**

| Method | Description |
|--------|-------------|
| `IList.none()` | `null`-valued placeholder list |
| `IList.unit()` | Empty `ArrayList`-backed list |
| `IList.unit(Producer<List<?>> constructor)` | Empty list with custom backing type |
| `IList.of(E... elems)` | Varargs factory |
| `IList.of(List<E> list)` | Wrap an existing list |
| `IList.from(Iterable<E> iter)` | Construct from any `Iterable` |

`MList` has the same factory methods (`MList.unit()`, `MList.of(...)`, `MList.from(...)`) plus all standard `java.util.List` mutation methods (`add`, `remove`, `set`, `addAll`, `clear`, `sort`, `replaceAll`).

**Gotcha:** Calling `.list()` on an unapplied `IList` triggers evaluation of all pending `map`/`filter` operations. If you chain many operations and never call `.applied()` or `.list()`, the computation accumulates but does not run.

---

### `ISet<E>` and `MSet<E>`

Immutable and mutable set wrappers backed by `java.util.Set`.

```java
import com.simplj.lambda.data.ISet;
import com.simplj.lambda.data.MSet;

ISet<String> fruits = ISet.of("apple", "banana", "cherry");

// Transformation
ISet<Integer> lengths = fruits.map(String::length);

// Include / delete
ISet<String> more = fruits.include("date");
ISet<String> less = fruits.delete("banana");

// Retain only elements in the given collection
ISet<String> kept = fruits.preserve(java.util.Arrays.asList("apple", "cherry"));

// Query
String found = fruits.find(s -> s.startsWith("b")); // "banana"

// Fold
int totalLength = fruits.fold(0, (acc, s) -> acc + s.length());

// Split
com.simplj.lambda.tuples.Couple<ISet<String>, ISet<String>> split =
    fruits.split(s -> s.length() > 5);

// Mutable
MSet<String> mutableSet = fruits.mutable();
mutableSet.include("elderberry").delete("apple");
ISet<String> back = mutableSet.immutable();
```

API mirrors `IList` for all query/transformation methods. Construction: `ISet.unit()`, `ISet.of(A... elems)`, `ISet.of(Set<A> set)`, `ISet.from(Iterable<E>)`.

---

### `IMap<K, V>` and `MMap<K, V>`

Immutable and mutable map wrappers backed by `java.util.Map`.

```java
import com.simplj.lambda.data.IMap;
import com.simplj.lambda.data.MMap;
import com.simplj.lambda.function.BiFunction;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;

IMap<String, Integer> scores = IMap.of(
    Tuple.of("Alice", 90),
    Tuple.of("Bob", 85),
    Tuple.of("Carol", 92)
);

// Map keys, values, or entries
IMap<String, String> graded = scores.mapV(n -> n >= 90 ? "A" : "B");
IMap<String, Integer> subset = scores.filterByValue(Condition.greaterOrEqual(90));

// Include / delete
IMap<String, Integer> updated = scores.include("Dave", 88);
IMap<String, Integer> trimmed = scores.delete("Bob");

// Replace
IMap<String, Integer> raised = scores.replacing("Alice", 95);

// Find
Couple<String, Integer> top = scores.findByValue(Condition.greaterOrEqual(92));
System.out.println(top.first() + ": " + top.second()); // Carol: 92

// Query predicates
boolean anyA = scores.anyValue(Condition.greaterOrEqual(90)); // true
boolean allPass = scores.allValues(Condition.greaterOrEqual(80)); // true

// Fold
int total = scores.fold(0, (acc, k, v) -> acc + v);

// Split
Couple<IMap<String, Integer>, IMap<String, Integer>> halves =
    scores.split((k, v) -> v >= 90);

// Merge entries into a set
ISet<String> labels = scores.merge((k, v) -> k + "=" + v);

// Mutable map
MMap<String, Integer> mutable = scores.mutable();
mutable.include("Eve", 91).delete("Bob");
IMap<String, Integer> back = mutable.immutable();
```

**Construction:** `IMap.unit()`, `IMap.of(Couple<A,B>... elems)`, `IMap.of(Map<A,B> map)`. Custom backing type: `IMap.unit(Producer<Map<?,?>> constructor)`.

---

### `IArray<E>` and `MArray<E>`

Typed array wrappers with functional map/flatmap. Also provides factories for primitive arrays.

```java
import com.simplj.lambda.data.IArray;
import com.simplj.lambda.data.MArray;

// From varargs or existing array
IArray<String> arr = IArray.of("x", "y", "z");

// From primitives (auto-boxed)
IArray<Integer> ints = IArray.of(1, 2, 3, 4, 5);

// Map
IArray<Integer> lengths = arr.map(String::length);

// Indexed
com.simplj.lambda.tuples.Couple<Integer, String>[] indexed = arr.indexed().array();

// Set a value (returns new immutable array)
IArray<String> updated = arr.set(1, "Y");

// Get
String val = arr.get(0); // "x"

// Force evaluation
IArray<Integer> applied = lengths.applied();

// Get underlying array
String[] raw = arr.array();

// Mutable
MArray<String> mutable = arr.mutable();
mutable.set(0, "X");
IArray<String> back = mutable.immutable();
```

**Primitive factories:** `IArray.of(int... arr)`, `IArray.of(long... arr)`, `IArray.of(double... arr)`, and analogous `MArray` variants.

*See Also:* [Sliding Window](#sliding-window), [Utility Helpers](#utility-helpers)

---

## Tuples

**Package:** `com.simplj.lambda.tuples`

Typed, immutable, heterogeneous containers from 2 to 8 elements. The `Tuple` class is a static factory; the concrete classes are `Couple` (2), `Triple` (3), `Quadruple` (4), `Pentuple` (5), `Hextuple` (6), `Septuple` (7), `Octuple` (8).

```java
import com.simplj.lambda.tuples.Tuple;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Triple;
import com.simplj.lambda.tuples.Quadruple;

// Construction
Couple<String, Integer>         pair    = Tuple.of("name", 42);
Triple<String, Integer, Boolean> triple = Tuple.of("name", 42, true);
Quadruple<String, Integer, Boolean, Double> quad =
    Tuple.of("name", 42, true, 3.14);
// ... up to Octuple with 8 elements

// Access
String  first  = pair.first();   // "name"
Integer second = pair.second();  // 42

// Extend a Couple to a Triple
Triple<String, Integer, Boolean> extended = pair.add(true);

// Non-destructive element replacement
Couple<String, Integer> renamed = pair.modifyFirst("newName");
Couple<String, Integer> valued  = pair.modifySecond(99);

// Equality and hashing work on element values
Couple<String, Integer> copy = Tuple.of("name", 42);
System.out.println(pair.equals(copy)); // true
```

**`Tuple2<A, B>` interface** — a structural interface that `Couple` implements. Use it when you only need the `first()` / `second()` contract without committing to `Couple`.

```java
import com.simplj.lambda.tuples.Tuple2;

Tuple2<String, Integer> t = Tuple2.of("hello", 5);
```

| Tuple Class | Accessor Methods |
|-------------|-----------------|
| `Couple<A,B>` | `first()`, `second()` |
| `Triple<A,B,C>` | `first()`, `second()`, `third()` |
| `Quadruple<A,B,C,D>` | `first()`, `second()`, `third()`, `fourth()` |
| `Pentuple<A,B,C,D,E>` | `first()`, `second()`, `third()`, `fourth()`, `fifth()` |
| `Hextuple<A,B,C,D,E,F>` | adds `sixth()` |
| `Septuple<A,B,C,D,E,F,G>` | adds `seventh()` |
| `Octuple<A,B,C,D,E,F,G,H>` | adds `eighth()` |

Each tuple class also has `modify<Position>(V newVal)` methods that return a new tuple with that element replaced.

*See Also:* [Functional Collections](#functional-collections) — `IMap.of(Couple<K,V>... elems)` uses `Couple` as entries.

---

## Either — Disjoint Union Type

**Package:** `com.simplj.lambda.util`

`Either<L, R>` holds exactly one of two values: a **Left** or a **Right**. By convention:
- **Left** = failure / error (often `Exception`)
- **Right** = success / result

It is the return type of `.pure()` on any `Executable` and the result type of `Try.result()`.

```java
import com.simplj.lambda.util.Either;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Consumer;

// Construction
Either<String, Integer> success = Either.right(42);
Either<String, Integer> failure = Either.left("something went wrong");

// Inspection
System.out.println(success.isRight()); // true
System.out.println(failure.isLeft());  // true

// Extraction (returns null if wrong side)
Integer value = success.right(); // 42
String  error = failure.left();  // "something went wrong"

// Map the Right side — Left passes through unchanged
Either<String, String> mapped = success.map(n -> "value:" + n);
// Right["value:42"]

// Flatmap the Right side
Either<String, Integer> doubled = success.flatmap(n ->
    n > 0 ? Either.right(n * 2) : Either.left("non-positive"));
// Right[84]

// Map the Left side
Either<Integer, Integer> leftMapped = failure.mapLeft(String::length);

// Transform both sides at once
Either<Integer, String> transformed = success.transform(
    leftVal -> leftVal.length(),
    rightVal -> "Result: " + rightVal
);

// FlatTransform — the right transformer returns an Either
Either<Integer, String> flatTransformed = success.flatTransform(
    leftVal -> leftVal.length(),
    rightVal -> Either.right("Result: " + rightVal)
);

// Side-effect on Right (records the value then returns same Either)
Either<String, Integer> recorded = success.record(v -> System.out.println("Got: " + v));

// Side-effect on Left
Either<String, Integer> recordedLeft = failure.recordLeft(e -> System.out.println("Error: " + e));

// Pattern on the result
if (success.isRight()) {
    System.out.println("Success: " + success.right());
} else {
    System.out.println("Failure: " + success.left());
}
```

| Method | Returns | Description |
|--------|---------|-------------|
| `Either.left(A a)` | `Either<A,B>` | Creates a Left |
| `Either.right(B b)` | `Either<A,B>` | Creates a Right |
| `isLeft()` | `boolean` | True when this is a Left |
| `isRight()` | `boolean` | True when this is a Right |
| `left()` | `L` | Returns Left value or `null` |
| `right()` | `R` | Returns Right value or `null` |
| `map(Function<R,A> f)` | `Either<L,A>` | Transforms Right; Left passes through |
| `flatmap(Function<R,Either<L,A>> f)` | `Either<L,A>` | Flatmaps Right; Left passes through |
| `mapLeft(Function<L,E> f)` | `Either<E,R>` | Transforms Left; Right passes through |
| `transform(Function<L,E> leftT, Function<R,A> rightT)` | `Either<E,A>` | Applies the appropriate transformer |
| `flatTransform(Function<L,E> leftT, Function<R,Either<E,A>> rightT)` | `Either<E,A>` | Like `transform` but flatmaps Right |
| `record(Consumer<R> consumer)` | `Either<L,R>` | Applies consumer to Right if present; returns `this` |
| `recordLeft(Consumer<L> consumer)` | `Either<L,R>` | Applies consumer to Left if present; returns `this` |

**Gotcha:** `map` and `flatmap` are **eager** — the function executes immediately when called.

*See Also:* [Try — Functional Exception Handling](#try--functional-exception-handling), [Exception-Aware Executables](#exception-aware-executables)

---

## Try — Functional Exception Handling

**Package:** `com.simplj.lambda.util`

`Try<A>` is the functional equivalent of a `try-catch` block. It is **lazy**: no code runs until you call `result()`, `resultOrThrow()`, `run()`, or `runOrThrowRE()`. The pipeline is built by chaining `map`, `recover`, `handle`, `log`, and `finalize`.

### Basic Usage

```java
import com.simplj.lambda.util.Try;
import com.simplj.lambda.util.Either;
import com.simplj.lambda.executable.Provider;

// Wrapping a Provider (zero-arg supplier that throws)
Try<Integer> t = Try.execute(() -> Integer.parseInt("42"));

// Materialise — returns Either<Exception, Integer>
Either<Exception, Integer> result = t.result();
System.out.println(result.right()); // 42

// Or throw if failed
Integer value = t.resultOrThrow(); // throws Exception on failure

// Run silently (suppress exception, log if .log() is set)
t.run();
```

### Pipeline Operations

```java
import com.simplj.lambda.util.Try;
import com.simplj.lambda.executable.Executable;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;

Try<Integer> pipeline = Try
    .execute(() -> Integer.parseInt(System.getProperty("count", "5")))
    .map(n -> n * 2)                          // transform result
    .filter(Condition.greaterOrEqual(0))       // filter — throws FilteredOutException if false
    .record(n -> System.out.println("value: " + n)) // side-effect on success
    .log(ex -> System.err.println("Error: " + ex))  // log exception if thrown
    .recover(ex -> -1)                         // recover from any exception
    .finalize(() -> System.out.println("done")); // always runs (like finally)

Either<Exception, Integer> r = pipeline.result();
```

### Exception Handling

```java
import com.simplj.lambda.util.Try;

// Handle a specific exception type
Try<Integer> specific = Try
    .execute(() -> Integer.parseInt("abc"))
    .handle(NumberFormatException.class, ex -> 0);  // recover when NFE

// Recover conditionally
Try<Integer> conditional = Try
    .execute(() -> riskyComputation())
    .recoverWhen(ex -> ex instanceof IllegalStateException, ex -> -1);

// Recover on specific class
Try<Integer> onClass = Try
    .execute(() -> riskyComputation())
    .recoverOn(IllegalArgumentException.class, ex -> 0);

// Map the exception type to a typed exception
Try.TypedTry<IllegalArgumentException, Integer> typed = Try
    .execute(() -> riskyComputation())
    .mapException(ex -> new IllegalArgumentException("wrapped", ex));

try {
    int v = typed.resultOrThrow(); // throws IllegalArgumentException
} catch (IllegalArgumentException e) {
    // type-safe catch
}
```

### Try-with-Resources

`Try` supports automatic resource closing via `AutoCloseableMarker`.

```java
import com.simplj.lambda.util.Try;

Try<String> resourceTry = Try.execute(marker -> {
    java.io.BufferedReader reader = marker.markForAutoClose(
        new java.io.BufferedReader(new java.io.FileReader("/tmp/data.txt"))
    );
    return reader.readLine();
});
// reader.close() is called automatically when result() is invoked
Either<Exception, String> result = resourceTry.result();
```

If closing fails, the result is `Either.left(AutoCloseException)`. Inspect it with `AutoCloseException.result()` (the original result) and `AutoCloseException.failedCloseableList()`.

### Retry

```java
import com.simplj.lambda.util.Try;
import com.simplj.lambda.util.retry.RetryContext;

Try<String> withRetry = Try
    .execute(() -> callUnstableApi())
    .retry(200L, 2.0, 3);   // initialDelay=200ms, multiplier=2.0, maxAttempts=3
// Delays: 200ms, 400ms, 800ms

// Or use a RetryContext for more control
RetryContext ctx = RetryContext.times(100L, d -> d + 100, 5).build();
Try<String> withCtx = Try.execute(() -> callUnstableApi()).retry(ctx);
```

### Flatten

```java
// Unwrap a Try<Try<A>> to Try<A>
Try<Integer> outer = Try.execute(() -> Try.execute(() -> 42));
Try<Integer> flat  = Try.flatten(outer);
```

| Method | Returns | Description |
|--------|---------|-------------|
| `Try.execute(Provider<R> f)` | `Try<R>` | Wraps a zero-arg supplier |
| `Try.execute(Excerpt f)` | `Try<Void>` | Wraps a zero-arg side-effect |
| `Try.execute(Receiver<AutoCloseableMarker> f)` | `Try<Void>` | Try-with-resources (side-effect) |
| `Try.execute(Executable<AutoCloseableMarker,R> f)` | `Try<R>` | Try-with-resources (returning) |
| `Try.flatExecute(Provider<Either<Exception,R>> f)` | `Try<R>` | Flattens a provider of `Either` |
| `Try.flatten(Try<R> t)` | `Try<R>` | Flattens a `Try<Try<R>>` |
| `map(Executable<A,R> f)` | `Try<R>` | Transforms result (lazy) |
| `flatmap(Executable<A,Try<R>> f)` | `Try<R>` | Flatmaps to another `Try` (lazy) |
| `filter(Condition<A> c)` | `Try<A>` | Filters result; throws `FilteredOutException` if false |
| `filter(Condition<A> c, Producer<X> exF)` | `Try<A>` | Filters with a custom exception producer |
| `filter(Condition<A> c, X ex)` | `Try<A>` | Filters with a specific exception |
| `record(Consumer<A> consumer)` | `Try<A>` | Side-effect on success (lazy) |
| `log(Consumer<Exception> f)` | `Try<A>` | Logs the exception if one occurs |
| `recover(Function<Exception,A> f)` | `Try<A>` | Recovers from any exception |
| `recoverWhen(Condition<Exception> c, Function<Exception,A> r)` | `Try<A>` | Recovers conditionally |
| `recoverOn(Class<? extends Exception> c, Function<Exception,A> r)` | `Try<A>` | Recovers for a specific exception class |
| `handle(Class<E> type, Function<E,A> f)` | `Try<A>` | Type-safe handler for one exception type |
| `mapException(Function<Exception,E> f)` | `TypedTry<E,A>` | Converts the exception type |
| `retry(RetryContext ctx)` | `Try<A>` | Adds retry behaviour |
| `retry(long delay, double mult, int max)` | `Try<A>` | Shorthand retry |
| `finalize(Excerpt f)` | `Try<A>` | Always-run cleanup (like `finally`) |
| `result()` | `Either<Exception,A>` | **Executes** and returns `Either` |
| `resultOrThrow()` | `A` | **Executes**; throws on failure |
| `run()` | `void` | **Executes** silently |
| `runOrThrowRE()` | `void` | **Executes**; wraps exception in `RuntimeException` |

**Gotcha:** Do not pass an `Executable.withRetry(...)` into `Try.execute(Executable<AutoCloseableMarker, R>)`. Use `Try.retry(...)` instead. Mixing the two can cause the `AutoCloseableMarker` to be reused across retries in an unexpected way.

*See Also:* [Either](#either--disjoint-union-type), [Retry Mechanism](#retry-mechanism)

---

## Expr — Expression Blocks & Pattern Matching

**Package:** `com.simplj.lambda.util`

`Expr<A>` and `PureExpr<A>` provide a scoped, expression-oriented way to work with a value — inspired by Scala's `val x = ... ; x match { ... }`. Use them when you want to perform a chain of operations on a local value without declaring a named variable, or when you want pattern-matching (`when`/`then`/`otherwise`) syntax.

`Expr<A>` works with `Executable` (can throw); `PureExpr<A>` works with `Function` (cannot throw).

### Basic Usage

```java
import com.simplj.lambda.util.Expr;

// Scope a value and transform it
String result = Expr.let("  hello  ")
    .map(s -> s.trim())              // may throw (Executable)
    .map(String::toUpperCase)
    .in(s -> s + "!");               // final extraction

// Or yield from a Provider
String yielded = Expr.let("hello")
    .yield(() -> "world");           // ignores the scoped value, provides a new one

// Or return a constant
String constant = Expr.let("anything").returning("fixed");

// Record a side-effect conditionally, then extract
String logged = Expr.let(System.getProperty("user.home"))
    .recordIf(s -> s != null, s -> System.out.println("home: " + s))
    .in(s -> s != null ? s : "/tmp");

// Throw an exception based on the value
Expr.let("invalid@@@input")
    .err(s -> new IllegalArgumentException("bad input: " + s));
```

### Pattern Matching with `when`/`then`/`otherwise`

```java
import com.simplj.lambda.util.Expr;

int x = 42;

String label = Expr.let(x)
    .when(v -> v < 0).then("negative")
    .when(v -> v == 0).then("zero")
    .otherwise("positive");
// "positive"

// Executable branches
String fileContent = Expr.let("/tmp/data.txt")
    .when(path -> path.endsWith(".txt")).then(path -> readFile(path))
    .otherwise(path -> "unsupported: " + path);

// Error branch
int strictValue = Expr.let(x)
    .when(v -> v > 0).then(v -> v * 2)
    .otherwiseErr(v -> new IllegalStateException("expected positive, got " + v));
```

### `PureExpr<A>` — exception-free variant

```java
import com.simplj.lambda.util.Expr;
import com.simplj.lambda.util.PureExpr;

PureExpr<Integer> pure = Expr.let(10).pure();
String result = pure
    .map(n -> n * 2)
    .in(n -> "value: " + n);
// "value: 20"
```

| Method | Returns | Description |
|--------|---------|-------------|
| `Expr.let(T val)` | `Expr<T>` | Scopes `val` for chained operations |
| `pure()` | `PureExpr<A>` | Converts to pure (exception-free) form |
| `in(Executable<A,B> f)` | `B` | Extracts result by applying `f`; may throw |
| `yield(Provider<B> p)` | `B` | Ignores current value, produces from `p` |
| `returning(B r)` | `B` | Ignores current value, returns `r` |
| `err(Function<A,E> f)` | `void` | Always throws the exception produced by `f` |
| `record(Receiver<A> consumer)` | `Expr<A>` | Side-effect; returns `this` |
| `recordIf(Condition<A> c, Receiver<A> consumer)` | `Expr<A>` | Conditional side-effect |
| `map(Executable<A,T> f)` | `Expr<T>` | Transforms the scoped value |
| `mapIf(Condition<A> c, Executable<A,A> f)` | `Expr<A>` | Transforms only when `c` is true |
| `when(A match)` | `Expr.When<A>` | Pattern match on equality |
| `when(Condition<A> c)` | `Expr.When<A>` | Pattern match on condition |

`Expr.When<A>` → `.then(R r)` / `.then(Executable<A,R> f)` / `.err(...)` → `Expr.Then<A,R>`  
`Expr.Then<A,R>` → `.when(...)` → `Expr.TypedWhen<A,R>` → `.then(...)` → `Expr.Then<A,R>` (chains)  
`Expr.Then<A,R>` → `.otherwise(R defVal)` / `.otherwise(Executable<A,R> f)` / `.otherwiseNull()` / `.otherwiseErr(...)` — terminates the chain.

---

## Lazy — Deferred Initialization

**Package:** `com.simplj.lambda.util`

`Lazy<A>` wraps an expensive initialization behind a `Producer` and evaluates it only on the first `get()` call. After the first evaluation the value is cached. Optionally, the cached value can be **restated** by a function whenever a condition is met.

```java
import com.simplj.lambda.util.Lazy;
import com.simplj.lambda.function.Producer;
import com.simplj.lambda.function.Function;
import com.simplj.lambda.function.Condition;

// Basic lazy initialization
Lazy<java.util.List<String>> lazy = Lazy.of(() -> {
    System.out.println("initializing..."); // runs only once
    return loadExpensiveList();
});

java.util.List<String> list1 = lazy.get(); // "initializing..." printed
java.util.List<String> list2 = lazy.get(); // no print — cached

// Lazy with restating: if the cached value is empty, re-initialize
Lazy<java.util.List<String>> reloading = Lazy.restating(
    QuickStart::loadExpensiveList,
    list -> list.isEmpty(),             // condition to restate
    list -> loadExpensiveList()         // restate function
);

// Queue a mutation to apply on next get()
Lazy<String> mutableLazy = Lazy.of(() -> "hello");
mutableLazy.mutate(String::toUpperCase); // not applied yet
String value = mutableLazy.get(); // "HELLO"
```

| Method | Returns | Description |
|--------|---------|-------------|
| `Lazy.of(Producer<T> lazyVal)` | `Lazy<T>` | Creates a lazy value |
| `Lazy.restating(Producer<T> lazyVal, Condition<T> condition, Function<T,T> restateF)` | `Lazy<T>` | Creates a lazy value that restates on condition |
| `mutate(Function<A,A> f)` | `Lazy<A>` | Schedules a transformation to apply on next `get()` |
| `get()` | `A` | Initialises (once), applies pending mutations, restates if needed |

**Thread safety:** `Lazy` uses `volatile` fields and `AtomicBoolean` for the initialised flag, but the initializer lambda itself may run more than once in a highly contested scenario. Do not rely on exactly-once semantics under concurrent access.

---

## Mutable — Tracked Mutable State

**Package:** `com.simplj.lambda.util`

`Mutable<A>` is an explicit, value-semantics mutable cell. Unlike a plain field, it supports conditional updates, functional transformations, and an optional `MutableWatcher` that fires when the value changes.

```java
import com.simplj.lambda.util.Mutable;
import com.simplj.lambda.util.MutableWatcher;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.function.Function;

// Basic usage
Mutable<Integer> counter = Mutable.of(0);
counter.set(1);                          // set unconditionally
counter.mutate(n -> n + 1);              // set to f(current)
int value = counter.get();               // 2

// Conditional set
counter.set(Condition.lesserOrEqual(5), 10); // set only if current <= 5
counter.mutate(Condition.greaterOrEqual(0), n -> n - 1); // decrement only if >= 0

// Change type
Mutable<String> asString = counter.change(n -> "count:" + n);
System.out.println(asString.get()); // "count:9" (or current value)

// With watcher — notified on every update
MutableWatcher<Integer> watcher = (previous, current) ->
    System.out.println("changed from " + previous + " to " + current);

Mutable<Integer> watched = Mutable.of(0, watcher);
watched.set(42); // prints "changed from 0 to 42"

// Copy
Mutable<Integer> copy = counter.copy();
```

| Method | Returns | Description |
|--------|---------|-------------|
| `Mutable.of(T val)` | `Mutable<T>` | Creates with an initial value |
| `Mutable.of(T val, MutableWatcher<T> watcher)` | `Mutable<T>` | Creates with watcher |
| `get()` | `A` | Returns the current value |
| `set(A val)` | `Mutable<A>` | Updates unconditionally |
| `set(Condition<A> condition, A val)` | `Mutable<A>` | Updates if `condition.evaluate(current)` is true |
| `mutate(Function<A,A> f)` | `Mutable<A>` | Sets to `f(current)` |
| `mutate(Condition<A> condition, Function<A,A> f)` | `Mutable<A>` | Sets to `f(current)` if condition is met |
| `change(Function<A,R> f)` | `Mutable<R>` | Produces a new `Mutable` of a different type |
| `change(Function<A,R> f, MutableWatcher<R> watcher)` | `Mutable<R>` | As above with a watcher on the new `Mutable` |
| `copy()` | `Mutable<A>` | Snapshot copy — not linked to the original |

**`MutableWatcher<A>`** — a `@FunctionalInterface` with `void onUpdate(A previous, A current)`. Implement it to react to value changes.

---

## Timed — Execution Timing

**Package:** `com.simplj.lambda.util`

`Timed` measures the wall-clock duration of a `Provider` or `Try` execution and bundles the result with the elapsed time.

```java
import com.simplj.lambda.util.Timed;
import com.simplj.lambda.util.Try;

// Eager timing — executes immediately, throws on failure
Timed.TimedExecution<String> te = Timed.execute(() -> {
    Thread.sleep(50);
    return "done";
});
System.out.println(te.result());   // "done"
System.out.println(te.duration()); // ~50 (milliseconds)

// Lazy timing — wraps a Try; apply() does not throw
Timed.TimedExecution<com.simplj.lambda.util.Either<Exception, String>> lazy =
    Timed.apply(() -> {
        Thread.sleep(30);
        return "ok";
    });

// Timing a Try block
Try<Integer> t = Try.execute(() -> Integer.parseInt("99"));
Timed.TimedExecution<com.simplj.lambda.util.Either<Exception, Integer>> timed =
    Timed.apply(t);
```

| Method | Returns | Description |
|--------|---------|-------------|
| `Timed.execute(Provider<T> provider)` | `TimedExecution<T>` | Runs immediately; throws on failure |
| `Timed.execute(Try<T> tryBlock)` | `TimedExecution<T>` | Runs a `Try` immediately; throws on failure |
| `Timed.apply(Producer<T> provider)` | `TimedExecution<Either<Exception,T>>` | Runs immediately; wraps failure in `Either.left` |
| `Timed.apply(Try<T> tryBlock)` | `TimedExecution<Either<Exception,T>>` | Runs a `Try`; wraps failure in `Either.left` |
| `TimedExecution.result()` | `R` | The result value |
| `TimedExecution.duration()` | `long` | Elapsed milliseconds |

---

## Thunk — Lazy Computation Chain

**Package:** `com.simplj.lambda.monadic`

`Thunk<A>` is a lazy monad: it holds a computation that is not executed until `result()` or `resultOrThrow()` is called. Operations like `map`, `flatmap`, `filter`, and `recover` build up a computation graph without running anything.

```java
import com.simplj.lambda.monadic.Thunk;
import com.simplj.lambda.util.Either;

// From a value
Thunk<Integer> t1 = Thunk.init(42);

// From a lazy Producer
Thunk<String> t2 = Thunk.of(() -> expensiveLoad());

// Build a computation chain — nothing runs yet
Thunk<String> chain = Thunk
    .of(() -> "  hello  ")
    .map(s -> s.trim())               // lazy
    .map(String::toUpperCase)         // lazy
    .filter(s -> !s.isEmpty())        // lazy — throws FilteredOutException if empty
    .record(s -> System.out.println("processing: " + s)); // lazy side-effect

// Execute
Either<Exception, String> result = chain.result();
System.out.println(result.right()); // "HELLO"

// Or throw on failure
String value = chain.resultOrThrow(); // throws if computation failed

// Flatmap to another Thunk
Thunk<Integer> composed = chain.flatmap(s -> Thunk.init(s.length()));

// Recovery
Thunk<String> safe = Thunk
    .of(() -> riskyLoad())
    .recover(ex -> "default");

Thunk<String> conditional = Thunk
    .of(() -> riskyLoad())
    .recoverWhen(ex -> ex instanceof java.io.IOException, ex -> "io-default");

Thunk<String> onClass = Thunk
    .of(() -> riskyLoad())
    .recoverOn(java.io.IOException.class, ex -> "io-default");

// Force evaluation and cache the result
Thunk<String> applied = chain.applied();
// Subsequent calls to result() on applied use the cached value
```

| Method | Returns | Description |
|--------|---------|-------------|
| `Thunk.init(T val)` | `Thunk<T>` | Wraps a constant value lazily |
| `Thunk.of(Producer<T> f)` | `Thunk<T>` | Wraps a lazy producer |
| `map(Executable<A,R> f)` | `Thunk<R>` | Adds a lazy transformation |
| `flatmap(Executable<A,Thunk<R>> f)` | `Thunk<R>` | Adds a lazy flatmap |
| `filter(Condition<A> f)` | `Thunk<A>` | Filters; `FilteredOutException` if false |
| `record(Receiver<A> r)` | `Thunk<A>` | Lazy side-effect |
| `recover(Function<Exception,A> f)` | `Thunk<A>` | Recovers from any exception |
| `recoverWhen(Condition<Exception> c, Function<Exception,A> f)` | `Thunk<A>` | Conditional recovery |
| `recoverOn(Class<? extends Exception> clazz, Function<Exception,A> f)` | `Thunk<A>` | Type-specific recovery |
| `result()` | `Either<Exception,A>` | **Executes** the chain and returns `Either` |
| `resultOrThrow()` | `A` | **Executes**; throws on failure |
| `applied()` | `Thunk<A>` | **Executes** and caches; returns a `Thunk` backed by the cached result |

**Difference from `Try`:** `Try` is designed for single-shot execution with resource management and retry. `Thunk` is a reusable lazy computation that can be re-evaluated multiple times (unless `applied()`). They both return `Either` and support `recover`, but `Thunk` has no `finalize` or `handle`.

**Gotcha:** Calling `equals()` or `hashCode()` on an unapplied `Thunk` throws `IllegalStateException`. Call `applied()` first.

---

## Retry Mechanism

**Package:** `com.simplj.lambda.util.retry`

The retry package provides a composable retry strategy that integrates with `Executable`, `Provider`, and `Try`.

### `RetryContext` and `RetryContextBuilder`

Three factory methods cover the most common strategies:

```java
import com.simplj.lambda.util.retry.RetryContext;
import com.simplj.lambda.function.Function;
import java.util.Collections;

// 1. Limit by attempt count
RetryContext byCount = RetryContext
    .times(200L, delay -> delay * 2, 5)   // initial=200ms, exponential backoff, max 5 retries
    .logger(msg -> System.out.println("[RETRY] " + msg))
    .build();

// 2. Limit by total elapsed duration
RetryContext byDuration = RetryContext
    .duration(100L, delay -> delay + 100, 3000L) // up to 3000ms total
    .build();

// 3. Limit by both
RetryContext both = RetryContext
    .builder(100L, delay -> delay + 100, 10, 5000L)
    .build();

// Filter which exceptions trigger a retry
RetryContext onlyIO = RetryContext
    .times(100L, Function.id(), 3)
    .exceptions(
        Collections.singleton(java.io.IOException.class),
        true                              // true = inclusive (retry only on IOException)
    )
    .build();

// Execute with retry
try {
    String result = byCount.retry(() -> callFlaky());
} catch (Exception e) {
    System.err.println("All retries exhausted: " + e);
}

// Pre-retry hook (runs before each retry attempt)
RetryContext withHook = RetryContext
    .times(500L, delay -> delay, 3)
    .registerPreRetryHook(() -> System.out.println("retrying..."))
    .build();
```

### Integrating Retry with `Executable`

```java
import com.simplj.lambda.executable.Executable;

Executable<String, Integer> fragile = s -> Integer.parseInt(s) / riskyDivisor();
Executable<String, Integer> resilient = fragile.withRetry(byCount);

try {
    int value = resilient.execute("42");
} catch (Exception e) { /* all retries exhausted */ }
```

### Integrating Retry with `Try`

```java
import com.simplj.lambda.util.Try;

Try<String> t = Try.execute(() -> callFlaky()).retry(byCount);
Either<Exception, String> result = t.result();
```

### `ResettableRetryContext<T>`

A `ResettableRetryContext` resets the input before each retry — useful when the resource or connection object needs reinitialisation.

```java
import com.simplj.lambda.util.retry.ResettableRetryContext;
import com.simplj.lambda.executable.Executable;

ResettableRetryContext<java.sql.Connection> resettable =
    byCount.resettableContext(conn -> {
        conn.close();
        return openNewConnection();
    });

Executable<java.sql.Connection, String> query = conn -> executeQuery(conn);
Executable<java.sql.Connection, String> withReset = query.withRetry(resettable);
```

### `CustomRetryContext<T>`

For fully custom retry logic, use `RetryContext.custom(RetryCondition<T>)`.

```java
import com.simplj.lambda.util.retry.RetryContext;
import com.simplj.lambda.util.retry.RetryCondition;

// RetryCondition<T>: (count, duration, Either<Exception, T>) -> boolean
RetryContext customCtx = RetryContext
    .custom((count, duration, either) ->
        either.isLeft() && either.left() instanceof java.net.SocketTimeoutException && count < 3)
    .build();
```

| Builder Method | Description |
|----------------|-------------|
| `times(long initialDelay, Function<Long,Long> delayCalculator, int maxAttempts)` | Retry up to `maxAttempts` times |
| `duration(long initialDelay, Function<Long,Long> delayCalculator, long maxDuration)` | Retry for up to `maxDuration` ms |
| `builder(long initialDelay, Function<Long,Long> delayCalculator, int maxAttempts, long maxDuration)` | Both limits |
| `.logger(Consumer<String> f)` | Custom logger (default: `System.out.println`) |
| `.registerPreRetryHook(Excerpt hook)` | Runs before each retry attempt |
| `.exceptions(Set<Class<? extends Exception>> exceptions, boolean isInclusive)` | Filter which exceptions trigger retry |
| `.build()` | Produces `RetryContext` |
| `RetryContext.custom(RetryCondition<T>)` | Fully custom condition |

**Constraint:** `initialDelay`, `maxAttempts`, and `maxDuration` cannot be negative; passing a negative value throws `IllegalArgumentException` immediately.

---

## Sequences

**Package:** `com.simplj.lambda.sequence`

`Series<T>` is an infinite (or optionally bounded / circular) lazy sequence driven by a generation function. It maintains a sliding window of predecessor values and the current element, and lazily produces successors on demand.

### `Series<T>`

```java
import com.simplj.lambda.sequence.Series;
import com.simplj.lambda.sequence.Serial;
import com.simplj.lambda.function.Function;

// Simple series: each element is f(previous)
Series<Integer> naturals = Series.of(1, n -> n + 1);

System.out.println(naturals.current());   // 1
System.out.println(naturals.successor()); // 2

naturals.next(); // advances: current = 2, successor = 3
System.out.println(naturals.current());   // 2
System.out.println(naturals.count());     // 2 (how many times next() was called + 1)

// Predecessor access
int prev = naturals.predecessor();        // 1 (last element before current)

// Fibonacci — depends on two predecessors
// Use the Serial<T> interface: (index, SlidingWindow<T> predecessors, T current) -> T
Series<Integer> fib = Series.of(
    1,
    Serial.<Integer>of((idx, preds, cur) -> {
        if (preds.isEmpty()) return cur; // first step: 1 + no pred = 1? actually we need 2 preds
        return cur + preds.get(0);
    }),
    2 // track 2 predecessors
);

// Filtered series — only even numbers
Series<Integer> evens = Series.of(1, n -> n + 1, n -> n % 2 == 0);

// Collect elements eagerly
java.util.List<Integer> first10 = Series.of(0, n -> n + 1).toList(10);
System.out.println(first10); // [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
```

### `BoundedSeries<T>` — finite series

```java
import com.simplj.lambda.sequence.Series;
import com.simplj.lambda.sequence.Bound;

// A Bound determines when hasNext() returns false
Series<Integer> bounded = Series.of(1, n -> n + 1)
    .bounded((current, successor) -> successor > 10); // stop when successor > 10

while (bounded.hasNext()) {
    System.out.print(bounded.next() + " "); // 2 3 4 5 6 7 8 9 10
}
```

### `CircularSeries<T>` — wrapping series

```java
import com.simplj.lambda.sequence.Series;

// The series wraps when the condition is met: reset to initial value
Series<Integer> clock = Series.of(1, n -> n + 1)
    .circular(n -> n > 12); // wrap when > 12 (like clock hours)
// 1,2,3,...,12,1,2,3,...
```

| Method | Returns | Description |
|--------|---------|-------------|
| `Series.of(A initial, Function<A,A> seriesF)` | `Series<A>` | Simple series; each element is `f(current)` |
| `Series.of(A initial, Function<A,A> seriesF, Condition<A> filter)` | `Series<A>` | Series with a filter (skips non-matching values) |
| `Series.of(A initial, Serial<A> seriesF)` | `Series<A>` | Advanced series with index and sliding window access |
| `Series.of(A initial, Serial<A> seriesF, int predCount)` | `Series<A>` | As above with a specified predecessor window size |
| `bounded(Bound<T> bound)` | `Series<T>` (BoundedSeries) | Makes the series finite |
| `circular(Condition<T> resetCondition)` | `Series<T>` (CircularSeries) | Wraps around when condition is met |
| `hasNext()` | `boolean` | Always `true` for unbounded; `false` when bound is reached |
| `next()` | `T` | Advances and returns the next element |
| `current()` | `T` | Current element (does not advance) |
| `successor()` | `T` | Next element without advancing |
| `predecessor()` | `T` | Most recent predecessor |
| `predecessor(int i)` | `T` | `i`-th predecessor (0 = most recent) |
| `count()` | `int` | Number of elements produced so far (including initial) |
| `toList()` | `List<T>` | Consumes the entire series (only safe for bounded/circular) |
| `toList(int limit)` | `List<T>` | Consumes up to `limit` elements |
| `copy()` | `Series<T>` | Returns an independent copy at the current position |

**`Serial<T>`** — `@FunctionalInterface` with `T generate(int index, SlidingWindow<T> predecessors, T current)`. Use when the generation function depends on the index or multiple past values.

**Gotcha:** Calling `toList()` on an unbounded `Series` will run until the JVM runs out of memory. Always use `toList(int limit)` unless the series is `bounded`.

---

## Sliding Window

**Package:** `com.simplj.lambda.data`

`SlidingWindow<T>` is a fixed-capacity FIFO window backed by a list. When it is full, adding a new element drops the oldest one. It is used internally by `Series` to track predecessor values, and is also useful as a stand-alone rolling buffer.

```java
import com.simplj.lambda.data.SlidingWindow;
import com.simplj.lambda.data.IList;

// Create a window with capacity 3
SlidingWindow<Integer> window = SlidingWindow.of(3);

window.add(1).add(2).add(3);
System.out.println(window.size());  // 3
System.out.println(window.first()); // 1
System.out.println(window.last());  // 3

window.add(4); // drops 1
System.out.println(window.first()); // 2

// Insert at index
SlidingWindow<Integer> inserted = window.insert(1, 99);

// Access
int val = window.get(0); // 2 (oldest)

// Aggregate
int sum = window.fold(0, (acc, n) -> acc + n);       // 2+3+4 = 9
int max = window.reduce((a, b) -> Math.max(a, b));   // 4

// Transform
SlidingWindow<String> strs = window.map(n -> "v" + n);

// Filter (preserves capacity limit)
SlidingWindow<Integer> evens = window.filter(n -> n % 2 == 0);

// Convert
IList<Integer> list = window.toIList();

// Copy
SlidingWindow<Integer> copy = window.copy();
```

| Method | Returns | Description |
|--------|---------|-------------|
| `SlidingWindow.of(int limit)` | `SlidingWindow<A>` | Creates a window with given capacity |
| `add(T val)` | `SlidingWindow<T>` | Appends `val`; drops oldest if full |
| `insert(int idx, T val)` | `SlidingWindow<T>` | Inserts at index |
| `get(int idx)` | `T` | Gets element at index |
| `first()` | `T` | Oldest element |
| `mid()` | `T` | Middle element |
| `last()` | `T` | Newest element |
| `limit()` | `int` | Maximum capacity |
| `size()` | `int` | Current element count |
| `isEmpty()` | `boolean` | True when no elements |
| `fold(R unit, BiFunction<R,T,R> accumulator)` | `R` | Left fold |
| `reduce(BiFunction<T,T,T> reducer)` | `T` | Reduce to single value |
| `map(Function<T,R> f)` | `SlidingWindow<R>` | Transforms each element; same capacity |
| `filter(Condition<T> c)` | `SlidingWindow<T>` | Retains matching elements; same capacity |
| `filterOut(Condition<T> c)` | `SlidingWindow<T>` | Removes matching elements |
| `take(int n)` | `SlidingWindow<T>` | Keeps first `n` elements |
| `skip(int n)` | `SlidingWindow<T>` | Drops first `n` elements |
| `toIList()` | `IList<T>` | Converts to `IList` |
| `toList()` | `List<T>` | Converts to `java.util.List` |
| `copy()` | `SlidingWindow<T>` | Independent copy |

---

## Utility Helpers

**Package:** `com.simplj.lambda.data`

### `Util`

Static helper methods for common generic operations.

```java
import com.simplj.lambda.data.Util;
import com.simplj.lambda.function.Condition;
import com.simplj.lambda.tuples.Couple;
import com.simplj.lambda.tuples.Tuple;
import java.util.Arrays;
import java.util.Set;
import java.util.Map;

// Find first element matching a condition in a Collection
java.util.List<String> list = Arrays.asList("apple", "banana", "cherry");
String found = Util.find(list, s -> s.startsWith("b")); // "banana"

// Find first matching in an array
String[] arr = {"x", "y", "z"};
String y = Util.find(arr, s -> s.equals("y")); // "y"

// Unchecked cast (use with care)
Object o = "hello";
String s = Util.cast(o); // safe if you know the type

// Cast with fallback exception
try {
    String safe = Util.tryCastOrThrow(o, ex -> new IllegalArgumentException("bad cast", ex));
} catch (IllegalArgumentException e) { /* wrong type */ }

// Build a Set from varargs
Set<String> set = Util.asSet("a", "b", "c");

// Build a Map from Couple varargs
Map<String, Integer> map = Util.asMap(Tuple.of("x", 1), Tuple.of("y", 2));
```

| Method | Returns | Description |
|--------|---------|-------------|
| `Util.find(Collection<T> source, Condition<T> c)` | `T` | First match or `null` |
| `Util.find(T[] source, Condition<T> c)` | `T` | First match in array or `null` |
| `Util.cast(Object o)` | `T` | Unchecked cast |
| `Util.tryCastOrThrow(Object o, Function<Exception,E> fX)` | `T` | Cast or throw the given exception |
| `Util.asSet(E... elems)` | `Set<E>` | Varargs to `HashSet` |
| `Util.asMap(Couple<K,V>... elems)` | `Map<K,V>` | Couples to `HashMap` |

---

## Error Handling & Exceptions

### Custom Exceptions

| Exception | Package | Trigger |
|-----------|---------|---------|
| `FilteredOutException` | `com.simplj.lambda.monadic.exception` | Thrown by `Thunk.filter(...)` and `Try.filter(...)` when the predicate returns `false` |
| `Try.AutoCloseException` | `com.simplj.lambda.util` | Thrown when one or more `AutoCloseable` resources fail to close inside a `Try.execute(Receiver<AutoCloseableMarker>)` or `Try.execute(Executable<AutoCloseableMarker,R>)` block |

### `FilteredOutException`

```java
import com.simplj.lambda.monadic.exception.FilteredOutException;
import com.simplj.lambda.util.Try;
import com.simplj.lambda.util.Either;

Either<Exception, Integer> r = Try.execute(() -> 5)
    .filter(n -> n > 10) // fails — 5 is not > 10
    .result();

System.out.println(r.isLeft()); // true
System.out.println(r.left() instanceof FilteredOutException); // true
```

To filter with a custom exception instead:

```java
Try.execute(() -> 5)
   .filter(n -> n > 10, () -> new IllegalArgumentException("too small"))
   .result();
```

### `Try.AutoCloseException`

```java
import com.simplj.lambda.util.Try;
import com.simplj.lambda.util.Either;

Either<Exception, String> r = Try.execute(marker -> {
    BadCloseable c = marker.markForAutoClose(new BadCloseable()); // close() throws
    return "ok";
}).result();

if (r.isLeft() && r.left() instanceof Try.AutoCloseException) {
    Try.AutoCloseException ace = (Try.AutoCloseException) r.left();
    System.out.println(ace.result());                // original result before close failed
    ace.failedCloseableList().forEach(pair ->
        System.out.println("failed to close: " + pair.first() + " due to " + pair.second())
    );
}
```

### `IllegalStateException` from `Thunk`

`Thunk.equals()` and `Thunk.hashCode()` throw `IllegalStateException` if called before `applied()`.

### `NoSuchElementException` from `Series`

`Series.predecessor()` throws `NoSuchElementException` if `next()` has never been called.

---

## Configuration Reference

JLX has no external configuration file. All configuration is done programmatically through builder or factory APIs. The table below lists every tunable parameter in the library.

| API | Parameter | Type | Default | Description |
|-----|-----------|------|---------|-------------|
| `RetryContext.times(...)` | `initialDelay` | `long` (ms) | — | Delay before first retry |
| `RetryContext.times(...)` | `delayCalculator` | `Function<Long,Long>` | — | Computes next delay from current delay |
| `RetryContext.times(...)` | `maxAttempts` | `int` | — | Maximum number of retry attempts |
| `RetryContext.duration(...)` | `maxDuration` | `long` (ms) | — | Maximum total time to keep retrying |
| `RetryContextBuilder.logger(...)` | `f` | `Consumer<String>` | `System.out::println` | Logger for retry messages |
| `RetryContextBuilder.registerPreRetryHook(...)` | `hook` | `Excerpt` | no-op | Runs before each retry |
| `RetryContextBuilder.exceptions(...)` | `exceptions` | `Set<Class<? extends Exception>>` | — (all) | Exception classes to include or exclude |
| `RetryContextBuilder.exceptions(...)` | `isInclusive` | `boolean` | — | `true` = retry only on listed exceptions; `false` = retry on all *except* listed |
| `IList.unit(Producer<List<?>> constructor)` | `constructor` | `Producer<List<?>>` | `ArrayList::new` | Backing list implementation |
| `ISet.unit(Producer<Set<?>> constructor)` | `constructor` | `Producer<Set<?>>` | `HashSet::new` | Backing set implementation |
| `IMap.unit(Producer<Map<?,?>> constructor)` | `constructor` | `Producer<Map<?,?>>` | `HashMap::new` | Backing map implementation |
| `SlidingWindow.of(int limit)` | `limit` | `int` | — | Maximum number of elements to retain |
| `Series.of(..., int predCount)` | `predCount` | `int` | 1 | Size of predecessor window for `Serial` |
| `Lazy.restating(...)` | `condition` | `Condition<T>` | — | When to restate the cached value |
| `Mutable.of(T, MutableWatcher<T>)` | `watcher` | `MutableWatcher<T>` | `null` | Callback on every value change |

---

## Thread Safety & Concurrency Notes

| Class | Thread-Safe? | Notes |
|-------|-------------|-------|
| `Lazy<A>` | Partially | `isApplied` flag uses `AtomicBoolean`; `volatile` fields protect visibility. However, the initializer lambda is **not** guaranteed to run exactly once under high contention. Do not use for singletons requiring strict once-only initialization — prefer `java.util.concurrent.atomic.AtomicReference` for that. |
| `Mutable<A>` | No | `get`, `set`, `mutate` are not synchronized. Wrap in `synchronized` or use `AtomicReference` for concurrent use. |
| `SlidingWindow<T>` | No | Not synchronized. Use one instance per thread. |
| `Series<T>` | No | Mutable internal state (`idx`, `val`, `succ`). Do not share a `Series` instance across threads; use `copy()` to get an independent instance per thread. |
| `IList`, `ISet`, `IMap`, `IArray` | Effectively immutable | Transformation results (`map`, `filter`, etc.) are lazy and stored as functions, not data. The underlying `java.util.List`/`Set`/`Map` is not modified. Safe to share across threads **after** `.applied()` is called. |
| `MList`, `MSet`, `MMap`, `MArray` | No | Mutable wrappers around unsynchronized Java collections. Do not share without external synchronization. |
| `Either<L, R>` | Immutable | Safe to share across threads. |
| `Tuple`, `Couple`, `Triple`, etc. | Immutable | Safe to share across threads. |
| `Try<A>` | No | Shares `logger` and `finalizeF` fields mutably. Build a `Try` pipeline per thread or call chain. |
| `Thunk<A>` | No | Not synchronized. Call `applied()` on a single thread and share the `applied` result. |
| `RetryContext` | Yes | Immutable after `build()`. Safe to share across threads and reuse for multiple calls. |
| `Function`, `Executable`, etc. | Yes | All functional interfaces are stateless lambdas by convention. Thread-safe as long as any captured state is thread-safe. |

---

## FAQ / Troubleshooting

**Q: I get a compile error when passing a method that throws a checked exception to `Function`.**

Use `Executable` instead of `Function`. `Executable<I, O>` accepts lambdas that declare `throws Exception`. Convert to a pure `Function` with `.pure()` if you need `Either`-wrapped results.

---

**Q: `IList.map(...)` seems to have no effect — the list is unchanged.**

`map`, `flatmap`, and `filter` on `I*` types are **lazy**. Call `.applied()` to force evaluation, or call `.list()` to get a materialised `java.util.List`. The original `IList` is never mutated.

```java
IList<Integer> result = IList.of(1, 2, 3)
    .map(n -> n * 2)
    .applied();          // force evaluation here
System.out.println(result.list()); // [2, 4, 6]
```

---

**Q: `Thunk.equals(...)` throws `IllegalStateException`.**

Call `applied()` first to force evaluation and cache the result before comparing.

```java
Thunk<Integer> t = Thunk.of(() -> 42).applied();
System.out.println(t.equals(Thunk.of(() -> 42).applied())); // true
```

---

**Q: `Try.execute(Executable<AutoCloseableMarker, R>)` with `withRetry(...)` gives unexpected results.**

Do not pass an `Executable` that already has `.withRetry(...)` applied into `Try.execute`. Use `Try.retry(RetryContext)` instead. Mixing the two can cause the `AutoCloseableMarker` to be reused across retries, which leads to double-close or missed-close bugs.

---

**Q: My `RetryContext` does not retry — it fails immediately.**

Check that `initialDelay` and `maxAttempts` / `maxDuration` are not zero or negative. Both throw `IllegalArgumentException` if negative. A `maxAttempts` of `0` means no retries. Use `maxAttempts >= 1` for at least one retry after the initial failure.

---

**Q: `Series.toList()` runs forever / out of memory.**

`Series` is infinite by default. Always use `toList(int limit)` or call `.bounded(...)` first to create a finite series.

```java
// Safe
java.util.List<Integer> first100 = Series.of(1, n -> n + 1).toList(100);

// Also safe
Series<Integer> finite = Series.of(1, n -> n + 1).bounded((cur, next) -> next > 100);
java.util.List<Integer> all = finite.toList(); // stops at 100
```

---

**Q: How do I use a `LinkedList` instead of `ArrayList` for `IList`?**

Pass a constructor producer to the factory:

```java
IList<String> linked = IList.unit(() -> new java.util.LinkedList<>());
IList<String> withData = IList.of(
    java.util.Arrays.asList("a", "b", "c"),
    () -> new java.util.LinkedList<>()
);
```

---

**Q: How do I combine `Either` results from multiple operations?**

Chain `flatmap`:

```java
Either<Exception, Integer> step1 = Either.right(10);
Either<Exception, String>  step2 = step1.flatmap(n ->
    n > 0 ? Either.right("positive:" + n) : Either.left(new Exception("non-positive"))
);
```

---

**Q: How do I turn a `Snippet` (pure) into a `Consumer<T>` or `Producer<T>`?**

```java
import com.simplj.lambda.function.Snippet;
import com.simplj.lambda.function.Consumer;
import com.simplj.lambda.function.Producer;

Snippet s = () -> System.out.println("side effect");

Consumer<String> asConsumer = s.toConsumer();  // ignores the String input
Producer<Integer> asProducer = s.toProducer(); // runs side effect, produces null
```

For the `Excerpt` (exception-throwing) equivalent:

```java
import com.simplj.lambda.executable.Excerpt;
import com.simplj.lambda.executable.Receiver;
import com.simplj.lambda.executable.Provider;

Excerpt e = () -> System.out.println("side effect");
Receiver<String> asReceiver = e.toReceiver();
Provider<Integer> asProvider = e.toProvider();
```

---

*See also: [Javadoc](https://javadoc.io/doc/com.simplj.lambda/jlx) | [GitHub](https://github.com/simplj/jlx) | [Maven Central](https://search.maven.org/search?q=g:%22com.simplj.lambda%22%20AND%20a:%22jlx%22)*
