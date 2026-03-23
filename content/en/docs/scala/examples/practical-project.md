---
lastmod: "2026-01-14"
title: Practical Projects
description: "Step-by-step practical Scala project implementation"
weight: 5
---

{{< callout type="info" title="TL;DR" >}}
- **REST API Server**: Build functional web server with http4s + Circe + Cats Effect
- **Data Pipeline**: Memory-efficient stream processing with FS2
- **CLI Tool**: Type-safe command-line parser with scopt
- **Error Handling**: Collect multiple validation errors simultaneously with Cats Validated
- All examples emphasize functional style with immutability, referential transparency, and type safety
{{< /callout >}}

**Target Audience**: Developers building real services with Scala

**Prerequisites**:
- Scala basic syntax and functional programming concepts
- sbt build tool usage
- REST API and HTTP basics (Project 1)
- (Optional) Basic knowledge of Cats Effect/IO monad

---

Examples of building real services with Scala. Implement REST API servers and data pipelines. These examples demonstrate how to write type-safe, maintainable code by leveraging Scala's functional programming characteristics.

#### Project 1: REST API Server

http4s is Scala's functional HTTP library. Build web servers while maintaining immutability and referential transparency. Circe handles JSON processing, Cats Effect handles asynchronous processing.

**Tech Stack**

Libraries used in this project. http4s-ember is a lightweight HTTP server/client implementation, and Circe generates JSON codecs at compile time.

- **Scala 3** + **http4s** (functional HTTP library)
- **Circe** (JSON processing)
- **Cats Effect** (async processing)

**build.sbt**

```scala
ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "scala-api-server",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-ember-server" % "0.23.25",
      "org.http4s"      %% "http4s-ember-client" % "0.23.25",
      "org.http4s"      %% "http4s-circe"        % "0.23.25",
      "org.http4s"      %% "http4s-dsl"          % "0.23.25",
      "io.circe"        %% "circe-generic"       % "0.14.6",
      "io.circe"        %% "circe-parser"        % "0.14.6",
      "ch.qos.logback"   % "logback-classic"     % "1.4.14"
    )
  )
```

**Domain Model**

First, define domain models to use in the API. Represent immutable data with Case Classes and leverage Circe's automatic codec generation.

```scala
// domain/models.scala
package domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*

// Domain model definition
case class UserId(value: Long) extends AnyVal
case class User(
  id: UserId,
  name: String,
  email: String,
  createdAt: java.time.Instant
)

case class CreateUserRequest(name: String, email: String)
case class UpdateUserRequest(name: Option[String], email: Option[String])
case class UserResponse(id: Long, name: String, email: String)

// JSON codecs (Circe)
object JsonCodecs:
  // UserId codec
  given Encoder[UserId] = Encoder.encodeLong.contramap(_.value)
  given Decoder[UserId] = Decoder.decodeLong.map(UserId.apply)

  // Request/Response codecs
  given Decoder[CreateUserRequest] = deriveDecoder
  given Decoder[UpdateUserRequest] = deriveDecoder
  given Encoder[UserResponse] = deriveEncoder

  // User → UserResponse conversion
  extension (user: User)
    def toResponse: UserResponse = UserResponse(
      id = user.id.value,
      name = user.name,
      email = user.email
    )
```

Wrapping UserId as AnyVal provides type safety without runtime overhead. JSON codecs are auto-generated at compile time using Circe's semiauto.

{{< callout type="tip" title="Key Points" >}}
- **Case Class**: Foundation for immutable data modeling
- **AnyVal wrapping**: Type safety like `UserId(value: Long)` with no runtime overhead
- **Circe deriveEncoder/Decoder**: Auto-generate compile-time JSON codecs
- **extension**: Add conversion methods to models (`user.toResponse`)
{{< /callout >}}

**Repository (In-Memory)**

Define data storage. Use Cats Effect's Ref to implement thread-safe in-memory storage.

