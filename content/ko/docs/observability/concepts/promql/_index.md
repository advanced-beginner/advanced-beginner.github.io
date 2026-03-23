---
title: PromQL
description: Prometheus Query Language 기초부터 고급 활용까지
weight: 4
bookCollapseSection: true
author: "@advanced-beginner"
lastmod: "2026-01-15"
---

## 전체 비유: 의료 데이터 분석 언어

PromQL을 **병원 EMR(전자의무기록) 분석 언어**에 비유하면 이해하기 쉽습니다:

| EMR 분석 비유 | PromQL 개념 | 역할 |
|-------------|------------|------|
| 환자 기록 검색 | 기본 쿼리 | 원하는 데이터 조회 |
| 조건 필터 (연령, 진료과) | 레이블 매칭 | 특정 조건으로 필터링 |
| 통계 분석 (평균, 합계) | 집계 연산자 | 데이터 요약/분석 |
| 추세 분석 (일별 변화) | rate/increase | 시간에 따른 변화율 |
| 백분위 분석 (P95) | histogram_quantile | 분포 분석 |
| 사전 계산 보고서 | Recording Rules | 자주 쓰는 쿼리 저장 |
| 이상 징후 알림 | Alerting Rules | 조건 기반 알림 |

이처럼 의료진이 EMR 시스템에서 환자 데이터를 분석하듯, PromQL로 시계열 메트릭을 분석합니다.

---

PromQL(Prometheus Query Language)은 Prometheus에서 시계열 데이터를 조회하고 분석하는 쿼리 언어입니다.

## 왜 PromQL을 배워야 하는가?

| 활용 | 설명 |
|------|------|
| **대시보드** | Grafana 패널에서 데이터 시각화 |
| **알림** | 조건 기반 자동 알림 규칙 작성 |
| **분석** | Ad-hoc 쿼리로 문제 원인 분석 |
| **Recording Rules** | 복잡한 쿼리 사전 계산으로 성능 최적화 |

## 학습 순서

### 기초 (1시간)

1. [기본 문법]({{< relref "/docs/observability/concepts/promql/syntax-basics" >}}) - 셀렉터, 레이블 매칭, 시간 범위
2. [집계 연산자]({{< relref "/docs/observability/concepts/promql/aggregation-operators" >}}) - sum, avg, count, topk, by/without

### 실전 활용 (2시간)

3. [rate와 increase]({{< relref "/docs/observability/concepts/promql/rate-and-increase" >}}) - Counter 메트릭 처리의 핵심
4. [histogram_quantile]({{< relref "/docs/observability/concepts/promql/histogram-quantile" >}}) - P50/P95/P99 백분위 계산

### 고급 (1시간)

5. [Recording Rules]({{< relref "/docs/observability/concepts/promql/recording-rules" >}}) - 복잡한 쿼리 사전 계산
6. [Alerting Rules]({{< relref "/docs/observability/concepts/promql/alerting-rules" >}}) - 알림 규칙 작성법

## 빠른 참조

### 자주 쓰는 함수

| 함수 | 용도 | 예시 |
|------|------|------|
| `rate()` | Counter의 초당 증가율 | `rate(http_requests_total[5m])` |
| `increase()` | Counter의 총 증가량 | `increase(http_requests_total[1h])` |
| `sum()` | 합계 | `sum(rate(http_requests_total[5m]))` |
| `avg()` | 평균 | `avg(node_cpu_seconds_total)` |
| `histogram_quantile()` | 백분위 | `histogram_quantile(0.99, rate(...[5m]))` |

### 자주 쓰는 패턴

```promql
# 에러율
sum(rate(http_requests_total{status=~"5.."}[5m]))
/ sum(rate(http_requests_total[5m]))

# P99 응답시간
histogram_quantile(0.99,
  sum(rate(http_request_duration_seconds_bucket[5m])) by (le)
)

# CPU 사용률
100 - (avg(rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)
```

## 학습 경로

```mermaid
graph LR
    A["기본 문법"] --> B["집계 연산자"]
    B --> C["rate/increase"]
    C --> D["histogram_quantile"]
    D --> E["Recording Rules"]
    E --> F["Alerting Rules"]
```
