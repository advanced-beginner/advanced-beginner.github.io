---
title: 용어 사전
description: Observability 핵심 용어 정의
weight: 1
author: "@advanced-beginner"
lastmod: "2026-01-15"
---

> **빠른 이동**: [A](#a) | [B](#b) | [C](#c) | [D](#d) | [E](#e) | [F](#f) | [G](#g) | [H](#h) | [I](#i) | [J](#j) | [K](#k) | [L](#l) | [M](#m) | [N](#n) | [O](#o) | [P](#p) | [R](#r) | [S](#s) | [T](#t) | [U](#u) | [V](#v) | [W](#w) | [Z](#z)

---

## A

### Alerting Rules
Prometheus에서 조건 기반 알림을 정의하는 규칙. 조건 만족 시 Alertmanager로 알림 전송.

### Alertmanager
Prometheus의 알림을 수신하여 그룹화, 억제, 라우팅하는 컴포넌트.

### Annotation
알림 규칙에서 알림 메시지, 설명, 런북 URL 등을 정의하는 메타데이터.

## B

### Bucket
Histogram에서 값의 분포를 측정하는 구간. `le` (less than or equal) 라벨로 구분. 예: `le="0.5"`는 0.5초 이하.

## C

### Cardinality
고유한 시계열(time series)의 수. 라벨 조합이 많을수록 카디널리티 증가.

### Collector (OTel)
OpenTelemetry Collector. 관측성 데이터를 수신(Receivers), 처리(Processors), 전송(Exporters)하는 에이전트.

### Compaction
Prometheus TSDB에서 오래된 블록들을 병합하여 저장 효율을 높이는 과정.

### Context Propagation
분산 시스템에서 Trace ID, Span ID를 서비스 간에 전달하는 메커니즘.

### Counter
단조 증가하는 메트릭 타입. 요청 수, 에러 수 등에 사용. `rate()` 함수로 변화율 계산.

## D

### Dashboard
메트릭을 시각화하여 시스템 상태를 한눈에 파악할 수 있게 하는 화면. Grafana가 대표적.

### deriv()
Gauge 메트릭의 변화율(미분)을 계산하는 PromQL 함수. Counter에는 rate() 사용.

## E

### ELK Stack
Elasticsearch, Logstash, Kibana의 조합. 로그 수집, 저장, 시각화를 위한 스택.

### Error Budget
SLO에서 허용 가능한 에러의 총량. 예: 99.9% SLO면 0.1%가 에러 버짓.

### Error Rate
전체 요청 중 에러(5xx)가 발생한 비율. 에러율 = 에러 수 / 전체 요청 수.

### Exemplar
메트릭과 연결된 트레이스 샘플. 메트릭에서 관련 트레이스로 직접 이동 가능.

### Exporter
애플리케이션/시스템의 메트릭을 Prometheus 형식으로 노출하는 컴포넌트.

## F

### Federation
Prometheus 인스턴스들을 계층적으로 연결하여 글로벌 뷰를 제공하는 확장 방식.

### Filebeat
Elastic 스택의 경량 로그 수집기. 로그 파일을 읽어 Logstash나 Elasticsearch로 전송.

### Firing
알림이 발동된 상태. `for` 시간 동안 조건이 지속되면 Pending에서 Firing으로 전환.

### Four Golden Signals
Google SRE가 제시한 4대 핵심 지표: Latency, Traffic, Errors, Saturation.

## G

### Gauge
현재 값을 나타내는 메트릭 타입. 증가/감소 가능. CPU 사용률, 온도 등에 사용.

### Grafana
오픈소스 데이터 시각화 및 모니터링 도구. Prometheus, Loki, Tempo 등과 통합.

### Grouping
Alertmanager에서 동일한 유형의 알림들을 하나로 묶어 알림 피로를 줄이는 기능.

## H

### Head Block
Prometheus TSDB에서 최근 2시간 데이터를 메모리에 저장하는 블록.

### Histogram
값의 분포를 버킷으로 측정하는 메트릭 타입. 응답시간 분포 측정에 사용. `histogram_quantile()`로 백분위 계산.

### histogram_quantile()
Histogram 버킷 데이터에서 백분위(P50, P95, P99 등)를 계산하는 PromQL 함수.

## I

### increase()
Counter 메트릭의 시간 범위 내 총 증가량을 계산하는 PromQL 함수. 기간별 합계에 사용.

### Inhibition
Alertmanager에서 특정 알림이 발생했을 때 관련 알림을 억제하는 기능.

### Instant Vector
PromQL에서 특정 시점의 시계열 집합. 대시보드에 표시되는 대부분의 숫자.

### Instrumentation
애플리케이션에 관측성 데이터 수집 코드를 추가하는 것. 자동/수동 계측 방식이 있음.

### irate()
마지막 두 샘플만 사용하여 순간 증가율을 계산하는 PromQL 함수.

## J

### Jaeger
CNCF 분산 추적 도구. 마이크로서비스 환경에서 요청 흐름을 추적하고 시각화.

## K

### Kibana
Elastic 스택의 시각화 도구. Elasticsearch 데이터를 대시보드로 표시.

## L

### Label
메트릭에 붙는 키-값 메타데이터. 필터링과 그룹화에 사용.

### Latency
응답 지연시간. Golden Signal 중 하나. P50, P95, P99 등 백분위로 측정.

### le (Label)
Histogram 버킷의 상한값을 나타내는 라벨. "less than or equal"의 약자.

### LogQL
Grafana Loki의 쿼리 언어. PromQL과 유사한 문법.

### Logstash
Elastic 스택의 로그 처리 파이프라인. 로그 수집, 변환, 전송.

### Loki
Grafana의 로그 수집 시스템. 라벨 기반 인덱싱으로 경량화.

## M

### MeterRegistry
Micrometer에서 메트릭을 등록하고 관리하는 중앙 레지스트리.

### Micrometer
JVM 애플리케이션을 위한 메트릭 파사드. Prometheus, Datadog 등 다양한 백엔드 지원.

### Mimir
Grafana Labs의 장기 메트릭 저장소. Cortex의 후속 프로젝트.

## N

### Native Histogram
Prometheus 2.40+에서 도입된 자동 버킷 관리 히스토그램. 버킷 설계 문제 해결.

## O

### Offset
PromQL에서 현재 시점이 아닌 과거 시점의 데이터를 조회하는 수정자. 예: `offset 1h`.

### OpenTelemetry (OTel)
메트릭, 로그, 트레이스를 위한 벤더 중립적 관측성 표준 프레임워크.

### OTLP
OpenTelemetry Protocol. 관측성 데이터 전송을 위한 표준 프로토콜.

## P

### P50, P95, P99
백분위수. P99는 전체 요청의 99%가 이 값 이하임을 의미. 응답시간 측정에 주로 사용.

### Pending
알림 조건이 만족되었지만 `for` 시간이 경과하지 않은 대기 상태.

### Percentile (백분위)
데이터 분포에서 특정 비율 이하에 해당하는 값. P99 = 99%가 이 값 이하.

### PromQL
Prometheus Query Language. 시계열 데이터 조회 및 분석 언어.

### Promtail
Grafana Loki의 로그 수집 에이전트. 로그를 읽어 Loki로 전송.

### Pull Model
Prometheus가 타겟을 찾아가서 메트릭을 수집하는 방식 (Push의 반대).

### Push Model
애플리케이션이 모니터링 시스템으로 메트릭을 전송하는 방식 (Pull의 반대).

### Pushgateway
Prometheus에서 짧은 수명의 배치 작업 메트릭을 임시 저장하는 컴포넌트.

## R

### Range Vector
PromQL에서 시간 범위 내의 값들. `[5m]` 형식으로 지정. rate(), increase() 등의 입력.

### rate()
Counter의 초당 평균 증가율을 계산하는 PromQL 함수.

### Rate Limiting Sampling
초당 N개까지만 트레이스를 수집하는 샘플링 방식. 트래픽 급증 시 유용.

### Receiver
Alertmanager에서 알림을 수신하여 Slack, PagerDuty 등으로 전송하는 설정.

### Recording Rules
복잡한 쿼리를 미리 계산하여 새 메트릭으로 저장하는 Prometheus 규칙.

### RED Method
Rate, Errors, Duration을 측정하는 마이크로서비스 모니터링 방법론.

### Relabeling
Prometheus에서 스크래핑 전후에 라벨을 변환, 추가, 삭제하는 기능.

### Remote Write/Read
Prometheus에서 외부 장기 저장소(Thanos, VictoriaMetrics 등)와 데이터를 주고받는 기능.

### Root Span
Trace에서 첫 번째 Span. Parent Span이 없음. 요청의 시작점.

### Routing
Alertmanager에서 알림을 조건에 따라 다른 수신자에게 전달하는 기능.

### RPS (Requests Per Second)
초당 요청 수. 트래픽을 측정하는 기본 지표.

## S

### Sampling
전체 트레이스 중 일부만 저장하는 기법. 비용 최적화 목적.

### Saturation
시스템 리소스의 포화도. Golden Signal 중 하나. CPU, 메모리 사용률 등.

### Scalar
PromQL에서 레이블 없는 단일 숫자 값. 임계값 비교 등에 사용.

### Scrape
Prometheus가 타겟에서 메트릭을 수집하는 행위.

### Scrape Interval
Prometheus가 타겟에서 메트릭을 수집하는 주기. 기본값 15초.

### Semantic Conventions
OpenTelemetry에서 정의한 표준화된 속성 이름 규칙. `http.method`, `db.system` 등.

### Service Discovery
Prometheus가 모니터링 대상(타겟)을 자동으로 발견하는 기능. Kubernetes, Consul 등 지원.

### Service Level Agreement (SLA)
서비스 제공자와 사용자 간의 서비스 품질 계약. SLO 위반 시 보상 조건 포함.

### Service Level Indicator (SLI)
서비스 수준을 측정하는 지표. 예: P99 응답시간, 에러율.

### Service Level Objective (SLO)
SLI의 목표값. 예: P99 < 500ms, 가용성 99.9%.

### Silencing
Alertmanager에서 특정 시간 동안 알림을 무시하는 기능. 유지보수 중 사용.

### Span
분산 추적에서 단일 작업 단위. Trace는 여러 Span으로 구성.

### Span ID
개별 Span을 식별하는 고유 ID.

### Structured Log
JSON 등 구조화된 형식의 로그. 필드별 검색과 파싱이 용이.

### Summary
클라이언트에서 백분위를 미리 계산하는 메트릭 타입. 집계 불가로 Histogram 권장.

## T

### Tail-based Sampling
요청 완료 후 에러/느린 요청을 우선 저장하는 샘플링 방식.

### Tempo
Grafana의 분산 추적 백엔드. 대용량 트레이스 저장에 최적화.

### Thanos
Prometheus의 장기 저장소. 오브젝트 스토리지 기반, 글로벌 뷰 제공.

### Three Pillars (3요소)
Observability의 세 기둥: Metrics, Logs, Traces.

### Time Series
시간에 따라 기록된 데이터 포인트의 연속. 메트릭명 + 라벨 조합으로 고유 식별.

### Timer
Micrometer에서 작업 시간을 측정하는 메트릭. Histogram 기반.

### Trace
분산 시스템에서 하나의 요청이 지나가는 전체 경로. 여러 Span으로 구성.

### Trace ID
트레이스를 식별하는 고유 ID. 모든 Span이 동일한 Trace ID를 공유.

### Traffic
시스템이 처리하는 요청량. Golden Signal 중 하나. RPS, 처리량으로 측정.

### TSDB (Time Series Database)
시계열 데이터 저장에 최적화된 데이터베이스. Prometheus 내장 TSDB가 대표적.

## U

### USE Method
Utilization, Saturation, Errors를 측정하는 리소스 모니터링 방법론.

## V

### VictoriaMetrics
고성능 시계열 데이터베이스. Prometheus 호환, 장기 저장소로 사용.

## W

### W3C Trace Context
분산 추적을 위한 HTTP 헤더 표준. `traceparent` 헤더 사용.

### WAL (Write-Ahead Log)
데이터 손실 방지를 위해 메모리 데이터를 디스크에 먼저 기록하는 로그.

## Z

### Zipkin
오픈소스 분산 추적 시스템. 가볍고 설치가 쉬움.
