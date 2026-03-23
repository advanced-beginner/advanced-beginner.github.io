---
lastmod: "2026-01-10"
title: Frequently Asked Questions
weight: 4
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

This section compiles frequently asked questions and answers about Kafka. Organized by topic from basic concepts to configuration, error handling, performance tuning, operations, and Spring Kafka.

{{< callout type="tip" title="TL;DR" >}}
- **Basic Concepts**: Kafka is a distributed event streaming platform, not a message queue; order is guaranteed per Partition
- **Configuration**: Use `acks=all` for safety, `auto.offset.reset=earliest` recommended, manual commit to prevent message loss
- **Error Handling**: Use `@RetryableTopic` for retries then move to Dead Letter Topic
- **Performance**: Producer - batch/compression; Consumer - increase instances and adjust fetch settings
- **Operations**: Recommend `replication.factor=3`, `min.insync.replicas=2`
- **Spring Kafka**: Simple implementation with `KafkaTemplate` and `@KafkaListener`
{{< /callout >}}

#### Basic Concepts

**Q: Is Kafka a message queue?**

Kafka is not a message queue but a distributed event streaming platform. Compared to traditional message queues like RabbitMQ, there are several differences.

In terms of message retention, RabbitMQ deletes messages immediately after a Consumer consumes them, while Kafka retains messages until the configured retention period. This characteristic allows Kafka to reprocess already processed messages by moving the Offset, which is impossible with message queues. Order guarantees also differ. Message queues guarantee order across the entire queue, but Kafka only guarantees order within a Partition. In terms of scalability, message queues mainly rely on vertical scaling, while Kafka enables easy horizontal scaling based on Partitions.

Therefore, Kafka is suitable for event sourcing and CQRS architecture, real-time stream processing, log aggregation from multiple systems, and situations requiring message reprocessing.

**Q: What's the right number of Partitions?**

Partition count is determined considering throughput requirements and Consumer count. A rough formula is "Partition count = max(throughput requirement / single Partition throughput, Consumer count)".

General guidelines are 3-6 for small systems in development or test environments, 6-12 for medium-sized systems in general production environments, and 12-50 for large systems requiring high throughput.

There are points to note. Partitions can be increased but not decreased, so it's safer to start small and increase as needed. More Partitions increase Leader Election time and metadata management overhead. If Partition count is less than Consumer count, some Consumers become idle.

**Q: How is message order guaranteed?**

In Kafka, message order is guaranteed only within the same Partition. Therefore, messages requiring order must use the same Key to be sent to the same Partition.

```java
// Messages with a specific key always go to the same Partition
kafkaTemplate.send("orders", orderId, orderEvent);
//                          ^ Key
```

The Key selection criteria is using business entity identifiers. In order systems, use orderId; for user activity logs, use userId; for IoT data, use deviceId as the Key. This ensures order is maintained for events related to the same order, user, or device.

**Q: Why is Consumer Group necessary?**

Consumer Group is necessary for parallel processing and fault recovery. Consumers with the same Group ID share Topic Partitions for processing. For example, if 3 Consumers process 6 Partitions, each Consumer handles 2.

Benefits include: distributing Partitions to increase throughput; when a Consumer fails, its Partitions are automatically reassigned to other Consumers; independent Consumer Groups can each process the same messages, allowing multiple services to utilize the same events.

{{< callout type="info" title="Key Points" >}}
- Kafka is a **distributed event streaming platform**, not a message queue
- Message order is guaranteed only **within the same Partition**
- Start with fewer Partitions and increase as needed (cannot decrease)
- Consumer Groups implement **parallel processing** and **fault recovery**
{{< /callout >}}

#### Configuration

**Q: How should I configure acks?**

The acks setting should be chosen based on data importance. acks=0 sends without confirmation for highest throughput but with potential message loss, suitable for loss-tolerant data like logs or metrics. acks=1 confirms with Leader only, balancing throughput and safety for general events. acks=all requires confirmation from all ISR, increasing latency but safest for critical data like financial or order data.

```yaml
# application.yml
spring:
  kafka:
    producer:
      acks: all                    # Recommended
      properties:
        min.insync.replicas: 2     # Confirm at least 2 replicas
```

**Q: What value should I use for auto.offset.reset?**

auto.offset.reset determines where to start reading when a Consumer Group first starts or has no saved Offset. earliest reads from the beginning to prevent data loss and is recommended in most cases. latest reads from the newest for real-time processing only. none throws an exception if no saved Offset exists, for strict Offset management.

Important: this setting only applies when it's a new Consumer Group. Existing groups with committed Offsets use their saved Offset.

```yaml
spring:
  kafka:
    consumer:
      auto-offset-reset: earliest  # Recommended
```

