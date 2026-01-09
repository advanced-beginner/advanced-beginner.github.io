---
lastmod: "2026-01-09"
title: Scala 2 vs Scala 3 비교
weight: 3
---

동일한 로직을 Scala 2와 Scala 3로 구현하여 차이점을 비교합니다. 두 버전 간의 문법 변화를 이해하면 코드를 마이그레이션하거나 양쪽 버전 모두에서 작업할 때 도움이 됩니다. 이 문서에서는 주요 변경 사항들을 실제 코드 예제로 보여줍니다.

#### 문법 스타일

Scala 3에서는 문법이 상당히 간소화되었습니다. 가장 눈에 띄는 변화는 들여쓰기 기반 구문의 도입과 일부 키워드의 변경입니다.

**블록 구문**

Scala 3에서는 중괄호 대신 들여쓰기로 블록을 구분할 수 있습니다. 이는 Python과 유사한 스타일로, 코드가 더 간결해집니다. Scala 2에서는 반드시 중괄호를 사용해야 합니다.

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

**if 표현식**

Scala 3에서는 if 조건 주위의 괄호가 선택 사항이 되었고, `then` 키워드가 도입되었습니다. 이로 인해 조건문이 더 자연스러운 영어 문장처럼 읽힙니다.

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

**for 표현식**

for 표현식도 마찬가지로 들여쓰기 기반 구문을 지원합니다. `do` 키워드는 부수 효과를 위한 for 루프에서, `yield`는 새 컬렉션을 생성할 때 사용합니다.

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

#### 열거형 (Enum)

Scala 3에서 가장 환영받는 변화 중 하나는 네이티브 enum 지원입니다. Scala 2에서는 sealed trait과 case object/class를 조합해야 했던 패턴이 단일 enum 선언으로 간소화되었습니다.

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

#### 암시적 기능

Scala 3에서 가장 큰 변화는 암시적 기능의 재설계입니다. `implicit` 키워드는 여러 역할을 했기 때문에 혼란스러웠지만, Scala 3에서는 각 용도에 맞는 별도의 키워드로 분리되었습니다.

**암시적 값과 매개변수**

암시적 값 정의에는 `given`, 암시적 매개변수에는 `using` 키워드를 사용합니다. 이로 인해 코드의 의도가 더 명확해집니다.

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

**확장 메서드**

확장 메서드는 기존 타입에 새 메서드를 추가하는 기능입니다. Scala 3에서는 `extension` 키워드로 이를 명시적으로 표현합니다. Scala 2에서는 implicit class 패턴을 사용했습니다.

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

**타입 클래스**

타입 클래스 패턴도 `given`과 `using`을 통해 더 간결하게 표현됩니다. `implicitly` 대신 `summon`을 사용하여 암시적 인스턴스를 조회합니다.

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

#### 진입점

애플리케이션의 진입점 정의 방식도 간소화되었습니다. Scala 3에서는 `@main` 어노테이션을 사용하여 더 간단하게 메인 함수를 정의할 수 있으며, 명령줄 인자도 자동으로 파싱됩니다.

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

#### 새로운 타입 기능 (Scala 3 전용)

Scala 3에서는 타입 시스템이 크게 확장되었습니다. Union Type과 Opaque Type은 Scala 3에서만 사용할 수 있는 새로운 기능입니다.

**Union Types**

Union Type은 여러 타입 중 하나일 수 있는 값을 표현합니다. Scala 2에서는 Either를 사용해야 했지만, Scala 3에서는 `|` 연산자로 간단히 표현할 수 있습니다.

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

**Opaque Types**

Opaque Type은 런타임 오버헤드 없이 타입 안전성을 제공합니다. 외부에서는 다른 타입으로 보이지만 내부에서는 기반 타입과 동일하게 처리됩니다.

```scala
// Scala 3
object Money:
  opaque type USD = BigDecimal
  def usd(amount: BigDecimal): USD = amount
  extension (x: USD) def value: BigDecimal = x

// Scala 2에서는 Value Class 사용
case class USD(value: BigDecimal) extends AnyVal
```

#### 마이그레이션 요약

Scala 2에서 Scala 3로 마이그레이션할 때 참고할 주요 변경 사항을 정리했습니다. 대부분의 변경은 더 명확하고 일관된 문법을 위한 것입니다.

| Scala 2 | Scala 3 |
|---------|---------|
| `implicit val` | `given` |
| `implicit def` (변환) | `given Conversion` |
| `(implicit x: T)` | `(using x: T)` |
| `implicitly[T]` | `summon[T]` |
| `implicit class` | `extension` |
| `sealed trait` + `case object` | `enum` |
| `_` (와일드카드 import) | `*` |

#### 호환성

Scala 3 컴파일러는 하위 호환성을 위해 Scala 2 문법의 대부분을 여전히 지원합니다. 따라서 기존 코드를 점진적으로 마이그레이션할 수 있습니다.

```scala
// Scala 3에서도 가능
implicit val x: Int = 42
def f(implicit n: Int): Int = n * 2

// 권장하지 않지만 동작함
for (i <- 1 to 5) { println(i) }
```

이 호환성 덕분에 Scala 2 코드베이스를 Scala 3로 마이그레이션할 때 모든 것을 한 번에 바꿀 필요가 없습니다. 새로운 코드는 Scala 3 스타일로 작성하면서 기존 코드는 점진적으로 업데이트할 수 있습니다.

#### 다음 단계

더 자세한 버전 비교와 마이그레이션 정보는 다음 자료를 참고하세요.

- [버전 비교](../../appendix/version-comparison/) — 전체 차이점 요약
- [마이그레이션 가이드](https://docs.scala-lang.org/scala3/guides/migration/compatibility-intro.html)

