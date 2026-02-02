---
lastmod: "2026-01-15"
title: 매크로와 메타프로그래밍
weight: 15
---

## 전체 비유: 3D 프린터 공장

메타프로그래밍을 <strong>3D 프린터가 있는 공장</strong>에 비유하면 이해하기 쉽습니다:

| 3D 프린터 비유 | Scala 개념 | 역할 |
|--------------|-----------|------|
| 도면 임베딩 | `inline` | 코드를 호출 지점에 직접 삽입 |
| 조건부 부품 선택 | `inline if/match` | 컴파일 타임 조건 분기 |
| 설계 검증 시스템 | `error`, `constValue` | 컴파일 타임 검증 |
| 3D 프린터 (부품 생성) | 매크로 (`${ }`) | 컴파일 타임 코드 생성 |
| 설계도 원본 | 인용 (`'{ }`) | 런타임 코드 표현 |
| 프린터 명령어 | `Expr[T]` | 타입 안전한 코드 조각 |

3D 프린터가 **설계도(인용)를 받아 실제 부품(코드)을 출력**하듯이, 매크로는 컴파일 타임에 코드를 분석하고 새로운 코드를 생성합니다. 핵심은 **런타임 전에 모든 준비를 마치는 것**입니다.

{{< callout type="info" title="TL;DR" >}}
- **inline**: 컴파일 타임에 코드를 인라인하여 런타임 오버헤드 제거
- **inline if/match**: 조건부 컴파일, 타입별 최적화
- **compiletime 패키지**: `error`, `constValue`, `summonInline` 등 컴파일 타임 연산
- **매크로 (`${ }`)**: 컴파일 타임 코드 생성, `scala.quoted` API 사용
- Scala 3 매크로는 Scala 2보다 타입 안전하고 간결
{{< /callout >}}

**대상 독자:** 타입 수준 프로그래밍에 익숙한 고급 개발자
**선수 지식:** 제네릭, 타입 클래스, 고급 타입 시스템

메타프로그래밍을 통해 컴파일 타임에 코드를 생성하거나 검증할 수 있습니다. Scala 3에서는 `inline`과 새로운 매크로 시스템을 제공합니다. 이 기능들을 활용하면 보일러플레이트 코드를 줄이고, 컴파일 시점에 최적화를 수행하며, 타입 안전한 코드 생성이 가능합니다.

> 📚 **사전 지식**: 이 문서는 고급 주제입니다. 다음 개념에 익숙해야 합니다:
> - [제네릭](generics/) - 타입 매개변수
> - [타입 클래스](type-classes/) - 타입 수준 추상화
> - [고급 타입](type-system-advanced/) - Match Types, Type Lambdas
>
> **난이도**: ⭐⭐⭐⭐⭐ (매우 고급)

#### Inline

`inline` 키워드로 컴파일 타임에 코드를 인라인합니다. 인라인된 함수는 호출 지점에서 함수 본문으로 대체되어 런타임 오버헤드가 제거됩니다.

**기본 사용**

```scala
// 메서드 인라이닝
inline def twice(x: Int): Int = x + x

val result = twice(21)  // 컴파일 시 42로 대체됨
```

**상수 폴딩**

`inline val`을 사용하면 상수가 컴파일 타임에 인라인됩니다. 이를 통해 상수 연산이 컴파일 시점에 계산되어 런타임 성능이 향상됩니다.

```scala
inline val Pi = 3.14159

// 컴파일 타임에 계산됨
inline def circleArea(radius: Double): Double =
  Pi * radius * radius

val area = circleArea(5)  // 78.53975로 컴파일됨
```

**조건부 컴파일**

`inline if`를 사용하면 조건에 따라 코드가 컴파일 타임에 선택됩니다. 조건이 false인 분기는 컴파일된 바이트코드에서 완전히 제거됩니다.

```scala
// 먼저 Config 객체 정의
object Config:
  inline val Debug = true  // 또는 false

inline def debug(inline msg: String): Unit =
  inline if Config.Debug then
    println(msg)
  else
    ()  // 컴파일 시 제거됨

debug("디버그 메시지")  // Config.Debug가 false면 코드 자체가 제거됨
```

{{< callout type="info" title="핵심 포인트" >}}
- `inline def`: 호출 지점에서 함수 본문으로 대체
- `inline val`: 상수가 컴파일 타임에 인라인
- `inline if`: 조건에 따라 코드가 컴파일 타임에 선택
{{< /callout >}}

#### Inline Match

컴파일 타임에 패턴 매칭을 수행합니다. `inline match`는 입력 타입에 따라 적절한 분기를 컴파일 타임에 선택하여 런타임 패턴 매칭 오버헤드를 제거합니다.

```scala
inline def toInt(x: Any): Int = inline x match
  case x: Int    => x
  case x: String => x.toInt
  case x: Double => x.toInt

toInt(42)      // 컴파일 시 Int 분기 선택
toInt("42")    // 컴파일 시 String 분기 선택
```

