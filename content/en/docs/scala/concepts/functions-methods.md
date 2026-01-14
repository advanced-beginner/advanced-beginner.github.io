---
lastmod: "2026-01-14"
title: Functions and Methods
weight: 3
---

{{< callout type="info" title="TL;DR" >}}
- In Scala, functions are **first-class citizens** that can be stored in variables, passed as arguments, and returned as values
- **Currying** implements a pattern of applying configuration values once and reusing them
- Methods (`def`) belong to classes, while function values (`val`) are suitable for passing/storing
- `@tailrec` ensures tail recursion optimization
{{< /callout >}}

**Target Audience:** Java developers or those with OOP experience
**Prerequisites:** Scala basic syntax, control structures

In Scala, functions are first-class citizens. You can store functions in variables, pass them as arguments, and return them as values. This characteristic is the essence of functional programming and makes code more concise and reusable.

#### Why First-Class Functions?

**Problems Java Developers Face**

In Java, passing "behavior" requires interfaces and classes:

```java
// Java: Changing price calculation method with strategy pattern
public interface PricingStrategy {
    double calculate(double price);
}

public class RegularPricing implements PricingStrategy {
    @Override
    public double calculate(double price) { return price; }
}

public class DiscountPricing implements PricingStrategy {
    private double rate;
    public DiscountPricing(double rate) { this.rate = rate; }
    @Override
    public double calculate(double price) { return price * (1 - rate); }
}

// Usage
public double applyPricing(double price, PricingStrategy strategy) {
    return strategy.calculate(price);
}
applyPricing(10000, new DiscountPricing(0.1));  // 9000.0
```

Improved with Java 8 lambdas, but still requires explicit functional interfaces (`Function`, `BiFunction`, `Consumer`, etc.):

```java
// Java 8+
Function<Double, Double> discount = price -> price * 0.9;
double result = discount.apply(10000.0);  // Need to call apply() method
```

**Scala's Solution**

In Scala, functions are values. You can use them directly without special interfaces:

```scala
// Scala: Functions as values
val discount = (price: Double) => price * 0.9
val result = discount(10000)  // 9000.0 - direct invocation

// Pass functions as arguments
def applyPricing(price: Double, strategy: Double => Double): Double =
  strategy(price)

applyPricing(10000, discount)           // 9000.0
applyPricing(10000, _ * 1.1)            // 11000.0 (inline lambda)
applyPricing(10000, Math.floor)         // Can also pass existing methods
```

**Real-world Use: Simplifying Strategy Pattern**

Since functions can be treated as first-class citizens, you can implement the strategy pattern simply without interfaces and classes. The example below shows how to define and select various discount strategies as functions in an order system.

```scala
// Order processing system
case class Order(items: List[Item], customerId: String)
case class Item(name: String, price: Double, quantity: Int)

// Various discount strategies - just define as functions
val noDiscount: Order => Double = order =>
  order.items.map(i => i.price * i.quantity).sum

val memberDiscount: Order => Double = order =>
  noDiscount(order) * 0.9

val bulkDiscount: Order => Double = order => {
  val total = noDiscount(order)
  if (total > 100000) total * 0.85 else total
}

// Strategy selection
def calculateTotal(order: Order, strategy: Order => Double): Double =
  strategy(order)

// Manage strategies with Map
val strategies = Map(
  "regular"  -> noDiscount,
  "member"   -> memberDiscount,
  "bulk"     -> bulkDiscount
)

// Select strategy based on customer type
def getStrategy(customerId: String): Order => Double =
  if (customerId.startsWith("VIP")) memberDiscount
  else noDiscount
```

{{< callout type="info" title="Key Points" >}}
- Functions can be stored in variables, passed as arguments, and returned as values like values
- Implement strategy pattern simply with just functions, without interfaces/classes
- Unlike Java's functional interfaces, can call directly without `.apply()`
{{< /callout >}}

#### Why Currying?

Currying is a technique of transforming a function that takes multiple parameters into a series of functions that each take a single parameter. This allows creating new functions by applying only some arguments of a function.

**Problem: Repeated Configuration Values**

You have to pass a logger instance every time you log:

