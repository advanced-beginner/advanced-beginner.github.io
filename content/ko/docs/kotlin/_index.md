---
title: Kotlin
bookCollapseSection: true
description: 간결하고 안전한 JVM 언어 Kotlin 가이드 - 기본 문법부터 Null Safety, 코루틴, Spring Boot 연동, Multiplatform까지
weight: 6
lastmod: "2026-05-13"
---

#### Kotlin이란?

Kotlin은 JetBrains가 개발한 <strong>다중 패러다임 정적 타입 언어</strong>입니다. JVM, Android, JavaScript, Native 등 다양한 플랫폼을 타깃으로 하며, "실용적이고 안전한 프로그래밍 언어"를 목표로 설계되었습니다. 2017년 Android 공식 언어로 채택된 이후 백엔드(Spring Boot), 데이터 엔지니어링, 멀티플랫폼 개발까지 사용 영역이 빠르게 확장되었습니다.

**Kotlin의 핵심 가치**

Kotlin은 다음 네 가지 가치를 중심으로 설계되었습니다.

| 가치 | 의미 |
|------|------|
| 안전성 (Safety) | Null 참조 오류를 타입 시스템으로 차단 |
| 간결성 (Conciseness) | 보일러플레이트 제거, 의도 중심 코드 |
| 상호운용성 (Interoperability) | 기존 JVM 라이브러리와 100% 호환 |
| 도구 친화성 (Tooling) | IntelliJ 기반의 강력한 IDE 지원 |

**언제 Kotlin을 써야 할까?**

프로젝트 특성에 따라 Kotlin 도입 여부를 결정하세요.

**적합한 경우:**
- Android 애플리케이션 개발 (공식 권장)
- Spring Boot 기반 백엔드 (Kotlin DSL, 코루틴 친화)
- Kafka·Reactive 시스템에서 비동기 처리
- 한 코드베이스로 iOS·Android·Web을 노리는 멀티플랫폼 프로젝트
- Null 안정성과 타입 안전성이 중요한 시스템

**과할 수 있는 경우:**
- 단순 스크립트 또는 일회성 도구
- 팀이 JVM 경험이 전혀 없고 Python·Go가 더 적합한 경우

#### 이 가이드에서 다루는 것

이 가이드는 Kotlin 언어의 기초부터 코루틴, Spring Boot/Kafka 연동, Kotlin Multiplatform까지 단계별로 학습할 수 있도록 구성되어 있습니다.

**[Quick Start](quick-start/)**
5분 만에 Kotlin을 설치하고 첫 번째 프로그램을 실행합니다.

**[개념 이해](concepts/)**

Kotlin의 핵심 개념을 기초, 중급, 고급으로 분류하여 단계별로 학습합니다.

**기초:**

| 주제 | 배우는 것 |
|------|----------|
| [기본 문법](concepts/basics/) | 변수, 상수, 표현식, 패키지 |
| [변수와 타입](concepts/variables-types/) | val/var, 기본 타입, 타입 추론 |
| [함수](concepts/functions/) | fun, 기본값, 명명 인자, 람다 |
| [Null Safety](concepts/null-safety/) | `?`, `!!`, `?.`, `?:` 연산자 |
| [클래스와 객체](concepts/classes-objects/) | class, object, companion |
| [Data/Sealed Class](concepts/data-sealed-classes/) | 불변 데이터, 닫힌 계층 |
| [컬렉션](concepts/collections/) | List, Map, Set, Sequence |

**중급:**

| 주제 | 배우는 것 |
|------|----------|
| [확장 함수](concepts/extension-functions/) | 기존 타입에 메서드 추가 |
| [스코프 함수](concepts/scope-functions/) | let, run, with, apply, also |
| [제네릭과 변성](concepts/generics-variance/) | `<T>`, in/out, 타입 경계 |
| [위임](concepts/delegation/) | by, lazy, observable |
| [인라인/Reified](concepts/inline-reified/) | 인라인 함수, 실체화된 타입 매개변수 |

**고급:**

| 주제 | 배우는 것 |
|------|----------|
| [코루틴 기초](concepts/coroutines-basics/) | suspend, launch, async, await |
| [Flow와 비동기 스트림](concepts/flow-async-streams/) | Flow, StateFlow, SharedFlow |
| [코루틴 고급](concepts/coroutines-advanced/) | Context, Scope, Channel, 예외 |
| [DSL 빌더](concepts/dsl-builders/) | 타입 세이프 빌더 패턴 |
| [Multiplatform 개요](concepts/multiplatform-overview/) | KMP 구조, expect/actual |

