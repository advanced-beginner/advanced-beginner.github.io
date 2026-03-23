---
lastmod: "2026-01-10"
title: Scala 2 vs Scala 3 버전 비교
description: "Scala 2와 Scala 3의 주요 변경사항을 비교 설명합니다."
weight: 2
---

Scala 2와 Scala 3의 주요 차이점을 한눈에 정리합니다. Scala 3는 2020년에 출시되어 더 간결한 문법, 강력한 타입 시스템, 개선된 암시적 기능을 제공합니다.

{{< callout type="tip" title="TL;DR" >}}
- **새 프로젝트**: Scala 3 권장 (더 간결한 문법, 개선된 타입 시스템)
- **Spark 사용**: Scala 2.12/2.13 유지 (Spark가 아직 Scala 3 미지원)
- **핵심 변경**: `implicit` → `given`/`using`, 들여쓰기 기반 문법, `enum` 추가
- **호환성**: Scala 3에서 Scala 2.13 라이브러리 사용 가능
- **마이그레이션**: `-source:3.0-migration` 옵션으로 점진적 전환
{{< /callout >}}

#### 새로운 기능 (Scala 3)

Scala 3에서 추가된 주요 기능들을 카테고리별로 살펴봅니다.

**문법 개선**

Scala 3는 중괄호 대신 들여쓰기 기반 문법을 선택적으로 사용할 수 있어 코드가 더 간결해졌습니다. 아래 표는 주요 문법 차이를 보여줍니다.

| 기능 | Scala 2 | Scala 3 |
|------|---------|---------|
| 블록 구문 | 중괄호 필수 | 들여쓰기 기반 옵션 |
| if 조건 | `if (cond)` | `if cond then` |
| for 루프 | `for (x <- list)` | `for x <- list do` |
| match | 중괄호 필수 | 들여쓰기 기반 |
| 와일드카드 import | `import pkg._` | `import pkg.*` |

**열거형**

Scala 3의 `enum`은 열거형을 간결하게 정의합니다. Scala 2에서는 sealed trait과 case object 조합이 필요했습니다.

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

**타입 시스템**

Scala 3는 Union Types, Intersection Types, Match Types 등 강력한 타입 기능을 추가했습니다. 아래 표는 타입 시스템의 주요 차이점입니다.

| 기능 | Scala 2 | Scala 3 |
|------|---------|---------|
| Union Types | Either 사용 | `A \| B` |
| Intersection Types | `A with B` | `A & B` |
| Opaque Types | Value Class | `opaque type` |
| Match Types | 불가 | 지원 |
| Type Lambdas | 복잡한 문법 | `[X] =>> F[X]` |

**암시적 기능**

Scala 3는 암시적 기능을 `given`/`using`으로 재설계하여 의도가 더 명확해졌습니다. 아래 표는 암시적 기능의 문법 변화를 보여줍니다.

| 기능 | Scala 2 | Scala 3 |
|------|---------|---------|
| 암시적 값 | `implicit val` | `given` |
| 암시적 매개변수 | `(implicit x: T)` | `(using x: T)` |
| 암시적 조회 | `implicitly[T]` | `summon[T]` |
| 확장 메서드 | `implicit class` | `extension` |
| 암시적 변환 | `implicit def` | `given Conversion` |

**메타프로그래밍**

Scala 3의 매크로 시스템은 완전히 재설계되었습니다. `inline` 키워드가 컴파일 타임 인라이닝을 보장하고, quotes API로 타입 안전한 매크로를 작성합니다.

| 기능 | Scala 2 | Scala 3 |
|------|---------|---------|
| 인라이닝 | `@inline` (힌트) | `inline` (보장) |
| 매크로 API | scala.reflect | scala.quoted |
| 컴파일 타임 연산 | 제한적 | compiletime 패키지 |

{{< callout type="info" title="핵심 포인트" >}}
- **문법**: 들여쓰기 기반 옵션, `if cond then`, `for x <- list do`
- **enum**: `sealed trait` + `case object` 조합을 간결하게 대체
- **타입**: Union(`|`), Intersection(`&`), Opaque Types 추가
- **암시적**: `implicit` → `given`/`using`으로 의도 명확화
{{< /callout >}}

#### 변경된 기능

기존 기능 중 문법이나 동작이 변경된 것들입니다.

**트레이트 매개변수**

Scala 3에서는 트레이트도 매개변수를 가질 수 있습니다. 이전에는 abstract class가 필요했던 패턴을 트레이트로 구현할 수 있습니다.

```scala
// Scala 3에서 트레이트도 매개변수 가능
trait Greeting(val name: String):
  def greet(): String = s"Hello, $name!"

class Person extends Greeting("World")

// Scala 2에서는 불가 - abstract class 필요
```

**진입점**

`@main` 어노테이션으로 진입점을 간단히 정의할 수 있습니다. 명령줄 인자도 타입 안전하게 파싱됩니다.

