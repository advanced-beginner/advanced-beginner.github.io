---
title: 기본 문법
description: PromQL 셀렉터, 레이블 매칭, 시간 범위 선택 - 쿼리의 기초를 다집니다
weight: 1
author: "@advanced-beginner"
lastmod: "2026-01-15"
---

## 전체 비유: 환자 기록 검색

PromQL 기본 문법을 **병원 EMR 검색 시스템**에 비유하면 이해하기 쉽습니다:

| EMR 검색 비유 | PromQL 문법 | 역할 |
|-------------|------------|------|
| 전체 환자 기록 | 메트릭명 | 모든 시계열 조회 |
| 현재 상태 조회 | Instant Vector | 특정 시점 데이터 |
| 기간별 기록 조회 | Range Vector | 시간 범위 데이터 |
| 진료과 필터 | 레이블 매칭 (=) | 정확히 일치 필터 |
| 제외 검색 | 레이블 매칭 (!=) | 특정 값 제외 |
| 패턴 검색 | 정규식 (=~) | 유연한 필터링 |
| 이전 기록 조회 | offset | 과거 시점 데이터 |
| 기준 비교 (정상 범위) | 비교 연산자 | 임계값 비교 |

이처럼 EMR에서 조건을 조합하여 환자 기록을 검색하듯, PromQL로 메트릭을 필터링하고 조회합니다.

---

> **대상 독자**: PromQL을 처음 배우는 개발자
> **선수 지식**: [메트릭 기초](../metrics-fundamentals/)
> **소요 시간**: 약 25-30분
> **이 문서를 읽으면**: 기본적인 PromQL 쿼리를 작성하고 원하는 메트릭을 조회할 수 있습니다

## TL;DR

{{< callout type="info" >}}
**핵심 요약:**
- **Instant Vector**: 현재 시점의 값 `http_requests_total`
- **Range Vector**: 시간 범위의 값들 `http_requests_total[5m]`
- **레이블 매칭**: `=`, `!=`, `=~`, `!~`로 필터링
- **연산**: 산술(`+`, `-`), 비교(`>`, `<`), 논리(`and`, `or`)
{{< /callout >}}

## 왜 PromQL이 필요한가?

Prometheus는 수천 개의 시계열 데이터를 저장합니다. 하지만 **데이터가 있다고 인사이트가 생기는 것은 아닙니다.** "지금 시스템이 건강한가?", "어제와 비교해서 어떤가?", "어떤 서비스가 문제인가?" 같은 질문에 답하려면 **데이터를 조회하고 변환하는 언어**가 필요합니다. 그것이 PromQL입니다.

### 비유: SQL과 데이터베이스

관계형 데이터베이스에 데이터가 있어도, SQL 없이는 "30세 이상 고객 중 이번 달 구매액이 10만원 이상인 사람"을 찾을 수 없습니다. SQL이 RDBMS의 질의 언어이듯, **PromQL은 시계열 데이터베이스(Prometheus)의 질의 언어**입니다.

| SQL | PromQL |
|-----|--------|
| `SELECT * FROM users WHERE age > 30` | `http_requests_total{status="500"}` |
| `COUNT(*) GROUP BY status` | `sum by (status) (http_requests_total)` |
| `AVG(response_time)` | `avg(http_request_duration_seconds)` |
| 행(Row) 단위 데이터 | 시계열(Time Series) 단위 데이터 |

SQL이 "테이블의 행"을 다룬다면, PromQL은 **"시간 축을 가진 메트릭"**을 다룹니다.

### PromQL이 해결하는 문제

| 질문 | PromQL 답 |
|------|----------|
| "현재 에러율은?" | `rate(http_requests_total{status="500"}[5m])` |
| "P99 응답시간은?" | `histogram_quantile(0.99, rate(http_request_duration_seconds_bucket[5m]))` |
| "어제 같은 시간 대비 트래픽은?" | `rate(http_requests_total[5m]) / rate(http_requests_total[5m] offset 1d)` |
| "CPU 80% 이상인 서버는?" | `node_cpu_usage_percent > 80` |

