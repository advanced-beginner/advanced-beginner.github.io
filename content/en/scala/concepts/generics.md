---
lastmod: "2026-01-06"
title: Generics
weight: 9
---

Generics allow you to write type-safe and reusable code.

## Type Parameters

### In Classes

```scala
// Single type parameter
class Box[A](value: A) {
  def get: A = value
  def map[B](f: A => B): Box[B] = new Box(f(value))
}

val intBox = new Box(42)
val strBox = new Box("hello")

intBox.get        // 42
strBox.get        // "hello"
intBox.map(_ * 2) // Box(84)

// Multiple type parameters
class Pair[A, B](val first: A, val second: B) {
  def swap: Pair[B, A] = new Pair(second, first)
}

val pair = new Pair(1, "one")
pair.first   // 1
pair.second  // "one"
pair.swap    // Pair("one", 1)
```

### In Methods

```scala
def identity[A](x: A): A = x

identity(42)      // 42
identity("hello") // "hello"

def swap[A, B](pair: (A, B)): (B, A) = (pair._2, pair._1)

swap((1, "one"))  // ("one", 1)
```

### In Traits

```scala
trait Container[A] {
  def get: A
  def map[B](f: A => B): Container[B]
}

class Box[A](value: A) extends Container[A] {
  def get: A = value
  def map[B](f: A => B): Container[B] = new Box(f(value))
}
```

## Type Bounds

### Type Bounds Visualization

```mermaid
graph TB
    subgraph "Upper Bound"
        direction TB
        Animal["Animal"]
        Dog["Dog"]
        Cat["Cat"]
        Dog -->|"<:"| Animal
        Cat -->|"<:"| Animal
        UB["A ≤ Animal<br>A is subtype of Animal"]
    end

    subgraph "Lower Bound"
        direction TB
        Fruit["Fruit"]
        Apple["Apple"]
        RedApple["RedApple"]
        Apple -->|"<:"| Fruit
        RedApple -->|"<:"| Apple
        LB["B ≥ Apple<br>B is supertype of Apple"]
    end
```

### Upper Bound

`A <: B` means A must be a subtype of B.

```scala
trait Animal {
  def name: String
}

class Dog(val name: String) extends Animal
class Cat(val name: String) extends Animal

// A must be a subtype of Animal
def printNames[A <: Animal](animals: List[A]): Unit =
  animals.foreach(a => println(a.name))

printNames(List(Dog("Buddy"), Dog("Max")))
// printNames(List("not an animal"))  // Compile error
```

### Lower Bound

`A >: B` means A must be a supertype of B.

```scala
class Fruit
class Apple extends Fruit
class RedApple extends Apple

// B must be a supertype of Apple
def addFruit[B >: Apple](fruits: List[B], fruit: B): List[B] =
  fruit :: fruits

val fruits: List[Fruit] = List(new Apple)
addFruit(fruits, new Fruit)     // OK - Fruit >: Apple
addFruit(fruits, new Apple)     // OK - Apple >: Apple (same type)
addFruit(fruits, new RedApple)  // OK - RedApple upcasts to Apple
```

> **Key insight:** `B >: Apple` means "B is Apple or a supertype of Apple". Subtypes (RedApple) can also be used as they upcast to Apple.

### Context Bound

`A : Ordering` means an implicit instance of `Ordering[A]` is required.

```scala
// Context bound
def max[A: Ordering](a: A, b: A): A = {
  val ord = implicitly[Ordering[A]]
  if (ord.gt(a, b)) a else b
}

max(1, 2)        // 2
max("a", "b")    // "b"

// Equivalent expression
def max2[A](a: A, b: A)(implicit ord: Ordering[A]): A =
  if (ord.gt(a, b)) a else b
```

## Type Inference

