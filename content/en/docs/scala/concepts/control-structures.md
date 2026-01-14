---
lastmod: "2026-01-14"
title: Control Structures
weight: 2
---

{{< callout type="info" title="TL;DR" >}}
- Scala's if, for, and match are all **expressions** that return values
- for comprehensions concisely express collection transformations and monadic operations
- match is more powerful than Java's switch, supporting type matching and guard conditions
- while is a statement, so avoid it in functional code
{{< /callout >}}

**Target Audience:** Developers with experience in other languages like Java/Python
**Prerequisites:** Scala basic syntax (variables, types)

Scala's control structures are **expressions**. That is, all control structures return values. This is fundamentally different from statement-based control structures in languages like Java or C. The expression-based approach makes code more concise and functional.

#### if Expression

Scala's `if` is an expression, not a statement. Therefore, there's no need for a ternary operator—if itself returns a value.

**Basic Usage**

```scala
val x = 10

// if expression returns a value
val result = if (x > 5) "large" else "small or equal"
println(result)  // large

// No ternary operator needed (if itself returns a value)
val max = if (a > b) a else b
```

**Scala 3 Syntax**

In Scala 3, you can use the `then` keyword for more natural conditional syntax. Omitting parentheses and using indentation-based syntax makes code more readable.

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

**Unit Return**

Without `else`, the type can be inferred as `Unit`. In this case, if is used for side effects.

```scala
val x = 10

// Without else, type can be inferred as Unit
if (x > 5) println("large")

// Explicit Unit type
val result: Unit = if (x > 5) println("large")
```

{{< callout type="info" title="Key Points" >}}
- if is an **expression** that returns a value, eliminating the need for a ternary operator
- Scala 3 allows more natural syntax with the `then` keyword
- Without else, it can return `Unit`
{{< /callout >}}

#### for Expression

Scala's `for` is very powerful. It's used for everything from simple iteration to collection transformation. Also called for comprehensions, it can concisely express monadic operations.

**Basic Iteration**

You can iterate over Range or collection elements. `to` includes the end value, while `until` excludes it.

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

**Guards (Condition Filters)**

Using `if` guards allows processing only elements that meet specific conditions. You can also combine multiple conditions.

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

**Nested Iteration**

Using multiple generators allows concise expression of nested iteration. Useful for multiplication tables or coordinate generation.

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

**yield - Create New Collection**

Using `yield` makes the for expression return a new collection. This is equivalent to a combination of map, flatMap, and filter.

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

**With Pattern Matching**

You can use pattern matching in for expressions. Useful for decomposing tuples, case classes, or Option values.

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

**Scala 3 Syntax**

In Scala 3, you can write match with indentation-based syntax using the `do` keyword.

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

{{< callout type="info" title="Key Points" >}}
- for is an **expression** that creates new collections with `yield`
- Guards (`if`) filter conditions, nested generators enable multiple iterations
- Can be used with pattern matching to decompose tuples or Option values
- for comprehension is syntactic sugar for map, flatMap, and filter
{{< /callout >}}

#### while Loop

`while` is a statement, not an expression. It doesn't return a value and returns `Unit`. Since it requires mutable state, it's avoided in functional programming.

```scala
var i = 0
while (i < 5) {
  println(i)
  i += 1
}
```

**do-while (Scala 2 Only)**

`do-while` was removed in Scala 3. In Scala 3, you should replace it with another approach.

> ⚠️ **Warning**: `do-while` was **removed in Scala 3**. Use `while` loop instead in Scala 3.

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

{{< callout type="info" title="Key Points" >}}
- while is a **statement** that doesn't return a value (returns `Unit`)
- Since it requires mutable state (`var`), it's avoided in functional code
- `do-while` was removed in Scala 3
{{< /callout >}}

#### match Expression

Scala's `match` is much more powerful than Java's `switch`. It supports value matching, type matching, pattern matching, and guard conditions.

**Basic Matching**

Returns different results based on values. `_` is a wildcard that matches any value.

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

**Type Matching**

Can branch based on value types. Safely performs type checking and casting.

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

**Guard Conditions**

Using `if` guards allows specifying additional conditions. Guard conditions are evaluated after pattern matching.

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

**OR Pattern**

Using `|` allows grouping multiple patterns into one case.

```scala
val char = 'a'

val result = char match {
  case 'a' | 'e' | 'i' | 'o' | 'u' => "vowel"
  case _ => "consonant"
}
```

**Scala 3 Syntax**

In Scala 3, you can write match with indentation-based syntax.

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

{{< callout type="info" title="Key Points" >}}
- match is an **expression** that returns a value
- Supports value matching, type matching, guard conditions (`if`), and OR patterns (`|`)
- `_` is a wildcard that matches all values
- More powerful than Java's switch and the foundation of pattern matching
{{< /callout >}}

#### Expression vs Statement

Almost everything in Scala is an expression. Blocks, try-catch, and even throw return values.

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
  if (b == 0) throw new ArithmeticException("Cannot divide by zero")
  else a / b
```

{{< callout type="info" title="Key Points" >}}
- Almost everything in Scala is an **expression**
- The last value of a block becomes the result value
- try-catch is also an expression that returns a value
- throw is an expression of type `Nothing`
{{< /callout >}}

#### Exercises

Practice control structures with these exercises.

**1. FizzBuzz**

For numbers 1 to 100, print "Fizz" for multiples of 3, "Buzz" for multiples of 5, "FizzBuzz" for multiples of both 3 and 5, and the number otherwise.

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

**2. Multiplication Table**

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

**3. Grade Calculator**

Write a function that takes a score (0-100) and returns a letter grade: A for 90 and above, B for 80 and above, C for 70 and above, D for 60 and above, and F for below 60.

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

#### Next Steps

Once you've learned control structures, proceed to the next topics.

- [Functions and Methods](../functions-methods/) — Function definition and advanced features
- [Pattern Matching](../pattern-matching/) — Advanced match expressions
