---
lastmod: "2026-01-06"
title: Scala 2 vs Scala 3 Comparison
weight: 3
---

Compare the differences by implementing the same logic in Scala 2 and Scala 3.

## Syntax Style

### Block Syntax

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

### if Expressions

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

### for Expressions

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

## Enumerations (Enum)

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

## Implicit Features

### Implicit Values and Parameters

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

### Extension Methods

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

### Type Classes

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

## Entry Point

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

## New Type Features (Scala 3 Only)

### Union Types

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

### Opaque Types

```scala
// Scala 3
object Money:
  opaque type USD = BigDecimal
  def usd(amount: BigDecimal): USD = amount
  extension (x: USD) def value: BigDecimal = x

// In Scala 2, use Value Class
case class USD(value: BigDecimal) extends AnyVal
```

## Migration Summary

| Scala 2 | Scala 3 |
|---------|---------|
| `implicit val` | `given` |
| `implicit def` (conversion) | `given Conversion` |
| `(implicit x: T)` | `(using x: T)` |
| `implicitly[T]` | `summon[T]` |
| `implicit class` | `extension` |
| `sealed trait` + `case object` | `enum` |
| `_` (wildcard import) | `*` |

## Compatibility

Most Scala 2 syntax still works in Scala 3:

```scala
// Works in Scala 3
implicit val x: Int = 42
def f(implicit n: Int): Int = n * 2

// Not recommended but works
for (i <- 1 to 5) { println(i) }
```

## Next Steps

- [Version Comparison](../../appendix/version-comparison/) — Full difference summary
- [Migration Guide](https://docs.scala-lang.org/scala3/guides/migration/compatibility-intro.html)
