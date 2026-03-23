---
lastmod: "2026-01-14"
title: Pattern Matching
description: "Pattern matching mechanics and matching strategies"
weight: 6
---

{{< callout type="info" title="TL;DR" >}}
- Pattern matching is a powerful feature for analyzing value structures and extracting data
- Supports various patterns: literals, variables, types, tuples, case classes, sequences, and more
- Guards (`if`) add conditions, `@` binds the entire value
- Compiler checks exhaustiveness for `sealed` types
{{< /callout >}}

**Target Audience:** Developers familiar with basic Scala syntax
**Prerequisites:** Case classes, basic type system

Pattern matching is one of Scala's most powerful features. It elegantly handles value structure analysis, data extraction, and conditional branching. Unlike Java's switch statement, Scala's match is an expression that returns a value and provides much richer functionality including type matching, destructuring, guard conditions, and more.

#### Basic match Expression

A match expression compares a value against multiple patterns in order and returns the result of the first matching case. The wildcard pattern (`_`) matches any value, so it's used as a default.

```scala
val x = 3

val result = x match {
  case 1 => "one"
  case 2 => "two"
  case 3 => "three"
  case _ => "other"  // wildcard (default)
}
println(result)  // three
```

#### Pattern Types

Scala supports various types of patterns. You can match literal values, variable bindings, type checks, tuples, case classes, sequences, and more.

**1. Literal Pattern**

Matches directly against literal values like integers, strings, and booleans.

```scala
def describe(x: Any): String = x match {
  case 0     => "zero"
  case true  => "true"
  case "hi"  => "greeting"
  case null  => "null"
  case _     => "other"
}
```

**2. Variable Pattern**

Names starting with lowercase are variable patterns that bind the matched value. Names starting with uppercase are interpreted as constant references.

```scala
val x = 42

x match {
  case n => println(s"value is $n")  // n is bound to x
}

// Note: lowercase names are variable patterns
// uppercase names are constant references
val One = 1
val two = 2

x match {
  case One => "matches constant One"
  case two => "matches any value (variable pattern)"
  // case `two` => "treats as constant with backticks"
}
```

**3. Type Pattern**

Checks the runtime type of a value and casts it to that type. Safer than isInstanceOf/asInstanceOf.

```scala
def describe(x: Any): String = x match {
  case i: Int       => s"integer: $i"
  case s: String    => s"string: $s (length: ${s.length})"
  case d: Double    => s"double: $d"
  case l: List[_]   => s"list (length: ${l.length})"
  case _            => "unknown type"
}

println(describe(42))          // integer: 42
println(describe("hello"))     // string: hello (length: 5)
println(describe(List(1,2,3))) // list (length: 3)
```

**4. Tuple Pattern**

Deconstructs each element of a tuple. Nested tuples are also supported.

```scala
val pair = (1, "one")

pair match {
  case (1, s)    => s"one: $s"
  case (2, s)    => s"two: $s"
  case (n, s)    => s"$n: $s"
}

// Nested tuples
val nested = ((1, 2), (3, 4))
nested match {
  case ((a, b), (c, d)) => s"$a, $b, $c, $d"
}
```

**5. Case Class Pattern**

Deconstructs fields of case classes. Nested case classes can also be deeply deconstructed.

```scala
case class Person(name: String, age: Int)
case class Address(city: String, zipCode: String)
case class Employee(person: Person, address: Address)

val emp = Employee(Person("Kim", 30), Address("Seoul", "12345"))

emp match {
  case Employee(Person(name, age), Address(city, _)) =>
    s"$name ($age years old), lives in $city"
}
```

**6. Sequence Pattern**

Matches the structure of sequences like lists or arrays. Use `::` to separate head and tail, or `_*` to ignore remaining elements.

