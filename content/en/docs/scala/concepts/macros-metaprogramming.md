---
lastmod: "2026-01-06"
title: Macros and Metaprogramming
weight: 15
---

Metaprogramming allows you to generate or validate code at compile time. Scala 3 provides `inline` and a new macro system.

> **Prerequisites**: This is an advanced topic. You should be familiar with:
> - [Generics](../generics/) - Type parameters
> - [Type Classes](../type-classes/) - Type-level abstraction
> - [Advanced Types](../type-system-advanced/) - Match Types, Type Lambdas
>
> **Difficulty**: Very Advanced

## Inline

The `inline` keyword inlines code at compile time.

### Basic Usage

```scala
// Method inlining
inline def twice(x: Int): Int = x + x

val result = twice(21)  // Replaced with 42 at compile time
```

### Constant Folding

```scala
inline val Pi = 3.14159

// Computed at compile time
inline def circleArea(radius: Double): Double =
  Pi * radius * radius

val area = circleArea(5)  // Compiled as 78.53975
```

### Conditional Compilation

```scala
// First define Config object
object Config:
  inline val Debug = true  // or false

inline def debug(inline msg: String): Unit =
  inline if Config.Debug then
    println(msg)
  else
    ()  // Removed at compile time

debug("Debug message")  // Code itself is removed if Config.Debug is false
```

## Inline Match

Performs pattern matching at compile time.

```scala
inline def toInt(x: Any): Int = inline x match
  case x: Int    => x
  case x: String => x.toInt
  case x: Double => x.toInt

toInt(42)      // Int branch selected at compile time
toInt("42")    // String branch selected at compile time
```

### Type-based Optimization

```scala
transparent inline def stringify[T](x: T): String =
  inline x match
    case x: Int    => x.toString
    case x: String => x
    case x: Double => f"$x%.2f"
    case _         => x.toString

val s1: String = stringify(42)      // "42"
val s2: String = stringify("hello") // "hello"
val s3: String = stringify(3.14159) // "3.14"
```

## Compile-time Operations

### compiletime Package

```scala
import scala.compiletime.*

// Compile-time error
inline def checkPositive(inline n: Int): Int =
  inline if n <= 0 then
    error("n must be positive")
  else
    n

checkPositive(5)   // OK
// checkPositive(-1)  // Compile error: n must be positive
```

### constValue

Gets literal type values at compile time.

```scala
import scala.compiletime.constValue

// Extract value from literal type
inline def literalValue[T <: Int]: Int = constValue[T]

val three = literalValue[3]  // Replaced with 3 at compile time

// Practical example: Tuple size
import scala.compiletime.ops.int.*
type TupleSize[T <: Tuple] = T match
  case EmptyTuple => 0
  case h *: t => 1 + TupleSize[t]
```

### summonInline

```scala
import scala.compiletime.summonInline

trait Show[A]:
  def show(a: A): String

inline def show[A](a: A): String =
  summonInline[Show[A]].show(a)
```

## Macros

Scala 3 macros use the `quotes` API.

### Simple Macro

```scala
import scala.quoted.*

// Macro definition
inline def printCode(inline x: Any): Unit = ${ printCodeImpl('x) }

def printCodeImpl(x: Expr[Any])(using Quotes): Expr[Unit] =
  import quotes.reflect.*
  '{ println(${Expr(x.show)}) }

// Usage
printCode(1 + 2)  // Prints "1 + 2"
```

### Expression Generation

```scala
import scala.quoted.*

inline def toStringMacro[T](x: T): String = ${ toStringImpl('x) }

def toStringImpl[T: Type](x: Expr[T])(using Quotes): Expr[String] =
  '{ ${x}.toString }
```

## Scala 2 vs Scala 3 Macros

| Feature | Scala 2 | Scala 3 |
|---------|---------|---------|
| API | scala.reflect.macros | scala.quoted |
| Safety | Low | High (Staged) |
| Complexity | High | Relatively low |
| Migration | - | Complete rewrite required |

## Practical Use Cases

### 1. Automatic Logging

```scala
inline def logged[T](inline block: T): T =
  val result = block
  println(s"Result: $result")
  result

val x = logged {
  val a = 1
  val b = 2
  a + b
}  // Prints "Result: 3"
```

### 2. Print Type Name

```scala
import scala.compiletime.constValue
import scala.deriving.Mirror

inline def typeName[T](using m: Mirror.Of[T]): String =
  constValue[m.MirroredLabel]

case class Person(name: String, age: Int)

typeName[Person]  // "Person"
```

### 3. Compile-time Validation

```scala
import scala.compiletime.error

inline def requirePositive(inline n: Int): Int =
  inline if n <= 0 then
    error("Value must be positive")
  else
    n

val valid = requirePositive(5)    // OK
// val invalid = requirePositive(-1)  // Compile error
```

## Best Practices

### DO

- Use `inline` for small performance-critical functions
- Use macros for compile-time validation
- Use macros for boilerplate code generation

### DON'T

- Don't make every function `inline` (increases compile time)
- Don't implement complex logic with macros
- Avoid macro abuse that makes debugging difficult

## Exercises

### 1. Compile-time Calculation

Write an `inline` function that computes Fibonacci numbers at compile time.

<details>
<summary>Show Answer</summary>

```scala
inline def fib(inline n: Int): Int =
  inline if n <= 1 then n
  else fib(n - 1) + fib(n - 2)

val f10 = fib(10)  // Replaced with 55 at compile time
```

</details>

## References

- [Scala 3 Metaprogramming](https://docs.scala-lang.org/scala3/reference/metaprogramming.html)
- [Inline](https://docs.scala-lang.org/scala3/reference/metaprogramming/inline.html)
- [Macros](https://docs.scala-lang.org/scala3/reference/metaprogramming/macros.html)

## Next Steps

- [Concurrency](../concurrency/) — Future, Promise
- [Functional Patterns](../functional-patterns/) — Functor, Monad
