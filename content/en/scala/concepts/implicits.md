---
lastmod: "2026-01-06"
title: Implicit / Given
weight: 11
---

Implicit features are one of Scala's powerful capabilities. This covers both Scala 2's `implicit` and Scala 3's `given`/`using`.

## Scala 2: Implicit

### Implicit Values

```scala
// Define implicit value
implicit val defaultName: String = "Guest"

// Use implicit parameter
def greet(implicit name: String): String = s"Hello, $name!"

greet              // "Hello, Guest!" (implicitly passed)
greet("Alice")     // "Hello, Alice!" (explicitly passed)
```

### Implicit Parameters

```scala
case class Config(url: String, timeout: Int)

implicit val defaultConfig: Config = Config("localhost", 5000)

def connect(implicit config: Config): Unit =
  println(s"Connecting to ${config.url} with timeout ${config.timeout}")

connect  // Uses implicit Config
```

### Implicit Conversions

```scala
// Implicit conversion from Int to String
implicit def intToString(i: Int): String = i.toString

val s: String = 42  // Automatically converted to "42"

// Use with caution - can be dangerous!
```

### Implicit Classes (Extension Methods)

```scala
implicit class RichString(s: String) {
  def exclaim: String = s + "!"
  def words: List[String] = s.split(" ").toList
}

"Hello".exclaim          // "Hello!"
"Hello World".words      // List("Hello", "World")
```

## Scala 3: Given / Using

In Scala 3, `implicit` is separated into clearer keywords.

### Given Instances

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// Define instance with given
given defaultName: String = "Guest"

// Use with using
def greet(using name: String): String = s"Hello, $name!"

greet              // "Hello, Guest!"
greet(using "Alice") // "Hello, Alice!"
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
implicit val defaultName: String = "Guest"

def greet(implicit name: String): String = s"Hello, $name!"

greet              // "Hello, Guest!"
greet("Alice")     // "Hello, Alice!"
```
{{% /tab %}}
{{< /tabs >}}

### Anonymous Given

```scala
// Unnamed given
given String = "Guest"

// Reference by type only
summon[String]  // "Guest"
```

### Using Clause

```scala
case class Config(url: String, timeout: Int)

given Config = Config("localhost", 5000)

def connect(using config: Config): Unit =
  println(s"Connecting to ${config.url}")

connect  // Implicitly uses Config
```

### Extension Methods (Scala 3)

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
extension (s: String)
  def exclaim: String = s + "!"
  def words: List[String] = s.split(" ").toList
  def repeatN(n: Int): String = s * n

"Hello".exclaim      // "Hello!"
"Hello".repeatN(3)   // "HelloHelloHello"
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
implicit class StringOps(s: String) {
  def exclaim: String = s + "!"
  def words: List[String] = s.split(" ").toList
  def repeatN(n: Int): String = s * n
}

"Hello".exclaim      // "Hello!"
"Hello".repeatN(3)   // "HelloHelloHello"
```
{{% /tab %}}
{{< /tabs >}}

## Type Class Pattern

### Definition

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// Type class definition
trait Show[A]:
  def show(a: A): String

// Instance definitions
given Show[Int] with
  def show(a: Int): String = a.toString

given Show[String] with
  def show(a: String): String = s"\"$a\""

// Usage
def print[A](a: A)(using s: Show[A]): Unit =
  println(s.show(a))

print(42)       // "42"
print("hello")  // "\"hello\""
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// Type class definition
trait Show[A] {
  def show(a: A): String
}

// Instance definitions
implicit val intShow: Show[Int] = new Show[Int] {
  def show(a: Int): String = a.toString
}

implicit val stringShow: Show[String] = new Show[String] {
  def show(a: String): String = s"\"$a\""
}

// Usage
def print[A](a: A)(implicit s: Show[A]): Unit =
  println(s.show(a))

print(42)       // "42"
print("hello")  // "\"hello\""
```
{{% /tab %}}
{{< /tabs >}}

### Context Bounds

```scala
// Context bound syntax
def print[A: Show](a: A): Unit = {
  val s = summon[Show[A]]  // Scala 3
  // val s = implicitly[Show[A]]  // Scala 2
  println(s.show(a))
}
```

## Implicit Scope

Implicit values are searched in this order:

1. **Current scope** - Local variables, imported implicits
2. **Companion objects of associated types** - Type parameters, parent types, etc.

```scala
case class User(name: String)

object User {
  // Implicit defined in companion object
  implicit val ordering: Ordering[User] =
    Ordering.by(_.name)
}

