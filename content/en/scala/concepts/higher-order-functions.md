---
lastmod: "2026-01-06"
title: Higher-Order Functions
weight: 8
---

Higher-Order Functions are functions that take functions as arguments or return functions. They are a core concept in functional programming.

## What is a Higher-Order Function?

```scala
// Function that takes a function as argument
def applyTwice(f: Int => Int, x: Int): Int = f(f(x))

val double = (x: Int) => x * 2
applyTwice(double, 3)  // 12 (3 -> 6 -> 12)

// Function that returns a function
def multiplier(factor: Int): Int => Int = {
  (x: Int) => x * factor
}

val triple = multiplier(3)
triple(4)  // 12
```

## Key Higher-Order Functions

### map

Transforms each element.

```scala
val numbers = List(1, 2, 3, 4, 5)

// Double each element
numbers.map(x => x * 2)     // List(2, 4, 6, 8, 10)
numbers.map(_ * 2)          // Shorthand

// Type conversion
numbers.map(_.toString)     // List("1", "2", "3", "4", "5")

// Complex transformation
case class Person(name: String, age: Int)
val ages = List(25, 30, 35)
ages.map(age => Person(s"Person$age", age))
```

### filter

Selects only matching elements.

```scala
val numbers = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

numbers.filter(_ % 2 == 0)     // List(2, 4, 6, 8, 10)
numbers.filter(_ > 5)          // List(6, 7, 8, 9, 10)
numbers.filterNot(_ % 2 == 0)  // List(1, 3, 5, 7, 9)

// Chaining
numbers
  .filter(_ % 2 == 0)
  .filter(_ > 4)
// List(6, 8, 10)
```

### flatMap

Transforms and flattens.

```scala
val numbers = List(1, 2, 3)

// map + flatten
numbers.map(n => List(n, n * 10))
// List(List(1, 10), List(2, 20), List(3, 30))

numbers.flatMap(n => List(n, n * 10))
// List(1, 10, 2, 20, 3, 30)

// With Option
def parse(s: String): Option[Int] = s.toIntOption

val strings = List("1", "two", "3")
strings.flatMap(parse)  // List(1, 3)
```

### fold / foldLeft / foldRight

Reduces elements with an initial value.

```scala
val numbers = List(1, 2, 3, 4, 5)

// foldLeft: Reduce from left
numbers.foldLeft(0)(_ + _)    // 15
numbers.foldLeft(1)(_ * _)    // 120

// Visualization: ((((0 + 1) + 2) + 3) + 4) + 5

// foldRight: Reduce from right
numbers.foldRight(0)(_ + _)   // 15
// Process: 1 + (2 + (3 + (4 + (5 + 0))))

// String concatenation
List("a", "b", "c").foldLeft("")(_ + _)  // "abc"

// Complex reduction
case class Stats(sum: Int, count: Int)
numbers.foldLeft(Stats(0, 0)) { (stats, n) =>
  Stats(stats.sum + n, stats.count + 1)
}
// Stats(15, 5)
```

### reduce

Reduces without initial value (error on empty collection).

```scala
val numbers = List(1, 2, 3, 4, 5)

numbers.reduce(_ + _)    // 15
numbers.reduce(_ * _)    // 120
numbers.reduce(_ max _)  // 5
numbers.reduce(_ min _)  // 1

// reduceOption: Returns None for empty collection
List.empty[Int].reduceOption(_ + _)  // None
```

### collect

Filter + transform with pattern matching.

```scala
val mixed: List[Any] = List(1, "hello", 2, "world", 3)

// Extract integers and double them
mixed.collect {
  case i: Int => i * 2
}
// List(2, 4, 6)

// Extract values from Option
val maybes = List(Some(1), None, Some(3), None, Some(5))
maybes.collect {
  case Some(n) => n
}
// List(1, 3, 5)
```

### partition

Splits into two groups by condition.

```scala
val numbers = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

val (evens, odds) = numbers.partition(_ % 2 == 0)
// evens = List(2, 4, 6, 8, 10)
// odds = List(1, 3, 5, 7, 9)
```

### groupBy

Groups by key function.

```scala
val words = List("apple", "banana", "avocado", "cherry", "apricot")

val byFirstLetter = words.groupBy(_.head)
// Map(
//   'a' -> List("apple", "avocado", "apricot"),
//   'b' -> List("banana"),
//   'c' -> List("cherry")
// )

case class Person(name: String, city: String)
val people = List(
  Person("Alice", "Seoul"),
  Person("Bob", "Busan"),
  Person("Carol", "Seoul")
)

val byCity = people.groupBy(_.city)
// Map("Seoul" -> List(Alice, Carol), "Busan" -> List(Bob))
```

## Function Composition

### andThen and compose

```scala
val addOne = (x: Int) => x + 1
val double = (x: Int) => x * 2

// andThen: Left -> Right
val addThenDouble = addOne andThen double
addThenDouble(3)  // (3 + 1) * 2 = 8

// compose: Right -> Left
val doubleThenAdd = addOne compose double
doubleThenAdd(3)  // (3 * 2) + 1 = 7
```

### Chaining

```scala
val numbers = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

val result = numbers
  .filter(_ % 2 == 0)     // Only evens
  .map(_ * 2)             // Double
  .filter(_ > 10)         // Greater than 10
  .sum                    // Sum

// result = 12 + 16 + 20 = 48
```