이런 질문들에 **실시간으로** 답할 수 있어야 모니터링과 알림이 가능해집니다.

---

## 데이터 타입

PromQL을 이해하려면 먼저 **데이터가 어떤 형태로 존재하는지** 알아야 합니다. PromQL의 모든 쿼리는 아래 세 가지 타입 중 하나를 반환합니다.

### Instant Vector (순간 벡터)

**정의**: 특정 시점의 시계열 집합입니다.

**왜 필요한가?**

"지금 이 순간" 시스템 상태를 알고 싶을 때 사용합니다. 대시보드에 표시되는 대부분의 숫자는 Instant Vector입니다.

**비유: 스냅샷 사진**

사진은 "그 순간"을 포착합니다. Instant Vector도 마찬가지로 "쿼리 실행 시점"의 모든 시계열 값을 반환합니다. 각 시계열당 **하나의 값**만 있습니다.

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

**정의**: 시간 범위 내의 값들입니다. `[시간]` 형식으로 지정합니다.

**왜 필요한가?**

"지난 5분간 어떻게 변했는가?"를 알려면 **여러 시점의 데이터**가 필요합니다. `rate()`, `increase()`, `avg_over_time()` 같은 함수는 Range Vector를 입력으로 받아 **추세나 변화율**을 계산합니다.

**비유: 동영상**

사진이 한 순간이라면, 동영상은 시간의 흐름을 담습니다. Range Vector는 "최근 5분간의 프레임들"처럼 **여러 데이터 포인트**를 포함합니다.

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

**정의**: 단일 숫자 값입니다. 레이블이나 시간 정보 없이 **순수한 숫자**만 존재합니다.

**왜 필요한가?**

계산의 기준값이나 임계값으로 사용합니다. "메모리 사용률이 80%를 넘으면"에서 `80`이 스칼라입니다. 또한 `count()`, `scalar()` 같은 함수의 결과가 스칼라입니다.

```promql
# 숫자 리터럴
100

# 집계 결과
count(up)
```

---

## 레이블 매칭

Prometheus의 모든 시계열은 **메트릭 이름 + 레이블 조합**으로 식별됩니다. 같은 `http_requests_total`이라도 `{method="GET", status="200"}`과 `{method="POST", status="500"}`은 **서로 다른 시계열**입니다.

### 왜 레이블 매칭이 필요한가?

시스템에는 수백, 수천 개의 시계열이 있습니다. 전체를 조회하면 의미 없는 데이터 더미가 됩니다. **필요한 시계열만 골라내는 것**이 레이블 매칭의 역할입니다.

**비유: 도서관 검색**

도서관에서 "책"이라고만 검색하면 모든 책이 나옵니다. "저자가 홍길동이고, 분야가 IT이고, 출판년도가 2020년 이후"처럼 **조건을 조합**해야 원하는 책을 찾을 수 있습니다. 레이블 매칭은 이런 "필터 조건"입니다.

```promql
# SQL로 비유하면:
# SELECT * FROM http_requests_total WHERE method='GET' AND status LIKE '5%'

http_requests_total{method="GET", status=~"5.."}
```

### 기본 매처

| 매처 | 의미 | 예시 | SQL 비유 |
|------|------|------|----------|
| `=` | 정확히 일치 | `{status="200"}` | `WHERE status = '200'` |
| `!=` | 일치하지 않음 | `{status!="200"}` | `WHERE status != '200'` |
| `=~` | 정규식 일치 | `{status=~"2.."}` | `WHERE status LIKE '2__'` |
| `!~` | 정규식 불일치 | `{status!~"2.."}` | `WHERE status NOT LIKE '2__'` |

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

Prometheus 메트릭은 원시(raw) 데이터입니다. 이 데이터를 **의미 있는 지표로 변환**하려면 연산이 필요합니다.

### 왜 연산자가 필요한가?

메트릭 원본 값만으로는 답할 수 없는 질문들이 있습니다:

