---
lastmod: "2026-01-06"
title: Pattern Matching
weight: 6
---

Pattern matching is one of Scala's most powerful features. It elegantly handles analyzing value structures, extracting data, and branching based on conditions.

## Basic match Expression

```scala
val x = 3

val result = x match {
  case 1 => "one"
  case 2 => "two"
  case 3 => "three"
  case _ => "other"  // Wildcard (default)
}
println(result)  // three
```

## Types of Patterns

### 1. Literal Pattern

```scala
def describe(x: Any): String = x match {
  case 0     => "zero"
  case true  => "true"
  case "hi"  => "greeting"
  case null  => "null"
  case _     => "other"
}
```

### 2. Variable Pattern

```scala
val x = 42

x match {
  case n => println(s"Value is $n")  // n binds to x
}

// Note: lowercase names are variable patterns
// Uppercase names are constant references
val One = 1
val two = 2

x match {
  case One => "Matches constant One"
  case two => "Matches any value (variable pattern)"
  // case `two` => "Backticks treat as constant"
}
```

### 3. Type Pattern

```scala
def describe(x: Any): String = x match {
  case i: Int       => s"Integer: $i"
  case s: String    => s"String: $s (length: ${s.length})"
  case d: Double    => s"Double: $d"
  case l: List[_]   => s"List (length: ${l.length})"
  case _            => "Unknown type"
}

println(describe(42))          // Integer: 42
println(describe("hello"))     // String: hello (length: 5)
println(describe(List(1,2,3))) // List (length: 3)
```

### 4. Tuple Pattern

```scala
val pair = (1, "one")

pair match {
  case (1, s)    => s"One: $s"
  case (2, s)    => s"Two: $s"
  case (n, s)    => s"$n: $s"
}

// Nested tuples
val nested = ((1, 2), (3, 4))
nested match {
  case ((a, b), (c, d)) => s"$a, $b, $c, $d"
}
```

### 5. Case Class Pattern

```scala
case class Person(name: String, age: Int)
case class Address(city: String, zipCode: String)
case class Employee(person: Person, address: Address)

val emp = Employee(Person("John", 30), Address("Seoul", "12345"))

emp match {
  case Employee(Person(name, age), Address(city, _)) =>
    s"$name ($age years old), lives in $city"
}
```

### 6. Sequence Pattern

```scala
val list = List(1, 2, 3, 4, 5)

list match {
  case Nil            => "Empty list"
  case head :: Nil    => s"Single element: $head"
  case head :: tail   => s"First: $head, rest: $tail"
}

// Specific patterns
list match {
  case List(1, 2, _*) => "Starts with 1, 2"  // Scala 3
  case _              => "Other pattern"
}

// Length check
list match {
  case List(a)          => s"1 element: $a"
  case List(a, b)       => s"2 elements: $a, $b"
  case List(a, b, c)    => s"3 elements: $a, $b, $c"
  case _ :: _ :: _ :: _ => "4 or more elements"
  case _                => "Empty list"
}
```

### 7. OR Pattern

```scala
val day = "Monday"

day match {
  case "Saturday" | "Sunday" => "Weekend"
  case _                     => "Weekday"
}

// Numbers
val n = 5
n match {
  case 1 | 2 | 3 => "Small"
  case 4 | 5 | 6 => "Medium"
  case _         => "Large"
}
```

## Guards

Use `if` conditions to further restrict patterns.

```scala
def classify(n: Int): String = n match {
  case x if x < 0   => "Negative"
  case x if x == 0  => "Zero"
  case x if x < 10  => "Single digit positive"
  case x if x < 100 => "Two digit positive"
  case _            => "Three or more digits"
}

// With case classes
case class Person(name: String, age: Int)

def describe(p: Person): String = p match {
  case Person(_, age) if age < 0   => "Invalid age"
  case Person(name, _) if name.isEmpty => "No name"
  case Person(name, age) if age < 18 => s"$name is a minor"
  case Person(name, age)             => s"$name is an adult"
}
```

## Pattern Binding (@)

Bind the entire value to a variable while also destructuring.

```scala
case class Person(name: String, age: Int)

val person = Person("Alice", 30)

person match {
  case p @ Person(_, age) if age >= 18 =>
    println(s"Adult: $p")  // Use entire Person object
  case _ =>
    println("Minor")
}

// With lists
List(1, 2, 3) match {
  case all @ (first :: rest) =>
    println(s"All: $all, First: $first, Rest: $rest")
  case _ =>
    println("Empty list")
}
```

## Extractors

Define custom patterns with `unapply` methods.

```scala
object Even {
  def unapply(n: Int): Boolean = n % 2 == 0
}

object Odd {
  def unapply(n: Int): Boolean = n % 2 != 0
}

42 match {
  case Even() => "Even"
  case Odd()  => "Odd"
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
  case Email(user, domain) => s"User: $user, Domain: $domain"
  case _ => "Invalid email"
}
```

