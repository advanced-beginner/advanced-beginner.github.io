---
title: Basic Examples
weight: 2
lastmod: "2026-01-07"
---

# Basic Examples

Example code utilizing Spark's core features.

## Data Loading

### Reading CSV Files

```java
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class CsvExample {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("CSV Example")
                .master("local[*]")
                .getOrCreate();

        // Basic read
        Dataset<Row> df = spark.read()
                .option("header", "true")       // First row as header
                .option("inferSchema", "true")  // Auto type inference
                .csv("data/employees.csv");

        // Detailed options
        Dataset<Row> dfDetailed = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .option("sep", ",")             // Delimiter
                .option("quote", "\"")          // Quote character
                .option("escape", "\\")         // Escape character
                .option("nullValue", "NA")      // Null representation
                .option("dateFormat", "yyyy-MM-dd")
                .option("timestampFormat", "yyyy-MM-dd HH:mm:ss")
                .csv("data/employees.csv");

        df.show();
        df.printSchema();

        spark.stop();
    }
}
```

### Reading JSON Files

```java
// Single file
Dataset<Row> df = spark.read().json("data/users.json");

// Multiline JSON
Dataset<Row> dfMulti = spark.read()
        .option("multiLine", "true")
        .json("data/users_multiline.json");

// JSON Lines (one JSON object per line)
Dataset<Row> dfLines = spark.read().json("data/users.jsonl");

df.show();
```

### Reading Parquet Files

```java
// Parquet (recommended format)
Dataset<Row> df = spark.read().parquet("data/users.parquet");

// Read only specific columns (column pruning)
Dataset<Row> selected = spark.read()
        .parquet("data/users.parquet")
        .select("id", "name");
```

### Reading from Database Using JDBC

```java
Dataset<Row> df = spark.read()
        .format("jdbc")
        .option("url", "jdbc:mysql://localhost:3306/mydb")
        .option("dbtable", "employees")
        .option("user", "user")
        .option("password", "password")
        .option("driver", "com.mysql.cj.jdbc.Driver")
        .load();

// Using query
Dataset<Row> dfQuery = spark.read()
        .format("jdbc")
        .option("url", "jdbc:mysql://localhost:3306/mydb")
        .option("query", "SELECT * FROM employees WHERE age > 30")
        .option("user", "user")
        .option("password", "password")
        .load();
```

## Data Transformation

### Column Operations

```java
import static org.apache.spark.sql.functions.*;

Dataset<Row> employees = spark.read()
        .option("header", "true")
        .option("inferSchema", "true")
        .csv("data/employees.csv");

// Add new column
Dataset<Row> withBonus = employees.withColumn(
    "bonus",
    col("salary").multiply(0.1)
);

// Add multiple columns
Dataset<Row> enhanced = employees
    .withColumn("bonus", col("salary").multiply(0.1))
    .withColumn("total_compensation", col("salary").plus(col("bonus")))
    .withColumn("hire_year", year(col("hire_date")))
    .withColumn("name_upper", upper(col("name")));

// Rename column
Dataset<Row> renamed = employees.withColumnRenamed("name", "employee_name");

// Drop columns
Dataset<Row> dropped = employees.drop("middle_name", "suffix");

// Cast column type
Dataset<Row> casted = employees.withColumn(
    "salary",
    col("salary").cast("double")
);

enhanced.show();
```

### Filtering

```java
// Single condition
Dataset<Row> highEarners = employees.filter(col("salary").gt(60000));

// Multiple conditions
Dataset<Row> filtered = employees.filter(
    col("age").geq(30)
    .and(col("department").equalTo("Engineering"))
    .and(col("salary").between(50000, 80000))
);

// String conditions
Dataset<Row> smithFamily = employees.filter(col("name").startsWith("Smith"));
Dataset<Row> hasEmail = employees.filter(col("email").contains("@company.com"));
Dataset<Row> pattern = employees.filter(col("name").rlike("^[A-Z][a-z]+$"));

// NULL handling
Dataset<Row> withManager = employees.filter(col("manager_id").isNotNull());
Dataset<Row> noManager = employees.filter(col("manager_id").isNull());

// IN condition
Dataset<Row> depts = employees.filter(
    col("department").isin("Engineering", "Marketing", "Sales")
);

filtered.show();
```