- "메모리 사용률은 몇 퍼센트인가?" → 사용량 / 전체량 × 100 (산술 연산)
- "CPU가 80%를 넘는 서버는?" → CPU > 80 (비교 연산)
- "에러이면서 동시에 GET 요청인 것은?" → 조건 AND 조건 (논리 연산)

연산자는 **원시 데이터를 비즈니스 의미가 있는 지표로 변환**합니다.

### 산술 연산자

**용도**: 단위 변환, 비율/백분율 계산, 정규화

| 연산자 | 의미 | 사용 예 |
|--------|------|--------|
| `+` | 더하기 | 총합 계산 |
| `-` | 빼기 | 차이 계산 |
| `*` | 곱하기 | 백분율 변환 |
| `/` | 나누기 | 비율 계산 |
| `%` | 나머지 | 주기 계산 |
| `^` | 거듭제곱 | 지수 계산 |

```promql
# 바이트를 기가바이트로 변환
node_memory_MemTotal_bytes / 1024 / 1024 / 1024

# 사용률 계산 (퍼센트)
node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes * 100
```

**비유: 엑셀 수식**

엑셀에서 `=A1/B1*100`으로 백분율을 계산하듯, PromQL에서도 메트릭 간 수식 계산이 가능합니다. 단, PromQL은 **같은 레이블을 가진 시계열끼리** 자동 매칭하여 연산합니다.

### 비교 연산자

**용도**: 임계값 기반 필터링, 알림 조건 정의

| 연산자 | 의미 | 알림 예시 |
|--------|------|----------|
| `==` | 같음 | 서비스 다운 감지 |
| `!=` | 다름 | 비정상 상태 감지 |
| `>` | 큼 | 임계값 초과 |
| `<` | 작음 | 리소스 부족 |
| `>=` | 크거나 같음 | SLA 위반 |
| `<=` | 작거나 같음 | 정상 범위 확인 |

**핵심 개념**: 비교 연산자는 **조건을 만족하는 시계열만 반환**합니다. 이것이 알림(Alerting)의 기본 원리입니다.

```promql
# CPU 사용률이 80% 초과인 것만 → 알림 조건으로 사용
node_cpu_seconds_total > 0.8

# 타겟이 다운된 것 → up == 0이면 "죽은 서비스"
up == 0

# bool modifier: 조건을 0/1로 반환 (필터링 대신 참/거짓 값 반환)
up == bool 1
```

### 논리 연산자

**용도**: 복잡한 조건 조합, 집합 연산

논리 연산자는 SQL의 `INNER JOIN`, `UNION`, `EXCEPT`와 유사하게 **두 벡터의 시계열을 조합**합니다.

| 연산자 | 의미 | SQL 비유 |
|--------|------|----------|
| `and` | 교집합 (양쪽 모두 존재) | `INNER JOIN` |
| `or` | 합집합 (한쪽이라도 존재) | `UNION` |
| `unless` | 차집합 (왼쪽에만 존재) | `LEFT JOIN ... WHERE B.id IS NULL` |

```promql
# 두 조건 모두 만족 (에러이면서 GET 요청인 시계열)
http_requests_total{status="500"} and http_requests_total{method="GET"}

# 둘 중 하나라도 만족 (app 또는 api 서비스)
up{job="app"} or up{job="api"}

# A에는 있지만 B에는 없는 것 (200이 아닌 모든 요청)
http_requests_total unless http_requests_total{status="200"}
```

---

## 벡터 매칭

두 벡터 간 연산을 할 때, PromQL은 **레이블이 정확히 일치하는 시계열끼리** 연산합니다. 하지만 실제 환경에서는 레이블이 완벽히 일치하지 않는 경우가 많습니다.

### 왜 벡터 매칭이 필요한가?

예를 들어, "에러율 = 에러 수 / 전체 요청 수"를 계산한다고 합시다.

```promql
http_errors_total / http_requests_total  # 레이블이 다르면 결과 없음!
```

