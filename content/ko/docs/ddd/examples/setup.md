---
title: 프로젝트 설정
weight: 1
lastmod: 2026-01-10
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

# 프로젝트 설정

{{% notice style="primary" title="TL;DR" %}}
- DDD 계층형 아키텍처(Domain, Application, Infrastructure, Interfaces)로 패키지 구성
- Spring Boot 3.2.x + Spring Kafka + JPA 기반 의존성 설정
- AggregateRoot, DomainEvent, Entity 기반 클래스 구현
- Docker Compose로 Kafka + PostgreSQL 개발 환경 구축
{{% /notice %}}

## 대상 독자 및 선수 지식

| 항목 | 요구 수준 |
|------|----------|
| **대상 독자** | DDD 패턴을 Spring Boot에 적용하려는 백엔드 개발자 |
| **Java** | Java 17+ 문법, Record, Stream API |
| **Spring Boot** | Spring Boot 기본 구조, DI, @Service, @Repository |
| **Gradle** | Kotlin DSL 기본 문법 |
| **Docker** | docker-compose up/down 실행 경험 |

DDD 예제 프로젝트의 구조와 의존성을 설정합니다.

## 프로젝트 구조

```
order-service/
├── src/main/java/com/example/order/
│   ├── domain/                    # 도메인 계층
│   │   ├── model/                 # Aggregate, Entity, VO
│   │   │   ├── Order.java
│   │   │   ├── OrderLine.java
│   │   │   ├── OrderId.java
│   │   │   └── Money.java
│   │   ├── event/                 # 도메인 이벤트
│   │   │   ├── OrderCreatedEvent.java
│   │   │   └── OrderConfirmedEvent.java
│   │   ├── repository/            # Repository 인터페이스
│   │   │   └── OrderRepository.java
│   │   └── service/               # 도메인 서비스
│   │       └── OrderValidator.java
│   │
│   ├── application/               # 응용 계층
│   │   ├── service/               # Application Service
│   │   │   └── OrderService.java
│   │   ├── command/               # Command 객체
│   │   │   └── CreateOrderCommand.java
│   │   └── dto/                   # DTO
│   │       └── OrderResponse.java
│   │
│   ├── infrastructure/            # 인프라 계층
│   │   ├── persistence/           # JPA 구현
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   └── mapper/
│   │   └── event/                 # 이벤트 발행
│   │       └── KafkaEventPublisher.java
│   │
│   └── interfaces/                # 인터페이스 계층 (API)
│       └── rest/
│           └── OrderController.java
│
├── build.gradle.kts
└── docker-compose.yml
```

## 의존성 설정

### build.gradle.kts

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Kafka (이벤트 발행용)
    implementation("org.springframework.kafka:spring-kafka")

    // Database
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    // Lombok (선택)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

## 계층별 역할

```mermaid
flowchart TB
    subgraph Interfaces["interfaces (API)"]
        CTRL["Controller"]
    end

    subgraph Application["application"]
        SVC["Application Service"]
        CMD["Command/Query"]
    end

    subgraph Domain["domain"]
        MODEL["Aggregate/Entity/VO"]
        REPO_IF["Repository Interface"]
        DSVC["Domain Service"]
        EVT["Domain Event"]
    end

    subgraph Infrastructure["infrastructure"]
        REPO_IMPL["Repository 구현"]
        JPA["JPA Entity"]
        KAFKA["Event Publisher"]
    end

    CTRL --> SVC
    SVC --> MODEL
    SVC --> REPO_IF
    SVC --> DSVC
    MODEL --> EVT
    REPO_IF -.->|구현| REPO_IMPL
    REPO_IMPL --> JPA
    EVT -.->|발행| KAFKA
```

> **다이어그램 설명**: Interfaces 계층의 Controller가 Application 계층의 Service를 호출하고, Service는 Domain 계층의 Aggregate/Entity와 Repository Interface를 사용합니다. Infrastructure 계층은 Repository 구현체와 JPA Entity, Kafka Publisher를 제공하며, Domain Event는 Infrastructure를 통해 발행됩니다.

