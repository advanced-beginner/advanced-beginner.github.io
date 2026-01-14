---
lastmod: "2026-01-14"
title: Higher-Order Functions
weight: 8
---

{{< callout type="info" title="TL;DR" >}}
- **Higher-order functions** are functions that take functions as arguments or return functions
- They separate "what" to do from "how" to do it, improving code reusability
- Process collections declaratively with map, filter, fold, etc.
- Implement advanced abstractions with currying, closures, and partial functions
{{< /callout >}}

**Target Audience:** Developers interested in functional programming
**Prerequisites:** Scala functions and methods, collection basics

Higher-order functions are functions that take functions as arguments or return functions. As a core concept of functional programming, they increase code reusability and raise the level of abstraction.

#### Why Are Higher-Order Functions Powerful?

Higher-order functions separate "what" to do from "how" to do it. They abstract repeating patterns into functions and pass only the varying parts as arguments.

**Problem: Repeating Patterns**

In Java-style imperative code, similar patterns like filtering, transformation, and reduction repeat frequently.

```java
// Java: Calculate total from order list
List<Order> orders = getOrders();
double total = 0;
for (Order order : orders) {
    if (order.getStatus().equals("COMPLETED")) {  // Filtering
        double price = order.getPrice() * 1.1;     // Transformation (add tax)
        total += price;                             // Reduction
    }
}

// Same pattern repeats elsewhere...
List<String> names = new ArrayList<>();
for (User user : users) {
    if (user.isActive()) {                        // Filtering
        names.add(user.getName().toUpperCase());  // Transformation
    }
}
```

**Problems:**
- Focus on "how" rather than "what"
- Bug potential from mutable state (`total`, `names`)
- Difficult to reuse

**Higher-Order Function Solution**

In Scala, you only declare "what":

```scala
// Scala: Declarative style
val total = orders
  .filter(_.status == "COMPLETED")  // What: only completed orders
  .map(_.price * 1.1)               // What: add tax
  .sum                              // What: sum

val names = users
  .filter(_.isActive)               // What: only active users
  .map(_.name.toUpperCase)          // What: names to uppercase
```

**Advantages:**
- Intent is clearly expressed
- Immutability maintained (no mutable state)
- Each operation can be tested independently
- Complex transformations are readable through chaining

**Comparison with Java Stream API**

Java 8+ Stream API provides similar functionality, but Scala is more concise. The table below summarizes the differences between the two approaches.

```java
// Java Stream
double total = orders.stream()
    .filter(o -> o.getStatus().equals("COMPLETED"))
    .mapToDouble(o -> o.getPrice() * 1.1)
    .sum();
```

```scala
// Scala
val total = orders
  .filter(_.status == "COMPLETED")
  .map(_.price * 1.1)
  .sum
```

| Comparison | Java Stream | Scala Collection |
|----------|-------------|------------------|
| Default behavior | Lazy evaluation | Eager evaluation (lazy with view) |
| Reusability | Single use only | Unlimited reuse |
| Syntax | Requires `.stream()`, `.collect()` | Direct use |
| Type hints | Often required | Mostly inferred |
| Primitive handling | Requires `mapToInt`, `mapToDouble` | Automatic conversion |

#### Practical Example: Order Processing Pipeline

Let's see how higher-order functions are used in actual business logic.

**Requirements**

An online shopping mall needs the following processing:
1. Filter only valid orders
2. Apply discounts based on membership tier
3. Calculate total amount per product
4. Add shipping fee
5. Calculate final payment amount

**Domain Model**

First, define the domain model needed for order processing.

```scala
case class Order(
  id: String,
  customerId: String,
  items: List[OrderItem],
  status: OrderStatus
)

case class OrderItem(
  productId: String,
  name: String,
  price: Double,
  quantity: Int
)

enum OrderStatus:
  case Pending, Confirmed, Shipped, Cancelled

case class Customer(
  id: String,
  name: String,
  tier: CustomerTier
)

enum CustomerTier:
  case Bronze, Silver, Gold, Platinum
```

