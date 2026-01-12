---
title: Structured Streaming
weight: 8
lastmod: "2026-01-07"
---

# Structured Streaming

Structured Streaming is Spark's stream processing engine. It processes real-time data using the same DataFrame/Dataset API as batch processing.

## What is Structured Streaming?

**Structured Streaming** treats stream data as an infinitely appending table. When new data arrives, it performs incremental processing.

### Core Concepts

```
Input Stream (Infinite Table)
    ↓
[Data Arrival] → [Incremental Processing] → [Result Update]
    ↓
Output (Continuously Updated Result Table)
```

### Batch vs Streaming Code Comparison

```java
// Batch processing (static data)
Dataset<Row> batchDf = spark.read()
        .json("input/data.json");

Dataset<Row> result = batchDf
        .groupBy("category")
        .count();

result.write().parquet("output");

// Stream processing (dynamic data) - almost identical!
Dataset<Row> streamDf = spark.readStream()  // Changed to readStream
        .json("input/");

Dataset<Row> result = streamDf              // Same processing logic
        .groupBy("category")
        .count();

result.writeStream()                        // Changed to writeStream
        .outputMode("complete")
        .format("console")
        .start()
        .awaitTermination();
```

## Basic Usage

### Reading Streams

```java
SparkSession spark = SparkSession.builder()
        .appName("Structured Streaming")
        .master("local[*]")
        .getOrCreate();

// File source (directory monitoring)
Dataset<Row> fileStream = spark.readStream()
        .schema(schema)  // Schema required for streams
        .json("input/");

// Kafka source
Dataset<Row> kafkaStream = spark.readStream()
        .format("kafka")
        .option("kafka.bootstrap.servers", "localhost:9092")
        .option("subscribe", "topic-name")
        .load();

// Socket source (for testing)
Dataset<Row> socketStream = spark.readStream()
        .format("socket")
        .option("host", "localhost")
        .option("port", 9999)
        .load();

// Rate source (for testing - generates specified rows per second)
Dataset<Row> rateStream = spark.readStream()
        .format("rate")
        .option("rowsPerSecond", 10)
        .load();
```

### Processing Streams

Apply the same operations as regular DataFrames to stream DataFrames:

```java
// Filtering
Dataset<Row> filtered = stream.filter(col("value").gt(100));

// Transformation
Dataset<Row> transformed = stream
        .withColumn("timestamp", current_timestamp())
        .withColumn("doubled", col("value").multiply(2));

// Aggregation (stateful)
Dataset<Row> aggregated = stream
        .groupBy("category")
        .agg(
            count("*").alias("count"),
            sum("value").alias("total")
        );
```

### Writing Streams

```java
StreamingQuery query = result.writeStream()
        .outputMode("append")           // Output mode
        .format("parquet")              // Output format
        .option("path", "output/")      // Output path
        .option("checkpointLocation", "checkpoint/")  // Checkpoint required
        .trigger(Trigger.ProcessingTime("10 seconds"))  // Trigger
        .start();

// Wait for query termination
query.awaitTermination();

// Or run in background
// Manage with query.isActive(), query.stop() etc.
```

## Output Mode

| Mode | Description | When to Use |
|------|-------------|-------------|
| **append** | Output only new rows | Simple transformations without aggregation |
| **complete** | Output entire result table | Aggregation queries |
| **update** | Output only changed rows | Aggregation queries (some sinks only) |

```java
// append - new rows only (without aggregation)
filtered.writeStream()
    .outputMode("append")
    .format("parquet")
    .start();

// complete - entire result (with aggregation)
aggregated.writeStream()
    .outputMode("complete")
    .format("console")
    .start();

// update - changed only (with aggregation)
aggregated.writeStream()
    .outputMode("update")
    .format("console")
    .start();
```

## Trigger

Controls data processing frequency.

```java
import org.apache.spark.sql.streaming.Trigger;

// Default - as fast as possible (micro-batch)
.trigger(Trigger.ProcessingTime(0))

// Specified interval
.trigger(Trigger.ProcessingTime("10 seconds"))
.trigger(Trigger.ProcessingTime("1 minute"))

// Run once (like batch)
.trigger(Trigger.Once())

// Process all available data then terminate
.trigger(Trigger.AvailableNow())

// Continuous processing (millisecond latency, experimental)
.trigger(Trigger.Continuous("1 second"))
```

