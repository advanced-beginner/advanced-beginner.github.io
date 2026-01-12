---
lastmod: "2026-01-06"
title: Functions and Methods
weight: 3
---

In Scala, functions are first-class citizens. You can store functions in variables, pass them as arguments, and return them as values.

## Method Definition

### Basic Syntax

Methods are defined with the `def` keyword.

```scala
// Basic form
def add(a: Int, b: Int): Int = {
  a + b
}

// Single line can omit braces
def add(a: Int, b: Int): Int = a + b

// Return type inference (not recommended - explicit is better)
def add(a: Int, b: Int) = a + b
```

### Parameter Types are Required

Scala doesn't infer parameter types. They must be specified.

```scala
// Correct
def greet(name: String): String = s"Hello, $name!"

// Compile error
// def greet(name) = s"Hello, $name!"
```

### Unit Return (Side Effects)

Methods that don't return anything return `Unit`.

```scala
def printGreeting(name: String): Unit = {
  println(s"Hello, $name!")
}

// Shorthand (omit return type)
def printGreeting(name: String) = println(s"Hello, $name!")
```

### Scala 3 Syntax

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// Indentation-based
def greet(name: String): String =
  val greeting = s"Hello, $name!"
  greeting

// Multiple lines
def calculate(x: Int, y: Int): Int =
  val sum = x + y
  val product = x * y
  sum + product
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// Braces required
def greet(name: String): String = {
  val greeting = s"Hello, $name!"
  greeting
}

// Multiple lines
def calculate(x: Int, y: Int): Int = {
  val sum = x + y
  val product = x * y
  sum + product
}
```
{{% /tab %}}
{{< /tabs >}}

## Default Parameter Values

Parameters can have default values.

```scala
def greet(name: String = "World", punctuation: String = "!"): String =
  s"Hello, $name$punctuation"

println(greet())                    // Hello, World!
println(greet("Scala"))             // Hello, Scala!
println(greet("Scala", "?"))        // Hello, Scala?
```

## Named Arguments

Passing arguments by name allows changing the order.

```scala
def createPerson(name: String, age: Int, city: String): String =
  s"$name, $age years old, lives in $city"

// Can change order
println(createPerson(age = 30, city = "Seoul", name = "John"))

// Partially named
println(createPerson("Jane", city = "Busan", age = 25))
```

## Varargs

Use `*` to accept a variable number of arguments.

```scala
def sum(numbers: Int*): Int = numbers.sum

println(sum(1, 2, 3))        // 6
println(sum(1, 2, 3, 4, 5))  // 15

// Spread a sequence
val nums = Seq(1, 2, 3, 4, 5)
println(sum(nums*))          // Scala 3
// println(sum(nums: _*))    // Scala 2
```

## Anonymous Functions (Lambdas)

Functions can be defined without names.

### Basic Syntax

```scala
// Full form
val add: (Int, Int) => Int = (a: Int, b: Int) => a + b

// Type inference
val add = (a: Int, b: Int) => a + b

// Single parameter can omit parentheses
val double = (x: Int) => x * 2

// Usage
println(add(1, 2))    // 3
println(double(5))    // 10
```

### Shorthand Syntax

```scala
val numbers = List(1, 2, 3, 4, 5)

// Full form
numbers.map((x: Int) => x * 2)

// Type inference
numbers.map(x => x * 2)

// Placeholder syntax (each _ is a different argument)
numbers.map(_ * 2)

// Multiple arguments
numbers.reduce((a, b) => a + b)
numbers.reduce(_ + _)
```

### Multi-line Lambdas

```scala
val process = (x: Int) => {
  val doubled = x * 2
  val squared = doubled * doubled
  squared
}
```

## Higher-Order Functions

Functions that take functions as arguments or return functions.

### Taking Functions as Arguments

```scala
def applyTwice(f: Int => Int, x: Int): Int = f(f(x))

val double = (x: Int) => x * 2
println(applyTwice(double, 3))  // 12 (3 -> 6 -> 12)

// Pass lambda directly
println(applyTwice(x => x + 10, 5))  // 25 (5 -> 15 -> 25)
```

### Returning Functions

```scala
def multiplier(factor: Int): Int => Int = {
  (x: Int) => x * factor
}

val triple = multiplier(3)
println(triple(5))   // 15
println(triple(10))  // 30
```

## Currying

Define functions with multiple parameter lists.

```scala
// Curried function
def add(a: Int)(b: Int): Int = a + b

