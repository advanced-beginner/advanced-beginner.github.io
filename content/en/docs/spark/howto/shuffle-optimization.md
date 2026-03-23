---
title: Optimizing Shuffle
weight: 3
lastmod: "2026-01-16"
author:
  name: Advanced Beginner
  github: advanced-beginner
doc_type: howto
description: "Guide to minimize Spark shuffle and significantly improve job performance"
---

{{< callout type="info" >}}
**Estimated Time**: About 20 minutes
{{< /callout >}}

{{< callout type="tip" title="TL;DR" >}}
- **Check Shuffle**: `Exchange` node in `df.explain()` = shuffle occurs
- **Eliminate Unnecessary Shuffles**: Perform multiple aggregations at once in the same group
- **Broadcast Join**: Use `broadcast()` for small tables (tens of MB)
- **Shuffle Partition Count**: Adjust `spark.sql.shuffle.partitions` (default 200)
{{< /callout >}}

## Problem Definition

Shuffle optimization is needed when you see these symptoms:

| Symptom | Where to Check |
|---------|----------------|
| Stage transitions take 10+ seconds | Spark UI → Jobs tab |
| Shuffle Read/Write is tens of GB or more | Spark UI → Stages tab |
| Timeout due to network I/O | Application logs |
| Many `Exchange` nodes in `explain()` | Execution plan output |

Shuffle is the most expensive operation in Spark. The following operations cause shuffle:
- `groupBy`, `reduceByKey`
- `join`, `cogroup`
- `repartition`, `coalesce(shuffle=true)`
- `distinct`, `sortByKey`

---

## Prerequisites

| Item | Requirement | How to Verify |
|------|-------------|---------------|
| **Spark Version** | 2.4+ (AQE requires 3.0+) | `spark-submit --version` |
| **Spark UI** | Accessible | Open `http://localhost:4040` in browser |
| **Permissions** | Can modify Spark settings | Verify spark-submit execution permissions |

**Supported Environments**: Linux, macOS, Windows (WSL2 recommended)

### Environment Verification

Run the following commands to verify your environment is ready:

```bash
# Check Spark version
spark-submit --version

# Verify Spark UI access (while application is running)
curl -s http://localhost:4040/api/v1/applications | head -1
```

### Recommended Resolution Order

Follow this order for maximum optimization impact:

1. **Step 2**: Eliminate unnecessary shuffles (most effective)
2. **Step 3**: Broadcast join (for small table joins)
3. **Step 4**: Adjust shuffle partition count
4. **Step 5-6**: Advanced optimization (as needed)

---

## Step 1: Identify Shuffle Points

### 1.1 Check in Execution Plan

```java
Dataset<Row> result = df
    .groupBy("category")
    .agg(count("*"), sum("amount"))
    .orderBy(col("count").desc());

// Check execution plan
result.explain();
```

**Example Output:**
```
== Physical Plan ==
*(3) Sort [count DESC]
+- Exchange rangepartitioning    ← Shuffle!
   +- *(2) HashAggregate
      +- Exchange hashpartitioning   ← Shuffle!
         +- *(1) HashAggregate
            +- FileScan parquet
```

An `Exchange` node indicates shuffle is occurring.

### 1.2 Check in Spark UI

1. **Jobs** tab → Select specific Job
2. Check **DAG Visualization**
3. Stage boundaries (dotted lines) are shuffle points

### 1.3 Check Shuffle Metrics

```java
// Shuffle metrics logging
df.write()
    .mode("overwrite")
    .parquet("output");

// Spark UI → Stages → Completed Stage → Check Shuffle Read/Write
```

---

## Step 2: Eliminate Unnecessary Shuffles

### 2.1 Consolidate Aggregations

**Problem: Two Shuffles**
```java
// Two groupBys = two shuffles
Dataset<Row> counts = df.groupBy("category").count();
Dataset<Row> sums = df.groupBy("category").agg(sum("amount"));
Dataset<Row> result = counts.join(sums, "category");  // Another shuffle!
```

**Solution: One Shuffle**
```java
// Perform all aggregations in one groupBy
Dataset<Row> result = df.groupBy("category")
    .agg(
        count("*").alias("count"),
        sum("amount").alias("total_amount"),
        avg("amount").alias("avg_amount")
    );
```

### 2.2 Apply Filter First

**Problem: Filter After Join**
```java
Dataset<Row> joined = large.join(small, "key");
Dataset<Row> result = joined.filter(col("status").equalTo("ACTIVE"));
```

**Solution: Filter Before Join**
```java
// Reduce data volume first to decrease shuffle size
Dataset<Row> filteredLarge = large.filter(col("status").equalTo("ACTIVE"));
Dataset<Row> result = filteredLarge.join(small, "key");
```

### 2.3 Optimize Deduplication

**Problem: Full Distinct**
```java
Dataset<Row> unique = df.distinct();  // Distinct on all columns
```

**Solution: Only Necessary Columns**
```java
// Select only needed columns before distinct (reduces shuffle data)
Dataset<Row> unique = df.select("id", "category").distinct();
```

---

## Step 3: Leverage Broadcast Join

Completely eliminate shuffle when joining with small tables.

### 3.1 Explicit Broadcast

```java
import static org.apache.spark.sql.functions.broadcast;

// Replicate small table (tens of MB or less) to all Executors
Dataset<Row> result = largeTable.join(
    broadcast(smallTable),
    "key"
);
```

### 3.2 Adjust Auto-Broadcast Threshold