```scala
// Pass logger every time
def logInfo(logger: Logger, message: String): Unit =
  logger.info(message)

def logError(logger: Logger, message: String, cause: Throwable): Unit =
  logger.error(message, cause)

// Repetition every time it's used
val logger = LoggerFactory.getLogger("OrderService")
logInfo(logger, "Order started")
logInfo(logger, "Validation complete")
logInfo(logger, "Processing payment")
logError(logger, "Payment failed", exception)
```

**Currying's Solution**

Apply configuration once and reuse:

```scala
// Separate with currying
def logInfo(logger: Logger)(message: String): Unit =
  logger.info(message)

def logError(logger: Logger)(message: String)(cause: Throwable): Unit =
  logger.error(message, cause)

// Apply logger only once
val logger = LoggerFactory.getLogger("OrderService")
val info = logInfo(logger)      // String => Unit
val error = logError(logger)    // String => Throwable => Unit

// Use concisely thereafter
info("Order started")
info("Validation complete")
info("Processing payment")
error("Payment failed")(exception)
```

**Real-world Use: Dependency Injection Pattern**

Using currying, you can manage dependencies elegantly without a DI framework. Dependencies like databases or external services are received in the first parameter group of a function, and "services" are created through partial application.

```scala
// Database operation functions
def findUser(db: Database)(userId: String): Option[User] =
  db.query(s"SELECT * FROM users WHERE id = '$userId'").headOption

def saveOrder(db: Database)(order: Order): Unit =
  db.execute(s"INSERT INTO orders ...")

def sendEmail(mailer: Mailer)(to: String)(subject: String)(body: String): Unit =
  mailer.send(to, subject, body)

// Configure once at application startup
val db = Database.connect("jdbc:postgresql://localhost/shop")
val mailer = Mailer.create("smtp.example.com")

// Create "services" with partial application
val getUser = findUser(db)           // String => Option[User]
val createOrder = saveOrder(db)      // Order => Unit
val notify = sendEmail(mailer)       // String => String => String => Unit

// Use concisely in business logic
def processOrder(userId: String, items: List[Item]): Unit = {
  getUser(userId) match {
    case Some(user) =>
      val order = Order(items, userId)
      createOrder(order)
      notify(user.email)("Order Confirmation")(s"Your order has been received: ${order.id}")
    case None =>
      throw new IllegalArgumentException(s"User not found: $userId")
  }
}
```

{{< callout type="info" title="Key Points" >}}
- Currying is a technique of separating a function into multiple parameter groups
- Apply configuration values once and reuse to reduce repetition
- Can implement dependency injection pattern without a DI framework
{{< /callout >}}

#### Method vs Function: When to Choose What?

Methods (`def`) and function values (`val`) look similar but have different use purposes. Methods define behavior belonging to a class or object, while function values are useful when passing to other functions or storing in collections.

The table below summarizes what to choose in different situations.

| Situation | Recommended | Reason |
|-----------|-------------|--------|
| Define class/object behavior | `def` method | Clear ownership, can reference `this` |
| Need to store/pass functions | `val` function | Already a value, no conversion needed |
| Recursion | `def` method | `@tailrec` tail recursion optimization possible |
| Collection operation callbacks | Inline lambda | `list.map(x => x * 2)` |
| Need overloading | `def` method | Functions cannot be overloaded |
| Need lazy evaluation | `def` method | Evaluated each time it's called |

**Warning: Excessive Function Use**

Declaring all methods as function values can actually make code more complex. Use function values only when you actually need to treat functions as values, such as for strategy patterns, callbacks, or higher-order function arguments.

```scala
// ❌ Unnecessarily complex
class Calculator {
  val add: (Int, Int) => Int = (a, b) => a + b
  val multiply: (Int, Int) => Int = (a, b) => a * b
}

// ✅ Simple and clear
class Calculator {
  def add(a: Int, b: Int): Int = a + b
  def multiply(a: Int, b: Int): Int = a * b
}

// Functions are appropriate: strategy pattern, callbacks, higher-order function arguments
val operations: Map[String, (Int, Int) => Int] = Map(
  "+" -> (_ + _),
  "-" -> (_ - _),
  "*" -> (_ * _)
)
```

{{< callout type="info" title="Key Points" >}}
- **Method (`def`)**: Suitable for class/object behavior definition, recursion, overloading
- **Function value (`val`)**: Suitable for storage/passing, strategy pattern, callbacks
- Don't declare everything as function values; use only when necessary
{{< /callout >}}

#### Method Definition