**[실습 예제](examples/)**

Gradle Kotlin DSL 기반의 실행 가능한 예제 프로젝트입니다. Kotlin 기본 사용부터 Spring Boot·Kafka 연동, 코루틴 실무 적용, Kotlin Multiplatform 미니 프로젝트까지 다룹니다.

- [환경 설정](examples/setup/) - JDK, Gradle Kotlin DSL, IDE
- [기본 예제](examples/basic/) - Hello Kotlin, 기본 개념 활용
- [Spring Boot 연동](examples/spring-boot-integration/) - Kotlin + Spring Boot 시작
- [Kafka 연동](examples/kafka-integration/) - Producer/Consumer를 Kotlin으로
- [코루틴 실무 사용](examples/coroutines-practical/) - 실제 비동기 시나리오
- [Multiplatform 시작](examples/multiplatform-intro/) - KMP 미니 프로젝트

**[How-To Guide](howto/)**

특정 문제를 해결하기 위한 단계별 가이드입니다.

- [코루틴 디버깅](howto/coroutine-debugging/) - 비동기 코드 추적
- [Null Safety 마이그레이션](howto/null-safety-migration/) - Java 코드를 Kotlin으로 안전하게 이관
- [Gradle Kotlin DSL 팁](howto/gradle-kotlin-dsl-tips/) - 빌드 스크립트 실전 노하우
- [Kotest vs JUnit](howto/kotest-vs-junit/) - 테스트 프레임워크 선택
- [성능 프로파일링](howto/performance-profiling/) - JVM 성능 튜닝

**[부록](appendix/)**

- [용어 사전](appendix/glossary/) - Kotlin 핵심 용어
- [버전 비교](appendix/version-comparison/) - Kotlin 1.x → 2.x 변경점
- [FAQ](appendix/faq/) - 자주 묻는 질문
- [참고 자료](appendix/references/) - 공식 문서 및 추가 학습 자료

#### 바로 시작하기

> 🚀 **설치 없이 체험:** [Kotlin Playground](https://play.kotlinlang.org/)에서 브라우저로 Kotlin을 바로 실행할 수 있습니다.

```kotlin
// Kotlin Playground에서 이 코드를 실행해보세요
fun main() {
    val name = "Kotlin"
    println("Hello, $name!")
}
```

#### 선수 지식

- **필수**: 프로그래밍 기초 (변수, 함수, 조건문, 반복문)
- **도움됨**: JVM 언어 경험(Java 등), 객체지향 개념, Gradle 또는 Maven 기본 사용

#### 학습 경로 제안

목표에 따라 권장하는 학습 경로입니다.

```text
처음이라면:        Quick Start → 기본 문법 → 변수와 타입 → 함수 → Null Safety
백엔드 지향:       클래스 → Data Class → 확장 함수 → Spring Boot 연동 → Kafka 연동
비동기/스트림:     코루틴 기초 → Flow → 코루틴 고급 → 코루틴 실무 사용
멀티플랫폼:        Multiplatform 개요 → Multiplatform 시작
```

> 💡 **함께 읽기**: 백엔드 지향 학습 경로를 마쳤다면 [Kafka 가이드]({{< relref "/docs/kafka" >}})와 [DDD 가이드]({{< relref "/docs/ddd" >}})로 확장해 메시징 기반 도메인 모델링을 익힐 수 있습니다.

#### 흔한 오해

Kotlin에 대한 몇 가지 흔한 오해를 바로잡습니다.

**"Kotlin은 Android 전용 언어다"** — Kotlin은 JVM 백엔드, JavaScript, Native까지 타깃합니다. Spring Boot 공식 지원, Ktor, Kotlin Multiplatform 등 영역이 매우 넓습니다.

**"Java만 알면 Kotlin은 그냥 쓸 수 있다"** — 문법은 친숙하지만 Null Safety, 확장 함수, 코루틴, 스코프 함수 같은 Kotlin 특유의 사고방식은 별도 학습이 필요합니다.

**"코루틴은 단순한 스레드 풀 추상화다"** — 코루틴은 구조화된 동시성(Structured Concurrency), 협력적 취소, 백프레셔를 갖춘 비동기 프로그래밍 모델입니다.
