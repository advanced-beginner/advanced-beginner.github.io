---
title: Hands-on Examples
weight: 3
lastmod: "2026-01-07"
---

Example code for hands-on experience with Spark.

## Example List

### [Environment Setup](setup/)

Configure project environment for integrating Java/Spring Boot with Spark.

- Gradle dependency configuration
- SparkSession bean setup
- Considerations for Spring Boot and Spark integration
- Resolving logging conflicts

### [Basic Examples](basic/)

Basic examples utilizing Spark's core features.

- Data loading (CSV, JSON, Parquet)
- Data transformation and filtering
- Aggregation and grouping
- Join operations
- SQL queries
- **Real public dataset examples** (NYC Taxi, Kaggle)

### [Monitoring Setup](monitoring/)

Monitoring configuration for stable operation of Spark applications in production environments.

- Spark UI / History Server setup
- Prometheus + Grafana integration
- Custom metrics implementation
- Structured logging (Log4j2, JSON)
- Alert configuration

### [Spring Boot Integration](spring-boot/)

Spark and Spring Boot integration patterns for Java/Spring developers.

- SparkSession bean configuration and profile settings
- Service layer patterns (sync/async)
- REST API integration
- Integration testing
- Java vs Scala comparison

### [ETL Pipeline](etl-pipeline/)

Complete ETL pipeline examples ready for production environments.

- Abstract ETL job class (Template Method pattern)
- Data cleaning utilities
- Incremental ETL (CDC, Upsert)
- Error handling and retry logic
- Spring scheduling integration

## Before Running Examples

### Prerequisites

- **Java 17+**
- **Gradle 8.x** or **Maven 3.x**
- **IDE** (IntelliJ IDEA, VS Code, etc.)

### Common Gradle Setup

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

java {
    sourceCompatibility = '17'
}

repositories {
    mavenCentral()
}

dependencies {
    // Spark
    implementation 'org.apache.spark:spark-core_2.13:3.5.1'
    implementation 'org.apache.spark:spark-sql_2.13:3.5.1'

    // Spring Boot (optional)
    implementation 'org.springframework.boot:spring-boot-starter'

    // Logging (conflict prevention)
    implementation 'org.slf4j:slf4j-simple:2.0.9'
}

configurations.all {
    exclude group: 'org.slf4j', module: 'slf4j-log4j12'
    exclude group: 'log4j', module: 'log4j'
}
```

## Next Steps

After completing the examples:

- [Performance Tuning](../concepts/tuning/) - Code optimization strategies
- [Deployment](../concepts/deployment/) - Production deployment
- [Appendix](../appendix/) - Glossary, FAQ