Methods are functions defined in a class or object. Declared with the `def` keyword, you specify parameter types and return type.

**Basic Syntax**

Define methods with the `def` keyword.

```scala
// Basic form
def add(a: Int, b: Int): Int = {
  a + b
}

// Single line can omit braces
def add(a: Int, b: Int): Int = a + b

// Return type inference (not recommended - explicit is better)
def add(a: Int, b: Int) = a + b
```

**Parameter Types are Required**

Scala doesn't infer parameter types. They must be specified.

```scala
// Correct
def greet(name: String): String = s"Hello, $name!"

// Compile error
// def greet(name) = s"Hello, $name!"
```

**Unit Return (Side Effects)**

Methods that don't return anything return `Unit`. Similar to Java's `void`, but Unit is an actual value in Scala.

```scala
def printGreeting(name: String): Unit = {
  println(s"Hello, $name!")
}

// Shorthand (omit return type)
def printGreeting(name: String) = println(s"Hello, $name!")
```

**Scala 3 Syntax**

In Scala 3, you can delimit blocks with indentation instead of braces. Multi-line methods can also be written cleanly with just indentation.

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// Indentation-based
def greet(name: String): String =
  val greeting = s"Hello, $name!"
  greeting

// Multiple lines
def calculate(x: Int, y: Int): Int =
  val sum = x + y
  val product = x * y
  sum + product
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// Braces required
def greet(name: String): String = {
  val greeting = s"Hello, $name!"
  greeting
}

// Multiple lines
def calculate(x: Int, y: Int): Int = {
  val sum = x + y
  val product = x * y
  sum + product
}
```
{{% /tab %}}
{{< /tabs >}}

{{< callout type="info" title="Key Points" >}}
- Parameter types must be specified (not inferred)
- `Unit` return is similar to Java's `void` but is an actual value
- In Scala 3, you can delimit blocks with indentation
{{< /callout >}}

#### Default Parameter Values

Specifying default values for parameters allows omitting those arguments when calling. This can consolidate multiple overloaded methods into one.

```scala
def greet(name: String = "World", punctuation: String = "!"): String =
  s"Hello, $name$punctuation"

println(greet())                    // Hello, World!
println(greet("Scala"))             // Hello, Scala!
println(greet("Scala", "?"))        // Hello, Scala?
```

#### Named Arguments

Passing arguments by name allows changing the order. This improves readability when there are many parameters or multiple parameters of the same type.

```scala
def createPerson(name: String, age: Int, city: String): String =
  s"$name, $age years old, lives in $city"

// Can change order
println(createPerson(age = 30, city = "Seoul", name = "John"))

// Partially named
println(createPerson("Jane", city = "Busan", age = 25))
```

#### Varargs

Use `*` to accept a variable number of arguments. Use the sequence spread operator when passing a collection as varargs.

```scala
def sum(numbers: Int*): Int = numbers.sum

println(sum(1, 2, 3))        // 6
println(sum(1, 2, 3, 4, 5))  // 15

// Spread a sequence
val nums = Seq(1, 2, 3, 4, 5)
println(sum(nums*))          // Scala 3
// println(sum(nums: _*))    // Scala 2
```

{{< callout type="info" title="Key Points" >}}
- **Default values**: Reduce overloading with parameter defaults
- **Named arguments**: Can change order or improve readability
- **Varargs**: Receive multiple arguments with `*`, pass collections with sequence spread
{{< /callout >}}

#### Anonymous Functions (Lambdas)

Anonymous functions (lambdas) are functions defined without names. Mainly used when passing to higher-order functions or storing in variables. Write parameters on the left of the arrow (`=>`) and the body on the right.

**Basic Syntax**

```scala
// Full form
val add: (Int, Int) => Int = (a: Int, b: Int) => a + b

// Type inference
val add = (a: Int, b: Int) => a + b

// Single parameter can omit parentheses
val double = (x: Int) => x * 2

// Usage
println(add(1, 2))    // 3
println(double(5))    // 10
```

**Shorthand Syntax**

When using lambdas in collection methods, type inference and placeholder syntax allow more concise writing. The placeholder `_` represents each different argument.

```scala
val numbers = List(1, 2, 3, 4, 5)

// Full form
numbers.map((x: Int) => x * 2)

// Type inference
numbers.map(x => x * 2)

