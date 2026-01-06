---
lastmod: "2026-01-06"
title: 참고 자료
weight: 4
---

Scala 학습에 도움이 되는 참고 자료를 정리합니다.

## 공식 문서

### Scala 공식

- [Scala 공식 사이트](https://www.scala-lang.org/) — 최신 뉴스, 다운로드
- [Scala 3 Documentation](https://docs.scala-lang.org/scala3/) — Scala 3 공식 문서
- [Scala 2 Documentation](https://docs.scala-lang.org/) — Scala 2 공식 문서
- [Scala API Docs](https://www.scala-lang.org/api/current/) — 표준 라이브러리 API

### 학습 자료

- [Tour of Scala](https://docs.scala-lang.org/tour/tour-of-scala.html) — 핵심 기능 빠른 둘러보기
- [Scala 3 Book](https://docs.scala-lang.org/scala3/book/introduction.html) — 단계별 학습서
- [Scala Exercises](https://www.scala-exercises.org/) — 인터랙티브 학습

### 빌드 도구

- [sbt Documentation](https://www.scala-sbt.org/1.x/docs/) — sbt 공식 문서
- [Mill Build Tool](https://mill-build.com/mill/Intro_to_Mill.html) — Mill 문서

## 서적

### 입문서

- **"Programming in Scala" (4th Edition)**
  - 저자: Martin Odersky, Lex Spoon, Bill Venners
  - Scala 창시자가 쓴 권위 있는 책

- **"Scala for the Impatient" (3rd Edition)**
  - 저자: Cay S. Horstmann
  - 빠르게 Scala를 배우고 싶은 분께 추천

### 함수형 프로그래밍

- **"Functional Programming in Scala" (2nd Edition)**
  - 저자: Michael Pilquist, Rúnar Bjarnason, Paul Chiusano
  - "레드북"으로 유명, FP 심화 학습

- **"Essential Scala"**
  - 저자: Noel Welsh, Dave Gurnell
  - [무료 온라인 버전](https://underscore.io/books/essential-scala/)

### 고급

- **"Scala with Cats"**
  - 저자: Noel Welsh, Dave Gurnell
  - [무료 온라인 버전](https://underscore.io/books/scala-with-cats/)

- **"Practical FP in Scala"**
  - 저자: Gabriel Volpe
  - 실무 함수형 프로그래밍

## 실무 라이브러리 맛보기

이 가이드에서 배운 개념이 실무 라이브러리에서 어떻게 활용되는지 간단히 소개합니다.

### Cats: 타입 클래스 실전 활용

[Type Classes](../concepts/type-classes/)에서 배운 패턴이 Cats에서 어떻게 사용되는지 보세요.

```scala
// 이 가이드에서 배운 타입 클래스 패턴
trait Monoid[A]:
  def empty: A
  def combine(x: A, y: A): A

// Cats에서는 이렇게 활용합니다
import cats.syntax.all.*
import cats.Monoid

// 이미 정의된 인스턴스 사용
List(1, 2, 3).combineAll  // 6 (Monoid[Int] 사용)
List("a", "b").combineAll // "ab" (Monoid[String] 사용)

// 커스텀 타입에 적용
case class Order(total: Int, items: Int)

given Monoid[Order] with
  def empty = Order(0, 0)
  def combine(x: Order, y: Order) =
    Order(x.total + y.total, x.items + y.items)

List(Order(100, 2), Order(200, 3)).combineAll
// Order(300, 5)
```

**연결 개념:** [Type Classes](../concepts/type-classes/), [Implicits/Given](../concepts/implicits/)

### ZIO: 함수형 효과 시스템

[Higher-Order Functions](../concepts/higher-order-functions/)과 [For Comprehensions](../concepts/for-comprehensions/)이 ZIO에서 빛을 발합니다.

```scala
import zio.*

// 부수 효과를 값으로 표현
val readLine: ZIO[Any, IOException, String] = Console.readLine
val printLine: String => ZIO[Any, IOException, Unit] = Console.printLine

// for comprehension으로 순차 실행
val program: ZIO[Any, IOException, Unit] = for
  _    <- printLine("이름을 입력하세요:")
  name <- readLine
  _    <- printLine(s"안녕하세요, $name!")
yield ()

// 이 시점에서는 아무것도 실행되지 않음 (순수 값)
// Unsafe.unsafe { implicit u => Runtime.default.unsafe.run(program) }
```

**연결 개념:** [For Comprehensions](../concepts/for-comprehensions/), [Functional Patterns](../concepts/functional-patterns/)

### http4s: 함수형 HTTP

[Pattern Matching](../concepts/pattern-matching/)과 [Case Classes](../concepts/case-classes/)가 라우팅에 활용됩니다.

```scala
import org.http4s.*
import org.http4s.dsl.io.*

// 패턴 매칭으로 HTTP 라우팅
val routes = HttpRoutes.of[IO] {
  case GET -> Root / "users" / IntVar(id) =>
    Ok(s"User $id")

  case req @ POST -> Root / "users" =>
    req.as[User].flatMap(user => Created(user.asJson))

  case GET -> Root / "health" =>
    Ok("healthy")
}
```

**연결 개념:** [Pattern Matching](../concepts/pattern-matching/), [Case Classes](../concepts/case-classes/)

### Circe: JSON 타입 클래스

[Type Classes](../concepts/type-classes/)와 [Generics](../concepts/generics/)가 JSON 변환에 사용됩니다.

```scala
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*

// case class 정의만 하면 자동으로 JSON 변환 가능
case class User(name: String, age: Int)

val user = User("Alice", 30)
user.asJson.noSpaces  // {"name":"Alice","age":30}

// 파싱도 타입 클래스로
"""{"name":"Bob","age":25}""".as[User]
// Right(User("Bob", 25))
```

**연결 개념:** [Case Classes](../concepts/case-classes/), [Type Classes](../concepts/type-classes/)

### 다음 학습 방향

| 관심 분야 | 추천 라이브러리 | 참고 자료 |
|----------|----------------|----------|
| 함수형 기초 | Cats | [Scala with Cats (무료)](https://underscore.io/books/scala-with-cats/) |
| 비동기/동시성 | ZIO 또는 Cats Effect | [ZIO 공식 문서](https://zio.dev/) |
| 웹 개발 | http4s + Circe | [http4s 튜토리얼](https://http4s.org/v0.23/docs/) |
| 데이터 처리 | Spark | [Spark Scala API](https://spark.apache.org/docs/latest/api/scala/) |

---

## 온라인 강의

### 무료

- [Scala & Functional Programming Essentials](https://rockthejvm.com/) — Rock the JVM
- [Functional Programming Principles in Scala](https://www.coursera.org/learn/scala-functional-programming) — Coursera (Martin Odersky)
- [Scala 3 New Features](https://docs.scala-lang.org/scala3/new-in-scala3.html) — 공식 문서

### 유료

- [Rock the JVM](https://rockthejvm.com/) — 종합적인 Scala 강의
- [Zionomicon](https://www.zionomicon.com/) — ZIO 심화

## 라이브러리 문서

### 함수형 프로그래밍

- [Cats](https://typelevel.org/cats/) — 타입 클래스 라이브러리
- [ZIO](https://zio.dev/) — 효과 시스템
- [Cats Effect](https://typelevel.org/cats-effect/) — 비동기/동시성

### 웹 개발

- [Play Framework](https://www.playframework.com/documentation/latest/Home) — 웹 프레임워크
- [http4s](https://http4s.org/) — 함수형 HTTP
- [Akka HTTP](https://doc.akka.io/docs/akka-http/current/) — 액터 기반 HTTP

### 데이터 처리

- [Apache Spark](https://spark.apache.org/docs/latest/api/scala/) — 분산 데이터 처리
- [Apache Kafka](https://kafka.apache.org/documentation/) — 스트리밍 플랫폼
- [Doobie](https://tpolecat.github.io/doobie/) — 함수형 JDBC

### JSON

- [Circe](https://circe.github.io/circe/) — JSON 라이브러리
- [Play JSON](https://github.com/playframework/play-json) — Play JSON
- [uPickle](https://com-lihaoyi.github.io/upickle/) — 경량 JSON

### 테스트

- [ScalaTest](https://www.scalatest.org/) — 테스트 프레임워크
- [MUnit](https://scalameta.org/munit/) — 경량 테스트
- [ScalaCheck](https://scalacheck.org/) — 속성 기반 테스트

## 커뮤니티

### 포럼/디스코드

- [Scala Users Forum](https://users.scala-lang.org/) — 공식 포럼
- [Scala Discord](https://discord.gg/scala) — 실시간 채팅
- [Typelevel Discord](https://discord.gg/XF3CXcMzqD) — Cats, fs2 등

### Q&A

- [Stack Overflow - Scala](https://stackoverflow.com/questions/tagged/scala)
- [Reddit - r/scala](https://www.reddit.com/r/scala/)

### 블로그/뉴스

- [Scala Times](https://scalatimes.com/) — 주간 뉴스레터
- [Typelevel Blog](https://typelevel.org/blog/) — 함수형 Scala
- [Li Haoyi's Blog](https://www.lihaoyi.com/) — Scala 팁

## 도구

### IDE

- [IntelliJ IDEA Scala Plugin](https://plugins.jetbrains.com/plugin/1347-scala)
- [Metals (VS Code)](https://scalameta.org/metals/)

### 유틸리티

- [Scastie](https://scastie.scala-lang.org/) — 온라인 Scala 실행 ⭐ **이 가이드의 모든 예제를 여기서 실행 가능!**
- [Scaladex](https://index.scala-lang.org/) — 라이브러리 검색
- [Scalafmt](https://scalameta.org/scalafmt/) — 코드 포맷터
- [Scalafix](https://scalacenter.github.io/scalafix/) — 리팩토링 도구

## 컨퍼런스

- [Scala Days](https://scaladays.org/) — 주요 Scala 컨퍼런스
- [Scala.io](https://scala.io/) — 유럽 Scala 컨퍼런스
- [Typelevel Summit](https://typelevel.org/event/) — 함수형 Scala

## 추천 학습 경로

### 입문자

1. Quick Start로 환경 설정
2. Tour of Scala로 핵심 개념
3. "Scala for the Impatient" 또는 Coursera 강의
4. 간단한 프로젝트 실습

### 중급자

1. "Programming in Scala" 정독
2. Cats 또는 ZIO 학습
3. 실무 프로젝트에 함수형 스타일 적용

### 고급자

1. "Functional Programming in Scala"
2. "Scala with Cats"
3. 타입 시스템 심화 (Shapeless, Type-level programming)
4. 오픈 소스 기여
