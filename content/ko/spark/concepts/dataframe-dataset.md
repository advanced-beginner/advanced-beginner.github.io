---
title: DataFrame과 Dataset
weight: 3
lastmod: "2026-01-09"
author:
  name: Advanced Beginner
  github: advanced-beginner
---

DataFrame과 Dataset은 Spark의 현대적인 고수준 API입니다. RDD보다 사용하기 쉽고, Catalyst Optimizer를 통한 자동 최적화를 제공합니다.

#### 개념 정리

**DataFrame**

**DataFrame**은 이름 있는 컬럼으로 구성된 분산 데이터 컬렉션입니다. 관계형 데이터베이스의 테이블이나 Python/R의 DataFrame과 유사합니다.

```java
// DataFrame은 Dataset<Row>의 별칭
Dataset<Row> df = spark.read().json("employees.json");
```

**Dataset**

**Dataset**은 특정 타입을 가진 분산 데이터 컬렉션입니다. 컴파일 타임 타입 안전성을 제공합니다.

```java
// Java에서 Dataset 사용 시 Encoder 필요
public class Employee implements Serializable {
    private String name;
    private int age;
    // getters, setters...
}

Encoder<Employee> encoder = Encoders.bean(Employee.class);
Dataset<Employee> ds = spark.read().json("employees.json").as(encoder);
```

**Java에서의 사용**

| 개념 | Java 표현 | 설명 |
|------|----------|------|
| DataFrame | `Dataset<Row>` | 스키마는 있지만 Row 타입 |
| Dataset | `Dataset<T>` | 타입 파라미터로 POJO 사용 |
| Row | `org.apache.spark.sql.Row` | 스키마 기반 제네릭 행 |

#### DataFrame 생성

**1. 파일에서 생성**

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

// Parquet (권장 포맷)
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

**2. 프로그래밍 방식으로 생성**

```java
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.*;

import java.util.Arrays;
import java.util.List;

// 데이터 준비
List<Row> data = Arrays.asList(
    RowFactory.create("Alice", 30, "Engineering"),
    RowFactory.create("Bob", 25, "Marketing"),
    RowFactory.create("Charlie", 35, "Engineering")
);

// 스키마 정의
StructType schema = new StructType()
        .add("name", DataTypes.StringType, false)
        .add("age", DataTypes.IntegerType, false)
        .add("department", DataTypes.StringType, true);

// DataFrame 생성
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

**3. POJO에서 생성**

```java
import org.apache.spark.sql.Encoders;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

// POJO 정의 (JavaBean 규약 따라야 함)
public class Employee implements Serializable {
    private String name;
    private int age;
    private String department;

    // 기본 생성자 필수
    public Employee() {}

    public Employee(String name, int age, String department) {
        this.name = name;
        this.age = age;
        this.department = department;
    }

    // Getter/Setter 필수
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}

// Dataset 생성
List<Employee> employees = Arrays.asList(
    new Employee("Alice", 30, "Engineering"),
    new Employee("Bob", 25, "Marketing")
);

Dataset<Employee> ds = spark.createDataset(employees, Encoders.bean(Employee.class));
ds.show();
```

#### 기본 연산

**스키마 확인**

```java
// 스키마 출력
df.printSchema();
// root
//  |-- name: string (nullable = false)
//  |-- age: integer (nullable = false)
//  |-- department: string (nullable = true)

// 컬럼 목록
String[] columns = df.columns();

// 데이터 타입 확인
StructType schema = df.schema();
```

**데이터 확인**

```java
// 상위 n개 행 출력
df.show();       // 기본 20행
df.show(10);     // 10행
df.show(false);  // 문자열 잘림 없이

// 첫 번째 행
Row first = df.first();

// 상위 n개 행을 배열로
Row[] rows = (Row[]) df.take(5);

// 통계 요약
df.describe("age", "salary").show();
// +-------+------------------+------------------+
// |summary|               age|            salary|
// +-------+------------------+------------------+
// |  count|                 3|                 3|
// |   mean|              30.0|           50000.0|
// | stddev| 5.0|           10000.0|
// |    min|                25|             40000|
// |    max|                35|             60000|
// +-------+------------------+------------------+
```

**Select (컬럼 선택)**

```java
import static org.apache.spark.sql.functions.*;

// 컬럼 선택
df.select("name", "age").show();

// Column 객체 사용
df.select(col("name"), col("age")).show();

// 컬럼 연산
df.select(
    col("name"),
    col("age"),
    col("age").plus(10).alias("age_plus_10"),
    expr("age * 2").alias("age_doubled")
).show();

