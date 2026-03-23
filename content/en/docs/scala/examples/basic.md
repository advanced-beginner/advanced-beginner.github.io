---
lastmod: "2026-01-14"
title: Basic Examples
description: "Step-by-step Scala basic syntax and collection usage"
weight: 2
---

{{< callout type="info" title="TL;DR" >}}
- Model immutable data with **case classes**, utilize auto-generated `equals` and `copy`
- **Option** for null safety, **Either** for detailed error handling
- **Pattern matching** for type-based branching
- **Collection API** (`map`, `filter`, `groupBy`) for declarative data processing
- **Type classes** for extensible functionality implementation
{{< /callout >}}

**Target Audience**: Developers who have completed Scala environment setup and want to learn Scala through practical code

**Prerequisites**:
- Completed Scala development environment setup (see [Environment Setup]({{< relref "/docs/scala/examples/setup" >}}))
- Basic object-oriented programming concepts
- Functional programming basics (recommended)

---

Comprehensive examples utilizing core Scala concepts. This document demonstrates commonly used features in real-world scenarios with example code, including case classes, pattern matching, collection processing, Option, Either, and type classes.

> 💻 **Run Online:** All examples below can be copied and run directly at [Scastie](https://scastie.scala-lang.org/).
> Select Scala 3 and paste the code!

#### Example 1: Data Modeling

Define domain models using case classes. Case classes are ideal for representing immutable data, with equals, hashCode, toString, and copy methods auto-generated. Let's model Product, OrderLine, and Order commonly seen in e-commerce domains.

```scala
// Domain models
case class Product(id: Int, name: String, price: Double)
case class OrderLine(product: Product, quantity: Int)
case class Order(id: Int, customer: String, lines: List[OrderLine])

// Factory method
object Order:
  def create(id: Int, customer: String, lines: List[OrderLine]): Option[Order] =
    if lines.isEmpty then None
    else Some(Order(id, customer, lines))

// Usage
val laptop = Product(1, "Laptop", 1500000)
val mouse = Product(2, "Mouse", 50000)

val order = Order.create(
  1,
  "John Smith",
  List(
    OrderLine(laptop, 1),
    OrderLine(mouse, 2)
  )
)
```

In the code above, the Order.create factory method returns None if the order lines are empty, and Some(Order(...)) otherwise. This prevents empty orders from being created at compile time.

{{< callout type="tip" title="Key Points" >}}
- **Case classes**: Ideal for immutable data representation, auto-generate `equals`, `hashCode`, `toString`, `copy`
- **Factory method**: Forces validation during object creation to prevent invalid states
- **Option return**: Express "value may be absent" through type instead of null
{{< /callout >}}

#### Example 2: Order Processing

Implement business logic with pattern matching and higher-order functions. Using Scala 3's extension keyword, you can add new methods to existing classes without modifying them.

```scala
// Order extension methods
extension (order: Order)
  def totalPrice: Double =
    order.lines.map(line => line.product.price * line.quantity).sum

  def itemCount: Int =
    order.lines.map(_.quantity).sum

  def hasProduct(productId: Int): Boolean =
    order.lines.exists(_.product.id == productId)

  def applyDiscount(rate: Double): Order =
    order.copy(
      lines = order.lines.map { line =>
        line.copy(
          product = line.product.copy(
            price = line.product.price * (1 - rate)
          )
        )
      }
    )

// Usage
order.foreach { o =>
  println(s"Total: ${o.totalPrice}")        // 1,600,000
  println(s"Item count: ${o.itemCount}")    // 3
  println(s"Has laptop: ${o.hasProduct(1)}") // true

  val discounted = o.applyDiscount(0.1)
  println(s"After discount: ${discounted.totalPrice}")  // 1,440,000
}
```

The totalPrice method calculates the sum of each order line's price multiplied by quantity. The applyDiscount method uses nested copy methods to apply discounts while maintaining immutability.

{{< callout type="tip" title="Key Points" >}}
- **extension**: Add new methods without modifying existing classes (Scala 3)
- **copy**: Create new object with some fields changed from immutable object
- **Higher-order functions**: Process collections declaratively with `map`, `sum`, `exists`
{{< /callout >}}

#### Example 3: Error Handling

Safe error handling using Either. Either represents success (Right) and failure (Left), allowing more detailed error information than Option. Useful for validation logic to explain why failure occurred.

```scala
// Error types
enum ValidationError:
  case EmptyName
  case InvalidPrice(price: Double)
  case InvalidQuantity(qty: Int)

import ValidationError.*

// Validation functions
def validateProduct(name: String, price: Double): Either[ValidationError, Product] =
  if name.isEmpty then Left(EmptyName)
  else if price <= 0 then Left(InvalidPrice(price))
  else Right(Product(0, name, price))

def validateOrderLine(
  product: Product,
  quantity: Int
): Either[ValidationError, OrderLine] =
  if quantity <= 0 then Left(InvalidQuantity(quantity))
  else Right(OrderLine(product, quantity))

// Composition
def createOrderLine(
  name: String,
  price: Double,
  quantity: Int
): Either[ValidationError, OrderLine] =
  for
    product <- validateProduct(name, price)
    line <- validateOrderLine(product, quantity)
  yield line

// Usage
createOrderLine("Laptop", 1500000, 1) match
  case Right(line) => println(s"Order line: $line")
  case Left(EmptyName) => println("Product name is empty")
  case Left(InvalidPrice(p)) => println(s"Invalid price: $p")
  case Left(InvalidQuantity(q)) => println(s"Invalid quantity: $q")
```

Using for comprehension, you can chain Either values sequentially. If the first validation fails, it immediately returns Left. If all validations pass, the final result is wrapped in Right.

{{< callout type="tip" title="Key Points" >}}
- **Either[L, R]**: `Left` for failure, `Right` for success (convention)
- **enum**: Define error types as enumeration for type safety
- **for comprehension**: Chain multiple Eithers sequentially, stop at first failure
- **Pattern matching**: Explicitly handle all error cases
{{< /callout >}}

#### Example 4: Collection Processing

Process data in functional style. Scala collections provide rich methods like map, filter, groupBy, sortBy. Using these declarative methods instead of imperative loops makes code concise and readable.

```scala
// Sample data
val products = List(
  Product(1, "Laptop", 1500000),
  Product(2, "Mouse", 50000),
  Product(3, "Keyboard", 150000),
  Product(4, "Monitor", 500000),
  Product(5, "Speaker", 200000)
)

// Filtering
val expensive = products.filter(_.price >= 200000)
println(s"Expensive products: ${expensive.map(_.name)}")
// List(Laptop, Monitor)

// Transformation
val priceList = products.map(p => s"${p.name}: ${p.price}")
println(priceList.mkString("\n"))

// Grouping
val byPriceRange = products.groupBy { p =>
  if p.price < 100000 then "Budget"
  else if p.price < 500000 then "Mid-range"
  else "Premium"
}
println(s"By price range: $byPriceRange")

// Aggregation
val totalValue = products.map(_.price).sum
val avgPrice = products.map(_.price).sum / products.length
val maxPrice = products.maxBy(_.price)

println(s"Total value: ${totalValue}")
println(s"Average price: ${avgPrice}")
println(s"Most expensive: ${maxPrice.name}")

// Sorting
val sortedByPrice = products.sortBy(_.price)
val sortedByName = products.sortBy(_.name)
```

filter extracts only elements meeting the condition, map transforms each element. groupBy groups elements by key function and returns a Map. sortBy sorts by the given function's result.

{{< callout type="tip" title="Key Points" >}}
- **filter**: Extract only elements meeting condition
- **map**: Transform each element to create new collection
- **groupBy**: Group by key function → returns `Map[K, List[V]]`
- **sortBy**: Specify sort criteria, **maxBy/minBy**: Extract max/min element
{{< /callout >}}

#### Example 5: Using Option

Use Option instead of null. Option represents situations where a value may or may not exist through type. Prevents NullPointerException at compile time, enabling safer code.

```scala
// Repository
object ProductRepository:
  private val products = Map(
    1 -> Product(1, "Laptop", 1500000),
    2 -> Product(2, "Mouse", 50000)
  )

  def findById(id: Int): Option[Product] = products.get(id)

  def findByName(name: String): Option[Product] =
    products.values.find(_.name.contains(name))

// Usage
ProductRepository.findById(1) match
  case Some(product) => println(s"Found: $product")
  case None => println("Product not found")

// Chaining
val price = ProductRepository
  .findById(1)
  .map(_.price)
  .getOrElse(0.0)

// for comprehension
val orderTotal = for
  laptop <- ProductRepository.findById(1)
  mouse <- ProductRepository.findById(2)
yield laptop.price + mouse.price

println(s"Order total: ${orderTotal.getOrElse(0.0)}")
```

Applying map to Option performs transformation only when value exists. Using for comprehension, you can combine multiple Option values; if any is None, the entire result becomes None.

{{< callout type="tip" title="Key Points" >}}
- **Option[T]**: Express value presence with `Some(value)` or `None`
- **getOrElse**: Return default value when None
- **map/flatMap**: Perform transformation only when value exists
- **for comprehension**: Combine multiple Options, entire becomes None if any is None
{{< /callout >}}

#### Example 6: Type Classes

Implement extensible functionality with type classes. Type classes are a pattern for adding new functionality to existing types without modification. Using JSON encoder as example to show type class usage.

```scala
// JSON encoder type class
trait JsonEncoder[A]:
  def encode(a: A): String

object JsonEncoder:
  given JsonEncoder[String] with
    def encode(s: String): String = s"\"$s\""

  given JsonEncoder[Int] with
    def encode(i: Int): String = i.toString

  given JsonEncoder[Double] with
    def encode(d: Double): String = d.toString

  given JsonEncoder[Product] with
    def encode(p: Product): String =
      s"""{"id":${p.id},"name":"${p.name}","price":${p.price}}"""

  given [A](using e: JsonEncoder[A]): JsonEncoder[List[A]] with
    def encode(list: List[A]): String =
      list.map(e.encode).mkString("[", ",", "]")

// Extension method
extension [A](a: A)(using e: JsonEncoder[A])
  def toJson: String = e.encode(a)

// Usage
val laptop = Product(1, "Laptop", 1500000)
println(laptop.toJson)
// {"id":1,"name":"Laptop","price":1500000.0}

val products = List(
  Product(1, "Laptop", 1500000),
  Product(2, "Mouse", 50000)
)
println(products.toJson)
// [{"id":1,"name":"Laptop","price":1500000.0},{"id":2,"name":"Mouse","price":50000.0}]
```

Define type class instances with given keyword. The List encoder is automatically generated if the element type has an encoder. Through extension, add toJson method to all encodable types.

{{< callout type="tip" title="Key Points" >}}
- **Type classes**: Pattern for adding functionality without modifying existing types
- **trait**: Define type class interface
- **given**: Define type class instance (Scala 3)
- **extension + using**: Conditional extension methods (add `toJson` only to types with encoder)
{{< /callout >}}

#### Run Example Project

To run the examples locally, execute sbt run in the example project directory.

```bash
cd examples/scala/scala3-basics
sbt run
```

#### Example 7: Real-world Scenario - REST API Response Handling

Pattern for processing actual API responses. HTTP responses can succeed or fail, so we'll create utilities to handle this safely.

```scala
import scala.util.{Try, Success, Failure}

// API response model
case class ApiResponse[T](
  status: Int,
  data: Option[T],
  error: Option[String]
)

// User domain
case class User(id: Long, name: String, email: String)

// API client simulation
object UserApiClient:
  def fetchUser(id: Long): ApiResponse[User] =
    if id > 0 then
      ApiResponse(200, Some(User(id, s"User$id", s"user$id@example.com")), None)
    else
      ApiResponse(404, None, Some("User not found"))

  def fetchUsers(ids: List[Long]): List[ApiResponse[User]] =
    ids.map(fetchUser)

// Response handling utilities
object ApiResponseHandler:
  extension [T](response: ApiResponse[T])
    def toEither: Either[String, T] =
      response match
        case ApiResponse(status, Some(data), _) if status < 400 => Right(data)
        case ApiResponse(_, _, Some(error)) => Left(error)
        case _ => Left("Unknown error")

    def toOption: Option[T] = response.data.filter(_ => response.status < 400)

// Usage example
import ApiResponseHandler.*

val response = UserApiClient.fetchUser(1)
val userOrError = response.toEither

userOrError match
  case Right(user) => println(s"Welcome, ${user.name}!")
  case Left(error) => println(s"Error: $error")

// Process multiple users
val userIds = List(1L, 2L, -1L, 3L)
val results = UserApiClient.fetchUsers(userIds)
  .map(_.toEither)
  .collect { case Right(user) => user }

println(s"Successfully fetched users: ${results.length}")
```

We defined extension methods to convert ApiResponse to Either or Option. The collect method uses a partial function to extract only Right cases.

{{< callout type="tip" title="Key Points" >}}
- **API response wrapper**: Represent status code, data, error message in single type
- **Extension methods**: Convert response to `Either` or `Option` for consistent error handling
- **collect**: Extract only elements matching specific pattern to create new collection
{{< /callout >}}

#### Example 8: Real-world Scenario - Configuration Management

Pattern for type-safe environment-specific configuration management. Define environments with enum and return appropriate configuration for each through pattern matching.

```scala
// Configuration ADT
enum Environment:
  case Development, Staging, Production

case class DatabaseConfig(
  host: String,
  port: Int,
  database: String,
  maxConnections: Int
)

case class AppConfig(
  environment: Environment,
  database: DatabaseConfig,
  debug: Boolean
)

object AppConfig:
  import Environment.*

  def load(env: Environment): AppConfig = env match
    case Development =>
      AppConfig(
        environment = Development,
        database = DatabaseConfig("localhost", 5432, "dev_db", 5),
        debug = true
      )
    case Staging =>
      AppConfig(
        environment = Staging,
        database = DatabaseConfig("staging.db.internal", 5432, "staging_db", 20),
        debug = true
      )
    case Production =>
      AppConfig(
        environment = Production,
        database = DatabaseConfig("prod.db.internal", 5432, "prod_db", 100),
        debug = false
      )

  def fromString(envStr: String): Either[String, AppConfig] =
    envStr.toLowerCase match
      case "dev" | "development" => Right(load(Development))
      case "staging" => Right(load(Staging))
      case "prod" | "production" => Right(load(Production))
      case _ => Left(s"Unknown environment: $envStr")

// Usage example
val config = AppConfig.fromString("production")

config match
  case Right(cfg) =>
    println(s"Environment: ${cfg.environment}")
    println(s"DB Host: ${cfg.database.host}")
    println(s"Debug mode: ${cfg.debug}")
  case Left(error) =>
    println(s"Failed to load config: $error")
```

Enumerate possible environments with Environment enum and return appropriate configuration for each through pattern matching. The fromString method accepts string input and returns Either, safely handling invalid environment strings.

{{< callout type="tip" title="Key Points" >}}
- **enum**: Enumerate possible values as types for compile-time safety
- **Pattern matching**: Clearly branch configuration for each environment
- **Either return**: Safely handle invalid input, can pass error messages
{{< /callout >}}

#### Exercises

Improve your Scala skills by implementing the following exercises yourself.

1. **Add Inventory Management** ⭐: Add a `stock` field to `Product` and implement stock checking logic. Prevent order creation when stock is insufficient.

2. **Order Status** ⭐⭐: Add status (PENDING, CONFIRMED, SHIPPED) to `Order`. Define with enum and implement status transition logic.

3. **Search Feature** ⭐⭐: Implement a function to search products by price range and name. Create search functionality that can combine multiple conditions.

> 💡 Practice exercise solutions by implementing and testing directly at [Scastie](https://scastie.scala-lang.org/)!

#### Next Steps

After learning basic examples, compare Scala 2 and 3 syntax differences.

- [Scala 2 vs 3 Comparison]({{< relref "/docs/scala/examples/scala2-vs-scala3" >}}) — Version-specific code comparison