```scala
val list = List(1, 2, 3, 4, 5)

list match {
  case Nil            => "empty list"
  case head :: Nil    => s"one element: $head"
  case head :: tail   => s"first: $head, rest: $tail"
}

// Specific patterns
list match {
  case List(1, 2, _*) => "starts with 1, 2"  // Scala 3
  // case List(1, 2, _*) => "starts with 1, 2"  // Scala 2 same
  case _              => "other pattern"
}

// Length check
list match {
  case List(a)          => s"1 element: $a"
  case List(a, b)       => s"2 elements: $a, $b"
  case List(a, b, c)    => s"3 elements: $a, $b, $c"
  case _ :: _ :: _ :: _ => "4 or more elements"
  case _                => "empty list"
}
```

**7. OR Pattern**

The `|` operator combines multiple patterns into a single case. Useful when multiple values should perform the same action.

```scala
val day = "Monday"

day match {
  case "Saturday" | "Sunday" => "weekend"
  case _                     => "weekday"
}

// Numbers
val n = 5
n match {
  case 1 | 2 | 3 => "small number"
  case 4 | 5 | 6 => "medium number"
  case _         => "large number"
}
```

{{< callout type="info" title="Key Points" >}}
- **Literal patterns**: Match exact values
- **Variable patterns**: Start with lowercase, bind values
- **Type patterns**: Runtime type check with safe casting
- **Tuple/Case class patterns**: Destructure to extract fields
- **Sequence patterns**: Deconstruct lists with `::`, `_*`
- **OR patterns**: Combine multiple patterns with `|`
{{< /callout >}}

#### Guard

`if` conditions can further restrict patterns. The guard condition is evaluated after the pattern matches. If the guard is false, it moves to the next case.

```scala
def classify(n: Int): String = n match {
  case x if x < 0   => "negative"
  case x if x == 0  => "zero"
  case x if x < 10  => "single digit positive"
  case x if x < 100 => "double digit positive"
  case _            => "three or more digits"
}

// With case classes
case class Person(name: String, age: Int)

def describe(p: Person): String = p match {
  case Person(_, age) if age < 0   => "invalid age"
  case Person(name, _) if name.isEmpty => "no name"
  case Person(name, age) if age < 18 => s"$name is a minor"
  case Person(name, age)             => s"$name is an adult"
}
```

#### Pattern Binding (@)

The `@` operator binds the entire value to a variable while simultaneously deconstructing its internal structure. Useful when you need the original object after pattern matching.

```scala
case class Person(name: String, age: Int)

val person = Person("Alice", 30)

person match {
  case p @ Person(_, age) if age >= 18 =>
    println(s"adult: $p")  // use entire Person object
  case _ =>
    println("minor")
}

// Use with lists
List(1, 2, 3) match {
  case all @ (first :: rest) =>
    println(s"all: $all, first: $first, rest: $rest")
  case _ =>
    println("empty list")
}
```

#### Extractor

Custom patterns can be created by defining the `unapply` method. To support pattern matching for non-case class types, define an extractor directly.

```scala
object Even {
  def unapply(n: Int): Boolean = n % 2 == 0
}

object Odd {
  def unapply(n: Int): Boolean = n % 2 != 0
}

42 match {
  case Even() => "even"
  case Odd()  => "odd"
}

// Extractor that extracts values
object Email {
  def unapply(email: String): Option[(String, String)] = {
    val parts = email.split("@")
    if (parts.length == 2) Some((parts(0), parts(1)))
    else None
  }
}

"user@example.com" match {
  case Email(user, domain) => s"user: $user, domain: $domain"
  case _ => "invalid email"
}
```

{{< callout type="info" title="Key Points" >}}
- **Guard (`if`)**: Additional condition check after pattern matching
- **Pattern binding (`@`)**: Bind entire value to variable while deconstructing internal structure
- **Extractor (`unapply`)**: Define custom patterns to extend pattern matching
{{< /callout >}}

#### Scala 3 New Features

Scala 3 added several new features to pattern matching.

**Indentation-based Syntax**

Match blocks can be defined with indentation instead of braces.

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// Define blocks with indentation without braces
val x: Any = "hello"