// Placeholder syntax (each _ is a different argument)
numbers.map(_ * 2)

// Multiple arguments
numbers.reduce((a, b) => a + b)
numbers.reduce(_ + _)
```

**Multi-line Lambdas**

When the lambda body spans multiple lines, enclose in braces and the last expression becomes the return value.

```scala
val process = (x: Int) => {
  val doubled = x * 2
  val squared = doubled * doubled
  squared
}
```

{{< callout type="info" title="Key Points" >}}
- Lambdas are defined in `(params) => body` format
- Can write concisely with placeholder `_`
- Can omit parameter types when they're inferred
{{< /callout >}}

#### Higher-Order Functions

Higher-order functions are functions that take functions as arguments or return functions. A core concept of functional programming that increases code reusability and abstraction level.

**Taking Functions as Arguments**

Taking functions as parameters allows injecting behavior from outside. This enables performing various behaviors with the same code structure.

```scala
def applyTwice(f: Int => Int, x: Int): Int = f(f(x))

val double = (x: Int) => x * 2
println(applyTwice(double, 3))  // 12 (3 -> 6 -> 12)

// Pass lambda directly
println(applyTwice(x => x + 10, 5))  // 25 (5 -> 15 -> 25)
```

**Returning Functions**

Returning functions allows dynamically creating functions that "remember" configuration values or environment. This is called a closure.

```scala
def multiplier(factor: Int): Int => Int = {
  (x: Int) => x * factor
}

val triple = multiplier(3)
println(triple(5))   // 15
println(triple(10))  // 30
```

{{< callout type="info" title="Key Points" >}}
- **Higher-order functions**: Functions that take functions as arguments or return functions
- Returning functions can create closures that "remember" configuration
- Increases code reusability and abstraction level
{{< /callout >}}

#### Currying

Currying is separating a function that takes multiple parameters into multiple parameter lists. Each time arguments are applied to a parameter list, a new function is returned.

```scala
// Curried function
def add(a: Int)(b: Int): Int = a + b

println(add(1)(2))  // 3
```

**Partial Application**

You can create new functions by applying only some arguments of a curried function:

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
def add(a: Int)(b: Int): Int = a + b

// Apply only first parameter
val add5 = add(5)   // Int => Int
println(add5(10))   // 15

// Type annotation also works
val add10: Int => Int = add(10)
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
def add(a: Int)(b: Int): Int = a + b

// Need underscore for partial application
val add5 = add(5)_  // Int => Int
println(add5(10))   // 15

// Or provide type hint
val add10: Int => Int = add(10)
```
{{% /tab %}}
{{< /tabs >}}

> 💡 **Difference:** In Scala 2, an underscore like `add(5)_` was needed, but Scala 3 infers from context.

**Currying Use Cases**

Currying is useful for improving type inference and building DSLs. Type information from the first parameter group is used for type inference of the second group.

```scala
// Improved type inference
def transform[A, B](list: List[A])(f: A => B): List[B] =
  list.map(f)

// f's type is inferred from first parameter
transform(List(1, 2, 3))(x => x * 2)  // List(2, 4, 6)

// DSL with currying
def withResource[T](resource: => T)(cleanup: T => Unit)(action: T => Unit): Unit = {
  val r = resource
  try {
    action(r)
  } finally {
    cleanup(r)
  }
}
```

{{< callout type="info" title="Key Points" >}}
- Currying uses `def f(a)(b)(c)` format with multiple parameter lists
- Partial application creates new functions with only some arguments applied
- Useful for improving type inference and building DSLs
{{< /callout >}}

#### Partially Applied Functions

Partial application is a technique of creating new functions by applying only some arguments. Can also be applied to regular non-curried functions, using underscore (`_`) to indicate remaining arguments.

```scala
def log(level: String, message: String): Unit =
  println(s"[$level] $message")

// Partial application
val info = log("INFO", _)
val error = log("ERROR", _)

info("Starting")         // [INFO] Starting
error("Error occurred")  // [ERROR] Error occurred
```

{{< callout type="info" title="Key Points" >}}
- Partial application indicates remaining arguments with `_`
- Can also apply to non-curried functions
- Creates specialized functions by pre-applying common arguments
{{< /callout >}}

#### Recursive Functions

Recursive functions call themselves. Must specify return type so the compiler can verify the type of recursive calls.

