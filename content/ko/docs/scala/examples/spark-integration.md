---
lastmod: "2026-01-10"
title: Spark 연동
description: "Scala로 Spark 애플리케이션을 단계별로 구현합니다."
weight: 4
---

{{< callout type="info" title="TL;DR" >}}
- **Scala는 Spark의 네이티브 언어**: 최신 기능 가장 먼저 지원, 가장 간결한 API
- **DataFrame**: SQL 스타일 데이터 처리, `$"column"` 문법으로 컬럼 참조
- **Dataset[T]**: Case Class로 타입 안전한 데이터 처리, 컴파일 타임 오류 검출
- **성능 최적화**: 브로드캐스트 조인, 캐싱, Predicate Pushdown 활용
- **주의**: Spark 3.5는 Scala 2.12/2.13만 지원 (Scala 3 미지원)
{{< /callout >}}

**대상 독자**: 대규모 데이터 처리를 배우려는 Scala 개발자, Spark 입문자

**선수 지식**:
- Scala 기본 문법 및 함수형 프로그래밍 개념
- sbt 빌드 도구 사용법
- SQL 기초 (권장)

---

Scala로 Apache Spark를 활용하는 방법을 배웁니다. Scala는 Spark의 네이티브 언어로, 가장 풍부한 API를 제공합니다. Spark 자체가 Scala로 작성되었기 때문에 새로운 기능이 가장 먼저 Scala API에 추가되며, 타입 안전성과 함수형 프로그래밍의 장점을 최대한 활용할 수 있습니다.

#### 왜 Scala로 Spark를 사용하는가?

Java와 Scala로 동일한 Spark 작업을 구현했을 때의 차이를 비교해보면 Scala의 장점이 명확해집니다. Java 코드는 장황하고 보일러플레이트가 많은 반면, Scala 코드는 간결하고 의도가 분명합니다.

**Java vs Scala 비교**

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

**Scala + Spark의 장점**

Scala와 Spark를 함께 사용할 때 얻을 수 있는 주요 이점을 정리했습니다. 네이티브 API를 통해 최신 기능에 가장 먼저 접근할 수 있고, Case Class를 활용한 타입 안전한 데이터 처리가 가능합니다.

| 장점 | 설명 |
|------|------|
| **네이티브 API** | Spark는 Scala로 작성됨, 최신 기능 가장 먼저 지원 |
| **타입 안전성** | Dataset API로 컴파일 타임 타입 체크 |
| **Case Class 통합** | 스키마 자동 추론, 타입 안전한 데이터 처리 |
| **함수형 스타일** | map, filter, reduce 등 자연스럽게 활용 |
| **REPL 지원** | spark-shell로 대화형 개발 가능 |

{{< callout type="tip" title="핵심 포인트" >}}
- Scala는 Spark의 네이티브 언어로 최신 기능이 가장 먼저 지원됨
- Java 대비 훨씬 간결한 코드 작성 가능 (`$"column"` 문법 등)
- Case Class로 타입 안전한 Dataset API 활용 가능
{{< /callout >}}

#### 환경 설정

Spark 프로젝트를 시작하려면 먼저 build.sbt에 Spark 의존성을 추가해야 합니다. 현재 Spark 3.5는 Scala 2.12와 2.13을 지원하며, Scala 3는 아직 지원되지 않습니다.

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

> **주의:** Spark 3.5는 Scala 2.12/2.13만 지원합니다. Scala 3는 아직 지원되지 않습니다.

**project/build.properties**

```properties
sbt.version=1.10.6
```

{{< callout type="tip" title="핵심 포인트" >}}
- Spark 3.5는 **Scala 2.12/2.13만 지원** (Scala 3 미지원)
- `spark-core`와 `spark-sql` 의존성 추가 필수
- sbt 버전은 `project/build.properties`에 명시
{{< /callout >}}

#### 기본 예제: DataFrame 처리

Spark의 핵심 진입점은 SparkSession입니다. SparkSession을 통해 데이터를 읽고, DataFrame을 생성하고, SQL 쿼리를 실행할 수 있습니다.

**SparkSession 생성**

SparkSession을 생성할 때는 애플리케이션 이름과 실행 모드를 지정합니다. 로컬 개발 시에는 `local[*]`를 사용하여 모든 CPU 코어를 활용합니다.

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

이 예제에서 `spark.implicits._`를 import하면 `$"column_name"` 문법으로 컬럼을 참조할 수 있습니다. 이는 Scala의 문자열 보간법과 암시적 변환을 활용한 것입니다.

