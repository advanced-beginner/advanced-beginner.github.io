---
title: Transformations and Actions
weight: 5
lastmod: "2026-01-07"
---

# Transformations and Actions

All Spark operations are classified into two categories: **Transformations** and **Actions**. Understanding this distinction is fundamental to Spark programming.

## Core Concepts

### Transformation

**Transformations** are operations that create new RDDs/DataFrames from existing ones.

- **Lazy Evaluation**: Not executed immediately when called
- **Immutability**: Creates new data without modifying original
- **DAG Construction**: Adds operations to the execution plan (DAG)

```java
// These lines don't execute - they just add to the DAG
Dataset<Row> df = spark.read().csv("data.csv");
Dataset<Row> filtered = df.filter(col("age").gt(30));      // Transformation
Dataset<Row> selected = filtered.select("name", "age");    // Transformation

// Nothing has executed yet!
```

### Action

**Actions** are operations that trigger actual computation and return results.

- **Immediate Execution**: Executes DAG to produce results
- **Result Return**: Returns values to Driver or saves externally
- **Job Creation**: Each Action creates one Job

```java
// When Action is called, the entire DAG executes
long count = selected.count();        // Action! → Job executes
selected.show();                      // Action! → Job executes
List<Row> rows = selected.collectAsList();  // Action! → Job executes
```

## Why Lazy Evaluation?

### 1. Optimization Opportunities

Lazy evaluation allows Spark to analyze and optimize the entire DAG.

```java
// Code that looks inefficient
Dataset<Row> result = df
    .select("name", "age", "salary", "department")  // Select 4 columns
    .filter(col("age").gt(30))                       // Filter
    .select("name", "age");                          // Use only 2 columns

// Catalyst Optimizer optimizes:
// → Executes select("name", "age") first to remove unnecessary columns
// → Actually reads only "name", "age" and filters
```

### 2. Pipelining

Multiple Transformations are pipelined within a single Stage:

```java
// Logically 3 iterations
Dataset<Row> result = df
    .filter(col("status").equalTo("ACTIVE"))
    .map(row -> transform(row))
    .filter(col("score").gt(80));

// Actually processed in 1 iteration (pipelining)
// filter1 → map → filter2 applied sequentially to each record
```

### 3. Avoiding Unnecessary Computation

```java
Dataset<Row> expensive = df
    .groupBy("category")
    .agg(complexAggregation());

// If not used later, computation is skipped
// If only take(1) is called, only computes what's needed
Row first = expensive.first();
```

## Types of Transformations

### Narrow Transformations

Each input partition contributes to at most one output partition. **No shuffle occurs.**

```java
// map - transform each element
Dataset<Row> doubled = df.withColumn("doubled", col("value").multiply(2));

// filter - conditional filtering
Dataset<Row> adults = df.filter(col("age").geq(18));

// flatMap - 1:N transformation
Dataset<String> words = df.flatMap(
    row -> Arrays.asList(row.getString(0).split(" ")).iterator(),
    Encoders.STRING()
);

// select - column selection
Dataset<Row> subset = df.select("name", "email");

// withColumn - add/modify column
Dataset<Row> enhanced = df.withColumn("year", year(col("date")));

// union - combine two DataFrames (partitions maintained)
Dataset<Row> combined = df1.union(df2);

// sample - sampling
Dataset<Row> sampled = df.sample(0.1);  // 10% sample
```

**Characteristics:**
- Processing completed within same partition
- No network I/O
- Very efficient
- Consecutive Narrow Transformations are pipelined

### Wide Transformations

Data from multiple input partitions contributes to one output partition. **Shuffle occurs.**

```java
// groupBy - grouping
Dataset<Row> grouped = df.groupBy("department").agg(sum("salary"));

// join - joining
Dataset<Row> joined = employees.join(departments, "department_id");

// distinct - deduplication
Dataset<Row> unique = df.distinct();

// repartition - partition redistribution
Dataset<Row> repartitioned = df.repartition(100);
Dataset<Row> repartitionedByKey = df.repartition(col("key"));

// orderBy/sort - global sorting
Dataset<Row> sorted = df.orderBy(col("salary").desc());

// reduceByKey (RDD) - aggregation by key
JavaPairRDD<String, Integer> reduced = pairRdd.reduceByKey(Integer::sum);
```

**Characteristics:**
- Data redistribution (shuffle) required
- Network I/O occurs
- Becomes Stage boundary
- Greatest impact on performance

## Types of Actions

### Value-Returning Actions

```java
// collect - bring all data to Driver (caution: OOM with large data)
List<Row> allRows = df.collectAsList();

// first/head - first element
Row firstRow = df.first();
Row headRow = df.head();

// take - first n elements
List<Row> topN = df.takeAsList(10);

// count - number of elements
long rowCount = df.count();

// reduce - aggregation (RDD)
int sum = numbersRdd.reduce(Integer::sum);
```

### Output Actions

```java
// show - console output
df.show();
df.show(20);                    // 20 rows
df.show(20, false);             // no string truncation
df.show(20, 100, false);        // max 100 chars, no truncation

// printSchema - schema output
df.printSchema();

// describe - statistics output
df.describe("age", "salary").show();
```

### Save Actions

```java
// write - save to file
df.write()
    .mode("overwrite")           // overwrite, append, ignore, error
    .partitionBy("year", "month")
    .parquet("output/data");

// saveAsTable - save to Hive table
df.write()
    .mode("overwrite")
    .saveAsTable("my_table");

// foreach - execute function on each partition
df.foreach(row -> {
    // Runs on Executor
    externalSystem.write(row);
});

// foreachPartition - partition-level processing
df.foreachPartition(partition -> {
    Connection conn = getConnection();
    while (partition.hasNext()) {
        Row row = partition.next();
        insertToDb(conn, row);
    }
    conn.close();
});
```

