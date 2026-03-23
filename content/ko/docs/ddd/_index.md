---
title: Domain-Driven Design
bookCollapseSection: true
description: 복잡한 비즈니스 로직을 체계적으로 다루는 DDD 가이드 - 전략적/전술적 설계, Aggregate, CQRS, 이벤트 소싱
weight: 2
lastmod: 2026-01-09
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

<strong>Domain-Driven Design(DDD)</strong>은 복잡한 비즈니스 로직을 체계적으로 다루기 위한 설계 방법론입니다. Eric Evans가 2003년 동명의 책에서 제시한 이 접근법의 핵심 아이디어는 단순합니다. 코드가 비즈니스를 반영해야 한다는 것입니다. 데이터베이스 테이블이나 기술 프레임워크가 아닌, 비즈니스 도메인이 설계의 중심이 됩니다.

## 언제 DDD가 필요한가?

모든 프로젝트에 DDD가 필요한 것은 아닙니다. DDD 도입 여부를 결정할 때는 프로젝트의 특성을 신중하게 고려해야 합니다.

**DDD가 도움이 되는 상황**

DDD는 비즈니스 로직이 복잡할 때 진가를 발휘합니다. 단순한 CRUD를 넘어서 다양한 규칙, 조건, 계산이 존재하는 도메인에서 효과적입니다. 또한 도메인 전문가와의 협업이 필요한 경우, 즉 개발자만으로는 요구사항을 완전히 이해하기 어려운 상황에서 유비쿼터스 언어를 통해 커뮤니케이션 간극을 해소할 수 있습니다. 시스템이 오래 유지보수될 예정이거나 여러 팀이 하나의 시스템을 개발하여 경계와 책임 분리가 필요한 경우에도 DDD가 적합합니다.

**DDD가 과할 수 있는 상황**

반면에 단순 CRUD 애플리케이션이나 비즈니스 로직보다 기술적 복잡성이 주인 경우(예: 고성능 데이터 처리)에는 DDD가 오히려 불필요한 복잡성을 더할 수 있습니다. 프로토타입이나 단기 프로젝트, 팀 전체가 DDD를 이해하고 적용할 준비가 안 된 경우에도 마찬가지입니다.

> "DDD는 복잡성을 다루는 도구입니다. 복잡하지 않은 곳에 적용하면 오히려 복잡성을 만듭니다."

## 기존 방식과 무엇이 다른가?

DDD는 전통적인 데이터 중심 설계와 근본적으로 다른 접근법을 취합니다. 아래 표는 두 방식의 차이점을 보여줍니다. 기존 방식에서는 데이터베이스 스키마를 먼저 설계하고 개발자 용어로 코드를 작성했다면, DDD에서는 비즈니스 모델을 먼저 설계하고 비즈니스 용어(유비쿼터스 언어)로 코드를 작성합니다. 비즈니스 로직이 서비스 레이어에 분산되는 대신 도메인 객체에 응집되며, 전체 시스템이 하나의 모델을 사용하는 것이 아니라 Bounded Context로 모델을 분리합니다. 엔티티는 단순한 데이터 컨테이너가 아닌 비즈니스 행위의 주체로 역할합니다.

| 기존 방식 | DDD 방식 |
|----------|----------|
| 데이터베이스 스키마부터 설계 | 비즈니스 모델부터 설계 |
| 개발자 용어로 코드 작성 | 비즈니스 용어(유비쿼터스 언어)로 코드 작성 |
| 비즈니스 로직이 서비스 레이어에 분산 | 도메인 객체에 로직 응집 |
| 전체 시스템이 하나의 모델 | Bounded Context로 모델 분리 |
| 엔티티 = 데이터 컨테이너 | 엔티티 = 비즈니스 행위의 주체 |

## 이 가이드에서 다루는 것

**[Quick Start]({{< relref "/docs/ddd/quick-start" >}})**

DDD 핵심 개념을 빠르게 훑어봅니다. 전체 그림을 먼저 잡고 세부 내용으로 들어가세요.

**[개념 이해]({{< relref "/docs/ddd/concepts" >}})**

DDD는 크게 <strong>전략적 설계</strong>와 <strong>전술적 설계</strong>로 나뉩니다. 전략적 설계는 시스템의 큰 그림을 다루며 Bounded Context, Context Map, 유비쿼터스 언어 등의 개념을 포함합니다. 전술적 설계는 코드 레벨의 패턴을 다루며 Entity, Value Object, Repository, Aggregate, 도메인 이벤트 등의 구체적인 구현 패턴을 제공합니다. 심화 주제로는 CQRS를 통한 명령과 조회의 분리, 다양한 아키텍처 패턴(Layered, Hexagonal, Clean Architecture), 도메인 모델 테스트 전략, 그리고 흔한 실수를 피하는 안티패턴 가이드가 있습니다.

