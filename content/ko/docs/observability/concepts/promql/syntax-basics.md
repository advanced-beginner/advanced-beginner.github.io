---
title: 기본 문법
description: PromQL 셀렉터, 레이블 매칭, 시간 범위 선택 - 쿼리의 기초를 다집니다
weight: 1
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

> **대상 독자**: PromQL을 처음 배우는 개발자
> **선수 지식**: [메트릭 기초]({{< relref "/docs/observability/concepts/metrics-fundamentals" >}})
> **이 문서를 읽으면**: 기본적인 PromQL 쿼리를 작성하고 원하는 메트릭을 조회할 수 있습니다

## TL;DR

{{< callout type="info" >}}
**핵심 요약:**
- **Instant Vector**: 현재 시점의 값 `http_requests_total`
- **Range Vector**: 시간 범위의 값들 `http_requests_total[5m]`
- **레이블 매칭**: `=`, `!=`, `=~`, `!~`로 필터링
- **연산**: 산술(`+`, `-`), 비교(`>`, `<`), 논리(`and`, `or`)
{{< /callout >}}

## 데이터 타입

### Instant Vector (순간 벡터)

특정 시점의 시계열 집합입니다.

```promql
# 모든 http_requests_total 시계열의 현재 값
http_requests_total
```

**결과:**
```
http_requests_total{method="GET", status="200"} 1523
http_requests_total{method="GET", status="500"} 12
http_requests_total{method="POST", status="201"} 342
```

### Range Vector (범위 벡터)

시간 범위 내의 값들입니다. `[시간]` 형식으로 지정합니다.

```promql
# 최근 5분간의 데이터 포인트들
http_requests_total[5m]
```

**결과:**
```
http_requests_total{method="GET", status="200"}
  1500 @1704700500
  1510 @1704700515
  1523 @1704700530
```

### 시간 단위

| 단위 | 의미 | 예시 |
|------|------|------|
| `s` | 초 | `[30s]` |
| `m` | 분 | `[5m]` |
| `h` | 시간 | `[1h]` |
| `d` | 일 | `[7d]` |
| `w` | 주 | `[2w]` |
| `y` | 년 | `[1y]` |

### Scalar (스칼라)

단일 숫자 값입니다.

```promql
# 숫자 리터럴
100

# 집계 결과
count(up)
```

---

## 레이블 매칭

### 기본 매처

| 매처 | 의미 | 예시 |
|------|------|------|
| `=` | 정확히 일치 | `{status="200"}` |
| `!=` | 일치하지 않음 | `{status!="200"}` |
| `=~` | 정규식 일치 | `{status=~"2.."}` |
| `!~` | 정규식 불일치 | `{status!~"2.."}` |

### 예시

```promql
# status가 200인 것만
http_requests_total{status="200"}

# status가 200이 아닌 것
http_requests_total{status!="200"}

# status가 5xx인 것 (정규식)
http_requests_total{status=~"5.."}

# status가 2xx 또는 3xx인 것
http_requests_total{status=~"[23].."}

# method가 GET 또는 POST인 것
http_requests_total{method=~"GET|POST"}
```

### 여러 조건 결합

```promql
# AND 조건 (모든 조건 만족)
http_requests_total{method="GET", status="200", path="/api"}

# OR 조건 (정규식 사용)
http_requests_total{status=~"200|201|204"}
```

{{< callout type="warning" >}}
**주의**: 레이블 매처 없이 메트릭명만 사용하면 해당 메트릭의 **모든 시계열**을 반환합니다. 카디널리티가 높은 메트릭에서는 성능 문제가 발생할 수 있습니다.
{{< /callout >}}

---

## 연산자

### 산술 연산자

| 연산자 | 의미 |
|--------|------|
| `+` | 더하기 |
| `-` | 빼기 |
| `*` | 곱하기 |
| `/` | 나누기 |
| `%` | 나머지 |
| `^` | 거듭제곱 |

```promql
# 바이트를 기가바이트로 변환
node_memory_MemTotal_bytes / 1024 / 1024 / 1024

# 사용률 계산 (퍼센트)
node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes * 100
```

### 비교 연산자

| 연산자 | 의미 |
|--------|------|
| `==` | 같음 |
| `!=` | 다름 |
| `>` | 큼 |
| `<` | 작음 |
| `>=` | 크거나 같음 |
| `<=` | 작거나 같음 |

