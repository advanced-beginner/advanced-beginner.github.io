---
title: 환경 설정
weight: 1
lastmod: "2026-01-10"
author:
  name: Advanced Beginner
  github: advanced-beginner
---

{{< callout type="tip" title="TL;DR" >}}
- **Gradle 의존성**: `spark-core_2.13:3.5.1`, `spark-sql_2.13:3.5.1`
- **로깅 충돌 방지**: `exclude group: 'org.slf4j'` 필수
- **SparkSession**: `getOrCreate()`로 싱글톤 관리
- **Windows**: winutils 설정 필요
{{< /callout >}}

## 대상 독자 및 선수 지식

| 구분 | 내용 |
|------|------|
| **대상 독자** | Java/Spring Boot 프로젝트에 Spark를 도입하려는 개발자 |
| **선수 지식** | Java 17, Gradle 기본, Spring Boot 경험 (선택) |
| **학습 목표** | 로컬 환경에서 Spark 애플리케이션을 실행할 수 있다 |
| **예상 소요 시간** | 약 15분 |

---

Java/Spring Boot 프로젝트에서 Spark를 사용하기 위한 환경을 구성합니다.

## 순수 Java 프로젝트

가장 간단한 구성입니다.

**build.gradle**

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

    // Spark SQL (DataFrame API)
    implementation 'org.apache.spark:spark-sql_2.13:3.5.1'

    // Kafka 연동 (선택)
    implementation 'org.apache.spark:spark-sql-kafka-0-10_2.13:3.5.1'

    // MLlib (선택)
    implementation 'org.apache.spark:spark-mllib_2.13:3.5.1'

    // 로깅 (충돌 방지)
    implementation 'org.slf4j:slf4j-simple:2.0.9'
}

// 로깅 충돌 방지
configurations.all {
    exclude group: 'org.slf4j', module: 'slf4j-log4j12'
    exclude group: 'log4j', module: 'log4j'
}

application {
    mainClass = 'com.example.SparkApp'
}

// 실행 시 메모리 설정
tasks.named('run') {
    jvmArgs = ['-Xmx2g']
}
```

**기본 구조**

```
src/
├── main/
│   ├── java/
│   │   └── com/example/
│   │       └── SparkApp.java
│   └── resources/
│       └── data/
│           └── sample.csv
```

**SparkApp.java**

```java
package com.example;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class SparkApp {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("My Spark App")
                .master("local[*]")
                .config("spark.driver.memory", "2g")
                .getOrCreate();

        spark.sparkContext().setLogLevel("WARN");

        // 데이터 처리
        Dataset<Row> df = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("src/main/resources/data/sample.csv");

        df.show();

        spark.stop();
    }
}
```

{{< callout type="info" title="핵심 포인트: 순수 Java 설정" >}}
- **Scala 버전 일치**: 모든 Spark 의존성은 동일한 Scala 버전(2.13) 사용
- **로깅 충돌 제거**: `exclude group: 'org.slf4j'`로 충돌 방지
- **메모리 설정**: `-Xmx2g` 이상 권장 (데이터 크기에 따라 조정)
- **local[\*]**: 모든 CPU 코어 사용 (로컬 개발 모드)
{{< /callout >}}

## Spring Boot 통합

Spring Boot와 Spark를 함께 사용하는 구성입니다.

**build.gradle**

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
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
    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'

    // Spark - Spring 의존성 관리에서 제외
    implementation('org.apache.spark:spark-core_2.13:3.5.1') {
        exclude group: 'org.slf4j'
        exclude group: 'log4j'
    }
    implementation('org.apache.spark:spark-sql_2.13:3.5.1') {
        exclude group: 'org.slf4j'
        exclude group: 'log4j'
    }

    // 테스트
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

// Spark와 Spring의 로깅 충돌 방지
configurations.all {
    exclude group: 'org.slf4j', module: 'slf4j-log4j12'
    exclude group: 'log4j', module: 'log4j'
    exclude group: 'ch.qos.logback', module: 'logback-classic'
}
```

**SparkConfig.java**

```java
package com.example.config;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

@Configuration
public class SparkConfig {

    @Value("${spark.app.name:SpringSparkApp}")
    private String appName;

    @Value("${spark.master:local[*]}")
    private String master;

    @Value("${spark.driver.memory:2g}")
    private String driverMemory;

    private SparkSession sparkSession;

    @Bean
    public SparkConf sparkConf() {
        return new SparkConf()
                .setAppName(appName)
                .setMaster(master)
                .set("spark.driver.memory", driverMemory)
                .set("spark.sql.adaptive.enabled", "true");
    }

    @Bean
    public SparkSession sparkSession(SparkConf sparkConf) {
        sparkSession = SparkSession.builder()
                .config(sparkConf)
                .getOrCreate();

        sparkSession.sparkContext().setLogLevel("WARN");
        return sparkSession;
    }

    @PreDestroy
    public void closeSparkSession() {
        if (sparkSession != null) {
            sparkSession.stop();
        }
    }
}
```

**application.yml**

```yaml
spring:
  application:
    name: spark-spring-app

spark:
  app:
    name: ${spring.application.name}
  master: local[*]
  driver:
    memory: 2g

server:
  port: 8080
```

