---
title: Performance Tuning
description: "Spark performance tuning parameters and strategies"
weight: 10
lastmod: "2026-01-07"
---

# Performance Tuning

Learn strategies and specific configuration methods for optimizing Spark application performance.

## Tuning Principles

### Tuning Order

1. **Code optimization** — Improve algorithms and logic
2. **Data structure optimization** — Partitioning, schema optimization
3. **Resource configuration** — Memory, CPU, partition count
4. **Detailed settings** — Spark configuration parameters

### Measurement Tools

- **Spark UI**: Job, Stage, Task analysis
- **Event Log**: Detailed execution history
- **Metrics**: Executor metric monitoring

## Code-Level Optimization

### 1. Minimize Shuffle

```java
// Bad: Unnecessary shuffles
Dataset<Row> result = df
    .repartition(100)  // Shuffle
    .groupBy("key")    // Shuffle
    .agg(sum("value"))
    .orderBy("key");   // Shuffle

// Good: Only necessary shuffles
Dataset<Row> result = df
    .groupBy("key")
    .agg(sum("value"));
// Only add orderBy when truly needed
```

### 2. Early Filtering

```java
// Bad: Filter after join
Dataset<Row> joined = large.join(small, "key")
    .filter(col("status").equalTo("ACTIVE"));

// Good: Filter before join
Dataset<Row> filtered = large.filter(col("status").equalTo("ACTIVE"));
Dataset<Row> joined = filtered.join(small, "key");
```

### 3. Column Selection

```java
// Bad: Keep all columns
Dataset<Row> result = df.join(other, "id")
    .groupBy("category")
    .agg(sum("value"));

// Good: Select only needed columns
Dataset<Row> result = df.select("id", "category", "value")
    .join(other.select("id"), "id")
    .groupBy("category")
    .agg(sum("value"));
```

### 4. Broadcast Join

```java
import static org.apache.spark.sql.functions.broadcast;

// Broadcast small tables (tens of MB)
Dataset<Row> result = largeTable.join(
    broadcast(smallTable),
    "key"
);

// Set auto broadcast threshold
spark.conf().set("spark.sql.autoBroadcastJoinThreshold", "100MB");
```

### 5. Leverage Caching

```java
// Cache repeatedly used data
Dataset<Row> processed = df.filter(...).groupBy(...).agg(...);
processed.cache();

// Use multiple times
process1(processed);
process2(processed);
process3(processed);

processed.unpersist();
```

## Data Structure Optimization

### 1. File Formats

| Format | Compression | Column Pruning | Use Case |
|--------|-------------|----------------|----------|
| Parquet | High | Supported | Analytical queries (recommended) |
| ORC | High | Supported | Hive compatibility |
| Avro | Medium | Not supported | Streaming, schema evolution |
| JSON | None | Not supported | When compatibility needed |
| CSV | None | Not supported | Simple data exchange |

```java
// Save as Parquet (recommended)
df.write()
    .mode("overwrite")
    .option("compression", "snappy")  // snappy, gzip, zstd
    .parquet("output/data");
```

### 2. Partitioning Strategy

```java
// Partition matching query patterns
df.write()
    .partitionBy("year", "month")
    .parquet("output/data");

// Partition pruning on query
Dataset<Row> jan2024 = spark.read()
    .parquet("output/data")
    .filter(col("year").equalTo(2024).and(col("month").equalTo(1)));
// Only reads year=2024/month=1 partition
```

### 3. Bucketing

```java
// Bucket by join key
df.write()
    .bucketBy(100, "user_id")
    .sortBy("timestamp")
    .saveAsTable("events");

// Join tables with same bucketing has no shuffle
```

### 4. Small File Problem Resolution

```java
// Adjust partitions before writing
df.coalesce(10)  // Merge into 10 files
  .write()
  .parquet("output");

// Or specify max records per file
df.write()
    .option("maxRecordsPerFile", 1000000)
    .parquet("output");
```

## Resource Configuration

### Memory Structure

```
Executor Memory (spark.executor.memory)
├── Execution Memory: Shuffle, join, sort
├── Storage Memory: Cache, broadcast
└── User Memory: UDFs, user objects

Default ratio (spark.memory.fraction = 0.6):
├── Execution + Storage: 60%
└── User: 40%
```

### Recommended Settings

```bash
# Executor settings
spark.executor.instances=10       # Number of Executors
spark.executor.memory=8g          # Executor memory
spark.executor.cores=4            # Cores per Executor

# Driver settings
spark.driver.memory=4g            # Driver memory
spark.driver.cores=2              # Driver cores

# Parallelism
spark.default.parallelism=200     # Default RDD partition count
spark.sql.shuffle.partitions=200  # SQL shuffle partition count
```

### Cluster Sizing Guide

```
Total cores = spark.executor.instances × spark.executor.cores
Recommended partitions = Total cores × 2-4

Example: 50 Executors × 4 cores = 200 cores
→ Partition count: 400-800
```

### Executor Memory Calculation

```
spark.executor.memory = Container memory × 0.9 - Overhead

Example: 10GB container
- Overhead (10%): 1GB
- spark.executor.memory: ~8GB
```

## Shuffle Optimization

### Shuffle-Related Settings