**Q: Should I enable enable.auto.commit?**

Setting enable.auto.commit to false and using manual commit is recommended. Auto commit commits Offset at configured intervals, so it may commit before message processing completes. In this case, the message is lost if processing fails.

```java
// Auto commit: may commit before processing
@KafkaListener(topics = "orders")
public void listen(String message) {
    processOrder(message);  // Offset already committed even if this fails
}

// Manual commit: commit after successful processing
@KafkaListener(topics = "orders")
public void listen(String message, Acknowledgment ack) {
    processOrder(message);
    ack.acknowledge();  // Explicit commit after successful processing
}
```

Using manual commit means committing only after successful message processing, enabling reprocessing on failure. Configure enable-auto-commit as false and ack-mode as manual.

{{< callout type="info" title="Key Points" >}}
- **acks=all**: Ensure safety with all ISR confirmation for important data
- **auto.offset.reset=earliest**: Read from beginning to prevent data loss
- **enable.auto.commit=false**: Manual commit to save Offset only after processing completes
{{< /callout >}}

#### Error Handling

**Q: What happens when an exception occurs in Consumer?**

By default, exceptions cause infinite retries, and if unresolved, the application stops. To prevent this, explicit error handling strategies are needed.

The recommended approach is using @RetryableTopic to retry a specified number of times, then move to Dead Letter Topic if all retries fail.

```java
@RetryableTopic(
    attempts = "3",
    backoff = @Backoff(delay = 1000, multiplier = 2),
    dltTopicSuffix = "-dlt"
)
@KafkaListener(topics = "orders")
public void listen(OrderEvent event) {
    processOrder(event);
}
```

With this configuration: first failure retries after 1 second, second failure retries after 2 seconds, third failure moves the message to orders-dlt Topic.

**Q: How do I handle Dead Letter Topic (DLT)?**

Messages arriving at DLT are monitored by a separate Consumer and processed manually. The @DltHandler annotation defines a handler called when DLT messages are received.

```java
@DltHandler
public void handleDlt(OrderEvent event,
                      @Header(KafkaHeaders.ORIGINAL_TOPIC) String topic,
                      @Header(KafkaHeaders.EXCEPTION_MESSAGE) String error) {
    log.error("DLT received - Topic: {}, Error: {}", topic, error);
    alertService.sendAlert(event, error);
    // Manual review then reprocess or discard
}
```

DLT operational strategies include: setting up Slack or email alerts, periodically reviewing DLT messages, republishing to original Topic after problem resolution, or discarding if unresolvable.

**Q: Why is idempotency important?**

Because duplicate messages can occur due to network failures. If a Producer sends a message, the Broker stores it and sends an ACK, but the ACK is lost due to network error, the Producer judges it as transmission failure and resends. In this case, the same message is stored twice.

For the Producer side, set enable.idempotence to true to prevent duplicate transmission at the Producer level.

```yaml
spring:
  kafka:
    producer:
      properties:
        enable.idempotence: true
```

Consumer side must also ensure idempotency. Implement by storing processed message IDs and skipping duplicate messages. Or design business logic itself to be idempotent. For example, instead of "deduct 1000 from balance", implement "set balance to 50000".

{{< callout type="info" title="Key Points" >}}
- **@RetryableTopic**: Configure retry count and backoff strategy
- **Dead Letter Topic**: Store failed messages in separate Topic for manual processing
- **Idempotency**: Producer uses `enable.idempotence=true`, Consumer prevents duplicates in business logic
{{< /callout >}}

#### Performance Tuning

**Q: How do I increase Producer throughput?**

Enable batching and compression to increase throughput.

```yaml
spring:
  kafka:
    producer:
      batch-size: 32768         # 32KB batch
      properties:
        linger.ms: 20           # Wait 20ms before sending
        compression.type: lz4   # Compression
        buffer.memory: 67108864 # 64MB buffer
```

batch.size increased from default 16KB to 32KB or more sends more messages at once. linger.ms set from default 0 to 5-100ms waits for batch to fill, improving efficiency. compression.type using lz4 or snappy reduces network load.

Note that longer linger.ms increases latency, so balance throughput and latency.

**Q: How do I increase Consumer throughput?**

Increase Consumer instances and adjust fetch settings.

```yaml
spring:
  kafka:
    consumer:
      properties:
        fetch.min.bytes: 50000      # Minimum 50KB
        fetch.max.wait.ms: 500      # Maximum 500ms wait
        max.poll.records: 500       # Maximum 500 per poll
```

Strategy varies by bottleneck. If Consumer CPU is the bottleneck, add Consumer instances. If Partition count is insufficient, increase Partitions. If network is the bottleneck, adjust fetch settings to retrieve more data at once.