## Understanding Execution Flow

### DAG Construction and Execution

```java
Dataset<Row> df = spark.read().csv("data.csv");

// Step 1: Build DAG (Transformations)
Dataset<Row> step1 = df.filter(col("status").equalTo("ACTIVE"));
Dataset<Row> step2 = step1.select("id", "name", "score");
Dataset<Row> step3 = step2.filter(col("score").gt(80));

// Not executed yet - only DAG is built

// Step 2: Action called → Job created → Execution
long count = step3.count();  // Job 1 executes

// Step 3: Another Action → New Job executes
step3.show();               // Job 2 executes (recomputes from start)
```

### Job, Stage, Task Relationship

```java
Dataset<Row> result = df
    .filter(col("active").equalTo(true))      // Stage 1: Narrow
    .withColumn("year", year(col("date")))    // Stage 1: Narrow
    .groupBy("year")                           // Stage boundary (Wide)
    .agg(sum("amount").alias("total"))        // Stage 2
    .orderBy(col("total").desc());            // Stage boundary (Wide) → Stage 3

result.show();  // 1 Job, 3 Stages
```

```
Job 1
├── Stage 1: filter → withColumn (Narrow, pipelined)
│   └── [Task 1] [Task 2] [Task 3] ...
│       ↓ Shuffle ↓
├── Stage 2: groupBy → agg
│   └── [Task 1] [Task 2] [Task 3] ...
│       ↓ Shuffle ↓
└── Stage 3: orderBy → show
    └── [Task 1] [Task 2] [Task 3] ...
```

## Caching and Reuse

Using the same DataFrame with multiple Actions recomputes each time. Caching prevents this.

```java
// Without caching - inefficient
Dataset<Row> processed = df.filter(...).groupBy(...).agg(...);
long count = processed.count();      // Full computation
processed.show();                    // Full recomputation!
processed.write().parquet("...");    // Full recomputation!!

// With caching - efficient
Dataset<Row> processed = df.filter(...).groupBy(...).agg(...);
processed.cache();                   // Register cache (not computed yet)

long count = processed.count();      // Compute + cache
processed.show();                    // Read from cache
processed.write().parquet("...");    // Read from cache

processed.unpersist();               // Release cache
```

### When to Cache

1. **Same DataFrame used with multiple Actions**
2. **Iterative algorithms** (machine learning, etc.)
3. **Interactive analysis** (repeated exploration of same data)

### When NOT to Cache

1. **DataFrame used only once**
2. **Memory constrained**
3. **Source data changes frequently**

## Important Considerations

### 1. Transformations Alone Don't Execute

```java
// Nothing executes!
Dataset<Row> result = df
    .filter(...)
    .groupBy(...)
    .agg(...);

// Action required
result.count();  // Now it executes
```

### 2. Cannot Modify Driver Variables in foreach

```java
// Wrong code - doesn't work!
int[] counter = {0};
df.foreach(row -> counter[0]++);  // Runs on Executor
System.out.println(counter[0]);   // Always prints 0

// Correct approach
long count = df.count();          // Use Action
```

### 3. collect is Dangerous with Large Data

```java
// Dangerous! - Bringing 1 billion rows to Driver memory
List<Row> allRows = hugeDataFrame.collectAsList();  // OOM!

// Safe alternatives
hugeDataFrame.take(1000);                     // Only some
hugeDataFrame.show(100);                      // Just display
hugeDataFrame.write().parquet("output");      // Distributed save
```

### 4. Check Execution Plans

```java
// Check plan before execution
df.filter(...).groupBy(...).agg(...).explain();

// Output:
// == Physical Plan ==
// *(2) HashAggregate(...)
// +- Exchange hashpartitioning(...)  ← Shuffle!
//    +- *(1) HashAggregate(...)
//       +- *(1) Filter(...)
//          +- FileScan parquet(...)
```

## Practical Tips

### 1. Minimize Wide Transformations

```java
// Inefficient: Two Wide Transformations
df.groupBy("dept").agg(sum("salary"))
  .orderBy("dept");

// Efficient: Only sort when needed
df.groupBy("dept").agg(sum("salary"))
  .show();  // Remove orderBy if sorting not needed
```

### 2. Filter as Early as Possible

```java
// Inefficient: Filter after join
employees.join(departments, "dept_id")
         .filter(col("status").equalTo("ACTIVE"));

// Efficient: Filter before join (reduces shuffle data)
employees.filter(col("status").equalTo("ACTIVE"))
         .join(departments, "dept_id");
```

### 3. Select Only Needed Columns

```java
// Inefficient: Keep all columns
df.join(other, "id")
  .groupBy("category")
  .agg(sum("value"));

// Efficient: Select only needed columns
df.select("id", "category", "value")
  .join(other.select("id", "factor"), "id")
  .groupBy("category")
  .agg(sum("value"));
```

## Summary

| Aspect | Transformation | Action |
|--------|---------------|--------|
| Execution Timing | Lazy | Eager |
| Return Value | RDD/DataFrame | Value or void |
| DAG | Adds to | Triggers |
| Examples | map, filter, groupBy | count, show, collect |

## Next Steps

- [Partitioning and Shuffle](partitioning/) - Internal workings of Wide Transformations
- [Caching and Persistence](caching/) - Reusing intermediate results

## Related Documents

- [Architecture](architecture/) - Execution structure of Job, Stage, Task
- [RDD Basics](rdd/) - Low-level Transformation API
- [DataFrame and Dataset](dataframe-dataset/) - High-level data processing API
- [Performance Tuning](tuning/) - Execution plan optimization
- [Glossary](../appendix/glossary/) - Transformation, Action term definitions