```scala
// repository/UserRepository.scala
package repository

import cats.effect.{IO, Ref}
import domain.*

trait UserRepository:
  def findById(id: UserId): IO[Option[User]]
  def findAll: IO[List[User]]
  def create(request: CreateUserRequest): IO[User]
  def update(id: UserId, request: UpdateUserRequest): IO[Option[User]]
  def delete(id: UserId): IO[Boolean]

object InMemoryUserRepository:
  def make: IO[UserRepository] =
    for
      store <- Ref.of[IO, Map[UserId, User]](Map.empty)
      counter <- Ref.of[IO, Long](0L)
    yield new UserRepository:

      def findById(id: UserId): IO[Option[User]] =
        store.get.map(_.get(id))

      def findAll: IO[List[User]] =
        store.get.map(_.values.toList)

      def create(request: CreateUserRequest): IO[User] =
        for
          newId <- counter.updateAndGet(_ + 1)
          user = User(
            id = UserId(newId),
            name = request.name,
            email = request.email,
            createdAt = java.time.Instant.now()
          )
          _ <- store.update(_ + (user.id -> user))
        yield user

      def update(id: UserId, request: UpdateUserRequest): IO[Option[User]] =
        store.modify { currentStore =>
          currentStore.get(id) match
            case Some(existing) =>
              val updated = existing.copy(
                name = request.name.getOrElse(existing.name),
                email = request.email.getOrElse(existing.email)
              )
              (currentStore + (id -> updated), Some(updated))
            case None =>
              (currentStore, None)
        }

      def delete(id: UserId): IO[Boolean] =
        store.modify { currentStore =>
          if currentStore.contains(id) then
            (currentStore - id, true)
          else
            (currentStore, false)
        }
```

Ref.modify guarantees atomic updates. Defining the interface as a trait makes it easy to switch to actual database implementations later.

{{< callout type="tip" title="Key Points" >}}
- **trait**: Define interface for easy implementation replacement (testing, DB switching)
- **Ref[IO, A]**: Thread-safe mutable state management
- **Ref.modify**: Atomic read-modify-write operation
- **for comprehension**: Sequential composition of IO operations
{{< /callout >}}

**HTTP Routes**

Define RESTful endpoints using http4s DSL. Each route returns an IO value, managing side effects purely.

```scala
// routes/UserRoutes.scala
package routes

import cats.effect.IO
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.circe.*
import io.circe.syntax.*
import domain.*
import domain.JsonCodecs.given
import repository.UserRepository

object UserRoutes:
  def routes(repo: UserRepository): HttpRoutes[IO] = HttpRoutes.of[IO]:

    // GET /users - List all
    case GET -> Root / "users" =>
      for
        users <- repo.findAll
        response <- Ok(users.map(_.toResponse).asJson)
      yield response

    // GET /users/:id - Get by ID
    case GET -> Root / "users" / LongVar(id) =>
      for
        userOpt <- repo.findById(UserId(id))
        response <- userOpt match
          case Some(user) => Ok(user.toResponse.asJson)
          case None => NotFound(s"User $id not found")
      yield response

    // POST /users - Create
    case req @ POST -> Root / "users" =>
      for
        createReq <- req.as[CreateUserRequest]
        user <- repo.create(createReq)
        response <- Created(user.toResponse.asJson)
      yield response

    // PUT /users/:id - Update
    case req @ PUT -> Root / "users" / LongVar(id) =>
      for
        updateReq <- req.as[UpdateUserRequest]
        userOpt <- repo.update(UserId(id), updateReq)
        response <- userOpt match
          case Some(user) => Ok(user.toResponse.asJson)
          case None => NotFound(s"User $id not found")
      yield response

    // DELETE /users/:id - Delete
    case DELETE -> Root / "users" / LongVar(id) =>
      for
        deleted <- repo.delete(UserId(id))
        response <- if deleted then NoContent() else NotFound(s"User $id not found")
      yield response

  // JSON decoders
  given EntityDecoder[IO, CreateUserRequest] = jsonOf[IO, CreateUserRequest]
  given EntityDecoder[IO, UpdateUserRequest] = jsonOf[IO, UpdateUserRequest]
```

