---
lastmod: "2026-01-10"
title: FAQ
description: "Scala 자주 묻는 질문과 답변입니다."
weight: 3
---

Scala에 대해 자주 묻는 질문과 답변입니다. 질문을 카테고리별로 분류하여 빠르게 원하는 정보를 찾을 수 있도록 했습니다.

{{< callout type="tip" title="TL;DR" >}}
- **버전 선택**: 새 프로젝트는 Scala 3, Spark 사용 시 Scala 2.12/2.13
- **val vs var**: 항상 `val`(불변) 우선 사용
- **null 대신 Option**: `Option`, `Some`, `None`으로 안전한 null 처리
- **IDE**: IntelliJ IDEA + Scala 플러그인 (가장 완성도 높음)
- **성능**: Java와 동등, 컬렉션 체이닝과 람다 사용 시 주의
{{< /callout >}}

#### 일반

Scala 학습과 관련된 일반적인 질문들입니다.

**Scala를 배워야 할까요?**

**그렇습니다.** Scala는 다음과 같은 경우에 특히 유용합니다:

- 대규모 데이터 처리 (Apache Spark)
- 분산 시스템 (Akka)
- 타입 안전성이 중요한 프로젝트
- 함수형 프로그래밍을 배우고 싶을 때

**Scala 2와 Scala 3 중 무엇을 배워야 하나요?**

상황에 따라 다릅니다.

- **새 프로젝트**: Scala 3 권장
- **Spark 사용**: Scala 2.12/2.13 (Spark가 아직 Scala 3 미지원)
- **기존 프로젝트 유지보수**: 프로젝트 버전에 맞춤

두 버전의 핵심 개념은 같으므로, 하나를 배우면 다른 것도 쉽게 적응할 수 있습니다.

**Scala는 어렵나요?**

기초는 어렵지 않습니다. Java 경험이 있다면 기본 문법은 금방 익힐 수 있습니다.

**어려운 부분:**
- 고급 타입 시스템 (공변성, 타입 클래스)
- 암시적 기능의 복잡한 사용
- 매크로/메타프로그래밍

점진적으로 학습하세요. 모든 고급 기능을 처음부터 알 필요는 없습니다.

{{< callout type="info" title="핵심 포인트" >}}
- Scala 3는 새 프로젝트에 권장, Spark 사용 시에는 Scala 2 유지
- 기초는 어렵지 않으나 고급 타입 시스템은 점진적으로 학습 필요
{{< /callout >}}

#### 문법

Scala 문법에 관한 질문들입니다.

**val과 var의 차이는?**

```scala
val x = 10  // 불변 (변경 불가)
// x = 20   // 컴파일 에러!

var y = 10  // 가변 (변경 가능)
y = 20      // OK
```

**권장:** 가능하면 항상 `val`을 사용하세요.

**def, val, lazy val의 차이는?**

```scala
// 매번 새로 계산
def computed = { println("computing"); 42 }
computed  // "computing" 출력
computed  // "computing" 출력

// 한 번만 계산 (즉시)
val eager = { println("eager"); 42 }  // "eager" 출력
eager     // 출력 없음
eager     // 출력 없음

// 한 번만 계산 (지연)
lazy val deferred = { println("lazy"); 42 }
// 아직 출력 없음
deferred  // "lazy" 출력
deferred  // 출력 없음
```

**세미콜론이 필요한가요?**

보통 필요 없습니다. Scala는 줄 끝에 자동으로 세미콜론을 추론합니다.

```scala
val x = 1
val y = 2

// 한 줄에 여러 문장은 세미콜론 필요
val a = 1; val b = 2
```

**Unit은 무엇인가요?**

`Unit`은 Java의 `void`와 유사합니다. 의미 있는 값을 반환하지 않음을 나타냅니다.

```scala
def printMessage(): Unit = println("Hello")

// Unit의 유일한 값은 ()
val unit: Unit = ()
```

{{< callout type="info" title="핵심 포인트" >}}
- `val`(불변)을 기본으로, `var`(가변)는 필요할 때만 사용
- `lazy val`은 첫 접근 시 초기화, `def`는 호출마다 재계산
- 세미콜론은 한 줄에 여러 문장 작성 시에만 필요
{{< /callout >}}

#### 함수형 프로그래밍

함수형 프로그래밍 패턴에 관한 질문들입니다.

**Option, Some, None은 무엇인가요?**

`Option`은 값이 있거나 없음을 안전하게 표현합니다:

```scala
def findUser(id: Int): Option[String] =
  if (id > 0) Some(s"User$id") else None

findUser(1)   // Some("User1")
findUser(-1)  // None

// 사용
findUser(1).getOrElse("Unknown")  // "User1"
findUser(-1).getOrElse("Unknown") // "Unknown"
```

**null 대신 Option을 사용하세요!**

**map과 flatMap의 차이는?**

