---
lastmod: "2026-01-06"
title: Scala 2 vs Scala 3 비교
weight: 3
---

동일한 로직을 Scala 2와 Scala 3로 구현하여 차이점을 비교합니다.

## 문법 스타일

### 블록 구문

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// 들여쓰기 기반 (선택)
def greet(name: String): String =
  val greeting = s"Hello, $name!"
  greeting

// 여러 줄
def process(x: Int): Int =
  val doubled = x * 2
  val squared = doubled * doubled
  squared
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// 중괄호 필수
def greet(name: String): String = {
  val greeting = s"Hello, $name!"
  greeting
}

// 여러 줄
def process(x: Int): Int = {
  val doubled = x * 2
  val squared = doubled * doubled
  squared
}
```
{{% /tab %}}
{{< /tabs >}}

### if 표현식

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
val result = if x > 0 then "positive" else "non-positive"

val grade = if score >= 90 then "A"
  else if score >= 80 then "B"
  else if score >= 70 then "C"
  else "F"
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
val result = if (x > 0) "positive" else "non-positive"

val grade = if (score >= 90) "A"
  else if (score >= 80) "B"
  else if (score >= 70) "C"
  else "F"
```
{{% /tab %}}
{{< /tabs >}}

### for 표현식

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// do 키워드
for x <- list do
  println(x)

// 여러 생성자
for
  x <- 1 to 3
  y <- 1 to 3
do
  println(s"$x, $y")

// yield
val result = for
  x <- 1 to 5
  if x % 2 == 0
yield x * x
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// 괄호/중괄호
for (x <- list) {
  println(x)
}

// 여러 생성자
for {
  x <- 1 to 3
  y <- 1 to 3
} {
  println(s"$x, $y")
}

// yield
val result = for {
  x <- 1 to 5
  if x % 2 == 0
} yield x * x
```
{{% /tab %}}
{{< /tabs >}}

## 열거형 (Enum)

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// 단순 열거형
enum Color:
  case Red, Green, Blue

// 매개변수 열거형
enum HttpStatus(val code: Int):
  case OK extends HttpStatus(200)
  case NotFound extends HttpStatus(404)
  case ServerError extends HttpStatus(500)

// ADT
enum Shape:
  case Circle(radius: Double)
  case Rectangle(width: Double, height: Double)

// 사용
val color = Color.Red
val status = HttpStatus.OK
val circle = Shape.Circle(5.0)
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// sealed trait + case object
sealed trait Color
object Color {
  case object Red extends Color
  case object Green extends Color
  case object Blue extends Color
}

// 매개변수 열거형
sealed abstract class HttpStatus(val code: Int)
object HttpStatus {
  case object OK extends HttpStatus(200)
  case object NotFound extends HttpStatus(404)
  case object ServerError extends HttpStatus(500)
}

// ADT
sealed trait Shape
case class Circle(radius: Double) extends Shape
case class Rectangle(width: Double, height: Double) extends Shape

// 사용
val color: Color = Color.Red
val status: HttpStatus = HttpStatus.OK
val circle: Shape = Circle(5.0)
```
{{% /tab %}}
{{< /tabs >}}

## 암시적 기능

### 암시적 값과 매개변수

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// given 인스턴스
given defaultTimeout: Int = 5000

// using 절
def connect(url: String)(using timeout: Int): Unit =
  println(s"Connecting to $url with timeout $timeout")

// 호출
connect("localhost")              // given 사용
connect("localhost")(using 10000) // 명시적
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// implicit val
implicit val defaultTimeout: Int = 5000

// implicit 매개변수
def connect(url: String)(implicit timeout: Int): Unit =
  println(s"Connecting to $url with timeout $timeout")

// 호출
connect("localhost")        // implicit 사용
connect("localhost")(10000) // 명시적
```
{{% /tab %}}
{{< /tabs >}}

### 확장 메서드

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
extension (s: String)
  def exclaim: String = s + "!"
  def words: List[String] = s.split(" ").toList
  def reverse: String = s.reverse

"Hello".exclaim  // "Hello!"
"Hello World".words  // List("Hello", "World")
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
implicit class StringOps(s: String) {
  def exclaim: String = s + "!"
  def words: List[String] = s.split(" ").toList
  def reverse: String = s.reverse
}

"Hello".exclaim  // "Hello!"
"Hello World".words  // List("Hello", "World")
```
{{% /tab %}}
{{< /tabs >}}

### 타입 클래스

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
trait Show[A]:
  def show(a: A): String

object Show:
  given Show[Int] with
    def show(a: Int): String = a.toString

  given Show[String] with
    def show(a: String): String = s"\"$a\""

def print[A](a: A)(using s: Show[A]): Unit =
  println(s.show(a))

// summon
val intShow = summon[Show[Int]]
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
trait Show[A] {
  def show(a: A): String
}

object Show {
  implicit val intShow: Show[Int] = new Show[Int] {
    def show(a: Int): String = a.toString
  }

  implicit val stringShow: Show[String] = new Show[String] {
    def show(a: String): String = s""""$a""""
  }
}

def print[A](a: A)(implicit s: Show[A]): Unit =
  println(s.show(a))

// implicitly
val intShow = implicitly[Show[Int]]
```
{{% /tab %}}
{{< /tabs >}}

## 진입점

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
@main def hello(): Unit =
  println("Hello, World!")

// 인자 받기
@main def greet(name: String, count: Int): Unit =
  for _ <- 1 to count do
    println(s"Hello, $name!")
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
object Hello {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
  }
}

// 또는 App 트레이트
object Hello extends App {
  println("Hello, World!")
}
```
{{% /tab %}}
{{< /tabs >}}

## 새로운 타입 기능 (Scala 3 전용)

### Union Types

```scala
// Scala 3
def process(input: Int | String): String = input match
  case i: Int    => s"숫자: $i"
  case s: String => s"문자열: $s"

// Scala 2에서는 Either 사용
def process(input: Either[Int, String]): String = input match {
  case Left(i)  => s"숫자: $i"
  case Right(s) => s"문자열: $s"
}
```

### Opaque Types

```scala
// Scala 3
object Money:
  opaque type USD = BigDecimal
  def usd(amount: BigDecimal): USD = amount
  extension (x: USD) def value: BigDecimal = x

// Scala 2에서는 Value Class 사용
case class USD(value: BigDecimal) extends AnyVal
```

## 마이그레이션 요약

| Scala 2 | Scala 3 |
|---------|---------|
| `implicit val` | `given` |
| `implicit def` (변환) | `given Conversion` |
| `(implicit x: T)` | `(using x: T)` |
| `implicitly[T]` | `summon[T]` |
| `implicit class` | `extension` |
| `sealed trait` + `case object` | `enum` |
| `_` (와일드카드 import) | `*` |

## 호환성

Scala 3에서도 Scala 2 문법 대부분이 동작합니다:

```scala
// Scala 3에서도 가능
implicit val x: Int = 42
def f(implicit n: Int): Int = n * 2

// 권장하지 않지만 동작함
for (i <- 1 to 5) { println(i) }
```

## 다음 단계

- [버전 비교](../../appendix/version-comparison/) — 전체 차이점 요약
- [마이그레이션 가이드](https://docs.scala-lang.org/scala3/guides/migration/compatibility-intro.html)