## Kafka Integration

### Reading from Kafka

```java
Dataset<Row> kafkaStream = spark.readStream()
        .format("kafka")
        .option("kafka.bootstrap.servers", "localhost:9092")
        .option("subscribe", "my-topic")  // Single topic
        // .option("subscribePattern", "topic.*")  // Pattern
        // .option("assign", "{\"topic\":[0,1,2]}")  // Specific partitions
        .option("startingOffsets", "earliest")  // earliest, latest, or JSON
        .load();

// Kafka message structure:
// key (binary), value (binary), topic, partition, offset, timestamp, ...

// Parse value
Dataset<Row> parsed = kafkaStream
        .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
        .select(from_json(col("value"), schema).alias("data"))
        .select("data.*");
```

### Writing to Kafka

```java
// Send results to Kafka
result
    .selectExpr("CAST(key AS STRING)", "to_json(struct(*)) AS value")
    .writeStream()
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("topic", "output-topic")
    .option("checkpointLocation", "checkpoint/kafka-output")
    .start();
```

## Window Operations

Window functions for time-based aggregation.

### Tumbling Window

Non-overlapping fixed-size windows:

```java
Dataset<Row> windowedCounts = stream
    .withWatermark("timestamp", "10 minutes")
    .groupBy(
        window(col("timestamp"), "5 minutes"),  // 5-minute window
        col("category")
    )
    .count();

// Result:
// +------------------------------------------+--------+-----+
// |window                                    |category|count|
// +------------------------------------------+--------+-----+
// |{2024-01-01 10:00:00, 2024-01-01 10:05:00}|A       |  150|
// |{2024-01-01 10:05:00, 2024-01-01 10:10:00}|A       |  180|
// +------------------------------------------+--------+-----+
```

### Sliding Window

Overlapping windows:

```java
Dataset<Row> slidingCounts = stream
    .withWatermark("timestamp", "10 minutes")
    .groupBy(
        window(col("timestamp"), "10 minutes", "5 minutes"),  // 10-min window, 5-min slide
        col("category")
    )
    .count();

// Each event can be included in 2 windows
```

### Session Window

Activity-based dynamic windows:

```java
// Spark 3.2+
Dataset<Row> sessionCounts = stream
    .withWatermark("timestamp", "10 minutes")
    .groupBy(
        session_window(col("timestamp"), "5 minutes"),  // Session ends after 5-min inactivity
        col("user_id")
    )
    .count();
```

## Watermark

A mechanism for handling late-arriving data.

```java
Dataset<Row> result = stream
    .withWatermark("eventTime", "10 minutes")  // Allow up to 10 minutes delay
    .groupBy(
        window(col("eventTime"), "5 minutes"),
        col("category")
    )
    .count();

// Watermark = max(eventTime) - 10 minutes
// Data older than watermark is ignored
```

### Watermark Behavior

```
Event time order:
10:00 → 10:05 → 10:03 (late) → 10:10 → 09:55 (very late)

Watermark = 10:00 (10 minutes delay allowed)
- 10:03 event: After watermark (10:00) → Processed
- 09:55 event: Before watermark (10:00) → Ignored
```

## State Management

Aggregation queries maintain state.

### State Store Configuration

```java
// Use RocksDB state store (suitable for large state)
spark.conf().set(
    "spark.sql.streaming.stateStore.providerClass",
    "org.apache.spark.sql.execution.streaming.state.RocksDBStateStoreProvider"
);

// State store memory settings
spark.conf().set("spark.sql.streaming.stateStore.rocksdb.memory.mb", "256");
```

### State Timeout

```java
// Set timeout for group state (mapGroupsWithState/flatMapGroupsWithState)
.groupByKey(row -> row.getString(0), Encoders.STRING())
.mapGroupsWithState(
    mappingFunc,
    Encoders.bean(State.class),
    Encoders.bean(Output.class),
    GroupStateTimeout.ProcessingTimeTimeout()  // Or EventTimeTimeout
);
```