**타입별 최적화**

`transparent inline`은 반환 타입을 더 정밀하게 추론합니다. 각 타입에 대해 최적화된 코드가 생성되어 불필요한 변환이나 박싱이 제거됩니다.

```scala
transparent inline def stringify[T](x: T): String =
  inline x match
    case x: Int    => x.toString
    case x: String => x
    case x: Double => f"$x%.2f"
    case _         => x.toString

val s1: String = stringify(42)      // "42"
val s2: String = stringify("hello") // "hello"
val s3: String = stringify(3.14159) // "3.14"
```

{{< callout type="info" title="핵심 포인트" >}}
- `inline match`: 컴파일 타임 패턴 매칭
- `transparent inline`: 더 정밀한 반환 타입 추론
- 타입별로 최적화된 코드 생성
{{< /callout >}}

#### Compile-time Operations

Scala 3는 컴파일 타임 연산을 위한 다양한 기능을 제공합니다. `scala.compiletime` 패키지에는 컴파일 타임에 평가되는 유틸리티 함수들이 포함되어 있습니다.

**compiletime 패키지**

`error`, `constValue`, `summonInline` 등의 함수를 통해 컴파일 타임에 에러를 발생시키거나 값을 추출할 수 있습니다.

```scala
import scala.compiletime.*

// 컴파일 타임 에러
inline def checkPositive(inline n: Int): Int =
  inline if n <= 0 then
    error("n must be positive")
  else
    n

checkPositive(5)   // OK
// checkPositive(-1)  // 컴파일 에러: n must be positive
```

**constValue**

`constValue`는 리터럴 타입의 값을 컴파일 타임에 가져옵니다. 타입 수준에서 정의된 상수를 값 수준으로 가져올 때 유용합니다.

```scala
import scala.compiletime.constValue

// 리터럴 타입에서 값 추출
inline def literalValue[T <: Int]: Int = constValue[T]

val three = literalValue[3]  // 컴파일 시 3으로 대체

// 실용적인 예: 튜플 크기
import scala.compiletime.ops.int.*
type TupleSize[T <: Tuple] = T match
  case EmptyTuple => 0
  case h *: t => 1 + TupleSize[t]
```

**summonInline**

`summonInline`은 컴파일 타임에 타입 클래스 인스턴스를 소환합니다. 인스턴스가 없으면 컴파일 에러가 발생합니다.

```scala
import scala.compiletime.summonInline

trait Show[A]:
  def show(a: A): String

inline def show[A](a: A): String =
  summonInline[Show[A]].show(a)
```

{{< callout type="info" title="핵심 포인트" >}}
- `error`: 컴파일 타임 에러 발생
- `constValue`: 리터럴 타입에서 값 추출
- `summonInline`: 컴파일 타임 타입 클래스 소환
{{< /callout >}}

#### 매크로

Scala 3의 매크로는 `quotes` API를 사용합니다. 매크로를 통해 컴파일 타임에 코드를 분석하고 새로운 코드를 생성할 수 있습니다. `inline`보다 더 강력하지만 복잡도도 높습니다.

**간단한 매크로**

매크로는 `${ ... }` 스플라이스와 `'{ ... }` 인용을 통해 정의됩니다. 스플라이스는 컴파일 타임에 실행되고, 인용은 런타임 코드를 나타냅니다.

```scala
import scala.quoted.*

// 매크로 정의
inline def printCode(inline x: Any): Unit = ${ printCodeImpl('x) }

def printCodeImpl(x: Expr[Any])(using Quotes): Expr[Unit] =
  import quotes.reflect.*
  '{ println(${Expr(x.show)}) }

// 사용
printCode(1 + 2)  // "1 + 2" 출력
```

**표현식 생성**

`Expr` 타입을 사용하여 컴파일 타임에 표현식을 생성하고 조작할 수 있습니다. 이를 통해 타입 안전한 코드 생성이 가능합니다.

```scala
import scala.quoted.*

inline def toStringMacro[T](x: T): String = ${ toStringImpl('x) }

def toStringImpl[T: Type](x: Expr[T])(using Quotes): Expr[String] =
  '{ ${x}.toString }
```

{{< callout type="info" title="핵심 포인트" >}}
- `${ ... }`: 스플라이스, 컴파일 타임에 실행
- `'{ ... }`: 인용, 런타임 코드 표현
- `Expr[T]`: 타입 안전한 표현식 래퍼
{{< /callout >}}

#### Scala 2 vs Scala 3 매크로

Scala 2와 Scala 3의 매크로 시스템은 완전히 다릅니다. Scala 3에서는 타입 안전성이 크게 향상되었지만, Scala 2 매크로를 마이그레이션하려면 완전히 다시 작성해야 합니다. 아래 표는 두 버전의 주요 차이점을 정리한 것입니다.

