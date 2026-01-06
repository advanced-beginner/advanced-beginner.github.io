---
lastmod: "2026-01-06"
title: Classes and Objects
weight: 4
---

Scala supports both object-oriented and functional programming. This document covers OOP features like classes, objects, and traits.

## Classes

### Basic Class Definition

```scala
// Basic class
class Person(name: String, age: Int) {
  def greet(): String = s"Hello, I'm $name."
}

val person = new Person("John", 30)
println(person.greet())  // Hello, I'm John.
```

### Constructor Parameters

Adding `val` or `var` to constructor parameters automatically creates fields.

```scala
// name is not accessible from outside, age is a val field
class Person(name: String, val age: Int)

val p = new Person("John", 30)
// println(p.name)  // Compile error
println(p.age)      // 30

// var makes it mutable
class MutablePerson(var name: String, var age: Int)

val mp = new MutablePerson("John", 30)
mp.age = 31
println(mp.age)  // 31
```

### Auxiliary Constructors

```scala
class Person(val name: String, val age: Int) {
  // Auxiliary constructor
  def this(name: String) = this(name, 0)
  def this() = this("Unknown", 0)
}

val p1 = new Person("John", 30)
val p2 = new Person("Jane")      // age = 0
val p3 = new Person()            // name = "Unknown", age = 0
```

### Using Default Values (Recommended)

```scala
// Default values are cleaner than auxiliary constructors
class Person(val name: String = "Unknown", val age: Int = 0)

val p1 = new Person("John", 30)
val p2 = new Person("Jane")
val p3 = new Person()
```

### Scala 3 Syntax

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
class Person(val name: String, val age: Int):
  def greet(): String = s"Hello, I'm $name."

  def isAdult: Boolean = age >= 18

  override def toString: String = s"Person($name, $age)"
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
class Person(val name: String, val age: Int) {
  def greet(): String = s"Hello, I'm $name."

  def isAdult: Boolean = age >= 18

  override def toString: String = s"Person($name, $age)"
}
```
{{% /tab %}}
{{< /tabs >}}

## Object

`object` defines a singleton instance.

### Singleton Object

```scala
object DatabaseConnection {
  private var connection: String = _

  def connect(url: String): Unit = {
    connection = url
    println(s"Connected to $url")
  }

  def getConnection: String = connection
}

// Use directly without new
DatabaseConnection.connect("jdbc:mysql://localhost/db")
println(DatabaseConnection.getConnection)
```

### Utility Methods

```scala
object MathUtils {
  def square(x: Int): Int = x * x
  def cube(x: Int): Int = x * x * x
  val PI: Double = 3.14159
}

println(MathUtils.square(5))  // 25
println(MathUtils.PI)         // 3.14159
```

## Companion Object

An object with the same name as a class is called a **companion object**.

```scala
class Circle(val radius: Double) {
  import Circle._  // Import companion object members

  def area: Double = PI * radius * radius
  def circumference: Double = 2 * PI * radius
}

object Circle {
  val PI: Double = 3.14159

  // Factory method
  def apply(radius: Double): Circle = new Circle(radius)

  def fromDiameter(diameter: Double): Circle = new Circle(diameter / 2)
}

// apply allows creation without new
val c1 = Circle(5)
val c2 = Circle.fromDiameter(10)

println(c1.area)  // 78.53975
```

### Private Member Access

Companion objects and classes can access each other's `private` members.

```scala
class Person private (val name: String, val age: Int)

object Person {
  def create(name: String, age: Int): Option[Person] =
    if (age >= 0) Some(new Person(name, age))  // Access private constructor
    else None
}

val person = Person.create("John", 30)  // Some(Person)
val invalid = Person.create("Error", -5)  // None
```

## Trait

Traits are similar to Java interfaces but can include implementations.

### Basic Trait

```scala
trait Greeter {
  def greet(name: String): String
}

class FormalGreeter extends Greeter {
  def greet(name: String): String = s"Good day, $name."
}

class CasualGreeter extends Greeter {
  def greet(name: String): String = s"Hey, $name!"
}
```

### Trait with Implementation

```scala
trait Logger {
  def log(message: String): Unit = println(s"[LOG] $message")

  def info(message: String): Unit = log(s"[INFO] $message")
  def error(message: String): Unit = log(s"[ERROR] $message")
}

class MyService extends Logger {
  def doSomething(): Unit = {
    info("Starting work")
    // Do work
    info("Work complete")
  }
}
```

### Multiple Traits (Mixin)

```scala
trait Swimmer {
  def swim(): String = "Swimming..."
}

trait Flyer {
  def fly(): String = "Flying..."
}

// Multiple trait mixin
class Duck extends Swimmer with Flyer {
  def quack(): String = "Quack!"
}

