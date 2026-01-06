---
lastmod: "2026-01-06"
title: Case Classes
weight: 5
---

Case classes are special classes for **immutable data modeling**. They let you define data classes without boilerplate code.

## Basic Syntax

```scala
case class Person(name: String, age: Int)

// Can create without new keyword
val alice = Person("Alice", 30)
val bob = Person("Bob", 25)
```

## Auto-Generated Features

When you declare a case class, the compiler automatically generates:

### 1. apply Method (Factory)

```scala
// Create without new
val person = Person("Alice", 30)

// Actually works like this
val person = Person.apply("Alice", 30)
```

### 2. unapply Method (Extractor)

Used in pattern matching.

```scala
val Person(name, age) = Person("Alice", 30)
println(name)  // Alice
println(age)   // 30
```

### 3. Field Accessors

All constructor parameters are declared as `val`.

```scala
val person = Person("Alice", 30)
println(person.name)  // Alice
println(person.age)   // 30

// Immutable, cannot modify
// person.age = 31  // Compile error!
```

### 4. copy Method

Creates a new instance with some fields changed.

```scala
val alice = Person("Alice", 30)

// New instance with only age changed
val olderAlice = alice.copy(age = 31)
println(olderAlice)  // Person(Alice,31)

// Change only name
val bob = alice.copy(name = "Bob")
println(bob)  // Person(Bob,30)

// Change multiple fields
val carol = alice.copy(name = "Carol", age = 25)
println(carol)  // Person(Carol,25)
```

### 5. equals and hashCode

Provides structural equality.

```scala
val person1 = Person("Alice", 30)
val person2 = Person("Alice", 30)
val person3 = Person("Bob", 25)

println(person1 == person2)  // true (same content)
println(person1 == person3)  // false

// Works correctly in HashSet/HashMap
val set = Set(person1, person2)
println(set.size)  // 1 (duplicates removed)
```

### 6. toString

Provides readable string representation.

```scala
val person = Person("Alice", 30)
println(person.toString)  // Person(Alice,30)
println(person)           // Person(Alice,30)
```

## Nested Case Classes

```scala
case class Address(city: String, zipCode: String)
case class Employee(name: String, address: Address)

val emp = Employee("John", Address("Seoul", "12345"))

// Nested copy
val empInBusan = emp.copy(address = emp.address.copy(city = "Busan"))
println(empInBusan)  // Employee(John,Address(Busan,12345))
```

## With Pattern Matching

Case classes shine when used with pattern matching.

```scala
case class Order(id: Int, product: String, quantity: Int)

def processOrder(order: Order): String = order match {
  case Order(_, _, q) if q <= 0   => "Invalid quantity"
  case Order(_, _, q) if q > 100  => "Bulk order"
  case Order(id, product, q)      => s"Order #$id: $product x$q"
}

println(processOrder(Order(1, "Laptop", 5)))    // Order #1: Laptop x5
println(processOrder(Order(2, "Mouse", 150)))   // Bulk order
println(processOrder(Order(3, "Keyboard", -1))) // Invalid quantity
```

## ADT (Algebraic Data Types)

Combine case classes with `sealed trait` to define ADTs.

### Scala 3

```scala
enum Shape:
  case Circle(radius: Double)
  case Rectangle(width: Double, height: Double)
  case Triangle(base: Double, height: Double)

import Shape.*

def area(shape: Shape): Double = shape match
  case Circle(r)         => math.Pi * r * r
  case Rectangle(w, h)   => w * h
  case Triangle(b, h)    => 0.5 * b * h

println(area(Circle(5)))         // 78.539...
println(area(Rectangle(3, 4)))   // 12.0
```

### Scala 2

```scala
sealed trait Shape
case class Circle(radius: Double) extends Shape
case class Rectangle(width: Double, height: Double) extends Shape
case class Triangle(base: Double, height: Double) extends Shape

def area(shape: Shape): Double = shape match {
  case Circle(r)         => math.Pi * r * r
  case Rectangle(w, h)   => w * h
  case Triangle(b, h)    => 0.5 * b * h
}
```

### Importance of sealed