```scala
// Types are inferred
val list = List(1, 2, 3)           // List[Int]
val map = Map("a" -> 1, "b" -> 2)  // Map[String, Int]

// Cases requiring explicit types
val empty = List.empty[Int]        // List[Int]
val none: Option[Int] = None       // Option[Int]
```

## Common Generic Types

### Option[A]

```scala
val some: Option[Int] = Some(42)
val none: Option[Int] = None

some.map(_ * 2)        // Some(84)
none.map(_ * 2)        // None
some.getOrElse(0)      // 42
none.getOrElse(0)      // 0
```

### Either[A, B]

```scala
val right: Either[String, Int] = Right(42)
val left: Either[String, Int] = Left("error")

right.map(_ * 2)       // Right(84)
left.map(_ * 2)        // Left("error")

// Pattern matching
right match {
  case Right(value) => s"Value: $value"
  case Left(error)  => s"Error: $error"
}
```

### Try[A]

```scala
import scala.util.{Try, Success, Failure}

val success: Try[Int] = Try("42".toInt)
val failure: Try[Int] = Try("abc".toInt)

success.map(_ * 2)     // Success(84)
failure.map(_ * 2)     // Failure(NumberFormatException)

success.getOrElse(0)   // 42
failure.getOrElse(0)   // 0
```

## Generic ADT

```scala
// Generic result type
sealed trait Result[+E, +A]
case class Success[A](value: A) extends Result[Nothing, A]
case class Error[E](error: E) extends Result[E, Nothing]

def divide(a: Int, b: Int): Result[String, Int] =
  if (b == 0) Error("Cannot divide by zero")
  else Success(a / b)

divide(10, 2) match {
  case Success(v) => println(s"Result: $v")
  case Error(e)   => println(s"Error: $e")
}
```

## Comparison with Java Generics

| Feature | Scala | Java |
|---------|-------|------|
| Syntax | `[A]` | `<A>` |
| Upper bound | `A <: B` | `A extends B` |
| Lower bound | `A >: B` | `A super B` |
| Wildcard | `_` | `?` |
| Variance | Declaration-site | Use-site |

```scala
// Scala
class Box[A](val value: A)
def process[A <: Comparable[A]](a: A): Unit = ???

// Java equivalent
// class Box<A> { ... }
// void process<A extends Comparable<A>>(A a) { ... }
```

## Exercises

### 1. Generic Stack Implementation

Implement an immutable Stack with generics.

<details>
<summary>Show Answer</summary>

```scala
sealed trait Stack[+A] {
  def push[B >: A](elem: B): Stack[B]
  def pop: (A, Stack[A])
  def isEmpty: Boolean
}

case object EmptyStack extends Stack[Nothing] {
  def push[B](elem: B): Stack[B] = NonEmptyStack(elem, this)
  def pop: Nothing = throw new NoSuchElementException("Empty stack")
  def isEmpty: Boolean = true
}

case class NonEmptyStack[+A](top: A, rest: Stack[A]) extends Stack[A] {
  def push[B >: A](elem: B): Stack[B] = NonEmptyStack(elem, this)
  def pop: (A, Stack[A]) = (top, rest)
  def isEmpty: Boolean = false
}

val stack = EmptyStack.push(1).push(2).push(3)
val (top, rest) = stack.pop  // (3, Stack(2, 1))
```

</details>

### 2. Generic find Function

Implement a generic function to find the first element matching a condition.

<details>
<summary>Show Answer</summary>

```scala
def find[A](list: List[A])(predicate: A => Boolean): Option[A] =
  list match {
    case Nil                        => None
    case head :: _ if predicate(head) => Some(head)
    case _ :: tail                  => find(tail)(predicate)
  }

find(List(1, 2, 3, 4, 5))(_ > 3)  // Some(4)
find(List("a", "bb", "ccc"))(_.length > 2)  // Some("ccc")
```

</details>

## Next Steps

- [Variance](../variance/) — Generic type variance
- [Type Classes](../type-classes/) — Ad-hoc polymorphism
