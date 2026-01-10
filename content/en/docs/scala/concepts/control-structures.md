---
lastmod: "2026-01-06"
title: Control Structures
weight: 2
---

Scala's control structures are **expressions**. That is, all control structures return values.

## if Expression

### Basic Usage

Scala's `if` is an expression, not a statement.

```scala
val x = 10

// if expression returns a value
val result = if (x > 5) "large" else "small or equal"
println(result)  // large

// No need for ternary operator (if itself returns a value)
val max = if (a > b) a else b
```

### Scala 3 Syntax

Scala 3 allows using the `then` keyword.

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
val x = 10

// Using then keyword (recommended)
val result = if x > 5 then "large" else "small or equal"

// Multiple lines
val message =
  if x > 100 then
    "very large"
  else if x > 50 then
    "large"
  else
    "small"
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
val x = 10

// Parentheses required
val result = if (x > 5) "large" else "small or equal"

// Multiple lines
val message = {
  if (x > 100) {
    "very large"
  } else if (x > 50) {
    "large"
  } else {
    "small"
  }
}
```
{{% /tab %}}
{{< /tabs >}}

### Unit Return

Without `else`, the result can be `Unit`.

```scala
val x = 10

// Without else, type can be inferred as Unit
if (x > 5) println("large")

// Explicit Unit type
val result: Unit = if (x > 5) println("large")
```

## for Expression

Scala's `for` is very powerful. It's used for everything from simple iteration to collection transformation.

### Basic Iteration

```scala
// Iteration with Range
for (i <- 1 to 5) {
  println(i)  // 1, 2, 3, 4, 5
}

// until: excludes end value
for (i <- 1 until 5) {
  println(i)  // 1, 2, 3, 4
}

// Collection iteration
val fruits = List("apple", "banana", "cherry")
for (fruit <- fruits) {
  println(fruit)
}
```

### Guards (Condition Filters)

```scala
// Execute only when condition is true
for (i <- 1 to 10 if i % 2 == 0) {
  println(i)  // 2, 4, 6, 8, 10
}

// Multiple conditions
for {
  i <- 1 to 100
  if i % 3 == 0
  if i % 5 == 0
} println(i)  // 15, 30, 45, 60, 75, 90
```

### Nested Iteration

```scala
// Multiplication table
for {
  i <- 2 to 9
  j <- 1 to 9
} {
  println(s"$i x $j = ${i * j}")
}

// Generate coordinates
for {
  x <- 0 until 3
  y <- 0 until 3
} println(s"($x, $y)")
```

### yield - Create New Collection

Using `yield` makes the for expression return a new collection.

```scala
// Transform each element to create new list
val numbers = List(1, 2, 3, 4, 5)
val doubled = for (n <- numbers) yield n * 2
// List(2, 4, 6, 8, 10)

// Filter + Transform
val evenSquares = for {
  n <- 1 to 10
  if n % 2 == 0
} yield n * n
// Vector(4, 16, 36, 64, 100)

// Nested + yield
val pairs = for {
  x <- 1 to 3
  y <- 1 to 3
} yield (x, y)
// Vector((1,1), (1,2), (1,3), (2,1), (2,2), (2,3), (3,1), (3,2), (3,3))
```

### With Pattern Matching

```scala
val pairs = List((1, "one"), (2, "two"), (3, "three"))

for ((num, str) <- pairs) {
  println(s"$num = $str")
}

// Extract values from Option
val maybeValues = List(Some(1), None, Some(3), None, Some(5))
for (Some(value) <- maybeValues) {
  println(value)  // 1, 3, 5 (skips None)
}
```

### Scala 3 Syntax

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// do keyword (optional)
for i <- 1 to 5 do
  println(i)

// Indentation-based
for
  i <- 1 to 3
  j <- 1 to 3
do
  println(s"$i, $j")

// yield
val result = for
  i <- 1 to 5
  if i % 2 == 0
yield i * i
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// Using braces
for (i <- 1 to 5) {
  println(i)
}

// Multiple generators
for {
  i <- 1 to 3
  j <- 1 to 3
} {
  println(s"$i, $j")
}

// yield
val result = for {
  i <- 1 to 5
  if i % 2 == 0
} yield i * i
```
{{% /tab %}}
{{< /tabs >}}

## while Loop

`while` is a statement, not an expression. It doesn't return a value and returns `Unit`.

```scala
var i = 0
while (i < 5) {
  println(i)
  i += 1
}
```

### do-while (Scala 2 Only)

