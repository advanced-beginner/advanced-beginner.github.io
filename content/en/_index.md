---
lastmod: "2026-01-07"
title: Advanced Beginner
description: Beyond basics to real-world practice - A technical guide that starts from core principles
---

## What is this site?

You've followed tutorials and built "Hello World", but when facing complex real-world situations, you're not sure **why things should be done a certain way** — this guide bridges that gap.

Rather than just "do it this way", we explain **why such design is necessary** from first principles. When you understand the principles, you can make the right decisions even in situations not covered by documentation.

## Who is this guide for?

| This guide is for you if... | This guide is NOT for you if... |
|---------------------|-------------------------|
| You know the basics but struggle with real-world application | You're a complete beginner (start with basic tutorials first) |
| You want to understand the "why" | You only want copy-paste solutions |
| You're a Java/Scala developer with Spring Boot experience | - |
| You want to understand design principles and trade-offs | You only need a quick reference |

## Available Guides

| Technology | Docs | How-to | Description |
|------------|:----:|:------:|-------------|
| [Apache Kafka]({{< relref "/docs/kafka" >}}) | 22 | 4 | Distributed messaging system |
| [Domain-Driven Design]({{< relref "/docs/ddd" >}}) | 21 | 4 | Complex business logic design |
| [Scala]({{< relref "/docs/scala" >}}) | 28 | 2 | Functional + OOP JVM language |
| [Apache Spark]({{< relref "/docs/spark" >}}) | 24 | 3 | Large-scale data processing |
| [Kubernetes]({{< relref "/docs/kubernetes" >}}) | 18 | 4 | Container orchestration |
| [Elasticsearch]({{< relref "/docs/elasticsearch" >}}) | 20 | - | Distributed search engine |
| [Observability]({{< relref "/docs/observability" >}}) | 28 | 3 | System observability (Metrics, Logs, Traces) |

---

### [Apache Kafka]({{< relref "/docs/kafka" >}})

Practical usage of distributed messaging systems. From Producer/Consumer basics to transactions, replication, failure handling, and performance tuning.

**What you'll learn:**
- Core components of Kafka and message flow
- Consumer Group and Offset management strategies
- Achieving exactly-once delivery with transactions
- Real-world error handling patterns and monitoring

### [Domain-Driven Design]({{< relref "/docs/ddd" >}})

A methodology for tackling complex business logic systematically. From strategic design to tactical patterns, CQRS, and event sourcing.

**What you'll learn:**
- Dividing system boundaries with Bounded Contexts
- Designing consistent domain models with Aggregates
- Implementing loose coupling with Domain Events
- Practical order domain implementation examples

### [Scala]({{< relref "/docs/scala" >}})

A JVM language combining functional and object-oriented programming. From basic syntax to advanced type systems and functional patterns. Covers both Scala 2.13 and Scala 3 syntax.

**What you'll learn:**
- Scala basics and functional programming fundamentals
- Pattern Matching and Case Classes
- Type system: Generics, Variance, Type Classes
- Implicits/Given and principles of implicit conversions

### [Apache Spark]({{< relref "/docs/spark" >}})

A distributed computing engine for large-scale data processing. Explains in-memory processing, lazy evaluation, and DAG optimization from a Java/Spring developer's perspective.

**What you'll learn:**
- Spark architecture and execution model (Driver, Executor, Stage)
- Differences between RDD, DataFrame, and Dataset
- Real-time data processing with Structured Streaming
- Memory tuning and performance optimization strategies

### [Kubernetes]({{< relref "/docs/kubernetes" >}})

A practical guide to container orchestration platform. From Pod, Deployment, Service to scaling, networking, and monitoring.

**What you'll learn:**
- Kubernetes architecture and core resources (Pod, Deployment, Service)
- Configuration management with ConfigMap/Secret
- Auto-scaling with HPA
- Health checks and zero-downtime deployment strategies

### [Elasticsearch]({{< relref "/docs/elasticsearch" >}})

A distributed search engine for large-scale search and real-time analytics. Covers full-text search beyond RDB limitations and practical operational know-how.

**What you'll learn:**
- Inverted Index and Lucene internals
- Query DSL and Aggregation analysis
- Korean search optimization and autocomplete implementation
- Cluster operation, performance tuning, and failure response

### [Observability]({{< relref "/docs/observability" >}})

A practical guide to system observability. From the three pillars (Metrics, Logs, Traces) to advanced Prometheus, Grafana, and PromQL.

**What you'll learn:**
- Roles and interconnections of the three observability pillars (Metrics, Logs, Traces)
- Prometheus architecture and PromQL query language
- SRE Golden Signals (Latency, Traffic, Errors, Saturation)
- Distributed tracing and OpenTelemetry integration

## Characteristics of this guide

**First Principles** — We start from the fundamental problems the technology solves, not just surface-level usage. Understanding "why" before "how" builds adaptability.

**Executable Examples** — All code actually runs. We provide examples you can verify immediately after concept explanations.

**Real-world Perspective** — Theory and production reality often differ. We cover trade-offs and practical choices alongside the ideal approach.

## Where should I start?

- **New to Kafka** → [Kafka Quick Start]({{< relref "/docs/kafka/quick-start" >}})
- **Know Kafka basics but want deeper understanding** → [Kafka Core Components]({{< relref "/docs/kafka/concepts/core-components" >}})
- **Curious about what DDD is** → [DDD Quick Start]({{< relref "/docs/ddd/quick-start" >}})
- **Want to learn domain model design** → [Tactical Design]({{< relref "/docs/ddd/concepts/tactical-design" >}})
- **New to Scala** → [Scala Quick Start]({{< relref "/docs/scala/quick-start" >}})
- **Want deep understanding of Scala's type system** → [Type Classes]({{< relref "/docs/scala/concepts/type-classes" >}})
- **Want to process large-scale data with Spark** → [Spark Quick Start]({{< relref "/docs/spark/quick-start" >}})
- **Want to understand Spark internals** → [Spark Architecture]({{< relref "/docs/spark/concepts/architecture" >}})
- **New to Kubernetes** → [Kubernetes Quick Start]({{< relref "/docs/kubernetes/quick-start" >}})
- **Want to optimize Kubernetes resource settings** → [Resource Management]({{< relref "/docs/kubernetes/concepts/resources" >}})
- **Want to implement a search engine** → [Elasticsearch Quick Start]({{< relref "/docs/elasticsearch/quick-start" >}})
- **Want to optimize Korean search** → [Korean Search Optimization]({{< relref "/docs/elasticsearch/concepts/korean-search" >}})
- **Want to start system monitoring** → [Observability Quick Start]({{< relref "/docs/observability/quick-start" >}})
- **Want to learn PromQL in depth** → [Advanced PromQL]({{< relref "/docs/observability/concepts/promql" >}})
