---
lastmod: "2026-01-14"
title: Collections
description: "Scala collection library mechanics and usage patterns"
weight: 7
---

{{< callout type="info" title="TL;DR" >}}
- Scala uses **immutable collections** by default (thread-safe, predictable)
- **List** is fast for prepending, **Vector** is fast for random access
- Built-in support for **higher-order functions** like map, filter, fold
- **Option** handles missing values type-safely instead of null
{{< /callout >}}

**Target Audience:** Developers familiar with Java collections
**Prerequisites:** Basic Scala syntax, functions and methods

Scala's collection library provides rich data structures optimized for functional programming. Immutable collections are the default, and a consistent API allows working with various data structures. Unlike Java collections, Scala collections have built-in support for higher-order functions like map, filter, and fold.

#### Collection Hierarchy

Scala collections have a well-designed hierarchy. At the top is Iterable, with ordered Seq, duplicate-free Set, and key-value Map underneath.

```
                  Iterable
                     │
        ┌────────────┼────────────┐
        │            │            │
       Seq          Set          Map
        │            │            │
   ┌────┴────┐       │       ┌────┴────┐
   │         │       │       │         │
IndexedSeq LinearSeq SortedSet HashMap SortedMap
   │         │       │
Vector    List    TreeSet
Array   LazyList
```

#### Immutable vs Mutable

Scala uses **immutable collections** by default. Immutable collections are thread-safe and guarantee predictable behavior. Mutable collections must be explicitly imported when needed.

```scala
// Immutable (default)
import scala.collection.immutable._  // Implicitly imported
val list = List(1, 2, 3)
val set = Set(1, 2, 3)
val map = Map("a" -> 1, "b" -> 2)

// Mutable (explicit import required)
import scala.collection.mutable
val mutableList = mutable.ListBuffer(1, 2, 3)
val mutableSet = mutable.Set(1, 2, 3)
val mutableMap = mutable.Map("a" -> 1)
```

#### Seq (Sequence)

Seq is an ordered collection. Elements have a defined order and can be accessed by index. List and Vector are most commonly used.

**List (Linked List)**

List is an immutable linked list. Prepending/removing from the front is O(1) fast, but index access is O(n).

```scala
val list = List(1, 2, 3, 4, 5)

// Element access
list.head          // 1
list.tail          // List(2, 3, 4, 5)
list(2)            // 3 (index access - O(n))

// Add element (front - O(1))
val newList = 0 :: list    // List(0, 1, 2, 3, 4, 5)
val concat = list :+ 6     // List(1, 2, 3, 4, 5, 6) - O(n)

// List concatenation
val combined = List(1, 2) ::: List(3, 4)  // List(1, 2, 3, 4)
// Or
val combined2 = List(1, 2) ++ List(3, 4)

// Pattern matching
list match {
  case Nil          => "empty list"
  case head :: tail => s"first: $head, rest: $tail"
}
```

**Vector (Indexed Sequence)**

Vector is an immutable sequence with fast random access. Implemented as a 32-way tree structure, most operations are effectively constant time.

```scala
val vector = Vector(1, 2, 3, 4, 5)

// Random access - O(~1) (effectively constant time)
vector(2)  // 3

// Update (returns new Vector)
val updated = vector.updated(2, 100)  // Vector(1, 2, 100, 4, 5)

// Add
val appended = vector :+ 6
val prepended = 0 +: vector
```

**Range**

Range is a special sequence representing a numeric range. It computes elements on demand without storing all in memory.

```scala
val r1 = 1 to 10      // 1 to 10 (inclusive)
val r2 = 1 until 10   // 1 to 9 (10 excluded)
val r3 = 1 to 10 by 2 // 1, 3, 5, 7, 9

r1.toList  // List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
```

#### Set

Set is a collection without duplicates. It can quickly check element membership and supports mathematical set operations (union, intersection, difference).

```scala
val set = Set(1, 2, 3, 2, 1)  // Set(1, 2, 3)

// Membership
set.contains(2)  // true
set(2)           // true (apply = contains)

// Add/Remove
val added = set + 4       // Set(1, 2, 3, 4)
val removed = set - 2     // Set(1, 3)

// Set operations
val a = Set(1, 2, 3)
val b = Set(2, 3, 4)

a union b      // Set(1, 2, 3, 4)
a | b          // Same as above

a intersect b  // Set(2, 3)
a & b          // Same as above

a diff b       // Set(1)
a -- b         // Same as above
```

**SortedSet**

SortedSet is a set that keeps elements always sorted. Useful when min/max values or range queries are needed.

```scala
import scala.collection.immutable.SortedSet

val sorted = SortedSet(3, 1, 4, 1, 5, 9, 2, 6)
// SortedSet(1, 2, 3, 4, 5, 6, 9)

sorted.firstKey  // 1
sorted.lastKey   // 9
sorted.range(2, 6)  // SortedSet(2, 3, 4, 5)
```

