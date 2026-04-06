---
title: DataFrame and Dataset
description: "DataFrame and Dataset mechanics and usage patterns"
weight: 3
lastmod: "2026-04-06"
---

# DataFrame and Dataset

DataFrame and Dataset are Spark's modern high-level APIs. They are easier to use than RDDs and provide automatic optimization through the Catalyst Optimizer.

## Concept Overview

### DataFrame

A **DataFrame** is a distributed data collection organized into named columns. It's similar to a relational database table or a DataFrame in Python/R.

```java
// DataFrame is an alias for Dataset<Row>
Dataset<Row> df = spark.read().json("employees.json");
```

### Dataset

A **Dataset** is a distributed data collection with a specific type. It provides compile-time type safety.

```java
// Encoder required when using Dataset in Java
public class Employee implements Serializable {
    private String name;
    private int age;
    // getters, setters...
}

Encoder<Employee> encoder = Encoders.bean(Employee.class);
Dataset<Employee> ds = spark.read().json("employees.json").as(encoder);
```

### Usage in Java

| Concept | Java Expression | Description |
|---------|-----------------|-------------|
| DataFrame | `Dataset<Row>` | Has schema but Row type |
| Dataset | `Dataset<T>` | Uses POJO as type parameter |
| Row | `org.apache.spark.sql.Row` | Schema-based generic row |

## Creating DataFrames

### 1. From Files

```java
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

SparkSession spark = SparkSession.builder()
        .appName("DataFrame Example")
        .master("local[*]")
        .getOrCreate();

// CSV
Dataset<Row> csvDf = spark.read()
        .option("header", "true")
        .option("inferSchema", "true")
        .csv("data.csv");

// JSON
Dataset<Row> jsonDf = spark.read().json("data.json");

// Parquet (recommended format)
Dataset<Row> parquetDf = spark.read().parquet("data.parquet");

// JDBC
Dataset<Row> jdbcDf = spark.read()
        .format("jdbc")
        .option("url", "jdbc:mysql://localhost:3306/mydb")
        .option("dbtable", "employees")
        .option("user", "user")
        .option("password", "pass")
        .load();
```

### 2. Programmatically

```java
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.*;

import java.util.Arrays;
import java.util.List;

// Prepare data
List<Row> data = Arrays.asList(
    RowFactory.create("Alice", 30, "Engineering"),
    RowFactory.create("Bob", 25, "Marketing"),
    RowFactory.create("Charlie", 35, "Engineering")
);

// Define schema
StructType schema = new StructType()
        .add("name", DataTypes.StringType, false)
        .add("age", DataTypes.IntegerType, false)
        .add("department", DataTypes.StringType, true);

// Create DataFrame
Dataset<Row> df = spark.createDataFrame(data, schema);

df.show();
// +-------+---+-----------+
// |   name|age| department|
// +-------+---+-----------+
// |  Alice| 30|Engineering|
// |    Bob| 25|  Marketing|
// |Charlie| 35|Engineering|
// +-------+---+-----------+
```

### 3. From POJOs

```java
import org.apache.spark.sql.Encoders;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

// Define POJO (must follow JavaBean conventions)
public class Employee implements Serializable {
    private String name;
    private int age;
    private String department;

    // Default constructor required
    public Employee() {}

    public Employee(String name, int age, String department) {
        this.name = name;
        this.age = age;
        this.department = department;
    }

    // Getter/Setter required
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}

// Create Dataset
List<Employee> employees = Arrays.asList(
    new Employee("Alice", 30, "Engineering"),
    new Employee("Bob", 25, "Marketing")
);

Dataset<Employee> ds = spark.createDataset(employees, Encoders.bean(Employee.class));
ds.show();
```

## Basic Operations

### Schema Inspection

```java
// Print schema
df.printSchema();
// root
//  |-- name: string (nullable = false)
//  |-- age: integer (nullable = false)
//  |-- department: string (nullable = true)

// Column list
String[] columns = df.columns();

// Check data types
StructType schema = df.schema();
```

### Data Inspection

```java
// Print top n rows
df.show();       // Default 20 rows
df.show(10);     // 10 rows
df.show(false);  // No string truncation

// First row
Row first = df.first();

// Top n rows as array
Row[] rows = (Row[]) df.take(5);

// Statistical summary
df.describe("age", "salary").show();
// +-------+------------------+------------------+
// |summary|               age|            salary|
// +-------+------------------+------------------+
// |  count|                 3|                 3|
// |   mean|              30.0|           50000.0|
// | stddev|               5.0|           10000.0|
// |    min|                25|             40000|
// |    max|                35|             60000|
// +-------+------------------+------------------+
```

