---
lastmod: "2026-01-14"
title: Scala 2 vs Scala 3 Comparison
weight: 3
---

{{% notice style="primary" title="TL;DR" %}}
- **Syntax**: Scala 3 supports simpler syntax (indentation-based, no braces required)
- **Enums**: Simple definition with `enum` keyword (replaces `sealed trait` + `case object`)
- **Implicits**: Clearer with `given`/`using` keywords (replacing `implicit`)
- **New features**: Union Types, Opaque Types, Intersection Types
- **Compatibility**: Most Scala 2 code works in Scala 3
{{% /notice %}}

**Target Audience**: Developers familiar with Scala 2, developers choosing Scala 3

**Prerequisites**:
- Scala basic syntax
- Functional programming basics
- Pattern matching

---

Compare the differences by implementing the same logic in Scala 2 and Scala 3. Most Scala 2 syntax works in Scala 3, but Scala 3 offers more concise and readable new syntax. This document compares code by topic to help you understand the differences.

#### Syntax Style

Scala 3 supports more concise syntax than Scala 2. Support for indentation-based blocks and optional braces allows writing Python-like clean code.

**Block Syntax**

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// Indentation-based (optional)
def greet(name: String): String =
  val greeting = s"Hello, $name!"
  greeting

// Multiple lines
def process(x: Int): Int =
  val doubled = x * 2
  val squared = doubled * doubled
  squared
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
def process(x: Int): Int = {
  val doubled = x * 2
  val squared = doubled * doubled
  squared
}
```
{{% /tab %}}
{{< /tabs >}}

Scala 3 can omit braces by defining blocks with indentation. Scala 2 style braces also work in Scala 3.

{{% notice style="tip" title="Key Points" %}}
- **Scala 3**: Can omit braces, use indentation
- **Scala 2**: Braces always required
- Both styles work in Scala 3 (backward compatible)
{{% /notice %}}

**if Expressions**

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
val result = if x > 0 then "positive" else "non-positive"

val grade = if score >= 90 then "A"
  else if score >= 80 then "B"
  else if score >= 70 then "C"
  else "F"
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
val result = if (x > 0) "positive" else "non-positive"

val grade = if (score >= 90) "A"
  else if (score >= 80) "B"
  else if (score >= 70) "C"
  else "F"
```
{{% /tab %}}
{{< /tabs >}}

Scala 3 uses `then` keyword, omitting parentheses. Improves readability.

{{% notice style="tip" title="Key Points" %}}
- **Scala 3**: `if condition then result` (without parentheses)
- **Scala 2**: `if (condition) result` (with parentheses)
{{% /notice %}}

**for Expressions**

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// do keyword
for x <- list do
  println(x)

// Multiple generators
for
  x <- 1 to 3
  y <- 1 to 3
do
  println(s"$x, $y")

// yield
val result = for
  x <- 1 to 5
  if x % 2 == 0
yield x * x
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// Parentheses/braces
for (x <- list) {
  println(x)
}

// Multiple generators
for {
  x <- 1 to 3
  y <- 1 to 3
} {
  println(s"$x, $y")
}

// yield
val result = for {
  x <- 1 to 5
  if x % 2 == 0
} yield x * x
```
{{% /tab %}}
{{< /tabs >}}

Scala 3 separates loop body with `do` keyword. Clearly distinguishes loop definition from loop body.

{{% notice style="tip" title="Key Points" %}}
- **Scala 3**: Separate loop body with `do` keyword
- **Scala 2**: Define loop and body together with braces
- Scala 2 style also works in Scala 3
{{% /notice %}}

#### Enumerations (Enum)

Scala 3 offers new `enum` syntax, greatly simplifying enum definition compared to Scala 2's `sealed trait` + `case object` pattern.

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// Simple enum
enum Color:
  case Red, Green, Blue

// Parameterized enum
enum HttpStatus(val code: Int):
  case OK extends HttpStatus(200)
  case NotFound extends HttpStatus(404)
  case ServerError extends HttpStatus(500)

// ADT
enum Shape:
  case Circle(radius: Double)
  case Rectangle(width: Double, height: Double)

// Usage
val color = Color.Red
val status = HttpStatus.OK
val circle = Shape.Circle(5.0)
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// sealed trait + case object
sealed trait Color
object Color {
  case object Red extends Color
  case object Green extends Color
  case object Blue extends Color
}

// Parameterized enum
sealed abstract class HttpStatus(val code: Int)
object HttpStatus {
  case object OK extends HttpStatus(200)
  case object NotFound extends HttpStatus(404)
  case object ServerError extends HttpStatus(500)
}

// ADT
sealed trait Shape
case class Circle(radius: Double) extends Shape
case class Rectangle(width: Double, height: Double) extends Shape

// Usage
val color: Color = Color.Red
val status: HttpStatus = HttpStatus.OK
val circle: Shape = Circle(5.0)
```
{{% /tab %}}
{{< /tabs >}}