```scala
def factorial(n: Int): Int =
  if (n <= 1) 1
  else n * factorial(n - 1)

println(factorial(5))  // 120
```

**Tail Recursion Optimization**

Tail recursion is when the recursive call is the last operation of the function. The Scala compiler converts tail recursion to a loop to prevent stack overflow. Adding the `@tailrec` annotation causes a compilation error if it's not tail recursive.

```scala
import scala.annotation.tailrec

def factorial(n: Int): Int = {
  @tailrec
  def loop(n: Int, acc: Int): Int =
    if (n <= 1) acc
    else loop(n - 1, n * acc)

  loop(n, 1)
}

println(factorial(5))  // 120
```

{{< callout type="info" title="Key Points" >}}
- Recursive functions **must specify return type**
- `@tailrec` ensures tail recursion optimization and prevents stack overflow
- Tail recursion requires the recursive call to be the function's last operation
{{< /callout >}}

#### Function Types

Function types are in `(ArgTypes) => ReturnType` format. This type is used when storing functions in variables or passing as parameters.

```scala
// Function type declarations
val f1: Int => Int = x => x * 2
val f2: (Int, Int) => Int = (a, b) => a + b
val f3: () => Int = () => 42
val f4: Int => Int => Int = a => b => a + b  // Curried

// Higher-order function types
def process(f: String => Int): Int = f("hello")
```

{{< callout type="info" title="Key Points" >}}
- Function types are expressed as `(A, B) => C`
- No arguments: `() => C`, curried: `A => B => C`
- Frequently used as higher-order function parameter types
{{< /callout >}}

#### @main Annotation (Scala 3)

In Scala 3, you can simply define program entry points with the `@main` annotation. Declaring parameters automatically parses command-line arguments.

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// Simple entry point
@main def hello(): Unit =
  println("Hello, World!")

// With arguments
@main def greet(name: String, times: Int): Unit =
  for _ <- 1 to times do
    println(s"Hello, $name!")

// Run: scala greet Scala 3
// Output:
// Hello, Scala!
// Hello, Scala!
// Hello, Scala!
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// main method in object
object Hello {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
  }
}

// Or extend App trait
object Hello extends App {
  println("Hello, World!")
}
```
{{% /tab %}}
{{< /tabs >}}

{{< callout type="info" title="Key Points" >}}
- Scala 3: Simply define entry points with `@main`
- Declaring parameters automatically parses command-line arguments
- Scala 2: Use `main` method of `object` or `App` trait
{{< /callout >}}

#### Exercises

Practice function and method concepts with these exercises.

**1. Higher-Order Function Implementation ⭐⭐**

Implement `applyAll` that takes a value and a list of functions, applying all functions sequentially.

```scala
def applyAll(x: Int, functions: List[Int => Int]): Int = ???

val fns = List(
  (x: Int) => x + 1,
  (x: Int) => x * 2,
  (x: Int) => x - 3
)
println(applyAll(5, fns))  // ((5 + 1) * 2) - 3 = 9
```

<details>
<summary>Show Answer</summary>

```scala
def applyAll(x: Int, functions: List[Int => Int]): Int =
  functions.foldLeft(x)((acc, f) => f(acc))
```

</details>

**2. Currying Conversion ⭐**

Convert a regular function to a curried function.

```scala
def add(a: Int, b: Int, c: Int): Int = a + b + c

// Conversion result
val curriedAdd: Int => Int => Int => Int = ???
```

<details>
<summary>Show Answer</summary>

```scala
val curriedAdd: Int => Int => Int => Int =
  a => b => c => a + b + c

// Or
val curriedAdd = (add _).curried
```

</details>

**3. Tail-Recursive Fibonacci ⭐⭐⭐**

Write a tail-recursive function to calculate Fibonacci numbers.

<details>
<summary>Show Answer</summary>

```scala
import scala.annotation.tailrec

def fibonacci(n: Int): Long = {
  @tailrec
  def loop(n: Int, prev: Long, curr: Long): Long =
    if (n <= 0) prev
    else loop(n - 1, curr, prev + curr)

  loop(n, 0, 1)
}

println(fibonacci(10))  // 55
println(fibonacci(50))  // 12586269025
```

</details>

#### Next Steps

- [Classes and Objects](../classes-objects/) — OOP basics
- [Higher-Order Functions](../higher-order-functions/) — Advanced functional programming