### Sorting

```java
// Single column
Dataset<Row> sorted = employees.orderBy("salary");
Dataset<Row> sortedDesc = employees.orderBy(col("salary").desc());

// Multiple columns
Dataset<Row> multiSort = employees.orderBy(
    col("department").asc(),
    col("salary").desc()
);

// NULL handling
Dataset<Row> nullsFirst = employees.orderBy(col("manager_id").asc_nulls_first());
Dataset<Row> nullsLast = employees.orderBy(col("salary").desc_nulls_last());

multiSort.show();
```

## Aggregation

### Basic Aggregation

```java
// Overall aggregation
Dataset<Row> stats = employees.agg(
    count("*").alias("total_count"),
    countDistinct("department").alias("dept_count"),
    sum("salary").alias("total_salary"),
    avg("salary").alias("avg_salary"),
    max("salary").alias("max_salary"),
    min("salary").alias("min_salary"),
    stddev("salary").alias("stddev_salary")
);

stats.show();
```

### Group Aggregation

```java
// Single group
Dataset<Row> byDept = employees
    .groupBy("department")
    .agg(
        count("*").alias("employee_count"),
        avg("salary").alias("avg_salary"),
        sum("salary").alias("total_salary")
    )
    .orderBy(col("total_salary").desc());

// Multiple groups
Dataset<Row> byDeptLevel = employees
    .groupBy("department", "level")
    .agg(
        count("*").alias("count"),
        avg("salary").alias("avg_salary")
    );

byDept.show();
```

### Pivot

```java
// Pivot table
Dataset<Row> pivoted = employees
    .groupBy("department")
    .pivot("level", Arrays.asList("Junior", "Senior", "Lead"))
    .agg(avg("salary"));

pivoted.show();
// +----------+-------+-------+------+
// |department| Junior| Senior|  Lead|
// +----------+-------+-------+------+
// |     Sales| 45000 | 65000 | 85000|
// |     Eng  | 55000 | 75000 | 95000|
// +----------+-------+-------+------+
```

## Joins

### Basic Join

```java
Dataset<Row> employees = spark.read().parquet("employees.parquet");
Dataset<Row> departments = spark.read().parquet("departments.parquet");

// Inner Join
Dataset<Row> joined = employees.join(
    departments,
    employees.col("department_id").equalTo(departments.col("id"))
);

// When column names are the same
Dataset<Row> simpleJoin = employees.join(departments, "department_id");

// Left Join
Dataset<Row> leftJoined = employees.join(
    departments,
    employees.col("department_id").equalTo(departments.col("id")),
    "left"
);

// Join types: inner, left, right, full, left_semi, left_anti, cross
```

### Multi-Condition Join

```java
Dataset<Row> multiJoin = orders.join(
    products,
    orders.col("product_id").equalTo(products.col("id"))
        .and(orders.col("region").equalTo(products.col("region")))
);
```

### Broadcast Join

```java
import static org.apache.spark.sql.functions.broadcast;

// Broadcast small table
Dataset<Row> optimized = employees.join(
    broadcast(departments),
    "department_id"
);
```

## Using SQL

```java
// Register temporary views
employees.createOrReplaceTempView("employees");
departments.createOrReplaceTempView("departments");

// Execute SQL query
Dataset<Row> result = spark.sql("""
    SELECT
        e.name,
        e.salary,
        d.department_name,
        AVG(e.salary) OVER (PARTITION BY e.department_id) as dept_avg_salary
    FROM employees e
    JOIN departments d ON e.department_id = d.id
    WHERE e.age > 25
    ORDER BY e.salary DESC
    LIMIT 100
    """);

result.show();

// Using CTE
Dataset<Row> cteResult = spark.sql("""
    WITH dept_stats AS (
        SELECT
            department_id,
            AVG(salary) as avg_salary,
            COUNT(*) as emp_count
        FROM employees
        GROUP BY department_id
    )
    SELECT
        d.department_name,
        ds.avg_salary,
        ds.emp_count
    FROM dept_stats ds
    JOIN departments d ON ds.department_id = d.id
    ORDER BY ds.avg_salary DESC
    """);

cteResult.show();
```

## Saving Data

### Saving to Files