`sealed` restricts inheritance to the same file. This enables:

1. **Exhaustive pattern matching**: Compiler knows all cases
2. **Warnings**: Warns about missing cases

```scala
// Warning if cases are missing
def describe(shape: Shape): String = shape match {
  case Circle(r) => s"Circle with radius $r"
  // Rectangle, Triangle missing - compile warning!
}
```

## Option, Either, Try

Representative case class usages from Scala's standard library.

> The examples below show **conceptual structure**. Actual standard library implementations are more complex with various optimizations.

### Option

```scala
// Conceptual structure (differs from actual implementation)
sealed trait Option[+A]
case class Some[+A](value: A) extends Option[A]
case object None extends Option[Nothing]

// Usage
def divide(a: Int, b: Int): Option[Int] =
  if (b == 0) None else Some(a / b)

divide(10, 2) match {
  case Some(result) => println(s"Result: $result")
  case None         => println("Cannot divide by zero")
}
```

### Either

```scala
sealed trait Either[+L, +R]
case class Left[+L](value: L) extends Either[L, Nothing]
case class Right[+R](value: R) extends Either[Nothing, R]

// Usage
def parseAge(input: String): Either[String, Int] =
  input.toIntOption match {
    case Some(age) if age >= 0 => Right(age)
    case Some(_)               => Left("Age cannot be negative")
    case None                  => Left("Not a number")
  }
```

## Case Class vs Regular Class

| Feature | Case Class | Regular Class |
|---------|-----------|---------------|
| Immutability | Immutable by default (val) | Optional |
| equals/hashCode | Auto structural comparison | Reference comparison (default) |
| copy method | Auto-generated | Must implement |
| Pattern matching | unapply auto-generated | Must implement |
| new keyword | Not needed | Needed |

## Best Practices

### 1. Use for Immutable Data

```scala
// Good: Immutable data
case class Config(host: String, port: Int)

// Avoid: When mutable state is needed
// case class Counter(var count: Int)  // Anti-pattern
```

### 2. Good for Small Domain Models

```scala
case class Money(amount: BigDecimal, currency: String)
case class OrderLine(product: String, quantity: Int, unitPrice: Money)
case class Order(id: String, lines: List[OrderLine])
```

### 3. DTO (Data Transfer Object)

```scala
case class CreateUserRequest(name: String, email: String)
case class UserResponse(id: Long, name: String, email: String)
```

### 4. Composition Over Inheritance

```scala
// Avoid: Case class inheritance
// case class SpecialPerson(name: String, age: Int, badge: String)
//     extends Person(name, age)  // Can cause issues

// Good: Use composition
case class Person(name: String, age: Int)
case class Badge(id: String, level: String)
case class Employee(person: Person, badge: Badge)
```

## Exercises

### 1. Result Type Implementation

Implement a `Result[T]` type: `Success(value)` or `Failure(message)`

<details>
<summary>Show Answer</summary>

```scala
sealed trait Result[+T]
case class Success[+T](value: T) extends Result[T]
case class Failure(message: String) extends Result[Nothing]

def divide(a: Int, b: Int): Result[Int] =
  if (b == 0) Failure("Cannot divide by zero")
  else Success(a / b)

divide(10, 2) match {
  case Success(v) => println(s"Result: $v")
  case Failure(m) => println(s"Error: $m")
}
```

</details>

### 2. Expression Tree

Define an ADT for mathematical expressions and write an evaluation function.

<details>
<summary>Show Answer</summary>

```scala
sealed trait Expr
case class Num(value: Double) extends Expr
case class Add(left: Expr, right: Expr) extends Expr
case class Mul(left: Expr, right: Expr) extends Expr

def eval(expr: Expr): Double = expr match {
  case Num(v)      => v
  case Add(l, r)   => eval(l) + eval(r)
  case Mul(l, r)   => eval(l) * eval(r)
}

// (1 + 2) * 3
val expr = Mul(Add(Num(1), Num(2)), Num(3))
println(eval(expr))  // 9.0
```

</details>

## Next Steps

- [Pattern Matching](../pattern-matching/) — Advanced match expressions
- [Collections](../collections/) — Scala collection library