x match
  case s: String => s"string: $s"
  case i: Int    => s"integer: $i"
  case _         => "other"
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
val x: Any = "hello"

x match {
  case s: String => s"string: $s"
  case i: Int    => s"integer: $i"
  case _         => "other"
}
```
{{% /tab %}}
{{< /tabs >}}

**@switch Annotation**

The `@switch` annotation ensures the compiler generates a jump table. If a jump table cannot be generated, a compile error occurs.

```scala
import scala.annotation.switch

// Ensures compiler generates jump table
def dayOfWeek(n: Int): String = (n: @switch) match
  case 1 => "Mon"
  case 2 => "Tue"
  case 3 => "Wed"
  case 4 => "Thu"
  case 5 => "Fri"
  case 6 => "Sat"
  case 7 => "Sun"
  case _ => "?"
```

**Match Types (Scala 3 only)**

Match Types are a powerful Scala 3 feature that performs pattern matching at the type level. You can define different output types based on input types.

```scala
// Return type determined by type
type Elem[X] = X match
  case String      => Char
  case Array[t]    => t
  case Iterable[t] => t

// Type determined at compile time
val char: Elem[String] = 'a'        // Char
val int: Elem[Array[Int]] = 1       // Int
val str: Elem[List[String]] = "hi"  // String
```

> 💡 Match Types are an advanced feature used for type-level programming. For details, see [Advanced Type System]({{< relref "/docs/scala/concepts/type-system-advanced" >}}).

#### Where Pattern Matching is Used

Pattern matching is used in various places beyond match expressions.

**1. val Definition**

Values can be deconstructed with patterns during variable declaration.

```scala
val (a, b) = (1, 2)
val Person(name, age) = Person("Alice", 30)
val head :: tail = List(1, 2, 3)
```

**2. for Expression**

Using patterns in for comprehensions allows destructuring while iterating. Elements that don't match the pattern are automatically filtered out.

```scala
val pairs = List((1, "one"), (2, "two"), (3, "three"))

for ((num, str) <- pairs) {
  println(s"$num = $str")
}

// Option filtering
val maybeValues = List(Some(1), None, Some(3))
for (Some(x) <- maybeValues) {
  println(x)  // 1, 3 (None is skipped)
}
```

**3. catch Clause**

Exceptions are matched with patterns in try-catch.

```scala
try {
  // risky code
} catch {
  case e: NumberFormatException => "number format error"
  case e: IllegalArgumentException => "invalid argument"
  case e: Exception => s"other error: ${e.getMessage}"
}
```

**4. Partial Function**

PartialFunction is a function defined only for some inputs. Combined with pattern matching, it can process only values that meet certain conditions.

```scala
val divide: PartialFunction[(Int, Int), Int] = {
  case (a, b) if b != 0 => a / b
}

println(divide.isDefinedAt((10, 2)))  // true
println(divide.isDefinedAt((10, 0)))  // false
println(divide((10, 2)))              // 5

// Use with collect
val pairs = List((10, 2), (20, 0), (30, 3))
val results = pairs.collect {
  case (a, b) if b != 0 => a / b
}
println(results)  // List(5, 10)
```

{{< callout type="info" title="Key Points" >}}
- Pattern matching is used in val, for, catch, and PartialFunction beyond match
- PartialFunction performs filtering+transformation with `collect`
- Elements that don't match in for expressions are automatically filtered
{{< /callout >}}

#### Exhaustiveness Check

For `sealed` types, the compiler checks that all cases are covered. If a case is missing, it issues a warning to prevent runtime errors.

```scala
sealed trait Color
case object Red extends Color
case object Green extends Color
case object Blue extends Color

// Warning: match may not be exhaustive
def describe(c: Color): String = c match {
  case Red   => "red"
  case Green => "green"
  // Blue missing - warning!
}

