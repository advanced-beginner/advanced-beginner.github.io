---
title: Quick Start
weight: 1
lastmod: "2026-01-10"
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **대상 독자**: 빅데이터 처리에 관심이 있는 Java 개발자
> **선수 지식**: Java 기본 문법, SQL 기초, Gradle/Maven 사용 경험
> **이 문서를 읽으면**: 로컬 환경에서 Spark 프로젝트를 생성하고, DataFrame으로 CSV 데이터를 조회/집계할 수 있습니다

{{< callout type="tip" title="TL;DR" >}}
- SparkSession은 Spark의 통합 진입점으로 `SparkSession.builder().getOrCreate()`로 생성합니다
- DataFrame API로 필터링, 집계, SQL 쿼리를 실행할 수 있습니다
- `local[*]` 모드로 로컬 개발 환경에서 바로 테스트할 수 있습니다
{{< /callout >}}

5분 만에 Spark 애플리케이션을 실행하고 데이터를 처리해봅니다. 이 가이드를 따라하면 프로젝트 생성부터 데이터 조회까지 전체 과정을 경험할 수 있습니다.

#### 전체 흐름

아래 다이어그램은 Quick Start의 진행 순서를 보여줍니다:

```mermaid
flowchart LR
    A[1. 프로젝트 생성] --> B[2. 의존성 추가]
    B --> C[3. SparkSession 생성]
    C --> D[4. 데이터 처리]
    D --> E[5. 결과 확인]
```

#### 준비물

시작하기 전에 아래 환경을 준비합니다:

- **Java 17+** (Java 8, 11도 지원하나 17 권장)
- **Gradle** 또는 **Maven**
- **IDE** (IntelliJ IDEA, VS Code 등)

**시작 전 확인**

| 항목 | 확인 명령어 | 예상 결과 |
|------|-------------|-----------|
| Java | `java -version` | `openjdk version "17.x.x"` 이상 |
| Gradle | `gradle --version` | `Gradle 8.x` 이상 |
| Maven (대안) | `mvn --version` | `Apache Maven 3.x.x` |

#### Step 1/5: 프로젝트 생성 (~1분)

Spring Initializr나 IDE에서 Java 프로젝트를 생성합니다. 이 예제에서는 순수 Java 프로젝트로 시작합니다.

```bash
mkdir spark-quickstart
cd spark-quickstart
```

#### Step 2/5: Gradle 설정 (~2분)

`build.gradle` 파일을 생성합니다:

```groovy
plugins {
    id 'java'
    id 'application'
}

group = 'com.example'
version = '1.0.0'

java {
    sourceCompatibility = '17'
}

repositories {
    mavenCentral()
}

dependencies {
    // Spark Core
    implementation 'org.apache.spark:spark-core_2.13:3.5.1'
    // Spark SQL (DataFrame, Dataset 사용을 위해)
    implementation 'org.apache.spark:spark-sql_2.13:3.5.1'

    // 로깅
    implementation 'org.slf4j:slf4j-simple:2.0.9'
}

application {
    mainClass = 'com.example.SparkQuickStart'
}

// Spark JAR 충돌 방지
configurations.all {
    exclude group: 'org.slf4j', module: 'slf4j-log4j12'
}
```

> **버전 참고:** `spark-core_2.13`에서 `2.13`은 Scala 버전입니다. Java에서 사용해도 Scala 런타임이 필요하기 때문에 명시합니다.

**Maven 사용 시**

Maven을 선호한다면 아래 `pom.xml` 설정을 사용합니다:

`pom.xml` 파일:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>spark-quickstart</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <spark.version>3.5.1</spark.version>
        <scala.version>2.13</scala.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-core_${scala.version}</artifactId>
            <version>${spark.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-sql_${scala.version}</artifactId>
            <version>${spark.version}</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.9</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <mainClass>com.example.SparkQuickStart</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

실행:
```bash
mvn compile exec:java
```

#### Step 3/5: 샘플 데이터 생성 (~1분)

`src/main/resources/employees.csv` 파일을 생성합니다:

```csv
id,name,department,salary
1,김철수,Engineering,5000
2,이영희,Marketing,4500
3,박민수,Engineering,5500
4,정수진,Sales,4000
5,최동욱,Engineering,6000
6,한미영,Marketing,4800
7,강준혁,Sales,4200
8,윤서연,Engineering,5200
```

#### Step 4/5: Spark 애플리케이션 작성 (~3분)

`src/main/java/com/example/SparkQuickStart.java`:

```java
package com.example;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import static org.apache.spark.sql.functions.*;

public class SparkQuickStart {

    public static void main(String[] args) {
        // 1. SparkSession 생성 - Spark의 진입점
        SparkSession spark = SparkSession.builder()
                .appName("Quick Start")
                .master("local[*]")  // 로컬 모드, 모든 코어 사용
                .getOrCreate();

        // 로그 레벨 조정 (너무 많은 로그 방지)
        spark.sparkContext().setLogLevel("WARN");

        System.out.println("=== Spark Quick Start ===\n");

        // 2. CSV 파일 읽기
        Dataset<Row> employees = spark.read()
                .option("header", "true")       // 첫 줄을 헤더로
                .option("inferSchema", "true")  // 타입 자동 추론
                .csv("src/main/resources/employees.csv");

        // 3. 데이터 확인
        System.out.println("=== 전체 직원 데이터 ===");
        employees.show();

        // 4. 스키마 확인
        System.out.println("=== 스키마 ===");
        employees.printSchema();

        // 5. 필터링 - 연봉 5000 이상
        System.out.println("=== 연봉 5000 이상 직원 ===");
        employees.filter(col("salary").geq(5000)).show();

        // 6. 집계 - 부서별 평균 연봉
        System.out.println("=== 부서별 평균 연봉 ===");
        employees.groupBy("department")
                .agg(
                    avg("salary").alias("avg_salary"),
                    count("*").alias("employee_count")
                )
                .orderBy(desc("avg_salary"))
                .show();

        // 7. SQL 사용 - 동일한 작업을 SQL로
        employees.createOrReplaceTempView("employees");

        System.out.println("=== SQL로 조회: Engineering 부서 ===");
        spark.sql("""
            SELECT name, salary
            FROM employees
            WHERE department = 'Engineering'
            ORDER BY salary DESC
            """).show();

        // 8. SparkSession 종료
        spark.stop();

        System.out.println("=== 완료 ===");
    }
}
```

#### Step 5/5: 실행 (~1분)

```bash
./gradlew run
```

Windows의 경우:
```bash
gradlew.bat run
```

#### 예상 출력

정상적으로 실행되면 아래와 같은 출력을 확인할 수 있습니다:

```
=== Spark Quick Start ===

=== 전체 직원 데이터 ===
+---+------+-----------+------+
| id|  name| department|salary|
+---+------+-----------+------+
|  1|김철수|Engineering|  5000|
|  2|이영희|  Marketing|  4500|
|  3|박민수|Engineering|  5500|
|  4|정수진|      Sales|  4000|
|  5|최동욱|Engineering|  6000|
|  6|한미영|  Marketing|  4800|
|  7|강준혁|      Sales|  4200|
|  8|윤서연|Engineering|  5200|
+---+------+-----------+------+

=== 스키마 ===
root
 |-- id: integer (nullable = true)
 |-- name: string (nullable = true)
 |-- department: string (nullable = true)
 |-- salary: integer (nullable = true)

=== 연봉 5000 이상 직원 ===
+---+------+-----------+------+
| id|  name| department|salary|
+---+------+-----------+------+
|  1|김철수|Engineering|  5000|
|  3|박민수|Engineering|  5500|
|  5|최동욱|Engineering|  6000|
|  8|윤서연|Engineering|  5200|
+---+------+-----------+------+

=== 부서별 평균 연봉 ===
+-----------+----------+--------------+
| department|avg_salary|employee_count|
+-----------+----------+--------------+
|Engineering|    5425.0|             4|
|  Marketing|    4650.0|             2|
|      Sales|    4100.0|             2|
+-----------+----------+--------------+

=== SQL로 조회: Engineering 부서 ===
+------+------+
|  name|salary|
+------+------+
|최동욱|  6000|
|박민수|  5500|
|윤서연|  5200|
|김철수|  5000|
+------+------+

=== 완료 ===
```

**축하합니다!** 첫 번째 Spark 애플리케이션을 성공적으로 실행했습니다.

---

#### 프로덕션 수준 코드

실제 운영 환경에서는 예외 처리와 리소스 정리가 필수입니다:

```java
package com.example;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.AnalysisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.spark.sql.functions.*;

public class SparkQuickStartProduction {
    private static final Logger logger = LoggerFactory.getLogger(SparkQuickStartProduction.class);

    public static void main(String[] args) {
        SparkSession spark = null;
        int exitCode = 0;

        try {
            // SparkSession 생성
            spark = SparkSession.builder()
                    .appName("Quick Start Production")
                    .master("local[*]")
                    .config("spark.sql.session.timeZone", "Asia/Seoul")
                    .getOrCreate();

            spark.sparkContext().setLogLevel("WARN");
            logger.info("SparkSession 생성 완료");

            // 데이터 읽기 (에러 모드 설정)
            Dataset<Row> employees = spark.read()
                    .option("header", "true")
                    .option("inferSchema", "true")
                    .option("mode", "FAILFAST")  // 잘못된 데이터 시 즉시 실패
                    .csv("src/main/resources/employees.csv");

            // 스키마 검증
            validateSchema(employees);

            // 데이터 처리
            Dataset<Row> result = employees
                    .filter(col("salary").isNotNull())
                    .groupBy("department")
                    .agg(
                        avg("salary").alias("avg_salary"),
                        count("*").alias("count")
                    );

            // 결과 출력
            result.show();
            logger.info("처리 완료: {} 개 부서", result.count());

        } catch (AnalysisException e) {
            logger.error("데이터 분석 오류: {}", e.getMessage());
            exitCode = 1;
        } catch (Exception e) {
            logger.error("Spark 작업 실패", e);
            exitCode = 1;
        } finally {
            // 리소스 정리 (항상 실행)
            if (spark != null) {
                spark.stop();
                logger.info("SparkSession 종료");
            }
        }

        System.exit(exitCode);
    }

    private static void validateSchema(Dataset<Row> df) {
        String[] requiredColumns = {"id", "name", "department", "salary"};
        for (String col : requiredColumns) {
            if (!java.util.Arrays.asList(df.columns()).contains(col)) {
                throw new IllegalArgumentException("필수 컬럼 누락: " + col);
            }
        }
    }
}
```

**프로덕션 코드의 핵심 포인트:**

아래 표는 프로덕션 환경에서 필수적인 코드 패턴을 정리한 것입니다:

| 항목 | 설명 |
|------|------|
| `try-finally` | SparkSession이 항상 정리되도록 보장 |
| `option("mode", "FAILFAST")` | 잘못된 데이터 발견 시 즉시 실패 |
| `Logger` | System.out 대신 구조화된 로깅 |
| `exitCode` | 스크립트/CI 연동을 위한 종료 코드 |
| `validateSchema` | 런타임 스키마 검증 |

각 항목은 운영 환경에서 발생할 수 있는 문제를 예방하거나 디버깅을 용이하게 합니다.

---

#### 무엇이 일어났나요?

위 코드에서 각 단계가 어떻게 동작하는지 살펴봅니다.

**1. SparkSession 생성**

```java
SparkSession spark = SparkSession.builder()
        .appName("Quick Start")
        .master("local[*]")
        .getOrCreate();
```

- `SparkSession`: Spark 2.0부터 사용하는 통합 진입점. 이전의 `SparkContext`, `SQLContext`, `HiveContext`를 모두 통합
- `appName`: Spark UI에 표시될 애플리케이션 이름
- `master("local[*]")`: 로컬 모드로 실행, `*`는 모든 가용 CPU 코어 사용
  - `local`: 단일 스레드
  - `local[4]`: 4개 스레드
  - `local[*]`: 모든 코어
  - 클러스터 환경에서는 `spark://master:7077`, `yarn` 등 사용

**2. 데이터 읽기**

```java
Dataset<Row> employees = spark.read()
        .option("header", "true")
        .option("inferSchema", "true")
        .csv("src/main/resources/employees.csv");
```

- `Dataset<Row>`: Spark의 분산 데이터 컬렉션. Java에서는 `Row` 타입의 Dataset이 DataFrame 역할
- `option("inferSchema", "true")`: 데이터를 샘플링하여 각 컬럼의 타입을 자동 추론
- Spark는 CSV, JSON, Parquet, JDBC 등 다양한 데이터 소스 지원

**3. DataFrame 연산**

```java
employees.filter(col("salary").geq(5000))
```

- Java의 Stream API와 유사하지만, 분산 처리됨
- `filter`, `select`, `groupBy` 등은 **Transformation** — 지연 평가됨
- `show`, `collect`, `count` 등은 **Action** — 실제 연산 수행

**4. SQL 사용**

```java
employees.createOrReplaceTempView("employees");
spark.sql("SELECT * FROM employees WHERE ...");
```

- DataFrame을 임시 뷰로 등록하면 SQL 쿼리 가능
- 기존 SQL 지식을 그대로 활용할 수 있음
- 내부적으로 동일한 실행 엔진(Catalyst Optimizer) 사용

---

#### Java 개발자를 위한 비교

기존 Java 코드와 Spark 코드를 비교하면 유사점과 차이점을 쉽게 이해할 수 있습니다.

**Java Stream vs Spark DataFrame**

```java
// Java Stream (단일 JVM)
List<Employee> highEarners = employees.stream()
        .filter(e -> e.getSalary() >= 5000)
        .collect(Collectors.toList());

// Spark DataFrame (분산 처리)
Dataset<Row> highEarners = employees
        .filter(col("salary").geq(5000));
```

두 코드는 매우 유사하지만:
- **Java Stream**: 단일 JVM에서 실행, 메모리 제한
- **Spark DataFrame**: 여러 노드에 분산, 수십TB 데이터도 처리 가능

---

#### 트러블슈팅

Spark 실행 시 자주 발생하는 문제와 해결 방법입니다.

**로그가 너무 많아요**

Spark는 기본적으로 많은 로그를 출력합니다. `log4j2.properties` 파일을 `src/main/resources`에 추가하거나:

```java
spark.sparkContext().setLogLevel("WARN");  // 또는 "ERROR"
```

**Java 버전 오류**

Spark 3.5는 Java 8, 11, 17을 지원합니다. Java 21은 아직 공식 지원되지 않으니 주의하세요.

```
Error: A JNI error has occurred
```

→ Java 버전 확인: `java -version`

**메모리 부족 (OutOfMemoryError)**

로컬 실행 시 기본 메모리가 부족할 수 있습니다:

```bash
./gradlew run -Dorg.gradle.jvmargs="-Xmx2g"
```

또는 `build.gradle`에 추가:

```groovy
application {
    applicationDefaultJvmArgs = ['-Xmx2g']
}
```

**Windows에서 Hadoop 관련 오류**

Windows에서 실행 시 `winutils.exe` 관련 경고가 나올 수 있습니다. 기능에는 영향 없지만, 해결하려면:

1. [winutils](https://github.com/steveloughran/winutils)에서 다운로드
2. `C:\hadoop\bin\winutils.exe` 경로에 저장
3. 환경 변수 `HADOOP_HOME=C:\hadoop` 설정

---

#### Spark UI 확인하기

Spark 애플리케이션 실행 중 `http://localhost:4040`에 접속하면 Spark UI를 확인할 수 있습니다:

- **Jobs**: 실행된 Job 목록과 상태
- **Stages**: 각 Stage의 Task 분배 현황
- **Storage**: 캐시된 RDD/DataFrame 정보
- **Environment**: Spark 설정값

> **참고:** 애플리케이션이 종료되면 UI도 종료됩니다. 종료 전에 확인하려면 `spark.stop()` 전에 `Thread.sleep(60000);`을 추가하세요.

---

#### 다음 단계

Quick Start를 완료했다면, 학습 목표에 따라 다음 문서를 선택하세요:

| 목표 | 추천 문서 |
|------|----------|
| Spark 내부 동작 이해 | [아키텍처](../concepts/architecture/) |
| RDD 기초 학습 | [RDD 기초](../concepts/rdd/) |
| DataFrame 심화 | [DataFrame과 Dataset](../concepts/dataframe-dataset/) |
| Spring Boot 통합 | [환경 설정](../examples/setup/) |

Spark의 전체적인 동작 원리를 이해하려면 아키텍처 문서를, 실습 위주로 진행하려면 환경 설정 문서를 먼저 읽는 것을 권장합니다.
