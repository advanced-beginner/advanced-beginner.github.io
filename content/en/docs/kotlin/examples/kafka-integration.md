---
title: "Kafka Integration"
description: "Implement Producer/Consumer with Kotlin + Spring Boot 3.2.x + Spring Kafka. Covers KafkaTemplate, @KafkaListener, application.yml configuration, and message send/receive tests."
weight: 4
lastmod: "2026-05-13"
---

> **Estimated time**: about 25 minutes

{{< callout type="tip" title="TL;DR" >}}
- Add the `spring-kafka` dependency and configure `application.yml`
- Publish messages with `KafkaTemplate<String, String>`
- Receive messages with `@KafkaListener(topics = ["..."], groupId = "...")`
- Consumer Group naming convention: `{app-name}-group`
{{< /callout >}}

**Target audience**: Developers familiar with Kotlin + Spring Boot basics
**Prerequisites**: [Spring Boot Integration](spring-boot-integration/), Kafka basics

---

Implement an example that publishes and receives messages using Kotlin and Spring Kafka. This example uses the Kafka instance (KRaft mode, port 9092) in the site's `docker/` directory.

#### Step 1 — Start the Kafka Broker

```bash
# From the project root
cd docker
docker-compose up -d

# Check status
docker-compose ps
```

The `kafka` container should be in the `Up` state on success.

#### Step 2 — Project Configuration

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
      acks: all              # Wait for all replicas to acknowledge
      retries: 3
    consumer:
      group-id: kotlin-kafka-demo-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      enable-auto-commit: false  # Manual commit recommended
    listener:
      ack-mode: manual_immediate  # Enables the Acknowledgment parameter on @KafkaListener

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

#### Step 3 — Kafka Configuration Class

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

#### Step 4 — Domain Models

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

#### Step 5 — Producer Service

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
                    log.error("Failed to send message: $messageId", ex)
                } else {
                    log.info(
                        "Message sent: $messageId " +
                        "(partition=${result.recordMetadata.partition()}, " +
                        "offset=${result.recordMetadata.offset()})"
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
                    log.error("Failed to send order: ${order.orderId}", ex)
                } else {
                    log.info("Order sent: ${order.orderId}")
                }
            }
    }
}
```

#### Step 6 — Consumer Service

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
            "Message received — ID: ${message.id}, " +
            "sender: ${message.sender}, " +
            "content: ${message.content}, " +
            "partition: ${record.partition()}, offset: ${record.offset()}"
        )
        // Actual business logic
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
            log.info("Order received — orderId: ${order.orderId}, customer: ${order.customerId}")
            processOrder(order)
            ack.acknowledge()   // Manual commit
        } catch (ex: Exception) {
            log.error("Order processing failed: ${record.key()}", ex)
            // Retry strategy: send to Dead Letter Queue, etc.
        }
    }

    private fun processMessage(message: Message) {
        log.info("Message processed: ${message.id}")
    }

    private fun processOrder(order: Order) {
        log.info("Order processed: ${order.orderId}, amount: ${order.amount}")
    }
}
```

#### Step 7 — REST Controller

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

#### Step 8 — Running the Application

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

#### Step 9 — Testing Message Send/Receive

With the server running, test using the curl commands below.

```bash
# Send a message
curl -X POST http://localhost:8080/api/kafka/messages \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello, Kafka!","sender":"Alice"}'

# Response
# {"messageId":"550e8400-e29b-41d4-a716-446655440000","status":"ACCEPTED"}

# Send an order
curl -X POST http://localhost:8080/api/kafka/orders \
  -H "Content-Type: application/json" \
  -d '{"productId":"PROD-001","quantity":2,"amount":50000,"customerId":"user-123"}'

# Response
# {"orderId":"...","status":"ACCEPTED"}
```

Check the server logs to confirm the Consumer received the message.

```text
INFO  MessageConsumer - Message received — ID: 550e8400..., sender: Alice, content: Hello, Kafka!, partition: 0, offset: 0
INFO  MessageConsumer - Message processed: 550e8400...
```

#### Step 10 — Checking the Consumer Group