{{< callout type="tip" title="Key Points" >}}
- **http4s DSL**: Define routes with pattern matching like `GET -> Root / "users" / LongVar(id)`
- **HttpRoutes[IO]**: Pure functional routes, side effects managed by IO
- **req.as[T]**: Decode request body to type T
- **Ok, Created, NotFound**: HTTP response generation helpers
{{< /callout >}}

**Main Application**

Combine all components to start the server. Inheriting IOApp.Simple makes it easy to define entry point for IO-based applications.

```scala
// Main.scala
import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import repository.InMemoryUserRepository
import routes.UserRoutes

object Main extends IOApp.Simple:
  def run: IO[Unit] =
    for
      repo <- InMemoryUserRepository.make
      routes = Router("/" -> UserRoutes.routes(repo)).orNotFound
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(host"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(routes)
        .build
        .use { server =>
          IO.println(s"Server started at http://localhost:8080") *>
          IO.never
        }
    yield ()
```

**Run and Test**

Run the server and test the API with curl.

```bash
# Run server
sbt run
# Server started at http://localhost:8080

# Test (in another terminal)
# Create user
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice", "email": "alice@example.com"}'
# {"id":1,"name":"Alice","email":"alice@example.com"}

# List all
curl http://localhost:8080/users

# Get by ID
curl http://localhost:8080/users/1

# Update
curl -X PUT http://localhost:8080/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice Kim"}'

# Delete
curl -X DELETE http://localhost:8080/users/1
```

{{< callout type="tip" title="Key Points" >}}
- **IOApp.Simple**: Convenient entry point for IO-based apps
- **EmberServerBuilder**: http4s lightweight server implementation
- **Router**: Compose multiple routes into one
- **use + IO.never**: Safely manage resources while keeping server running
{{< /callout >}}

#### Project 2: Data Pipeline

FS2 is a functional stream processing library. Provides memory-efficient stream processing and resource-safe file I/O.

**FS2 Stream Processing**

Add FS2 dependencies to build.sbt.

```scala
// Add to build.sbt
libraryDependencies += "co.fs2" %% "fs2-core" % "3.9.4"
libraryDependencies += "co.fs2" %% "fs2-io"   % "3.9.4"
```

Implement pipeline that processes log data as streams and aggregates. FS2 Streams are lazily evaluated and can process large data with constant memory.

