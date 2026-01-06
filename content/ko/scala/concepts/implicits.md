---
lastmod: "2026-01-06"
title: Implicit / Given
weight: 11
---

암시적 기능은 Scala의 강력한 기능 중 하나입니다. Scala 2의 `implicit`과 Scala 3의 `given`/`using`을 모두 다룹니다.

## Scala 2: Implicit

### Implicit 값

```scala
// 암시적 값 정의
implicit val defaultName: String = "Guest"

// 암시적 매개변수 사용
def greet(implicit name: String): String = s"Hello, $name!"

greet              // "Hello, Guest!" (암시적으로 전달)
greet("Alice")     // "Hello, Alice!" (명시적 전달)
```

### Implicit 매개변수

```scala
case class Config(url: String, timeout: Int)

implicit val defaultConfig: Config = Config("localhost", 5000)

def connect(implicit config: Config): Unit =
  println(s"Connecting to ${config.url} with timeout ${config.timeout}")

connect  // 암시적 Config 사용
```

### Implicit 변환

```scala
// Int에서 String으로 암시적 변환
implicit def intToString(i: Int): String = i.toString

val s: String = 42  // 자동으로 "42"로 변환

// 위험할 수 있으므로 주의해서 사용!
```

### Implicit 클래스 (확장 메서드)

```scala
implicit class RichString(s: String) {
  def exclaim: String = s + "!"
  def words: List[String] = s.split(" ").toList
}

"Hello".exclaim          // "Hello!"
"Hello World".words      // List("Hello", "World")
```

## Scala 3: Given / Using

Scala 3에서는 `implicit`이 더 명확한 키워드들로 분리되었습니다.

### Given 인스턴스

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// given으로 인스턴스 정의
given defaultName: String = "Guest"

// using으로 사용
def greet(using name: String): String = s"Hello, $name!"

greet              // "Hello, Guest!"
greet(using "Alice") // "Hello, Alice!"
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
implicit val defaultName: String = "Guest"

def greet(implicit name: String): String = s"Hello, $name!"

greet              // "Hello, Guest!"
greet("Alice")     // "Hello, Alice!"
```
{{% /tab %}}
{{< /tabs >}}

### 익명 Given

```scala
// 이름 없는 given
given String = "Guest"

// 타입만으로 참조
summon[String]  // "Guest"
```

### Using 절

```scala
case class Config(url: String, timeout: Int)

given Config = Config("localhost", 5000)

def connect(using config: Config): Unit =
  println(s"Connecting to ${config.url}")

connect  // Config를 암시적으로 사용
```

### Extension 메서드 (Scala 3)

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
extension (s: String)
  def exclaim: String = s + "!"
  def words: List[String] = s.split(" ").toList
  def repeatN(n: Int): String = s * n

"Hello".exclaim      // "Hello!"
"Hello".repeatN(3)   // "HelloHelloHello"
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
implicit class StringOps(s: String) {
  def exclaim: String = s + "!"
  def words: List[String] = s.split(" ").toList
  def repeatN(n: Int): String = s * n
}

"Hello".exclaim      // "Hello!"
"Hello".repeatN(3)   // "HelloHelloHello"
```
{{% /tab %}}
{{< /tabs >}}

## 타입 클래스 패턴

### 정의

{{< tabs groupid="scala-version" >}}
{{% tab title="Scala 3" %}}
```scala
// 타입 클래스 정의
trait Show[A]:
  def show(a: A): String

// 인스턴스 정의
given Show[Int] with
  def show(a: Int): String = a.toString

given Show[String] with
  def show(a: String): String = s"\"$a\""

// 사용
def print[A](a: A)(using s: Show[A]): Unit =
  println(s.show(a))

print(42)       // "42"
print("hello")  // "\"hello\""
```
{{% /tab %}}
{{% tab title="Scala 2" %}}
```scala
// 타입 클래스 정의
trait Show[A] {
  def show(a: A): String
}

// 인스턴스 정의
implicit val intShow: Show[Int] = new Show[Int] {
  def show(a: Int): String = a.toString
}

implicit val stringShow: Show[String] = new Show[String] {
  def show(a: String): String = s"\"$a\""
}

// 사용
def print[A](a: A)(implicit s: Show[A]): Unit =
  println(s.show(a))

print(42)       // "42"
print("hello")  // "\"hello\""
```
{{% /tab %}}
{{< /tabs >}}

### 컨텍스트 경계

```scala
// 컨텍스트 경계 문법
def print[A: Show](a: A): Unit = {
  val s = summon[Show[A]]  // Scala 3
  // val s = implicitly[Show[A]]  // Scala 2
  println(s.show(a))
}
```

## 암시적 범위 (Implicit Scope)

암시적 값은 다음 순서로 검색됩니다:

1. **현재 범위** - 지역 변수, import된 암시적
2. **연관 타입의 컴패니언 객체** - 타입 매개변수, 부모 타입 등

```scala
case class User(name: String)

object User {
  // 컴패니언 객체에 암시적 정의
  implicit val ordering: Ordering[User] =
    Ordering.by(_.name)
}

// 자동으로 User.ordering을 찾음
List(User("Bob"), User("Alice")).sorted
// List(User("Alice"), User("Bob"))
```

