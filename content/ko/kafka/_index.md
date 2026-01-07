---
title: Apache Kafka
description: 분산 메시징 시스템 Kafka 실무 가이드 - Producer/Consumer, 트랜잭션, 복제, 장애 처리, 성능 튜닝까지
weight: 1
---

## Kafka란?

Apache Kafka는 **분산 이벤트 스트리밍 플랫폼**입니다. 단순히 말하면, 시스템 간에 메시지를 안전하고 빠르게 전달하는 중개자입니다.

### 왜 Kafka가 필요한가?

서비스 A가 서비스 B를 직접 호출하면 어떤 문제가 생길까요?

| 직접 호출의 문제 | Kafka 도입 후 |
|-----------------|---------------|
| B가 죽으면 A도 실패 | A는 메시지만 보내고 자기 일 계속함 |
| B가 느리면 A도 느려짐 | 비동기 처리로 A는 즉시 응답 |
| B를 C, D도 호출하면 부하 폭증 | Kafka가 버퍼 역할, 각자 속도로 소비 |
| A-B 강한 결합 | Producer/Consumer 분리로 느슨한 결합 |

Kafka는 이런 문제들을 해결하면서도 **메시지 유실 없이**, **순서를 보장**하며, **대용량 처리**가 가능합니다.

### 언제 Kafka를 써야 할까?

**적합한 경우:**
- 서비스 간 비동기 통신이 필요할 때
- 이벤트 기반 아키텍처를 구축할 때
- 대용량 로그/이벤트 수집이 필요할 때
- 실시간 데이터 파이프라인을 구축할 때

**과할 수 있는 경우:**
- 단순 요청-응답이면 충분할 때
- 메시지 양이 적고 복잡성이 낮을 때
- 운영 인프라를 갖출 여력이 없을 때

## 이 가이드에서 다루는 것

### [Quick Start](quick-start/)
5분 만에 Kafka 메시지를 보내고 받아봅니다. 개념보다 먼저 동작하는 코드를 확인하세요.

### [개념 이해](concepts/)
단순히 "이렇게 쓰세요"가 아닌, **왜 이렇게 동작하는지** 원리를 설명합니다.

| 주제 | 배우는 것 |
|------|----------|
| [핵심 구성요소](concepts/core-components/) | Broker, Topic, Partition, Producer, Consumer의 역할과 관계 |
| [메시지 흐름](concepts/message-flow/) | 메시지가 Producer에서 Consumer까지 도달하는 전체 과정 |
| [Consumer Group과 Offset](concepts/consumer-group-offset/) | 병렬 처리와 메시지 위치 관리의 핵심 |
| [복제와 장애 대응](concepts/replication/) | 데이터 유실 없이 장애를 견디는 방법 |
| [트랜잭션](concepts/transactions/) | exactly-once 처리를 보장하는 방법 |
| [에러 처리](concepts/error-handling/) | 실무에서 마주치는 오류 상황과 해결 패턴 |
| [성능 튜닝](concepts/producer-tuning/) | Producer/Consumer 최적화 전략 |
| [모니터링](concepts/monitoring/) | 운영 환경에서 Kafka 상태 파악하기 |

### [실습 예제](examples/)
Spring Boot 기반의 실행 가능한 예제 코드입니다.

- [환경 설정](examples/setup/) - Docker로 Kafka 클러스터 구성
- [기본 예제](examples/basic/) - Producer/Consumer 구현
- [주문 시스템](examples/order-system/) - 실제 비즈니스 시나리오 예제

### [부록](appendix/)
- [용어 사전](appendix/glossary/) - Kafka 용어 빠른 참조
- [참고 자료](appendix/references/) - 공식 문서 및 추가 학습 자료

## 선수 지식

- **필수**: Java 기본, Spring Boot 경험
- **도움됨**: REST API 개발 경험, 기본적인 분산 시스템 개념

## 학습 경로 제안

```
처음이라면:     Quick Start → 핵심 구성요소 → 메시지 흐름 → 기본 예제
실무 적용:      Consumer Group → 에러 처리 → 트랜잭션 → 주문 시스템
운영 준비:      복제와 장애 대응 → 성능 튜닝 → 모니터링
```

각 문서는 독립적으로 읽을 수 있지만, 처음이라면 위 순서를 추천합니다.
