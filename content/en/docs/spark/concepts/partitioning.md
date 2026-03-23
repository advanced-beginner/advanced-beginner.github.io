---
title: Partitioning and Shuffle
description: "Partitioning and shuffle mechanics and performance impact"
weight: 6
lastmod: "2026-01-07"
---

# Partitioning and Shuffle

Partitioning is key to Spark performance. Understanding how data is distributed and optimizing it is crucial for large-scale data processing.

## What is a Partition?

A **Partition** is a logical division unit of RDD/DataFrame data. Each partition is processed on one node in the cluster.

```
DataFrame (100 million rows)
├── Partition 0 (10 million rows) → Executor 1, Task 0
├── Partition 1 (10 million rows) → Executor 2, Task 1
├── Partition 2 (10 million rows) → Executor 1, Task 2
├── ...
└── Partition 9 (10 million rows) → Executor 4, Task 9
```

### Why Partitions Matter

| Partition Count | Impact |
|-----------------|--------|
| Too few | Low parallelism, possible memory issues, underutilized nodes |
| Too many | Scheduling overhead, small task inefficiency, increased shuffle cost |
| Optimal | Balanced load distribution, efficient resource usage |

## Checking and Adjusting Partition Count

### Checking Current Partition Count

```java
Dataset<Row> df = spark.read().parquet("data.parquet");
int partitions = df.rdd().getNumPartitions();
System.out.println("Partition count: " + partitions);

// Count data per partition
df.mapPartitions(
    iter -> {
        int count = 0;
        while (iter.hasNext()) { iter.next(); count++; }
        return Collections.singletonList(count).iterator();
    },
    Encoders.INT()
).collectAsList().forEach(System.out::println);
```

### Guidelines for Determining Partition Count

```
Recommended partition size: 100MB ~ 200MB
Minimum partition count: Total cluster cores × 2-4
Maximum partition count: Data size (GB) × 2-4
```

Example:
- 100GB data, 50-core cluster
- Minimum: 50 × 2 = 100 partitions
- Maximum: 100 × 4 = 400 partitions
- Recommended: ~200 partitions (100GB ÷ 200MB × 4)

### Adjusting Partitions

```java
// repartition - Change partition count (causes shuffle)
Dataset<Row> repartitioned = df.repartition(200);

// coalesce - Reduce partition count (no shuffle)
Dataset<Row> coalesced = df.coalesce(100);

// Key-based repartitioning
Dataset<Row> keyPartitioned = df.repartition(col("department"));
Dataset<Row> keyWithCount = df.repartition(100, col("department"));
```

### repartition vs coalesce

| Feature | repartition | coalesce |
|---------|------------|----------|
| Shuffle | Yes | No |
| Partition count | Increase/decrease | Decrease only |
| Data distribution | Evenly distributed | May be uneven |
| Use case | Increase partitions, need even distribution | Decrease partitions |

```java
// Reduce partitions before writing (control file count)
df.coalesce(10)
  .write()
  .parquet("output");
// → Creates 10 parquet files

// Repartition to resolve skew
df.repartition(200, col("key"))
  .groupBy("key")
  .agg(sum("value"));
```

## Shuffle

**Shuffle** is the process of redistributing data across partitions. It occurs during Wide Transformations.

### Operations That Cause Shuffle

```java
// groupBy - Same keys to same partition
df.groupBy("key").agg(sum("value"));

// join - Same keys to same partition
df1.join(df2, "key");

// distinct - Redistribute for deduplication
df.distinct();

// repartition - Explicit redistribution
df.repartition(100);

// orderBy/sort - Global sorting
df.orderBy("column");
```

### Shuffle Process

```
Map Phase (Shuffle Write)
├── Each Task classifies data by key
├── Creates shuffle files per partition
└── Saves to disk (using memory buffer)

Reduce Phase (Shuffle Read)
├── Each Task reads shuffle files for needed partitions
├── Data transferred over network
└── Performs sorting/aggregation
```

### Cost of Shuffle

Shuffle is the most expensive operation in Spark:

1. **Disk I/O**: Intermediate results saved to disk
2. **Network I/O**: Data transfer between nodes
3. **Serialization**: Data serialization/deserialization
4. **Sorting**: Sorting by key