| 특성 | Scala 2 | Scala 3 |
|------|---------|---------|
| API | scala.reflect.macros | scala.quoted |
| 안전성 | 낮음 | 높음 (Staged) |
| 복잡도 | 높음 | 상대적으로 낮음 |
| 마이그레이션 | - | 완전 재작성 필요 |

#### 실용적 사용 사례

매크로와 inline은 다양한 실용적 용도로 활용됩니다. 자동 로깅, 타입 이름 추출, 컴파일 타임 검증 등이 대표적인 예시입니다.

**1. 자동 로깅**

inline을 활용하면 디버깅을 위한 자동 로깅을 오버헤드 없이 구현할 수 있습니다.

```scala
inline def logged[T](inline block: T): T =
  val result = block
  println(s"Result: $result")
  result

val x = logged {
  val a = 1
  val b = 2
  a + b
}  // "Result: 3" 출력
```

**2. 타입 이름 출력**

`Mirror`와 `constValue`를 결합하면 타입의 이름을 컴파일 타임에 추출할 수 있습니다.

```scala
import scala.compiletime.constValue
import scala.deriving.Mirror

inline def typeName[T](using m: Mirror.Of[T]): String =
  constValue[m.MirroredLabel]

case class Person(name: String, age: Int)

typeName[Person]  // "Person"
```

**3. 컴파일 타임 검증**

`error` 함수를 사용하면 특정 조건에서 컴파일 에러를 발생시킬 수 있습니다. 이를 통해 잘못된 사용을 런타임이 아닌 컴파일 타임에 잡을 수 있습니다.

```scala
import scala.compiletime.error

inline def requirePositive(inline n: Int): Int =
  inline if n <= 0 then
    error("값은 양수여야 합니다")
  else
    n

val valid = requirePositive(5)    // OK
// val invalid = requirePositive(-1)  // 컴파일 에러
```

#### 모범 사례

매크로와 inline을 효과적으로 사용하기 위한 지침입니다.

**DO**

다음 상황에서는 inline과 매크로 사용이 권장됩니다.

- 성능이 중요한 작은 함수에 `inline` 사용
- 컴파일 타임 검증에 매크로 사용
- 보일러플레이트 코드 생성에 매크로 사용

**DON'T**

다음은 피해야 할 안티패턴입니다.

- 모든 함수를 `inline`으로 만들지 마세요 (컴파일 시간 증가)
- 복잡한 로직을 매크로로 구현하지 마세요
- 디버깅이 어려운 매크로 남용 피하기

#### 연습 문제

다음 연습 문제를 통해 inline과 매크로 개념을 복습해보세요.

**1. Compile-time 계산 ⭐⭐⭐**

피보나치 수를 컴파일 타임에 계산하는 `inline` 함수를 작성하세요.

<details>
<summary>정답 보기</summary>

```scala
inline def fib(inline n: Int): Int =
  inline if n <= 1 then n
  else fib(n - 1) + fib(n - 2)

val f10 = fib(10)  // 컴파일 시 55로 대체
```

</details>

#### 참고 자료

더 자세한 내용은 공식 문서를 참고하세요.

- [Scala 3 Metaprogramming](https://docs.scala-lang.org/scala3/reference/metaprogramming.html)
- [Inline](https://docs.scala-lang.org/scala3/reference/metaprogramming/inline.html)
- [Macros](https://docs.scala-lang.org/scala3/reference/metaprogramming/macros.html)

#### 관련 개념

메타프로그래밍은 다음 개념들과 밀접하게 연결됩니다:

| 관련 개념 | 연결 관계 |
|----------|----------|
| [제네릭]({{< relref "/docs/scala/concepts/generics" >}}) | 타입 매개변수와 inline 결합 |
| [타입 클래스]({{< relref "/docs/scala/concepts/type-classes" >}}) | `summonInline`으로 컴파일 타임 해결 |
| [고급 타입]({{< relref "/docs/scala/concepts/type-system-advanced" >}}) | Match Types와 inline match 연계 |
| [Implicits]({{< relref "/docs/scala/concepts/implicits" >}}) | given 인스턴스의 컴파일 타임 소환 |
| [패턴 매칭]({{< relref "/docs/scala/concepts/pattern-matching" >}}) | inline match로 컴파일 타임 분기 |

#### 다음 단계

| 학습 경로 | 설명 |
|----------|------|
| [동시성]({{< relref "/docs/scala/concepts/concurrency" >}}) | Future, Promise를 활용한 비동기 프로그래밍 |
| [함수형 패턴]({{< relref "/docs/scala/concepts/functional-patterns" >}}) | Functor, Monad 등 추상화 패턴 |
| [Scala 3 Metaprogramming 문서](https://docs.scala-lang.org/scala3/reference/metaprogramming.html) | 공식 레퍼런스 |
