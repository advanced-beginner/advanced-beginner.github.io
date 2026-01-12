---
lastmod: "2026-01-06"
title: Environment Setup
weight: 1
---

# Environment Setup

Reference guide for setting up Kafka with Spring Boot.

> **Completed Quick Start?**
> If you completed the [Quick Start](../../quick-start/), you've already set up the basic environment. This document is a **reference guide** for configuration details and production environment setup.

---

## Running Kafka with Docker

### docker-compose.yml

The `docker/docker-compose.yml` file in the project root.

```yaml
version: '3.8'

services:
  kafka:
    image: apache/kafka:3.6.1
    hostname: kafka
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
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_LOG_DIRS: /var/lib/kafka/data
      CLUSTER_ID: MkU3OEVBNTcwNTJENDM2Qk
    volumes:
      - kafka-data:/var/lib/kafka/data

volumes:
  kafka-data:
```

> **KRaft Mode**: This configuration uses Kafka's own metadata management without Zookeeper (Kafka 3.3+).

### Commands

```bash
# Start
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f kafka

# Stop
docker-compose down

# Stop with data removal
docker-compose down -v
```

---

## Spring Boot Dependencies

### build.gradle.kts

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
}

dependencies {
    // Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // Web (for REST API)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}
```

### Maven (pom.xml)

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

---

## application.yml Configuration

### Quick Start Basic Configuration

The minimal configuration used in the [Quick Start example](../../quick-start/).

```yaml
spring:
  application:
    name: kafka-example

  kafka:
    bootstrap-servers: localhost:9092

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

    consumer:
      group-id: quickstart-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
```

### Production Recommended Configuration

Consider adding these settings for production environments.

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all                    # All replicas confirm
      retries: 3                   # Retry count
      properties:
        linger.ms: 1               # Batch wait time
        enable.idempotence: true   # Prevent duplicate sends

    consumer:
      group-id: quickstart-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      enable-auto-commit: true
      properties:
        max.poll.records: 500
        max.poll.interval.ms: 300000
```

---

## Configuration Details

### Producer Settings

| Setting | Description | Default | Recommended |
|---------|-------------|---------|-------------|
| `acks` | Confirmation level | `1` | `all` (production) |
| `retries` | Retry count | `2147483647` | `3` |
| `batch-size` | Batch size (bytes) | `16384` | `16384` |
| `linger-ms` | Batch wait time | `0` | `1` |
| `buffer-memory` | Buffer memory | `33554432` | `33554432` |

### Consumer Settings

| Setting | Description | Default | Recommended |
|---------|-------------|---------|-------------|
| `group-id` | Consumer Group ID | - | Service name |
| `auto-offset-reset` | Initial Offset | `latest` | `earliest` (dev) |
| `enable-auto-commit` | Auto commit | `true` | Depends on situation |
| `max-poll-records` | Max records per poll | `500` | `500` |

---

## JSON Message Processing

### Add Dependency

```kotlin
dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind")
}
```

### Configuration

```yaml
spring:
  kafka:
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.example.*"
```

### Usage Example

```java
// Domain class
public record OrderEvent(
    String orderId,
    String status,
    LocalDateTime timestamp
) {}

// Producer
kafkaTemplate.send("orders", orderId, new OrderEvent(orderId, "CREATED", now()));

// Consumer
@KafkaListener(topics = "orders")
public void consume(OrderEvent event) {
    log.info("Order event: {}", event);
}
```

---

## Profile-based Configuration

### application.yml (common)

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}
```

### application-local.yml

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      auto-offset-reset: earliest
```

### application-prod.yml

```yaml
spring:
  kafka:
    bootstrap-servers: kafka-1:9092,kafka-2:9092,kafka-3:9092
    producer:
      acks: all
    consumer:
      auto-offset-reset: latest
```

---

## Common Errors and Solutions

### Connection Error

```
Connection to node -1 could not be established
```

**Cause:** Cannot connect to Kafka broker

**Solution:**
1. Check Kafka is running: `docker-compose ps`
2. Check port: `netstat -an | grep 9092`
3. Verify bootstrap-servers setting

### Serialization Error

```
Failed to serialize value
```

**Cause:** Serializer configuration mismatch

**Solution:**
```yaml
spring:
  kafka:
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

### Deserialization Error

```
Failed to deserialize; nested exception is java.lang.IllegalArgumentException
```

**Cause:** Trusted packages not configured

**Solution:**
```yaml
spring:
  kafka:
    consumer:
      properties:
        spring.json.trusted.packages: "*"  # Or specific package
```

### Missing Group ID

```
group.id is required
```

**Cause:** Consumer group-id not set

**Solution:**
```yaml
spring:
  kafka:
    consumer:
      group-id: quickstart-group
```

---

## Configuration Verification Checklist

- [ ] Kafka running via Docker
- [ ] spring-kafka dependency added
- [ ] bootstrap-servers configured
- [ ] Producer serializer configured
- [ ] Consumer deserializer configured
- [ ] Consumer group-id configured
- [ ] (For JSON) trusted.packages configured

---

## Next Steps

- [Basic Examples](../basic/) - Producer/Consumer implementation
- [Order System](../order-system/) - Real-world example