Scala 3 enum definition is much more concise. Also suitable for ADT (Algebraic Data Type) definition.

{{% notice style="tip" title="Key Points" %}}
- **Scala 3 enum**: Simple definition, ideal for simple enumerations and ADTs
- **Scala 2**: Requires `sealed trait` + `case object/class` boilerplate
- Scala 3 enums are internally implemented as sealed traits
{{% /notice %}}

#### Implicit Features

Scala 3 clarified the role of implicit features by splitting them into `given`/`using`. This makes distinction between implicit values, implicit parameters, and implicit conversions clearer.

**Implicit Values and Parameters**

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// given instance
given defaultTimeout: Int = 5000

// using clause
def connect(url: String)(using timeout: Int): Unit =
  println(s"Connecting to $url with timeout $timeout")

// Calling
connect("localhost")              // Uses given
connect("localhost")(using 10000) // Explicit
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// implicit val
implicit val defaultTimeout: Int = 5000

// implicit parameter
def connect(url: String)(implicit timeout: Int): Unit =
  println(s"Connecting to $url with timeout $timeout")

// Calling
connect("localhost")        // Uses implicit
connect("localhost")(10000) // Explicit
```
{{% /tab %}}
{{< /tabs >}}

Scala 3 uses `given` keyword to define implicit instances and `using` keyword for implicit parameters. This makes code intent clearer than `implicit` overloading.

{{% notice style="tip" title="Key Points" %}}
- **Scala 3**: `given` for instances, `using` for parameters
- **Scala 2**: `implicit` for both, ambiguous distinction
- Scala 3 separates naming instances from using them
{{% /notice %}}

**Extension Methods**

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
extension (s: String)
  def exclaim: String = s + "!"
  def words: List[String] = s.split(" ").toList
  def reverse: String = s.reverse

"Hello".exclaim  // "Hello!"
"Hello World".words  // List("Hello", "World")
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
implicit class StringOps(s: String) {
  def exclaim: String = s + "!"
  def words: List[String] = s.split(" ").toList
  def reverse: String = s.reverse
}

"Hello".exclaim  // "Hello!"
"Hello World".words  // List("Hello", "World")
```
{{% /tab %}}
{{< /tabs >}}

Scala 3 uses `extension` keyword to explicitly declare adding new methods to existing types.

{{% notice style="tip" title="Key Points" %}}
- **Scala 3**: Use `extension` keyword directly (clearer intent)
- **Scala 2**: Use `implicit class` (create wrapper class)
- Scala 3 extensions compile to more efficient bytecode
{{% /notice %}}

**Type Classes**

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
trait Show[A]:
  def show(a: A): String

object Show:
  given Show[Int] with
    def show(a: Int): String = a.toString

  given Show[String] with
    def show(a: String): String = s"\"$a\""

def print[A](a: A)(using s: Show[A]): Unit =
  println(s.show(a))

// summon
val intShow = summon[Show[Int]]
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
trait Show[A] {
  def show(a: A): String
}

object Show {
  implicit val intShow: Show[Int] = new Show[Int] {
    def show(a: Int): String = a.toString
  }

  implicit val stringShow: Show[String] = new Show[String] {
    def show(a: String): String = s""""$a""""
  }
}

def print[A](a: A)(implicit s: Show[A]): Unit =
  println(s.show(a))

