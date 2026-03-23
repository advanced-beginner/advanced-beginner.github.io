---
title: Kafka 모니터링
description: Kafka 클러스터의 핵심 메트릭을 수집하고 모니터링합니다
weight: 3
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

> **소요 시간**: 20분
> **선수 지식**: Kafka 기초, [환경 구성]({{< relref "/docs/observability/examples/setup" >}})
> **이 문서를 읽으면**: Kafka 클러스터와 Consumer Lag을 모니터링할 수 있습니다

## Kafka 모니터링 핵심 지표

| 카테고리 | 지표 | 의미 |
|----------|------|------|
| **Consumer** | Lag | 처리 지연 (가장 중요) |
| **Broker** | Under-replicated Partitions | 복제 문제 |
| **Producer** | Record Error Rate | 전송 실패율 |
| **Broker** | Disk Usage | 저장 공간 |

## Step 1: Kafka + JMX Exporter 설정

```yaml
# docker-compose.yml에 추가
services:
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: kafka
    ports:
      - "9092:9092"
      - "9101:9101"  # JMX
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:29093
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:29093
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      CLUSTER_ID: MkU3OEVBNTcwNTJENDM2Qk
      KAFKA_JMX_PORT: 9101
      KAFKA_JMX_HOSTNAME: localhost
      KAFKA_OPTS: -javaagent:/opt/jmx-exporter/jmx_prometheus_javaagent.jar=7071:/opt/jmx-exporter/kafka.yml
    volumes:
      - ./jmx-exporter:/opt/jmx-exporter

  kafka-exporter:
    image: danielqsj/kafka-exporter:latest
    container_name: kafka-exporter
    ports:
      - "9308:9308"
    command:
      - --kafka.server=kafka:9092
      - --topic.filter=.*
      - --group.filter=.*
```

## Step 2: JMX Exporter 설정

```bash
mkdir -p jmx-exporter
curl -L https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.19.0/jmx_prometheus_javaagent-0.19.0.jar -o jmx-exporter/jmx_prometheus_javaagent.jar
```

```yaml
# jmx-exporter/kafka.yml
lowercaseOutputName: true
rules:
  # Broker 메트릭
  - pattern: kafka.server<type=(.+), name=(.+), clientId=(.+), topic=(.+), partition=(.*)><>Value
    name: kafka_server_$1_$2
    type: GAUGE
    labels:
      clientId: "$3"
      topic: "$4"
      partition: "$5"

  - pattern: kafka.server<type=(.+), name=(.+)><>Value
    name: kafka_server_$1_$2
    type: GAUGE

  # Under-replicated Partitions
  - pattern: kafka.server<type=ReplicaManager, name=UnderReplicatedPartitions><>Value
    name: kafka_server_replicamanager_underreplicatedpartitions
    type: GAUGE

  # Messages In Per Sec
  - pattern: kafka.server<type=BrokerTopicMetrics, name=MessagesInPerSec, topic=(.+)><>OneMinuteRate
    name: kafka_server_brokertopicmetrics_messagesin_total
    type: GAUGE
    labels:
      topic: "$1"

  # Bytes In Per Sec
  - pattern: kafka.server<type=BrokerTopicMetrics, name=BytesInPerSec, topic=(.+)><>OneMinuteRate
    name: kafka_server_brokertopicmetrics_bytesin_total
    type: GAUGE
    labels:
      topic: "$1"
```

## Step 3: Prometheus 설정 추가

```yaml
# prometheus/prometheus.yml에 추가
scrape_configs:
  - job_name: 'kafka'
    static_configs:
      - targets: ['kafka:7071']

  - job_name: 'kafka-exporter'
    static_configs:
      - targets: ['kafka-exporter:9308']
```

## Step 4: 핵심 PromQL 쿼리

### Consumer Lag