**DataService.java**

```java
package com.example.service;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Service;

import static org.apache.spark.sql.functions.*;

@Service
public class DataService {

    private final SparkSession spark;

    public DataService(SparkSession spark) {
        this.spark = spark;
    }

    public Dataset<Row> loadData(String path) {
        return spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(path);
    }

    public Dataset<Row> aggregateByCategory(Dataset<Row> df, String categoryCol, String valueCol) {
        return df.groupBy(categoryCol)
                .agg(
                    count("*").alias("count"),
                    sum(valueCol).alias("total"),
                    avg(valueCol).alias("average")
                )
                .orderBy(col("total").desc());
    }
}
```

**DataController.java**

```java
package com.example.controller;

import com.example.service.DataService;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/data")
public class DataController {

    private final DataService dataService;

    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/summary")
    public List<Map<String, Object>> getSummary(
            @RequestParam String path,
            @RequestParam String category,
            @RequestParam String value) {

        Dataset<Row> df = dataService.loadData(path);
        Dataset<Row> result = dataService.aggregateByCategory(df, category, value);

        // DataFrame을 JSON 응답으로 변환
        return result.collectAsList().stream()
                .map(row -> {
                    return Map.of(
                        "category", row.getAs(category),
                        "count", row.getAs("count"),
                        "total", row.getAs("total"),
                        "average", row.getAs("average")
                    );
                })
                .collect(Collectors.toList());
    }
}
```

{{< callout type="info" title="핵심 포인트: Spring Boot 통합" >}}
- **@Configuration + @Bean**: SparkSession을 Spring Bean으로 관리
- **@PreDestroy**: 애플리케이션 종료 시 SparkSession 정리
- **application.yml**: 환경별 Spark 설정 분리 (local, production)
- **의존성 주입**: Service 레이어에서 SparkSession 주입받아 사용
{{< /callout >}}

## 로깅 설정

**log4j2.properties**

Spark는 Log4j2를 사용합니다. `src/main/resources/log4j2.properties`:

```properties
# 루트 로거
rootLogger.level = WARN
rootLogger.appenderRef.console.ref = console

# 콘솔 출력
appender.console.type = Console
appender.console.name = console
appender.console.layout.type = PatternLayout
appender.console.layout.pattern = %d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n

# Spark 로그
logger.spark.name = org.apache.spark
logger.spark.level = WARN

# Kafka 로그
logger.kafka.name = org.apache.kafka
logger.kafka.level = WARN

# 애플리케이션 로그
logger.app.name = com.example
logger.app.level = INFO
```

## Windows 환경 설정

Windows에서는 Hadoop 바이너리가 필요합니다.

**winutils 설정**

```bash
# 1. winutils 다운로드
# https://github.com/steveloughran/winutils 에서 다운로드

# 2. 디렉토리 생성
mkdir C:\hadoop\bin

# 3. winutils.exe를 C:\hadoop\bin에 복사

# 4. 환경 변수 설정
setx HADOOP_HOME C:\hadoop
setx PATH "%PATH%;%HADOOP_HOME%\bin"
```

**또는 코드에서 설정**

```java
// Windows에서 Hadoop 홈 설정
System.setProperty("hadoop.home.dir", "C:\\hadoop");
```

## 트러블슈팅

**로깅 충돌**

```
SLF4J: Class path contains multiple SLF4J bindings
```

**해결**: build.gradle에서 충돌하는 로깅 라이브러리 제외

```groovy
configurations.all {
    exclude group: 'org.slf4j', module: 'slf4j-log4j12'
    exclude group: 'log4j', module: 'log4j'
    exclude group: 'ch.qos.logback', module: 'logback-classic'
}
```

**SparkSession 중복 생성**

```
Only one SparkContext may be running in this JVM
```

**해결**: SparkSession을 싱글톤으로 관리하거나 `getOrCreate()` 사용

```java
// getOrCreate 사용
SparkSession spark = SparkSession.builder()
    .appName("App")
    .getOrCreate();  // 기존 세션 재사용
```

**메모리 부족**

```
java.lang.OutOfMemoryError: Java heap space
```

**해결**: 메모리 설정 증가

```groovy
// build.gradle
tasks.named('run') {
    jvmArgs = ['-Xmx4g']
}
```

또는 SparkSession에서:

```java
.config("spark.driver.memory", "4g")
.config("spark.executor.memory", "4g")
```

**Java 버전 호환성**

```
Unsupported class file major version
```

**해결**: Spark 3.5는 Java 8, 11, 17 지원. Java 21은 미지원.

```bash
java -version  # 버전 확인
```

{{< callout type="info" title="핵심 포인트: 트러블슈팅" >}}
- **SLF4J 충돌**: `configurations.all { exclude group: 'org.slf4j' }`로 해결
- **SparkContext 중복**: `getOrCreate()` 메서드로 기존 세션 재사용
- **메모리 부족**: `spark.driver.memory`, `spark.executor.memory` 증가
- **Java 버전**: Spark 3.5는 Java 8, 11, 17 지원 (21은 미지원)
{{< /callout >}}

## 다음 단계

환경 설정이 완료되었다면:

- [기본 예제](basic/) - 데이터 처리 예제