```promql
# CPU 사용률이 80% 초과인 것만
node_cpu_seconds_total > 0.8

# 타겟이 다운된 것
up == 0

# bool modifier: 조건을 0/1로 반환
up == bool 1
```

### 논리 연산자

| 연산자 | 의미 |
|--------|------|
| `and` | 교집합 |
| `or` | 합집합 |
| `unless` | 차집합 |

```promql
# 두 조건 모두 만족
http_requests_total{status="500"} and http_requests_total{method="GET"}

# 둘 중 하나라도 만족
up{job="app"} or up{job="api"}

# A에는 있지만 B에는 없는 것
http_requests_total unless http_requests_total{status="200"}
```

---

## 벡터 매칭

두 벡터 간 연산 시 라벨이 어떻게 매칭되는지 제어합니다.

### on / ignoring

```promql
# 특정 라벨만 기준으로 매칭
method_total / on(method) method_errors

# 특정 라벨 제외하고 매칭
method_total / ignoring(instance) method_errors
```

### group_left / group_right

다대일 또는 일대다 매칭 시 사용합니다.

```promql
# 좌측이 더 많은 시계열을 가질 때
http_requests_total
* on(job) group_left(version)
app_info
```

---

## Offset 수정자

과거 시점의 데이터를 조회합니다.

```promql
# 1시간 전 값
http_requests_total offset 1h

# 1시간 전 5분 범위
rate(http_requests_total[5m] offset 1h)

# 어제 같은 시간과 비교
rate(http_requests_total[5m])
- rate(http_requests_total[5m] offset 1d)
```

---

## @ 수정자

특정 Unix 타임스탬프 시점의 데이터를 조회합니다.

```promql
# 특정 시점 (Unix timestamp)
http_requests_total @ 1704700800

# 쿼리 시작 시점
http_requests_total @ start()

# 쿼리 종료 시점
http_requests_total @ end()
```

---

## 실전 예제

### 기본 조회

```promql
# 모든 HTTP 요청 수
http_requests_total

# GET 요청만
http_requests_total{method="GET"}

# 5xx 에러만
http_requests_total{status=~"5.."}

# 특정 서비스의 최근 5분 데이터
http_requests_total{service="order-service"}[5m]
```

### 필터링

```promql
# 요청 수가 1000 이상인 엔드포인트
http_requests_total > 1000

# production 환경만
http_requests_total{env="production"}

# 특정 경로 제외
http_requests_total{path!~"/health|/metrics"}
```

### 시간 비교

```promql
# 현재 값
http_requests_total

# 1시간 전 값
http_requests_total offset 1h

# 1일 전 값
http_requests_total offset 1d
```

---

## 자주 하는 실수

### 1. Range Vector를 직접 그래프로

```promql
# ❌ Range Vector는 그래프로 표시 불가
http_requests_total[5m]

# ✅ 함수로 Instant Vector 변환
rate(http_requests_total[5m])
```

### 2. Counter를 직접 비교

```promql
# ❌ Counter는 누적값이므로 의미 없음
http_requests_total > 1000

# ✅ rate()로 변화율 계산 후 비교
rate(http_requests_total[5m]) > 10
```

### 3. 정규식 앵커 누락

```promql
# ❌ "200"을 포함하는 모든 것 (예: "2001", "1200")
{status=~"200"}

# ✅ 정확히 "200"만
{status=~"^200$"}
# 또는 그냥 = 사용
{status="200"}
```

---

## 핵심 정리

| 개념 | 문법 | 예시 |
|------|------|------|
| **Instant Vector** | 메트릭명 | `up` |
| **Range Vector** | `[시간]` | `up[5m]` |
| **레이블 매칭** | `{label="value"}` | `{job="api"}` |
| **정규식** | `=~` | `{status=~"5.."}` |
| **Offset** | `offset 시간` | `up offset 1h` |

---

## 다음 단계

| 추천 순서 | 문서 | 배우는 것 |
|----------|------|----------|
| 1 | [집계 연산자]({{< relref "/docs/observability/concepts/promql/aggregation-operators" >}}) | sum, avg, topk, by/without |
| 2 | [rate와 increase]({{< relref "/docs/observability/concepts/promql/rate-and-increase" >}}) | Counter 처리법 |
