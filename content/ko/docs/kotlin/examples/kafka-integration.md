---
title: "Kafka 연동"
description: "Kotlin + Spring Boot 3.2.x + Spring Kafka로 Producer/Consumer를 구현합니다. KafkaTemplate, @KafkaListener, application.yml 설정과 메시지 송수신 테스트를 다룹니다."
lastmod: "2026-05-13"
---

> **소요 시간**: 약 25분

{{< callout type="tip" title="TL;DR" >}}
- `spring-kafka` 의존성 추가 후 `application.yml` 설정
- `KafkaTemplate<String, String>`으로 메시지 발행
- `@KafkaListener(topics = ["..."], groupId = "...")`로 메시지 수신
- Consumer Group 네이밍 규칙: `{app-name}-group`
{{< /callout >}}

**대상 독자**: Kotlin + Spring Boot 기초를 이해한 개발자
**선수 지식**: [Spring Boot 연동](spring-boot-integration/), Kafka 기초

---

Kotlin과 Spring Kafka를 사용하여 메시지를 발행하고 수신하는 예제를 구현합니다. 이 예제는 사이트의 `docker/` 디렉토리에 있는 Kafka(KRaft 모드, 포트 9092)를 사용합니다.

#### Step 1 — Kafka 브로커 실행

```bash
# 프로젝트 루트에서
cd docker
docker-compose up -d

# 상태 확인
docker-compose ps
```

정상 실행 시 `kafka` 컨테이너가 `Up` 상태여야 합니다.

#### Step 2 — 프로젝트 설정

**build.gradle.kts**

```kotlin
plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0"
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.5"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}
```

**src/main/resources/application.yml**

```yaml
spring:
  application:
    name: kotlin-kafka-demo

  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all              # 모든 리플리카 확인 후 완료
      retries: 3
    consumer:
      group-id: kotlin-kafka-demo-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      enable-auto-commit: false  # 수동 커밋 권장
    listener:
      ack-mode: manual_immediate  # @KafkaListener의 Acknowledgment 파라미터 활성화

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

#### Step 3 — Kafka 설정 클래스

```kotlin
// src/main/kotlin/com/example/kafka/config/KafkaTopicConfig.kt
package com.example.kafka.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {

    companion object {
        const val TOPIC_MESSAGES = "kotlin.messages"
        const val TOPIC_ORDERS = "kotlin.orders"
    }

    @Bean
    fun messagesTopic(): NewTopic = TopicBuilder
        .name(TOPIC_MESSAGES)
        .partitions(3)
        .replicas(1)
        .build()

    @Bean
    fun ordersTopic(): NewTopic = TopicBuilder
        .name(TOPIC_ORDERS)
        .partitions(3)
        .replicas(1)
        .build()
}
```

#### Step 4 — 도메인 모델

```kotlin
// src/main/kotlin/com/example/kafka/domain/Message.kt
package com.example.kafka.domain

import java.time.LocalDateTime

data class Message(
    val id: String,
    val content: String,
    val sender: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class Order(
    val orderId: String,
    val productId: String,
    val quantity: Int,
    val amount: Long,
    val customerId: String
)
```

#### Step 5 — Producer 서비스

```kotlin
// src/main/kotlin/com/example/kafka/producer/MessageProducer.kt
package com.example.kafka.producer

import com.example.kafka.config.KafkaTopicConfig
import com.example.kafka.domain.Message
import com.example.kafka.domain.Order
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class MessageProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun sendMessage(content: String, sender: String): String {
        val messageId = UUID.randomUUID().toString()
        val message = Message(
            id = messageId,
            content = content,
            sender = sender
        )
        val payload = objectMapper.writeValueAsString(message)

        kafkaTemplate.send(KafkaTopicConfig.TOPIC_MESSAGES, messageId, payload)
            .whenComplete { result, ex ->
                if (ex != null) {
                    log.error("메시지 전송 실패: $messageId", ex)
                } else {
                    log.info(
                        "메시지 전송 완료: $messageId " +
                        "(파티션=${result.recordMetadata.partition()}, " +
                        "오프셋=${result.recordMetadata.offset()})"
                    )
                }
            }

        return messageId
    }

    fun sendOrder(order: Order) {
        val payload = objectMapper.writeValueAsString(order)
        kafkaTemplate.send(KafkaTopicConfig.TOPIC_ORDERS, order.orderId, payload)
            .whenComplete { _, ex ->
                if (ex != null) {
                    log.error("주문 전송 실패: ${order.orderId}", ex)
                } else {
                    log.info("주문 전송 완료: ${order.orderId}")
                }
            }
    }
}
```

#### Step 6 — Consumer 서비스

```kotlin
// src/main/kotlin/com/example/kafka/consumer/MessageConsumer.kt
package com.example.kafka.consumer

import com.example.kafka.config.KafkaTopicConfig
import com.example.kafka.domain.Message
import com.example.kafka.domain.Order
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service

