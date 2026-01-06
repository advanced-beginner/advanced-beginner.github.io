---
lastmod: "2026-01-06"
title: References
weight: 4
---

Curated reference materials for learning Scala.

## Official Documentation

### Scala Official

- [Scala Official Site](https://www.scala-lang.org/) — Latest news, downloads
- [Scala 3 Documentation](https://docs.scala-lang.org/scala3/) — Official Scala 3 docs
- [Scala 2 Documentation](https://docs.scala-lang.org/) — Official Scala 2 docs
- [Scala API Docs](https://www.scala-lang.org/api/current/) — Standard library API

### Learning Resources

- [Tour of Scala](https://docs.scala-lang.org/tour/tour-of-scala.html) — Quick overview of core features
- [Scala 3 Book](https://docs.scala-lang.org/scala3/book/introduction.html) — Step-by-step learning guide
- [Scala Exercises](https://www.scala-exercises.org/) — Interactive learning

### Build Tools

- [sbt Documentation](https://www.scala-sbt.org/1.x/docs/) — Official sbt docs
- [Mill Build Tool](https://mill-build.com/mill/Intro_to_Mill.html) — Mill documentation

## Books

### Introductory

- **"Programming in Scala" (4th Edition)**
  - Authors: Martin Odersky, Lex Spoon, Bill Venners
  - Authoritative book written by Scala's creator

- **"Scala for the Impatient" (3rd Edition)**
  - Author: Cay S. Horstmann
  - Recommended for those who want to learn Scala quickly

### Functional Programming

- **"Functional Programming in Scala" (2nd Edition)**
  - Authors: Michael Pilquist, Rúnar Bjarnason, Paul Chiusano
  - Known as "the red book", deep dive into FP

- **"Essential Scala"**
  - Authors: Noel Welsh, Dave Gurnell
  - [Free online version](https://underscore.io/books/essential-scala/)

### Advanced

- **"Scala with Cats"**
  - Authors: Noel Welsh, Dave Gurnell
  - [Free online version](https://underscore.io/books/scala-with-cats/)

- **"Practical FP in Scala"**
  - Author: Gabriel Volpe
  - Real-world functional programming

## Library Samples

A brief introduction to how concepts from this guide are used in real-world libraries.

### Cats: Type Classes in Practice

See how patterns from [Type Classes](../concepts/type-classes/) are used in Cats.

```scala
// Type class pattern learned in this guide
trait Monoid[A]:
  def empty: A
  def combine(x: A, y: A): A

// Used like this in Cats
import cats.syntax.all.*
import cats.Monoid

// Use pre-defined instances
List(1, 2, 3).combineAll  // 6 (uses Monoid[Int])
List("a", "b").combineAll // "ab" (uses Monoid[String])

// Apply to custom types
case class Order(total: Int, items: Int)

given Monoid[Order] with
  def empty = Order(0, 0)
  def combine(x: Order, y: Order) =
    Order(x.total + y.total, x.items + y.items)

List(Order(100, 2), Order(200, 3)).combineAll
// Order(300, 5)
```

**Related concepts:** [Type Classes](../concepts/type-classes/), [Implicits/Given](../concepts/implicits/)

### ZIO: Functional Effect System

[Higher-Order Functions](../concepts/higher-order-functions/) and [For Comprehensions](../concepts/for-comprehensions/) shine in ZIO.

```scala
import zio.*

// Represent side effects as values
val readLine: ZIO[Any, IOException, String] = Console.readLine
val printLine: String => ZIO[Any, IOException, Unit] = Console.printLine

// Sequential execution with for comprehension
val program: ZIO[Any, IOException, Unit] = for
  _    <- printLine("Enter your name:")
  name <- readLine
  _    <- printLine(s"Hello, $name!")
yield ()

// Nothing executes at this point (pure value)
// Unsafe.unsafe { implicit u => Runtime.default.unsafe.run(program) }
```

**Related concepts:** [For Comprehensions](../concepts/for-comprehensions/), [Functional Patterns](../concepts/functional-patterns/)

### http4s: Functional HTTP

[Pattern Matching](../concepts/pattern-matching/) and [Case Classes](../concepts/case-classes/) are used for routing.

```scala
import org.http4s.*
import org.http4s.dsl.io.*

// HTTP routing with pattern matching
val routes = HttpRoutes.of[IO] {
  case GET -> Root / "users" / IntVar(id) =>
    Ok(s"User $id")

  case req @ POST -> Root / "users" =>
    req.as[User].flatMap(user => Created(user.asJson))

  case GET -> Root / "health" =>
    Ok("healthy")
}
```

**Related concepts:** [Pattern Matching](../concepts/pattern-matching/), [Case Classes](../concepts/case-classes/)

### Circe: JSON Type Classes

[Type Classes](../concepts/type-classes/) and [Generics](../concepts/generics/) are used for JSON conversion.

```scala
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*

// Just define case class for automatic JSON conversion
case class User(name: String, age: Int)

val user = User("Alice", 30)
user.asJson.noSpaces  // {"name":"Alice","age":30}

// Parsing also uses type classes
"""{"name":"Bob","age":25}""".as[User]
// Right(User("Bob", 25))
```

**Related concepts:** [Case Classes](../concepts/case-classes/), [Type Classes](../concepts/type-classes/)

### Next Learning Directions

| Interest Area | Recommended Library | Reference |
|---------------|---------------------|-----------|
| FP Basics | Cats | [Scala with Cats (free)](https://underscore.io/books/scala-with-cats/) |
| Async/Concurrency | ZIO or Cats Effect | [ZIO Docs](https://zio.dev/) |
| Web Development | http4s + Circe | [http4s Tutorial](https://http4s.org/v0.23/docs/) |
| Data Processing | Spark | [Spark Scala API](https://spark.apache.org/docs/latest/api/scala/) |

---

## Online Courses

### Free

- [Scala & Functional Programming Essentials](https://rockthejvm.com/) — Rock the JVM
- [Functional Programming Principles in Scala](https://www.coursera.org/learn/scala-functional-programming) — Coursera (Martin Odersky)
- [Scala 3 New Features](https://docs.scala-lang.org/scala3/new-in-scala3.html) — Official docs

### Paid

- [Rock the JVM](https://rockthejvm.com/) — Comprehensive Scala courses
- [Zionomicon](https://www.zionomicon.com/) — Deep dive into ZIO

## Library Documentation

### Functional Programming

- [Cats](https://typelevel.org/cats/) — Type class library
- [ZIO](https://zio.dev/) — Effect system
- [Cats Effect](https://typelevel.org/cats-effect/) — Async/concurrency

### Web Development

- [Play Framework](https://www.playframework.com/documentation/latest/Home) — Web framework
- [http4s](https://http4s.org/) — Functional HTTP
- [Akka HTTP](https://doc.akka.io/docs/akka-http/current/) — Actor-based HTTP

### Data Processing

- [Apache Spark](https://spark.apache.org/docs/latest/api/scala/) — Distributed data processing
- [Apache Kafka](https://kafka.apache.org/documentation/) — Streaming platform
- [Doobie](https://tpolecat.github.io/doobie/) — Functional JDBC

### JSON

- [Circe](https://circe.github.io/circe/) — JSON library
- [Play JSON](https://github.com/playframework/play-json) — Play JSON
- [uPickle](https://com-lihaoyi.github.io/upickle/) — Lightweight JSON

### Testing

- [ScalaTest](https://www.scalatest.org/) — Testing framework
- [MUnit](https://scalameta.org/munit/) — Lightweight testing
- [ScalaCheck](https://scalacheck.org/) — Property-based testing

## Community

### Forums/Discord

- [Scala Users Forum](https://users.scala-lang.org/) — Official forum
- [Scala Discord](https://discord.gg/scala) — Real-time chat
- [Typelevel Discord](https://discord.gg/XF3CXcMzqD) — Cats, fs2, etc.

### Q&A

- [Stack Overflow - Scala](https://stackoverflow.com/questions/tagged/scala)
- [Reddit - r/scala](https://www.reddit.com/r/scala/)

### Blogs/News

- [Scala Times](https://scalatimes.com/) — Weekly newsletter
- [Typelevel Blog](https://typelevel.org/blog/) — Functional Scala
- [Li Haoyi's Blog](https://www.lihaoyi.com/) — Scala tips

## Tools

### IDE

- [IntelliJ IDEA Scala Plugin](https://plugins.jetbrains.com/plugin/1347-scala)
- [Metals (VS Code)](https://scalameta.org/metals/)

### Utilities

- [Scastie](https://scastie.scala-lang.org/) — Online Scala execution - **All examples in this guide can be run here!**
- [Scaladex](https://index.scala-lang.org/) — Library search
- [Scalafmt](https://scalameta.org/scalafmt/) — Code formatter
- [Scalafix](https://scalacenter.github.io/scalafix/) — Refactoring tool

## Conferences

- [Scala Days](https://scaladays.org/) — Major Scala conference
- [Scala.io](https://scala.io/) — European Scala conference
- [Typelevel Summit](https://typelevel.org/event/) — Functional Scala

## Recommended Learning Paths

### Beginners

1. Set up environment with Quick Start
2. Tour of Scala for core concepts
3. "Scala for the Impatient" or Coursera course
4. Simple project practice

### Intermediate

1. Read "Programming in Scala" thoroughly
2. Learn Cats or ZIO
3. Apply functional style to real projects

### Advanced

1. "Functional Programming in Scala"
2. "Scala with Cats"
3. Deep dive into type system (Shapeless, Type-level programming)
4. Contribute to open source