```scala
// Scala 3
@main def hello(): Unit = println("Hello!")

@main def greet(name: String): Unit = println(s"Hello, $name!")

// Scala 2
object Hello {
  def main(args: Array[String]): Unit = println("Hello!")
}
```

**Creator Applications**

Scala 3에서는 `new` 키워드 없이 클래스 인스턴스를 생성할 수 있습니다. case class가 아니어도 apply 메서드 없이 직접 호출이 가능합니다.

```scala
// Scala 3: new 없이 클래스 인스턴스 생성
class Person(name: String)
val p = Person("Alice")  // new 없이!

// Scala 2: new 필요 (case class가 아니면)
val p = new Person("Alice")
```

{{< callout type="info" title="핵심 포인트" >}}
- **트레이트 매개변수**: Scala 3에서 트레이트도 생성자 매개변수 가능
- **@main**: 진입점 정의가 간단해지고 인자 파싱 자동화
- **Creator Applications**: 일반 클래스도 `new` 없이 인스턴스 생성
{{< /callout >}}

#### 제거된 기능

다음 기능은 Scala 3에서 제거되었습니다. 대부분 더 나은 대안이 있으며, 마이그레이션 시 해당 대안을 사용해야 합니다.

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

{{< callout type="info" title="핵심 포인트" >}}
- **절차적 문법** (`def f() { }`): 명시적으로 `: Unit =` 사용
- **XML 리터럴**: 별도 라이브러리로 대체
- **기호 리터럴** (`'symbol`): 문자열 사용
{{< /callout >}}

#### 호환성

Scala 3는 Scala 2.13과의 바이너리 호환성을 제공합니다. 기존 라이브러리를 그대로 사용할 수 있어 마이그레이션 부담이 줄어듭니다.

**Scala 2 라이브러리 사용**

Scala 3 프로젝트에서 Scala 2.13용 라이브러리를 직접 의존성으로 추가할 수 있습니다.

Scala 3 프로젝트에서 Scala 2.13 라이브러리 사용 가능:

```scala
libraryDependencies += "org.typelevel" % "cats-core_2.13" % "2.10.0"
```

**Cross-building**

라이브러리 개발 시 여러 Scala 버전을 동시에 지원할 수 있습니다.

```scala
// build.sbt
scalaVersion := "3.3.1"
crossScalaVersions := Seq("2.13.12", "3.3.1")
```

**마이그레이션 모드**

`-source:3.0-migration` 옵션을 사용하면 Scala 2 코드를 점진적으로 마이그레이션할 수 있습니다. `-rewrite` 옵션과 함께 사용하면 일부 변환이 자동으로 적용됩니다.

```scala
// build.sbt
scalacOptions ++= Seq(
  "-source:3.0-migration",
  "-rewrite"
)
```

{{< callout type="info" title="핵심 포인트" >}}
- **바이너리 호환**: Scala 3에서 Scala 2.13 라이브러리 직접 사용 가능
- **Cross-building**: `crossScalaVersions`로 여러 버전 동시 지원
- **마이그레이션 모드**: `-source:3.0-migration -rewrite`로 자동 변환
{{< /callout >}}

#### 권장 사항

버전 선택 시 참고할 지침입니다.

**새 프로젝트**

- **Scala 3 권장**: 새로운 기능, 더 나은 에러 메시지, 개선된 타입 추론

**기존 프로젝트**

기존 Scala 2 프로젝트의 마이그레이션 전략입니다.

- **점진적 마이그레이션**: `-source:3.0-migration` 옵션 사용
- **의존성 확인**: 주요 라이브러리의 Scala 3 지원 여부 확인
- **테스트 유지**: 마이그레이션 전후 테스트 통과 확인

**Spark 사용**

Apache Spark를 사용하는 프로젝트는 Scala 2를 유지해야 합니다.

- **Scala 2.12/2.13 유지**: Spark는 아직 Scala 3 미지원 (2024년 기준)

{{< callout type="info" title="핵심 포인트" >}}
- **새 프로젝트**: Scala 3 선택 (더 나은 문법, 에러 메시지, 타입 추론)
- **기존 프로젝트**: 점진적 마이그레이션, 의존성 Scala 3 지원 확인
- **Spark**: Scala 2 유지 필수, Scala 3 지원 대기 중
{{< /callout >}}

#### 참고 자료

공식 마이그레이션 가이드와 참고 문서입니다.

- [Scala 3 Migration Guide](https://docs.scala-lang.org/scala3/guides/migration/compatibility-intro.html)
- [Scala 3 Reference](https://docs.scala-lang.org/scala3/reference/)
- [Changed Features](https://docs.scala-lang.org/scala3/reference/changed-features.html)
- [Dropped Features](https://docs.scala-lang.org/scala3/reference/dropped-features.html)