## Shuffle Partition Settings

```java
// Set shuffle partition count (default: 200)
spark.conf().set("spark.sql.shuffle.partitions", "400");

// Or when creating SparkSession
SparkSession spark = SparkSession.builder()
    .appName("App")
    .config("spark.sql.shuffle.partitions", "400")
    .getOrCreate();
```

### Adaptive Query Execution (AQE)

In Spark 3.0+, AQE automatically adjusts shuffle partitions.

```java
// Enable AQE (enabled by default in Spark 3.2+)
spark.conf().set("spark.sql.adaptive.enabled", "true");

// Enable partition coalescing
spark.conf().set("spark.sql.adaptive.coalescePartitions.enabled", "true");

// Minimum partition size (coalescing threshold)
spark.conf().set("spark.sql.adaptive.advisoryPartitionSizeInBytes", "64MB");

// Skew join optimization
spark.conf().set("spark.sql.adaptive.skewJoin.enabled", "true");
```

Benefits of AQE:
- Adjusts partition count at runtime based on actual data size
- Automatically merges small partitions
- Automatically handles data skew

## Partitioning Strategies

### Hash Partitioning

Most common partitioning. Determines partition based on key hash value.

```java
// Hash Partitioning
df.repartition(100, col("user_id"));

// Hash function: partition = hash(key) % numPartitions
```

### Range Partitioning

Determines partition based on key value ranges. Suitable for sorted data.

```java
// Range Partitioning
df.repartitionByRange(100, col("timestamp"));

// Example: timestamp 0-100 → Partition 0
//          timestamp 100-200 → Partition 1
```

### Custom Partitioning (RDD)

RDDs allow defining custom partitioners.

```java
import org.apache.spark.Partitioner;

public class RegionPartitioner extends Partitioner {
    private int numPartitions;

    public RegionPartitioner(int numPartitions) {
        this.numPartitions = numPartitions;
    }

    @Override
    public int numPartitions() {
        return numPartitions;
    }

    @Override
    public int getPartition(Object key) {
        String region = (String) key;
        switch (region) {
            case "ASIA": return 0;
            case "EUROPE": return 1;
            case "AMERICA": return 2;
            default: return 3;
        }
    }
}

// Usage
JavaPairRDD<String, Integer> partitioned =
    pairRDD.partitionBy(new RegionPartitioner(4));
```

## Data Skew

Data skew occurs when data concentrates in specific partitions.

### Detecting Skew

```java
// Check data count per partition
df.groupBy(spark_partition_id().alias("partition"))
  .count()
  .orderBy(col("count").desc())
  .show();

// Output:
// +----------+--------+
// |partition |   count|
// +----------+--------+
// |        5 |10000000|  ← Skew!
// |        3 |   50000|
// |        1 |   45000|
// ...
```

### Skew Resolution Methods

**1. Salting**

```java
import java.util.Random;

// Add random salt to hot keys
int saltBuckets = 10;
Random rand = new Random();

Dataset<Row> salted = df.withColumn(
    "salted_key",
    concat(col("key"), lit("_"), lit(rand.nextInt(saltBuckets)))
);

// Aggregate with salted key
Dataset<Row> partialAgg = salted
    .groupBy("salted_key")
    .agg(sum("value").alias("partial_sum"));

// Final aggregation with original key
Dataset<Row> finalResult = partialAgg
    .withColumn("original_key", split(col("salted_key"), "_").getItem(0))
    .groupBy("original_key")
    .agg(sum("partial_sum").alias("total"));
```

**2. Broadcast Join**

Avoids skew when joining with small tables.

```java
import static org.apache.spark.sql.functions.broadcast;

// Broadcast small table
Dataset<Row> result = largeTable.join(
    broadcast(smallTable),
    "key"
);
```

**3. AQE Skew Join**

```java
// Enable AQE skew join in Spark 3.0+
spark.conf().set("spark.sql.adaptive.enabled", "true");
spark.conf().set("spark.sql.adaptive.skewJoin.enabled", "true");
spark.conf().set("spark.sql.adaptive.skewJoin.skewedPartitionFactor", "5");
spark.conf().set("spark.sql.adaptive.skewJoin.skewedPartitionThresholdInBytes", "256MB");
```

