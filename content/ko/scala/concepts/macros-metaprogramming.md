---
lastmod: "2026-01-06"
title: 매크로와 메타프로그래밍
weight: 15
---

메타프로그래밍을 통해 컴파일 타임에 코드를 생성하거나 검증할 수 있습니다. Scala 3에서는 `inline`과 새로운 매크로 시스템을 제공합니다.

> 📚 **사전 지식**: 이 문서는 고급 주제입니다. 다음 개념에 익숙해야 합니다:
> - [제네릭](../generics/) - 타입 매개변수
> - [타입 클래스](../type-classes/) - 타입 수준 추상화
> - [고급 타입](../type-system-advanced/) - Match Types, Type Lambdas
>
> **난이도**: ⭐⭐⭐⭐⭐ (매우 고급)

## Inline

`inline` 키워드로 컴파일 타임에 코드를 인라인합니다.

### 기본 사용

```scala
// 메서드 인라이닝
inline def twice(x: Int): Int = x + x

val result = twice(21)  // 컴파일 시 42로 대체됨
```

### 상수 폴딩

```scala
inline val Pi = 3.14159

// 컴파일 타임에 계산됨
inline def circleArea(radius: Double): Double =
  Pi * radius * radius

val area = circleArea(5)  // 78.53975로 컴파일됨
```

### 조건부 컴파일

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

## Inline Match

컴파일 타임에 패턴 매칭을 수행합니다.

```scala
inline def toInt(x: Any): Int = inline x match
  case x: Int    => x
  case x: String => x.toInt
  case x: Double => x.toInt

toInt(42)      // 컴파일 시 Int 분기 선택
toInt("42")    // 컴파일 시 String 분기 선택
```

### 타입별 최적화

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

## Compile-time Operations

### compiletime 패키지

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

### constValue

리터럴 타입의 값을 컴파일 타임에 가져옵니다.

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

### summonInline

```scala
import scala.compiletime.summonInline

trait Show[A]:
  def show(a: A): String

inline def show[A](a: A): String =
  summonInline[Show[A]].show(a)
```

## 매크로

Scala 3의 매크로는 `quotes` API를 사용합니다.

### 간단한 매크로

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

### 표현식 생성

```scala
import scala.quoted.*

inline def toStringMacro[T](x: T): String = ${ toStringImpl('x) }

def toStringImpl[T: Type](x: Expr[T])(using Quotes): Expr[String] =
  '{ ${x}.toString }
```

## Scala 2 vs Scala 3 매크로

| 특성 | Scala 2 | Scala 3 |
|------|---------|---------|
| API | scala.reflect.macros | scala.quoted |
| 안전성 | 낮음 | 높음 (Staged) |
| 복잡도 | 높음 | 상대적으로 낮음 |
| 마이그레이션 | - | 완전 재작성 필요 |

## 실용적 사용 사례

### 1. 자동 로깅

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

### 2. 타입 이름 출력

```scala
import scala.compiletime.constValue
import scala.deriving.Mirror

inline def typeName[T](using m: Mirror.Of[T]): String =
  constValue[m.MirroredLabel]

case class Person(name: String, age: Int)

typeName[Person]  // "Person"
```

### 3. 컴파일 타임 검증

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

## 모범 사례

### DO

- 성능이 중요한 작은 함수에 `inline` 사용
- 컴파일 타임 검증에 매크로 사용
- 보일러플레이트 코드 생성에 매크로 사용

### DON'T

- 모든 함수를 `inline`으로 만들지 마세요 (컴파일 시간 증가)
- 복잡한 로직을 매크로로 구현하지 마세요
- 디버깅이 어려운 매크로 남용 피하기

## 연습 문제

### 1. Compile-time 계산 ⭐⭐⭐

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

## 참고 자료

- [Scala 3 Metaprogramming](https://docs.scala-lang.org/scala3/reference/metaprogramming.html)
- [Inline](https://docs.scala-lang.org/scala3/reference/metaprogramming/inline.html)
- [Macros](https://docs.scala-lang.org/scala3/reference/metaprogramming/macros.html)

## 다음 단계

- [동시성](../concurrency/) — Future, Promise
- [함수형 패턴](../functional-patterns/) — Functor, Monad
