---
lastmod: "2026-01-14"
title: Spark Integration
weight: 4
---

{{% notice style="primary" title="TL;DR" %}}
- **Scala is Spark's native language**: Latest features supported first, most concise API
- **DataFrame**: SQL-style data processing, `$"column"` syntax for column references
- **Dataset[T]**: Type-safe data processing with Case Classes, compile-time error detection
- **Performance optimization**: Utilize broadcast joins, caching, Predicate Pushdown
- **Note**: Spark 3.5 only supports Scala 2.12/2.13 (Scala 3 not supported)
{{% /notice %}}

**Target Audience**: Scala developers learning large-scale data processing, Spark beginners

**Prerequisites**:
- Scala basic syntax and functional programming concepts
- sbt build tool usage
- SQL basics (recommended)

---

Learn how to use Apache Spark with Scala. Scala is Spark's native language, providing the richest API. Since Spark itself is written in Scala, new features are added to the Scala API first, and you can maximize the benefits of type safety and functional programming.

#### Why Use Spark with Scala?

Comparing implementation of the same Spark task in Java and Scala clearly shows Scala's advantages. Java code is verbose with much boilerplate, while Scala code is concise with clear intent.

**Java vs Scala Comparison**

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

**Advantages of Scala + Spark**

Key benefits of using Scala and Spark together. You can access the latest features first through the native API and enable type-safe data processing using Case Classes.

| Advantage | Description |
|-----------|-------------|
| **Native API** | Spark is written in Scala, latest features supported first |
| **Type Safety** | Compile-time type checking with Dataset API |
| **Case Class Integration** | Auto schema inference, type-safe data processing |
| **Functional Style** | Natural use of map, filter, reduce, etc. |
| **REPL Support** | Interactive development with spark-shell |

{{% notice style="tip" title="Key Points" %}}
- Scala is Spark's native language with latest features supported first
- Much more concise code than Java (`$"column"` syntax, etc.)
- Leverage type-safe Dataset API with Case Classes
{{% /notice %}}

#### Environment Setup

To start a Spark project, first add Spark dependencies to build.sbt. Currently Spark 3.5 supports Scala 2.12 and 2.13, with Scala 3 not yet supported.

**build.sbt**

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

**project/build.properties**

```properties
sbt.version=1.10.6
```

{{% notice style="tip" title="Key Points" %}}
- Spark 3.5 **only supports Scala 2.12/2.13** (Scala 3 not supported)
- Must add `spark-core` and `spark-sql` dependencies
- Specify sbt version in `project/build.properties`
{{% /notice %}}

#### Basic Example: DataFrame Processing

Spark's core entry point is SparkSession. Through SparkSession, you can read data, create DataFrames, and execute SQL queries.

**Creating SparkSession**

When creating SparkSession, specify application name and execution mode. For local development, use `local[*]` to utilize all CPU cores.

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
    // +-------+-----------+------+
    // |   name| department|salary|
    // +-------+-----------+------+
    // |  Alice|Engineering| 75000|
    // |    Bob|Engineering| 80000|
    // |Charlie|      Sales| 65000|
    // |  Diana|      Sales| 70000|
    // |    Eve|  Marketing| 60000|
    // +-------+-----------+------+

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

In this example, importing `spark.implicits._` enables referencing columns with `$"column_name"` syntax. This leverages Scala's string interpolation and implicit conversions.

{{% notice style="tip" title="Key Points" %}}
- **SparkSession**: Spark's entry point, created with `builder()` pattern
- **spark.implicits._**: Enables `$"column"` syntax and `toDF()`
- **local[\*]**: Use all CPU cores in local mode
- Process data with `filter`, `select`, `groupBy`, `agg`, `orderBy`
{{% /notice %}}

#### Case Class and Dataset

Leveraging Scala Case Classes, you can **catch type errors at compile time**. DataFrames discover column name errors at runtime, but Datasets discover field name errors at compile time.

**Type-Safe Data Processing**

Defining schema with Case Class allows Spark to automatically infer the schema, enabling type-safe operations.

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
  // +-----+-----------+------+----------+
  // | name| department|salary|  joinDate|
  // +-----+-----------+------+----------+
  // |Alice|Engineering| 75000|2020-01-15|
  // |  Bob|Engineering| 80000|2019-03-20|
  // +-----+-----------+------+----------+

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

**DataFrame vs Dataset Comparison**

In DataFrames, column name typos are only discovered at runtime, but in Datasets, they're caught immediately at compile time.

```scala
// DataFrame: Runtime error possible
val df = employees.toDF()
df.filter($"salry" > 70000)  // Typo! Only discovered at runtime

// Dataset: Compile-time error
val ds: Dataset[Employee] = employees
ds.filter(_.salry > 70000)   // Compile error! Immediately caught
//            ^^^^^ value salry is not a member of Employee
```

