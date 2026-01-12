---
title: 집계 연산자
description: sum, avg, count, topk - PromQL 집계 연산자로 데이터를 요약하고 분석합니다
weight: 2
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

> **대상 독자**: PromQL 기본 문법을 아는 개발자
> **선수 지식**: [기본 문법]({{< relref "/docs/observability/concepts/promql/syntax-basics" >}})
> **이 문서를 읽으면**: 여러 시계열을 집계하여 의미 있는 지표를 계산할 수 있습니다

## TL;DR

{{< callout type="info" >}}
**핵심 요약:**
- **sum**: 합계 - 전체 요청 수
- **avg**: 평균 - 평균 응답시간
- **count**: 개수 - 활성 인스턴스 수
- **topk/bottomk**: 상위/하위 N개
- **by/without**: 그룹화 기준 지정
{{< /callout >}}

## 집계 연산자 목록

| 연산자 | 설명 | 반환 |
|--------|------|------|
| `sum` | 합계 | 단일 값 또는 그룹별 값 |
| `avg` | 평균 | 단일 값 또는 그룹별 값 |
| `min` | 최솟값 | 단일 값 또는 그룹별 값 |
| `max` | 최댓값 | 단일 값 또는 그룹별 값 |
| `count` | 시계열 개수 | 단일 값 또는 그룹별 값 |
| `stddev` | 표준편차 | 단일 값 또는 그룹별 값 |
| `stdvar` | 분산 | 단일 값 또는 그룹별 값 |
| `topk` | 상위 K개 | K개 시계열 |
| `bottomk` | 하위 K개 | K개 시계열 |
| `count_values` | 값별 개수 | 값당 하나의 시계열 |
| `quantile` | 분위수 | 단일 값 또는 그룹별 값 |

---

## sum (합계)

모든 시계열 값의 합을 계산합니다.

### 기본 사용

```promql
# 전체 HTTP 요청 수 (모든 인스턴스, 모든 상태)
sum(http_requests_total)

# 초당 요청 수 합계
sum(rate(http_requests_total[5m]))
```

### 그룹별 집계

```promql
# 상태 코드별 요청 수
sum by (status) (http_requests_total)

# 결과:
# {status="200"} 15000
# {status="404"} 500
# {status="500"} 100

# 서비스별, 상태별 요청 수
sum by (service, status) (rate(http_requests_total[5m]))
```

### 실전 예제

```promql
# 전체 에러 수
sum(rate(http_requests_total{status=~"5.."}[5m]))

# 서비스별 에러율
sum by (service) (rate(http_requests_total{status=~"5.."}[5m]))
/ sum by (service) (rate(http_requests_total[5m]))
```

---

## avg (평균)

모든 시계열 값의 평균을 계산합니다.

### 기본 사용

```promql
# 전체 CPU 사용률 평균
avg(node_cpu_seconds_total{mode="idle"})

# 인스턴스별 평균 메모리 사용률
avg by (instance) (
  node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes
) / avg by (instance) (node_memory_MemTotal_bytes) * 100
```

### 실전 예제

```promql
# 서비스별 평균 응답시간
avg by (service) (
  rate(http_request_duration_seconds_sum[5m])
  / rate(http_request_duration_seconds_count[5m])
)

# 클러스터 평균 CPU 사용률
100 - avg(rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100
```

---

## count (개수)

시계열의 개수를 셉니다.

### 기본 사용

```promql
# 모니터링 중인 타겟 수
count(up)

# 활성 타겟 수 (up=1)
count(up == 1)

# 다운된 타겟 수
count(up == 0)
```

### 그룹별 집계

```promql
# 서비스별 인스턴스 수
count by (job) (up)

# 결과:
# {job="api-server"} 5
# {job="web-server"} 3
# {job="database"} 2
```

### 실전 예제

```promql
# 에러가 발생한 서비스 수
count(
  sum by (service) (rate(http_requests_total{status=~"5.."}[5m])) > 0
)

# 응답시간이 SLA를 초과한 엔드포인트 수
count(
  histogram_quantile(0.99, sum by (le, path) (rate(http_request_duration_seconds_bucket[5m])))
  > 0.5
)
```

---

## min / max (최솟값 / 최댓값)

### 기본 사용

```promql
# 가장 낮은 메모리 사용률
min(node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes * 100)

# 가장 높은 CPU 사용률
max(100 - rate(node_cpu_seconds_total{mode="idle"}[5m]) * 100)
```

### 그룹별 집계

```promql
# 서비스별 최대 응답시간
max by (service) (
  rate(http_request_duration_seconds_sum[5m])
  / rate(http_request_duration_seconds_count[5m])
)
```

---

## topk / bottomk (상위 / 하위 K개)

### 기본 사용

```promql
# 요청이 가장 많은 상위 5개 엔드포인트
topk(5, sum by (path) (rate(http_requests_total[5m])))

# 메모리 사용률이 가장 높은 상위 10개 인스턴스
topk(10, node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes)

# 가장 여유 있는 하위 3개 디스크
bottomk(3, node_filesystem_avail_bytes)
```

