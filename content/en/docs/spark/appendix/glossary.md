---
title: Glossary
description: "Key Spark terminology and definitions"
weight: 1
lastmod: "2026-01-07"
---

# Glossary

Key terminology and concepts used in Spark. Each term links to related documentation.

## Core Concepts

### Action

Operations that trigger actual computation on RDD/DataFrame and return results. Examples include `count()`, `collect()`, `show()`, `write()`.
→ [Transformations and Actions](../concepts/transformations-actions/)

### Application

A Spark program submitted by the user. Consists of a [Driver](#driver) and [Executors](#executor).
→ [Architecture](../concepts/architecture/)

### Broadcast Variable

A shared variable distributed to all nodes as read-only. Used for efficiently sharing small datasets.
→ [Performance Tuning](../concepts/tuning/)

### Catalyst Optimizer

Spark SQL's query optimization engine. Transforms logical plans into optimized physical plans.
→ [Spark SQL](../concepts/spark-sql/)

### Checkpoint

A mechanism that saves RDD/DataFrame to reliable storage, breaking the [Lineage](#lineage) and enabling faster failure recovery.
→ [Caching and Persistence](../concepts/caching/)

### Cluster Manager

An external service that manages cluster resources. Options include Standalone, YARN, Kubernetes, and Mesos.
→ [Architecture](../concepts/architecture/), [Deployment and Cluster Management](../concepts/deployment/)

### Coalesce

An operation that reduces the number of partitions. Merges partitions without [Shuffle](#shuffle).
→ [Partitioning and Shuffle](../concepts/partitioning/)

### DAG (Directed Acyclic Graph)

A directed acyclic graph representing the dependency relationships of [Transformations](#transformation). Used by Spark to optimize execution plans.
→ [Architecture](../concepts/architecture/)

### DataFrame

A distributed data collection organized into named columns. In Java, it's represented as `Dataset<Row>`.
→ [DataFrame and Dataset](../concepts/dataframe-dataset/)

### Dataset

A distributed data collection with a specific type. Provides compile-time type safety.
→ [DataFrame and Dataset](../concepts/dataframe-dataset/)

### Driver

The process that runs the Spark application's main() function and creates the [SparkSession](#sparksession).
→ [Architecture](../concepts/architecture/)

### Executor

A JVM process running on Worker nodes. Executes [Tasks](#task) and stores data.
→ [Architecture](../concepts/architecture/)

### Job

A unit of parallel computation corresponding to a single [Action](#action). Consists of multiple [Stages](#stage).
→ [Architecture](../concepts/architecture/)

### Lazy Evaluation

A mechanism where [Transformations](#transformation) are not executed immediately but deferred until an [Action](#action) is called.
→ [Transformations and Actions](../concepts/transformations-actions/)

### Lineage

Information about how an [RDD](#rdd-resilient-distributed-dataset) was created through [Transformations](#transformation). Used for failure recovery.
→ [RDD Fundamentals](../concepts/rdd/)

### Narrow Transformation

A [Transformation](#transformation) where each input partition contributes to at most one output partition. No [Shuffle](#shuffle) occurs.
→ [Transformations and Actions](../concepts/transformations-actions/)

### Partition

A logical unit of data division in RDD/DataFrame. Each partition is processed on a single node in the cluster.
→ [Partitioning and Shuffle](../concepts/partitioning/)

### Persist

Storing RDD/DataFrame in memory or disk with a specified [Storage Level](#storage-level).
→ [Caching and Persistence](../concepts/caching/)

### RDD (Resilient Distributed Dataset)

Spark's fundamental data abstraction. An immutable, distributed, fault-tolerant data collection.
→ [RDD Fundamentals](../concepts/rdd/)

### Repartition

An operation that changes the number of partitions. Causes [Shuffle](#shuffle).
→ [Partitioning and Shuffle](../concepts/partitioning/)

### Serialization

The process of converting objects to byte streams. Required for network transfer or disk storage.
→ [Performance Tuning](../concepts/tuning/)

### Shuffle

Data redistribution across partitions. Occurs in [Wide Transformations](#wide-transformation) and significantly impacts performance.
→ [Partitioning and Shuffle](../concepts/partitioning/)

### SparkContext

An object representing the connection to a Spark cluster. Unified into [SparkSession](#sparksession) since Spark 2.0.
→ [Architecture](../concepts/architecture/)

### SparkSession

The unified entry point for Spark applications. Includes [SparkContext](#sparkcontext), SQLContext, and HiveContext.
→ [Quick Start](../quick-start/), [Architecture](../concepts/architecture/)

### Stage

A set of [Tasks](#task) separated by [Shuffle](#shuffle) boundaries. A single [Job](#job) consists of multiple Stages.
→ [Architecture](../concepts/architecture/)

### Storage Level

Specifies how data is stored when caching. Options include MEMORY_ONLY, MEMORY_AND_DISK, DISK_ONLY, etc.
→ [Caching and Persistence](../concepts/caching/)

### Task

The smallest unit of work executed on a single [Partition](#partition). Runs on [Executors](#executor).
→ [Architecture](../concepts/architecture/)

### Transformation

Operations that create new RDD/DataFrames from existing ones. Subject to [Lazy Evaluation](#lazy-evaluation).
→ [Transformations and Actions](../concepts/transformations-actions/)

### Tungsten

Spark's execution engine optimization project. Improves memory management, code generation, etc.
→ [Performance Tuning](../concepts/tuning/)

### Wide Transformation

A [Transformation](#transformation) where multiple input partitions contribute to a single output partition. Causes [Shuffle](#shuffle).
→ [Transformations and Actions](../concepts/transformations-actions/)

### Worker Node

A cluster node that runs [Executors](#executor).
→ [Architecture](../concepts/architecture/), [Deployment and Cluster Management](../concepts/deployment/)

## Streaming Terms

### Micro-Batch

The default mode of Structured Streaming that processes stream data in small batches.
→ [Structured Streaming](../concepts/structured-streaming/)

### Trigger

Configuration that determines when stream processing is executed.
→ [Structured Streaming](../concepts/structured-streaming/)

### Watermark

A late arrival tolerance setting for handling late-arriving data.
→ [Structured Streaming](../concepts/structured-streaming/)

### Window

Operations for time-based grouping. Includes Tumbling, Sliding, and Session Windows.
→ [Structured Streaming](../concepts/structured-streaming/)

## Machine Learning Terms

### Estimator

An algorithm that trains using the fit() method to produce a [Transformer](#transformer-ml).
→ [MLlib](../concepts/mllib/)

### Pipeline

A workflow connecting multiple [Estimators](#estimator) and [Transformers](#transformer-ml).
→ [MLlib](../concepts/mllib/)

### Transformer (ML)

A component that transforms data using the transform() method.
→ [MLlib](../concepts/mllib/)

## Configuration Related

### AQE (Adaptive Query Execution)

A feature that dynamically optimizes query plans at runtime. Available in Spark 3.0+.
→ [Performance Tuning](../concepts/tuning/)

### Broadcast Join

A join method that distributes small tables to all nodes to join without [Shuffle](#shuffle).
→ [Performance Tuning](../concepts/tuning/)

### CBO (Cost-Based Optimization)

An optimization technique that selects the optimal execution plan based on table statistics.
→ [Performance Tuning](../concepts/tuning/)

### Dynamic Allocation

A feature that automatically adjusts the number of [Executors](#executor) based on workload.
→ [Performance Tuning](../concepts/tuning/), [Deployment and Cluster Management](../concepts/deployment/)
