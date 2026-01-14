---
title: Environment Setup
weight: 1
lastmod: "2026-01-07"
---

# Environment Setup

Configure the environment for using Spark in Java/Spring Boot projects.

## Pure Java Project

The simplest configuration.

### build.gradle

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

    // Kafka integration (optional)
    implementation 'org.apache.spark:spark-sql-kafka-0-10_2.13:3.5.1'

    // MLlib (optional)
    implementation 'org.apache.spark:spark-mllib_2.13:3.5.1'

    // Logging (conflict prevention)
    implementation 'org.slf4j:slf4j-simple:2.0.9'
}

// Logging conflict prevention
configurations.all {
    exclude group: 'org.slf4j', module: 'slf4j-log4j12'
    exclude group: 'log4j', module: 'log4j'
}

application {
    mainClass = 'com.example.SparkApp'
}

// Memory settings for execution
tasks.named('run') {
    jvmArgs = ['-Xmx2g']
}
```

### Basic Structure

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

### SparkApp.java

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

        // Data processing
        Dataset<Row> df = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv("src/main/resources/data/sample.csv");

        df.show();

        spark.stop();
    }
}
```

## Spring Boot Integration

Configuration for using Spark with Spring Boot.

### build.gradle

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

    // Spark - exclude from Spring dependency management
    implementation('org.apache.spark:spark-core_2.13:3.5.1') {
        exclude group: 'org.slf4j'
        exclude group: 'log4j'
    }
    implementation('org.apache.spark:spark-sql_2.13:3.5.1') {
        exclude group: 'org.slf4j'
        exclude group: 'log4j'
    }

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

// Prevent logging conflicts between Spark and Spring
configurations.all {
    exclude group: 'org.slf4j', module: 'slf4j-log4j12'
    exclude group: 'log4j', module: 'log4j'
    exclude group: 'ch.qos.logback', module: 'logback-classic'
}
```

### SparkConfig.java

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

### application.yml

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

### DataService.java

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

### DataController.java

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

        // Convert DataFrame to JSON response
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

## Logging Settings

### log4j2.properties

Spark uses Log4j2. Create `src/main/resources/log4j2.properties`:

```properties
# Root logger
rootLogger.level = WARN
rootLogger.appenderRef.console.ref = console

# Console output
appender.console.type = Console
appender.console.name = console
appender.console.layout.type = PatternLayout
appender.console.layout.pattern = %d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n

# Spark logs
logger.spark.name = org.apache.spark
logger.spark.level = WARN

# Kafka logs
logger.kafka.name = org.apache.kafka
logger.kafka.level = WARN

# Application logs
logger.app.name = com.example
logger.app.level = INFO
```

## Windows Environment Setup

Windows requires Hadoop binaries.

### winutils Setup

```bash
# 1. Download winutils
# Download from https://github.com/steveloughran/winutils

# 2. Create directory
mkdir C:\hadoop\bin

# 3. Copy winutils.exe to C:\hadoop\bin

# 4. Set environment variables
setx HADOOP_HOME C:\hadoop
setx PATH "%PATH%;%HADOOP_HOME%\bin"
```

### Or Set in Code

```java
// Set Hadoop home on Windows
System.setProperty("hadoop.home.dir", "C:\\hadoop");
```

## Troubleshooting

### Logging Conflicts

```
SLF4J: Class path contains multiple SLF4J bindings
```

**Solution**: Exclude conflicting logging libraries in build.gradle

```groovy
configurations.all {
    exclude group: 'org.slf4j', module: 'slf4j-log4j12'
    exclude group: 'log4j', module: 'log4j'
    exclude group: 'ch.qos.logback', module: 'logback-classic'
}
```

### Duplicate SparkSession Creation

```
Only one SparkContext may be running in this JVM
```

**Solution**: Manage SparkSession as singleton or use `getOrCreate()`

```java
// Use getOrCreate
SparkSession spark = SparkSession.builder()
    .appName("App")
    .getOrCreate();  // Reuse existing session
```

### Out of Memory

```
java.lang.OutOfMemoryError: Java heap space
```

**Solution**: Increase memory settings

```groovy
// build.gradle
tasks.named('run') {
    jvmArgs = ['-Xmx4g']
}
```

Or in SparkSession:

```java
.config("spark.driver.memory", "4g")
.config("spark.executor.memory", "4g")
```

### Java Version Compatibility

```
Unsupported class file major version
```

**Solution**: Spark 3.5 supports Java 8, 11, 17. Java 21 is not supported.

```bash
java -version  # Check version
```

## Next Steps

After environment setup is complete:

- [Basic Examples](basic/) - Data processing examples