## Scala 3 New Features

### Indentation-Based Syntax

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// Block defined by indentation without braces
val x: Any = "hello"

x match
  case s: String => s"String: $s"
  case i: Int    => s"Integer: $i"
  case _         => "Other"
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
val x: Any = "hello"

x match {
  case s: String => s"String: $s"
  case i: Int    => s"Integer: $i"
  case _         => "Other"
}
```
{{% /tab %}}
{{< /tabs >}}

### @switch Annotation

```scala
import scala.annotation.switch

// Compiler guarantees jump table generation
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

### Match Types (Scala 3 Only)

Pattern matching at the type level:

```scala
// Return type determined by input type
type Elem[X] = X match
  case String      => Char
  case Array[t]    => t
  case Iterable[t] => t

// Type determined at compile time
val char: Elem[String] = 'a'        // Char
val int: Elem[Array[Int]] = 1       // Int
val str: Elem[List[String]] = "hi"  // String
```

> Match Types are an advanced feature used for type-level programming. See [Advanced Types](../type-system-advanced/) for details.

## Where Pattern Matching is Used

### 1. val Definition

```scala
val (a, b) = (1, 2)
val Person(name, age) = Person("Alice", 30)
val head :: tail = List(1, 2, 3)
```

### 2. for Expression

```scala
val pairs = List((1, "one"), (2, "two"), (3, "three"))

for ((num, str) <- pairs) {
  println(s"$num = $str")
}

// Option filtering
val maybeValues = List(Some(1), None, Some(3))
for (Some(x) <- maybeValues) {
  println(x)  // 1, 3 (skips None)
}
```

### 3. catch Clause

```scala
try {
  // Dangerous code
} catch {
  case e: NumberFormatException => "Number format error"
  case e: IllegalArgumentException => "Invalid argument"
  case e: Exception => s"Other error: ${e.getMessage}"
}
```

### 4. Partial Functions

```scala
val divide: PartialFunction[(Int, Int), Int] = {
  case (a, b) if b != 0 => a / b
}

println(divide.isDefinedAt((10, 2)))  // true
println(divide.isDefinedAt((10, 0)))  // false
println(divide((10, 2)))              // 5

// With collect
val pairs = List((10, 2), (20, 0), (30, 3))
val results = pairs.collect {
  case (a, b) if b != 0 => a / b
}
println(results)  // List(5, 10)
```

## Exhaustiveness Checking

`sealed` types allow the compiler to check all cases.

```scala
sealed trait Color
case object Red extends Color
case object Green extends Color
case object Blue extends Color

// Warning: match may not be exhaustive
def describe(c: Color): String = c match {
  case Red   => "Red"
  case Green => "Green"
  // Blue missing - warning!
}

// Suppress warning with @unchecked (not recommended)
def describe2(c: Color): String = (c: @unchecked) match {
  case Red => "Red"
}
```

## Common Mistakes and Anti-patterns

### What to Avoid

```scala
// 1. Using isInstanceOf/asInstanceOf instead of match
def process(x: Any): String = {
  if (x.isInstanceOf[Int]) x.asInstanceOf[Int].toString
  else if (x.isInstanceOf[String]) x.asInstanceOf[String]
  else "unknown"
}  // Not type safe!

// 2. Wildcard pattern first
x match {
  case _ => "default"     // Always matches!
  case n: Int => n.toString  // Unreachable code
}

// 3. Using get on Option
val opt: Option[Int] = Some(5)
opt.get  // NoSuchElementException if None!

// 4. Incomplete pattern matching (sealed types)
sealed trait Color
case object Red extends Color
case object Blue extends Color

def name(c: Color) = c match {
  case Red => "red"
  // Blue missing - runtime error possible!
}
```

### The Right Way

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
// or
opt.getOrElse(0)
opt.fold("default")(_.toString)

// 4. Handle all cases
def name(c: Color) = c match {
  case Red => "red"
  case Blue => "blue"  // Complete matching
}
```

## Exercises

### 1. List Sum

Write a recursive function to sum a list using pattern matching.

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

### 2. JSON Parser

Write an ADT representing simple JSON values and a stringify function.

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

### 3. Create an Extractor

Write an extractor that decomposes a URL into protocol, host, and path.

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
    println(s"Protocol: $protocol, Host: $host, Path: $path")
  case _ =>
    println("Invalid URL")
}
// Protocol: https, Host: example.com, Path: /path/to/page
```

</details>

## Next Steps

- [Collections](../collections/) — Scala collection library
- [Higher-Order Functions](../higher-order-functions/) — Advanced functional programming
