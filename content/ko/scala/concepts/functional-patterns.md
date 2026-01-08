---
lastmod: "2026-01-06"
title: 함수형 프로그래밍 패턴
weight: 17
---

이 문서에서는 Scala에서 사용되는 핵심 함수형 프로그래밍 패턴을 다룹니다.

> 📚 **사전 지식**: 이 문서를 이해하려면 다음 개념에 익숙해야 합니다:
> - [고차 함수](../higher-order-functions/) - map, flatMap, filter
> - [For Comprehension](../for-comprehensions/) - 모나딕 연산의 문법적 설탕
> - [제네릭](../generics/) - 타입 매개변수
>
> **난이도**: ⭐⭐⭐⭐ (고급)

## 참조 투명성

함수 호출을 그 결과로 대체해도 프로그램의 의미가 변하지 않는 속성입니다.

```scala
// 참조 투명
def add(a: Int, b: Int): Int = a + b

val x = add(1, 2)  // 3으로 대체 가능
val y = x + x      // add(1, 2) + add(1, 2)와 동일

// 참조 불투명 (부수 효과)
var counter = 0
def increment(): Int = {
  counter += 1
  counter
}

val a = increment()  // 1
val b = increment()  // 2 (결과가 달라짐!)
```

## 불변성

데이터를 변경하지 않고 새 데이터를 생성합니다.

```scala
// 불변 리스트
val list1 = List(1, 2, 3)
val list2 = 0 :: list1  // list1은 변경되지 않음

// 케이스 클래스 업데이트
case class Person(name: String, age: Int)
val alice = Person("Alice", 30)
val olderAlice = alice.copy(age = 31)  // alice는 변경되지 않음
```

## Functor

`map` 연산을 가진 타입입니다.

### Functor 법칙

```scala
// 1. 항등 법칙: fa.map(identity) == fa
List(1, 2, 3).map(identity) == List(1, 2, 3)

// 2. 합성 법칙: fa.map(f).map(g) == fa.map(f andThen g)
val f = (x: Int) => x + 1
val g = (x: Int) => x * 2

List(1, 2, 3).map(f).map(g) == List(1, 2, 3).map(f andThen g)
```

### 커스텀 Functor

```scala
trait Functor[F[_]]:
  def map[A, B](fa: F[A])(f: A => B): F[B]

// Option Functor
given Functor[Option] with
  def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa.map(f)

// List Functor
given Functor[List] with
  def map[A, B](fa: List[A])(f: A => B): List[B] = fa.map(f)
```

## Applicative

독립적인 효과를 결합합니다.

```scala
trait Applicative[F[_]] extends Functor[F]:
  def pure[A](a: A): F[A]
  def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]

// Option으로 예시
val some1: Option[Int] = Some(1)
val some2: Option[Int] = Some(2)

// 두 Option을 결합
val sum: Option[Int] = (some1, some2) match
  case (Some(a), Some(b)) => Some(a + b)
  case _ => None
```

## Monad

순차적인 효과를 연결합니다.

### Monad 흐름 시각화

```mermaid
flowchart LR
    subgraph "flatMap 연산"
        A["Option#91;A#93;"] -->|"flatMap"| F["A => Option#91;B#93;"]
        F --> B["Option#91;B#93;"]
    end

    subgraph "예시: 안전한 나눗셈"
        S1["Some(10)"] -->|"flatMap(divide(_, 2))"| S2["Some(5)"]
        S2 -->|"flatMap(divide(_, 0))"| N["None"]
    end

    subgraph "For Comprehension"
        FC["for {<br>  a <- Some(10)<br>  b <- divide(a, 2)<br>  c <- divide(b, 0)<br>} yield c"]
        Result["None"]
        FC --> Result
    end
```

### Monad 법칙

```scala
trait Monad[F[_]] extends Applicative[F]:
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

  // pure(a).flatMap(f) == f(a)  // 왼쪽 항등
  // m.flatMap(pure) == m        // 오른쪽 항등
  // m.flatMap(f).flatMap(g) == m.flatMap(a => f(a).flatMap(g))  // 결합
```

### 표준 라이브러리 Monad

```scala
// Option
val result = for {
  a <- Some(1)
  b <- Some(2)
} yield a + b  // Some(3)

// Either
val validated: Either[String, Int] = for {
  x <- Right(1)
  y <- Right(2)
} yield x + y  // Right(3)

// Future
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

def fetchUser(id: Int): Future[String] = Future(s"User$id")
def fetchOrders(user: String): Future[List[String]] = Future(List(s"Order1-$user"))

val asyncResult = for
  user <- fetchUser(1)
  orders <- fetchOrders(user)
yield orders
// Future(List("Order1-User1"))
```

## Option - null 대체

```scala
// 안전한 나눗셈
def divide(a: Int, b: Int): Option[Int] =
  if b == 0 then None else Some(a / b)

// 체이닝
val result = for {
  x <- divide(10, 2)  // Some(5)
  y <- divide(x, 0)   // None
} yield y             // None

// getOrElse
divide(10, 0).getOrElse(0)  // 0

// fold
divide(10, 2).fold(0)(_ * 2)  // 10
```