// 모든 컬럼 + 새 컬럼
df.select(
    col("*"),
    lit("Korea").alias("country")
).show();
```

**Filter (조건 필터링)**

```java
// 문자열 조건
df.filter("age > 25").show();

// Column 조건
df.filter(col("age").gt(25)).show();
df.filter(col("age").geq(25).and(col("department").equalTo("Engineering"))).show();

// where는 filter와 동일
df.where(col("age").gt(25)).show();

// 복합 조건
df.filter(
    col("age").between(25, 35)
    .and(col("department").isin("Engineering", "Marketing"))
).show();

// null 체크
df.filter(col("department").isNotNull()).show();

// 문자열 조건
df.filter(col("name").startsWith("A")).show();
df.filter(col("name").contains("li")).show();
df.filter(col("name").rlike("^A.*e$")).show();  // 정규식
```

**컬럼 추가/수정/삭제**

```java
// 새 컬럼 추가
Dataset<Row> withBonus = df.withColumn("bonus", col("salary").multiply(0.1));

// 컬럼 이름 변경
Dataset<Row> renamed = df.withColumnRenamed("name", "employee_name");

// 여러 컬럼 추가
Dataset<Row> enhanced = df
    .withColumn("bonus", col("salary").multiply(0.1))
    .withColumn("total", col("salary").plus(col("bonus")));

// 컬럼 삭제
Dataset<Row> dropped = df.drop("department");
Dataset<Row> droppedMultiple = df.drop("department", "age");

// 컬럼 타입 변환
Dataset<Row> casted = df.withColumn("age", col("age").cast(DataTypes.DoubleType));
```

**정렬**

```java
// 오름차순 정렬
df.orderBy("age").show();
df.orderBy(col("age")).show();
df.sort("age").show();

// 내림차순 정렬
df.orderBy(col("age").desc()).show();

// 다중 컬럼 정렬
df.orderBy(col("department").asc(), col("age").desc()).show();

// null 처리
df.orderBy(col("age").asc_nulls_first()).show();
df.orderBy(col("age").desc_nulls_last()).show();
```

#### 집계 연산

**groupBy**

```java
// 단일 컬럼 그룹화
df.groupBy("department").count().show();

// 여러 집계 함수
df.groupBy("department")
    .agg(
        count("*").alias("count"),
        avg("age").alias("avg_age"),
        max("salary").alias("max_salary"),
        min("salary").alias("min_salary"),
        sum("salary").alias("total_salary")
    )
    .show();

// 여러 컬럼으로 그룹화
df.groupBy("department", "level")
    .agg(avg("salary").alias("avg_salary"))
    .orderBy("department", "level")
    .show();
```

**집계 함수**

```java
import static org.apache.spark.sql.functions.*;

df.agg(
    count("*"),                      // 행 수
    countDistinct("department"),     // 고유값 수
    sum("salary"),                   // 합계
    avg("salary"),                   // 평균
    mean("salary"),                  // 평균 (avg와 동일)
    max("salary"),                   // 최대값
    min("salary"),                   // 최소값
    stddev("salary"),                // 표준편차
    variance("salary"),              // 분산
    first("name"),                   // 첫 값
    last("name"),                    // 마지막 값
    collect_list("department"),      // 리스트로 수집
    collect_set("department")        // 중복 제거 후 수집
).show();
```

**Window 함수**

```java
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;

// Window 정의
WindowSpec window = Window
    .partitionBy("department")
    .orderBy(col("salary").desc());

// 순위 함수
df.withColumn("rank", rank().over(window))
  .withColumn("dense_rank", dense_rank().over(window))
  .withColumn("row_number", row_number().over(window))
  .show();

// 집계 Window
WindowSpec windowAgg = Window.partitionBy("department");

df.withColumn("dept_avg_salary", avg("salary").over(windowAgg))
  .withColumn("salary_diff", col("salary").minus(col("dept_avg_salary")))
  .show();

// 이전/다음 값
df.withColumn("prev_salary", lag("salary", 1).over(window))
  .withColumn("next_salary", lead("salary", 1).over(window))
  .show();

// 누적 합계
WindowSpec runningWindow = Window
    .partitionBy("department")
    .orderBy("hire_date")
    .rowsBetween(Window.unboundedPreceding(), Window.currentRow());

df.withColumn("running_total", sum("salary").over(runningWindow)).show();
```

#### Join

```java
Dataset<Row> employees = spark.read().json("employees.json");
Dataset<Row> departments = spark.read().json("departments.json");

// Inner Join (기본)
Dataset<Row> joined = employees.join(departments, "department_id");

// 조건 명시
Dataset<Row> joined2 = employees.join(
    departments,
    employees.col("department_id").equalTo(departments.col("id"))
);