**Implementing Pipeline with Higher-Order Functions**

Using higher-order functions, define each step as an independent function and compose them. The key is applyDiscount, which returns a function.

```scala
object OrderProcessor:
  // Discount rate mapping
  val discountRates: Map[CustomerTier, Double] = Map(
    CustomerTier.Bronze   -> 0.0,
    CustomerTier.Silver   -> 0.05,
    CustomerTier.Gold     -> 0.10,
    CustomerTier.Platinum -> 0.15
  )

  // Order validation - flexible with higher-order function
  def isValidOrder(order: Order): Boolean =
    order.status != OrderStatus.Cancelled &&
    order.items.nonEmpty

  // Calculate order item total
  def calculateItemTotal(item: OrderItem): Double =
    item.price * item.quantity

  // Calculate order total
  def calculateOrderTotal(order: Order): Double =
    order.items.map(calculateItemTotal).sum

  // Create discount application function (higher-order function that returns a function)
  def applyDiscount(tier: CustomerTier): Double => Double = {
    val rate = discountRates.getOrElse(tier, 0.0)
    total => total * (1 - rate)
  }

  // Calculate shipping fee
  def calculateShipping(total: Double): Double =
    if (total >= 50000) 0
    else if (total >= 30000) 2500
    else 3500

  // Overall pipeline
  def processOrders(
    orders: List[Order],
    getCustomer: String => Option[Customer]
  ): List[(Order, Double)] = {
    orders
      .filter(isValidOrder)                          // 1. Only valid orders
      .flatMap { order =>                            // 2. Combine with customer info
        getCustomer(order.customerId).map(c => (order, c))
      }
      .map { case (order, customer) =>               // 3. Calculate price
        val subtotal = calculateOrderTotal(order)
        val discounted = applyDiscount(customer.tier)(subtotal)
        val shipping = calculateShipping(discounted)
        val finalTotal = discounted + shipping
        (order, finalTotal)
      }
  }
```

**Usage Example**

```scala
// Test data
val orders = List(
  Order("O001", "C001", List(
    OrderItem("P1", "Laptop", 1200000, 1),
    OrderItem("P2", "Mouse", 50000, 2)
  ), OrderStatus.Confirmed),
  Order("O002", "C002", List(
    OrderItem("P3", "Keyboard", 80000, 1)
  ), OrderStatus.Cancelled),  // Excluded
  Order("O003", "C003", List(
    OrderItem("P4", "Monitor", 350000, 2)
  ), OrderStatus.Confirmed)
)

val customers = Map(
  "C001" -> Customer("C001", "Kim Chulsoo", CustomerTier.Gold),
  "C003" -> Customer("C003", "Lee Younghee", CustomerTier.Silver)
)

val results = OrderProcessor.processOrders(
  orders,
  id => customers.get(id)
)

results.foreach { case (order, total) =>
  println(s"Order ${order.id}: ${total} won")
}
// Order O001: 1170000.0 won (10% discount + free shipping)
// Order O003: 665000.0 won (5% discount + free shipping)
```

**Extension: Asynchronous Processing**

The same pattern can be extended to an asynchronous pipeline by combining with Future.

```scala
import scala.concurrent.{Future, ExecutionContext}

def processOrdersAsync(
  orders: List[Order],
  getCustomer: String => Future[Option[Customer]]
)(using ec: ExecutionContext): Future[List[(Order, Double)]] = {
  Future.sequence {
    orders
      .filter(isValidOrder)
      .map { order =>
        getCustomer(order.customerId).map { maybeCustomer =>
          maybeCustomer.map { customer =>
            val subtotal = calculateOrderTotal(order)
            val discounted = applyDiscount(customer.tier)(subtotal)
            val shipping = calculateShipping(discounted)
            (order, discounted + shipping)
          }
        }
      }
  }.map(_.flatten)
}
```

#### Higher-Order Function Selection Guide

You need to choose the appropriate higher-order function depending on the situation.

**When to Use What?**