```java
SparkSession spark = SparkSession.builder()
    // Auto-broadcast tables 100MB or smaller
    .config("spark.sql.autoBroadcastJoinThreshold", "104857600")
    .getOrCreate();
```

### 3.3 Verify Broadcast Effect

```java
result.explain();
// If BroadcastHashJoin appears, join without shuffle
```

**Example Output:**
```
== Physical Plan ==
*(2) BroadcastHashJoin [key], [key], Inner, BuildRight
:- *(2) FileScan parquet [key, col1]
+- BroadcastExchange    ← Broadcast, not shuffle!
   +- *(1) FileScan parquet [key, col2]
```

---

## Step 4: Optimize Shuffle Partition Count

### 4.1 Adjust Default Settings

```java
SparkSession spark = SparkSession.builder()
    // Default 200, adjust based on data size
    .config("spark.sql.shuffle.partitions", "100")
    .getOrCreate();
```

**Recommended Partition Count:**
```
Partition count = max(cores × 2, data size (MB) / 200)
```

| Data Size | Recommended Partitions |
|-----------|----------------------|
| 1GB or less | 50-100 |
| 10GB | 100-200 |
| 100GB | 500-1000 |
| 1TB | 2000-5000 |

### 4.2 AQE Dynamic Partition Coalescing (Spark 3.0+)

```java
SparkSession spark = SparkSession.builder()
    .config("spark.sql.adaptive.enabled", "true")
    .config("spark.sql.adaptive.coalescePartitions.enabled", "true")
    .config("spark.sql.adaptive.coalescePartitions.minPartitionSize", "64MB")
    .getOrCreate();
```

AQE automatically merges small partitions to reduce overhead.

---

## Step 5: Pre-Partitioning with Bucketing

Eliminate shuffle for repeatedly joined tables using bucketing.

{{< callout type="warning" >}}
**Warning**: Bucketing requires a Hive metastore and rewrites the table. Overhead may be high for one-time joins. Use only when joining repeatedly on the same key.
{{< /callout >}}

### 5.1 Create Bucketed Tables

```java
// Bucket by join key
df.write()
    .bucketBy(100, "user_id")  // 100 buckets
    .sortBy("user_id")
    .saveAsTable("users_bucketed");

transactions.write()
    .bucketBy(100, "user_id")  // Same bucket count
    .sortBy("user_id")
    .saveAsTable("transactions_bucketed");
```

### 5.2 Shuffle-Free Join

```java
Dataset<Row> users = spark.table("users_bucketed");
Dataset<Row> transactions = spark.table("transactions_bucketed");

// Same bucket count + same key = join without shuffle
Dataset<Row> result = users.join(transactions, "user_id");
```

---

## Step 6: Optimize Shuffle Storage

### 6.1 Enable Shuffle Compression

```java
SparkSession spark = SparkSession.builder()
    .config("spark.shuffle.compress", "true")  // Default true
    .config("spark.shuffle.spill.compress", "true")  // Default true
    .getOrCreate();
```

### 6.2 Optimize Shuffle Directory

```java
// Store shuffle files on fast SSD
.config("spark.local.dir", "/ssd/spark-local")

// Distribute across multiple disks
.config("spark.local.dir", "/disk1/spark,/disk2/spark,/disk3/spark")
```

---

## Verification

Verify shuffle optimization using these criteria:

### Success Criteria

| Item | Success Condition |
|------|-------------------|
| **Shuffle Write** | 50%+ reduction compared to before |
| **Stage Count** | Fewer Exchange nodes in `explain()` |
| **Execution Time** | 30%+ reduction compared to before |
| **Broadcast Join** | `BroadcastHashJoin` in `explain()` |

### Check in Spark UI

1. Check Shuffle Read/Write sizes in **Stages** tab.
2. Verify shuffle data volume decreased.
3. Check for reduced Stage count (when shuffle is eliminated).

### Compare Execution Time

```java
// Compare execution time before and after optimization
long start = System.currentTimeMillis();
result.count();
long duration = System.currentTimeMillis() - start;
System.out.println("Execution time: " + duration + "ms");
```

---

## Troubleshooting Checklist

### Solutions by Error Message

| Error Message/Symptom | Cause | Solution |
|-----------------------|-------|----------|
| `FetchFailedException: Failed to connect` | Shuffle data transfer failed | Enable shuffle compression, increase network timeout |
| `java.io.IOException: No space left on device` | Shuffle disk space insufficient | Free up `spark.local.dir` or change path |
| `TimeoutException` (during shuffle) | Network bottleneck | Increase shuffle partitions, use broadcast |
| Shuffle Write > 100GB in Spark UI | Excessive shuffle | Eliminate unnecessary shuffle, apply filter first |
| Stage transitions take 10+ seconds | Shuffle overhead | Apply broadcast join or bucketing |

### Recommended Solutions by Situation

| Situation | Recommended Solution |
|-----------|---------------------|
| Joining with small table | Use `broadcast()` |
| Multiple aggregations | Perform all `agg` in one `groupBy` |
| Repeated joins | Apply bucketing |
| Too many shuffle partitions | Decrease `shuffle.partitions` or enable AQE |
| Too few shuffle partitions | Increase `shuffle.partitions` |

---

## Next Steps

- [Resolving Data Skew]({{< relref "/docs/spark/howto/data-skew" >}}) - Fix partition imbalance
- [Performance Tuning]({{< relref "/docs/spark/concepts/tuning" >}}) - Overall performance optimization