## Given Import (Scala 3)

```scala
object Givens:
  given Int = 42
  given String = "hello"
  val normalValue = 100

// 특정 타입의 given만 import (중괄호 사용)
import Givens.{given Int}

// 모든 given import
import Givens.given

// 일반 멤버와 given 모두 import
import Givens.*       // normalValue만 import
import Givens.given   // given Int, given String만 import

// 둘 다 필요하면
import Givens.{*, given}
```

> 💡 **Scala 2와 차이점:** Scala 2에서는 `import Givens._`로 implicit도 함께 import되었지만, Scala 3에서는 `given`을 명시적으로 import해야 합니다.

## 마이그레이션 가이드

| Scala 2 | Scala 3 |
|---------|---------|
| `implicit val x: T = ...` | `given x: T = ...` |
| `implicit def f: T = ...` | `given f: T = ...` |
| `def f(implicit x: T)` | `def f(using x: T)` |
| `implicitly[T]` | `summon[T]` |
| `implicit class` | `extension` |

### 점진적 마이그레이션

Scala 3에서도 `implicit`을 사용할 수 있습니다:

```scala
// Scala 3에서도 동작
implicit val x: Int = 42
def f(implicit n: Int): Int = n * 2
```

## 모범 사례

### DO

```scala
// 타입 클래스에 사용
given Ordering[MyClass] = ???

// 설정/컨텍스트 전달
def process(data: Data)(using config: Config): Result = ???

// 확장 메서드
extension (s: String)
  def toSlug: String = s.toLowerCase.replace(" ", "-")
```

### DON'T

```scala
// 무분별한 암시적 변환 (피하세요)
given Conversion[Int, String] = _.toString

// 너무 일반적인 타입의 암시적 (피하세요)
given String = "default"  // 어디서나 String이 필요하면 사용됨
```

## 흔한 실수와 Anti-patterns

### ❌ 피해야 할 것

```scala
// 1. 너무 일반적인 타입의 암시적 정의
implicit val defaultString: String = "hello"
// 모든 곳에서 String이 필요하면 이 값이 주입됨!

// 2. 무분별한 암시적 변환
implicit def stringToInt(s: String): Int = s.toInt
val x: Int = "123"  // 암시적으로 변환됨 - 위험!
val y: Int = "abc"  // NumberFormatException!

// 3. 암시적 범위 충돌
import library1._
import library2._  // 둘 다 같은 타입의 implicit 정의
// "ambiguous implicit values" 에러!

// 4. 복잡한 암시적 체인
// A → B → C → D 변환이 필요하면 컴파일 시간이 급격히 증가
```

### ✅ 올바른 방법

```scala
// 1. 구체적인 래퍼 타입 사용
case class AppConfig(dbUrl: String, timeout: Int)
given AppConfig = AppConfig("localhost", 5000)

// 2. 암시적 변환 대신 extension 메서드
extension (s: String)
  def toIntSafe: Option[Int] = s.toIntOption

"123".toIntSafe  // Some(123)
"abc".toIntSafe  // None

// 3. 명시적 import로 충돌 해결
import library1.{given OrderingInstance}  // 특정 given만 import

// 4. 단순한 타입 클래스 계층 유지
trait Show[A]:
  def show(a: A): String

// 파생 인스턴스는 한 단계로 제한
given [A: Show]: Show[List[A]] = ...
```

### 디버깅 팁

```scala
// 어떤 implicit이 선택되었는지 확인
// scalac: -Xprint:typer
// sbt: set scalacOptions += "-Xprint:typer"

// Scala 3에서 summon 사용
val ord = summon[Ordering[Int]]
println(ord)  // scala.math.Ordering$Int$@...
```

## 연습 문제

### 1. Printable 타입 클래스

`Printable` 타입 클래스를 정의하고 `Int`, `String`, `List[A]`에 대한 인스턴스를 구현하세요.

<details>
<summary>정답 보기</summary>

```scala
// Scala 3
trait Printable[A]:
  def format(a: A): String

given Printable[Int] with
  def format(a: Int): String = a.toString

given Printable[String] with
  def format(a: String): String = s"\"$a\""

given [A](using p: Printable[A]): Printable[List[A]] with
  def format(list: List[A]): String =
    list.map(p.format).mkString("[", ", ", "]")

def print[A](a: A)(using p: Printable[A]): Unit =
  println(p.format(a))

print(42)                    // 42
print("hello")               // "hello"
print(List(1, 2, 3))         // [1, 2, 3]
print(List("a", "b", "c"))   // ["a", "b", "c"]
```

</details>

### 2. Extension 메서드 구현

`Int`에 `times` 메서드를 추가하세요: `3.times { println("Hello") }`

<details>
<summary>정답 보기</summary>

```scala
extension (n: Int)
  def times(action: => Unit): Unit =
    for _ <- 1 to n do action

3.times {
  println("Hello")
}
// Hello
// Hello
// Hello
```

</details>

## 다음 단계

- [타입 클래스](../type-classes/) — 타입 클래스 패턴 심화
- [함수형 패턴](../functional-patterns/) — Functor, Monad