```scala
// StreamPipeline.scala
import cats.effect.*
import fs2.*
import fs2.io.file.{Files, Path}
import io.circe.parser.*
import io.circe.generic.auto.*
import scala.concurrent.duration.*

case class LogEntry(
  timestamp: String,
  level: String,
  message: String,
  service: String
)

case class LogStats(
  service: String,
  errorCount: Long,
  warnCount: Long,
  infoCount: Long
)

object StreamPipeline extends IOApp.Simple:

  // 1. Read log stream from file
  def readLogs(path: Path): Stream[IO, LogEntry] =
    Files[IO].readUtf8Lines(path)
      .filter(_.nonEmpty)
      .evalMap { line =>
        IO.fromEither(decode[LogEntry](line))
          .handleError(_ => LogEntry("", "UNKNOWN", line, "unknown"))
      }

  // 2. Real-time log aggregation (5-second window)
  def aggregateLogs(logs: Stream[IO, LogEntry]): Stream[IO, Map[String, LogStats]] =
    logs
      .groupWithin(1000, 5.seconds)  // Batch every 1000 items or 5 seconds
      .map { chunk =>
        chunk.toList
          .groupBy(_.service)
          .map { case (service, entries) =>
            service -> LogStats(
              service = service,
              errorCount = entries.count(_.level == "ERROR"),
              warnCount = entries.count(_.level == "WARN"),
              infoCount = entries.count(_.level == "INFO")
            )
          }
      }

  // 3. Error alert filter
  def alertOnErrors(logs: Stream[IO, LogEntry]): Stream[IO, LogEntry] =
    logs.filter(_.level == "ERROR")

  // 4. Print results
  def printStats(stats: Map[String, LogStats]): IO[Unit] =
    IO.println("=== Log Statistics ===") *>
    stats.values.toList.traverse_ { stat =>
      IO.println(s"  ${stat.service}: E=${stat.errorCount} W=${stat.warnCount} I=${stat.infoCount}")
    }

  def run: IO[Unit] =
    // Sample data (would normally read from file)
    val sampleLogs = Stream.emits(List(
      LogEntry("2024-01-15T10:00:00", "INFO", "Server started", "api"),
      LogEntry("2024-01-15T10:00:01", "ERROR", "DB connection failed", "api"),
      LogEntry("2024-01-15T10:00:02", "WARN", "Slow query detected", "db"),
      LogEntry("2024-01-15T10:00:03", "INFO", "Request processed", "api"),
      LogEntry("2024-01-15T10:00:04", "ERROR", "Timeout", "payment"),
      LogEntry("2024-01-15T10:00:05", "INFO", "Cache hit", "cache")
    )).covary[IO]

    // Execute pipeline
    for
      // Aggregation stream
      _ <- aggregateLogs(sampleLogs)
        .evalMap(printStats)
        .compile
        .drain

      // Error alert stream
      _ <- IO.println("\n=== Error Alerts ===")
      _ <- alertOnErrors(sampleLogs)
        .evalMap(e => IO.println(s"  [ALERT] ${e.service}: ${e.message}"))
        .compile
        .drain
    yield ()
```

groupWithin performs time or count-based window aggregation. compile.drain executes the stream to the end and discards results.

{{< callout type="tip" title="Key Points" >}}
- **FS2 Stream**: Lazy evaluation, memory efficient, suitable for large data processing
- **groupWithin**: Time/count-based window aggregation (useful for real-time analysis)
- **evalMap**: Apply IO operations to stream elements
- **compile.drain**: Execute stream to end and discard results
{{< /callout >}}

#### Project 3: CLI Tool

Using scopt makes it easy to implement type-safe command-line parsers. Declaratively define subcommands, options, and arguments.

**Command-line Parser with scopt**

```scala
// build.sbt
libraryDependencies += "com.github.scopt" %% "scopt" % "4.1.0"
```

```scala
// CliTool.scala
import scopt.OParser
import java.io.File

case class Config(
  command: String = "",
  input: Option[File] = None,
  output: Option[File] = None,
  verbose: Boolean = false,
  format: String = "json"
)

object CliTool extends App:
  val builder = OParser.builder[Config]

  val parser = {
    import builder.*
    OParser.sequence(
      programName("scala-cli"),
      head("scala-cli", "1.0"),

      cmd("convert")
        .action((_, c) => c.copy(command = "convert"))
        .text("Convert file format")
        .children(
          opt[File]('i', "input")
            .required()
            .action((x, c) => c.copy(input = Some(x)))
            .text("Input file"),
          opt[File]('o', "output")
            .required()
            .action((x, c) => c.copy(output = Some(x)))
            .text("Output file"),
          opt[String]('f', "format")
            .action((x, c) => c.copy(format = x))
            .text("Output format (json, csv, xml)")
        ),

      cmd("analyze")
        .action((_, c) => c.copy(command = "analyze"))
        .text("Analyze file content")
        .children(
          opt[File]('i', "input")
            .required()
            .action((x, c) => c.copy(input = Some(x)))
            .text("Input file"),
          opt[Unit]('v', "verbose")
            .action((_, c) => c.copy(verbose = true))
            .text("Verbose output")
        ),

      help("help").text("Print help message"),
      version("version").text("Print version")
    )
  }

  OParser.parse(parser, args, Config()) match
    case Some(config) =>
      config.command match
        case "convert" =>
          println(s"Converting ${config.input.get} to ${config.output.get} as ${config.format}")
          // Implement conversion logic
        case "analyze" =>
          println(s"Analyzing ${config.input.get}")
          if config.verbose then println("Verbose mode enabled")
          // Implement analysis logic
        case _ =>
          println("No command specified. Use --help for usage.")

    case None =>
      // Parsing failed (error message automatically printed)
      ()
```

