---
title: 기본 예제
weight: 2
---

# 기본 예제

Spark의 핵심 기능을 활용하는 예제 코드입니다.

## 데이터 로딩

### CSV 파일 읽기

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

        // 기본 읽기
        Dataset<Row> df = spark.read()
                .option("header", "true")       // 첫 줄을 헤더로
                .option("inferSchema", "true")  // 타입 자동 추론
                .csv("data/employees.csv");

        // 상세 옵션
        Dataset<Row> dfDetailed = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .option("sep", ",")             // 구분자
                .option("quote", "\"")          // 인용 문자
                .option("escape", "\\")         // 이스케이프 문자
                .option("nullValue", "NA")      // null 표현
                .option("dateFormat", "yyyy-MM-dd")
                .option("timestampFormat", "yyyy-MM-dd HH:mm:ss")
                .csv("data/employees.csv");

        df.show();
        df.printSchema();

        spark.stop();
    }
}
```

### JSON 파일 읽기

```java
// 단일 파일
Dataset<Row> df = spark.read().json("data/users.json");

// 멀티라인 JSON
Dataset<Row> dfMulti = spark.read()
        .option("multiLine", "true")
        .json("data/users_multiline.json");

// JSON Lines (한 줄에 하나의 JSON 객체)
Dataset<Row> dfLines = spark.read().json("data/users.jsonl");

df.show();
```

### Parquet 파일 읽기

```java
// Parquet (권장 포맷)
Dataset<Row> df = spark.read().parquet("data/users.parquet");

// 특정 컬럼만 읽기 (컬럼 프루닝)
Dataset<Row> selected = spark.read()
        .parquet("data/users.parquet")
        .select("id", "name");
```

### JDBC로 데이터베이스 읽기

```java
Dataset<Row> df = spark.read()
        .format("jdbc")
        .option("url", "jdbc:mysql://localhost:3306/mydb")
        .option("dbtable", "employees")
        .option("user", "user")
        .option("password", "password")
        .option("driver", "com.mysql.cj.jdbc.Driver")
        .load();

// 쿼리 사용
Dataset<Row> dfQuery = spark.read()
        .format("jdbc")
        .option("url", "jdbc:mysql://localhost:3306/mydb")
        .option("query", "SELECT * FROM employees WHERE age > 30")
        .option("user", "user")
        .option("password", "password")
        .load();
```

## 데이터 변환

### 컬럼 연산

```java
import static org.apache.spark.sql.functions.*;

Dataset<Row> employees = spark.read()
        .option("header", "true")
        .option("inferSchema", "true")
        .csv("data/employees.csv");

// 새 컬럼 추가
Dataset<Row> withBonus = employees.withColumn(
    "bonus",
    col("salary").multiply(0.1)
);

// 여러 컬럼 추가
Dataset<Row> enhanced = employees
    .withColumn("bonus", col("salary").multiply(0.1))
    .withColumn("total_compensation", col("salary").plus(col("bonus")))
    .withColumn("hire_year", year(col("hire_date")))
    .withColumn("name_upper", upper(col("name")));

// 컬럼 이름 변경
Dataset<Row> renamed = employees.withColumnRenamed("name", "employee_name");

// 컬럼 삭제
Dataset<Row> dropped = employees.drop("middle_name", "suffix");

// 컬럼 타입 변환
Dataset<Row> casted = employees.withColumn(
    "salary",
    col("salary").cast("double")
);

enhanced.show();
```

### 필터링

```java
// 단일 조건
Dataset<Row> highEarners = employees.filter(col("salary").gt(60000));

// 복합 조건
Dataset<Row> filtered = employees.filter(
    col("age").geq(30)
    .and(col("department").equalTo("Engineering"))
    .and(col("salary").between(50000, 80000))
);

// 문자열 조건
Dataset<Row> kimFamily = employees.filter(col("name").startsWith("Kim"));
Dataset<Row> hasEmail = employees.filter(col("email").contains("@company.com"));
Dataset<Row> pattern = employees.filter(col("name").rlike("^[A-Z][a-z]+$"));

// NULL 처리
Dataset<Row> withManager = employees.filter(col("manager_id").isNotNull());
Dataset<Row> noManager = employees.filter(col("manager_id").isNull());

// IN 조건
Dataset<Row> depts = employees.filter(
    col("department").isin("Engineering", "Marketing", "Sales")
);

filtered.show();
```

### 정렬

```java
// 단일 컬럼
Dataset<Row> sorted = employees.orderBy("salary");
Dataset<Row> sortedDesc = employees.orderBy(col("salary").desc());

// 다중 컬럼
Dataset<Row> multiSort = employees.orderBy(
    col("department").asc(),
    col("salary").desc()
);

// NULL 처리
Dataset<Row> nullsFirst = employees.orderBy(col("manager_id").asc_nulls_first());
Dataset<Row> nullsLast = employees.orderBy(col("salary").desc_nulls_last());

