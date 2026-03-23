---
bookCollapseSection: true
title: Concepts
description: "Spark core concepts learning guide and document index"
weight: 2
lastmod: "2026-01-09"
author:
  name: Advanced Beginner
  github: advanced-beginner
---

Understand Spark's core components and how they work. This section covers how Spark operates internally and concepts you need to know for efficient distributed processing.

## When Java/Spring Developers Learn Spark

For Java/Spring developers, Spark is both familiar and a new paradigm. While it uses a functional style similar to Stream API, the nature of distributed environments requires different thinking in some areas.

**Familiar Aspects**
- Functional APIs like `filter()`, `map()`, `groupBy()`
- Using Java lambda expressions
- SQL query support

**New Concepts to Understand**
- **Lazy Evaluation**: Method calls don't execute immediately
- **Serialization Constraints**: Objects used in closures must be serializable
- **Shuffle Cost**: Data movement involves network I/O, making it expensive
- **Immutable Data**: RDDs/DataFrames cannot be modified, always return new objects

Once you understand these differences, Spark becomes a powerful tool for large-scale data processing. Each concept document explains in detail with Java code examples.

## Learning Path

Following this order will give you a systematic understanding from Spark basics to operations.

**Fundamentals**

First, understand Spark's core structure and APIs:

1. [Architecture](architecture/) - Roles and interactions of Driver, Executor, Cluster Manager
2. [RDD Basics](rdd/) - Spark's basic abstraction, distributed collection concepts
3. [DataFrame and Dataset](dataframe-dataset/) - Modern high-level APIs
4. [Spark SQL](spark-sql/) - Distributed data processing with SQL
5. [Transformations and Actions](transformations-actions/) - Core of lazy evaluation and execution

**Advanced Concepts**

After understanding the basics, learn performance optimization and advanced features:

6. [Partitioning and Shuffle](partitioning/) - Core of distributed processing, data distribution strategies
7. [Caching and Persistence](caching/) - In-memory processing optimization
8. [Structured Streaming](structured-streaming/) - Real-time stream data processing
9. [MLlib](mllib/) - Distributed machine learning

**Operations**

Knowledge for operating Spark in production environments:

10. [Performance Tuning](tuning/) - Memory, partition, shuffle optimization
11. [Deployment and Cluster Management](deployment/) - Standalone, YARN, Kubernetes environments
12. [Spark Connect](spark-connect/) - Thin client architecture (Spark 3.4+)

## Core Concepts Summary

Brief introduction to essential concepts for understanding Spark. Detailed content for each concept is covered in individual documents.

**Execution Model**

Core components for understanding how Spark applications execute:

| Concept | Description |
|---------|-------------|
| Driver | Runs the application's main(), orchestrates work |
| Executor | Worker process that performs actual data processing |
| Cluster Manager | Resource allocation (Standalone, YARN, K8s) |
| Job | Unit of work corresponding to one Action |
| Stage | Set of Tasks divided by shuffle boundaries |
| Task | Smallest unit of work executed on a single partition |

The Driver coordinates overall work, while actual data processing runs in parallel across multiple Executors.

**Data Abstractions**

Comparison of characteristics of Spark's three data APIs:

| API | Type Safety | Optimization | When to Use |
|-----|-------------|--------------|-------------|
| RDD | Yes (generics) | Limited | When low-level control needed |
| DataFrame | No (Row) | Catalyst optimization | SQL-style processing |
| Dataset | Yes (case class) | Catalyst optimization | Type safety + optimization |

In most cases, use DataFrame, and choose Dataset when compile-time type checking is needed.

**Operation Types**

Spark operations are broadly divided into Transformations and Actions:

| Type | Characteristics | Examples |
|------|-----------------|----------|
| Transformation | Lazy evaluation, returns new RDD/DataFrame | map, filter, groupBy |
| Action | Immediate execution, returns value | collect, count, show |

Transformations are not executed immediately when called, but processed with an optimized execution plan when an Action is called.

**Narrow vs Wide Transformation**

Transformations are divided into two types depending on whether shuffle occurs:

| Type | Shuffle | Examples |
|------|---------|----------|
| Narrow | No (1:1 partition mapping) | map, filter, union |
| Wide | Yes (shuffle occurs) | groupBy, join, reduceByKey |

Wide Transformations cause network I/O and significantly impact performance. Each document covers how these concepts connect in detail.
