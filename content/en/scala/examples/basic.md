---
lastmod: "2026-01-06"
title: Basic Examples
weight: 2
---

Comprehensive examples utilizing core Scala concepts.

> **Run Online:** All examples below can be copied and run directly at [Scastie](https://scastie.scala-lang.org/).
> Select Scala 3 and paste the code!

## Example 1: Data Modeling

Define domain models using case classes.

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
val laptop = Product(1, "Laptop", 1500.00)
val mouse = Product(2, "Mouse", 50.00)

val order = Order.create(
  1,
  "John Smith",
  List(
    OrderLine(laptop, 1),
    OrderLine(mouse, 2)
  )
)
```

## Example 2: Order Processing

Implement business logic with pattern matching and higher-order functions.

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
  println(s"Total: $$${o.totalPrice}")       // $1,600.00
  println(s"Item count: ${o.itemCount}")     // 3
  println(s"Has laptop: ${o.hasProduct(1)}") // true

  val discounted = o.applyDiscount(0.1)
  println(s"After discount: $$${discounted.totalPrice}")  // $1,440.00
}
```

## Example 3: Error Handling

Safe error handling using Either.

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
createOrderLine("Laptop", 1500.00, 1) match
  case Right(line) => println(s"Order line: $line")
  case Left(EmptyName) => println("Product name is empty")
  case Left(InvalidPrice(p)) => println(s"Invalid price: $p")
  case Left(InvalidQuantity(q)) => println(s"Invalid quantity: $q")
```

## Example 4: Collection Processing

Process data in functional style.

```scala
// Sample data
val products = List(
  Product(1, "Laptop", 1500.00),
  Product(2, "Mouse", 50.00),
  Product(3, "Keyboard", 150.00),
  Product(4, "Monitor", 500.00),
  Product(5, "Speaker", 200.00)
)

// Filtering
val expensive = products.filter(_.price >= 200.00)
println(s"Expensive products: ${expensive.map(_.name)}")
// List(Laptop, Monitor)

// Transformation
val priceList = products.map(p => s"${p.name}: $$${p.price}")
println(priceList.mkString("\n"))

// Grouping
val byPriceRange = products.groupBy { p =>
  if p.price < 100 then "Budget"
  else if p.price < 500 then "Mid-range"
  else "Premium"
}
println(s"By price range: $byPriceRange")

// Aggregation
val totalValue = products.map(_.price).sum
val avgPrice = products.map(_.price).sum / products.length
val maxPrice = products.maxBy(_.price)

println(s"Total value: $$${totalValue}")
println(s"Average price: $$${avgPrice}")
println(s"Most expensive: ${maxPrice.name}")

// Sorting
val sortedByPrice = products.sortBy(_.price)
val sortedByName = products.sortBy(_.name)
```

## Example 5: Using Option

Use Option instead of null.

```scala
// Repository
object ProductRepository:
  private val products = Map(
    1 -> Product(1, "Laptop", 1500.00),
    2 -> Product(2, "Mouse", 50.00)
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

println(s"Order total: $$${orderTotal.getOrElse(0.0)}")
```

## Example 6: Type Classes

Implement extensible functionality with type classes.

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
val laptop = Product(1, "Laptop", 1500.00)
println(laptop.toJson)
// {"id":1,"name":"Laptop","price":1500.0}

val products = List(
  Product(1, "Laptop", 1500.00),
  Product(2, "Mouse", 50.00)
)
println(products.toJson)
// [{"id":1,"name":"Laptop","price":1500.0},{"id":2,"name":"Mouse","price":50.0}]
```

## Run Example Project

```bash
cd examples/scala/scala3-basics
sbt run
```

## Example 7: Real-world Scenario - REST API Response Handling

Pattern for processing actual API responses.

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

## Example 8: Real-world Scenario - Configuration Management

Pattern for type-safe environment-specific configuration.

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

## Exercises

1. **Add Inventory Management**: Add a `stock` field to `Product` and implement stock checking logic.

2. **Order Status**: Add status (PENDING, CONFIRMED, SHIPPED) to `Order`.

3. **Search Feature**: Implement a function to search products by price range and name.

> Try implementing these exercises at [Scastie](https://scastie.scala-lang.org/)!

## Next Steps

- [Scala 2 vs 3 Comparison](../scala2-vs-scala3/) — Code comparison by version