{{< callout type="tip" title="핵심 포인트" >}}
- **SparkSession**: Spark의 진입점, `builder()` 패턴으로 생성
- **spark.implicits._**: `$"column"` 문법 및 `toDF()` 활성화
- **local[\*]**: 로컬 모드에서 모든 CPU 코어 사용
- `filter`, `select`, `groupBy`, `agg`, `orderBy`로 데이터 처리
{{< /callout >}}

#### Case Class와 Dataset

Scala의 Case Class를 활용하면 컴파일 타임에 타입 오류를 잡을 수 있습니다. DataFrame은 런타임에 컬럼 이름 오류를 발견하지만, Dataset은 컴파일 타임에 필드 이름 오류를 발견합니다.

**타입 안전한 데이터 처리**

Case Class로 스키마를 정의하면 Spark가 자동으로 스키마를 추론합니다. 이를 통해 타입 안전한 연산이 가능해집니다.

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

**DataFrame vs Dataset 비교**

DataFrame에서는 컬럼 이름 오타가 런타임에서야 발견되지만, Dataset에서는 컴파일 타임에 즉시 발견됩니다.

```scala
// DataFrame: 런타임 오류 가능
val df = employees.toDF()
df.filter($"salry" > 70000)  // 오타! 런타임에서야 발견

// Dataset: 컴파일 타임 오류
val ds: Dataset[Employee] = employees
ds.filter(_.salry > 70000)   // 컴파일 에러! 즉시 발견
//            ^^^^^ value salry is not a member of Employee
```

{{< callout type="tip" title="핵심 포인트" >}}
- **Dataset[T]**: Case Class로 타입 안전한 데이터 처리
- **DataFrame**: 런타임 오류 가능, **Dataset**: 컴파일 타임 오류 검출
- **toDS()**: Seq를 Dataset으로 변환
- **groupByKey + mapGroups**: 타입 안전한 그룹화 및 집계
{{< /callout >}}

#### 함수형 스타일 활용

Scala의 함수형 프로그래밍 기능은 Spark와 잘 어울립니다. 고차 함수, 패턴 매칭, 불변 데이터 등의 개념이 분산 데이터 처리에 자연스럽게 적용됩니다.

**고차 함수로 데이터 변환**

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

UDF(User Defined Function)를 사용하면 Scala 함수를 Spark SQL에서 사용할 수 있습니다. 패턴 매칭을 활용하면 데이터 분류 로직을 명확하게 표현할 수 있습니다.

{{< callout type="tip" title="핵심 포인트" >}}
- **함수형 체이닝**: `filter` → `map` → `groupByKey` → `reduceGroups` 연결
- **UDF**: Scala 함수를 Spark SQL에서 사용 가능하게 변환
- **패턴 매칭**: 데이터 분류 로직을 명확하게 표현
{{< /callout >}}

#### 실전 예제: ETL 파이프라인

실제 데이터 엔지니어링에서 자주 사용하는 ETL(Extract, Transform, Load) 파이프라인을 구현해봅니다. 로그 데이터를 읽고, 변환하고, 분석 결과를 저장하는 전체 과정을 다룹니다.

**데이터 읽기, 변환, 저장**

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

이 ETL 파이프라인에서 Option 타입을 사용하여 nullable 필드를 안전하게 처리하고, 패턴 매칭으로 세션 유형을 분류합니다. 최종 결과는 Parquet 포맷으로 저장하여 후속 분석에 활용할 수 있습니다.

{{< callout type="tip" title="핵심 포인트" >}}
- **Option[T]**: nullable 필드를 타입 안전하게 처리
- **getOrElse**: 결측값에 기본값 적용
- **partitionBy**: 날짜별 파티셔닝으로 쿼리 성능 향상
- **Parquet**: 컬럼 기반 포맷으로 분석에 최적화
{{< /callout >}}

#### Spark SQL과 Scala

Spark SQL을 사용하면 SQL 쿼리와 Scala API를 자유롭게 혼합할 수 있습니다. 복잡한 조인이나 집계는 SQL로 작성하고, 그 결과를 Scala로 추가 처리할 수 있습니다.

**SQL과 Scala API 혼합 사용**

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

{{< callout type="tip" title="핵심 포인트" >}}
- **createOrReplaceTempView**: DataFrame을 SQL에서 테이블로 참조
- **spark.sql()**: SQL 쿼리 실행 후 DataFrame 반환
- **SQL + Scala API 혼합**: 복잡한 조인은 SQL로, 추가 처리는 Scala로
{{< /callout >}}

#### 성능 최적화 팁