**Q: Consumer Lag keeps increasing**

Increasing Consumer Lag means message arrival rate exceeds processing rate.

First check current status:

```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group order-processor-group --describe
```

Solutions by cause: if Consumer count is insufficient, add instances; if external API calls are slow, switch to async processing and set timeouts; if DB is the bottleneck, apply batch processing and optimize indexes; if business logic is inefficient, profile to find bottlenecks and optimize.

{{< callout type="info" title="Key Points" >}}
- **Producer Tuning**: Increase `batch.size`, set `linger.ms`, apply `compression.type=lz4`
- **Consumer Tuning**: Increase instance count, adjust `fetch.min.bytes` and `max.poll.records`
- **Consumer Lag**: Occurs when processing rate < arrival rate, identify bottleneck and resolve
{{< /callout >}}

#### Operations

**Q: How do I monitor Kafka?**

Collect JMX metrics and monitor key indicators. Core metrics: Consumer Lag indicates processing delay, set warning above 1000; Under-replicated Partitions indicates replication delay, warning if above 0; Request Latency indicates request delay, warning if above 100ms; Disk Usage warning above 80%.

Integrating Prometheus and Grafana enables visualization and alerting.

```yaml
# Prometheus AlertManager
- alert: KafkaConsumerLagHigh
  expr: kafka_consumer_lag > 10000
  for: 5m
  labels:
    severity: warning
```

**Q: What happens when a Broker goes down?**

Automatic recovery based on Replication settings. When a Leader Broker fails, one of the Followers in ISR is elected as new Leader, and remaining Followers follow the new Leader.

For automatic recovery: replication.factor must be 2 or higher, min.insync.replicas must be 2 or higher, and there must be surviving Brokers in ISR.

For production environments, setting replication.factor to 3 and min.insync.replicas to 2 is recommended. This ensures service continuity even if 1 Broker goes down.

**Q: How do I set message retention period?**

Configure retention per Topic.

```bash
# 7 days retention
kafka-configs.sh --bootstrap-server localhost:9092 \
  --alter --entity-type topics --entity-name orders \
  --add-config retention.ms=604800000
```

retention.ms is time-based, 7 days is 604800000ms. retention.bytes is size-based, 1GB is 1073741824bytes.

Recommended periods: general events 7 days, audit logs 90+ days, debugging 1-3 days. Decide based on disk capacity and business requirements.

{{< callout type="info" title="Key Points" >}}
- **Monitoring Metrics**: Consumer Lag, Under-replicated Partitions, Request Latency, Disk Usage
- **High Availability**: Recommend `replication.factor=3`, `min.insync.replicas=2`
- **Broker Failure**: Recovery via ISR-based automatic Leader Election
- **Message Retention**: Configure `retention.ms` and `retention.bytes` based on business needs
{{< /callout >}}

#### Spring Kafka

**Q: What's the difference between KafkaTemplate and KafkaProducer?**

KafkaTemplate is a Spring abstraction layer that's more convenient. It integrates with Spring Boot auto-configuration requiring only dependency injection, supports transactions, and simplifies callback handling.

```java
// KafkaTemplate (Spring abstraction)
@Autowired
private KafkaTemplate<String, OrderEvent> template;

public void send(OrderEvent event) {
    template.send("orders", event.orderId(), event);
}

// KafkaProducer (low-level API) - Not recommended in Spring
Producer<String, OrderEvent> producer = new KafkaProducer<>(props);
producer.send(new ProducerRecord<>("orders", event));
```

KafkaProducer is Kafka's low-level Java API, used only in non-Spring environments or special cases requiring fine-grained control.

**Q: How many threads does @KafkaListener use?**

By default, threads are created equal to Partition count. You can specify maximum thread count with concurrency setting.

```yaml
spring:
  kafka:
    listener:
      concurrency: 3  # Maximum 3 threads
```

The rules are: if concurrency is less than or equal to Partition count, threads equal to concurrency are created; if concurrency exceeds Partition count, only threads equal to Partition count are created and the rest are idle.

For example: with 6 Partitions and concurrency 3, 3 threads each handle 2 Partitions; with 3 Partitions and concurrency 6, only 3 threads are created, each handling 1 Partition.

{{< callout type="info" title="Key Points" >}}
- **KafkaTemplate**: Spring abstraction layer, auto-configuration and transaction support
- **@KafkaListener**: Declarative Consumer implementation, adjust thread count with `concurrency`
- **Thread Rule**: Actual threads = min(concurrency, Partition count)
{{< /callout >}}

#### Next Steps

- [Glossary](glossary/) - Kafka terminology
- [References](references/) - Learning resources