// Automatically finds User.ordering
List(User("Bob"), User("Alice")).sorted
// List(User("Alice"), User("Bob"))
```

## Given Import (Scala 3)

```scala
object Givens:
  given Int = 42
  given String = "hello"
  val normalValue = 100

// Import only specific type's given (using braces)
import Givens.{given Int}

// Import all givens
import Givens.given

// Import both regular members and givens
import Givens.*       // Only normalValue
import Givens.given   // Only given Int, given String

// If you need both
import Givens.{*, given}
```

> **Difference from Scala 2:** In Scala 2, `import Givens._` also imported implicits, but Scala 3 requires explicit `given` import.

## Migration Guide

| Scala 2 | Scala 3 |
|---------|---------|
| `implicit val x: T = ...` | `given x: T = ...` |
| `implicit def f: T = ...` | `given f: T = ...` |
| `def f(implicit x: T)` | `def f(using x: T)` |
| `implicitly[T]` | `summon[T]` |
| `implicit class` | `extension` |

### Gradual Migration

Scala 3 still supports `implicit`:

```scala
// Works in Scala 3
implicit val x: Int = 42
def f(implicit n: Int): Int = n * 2
```

## Best Practices

### DO

```scala
// Use for type classes
given Ordering[MyClass] = ???

// Pass configuration/context
def process(data: Data)(using config: Config): Result = ???

// Extension methods
extension (s: String)
  def toSlug: String = s.toLowerCase.replace(" ", "-")
```

### DON'T

```scala
// Avoid indiscriminate implicit conversions
given Conversion[Int, String] = _.toString

// Avoid too generic implicit types
given String = "default"  // Used anywhere String is needed
```

## Common Mistakes and Anti-patterns

### What to Avoid

```scala
// 1. Implicit with too generic type
implicit val defaultString: String = "hello"
// This value injected wherever String is needed!

// 2. Indiscriminate implicit conversions
implicit def stringToInt(s: String): Int = s.toInt
val x: Int = "123"  // Implicitly converted - dangerous!
val y: Int = "abc"  // NumberFormatException!

// 3. Implicit scope conflicts
import library1._
import library2._  // Both define implicit for same type
// "ambiguous implicit values" error!

// 4. Complex implicit chains
// If A → B → C → D conversion needed, compile time increases dramatically
```

### The Right Way

```scala
// 1. Use specific wrapper types
case class AppConfig(dbUrl: String, timeout: Int)
given AppConfig = AppConfig("localhost", 5000)

// 2. Use extension methods instead of implicit conversions
extension (s: String)
  def toIntSafe: Option[Int] = s.toIntOption

"123".toIntSafe  // Some(123)
"abc".toIntSafe  // None

// 3. Resolve conflicts with explicit imports
import library1.{given OrderingInstance}  // Import specific given only

// 4. Keep type class hierarchy simple
trait Show[A]:
  def show(a: A): String

// Limit derived instances to one level
given [A: Show]: Show[List[A]] = ...
```

### Debugging Tips

```scala
// Check which implicit was selected
// scalac: -Xprint:typer
// sbt: set scalacOptions += "-Xprint:typer"

// Use summon in Scala 3
val ord = summon[Ordering[Int]]
println(ord)  // scala.math.Ordering$Int$@...
```

## Exercises

### 1. Printable Type Class

Define a `Printable` type class and implement instances for `Int`, `String`, and `List[A]`.

<details>
<summary>Show Answer</summary>

```scala
// Scala 3
trait Printable[A]:
  def format(a: A): String

given Printable[Int] with
  def format(a: Int): String = a.toString

given Printable[String] with
  def format(a: String): String = s"\"$a\""

given [A](using p: Printable[A]): Printable[List[A]] with
  def format(list: List[A]): String =
    list.map(p.format).mkString("[", ", ", "]")

def print[A](a: A)(using p: Printable[A]): Unit =
  println(p.format(a))

print(42)                    // 42
print("hello")               // "hello"
print(List(1, 2, 3))         // [1, 2, 3]
print(List("a", "b", "c"))   // ["a", "b", "c"]
```

</details>

### 2. Extension Method Implementation

Add a `times` method to `Int`: `3.times { println("Hello") }`

<details>
<summary>Show Answer</summary>

```scala
extension (n: Int)
  def times(action: => Unit): Unit =
    for _ <- 1 to n do action

3.times {
  println("Hello")
}
// Hello
// Hello
// Hello
```

</details>

## Next Steps

- [Type Classes](../type-classes/) — Advanced type class patterns
- [Functional Patterns](../functional-patterns/) — Functor, Monad
