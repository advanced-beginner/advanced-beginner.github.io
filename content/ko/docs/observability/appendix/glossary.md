---
title: 용어 사전
description: Observability 핵심 용어 정의
weight: 1
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

> **빠른 이동**: [A](#a) | [C](#c) | [E](#e) | [F](#f) | [G](#g) | [H](#h) | [I](#i) | [L](#l) | [M](#m) | [O](#o) | [P](#p) | [R](#r) | [S](#s) | [T](#t) | [U](#u) | [W](#w)

---

## A

### Alerting Rules
Prometheus에서 조건 기반 알림을 정의하는 규칙. 조건 만족 시 Alertmanager로 알림 전송.

### Alertmanager
Prometheus의 알림을 수신하여 그룹화, 억제, 라우팅하는 컴포넌트.

## C

### Cardinality
고유한 시계열(time series)의 수. 라벨 조합이 많을수록 카디널리티 증가.

### Context Propagation
분산 시스템에서 Trace ID, Span ID를 서비스 간에 전달하는 메커니즘.

### Counter
단조 증가하는 메트릭 타입. 요청 수, 에러 수 등에 사용. `rate()` 함수로 변화율 계산.

## E

### Exemplar
메트릭과 연결된 트레이스 샘플. 메트릭에서 관련 트레이스로 직접 이동 가능.

### Exporter
애플리케이션/시스템의 메트릭을 Prometheus 형식으로 노출하는 컴포넌트.

## F

### Four Golden Signals
Google SRE가 제시한 4대 핵심 지표: Latency, Traffic, Errors, Saturation.

## G

### Gauge
현재 값을 나타내는 메트릭 타입. 증가/감소 가능. CPU 사용률, 온도 등에 사용.

## H

### Histogram
값의 분포를 버킷으로 측정하는 메트릭 타입. 응답시간 분포 측정에 사용. `histogram_quantile()`로 백분위 계산.

## I

### Instrumentation
애플리케이션에 관측성 데이터 수집 코드를 추가하는 것. 자동/수동 계측 방식이 있음.

### irate()
마지막 두 샘플만 사용하여 순간 증가율을 계산하는 PromQL 함수.

## L

### Label
메트릭에 붙는 키-값 메타데이터. 필터링과 그룹화에 사용.

### LogQL
Grafana Loki의 쿼리 언어. PromQL과 유사한 문법.

### Loki
Grafana의 로그 수집 시스템. 라벨 기반 인덱싱으로 경량화.

## M

### Micrometer
JVM 애플리케이션을 위한 메트릭 파사드. Prometheus, Datadog 등 다양한 백엔드 지원.

## O

### OpenTelemetry (OTel)
메트릭, 로그, 트레이스를 위한 벤더 중립적 관측성 표준 프레임워크.

### OTLP
OpenTelemetry Protocol. 관측성 데이터 전송을 위한 표준 프로토콜.

## P

### Percentile (백분위)
데이터 분포에서 특정 비율 이하에 해당하는 값. P99 = 99%가 이 값 이하.

### PromQL
Prometheus Query Language. 시계열 데이터 조회 및 분석 언어.

### Pull Model
Prometheus가 타겟을 찾아가서 메트릭을 수집하는 방식 (Push의 반대).

## R

### rate()
Counter의 초당 평균 증가율을 계산하는 PromQL 함수.

### Recording Rules
복잡한 쿼리를 미리 계산하여 새 메트릭으로 저장하는 Prometheus 규칙.

### RED Method
Rate, Errors, Duration을 측정하는 마이크로서비스 모니터링 방법론.

## S

### Sampling
전체 트레이스 중 일부만 저장하는 기법. 비용 최적화 목적.

### Scrape
Prometheus가 타겟에서 메트릭을 수집하는 행위.

### Service Level Indicator (SLI)
서비스 수준을 측정하는 지표. 예: P99 응답시간, 에러율.

### Service Level Objective (SLO)
SLI의 목표값. 예: P99 < 500ms, 가용성 99.9%.

### Span
분산 추적에서 단일 작업 단위. Trace는 여러 Span으로 구성.

## T

### Tail-based Sampling
요청 완료 후 에러/느린 요청을 우선 저장하는 샘플링 방식.

### Tempo
Grafana의 분산 추적 백엔드. 대용량 트레이스 저장에 최적화.

### Three Pillars (3요소)
Observability의 세 기둥: Metrics, Logs, Traces.

### Trace
분산 시스템에서 하나의 요청이 지나가는 전체 경로. 여러 Span으로 구성.

### Trace ID
트레이스를 식별하는 고유 ID. 모든 Span이 동일한 Trace ID를 공유.

## U

### USE Method
Utilization, Saturation, Errors를 측정하는 리소스 모니터링 방법론.

## W

### W3C Trace Context
분산 추적을 위한 HTTP 헤더 표준. `traceparent` 헤더 사용.
