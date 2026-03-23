---
lastmod: "2026-01-16"
title: Implicit/Given 디버깅
description: "Implicit/Given 관련 오류를 진단하고 해결하는 방법을 안내합니다."
weight: 1
---

컴파일러가 암시적 값을 찾지 못할 때 원인을 진단하고 해결하는 방법을 안내합니다.

**소요 시간**: 약 10-15분

{{< callout type="tip" title="TL;DR" >}}
- **Scala 2**: `-Xlog-implicits` 플래그로 검색 과정 확인
- **Scala 3**: 컴파일러 메시지가 더 명확하며, `import` 제안도 제공
- **공통**: 스코프에 암시적 값이 있는지, 타입이 정확히 일치하는지 확인
{{< /callout >}}

---

## 이 가이드가 해결하는 문제

다음과 같은 컴파일 에러가 발생할 때 이 가이드를 사용하세요:

{{< tabs "implicit-error-messages" >}}
{{< tab "Scala 2" >}}
```
could not find implicit value for parameter ord: Ordering[MyClass]
```
또는
```
ambiguous implicit values
```
{{< /tab >}}
{{< tab "Scala 3" >}}
```
No given instance of type Ordering[MyClass] was found
```
또는
```
Ambiguous given instances
```
{{< /tab >}}
{{< /tabs >}}

### 이 가이드가 다루지 않는 것

- **암시적 변환(implicit conversion)의 원리**: [Implicit/Given 개념 문서]({{< relref "/docs/scala/concepts/implicits" >}})를 참조하세요
- **타입 클래스 설계 방법**: [타입 클래스 개념 문서]({{< relref "/docs/scala/concepts/type-classes" >}})를 참조하세요
- **Cats/ZIO 등 라이브러리의 암시적 값**: 해당 라이브러리 문서를 참조하세요

---

## 시작하기 전에

다음 환경이 준비되어 있는지 확인하세요:

| 항목 | 요구 사항 | 확인 방법 |
|------|----------|----------|
| Scala 버전 | 2.13.x 또는 3.x | `scala -version` |
| 빌드 도구 | sbt 1.x 또는 Gradle 8.x | `sbt --version` |
| IDE (선택) | IntelliJ IDEA + Scala 플러그인 또는 VS Code + Metals | - |

```bash
# Scala 버전 확인
scala -version
# 출력 예: Scala code runner version 3.3.1

# sbt 버전 확인
sbt --version
# 출력 예: sbt version in this project: 1.9.7
```

---

## 1단계: 에러 메시지 분석

### 1.1 필요한 타입 확인

에러 메시지에서 필요한 암시적 타입을 정확히 파악하세요:

```scala
// 에러: could not find implicit value for parameter ord: Ordering[Person]
case class Person(name: String, age: Int)

val people = List(Person("Alice", 30), Person("Bob", 25))
people.sorted  // Ordering[Person]이 필요함
```

### 1.2 스코프 확인

암시적 값은 다음 우선순위로 검색됩니다:

| 우선순위 | 검색 위치 | 예시 |
|---------|----------|------|
| 1 | 현재 스코프의 명시적 정의 | `implicit val`, `given` |
| 2 | 명시적 import | `import MyImplicits._` |
| 3 | 컴패니언 객체 | `object Person { implicit val ord = ... }` |
| 4 | 타입의 슈퍼타입 컴패니언 | 상속 관계의 컴패니언 객체 |
| 5 | 패키지 객체 | `package object mypackage` |

---

## 2단계: 컴파일러 디버깅 플래그 사용

{{< tabs "compiler-debug-flags" >}}
{{< tab "Scala 2" >}}
### -Xlog-implicits 플래그

sbt에서 다음 옵션을 추가하세요:

```scala
// build.sbt
scalacOptions += "-Xlog-implicits"
```

컴파일 후 다음과 같은 출력을 확인하세요:

```
[info] /path/to/file.scala:10: Ordering.ordered is not a valid implicit value
[info] for Ordering[Person] because:
[info] hasMatchingSymbol reported error: type mismatch;
[info]  found   : Ordering[Comparable[Person]]
[info]  required: Ordering[Person]
```

**성공 확인**: 위와 같은 상세 로그가 출력되면 플래그가 정상 적용된 것입니다.
{{< /tab >}}
{{< tab "Scala 3" >}}
### 향상된 에러 메시지

Scala 3는 기본적으로 더 상세한 에러 메시지와 해결 제안을 제공합니다:

```
-- Error: /path/to/file.scala:10:8 ------
10 |  people.sorted
   |         ^
   |No given instance of type Ordering[Person] was found
   |for parameter ord of method sorted in trait SeqOps
   |
   |The following import might fix the problem:
   |
   |  import scala.math.Ordering.Implicits._
```

**성공 확인**: 컴파일러가 `import` 제안을 보여주면 Scala 3의 향상된 진단이 작동하는 것입니다.
{{< /tab >}}
{{< /tabs >}}

### IDE 설정

{{< tabs "ide-settings" >}}
{{< tab "IntelliJ IDEA" >}}
1. <strong>Preferences</strong> → <strong>Build, Execution, Deployment</strong> → <strong>Compiler</strong> → <strong>Scala Compiler</strong>로 이동하세요
2. <strong>Additional compiler options</strong>에 `-Xlog-implicits` 추가하세요 (Scala 2)
3. **Ctrl+Shift+P** (Windows/Linux) 또는 **Cmd+Shift+P** (macOS)로 암시적 힌트를 토글하세요

