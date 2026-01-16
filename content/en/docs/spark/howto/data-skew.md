---
title: Resolving Data Skew
weight: 2
lastmod: "2026-01-16"
author:
  name: Advanced Beginner
  github: advanced-beginner
doc_type: howto
description: "Guide to diagnose and resolve data skew issues where data concentrates in specific partitions"
---

{{< callout type="info" >}}
**Estimated Time**: About 20 minutes
{{< /callout >}}

{{% notice style="tip" title="TL;DR" %}}
- **Diagnosis**: Compare Task Duration Min/Max in Spark UI Stages tab (10x+ difference = skew)
- **Enable AQE**: `spark.sql.adaptive.skewJoin.enabled=true` (Spark 3.0+)
- **Manual Fix**: Distribute hot keys using Salting technique
{{% /notice %}}

## Problem Definition

A Spark job mostly completes quickly but **some Tasks take much longer**:

```
Stage 3: 199/200 tasks completed... (last 1 running for tens of minutes)
```

This is **Data Skew** - data is concentrated on specific keys, causing those partitions to be overloaded.

---

## Prerequisites

| Item | Requirement | How to Verify |
|------|-------------|---------------|
| **Spark Version** | 2.4+ (AQE requires 3.0+) | `spark-submit --version` |
| **Spark UI** | Accessible | Open `http://localhost:4040` in browser |
| **Data** | Key column used for joins/grouping exists | Verify key column in schema |

**Supported Environments**: Linux, macOS, Windows (WSL2 recommended)

### Environment Verification

Run the following commands to verify your environment is ready:

```bash
# Check Spark version (3.0+ required for AQE)
spark-submit --version

# Verify Spark UI access (while application is running)
curl -s http://localhost:4040/api/v1/applications | head -1
```

---

## Step 1: Diagnosing Skew

### 1.1 Check in Spark UI

1. Click **Stages** tab in Spark UI
2. Select the slow Stage
3. Check **Summary Metrics**:

| Metric | Normal | Skew Suspected |
|--------|--------|----------------|
| Duration (Min / Max) | Similar | **10x+ difference** |
| Shuffle Read Size (Min / Max) | Even | Only some are large |
| Records Read (Min / Max) | Even | Only some are high |

### 1.2 Check with Code

```java
import static org.apache.spark.sql.functions.*;

// Check data distribution per partition
df.groupBy(spark_partition_id().alias("partition_id"))
    .count()
    .orderBy(col("count").desc())
    .show(20);

// Example result (with skew):
// +------------+--------+
// |partition_id|   count|
// +------------+--------+
// |          15| 5000000|  ← Abnormally large!
// |           3|   50000|
// |           7|   48000|
// |           1|   45000|
// ...
```

### 1.3 Identify Hot Keys

```java
// Find which keys are hot keys
df.groupBy("join_key")
    .count()
    .orderBy(col("count").desc())
    .show(20);

// Example result:
// +--------+--------+
// |join_key|   count|
// +--------+--------+
// |   null | 3000000|  ← null is the hot key!
// | user_1 |  500000|
// | user_2 |   10000|
// ...
```

---

## Step 2: Enable AQE Skew Join (Spark 3.0+)

The simplest solution. Spark automatically detects and handles skew.

```java
SparkSession spark = SparkSession.builder()
    .appName("Skew Fix")
    .config("spark.sql.adaptive.enabled", "true")
    .config("spark.sql.adaptive.skewJoin.enabled", "true")
    .config("spark.sql.adaptive.skewJoin.skewedPartitionFactor", "5")
    .config("spark.sql.adaptive.skewJoin.skewedPartitionThresholdInBytes", "256MB")
    .getOrCreate();

// Perform normal join - AQE handles skew automatically
Dataset<Row> result = largeTable.join(smallTable, "key");
```

**Configuration Explanation:**

| Setting | Default | Description |
|---------|---------|-------------|
| `skewedPartitionFactor` | 5 | Considered skewed if 5x larger than median |
| `skewedPartitionThresholdInBytes` | 256MB | Must be at least 256MB to be considered skewed |

---

## Step 3: Apply Salting Technique

Use when AQE is not available or more fine-grained control is needed.

{{< callout type="warning" >}}
**Warning**: Salting replicates the small table, increasing memory usage. Setting `numSaltBuckets` too high may degrade performance. Start with values between 10-20.
{{< /callout >}}

### 3.1 Basic Salting

Add random suffixes to hot keys to distribute across multiple partitions.

