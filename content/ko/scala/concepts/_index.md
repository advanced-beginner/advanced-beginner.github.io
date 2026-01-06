---
title: 개념 이해
weight: 2
---

Scala의 핵심 개념을 체계적으로 배웁니다. 기초부터 고급까지, 각 개념은 Scala 2와 Scala 3 문법을 함께 다룹니다.

## 기초 (Basics)

프로그래밍의 기본 구성 요소를 Scala로 배웁니다.

| 주제 | 내용 | 핵심 키워드 |
|------|------|-------------|
| [기본 문법](basics/) | 변수, 상수, 기본 타입 | `val`, `var`, 타입 추론 |
| [제어 구조](control-structures/) | 조건문, 반복문 | `if`, `for`, `while`, `match` |
| [함수와 메서드](functions-methods/) | 함수 정의, 람다 | `def`, `=>`, 기본값 |
| [클래스와 객체](classes-objects/) | OOP 기초 | `class`, `object`, `trait` |
| [케이스 클래스](case-classes/) | 불변 데이터 모델 | `case class`, `copy` |
| [패턴 매칭](pattern-matching/) | 강력한 분기 처리 | `match`, `case`, 가드 |

## 중급 (Intermediate)

함수형 프로그래밍과 Scala의 특징적인 기능을 배웁니다.

| 주제 | 내용 | 핵심 키워드 |
|------|------|-------------|
| [컬렉션](collections/) | 데이터 구조 | `List`, `Map`, `Set`, `Seq` |
| [고차 함수](higher-order-functions/) | 함수형 프로그래밍 | `map`, `filter`, `fold` |
| [제네릭](generics/) | 타입 매개변수 | `[T]`, 타입 경계 |
| [For Comprehension](for-comprehensions/) | 모나딕 연산 | `for-yield`, `flatMap` |
| [Implicit/Given](implicits/) | 문맥적 추상화 | `implicit`, `given`, `using` |

## 고급 (Advanced)

전문적인 Scala 개발을 위한 고급 주제입니다.

| 주제 | 내용 | 핵심 키워드 |
|------|------|-------------|
| [타입 클래스](type-classes/) | Ad-hoc 다형성 | 타입 클래스 패턴 |
| [공변성/반공변성](variance/) | 제네릭 타입 변성 | `+T`, `-T`, 무공변 |
| [고급 타입](type-system-advanced/) | Scala 3 타입 기능 | Union, Intersection, Match Types |
| [매크로](macros-metaprogramming/) | 컴파일 타임 코드 생성 | `inline`, 매크로 |
| [동시성](concurrency/) | 비동기 프로그래밍 | `Future`, `Promise` |
| [함수형 패턴](functional-patterns/) | FP 디자인 패턴 | Functor, Monad |

## 학습 가이드

### 처음 시작한다면

```
기본 문법 → 제어 구조 → 함수와 메서드 → 클래스와 객체
```

기초를 탄탄히 다지는 것이 중요합니다. 특히 `val`과 `var`의 차이, 표현식 기반 문법을 이해하세요.

### 함수형 프로그래밍을 배우고 싶다면

```
컬렉션 → 고차 함수 → For Comprehension → 함수형 패턴
```

Scala의 컬렉션 API를 마스터하면 함수형 사고방식이 자연스럽게 익혀집니다.

### Scala 3로 전환한다면

```
Implicit/Given → 고급 타입 → 매크로
```

Scala 3의 가장 큰 변화는 implicit 시스템입니다. given/using 문법을 먼저 익히세요.

## Scala 2 vs Scala 3 비교

이 가이드의 모든 문서는 두 버전을 함께 다룹니다. 주요 차이점:

| 기능 | Scala 2 | Scala 3 |
|------|---------|---------|
| 문법 스타일 | 중괄호 필수 | 들여쓰기 옵션 |
| 암시적 값 | `implicit val` | `given` |
| 암시적 매개변수 | `implicit` | `using` |
| 열거형 | `sealed trait` + `case object` | `enum` |
| 확장 메서드 | `implicit class` | `extension` |
| 타입 기능 | 제한적 | Union, Intersection, Match Types |

> 각 문서에서 {{< badge >}}Scala 2{{< /badge >}} {{< badge >}}Scala 3{{< /badge >}} 뱃지로 버전별 차이를 표시합니다.
