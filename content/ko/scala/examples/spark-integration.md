---
lastmod: "2026-01-08"
title: Spark 연동
weight: 4
---

Scala로 Apache Spark를 활용하는 방법을 배웁니다. Scala는 Spark의 네이티브 언어로, 가장 풍부한 API를 제공합니다.

## 왜 Scala로 Spark를 사용하는가?

### Java vs Scala 비교

```java
// Java: 장황한 코드
Dataset<Row> result = spark.read()
    .option("header", "true")
    .csv("data.csv")
    .filter(col("age").gt(30))
    .groupBy(col("department"))
    .agg(avg(col("salary")).alias("avg_salary"));
```

```scala
// Scala: 간결하고 표현력 있는 코드
val result = spark.read
  .option("header", "true")
  .csv("data.csv")
  .filter($"age" > 30)
  .groupBy($"department")
  .agg(avg($"salary").as("avg_salary"))
```

### Scala + Spark의 장점

| 장점 | 설명 |
|------|------|
| **네이티브 API** | Spark는 Scala로 작성됨, 최신 기능 가장 먼저 지원 |
| **타입 안전성** | Dataset API로 컴파일 타임 타입 체크 |
| **Case Class 통합** | 스키마 자동 추론, 타입 안전한 데이터 처리 |
| **함수형 스타일** | map, filter, reduce 등 자연스럽게 활용 |
| **REPL 지원** | spark-shell로 대화형 개발 가능 |

---

## 환경 설정

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

> **주의:** Spark 3.5는 Scala 2.12/2.13만 지원합니다. Scala 3는 아직 지원되지 않습니다.

### project/build.properties

```properties
sbt.version=1.10.6
```

---

## 기본 예제: DataFrame 처리

### SparkSession 생성

```scala
import org.apache.spark.sql.SparkSession

object SparkBasics extends App {
  // SparkSession 생성
  val spark = SparkSession.builder()
    .appName("Scala Spark Example")
    .master("local[*]")  // 로컬 모드, 모든 CPU 사용
    .getOrCreate()

  // 암시적 변환 import ($ 문법 사용을 위해)
  import spark.implicits._

  // 로그 레벨 조정
  spark.sparkContext.setLogLevel("WARN")

  // 예제 실행
  basicDataFrameOps()

  spark.stop()

  def basicDataFrameOps(): Unit = {
    // 1. DataFrame 생성
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

    // 2. 필터링과 선택
    df.filter($"salary" > 65000)
      .select($"name", $"salary")
      .show()

    // 3. 그룹화와 집계
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

## Case Class와 Dataset

### 타입 안전한 데이터 처리

Scala의 Case Class를 활용하면 **컴파일 타임에 타입 오류를 잡을 수 있습니다.**

```scala
import org.apache.spark.sql.{Dataset, SparkSession}

// 1. Case Class 정의 (스키마 역할)
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

  // 2. Dataset[Employee] 생성
  val employees: Dataset[Employee] = Seq(
    Employee("Alice", "Engineering", 75000, "2020-01-15"),
    Employee("Bob", "Engineering", 80000, "2019-03-20"),
    Employee("Charlie", "Sales", 65000, "2021-06-01"),
    Employee("Diana", "Sales", 70000, "2020-11-10"),
    Employee("Eve", "Marketing", 60000, "2022-02-28")
  ).toDS()

  // 3. 타입 안전한 연산
  val highEarners: Dataset[Employee] = employees
    .filter(_.salary > 70000)  // 컴파일 타임 체크!

  highEarners.show()
  // +-----+-----------+------+----------+
  // | name| department|salary|  joinDate|
  // +-----+-----------+------+----------+
  // |Alice|Engineering| 75000|2020-01-15|
  // |  Bob|Engineering| 80000|2019-03-20|
  // +-----+-----------+------+----------+

  // 4. map으로 변환 (타입 안전)
  val names: Dataset[String] = employees.map(_.name)
  names.show()

  // 5. groupByKey로 집계
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

### DataFrame vs Dataset 비교

```scala
// DataFrame: 런타임 오류 가능
val df = employees.toDF()
df.filter($"salry" > 70000)  // 오타! 런타임에서야 발견

// Dataset: 컴파일 타임 오류
val ds: Dataset[Employee] = employees
ds.filter(_.salry > 70000)   // 컴파일 에러! 즉시 발견
//            ^^^^^ value salry is not a member of Employee
```

---

## 함수형 스타일 활용

### 고차 함수로 데이터 변환

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

  // 1. 함수형 체이닝
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

  // 2. UDF (User Defined Function) 정의
  val categorizeAmount = udf((amount: Double) => amount match {
    case a if a >= 200 => "HIGH"
    case a if a >= 100 => "MEDIUM"
    case _ => "LOW"
  })

  orders.toDF()
    .withColumn("category", categorizeAmount($"amount"))
    .show()

  // 3. 패턴 매칭 활용
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

---

## 실전 예제: ETL 파이프라인