`http_errors_total{method="GET", instance="server1"}`과 `http_requests_total{method="GET", instance="server1", path="/"}`은 레이블이 다르므로 매칭되지 않습니다. 벡터 매칭은 이런 상황에서 **어떤 레이블을 기준으로 매칭할지** 명시적으로 지정합니다.

**비유: 엑셀 VLOOKUP**

엑셀에서 두 시트의 데이터를 합칠 때 "어떤 열을 기준으로 매칭할지" 지정합니다. 벡터 매칭의 `on()`은 "이 레이블만 보고 매칭하라"는 의미입니다.

### on / ignoring

```promql
# on(): 특정 레이블만 기준으로 매칭
# "method 레이블만 같으면 매칭한다"
method_total / on(method) method_errors

# ignoring(): 특정 레이블 제외하고 매칭
# "instance 레이블은 무시하고 나머지가 같으면 매칭한다"
method_total / ignoring(instance) method_errors
```

### group_left / group_right

다대일(many-to-one) 또는 일대다(one-to-many) 매칭 시 사용합니다.

**언제 필요한가?**

메타데이터 메트릭(예: 버전 정보)을 다른 메트릭에 붙일 때 사용합니다. `app_info{job="api", version="2.0"}` 같은 메트릭은 job당 하나지만, `http_requests_total`은 job당 여러 시계열이 있습니다.

```promql
# 좌측(http_requests_total)이 더 많은 시계열을 가질 때
# app_info에서 version 레이블을 가져와서 붙인다
http_requests_total
* on(job) group_left(version)
app_info
```

**결과**: `http_requests_total{job="api", method="GET", version="2.0"}` 처럼 version 레이블이 추가됩니다.

---

## Offset 수정자

### 왜 Offset이 필요한가?

모니터링의 핵심 질문 중 하나는 **"과거와 비교해서 어떤가?"**입니다.

- "지금 트래픽이 어제 같은 시간보다 높은가?"
- "배포 전후로 응답시간이 어떻게 변했는가?"
- "지난주 같은 요일과 비교하면?"

Offset은 현재 시점이 아닌 **과거 시점의 데이터를 조회**합니다.

**비유: 타임머신**

Offset은 "지금으로부터 1시간 전으로 가서 그 시점의 데이터를 봐라"라는 타임머신 명령입니다.

```promql
# 1시간 전 값
http_requests_total offset 1h

# 1시간 전 5분 범위 (과거 시점의 rate)
rate(http_requests_total[5m] offset 1h)

# 어제 같은 시간과 비교 (현재 - 어제)
rate(http_requests_total[5m])
- rate(http_requests_total[5m] offset 1d)
```

### 실전 활용: 주간 비교

```promql
# 지난주 같은 요일 대비 증감률
(rate(http_requests_total[5m]) - rate(http_requests_total[5m] offset 7d))
/ rate(http_requests_total[5m] offset 7d) * 100
```

---

## @ 수정자

### 왜 @ 수정자가 필요한가?

Offset은 "현재로부터 상대적인 시간"을 지정하지만, 때로는 **정확한 특정 시점**의 데이터가 필요합니다.

- "장애가 발생했던 2026-01-10 14:30:00의 상태는?"
- "배포 직전 시점의 메트릭은?"

@ 수정자는 Unix 타임스탬프로 **절대 시점**을 지정합니다.

```promql
# 특정 시점 (Unix timestamp: 2026-01-10 10:00:00 UTC)
http_requests_total @ 1736503200

# 쿼리 시작 시점 (Range Query에서 유용)
http_requests_total @ start()

# 쿼리 종료 시점
http_requests_total @ end()
```

{{< callout type="info" >}}
**Offset vs @**: Offset은 "현재 기준 상대 시간", @는 "절대 시점"입니다. 대부분의 경우 Offset이 더 직관적이지만, 특정 사건 시점을 분석할 때는 @가 유용합니다.
{{< /callout >}}

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
| 1 | [집계 연산자](aggregation-operators/) | sum, avg, topk, by/without |
| 2 | [rate와 increase](rate-and-increase/) | Counter 처리법 |