**성공 확인**: 코드에서 암시적 파라미터가 회색 텍스트로 표시됩니다.
{{< /tab >}}
{{< tab "VS Code + Metals" >}}
1. 명령 팔레트(**Ctrl+Shift+P**)를 열고 `Metals: Toggle inlay hints`를 실행하세요
2. 설정에서 `metals.inlayHints.implicitArguments.enable`을 `true`로 설정하세요

**성공 확인**: 코드에서 암시적 파라미터가 인라인 힌트로 표시됩니다.
{{< /tab >}}
{{< /tabs >}}

---

## 3단계: 해결 방법

### 3.1 암시적 값 직접 정의

{{< tabs "define-implicit" >}}
{{< tab "Scala 2" >}}
```scala
case class Person(name: String, age: Int)

object Person {
  implicit val ordering: Ordering[Person] = Ordering.by(_.age)
}

// 이제 정상 동작
val people = List(Person("Alice", 30), Person("Bob", 25))
people.sorted  // List(Person("Bob", 25), Person("Alice", 30))
```
{{< /tab >}}
{{< tab "Scala 3" >}}
```scala
case class Person(name: String, age: Int)

object Person:
  given Ordering[Person] = Ordering.by(_.age)

// 이제 정상 동작
val people = List(Person("Alice", 30), Person("Bob", 25))
people.sorted  // List(Person("Bob", 25), Person("Alice", 30))
```
{{< /tab >}}
{{< /tabs >}}

### 3.2 기존 암시적 값 활용

이미 존재하는 암시적 값을 조합하세요:

{{< tabs "combine-implicit" >}}
{{< tab "Scala 2" >}}
```scala
case class Person(name: String, age: Int)

object Person {
  // String의 Ordering을 활용
  implicit val ordering: Ordering[Person] = Ordering.by(_.name)
}
```
{{< /tab >}}
{{< tab "Scala 3" >}}
```scala
case class Person(name: String, age: Int)

object Person:
  // 기존 given 인스턴스 활용
  given Ordering[Person] = Ordering.by(_.name)
```
{{< /tab >}}
{{< /tabs >}}

### 3.3 명시적으로 전달

암시적 검색이 복잡할 때는 명시적으로 전달하세요:

{{< tabs "explicit-passing" >}}
{{< tab "Scala 2" >}}
```scala
// 명시적으로 Ordering 전달
people.sorted(Ordering.by[Person, Int](_.age))
```
{{< /tab >}}
{{< tab "Scala 3" >}}
```scala
// using 키워드로 명시적 전달
people.sorted(using Ordering.by[Person, Int](_.age))
```
{{< /tab >}}
{{< /tabs >}}

---

## 4단계: 흔한 실수와 해결

### 4.1 타입 불일치

{{< callout type="warning" title="주의" >}}
와일드카드 타입(`_`)은 암시적 값으로 사용할 수 없습니다.
{{< /callout >}}

```scala
// 잘못된 예: 타입 파라미터 누락
implicit val ord: Ordering[_] = Ordering.by(_.toString)  // 컴파일 안 됨

// 올바른 예: 정확한 타입 지정
implicit val ord: Ordering[Person] = Ordering.by(_.name)
```

### 4.2 import 누락

```scala
// JSON 라이브러리 사용 시 흔한 실수
import io.circe.generic.auto._  // 이 import가 필요함

case class User(name: String)
val json = User("Alice").asJson  // Encoder[User]가 필요
```

**해결**: 라이브러리 문서에서 필요한 import를 확인하세요.

### 4.3 스코프 문제

```scala
// 잘못된 예: 메서드 내부에 정의하면 외부에서 사용 불가
def process(): Unit = {
  implicit val ord: Ordering[Person] = Ordering.by(_.age)
}

// 올바른 예: 적절한 스코프에 정의
object Implicits {
  implicit val personOrdering: Ordering[Person] = Ordering.by(_.age)
}

import Implicits._
```

### 4.4 암시적 충돌

동일 타입의 암시적 값이 여러 개 있으면 충돌이 발생합니다:

```scala
// 에러: ambiguous implicit values
implicit val byAge: Ordering[Person] = Ordering.by(_.age)
implicit val byName: Ordering[Person] = Ordering.by(_.name)

people.sorted  // 어떤 것을 사용해야 할지 모름
```

**해결**: 하나만 import하거나 명시적으로 전달하세요.

---

## 체크리스트

암시적 값을 찾지 못할 때 다음을 확인하세요:

- [ ] **타입이 정확히 일치하는가?** - 제네릭 타입 파라미터 포함
- [ ] **스코프에 있는가?** - import 또는 컴패니언 객체 확인
- [ ] **컴파일 순서가 맞는가?** - 암시적 정의가 사용 전에 컴파일되어야 함
- [ ] **충돌하는 암시적 값이 없는가?** - 동일 타입의 여러 암시적 값 확인
- [ ] **디버깅 플래그를 사용했는가?** - `-Xlog-implicits` (Scala 2)

**모든 항목을 확인했는데도 문제가 해결되지 않으면**, 최소 재현 코드를 만들어 Scala Discord나 Stack Overflow에 질문하세요.

---

## 관련 문서

- [Implicit/Given 개념]({{< relref "/docs/scala/concepts/implicits" >}}) - 암시적 변환의 원리
- [타입 클래스]({{< relref "/docs/scala/concepts/type-classes" >}}) - 타입 클래스 패턴
- [Future 에러 처리]({{< relref "/docs/scala/howto/future-error-handling" >}}) - ExecutionContext 문제 해결
