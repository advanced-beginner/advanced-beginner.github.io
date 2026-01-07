---
title: Quick Start
weight: 1
---

# Quick Start

5분 만에 Spark 애플리케이션을 실행하고 데이터를 처리해봅니다.

## 전체 흐름

```
1. 프로젝트 생성 → 2. 의존성 추가 → 3. SparkSession 생성 → 4. 데이터 처리 → 5. 결과 확인
```

## 준비물

- **Java 17+** (Java 8, 11도 지원하나 17 권장)
- **Gradle** 또는 **Maven**
- **IDE** (IntelliJ IDEA, VS Code 등)

## Step 1: 프로젝트 생성

Spring Initializr나 IDE에서 Java 프로젝트를 생성합니다. 이 예제에서는 순수 Java 프로젝트로 시작합니다.

```bash
mkdir spark-quickstart
cd spark-quickstart
```

## Step 2: Gradle 설정

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

## Step 3: 샘플 데이터 생성

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

## Step 4: Spark 애플리케이션 작성

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

## Step 5: 실행

```bash
./gradlew run
```

Windows의 경우:
```bash
gradlew.bat run
```

## 예상 출력

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

## 무엇이 일어났나요?

### 1. SparkSession 생성

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

### 2. 데이터 읽기

```java
Dataset<Row> employees = spark.read()
        .option("header", "true")
        .option("inferSchema", "true")
        .csv("src/main/resources/employees.csv");
```

- `Dataset<Row>`: Spark의 분산 데이터 컬렉션. Java에서는 `Row` 타입의 Dataset이 DataFrame 역할
- `option("inferSchema", "true")`: 데이터를 샘플링하여 각 컬럼의 타입을 자동 추론
- Spark는 CSV, JSON, Parquet, JDBC 등 다양한 데이터 소스 지원

### 3. DataFrame 연산

```java
employees.filter(col("salary").geq(5000))
```

- Java의 Stream API와 유사하지만, 분산 처리됨
- `filter`, `select`, `groupBy` 등은 **Transformation** — 지연 평가됨
- `show`, `collect`, `count` 등은 **Action** — 실제 연산 수행

### 4. SQL 사용

```java
employees.createOrReplaceTempView("employees");
spark.sql("SELECT * FROM employees WHERE ...");
```

- DataFrame을 임시 뷰로 등록하면 SQL 쿼리 가능
- 기존 SQL 지식을 그대로 활용할 수 있음
- 내부적으로 동일한 실행 엔진(Catalyst Optimizer) 사용

---

## Java 개발자를 위한 비교

### Java Stream vs Spark DataFrame

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

## 트러블슈팅

### 로그가 너무 많아요

Spark는 기본적으로 많은 로그를 출력합니다. `log4j2.properties` 파일을 `src/main/resources`에 추가하거나:

```java
spark.sparkContext().setLogLevel("WARN");  // 또는 "ERROR"
```

### Java 버전 오류

Spark 3.5는 Java 8, 11, 17을 지원합니다. Java 21은 아직 공식 지원되지 않으니 주의하세요.

```
Error: A JNI error has occurred
```

→ Java 버전 확인: `java -version`

### 메모리 부족 (OutOfMemoryError)

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

### Windows에서 Hadoop 관련 오류

Windows에서 실행 시 `winutils.exe` 관련 경고가 나올 수 있습니다. 기능에는 영향 없지만, 해결하려면:

1. [winutils](https://github.com/steveloughran/winutils)에서 다운로드
2. `C:\hadoop\bin\winutils.exe` 경로에 저장
3. 환경 변수 `HADOOP_HOME=C:\hadoop` 설정

---

## Spark UI 확인하기

Spark 애플리케이션 실행 중 `http://localhost:4040`에 접속하면 Spark UI를 확인할 수 있습니다:

- **Jobs**: 실행된 Job 목록과 상태
- **Stages**: 각 Stage의 Task 분배 현황
- **Storage**: 캐시된 RDD/DataFrame 정보
- **Environment**: Spark 설정값

> **참고:** 애플리케이션이 종료되면 UI도 종료됩니다. 종료 전에 확인하려면 `spark.stop()` 전에 `Thread.sleep(60000);`을 추가하세요.

---

## 다음 단계

Quick Start를 완료했다면, 다음 단계로 진행하세요:

| 목표 | 추천 문서 |
|------|----------|
| Spark 내부 동작 이해 | [아키텍처](../concepts/architecture/) |
| RDD 기초 학습 | [RDD 기초](../concepts/rdd/) |
| DataFrame 심화 | [DataFrame과 Dataset](../concepts/dataframe-dataset/) |
| Spring Boot 통합 | [환경 설정](../examples/setup/) |