### 실전 예제

```promql
# 에러율이 가장 높은 상위 5개 서비스
topk(5,
  sum by (service) (rate(http_requests_total{status=~"5.."}[5m]))
  / sum by (service) (rate(http_requests_total[5m]))
)

# 지연시간이 가장 긴 상위 3개 엔드포인트
topk(3,
  histogram_quantile(0.99,
    sum by (le, path) (rate(http_request_duration_seconds_bucket[5m]))
  )
)
```

---

## by / without (그룹화)

### by: 지정한 라벨만 유지

```promql
# status 라벨만 유지하고 집계
sum by (status) (http_requests_total)

# 여러 라벨 유지
sum by (service, status) (http_requests_total)
```

### without: 지정한 라벨 제외하고 유지

```promql
# instance 라벨 제외하고 집계
sum without (instance) (http_requests_total)

# 여러 라벨 제외
sum without (instance, pod) (http_requests_total)
```

### by vs without

```promql
# 다음 두 쿼리는 동일한 결과
# 라벨: {service, instance, status}

# by 사용
sum by (service, status) (http_requests_total)

# without 사용
sum without (instance) (http_requests_total)
```

{{< callout type="info" >}}
**선택 기준:**
- 유지할 라벨이 적으면 → `by` 사용
- 제거할 라벨이 적으면 → `without` 사용
{{< /callout >}}

---

## count_values (값별 개수)

특정 값이 몇 번 나타나는지 셉니다.

```promql
# 버전별 인스턴스 수
count_values("version", app_version)

# 결과:
# {version="1.0.0"} 5
# {version="1.1.0"} 3
# {version="2.0.0"} 2
```

---

## quantile (분위수)

값들 중 특정 백분위를 계산합니다.

```promql
# 모든 인스턴스 중 P90 CPU 사용률
quantile(0.9, node_cpu_seconds_total)

# 서비스별 P50 응답시간
quantile by (service) (0.5,
  rate(http_request_duration_seconds_sum[5m])
  / rate(http_request_duration_seconds_count[5m])
)
```

{{< callout type="warning" >}}
`quantile()`은 Gauge나 계산된 값에 사용합니다. Histogram 버킷 데이터에서 백분위를 계산하려면 `histogram_quantile()`을 사용하세요.
{{< /callout >}}

---

## 실전 패턴

### 에러율 계산

```promql
# 전체 에러율
sum(rate(http_requests_total{status=~"5.."}[5m]))
/ sum(rate(http_requests_total[5m]))
* 100

# 서비스별 에러율
sum by (service) (rate(http_requests_total{status=~"5.."}[5m]))
/ sum by (service) (rate(http_requests_total[5m]))
* 100
```

### 가용성 계산

```promql
# 성공률 (가용성)
sum(rate(http_requests_total{status!~"5.."}[5m]))
/ sum(rate(http_requests_total[5m]))
* 100

# 서비스별 가용성
(1 - sum by (service) (rate(http_requests_total{status=~"5.."}[5m]))
     / sum by (service) (rate(http_requests_total[5m])))
* 100
```

### 리소스 사용률

```promql
# 전체 메모리 사용률
(sum(node_memory_MemTotal_bytes) - sum(node_memory_MemAvailable_bytes))
/ sum(node_memory_MemTotal_bytes) * 100

# 인스턴스별 디스크 사용률
(sum by (instance) (node_filesystem_size_bytes)
 - sum by (instance) (node_filesystem_avail_bytes))
/ sum by (instance) (node_filesystem_size_bytes) * 100
```

### 트래픽 분석

```promql
# 엔드포인트별 트래픽 비율
sum by (path) (rate(http_requests_total[5m]))
/ sum(rate(http_requests_total[5m]))
* 100

# 상위 10개 엔드포인트
topk(10, sum by (path) (rate(http_requests_total[5m])))
```

---

## 핵심 정리

| 연산자 | 용도 | 예시 |
|--------|------|------|
| `sum` | 합계 | 총 요청 수 |
| `avg` | 평균 | 평균 CPU 사용률 |
| `count` | 개수 | 인스턴스 수 |
| `min/max` | 극값 | 최고 메모리 |
| `topk` | 상위 N개 | 트래픽 상위 5개 |
| `by` | 그룹화 | 서비스별 집계 |
| `without` | 라벨 제외 | instance 제외 집계 |

---

## 다음 단계

| 추천 순서 | 문서 | 배우는 것 |
|----------|------|----------|
| 1 | [rate와 increase]({{< relref "/docs/observability/concepts/promql/rate-and-increase" >}}) | Counter 처리의 핵심 |
| 2 | [histogram_quantile]({{< relref "/docs/observability/concepts/promql/histogram-quantile" >}}) | P99 응답시간 계산 |
