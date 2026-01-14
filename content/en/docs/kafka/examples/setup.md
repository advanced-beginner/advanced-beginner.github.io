---
lastmod: "2026-01-10"
title: Environment Setup
weight: 1
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

Reference guide for setting up Kafka with Spring Boot.

{{% notice style="tip" title="TL;DR" %}}
- **Kafka Execution**: Run Kafka 3.6.1 in KRaft mode via Docker Compose
- **Dependencies**: Add `spring-kafka`, `spring-boot-starter-web`
- **Basic Settings**: Configure `bootstrap-servers`, Serializer/Deserializer, `group-id`
- **Production**: Ensure stability with `acks: all`, `enable.idempotence: true`
{{% /notice %}}

#### Target Audience and Prerequisites

| Item | Description |
|------|-------------|
| **Target Audience** | Developers setting up Kafka environment in Spring Boot projects |
| **Prerequisites** | Basic Docker usage, Gradle or Maven build tools, Spring Boot configuration files (application.yml) |
| **Required Tools** | Docker Desktop or Docker Engine, JDK 17+, IDE (IntelliJ IDEA recommended) |
| **Estimated Time** | About 15 minutes |

If you completed Quick Start, you've already set up the basic environment. This document is a reference guide for configuration details and production environment setup.

#### Running Kafka with Docker

**docker-compose.yml**

This is the `docker/docker-compose.yml` file in the project root. This configuration uses KRaft mode where Kafka manages its own metadata without Zookeeper. KRaft is recommended for Kafka 3.3 and above.

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

**Commands**

To start Kafka, use `docker-compose up -d`. To check status, use `docker-compose ps`; to check logs, use `docker-compose logs -f kafka`. To stop, use `docker-compose down`, and to delete all data as well, use `docker-compose down -v`.

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

{{% notice style="info" title="Docker Kafka Key Points" %}}
- **KRaft Mode**: Kafka manages its own metadata without Zookeeper (recommended for 3.3+)
- **Ports**: 9092 (client), 9093 (controller)
- **Volume**: Ensure data persistence with `kafka-data`
- **Full Reset**: Delete volumes with `docker-compose down -v`
{{% /notice %}}

#### Spring Boot Dependencies

**build.gradle.kts**

When using Gradle Kotlin DSL, add dependencies as follows.

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

**Maven (pom.xml)**

When using Maven, add dependencies as follows.

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

{{% notice style="info" title="Dependencies Key Points" %}}
- **spring-kafka**: Kafka Producer/Consumer abstraction, provides KafkaTemplate
- **spring-boot-starter-web**: For REST API endpoint implementation (optional)
- **spring-kafka-test**: Provides EmbeddedKafka for testing
{{% /notice %}}

#### application.yml Configuration

**Quick Start Basic Configuration**

The minimal configuration used in the Quick Start example.

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

**Production Recommended Configuration**

Consider these additional settings for production environments. Setting acks to all provides high data stability by getting confirmation from all replicas. Setting enable.idempotence to true prevents duplicate sends.

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

{{% notice style="info" title="application.yml Key Points" %}}
- **bootstrap-servers**: Kafka broker connection address (required)
- **Serializer/Deserializer**: Use StringSerializer for strings, JsonSerializer for objects
- **group-id**: Consumer Group identifier, service name-based recommended
- **Production Settings**: `acks: all` for data stability, `enable.idempotence: true` to prevent duplicates
{{% /notice %}}

#### Configuration Details

**Producer Settings**

acks is the confirmation level with a default of 1; all is recommended for production. retries is the retry count with a very large default, so you can limit it to around 3. batch-size is the batch size with a default of 16384 bytes which is appropriate. linger-ms is the batch wait time with a default of 0, but setting it to around 1ms improves batch efficiency. buffer-memory is the buffer memory with a default of 33554432 bytes (32MB) which is appropriate.

**Consumer Settings**

group-id is the Consumer Group ID; using the service name is recommended. auto-offset-reset is the initial Offset; earliest is recommended for development environments. enable-auto-commit is whether to auto-commit; choose based on your situation. max-poll-records is the maximum records to fetch at once with a default of 500 which is appropriate.

#### JSON Message Processing

**Add Dependency**

Add Jackson dependency for JSON serialization.

```kotlin
dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind")
}
```

**Configuration**

Producer uses JsonSerializer, Consumer uses JsonDeserializer. The trusted.packages setting specifies packages of classes to deserialize.

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

**Usage Example**

Using Java Record as a domain class allows concise JSON message exchange.

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

{{% notice style="info" title="JSON Message Processing Key Points" %}}
- **JsonSerializer/JsonDeserializer**: Auto serialize/deserialize objects to JSON
- **trusted.packages**: Specify allowed packages for deserialization (security)
- **Java Record**: Recommended for defining events as immutable data classes
{{% /notice %}}

#### Profile-based Configuration

Use profiles to apply different settings per environment.

**application.yml (common)**

Uses the environment variable value if present, otherwise defaults to localhost:9092.

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}
```

**application-local.yml**

In local development environment, set to earliest to read all messages from the beginning.

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      auto-offset-reset: earliest
```

**application-prod.yml**

In production environment, specify multiple Brokers and set acks to all for increased stability.

```yaml
spring:
  kafka:
    bootstrap-servers: kafka-1:9092,kafka-2:9092,kafka-3:9092
    producer:
      acks: all
    consumer:
      auto-offset-reset: latest
```

{{% notice style="info" title="Profile-based Configuration Key Points" %}}
- **Environment Variables**: Specify default with `${KAFKA_SERVERS:localhost:9092}` format
- **Local Development**: Read all messages from beginning with `earliest`
- **Production**: Specify multiple brokers, read from latest messages with `latest`
{{% /notice %}}

#### Common Errors and Solutions

**Connection Error**

The `Connection to node -1 could not be established` error occurs when unable to connect to the Kafka broker. Check Kafka running status with `docker-compose ps`, verify the port is open with `netstat -an | grep 9092`, and also verify the bootstrap-servers setting is correct.

**Serialization Error**

The `Failed to serialize value` error occurs when Serializer settings don't match. Use JsonSerializer when sending JSON objects.

```yaml
spring:
  kafka:
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

**Deserialization Error**

The `Failed to deserialize` error occurs when trusted packages are not configured. Specify the packages of classes that JsonDeserializer should deserialize.

```yaml
spring:
  kafka:
    consumer:
      properties:
        spring.json.trusted.packages: "*"  # Or specific package
```

**Missing Group ID**

The `group.id is required` error occurs when Consumer's group-id is not set.

```yaml
spring:
  kafka:
    consumer:
      group-id: quickstart-group
```

#### Configuration Verification Checklist

Verify the following items to ensure environment setup is complete. Check if Kafka is running via Docker, if spring-kafka dependency is added, if bootstrap-servers is configured, if Producer's serializer is configured, if Consumer's deserializer is configured, and if Consumer's group-id is configured. If using JSON, trusted.packages should also be configured.

#### Next Steps

- [Basic Examples](basic/) - Producer/Consumer implementation
- [Order System](order-system/) - Real-world example