**4. Two-Stage Aggregation**

```java
// Stage 1: Partial aggregation within partitions
Dataset<Row> partial = df
    .repartition(1000)  // Distribute across many partitions
    .groupBy("key", spark_partition_id().alias("part"))
    .agg(sum("value").alias("partial_sum"));

// Stage 2: Final aggregation
Dataset<Row> result = partial
    .groupBy("key")
    .agg(sum("partial_sum").alias("total"));
```

## Joins and Partitioning

### Join Strategies

```java
// 1. Broadcast Hash Join (small table)
// Automatic or explicit broadcast
spark.conf().set("spark.sql.autoBroadcastJoinThreshold", "100MB");
df1.join(broadcast(df2), "key");

// 2. Sort-Merge Join (large tables)
// Sort both tables by key then merge
// Default join strategy

// 3. Shuffle Hash Join
// Create hash table on one side only
df1.join(df2.hint("shuffle_hash"), "key");
```

### Join Optimization

```java
// Filter before join (reduces shuffle data)
Dataset<Row> filtered1 = df1.filter(col("status").equalTo("ACTIVE"));
Dataset<Row> filtered2 = df2.filter(col("type").equalTo("VALID"));
Dataset<Row> joined = filtered1.join(filtered2, "key");

// Select only needed columns
Dataset<Row> slim1 = df1.select("key", "needed_col1");
Dataset<Row> slim2 = df2.select("key", "needed_col2");
Dataset<Row> joined = slim1.join(slim2, "key");

// Pre-partitioning (avoids shuffle if same key partitioning)
Dataset<Row> part1 = df1.repartition(100, col("key"));
Dataset<Row> part2 = df2.repartition(100, col("key"));
Dataset<Row> joined = part1.join(part2, "key");
// → Same keys already in same partition, no shuffle needed
```

## File Partitioning

Data can be partitioned at the file system level when saving.

```java
// Save with partition columns
df.write()
    .partitionBy("year", "month")
    .parquet("output/data");

// Generated directory structure:
// output/data/
// ├── year=2024/
// │   ├── month=01/
// │   │   ├── part-00000.parquet
// │   │   └── part-00001.parquet
// │   ├── month=02/
// │   ...
// └── year=2025/
//     ...

// Partition pruning (reads only relevant partitions)
Dataset<Row> filtered = spark.read()
    .parquet("output/data")
    .filter(col("year").equalTo(2024).and(col("month").equalTo(1)));
// → Only scans year=2024/month=01/ directory
```

### Bucketing

```java
// Save with bucketing (join optimization)
df.write()
    .bucketBy(100, "user_id")
    .sortBy("timestamp")
    .saveAsTable("user_events");

// Join tables with same bucketing has no shuffle
Dataset<Row> users = spark.table("users").bucketBy(100, "user_id");
Dataset<Row> events = spark.table("user_events");
Dataset<Row> joined = users.join(events, "user_id");
// → Join without shuffle (same user_id in same bucket)
```

## Monitoring

### Checking Shuffle in Spark UI

1. **Stages tab**: Shuffle read/write size for each Stage
2. **Tasks tab**: Shuffle data size for individual Tasks
3. **Executors tab**: Shuffle data statistics per Executor

### Shuffle Metrics

Key metrics to watch:
- **Shuffle Write**: Shuffle data output from Stage
- **Shuffle Read**: Shuffle data read by Stage
- **Shuffle Spill (Memory)**: Memory spill size
- **Shuffle Spill (Disk)**: Disk spill size (high means memory shortage)

## Next Steps

- [Caching and Persistence](caching/) - Prevent recomputation with intermediate result storage
- [Performance Tuning](tuning/) - Overall performance optimization strategies

## Related Documents

- [Architecture](architecture/) - Understanding distributed processing structure
- [Transformations and Actions](transformations-actions/) - Narrow/Wide Transformations
- [Spark SQL](spark-sql/) - Partition handling in SQL queries
- [Deployment and Cluster Management](deployment/) - Partition settings in cluster environments
- [Glossary](../appendix/glossary/) - Partition, Shuffle term definitions
