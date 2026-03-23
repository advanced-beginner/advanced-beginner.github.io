---
lastmod: "2026-01-14"
title: Case Classes
description: "Case class mechanics and immutable data modeling"
weight: 5
---

{{< callout type="info" title="TL;DR" >}}
- Case classes are special classes for **immutable data modeling**
- `apply`, `unapply`, `copy`, `equals`, `hashCode`, and `toString` are automatically generated
- Define ADTs (Algebraic Data Types) with `sealed trait` + case classes
- Show their true value when used with pattern matching
{{< /callout >}}

**Target Audience:** Developers who have learned Scala basic syntax
**Prerequisites:** Classes and objects, basic understanding of type system

Case classes are special classes for **immutable data modeling**. You can define data classes without boilerplate code. The compiler automatically generates useful methods like equals, hashCode, toString, and copy, so you can define data-centric classes very concisely. They show their true value especially when used with pattern matching.

#### Basic Syntax

Case classes are defined with the `case class` keyword. You can create instances without the new keyword, because the compiler automatically generates an apply factory method.

```scala
case class Person(name: String, age: Int)

// Can create without new keyword
val alice = Person("Alice", 30)
val bob = Person("Bob", 25)
```

#### Auto-Generated Features

When you declare a case class, the compiler automatically generates several useful methods. This eliminates the need to write boilerplate code when defining data classes.

**1. apply Method (Factory)**

The apply method is automatically generated, allowing instance creation without the new keyword.

```scala
// Can create without new
val person = Person("Alice", 30)

// Actually works like this
val person = Person.apply("Alice", 30)
```

**2. unapply Method (Extractor)**

The unapply method is automatically generated, allowing field extraction in pattern matching.

```scala
val Person(name, age) = Person("Alice", 30)
println(name)  // Alice
println(age)   // 30
```

**3. Field Accessors**

All constructor parameters are automatically declared as `val` and accessible from outside. Case classes are immutable by default.

```scala
val person = Person("Alice", 30)
println(person.name)  // Alice
println(person.age)   // 30

// Immutable, cannot modify
// person.age = 31  // Compile error!
```

**4. copy Method**

Creates a new instance with some fields of an immutable object changed. The original object doesn't change.

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

**5. equals and hashCode**

Provides structural equality. Unlike regular classes, compares field values rather than references.

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

**6. toString**

Provides readable string representation. Useful for debugging and logging.

```scala
val person = Person("Alice", 30)
println(person.toString)  // Person(Alice,30)
println(person)           // Person(Alice,30)
```

{{< callout type="info" title="Key Points" >}}
- `apply`: Create instances without new
- `unapply`: Extract fields in pattern matching
- `copy`: Create new instance with some fields changed
- `equals/hashCode`: Structural equality (compare field values)
- `toString`: Readable string representation
{{< /callout >}}

#### Nested Case Classes

Case classes can have other case classes as fields. Using nested copy, you can easily update even deeply structured immutable objects.

```scala
case class Address(city: String, zipCode: String)
case class Employee(name: String, address: Address)

val emp = Employee("John", Address("Seoul", "12345"))

// Nested copy
val empInBusan = emp.copy(address = emp.address.copy(city = "Busan"))
println(empInBusan)  // Employee(John,Address(Busan,12345))
```

{{< callout type="info" title="Key Points" >}}
- Update nested case classes by chaining `copy`
- Can easily modify even deeply structured immutable objects
{{< /callout >}}

#### With Pattern Matching

Case classes show their true value when used with pattern matching. Thanks to the automatically generated unapply method, you can easily extract fields and apply guard conditions.

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

{{< callout type="info" title="Key Points" >}}
- Easily extract fields with case class `unapply`
- Additional filtering possible with guard conditions (`if`)
- Ignore unnecessary fields with wildcard `_`
{{< /callout >}}

#### ADT (Algebraic Data Types)

You can define ADTs (Algebraic Data Types) by combining case classes with `sealed trait`. ADTs are a powerful way to express domain models in functional programming. Declaring as sealed requires all subtypes to be defined in the same file, so the compiler can check pattern matching completeness.

**Scala 3**

In Scala 3, you can define ADTs more concisely with the enum keyword.

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

