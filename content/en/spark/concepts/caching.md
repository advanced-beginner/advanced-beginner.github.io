---
title: Caching and Persistence
weight: 7
lastmod: "2026-01-07"
---

# Caching and Persistence

Learn how to leverage Spark's in-memory computing capabilities to cache intermediate results for reuse.

## What is Caching?

**Caching** is storing RDD/DataFrame in memory or disk for reuse in subsequent operations.

### Why Caching is Needed

```java
// Without caching: Inefficient
Dataset<Row> processed = df
    .filter(col("status").equalTo("ACTIVE"))
    .groupBy("category")
    .agg(sum("amount").alias("total"));

// Same data processed 3 times (full recomputation each time)
long count = processed.count();              // Job 1: Full computation
processed.show();                            // Job 2: Full recomputation
processed.write().parquet("output");         // Job 3: Full recomputation

// With caching: Efficient
processed.cache();                           // Register cache
long count = processed.count();              // Job 1: Compute + cache
processed.show();                            // Read from cache (fast)
processed.write().parquet("output");         // Read from cache (fast)
processed.unpersist();                       // Release cache
```

## Basic Usage

### cache()

```java
Dataset<Row> df = spark.read().parquet("large-data.parquet");

// cache() call - Cache with default storage level
df.cache();

// Actual caching happens on first Action
df.count();  // Data processing + cache to memory

// Subsequent Actions read from cache
df.filter(col("x").gt(10)).count();
df.groupBy("category").count().show();

// Release cache
df.unpersist();
```

### persist()

Allows specifying storage level directly.

```java
import org.apache.spark.storage.StorageLevel;

// Cache in memory only (default)
df.persist(StorageLevel.MEMORY_ONLY());

// Memory + Disk (use disk when memory insufficient)
df.persist(StorageLevel.MEMORY_AND_DISK());

// Serialized in memory (saves memory, uses CPU)
df.persist(StorageLevel.MEMORY_ONLY_SER());

// Serialized + disk backup
df.persist(StorageLevel.MEMORY_AND_DISK_SER());

// Disk only
df.persist(StorageLevel.DISK_ONLY());

// Maintain 2 replicas (for fault tolerance)
df.persist(StorageLevel.MEMORY_AND_DISK_2());
```

## Storage Level Details

| Storage Level | Memory | Disk | Serialized | Replicas | Characteristics |
|--------------|--------|------|------------|----------|-----------------|
| MEMORY_ONLY | O | X | X | 1 | Fastest, high memory usage |
| MEMORY_AND_DISK | O | O | X | 1 | Falls back to disk when memory insufficient |
| MEMORY_ONLY_SER | O | X | O | 1 | Saves memory, uses CPU |
| MEMORY_AND_DISK_SER | O | O | O | 1 | Memory savings + disk backup |
| DISK_ONLY | X | O | O | 1 | No memory usage |
| OFF_HEAP | X (off-heap) | X | O | 1 | No GC impact |
| *_2 | - | - | - | 2 | 2 replicas |

### Which Level to Choose?

```
Sufficient memory + fast access needed → MEMORY_ONLY
Limited memory + fast access needed → MEMORY_ONLY_SER
Limited memory + reliability needed → MEMORY_AND_DISK_SER
Extremely limited memory → DISK_ONLY
High availability needed → *_2 variants
```

## Caching vs Checkpoint

### Caching (cache/persist)

```java
df.cache();
```

- **Pros**: Fast, simple
- **Cons**: Requires lineage recomputation on failure
- **Use case**: Optimizing repeated access

### Checkpoint

```java
// Set checkpoint directory
spark.sparkContext().setCheckpointDir("hdfs:///checkpoints");

// Save checkpoint
df.checkpoint();
// Or eager checkpoint (save immediately)
df.checkpoint(true);
```

- **Pros**: Cuts lineage, immediate recovery on failure
- **Cons**: Requires disk I/O
- **Use case**: Long lineage, failure recovery needed

### When to Use What?

```java
// Simple repeated use → cache
Dataset<Row> frequently = df.filter(...).cache();
for (int i = 0; i < 10; i++) {
    process(frequently);
}

// Cut long lineage → checkpoint
Dataset<Row> stage1 = df.map(...).filter(...).join(...);
stage1 = stage1.checkpoint();  // Cut lineage

Dataset<Row> stage2 = stage1.map(...).filter(...).join(...);
stage2 = stage2.checkpoint();  // Cut again

// ML iterative algorithms → use both
Dataset<Row> data = loadData().cache();  // Repeated access

for (int iter = 0; iter < 100; iter++) {
    data = processIteration(data);
    if (iter % 10 == 0) {
        data = data.checkpoint();  // Periodically cut lineage
    }
}
```

## Cache Management

### Checking Cache Status

```java
// Check if cached
boolean isCached = spark.catalog().isCached("table_name");

// Check in Spark UI
// http://localhost:4040 → Storage tab
```

### Releasing Cache

```java
// Release specific DataFrame
df.unpersist();

// Blocking mode (wait for release completion)
df.unpersist(true);

// Uncache table
spark.catalog().uncacheTable("table_name");

// Clear all cache
spark.catalog().clearCache();
```

### Refreshing Cache

```java
// Refresh cached table (reflect source changes)
spark.catalog().refreshTable("table_name");
```

