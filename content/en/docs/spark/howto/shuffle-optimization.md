---
title: Optimizing Shuffle
weight: 3
lastmod: "2026-01-10"
author:
  name: Advanced Beginner
  github: advanced-beginner
doc_type: howto
---

{{% notice style="tip" title="TL;DR" %}}
- **Check Shuffle**: `Exchange` node in `df.explain()` = shuffle occurs
- **Eliminate Unnecessary Shuffles**: Perform multiple aggregations at once in the same group
- **Broadcast Join**: Use `broadcast()` for small tables (tens of MB)
- **Shuffle Partition Count**: Adjust `spark.sql.shuffle.partitions` (default 200)
{{% /notice %}}

## Problem Definition

In Spark jobs, **Stage transitions take too long** or excessive network I/O occurs. Shuffle is the most expensive operation in Spark, so it should be minimized.

Operations that cause shuffle:
- `groupBy`, `reduceByKey`
- `join`, `cogroup`
- `repartition`, `coalesce(shuffle=true)`
- `distinct`, `sortByKey`

---

## Prerequisites

| Item | Description |
|------|-------------|
| **Environment** | Spark application execution environment |
| **Tools** | Access to Spark UI, `explain()` method available |

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

Confirm that shuffle is optimized:

### Check in Spark UI

1. **Stages** tab → Check Shuffle Read/Write sizes
2. Verify shuffle data volume decreased compared to before
3. Check for reduced Stage count (when shuffle is eliminated)

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

| Situation | Recommended Solution |
|-----------|---------------------|
| Joining with small table | Use `broadcast()` |
| Multiple aggregations | Perform all `agg` in one `groupBy` |
| Repeated joins | Apply bucketing |
| Too many shuffle partitions | Decrease `shuffle.partitions` or enable AQE |
| Too few shuffle partitions | Increase `shuffle.partitions` |

---

## Next Steps

- [Resolving Data Skew](../data-skew/) - Fix partition imbalance
- [Performance Tuning](../../concepts/tuning/) - Overall performance optimization