**Scala 2**

In Scala 2, implement ADTs with sealed trait and case class combinations.

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

**Importance of sealed**

`sealed` restricts inheritance to the same file only. This provides the following benefits:

1. **Exhaustive pattern matching**: Compiler knows all cases
2. **Provides warnings**: Warns about missing cases

```scala
// Warning if cases are missing
def describe(shape: Shape): String = shape match {
  case Circle(r) => s"Circle with radius $r"
  // Rectangle, Triangle missing - compile warning!
}
```

{{< callout type="info" title="Key Points" >}}
- Define ADTs with `sealed trait` + case classes
- `sealed` allows inheritance only in the same file
- Compiler checks pattern matching completeness and warns about missing cases
- In Scala 3, can define more concisely with `enum`
{{< /callout >}}

#### Option, Either, Try

Representative case class usage examples from Scala's standard library. These are all ADTs defined with sealed trait and case classes, expressing operations that can fail in a type-safe way instead of null or exceptions.

> 💡 The examples below show **conceptual structure**. Actual standard library implementations are more complex with various optimizations applied.

**Option**

Option expresses cases where a value may or may not be present. Using Option instead of null can prevent NullPointerException.

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

**Either**

Either holds one of two possible types of values. By convention, Left holds failure (error messages, etc.), and Right holds success values.

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

{{< callout type="info" title="Key Points" >}}
- **Option**: Expresses value present (`Some`) or absent (`None`)
- **Either**: One of two possible results (`Left`=failure, `Right`=success)
- Expresses failure in type-safe way instead of null or exceptions
{{< /callout >}}

#### Case Class vs Regular Class

The table below summarizes the main differences between case classes and regular classes. Case classes are optimized for immutable data modeling, and regular classes are suitable when mutable state or complex behavior is needed.

| Feature | Case Class | Regular Class |
|---------|-----------|---------------|
| Immutability | Immutable by default (val) | Optional |
| equals/hashCode | Auto structural comparison | Reference comparison (default) |
| copy method | Auto-generated | Must implement manually |
| Pattern matching | unapply auto-generated | Must implement manually |
| new keyword | Not needed | Needed |

{{< callout type="info" title="Key Points" >}}
- Case classes are optimized for immutable data
- Automatically support structural comparison, copy, and pattern matching
- Use regular classes when mutable state is needed
{{< /callout >}}

#### Best Practices

Recommendations for effectively using case classes.

**1. Use for Immutable Data**

Case classes are designed for immutable data. Using var can break the immutability assumption of equals/hashCode.

```scala
// Good: Immutable data
case class Config(host: String, port: Int)

// Avoid: When mutable state is needed
// case class Counter(var count: Int)  // Anti-pattern
```

**2. Good for Small Domain Models**

Case classes are ideal for defining concise domain models.

```scala
case class Money(amount: BigDecimal, currency: String)
case class OrderLine(product: String, quantity: Int, unitPrice: Money)
case class Order(id: String, lines: List[OrderLine])
```

**3. DTO (Data Transfer Object)**

Case classes are also suitable for API request/response objects. JSON serialization libraries also support case classes well.

```scala
case class CreateUserRequest(name: String, email: String)
case class UserResponse(id: Long, name: String, email: String)
```

**4. Composition Over Inheritance**

Case class inheritance is not recommended as it can cause issues with equals/hashCode implementation. Use composition instead.

```scala
// Avoid: Case class inheritance
// case class SpecialPerson(name: String, age: Int, badge: String)
//     extends Person(name, age)  // Can cause issues

// Good: Use composition
case class Person(name: String, age: Int)
case class Badge(id: String, level: String)
case class Employee(person: Person, badge: Badge)
```

{{< callout type="info" title="Key Points" >}}
- Use case classes for immutable data, domain models, DTOs
- Avoid using `var` and maintain immutability
- Use composition instead of case class inheritance
{{< /callout >}}

#### Exercises

Practice case class and ADT concepts with these exercises.

**1. Result Type Implementation**

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

**2. Expression Tree**

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

#### Next Steps

- [Pattern Matching](pattern-matching/) — Advanced match expressions
- [Collections](collections/) — Scala collection library