{{% notice style="tip" title="Key Points" %}}
- **Dataset[T]**: Type-safe data processing with Case Classes
- **DataFrame**: Runtime errors possible, **Dataset**: Compile-time error detection
- **toDS()**: Convert Seq to Dataset
- **groupByKey + mapGroups**: Type-safe grouping and aggregation
{{% /notice %}}

#### Leveraging Functional Style

Scala's functional programming features work well with Spark. Concepts like higher-order functions, pattern matching, and immutable data naturally apply to distributed data processing.

**Data Transformation with Higher-Order Functions**

```scala
import org.apache.spark.sql.functions._

object FunctionalSparkExample extends App {
  val spark = SparkSession.builder()
    .appName("Functional Spark")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  case class Order(
    orderId: String,
    customerId: String,
    amount: Double,
    status: String
  )

  val orders = Seq(
    Order("O001", "C1", 150.0, "COMPLETED"),
    Order("O002", "C2", 200.0, "PENDING"),
    Order("O003", "C1", 75.0, "COMPLETED"),
    Order("O004", "C3", 300.0, "CANCELLED"),
    Order("O005", "C2", 180.0, "COMPLETED")
  ).toDS()

  // 1. Functional chaining
  val result = orders
    .filter(_.status == "COMPLETED")
    .map(o => (o.customerId, o.amount))
    .groupByKey(_._1)
    .mapValues(_._2)
    .reduceGroups(_ + _)
    .map { case (customerId, totalAmount) =>
      (customerId, totalAmount)
    }
    .toDF("customer_id", "total_amount")

  result.show()
  // +-----------+------------+
  // |customer_id|total_amount|
  // +-----------+------------+
  // |         C1|       225.0|
  // |         C2|       180.0|
  // +-----------+------------+

  // 2. Define UDF (User Defined Function)
  val categorizeAmount = udf((amount: Double) => amount match {
    case a if a >= 200 => "HIGH"
    case a if a >= 100 => "MEDIUM"
    case _ => "LOW"
  })

  orders.toDF()
    .withColumn("category", categorizeAmount($"amount"))
    .show()

  // 3. Leverage pattern matching
  val statusCounts = orders
    .map { order =>
      order.status match {
        case "COMPLETED" => ("completed", 1)
        case "PENDING"   => ("pending", 1)
        case "CANCELLED" => ("cancelled", 1)
        case _           => ("unknown", 1)
      }
    }
    .groupByKey(_._1)
    .mapValues(_._2)
    .reduceGroups(_ + _)

  statusCounts.show()

  spark.stop()
}
```

UDF (User Defined Function) allows using Scala functions in Spark SQL. Leveraging pattern matching enables clear expression of data classification logic.

{{% notice style="tip" title="Key Points" %}}
- **Functional chaining**: Connect `filter` → `map` → `groupByKey` → `reduceGroups`
- **UDF**: Convert Scala functions to be usable in Spark SQL
- **Pattern matching**: Clearly express data classification logic
{{% /notice %}}

#### Practical Example: ETL Pipeline

Implement ETL (Extract, Transform, Load) pipeline commonly used in actual data engineering. Cover the entire process of reading log data, transforming it, and storing analysis results.

**Read, Transform, Save Data**

```scala
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

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

  // 2. Generate sample data (normally read from file)
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

In this ETL pipeline, use Option type to safely handle nullable fields and classify session types with pattern matching. Final results are saved in Parquet format for use in subsequent analysis.

{{% notice style="tip" title="Key Points" %}}
- **Option[T]**: Type-safe handling of nullable fields
- **getOrElse**: Apply default value to missing values
- **partitionBy**: Date-based partitioning improves query performance
- **Parquet**: Column-based format optimized for analysis
{{% /notice %}}

#### Spark SQL and Scala

Using Spark SQL, you can freely mix SQL queries and Scala API. Write complex joins or aggregations in SQL, then further process results with Scala.

**Mixing SQL and Scala API**

```scala
object SparkSQLExample extends App {
  val spark = SparkSession.builder()
    .appName("Spark SQL")
    .master("local[*]")
    .getOrCreate()

  import spark.implicits._

  case class Product(id: Int, name: String, category: String, price: Double)
  case class Sale(productId: Int, quantity: Int, date: String)

  val products = Seq(
    Product(1, "Laptop", "Electronics", 1200.0),
    Product(2, "Phone", "Electronics", 800.0),
    Product(3, "Desk", "Furniture", 350.0),
    Product(4, "Chair", "Furniture", 150.0)
  ).toDS()

  val sales = Seq(
    Sale(1, 5, "2024-01-15"),
    Sale(2, 10, "2024-01-15"),
    Sale(1, 3, "2024-01-16"),
    Sale(3, 7, "2024-01-16"),
    Sale(4, 15, "2024-01-16")
  ).toDS()

  // 1. Register temporary views
  products.createOrReplaceTempView("products")
  sales.createOrReplaceTempView("sales")

