---
lastmod: "2026-01-06"
title: Scala 2 vs Scala 3 버전 비교
weight: 2
---

Scala 2와 Scala 3의 주요 차이점을 한눈에 정리합니다.

## 새로운 기능 (Scala 3)

### 문법 개선

| 기능 | Scala 2 | Scala 3 |
|------|---------|---------|
| 블록 구문 | 중괄호 필수 | 들여쓰기 기반 옵션 |
| if 조건 | `if (cond)` | `if cond then` |
| for 루프 | `for (x <- list)` | `for x <- list do` |
| match | 중괄호 필수 | 들여쓰기 기반 |
| 와일드카드 import | `import pkg._` | `import pkg.*` |

### 열거형

```scala
// Scala 3
enum Color:
  case Red, Green, Blue

enum Planet(val mass: Double):
  case Earth extends Planet(5.97e24)

// Scala 2
sealed trait Color
object Color {
  case object Red extends Color
  case object Green extends Color
  case object Blue extends Color
}
```

### 타입 시스템

| 기능 | Scala 2 | Scala 3 |
|------|---------|---------|
| Union Types | Either 사용 | `A \| B` |
| Intersection Types | `A with B` | `A & B` |
| Opaque Types | Value Class | `opaque type` |
| Match Types | 불가 | 지원 |
| Type Lambdas | 복잡한 문법 | `[X] =>> F[X]` |

### 암시적 기능

| 기능 | Scala 2 | Scala 3 |
|------|---------|---------|
| 암시적 값 | `implicit val` | `given` |
| 암시적 매개변수 | `(implicit x: T)` | `(using x: T)` |
| 암시적 조회 | `implicitly[T]` | `summon[T]` |
| 확장 메서드 | `implicit class` | `extension` |
| 암시적 변환 | `implicit def` | `given Conversion` |

### 메타프로그래밍

| 기능 | Scala 2 | Scala 3 |
|------|---------|---------|
| 인라이닝 | `@inline` (힌트) | `inline` (보장) |
| 매크로 API | scala.reflect | scala.quoted |
| 컴파일 타임 연산 | 제한적 | compiletime 패키지 |

## 변경된 기능

### 트레이트 매개변수

```scala
// Scala 3에서 트레이트도 매개변수 가능
trait Greeting(val name: String):
  def greet(): String = s"Hello, $name!"

class Person extends Greeting("World")

// Scala 2에서는 불가 - abstract class 필요
```

### 진입점

```scala
// Scala 3
@main def hello(): Unit = println("Hello!")

@main def greet(name: String): Unit = println(s"Hello, $name!")

// Scala 2
object Hello {
  def main(args: Array[String]): Unit = println("Hello!")
}
```

### Creator Applications

```scala
// Scala 3: new 없이 클래스 인스턴스 생성
class Person(name: String)
val p = Person("Alice")  // new 없이!

// Scala 2: new 필요 (case class가 아니면)
val p = new Person("Alice")
```

## 제거된 기능

다음 기능은 Scala 3에서 제거되었습니다:

| 제거된 기능 | 대안 |
|------------|------|
| 절차적 문법 (`def f() { }`) | `def f(): Unit = { }` |
| `do-while` | `while` + 조건 변수 |
| XML 리터럴 | 라이브러리 사용 |
| 기호 리터럴 (`'symbol`) | 문자열 |
| `DelayedInit` | 일반 생성자 |
| 자동 적용 `()` | 명시적 호출 |
| `private[this]` | `private` |
| `protected[this]` | `protected` |

## 호환성

### Scala 2 라이브러리 사용

Scala 3 프로젝트에서 Scala 2.13 라이브러리 사용 가능:

```scala
libraryDependencies += "org.typelevel" % "cats-core_2.13" % "2.10.0"
```

### Cross-building

```scala
// build.sbt
scalaVersion := "3.3.1"
crossScalaVersions := Seq("2.13.12", "3.3.1")
```

### 마이그레이션 모드

```scala
// build.sbt
scalacOptions ++= Seq(
  "-source:3.0-migration",
  "-rewrite"
)
```

## 권장 사항

### 새 프로젝트

- **Scala 3 권장**: 새로운 기능, 더 나은 에러 메시지, 개선된 타입 추론

### 기존 프로젝트

- **점진적 마이그레이션**: `-source:3.0-migration` 옵션 사용
- **의존성 확인**: 주요 라이브러리의 Scala 3 지원 여부 확인
- **테스트 유지**: 마이그레이션 전후 테스트 통과 확인

### Spark 사용

- **Scala 2.12/2.13 유지**: Spark는 아직 Scala 3 미지원 (2024년 기준)

## 참고 자료

- [Scala 3 Migration Guide](https://docs.scala-lang.org/scala3/guides/migration/compatibility-intro.html)
- [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/)
- [Changed Features](https://docs.scala-lang.org/scala3/reference/changed-features.html)
- [Dropped Features](https://docs.scala-lang.org/scala3/reference/dropped-features.html)