```scala
val opt = Some(5)

// map: A => B
opt.map(_ * 2)  // Some(10)

// flatMap: A => Option[B]
opt.flatMap(x => if (x > 0) Some(x * 2) else None)  // Some(10)

// 차이
val nested = Some(Some(5))
nested.map(_.map(_ * 2))      // Some(Some(10))
nested.flatMap(_.map(_ * 2))  // Some(10)
```

**for comprehension은 어떻게 동작하나요?**

for comprehension은 `flatMap`, `map`, `withFilter`로 변환됩니다:

```scala
// for comprehension
for {
  x <- Some(1)
  y <- Some(2)
} yield x + y

// 변환됨
Some(1).flatMap(x =>
  Some(2).map(y =>
    x + y
  )
)
// 결과: Some(3)
```

{{< callout type="info" title="핵심 포인트" >}}
- `Option`으로 null을 안전하게 대체, `getOrElse`로 기본값 처리
- `map`은 값 변환, `flatMap`은 중첩 컨테이너 평탄화
- for comprehension은 `flatMap`/`map` 체인의 가독성 좋은 표현
{{< /callout >}}

#### 트러블슈팅

개발 중 자주 발생하는 문제와 해결책입니다.

**"implicit not found" 에러**

```
could not find implicit value for parameter ord: Ordering[MyClass]
```

**해결:** 필요한 암시적 인스턴스를 정의하거나 import하세요.

```scala
case class Person(name: String, age: Int)

// 인스턴스 정의
implicit val personOrdering: Ordering[Person] = Ordering.by(_.age)

// 또는 컴패니언 객체에 정의
object Person {
  implicit val ordering: Ordering[Person] = Ordering.by(_.age)
}
```

**"value xxx is not a member of yyy"**

확장 메서드가 import되지 않았을 수 있습니다:

```scala
import cats.syntax.all._  // Cats 확장 메서드
import zio.prelude._      // ZIO 확장 메서드
```

**컴파일이 너무 느려요**

- **증분 컴파일 사용**: `sbt ~compile`
- **병렬 컴파일**: `parallelExecution := true`
- **캐시 활용**: `.bsp` 디렉토리 유지

**IntelliJ에서 빨간 줄이 표시돼요**

1. **File** → **Invalidate Caches** → **Restart**
2. sbt 탭에서 **Reload**
3. **Build** → **Rebuild Project**

{{< callout type="info" title="핵심 포인트" >}}
- "implicit not found"는 필요한 타입 클래스 인스턴스 정의/import로 해결
- 확장 메서드 에러는 올바른 import 구문 확인
- 컴파일 속도 개선: 증분 컴파일(`sbt ~compile`) 활용
{{< /callout >}}

#### 성능

Scala 성능에 관한 질문들입니다.

**Scala는 Java보다 느린가요?**

대부분의 경우 비슷합니다. Scala는 JVM 바이트코드로 컴파일되므로 런타임 성능은 Java와 유사합니다.

**주의할 점:**
- 람다와 고차 함수는 객체 생성을 유발
- 컬렉션 체이닝은 중간 컬렉션 생성 가능
- `@tailrec`으로 꼬리 재귀 최적화 확인

**불변 컬렉션이 성능에 영향을 주나요?**

Scala의 불변 컬렉션은 <strong>구조적 공유</strong>를 사용하여 효율적입니다:

```scala
val list1 = List(1, 2, 3)
val list2 = 0 :: list1  // list1의 데이터 재사용
```

성능이 정말 중요하면 `Array`나 가변 컬렉션을 사용하세요.

{{< callout type="info" title="핵심 포인트" >}}
- Scala 런타임 성능은 Java와 동등 (JVM 바이트코드)
- 불변 컬렉션은 구조적 공유로 효율적, 극한 성능 필요 시 `Array` 사용
- `@tailrec`으로 꼬리 재귀 최적화 검증
{{< /callout >}}

#### 도구

개발 도구 선택에 관한 질문들입니다.

**IDE는 무엇을 사용해야 하나요?**

- **IntelliJ IDEA + Scala 플러그인**: 가장 완성도 높음
- **VS Code + Metals**: 가볍고 빠름

**빌드 도구는?**

- **sbt**: Scala 표준 빌드 도구
- **Mill**: 더 빠른 대안
- **Gradle**: Java 프로젝트와 혼합 시

**테스트 프레임워크는?**

- **ScalaTest**: 가장 널리 사용, 다양한 스타일
- **MUnit**: 간단하고 가벼움 (Scala 3 권장)
- **Specs2**: BDD 스타일

{{< callout type="info" title="핵심 포인트" >}}
- IDE: IntelliJ IDEA(완성도 높음) 또는 VS Code + Metals(가벼움)
- 빌드: sbt(표준), Mill(빠름), Gradle(Java 혼합 시)
- 테스트: ScalaTest(범용), MUnit(Scala 3 경량)
{{< /callout >}}

#### 추가 질문

더 궁금한 점이 있다면:

- [Scala 공식 문서](https://docs.scala-lang.org/)
- [Stack Overflow - Scala](https://stackoverflow.com/questions/tagged/scala)
- [Scala Users Forum](https://users.scala-lang.org/)