## Currying

Transforms a function with multiple arguments into a chain of single-argument functions.

```scala
// Regular function
def add(a: Int, b: Int): Int = a + b
add(1, 2)  // 3

// Curried function
def addCurried(a: Int)(b: Int): Int = a + b
addCurried(1)(2)  // 3

// Partial application
val add5 = addCurried(5)
add5(3)  // 8

// Curry existing function
val addCurried2 = (add _).curried
val add10 = addCurried2(10)
add10(5)  // 15
```

### Currying Use Cases

```scala
// Improved type inference
def transform[A, B](list: List[A])(f: A => B): List[B] =
  list.map(f)

// A type inferred from first argument, no need to specify f's type
transform(List(1, 2, 3))(x => x * 2)

// DSL style (pseudo code)
// Database and Connection are hypothetical types
trait Connection:
  def execute(sql: String): Unit
  def close(): Unit

trait Database:
  def connect(): Connection

def withTransaction[T](db: Database)(block: Connection => T): T =
  val conn = db.connect()
  try block(conn)
  finally conn.close()

// Usage example
// withTransaction(myDatabase) { conn =>
//   conn.execute("INSERT ...")
// }
```

## Closures

Functions capture variables from their defining environment.

```scala
def makeCounter(): () => Int = {
  var count = 0
  () => {
    count += 1
    count
  }
}

val counter = makeCounter()
counter()  // 1
counter()  // 2
counter()  // 3

val anotherCounter = makeCounter()
anotherCounter()  // 1 (independent count)
```

## Partial Functions

Functions defined only for some inputs.

```scala
val divide: PartialFunction[(Int, Int), Int] = {
  case (a, b) if b != 0 => a / b
}

divide.isDefinedAt((10, 2))   // true
divide.isDefinedAt((10, 0))   // false

divide((10, 2))  // 5
// divide((10, 0))  // MatchError

// With collect
val pairs = List((10, 2), (20, 0), (30, 3))
pairs.collect(divide)  // List(5, 10)

// Combine with orElse
val safeDivide = divide orElse {
  case (a, 0) => 0
}
safeDivide((10, 0))  // 0
```

## Common Mistakes and Anti-patterns

### What to Avoid

```scala
// 1. Unnecessary lambda wrapping
list.map(x => f(x))  // Inefficient
list.map(x => x.toString)  // Inefficient

// 2. var + foreach instead of foldLeft
var sum = 0
list.foreach(sum += _)  // Mutable state!

// 3. map + flatten instead of flatMap
list.map(f).flatten  // Creates intermediate collection

// 4. Overusing complex placeholders
list.map(_ + _ * _)  // Hard to read!

// 5. Side effects in map
list.map { x =>
  println(x)  // Side effect!
  x * 2
}
```

### The Right Way

```scala
// 1. Use method reference (eta expansion)
list.map(f)
list.map(_.toString)

// 2. Use foldLeft
list.foldLeft(0)(_ + _)

// 3. Use flatMap
list.flatMap(f)

// 4. Use explicit lambda
list.reduce((a, b) => a + b * c)

// 5. Separate transformation and side effects
val doubled = list.map(_ * 2)
doubled.foreach(println)
// Or use tap (Scala 2.13+)
list.map(_ * 2).tapEach(println)
```

### Performance Tips

```scala
// Chaining vs View
// Each operation creates new collection
list.map(_ * 2).filter(_ > 10).take(5)

// View for lazy evaluation (no intermediate collections)
list.view.map(_ * 2).filter(_ > 10).take(5).toList

// Especially effective for large collections
(1 to 1000000)
  .view
  .map(_ * 2)
  .filter(_ % 3 == 0)
  .take(10)
  .toList
```

## Exercises

### 1. Implement Your Own map

Implement `myMap` using foldRight.

<details>
<summary>Show Answer</summary>

```scala
def myMap[A, B](list: List[A])(f: A => B): List[B] =
  list.foldRight(List.empty[B]) { (elem, acc) =>
    f(elem) :: acc
  }

myMap(List(1, 2, 3))(_ * 2)  // List(2, 4, 6)
```

</details>

### 2. Pipeline Function

Implement a `pipe` function that applies multiple functions sequentially.

<details>
<summary>Show Answer</summary>

```scala
def pipe[A](value: A)(functions: (A => A)*): A =
  functions.foldLeft(value)((v, f) => f(v))

pipe(5)(
  _ + 1,   // 6
  _ * 2,   // 12
  _ - 3    // 9
)  // 9
```

</details>

### 3. Memoization

Implement a higher-order function that caches results.

<details>
<summary>Show Answer</summary>

```scala
def memoize[A, B](f: A => B): A => B = {
  val cache = scala.collection.mutable.Map.empty[A, B]
  (a: A) => cache.getOrElseUpdate(a, f(a))
}

def slowFib(n: Int): BigInt =
  if (n <= 1) n else slowFib(n - 1) + slowFib(n - 2)

lazy val fastFib: Int => BigInt = memoize { n =>
  if (n <= 1) n else fastFib(n - 1) + fastFib(n - 2)
}

fastFib(100)  // Calculated quickly
```

</details>

## Next Steps

- [For Comprehension](../for-comprehensions/) — Elegant expression of monadic operations
- [Implicit/Given](../implicits/) — Contextual abstraction
