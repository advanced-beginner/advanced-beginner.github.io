---
lastmod: "2026-01-08"
title: 실무 프로젝트
weight: 5
---

Scala로 실제 서비스를 구축하는 예제입니다. REST API 서버와 데이터 파이프라인을 구현합니다.

## 프로젝트 1: REST API 서버

### 기술 스택

- **Scala 3** + **http4s** (함수형 HTTP 라이브러리)
- **Circe** (JSON 처리)
- **Cats Effect** (비동기 처리)

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

### 도메인 모델

```scala
// domain/models.scala
package domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*

// 도메인 모델 정의
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

// JSON 코덱 (Circe)
object JsonCodecs:
  // UserId 코덱
  given Encoder[UserId] = Encoder.encodeLong.contramap(_.value)
  given Decoder[UserId] = Decoder.decodeLong.map(UserId.apply)

  // Request/Response 코덱
  given Decoder[CreateUserRequest] = deriveDecoder
  given Decoder[UpdateUserRequest] = deriveDecoder
  given Encoder[UserResponse] = deriveEncoder

  // User → UserResponse 변환
  extension (user: User)
    def toResponse: UserResponse = UserResponse(
      id = user.id.value,
      name = user.name,
      email = user.email
    )
```

### 리포지토리 (인메모리)

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

### HTTP 라우트

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

    // GET /users - 전체 조회
    case GET -> Root / "users" =>
      for
        users <- repo.findAll
        response <- Ok(users.map(_.toResponse).asJson)
      yield response

    // GET /users/:id - 단건 조회
    case GET -> Root / "users" / LongVar(id) =>
      for
        userOpt <- repo.findById(UserId(id))
        response <- userOpt match
          case Some(user) => Ok(user.toResponse.asJson)
          case None => NotFound(s"User $id not found")
      yield response

    // POST /users - 생성
    case req @ POST -> Root / "users" =>
      for
        createReq <- req.as[CreateUserRequest]
        user <- repo.create(createReq)
        response <- Created(user.toResponse.asJson)
      yield response

    // PUT /users/:id - 수정
    case req @ PUT -> Root / "users" / LongVar(id) =>
      for
        updateReq <- req.as[UpdateUserRequest]
        userOpt <- repo.update(UserId(id), updateReq)
        response <- userOpt match
          case Some(user) => Ok(user.toResponse.asJson)
          case None => NotFound(s"User $id not found")
      yield response

    // DELETE /users/:id - 삭제
    case DELETE -> Root / "users" / LongVar(id) =>
      for
        deleted <- repo.delete(UserId(id))
        response <- if deleted then NoContent() else NotFound(s"User $id not found")
      yield response

  // JSON 디코더
  given EntityDecoder[IO, CreateUserRequest] = jsonOf[IO, CreateUserRequest]
  given EntityDecoder[IO, UpdateUserRequest] = jsonOf[IO, UpdateUserRequest]
```

### 메인 애플리케이션

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

### 실행 및 테스트

```bash
# 서버 실행
sbt run
# Server started at http://localhost:8080

# 테스트 (다른 터미널에서)
# 사용자 생성
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice", "email": "alice@example.com"}'
# {"id":1,"name":"Alice","email":"alice@example.com"}

# 전체 조회
curl http://localhost:8080/users
# [{"id":1,"name":"Alice","email":"alice@example.com"}]

# 단건 조회
curl http://localhost:8080/users/1

# 수정
curl -X PUT http://localhost:8080/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Alice Kim"}'

