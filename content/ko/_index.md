---
title: Advanced Beginner
description: 기초를 넘어 실무로 - 핵심 원리부터 이해하는 기술 가이드
---

## 이 사이트는 무엇인가요?

튜토리얼을 따라 "Hello World"는 만들어봤지만, 실무에서 마주하는 복잡한 상황에서 **왜 이렇게 해야 하는지** 모르겠다면 — 이 가이드가 그 간극을 메워드립니다.

단순히 "이렇게 하세요"가 아닌, **왜 이런 설계가 필요한지** 원리부터 설명합니다. 원리를 이해하면 문서에 없는 상황에서도 올바른 판단을 내릴 수 있습니다.

## 누구를 위한 가이드인가요?

| 이런 분께 적합합니다 | 이런 분께는 맞지 않습니다 |
|---------------------|-------------------------|
| 기본 문법은 알지만 실무 적용이 막막한 분 | 완전 초보자 (기본 튜토리얼 먼저 권장) |
| "왜?"를 알고 싶은 분 | 복사-붙여넣기만 원하는 분 |
| Spring Boot 경험이 있는 Java/Scala 개발자 | - |
| 설계 원칙과 트레이드오프를 이해하고 싶은 분 | 빠른 레퍼런스만 필요한 분 |

## 제공하는 가이드

| 기술 | 문서 | How-to | 설명 |
|------|:----:|:------:|------|
| [Apache Kafka](docs/kafka/) | 22 | 4 | 분산 메시징 시스템 |
| [Domain-Driven Design](docs/ddd/) | 21 | 4 | 복잡한 비즈니스 로직 설계 |
| [Scala](docs/scala/) | 28 | 2 | 함수형 + 객체지향 JVM 언어 |
| [Apache Spark](docs/spark/) | 24 | 3 | 대규모 데이터 분산 처리 |
| [Kubernetes](docs/kubernetes/) | 18 | 4 | 컨테이너 오케스트레이션 |
| [Elasticsearch](docs/elasticsearch/) | 20 | 2 | 분산 검색 엔진 |
| [Observability](docs/observability/) | 28 | 3 | 시스템 관측성 (Metrics, Logs, Traces) |
| [Kotlin](docs/kotlin/) | 27 | 5 | 안전하고 간결한 JVM 언어 (코루틴/Spring Boot/Multiplatform) |

---

### [Apache Kafka](docs/kafka/)

분산 메시징 시스템의 실무 활용법. Producer/Consumer 기본부터 트랜잭션, 복제, 장애 처리, 성능 튜닝까지.

**배우는 것:**
- Kafka의 핵심 구성요소와 메시지 흐름
- Consumer Group과 Offset 관리 전략
- 트랜잭션으로 exactly-once 보장하기
- 실무 에러 처리 패턴과 모니터링

### [Domain-Driven Design](docs/ddd/)

복잡한 비즈니스 로직을 체계적으로 다루는 설계 방법론. 전략적 설계부터 전술적 패턴, CQRS와 이벤트 소싱까지.

**배우는 것:**
- Bounded Context로 시스템 경계 나누기
- Aggregate로 일관성 있는 도메인 모델 설계
- 도메인 이벤트로 느슨한 결합 구현
- 실제 주문 도메인 구현 예제

### [Scala](docs/scala/)

함수형과 객체지향을 결합한 JVM 언어. 기본 문법부터 고급 타입 시스템, 함수형 패턴까지. Scala 2.13과 Scala 3 문법을 함께 다룹니다.

**배우는 것:**
- Scala 기본 문법과 함수형 프로그래밍 기초
- Pattern Matching과 Case Classes 활용
- 타입 시스템: Generics, Variance, Type Classes
- Implicits/Given과 암시적 변환의 원리

### [Apache Spark](docs/spark/)

대규모 데이터 처리를 위한 분산 컴퓨팅 엔진. Java/Spring 개발자 관점에서 인메모리 처리, 지연 평가, DAG 최적화를 설명합니다.

**배우는 것:**
- Spark 아키텍처와 실행 모델 (Driver, Executor, Stage)
- RDD, DataFrame, Dataset의 차이와 활용
- Structured Streaming으로 실시간 데이터 처리
- 메모리 튜닝과 성능 최적화 전략

### [Kubernetes](docs/kubernetes/)

컨테이너 오케스트레이션 플랫폼 실무 가이드. Pod, Deployment, Service부터 스케일링, 네트워킹, 모니터링까지.

