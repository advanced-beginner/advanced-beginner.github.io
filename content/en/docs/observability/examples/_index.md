---
title: Practical Examples
description: Experience observability hands-on with executable code
weight: 3
bookCollapseSection: true
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

Implement observability directly with executable code and configurations.

## Learning Path

1. [Environment Setup]({{< relref "/docs/observability/examples/setup" >}}) - Configure Prometheus + Grafana + Loki + Tempo with Docker Compose
2. [Spring Boot Metrics]({{< relref "/docs/observability/examples/spring-boot-metrics" >}}) - Configure Actuator + Micrometer
3. [Kafka Monitoring]({{< relref "/docs/observability/examples/kafka-monitoring" >}}) - Build Kafka cluster observability
4. [Full-Stack Observability]({{< relref "/docs/observability/examples/full-stack" >}}) - Integrate Metrics + Logs + Traces

## Example Project Structure

```
examples/
├── docker-compose.yml       # Complete infrastructure
├── prometheus/
│   ├── prometheus.yml      # Prometheus configuration
│   └── rules/              # Recording/Alerting Rules
├── grafana/
│   ├── provisioning/       # Datasources, dashboards
│   └── dashboards/         # JSON dashboards
└── spring-app/
    └── application.yml     # Spring Boot configuration
```

## Tech Stack

| Component | Tool | Role |
|---------|------|------|
| Metrics Collection | Prometheus | Time-series metrics storage |
| Metrics Exposure | Micrometer | Spring Boot metrics |
| Log Collection | Loki + Promtail | Log storage/querying |
| Distributed Tracing | Tempo | Trace storage |
| Visualization | Grafana | Dashboard |
| Alerting | Alertmanager | Alert routing |

## Prerequisites

- Docker 24.0+
- Docker Compose 2.20+
- 4GB+ memory
- (Optional) Java 17+, Gradle 8+
