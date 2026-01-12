---
title: Errors (에러)
description: 에러율을 정의하고 분류하여 서비스 신뢰성을 모니터링합니다
weight: 3
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

> **대상 독자**: 서비스 신뢰성을 개선하려는 개발자, SRE
> **선수 지식**: [집계 연산자]({{< relref "/docs/observability/concepts/promql/aggregation-operators" >}})
> **이 문서를 읽으면**: 에러를 체계적으로 분류하고 SLO 기반 모니터링을 설정할 수 있습니다

## TL;DR

{{< callout type="info" >}}
**핵심 요약:**
- **에러율**: 실패 요청 / 전체 요청
- HTTP 5xx만 에러가 아님: 비즈니스 로직 실패도 포함
- **에러 버짓**: 허용 가능한 에러 양 (SLO 기반)
- 에러 **분류**가 중요: 클라이언트 vs 서버, 일시적 vs 영구적
{{< /callout >}}

## 에러 정의

### 무엇이 에러인가?

| 유형 | 예시 | 에러 여부 |
|------|------|----------|
| HTTP 5xx | 500, 502, 503 | ✅ 서버 에러 |
| HTTP 4xx | 400, 404, 429 | ⚠️ 상황에 따라 |
| 타임아웃 | 요청 시간 초과 | ✅ 에러 |
| 비즈니스 실패 | 결제 실패, 재고 부족 | ⚠️ 정의 필요 |
| 느린 응답 | SLA 초과 응답 | ⚠️ 정의 필요 |

{{< callout type="warning" >}}
**4xx는 상황에 따라 다릅니다:**
- `400 Bad Request`: 클라이언트 버그 → 에러로 집계 가능
- `404 Not Found`: 정상적인 탐색 → 제외할 수 있음
- `429 Too Many Requests`: 의도적 제한 → 제외
{{< /callout >}}

### 에러 분류 체계

```mermaid
graph TD
    E["에러"] --> C["클라이언트 에러<br>(4xx)"]
    E --> S["서버 에러<br>(5xx)"]

    C --> C1["잘못된 요청<br>400, 422"]
    C --> C2["인증/권한<br>401, 403"]
    C --> C3["없는 리소스<br>404"]
    C --> C4["제한 초과<br>429"]

    S --> S1["내부 오류<br>500"]
    S --> S2["의존성 실패<br>502, 503"]
    S --> S3["타임아웃<br>504"]
```

---

## 측정 방법

### 기본 에러율

```promql
# 5xx 에러율 (%)
sum(rate(http_requests_total{status=~"5.."}[5m]))
/ sum(rate(http_requests_total[5m]))
* 100

# 서비스별 에러율
sum by (service) (rate(http_requests_total{status=~"5.."}[5m]))
/ sum by (service) (rate(http_requests_total[5m]))
* 100
```

### 확장된 에러율 (4xx 포함)

```promql
# 4xx + 5xx (특정 코드 제외)
sum(rate(http_requests_total{status=~"[45]..", status!~"404|429"}[5m]))
/ sum(rate(http_requests_total[5m]))
* 100
```

### 에러 수

```promql
# 초당 에러 수
sum(rate(http_requests_total{status=~"5.."}[5m]))

# 1시간 에러 총 수
sum(increase(http_requests_total{status=~"5.."}[1h]))

# 상태 코드별 에러 수
sum by (status) (rate(http_requests_total{status=~"[45].."}[5m]))
```

### 가용성 (반대 지표)

```promql
# 가용성 = 1 - 에러율
(1 - (
  sum(rate(http_requests_total{status=~"5.."}[5m]))
  / sum(rate(http_requests_total[5m]))
)) * 100

# 99.9% 가용성 = 0.1% 에러율
```

---

## SLO와 에러 버짓

### SLO 정의

| SLO | 허용 에러율 | 월간 허용 다운타임 |
|-----|-----------|------------------|
| 99% | 1% | 7.2시간 |
| 99.9% | 0.1% | 43.2분 |
| 99.99% | 0.01% | 4.3분 |

### 에러 버짓 계산

```promql
# 월간 에러 버짓 (99.9% SLO)
# 허용 에러율: 0.1% = 0.001

# 현재 에러율
sum(rate(http_requests_total{status=~"5.."}[30d]))
/ sum(rate(http_requests_total[30d]))

# 남은 에러 버짓 (%)
(0.001 - (
  sum(rate(http_requests_total{status=~"5.."}[30d]))
  / sum(rate(http_requests_total[30d]))
)) / 0.001 * 100
```

### 에러 버짓 소진 속도

```promql
# 현재 속도로 에러 버짓 소진까지 남은 시간
# burn rate = 현재 에러율 / 허용 에러율
# 남은 시간 = 남은 버짓 / burn rate

# 예: burn rate 2 = 2배 속도로 에러 발생
# 30일 버짓을 15일 만에 소진
```

---

## 알림 규칙

### 기본 에러율 알림