| 주제 | 배우는 것 |
|------|----------|
| [전략적 설계]({{< relref "/docs/ddd/concepts/strategic-design" >}}) | Bounded Context, Context Map, 유비쿼터스 언어 |
| [전술적 설계]({{< relref "/docs/ddd/concepts/tactical-design" >}}) | Entity, Value Object, Repository 패턴 |
| [Aggregate]({{< relref "/docs/ddd/concepts/aggregate" >}}) | 일관성 경계와 트랜잭션 범위 설계 |
| [도메인 이벤트]({{< relref "/docs/ddd/concepts/architecture/event-driven" >}}) | 느슨한 결합을 위한 이벤트 기반 통신 |
| [CQRS]({{< relref "/docs/ddd/concepts/architecture/cqrs" >}}) | 명령과 조회의 분리 |
| [아키텍처 패턴]({{< relref "/docs/ddd/concepts/architecture" >}}) | Layered, Hexagonal, Clean Architecture |
| [테스트 전략]({{< relref "/docs/ddd/concepts/testing" >}}) | 도메인 모델 테스트 방법 |
| [안티패턴]({{< relref "/docs/ddd/concepts/anti-patterns" >}}) | 흔한 실수와 피하는 방법 |

**[How-To Guide]({{< relref "/docs/ddd/howto" >}})**

DDD를 적용하면서 마주치는 구체적인 문제를 해결하는 방법을 단계별로 안내합니다.

- [Aggregate 경계 정하기]({{< relref "/docs/ddd/howto/aggregate-boundaries" >}}) - 불변식 기반으로 Aggregate 경계를 설계하는 방법

**[실습 예제]({{< relref "/docs/ddd/examples" >}})**

실제 주문 도메인을 DDD로 구현하는 예제를 통해 학습한 개념을 실전에 적용합니다. 환경 설정부터 시작하여 주문 도메인의 Entity, Value Object, Aggregate를 구현하고, 애플리케이션 레이어에서 Use Case와 도메인 서비스를 작성합니다.

- [환경 설정]({{< relref "/docs/ddd/examples/setup" >}}) - 프로젝트 구조와 의존성
- [주문 도메인]({{< relref "/docs/ddd/examples/order-domain" >}}) - Entity, Value Object, Aggregate 구현
- [애플리케이션 레이어]({{< relref "/docs/ddd/examples/application-layer" >}}) - Use Case와 도메인 서비스

**[부록]({{< relref "/docs/ddd/appendix" >}})**

학습 중 참고할 수 있는 보조 자료를 제공합니다. 용어 사전에서는 DDD 핵심 용어를 빠르게 확인할 수 있고, FAQ에서는 자주 묻는 질문에 대한 답변을, 참고 자료에서는 추가 학습을 위한 도서와 아티클을 찾을 수 있습니다.

- [용어 사전]({{< relref "/docs/ddd/appendix/glossary" >}}) - DDD 용어 빠른 참조
- [FAQ]({{< relref "/docs/ddd/appendix/faq" >}}) - 자주 묻는 질문
- [참고 자료]({{< relref "/docs/ddd/appendix/references" >}}) - 추가 학습 자료

## 선수 지식

이 가이드를 효과적으로 학습하려면 Java와 Spring Boot의 기본 지식, 그리고 객체지향 프로그래밍에 대한 이해가 필수입니다. 기본적인 데이터베이스 지식과 디자인 패턴에 대한 이해가 있으면 학습에 도움이 됩니다.

## 학습 경로 제안

학습 목표에 따라 다음과 같은 경로를 권장합니다. DDD를 처음 접한다면 Quick Start에서 전체 개념을 훑어본 후 전략적 설계와 전술적 설계 순서로 진행하세요. 모델링을 깊이 이해하고 싶다면 Aggregate, 도메인 이벤트, 주문 도메인 예제 순서로 학습하세요. 아키텍처에 관심이 있다면 CQRS, 아키텍처 패턴, 테스트 전략 순서로 학습하는 것이 효과적입니다.

## 흔한 오해

DDD에 대해 몇 가지 흔한 오해가 있습니다.

<strong>"DDD는 특정 아키텍처를 강제한다"</strong>는 사실이 아닙니다. DDD는 설계 원칙이지 구현 방식이 아닙니다. Hexagonal이든 Layered든 프로젝트에 맞는 아키텍처를 자유롭게 선택할 수 있습니다.

<strong>"DDD를 하려면 이벤트 소싱이 필수다"</strong>도 오해입니다. 이벤트 소싱은 선택 사항이며, 전통적인 상태 저장 방식으로도 충분히 DDD를 적용할 수 있습니다.

<strong>"DDD는 마이크로서비스와 같다"</strong>는 것도 다릅니다. DDD의 Bounded Context가 마이크로서비스 경계를 정하는 데 도움이 되지만, DDD는 모놀리식 아키텍처에서도 효과적으로 적용할 수 있습니다.
