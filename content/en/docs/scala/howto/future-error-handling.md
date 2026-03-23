---
lastmod: "2026-01-16"
title: Future Error Handling
description: "How to diagnose and safely handle Future errors"
weight: 2
---

Learn how to safely handle and debug exceptions in asynchronous code.

**Estimated time**: 15-20 minutes

{{< callout type="tip" title="TL;DR" >}}
- `recover`/`recoverWith`: Handle specific exceptions and return fallback values
- `transform`: Transform both success and failure cases
- `onComplete`: Use only for side effects (logging, etc.)
- **Never do this**: Use `Await.result` to throw exceptions synchronously
{{< /callout >}}

---

## What This Guide Solves

Use this guide when:

- Exceptions in Future silently disappear
- You don't know how to handle errors in asynchronous code
- You need to decide between `recover`, `recoverWith`, and `transform`

### Symptoms

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

val future = Future {
  throw new RuntimeException("Something went wrong")
}
// Program exits normally, but where did the exception go?
```

### What This Guide Does NOT Cover

- **Basic Future concepts**: See [Concurrency Concepts](../concepts/concurrency/)
- **Cats Effect IO / ZIO error handling**: Refer to respective library documentation
- **Akka actor system error handling**: Refer to Akka documentation

---

## Before You Begin

Ensure you have the following environment ready:

| Item | Requirement | How to Check |
|------|-------------|--------------|
| Scala version | 2.13.x or 3.x | `scala -version` |
| Build tool | sbt 1.x or Gradle 8.x | `sbt --version` |
| Dependencies | scala-library (included by default) | - |

### Required Imports

All examples require these imports:

```scala
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}
```

---

## Step 1: Understanding Future Failures

### 1.1 Two States of Future

```scala
val successFuture: Future[Int] = Future.successful(42)
val failedFuture: Future[Int] = Future.failed(new Exception("Error"))

successFuture.value  // Some(Success(42))
failedFuture.value   // Some(Failure(java.lang.Exception: Error))
```

### 1.2 Why Exceptions Disappear

Future runs asynchronously, so if the main thread exits first, there's no opportunity to observe the exception:

```scala
val future = Future {
  Thread.sleep(100)
  throw new RuntimeException("Error")
}

// If main thread exits immediately, exception is never printed
println("Main thread finished")
```

**Solution**: Register appropriate error handlers, or use `Await` in tests.

---

## Step 2: Choose an Error Handling Pattern

Refer to this decision guide:

| Situation | Method to Use |
|-----------|---------------|
| Want to replace exception with a default value | `recover` |
| Want to run another Future on exception | `recoverWith` |
| Want to transform both success and failure to different types | `transform` |
| Want to log only while passing result through | `andThen` |

### 2.1 recover - Transform Exception to Value

Handle specific exceptions and return default values:

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.IOException

// Exception definitions
class UserNotFoundException(msg: String) extends Exception(msg)
class DatabaseException(msg: String) extends Exception(msg)

// Usage example
def fetchUserAge(userId: String): Future[Int] = {
  // Actually queries DB
  Future {
    if (userId == "unknown") throw new UserNotFoundException(s"User $userId not found")
    30 // Normal return
  }.recover {
    case _: UserNotFoundException => 0  // Default value
    case e: DatabaseException =>
      println(s"DB error for user $userId: ${e.getMessage}")
      -1  // Error indicator value
  }
}

// Test
fetchUserAge("alice").foreach(age => println(s"Age: $age"))  // Age: 30
fetchUserAge("unknown").foreach(age => println(s"Age: $age")) // Age: 0
```

### 2.2 recoverWith - Transform Exception to Another Future

Execute a fallback Future on failure:

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.TimeoutException

case class Data(value: String)

def fetchFromPrimary(): Future[Data] = Future {
  // Simulation: Timeout occurs
  throw new TimeoutException("Primary server timeout")
}

def fetchFromBackup(): Future[Data] = Future {
  Data("backup data")
}

val result: Future[Data] = fetchFromPrimary().recoverWith {
  case _: TimeoutException =>
    println("Primary timed out, trying backup...")
    fetchFromBackup()
}

result.foreach(d => println(s"Got: ${d.value}"))
// Output:
// Primary timed out, trying backup...
// Got: backup data
```

### 2.3 transform - Transform Both Success and Failure

Use when you need to handle both success and failure:

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

def riskyOperation(): Future[Int] = Future {
  if (scala.util.Random.nextBoolean()) 42
  else throw new RuntimeException("Random failure")
}

val future: Future[String] = riskyOperation().transform {
  case Success(value) =>
    Success(s"Got: $value")
  case Failure(e) =>
    Success(s"Failed: ${e.getMessage}")  // Transform failure to success
}

future.foreach(println)  // "Got: 42" or "Failed: Random failure"
```

---

## Step 3: Logging and Monitoring

### 3.1 onComplete - For Side Effects Only

{{< callout type="warning" title="Warning" >}}
`onComplete` returns `Unit`. Do not use it for business logic.
{{< /callout >}}

Use only for logging or metrics:

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

def processOrder(orderId: String): Future[String] = Future {
  s"Order $orderId processed"
}

val future = processOrder("ORD-123")

future.onComplete {
  case Success(result) =>
    println(s"[INFO] Order processed: $result")
    // metrics.incrementCounter("orders.success")
  case Failure(e) =>
    println(s"[ERROR] Order failed: ${e.getMessage}")
    // metrics.incrementCounter("orders.failure")
}