```java
// Parquet (recommended)
result.write()
    .mode("overwrite")
    .parquet("output/result.parquet");

// Partitioning
result.write()
    .mode("overwrite")
    .partitionBy("year", "month")
    .parquet("output/partitioned");

// CSV
result.write()
    .mode("overwrite")
    .option("header", "true")
    .csv("output/result.csv");

// Save as single file
result.coalesce(1)
    .write()
    .mode("overwrite")
    .option("header", "true")
    .csv("output/single_file");
```

### Save Modes

| Mode | Description |
|------|-------------|
| `overwrite` | Overwrite existing data |
| `append` | Append to existing data |
| `ignore` | Ignore if already exists |
| `error` (default) | Error if already exists |

## Complete Example: Sales Analysis

```java
public class SalesAnalysisExample {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("Sales Analysis")
                .master("local[*]")
                .getOrCreate();

        spark.sparkContext().setLogLevel("WARN");

        // Load data
        Dataset<Row> orders = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("data/orders.csv");

        Dataset<Row> products = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("data/products.csv");

        Dataset<Row> customers = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("data/customers.csv");

        // 1. Join orders with products
        Dataset<Row> ordersWithProducts = orders
                .join(broadcast(products), "product_id")
                .withColumn("revenue", col("quantity").multiply(col("price")));

        // 2. Monthly revenue aggregation
        Dataset<Row> monthlyRevenue = ordersWithProducts
                .withColumn("month", date_format(col("order_date"), "yyyy-MM"))
                .groupBy("month")
                .agg(
                    sum("revenue").alias("total_revenue"),
                    count("*").alias("order_count"),
                    countDistinct("customer_id").alias("unique_customers")
                )
                .orderBy("month");

        System.out.println("=== Monthly Revenue ===");
        monthlyRevenue.show();

        // 3. Top 5 products by category
        WindowSpec windowSpec = Window
                .partitionBy("category")
                .orderBy(col("total_revenue").desc());

        Dataset<Row> topProducts = ordersWithProducts
                .groupBy("category", "product_name")
                .agg(sum("revenue").alias("total_revenue"))
                .withColumn("rank", rank().over(windowSpec))
                .filter(col("rank").leq(5));

        System.out.println("=== Top 5 Products by Category ===");
        topProducts.show(20);

        // 4. Customer segment analysis
        Dataset<Row> customerStats = ordersWithProducts
                .join(customers, "customer_id")
                .groupBy("customer_id", "customer_name", "segment")
                .agg(
                    sum("revenue").alias("total_spent"),
                    count("*").alias("order_count"),
                    avg("revenue").alias("avg_order_value")
                );

        Dataset<Row> segmentAnalysis = customerStats
                .groupBy("segment")
                .agg(
                    count("*").alias("customer_count"),
                    avg("total_spent").alias("avg_customer_value"),
                    sum("total_spent").alias("segment_revenue")
                )
                .orderBy(col("segment_revenue").desc());

        System.out.println("=== Customer Segment Analysis ===");
        segmentAnalysis.show();

        // 5. Complex analysis with SQL
        ordersWithProducts.createOrReplaceTempView("orders_enriched");

        Dataset<Row> sqlAnalysis = spark.sql("""
            WITH monthly_category AS (
                SELECT
                    DATE_FORMAT(order_date, 'yyyy-MM') as month,
                    category,
                    SUM(revenue) as revenue
                FROM orders_enriched
                GROUP BY DATE_FORMAT(order_date, 'yyyy-MM'), category
            ),
            category_growth AS (
                SELECT
                    month,
                    category,
                    revenue,
                    LAG(revenue) OVER (PARTITION BY category ORDER BY month) as prev_revenue
                FROM monthly_category
            )
            SELECT
                month,
                category,
                revenue,
                prev_revenue,
                ROUND((revenue - prev_revenue) / prev_revenue * 100, 2) as growth_rate
            FROM category_growth
            WHERE prev_revenue IS NOT NULL
            ORDER BY month, category
            """);

        System.out.println("=== Monthly Growth Rate by Category ===");
        sqlAnalysis.show(20);

        // 6. Save results
        monthlyRevenue.write()
                .mode("overwrite")
                .parquet("output/monthly_revenue");

        segmentAnalysis.write()
                .mode("overwrite")
                .parquet("output/segment_analysis");

        spark.stop();
    }
}
```