```java
// Shuffle partition count (most important)
spark.conf().set("spark.sql.shuffle.partitions", "400");

// Shuffle buffer size
spark.conf().set("spark.shuffle.file.buffer", "64k");

// Shuffle compression
spark.conf().set("spark.shuffle.compress", "true");

// Shuffle spill threshold
spark.conf().set("spark.shuffle.spill.compress", "true");
```

### AQE (Adaptive Query Execution)

Automatic optimization in Spark 3.0+:

```java
// Enable AQE (enabled by default in 3.2+)
spark.conf().set("spark.sql.adaptive.enabled", "true");

// Auto partition coalescing
spark.conf().set("spark.sql.adaptive.coalescePartitions.enabled", "true");
spark.conf().set("spark.sql.adaptive.advisoryPartitionSizeInBytes", "64MB");

// Auto broadcast join conversion
spark.conf().set("spark.sql.adaptive.localShuffleReader.enabled", "true");

// Skew join handling
spark.conf().set("spark.sql.adaptive.skewJoin.enabled", "true");
```

## Serialization Optimization

### Kryo Serialization

```java
SparkSession spark = SparkSession.builder()
    .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    .config("spark.kryo.registrationRequired", "false")
    .getOrCreate();

// Register classes (performance improvement)
spark.conf().set("spark.kryo.classesToRegister",
    "com.example.MyClass1,com.example.MyClass2");
```

### Serialization Comparison

| Serialization | Speed | Size | Configuration |
|--------------|-------|------|---------------|
| Java (default) | Slow | Large | None |
| Kryo | Fast | Small | Recommended |

## GC Optimization

### GC Settings

```bash
# Use G1GC (suitable for large heaps)
spark.executor.extraJavaOptions=-XX:+UseG1GC -XX:MaxGCPauseMillis=200

# GC logging
spark.executor.extraJavaOptions=-XX:+PrintGCDetails -XX:+PrintGCTimeStamps
```

### Off-Heap Memory

```java
// Enable Off-Heap (reduces GC impact)
spark.conf().set("spark.memory.offHeap.enabled", "true");
spark.conf().set("spark.memory.offHeap.size", "2g");
```

## SQL Optimization

### Join Strategy Hints

```sql
-- Broadcast hint
SELECT /*+ BROADCAST(small) */ *
FROM large JOIN small ON large.id = small.id;

-- Shuffle hash join
SELECT /*+ SHUFFLE_HASH(t1) */ *
FROM t1 JOIN t2 ON t1.id = t2.id;

-- Sort merge join
SELECT /*+ SORT_MERGE(t1, t2) */ *
FROM t1 JOIN t2 ON t1.id = t2.id;
```

### Query Optimization

```java
// Check execution plan
df.explain("extended");

// Cost-based optimization
spark.conf().set("spark.sql.cbo.enabled", "true");
spark.conf().set("spark.sql.cbo.joinReorder.enabled", "true");

// Collect table statistics (Hive tables)
spark.sql("ANALYZE TABLE my_table COMPUTE STATISTICS");
spark.sql("ANALYZE TABLE my_table COMPUTE STATISTICS FOR COLUMNS col1, col2");
```

## Streaming Tuning

### Throughput Optimization

```java
// Trigger interval
.trigger(Trigger.ProcessingTime("5 seconds"))

// Kafka batch size
.option("maxOffsetsPerTrigger", "100000")

// State store configuration
spark.conf().set("spark.sql.streaming.stateStore.providerClass",
    "org.apache.spark.sql.execution.streaming.state.RocksDBStateStoreProvider");
```

## Performance Monitoring

### Spark UI Usage

Key items to check:
1. **Jobs**: Overall job execution time
2. **Stages**: Shuffle size, spill per stage
3. **Tasks**: Task distribution, skew detection
4. **Storage**: Cache usage
5. **SQL**: Query execution plans

### Checking Metrics

```java
// Runtime metrics
spark.sparkContext().statusTracker().getJobInfo(jobId);
spark.sparkContext().statusTracker().getStageInfo(stageId);

// Post-execution statistics
Dataset<Row> df = spark.read().parquet("data");
df.cache();
df.count();  // Load cache

// Logical/physical plan
df.explain(true);
```

## Checklist

### Code Checklist

- [ ] Remove unnecessary shuffles
- [ ] Apply early filtering
- [ ] Select only needed columns
- [ ] Broadcast small tables
- [ ] Cache repeatedly used data
- [ ] Use built-in functions instead of UDFs

### Data Checklist

- [ ] Use Parquet format
- [ ] Appropriate partitioning
- [ ] Merge small files
- [ ] Explicit schema specification

### Configuration Checklist

- [ ] Enable AQE
- [ ] Appropriate shuffle partition count
- [ ] Kryo serialization
- [ ] Adjust broadcast threshold
- [ ] Proper memory/core allocation

## Next Steps

- [Deployment and Cluster Management](deployment/) - Production environment configuration
- [FAQ](../appendix/faq/) - Solving common performance issues

## Related Documents

- [Architecture](architecture/) - Driver/Executor memory structure
- [Partitioning and Shuffle](partitioning/) - Detailed shuffle optimization
- [Caching and Persistence](caching/) - Cache memory management
- [Spark SQL](spark-sql/) - SQL query optimization
- [Basic Examples](../examples/basic/) - Optimization application examples
- [Glossary](../appendix/glossary/) - AQE, Tungsten term definitions