### Select (Column Selection)

```java
import static org.apache.spark.sql.functions.*;

// Select columns
df.select("name", "age").show();

// Using Column objects
df.select(col("name"), col("age")).show();

// Column operations
df.select(
    col("name"),
    col("age"),
    col("age").plus(10).alias("age_plus_10"),
    expr("age * 2").alias("age_doubled")
).show();

// All columns + new column
df.select(
    col("*"),
    lit("USA").alias("country")
).show();
```

### Filter (Conditional Filtering)

```java
// String condition
df.filter("age > 25").show();

// Column condition
df.filter(col("age").gt(25)).show();
df.filter(col("age").geq(25).and(col("department").equalTo("Engineering"))).show();

// where is same as filter
df.where(col("age").gt(25)).show();

// Complex conditions
df.filter(
    col("age").between(25, 35)
    .and(col("department").isin("Engineering", "Marketing"))
).show();

// null check
df.filter(col("department").isNotNull()).show();

// String conditions
df.filter(col("name").startsWith("A")).show();
df.filter(col("name").contains("li")).show();
df.filter(col("name").rlike("^A.*e$")).show();  // regex
```

### Add/Modify/Delete Columns

```java
// Add new column
Dataset<Row> withBonus = df.withColumn("bonus", col("salary").multiply(0.1));

// Rename column
Dataset<Row> renamed = df.withColumnRenamed("name", "employee_name");

// Add multiple columns
Dataset<Row> enhanced = df
    .withColumn("bonus", col("salary").multiply(0.1))
    .withColumn("total", col("salary").plus(col("bonus")));

// Drop column
Dataset<Row> dropped = df.drop("department");
Dataset<Row> droppedMultiple = df.drop("department", "age");

// Cast column type
Dataset<Row> casted = df.withColumn("age", col("age").cast(DataTypes.DoubleType));
```

### Sorting

```java
// Ascending sort
df.orderBy("age").show();
df.orderBy(col("age")).show();
df.sort("age").show();

// Descending sort
df.orderBy(col("age").desc()).show();

// Multi-column sort
df.orderBy(col("department").asc(), col("age").desc()).show();

// null handling
df.orderBy(col("age").asc_nulls_first()).show();
df.orderBy(col("age").desc_nulls_last()).show();
```

## Aggregation Operations

### groupBy

```java
// Single column grouping
df.groupBy("department").count().show();

// Multiple aggregation functions
df.groupBy("department")
    .agg(
        count("*").alias("count"),
        avg("age").alias("avg_age"),
        max("salary").alias("max_salary"),
        min("salary").alias("min_salary"),
        sum("salary").alias("total_salary")
    )
    .show();

// Group by multiple columns
df.groupBy("department", "level")
    .agg(avg("salary").alias("avg_salary"))
    .orderBy("department", "level")
    .show();
```

### Aggregation Functions

```java
import static org.apache.spark.sql.functions.*;

df.agg(
    count("*"),                      // row count
    countDistinct("department"),     // unique value count
    sum("salary"),                   // sum
    avg("salary"),                   // average
    mean("salary"),                  // average (same as avg)
    max("salary"),                   // maximum
    min("salary"),                   // minimum
    stddev("salary"),                // standard deviation
    variance("salary"),              // variance
    first("name"),                   // first value
    last("name"),                    // last value
    collect_list("department"),      // collect as list
    collect_set("department")        // collect with deduplication
).show();
```

### Window Functions

{{< callout type="info" title="Advanced Topic" >}}
Window functions are an advanced topic. You can skip this on first reading and come back when you need complex aggregations or ranking calculations.
{{< /callout >}}