> **Note:** `do-while` was **removed in Scala 3**. Use `while` loop instead.

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// Use while instead of do-while
var j = 0
while {
  println(j)
  j += 1
  j < 5  // Evaluate condition at the end
} do ()

// Or more simply
var k = 0
while
  println(k)
  k += 1
  k < 5
do ()
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// do-while available
var j = 0
do {
  println(j)
  j += 1
} while (j < 5)
```
{{% /tab %}}
{{< /tabs >}}

> **In functional programming, prefer `for` or recursion over `while`.**
> `while` requires mutable state (`var`).

## match Expression

Scala's `match` is much more powerful than Java's `switch`.

### Basic Matching

```scala
val day = 3

val dayName = day match {
  case 1 => "Monday"
  case 2 => "Tuesday"
  case 3 => "Wednesday"
  case 4 => "Thursday"
  case 5 => "Friday"
  case 6 => "Saturday"
  case 7 => "Sunday"
  case _ => "Invalid"  // Default (wildcard)
}
println(dayName)  // Wednesday
```

### Type Matching

```scala
def describe(x: Any): String = x match {
  case i: Int    => s"Integer: $i"
  case s: String => s"String: $s"
  case d: Double => s"Double: $d"
  case _         => "Unknown type"
}

println(describe(42))      // Integer: 42
println(describe("hello")) // String: hello
println(describe(3.14))    // Double: 3.14
```

### Guard Conditions

```scala
val x = 15

val result = x match {
  case n if n < 0  => "negative"
  case n if n == 0 => "zero"
  case n if n < 10 => "single digit positive"
  case n if n < 100 => "two digit positive"
  case _ => "three or more digits"
}
println(result)  // two digit positive
```

### OR Pattern

```scala
val char = 'a'

val result = char match {
  case 'a' | 'e' | 'i' | 'o' | 'u' => "vowel"
  case _ => "consonant"
}
```

### Scala 3 Syntax

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
val day = 3

// Indentation-based
val dayName = day match
  case 1 => "Monday"
  case 2 => "Tuesday"
  case 3 => "Wednesday"
  case _ => "Other"
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
val day = 3

// Braces required
val dayName = day match {
  case 1 => "Monday"
  case 2 => "Tuesday"
  case 3 => "Wednesday"
  case _ => "Other"
}
```
{{% /tab %}}
{{< /tabs >}}

## Expression vs Statement

Almost everything in Scala is an expression.

```scala
// Blocks are also expressions - last value is the result
val result = {
  val a = 1
  val b = 2
  a + b  // Block's result value
}
println(result)  // 3

// try-catch is also an expression
val parsed: Int = try {
  "42".toInt
} catch {
  case _: NumberFormatException => 0
}

// throw is also an expression (Nothing type)
def divide(a: Int, b: Int): Int =
  if (b == 0) throw new ArithmeticException("Cannot divide by 0")
  else a / b
```

## Exercises

### 1. FizzBuzz

For numbers 1 to 100:
- Multiple of 3: "Fizz"
- Multiple of 5: "Buzz"
- Multiple of both 3 and 5: "FizzBuzz"
- Otherwise: print the number

<details>
<summary>Show Answer</summary>

```scala
for (i <- 1 to 100) {
  val result = (i % 3, i % 5) match {
    case (0, 0) => "FizzBuzz"
    case (0, _) => "Fizz"
    case (_, 0) => "Buzz"
    case _      => i.toString
  }
  println(result)
}
```

</details>

### 2. Multiplication Table

Generate a multiplication table from 2 to 9 using `for` + `yield`.

<details>
<summary>Show Answer</summary>

```scala
val table = for {
  i <- 2 to 9
  j <- 1 to 9
} yield s"$i x $j = ${i * j}"

table.foreach(println)
```

</details>

### 3. Grade Calculator

Write a function that takes a score (0-100) and returns a letter grade.
- 90 and above: A
- 80 and above: B
- 70 and above: C
- 60 and above: D
- Below 60: F

<details>
<summary>Show Answer</summary>

```scala
def grade(score: Int): String = score match {
  case s if s >= 90 => "A"
  case s if s >= 80 => "B"
  case s if s >= 70 => "C"
  case s if s >= 60 => "D"
  case _            => "F"
}

println(grade(95))  // A
println(grade(72))  // C
println(grade(55))  // F
```

</details>

## Next Steps

- [Functions and Methods](../functions-methods/) — Function definition and advanced features
- [Pattern Matching](../pattern-matching/) — Advanced match expressions
