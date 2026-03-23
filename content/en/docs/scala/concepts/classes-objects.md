---
lastmod: "2026-01-14"
title: Classes and Objects
description: "Scala classes and objects: mechanics and design patterns"
weight: 4
---

{{< callout type="info" title="TL;DR" >}}
- **Classes**: Write constructor parameters directly in the declaration to reduce boilerplate
- **object**: Singleton instances are supported at the language level
- **Companion objects**: Replace static members with same-named objects as classes
- **Traits**: Enjoy the benefits of multiple inheritance while avoiding the diamond problem
{{< /callout >}}

**Target Audience:** Developers with Java/OOP experience
**Prerequisites:** Scala basic syntax, functions and methods

Scala supports both object-oriented and functional programming. Classes, objects, traits, and other OOP features can be used harmoniously with the functional paradigm. In particular, Scala's object supports the singleton pattern at the language level, and traits maximize code reuse while solving multiple inheritance problems.

#### Classes

Classes are the basic unit that encapsulates data and behavior. Scala classes write constructor parameters directly in the class declaration to reduce boilerplate code.

**Basic Class Definition**

```scala
// Basic class
class Person(name: String, age: Int) {
  def greet(): String = s"Hello, I'm $name."
}

val person = new Person("John", 30)
println(person.greet())  // Hello, I'm John.
```

**Constructor Parameters**

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

**Auxiliary Constructors**

Auxiliary constructors are defined with the `this` keyword and must first call the primary constructor or another auxiliary constructor.

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

**Using Default Values (Recommended)**

Using default parameter values is cleaner and easier to maintain than auxiliary constructors.

```scala
// Default values are cleaner than auxiliary constructors
class Person(val name: String = "Unknown", val age: Int = 0)

val p1 = new Person("John", 30)
val p2 = new Person("Jane")
val p3 = new Person()
```

**Scala 3 Syntax**

In Scala 3, you can define the class body with a colon and indentation. You can write cleanly without braces.

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

{{< callout type="info" title="Key Points" >}}
- Adding `val`/`var` to constructor parameters automatically creates fields
- Using default parameter values is cleaner than auxiliary constructors
- In Scala 3, define class body with colon and indentation
{{< /callout >}}

#### Object

`object` defines a singleton instance. Implementing the singleton pattern in Java requires complex code like private constructors, static fields, and synchronization, but Scala solves it with just the `object` keyword. Objects are lazily initialized on first access and are thread-safe.

**Singleton Object**

An object has only one instance throughout the program. Suitable for global state, utility methods, factories, etc.

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

**Utility Methods**

Using object for a collection of pure functions without state is similar to Java's static methods.

```scala
object MathUtils {
  def square(x: Int): Int = x * x
  def cube(x: Int): Int = x * x * x
  val PI: Double = 3.14159
}

println(MathUtils.square(5))  // 25
println(MathUtils.PI)         // 3.14159
```

{{< callout type="info" title="Key Points" >}}
- `object` defines a singleton instance (use without new)
- Lazily initialized and thread-safe
- Suitable for utility methods, global state, factories
{{< /callout >}}

#### Companion Object

An object with the same name as a class is called a **companion object**. Companion objects must be defined in the same file and can access each other's private members with the class. Replaces Java's static members and is mainly used to define factory methods or constants.

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

**Private Member Access**

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

{{< callout type="info" title="Key Points" >}}
- Companion objects are defined with the same name as a class in the same file
- Can access each other's `private` members
- `apply` method allows creating instances without new
{{< /callout >}}

#### Trait

Traits are similar to Java interfaces but can include implementations. Classes can mixin multiple traits to enjoy the benefits of multiple inheritance while avoiding the diamond problem. Traits play a key role in code reuse, separation of concerns, and modularization.

**Basic Trait**

Defining only abstract methods can be used like a Java interface.

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

**Trait with Implementation**

Providing default implementations in traits allows implementing classes to use them without redefinition.

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

**Multiple Traits (Mixin)**

You can combine multiple traits with the `with` keyword. The first supertype uses `extends`, the rest use `with`.

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

**Trait Stacking**

When multiple traits override the same method, they're called according to linearization order. `super.process` calls the next trait's method in linearization order. This is called the stackable modification pattern.

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

{{< callout type="info" title="Key Points" >}}
- Traits are interfaces that can include implementations
- Can mixin multiple traits with `with` keyword
- Trait stacking can combine behaviors (linearization order)
{{< /callout >}}

#### Abstract Class

Abstract classes cannot be instantiated and can define abstract members that must be implemented by subclasses. Unlike traits, they can have constructor parameters.

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

**Abstract Class vs Trait**

The table below summarizes the main differences between abstract classes and traits. Generally, traits are preferred, but abstract classes are used when constructor parameters are needed or Java compatibility is important.

| Feature | Abstract Class | Trait |
|---------|---------------|-------|
| Constructor parameters | Yes | Only in Scala 3 |
| Multiple inheritance | No | Yes (mixin) |
| Java compatibility | Good | Limited |

> **Recommendation:** Use traits unless there's a specific reason.

{{< callout type="info" title="Key Points" >}}
- Abstract classes can have constructor parameters
- Traits allow multiple inheritance, but abstract classes only single inheritance
- Generally prefer traits; use abstract classes when constructors are needed
{{< /callout >}}

#### Access Modifiers

Scala's access modifiers are more granular than Java's. The default is public, and private and protected can be used with scope specifiers for precise access control.

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

{{< callout type="info" title="Key Points" >}}
- Default is public; use `private` and `protected`
- `private[this]` only accessible from same instance
- `private[packagename]` controls package-level access
{{< /callout >}}

#### Enum (Scala 3)

In Scala 3, you can define enumerations with the `enum` keyword. Supports various forms from simple enumerations to enumerations with parameters and ADT (Algebraic Data Type) style. In Scala 2, enumerations were implemented with sealed trait and case object combinations.

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

{{< callout type="info" title="Key Points" >}}
- Scala 3: Concisely define enumerations and ADTs with `enum`
- Scala 2: Use `sealed trait` + `case object/class` combination
- Supports both enumerations with parameters and ADT style
{{< /callout >}}

#### Exercises

Practice class and object concepts with these exercises.

**1. Bank Account Class**

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

**2. Trait Mixin**

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

#### Next Steps

- [Case Classes]({{< relref "/docs/scala/concepts/case-classes" >}}) — Immutable data modeling
- [Pattern Matching]({{< relref "/docs/scala/concepts/pattern-matching" >}}) — Advanced match expressions
