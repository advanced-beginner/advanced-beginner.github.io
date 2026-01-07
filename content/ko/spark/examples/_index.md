---
title: 실습 예제
weight: 3
---

Spark를 실제로 사용해보는 예제 코드입니다.

## 예제 목록

### [환경 설정](setup/)

Java/Spring Boot와 Spark를 통합하는 프로젝트 환경을 구성합니다.

- Gradle 의존성 설정
- SparkSession 빈 구성
- Spring Boot와 Spark 통합 시 주의사항
- 로깅 충돌 해결

### [기본 예제](basic/)

Spark의 핵심 기능을 활용하는 기본 예제들입니다.

- 데이터 로딩 (CSV, JSON, Parquet)
- 데이터 변환과 필터링
- 집계와 그룹화
- 조인 연산
- SQL 쿼리

## 예제 실행 전 준비

### 필수 환경

- **Java 17+**
- **Gradle 8.x** 또는 **Maven 3.x**
- **IDE** (IntelliJ IDEA, VS Code 등)

### 공통 Gradle 설정

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

    // Spring Boot (선택)
    implementation 'org.springframework.boot:spring-boot-starter'

    // Logging (충돌 방지)
    implementation 'org.slf4j:slf4j-simple:2.0.9'
}

configurations.all {
    exclude group: 'org.slf4j', module: 'slf4j-log4j12'
    exclude group: 'log4j', module: 'log4j'
}
```

## 다음 단계

예제를 완료했다면:

- [성능 튜닝](../concepts/tuning/) - 코드 최적화 전략
- [배포](../concepts/deployment/) - 프로덕션 배포
- [부록](../appendix/) - 용어 사전, FAQ
