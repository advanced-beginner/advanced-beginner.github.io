---
title: Troubleshooting OutOfMemoryError
weight: 1
lastmod: "2026-01-10"
author:
  name: Advanced Beginner
  github: advanced-beginner
doc_type: howto
---

{{% notice style="tip" title="TL;DR" %}}
- **Driver OOM**: Reduce `collect()` result size, increase `spark.driver.memory`
- **Executor OOM**: Increase partition count (`repartition`), increase `spark.executor.memory`
- **Diagnose First**: Check Spark UI to identify where OOM occurs
{{% /notice %}}

## Problem Definition

The following error occurs during Spark application execution:

```
java.lang.OutOfMemoryError: Java heap space
```

Or:

```
Container killed by YARN for exceeding memory limits
```

This guide explains step-by-step how to diagnose and resolve OOM errors.

---

## Prerequisites

| Item | Description |
|------|-------------|
| **Environment** | Spark application execution environment (local or cluster) |
| **Tools** | Access to Spark UI (`http://localhost:4040` or History Server) |
| **Permissions** | Authority to change Spark settings |

---

## Step 1: Identify OOM Location

OOM occurs for different reasons on **Driver** and **Executor**. First, identify where it happened.

### Checking for Driver OOM

If the error message includes the following, it's a Driver OOM:

```
Exception in thread "main" java.lang.OutOfMemoryError
```

Or if it occurred during `collect()`, `toPandas()`, `show()` calls, it's likely a Driver OOM.

### Checking for Executor OOM

If the error message includes the following, it's an Executor OOM:

```
ExecutorLostFailure (executor X exited caused by one of the running tasks)
Lost task X.X in stage X.X: ExecutorLostFailure
Container killed by YARN for exceeding memory limits
```

---

## Step 2: Resolving Driver OOM

Driver OOM typically occurs when **collecting large amounts of data to the Driver**.

### 2.1 Review collect() Usage

**Problem Code:**
```java
// Collecting millions of records to Driver - OOM!
List<Row> allData = df.collect();
```

**Solution:**

```java
// 1. Collect only a limited number with take()
List<Row> sample = df.take(1000);

// 2. Save results to file
df.write().parquet("output/result");

// 3. Collect after aggregation (reduces data volume)
Dataset<Row> summary = df.groupBy("category")
    .agg(count("*"), sum("amount"));
List<Row> result = summary.collect();  // Collect only aggregated results
```

### 2.2 Increase Driver Memory

If the data being collected is legitimately large, increase Driver memory:

```bash
# Using spark-submit
spark-submit --driver-memory 8g myapp.jar

# Setting in code
SparkSession spark = SparkSession.builder()
    .config("spark.driver.memory", "8g")
    .getOrCreate();
```

### 2.3 Check maxResultSize

Check the limit on result size returned to Driver:

```java
// Default is 1g, increase if needed
.config("spark.driver.maxResultSize", "4g")
```

---

## Step 3: Resolving Executor OOM

Executor OOM typically occurs when **partitions are too large** or **memory is insufficient**.

### 3.1 Check Partition Size

```java
// Check current partition count
int numPartitions = df.rdd().getNumPartitions();
System.out.println("Partition count: " + numPartitions);

// Check data distribution per partition
df.groupBy(spark_partition_id())
    .count()
    .orderBy(col("count").desc())
    .show(20);
```

### 3.2 Increase Partition Count

Adjust partition count so each partition is 100-200MB:

```java
// Increase partition count (triggers shuffle)
Dataset<Row> repartitioned = df.repartition(200);

// Or reduce with coalesce (no shuffle, cannot increase)
Dataset<Row> coalesced = df.coalesce(100);
```

**Recommended Partition Count Calculation:**
```
Partition count = Data size (MB) / 200
```

Example: 40GB data → 40,000 / 200 = 200 partitions

### 3.3 Increase Executor Memory

```bash
# spark-submit
spark-submit \
  --executor-memory 8g \
  --executor-cores 4 \
  myapp.jar
```

**5GB Memory Per Core Rule:**
```
executor-memory = executor-cores × 5GB
```

Example: 4 cores → 20GB memory recommended

### 3.4 Enable Off-Heap Memory

To reduce GC pressure for large caches or shuffles:

```java
SparkSession spark = SparkSession.builder()
    .config("spark.memory.offHeap.enabled", "true")
    .config("spark.memory.offHeap.size", "4g")
    .getOrCreate();
```

---

## Step 4: Resolving Special Cases

### 4.1 OOM from Broadcast Variables

Broadcasting large tables copies them to all Executors, causing OOM:

```java
// Problem: Broadcasting a 1GB table
df.join(broadcast(largeTable), "key");  // OOM!

// Solution: Check broadcast threshold
// Default 10MB, auto-broadcast disabled if exceeded
.config("spark.sql.autoBroadcastJoinThreshold", "10485760")
```

### 4.2 Large Object Creation in UDFs

```java
// Problem: Creating new object for each row
df.withColumn("result", udf(row -> {
    List<String> huge = loadHugeList();  // Created every time!
    return process(row, huge);
}));

// Solution: Create once in foreachPartition
df.foreachPartition(partition -> {
    List<String> huge = loadHugeList();  // Once per partition
    while (partition.hasNext()) {
        process(partition.next(), huge);
    }
});
```

### 4.3 Window Function OOM

OOM occurs with large window frames:

```java
// Problem: Window over entire partition
WindowSpec unbounded = Window.partitionBy("user_id")
    .orderBy("timestamp")
    .rowsBetween(Window.unboundedPreceding, Window.unboundedFollowing);

// Solution: Limit window frame
WindowSpec bounded = Window.partitionBy("user_id")
    .orderBy("timestamp")
    .rowsBetween(-100, 100);  // Limit to 100 rows before and after
```

---

## Verification

Confirm that OOM is resolved:

1. **Check Spark UI**: Review memory usage in Executors tab
2. **Verify Job Completion**: All Stages completed successfully
3. **Check Logs**: No OOM-related error messages

```bash
# Check logs for OOM
grep -i "outofmemory\|oom\|killed" spark-logs/*.log
```

---

## Troubleshooting Checklist

| Symptom | Check | Solution |
|---------|-------|----------|
| Driver OOM | `collect()` calls | `take(n)` or save to file |
| Executor OOM | Size per partition | Increase partitions with `repartition` |
| GC Overhead | GC time > 10% | Increase memory or enable Off-Heap |
| YARN Container killed | Memory overhead | Increase `spark.executor.memoryOverhead` |

---

## Next Steps

- [Resolving Data Skew](data-skew/) - Data concentration in specific partitions
- [FAQ](../appendix/faq/) - Other error resolutions
