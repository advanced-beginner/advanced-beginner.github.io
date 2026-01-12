---
bookCollapseSection: true
lastmod: "2026-01-09"
title: 개념 이해
weight: 2
---

Scala의 핵심 개념을 체계적으로 배웁니다. 기초부터 고급까지, 각 개념은 Scala 2와 Scala 3 문법을 함께 다룹니다. 학습자의 수준과 목표에 따라 적절한 순서로 진행하면 효과적입니다.

#### 기초 (Basics)

프로그래밍의 기본 구성 요소를 Scala로 배웁니다. 변수 선언부터 패턴 매칭까지, Scala 프로그래밍의 토대가 되는 개념들입니다.

| 주제 | 내용 | 핵심 키워드 |
|------|------|-------------|
| [기본 문법](basics/) | 변수, 상수, 기본 타입 | `val`, `var`, 타입 추론 |
| [제어 구조](control-structures/) | 조건문, 반복문 | `if`, `for`, `while`, `match` |
| [함수와 메서드](functions-methods/) | 함수 정의, 람다 | `def`, `=>`, 기본값 |
| [클래스와 객체](classes-objects/) | OOP 기초 | `class`, `object`, `trait` |
| [케이스 클래스](case-classes/) | 불변 데이터 모델 | `case class`, `copy` |
| [패턴 매칭](pattern-matching/) | 강력한 분기 처리 | `match`, `case`, 가드 |

기초 개념들은 순서대로 학습하는 것이 좋습니다. 특히 변수 선언과 타입 시스템을 먼저 이해하면 이후 개념들을 쉽게 배울 수 있습니다.

#### 중급 (Intermediate)

함수형 프로그래밍과 Scala의 특징적인 기능을 배웁니다. 컬렉션 API를 마스터하면 대부분의 데이터 처리 작업을 간결하게 표현할 수 있습니다.

| 주제 | 내용 | 핵심 키워드 |
|------|------|-------------|
| [컬렉션](collections/) | 데이터 구조 | `List`, `Map`, `Set`, `Seq` |
| [고차 함수](higher-order-functions/) | 함수형 프로그래밍 | `map`, `filter`, `fold` |
| [제네릭](generics/) | 타입 매개변수 | `[T]`, 타입 경계 |
| [For Comprehension](for-comprehensions/) | 모나딕 연산 | `for-yield`, `flatMap` |
| [Implicit/Given](implicits/) | 문맥적 추상화 | `implicit`, `given`, `using` |

중급 주제들은 Scala의 함수형 프로그래밍 능력을 키우는 데 핵심적입니다. 컬렉션과 고차 함수를 먼저 익히고, 그 위에 For Comprehension과 Implicit을 쌓아가세요.

#### 고급 (Advanced)

전문적인 Scala 개발을 위한 고급 주제입니다. 타입 시스템의 고급 기능, 동시성 프로그래밍, 함수형 디자인 패턴을 다룹니다.

| 주제 | 내용 | 핵심 키워드 |
|------|------|-------------|
| [타입 클래스](type-classes/) | Ad-hoc 다형성 | 타입 클래스 패턴 |
| [공변성/반공변성](variance/) | 제네릭 타입 변성 | `+T`, `-T`, 무공변 |
| [고급 타입](type-system-advanced/) | Scala 3 타입 기능 | Union, Intersection, Match Types |
| [매크로](macros-metaprogramming/) | 컴파일 타임 코드 생성 | `inline`, 매크로 |
| [동시성](concurrency/) | 비동기 프로그래밍 | `Future`, `Promise` |
| [함수형 패턴](functional-patterns/) | FP 디자인 패턴 | Functor, Monad |

고급 주제들은 중급까지의 내용을 충분히 이해한 후에 학습하는 것이 좋습니다. 특히 타입 클래스는 라이브러리를 이해하고 확장하는 데 필수적입니다.

#### 학습 가이드

학습 목표에 따라 다른 경로를 추천합니다.

**처음 시작한다면**

기본 문법에서 시작하여 제어 구조, 함수와 메서드, 클래스와 객체 순서로 진행합니다. 기초를 탄탄히 다지는 것이 중요합니다. 특히 `val`과 `var`의 차이, 표현식 기반 문법을 이해하세요.

```
기본 문법 → 제어 구조 → 함수와 메서드 → 클래스와 객체
```

**함수형 프로그래밍을 배우고 싶다면**

컬렉션에서 시작하여 고차 함수, For Comprehension, 함수형 패턴 순서로 진행합니다. Scala의 컬렉션 API를 마스터하면 함수형 사고방식이 자연스럽게 익혀집니다.

```
컬렉션 → 고차 함수 → For Comprehension → 함수형 패턴
```

**Scala 3로 전환한다면**

Implicit/Given에서 시작하여 고급 타입, 매크로 순서로 진행합니다. Scala 3의 가장 큰 변화는 implicit 시스템입니다. given/using 문법을 먼저 익히세요.

```
Implicit/Given → 고급 타입 → 매크로
```

#### Scala 2 vs Scala 3 비교

이 가이드의 모든 문서는 두 버전을 함께 다룹니다. 아래 표는 주요 차이점을 정리한 것입니다.

| 기능 | Scala 2 | Scala 3 |
|------|---------|---------|
| 문법 스타일 | 중괄호 필수 | 들여쓰기 옵션 |
| 암시적 값 | `implicit val` | `given` |
| 암시적 매개변수 | `implicit` | `using` |
| 열거형 | `sealed trait` + `case object` | `enum` |
| 확장 메서드 | `implicit class` | `extension` |
| 타입 기능 | 제한적 | Union, Intersection, Match Types |

> 각 문서에서 {{< badge >}}Scala 2{{< /badge >}} {{< badge >}}Scala 3{{< /badge >}} 뱃지로 버전별 차이를 표시합니다.