multiSort.show();
```

## 집계

### 기본 집계

```java
// 전체 집계
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

### 그룹별 집계

```java
// 단일 그룹
Dataset<Row> byDept = employees
    .groupBy("department")
    .agg(
        count("*").alias("employee_count"),
        avg("salary").alias("avg_salary"),
        sum("salary").alias("total_salary")
    )
    .orderBy(col("total_salary").desc());

// 다중 그룹
Dataset<Row> byDeptLevel = employees
    .groupBy("department", "level")
    .agg(
        count("*").alias("count"),
        avg("salary").alias("avg_salary")
    );

byDept.show();
```

### 피벗

```java
// 피벗 테이블
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

## 조인

### 기본 조인

```java
Dataset<Row> employees = spark.read().parquet("employees.parquet");
Dataset<Row> departments = spark.read().parquet("departments.parquet");

// Inner Join
Dataset<Row> joined = employees.join(
    departments,
    employees.col("department_id").equalTo(departments.col("id"))
);

// 컬럼명이 같은 경우
Dataset<Row> simpleJoin = employees.join(departments, "department_id");

// Left Join
Dataset<Row> leftJoined = employees.join(
    departments,
    employees.col("department_id").equalTo(departments.col("id")),
    "left"
);

// 조인 유형: inner, left, right, full, left_semi, left_anti, cross
```

### 다중 조건 조인

```java
Dataset<Row> multiJoin = orders.join(
    products,
    orders.col("product_id").equalTo(products.col("id"))
        .and(orders.col("region").equalTo(products.col("region")))
);
```

### 브로드캐스트 조인

```java
import static org.apache.spark.sql.functions.broadcast;

// 작은 테이블 브로드캐스트
Dataset<Row> optimized = employees.join(
    broadcast(departments),
    "department_id"
);
```

## SQL 사용

```java
// 임시 뷰 등록
employees.createOrReplaceTempView("employees");
departments.createOrReplaceTempView("departments");

// SQL 쿼리 실행
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

// CTE 사용
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

## 데이터 저장

### 파일 저장

```java
// Parquet (권장)
result.write()
    .mode("overwrite")
    .parquet("output/result.parquet");

// 파티셔닝
result.write()
    .mode("overwrite")
    .partitionBy("year", "month")
    .parquet("output/partitioned");

// CSV
result.write()
    .mode("overwrite")
    .option("header", "true")
    .csv("output/result.csv");

// 단일 파일로 저장
result.coalesce(1)
    .write()
    .mode("overwrite")
    .option("header", "true")
    .csv("output/single_file");
```

### 저장 모드

| 모드 | 설명 |
|------|------|
| `overwrite` | 기존 데이터 덮어쓰기 |
| `append` | 기존 데이터에 추가 |
| `ignore` | 이미 존재하면 무시 |
| `error` (기본) | 이미 존재하면 오류 |

## 종합 예제: 매출 분석

```java
public class SalesAnalysisExample {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("Sales Analysis")
                .master("local[*]")
                .getOrCreate();

        spark.sparkContext().setLogLevel("WARN");

        // 데이터 로드
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

        // 1. 주문-제품 조인
        Dataset<Row> ordersWithProducts = orders
                .join(broadcast(products), "product_id")
                .withColumn("revenue", col("quantity").multiply(col("price")));

        // 2. 월별 매출 집계
        Dataset<Row> monthlyRevenue = ordersWithProducts
                .withColumn("month", date_format(col("order_date"), "yyyy-MM"))
                .groupBy("month")
                .agg(
                    sum("revenue").alias("total_revenue"),
                    count("*").alias("order_count"),
                    countDistinct("customer_id").alias("unique_customers")
                )
                .orderBy("month");

        System.out.println("=== 월별 매출 ===");
        monthlyRevenue.show();

        // 3. 카테고리별 Top 5 제품
        WindowSpec windowSpec = Window
                .partitionBy("category")
                .orderBy(col("total_revenue").desc());

        Dataset<Row> topProducts = ordersWithProducts
                .groupBy("category", "product_name")
                .agg(sum("revenue").alias("total_revenue"))
                .withColumn("rank", rank().over(windowSpec))
                .filter(col("rank").leq(5));

        System.out.println("=== 카테고리별 Top 5 제품 ===");
        topProducts.show(20);

        // 4. 고객 세그먼트 분석
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

        System.out.println("=== 고객 세그먼트 분석 ===");
        segmentAnalysis.show();

        // 5. SQL로 복합 분석
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

        System.out.println("=== 카테고리별 월간 성장률 ===");
        sqlAnalysis.show(20);

        // 6. 결과 저장
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

## 다음 단계

예제를 완료했다면:

- [성능 튜닝](../../concepts/tuning/) - 코드 최적화
- [부록](../../appendix/) - 용어 사전, FAQ
