---
lastmod: "2026-01-14"
title: Macros and Metaprogramming
weight: 15
---

{{< callout type="info" title="TL;DR" >}}
- **inline**: Inlines code at compile time to eliminate runtime overhead
- **inline if/match**: Conditional compilation, type-specific optimization
- **compiletime package**: Compile-time operations like `error`, `constValue`, `summonInline`
- **Macros (`${ }`)**: Compile-time code generation using `scala.quoted` API
- Scala 3 macros are more type-safe and concise than Scala 2
{{< /callout >}}

**Target Audience:** Advanced developers familiar with type-level programming
**Prerequisites:** Generics, type classes, advanced type system

Metaprogramming enables code generation and validation at compile time. Scala 3 provides `inline` and a new macro system. By leveraging these features, you can reduce boilerplate code, perform optimizations at compile time, and enable type-safe code generation.

> 📚 **Prerequisites**: This is an advanced topic. You should be familiar with:
> - [Generics](generics/) - Type parameters
> - [Type Classes](type-classes/) - Type-level abstraction
> - [Advanced Types](type-system-advanced/) - Match Types, Type Lambdas
>
> **Difficulty**: ⭐⭐⭐⭐⭐ (Very Advanced)

#### Inline

The `inline` keyword inlines code at compile time. Inlined functions are replaced with their function body at the call site, eliminating runtime overhead.

**Basic Usage**

```scala
// Method inlining
inline def twice(x: Int): Int = x + x

val result = twice(21)  // Replaced with 42 at compile time
```

**Constant Folding**

Using `inline val` inlines constants at compile time. This allows constant operations to be computed at compile time, improving runtime performance.

```scala
inline val Pi = 3.14159

// Computed at compile time
inline def circleArea(radius: Double): Double =
  Pi * radius * radius

val area = circleArea(5)  // Compiled to 78.53975
```

**Conditional Compilation**

Using `inline if` selects code at compile time based on conditions. Branches with false conditions are completely removed from the compiled bytecode.

```scala
// First, define Config object
object Config:
  inline val Debug = true  // or false

inline def debug(inline msg: String): Unit =
  inline if Config.Debug then
    println(msg)
  else
    ()  // Removed at compile time

debug("Debug message")  // If Config.Debug is false, the code itself is removed
```

{{< callout type="info" title="Key Points" >}}
- `inline def`: Replaced with function body at call site
- `inline val`: Constants inlined at compile time
- `inline if`: Code selected at compile time based on condition
{{< /callout >}}

#### Inline Match

Performs pattern matching at compile time. `inline match` selects the appropriate branch at compile time based on input type, eliminating runtime pattern matching overhead.

```scala
inline def toInt(x: Any): Int = inline x match
  case x: Int    => x
  case x: String => x.toInt
  case x: Double => x.toInt

toInt(42)      // Int branch selected at compile time
toInt("42")    // String branch selected at compile time
```

**Type-Specific Optimization**

`transparent inline` enables more precise return type inference. Optimized code is generated for each type, eliminating unnecessary conversions or boxing.

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

{{< callout type="info" title="Key Points" >}}
- `inline match`: Compile-time pattern matching
- `transparent inline`: More precise return type inference
- Type-specific optimized code generation
{{< /callout >}}

#### Compile-time Operations

Scala 3 provides various features for compile-time operations. The `scala.compiletime` package includes utility functions that are evaluated at compile time.

**compiletime Package**

Functions like `error`, `constValue`, `summonInline` enable you to raise errors or extract values at compile time.

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

**constValue**

`constValue` retrieves the value of a literal type at compile time. Useful for bringing type-level constants to the value level.

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

**summonInline**

`summonInline` summons a type class instance at compile time. If the instance doesn't exist, it causes a compile error.

```scala
import scala.compiletime.summonInline

trait Show[A]:
  def show(a: A): String

inline def show[A](a: A): String =
  summonInline[Show[A]].show(a)
```

{{< callout type="info" title="Key Points" >}}
- `error`: Raise compile-time errors
- `constValue`: Extract values from literal types
- `summonInline`: Summon type class instances at compile time
{{< /callout >}}

#### Macros

Scala 3 macros use the `quotes` API. Macros enable code analysis and generation at compile time. More powerful than `inline` but also more complex.

**Simple Macro**

Macros are defined using `${ ... }` splicing and `'{ ... }` quoting. Splicing executes at compile time, while quoting represents runtime code.

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

**Expression Generation**

The `Expr` type allows creating and manipulating expressions at compile time, enabling type-safe code generation.

```scala
import scala.quoted.*

inline def toStringMacro[T](x: T): String = ${ toStringImpl('x) }

def toStringImpl[T: Type](x: Expr[T])(using Quotes): Expr[String] =
  '{ ${x}.toString }
```

{{< callout type="info" title="Key Points" >}}
- `${ ... }`: Splicing, executes at compile time
- `'{ ... }`: Quoting, represents runtime code
- `Expr[T]`: Type-safe expression wrapper
{{< /callout >}}

#### Scala 2 vs Scala 3 Macros

The macro systems in Scala 2 and Scala 3 are completely different. Scala 3 significantly improved type safety, but migrating Scala 2 macros requires complete rewriting. The table below summarizes the key differences between the two versions.

| Feature | Scala 2 | Scala 3 |
|---------|---------|---------|
| API | scala.reflect.macros | scala.quoted |
| Safety | Low | High (Staged) |
| Complexity | High | Relatively low |
| Migration | - | Complete rewrite required |

#### Practical Use Cases

Macros and inline are used for various practical purposes. Common examples include automatic logging, extracting type names, and compile-time validation.

**1. Automatic Logging**

Using inline, you can implement automatic logging for debugging without overhead.

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

**2. Type Name Extraction**

Combining `Mirror` and `constValue` allows extracting type names at compile time.

```scala
import scala.compiletime.constValue
import scala.deriving.Mirror

inline def typeName[T](using m: Mirror.Of[T]): String =
  constValue[m.MirroredLabel]

case class Person(name: String, age: Int)

typeName[Person]  // "Person"
```

**3. Compile-time Validation**

Using the `error` function allows raising compile errors under specific conditions, catching incorrect usage at compile time rather than runtime.

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

#### Best Practices

Guidelines for effectively using macros and inline.

**DO**

The following situations recommend using inline and macros:

- Use `inline` for performance-critical small functions
- Use macros for compile-time validation
- Use macros for boilerplate code generation

**DON'T**

These are anti-patterns to avoid:

- Don't make every function `inline` (increases compile time)
- Don't implement complex logic with macros
- Avoid macro overuse that makes debugging difficult

#### Exercises

Practice inline and macro concepts with the following exercises.

**1. Compile-time Computation ⭐⭐⭐**

Write an `inline` function that computes Fibonacci numbers at compile time.

<details>
<summary>View Answer</summary>

```scala
inline def fib(inline n: Int): Int =
  inline if n <= 1 then n
  else fib(n - 1) + fib(n - 2)

val f10 = fib(10)  // Replaced with 55 at compile time
```

</details>

#### References

For more details, refer to the official documentation.

- [Scala 3 Metaprogramming](https://docs.scala-lang.org/scala3/reference/metaprogramming.html)
- [Inline](https://docs.scala-lang.org/scala3/reference/metaprogramming/inline.html)
- [Macros](https://docs.scala-lang.org/scala3/reference/metaprogramming/macros.html)

#### Next Steps

- [Concurrency](concurrency/) — Future, Promise
- [Functional Patterns](functional-patterns/) — Functor, Monad
