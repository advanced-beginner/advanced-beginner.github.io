---
lastmod: "2026-01-10"
title: 고급 타입 시스템
weight: 14
---

{{< callout type="info" title="TL;DR" >}}
- **Union Types (`|`)**: 여러 타입 중 하나, `Int | String`
- **Intersection Types (`&`)**: 여러 타입 모두 만족, `A & B`
- **Opaque Types**: 런타임 오버헤드 없는 타입 안전성
- **Match Types**: 타입 수준 패턴 매칭
- Scala 3 전용 기능으로 더 강력한 타입 표현 가능
{{< /callout >}}

**대상 독자:** 제네릭과 변성을 이해한 Scala 개발자
**선수 지식:** 타입 매개변수, 타입 경계, 변성

Scala 3는 더욱 강력하고 표현력 있는 타입 시스템을 제공합니다. 이 문서에서는 Scala 3의 새로운 타입 기능을 다룹니다.

> 📚 **사전 지식**: 이 문서를 이해하려면 다음 개념에 익숙해야 합니다:
> - [제네릭](../generics/) - 타입 매개변수, 타입 경계
> - [공변성/반공변성](../variance/) - 변성 개념
> - [패턴 매칭](../pattern-matching/) - 기본 패턴 매칭
>
> **난이도**: ⭐⭐⭐⭐ (고급) - Scala 3 전용 기능 포함

#### Union Types (|)

Union Type은 여러 타입 중 하나의 값을 가질 수 있음을 나타냅니다. 래퍼 없이 직접 값을 다룰 수 있어 Either보다 간결합니다.

```scala
// Scala 3만 지원
def process(input: Int | String): String = input match
  case i: Int    => s"숫자: $i"
  case s: String => s"문자열: $s"

process(42)        // "숫자: 42"
process("hello")   // "문자열: hello"

// 여러 타입
type JsonValue = String | Int | Boolean | Null
def toJson(value: JsonValue): String = value match
  case s: String  => s"\"$s\""
  case i: Int     => i.toString
  case b: Boolean => b.toString
  case null       => "null"
```

**Either와의 비교**

```scala
// Either: 명시적인 Left/Right 래퍼 필요
def divideEither(a: Int, b: Int): Either[String, Int] =
  if b == 0 then Left("0으로 나눌 수 없음") else Right(a / b)

// Union: 래퍼 없이 직접 값 반환
def divideUnion(a: Int, b: Int): Int | String =
  if b == 0 then "0으로 나눌 수 없음" else a / b
```

{{< callout type="info" title="핵심 포인트" >}}
- Union Type은 `A | B` 형태로 여러 타입 중 하나를 표현
- Either와 달리 래퍼 없이 직접 값을 다룸
- 패턴 매칭으로 타입을 구분하여 처리
{{< /callout >}}

#### Intersection Types (&)

Intersection Type은 여러 타입을 모두 만족하는 타입입니다. 객체가 여러 trait를 동시에 구현해야 할 때 유용합니다.

```scala
trait Printable:
  def print(): String

trait Serializable:
  def serialize(): Array[Byte]

// Printable이면서 Serializable인 타입
def process(obj: Printable & Serializable): Unit =
  println(obj.print())
  val bytes = obj.serialize()

// 구현
class Document(content: String) extends Printable, Serializable:
  def print(): String = content
  def serialize(): Array[Byte] = content.getBytes

process(Document("Hello"))
```

**구조적 타입 결합**

```scala
type Named = { def name: String }
type Aged = { def age: Int }

def describe(obj: Named & Aged): String =
  s"${obj.name}, ${obj.age}세"
```

{{< callout type="info" title="핵심 포인트" >}}
- Intersection Type은 `A & B` 형태로 여러 타입을 모두 만족
- 객체가 여러 trait를 동시에 구현해야 할 때 사용
- 구조적 타입과 결합하여 유연한 타입 정의 가능
{{< /callout >}}

#### Opaque Types

Opaque Type은 타입 별칭이지만 외부에서는 다른 타입으로 취급됩니다. 런타임 오버헤드 없이 타입 안전성을 제공하여 도메인 모델링에 매우 유용합니다.

```scala
object UserId:
  opaque type UserId = Long

  def apply(id: Long): UserId = id

  extension (id: UserId)
    def value: Long = id
    def isValid: Boolean = id > 0

import UserId.*

val id: UserId = UserId(42)
id.value       // 42
id.isValid     // true

// val x: Long = id  // 컴파일 에러! UserId != Long
```

**장점**

- 런타임 오버헤드 없음 (박싱 없음)
- 타입 안전성 제공
- 도메인 모델링에 유용

