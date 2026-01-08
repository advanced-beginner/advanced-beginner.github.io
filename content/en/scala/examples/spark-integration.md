---
lastmod: "2026-01-08"
title: Spark Integration
weight: 4
---

Learn how to use Apache Spark with Scala. Scala is Spark's native language, providing the richest API.

## Why Use Spark with Scala?

### Java vs Scala Comparison

```java
// Java: Verbose code
Dataset<Row> result = spark.read()
    .option("header", "true")
    .csv("data.csv")
    .filter(col("age").gt(30))
    .groupBy(col("department"))
    .agg(avg(col("salary")).alias("avg_salary"));
```

```scala
// Scala: Concise and expressive code
val result = spark.read
  .option("header", "true")
  .csv("data.csv")
  .filter($"age" > 30)
  .groupBy($"department")
  .agg(avg($"salary").as("avg_salary"))
```

### Advantages of Scala + Spark

| Advantage | Description |
|-----------|-------------|
| **Native API** | Spark is written in Scala, latest features supported first |
| **Type Safety** | Compile-time type checking with Dataset API |
| **Case Class Integration** | Auto schema inference, type-safe data processing |
| **Functional Style** | Natural use of map, filter, reduce, etc. |
| **REPL Support** | Interactive development with spark-shell |

---

## Environment Setup

### build.sbt

```scala
ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "spark-scala-example",
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.5.1",
      "org.apache.spark" %% "spark-sql"  % "3.5.1"
    )
  )
```

> **Note:** Spark 3.5 only supports Scala 2.12/2.13. Scala 3 is not yet supported.

---

## Basic Example: DataFrame Processing

### Creating SparkSession

```scala
import org.apache.spark.sql.SparkSession

object SparkBasics extends App {
  // Create SparkSession
  val spark = SparkSession.builder()
    .appName("Scala Spark Example")
    .master("local[*]")  // Local mode, use all CPUs
    .getOrCreate()

  // Import implicits for $ syntax
  import spark.implicits._

  // Adjust log level
  spark.sparkContext.setLogLevel("WARN")

  // Run example
  basicDataFrameOps()

  spark.stop()

  def basicDataFrameOps(): Unit = {
    // 1. Create DataFrame
    val data = Seq(
      ("Alice", "Engineering", 75000),
      ("Bob", "Engineering", 80000),
      ("Charlie", "Sales", 65000),
      ("Diana", "Sales", 70000),
      ("Eve", "Marketing", 60000)
    )

    val df = data.toDF("name", "department", "salary")
    df.show()

    // 2. Filtering and Selection
    df.filter($"salary" > 65000)
      .select($"name", $"salary")
      .show()

    // 3. Grouping and Aggregation
    df.groupBy($"department")
      .agg(
        avg($"salary").as("avg_salary"),
        max($"salary").as("max_salary"),
        count("*").as("employee_count")
      )
      .orderBy($"avg_salary".desc)
      .show()
  }
}
```

---

## Case Class and Dataset

### Type-Safe Data Processing

Using Scala Case Classes, you can **catch type errors at compile time**.

```scala
import org.apache.spark.sql.{Dataset, SparkSession}

// 1. Define Case Class (acts as schema)
case class Employee(
  name: String,
  department: String,
  salary: Int,
  joinDate: String
)

case class DepartmentStats(
  department: String,
  avgSalary: Double,
  employeeCount: Long
)

object TypeSafeExample extends App {
  val spark = SparkSession.builder()
    .appName("Type Safe Spark")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  // 2. Create Dataset[Employee]
  val employees: Dataset[Employee] = Seq(
    Employee("Alice", "Engineering", 75000, "2020-01-15"),
    Employee("Bob", "Engineering", 80000, "2019-03-20"),
    Employee("Charlie", "Sales", 65000, "2021-06-01"),
    Employee("Diana", "Sales", 70000, "2020-11-10"),
    Employee("Eve", "Marketing", 60000, "2022-02-28")
  ).toDS()

  // 3. Type-safe operations
  val highEarners: Dataset[Employee] = employees
    .filter(_.salary > 70000)  // Compile-time check!

  highEarners.show()

  // 4. Transform with map (type-safe)
  val names: Dataset[String] = employees.map(_.name)
  names.show()

  // 5. Aggregate with groupByKey
  val statsByDept: Dataset[DepartmentStats] = employees
    .groupByKey(_.department)
    .mapGroups { (dept, iter) =>
      val empList = iter.toList
      DepartmentStats(
        department = dept,
        avgSalary = empList.map(_.salary).sum.toDouble / empList.size,
        employeeCount = empList.size
      )
    }

  statsByDept.show()

  spark.stop()
}
```

### DataFrame vs Dataset Comparison

```scala
// DataFrame: Runtime error possible
val df = employees.toDF()
df.filter($"salry" > 70000)  // Typo! Only discovered at runtime

// Dataset: Compile-time error
val ds: Dataset[Employee] = employees
ds.filter(_.salry > 70000)   // Compile error! Immediately caught
//            ^^^^^ value salry is not a member of Employee
```

---

## Practical Example: ETL Pipeline

### Read, Transform, Save