```java
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;

// Define Window
WindowSpec window = Window
    .partitionBy("department")
    .orderBy(col("salary").desc());

// Ranking functions
df.withColumn("rank", rank().over(window))
  .withColumn("dense_rank", dense_rank().over(window))
  .withColumn("row_number", row_number().over(window))
  .show();

// Aggregate Window
WindowSpec windowAgg = Window.partitionBy("department");

df.withColumn("dept_avg_salary", avg("salary").over(windowAgg))
  .withColumn("salary_diff", col("salary").minus(col("dept_avg_salary")))
  .show();

// Previous/next values
df.withColumn("prev_salary", lag("salary", 1).over(window))
  .withColumn("next_salary", lead("salary", 1).over(window))
  .show();

// Running total
WindowSpec runningWindow = Window
    .partitionBy("department")
    .orderBy("hire_date")
    .rowsBetween(Window.unboundedPreceding(), Window.currentRow());

df.withColumn("running_total", sum("salary").over(runningWindow)).show();
```

## Joins

```java
Dataset<Row> employees = spark.read().json("employees.json");
Dataset<Row> departments = spark.read().json("departments.json");

// Inner Join (default)
Dataset<Row> joined = employees.join(departments, "department_id");

// Explicit condition
Dataset<Row> joined2 = employees.join(
    departments,
    employees.col("department_id").equalTo(departments.col("id"))
);

// Specify join type
employees.join(departments, col("department_id").equalTo(col("id")), "inner");
employees.join(departments, col("department_id").equalTo(col("id")), "left");
employees.join(departments, col("department_id").equalTo(col("id")), "right");
employees.join(departments, col("department_id").equalTo(col("id")), "full");
employees.join(departments, col("department_id").equalTo(col("id")), "left_semi");
employees.join(departments, col("department_id").equalTo(col("id")), "left_anti");

// Cross Join (all combinations)
employees.crossJoin(departments);
```

### Join Optimization

```java
import static org.apache.spark.sql.functions.broadcast;

// Broadcast Join - distribute small table to all nodes
// Avoids shuffle when joining with small tables (tens of MB or less)
Dataset<Row> optimizedJoin = employees.join(
    broadcast(departments),
    "department_id"
);

// Set auto broadcast threshold (default 10MB)
spark.conf().set("spark.sql.autoBroadcastJoinThreshold", "50MB");
```

## Dataset (Type-Safe API)

Using Dataset in Java enables compile-time type checking.

```java
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;

// Define Encoder
Encoder<Employee> employeeEncoder = Encoders.bean(Employee.class);

// DataFrame → Dataset conversion
Dataset<Employee> employeeDs = df.as(employeeEncoder);

// Type-safe operations
Dataset<Employee> seniors = employeeDs.filter(
    (FilterFunction<Employee>) emp -> emp.getAge() > 30
);

// map operation
Dataset<String> names = employeeDs.map(
    (MapFunction<Employee, String>) Employee::getName,
    Encoders.STRING()
);

// flatMap operation
Dataset<String> words = employeeDs.flatMap(
    (FlatMapFunction<Employee, String>) emp ->
        Arrays.asList(emp.getName().split(" ")).iterator(),
    Encoders.STRING()
);

// reduce operation
Employee oldest = employeeDs.reduce(
    (ReduceFunction<Employee>) (e1, e2) ->
        e1.getAge() > e2.getAge() ? e1 : e2
);
```

### Encoder Types

```java
// Primitive types
Encoders.STRING()
Encoders.INT()
Encoders.LONG()
Encoders.DOUBLE()
Encoders.BOOLEAN()

// JavaBean
Encoders.bean(Employee.class)

// Tuples
Encoders.tuple(Encoders.STRING(), Encoders.INT())

// Kryo (generic, has serialization overhead)
Encoders.kryo(MyClass.class)
```

## Choosing Between DataFrame and Dataset

| Situation | Recommended API |
|-----------|----------------|
| SQL-style aggregation/transformation | DataFrame |
| Need compile-time type safety | Dataset |
| Complex business logic | Dataset |
| Dynamic schema | DataFrame |
| Compatibility with Python/R | DataFrame |
| Need best performance | DataFrame (Tungsten optimization) |

## Practical Example: Sales Analysis