```scala
object Money:
  opaque type USD = BigDecimal
  opaque type EUR = BigDecimal

  def usd(amount: BigDecimal): USD = amount
  def eur(amount: BigDecimal): EUR = amount

  extension (x: USD)
    def +(y: USD): USD = x + y
    def value: BigDecimal = x

import Money.*

val dollars = usd(100)
val more = usd(50)
val total = dollars + more  // OK: USD + USD

// val mixed = dollars + eur(50)  // 컴파일 에러! USD + EUR
```

{{< callout type="info" title="핵심 포인트" >}}
- Opaque Type은 외부에서는 다른 타입으로 취급되는 타입 별칭
- 런타임 오버헤드 없이 타입 안전성 제공 (박싱 없음)
- 도메인 모델링에서 타입 혼동 방지에 유용
{{< /callout >}}

#### Match Types

Match Type은 타입 레벨에서 패턴 매칭을 수행합니다. 입력 타입에 따라 다른 결과 타입을 계산할 수 있습니다.

```scala
type Elem[X] = X match
  case String      => Char
  case Array[t]    => t
  case Iterable[t] => t

val a: Elem[String] = 'a'        // Char
val b: Elem[Array[Int]] = 1      // Int
val c: Elem[List[String]] = "hi" // String
```

**재귀 Match Types**

```scala
type Flatten[X] = X match
  case List[List[t]] => Flatten[List[t]]
  case List[t]       => List[t]

// List[List[List[Int]]] -> List[Int]
val x: Flatten[List[List[List[Int]]]] = List(1, 2, 3)
```

{{< callout type="info" title="핵심 포인트" >}}
- Match Type은 타입 수준에서 패턴 매칭 수행
- 입력 타입에 따라 다른 결과 타입 계산
- 재귀적 Match Types로 복잡한 타입 변환 가능
{{< /callout >}}

#### Type Lambdas

Type Lambda는 타입 생성자를 타입 매개변수로 전달할 수 있게 해줍니다. 고차 타입을 다룰 때 유용합니다.

```scala
// 타입 람다: [X] =>> F[X, Y]
type IntMap = [V] =>> Map[Int, V]

val map: IntMap[String] = Map(1 -> "one", 2 -> "two")
// 실제로는 Map[Int, String]

// Either를 고정된 에러 타입으로
type Result = [A] =>> Either[String, A]

val ok: Result[Int] = Right(42)
val err: Result[Int] = Left("에러")
```

{{< callout type="info" title="핵심 포인트" >}}
- Type Lambda는 `[X] =>> F[X]` 형태로 타입 생성자 표현
- 고차 타입을 다룰 때 타입 매개변수 부분 적용에 유용
- Scala 2의 복잡한 문법을 간결하게 대체
{{< /callout >}}

#### Dependent Function Types

Dependent Function Type은 반환 타입이 매개변수 값에 의존하는 함수 타입입니다.

```scala
trait Key:
  type Value

val intKey = new Key { type Value = Int }
val strKey = new Key { type Value = String }

// 반환 타입이 key의 Value 타입에 의존
def get(key: Key): key.Value = ???

// 의존 함수 타입
val getter: (key: Key) => key.Value = (key: Key) => ???
```

#### Polymorphic Function Types

Polymorphic Function Type은 타입 매개변수를 가진 함수 타입입니다. 함수 값 자체가 제네릭할 수 있습니다.

```scala
// 다형 함수 타입
val identity: [A] => A => A = [A] => (a: A) => a

identity[Int](42)       // 42
identity[String]("hi")  // "hi"

// 리스트의 첫 번째 요소
val head: [A] => List[A] => A = [A] => (list: List[A]) => list.head

head(List(1, 2, 3))  // 1
```

#### Scala 2와의 비교

아래 표는 Scala 2와 Scala 3의 고급 타입 기능 차이를 정리한 것입니다.

| 기능 | Scala 2 | Scala 3 |
|------|---------|---------|
| Union Types | Either 사용 | `A \| B` |
| Intersection | `A with B` | `A & B` |
| Opaque Types | Value Class | `opaque type` |
| Match Types | 불가 | 지원 |
| Type Lambdas | 복잡한 문법 | `[X] =>> F[X]` |

#### 연습 문제

**1. Opaque Type 구현 ⭐⭐**

`Email` opaque type을 구현하세요. 유효성 검사를 포함해야 합니다.

<details>
<summary>정답 보기</summary>

```scala
object Email:
  opaque type Email = String

  def apply(value: String): Option[Email] =
    if value.contains("@") && value.contains(".") then Some(value)
    else None

  def unsafe(value: String): Email = value

  extension (email: Email)
    def value: String = email
    def domain: String = email.split("@").last

import Email.*

val valid = Email("user@example.com")   // Some(Email)
val invalid = Email("invalid")          // None

valid.foreach(e => println(e.domain))   // "example.com"
```

</details>

#### 다음 단계

- [매크로](../macros-metaprogramming/) — 컴파일 타임 코드 생성
- [함수형 패턴](../functional-patterns/) — Functor, Monad