The table below summarizes which function to use based on the task type.

| Task | Function | Example | Result |
|------|------|------|------|
| 1:1 transformation | `map` | Add tax to price | `List[A]` → `List[B]` |
| Conditional filtering | `filter` | Only valid orders | `List[A]` → `List[A]` (fewer) |
| 1:N transformation + flatten | `flatMap` | Order → individual products | `List[A]` → `List[B]` (flattened) |
| Reduce to single value | `fold`/`reduce` | Calculate total | `List[A]` → `B` |
| Execute side effects | `foreach` | Logging, DB save | `Unit` |
| Split by condition | `partition` | Success/failure classification | `(List[A], List[A])` |
| Group by key | `groupBy` | Products by category | `Map[K, List[A]]` |
| Pattern matching transformation | `collect` | Extract specific types only | `List[B]` (matched only) |

**Performance Considerations**

When processing large collections, use view or iterator to avoid creating intermediate collections.

```scala
// ❌ Creating intermediate collections on large collection
val result = (1 to 1000000)
  .map(_ * 2)      // Creates 1 million item list
  .filter(_ > 100) // Creates another list
  .take(10)        // Only needed 10...

// ✅ Lazy evaluation with view - compute only as needed
val result = (1 to 1000000)
  .view
  .map(_ * 2)
  .filter(_ > 100)
  .take(10)
  .toList

// ✅ Iterator also lazy evaluation
val result = (1 to 1000000)
  .iterator
  .map(_ * 2)
  .filter(_ > 100)
  .take(10)
  .toList
```

**Caution: Excessive Chaining**

Too long chaining makes debugging difficult. Separate into meaningful units and give them names.

```scala
// ❌ Too long chaining is hard to debug
val result = data
  .filter(_.isValid)
  .map(_.transform)
  .flatMap(_.split)
  .groupBy(_.category)
  .map { case (k, v) => k -> v.map(_.process) }
  .filter { case (_, v) => v.nonEmpty }
  .toMap

// ✅ Separate into meaningful units and give names
val validData = data.filter(_.isValid)
val transformed = validData.map(_.transform).flatMap(_.split)
val grouped = transformed.groupBy(_.category)
val processed = grouped.map { case (k, v) =>
  k -> v.map(_.process)
}.filter { case (_, v) => v.nonEmpty }
```

#### What Are Higher-Order Functions?

Let's examine the basic concept of higher-order functions with examples.

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

#### Major Higher-Order Functions

Let's examine in detail the most commonly used higher-order functions in Scala collections.

**map**

map transforms each element. It returns a new collection of the same size as the original.

```scala
val numbers = List(1, 2, 3, 4, 5)

// Double each element
numbers.map(x => x * 2)     // List(2, 4, 6, 8, 10)
numbers.map(_ * 2)          // Abbreviated form

// Type transformation
numbers.map(_.toString)     // List("1", "2", "3", "4", "5")

// Complex transformation
case class Person(name: String, age: Int)
val ages = List(25, 30, 35)
ages.map(age => Person(s"Person$age", age))
```

**filter**

filter selects only elements matching the condition. It returns a collection smaller than or equal to the original.

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

**flatMap**

flatMap transforms each element into a collection and then flattens it into one. Essential when handling 1:N transformations or Options.

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

**fold / foldLeft / foldRight**

Fold family functions reduce a collection to a single value using an initial value and a binary operation. Used for various aggregations like sum, product, string concatenation.

```scala
val numbers = List(1, 2, 3, 4, 5)

// foldLeft: reduce from left
numbers.foldLeft(0)(_ + _)    // 15
numbers.foldLeft(1)(_ * _)    // 120

// Process visualization: ((((0 + 1) + 2) + 3) + 4) + 5

// foldRight: reduce from right
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

**reduce**

reduce reduces without an initial value. Be careful as it throws an error on empty collections.

```scala
val numbers = List(1, 2, 3, 4, 5)

numbers.reduce(_ + _)    // 15
numbers.reduce(_ * _)    // 120
numbers.reduce(_ max _)  // 5
numbers.reduce(_ min _)  // 1