## Joins

### Stream-Static Join

Join stream with static data:

```java
Dataset<Row> staticDf = spark.read().parquet("dimension-data");

Dataset<Row> enriched = stream.join(
    staticDf,
    stream.col("product_id").equalTo(staticDf.col("id")),
    "left"
);
```

### Stream-Stream Join

Join two streams (watermark required):

```java
Dataset<Row> impressions = spark.readStream()
    .format("kafka")
    .option("subscribe", "impressions")
    .load()
    .withWatermark("timestamp", "2 hours");

Dataset<Row> clicks = spark.readStream()
    .format("kafka")
    .option("subscribe", "clicks")
    .load()
    .withWatermark("timestamp", "3 hours");

// Time range join condition
Dataset<Row> joined = impressions.join(
    clicks,
    expr("""
        impressionId = clickImpressionId AND
        clickTime >= impressionTime AND
        clickTime <= impressionTime + interval 1 hour
    """),
    "leftOuter"
);
```

## Query Monitoring

```java
StreamingQuery query = result.writeStream()
    .format("console")
    .start();

// Check query status
System.out.println("Query ID: " + query.id());
System.out.println("Run ID: " + query.runId());
System.out.println("Is Active: " + query.isActive());
System.out.println("Status: " + query.status());

// Progress
StreamingQueryProgress progress = query.lastProgress();
if (progress != null) {
    System.out.println("Input rows: " + progress.numInputRows());
    System.out.println("Processing rate: " + progress.processedRowsPerSecond());
}

// Stop query
query.stop();
```

### Query Listener

```java
spark.streams().addListener(new StreamingQueryListener() {
    @Override
    public void onQueryStarted(QueryStartedEvent event) {
        System.out.println("Query started: " + event.id());
    }

    @Override
    public void onQueryProgress(QueryProgressEvent event) {
        System.out.println("Progress: " + event.progress().numInputRows() + " rows");
    }

    @Override
    public void onQueryTerminated(QueryTerminatedEvent event) {
        System.out.println("Query terminated: " + event.id());
    }
});
```

## Practical Example: Real-Time Sales Aggregation

```java
public class RealTimeSalesAggregation {
    public static void main(String[] args) throws Exception {
        SparkSession spark = SparkSession.builder()
                .appName("Real-Time Sales")
                .master("local[*]")
                .getOrCreate();

        // Define schema
        StructType schema = new StructType()
                .add("orderId", StringType, false)
                .add("productId", StringType, false)
                .add("category", StringType, false)
                .add("amount", DoubleType, false)
                .add("timestamp", TimestampType, false);

        // Read order data from Kafka
        Dataset<Row> orders = spark.readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("subscribe", "orders")
                .option("startingOffsets", "latest")
                .load()
                .selectExpr("CAST(value AS STRING)")
                .select(from_json(col("value"), schema).alias("order"))
                .select("order.*")
                .withWatermark("timestamp", "5 minutes");

        // Aggregate sales by category per 5-minute window
        Dataset<Row> salesByCategory = orders
                .groupBy(
                    window(col("timestamp"), "5 minutes"),
                    col("category")
                )
                .agg(
                    count("*").alias("orderCount"),
                    sum("amount").alias("totalSales"),
                    avg("amount").alias("avgOrderValue")
                );

        // Console output (for debugging)
        StreamingQuery consoleQuery = salesByCategory.writeStream()
                .outputMode("update")
                .format("console")
                .option("truncate", false)
                .trigger(Trigger.ProcessingTime("30 seconds"))
                .start();

        // Send results to Kafka
        StreamingQuery kafkaQuery = salesByCategory
                .selectExpr("to_json(struct(*)) AS value")
                .writeStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("topic", "sales-summary")
                .option("checkpointLocation", "checkpoint/sales-summary")
                .outputMode("update")
                .start();

        spark.streams().awaitAnyTermination();
    }
}
```

## Next Steps

- [MLlib](../mllib/) - Machine learning with Spark
- [Performance Tuning](../tuning/) - Streaming performance optimization