---

## Real Public Dataset Examples

Examples using public datasets commonly utilized in practice.

### NYC Taxi Data Analysis

New York City taxi data (TLC Trip Record Data) is one of the most widely used public datasets for learning big data analysis.

```java
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.*;
import static org.apache.spark.sql.functions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NYC Taxi Data Analysis Example
 * Data source: https://www.nyc.gov/site/tlc/about/tlc-trip-record-data.page
 */
public class NYCTaxiAnalysis {
    private static final Logger logger = LoggerFactory.getLogger(NYCTaxiAnalysis.class);

    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("NYC Taxi Analysis")
                .config("spark.sql.adaptive.enabled", "true")
                .config("spark.sql.adaptive.coalescePartitions.enabled", "true")
                .getOrCreate();

        try {
            // Explicit schema (better performance than inferSchema)
            StructType taxiSchema = new StructType()
                .add("VendorID", DataTypes.IntegerType)
                .add("tpep_pickup_datetime", DataTypes.TimestampType)
                .add("tpep_dropoff_datetime", DataTypes.TimestampType)
                .add("passenger_count", DataTypes.IntegerType)
                .add("trip_distance", DataTypes.DoubleType)
                .add("PULocationID", DataTypes.IntegerType)
                .add("DOLocationID", DataTypes.IntegerType)
                .add("payment_type", DataTypes.IntegerType)
                .add("fare_amount", DataTypes.DoubleType)
                .add("tip_amount", DataTypes.DoubleType)
                .add("total_amount", DataTypes.DoubleType);

            // Parquet format recommended (provided by NYC TLC)
            // Sample: https://d37ci6vzurychx.cloudfront.net/trip-data/yellow_tripdata_2024-01.parquet
            Dataset<Row> taxiData = spark.read()
                    .schema(taxiSchema)
                    .parquet("data/yellow_tripdata_2024-01.parquet");

            logger.info("Total records: {}", taxiData.count());

            // 1. Hourly trip pattern analysis
            Dataset<Row> hourlyPattern = taxiData
                .withColumn("pickup_hour", hour(col("tpep_pickup_datetime")))
                .withColumn("pickup_dayofweek", dayofweek(col("tpep_pickup_datetime")))
                .groupBy("pickup_dayofweek", "pickup_hour")
                .agg(
                    count("*").alias("trip_count"),
                    avg("trip_distance").alias("avg_distance"),
                    avg("total_amount").alias("avg_fare"),
                    avg("tip_amount").alias("avg_tip")
                )
                .orderBy("pickup_dayofweek", "pickup_hour");

            logger.info("=== Hourly Trip Patterns ===");
            hourlyPattern.show(24);

            // 2. Popular pickup/dropoff location analysis
            Dataset<Row> popularRoutes = taxiData
                .filter(col("trip_distance").gt(0))
                .groupBy("PULocationID", "DOLocationID")
                .agg(
                    count("*").alias("trip_count"),
                    avg("total_amount").alias("avg_fare"),
                    percentile_approx(col("trip_distance"), lit(0.5)).alias("median_distance")
                )
                .orderBy(col("trip_count").desc())
                .limit(20);

            logger.info("=== Top 20 Popular Routes ===");
            popularRoutes.show();

            // 3. Fare outlier detection
            Dataset<Row> fareStats = taxiData.agg(
                avg("total_amount").alias("mean"),
                stddev("total_amount").alias("stddev")
            ).first();

            double mean = fareStats.getDouble(0);
            double stddev = fareStats.getDouble(1);
            double threshold = mean + (3 * stddev);  // 3-sigma rule

            Dataset<Row> outliers = taxiData
                .filter(col("total_amount").gt(threshold)
                    .or(col("total_amount").lt(0)))
                .select("tpep_pickup_datetime", "trip_distance",
                        "fare_amount", "tip_amount", "total_amount");

            logger.info("=== Fare Outliers (> 3σ) ===");
            logger.info("Threshold: ${}", String.format("%.2f", threshold));
            outliers.show(10);

            // 4. Save results
            hourlyPattern.write()
                .mode("overwrite")
                .partitionBy("pickup_dayofweek")
                .parquet("output/nyc_taxi/hourly_pattern");

            logger.info("Analysis complete");

        } catch (Exception e) {
            logger.error("Error during analysis: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            spark.stop();
        }
    }
}
```