// reduceOption: returns None on empty collection
List.empty[Int].reduceOption(_ + _)  // None
```

**collect**

collect performs filtering and transformation simultaneously with pattern matching. It takes a PartialFunction as argument.

```scala
val mixed: List[Any] = List(1, "hello", 2, "world", 3)

// Extract only integers and double them
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

**partition**

partition separates elements that satisfy a condition and those that don't into two collections.

```scala
val numbers = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

val (evens, odds) = numbers.partition(_ % 2 == 0)
// evens = List(2, 4, 6, 8, 10)
// odds = List(1, 3, 5, 7, 9)
```

**groupBy**

groupBy groups elements according to the result of a key function, creating a Map.

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

#### Function Composition

Small functions can be combined to create larger functions.

**andThen and compose**

andThen composes functions from left to right, while compose composes from right to left.

```scala
val addOne = (x: Int) => x + 1
val double = (x: Int) => x * 2

// andThen: left -> right
val addThenDouble = addOne andThen double
addThenDouble(3)  // (3 + 1) * 2 = 8

// compose: right -> left
val doubleThenAdd = addOne compose double
doubleThenAdd(3)  // (3 * 2) + 1 = 7
```

**Chaining**

Construct data pipelines by calling collection methods sequentially.

```scala
val numbers = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

val result = numbers
  .filter(_ % 2 == 0)     // Only even numbers
  .map(_ * 2)             // Double
  .filter(_ > 10)         // Only above 10
  .sum                    // Sum

// result = 12 + 16 + 20 = 48
```

#### Currying

Currying transforms a function that takes multiple arguments into a chain of single-argument functions. Useful for partial application and type inference.

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

**Utilizing Currying**

Currying is particularly useful for improving type inference and building DSLs.

```scala
// Improved type inference
def transform[A, B](list: List[A])(f: A => B): List[B] =
  list.map(f)

// A type is inferred from first argument, no need to specify f's type
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

#### Closure

A closure captures variables from the environment where the function is defined. The function can access those variables even when executed outside its defining scope.

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

#### Partial Function

PartialFunction is a function defined only for some inputs. You can check if it's defined with isDefinedAt, and it's frequently used with collect.

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

#### Common Mistakes and Anti-patterns

Here are common mistakes when using higher-order functions and the correct solutions.

**❌ Things to Avoid**

```scala
// 1. Unnecessary lambda wrapping
list.map(x => f(x))  // Inefficient
list.map(x => x.toString)  // Inefficient

// 2. var + foreach instead of foldLeft
var sum = 0
list.foreach(sum += _)  // Mutable state!

// 3. flatMap instead of map + flatten
list.map(f).flatten  // Creates intermediate collection

// 4. Abusing complex placeholders
list.map(_ + _ * _)  // Hard to read!

// 5. map with side effects
list.map { x =>
  println(x)  // Side effect!
  x * 2
}
```

**✅ Correct Way**

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

**Performance Tips**

When processing large collections, using view enables lazy evaluation to avoid creating intermediate collections.

```scala
// Chaining vs View
// Creates new collection at each operation
list.map(_ * 2).filter(_ > 10).take(5)

// Lazy evaluation with View (no intermediate collections)
list.view.map(_ * 2).filter(_ > 10).take(5).toList

// Especially effective on large collections
(1 to 1000000)
  .view
  .map(_ * 2)
  .filter(_ % 3 == 0)
  .take(10)
  .toList
```

#### Practice Problems

Review higher-order function concepts through these practice problems.

**1. Implement Your Own map ⭐⭐**

Implement the `myMap` function using foldRight.

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

**2. Pipeline Function ⭐⭐**

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

**3. Memoization ⭐⭐⭐**

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

fastFib(100)  // Computed quickly
```

</details>

#### Next Steps

- [For Comprehension](../for-comprehensions/) — Elegant expression of monadic operations
- [Implicit/Given](../implicits/) — Contextual abstraction