Spark 애플리케이션의 성능을 최적화하는 주요 기법들을 소개합니다. 파티셔닝, 브로드캐스트 조인, 캐싱, Predicate Pushdown 등을 적절히 활용하면 성능을 크게 개선할 수 있습니다.

**1. 파티셔닝 최적화**

셔플 파티션 수를 데이터 크기에 맞게 조정하면 성능이 향상됩니다.

```scala
// 셔플 파티션 수 조정
spark.conf.set("spark.sql.shuffle.partitions", "200")

// 데이터 크기에 맞게 repartition
val optimized = largeDataset
  .repartition(100, $"key_column")  // 키 기반 파티셔닝
```

**2. 브로드캐스트 조인**

작은 테이블을 브로드캐스트하면 셔플을 피할 수 있습니다.

```scala
import org.apache.spark.sql.functions.broadcast

// 작은 테이블을 브로드캐스트
val result = largeDf.join(
  broadcast(smallDf),  // 작은 테이블 브로드캐스트
  largeDf("id") === smallDf("id")
)
```

**3. 캐싱 전략**

반복 사용되는 데이터셋은 캐싱하여 재계산을 피합니다.

```scala
// 반복 사용되는 데이터셋 캐싱
val cachedDf = expensiveComputation.cache()

// 메모리+디스크 캐싱
import org.apache.spark.storage.StorageLevel
expensiveComputation.persist(StorageLevel.MEMORY_AND_DISK)

// 사용 후 해제
cachedDf.unpersist()
```

**4. Predicate Pushdown**

필터 조건을 데이터 소스 수준까지 내려보내 불필요한 데이터 읽기를 방지합니다.

```scala
// 파일 읽기 시 필터 푸시다운
val filtered = spark.read
  .parquet("/data/logs")
  .filter($"date" === "2024-01-15")  // 파티션 프루닝 발생
  .filter($"status" === "ERROR")     // 필터 푸시다운
```

{{< callout type="tip" title="핵심 포인트" >}}
- **파티셔닝**: `repartition(n, $"key")`로 셔플 최적화
- **브로드캐스트 조인**: 작은 테이블을 모든 노드에 복제하여 셔플 방지
- **캐싱**: `cache()` 또는 `persist()`로 반복 사용 데이터 메모리에 유지
- **Predicate Pushdown**: 필터 조건을 데이터 소스 수준까지 내려보냄
{{< /callout >}}

#### 트러블슈팅

Spark 개발 중 자주 발생하는 오류와 해결 방법을 정리했습니다.

**흔한 오류와 해결**

| 오류 | 원인 | 해결 |
|------|------|------|
| `Task not serializable` | 클로저에 직렬화 불가능한 객체 포함 | 클로저 내부에서 객체 생성 또는 `@transient` 사용 |
| `OutOfMemoryError` | 드라이버/익스큐터 메모리 부족 | `spark.driver.memory`, `spark.executor.memory` 증가 |
| `Container killed by YARN` | 메모리 초과 | `spark.yarn.executor.memoryOverhead` 증가 |
| `shuffle read/write timeout` | 네트워크 이슈 | `spark.network.timeout` 증가 |

**Task not serializable 해결**

이 오류는 클로저가 직렬화할 수 없는 객체를 참조할 때 발생합니다. 해결 방법은 두 가지입니다.

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

{{< callout type="tip" title="핵심 포인트" >}}
- **Task not serializable**: 클로저에서 기본 타입만 캡처하거나 `@transient` 사용
- **OutOfMemoryError**: `spark.driver.memory`, `spark.executor.memory` 증가
- **메모리 문제**: `StorageLevel.MEMORY_AND_DISK`로 디스크 스필 허용
{{< /callout >}}

#### 실행 방법

Spark 애플리케이션을 실행하는 다양한 방법입니다.

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

로컬 개발 시에는 sbt run이나 spark-shell을 사용하고, 클러스터 배포 시에는 spark-submit을 사용합니다.

{{< callout type="tip" title="핵심 포인트" >}}
- **sbt run**: 로컬 개발 시 빠른 실행
- **spark-shell**: 대화형 개발 및 탐색적 분석
- **spark-submit**: 클러스터 배포 시 사용, JAR 패키징 필요
{{< /callout >}}

#### 다음 단계

Spark의 기본 사용법을 익혔다면 다음 주제들로 학습을 이어가세요.

- [Spark 가이드]({{< relref "/docs/spark" >}}) - Spark 심화 학습
- [Kafka 연동]({{< relref "/docs/kafka" >}}) - Structured Streaming + Kafka
- [함수형 패턴]({{< relref "/docs/scala/concepts/functional-patterns" >}}) - Spark에서의 함수형 프로그래밍

