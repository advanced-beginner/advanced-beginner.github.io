---
title: 참고 자료
description: 공식 문서, 도서, 추가 학습 자료
weight: 4
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

## 공식 문서

### Prometheus

| 자료 | 링크 | 설명 |
|------|------|------|
| Prometheus 공식 문서 | https://prometheus.io/docs/ | 설정, PromQL, 운영 가이드 |
| PromQL 참조 | https://prometheus.io/docs/prometheus/latest/querying/basics/ | 쿼리 언어 상세 |
| Alerting Rules | https://prometheus.io/docs/prometheus/latest/configuration/alerting_rules/ | 알림 규칙 작성 |

### Grafana

| 자료 | 링크 | 설명 |
|------|------|------|
| Grafana 공식 문서 | https://grafana.com/docs/grafana/latest/ | 대시보드, 패널 설정 |
| Loki 문서 | https://grafana.com/docs/loki/latest/ | 로그 수집/쿼리 |
| Tempo 문서 | https://grafana.com/docs/tempo/latest/ | 분산 추적 |

### OpenTelemetry

| 자료 | 링크 | 설명 |
|------|------|------|
| OpenTelemetry 공식 | https://opentelemetry.io/docs/ | 개념, SDK, Collector |
| Java 계측 | https://opentelemetry.io/docs/languages/java/ | Java 자동/수동 계측 |
| Semantic Conventions | https://opentelemetry.io/docs/concepts/semantic-conventions/ | 표준화된 속성 이름 |

---

## 도서

### 필수

| 도서 | 저자 | 내용 |
|------|------|------|
| **Site Reliability Engineering** | Google SRE Team | SRE 원칙, 황금 신호, SLO |
| **Observability Engineering** | Charity Majors, Liz Fong-Jones | 현대적 관측성 개념 |
| **The SRE Workbook** | Google SRE Team | SRE 실전 적용 |

### 권장

| 도서 | 저자 | 내용 |
|------|------|------|
| **Prometheus: Up & Running** | Brian Brazil | Prometheus 상세 가이드 |
| **Distributed Tracing in Practice** | Austin Parker 외 | 분산 추적 심화 |
| **Database Reliability Engineering** | Laine Campbell, Charity Majors | DB 관측성 |

---

## 블로그 & 아티클

### Prometheus/Grafana

- [Prometheus Best Practices](https://prometheus.io/docs/practices/) - 공식 권장사항
- [Grafana Blog](https://grafana.com/blog/) - 새 기능, 사례 연구
- [Robust Perception Blog](https://www.robustperception.io/blog/) - Brian Brazil의 Prometheus 팁

### SRE/Observability

- [Google SRE Books (무료)](https://sre.google/books/) - 전체 내용 무료 공개
- [Honeycomb Blog](https://www.honeycomb.io/blog/) - 관측성 심화
- [Charity Majors' Blog](https://charity.wtf/) - 관측성 리더의 인사이트

---

## 영상

### 컨퍼런스

| 영상 | 링크 | 내용 |
|------|------|------|
| PromCon | https://www.youtube.com/@PrometheusIo | Prometheus 컨퍼런스 |
| GrafanaCon | https://www.youtube.com/@Grafana | Grafana 컨퍼런스 |
| KubeCon | https://www.youtube.com/@caborgg | Kubernetes, 관측성 세션 |

### 튜토리얼

- [Prometheus Tutorial for Beginners](https://www.youtube.com/watch?v=h4Sl21AKiDg) - TechWorld with Nana
- [Grafana Tutorials](https://www.youtube.com/playlist?list=PLDGkOdUX1Ujrrse-cdj20RRah9hyHdxBu) - Grafana 공식

---

## 온라인 코스

| 코스 | 플랫폼 | 설명 |
|------|--------|------|
| Prometheus & Grafana | Udemy | 실습 중심 |
| Site Reliability Engineering | Coursera | Google의 SRE 코스 |
| Observability with OpenTelemetry | Linux Foundation | OTel 입문 |

---

## 커뮤니티

### Slack

- [Prometheus Users](https://prometheus-users.slack.com/)
- [Grafana Community](https://grafana.slack.com/)
- [CNCF Slack](https://slack.cncf.io/) - #prometheus, #opentelemetry

### GitHub

| 프로젝트 | 링크 |
|----------|------|
| Prometheus | https://github.com/prometheus/prometheus |
| Grafana | https://github.com/grafana/grafana |
| Loki | https://github.com/grafana/loki |
| Tempo | https://github.com/grafana/tempo |
| OpenTelemetry | https://github.com/open-telemetry |

---

## 대시보드 & 규칙

### Grafana 대시보드

| ID | 이름 | 용도 |
|----|------|------|
| 1860 | Node Exporter Full | 서버 모니터링 |
| 3662 | Prometheus Stats | Prometheus 자체 모니터링 |
| 4701 | JVM Micrometer | Spring Boot JVM |
| 7362 | MySQL Overview | MySQL 모니터링 |
| 7587 | PostgreSQL | PostgreSQL 모니터링 |
| 11074 | Kafka Exporter | Kafka 모니터링 |

https://grafana.com/grafana/dashboards/ 에서 검색 가능

### Alerting Rules

- [Awesome Prometheus Alerts](https://awesome-prometheus-alerts.grep.to/) - 상황별 알림 규칙 모음
- [Prometheus Operator Rules](https://github.com/prometheus-operator/kube-prometheus/tree/main/manifests) - Kubernetes 환경

---

## 도구

### 테스트 & 검증

| 도구 | 용도 |
|------|------|
| `promtool` | Prometheus 설정/규칙 검증 |
| `amtool` | Alertmanager 설정 검증 |
| `logcli` | Loki CLI 쿼리 도구 |

### 시뮬레이션

| 도구 | 용도 |
|------|------|
| `prometheus-fake-exporter` | 가짜 메트릭 생성 |
| `hey` | HTTP 부하 테스트 |
| `k6` | 부하 테스트 + 메트릭 |

---

## 인증

| 인증 | 주최 | 내용 |
|------|------|------|
| CKA/CKAD | CNCF | Kubernetes (Prometheus 연동) |
| Prometheus Certified Associate | CNCF | Prometheus 공식 인증 (2024~) |
| Grafana Associate | Grafana Labs | Grafana 기초 |
