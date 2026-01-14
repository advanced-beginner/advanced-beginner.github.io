---
title: Kafka Monitoring
description: Collect and monitor core metrics from Kafka clusters
weight: 3
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

> **Time Required**: 20 minutes
> **Prerequisites**: Kafka basics, [Environment Setup]({{< relref "/docs/observability/examples/setup" >}})
> **What You'll Learn**: Monitor Kafka clusters and Consumer Lag

## Core Kafka Monitoring Metrics

| Category | Metric | Meaning |
|----------|------|------|
| **Consumer** | Lag | Processing delay (most important) |
| **Broker** | Under-replicated Partitions | Replication issues |
| **Producer** | Record Error Rate | Send failure rate |
| **Broker** | Disk Usage | Storage space |

## Step 1: Kafka + JMX Exporter Configuration

```yaml
# Add to docker-compose.yml
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

## Step 2: JMX Exporter Configuration

```bash
mkdir -p jmx-exporter
curl -L https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.19.0/jmx_prometheus_javaagent-0.19.0.jar -o jmx-exporter/jmx_prometheus_javaagent.jar
```

```yaml
# jmx-exporter/kafka.yml
lowercaseOutputName: true
rules:
  # Broker metrics
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

## Step 3: Add Prometheus Configuration

```yaml
# Add to prometheus/prometheus.yml
scrape_configs:
  - job_name: 'kafka'
    static_configs:
      - targets: ['kafka:7071']

  - job_name: 'kafka-exporter'
    static_configs:
      - targets: ['kafka-exporter:9308']
```

## Step 4: Core PromQL Queries

### Consumer Lag

```promql
# Lag by consumer group
sum by (consumergroup, topic) (kafka_consumergroup_lag)

# Groups with lag over 10000
sum by (consumergroup, topic) (kafka_consumergroup_lag) > 10000

# Lag trends
sum(kafka_consumergroup_lag) by (consumergroup)
```

### Broker Status

```promql
# Under-replicated Partitions
kafka_server_replicamanager_underreplicatedpartitions

# ISR (In-Sync Replicas) shrinkage
kafka_server_replicamanager_isrshrinks_total
```

### Traffic

```promql
# Messages per second by topic
sum by (topic) (rate(kafka_server_brokertopicmetrics_messagesin_total[5m]))

# Bytes per second by topic
sum by (topic) (rate(kafka_server_brokertopicmetrics_bytesin_total[5m]))
```

## Step 5: Alert Rules

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

## Step 7: Grafana Dashboard

### Row 1: Overview

| Panel | Query | Type |
|------|------|------|
| Total Lag | `sum(kafka_consumergroup_lag)` | Stat |
| Messages/sec | `sum(rate(kafka_server_brokertopicmetrics_messagesin_total[5m]))` | Stat |
| Under-replicated | `sum(kafka_server_replicamanager_underreplicatedpartitions)` | Stat |

### Row 2: Consumer Lag

```promql
# Time Series: Lag trends
sum by (consumergroup) (kafka_consumergroup_lag)

# Table: Details
sum by (consumergroup, topic, partition) (kafka_consumergroup_lag)
```

### Row 3: Traffic

```promql
# Messages per second by topic
sum by (topic) (rate(kafka_server_brokertopicmetrics_messagesin_total[5m]))
```

---

## Spring Kafka Metrics

```yaml
# application.yml
management:
  metrics:
    enable:
      kafka: true
```

```promql
# Producer metrics
kafka_producer_record_send_total
kafka_producer_record_error_total

# Consumer metrics
kafka_consumer_records_consumed_total
kafka_consumer_fetch_manager_records_lag
```

---

## Verification Checklist

- [ ] kafka-exporter metrics exposed
- [ ] Consumer Lag query works
- [ ] Grafana dashboard configured
- [ ] Alert rules tested

---

## Next Steps

| Recommended Order | Document | What You'll Learn |
|----------|------|----------|
| 1 | [Full-Stack Example]({{< relref "/docs/observability/examples/full-stack" >}}) | Integrated example |
| 2 | [By Service Type]({{< relref "/docs/observability/concepts/golden-signals/by-service-type" >}}) | Kafka golden signals |