## Either - 에러 처리

```scala
sealed trait ValidationError
case class EmptyName(field: String) extends ValidationError
case class InvalidAge(age: Int) extends ValidationError

def validateName(name: String): Either[ValidationError, String] =
  if name.isEmpty then Left(EmptyName("name"))
  else Right(name)

def validateAge(age: Int): Either[ValidationError, Int] =
  if age < 0 || age > 150 then Left(InvalidAge(age))
  else Right(age)

case class Person(name: String, age: Int)

def createPerson(name: String, age: Int): Either[ValidationError, Person] =
  for {
    validName <- validateName(name)
    validAge <- validateAge(age)
  } yield Person(validName, validAge)

createPerson("Alice", 30)  // Right(Person("Alice", 30))
createPerson("", 30)       // Left(EmptyName("name"))
```

## Try - 예외 처리

```scala
import scala.util.{Try, Success, Failure}

def parseInt(s: String): Try[Int] = Try(s.toInt)

parseInt("42") match
  case Success(n) => println(s"숫자: $n")
  case Failure(e) => println(s"에러: ${e.getMessage}")

// 체이닝
val result = for {
  a <- parseInt("10")
  b <- parseInt("20")
} yield a + b  // Success(30)

// 실패 복구
parseInt("abc").getOrElse(0)  // 0
parseInt("abc").recover { case _: NumberFormatException => 0 }
```

## 함수 합성

```scala
val addOne = (x: Int) => x + 1
val double = (x: Int) => x * 2
val square = (x: Int) => x * x

// andThen: 왼쪽 -> 오른쪽
val pipeline = addOne andThen double andThen square
pipeline(3)  // ((3 + 1) * 2)^2 = 64

// compose: 오른쪽 -> 왼쪽
val composed = square compose double compose addOne
composed(3)  // (3 + 1) * 2)^2 = 64
```

## 커링과 부분 적용

```scala
// 커링
def add(a: Int)(b: Int): Int = a + b

val add5 = add(5)
add5(3)  // 8

// 부분 적용
def log(level: String, message: String): Unit =
  println(s"[$level] $message")

val error = log("ERROR", _)
val info = log("INFO", _)

error("Something went wrong")
info("Application started")
```

## Cats/ZIO 라이브러리

### Cats

```scala
import cats.*
import cats.implicits.*

// Validated - 에러 누적
import cats.data.Validated

type ValidationResult[A] = Validated[List[String], A]

val valid1: ValidationResult[Int] = Validated.valid(1)
val valid2: ValidationResult[Int] = Validated.valid(2)
val invalid: ValidationResult[Int] = Validated.invalid(List("에러"))

// 모든 에러 수집
(valid1, invalid, invalid).mapN(_ + _ + _)
// Invalid(List("에러", "에러"))
```

### ZIO

```scala
import zio.*
import java.io.IOException

// Console 연산은 IOException을 발생시킬 수 있음
val program: ZIO[Any, IOException, Int] = for
  _ <- Console.printLine("숫자를 입력하세요:")
  input <- Console.readLine
  num <- ZIO.fromOption(input.toIntOption)
           .orElseFail(new IOException("숫자가 아닙니다"))
yield num * 2
```

## 연습 문제

### 1. 커스텀 Monad ⭐⭐

`Box[A]` 타입에 대한 `flatMap`을 구현하세요.

<details>
<summary>정답 보기</summary>

```scala
case class Box[A](value: A):
  def map[B](f: A => B): Box[B] = Box(f(value))
  def flatMap[B](f: A => Box[B]): Box[B] = f(value)

val result = for {
  x <- Box(1)
  y <- Box(2)
} yield x + y  // Box(3)
```

</details>

### 2. 에러 누적 ⭐⭐⭐

여러 검증을 수행하고 모든 에러를 수집하세요.

<details>
<summary>정답 보기</summary>

```scala
type Errors = List[String]
type Validated[A] = Either[Errors, A]

def validateAll[A](validations: List[Validated[A]]): Validated[List[A]] =
  val (errors, values) = validations.partitionMap(identity)
  if errors.isEmpty then Right(values)
  else Left(errors.flatten)

val results = List(
  Right(1),
  Left(List("에러1")),
  Right(3),
  Left(List("에러2"))
)

validateAll(results)  // Left(List("에러1", "에러2"))
```

</details>

## 참고 자료

- [Cats 공식 문서](https://typelevel.org/cats/)
- [ZIO 공식 문서](https://zio.dev/)
- [Functional Programming in Scala](https://www.manning.com/books/functional-programming-in-scala)

## 다음 단계

- [Cats 라이브러리](https://typelevel.org/cats/)
- [ZIO 라이브러리](https://zio.dev/)
- [fs2 스트리밍](https://fs2.io/)