## Caching in SQL

```sql
-- Cache table
CACHE TABLE employees;

-- Lazy cache (cache on first query)
CACHE LAZY TABLE employees;

-- Cache query results
CACHE TABLE active_employees AS
SELECT * FROM employees WHERE status = 'ACTIVE';

-- Uncache
UNCACHE TABLE employees;

-- Clear all cache
CLEAR CACHE;
```

## Caching Best Practices

### 1. Only Cache Data Used Multiple Times

```java
// Good: Used multiple times
Dataset<Row> base = loadAndProcess().cache();
analyze1(base);
analyze2(base);
analyze3(base);
base.unpersist();

// Bad: Used only once (unnecessary caching)
Dataset<Row> oneTime = loadData().cache();
oneTime.write().parquet("output");  // Used only once
```

### 2. Cache at the Right Time

```java
// Good: Cache after expensive operations
Dataset<Row> expensive = df
    .filter(...)
    .join(otherDf, ...)      // Expensive join
    .groupBy(...).agg(...)   // Expensive aggregation
    .cache();                // Cache here

// Bad: Cache too early (stores unnecessary data)
Dataset<Row> early = df.cache();  // Before filtering
Dataset<Row> filtered = early.filter(col("needed").equalTo(true));
```

### 3. Monitor Memory

```java
// About 60% of Executor memory used for storage/cache (default)
spark.conf().set("spark.memory.storageFraction", "0.5");  // Adjust to 50%

// Prevent forced cache eviction (keep cache when memory low)
spark.conf().set("spark.memory.storageFraction", "0.6");
```

### 4. Release Immediately After Use

```java
Dataset<Row> cached = expensive.cache();
try {
    // Use cached data
    doAnalysis(cached);
} finally {
    cached.unpersist();  // Always release
}
```

### 5. Consider Serialization

```java
// Serialization efficient for large objects
Dataset<Row> largeObjects = df.persist(StorageLevel.MEMORY_ONLY_SER());

// Use Kryo serialization (more efficient)
spark.conf().set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
spark.conf().set("spark.kryo.registrationRequired", "false");
```

## Caching and Partitioning

```java
// Recommend adjusting partitions before caching
Dataset<Row> optimized = df
    .filter(...)
    .repartition(100)  // Adjust to appropriate partition count
    .cache();

// Too many partitions = memory overhead
// Too few partitions = reduced parallelism
```

## Practical Example: Machine Learning Pipeline

```java
public class MLPipelineWithCaching {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("ML Pipeline")
                .master("local[*]")
                .getOrCreate();

        // 1. Load and preprocess data (once)
        Dataset<Row> rawData = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("training-data.csv");

        Dataset<Row> processed = rawData
                .na().fill(0)
                .filter(col("label").isNotNull())
                .withColumn("features", createFeatures());

        // 2. Cache preprocessed data (used for repeated training)
        processed.cache();
        System.out.println("Data cached: " + processed.count() + " rows");

        // 3. Train/validation split
        Dataset<Row>[] splits = processed.randomSplit(new double[]{0.8, 0.2});
        Dataset<Row> training = splits[0].cache();
        Dataset<Row> validation = splits[1].cache();

        // 4. Hyperparameter tuning (cached data used repeatedly)
        double[] learningRates = {0.1, 0.01, 0.001};
        int[] maxDepths = {5, 10, 15};

        for (double lr : learningRates) {
            for (int depth : maxDepths) {
                // Training (reads from cache - fast)
                trainModel(training, lr, depth);

                // Validation (reads from cache - fast)
                double score = evaluate(validation);
                System.out.printf("lr=%.3f, depth=%d, score=%.4f%n", lr, depth, score);
            }
        }

        // 5. Release cache
        training.unpersist();
        validation.unpersist();
        processed.unpersist();

        spark.stop();
    }
}
```

## Troubleshooting

### Cache Failure Due to Memory Shortage

```
WARN MemoryStore: Not enough space to cache rdd_X in memory!
```

Solutions:
```java
// 1. Use serialization
df.persist(StorageLevel.MEMORY_AND_DISK_SER());

// 2. Use disk
df.persist(StorageLevel.DISK_ONLY());

// 3. Increase Executor memory
spark.conf().set("spark.executor.memory", "8g");

// 4. Release unnecessary cache
anotherDf.unpersist();
```

### Cache Not Working as Expected

```java
// Caution: cache() not applied when new DataFrame is created after
Dataset<Row> df = spark.read().parquet("data");
df.cache();
Dataset<Row> filtered = df.filter(...);  // New DataFrame
// filtered is NOT cached!

// Correct way
Dataset<Row> df = spark.read().parquet("data").cache();
Dataset<Row> filtered = df.filter(...);  // df is cached
```

## Next Steps

- [Structured Streaming](../structured-streaming/) - Real-time data processing
- [Performance Tuning](../tuning/) - Overall performance optimization

## Related Documents

- [Transformations and Actions](../transformations-actions/) - Relationship between lazy evaluation and caching
- [Partitioning and Shuffle](../partitioning/) - Partition optimization before caching
- [RDD Basics](../rdd/) - RDD-level persist/cache
- [MLlib](../mllib/) - Caching in machine learning
- [Glossary](../../appendix/glossary/) - Persist, Storage Level term definitions