### 데이터 읽기, 변환, 저장

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

  // 1. 스키마 정의 (타입 안전성)
  case class RawLog(
    timestamp: String,
    userId: String,
    action: String,
    page: String,
    duration: Option[Int]  // nullable 필드
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

  // 2. 샘플 데이터 생성 (실제로는 파일에서 읽음)
  val rawLogs = Seq(
    RawLog("2024-01-15T10:30:00", "U001", "VIEW", "/home", Some(30)),
    RawLog("2024-01-15T10:31:00", "U001", "CLICK", "/products", Some(5)),
    RawLog("2024-01-15T10:32:00", "U002", "VIEW", "/home", None),
    RawLog("2024-01-15T11:00:00", "U001", "PURCHASE", "/checkout", Some(120)),
    RawLog("2024-01-15T11:05:00", "U003", "VIEW", "/home", Some(45))
  ).toDS()

  // 3. 변환 함수들
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

  // 4. ETL 파이프라인
  val processedLogs: Dataset[ProcessedLog] = rawLogs
    // 결측값 처리
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
    // 필터링
    .filter(_.duration > 0)

  processedLogs.show()

  // 5. 집계 분석
  val hourlyStats = processedLogs
    .groupBy($"date", $"hour")
    .agg(
      countDistinct($"userId").as("unique_users"),
      count("*").as("total_events"),
      avg($"duration").as("avg_duration")
    )
    .orderBy($"date", $"hour")

  hourlyStats.show()

  // 6. 저장 (Parquet 포맷)
  processedLogs.write
    .mode(SaveMode.Overwrite)
    .partitionBy("date")
    .parquet("/tmp/processed_logs")

  println("ETL 완료: /tmp/processed_logs")

  spark.stop()
}
```

---

## Spark SQL과 Scala

### SQL과 Scala API 혼합 사용

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

  // 1. 임시 뷰 등록
  products.createOrReplaceTempView("products")
  sales.createOrReplaceTempView("sales")

  // 2. SQL 쿼리 실행
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

  // 3. SQL 결과를 Scala로 추가 처리
  val topCategory = revenueByCategory
    .as[(String, Double, Long)]
    .head()

  println(s"최고 매출 카테고리: ${topCategory._1} (${topCategory._2})")

  spark.stop()
}
```

---

## 성능 최적화 팁

### 1. 파티셔닝 최적화

```scala
// 셔플 파티션 수 조정
spark.conf.set("spark.sql.shuffle.partitions", "200")

// 데이터 크기에 맞게 repartition
val optimized = largeDataset
  .repartition(100, $"key_column")  // 키 기반 파티셔닝
```

### 2. 브로드캐스트 조인

```scala
import org.apache.spark.sql.functions.broadcast

// 작은 테이블을 브로드캐스트
val result = largeDf.join(
  broadcast(smallDf),  // 작은 테이블 브로드캐스트
  largeDf("id") === smallDf("id")
)
```

### 3. 캐싱 전략

```scala
// 반복 사용되는 데이터셋 캐싱
val cachedDf = expensiveComputation.cache()

// 메모리+디스크 캐싱
import org.apache.spark.storage.StorageLevel
expensiveComputation.persist(StorageLevel.MEMORY_AND_DISK)

// 사용 후 해제
cachedDf.unpersist()
```

### 4. Predicate Pushdown

```scala
// 파일 읽기 시 필터 푸시다운
val filtered = spark.read
  .parquet("/data/logs")
  .filter($"date" === "2024-01-15")  // 파티션 프루닝 발생
  .filter($"status" === "ERROR")     // 필터 푸시다운
```

---

## 트러블슈팅

### 흔한 오류와 해결

| 오류 | 원인 | 해결 |
|------|------|------|
| `Task not serializable` | 클로저에 직렬화 불가능한 객체 포함 | 클로저 내부에서 객체 생성 또는 `@transient` 사용 |
| `OutOfMemoryError` | 드라이버/익스큐터 메모리 부족 | `spark.driver.memory`, `spark.executor.memory` 증가 |
| `Container killed by YARN` | 메모리 초과 | `spark.yarn.executor.memoryOverhead` 증가 |
| `shuffle read/write timeout` | 네트워크 이슈 | `spark.network.timeout` 증가 |

### Task not serializable 해결

```scala
// ❌ 오류 발생
class MyProcessor {
  val config = loadConfig()  // 직렬화 불가

  def process(df: DataFrame): DataFrame = {
    df.filter($"value" > config.threshold)  // 클로저에 config 포함
  }
}

// ✅ 해결 방법 1: 로컬 변수로 캡처
class MyProcessor {
  val config = loadConfig()

  def process(df: DataFrame): DataFrame = {
    val threshold = config.threshold  // 기본 타입만 캡처
    df.filter($"value" > threshold)
  }
}

// ✅ 해결 방법 2: @transient 사용
class MyProcessor extends Serializable {
  @transient lazy val config = loadConfig()

  def process(df: DataFrame): DataFrame = {
    df.filter($"value" > config.threshold)
  }
}
```

---

## 실행 방법

```bash
# 1. sbt로 실행
sbt run

# 2. spark-submit으로 실행
sbt package
spark-submit \
  --class SparkBasics \
  --master local[*] \
  target/scala-2.13/spark-scala-example_2.13-0.1.jar

# 3. spark-shell로 대화형 실행
spark-shell --master local[*]
```

---

## 다음 단계

- [Spark 가이드]({{< relref "/spark" >}}) - Spark 심화 학습
- [Kafka 연동]({{< relref "/kafka" >}}) - Structured Streaming + Kafka
- [함수형 패턴](../concepts/functional-patterns/) - Spark에서의 함수형 프로그래밍