  // 2. Execute SQL query
  val revenueByCategory = spark.sql("""
    SELECT
      p.category,
      SUM(p.price * s.quantity) as total_revenue,
      SUM(s.quantity) as total_quantity
    FROM products p
    JOIN sales s ON p.id = s.productId
    GROUP BY p.category
    ORDER BY total_revenue DESC
  """)

  revenueByCategory.show()
  // +-----------+-------------+--------------+
  // |   category|total_revenue|total_quantity|
  // +-----------+-------------+--------------+
  // |Electronics|      17600.0|            18|
  // |  Furniture|       4700.0|            22|
  // +-----------+-------------+--------------+

  // 3. Further process SQL results with Scala
  val topCategory = revenueByCategory
    .as[(String, Double, Long)]
    .head()

  println(s"Top revenue category: ${topCategory._1} (${topCategory._2})")

  spark.stop()
}
```

{{% notice style="tip" title="Key Points" %}}
- **createOrReplaceTempView**: Reference DataFrame as table in SQL
- **spark.sql()**: Execute SQL query and return DataFrame
- **Mix SQL + Scala API**: Complex joins in SQL, additional processing in Scala
{{% /notice %}}

#### Performance Optimization Tips

Major techniques for optimizing Spark application performance. Properly utilizing partitioning, broadcast joins, caching, and Predicate Pushdown can significantly improve performance.

**1. Partitioning Optimization**

Adjusting shuffle partition count to match data size improves performance.

```scala
// Adjust shuffle partitions
spark.conf.set("spark.sql.shuffle.partitions", "200")

// Repartition based on data size
val optimized = largeDataset
  .repartition(100, $"key_column")  // Key-based partitioning
```

**2. Broadcast Join**

Broadcasting small tables avoids shuffles.

```scala
import org.apache.spark.sql.functions.broadcast

// Broadcast small table
val result = largeDf.join(
  broadcast(smallDf),  // Broadcast small table
  largeDf("id") === smallDf("id")
)
```

**3. Caching Strategy**

Cache repeatedly used datasets to avoid recomputation.

```scala
// Cache repeatedly used datasets
val cachedDf = expensiveComputation.cache()

// Memory + disk caching
import org.apache.spark.storage.StorageLevel
expensiveComputation.persist(StorageLevel.MEMORY_AND_DISK)

// Release after use
cachedDf.unpersist()
```

**4. Predicate Pushdown**

Push filter conditions down to data source level to prevent reading unnecessary data.

```scala
// Filter pushdown when reading files
val filtered = spark.read
  .parquet("/data/logs")
  .filter($"date" === "2024-01-15")  // Partition pruning occurs
  .filter($"status" === "ERROR")     // Filter pushdown
```

{{% notice style="tip" title="Key Points" %}}
- **Partitioning**: Optimize shuffles with `repartition(n, $"key")`
- **Broadcast join**: Replicate small tables to all nodes to prevent shuffles
- **Caching**: Keep repeatedly used data in memory with `cache()` or `persist()`
- **Predicate Pushdown**: Push filter conditions down to data source level
{{% /notice %}}

#### Troubleshooting

Common errors and solutions during Spark development.

**Common Errors and Solutions**

| Error | Cause | Solution |
|-------|-------|----------|
| `Task not serializable` | Non-serializable object in closure | Create object inside closure or use `@transient` |
| `OutOfMemoryError` | Insufficient driver/executor memory | Increase `spark.driver.memory`, `spark.executor.memory` |
| `Container killed by YARN` | Memory exceeded | Increase `spark.yarn.executor.memoryOverhead` |
| `shuffle read/write timeout` | Network issues | Increase `spark.network.timeout` |

**Task not serializable Solution**

This error occurs when closure references non-serializable objects. Two solutions exist.

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

{{% notice style="tip" title="Key Points" %}}
- **Task not serializable**: Capture only primitives in closure or use `@transient`
- **OutOfMemoryError**: Increase `spark.driver.memory`, `spark.executor.memory`
- **Memory issues**: Allow disk spill with `StorageLevel.MEMORY_AND_DISK`
{{% /notice %}}

#### How to Run

Various methods for running Spark applications.

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

For local development, use sbt run or spark-shell. For cluster deployment, use spark-submit.

{{% notice style="tip" title="Key Points" %}}
- **sbt run**: Quick execution for local development
- **spark-shell**: Interactive development and exploratory analysis
- **spark-submit**: Use for cluster deployment, requires JAR packaging
{{% /notice %}}

#### Next Steps

After learning basic Spark usage, continue learning with these topics.

- [Spark Guide]({{< relref "/docs/spark" >}}) - Deep dive into Spark
- [Kafka Integration]({{< relref "/docs/kafka" >}}) - Structured Streaming + Kafka
- [Functional Patterns](../concepts/functional-patterns/) - Functional programming in Spark