```java
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import static org.apache.spark.sql.functions.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

int numSaltBuckets = 10;  // Distribute hot keys across 10 partitions

// 1. Add salt to large table
Dataset<Row> saltedLarge = largeTable.withColumn("salt",
    expr("FLOOR(RAND() * " + numSaltBuckets + ")"))
    .withColumn("salted_key",
        concat(col("join_key"), lit("_"), col("salt")));

// 2. Replicate small table by salt count
List<Row> saltValues = IntStream.range(0, numSaltBuckets)
    .mapToObj(i -> RowFactory.create(i))
    .collect(Collectors.toList());

Dataset<Row> saltDf = spark.createDataFrame(saltValues,
    new StructType().add("salt", DataTypes.IntegerType));

Dataset<Row> replicatedSmall = smallTable.crossJoin(saltDf)
    .withColumn("salted_key",
        concat(col("join_key"), lit("_"), col("salt")));

// 3. Join on salted_key
Dataset<Row> result = saltedLarge.join(replicatedSmall, "salted_key")
    .drop("salt", "salted_key");
```

### 3.2 Selective Salting (Hot Keys Only)

Salt only hot keys to minimize overhead:

```java
// Hot key list (identified beforehand)
List<String> hotKeys = Arrays.asList("hot_user_1", "hot_user_2");

Dataset<Row> saltedLarge = largeTable.withColumn("salt",
    when(col("join_key").isin(hotKeys.toArray()),
        expr("FLOOR(RAND() * 10)"))
    .otherwise(lit(0)))
    .withColumn("salted_key",
        concat(col("join_key"), lit("_"), col("salt")));
```

---

## Step 4: Leverage Broadcast Join

Broadcast is most effective when joining with small tables (tens of MB or less).

{{< callout type="warning" >}}
**Warning**: If broadcast table size exceeds 20% of Executor memory, OOM may occur. Do not broadcast tables over 100MB.
{{< /callout >}}

```java
import static org.apache.spark.sql.functions.broadcast;

// Replicate small table to all Executors
Dataset<Row> result = largeTable.join(
    broadcast(smallTable),  // Join without shuffle
    "key"
);
```

**Adjust Broadcast Threshold:**

```java
// Default 10MB, increase if needed
.config("spark.sql.autoBroadcastJoinThreshold", "104857600")  // 100MB
```

---

## Step 5: Handle NULL Values

NULL values are often the hot keys.

```java
// 1. Separate NULL handling
Dataset<Row> nonNull = df.filter(col("key").isNotNull());
Dataset<Row> nullRows = df.filter(col("key").isNull());

// 2. Join only non-null
Dataset<Row> joined = nonNull.join(other, "key");

// 3. Process null rows separately and union
Dataset<Row> result = joined.union(
    nullRows.withColumn("other_col", lit(null))
);
```

---

## Verification

Verify skew resolution using these criteria:

### Success Criteria

| Item | Success Condition |
|------|-------------------|
| **Task Duration** | Min/Max difference within 3x |
| **Shuffle Read Size** | Even partition sizes (within 2x) |
| **Job Completion** | All Tasks in SUCCEEDED state |
| **skew_ratio** | 2-3 or less |

### Check in Spark UI

1. Check Task Duration distribution in **Stages** tab.
2. Verify Min/Max difference is within 2-3x.
3. Check **Event Timeline** for even Task distribution.

### Check with Code

```java
// Re-check partition distribution after processing
result.groupBy(spark_partition_id())
    .count()
    .agg(
        max("count").alias("max"),
        min("count").alias("min"),
        avg("count").alias("avg")
    )
    .withColumn("skew_ratio", col("max").divide(col("avg")))
    .show();

// skew_ratio of 2-3 or less is normal
```

---

## Troubleshooting Checklist

### Solutions by Error Message

| Symptom/Log Message | Cause | Solution |
|---------------------|-------|----------|
| `Stage X: 199/200 tasks completed` (last Task delayed) | Data skew | Enable AQE or apply Salting |
| `FetchFailedException: Too large frame` | Single partition data too large | Partition split or Salting |
| `Container killed by YARN` (specific Tasks only) | Hot partition memory exceeded | Distribute hot keys |
| Task Duration Min/Max 10x+ difference in Spark UI | Data skew | Apply skew resolution techniques |

### Recommended Solutions by Situation

| Situation | Recommended Solution |
|-----------|---------------------|
| Using Spark 3.0+ | Enable AQE skew join |
| Joining with small table | Broadcast join |
| Only specific keys are hot | Selective Salting |
| NULL is the hot key | Separate NULL handling |
| All keys evenly skewed | Full Salting |

---

## Next Steps

- [Optimizing Shuffle](shuffle-optimization/) - Minimize shuffle I/O
- [Performance Tuning](../concepts/tuning/) - Overall performance optimization