// Use separate map/recover to return results
```

### 3.2 andThen - Chainable Side Effects

Similar to `onComplete` but returns the original Future:

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

def processOrder(orderId: String): Future[String] = Future {
  s"Order $orderId processed"
}

def transformResult(s: String): String = s.toUpperCase

val result = processOrder("ORD-123")
  .andThen {
    case Success(_) => println("[INFO] Order started")
  }
  .map(transformResult)
  .andThen {
    case Success(r) => println(s"[INFO] Order completed: $r")
    case Failure(e) => println(s"[ERROR] Order failed: ${e.getMessage}")
  }

result.foreach(println)
// Output:
// [INFO] Order started
// [INFO] Order completed: ORDER ORD-123 PROCESSED
// ORDER ORD-123 PROCESSED
```

---

## Step 4: Common Mistakes and Solutions

### 4.1 Await Abuse

{{< callout type="error" title="Danger" >}}
`Await.result` in production code blocks threads and severely degrades performance.
Use only in test code.
{{< /callout >}}

```scala
import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

// Wrong: Blocking for exception handling
def badExample(): Unit = {
  val future = Future { throw new RuntimeException("Error") }
  try {
    val result = Await.result(future, 5.seconds)  // Blocking!
  } catch {
    case e: Exception => println(s"Error: ${e.getMessage}")
  }
}

// Correct: Asynchronous handling
def goodExample(): Future[Int] = {
  Future { throw new RuntimeException("Error") }
    .recover {
      case e: Exception =>
        println(s"Error: ${e.getMessage}")
        -1  // Return default value
    }
}
```

### 4.2 Swallowing Exceptions

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

// Wrong: Ignoring exceptions
val future = Future { throw new RuntimeException("Error") }
future.onComplete {
  case Failure(_) => // Do nothing - Dangerous!
  case Success(v) => println(v)
}

// Correct: At least log
future.onComplete {
  case Failure(e) => println(s"[ERROR] Unexpected error: ${e.getMessage}")
  case Success(v) => println(v)
}
```

### 4.3 Nested Future

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class User(id: String, name: String)
case class Order(id: String, userId: String)

def fetchUser(id: String): Future[User] = Future { User(id, "Alice") }
def fetchOrders(user: User): Future[List[Order]] = Future {
  List(Order("1", user.id))
}

// Wrong: Future[Future[T]]
val nested: Future[Future[List[Order]]] = fetchUser("123").map(fetchOrders)

// Correct: Use flatMap
val flat: Future[List[Order]] = fetchUser("123").flatMap(fetchOrders)
```

---

## Step 5: Error Handling with Multiple Futures

### 5.1 for comprehension

If any one fails, the entire computation fails:

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class User(id: String, name: String)
case class Order(id: String)
case class Payment(id: String)

def fetchUser(userId: String): Future[User] = Future { User(userId, "Alice") }
def fetchOrders(userId: String): Future[List[Order]] = Future { List(Order("1")) }
def fetchPayments(userId: String): Future[List[Payment]] = Future { List(Payment("p1")) }

val defaultUser = User("default", "Guest")

val result = for {
  user <- fetchUser("user-123")
  orders <- fetchOrders(user.id)
  payments <- fetchPayments(user.id)
} yield (user, orders, payments)

result.recover {
  case e: NoSuchElementException => (defaultUser, Nil, Nil)
  case e: Exception =>
    println(s"[ERROR] Failed to fetch data: ${e.getMessage}")
    throw e  // Re-throw
}
```

### 5.2 Future.sequence Error Handling

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

val futures: List[Future[Int]] = List(
  Future.successful(1),
  Future.failed(new Exception("Error")),
  Future.successful(3)
)

// If any fails, entire sequence fails
Future.sequence(futures).recover {
  case e =>
    println(s"Sequence failed: ${e.getMessage}")
    List.empty  // Return empty list on error
}

// To allow individual failures
val recovered: List[Future[Int]] = futures.map(_.recover { case _ => -1 })
Future.sequence(recovered).foreach(println)  // List(1, -1, 3)
```

### 5.3 firstCompletedOf - Use the Fastest Success

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

def fetchFromServer1(): Future[String] = Future {
  Thread.sleep(100)
  "Server 1 response"
}

def fetchFromServer2(): Future[String] = Future {
  Thread.sleep(50)
  "Server 2 response"
}

val fastest = Future.firstCompletedOf(List(
  fetchFromServer1(),
  fetchFromServer2()
))

fastest.foreach(println)  // "Server 2 response" (faster response)
```

---

## Checklist

When handling Future errors, verify the following:

- [ ] **Do all Futures have error handlers?** - `recover` or `recoverWith`
- [ ] **Are exceptions being logged?** - `onComplete` or `andThen`
- [ ] **Are you avoiding Await?** - Avoid outside of test code
- [ ] **Are there no nested Futures?** - Use `flatMap`
- [ ] **Are timeouts configured?** - Required for external calls

**If exceptions still disappear after checking all items**, add debugging logs with `onComplete`.

---

## Related Documentation

- [Concurrency](../concepts/concurrency/) - Future and Promise basics
- [Implicit/Given Debugging](implicit-debugging/) - ExecutionContext troubleshooting
- [Functional Patterns](../concepts/functional-patterns/) - Monadic error handling
