---
lastmod: "2026-01-06"
title: Scala
weight: 3
---

## Scala란?

Scala는 **함수형 프로그래밍**과 **객체지향 프로그래밍**을 결합한 JVM 기반 언어입니다. "Scalable Language"의 약자로, 작은 스크립트부터 대규모 분산 시스템까지 확장 가능하게 설계되었습니다.

### 왜 Scala인가?

| Java의 한계 | Scala의 해결책 |
|-------------|---------------|
| 장황한 코드 | 간결하고 표현력 있는 문법 |
| 제한적인 타입 시스템 | 강력한 타입 추론과 고급 타입 기능 |
| 함수형 프로그래밍 지원 부족 | 일급 함수, 불변성, 패턴 매칭 |
| Null 참조 문제 | Option 타입으로 안전한 null 처리 |

Scala는 Java와 100% 호환되면서도 더 안전하고 표현력 있는 코드를 작성할 수 있게 해줍니다.

### 언제 Scala를 써야 할까?

**적합한 경우:**
- 대규모 데이터 처리 (Apache Spark)
- 분산 시스템 개발 (Akka, Akka HTTP)
- 함수형 프로그래밍을 적용하고 싶을 때
- 타입 안전성이 중요한 프로젝트
- 동시성/병렬 처리가 필요한 시스템

**과할 수 있는 경우:**
- 단순 CRUD 웹 애플리케이션
- 팀이 Scala 경험이 없고 일정이 촉박할 때
- Java 생태계만으로 충분한 경우

### Scala 2 vs Scala 3

이 가이드는 **Scala 2.13**과 **Scala 3** 모두를 다룹니다.

| 구분 | Scala 2.13 | Scala 3 |
|------|-----------|---------|
| 상태 | 현재 가장 널리 사용 | 최신 버전, 점차 확산 중 |
| 문법 | 중괄호 기반 | 들여쓰기 기반 옵션 |
| implicit | implicit 키워드 | given/using으로 개선 |
| 열거형 | sealed trait + case object | enum 키워드 |
| 타입 시스템 | 강력함 | Union, Intersection 등 추가 |

> **추천:** 새 프로젝트라면 Scala 3를, 기존 프로젝트나 Spark 사용 시 Scala 2.13을 선택하세요.

## 이 가이드에서 다루는 것

### [Quick Start](quick-start/)
5분 만에 Scala를 설치하고 첫 번째 프로그램을 실행합니다.

### [개념 이해](concepts/)

**기초:**

| 주제 | 배우는 것 |
|------|----------|
| [기본 문법](concepts/basics/) | 변수, 상수, 기본 타입, 타입 추론 |
| [제어 구조](concepts/control-structures/) | if, for, while, match 표현식 |
| [함수와 메서드](concepts/functions-methods/) | def, 람다, 기본값, 가변 인자 |
| [클래스와 객체](concepts/classes-objects/) | class, object, trait, enum |
| [케이스 클래스](concepts/case-classes/) | 불변 데이터 모델링 |
| [패턴 매칭](concepts/pattern-matching/) | match 표현식의 강력한 활용 |

**중급:**

| 주제 | 배우는 것 |
|------|----------|
| [컬렉션](concepts/collections/) | List, Set, Map, Seq, Vector |
| [고차 함수](concepts/higher-order-functions/) | map, filter, fold, 커링 |
| [제네릭](concepts/generics/) | 타입 매개변수, 타입 경계 |
| [For Comprehension](concepts/for-comprehensions/) | 모나딕 연산의 우아한 표현 |
| [Implicit/Given](concepts/implicits/) | 암시적 변환과 문맥적 추상화 |

**고급:**

| 주제 | 배우는 것 |
|------|----------|
| [타입 클래스](concepts/type-classes/) | Ad-hoc 다형성 패턴 |
| [공변성/반공변성](concepts/variance/) | 제네릭 타입의 변성 |
| [고급 타입](concepts/type-system-advanced/) | Union, Intersection, Match Types |
| [매크로](concepts/macros-metaprogramming/) | 컴파일 타임 메타프로그래밍 |
| [동시성](concepts/concurrency/) | Future, Promise, ExecutionContext |
| [함수형 패턴](concepts/functional-patterns/) | Functor, Monad, 참조 투명성 |

### [실습 예제](examples/)
sbt 기반의 실행 가능한 예제 프로젝트입니다.

- [환경 설정](examples/setup/) - sbt, IDE 설정
- [기본 예제](examples/basic/) - 핵심 개념 활용 예제
- [Scala 2 vs 3 비교](examples/scala2-vs-scala3/) - 버전별 코드 비교

### [부록](appendix/)
- [용어 사전](appendix/glossary/) - Scala 용어 빠른 참조
- [버전 비교](appendix/version-comparison/) - Scala 2 vs 3 차이점 요약
- [FAQ](appendix/faq/) - 자주 묻는 질문
- [참고 자료](appendix/references/) - 공식 문서 및 추가 학습 자료

## 바로 시작하기

> 🚀 **설치 없이 체험:** [Scastie](https://scastie.scala-lang.org/)에서 브라우저로 Scala를 바로 실행해볼 수 있습니다!

```scala
// Scastie에서 이 코드를 실행해보세요
@main def hello() =
  val name = "Scala"
  println(s"Hello, $name!")
```

## 선수 지식

- **필수**: 프로그래밍 기초 (변수, 함수, 조건문, 반복문)
- **도움됨**: Java 경험, 객체지향 개념, 함수형 프로그래밍 기초

## 학습 경로 제안

```
처음이라면:     Quick Start → 기본 문법 → 제어 구조 → 함수 → 클래스
데이터 처리:    컬렉션 → 고차 함수 → For Comprehension → 기본 예제
고급 활용:      타입 클래스 → 공변성 → 함수형 패턴 → 동시성
Scala 3 전환:   Implicit/Given → 버전 비교 → 고급 타입
```

## 흔한 오해

**"Scala는 너무 어렵다"** — 기초 문법은 Java보다 간결합니다. 고급 기능은 필요할 때 점진적으로 학습하면 됩니다.

**"Scala는 죽어가는 언어다"** — Apache Spark, Kafka, Akka 등 대규모 프로젝트에서 여전히 핵심적으로 사용됩니다. Scala 3는 활발히 발전 중입니다.

**"함수형만 써야 한다"** — Scala는 다중 패러다임 언어입니다. 객체지향과 함수형을 상황에 맞게 혼합하세요.