// Join 유형 지정
employees.join(departments, col("department_id").equalTo(col("id")), "inner");
employees.join(departments, col("department_id").equalTo(col("id")), "left");
employees.join(departments, col("department_id").equalTo(col("id")), "right");
employees.join(departments, col("department_id").equalTo(col("id")), "full");
employees.join(departments, col("department_id").equalTo(col("id")), "left_semi");
employees.join(departments, col("department_id").equalTo(col("id")), "left_anti");

// Cross Join (모든 조합)
employees.crossJoin(departments);
```

**Join 최적화**

```java
import static org.apache.spark.sql.functions.broadcast;

// Broadcast Join - 작은 테이블을 모든 노드에 배포
// 작은 테이블(수십 MB 이하)과 조인 시 셔플 회피
Dataset<Row> optimizedJoin = employees.join(
    broadcast(departments),
    "department_id"
);

// 자동 Broadcast 임계값 설정 (기본 10MB)
spark.conf().set("spark.sql.autoBroadcastJoinThreshold", "50MB");
```

#### Dataset (타입 안전 API)

Java에서 Dataset을 사용하면 컴파일 타임에 타입 체크가 가능합니다.

```java
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;

// Encoder 정의
Encoder<Employee> employeeEncoder = Encoders.bean(Employee.class);

// DataFrame → Dataset 변환
Dataset<Employee> employeeDs = df.as(employeeEncoder);

// 타입 안전한 연산
Dataset<Employee> seniors = employeeDs.filter(
    (FilterFunction<Employee>) emp -> emp.getAge() > 30
);

// map 연산
Dataset<String> names = employeeDs.map(
    (MapFunction<Employee, String>) Employee::getName,
    Encoders.STRING()
);

// flatMap 연산
Dataset<String> words = employeeDs.flatMap(
    (FlatMapFunction<Employee, String>) emp ->
        Arrays.asList(emp.getName().split(" ")).iterator(),
    Encoders.STRING()
);

// reduce 연산
Employee oldest = employeeDs.reduce(
    (ReduceFunction<Employee>) (e1, e2) ->
        e1.getAge() > e2.getAge() ? e1 : e2
);
```

**Encoder 유형**

```java
// 기본 타입
Encoders.STRING()
Encoders.INT()
Encoders.LONG()
Encoders.DOUBLE()
Encoders.BOOLEAN()

// JavaBean
Encoders.bean(Employee.class)

// 튜플
Encoders.tuple(Encoders.STRING(), Encoders.INT())

// Kryo (범용, 직렬화 오버헤드 있음)
Encoders.kryo(MyClass.class)
```

#### DataFrame vs Dataset 선택 기준

| 상황 | 권장 API |
|------|---------|
| SQL 스타일 집계/변환 | DataFrame |
| 컴파일 타임 타입 안전성 필요 | Dataset |
| 복잡한 비즈니스 로직 | Dataset |
| 동적 스키마 | DataFrame |
| Python/R과 호환성 | DataFrame |
| 최고 성능 필요 | DataFrame (Tungsten 최적화) |

#### 실전 예제: 매출 분석

```java
public class SalesAnalysis {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("Sales Analysis")
                .master("local[*]")
                .getOrCreate();

        // 매출 데이터 로드
        Dataset<Row> sales = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("sales.csv");

        // 컬럼: date, product, category, quantity, price

        // 1. 총 매출 계산
        Dataset<Row> withRevenue = sales.withColumn(
            "revenue",
            col("quantity").multiply(col("price"))
        );

        // 2. 카테고리별 매출 집계
        Dataset<Row> categoryRevenue = withRevenue
            .groupBy("category")
            .agg(
                sum("revenue").alias("total_revenue"),
                avg("revenue").alias("avg_revenue"),
                count("*").alias("transaction_count")
            )
            .orderBy(col("total_revenue").desc());

        System.out.println("=== 카테고리별 매출 ===");
        categoryRevenue.show();

        // 3. 월별 추세 분석
        Dataset<Row> monthlyTrend = withRevenue
            .withColumn("month", date_format(col("date"), "yyyy-MM"))
            .groupBy("month")
            .agg(sum("revenue").alias("monthly_revenue"))
            .orderBy("month");

        System.out.println("=== 월별 매출 추세 ===");
        monthlyTrend.show();

        // 4. 상위 판매 상품 (Window 함수 활용)
        WindowSpec productWindow = Window
            .partitionBy("category")
            .orderBy(col("total_quantity").desc());

        Dataset<Row> productRanking = withRevenue
            .groupBy("category", "product")
            .agg(sum("quantity").alias("total_quantity"))
            .withColumn("rank", rank().over(productWindow))
            .filter(col("rank").leq(3));

