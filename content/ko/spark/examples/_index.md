---
title: 실습 예제
weight: 3
lastmod: "2026-01-09"
---

Spark를 실제로 사용해보는 예제 코드입니다. 각 예제는 독립적으로 실행할 수 있으며, 점진적으로 복잡한 기능을 다룹니다.

#### 예제 목록

**[환경 설정](setup/)**

Java/Spring Boot와 Spark를 통합하는 프로젝트 환경을 구성합니다.

- Gradle 의존성 설정
- SparkSession 빈 구성
- Spring Boot와 Spark 통합 시 주의사항
- 로깅 충돌 해결

**[기본 예제](basic/)**

Spark의 핵심 기능을 활용하는 기본 예제들입니다.

- 데이터 로딩 (CSV, JSON, Parquet)
- 데이터 변환과 필터링
- 집계와 그룹화
- 조인 연산
- SQL 쿼리
- **실제 공개 데이터셋 예제** (NYC Taxi, Kaggle)

**[모니터링 설정](monitoring/)**

프로덕션 환경에서 Spark 애플리케이션을 안정적으로 운영하기 위한 모니터링 설정입니다.

- Spark UI / History Server 설정
- Prometheus + Grafana 연동
- 커스텀 메트릭 구현
- 구조화된 로깅 (Log4j2, JSON)
- 알림 설정

**[Spring Boot 통합](spring-boot/)**

Java/Spring 개발자를 위한 Spark와 Spring Boot 통합 패턴입니다.

- SparkSession 빈 구성 및 프로파일 설정
- 서비스 레이어 패턴 (동기/비동기)
- REST API 통합
- 통합 테스트 작성
- Java vs Scala 비교

**[ETL 파이프라인](etl-pipeline/)**

프로덕션 환경에서 사용 가능한 완전한 ETL 파이프라인 예제입니다.

- 추상 ETL 작업 클래스 (템플릿 메서드 패턴)
- 데이터 정제 유틸리티
- 증분 ETL (CDC, Upsert)
- 에러 처리 및 재시도 로직
- Spring 스케줄링 통합

**[Delta Lake 통합](delta-lake/)**

ACID 트랜잭션과 시간 여행 기능을 갖춘 데이터 레이크를 구축합니다.

- Delta Lake 기본 CRUD 연산
- 시간 여행 (Time Travel)
- 스키마 진화 (Schema Evolution)
- 최적화 (Compaction, Z-Order, Vacuum)
- Change Data Feed (CDC)
- Bronze → Silver → Gold 아키텍처

#### 예제 실행 전 준비

예제를 실행하기 전에 아래 환경을 준비합니다.

**필수 환경**

- **Java 17+**
- **Gradle 8.x** 또는 **Maven 3.x**
- **IDE** (IntelliJ IDEA, VS Code 등)

**공통 Gradle 설정**

모든 예제에서 사용하는 기본 Gradle 설정입니다:

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

위 설정은 Spark와 Spring Boot의 로깅 충돌을 방지하고, 기본적인 의존성을 포함합니다.

#### 다음 단계

예제를 완료했다면:

- [성능 튜닝](../concepts/tuning/) - 코드 최적화 전략
- [배포](../concepts/deployment/) - 프로덕션 배포
- [부록](../appendix/) - 용어 사전, FAQ
