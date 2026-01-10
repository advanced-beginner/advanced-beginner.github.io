---
lastmod: "2026-01-10"
title: 환경 구성
weight: 1
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

Spring Boot에서 Kafka를 사용하기 위한 환경 설정 레퍼런스입니다.

{{% notice style="tip" title="TL;DR" %}}
- **Kafka 실행**: Docker Compose로 KRaft 모드 Kafka 3.6.1 실행
- **의존성**: `spring-kafka`, `spring-boot-starter-web` 추가
- **기본 설정**: `bootstrap-servers`, Serializer/Deserializer, `group-id` 설정
- **프로덕션**: `acks: all`, `enable.idempotence: true`로 안정성 확보
{{% /notice %}}

#### 대상 독자 및 선수 지식

| 항목 | 설명 |
|------|------|
| **대상 독자** | Spring Boot 프로젝트에서 Kafka 환경을 구성하려는 개발자 |
| **선수 지식** | Docker 기본 사용법, Gradle 또는 Maven 빌드 도구, Spring Boot 설정 파일(application.yml) |
| **필수 도구** | Docker Desktop 또는 Docker Engine, JDK 17+, IDE (IntelliJ IDEA 권장) |
| **예상 소요 시간** | 약 15분 |

Quick Start를 완료했다면 이미 기본 환경을 구성한 것입니다. 이 문서는 설정 상세 내용과 프로덕션 환경 구성을 위한 참조 문서입니다.

#### Docker로 Kafka 실행

**docker-compose.yml**

프로젝트 루트의 `docker/docker-compose.yml` 파일입니다. 이 설정은 Zookeeper 없이 Kafka 자체적으로 메타데이터를 관리하는 KRaft 모드를 사용합니다. KRaft는 Kafka 3.3 이상에서 권장됩니다.

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

**실행 명령**

Kafka를 시작하려면 `docker-compose up -d` 명령을 사용합니다. 상태를 확인하려면 `docker-compose ps`, 로그를 확인하려면 `docker-compose logs -f kafka` 명령을 사용합니다. 종료할 때는 `docker-compose down`을 사용하고, 데이터까지 모두 삭제하려면 `docker-compose down -v` 명령을 사용합니다.

```bash
# 시작
docker-compose up -d

# 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f kafka

# 종료
docker-compose down

# 데이터 포함 종료
docker-compose down -v
```

{{% notice style="info" title="Docker Kafka 핵심 포인트" %}}
- **KRaft 모드**: Zookeeper 없이 Kafka 자체 메타데이터 관리 (3.3 이상 권장)
- **포트**: 9092 (클라이언트), 9093 (컨트롤러)
- **볼륨**: `kafka-data`로 데이터 영속성 확보
- **완전 초기화**: `docker-compose down -v`로 볼륨까지 삭제
{{% /notice %}}

#### Spring Boot 의존성

**build.gradle.kts**

Gradle Kotlin DSL을 사용하는 경우 다음과 같이 의존성을 추가합니다.

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
}

