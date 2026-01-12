---
title: 실습 예제
description: 실행 가능한 코드로 Observability를 직접 경험합니다
weight: 3
bookCollapseSection: true
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

실행 가능한 코드와 설정으로 Observability를 직접 구현합니다.

## 학습 순서

1. [환경 구성](setup/) - Docker Compose로 Prometheus + Grafana + Loki + Tempo 구성
2. [Spring Boot 메트릭](spring-boot-metrics/) - Actuator + Micrometer 설정
3. [Kafka 모니터링](kafka-monitoring/) - Kafka 클러스터 관측성 구축
4. [풀스택 Observability](full-stack/) - 메트릭 + 로그 + 트레이스 통합

## 예제 프로젝트 구조

```
examples/
├── docker-compose.yml       # 전체 인프라
├── prometheus/
│   ├── prometheus.yml      # Prometheus 설정
│   └── rules/              # Recording/Alerting Rules
├── grafana/
│   ├── provisioning/       # 데이터소스, 대시보드
│   └── dashboards/         # JSON 대시보드
└── spring-app/
    └── application.yml     # Spring Boot 설정
```

## 기술 스택

| 구성요소 | 도구 | 역할 |
|---------|------|------|
| 메트릭 수집 | Prometheus | 시계열 메트릭 저장 |
| 메트릭 노출 | Micrometer | Spring Boot 메트릭 |
| 로그 수집 | Loki + Promtail | 로그 저장/조회 |
| 분산 추적 | Tempo | 트레이스 저장 |
| 시각화 | Grafana | 대시보드 |
| 알림 | Alertmanager | 알림 라우팅 |

## 사전 요구사항

- Docker 24.0+
- Docker Compose 2.20+
- 4GB+ 메모리
- (선택) Java 17+, Gradle 8+
