---
title: Troubleshooting OutOfMemoryError
weight: 1
lastmod: "2026-01-16"
author:
  name: Advanced Beginner
  github: advanced-beginner
doc_type: howto
description: "Step-by-step guide to diagnose and resolve OOM errors in Spark"
---

{{< callout type="info" >}}
**Estimated Time**: About 15 minutes
{{< /callout >}}

{{< callout type="tip" title="TL;DR" >}}
- **Driver OOM**: Reduce `collect()` result size, increase `spark.driver.memory`
- **Executor OOM**: Increase partition count (`repartition`), increase `spark.executor.memory`
- **Diagnose First**: Check Spark UI to identify where OOM occurs
{{< /callout >}}

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

| Item | Requirement | How to Verify |
|------|-------------|---------------|
| **Spark Version** | 2.4 or higher (3.x recommended) | `spark-submit --version` |
| **Java Version** | 8, 11, or 17 | `java -version` |
| **Spark UI** | Accessible | Open `http://localhost:4040` in browser |
| **Permissions** | Can modify Spark settings | Verify spark-submit execution permissions |

**Supported Environments**: Linux, macOS, Windows (WSL2 recommended)

### Environment Verification

Run the following commands to verify your environment is ready:

```bash
# Check Java version
java -version

# Check Spark version
spark-submit --version

# Verify Spark UI access (while application is running)
curl -s http://localhost:4040/api/v1/applications | head -1
```

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

Driver OOM typically occurs when **collecting large amounts of data to the Driver**. Follow these steps in order.

### 2.1 Review collect() Usage

First, check for `collect()` calls in your code.

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

{{< callout type="warning" >}}
**Warning**: Do not set Executor memory above 75% of the cluster node's physical memory. YARN/Kubernetes overhead may cause Container termination.
{{< /callout >}}

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

Verify OOM resolution using these criteria:

### Success Criteria

| Item | Success Condition |
|------|-------------------|
| **Job Completion** | All Stages in SUCCEEDED state |
| **Memory Usage** | Executor memory utilization below 80% |
| **GC Time** | Less than 10% of total execution time |
| **Error Logs** | No OOM-related messages |

### Verification Steps

1. **Check Spark UI**: Review memory usage in Executors tab.
   - Check the **Storage Memory** column for usage
   - There should be no red warnings

2. **Verify Job Completion**: Confirm all Stages are green (SUCCEEDED) in Jobs tab.

3. **Check Logs**: Run the following command to verify no OOM errors.

```bash
# Check logs for OOM (success if no output)
grep -i "outofmemory\|oom\|killed" spark-logs/*.log

# Expected result: No output
```

---

## Troubleshooting Checklist

### Solutions by Error Message

| Error Message | Cause | Solution |
|---------------|-------|----------|
| `java.lang.OutOfMemoryError: Java heap space` (Driver) | Collecting large data to Driver | `collect()` → `take(n)` or save to file |
| `java.lang.OutOfMemoryError: Java heap space` (Executor) | Partition size too large | Increase partitions with `repartition` |
| `Container killed by YARN for exceeding memory limits` | Insufficient YARN memory overhead | Increase `spark.executor.memoryOverhead` |
| `java.lang.OutOfMemoryError: GC overhead limit exceeded` | GC using 90%+ CPU | Increase memory or enable Off-Heap |
| `ExecutorLostFailure (executor X exited caused by one of the running tasks)` | Executor memory insufficient | Increase Executor memory, reduce partition size |
| `Total size of serialized results is bigger than spark.driver.maxResultSize` | Driver result size exceeded | Increase `spark.driver.maxResultSize` or reduce result size |

### Quick Diagnosis Table

| Symptom | Check | Solution |
|---------|-------|----------|
| Driver OOM | `collect()` calls | `take(n)` or save to file |
| Executor OOM | Size per partition | Increase partitions with `repartition` |
| GC Overhead | GC time > 10% | Increase memory or enable Off-Heap |
| YARN Container killed | Memory overhead | Increase `spark.executor.memoryOverhead` |

---

## Next Steps

- [Resolving Data Skew]({{< relref "/docs/spark/howto/data-skew" >}}) - Data concentration in specific partitions
- [FAQ]({{< relref "/docs/spark/appendix/faq" >}}) - Other error resolutions