dependencies {
    // Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // Web (REST API용)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}
```

**Maven (pom.xml)**

Maven을 사용하는 경우 다음과 같이 의존성을 추가합니다.

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

{{% notice style="info" title="의존성 핵심 포인트" %}}
- **spring-kafka**: Kafka Producer/Consumer 추상화, KafkaTemplate 제공
- **spring-boot-starter-web**: REST API 엔드포인트 구현용 (선택)
- **spring-kafka-test**: 테스트용 EmbeddedKafka 제공
{{% /notice %}}

#### application.yml 설정

**Quick Start 기본 설정**

Quick Start 예제에서 사용하는 최소 설정입니다.

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

**프로덕션 권장 설정**

실무 환경에서는 다음 설정을 추가로 고려합니다. acks를 all로 설정하면 모든 복제본에서 확인을 받아 데이터 안정성이 높아집니다. enable.idempotence를 true로 설정하면 중복 전송을 방지합니다.

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all                    # 모든 복제본 확인
      retries: 3                   # 재시도 횟수
      properties:
        linger.ms: 1               # 배치 대기 시간
        enable.idempotence: true   # 중복 전송 방지

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

{{% notice style="info" title="application.yml 핵심 포인트" %}}
- **bootstrap-servers**: Kafka 브로커 연결 주소 (필수)
- **Serializer/Deserializer**: 문자열은 StringSerializer, 객체는 JsonSerializer 사용
- **group-id**: Consumer Group 식별자, 서비스명 기반 권장
- **프로덕션 설정**: `acks: all`로 데이터 안정성, `enable.idempotence: true`로 중복 방지
{{% /notice %}}

#### 설정 항목 상세

**Producer 설정**

acks는 전송 확인 수준으로 기본값은 1이며 프로덕션에서는 all을 권장합니다. retries는 재시도 횟수로 기본값이 매우 크므로 3 정도로 제한할 수 있습니다. batch-size는 배치 크기로 기본값 16384바이트가 적절합니다. linger-ms는 배치 대기 시간으로 기본값 0이지만 1ms 정도로 설정하면 배치 효율이 높아집니다. buffer-memory는 버퍼 메모리로 기본값 33554432바이트(32MB)가 적절합니다.

**Consumer 설정**

group-id는 Consumer Group ID로 서비스명을 사용하는 것이 좋습니다. auto-offset-reset은 초기 Offset으로 개발 환경에서는 earliest를 권장합니다. enable-auto-commit은 자동 커밋 여부로 상황에 따라 선택합니다. max-poll-records는 한번에 가져올 최대 레코드 수로 기본값 500이 적절합니다.

#### JSON 메시지 처리

**의존성 추가**

JSON 직렬화를 위해 Jackson 의존성을 추가합니다.

```kotlin
dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind")
}
```

**설정**

Producer는 JsonSerializer를, Consumer는 JsonDeserializer를 사용합니다. trusted.packages 설정은 역직렬화할 클래스의 패키지를 지정합니다.

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

**사용 예시**

Java Record를 도메인 클래스로 사용하면 간결하게 JSON 메시지를 주고받을 수 있습니다.

```java
// 도메인 클래스
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
    log.info("주문 이벤트: {}", event);
}
```

{{% notice style="info" title="JSON 메시지 처리 핵심 포인트" %}}
- **JsonSerializer/JsonDeserializer**: 객체를 JSON으로 자동 직렬화/역직렬화
- **trusted.packages**: 역직렬화 허용 패키지 지정 (보안)
- **Java Record**: 불변 데이터 클래스로 이벤트 정의 권장
{{% /notice %}}

#### 프로필별 설정

환경별로 다른 설정을 적용하려면 프로필을 사용합니다.

**application.yml (공통)**

환경 변수가 있으면 해당 값을, 없으면 기본값 localhost:9092를 사용합니다.

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}
```

**application-local.yml**

로컬 개발 환경에서는 earliest로 설정하여 모든 메시지를 처음부터 읽습니다.

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      auto-offset-reset: earliest
```

**application-prod.yml**

프로덕션 환경에서는 여러 Broker를 지정하고, acks를 all로 설정하여 안정성을 높입니다.

```yaml
spring:
  kafka:
    bootstrap-servers: kafka-1:9092,kafka-2:9092,kafka-3:9092
    producer:
      acks: all
    consumer:
      auto-offset-reset: latest
```

{{% notice style="info" title="프로필별 설정 핵심 포인트" %}}
- **환경 변수**: `${KAFKA_SERVERS:localhost:9092}` 형식으로 기본값 지정
- **로컬 개발**: `earliest`로 모든 메시지 처음부터 읽기
- **프로덕션**: 다중 브로커 지정, `latest`로 최신 메시지부터 읽기
{{% /notice %}}

#### 일반적인 오류와 해결

**연결 오류**

`Connection to node -1 could not be established` 오류는 Kafka 브로커에 연결할 수 없을 때 발생합니다. `docker-compose ps`로 Kafka 실행 상태를 확인하고, `netstat -an | grep 9092`로 포트가 열려 있는지 확인합니다. bootstrap-servers 설정이 올바른지도 확인합니다.

**Serialization 오류**

`Failed to serialize value` 오류는 Serializer 설정이 맞지 않을 때 발생합니다. JSON 객체를 전송하려면 JsonSerializer를 사용해야 합니다.

```yaml
spring:
  kafka:
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

**Deserialization 오류**

`Failed to deserialize` 오류는 신뢰할 수 있는 패키지가 설정되지 않았을 때 발생합니다. JsonDeserializer가 역직렬화할 클래스의 패키지를 지정해야 합니다.

```yaml
spring:
  kafka:
    consumer:
      properties:
        spring.json.trusted.packages: "*"  # 또는 특정 패키지
```

**Group ID 누락**

`group.id is required` 오류는 Consumer의 group-id가 설정되지 않았을 때 발생합니다.

```yaml
spring:
  kafka:
    consumer:
      group-id: quickstart-group
```

#### 설정 확인 체크리스트

환경 구성이 완료되었는지 다음 항목을 확인합니다. Docker로 Kafka가 실행 중인지, spring-kafka 의존성이 추가되었는지, bootstrap-servers가 설정되었는지, Producer의 serializer가 설정되었는지, Consumer의 deserializer가 설정되었는지, Consumer의 group-id가 설정되었는지 확인합니다. JSON을 사용한다면 trusted.packages도 설정되어 있어야 합니다.

#### 다음 단계

- [기본 예제](../basic/) - Producer/Consumer 구현
- [주문 시스템](../order-system/) - 실전 예제