#### Map

Map is a collection of key-value pairs. It can quickly search values by key and is widely used for configurations, caches, and indexes.

```scala
val map = Map("a" -> 1, "b" -> 2, "c" -> 3)

// Value access
map("a")              // 1
map.get("a")          // Some(1)
map.get("z")          // None
map.getOrElse("z", 0) // 0

// Add/Update
val added = map + ("d" -> 4)
val updated = map + ("a" -> 10)

// Remove
val removed = map - "a"

// Iteration
for ((key, value) <- map) {
  println(s"$key = $value")
}

// keys, values
map.keys    // Iterable("a", "b", "c")
map.values  // Iterable(1, 2, 3)
```

#### Collection Operations

The core of Scala collections is rich higher-order functions. Combining these operations allows expressing complex data processing declaratively.

**Transform Operations**

Transform collections with map, flatMap, filter, etc. The original doesn't change; a new collection is returned.

```scala
val numbers = List(1, 2, 3, 4, 5)

// map: Transform each element
numbers.map(_ * 2)           // List(2, 4, 6, 8, 10)
numbers.map(n => n * n)      // List(1, 4, 9, 16, 25)

// flatMap: Transform and flatten
numbers.flatMap(n => List(n, n * 10))
// List(1, 10, 2, 20, 3, 30, 4, 40, 5, 50)

// filter: Only matching elements
numbers.filter(_ % 2 == 0)   // List(2, 4)

// filterNot: Only non-matching elements
numbers.filterNot(_ % 2 == 0) // List(1, 3, 5)

// collect: Filter + transform with pattern matching
numbers.collect {
  case n if n % 2 == 0 => n * 10
}  // List(20, 40)
```

**Reduce Operations**

Reduce and fold reduce all elements in a collection to a single value.

```scala
val numbers = List(1, 2, 3, 4, 5)

// reduce: Reduce elements to one
numbers.reduce(_ + _)      // 15
numbers.reduce(_ * _)      // 120

// fold: Reduce with initial value
numbers.foldLeft(0)(_ + _)   // 15
numbers.foldLeft(10)(_ + _)  // 25

// foldLeft vs foldRight
List("a", "b", "c").foldLeft("")(_ + _)   // "abc"
List("a", "b", "c").foldRight("")(_ + _)  // "abc" (same order, different calculation direction)
```

**Partition Operations**

Split collections into multiple parts with partition, groupBy, span, splitAt.

```scala
val numbers = List(1, 2, 3, 4, 5, 6)

// partition: Split into two by condition
val (evens, odds) = numbers.partition(_ % 2 == 0)
// evens = List(2, 4, 6), odds = List(1, 3, 5)

// groupBy: Group by key
val grouped = numbers.groupBy(_ % 3)
// Map(0 -> List(3, 6), 1 -> List(1, 4), 2 -> List(2, 5))

// span: Split while condition is true
val (before, after) = numbers.span(_ < 4)
// before = List(1, 2, 3), after = List(4, 5, 6)

// splitAt: Split by index
val (first, second) = numbers.splitAt(3)
// first = List(1, 2, 3), second = List(4, 5, 6)
```

**Search Operations**

Search for elements or check conditions in collections with find, exists, forall, contains, count.

```scala
val numbers = List(1, 2, 3, 4, 5)

// find: First matching element
numbers.find(_ > 3)    // Some(4)
numbers.find(_ > 10)   // None

// exists: Any element matches?
numbers.exists(_ > 3)  // true

// forall: All elements match?
numbers.forall(_ > 0)  // true

// contains: Contains value?
numbers.contains(3)    // true

// count: Count matching elements
numbers.count(_ % 2 == 0)  // 2
```

**Sort Operations**

Sort collections with sorted, sortBy, sortWith.

```scala
val numbers = List(3, 1, 4, 1, 5, 9, 2, 6)

numbers.sorted           // List(1, 1, 2, 3, 4, 5, 6, 9)
numbers.sortWith(_ > _)  // List(9, 6, 5, 4, 3, 2, 1, 1)

case class Person(name: String, age: Int)
val people = List(Person("Alice", 30), Person("Bob", 25), Person("Carol", 35))

people.sortBy(_.age)        // By age
people.sortBy(_.name)       // By name
people.sortWith(_.age > _.age)  // Age descending
```

#### Working with Option

Option is a collection representing a value that may or may not exist. Using Option instead of null prevents NullPointerException and handles missing values type-safely.

```scala
val maybeValue: Option[Int] = Some(42)
val noValue: Option[Int] = None

// map
maybeValue.map(_ * 2)  // Some(84)
noValue.map(_ * 2)     // None

// flatMap
def safeDivide(a: Int, b: Int): Option[Int] =
  if (b == 0) None else Some(a / b)

maybeValue.flatMap(v => safeDivide(v, 2))  // Some(21)

// getOrElse
maybeValue.getOrElse(0)  // 42
noValue.getOrElse(0)     // 0

// fold
maybeValue.fold(0)(_ * 2)  // 84
noValue.fold(0)(_ * 2)     // 0

// for comprehension
for {
  a <- Some(10)
  b <- Some(20)
} yield a + b  // Some(30)
```