val duck = new Duck
println(duck.swim())   // Swimming...
println(duck.fly())    // Flying...
println(duck.quack())  // Quack!
```

### Trait Stacking

```scala
trait Base {
  def process(s: String): String = s
}

trait Uppercase extends Base {
  override def process(s: String): String = super.process(s.toUpperCase)
}

trait Trim extends Base {
  override def process(s: String): String = super.process(s.trim)
}

// Applied right to left
class TextProcessor extends Base with Trim with Uppercase

val processor = new TextProcessor
println(processor.process("  hello world  "))  // HELLO WORLD
```

## Abstract Class

```scala
abstract class Animal(val name: String) {
  // Abstract method
  def speak(): String

  // Implemented method
  def describe(): String = s"$name says ${speak()}"
}

class Dog(name: String) extends Animal(name) {
  def speak(): String = "Woof"
}

class Cat(name: String) extends Animal(name) {
  def speak(): String = "Meow"
}

val dog = new Dog("Buddy")
println(dog.describe())  // Buddy says Woof
```

### Abstract Class vs Trait

| Feature | Abstract Class | Trait |
|---------|---------------|-------|
| Constructor parameters | Yes | Only in Scala 3 |
| Multiple inheritance | No | Yes (mixin) |
| Java compatibility | Good | Limited |

> **Recommendation:** Use traits unless there's a specific reason.

## Access Modifiers

```scala
class MyClass {
  private val privateField = 1      // Only this class
  protected val protectedField = 2  // This class and subclasses
  val publicField = 3               // Anywhere

  private[this] val strictPrivate = 4  // Only this instance
}

// Package level access
class PackageAccess {
  private[mypackage] val packagePrivate = 5  // Only within mypackage
}
```

## Enum (Scala 3)

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// Simple enumeration
enum Color:
  case Red, Green, Blue

val color = Color.Red
println(color)  // Red

// Enumeration with parameters
enum Planet(val mass: Double, val radius: Double):
  case Mercury extends Planet(3.303e+23, 2.4397e6)
  case Venus   extends Planet(4.869e+24, 6.0518e6)
  case Earth   extends Planet(5.976e+24, 6.37814e6)

println(Planet.Earth.mass)  // 5.976E24

// ADT style
enum Shape:
  case Circle(radius: Double)
  case Rectangle(width: Double, height: Double)
  case Triangle(base: Double, height: Double)

import Shape.*
val shapes = List(Circle(5), Rectangle(3, 4), Triangle(6, 4))
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// Implement enum with sealed trait + case object
sealed trait Color
object Color {
  case object Red extends Color
  case object Green extends Color
  case object Blue extends Color
}

val color: Color = Color.Red

// ADT style
sealed trait Shape
case class Circle(radius: Double) extends Shape
case class Rectangle(width: Double, height: Double) extends Shape
case class Triangle(base: Double, height: Double) extends Shape

val shapes: List[Shape] = List(Circle(5), Rectangle(3, 4), Triangle(6, 4))
```
{{% /tab %}}
{{< /tabs >}}

## Exercises

### 1. Bank Account Class

Implement a `BankAccount` class that manages balance.
- `deposit(amount)`: Deposit
- `withdraw(amount)`: Withdraw (return false if insufficient balance)
- `balance`: Current balance

<details>
<summary>Show Answer</summary>

```scala
class BankAccount(initialBalance: Double) {
  private var _balance: Double = initialBalance

  def balance: Double = _balance

  def deposit(amount: Double): Unit =
    if (amount > 0) _balance += amount

  def withdraw(amount: Double): Boolean =
    if (amount > 0 && amount <= _balance) {
      _balance -= amount
      true
    } else false
}

val account = new BankAccount(1000)
account.deposit(500)
println(account.balance)       // 1500.0
println(account.withdraw(200)) // true
println(account.balance)       // 1300.0
println(account.withdraw(2000)) // false
```

</details>

### 2. Trait Mixin

Define `Printable` and `Comparable` traits, and mixin into a `Product` class.

<details>
<summary>Show Answer</summary>

```scala
trait Printable {
  def print(): String
}

trait Comparable[T] {
  def compare(other: T): Int
}

case class Product(name: String, price: Double)
    extends Printable with Comparable[Product] {

  def print(): String = s"Product: $name, Price: $price"

  def compare(other: Product): Int = this.price.compare(other.price)
}

val p1 = Product("Laptop", 1500)
val p2 = Product("Mouse", 50)

println(p1.print())         // Product: Laptop, Price: 1500.0
println(p1.compare(p2))     // 1 (p1 is more expensive)
```

</details>

## Next Steps

- [Case Classes](../case-classes/) — Immutable data modeling
- [Pattern Matching](../pattern-matching/) — Advanced match expressions