println(add(1)(2))  // 3
```

### Partial Application

Create new functions by applying only some arguments:

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
def add(a: Int)(b: Int): Int = a + b

// Apply only first parameter
val add5 = add(5)   // Int => Int
println(add5(10))   // 15

// Type annotation also works
val add10: Int => Int = add(10)
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
def add(a: Int)(b: Int): Int = a + b

// Need underscore for partial application
val add5 = add(5)_  // Int => Int
println(add5(10))   // 15

// Or provide type hint
val add10: Int => Int = add(10)
```
{{% /tab %}}
{{< /tabs >}}

> **Difference:** In Scala 2, underscore like `add(5)_` was needed, but Scala 3 infers from context.

### Currying Use Cases

```scala
// Improved type inference
def transform[A, B](list: List[A])(f: A => B): List[B] =
  list.map(f)

// f's type is inferred from first parameter
transform(List(1, 2, 3))(x => x * 2)  // List(2, 4, 6)

// DSL with currying
def withResource[T](resource: => T)(cleanup: T => Unit)(action: T => Unit): Unit = {
  val r = resource
  try {
    action(r)
  } finally {
    cleanup(r)
  }
}
```

## Partially Applied Functions

Create new functions by applying only some arguments.

```scala
def log(level: String, message: String): Unit =
  println(s"[$level] $message")

// Partial application
val info = log("INFO", _)
val error = log("ERROR", _)

info("Starting")    // [INFO] Starting
error("Error occurred")  // [ERROR] Error occurred
```

## Recursive Functions

Recursive functions must specify return type.

```scala
def factorial(n: Int): Int =
  if (n <= 1) 1
  else n * factorial(n - 1)

println(factorial(5))  // 120
```

### Tail Recursion Optimization

Use `@tailrec` annotation to ensure tail recursion optimization.

```scala
import scala.annotation.tailrec

def factorial(n: Int): Int = {
  @tailrec
  def loop(n: Int, acc: Int): Int =
    if (n <= 1) acc
    else loop(n - 1, n * acc)

  loop(n, 1)
}

println(factorial(5))  // 120
```

## Function Types

Function types are `(ArgTypes) => ReturnType`.

```scala
// Function type declarations
val f1: Int => Int = x => x * 2
val f2: (Int, Int) => Int = (a, b) => a + b
val f3: () => Int = () => 42
val f4: Int => Int => Int = a => b => a + b  // Curried

// Higher-order function types
def process(f: String => Int): Int = f("hello")
```

## @main Annotation (Scala 3)

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// Simple entry point
@main def hello(): Unit =
  println("Hello, World!")

// With arguments
@main def greet(name: String, times: Int): Unit =
  for _ <- 1 to times do
    println(s"Hello, $name!")

// Run: scala greet Scala 3
// Output:
// Hello, Scala!
// Hello, Scala!
// Hello, Scala!
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// main method in object
object Hello {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
  }
}

// Or extend App trait
object Hello extends App {
  println("Hello, World!")
}
```
{{% /tab %}}
{{< /tabs >}}

## Exercises

### 1. Higher-Order Function Implementation

Implement `applyAll` that takes a value and a list of functions, applying all functions sequentially.

```scala
def applyAll(x: Int, functions: List[Int => Int]): Int = ???

val fns = List(
  (x: Int) => x + 1,
  (x: Int) => x * 2,
  (x: Int) => x - 3
)
println(applyAll(5, fns))  // ((5 + 1) * 2) - 3 = 9
```

<details>
<summary>Show Answer</summary>

```scala
def applyAll(x: Int, functions: List[Int => Int]): Int =
  functions.foldLeft(x)((acc, f) => f(acc))
```

</details>

### 2. Currying Conversion

Convert a regular function to a curried function.

```scala
def add(a: Int, b: Int, c: Int): Int = a + b + c

// Conversion result
val curriedAdd: Int => Int => Int => Int = ???
```

<details>
<summary>Show Answer</summary>

```scala
val curriedAdd: Int => Int => Int => Int =
  a => b => c => a + b + c

// Or
val curriedAdd = (add _).curried
```

</details>

### 3. Tail-Recursive Fibonacci

Write a tail-recursive function to calculate Fibonacci numbers.

<details>
<summary>Show Answer</summary>

```scala
import scala.annotation.tailrec

def fibonacci(n: Int): Long = {
  @tailrec
  def loop(n: Int, prev: Long, curr: Long): Long =
    if (n <= 0) prev
    else loop(n - 1, curr, prev + curr)

  loop(n, 0, 1)
}

println(fibonacci(10))  // 55
println(fibonacci(50))  // 12586269025
```

</details>

## Next Steps

- [Classes and Objects](../classes-objects/) — OOP basics
- [Higher-Order Functions](../higher-order-functions/) — Advanced functional programming
