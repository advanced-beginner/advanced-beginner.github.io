---
title: Concepts
weight: 2
lastmod: "2026-01-07"
---

Understand Spark's core components and how they work.

## Learning Path

### Fundamentals

1. [Architecture](architecture/) - Roles and interactions of Driver, Executor, Cluster Manager
2. [RDD Basics](rdd/) - Spark's basic abstraction, distributed collection concepts
3. [DataFrame and Dataset](dataframe-dataset/) - Modern high-level APIs
4. [Spark SQL](spark-sql/) - Distributed data processing with SQL
5. [Transformations and Actions](transformations-actions/) - Core of lazy evaluation and execution

### Advanced Concepts

6. [Partitioning and Shuffle](partitioning/) - Core of distributed processing, data distribution strategies
7. [Caching and Persistence](caching/) - In-memory processing optimization
8. [Structured Streaming](structured-streaming/) - Real-time stream data processing
9. [MLlib](mllib/) - Distributed machine learning

### Operations

10. [Performance Tuning](tuning/) - Memory, partition, shuffle optimization
11. [Deployment and Cluster Management](deployment/) - Standalone, YARN, Kubernetes environments
12. [Spark Connect](spark-connect/) - Thin client architecture (Spark 3.4+)

## Core Concepts Summary

Here's a brief introduction to essential concepts for understanding Spark:

### Execution Model

| Concept | Description |
|---------|-------------|
| Driver | Runs the application's main(), orchestrates work |
| Executor | Worker process that performs actual data processing |
| Cluster Manager | Resource allocation (Standalone, YARN, K8s) |
| Job | Unit of work corresponding to one Action |
| Stage | Set of Tasks divided by shuffle boundaries |
| Task | Smallest unit of work executed on a single partition |

### Data Abstractions

| API | Type Safety | Optimization | When to Use |
|-----|-------------|--------------|-------------|
| RDD | Yes (generics) | Limited | When low-level control needed |
| DataFrame | No (Row) | Catalyst optimization | SQL-style processing |
| Dataset | Yes (case class) | Catalyst optimization | Type safety + optimization |

### Operation Types

| Type | Characteristics | Examples |
|------|-----------------|----------|
| Transformation | Lazy evaluation, returns new RDD/DataFrame | map, filter, groupBy |
| Action | Immediate execution, returns value | collect, count, show |

### Narrow vs Wide Transformation

| Type | Shuffle | Examples |
|------|---------|----------|
| Narrow | No (1:1 partition mapping) | map, filter, union |
| Wide | Yes (shuffle occurs) | groupBy, join, reduceByKey |

Each document covers how these concepts connect in detail.