        System.out.println("=== 카테고리별 Top 3 상품 ===");
        productRanking.show();

        // 5. 결과 저장
        categoryRevenue.write()
            .mode("overwrite")
            .parquet("output/category_revenue");

        spark.stop();
    }
}
```

---

#### Java vs Scala 코드 비교

동일한 로직을 Java와 Scala로 작성한 비교입니다. Java 개발자가 Scala 문서를 읽을 때 참고하세요.

**DataFrame 생성 및 조회**

| 작업 | Java | Scala |
|------|------|-------|
| SparkSession 생성 | `SparkSession.builder().getOrCreate()` | `SparkSession.builder.getOrCreate()` |
| CSV 읽기 | `spark.read().option("header", "true").csv(path)` | `spark.read.option("header", true).csv(path)` |
| 스키마 출력 | `df.printSchema()` | `df.printSchema()` |
| 컬럼 참조 | `col("name")` | `$"name"` 또는 `col("name")` |

**코드 예시 비교**

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

**주요 차이점**

| 구분 | Java | Scala | 설명 |
|------|------|-------|------|
| **타입 선언** | `Dataset<Row>` | `DataFrame` | Scala는 타입 alias 사용 |
| **메서드 호출** | `.method()` | `.method` | Scala는 괄호 생략 가능 |
| **컬럼 참조** | `col("x")` | `$"x"` | Scala는 StringContext 사용 |
| **비교 연산** | `.gt(25)` | `> 25` | Scala는 연산자 오버로딩 |
| **산술 연산** | `.multiply(0.1)` | `* 0.1` | Scala는 연산자 오버로딩 |
| **람다** | `row -> row.getInt(0)` | `row => row.getInt(0)` | 화살표 문법 차이 |
| **익명 함수** | `(MapFunction<T,R>)` | 타입 추론 | Java는 명시적 캐스트 필요 |

**Dataset 타입 안전 코드 비교**

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
case class Employee(name: String, age: Int, department: String)

val ds = df.as[Employee]

val filtered = ds.filter(_.age > 30)

val names = ds.map(_.name)
```

> **Note**: Scala의 case class는 자동으로 Encoder가 생성되어 Java보다 간결합니다.
> Java 17+의 `record`를 사용하면 비슷하게 간결해집니다.

#### 실무 인사이트

**Java 개발자를 위한 실전 가이드**

1. **DataFrame vs Dataset 선택 기준**
   ```
   DataFrame 선택: SQL 스타일 작업, 동적 스키마, ETL 파이프라인
   Dataset 선택: 복잡한 비즈니스 로직, 타입 안전 필수, 도메인 객체 중심
   ```

2. **Java Record 활용 (Java 17+)**
   ```java
   // 기존 POJO 대신 Record 사용으로 간결화
   public record Employee(String name, int age, String department) {}

   Encoder<Employee> encoder = Encoders.bean(Employee.class);
   Dataset<Employee> ds = df.as(encoder);
   ```

3. **성능 최적화 팁**
   - `inferSchema` 대신 명시적 스키마 정의 (대용량 파일에서 성능 향상)
   - 불필요한 `Dataset<T>` 변환 피하기 (직렬화 오버헤드)
   - Parquet 포맷 + 파티셔닝으로 I/O 최소화

4. **흔한 실수와 해결책**

   | 실수 | 결과 | 해결 |
   |------|------|------|
   | `select("*")` 남용 | 불필요한 컬럼 처리 | 필요한 컬럼만 명시 |
   | UDF 과다 사용 | 최적화 불가, 성능 저하 | 내장 함수 우선 사용 |
   | 반복문 내 Action 호출 | N번의 Job 실행 | 한 번에 처리하도록 재구성 |
   | null 체크 누락 | NullPointerException | `isNotNull()` 필터 선행 |

5. **Spring Boot 통합 패턴**
   ```java
   @Service
   public class DataFrameService {
       private final SparkSession spark;

       // 생성자 주입 권장
       public DataFrameService(SparkSession spark) {
           this.spark = spark;
       }

       // 비즈니스 로직에서 DataFrame 반환 대신 DTO 변환
       public List<EmployeeDto> findHighEarners(int threshold) {
           return spark.read().parquet("employees")
               .filter(col("salary").gt(threshold))
               .select("name", "salary")
               .limit(1000)  // Driver 메모리 보호
               .as(Encoders.bean(EmployeeDto.class))
               .collectAsList();
       }
   }
   ```

#### 다음 단계

DataFrame과 Dataset을 이해했다면:

- [Spark SQL](../spark-sql/) - SQL로 DataFrame 쿼리하기
- [Transformation과 Action](../transformations-actions/) - 연산의 실행 시점 이해