```promql
# 컨슈머 그룹별 Lag
sum by (consumergroup, topic) (kafka_consumergroup_lag)

# Lag이 10000 이상인 그룹
sum by (consumergroup, topic) (kafka_consumergroup_lag) > 10000

# Lag 추이
sum(kafka_consumergroup_lag) by (consumergroup)
```

### Broker 상태

```promql
# Under-replicated Partitions
kafka_server_replicamanager_underreplicatedpartitions

# ISR (In-Sync Replicas) 축소
kafka_server_replicamanager_isrshrinks_total
```

### 트래픽

```promql
# 토픽별 초당 메시지 수
sum by (topic) (rate(kafka_server_brokertopicmetrics_messagesin_total[5m]))

# 토픽별 초당 바이트
sum by (topic) (rate(kafka_server_brokertopicmetrics_bytesin_total[5m]))
```

## Step 5: 알림 규칙

```yaml
# prometheus/rules/kafka-alerts.yml
groups:
  - name: kafka
    rules:
      - alert: KafkaConsumerLagHigh
        expr: sum by (consumergroup, topic) (kafka_consumergroup_lag) > 10000
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "High consumer lag: {{ $labels.consumergroup }}"
          description: "Lag is {{ $value }} on topic {{ $labels.topic }}"

      - alert: KafkaUnderReplicatedPartitions
        expr: kafka_server_replicamanager_underreplicatedpartitions > 0
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Kafka under-replicated partitions"
          description: "{{ $value }} partitions are under-replicated"

      - alert: KafkaBrokerDown
        expr: count(kafka_server_kafkaserver_brokerstate) < 3
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Kafka broker down"
```

## Step 6: Recording Rules

```yaml
# prometheus/rules/kafka-recording.yml
groups:
  - name: kafka_recording
    rules:
      - record: topic:kafka_messages:rate5m
        expr: sum by (topic) (rate(kafka_server_brokertopicmetrics_messagesin_total[5m]))

      - record: consumergroup:kafka_lag:sum
        expr: sum by (consumergroup) (kafka_consumergroup_lag)

      - record: :kafka_underreplicated:sum
        expr: sum(kafka_server_replicamanager_underreplicatedpartitions)
```

## Step 7: Grafana 대시보드

### Row 1: Overview

| 패널 | 쿼리 | 타입 |
|------|------|------|
| Total Lag | `sum(kafka_consumergroup_lag)` | Stat |
| Messages/sec | `sum(rate(kafka_server_brokertopicmetrics_messagesin_total[5m]))` | Stat |
| Under-replicated | `sum(kafka_server_replicamanager_underreplicatedpartitions)` | Stat |

### Row 2: Consumer Lag

```promql
# Time Series: Lag 추이
sum by (consumergroup) (kafka_consumergroup_lag)

# Table: 상세
sum by (consumergroup, topic, partition) (kafka_consumergroup_lag)
```

### Row 3: Traffic

```promql
# 토픽별 메시지/초
sum by (topic) (rate(kafka_server_brokertopicmetrics_messagesin_total[5m]))
```

---

## Spring Kafka 메트릭

```yaml
# application.yml
management:
  metrics:
    enable:
      kafka: true
```

```promql
# Producer 메트릭
kafka_producer_record_send_total
kafka_producer_record_error_total

# Consumer 메트릭
kafka_consumer_records_consumed_total
kafka_consumer_fetch_manager_records_lag
```

---

## 확인 체크리스트

- [ ] kafka-exporter 메트릭 노출 확인
- [ ] Consumer Lag 쿼리 동작 확인
- [ ] Grafana 대시보드 구성
- [ ] 알림 규칙 테스트

---

## 다음 단계

| 추천 순서 | 문서 | 배우는 것 |
|----------|------|----------|
| 1 | [풀스택 예제](full-stack/) | 통합 예제 |
| 2 | [서비스 유형별 적용](../concepts/golden-signals/by-service-type/) | Kafka 황금 신호 |