```java
public class SalesAnalysis {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("Sales Analysis")
                .master("local[*]")
                .getOrCreate();

        // Load sales data
        Dataset<Row> sales = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("sales.csv");

        // Columns: date, product, category, quantity, price

        // 1. Calculate total revenue
        Dataset<Row> withRevenue = sales.withColumn(
            "revenue",
            col("quantity").multiply(col("price"))
        );

        // 2. Aggregate revenue by category
        Dataset<Row> categoryRevenue = withRevenue
            .groupBy("category")
            .agg(
                sum("revenue").alias("total_revenue"),
                avg("revenue").alias("avg_revenue"),
                count("*").alias("transaction_count")
            )
            .orderBy(col("total_revenue").desc());

        System.out.println("=== Revenue by Category ===");
        categoryRevenue.show();

        // 3. Monthly trend analysis
        Dataset<Row> monthlyTrend = withRevenue
            .withColumn("month", date_format(col("date"), "yyyy-MM"))
            .groupBy("month")
            .agg(sum("revenue").alias("monthly_revenue"))
            .orderBy("month");

        System.out.println("=== Monthly Revenue Trend ===");
        monthlyTrend.show();

        // 4. Top selling products (using Window functions)
        WindowSpec productWindow = Window
            .partitionBy("category")
            .orderBy(col("total_quantity").desc());

        Dataset<Row> productRanking = withRevenue
            .groupBy("category", "product")
            .agg(sum("quantity").alias("total_quantity"))
            .withColumn("rank", rank().over(productWindow))
            .filter(col("rank").leq(3));

        System.out.println("=== Top 3 Products by Category ===");
        productRanking.show();

        // 5. Save results
        categoryRevenue.write()
            .mode("overwrite")
            .parquet("output/category_revenue");

        spark.stop();
    }
}
```

---

## Java vs Scala Code Comparison

Here's a comparison of the same logic written in Java and Scala. Reference this when reading Scala documentation as a Java developer.

### DataFrame Creation and Querying

| Operation | Java | Scala |
|-----------|------|-------|
| Create SparkSession | `SparkSession.builder().getOrCreate()` | `SparkSession.builder.getOrCreate()` |
| Read CSV | `spark.read().option("header", "true").csv(path)` | `spark.read.option("header", true).csv(path)` |
| Print schema | `df.printSchema()` | `df.printSchema()` |
| Column reference | `col("name")` | `$"name"` or `col("name")` |

### Code Example Comparison

**Java:**
```java
import static org.apache.spark.sql.functions.*;

Dataset<Row> result = df
    .filter(col("age").gt(25))
    .withColumn("bonus", col("salary").multiply(0.1))
    .groupBy("department")
    .agg(
        avg("salary").alias("avg_salary"),
        sum("bonus").alias("total_bonus")
    )
    .orderBy(col("avg_salary").desc());
```

**Scala:**
```scala
import org.apache.spark.sql.functions._

val result = df
  .filter($"age" > 25)
  .withColumn("bonus", $"salary" * 0.1)
  .groupBy("department")
  .agg(
    avg("salary").alias("avg_salary"),
    sum("bonus").alias("total_bonus")
  )
  .orderBy($"avg_salary".desc)
```

### Key Differences

| Aspect | Java | Scala | Description |
|--------|------|-------|-------------|
| **Type declaration** | `Dataset<Row>` | `DataFrame` | Scala uses type alias |
| **Method calls** | `.method()` | `.method` | Scala can omit parentheses |
| **Column reference** | `col("x")` | `$"x"` | Scala uses StringContext |
| **Comparison** | `.gt(25)` | `> 25` | Scala has operator overloading |
| **Arithmetic** | `.multiply(0.1)` | `* 0.1` | Scala has operator overloading |
| **Lambda** | `row -> row.getInt(0)` | `row => row.getInt(0)` | Arrow syntax difference |
| **Anonymous function** | `(MapFunction<T,R>)` | Type inference | Java needs explicit cast |

### Dataset Type-Safe Code Comparison

**Java:**
```java
Encoder<Employee> encoder = Encoders.bean(Employee.class);
Dataset<Employee> ds = df.as(encoder);

Dataset<Employee> filtered = ds.filter(
    (FilterFunction<Employee>) emp -> emp.getAge() > 30
);

Dataset<String> names = ds.map(
    (MapFunction<Employee, String>) Employee::getName,
    Encoders.STRING()
);
```

**Scala:**
```scala
case class Employee(name: String, age: Int, department: String)  // Scala's data class, similar to Java's Record

val ds = df.as[Employee]

val filtered = ds.filter(_.age > 30)

val names = ds.map(_.name)
```

> **Note**: Scala's case class automatically generates Encoders, making it more concise than Java.
> Using Java 17+ `record` achieves similar conciseness.

## Next Steps

After understanding DataFrame and Dataset:

- [Spark SQL](spark-sql/) - Query DataFrames with SQL
- [Transformations and Actions](transformations-actions/) - Understanding when operations execute