// implicitly
val intShow = implicitly[Show[Int]]
```
{{% /tab %}}
{{< /tabs >}}

Scala 3 uses `given ... with` for cleaner type class instance definition. `summon` replaces `implicitly`.

{{% notice style="tip" title="Key Points" %}}
- **Scala 3**: `given ... with` syntax, cleaner definition
- **Scala 2**: Create anonymous classes with `new`, verbose
- **Scala 3 summon**: Clearer naming than `implicitly`
{{% /notice %}}

#### Entry Point

Defining application entry points also differs significantly between Scala 2 and 3. Scala 3's `@main` annotation is simpler and automatically handles command-line argument parsing.

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
@main def hello(): Unit =
  println("Hello, World!")

// With arguments
@main def greet(name: String, count: Int): Unit =
  for _ <- 1 to count do
    println(s"Hello, $name!")
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
object Hello {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
  }
}

// Or with App trait
object Hello extends App {
  println("Hello, World!")
}
```
{{% /tab %}}
{{< /tabs >}}

Scala 3's `@main` annotation automatically parses command-line arguments to match method parameter types.

{{% notice style="tip" title="Key Points" %}}
- **Scala 3**: `@main` annotation, automatic argument parsing
- **Scala 2**: Define `main` method in object or extend `App` trait
- Scala 3 `@main` provides better type safety
{{% /notice %}}

#### New Type Features (Scala 3 Only)

Scala 3 introduced new type system features. Union Types, Intersection Types, Opaque Types offer richer type expression.

**Union Types**

Express a value can be one of multiple types. More concise than Scala 2's Either.

```scala
// Scala 3
def process(input: Int | String): String = input match
  case i: Int    => s"Number: $i"
  case s: String => s"String: $s"

// In Scala 2, use Either
def process(input: Either[Int, String]): String = input match {
  case Left(i)  => s"Number: $i"
  case Right(s) => s"String: $s"
}
```

**Opaque Types**

Define type aliases that are transparent at compile time but opaque at runtime, combining type safety without runtime overhead.

```scala
// Scala 3
object Money:
  opaque type USD = BigDecimal
  def usd(amount: BigDecimal): USD = amount
  extension (x: USD) def value: BigDecimal = x

// In Scala 2, use Value Class
case class USD(value: BigDecimal) extends AnyVal
```

**Intersection Types**

Express a value has multiple types simultaneously. Scala 2 could only achieve this with traits or with keyword.

```scala
// Scala 3
trait Readable:
  def read(): String

trait Writable:
  def write(data: String): Unit

def process(file: Readable & Writable): Unit =
  val data = file.read()
  file.write(data.toUpperCase)

// Scala 2 required with keyword
def process(file: Readable with Writable): Unit = {
  val data = file.read()
  file.write(data.toUpperCase)
}
```

{{% notice style="tip" title="Key Points" %}}
- **Union Types**: `A | B` - value can be A or B
- **Intersection Types**: `A & B` - value has both A and B properties
- **Opaque Types**: Type aliases transparent at compile time, opaque at runtime
{{% /notice %}}

#### Migration Summary

Table summarizing keywords changed from Scala 2 to 3.

| Scala 2 | Scala 3 |
|---------|---------|
| `implicit val` | `given` |
| `implicit def` (conversion) | `given Conversion` |
| `(implicit x: T)` | `(using x: T)` |
| `implicitly[T]` | `summon[T]` |
| `implicit class` | `extension` |
| `sealed trait` + `case object` | `enum` |
| `_` (wildcard import) | `*` |

#### Compatibility

Most Scala 2 syntax still works in Scala 3, making migration relatively easy.

```scala
// Works in Scala 3
implicit val x: Int = 42
def f(implicit n: Int): Int = n * 2

// Not recommended but works
for (i <- 1 to 5) { println(i) }
```

However, Scala 3 style is recommended for new projects to fully leverage new features.

{{% notice style="tip" title="Key Points" %}}
- Most Scala 2 syntax works in Scala 3 (backward compatibility)
- New features like Union Types, Opaque Types only in Scala 3
- Migration possible gradually (mix Scala 2/3 style)
{{% /notice %}}

#### Next Steps

Once you understand Scala 2 and 3 differences, proceed with practical project development.

- [Version Comparison](../../appendix/version-comparison/) — Full difference summary
- [Migration Guide](https://docs.scala-lang.org/scala3/guides/migration/compatibility-intro.html)
- [Practical Projects](../practical-project/) — Real service implementation