### 의존성 규칙

```mermaid
flowchart LR
    I[Interfaces] --> A[Application]
    A --> D[Domain]
    INF[Infrastructure] --> D
```

> **다이어그램 설명**: 의존성은 항상 안쪽(Domain)을 향합니다. Interfaces에서 Application으로, Application에서 Domain으로 의존하며, Infrastructure는 Domain의 인터페이스를 구현합니다.

| 규칙 | 설명 |
|------|------|
| **Domain은 독립적** | 다른 계층에 의존하지 않음 |
| **안쪽으로만 의존** | Interfaces → Application → Domain |
| **Infrastructure는 Domain 구현** | Repository Interface의 구현체 제공 |

{{% notice style="tip" title="핵심 포인트: 계층별 역할" %}}
- **Domain 계층**: 비즈니스 로직의 핵심. 외부 의존성 없이 순수 Java로 구현
- **Application 계층**: Use Case 조율. 트랜잭션 경계, 이벤트 발행 담당
- **Infrastructure 계층**: 기술 구현. JPA, Kafka 등 외부 기술 의존
- **Interfaces 계층**: 외부 연동. REST API, 메시지 핸들러 등
{{% /notice %}}

## application.yml

```yaml
spring:
  application:
    name: order-service

  datasource:
    url: jdbc:h2:mem:orderdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  h2:
    console:
      enabled: true
      path: /h2-console

  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

## 핵심 기반 클래스

### AggregateRoot

```java
package com.example.order.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AggregateRoot<ID> {

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public abstract ID getId();

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
```

### DomainEvent

```java
package com.example.order.domain.event;

import java.time.Instant;
import java.util.UUID;

public abstract class DomainEvent {

    private final String eventId;
    private final Instant occurredAt;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public abstract String getAggregateId();
}
```

### Entity 기반 클래스

```java
package com.example.order.domain.model;

import java.util.Objects;

public abstract class Entity<ID> {

    public abstract ID getId();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(getId(), entity.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
```

{{% notice style="tip" title="핵심 포인트: 기반 클래스" %}}
- **AggregateRoot**: 도메인 이벤트 수집 및 관리. 모든 Aggregate의 부모 클래스
- **DomainEvent**: 이벤트 ID와 발생 시각 자동 생성. 불변 객체로 설계
- **Entity**: ID 기반 동등성 비교. equals/hashCode는 ID만 사용
{{% /notice %}}

## Docker Compose (개발환경)

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    container_name: order-postgres
    environment:
      POSTGRES_DB: orderdb
      POSTGRES_USER: order
      POSTGRES_PASSWORD: order123
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

  kafka:
    image: apache/kafka:3.6.1
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      CLUSTER_ID: MkU3OEVBNTcwNTJENDM2Qk

volumes:
  postgres-data:
```

## 패키지 구조 원칙

### 1. 도메인 중심 패키지

```
// ❌ 기술 중심
com.example.order
├── controller
├── service
├── repository
└── entity

// ✅ 도메인 중심
com.example.order
├── domain           # 핵심 비즈니스 로직
├── application      # 유스케이스
├── infrastructure   # 기술 구현
└── interfaces       # 외부 인터페이스
```

### 2. 하위 도메인별 분리

```
com.example
├── order/           # 주문 도메인
│   ├── domain
│   ├── application
│   └── ...
├── inventory/       # 재고 도메인
│   ├── domain
│   └── ...
└── shipping/        # 배송 도메인
    ├── domain
    └── ...
```

{{% notice style="tip" title="핵심 포인트: 패키지 구조" %}}
- **기술 중심 X, 도메인 중심 O**: controller/service/repository 대신 domain/application/infrastructure 구조
- **하위 도메인별 분리**: 규모가 커지면 order/, inventory/, shipping/ 등 도메인별로 분리
- **Bounded Context 반영**: 각 하위 도메인은 독립적인 계층 구조를 가짐
{{% /notice %}}

## 다음 단계

- [주문 도메인](order-domain/) - Aggregate, Entity, Value Object 구현