Parser definition is declarative and automatically prints error messages for invalid arguments.

```bash
# Usage examples
sbt "run convert -i input.json -o output.csv -f csv"
sbt "run analyze -i data.json -v"
sbt "run --help"
```

{{< callout type="tip" title="Key Points" >}}
- **scopt OParser**: Type-safe command-line parser
- **cmd**: Define subcommands (convert, analyze, etc.)
- **opt**: Define options (`-i`, `--input`, etc.)
- **required/optional**: Specify mandatory/optional arguments
- Automatically print error messages for invalid arguments
{{< /callout >}}

#### Common Pattern: Error Handling

Using Cats Validated allows collecting multiple validation errors at once. Either stops at the first error, but Validated collects all errors.

**Using Either and Validated**

```scala
import cats.data.{Validated, ValidatedNec}
import cats.syntax.all.*

// Validation rules
sealed trait ValidationError
case class EmptyField(field: String) extends ValidationError
case class InvalidFormat(field: String, reason: String) extends ValidationError
case class OutOfRange(field: String, min: Int, max: Int) extends ValidationError

type ValidationResult[A] = ValidatedNec[ValidationError, A]

// Validation functions
def validateName(name: String): ValidationResult[String] =
  if name.isEmpty then EmptyField("name").invalidNec
  else if name.length < 2 then InvalidFormat("name", "minimum 2 characters").invalidNec
  else name.validNec

def validateEmail(email: String): ValidationResult[String] =
  val emailRegex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".r
  if email.isEmpty then EmptyField("email").invalidNec
  else if emailRegex.findFirstIn(email).isEmpty then InvalidFormat("email", "invalid email").invalidNec
  else email.validNec

def validateAge(age: Int): ValidationResult[Int] =
  if age < 0 || age > 150 then OutOfRange("age", 0, 150).invalidNec
  else age.validNec

// Combine
case class ValidatedUser(name: String, email: String, age: Int)

def validateUser(name: String, email: String, age: Int): ValidationResult[ValidatedUser] =
  (validateName(name), validateEmail(email), validateAge(age)).mapN(ValidatedUser.apply)

// Usage
validateUser("Alice", "alice@example.com", 30)  // Valid(ValidatedUser(...))
validateUser("", "invalid-email", 200)          // Invalid(Chain(EmptyField(name), InvalidFormat(email, ...), OutOfRange(age, ...)))
```

ValidatedNec collects errors in NonEmptyChain. mapN combines results only when all validations succeed. If any fail, all failure reasons are collected.

{{< callout type="tip" title="Key Points" >}}
- **Either**: Stop at first error (fail-fast)
- **Validated**: Collect all errors (accumulating errors)
- **ValidatedNec**: Collect errors in NonEmptyChain (efficient appending)
- **mapN**: Combine multiple Validateds, generate result only when all succeed
- Useful when showing multiple errors at once like user input validation
{{< /callout >}}

#### Next Steps

After learning practical project examples, continue with these topics.

- [Spark Integration](spark-integration/) - Large-scale data processing
- [Functional Patterns](../concepts/functional-patterns/) - Cats, ZIO usage
- [Concurrency](../concepts/concurrency/) - Future, IO deep dive