#### Performance Characteristics

Each collection has different performance characteristics, so choose appropriately. The table below summarizes time complexity for major operations.

| Collection | head | tail | Index Access | Update | Prepend | Append |
|------------|------|------|--------------|--------|---------|--------|
| List | O(1) | O(1) | O(n) | O(n) | O(1) | O(n) |
| Vector | O(1) | O(1) | O(log₃₂n)* | O(log₃₂n)* | O(log₃₂n)* | O(log₃₂n)* |
| Array | O(1) | O(n) | O(1) | O(1) | - | - |
| Set | - | - | O(log n)** | O(log n)** | O(log n)** | - |
| Map | - | - | O(log n)** | O(log n)** | O(log n)** | - |

> **Note:**
> - `*` Vector's O(log₃₂n) is about 6 steps even for n=1 billion, **effectively constant time**.
> - `**` HashSet/HashMap are average O(1), TreeSet/TreeMap are O(log n)

**Which Collection to Choose?**

Choose the appropriate collection based on use case.

```scala
// Frequent add/remove from front → List
val queue = 1 :: 2 :: 3 :: Nil

// Frequent random access → Vector
val indexed = Vector(1, 2, 3, 4, 5)
indexed(3)  // Fast!

// No duplicates, fast lookup → Set
val unique = Set(1, 2, 3)
unique.contains(2)  // Fast!

// Key-value lookup → Map
val lookup = Map("a" -> 1, "b" -> 2)
lookup.get("a")  // Fast!
```

#### Common Mistakes and Anti-patterns

Here are common mistakes when using collections and proper solutions.

**❌ What to Avoid**

```scala
// 1. Index-based access abuse (on List)
val list = List(1, 2, 3, 4, 5)
for (i <- 0 until list.length) {
  println(list(i))  // O(n^2) - Very slow!
}

// 2. Mutable collection abuse
val mutableList = mutable.ListBuffer[Int]()
for (i <- 1 to 1000) {
  mutableList += i  // Unnecessary mutability
}

// 3. map then flatten instead of flatMap
list.map(x => List(x, x * 2)).flatten  // Inefficient

// 4. head/last on empty collection
List.empty[Int].head  // NoSuchElementException!
```

**✅ Correct Approach**

```scala
// 1. Use foreach or iterator
list.foreach(println)  // O(n)
// Or use Vector for frequent index access
val vector = Vector(1, 2, 3, 4, 5)
vector(2)  // O(~1)

// 2. Immutable collections with functional transforms
val result = (1 to 1000).toList

// 3. Use flatMap
list.flatMap(x => List(x, x * 2))

// 4. Use headOption/lastOption
List.empty[Int].headOption  // None
List(1, 2, 3).headOption    // Some(1)
```

**Collection Selection Guide**

The diagram below shows how to select the appropriate collection based on requirements.

```mermaid
flowchart TD
    Start["Choose Data Structure"] --> Q1{"Order matters?"}
    Q1 -->|Yes| Q2{"Need random access?"}
    Q1 -->|No| Q3{"Allow duplicates?"}

    Q2 -->|Yes| Vector["Vector"]
    Q2 -->|No| Q4{"Add/remove from front?"}

    Q4 -->|Yes| List["List"]
    Q4 -->|No| Vector

    Q3 -->|Yes| List
    Q3 -->|No| Set["Set"]
```

*Diagram: Data structure selection flowchart. If order matters, choose Vector/List based on random access needs. If order doesn't matter, choose List/Set based on duplicate allowance.*

#### Practice Problems

Review collection usage with these practice problems.

**1. Word Frequency**

Calculate the frequency of each word in a list of strings.

<details>
<summary>Show Answer</summary>

```scala
val words = List("apple", "banana", "apple", "cherry", "banana", "apple")

val frequency = words.groupBy(identity).map { case (word, list) =>
  word -> list.length
}
// Map(apple -> 3, banana -> 2, cherry -> 1)

// Or
val frequency2 = words.groupMapReduce(identity)(_ => 1)(_ + _)
```

</details>

**2. Flatten Nested List**

Flatten a nested list to one dimension.

<details>
<summary>Show Answer</summary>

```scala
val nested = List(List(1, 2), List(3, 4, 5), List(6))

val flat = nested.flatten  // List(1, 2, 3, 4, 5, 6)

// Or
val flat2 = nested.flatMap(identity)
```

</details>

#### Next Steps

- [Higher-Order Functions](higher-order-functions/) — Advanced map, filter, fold
- [For Comprehension](for-comprehensions/) — Monadic operations