# 삭제
curl -X DELETE http://localhost:8080/users/1
```

---

## 프로젝트 2: 데이터 파이프라인

### FS2 스트림 처리

```scala
// build.sbt에 추가
libraryDependencies += "co.fs2" %% "fs2-core" % "3.9.4"
libraryDependencies += "co.fs2" %% "fs2-io"   % "3.9.4"
```

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

  // 1. 파일에서 로그 스트림 읽기
  def readLogs(path: Path): Stream[IO, LogEntry] =
    Files[IO].readUtf8Lines(path)
      .filter(_.nonEmpty)
      .evalMap { line =>
        IO.fromEither(decode[LogEntry](line))
          .handleError(_ => LogEntry("", "UNKNOWN", line, "unknown"))
      }

  // 2. 실시간 로그 집계 (5초 윈도우)
  def aggregateLogs(logs: Stream[IO, LogEntry]): Stream[IO, Map[String, LogStats]] =
    logs
      .groupWithin(1000, 5.seconds)  // 1000개 또는 5초마다 배치
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

  // 3. 에러 알림 필터
  def alertOnErrors(logs: Stream[IO, LogEntry]): Stream[IO, LogEntry] =
    logs.filter(_.level == "ERROR")

  // 4. 결과 출력
  def printStats(stats: Map[String, LogStats]): IO[Unit] =
    IO.println("=== Log Statistics ===") *>
    stats.values.toList.traverse_ { stat =>
      IO.println(s"  ${stat.service}: E=${stat.errorCount} W=${stat.warnCount} I=${stat.infoCount}")
    }

  def run: IO[Unit] =
    // 샘플 데이터 생성
    val sampleLogs = Stream.emits(List(
      LogEntry("2024-01-15T10:00:00", "INFO", "Server started", "api"),
      LogEntry("2024-01-15T10:00:01", "ERROR", "DB connection failed", "api"),
      LogEntry("2024-01-15T10:00:02", "WARN", "Slow query detected", "db"),
      LogEntry("2024-01-15T10:00:03", "INFO", "Request processed", "api"),
      LogEntry("2024-01-15T10:00:04", "ERROR", "Timeout", "payment"),
      LogEntry("2024-01-15T10:00:05", "INFO", "Cache hit", "cache")
    )).covary[IO]

    // 파이프라인 실행
    for
      // 집계 스트림
      _ <- aggregateLogs(sampleLogs)
        .evalMap(printStats)
        .compile
        .drain

      // 에러 알림 스트림
      _ <- IO.println("\n=== Error Alerts ===")
      _ <- alertOnErrors(sampleLogs)
        .evalMap(e => IO.println(s"  [ALERT] ${e.service}: ${e.message}"))
        .compile
        .drain
    yield ()
```

---

## 프로젝트 3: CLI 도구

### scopt를 이용한 명령줄 파서

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
          // 변환 로직 구현
        case "analyze" =>
          println(s"Analyzing ${config.input.get}")
          if config.verbose then println("Verbose mode enabled")
          // 분석 로직 구현
        case _ =>
          println("No command specified. Use --help for usage.")

    case None =>
      // 파싱 실패 (자동으로 에러 메시지 출력)
      ()
```

```bash
# 사용 예시
sbt "run convert -i input.json -o output.csv -f csv"
sbt "run analyze -i data.json -v"
sbt "run --help"
```

---

## 공통 패턴: 에러 처리

### Either와 Validated 활용

```scala
import cats.data.{Validated, ValidatedNec}
import cats.syntax.all.*

// 검증 규칙 정의
sealed trait ValidationError
case class EmptyField(field: String) extends ValidationError
case class InvalidFormat(field: String, reason: String) extends ValidationError
case class OutOfRange(field: String, min: Int, max: Int) extends ValidationError

type ValidationResult[A] = ValidatedNec[ValidationError, A]

// 검증 함수들
def validateName(name: String): ValidationResult[String] =
  if name.isEmpty then EmptyField("name").invalidNec
  else if name.length < 2 then InvalidFormat("name", "최소 2자 이상").invalidNec
  else name.validNec

def validateEmail(email: String): ValidationResult[String] =
  val emailRegex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".r
  if email.isEmpty then EmptyField("email").invalidNec
  else if emailRegex.findFirstIn(email).isEmpty then InvalidFormat("email", "유효하지 않은 이메일").invalidNec
  else email.validNec

def validateAge(age: Int): ValidationResult[Int] =
  if age < 0 || age > 150 then OutOfRange("age", 0, 150).invalidNec
  else age.validNec

// 조합
case class ValidatedUser(name: String, email: String, age: Int)

def validateUser(name: String, email: String, age: Int): ValidationResult[ValidatedUser] =
  (validateName(name), validateEmail(email), validateAge(age)).mapN(ValidatedUser.apply)

// 사용
validateUser("Alice", "alice@example.com", 30)  // Valid(ValidatedUser(...))
validateUser("", "invalid-email", 200)          // Invalid(Chain(EmptyField(name), InvalidFormat(email, ...), OutOfRange(age, ...)))
```

---

## 다음 단계

- [Spark 연동](spark-integration/) - 대규모 데이터 처리
- [함수형 패턴](../concepts/functional-patterns/) - Cats, ZIO 활용
- [동시성](../concepts/concurrency/) - Future, IO 심화