```scala
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions._

object ETLPipeline extends App {
  val spark = SparkSession.builder()
    .appName("ETL Pipeline")
    .master("local[*]")
    .config("spark.sql.shuffle.partitions", "4")
    .getOrCreate()

  import spark.implicits._

  // 1. Define schema (type safety)
  case class RawLog(
    timestamp: String,
    userId: String,
    action: String,
    page: String,
    duration: Option[Int]  // Nullable field
  )

  case class ProcessedLog(
    date: String,
    hour: Int,
    userId: String,
    action: String,
    page: String,
    duration: Int,
    sessionType: String
  )

  // 2. Sample data (normally read from file)
  val rawLogs = Seq(
    RawLog("2024-01-15T10:30:00", "U001", "VIEW", "/home", Some(30)),
    RawLog("2024-01-15T10:31:00", "U001", "CLICK", "/products", Some(5)),
    RawLog("2024-01-15T10:32:00", "U002", "VIEW", "/home", None),
    RawLog("2024-01-15T11:00:00", "U001", "PURCHASE", "/checkout", Some(120)),
    RawLog("2024-01-15T11:05:00", "U003", "VIEW", "/home", Some(45))
  ).toDS()

  // 3. Transformation functions
  def extractDateTime(log: RawLog): (String, Int) = {
    val parts = log.timestamp.split("T")
    val date = parts(0)
    val hour = parts(1).split(":")(0).toInt
    (date, hour)
  }

  def categorizeSession(duration: Int): String = duration match {
    case d if d >= 60 => "LONG"
    case d if d >= 20 => "MEDIUM"
    case _ => "SHORT"
  }

  // 4. ETL Pipeline
  val processedLogs: Dataset[ProcessedLog] = rawLogs
    // Handle missing values
    .map { log =>
      val (date, hour) = extractDateTime(log)
      ProcessedLog(
        date = date,
        hour = hour,
        userId = log.userId,
        action = log.action,
        page = log.page,
        duration = log.duration.getOrElse(0),
        sessionType = categorizeSession(log.duration.getOrElse(0))
      )
    }
    // Filter
    .filter(_.duration > 0)

  processedLogs.show()

  // 5. Aggregation analysis
  val hourlyStats = processedLogs
    .groupBy($"date", $"hour")
    .agg(
      countDistinct($"userId").as("unique_users"),
      count("*").as("total_events"),
      avg($"duration").as("avg_duration")
    )
    .orderBy($"date", $"hour")

  hourlyStats.show()

  // 6. Save (Parquet format)
  processedLogs.write
    .mode(SaveMode.Overwrite)
    .partitionBy("date")
    .parquet("/tmp/processed_logs")

  println("ETL complete: /tmp/processed_logs")

  spark.stop()
}
```

---

## Performance Optimization Tips

### 1. Partitioning Optimization

```scala
// Adjust shuffle partitions
spark.conf.set("spark.sql.shuffle.partitions", "200")

// Repartition based on data size
val optimized = largeDataset
  .repartition(100, $"key_column")  // Key-based partitioning
```

### 2. Broadcast Join

```scala
import org.apache.spark.sql.functions.broadcast

// Broadcast small table
val result = largeDf.join(
  broadcast(smallDf),  // Broadcast small table
  largeDf("id") === smallDf("id")
)
```

### 3. Caching Strategy

```scala
// Cache repeatedly used datasets
val cachedDf = expensiveComputation.cache()

// Memory + disk caching
import org.apache.spark.storage.StorageLevel
expensiveComputation.persist(StorageLevel.MEMORY_AND_DISK)

// Release after use
cachedDf.unpersist()
```

---

## Troubleshooting

### Common Errors and Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| `Task not serializable` | Non-serializable object in closure | Create object inside closure or use `@transient` |
| `OutOfMemoryError` | Insufficient driver/executor memory | Increase `spark.driver.memory`, `spark.executor.memory` |
| `Container killed by YARN` | Memory exceeded | Increase `spark.yarn.executor.memoryOverhead` |

### Task not serializable Solution

```scala
// ❌ Error
class MyProcessor {
  val config = loadConfig()  // Not serializable

  def process(df: DataFrame): DataFrame = {
    df.filter($"value" > config.threshold)  // config in closure
  }
}

// ✅ Solution 1: Capture local variable
class MyProcessor {
  val config = loadConfig()

  def process(df: DataFrame): DataFrame = {
    val threshold = config.threshold  // Capture only primitive
    df.filter($"value" > threshold)
  }
}

// ✅ Solution 2: Use @transient
class MyProcessor extends Serializable {
  @transient lazy val config = loadConfig()

  def process(df: DataFrame): DataFrame = {
    df.filter($"value" > config.threshold)
  }
}
```

---

## How to Run

```bash
# 1. Run with sbt
sbt run

# 2. Run with spark-submit
sbt package
spark-submit \
  --class SparkBasics \
  --master local[*] \
  target/scala-2.13/spark-scala-example_2.13-0.1.jar

# 3. Interactive with spark-shell
spark-shell --master local[*]
```

---

## Next Steps

- [Spark Guide]({{< relref "/spark" >}}) - Deep dive into Spark
- [Kafka Integration]({{< relref "/kafka" >}}) - Structured Streaming + Kafka
- [Functional Patterns](../concepts/functional-patterns/) - FP in Spark