#### How to Download Data

```bash
# NYC TLC official data (Parquet format, ~50MB/month)
wget https://d37ci6vzurychx.cloudfront.net/trip-data/yellow_tripdata_2024-01.parquet

# Zone Lookup table (location code → location name mapping)
wget https://d37ci6vzurychx.cloudfront.net/misc/taxi_zone_lookup.csv
```

### Kaggle Dataset Example

[Kaggle](https://www.kaggle.com) provides various real-world datasets.

```java
/**
 * Kaggle Credit Card Fraud Detection Dataset Analysis
 * Data: https://www.kaggle.com/datasets/mlg-ulb/creditcardfraud
 */
public class FraudDetectionAnalysis {
    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionAnalysis.class);

    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("Fraud Detection Analysis")
                .config("spark.sql.shuffle.partitions", "8")  // For small data
                .getOrCreate();

        try {
            // Credit card transaction data (284,807 transactions, 492 fraud cases)
            Dataset<Row> transactions = spark.read()
                    .option("header", "true")
                    .option("inferSchema", "true")
                    .csv("data/creditcard.csv");

            // Check class imbalance
            logger.info("=== Class Distribution ===");
            transactions.groupBy("Class")
                .agg(
                    count("*").alias("count"),
                    round(count("*").multiply(100.0).divide(transactions.count()), 2)
                        .alias("percentage")
                )
                .show();

            // Fraud transaction characteristics analysis
            Dataset<Row> fraudStats = transactions
                .groupBy("Class")
                .agg(
                    avg("Amount").alias("avg_amount"),
                    max("Amount").alias("max_amount"),
                    min("Amount").alias("min_amount"),
                    stddev("Amount").alias("stddev_amount"),
                    avg("Time").alias("avg_time_seconds")
                );

            logger.info("=== Legitimate vs Fraud Transaction Statistics ===");
            fraudStats.show();

            // Hourly fraud pattern
            Dataset<Row> hourlyFraud = transactions
                .withColumn("hour", floor(col("Time").divide(3600)).mod(24))
                .groupBy("hour", "Class")
                .count()
                .orderBy("hour");

            logger.info("=== Hourly Transaction Patterns ===");
            hourlyFraud.show(48);

            // Fraud rate by amount bucket
            Dataset<Row> amountBuckets = transactions
                .withColumn("amount_bucket",
                    when(col("Amount").lt(100), "0-100")
                    .when(col("Amount").lt(500), "100-500")
                    .when(col("Amount").lt(1000), "500-1000")
                    .when(col("Amount").lt(5000), "1000-5000")
                    .otherwise("5000+"))
                .groupBy("amount_bucket")
                .agg(
                    count("*").alias("total"),
                    sum(when(col("Class").equalTo(1), 1).otherwise(0)).alias("fraud_count")
                )
                .withColumn("fraud_rate",
                    round(col("fraud_count").multiply(100.0).divide(col("total")), 4));

            logger.info("=== Fraud Rate by Amount Bucket ===");
            amountBuckets.show();

        } finally {
            spark.stop();
        }
    }
}
```

### Public Dataset List

| Dataset | Size | Use Case | Download |
|---------|------|----------|----------|
| **NYC Taxi** | ~50MB/month | Time series, aggregation, joins | [TLC](https://www.nyc.gov/site/tlc/about/tlc-trip-record-data.page) |
| **Credit Card Fraud** | 150MB | Imbalanced classification, anomaly detection | [Kaggle](https://www.kaggle.com/datasets/mlg-ulb/creditcardfraud) |
| **Amazon Reviews** | Several GB | Text analysis, sentiment classification | [AWS Registry](https://registry.opendata.aws/amazon-reviews/) |
| **Common Crawl** | Several TB | Large-scale web analysis | [commoncrawl.org](https://commoncrawl.org/) |
| **Wikipedia Dumps** | Tens of GB | NLP, knowledge graphs | [dumps.wikimedia.org](https://dumps.wikimedia.org/) |

## Next Steps

After completing the examples:

- [Performance Tuning](../../concepts/tuning/) - Code optimization
- [Appendix](../../appendix/) - Glossary, FAQ
