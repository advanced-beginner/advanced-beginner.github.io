---
lastmod: "2026-01-06"
title: Advanced Type System
weight: 14
---

Scala 3 provides an even more powerful and expressive type system. This document covers Scala 3's new type features.

> **Prerequisites**: To understand this document, you should be familiar with:
> - [Generics](../generics/) - Type parameters, type bounds
> - [Variance](../variance/) - Variance concepts
> - [Pattern Matching](../pattern-matching/) - Basic pattern matching
>
> **Difficulty**: Advanced - Includes Scala 3-only features

## Union Types (|)

Represents one of several types.

```scala
// Scala 3 only
def process(input: Int | String): String = input match
  case i: Int    => s"Number: $i"
  case s: String => s"String: $s"

process(42)        // "Number: 42"
process("hello")   // "String: hello"

// Multiple types
type JsonValue = String | Int | Boolean | Null
def toJson(value: JsonValue): String = value match
  case s: String  => s"\"$s\""
  case i: Int     => i.toString
  case b: Boolean => b.toString
  case null       => "null"
```

### Comparison with Either

```scala
// Either: Requires explicit Left/Right wrapper
def divideEither(a: Int, b: Int): Either[String, Int] =
  if b == 0 then Left("Cannot divide by zero") else Right(a / b)

// Union: Return value directly without wrapper
def divideUnion(a: Int, b: Int): Int | String =
  if b == 0 then "Cannot divide by zero" else a / b
```

## Intersection Types (&)

A type that satisfies multiple types.

```scala
trait Printable:
  def print(): String

trait Serializable:
  def serialize(): Array[Byte]

// A type that is both Printable and Serializable
def process(obj: Printable & Serializable): Unit =
  println(obj.print())
  val bytes = obj.serialize()

// Implementation
class Document(content: String) extends Printable, Serializable:
  def print(): String = content
  def serialize(): Array[Byte] = content.getBytes

process(Document("Hello"))
```

### Structural Type Combination

```scala
type Named = { def name: String }
type Aged = { def age: Int }

def describe(obj: Named & Aged): String =
  s"${obj.name}, ${obj.age} years old"
```

## Opaque Types

Type aliases that are treated as different types externally.

```scala
object UserId:
  opaque type UserId = Long

  def apply(id: Long): UserId = id

  extension (id: UserId)
    def value: Long = id
    def isValid: Boolean = id > 0

import UserId.*

val id: UserId = UserId(42)
id.value       // 42
id.isValid     // true

// val x: Long = id  // Compile error! UserId != Long
```

### Benefits

- No runtime overhead (no boxing)
- Type safety
- Useful for domain modeling

```scala
object Money:
  opaque type USD = BigDecimal
  opaque type EUR = BigDecimal

  def usd(amount: BigDecimal): USD = amount
  def eur(amount: BigDecimal): EUR = amount

  extension (x: USD)
    def +(y: USD): USD = x + y
    def value: BigDecimal = x

import Money.*

val dollars = usd(100)
val more = usd(50)
val total = dollars + more  // OK: USD + USD

// val mixed = dollars + eur(50)  // Compile error! USD + EUR
```

## Match Types

Perform pattern matching at the type level.

```scala
type Elem[X] = X match
  case String      => Char
  case Array[t]    => t
  case Iterable[t] => t

val a: Elem[String] = 'a'        // Char
val b: Elem[Array[Int]] = 1      // Int
val c: Elem[List[String]] = "hi" // String
```

### Recursive Match Types

```scala
type Flatten[X] = X match
  case List[List[t]] => Flatten[List[t]]
  case List[t]       => List[t]

// List[List[List[Int]]] -> List[Int]
val x: Flatten[List[List[List[Int]]]] = List(1, 2, 3)
```

## Type Lambdas

Pass type constructors as type parameters.

```scala
// Type lambda: [X] =>> F[X, Y]
type IntMap = [V] =>> Map[Int, V]

val map: IntMap[String] = Map(1 -> "one", 2 -> "two")
// Actually Map[Int, String]

// Either with fixed error type
type Result = [A] =>> Either[String, A]

val ok: Result[Int] = Right(42)
val err: Result[Int] = Left("Error")
```

## Dependent Function Types

Return type depends on parameter value.

```scala
trait Key:
  type Value

val intKey = new Key { type Value = Int }
val strKey = new Key { type Value = String }

// Return type depends on key's Value type
def get(key: Key): key.Value = ???

// Dependent function type
val getter: (key: Key) => key.Value = (key: Key) => ???
```

## Polymorphic Function Types

Function types with type parameters.

```scala
// Polymorphic function type
val identity: [A] => A => A = [A] => (a: A) => a

identity[Int](42)       // 42
identity[String]("hi")  // "hi"

// First element of list
val head: [A] => List[A] => A = [A] => (list: List[A]) => list.head

head(List(1, 2, 3))  // 1
```

## Comparison with Scala 2

| Feature | Scala 2 | Scala 3 |
|---------|---------|---------|
| Union Types | Use Either | `A \| B` |
| Intersection | `A with B` | `A & B` |
| Opaque Types | Value Class | `opaque type` |
| Match Types | Not possible | Supported |
| Type Lambdas | Complex syntax | `[X] =>> F[X]` |

## Exercises

### 1. Implement Opaque Type

Implement an `Email` opaque type. Include validation.

<details>
<summary>Show Answer</summary>

```scala
object Email:
  opaque type Email = String

  def apply(value: String): Option[Email] =
    if value.contains("@") && value.contains(".") then Some(value)
    else None

  def unsafe(value: String): Email = value

  extension (email: Email)
    def value: String = email
    def domain: String = email.split("@").last

import Email.*

val valid = Email("user@example.com")   // Some(Email)
val invalid = Email("invalid")          // None

valid.foreach(e => println(e.domain))   // "example.com"
```

</details>

## Next Steps

- [Macros](../macros-metaprogramming/) — Compile-time code generation
- [Functional Patterns](../functional-patterns/) — Functor, Monad
