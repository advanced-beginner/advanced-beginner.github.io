---
lastmod: "2026-01-14"
title: Implicit/Given 디버깅
weight: 1
---

컴파일러가 암시적 값을 찾지 못할 때 원인을 진단하고 해결하는 방법을 안내합니다.

{{< callout type="tip" title="TL;DR" >}}
- **Scala 2**: `-Xlog-implicits` 플래그로 검색 과정 확인
- **Scala 3**: `import scala.util.boundary` 불필요, 컴파일러 메시지가 더 명확
- **공통**: 스코프에 암시적 값이 있는지, 타입이 정확히 일치하는지 확인
{{< /callout >}}

## 증상

다음과 같은 컴파일 에러가 발생합니다:

**Scala 2:**
```
could not find implicit value for parameter ord: Ordering[MyClass]
```

**Scala 3:**
```
No given instance of type Ordering[MyClass] was found
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

암시적 값은 다음 위치에서 검색됩니다:

| 우선순위 | 검색 위치 | 예시 |
|---------|----------|------|
| 1 | 현재 스코프의 명시적 정의 | `implicit val`, `given` |
| 2 | 명시적 import | `import MyImplicits._` |
| 3 | 컴패니언 객체 | `object Person { implicit val ord = ... }` |
| 4 | 타입의 슈퍼타입 컴패니언 | 상속 관계의 컴패니언 객체 |
| 5 | 패키지 객체 | `package object mypackage` |

---

## 2단계: 컴파일러 디버깅 플래그 사용

### 2.1 Scala 2: -Xlog-implicits

sbt에서 다음 옵션을 추가하세요:

```scala
// build.sbt
scalacOptions += "-Xlog-implicits"
```

**출력 예시:**
```
[info] /path/to/file.scala:10: Ordering.ordered is not a valid implicit value for Ordering[Person] because:
[info] hasMatchingSymbol reported error: type mismatch;
[info]  found   : Ordering[Person]
[info]  required: Ordering[Person]
```

### 2.2 Scala 3: 향상된 에러 메시지

Scala 3는 기본적으로 더 상세한 에러 메시지를 제공합니다:

```
-- Error: /path/to/file.scala:10:8 ------
10 |  people.sorted
   |         ^
   |No given instance of type Ordering[Person] was found for parameter ord of method sorted in trait SeqOps
   |
   |The following import might fix the problem:
   |
   |  import scala.math.Ordering.Implicits._
```

---

## 3단계: 해결 방법

### 3.1 암시적 값 직접 정의

**Scala 2:**
```scala
case class Person(name: String, age: Int)

object Person {
  implicit val ordering: Ordering[Person] = Ordering.by(_.age)
}

// 이제 정상 동작
val people = List(Person("Alice", 30), Person("Bob", 25))
people.sorted  // List(Person("Bob", 25), Person("Alice", 30))
```

**Scala 3:**
```scala
case class Person(name: String, age: Int)

object Person:
  given Ordering[Person] = Ordering.by(_.age)

// 이제 정상 동작
val people = List(Person("Alice", 30), Person("Bob", 25))
people.sorted
```

### 3.2 기존 암시적 값 활용

이미 존재하는 암시적 값을 조합하세요:

```scala
// Scala 2
case class Person(name: String, age: Int)

object Person {
  // String의 Ordering을 활용
  implicit val ordering: Ordering[Person] = Ordering.by(_.name)
}
```

```scala
// Scala 3
case class Person(name: String, age: Int)

object Person:
  // 기존 given 인스턴스 활용
  given Ordering[Person] = Ordering.by(_.name)
```

### 3.3 명시적으로 전달

암시적 검색이 복잡할 때는 명시적으로 전달하세요:

```scala
// Scala 2
people.sorted(Ordering.by[Person, Int](_.age))

// Scala 3
people.sorted(using Ordering.by[Person, Int](_.age))
```

---

## 4단계: 흔한 실수와 해결

### 4.1 타입 불일치

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

---

## 체크리스트

암시적 값을 찾지 못할 때 다음을 확인하세요:

- [ ] **타입이 정확히 일치하는가?** - 제네릭 타입 파라미터 포함
- [ ] **스코프에 있는가?** - import 또는 컴패니언 객체
- [ ] **컴파일 순서가 맞는가?** - 암시적 정의가 사용 전에 컴파일되어야 함
- [ ] **충돌하는 암시적 값이 없는가?** - 동일 타입의 여러 암시적 값
- [ ] **디버깅 플래그를 사용했는가?** - `-Xlog-implicits` (Scala 2)

---

## 관련 문서

- [Implicit/Given](../concepts/implicits/) - 암시적 변환의 원리
- [타입 클래스](../concepts/type-classes/) - 타입 클래스 패턴
- [Future 에러 처리](future-error-handling/) - 비동기 코드 디버깅