// @unchecked suppresses warning (not recommended)
def describe2(c: Color): String = (c: @unchecked) match {
  case Red => "red"
}
```

#### Common Mistakes and Anti-patterns

Here are common mistakes when using pattern matching and proper solutions.

**❌ What to Avoid**

```scala
// 1. Using isInstanceOf/asInstanceOf instead of match
def process(x: Any): String = {
  if (x.isInstanceOf[Int]) x.asInstanceOf[Int].toString
  else if (x.isInstanceOf[String]) x.asInstanceOf[String]
  else "unknown"
}  // Not type safe!

// 2. Wildcard pattern comes first
x match {
  case _ => "default"     // Always matches!
  case n: Int => n.toString  // Unreachable code
}

// 3. Using get on Option
val opt: Option[Int] = Some(5)
opt.get  // NoSuchElementException if None!

// 4. Incomplete pattern matching (sealed type)
sealed trait Color
case object Red extends Color
case object Blue extends Color

def name(c: Color) = c match {
  case Red => "red"
  // Blue missing - runtime error possible!
}
```

**✅ Correct Approach**

```scala
// 1. Use pattern matching
def process(x: Any): String = x match {
  case n: Int => n.toString
  case s: String => s
  case _ => "unknown"
}

// 2. Specific patterns first
x match {
  case n: Int => n.toString
  case _ => "default"
}

// 3. Use pattern matching or getOrElse
opt match {
  case Some(n) => n.toString
  case None => "default"
}
// Or
opt.getOrElse(0)
opt.fold("default")(_.toString)

// 4. Handle all cases
def name(c: Color) = c match {
  case Red => "red"
  case Blue => "blue"  // Complete matching
}
```

#### Practice Problems

Review pattern matching concepts with these practice problems.

**1. List Sum ⭐**

Write a recursive function that calculates the sum of a list using pattern matching.

<details>
<summary>Show Answer</summary>

```scala
def sum(list: List[Int]): Int = list match {
  case Nil         => 0
  case head :: tail => head + sum(tail)
}

println(sum(List(1, 2, 3, 4, 5)))  // 15
```

</details>

**2. JSON Parser ⭐⭐**

Create an ADT representing simple JSON values and a stringify function.

<details>
<summary>Show Answer</summary>

```scala
sealed trait Json
case class JString(value: String) extends Json
case class JNumber(value: Double) extends Json
case class JBool(value: Boolean) extends Json
case object JNull extends Json
case class JArray(items: List[Json]) extends Json
case class JObject(fields: Map[String, Json]) extends Json

def stringify(json: Json): String = json match {
  case JString(s)    => s"\"$s\""
  case JNumber(n)    => n.toString
  case JBool(b)      => b.toString
  case JNull         => "null"
  case JArray(items) => items.map(stringify).mkString("[", ",", "]")
  case JObject(fields) =>
    fields.map { case (k, v) => s"\"$k\":${stringify(v)}" }
          .mkString("{", ",", "}")
}

val json = JObject(Map(
  "name" -> JString("Alice"),
  "age" -> JNumber(30),
  "active" -> JBool(true)
))

println(stringify(json))
// {"name":"Alice","age":30.0,"active":true}
```

</details>

**3. Creating an Extractor ⭐⭐⭐**

Write an extractor that deconstructs URLs into protocol, host, and path.

<details>
<summary>Show Answer</summary>

```scala
object URL {
  def unapply(url: String): Option[(String, String, String)] = {
    val pattern = """(\w+)://([^/]+)(.*)""".r
    url match {
      case pattern(protocol, host, path) =>
        Some((protocol, host, if (path.isEmpty) "/" else path))
      case _ => None
    }
  }
}

"https://example.com/path/to/page" match {
  case URL(protocol, host, path) =>
    println(s"protocol: $protocol, host: $host, path: $path")
  case _ =>
    println("invalid URL")
}
// protocol: https, host: example.com, path: /path/to/page
```

</details>

#### Next Steps

- [Collections]({{< relref "/docs/scala/concepts/collections" >}}) — Scala collection library
- [Higher-Order Functions]({{< relref "/docs/scala/concepts/higher-order-functions" >}}) — Advanced functional programming
