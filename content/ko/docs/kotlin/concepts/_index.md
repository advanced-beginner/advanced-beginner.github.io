---
bookCollapseSection: true
lastmod: "2026-05-13"
title: 개념 이해
description: "Kotlin 핵심 개념의 학습 가이드와 문서 목록입니다."
weight: 2
---

Kotlin의 핵심 개념을 체계적으로 배웁니다. 기초부터 고급까지, 각 개념은 실무에서 바로 활용할 수 있는 예제와 함께 다룹니다. 학습자의 수준과 목표에 따라 적절한 순서로 진행하면 효과적입니다.

#### 기초 (Basics)

프로그래밍의 기본 구성 요소를 Kotlin으로 배웁니다. 변수 선언부터 데이터 모델링까지, Kotlin 프로그래밍의 토대가 되는 개념들입니다.

| 주제 | 내용 | 핵심 키워드 |
|------|------|-------------|
| [기본 문법](basics/) | 표현식, 패키지, 주석 | `package`, `import`, 표현식 |
| [변수와 타입](variables-types/) | 변수 선언, 기본 타입 | `val`, `var`, 타입 추론 |
| [함수](functions/) | 함수 정의, 람다 | `fun`, `=>`, 기본값, 명명 인자 |
| [Null Safety](null-safety/) | 안전한 null 처리 | `?`, `!!`, `?.`, `?:` |
| [클래스와 객체](classes-objects/) | OOP 기초 | `class`, `object`, `companion` |
| [Data/Sealed Class](data-sealed-classes/) | 데이터 모델링 | `data class`, `sealed class` |
| [컬렉션](collections/) | 데이터 구조 | `List`, `Map`, `Set`, `Sequence` |

기초 개념들은 순서대로 학습하는 것이 좋습니다. 특히 변수 선언과 Null Safety를 먼저 이해하면 이후 개념들을 쉽게 배울 수 있습니다.

#### 중급 (Intermediate)

Kotlin의 표현력을 결정짓는 특징적인 기능을 배웁니다. 확장 함수와 스코프 함수를 마스터하면 코드 가독성이 크게 향상됩니다.

| 주제 | 내용 | 핵심 키워드 |
|------|------|-------------|
| [확장 함수](extension-functions/) | 기존 타입에 메서드 추가 | `fun Type.method()` |
| [스코프 함수](scope-functions/) | 객체 컨텍스트 처리 | `let`, `run`, `with`, `apply`, `also` |
| [제네릭과 변성](generics-variance/) | 타입 매개변수 | `<T>`, `in`, `out` |
| [위임](delegation/) | 위임 패턴 | `by`, `lazy`, `Delegates.observable` |
| [인라인/Reified](inline-reified/) | 컴파일 타임 인라인 | `inline`, `reified` |

중급 주제들은 Kotlin 코드를 짧고 명확하게 만드는 핵심입니다. 확장 함수와 스코프 함수를 자유롭게 쓸 수 있어야 라이브러리 코드를 자연스럽게 읽고 작성할 수 있습니다.

#### 고급 (Advanced)

전문적인 Kotlin 개발을 위한 고급 주제입니다. 코루틴 기반 비동기 프로그래밍, DSL 빌더, Kotlin Multiplatform을 다룹니다.

| 주제 | 내용 | 핵심 키워드 |
|------|------|-------------|
| [코루틴 기초](coroutines-basics/) | 비동기 함수와 빌더 | `suspend`, `launch`, `async` |
| [Flow와 비동기 스트림](flow-async-streams/) | 반응형 스트림 | `Flow`, `StateFlow`, `SharedFlow` |
| [코루틴 고급](coroutines-advanced/) | 컨텍스트, 채널, 예외 | `CoroutineContext`, `Channel` |
| [DSL 빌더](dsl-builders/) | 타입 세이프 빌더 | `@DslMarker`, 람다 with receiver |
| [Multiplatform 개요](multiplatform-overview/) | KMP 구조 | `expect`, `actual`, common/jvm/native |

고급 주제들은 중급까지의 내용을 충분히 이해한 후에 학습하는 것이 좋습니다. 특히 코루틴은 백엔드·Android 양쪽에서 모두 핵심이므로 시간을 충분히 들여 익혀두면 좋습니다.

#### 학습 가이드

학습 목표에 따라 다른 경로를 추천합니다.

**처음 시작한다면**

기본 문법에서 시작하여 변수와 타입, 함수, Null Safety 순서로 진행합니다. Kotlin의 핵심 안전 장치인 Null Safety를 일찍 이해하는 것이 중요합니다.

```
기본 문법 → 변수와 타입 → 함수 → Null Safety → 클래스와 객체
```

**백엔드 개발을 한다면**

Data Class와 컬렉션, 확장 함수, 스코프 함수를 익힌 다음 코루틴으로 넘어갑니다. Spring Boot에서 자주 쓰는 패턴들이 이 흐름에 모두 들어 있습니다.

```
Data Class → 컬렉션 → 확장 함수 → 스코프 함수 → 코루틴 기초 → Flow
```

**비동기·반응형 시스템을 한다면**

코루틴 기초에서 시작하여 Flow, 코루틴 고급으로 진행합니다. Kafka 컨슈머, WebFlux 대체, Reactive 시스템에 활용할 수 있습니다.

```
코루틴 기초 → Flow → 코루틴 고급 → DSL 빌더
```

**라이브러리·DSL 작성을 한다면**

확장 함수와 스코프 함수를 익힌 다음 인라인/Reified, DSL 빌더로 진행합니다. 타입 안전한 빌더를 직접 설계할 수 있게 됩니다.

```
확장 함수 → 스코프 함수 → 인라인/Reified → DSL 빌더
```
