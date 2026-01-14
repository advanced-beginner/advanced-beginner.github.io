---
title: Glossary
description: Core Observability terminology definitions
weight: 1
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

> **Quick Navigation**: [A](#a) | [C](#c) | [E](#e) | [F](#f) | [G](#g) | [H](#h) | [I](#i) | [L](#l) | [M](#m) | [O](#o) | [P](#p) | [R](#r) | [S](#s) | [T](#t) | [U](#u) | [W](#w)

---

## A

### Alerting Rules
Rules in Prometheus that define condition-based alerts. When conditions are met, alerts are sent to Alertmanager.

### Alertmanager
A component that receives alerts from Prometheus and groups, inhibits, and routes them.

## C

### Cardinality
The number of unique time series. Higher cardinality occurs with more label combinations.

### Context Propagation
A mechanism for passing Trace ID and Span ID between services in distributed systems.

### Counter
A monotonically increasing metric type. Used for request counts, error counts, etc. Calculate rate of change with `rate()` function.

## E

### Exemplar
A trace sample linked to a metric. Allows direct navigation from metrics to related traces.

### Exporter
A component that exposes application/system metrics in Prometheus format.

## F

### Four Golden Signals
Four core metrics proposed by Google SRE: Latency, Traffic, Errors, Saturation.

## G

### Gauge
A metric type representing current value. Can increase/decrease. Used for CPU usage, temperature, etc.

## H

### Histogram
A metric type that measures value distribution in buckets. Used for measuring response time distribution. Calculate percentiles with `histogram_quantile()`.

## I

### Instrumentation
Adding observability data collection code to applications. Can be automatic or manual instrumentation.

### irate()
A PromQL function that calculates instantaneous rate using only the last two samples.

## L

### Label
Key-value metadata attached to metrics. Used for filtering and grouping.

### LogQL
Grafana Loki's query language. Similar syntax to PromQL.

### Loki
Grafana's log collection system. Lightweight with label-based indexing.

## M

### Micrometer
A metrics facade for JVM applications. Supports various backends like Prometheus, Datadog.

## O

### OpenTelemetry (OTel)
A vendor-neutral observability standard framework for metrics, logs, and traces.

### OTLP
OpenTelemetry Protocol. A standard protocol for transmitting observability data.

## P

### Percentile
A value below which a certain percentage of data falls in the distribution. P99 = 99% of values are below this.

### PromQL
Prometheus Query Language. A language for querying and analyzing time series data.

### Pull Model
A method where Prometheus actively scrapes targets for metrics (opposite of Push).

## R

### rate()
A PromQL function that calculates the average per-second rate of increase for Counters.

### Recording Rules
Prometheus rules that pre-calculate complex queries and store them as new metrics.

### RED Method
A microservice monitoring methodology measuring Rate, Errors, Duration.

## S

### Sampling
A technique that stores only a portion of all traces. Used for cost optimization.

### Scrape
The act of Prometheus collecting metrics from targets.

### Service Level Indicator (SLI)
A metric that measures service level. Example: P99 response time, error rate.

### Service Level Objective (SLO)
Target values for SLIs. Example: P99 < 500ms, 99.9% availability.

### Span
A single unit of work in distributed tracing. A Trace consists of multiple Spans.

## T

### Tail-based Sampling
A sampling method that prioritizes storing errors/slow requests after request completion.

### Tempo
Grafana's distributed tracing backend. Optimized for large-scale trace storage.

### Three Pillars
The three pillars of Observability: Metrics, Logs, Traces.

### Trace
The complete path of a single request through a distributed system. Consists of multiple Spans.

### Trace ID
A unique ID identifying a trace. All Spans share the same Trace ID.

## U

### USE Method
A resource monitoring methodology measuring Utilization, Saturation, Errors.

## W

### W3C Trace Context
An HTTP header standard for distributed tracing. Uses `traceparent` header.
