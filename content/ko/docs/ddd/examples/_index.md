---
bookCollapseSection: true
title: 실습 예제
weight: 3
lastmod: "2026-01-13"
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

> **대상 독자**: DDD 개념을 이해하고 실제 코드로 구현해보고 싶은 개발자
> **선수 지식**: [Quick Start](../quick-start/)와 [개념 이해](../concepts/) 섹션의 전술적 설계
> **이 섹션의 목적**: 주문 도메인을 통해 DDD 패턴을 실제 Spring Boot 코드로 구현

{{< callout type="warning" title="실습 전 준비사항" >}}
- Java 17 이상
- Gradle 8.x
- IDE (IntelliJ IDEA 권장)
- Docker (Kafka 실행용, 선택사항)
{{< /callout >}}

이 섹션에서는 Spring Boot를 사용하여 DDD 패턴을 실제로 적용한 주문 도메인을 구현합니다. 이론적인 개념을 코드로 옮기는 과정에서 각 패턴이 어떻게 상호작용하는지 직접 경험할 수 있습니다.

## 학습 경로

학습은 프로젝트 설정에서 시작하여 도메인 모델 구현, 애플리케이션 계층 구현, 그리고 인프라 계층 구현 순서로 진행됩니다. 각 단계에서는 이전 단계에서 구축한 기반 위에 새로운 요소를 추가합니다.

| 단계 | 문서 | 배우는 것 | 소요 시간 |
|------|------|----------|----------|
| 1 | [프로젝트 설정](setup/) | 프로젝트 구조, 의존성 구성 | 약 15분 |
| 2 | [주문 도메인](order-domain/) | Aggregate, Entity, Value Object 구현 | 약 40분 |
| 3 | [애플리케이션 계층](application-layer/) | Use Case, 도메인 서비스 구현 | 약 30분 |
| 4 | [Event Sourcing 실습](event-sourcing/) | 이벤트 저장, 스냅샷, 시간 여행 | 약 45분 |

## 완성 후 결과물

모든 실습을 완료하면 다음을 구현하게 됩니다:
- Order Aggregate (Entity, Value Object 포함)
- OrderApplicationService (Use Case 조율)
- OrderRepository (영속화)
- 도메인 이벤트 발행 및 처리