@Service
class MessageConsumer(
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        topics = [KafkaTopicConfig.TOPIC_MESSAGES],
        groupId = "kotlin-kafka-demo-group"
    )
    fun consumeMessage(record: ConsumerRecord<String, String>) {
        val message = objectMapper.readValue(record.value(), Message::class.java)
        log.info(
            "메시지 수신 — ID: ${message.id}, " +
            "발신자: ${message.sender}, " +
            "내용: ${message.content}, " +
            "파티션: ${record.partition()}, 오프셋: ${record.offset()}"
        )
        // 실제 비즈니스 로직 처리
        processMessage(message)
    }

    @KafkaListener(
        topics = [KafkaTopicConfig.TOPIC_ORDERS],
        groupId = "kotlin-kafka-demo-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun consumeOrder(
        record: ConsumerRecord<String, String>,
        ack: Acknowledgment
    ) {
        try {
            val order = objectMapper.readValue(record.value(), Order::class.java)
            log.info("주문 수신 — 주문ID: ${order.orderId}, 고객: ${order.customerId}")
            processOrder(order)
            ack.acknowledge()   // 수동 커밋
        } catch (ex: Exception) {
            log.error("주문 처리 실패: ${record.key()}", ex)
            // 재처리 전략: Dead Letter Queue로 전송 등
        }
    }

    private fun processMessage(message: Message) {
        log.info("메시지 처리 완료: ${message.id}")
    }

    private fun processOrder(order: Order) {
        log.info("주문 처리 완료: ${order.orderId}, 금액: ${order.amount}원")
    }
}
```

#### Step 7 — REST 컨트롤러

```kotlin
// src/main/kotlin/com/example/kafka/controller/KafkaController.kt
package com.example.kafka.controller

import com.example.kafka.domain.Order
import com.example.kafka.producer.MessageProducer
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

data class SendMessageRequest(val message: String, val sender: String = "anonymous")
data class SendMessageResponse(val messageId: String, val status: String)

data class SendOrderRequest(
    val productId: String,
    val quantity: Int,
    val amount: Long,
    val customerId: String
)

@RestController
@RequestMapping("/api/kafka")
class KafkaController(
    private val messageProducer: MessageProducer
) {
    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun sendMessage(@RequestBody request: SendMessageRequest): SendMessageResponse {
        val messageId = messageProducer.sendMessage(request.message, request.sender)
        return SendMessageResponse(messageId, "ACCEPTED")
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun sendOrder(@RequestBody request: SendOrderRequest): Map<String, String> {
        val order = Order(
            orderId = UUID.randomUUID().toString(),
            productId = request.productId,
            quantity = request.quantity,
            amount = request.amount,
            customerId = request.customerId
        )
        messageProducer.sendOrder(order)
        return mapOf("orderId" to order.orderId, "status" to "ACCEPTED")
    }
}
```

#### Step 8 — 애플리케이션 실행

```kotlin
// src/main/kotlin/com/example/kafka/KafkaApplication.kt
package com.example.kafka

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaApplication

fun main(args: Array<String>) {
    runApplication<KafkaApplication>(*args)
}
```

```bash
./gradlew bootRun
```

#### Step 9 — 메시지 송수신 테스트

서버가 실행 중인 상태에서 아래 curl 명령으로 테스트합니다.

```bash
# 메시지 전송
curl -X POST http://localhost:8080/api/kafka/messages \
  -H "Content-Type: application/json" \
  -d '{"message":"안녕하세요, Kafka!","sender":"홍길동"}'

# 응답
# {"messageId":"550e8400-e29b-41d4-a716-446655440000","status":"ACCEPTED"}

# 주문 전송
curl -X POST http://localhost:8080/api/kafka/orders \
  -H "Content-Type: application/json" \
  -d '{"productId":"PROD-001","quantity":2,"amount":50000,"customerId":"user-123"}'

# 응답
# {"orderId":"...","status":"ACCEPTED"}
```

서버 로그에서 Consumer가 메시지를 수신했는지 확인합니다.

```text
INFO  MessageConsumer - 메시지 수신 — ID: 550e8400..., 발신자: 홍길동, 내용: 안녕하세요, Kafka!, 파티션: 0, 오프셋: 0
INFO  MessageConsumer - 메시지 처리 완료: 550e8400...
```

#### Step 10 — Consumer Group 확인

```bash
# Kafka 컨테이너에서 Consumer Group 상태 확인
docker exec -it kafka \
  kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --group kotlin-kafka-demo-group
```

{{< callout type="info" title="핵심 정리" >}}
- `KafkaTemplate<String, String>.send(topic, key, value)` — 메시지 발행
- `@KafkaListener(topics = [...], groupId = "...")` — 메시지 수신 선언
- Consumer Group ID 규칙: `{app-name}-group` (예: `kotlin-kafka-demo-group`)
- `Acknowledgment.acknowledge()` — 수동 커밋으로 메시지 처리 보장
- JSON 직렬화에 `jackson-module-kotlin` 필요
{{< /callout >}}

#### 다음 단계

- [코루틴 실무 사용](coroutines-practical/) — suspend 함수로 비동기 메시지 처리
- [Kafka 가이드]({{< relref "/docs/kafka" >}}) — Kafka 심화 학습

> 💡 **함께 읽기**:
> - [Kafka Producer/Consumer 구현]({{< relref "/docs/kafka/examples/basic" >}}) — 같은 패턴을 Java + Spring Kafka로 비교해 봅니다.
> - [Consumer Group 개념]({{< relref "/docs/kafka/concepts/consumer-group" >}}) — `groupId` 설정이 파티션 분배에 미치는 영향을 이해합니다.
> - [에러 처리 패턴]({{< relref "/docs/kafka/concepts/error-handling" >}}) — Dead Letter Queue 등 실무 재처리 전략을 다룹니다.