```yaml
groups:
  - name: error_alerts
    rules:
      # 에러율 1% 초과 (warning)
      - alert: HighErrorRate
        expr: |
          sum by (service) (rate(http_requests_total{status=~"5.."}[5m]))
          / sum by (service) (rate(http_requests_total[5m]))
          > 0.01
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "{{ $labels.service }} error rate is {{ $value | humanizePercentage }}"

      # 에러율 5% 초과 (critical)
      - alert: CriticalErrorRate
        expr: |
          sum by (service) (rate(http_requests_total{status=~"5.."}[5m]))
          / sum by (service) (rate(http_requests_total[5m]))
          > 0.05
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "{{ $labels.service }} error rate critical: {{ $value | humanizePercentage }}"
```

### 에러 버짓 기반 알림

```yaml
      # 에러 버짓 50% 소진
      - alert: ErrorBudget50PercentConsumed
        expr: |
          (
            sum(rate(http_requests_total{status=~"5.."}[7d]))
            / sum(rate(http_requests_total[7d]))
          ) > (0.001 * 0.5 * 30 / 7)  # 주간으로 환산
        for: 1h
        labels:
          severity: warning
        annotations:
          summary: "Error budget 50% consumed this month"

      # 에러 버짓 급속 소진 (burn rate > 10)
      - alert: HighErrorBudgetBurnRate
        expr: |
          (
            sum(rate(http_requests_total{status=~"5.."}[1h]))
            / sum(rate(http_requests_total[1h]))
          ) / 0.001 > 10
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Error budget burning 10x faster than allowed"
```

### 새로운 에러 유형 감지

```yaml
      # 갑자기 등장한 에러 패턴
      - alert: NewErrorPattern
        expr: |
          sum by (service, status, path) (rate(http_requests_total{status=~"5.."}[5m])) > 0
          unless
          sum by (service, status, path) (rate(http_requests_total{status=~"5.."}[5m] offset 1h)) > 0
        for: 5m
        labels:
          severity: info
        annotations:
          summary: "New error pattern detected: {{ $labels.service }} {{ $labels.path }} {{ $labels.status }}"
```

---

## 에러 분석

### 에러 분포

```promql
# 상태 코드별 비율
sum by (status) (rate(http_requests_total{status=~"[45].."}[5m]))
/ ignoring(status) sum(rate(http_requests_total{status=~"[45].."}[5m]))
* 100

# 엔드포인트별 에러 집중도
topk(10,
  sum by (path) (rate(http_requests_total{status=~"5.."}[5m]))
)
```

### 에러 급증 탐지

```promql
# 1시간 전 대비 에러율 변화
sum(rate(http_requests_total{status=~"5.."}[5m]))
/ sum(rate(http_requests_total[5m]))
-
sum(rate(http_requests_total{status=~"5.."}[5m] offset 1h))
/ sum(rate(http_requests_total[5m] offset 1h))
```

---

## 대시보드 설계

### 권장 패널 구성

```
┌─────────────────────────────────────────────────────┐
│ Stat: Error Rate │ Stat: Error Count │ Stat: Budget │
├─────────────────────────────────────────────────────┤
│ Time Series: 에러율 추이 (5xx, 4xx 분리)             │
├─────────────────────────────────────────────────────┤
│ Pie Chart: 상태 코드별 분포                          │
├─────────────────────────────────────────────────────┤
│ Table: 에러 많은 엔드포인트 Top 10                   │
└─────────────────────────────────────────────────────┘
```

---

## Recording Rules

```yaml
groups:
  - name: error_rules
    rules:
      # 서비스별 에러율
      - record: service:http_requests_errors:ratio_rate5m
        expr: |
          sum by (service) (rate(http_requests_total{status=~"5.."}[5m]))
          / sum by (service) (rate(http_requests_total[5m]))

      # 전체 에러율
      - record: :http_requests_errors:ratio_rate5m
        expr: |
          sum(rate(http_requests_total{status=~"5.."}[5m]))
          / sum(rate(http_requests_total[5m]))

      # 가용성
      - record: service:http_requests_availability:ratio_rate5m
        expr: |
          1 - (
            sum by (service) (rate(http_requests_total{status=~"5.."}[5m]))
            / sum by (service) (rate(http_requests_total[5m]))
          )
```

---

## 핵심 정리

| 지표 | 계산 | 용도 |
|------|------|------|
| 에러율 | 5xx / 전체 | SLO 모니터링 |
| 에러 수 | `increase()` | 이벤트 집계 |
| 가용성 | 1 - 에러율 | SLA 보고 |
| 에러 버짓 | 허용량 - 사용량 | 릴리스 결정 |

---

## 다음 단계

| 추천 순서 | 문서 | 배우는 것 |
|----------|------|----------|
| 1 | [Saturation]({{< relref "/docs/observability/concepts/golden-signals/saturation" >}}) | 리소스 포화도 |
| 2 | [알림 후 액션 가이드]({{< relref "/docs/observability/appendix/alerting-actions" >}}) | 에러 대응 방법 |
