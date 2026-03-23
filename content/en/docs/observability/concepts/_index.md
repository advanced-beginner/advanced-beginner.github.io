---
title: Concepts
description: Understand the core concepts of Observability from their principles
weight: 2
bookCollapseSection: true
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

Not just "how to use it" but explaining **"why it was designed this way"**.

## Learning Path

### Foundational Concepts

If you're new to Observability, follow this order.

1. [Three Pillars of Observability]({{< relref "/docs/observability/concepts/three-pillars" >}}) - Roles of Metrics, Logs, Traces and their interconnections
2. [Metrics Fundamentals]({{< relref "/docs/observability/concepts/metrics-fundamentals" >}}) - Understanding Counter, Gauge, Histogram, Summary types
3. [Prometheus Architecture]({{< relref "/docs/observability/concepts/prometheus-architecture" >}}) - Pull model, time series DB, service discovery

### PromQL Deep Dive

A deep exploration of the Prometheus query language.

4. [PromQL Overview]({{< relref "/docs/observability/concepts/promql" >}}) - PromQL learning roadmap
5. [Syntax Basics]({{< relref "/docs/observability/concepts/promql/syntax-basics" >}}) - Selectors, label matching, time ranges
6. [Aggregation Operators]({{< relref "/docs/observability/concepts/promql/aggregation-operators" >}}) - sum, avg, count, topk, by/without
7. [rate and increase]({{< relref "/docs/observability/concepts/promql/rate-and-increase" >}}) - Core of Counter metric processing
8. [histogram_quantile]({{< relref "/docs/observability/concepts/promql/histogram-quantile" >}}) - Calculating percentiles (P50/P95/P99)
9. [Recording Rules]({{< relref "/docs/observability/concepts/promql/recording-rules" >}}) - Pre-computing complex queries
10. [Alerting Rules]({{< relref "/docs/observability/concepts/promql/alerting-rules" >}}) - Writing alerting rules

### SRE Golden Signals

Applying the 4 core indicators proposed by Google SRE by service type.

11. [Golden Signals Overview]({{< relref "/docs/observability/concepts/golden-signals" >}}) - Introduction to 4 signals and USE/RED methods
12. [Latency]({{< relref "/docs/observability/concepts/golden-signals/latency" >}}) - Latency measurement strategies
13. [Traffic]({{< relref "/docs/observability/concepts/golden-signals/traffic" >}}) - Traffic/throughput monitoring
14. [Errors]({{< relref "/docs/observability/concepts/golden-signals/errors" >}}) - Error rate definition and classification
15. [Saturation]({{< relref "/docs/observability/concepts/golden-signals/saturation" >}}) - Saturation (resource utilization)
16. [Application by Service Type]({{< relref "/docs/observability/concepts/golden-signals/by-service-type" >}}) - Guide for Web API, Kafka, DB

### Logging and Tracing

Integrating logs and distributed tracing beyond metrics.

17. [Log Aggregation]({{< relref "/docs/observability/concepts/log-aggregation" >}}) - Loki vs ELK comparison, log design patterns
18. [Distributed Tracing]({{< relref "/docs/observability/concepts/distributed-tracing" >}}) - Span, Trace ID, Context Propagation
19. [OpenTelemetry]({{< relref "/docs/observability/concepts/opentelemetry" >}}) - Observability standards and integration methods

### Operations

Practical knowledge for effective operations.

20. [Dashboard Design]({{< relref "/docs/observability/concepts/dashboard-design" >}}) - Effective visualization principles

## Document Structure Pattern

Each concept document follows this structure:

```
1. TL;DR - Key summary (within 5 lines)
2. Why is it needed? - Problem situation and solution
3. Core Concepts - Detailed explanation + diagrams
4. Practical Examples - Code ready to apply
5. Trade-offs - Pros/cons and selection criteria
6. Next Steps - Related document links
```

## Recommended Learning Path

```mermaid
graph TD
    subgraph "Beginner (1-2 hours)"
        A["Three Pillars"] --> B["Metrics Fundamentals"]
        B --> C["Prometheus Architecture"]
    end

    subgraph "PromQL Deep Dive (2-3 hours)"
        D["Syntax Basics"] --> E["Aggregation Operators"]
        E --> F["rate/increase"]
        F --> G["histogram_quantile"]
        G --> H["Recording Rules"]
        H --> I["Alerting Rules"]
    end

    subgraph "SRE Perspective (1-2 hours)"
        J["Golden Signals Overview"] --> K["4 Signals Deep Dive"]
        K --> L["Application by Service Type"]
    end

    C --> D
    C --> J
    I --> L
```
