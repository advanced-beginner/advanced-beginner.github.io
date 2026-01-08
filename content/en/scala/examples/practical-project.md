---
lastmod: "2026-01-08"
title: Practical Projects
weight: 5
---

Examples of building real services with Scala. Implement REST API servers and data pipelines.

## Project 1: REST API Server

### Tech Stack

- **Scala 3** + **http4s** (functional HTTP library)
- **Circe** (JSON processing)
- **Cats Effect** (async processing)

### build.sbt

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

### Domain Model

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

### Repository (In-Memory)

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

### HTTP Routes

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
```

### Main Application

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

### Run and Test

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

---

## Project 2: Data Pipeline

### FS2 Stream Processing

```scala
// build.sbt
libraryDependencies += "co.fs2" %% "fs2-core" % "3.9.4"
libraryDependencies += "co.fs2" %% "fs2-io"   % "3.9.4"
```

```scala
// StreamPipeline.scala
import cats.effect.*
import fs2.*
import fs2.io.file.{Files, Path}
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
      _ <- aggregateLogs(sampleLogs)
        .evalMap(printStats)
        .compile
        .drain
      _ <- IO.println("\n=== Error Alerts ===")
      _ <- alertOnErrors(sampleLogs)
        .evalMap(e => IO.println(s"  [ALERT] ${e.service}: ${e.message}"))
        .compile
        .drain
    yield ()
```

---

## Common Pattern: Error Handling

### Using Either and Validated

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

---

## Next Steps

- [Spark Integration](spark-integration/) - Large-scale data processing
- [Functional Patterns](../concepts/functional-patterns/) - Cats, ZIO usage
- [Concurrency](../concepts/concurrency/) - Future, IO deep dive