**배우는 것:**
- Kubernetes 아키텍처와 핵심 리소스 (Pod, Deployment, Service)
- ConfigMap/Secret으로 설정 관리
- HPA를 통한 자동 스케일링
- 헬스 체크와 무중단 배포 전략

### [Elasticsearch](docs/elasticsearch/)

대용량 검색과 실시간 분석을 위한 분산 검색 엔진. RDB의 한계를 넘는 전문 검색과 현실적인 운영 노하우를 다룹니다.

**배우는 것:**
- 역색인(Inverted Index)과 Lucene 내부 구조
- Query DSL과 집계(Aggregation) 분석
- 한글 검색 최적화와 자동완성 구현
- 클러스터 운영, 성능 튜닝, 장애 대응

### [Observability](docs/observability/)

시스템 관측성 실무 가이드. Metrics, Logs, Traces 3요소부터 Prometheus, Grafana, PromQL 심화까지.

**배우는 것:**
- 관측성 3요소(Metrics, Logs, Traces)의 역할과 상호 연결
- Prometheus 아키텍처와 PromQL 쿼리 언어
- SRE 황금 신호(Latency, Traffic, Errors, Saturation)
- 분산 추적과 OpenTelemetry 통합

### [Kotlin](docs/kotlin/)

JetBrains가 만든 다중 패러다임 JVM 언어. 기본 문법부터 Null Safety, 코루틴, Spring Boot/Kafka 연동, Kotlin Multiplatform까지.

**배우는 것:**
- Kotlin 기본 문법과 Null Safety로 안전한 코드 작성
- 확장 함수·스코프 함수로 표현력 있는 코드 만들기
- 코루틴과 Flow를 활용한 구조화된 비동기 프로그래밍
- Spring Boot/Kafka 환경에서의 Kotlin 활용 실무 패턴

## 이 가이드의 특징

**First Principles** — 표면적인 사용법이 아닌, 그 기술이 해결하려는 근본 문제부터 시작합니다. "어떻게"보다 "왜"를 먼저 이해하면 응용력이 생깁니다.

**실행 가능한 예제** — 모든 코드는 실제로 돌아갑니다. 개념 설명 후 바로 확인할 수 있는 예제를 제공합니다.

**실무 관점** — 이론적으로 완벽한 것과 실무에서 동작하는 것은 다릅니다. 트레이드오프와 현실적인 선택을 함께 다룹니다.

## 어디서 시작하면 좋을까요?

- **Kafka를 처음 접한다면** → [Kafka Quick Start](docs/kafka/quick-start/)
- **Kafka 기본은 아는데 깊이 이해하고 싶다면** → [Kafka 핵심 구성요소](docs/kafka/concepts/core-components/)
- **DDD가 뭔지 궁금하다면** → [DDD Quick Start](docs/ddd/quick-start/)
- **도메인 모델 설계를 배우고 싶다면** → [전술적 설계](docs/ddd/concepts/tactical-design/)
- **Scala를 처음 배운다면** → [Scala Quick Start](docs/scala/quick-start/)
- **Scala 타입 시스템을 깊이 이해하고 싶다면** → [Type Classes](docs/scala/concepts/type-classes/)
- **Spark로 대용량 데이터를 처리하고 싶다면** → [Spark Quick Start](docs/spark/quick-start/)
- **Spark 내부 구조를 이해하고 싶다면** → [Spark 아키텍처](docs/spark/concepts/architecture/)
- **Kubernetes를 처음 접한다면** → [Kubernetes Quick Start](docs/kubernetes/quick-start/)
- **Kubernetes 리소스 설정을 최적화하고 싶다면** → [리소스 관리](docs/kubernetes/concepts/resources/)
- **검색 엔진을 도입하고 싶다면** → [Elasticsearch Quick Start](docs/elasticsearch/quick-start/)
- **한글 검색을 최적화하고 싶다면** → [한글 검색 최적화](docs/elasticsearch/concepts/korean-search/)
- **시스템 모니터링을 시작하고 싶다면** → [Observability Quick Start](docs/observability/quick-start/)
- **PromQL을 깊이 배우고 싶다면** → [PromQL 심화](docs/observability/concepts/promql/)
- **Kotlin을 처음 배운다면** → [Kotlin Quick Start](docs/kotlin/quick-start/)
- **Kotlin 코루틴을 깊이 이해하고 싶다면** → [코루틴 기초](docs/kotlin/concepts/coroutines-basics/)
