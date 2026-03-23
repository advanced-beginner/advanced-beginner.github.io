---
title: Observability
bookCollapseSection: true
description: Practical guide to system observability - From the three pillars of Metrics, Logs, and Traces to advanced Prometheus, Grafana, and PromQL
weight: 5
author: "@advanced-beginner"
lastmod: "2026-01-16"
---

## What is Observability?

Observability is **the ability to understand internal system states from external outputs**. It's not just about installing monitoring tools, but building a system that can answer the question "Why did this problem occur?"

### Why is Observability Necessary?

As microservices and distributed systems become prevalent, the limitations of traditional monitoring have become clear.

| Limitations of Traditional Monitoring | After Adopting Observability |
|---------------------|----------------------|
| Only know "server CPU is high" | Can trace which requests are causing high CPU |
| Can only check pre-defined metrics | Can analyze unexpected problems with data |
| Difficult to understand service connections | Visualize entire flow with distributed tracing |
| Hours to trace root cause after failure | Immediately identify root cause with trace ID |
| Logs, metrics, traces are separated | Integrated analysis by connecting all three pillars |

### The Three Pillars of Observability

```mermaid
graph LR
    subgraph "Three Pillars of Observability"
        M["Metrics<br>Numeric Data"]
        L["Logs<br>Event Records"]
        T["Traces<br>Request Flow"]
    end

    M --> |"Anomaly Detection"| A["Alert"]
    A --> |"Detail Check"| L
    L --> |"Flow Tracking"| T
    T --> |"Performance Analysis"| M
```

| Pillar | Role | Representative Tools | Examples |
|------|------|----------|------|
| **Metrics** | Numeric-based state measurement | Prometheus, Micrometer | CPU 80%, Response time 200ms |
| **Logs** | Detailed event records | Loki, Elasticsearch | "Order creation failed: Out of stock" |
| **Traces** | Track entire request path | Jaeger, Tempo | Order → Payment → Shipping service flow |

### When Should You Adopt Observability?

**Suitable cases:**
- Operating microservices architecture
- When identifying failure causes takes too long
- When SLA/SLO-based operations are needed
- When you want to systematically analyze performance bottlenecks

**May be overkill:**
- Single monolithic applications
- Internal tools with very low traffic
- Small teams with insufficient operational staff

## What This Guide Covers

### [Quick Start]({{< relref "/docs/observability/quick-start" >}})
Build a Prometheus + Grafana environment in 10 minutes and verify your first metrics.

### [Concepts]({{< relref "/docs/observability/concepts" >}})
Explains not just "how to use" but **"why it was designed this way"**.

| Topic | What You'll Learn |
|------|----------|
| [Three Pillars of Observability]({{< relref "/docs/observability/concepts/three-pillars" >}}) | Roles of Metrics, Logs, Traces and their interconnections |
| [Metrics Fundamentals]({{< relref "/docs/observability/concepts/metrics-fundamentals" >}}) | Understanding Counter, Gauge, Histogram, Summary types |
| [Prometheus Architecture]({{< relref "/docs/observability/concepts/prometheus-architecture" >}}) | Pull model, time series DB, service discovery |
| [PromQL]({{< relref "/docs/observability/concepts/promql" >}}) | Query language from basics to advanced (7 documents) |
| [SRE Golden Signals]({{< relref "/docs/observability/concepts/golden-signals" >}}) | Deep dive into Latency, Traffic, Errors, Saturation (6 documents) |
| [Log Aggregation]({{< relref "/docs/observability/concepts/log-aggregation" >}}) | Loki vs ELK comparison, log design patterns |
| [Distributed Tracing]({{< relref "/docs/observability/concepts/distributed-tracing" >}}) | Span, Trace ID, Context Propagation |
| [OpenTelemetry]({{< relref "/docs/observability/concepts/opentelemetry" >}}) | Observability standards and integration methods |
| [Dashboard Design]({{< relref "/docs/observability/concepts/dashboard-design" >}}) | Effective visualization principles |

### [Examples]({{< relref "/docs/observability/examples" >}})
Hands-on experience with executable code.

- [Environment Setup]({{< relref "/docs/observability/examples/setup" >}}) - Full stack configuration with Docker Compose
- [Spring Boot Metrics]({{< relref "/docs/observability/examples/spring-boot-metrics" >}}) - Actuator + Micrometer setup
- [Kafka Monitoring]({{< relref "/docs/observability/examples/kafka-monitoring" >}}) - Building Kafka cluster observability
- [Full-Stack Observability]({{< relref "/docs/observability/examples/full-stack" >}}) - Metrics + Logs + Traces integration

### [How-To Guides]({{< relref "/docs/observability/howto" >}})
Common problem scenarios and solutions in practice.

- [Debugging High Latency]({{< relref "/docs/observability/howto/debug-high-latency" >}}) - Tracking P99 latency causes
- [Metrics Cardinality Optimization]({{< relref "/docs/observability/howto/reduce-cardinality" >}}) - Cost reduction strategies
- [Managing Alert Fatigue]({{< relref "/docs/observability/howto/manage-alert-fatigue" >}}) - Reduce noise alerts

### [Appendix]({{< relref "/docs/observability/appendix" >}})
- [Glossary]({{< relref "/docs/observability/appendix/glossary" >}}) - Quick reference for Observability terms
- [FAQ]({{< relref "/docs/observability/appendix/faq" >}}) - Frequently asked questions
- [Alerting Actions Guide]({{< relref "/docs/observability/appendix/alerting-actions" >}}) - Response strategies after PromQL detection
- [References]({{< relref "/docs/observability/appendix/references" >}}) - Official documentation and additional learning resources

## Prerequisites

- **Required**: Basic Docker usage, HTTP/REST API understanding
- **Helpful**: Spring Boot experience, Kubernetes basics, time series data concepts

## Tech Stack

Tools used in this guide.

| Component | Tool | Version |
|---------|------|------|
| Metrics Collection | Prometheus | 2.50+ |
| Metrics Exposure | Micrometer | 1.12+ |
| Visualization | Grafana | 10.x |
| Log Collection | Loki | 2.9+ |
| Distributed Tracing | Tempo | 2.x |
| Standardization | OpenTelemetry | 1.x |

## Suggested Learning Path

```mermaid
graph TD
    START["Start"] --> Q1{"Experience Level?"}

    Q1 --> |"Beginner"| P1["Quick Start<br>→ Three Pillars<br>→ Metrics Fundamentals<br>→ Spring Boot Example"]
    Q1 --> |"Intermediate"| P2["PromQL Advanced<br>→ Golden Signals<br>→ Full-Stack Example"]
    Q1 --> |"Operations"| P3["Distributed Tracing<br>→ Alerting Actions<br>→ Cardinality Optimization"]

    P1 --> NEXT["Next Steps"]
    P2 --> NEXT
    P3 --> NEXT
```

**If you're new:**
```
Quick Start → Three Pillars → Metrics Fundamentals → Prometheus Architecture → Spring Boot Example
```

**If you want to learn PromQL deeply:**
```
PromQL Basics → Aggregation Operators → rate vs increase → histogram_quantile → Recording Rules → Alerting Rules
```

**From an SRE/Operations perspective:**
```
Golden Signals Overview → Latency/Errors/Saturation → Application by Service Type → Alerting Actions Guide
```

Each document can be read independently, but if you're new, we recommend following the order above.