```bash
# Check Consumer Group status from the Kafka container
docker exec -it kafka \
  kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --group kotlin-kafka-demo-group
```

{{< callout type="info" title="Key takeaways" >}}
- `KafkaTemplate<String, String>.send(topic, key, value)` — publish a message
- `@KafkaListener(topics = [...], groupId = "...")` — declarative consumer
- Consumer Group ID convention: `{app-name}-group` (e.g., `kotlin-kafka-demo-group`)
- `Acknowledgment.acknowledge()` — manual commit guarantees processing
- JSON serialization requires `jackson-module-kotlin`
{{< /callout >}}

---

### Writing Tests

`spring-kafka-test`'s `@EmbeddedKafka` lets you verify Producer/Consumer against an in-memory broker without a real Kafka instance. Confirm `spring-kafka-test` is already in `build.gradle.kts`.

```kotlin
// build.gradle.kts — testImplementation block
testImplementation("org.springframework.boot:spring-boot-starter-test")
testImplementation("org.springframework.kafka:spring-kafka-test")  // Includes @EmbeddedKafka
```

**Producer unit test + Consumer receive verification**

Start an in-memory broker with `@EmbeddedKafka`, publish a message via `KafkaTemplate`, and verify the consumer receives it using `CountDownLatch`.

```kotlin
// src/test/kotlin/com/example/kafka/producer/MessageProducerTest.kt
package com.example.kafka.producer

import com.example.kafka.config.KafkaTopicConfig
import com.example.kafka.domain.Message
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest(
    properties = ["spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}"]
)
@EmbeddedKafka(
    partitions = 1,
    topics = [KafkaTopicConfig.TOPIC_MESSAGES]
)
class MessageProducerTest @Autowired constructor(
    private val messageProducer: MessageProducer,
    private val objectMapper: ObjectMapper,
    private val embeddedKafkaBroker: EmbeddedKafkaBroker
) {
    @Test
    fun `consumer receives a published message`() {
        // Consumer configuration
        val consumerProps = KafkaTestUtils.consumerProps(
            "test-group", "true", embeddedKafkaBroker
        ).apply {
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        }

        val consumerFactory = DefaultKafkaConsumerFactory<String, String>(consumerProps)
        val containerProps = ContainerProperties(KafkaTopicConfig.TOPIC_MESSAGES)

        val latch = CountDownLatch(1)
        var receivedValue: String? = null

        containerProps.messageListener = MessageListener<String, String> { record ->
            receivedValue = record.value()
            latch.countDown()
        }

        val container = KafkaMessageListenerContainer(consumerFactory, containerProps)
        container.start()
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.partitionsPerTopic)

        // Run the producer
        val messageId = messageProducer.sendMessage("test message", "tester")

        // Wait up to 5 seconds
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue()
        container.stop()

        // Verify the received message
        val received = objectMapper.readValue(receivedValue, Message::class.java)
        assertThat(received.id).isEqualTo(messageId)
        assertThat(received.content).isEqualTo("test message")
        assertThat(received.sender).isEqualTo("tester")
    }
}
```

{{< callout type="info" title="@EmbeddedKafka attributes" >}}
- `partitions` — partitions per topic (default 1)
- `topics` — list of topics to create up front
- `spring.embedded.kafka.brokers` — in-memory broker address (auto-injected)

By setting `spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}` as a `@SpringBootTest` property, the application configuration points to the in-memory broker.
{{< /callout >}}

Run the tests.

```bash
./gradlew test
```

#### Next Steps

- [Practical Coroutines](coroutines-practical/) — asynchronous message processing with suspend functions
- [Kafka Guide]({{< relref "/docs/kafka" >}}) — deep dive into Kafka

> Tip: **Further reading**:
> - [Kafka Producer/Consumer Implementation]({{< relref "/docs/kafka/examples/basic" >}}) — compare the same pattern in Java + Spring Kafka.
> - [Consumer Group Concepts]({{< relref "/docs/kafka/concepts/consumer-group" >}}) — understand how `groupId` affects partition assignment.
> - [Error Handling Patterns]({{< relref "/docs/kafka/concepts/error-handling" >}}) — Dead Letter Queue and other real-world retry strategies.
