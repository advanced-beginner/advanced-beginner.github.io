---
lastmod: "2026-01-10"
title: Consumer Advanced Operations
weight: 4
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

{{< callout type="info" title="TL;DR" >}}
- session.timeout.ms controls failure detection; max.poll.interval.ms limits processing time
- Use Cooperative Sticky Assignor to minimize rebalancing impact (Kafka 2.4+)
- Static Group Membership prevents rebalancing on restarts
- Consumer Lag is the most important monitoring metric; trend observation is key
- Use kafka-consumer-groups.sh for manual Offset reset (Consumer must be stopped)
{{< /callout >}}

**Target audience**: Developers and operators running Kafka Consumers in production

**Prerequisites**: Basic concepts from [Consumer Group & Offset](consumer-group/), ISR and Leader concepts from [Replication](replication/)

---

This document covers rebalancing optimization, Consumer Lag monitoring, and troubleshooting. It is based on Kafka 3.6.x, with code examples verified in Spring Boot 3.2.x, Spring Kafka 3.1.x, Micrometer 1.12.x, and Java 17 environments.

Before reading this document, you should understand basic concepts from [Consumer Group & Offset](consumer-group/) and ISR and Leader concepts from [Replication](replication/).

#### Core Consumer Group Settings

There are key settings that affect rebalancing and failure detection.

**Session and Heartbeat Settings**

```yaml
# application.yml - Kafka 3.6 default values shown
spring:
  kafka:
    consumer:
      properties:
        session.timeout.ms: 45000      # Default: 45 seconds (Kafka 3.0+)
        heartbeat.interval.ms: 3000    # Default: 3 seconds
        max.poll.interval.ms: 300000   # Default: 5 minutes
```

`session.timeout.ms` is the time after which the Broker considers a Consumer as failed. If no Heartbeat arrives within this time, the Consumer is assumed dead and rebalancing begins. Setting it to about 3 times the network latency is recommended.

`heartbeat.interval.ms` is the Heartbeat transmission interval. This is how often the Consumer sends "I'm alive" signals to the Broker. Setting it to 1/15 or less of session.timeout is recommended.

`max.poll.interval.ms` is the maximum allowed interval between poll() calls. If the next poll() is not called within this time, the Consumer is considered abnormal and removed from the group. Setting it to about 2 times the message processing time is recommended.

{{< callout type="info" title="Key Points" >}}
- session.timeout.ms: Heartbeat-based failure detection (default 45 seconds)
- max.poll.interval.ms: Processing time limit (default 5 minutes), rebalancing occurs if exceeded
- heartbeat.interval.ms should be set to 1/15 or less of session.timeout.ms
{{< /callout >}}

**max.poll.interval.ms Issues and Solutions**

External API calls or complex processing can extend the poll() interval, triggering rebalancing.

```java
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final BlockingQueue<String> retryQueue = new LinkedBlockingQueue<>();

    public OrderConsumer(PaymentService paymentService, OrderRepository orderRepository) {
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
    }

    // Problem: Timeout due to synchronous processing
    @KafkaListener(topics = "orders", groupId = "order-service-bad")
    public void consumeBad(String order) {
        // External payment API call - can take up to 3 minutes
        PaymentResult result = paymentService.process(order);  // Dangerous!
        // Rebalancing occurs if max.poll.interval.ms (5 min) is exceeded
    }

    // Solution: Async processing + manual commit
    @KafkaListener(topics = "orders", groupId = "order-service-async")
    public void consumeAsync(String order, Acknowledgment ack) {
        // Save to DB first (fast)
        orderRepository.saveForProcessing(order);
        ack.acknowledge();  // Commit immediately

        // Process in separate thread (no poll() blocking)
        CompletableFuture.runAsync(() -> paymentService.process(order))
            .exceptionally(ex -> {
                log.error("Payment processing failed. Moving to retry queue: {}", order, ex);
                retryQueue.add(order);
                return null;
            });
    }
}
```

Besides async processing, reducing `max.poll.records` is another option. Reducing the number of records fetched at once shortens processing time. In Spring Kafka, configure with `spring.kafka.consumer.max-poll-records: 10` (default 500).

{{< callout type="info" title="Key Points" >}}
- Synchronous external calls can exceed max.poll.interval.ms and trigger rebalancing
- Solutions: Async processing + manual commit, or reduce max.poll.records
- Recommended pattern: Save to DB first, commit immediately, process in separate thread
{{< /callout >}}

#### Deep Dive into Rebalancing

During rebalancing, all Consumers are paused. Based on Eager Protocol, when rebalancing starts, all Consumers release their Partitions (Stop-the-World), the Group Coordinator calculates new assignments, then assigns new Partitions to each Consumer. With 10 Consumers, this takes about 1 second; with 100 Consumers, about 10 seconds. In large clusters, it can take minutes.

**Cooperative Sticky Assignor (Kafka 2.4+, Recommended)**

Unlike the existing Eager Protocol, this reassigns only the necessary Partitions.

```yaml
spring:
  kafka:
    consumer:
      properties:
        partition.assignment.strategy: org.apache.kafka.clients.consumer.CooperativeStickyAssignor
```

The problem with Eager Protocol is that when all Consumers release Partitions simultaneously, throughput momentarily drops to 0. At LinkedIn, this "Stop-the-World" time in large clusters took minutes, causing SLA violations.

Cooperative Protocol (KIP-429) performs rebalancing in two phases. In the first phase, only necessary Partitions are released while unaffected Consumers continue processing. In the second phase, released Partitions are assigned to new Consumers. The key principle is not "release first, receive later" but "release only what's needed, receive immediately."

**Static Group Membership (Kafka 2.3+)**

A feature that prevents rebalancing on Consumer restart, useful for Kubernetes Rolling Updates.

```yaml
spring:
  kafka:
    consumer:
      properties:
        # Fixed ID assignment - recognized as the same Consumer even after restart
        group.instance.id: ${HOSTNAME:consumer-1}
        session.timeout.ms: 300000  # 5 minutes (allow time for restart)
```

Assigning a fixed ID prevents unnecessary rebalancing as the Consumer is recognized as the same one even after restart. session.timeout.ms should be set longer than the time needed for restart.

{{< callout type="info" title="Key Points" >}}
- Eager Protocol: Full Stop-the-World, can cause minute-long outages in large clusters
- Cooperative Sticky Assignor: Reassigns only what's needed, minimizes impact (Kafka 2.4+)
- Static Group Membership: Prevents rebalancing on restart, useful for K8s Rolling Update
{{< /callout >}}

**Rebalancing Monitoring Implementation**

```java
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

public class RebalanceMonitor implements ConsumerRebalanceListener {

    private static final Logger log = LoggerFactory.getLogger(RebalanceMonitor.class);
    private final MeterRegistry meterRegistry;
    private Instant rebalanceStart;

    public RebalanceMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        rebalanceStart = Instant.now();
        log.warn("Partitions revoked: {}. Need to commit in-flight messages!", partitions);

        meterRegistry.counter("kafka.rebalance.revoked",
            "partitions", String.valueOf(partitions.size())).increment();
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        Duration duration = Duration.between(rebalanceStart, Instant.now());

        log.info("Partitions assigned: {}. Rebalancing duration: {}ms",
                 partitions, duration.toMillis());

        Timer.builder("kafka.rebalance.duration")
            .description("Consumer rebalance duration")
            .register(meterRegistry)
            .record(duration);
    }
}
```

To register a ConsumerRebalanceListener in Spring Kafka, use `getContainerProperties().setConsumerRebalanceListener()` in the ContainerFactory configuration.

#### Consumer Lag Monitoring

Consumer Lag is the number of messages sent by the Producer minus the number processed by the Consumer. It's the most important monitoring metric.

**Lag Check Commands**

```bash
# List all Consumer Groups
kafka-consumer-groups.sh --list --bootstrap-server localhost:9092

# Detailed info for specific Consumer Group
kafka-consumer-groups.sh --describe --group order-service \
    --bootstrap-server localhost:9092

# Example output:
# GROUP           TOPIC    PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG      CONSUMER-ID
# order-service   orders   0          15234           15300           66       consumer-1-xxx
# order-service   orders   1          14892           15100           208      consumer-2-xxx
# order-service   orders   2          15001           15001           0        consumer-3-xxx
```

Lag 0-100 is normal. 100-1,000 requires attention - check processing speed. 1,000-10,000 is a warning level - consider adding more Consumers. 10,000+ is critical - immediate action required.

The LAG trend is more important than the absolute LAG value. LAG of 1000 remaining constant is fine, but LAG of 100 that keeps increasing requires action.

{{< callout type="info" title="Key Points" >}}
- Consumer Lag = messages to be processed, the most important monitoring metric
- Lag 0-100: normal, 100-1000: caution, 1000-10000: warning, 10000+: critical
- Trend is more important than absolute value; take action on persistent increase
{{< /callout >}}

**Prometheus + Grafana Monitoring**

You can collect Consumer Lag to Prometheus using kafka-exporter.

```yaml
# docker-compose.yml
services:
  kafka-exporter:
    image: danielqsj/kafka-exporter:v1.7.0
    command:
      - --kafka.server=kafka:9092
      - --web.listen-address=:9308
    ports:
      - "9308:9308"
    depends_on:
      - kafka
```

Key PromQL queries are as follows. `kafka_consumergroup_lag{consumergroup="order-service"}` queries current Lag. `rate(kafka_consumergroup_lag{consumergroup="order-service"}[5m])` shows the 5-minute Lag increase rate; greater than 0 means backlog. `count(kafka_consumergroup_lag > 10000)` indicates the number of partitions with Lag above 10000.

Alert rule configuration examples: Trigger Warning alert if Lag exceeds 10,000 for 5 minutes. Trigger Critical alert if Lag increase rate is above 100/sec for 10 minutes. Trigger Critical alert 1 minute after no active members in Consumer Group.

**Manual Offset Reset**

Used when you need to ignore existing Offsets and read from a specific position. Only possible when Consumer is stopped.

```bash
# Read from the very beginning
kafka-consumer-groups.sh --reset-offsets \
    --group order-service \
    --topic orders \
    --to-earliest \
    --execute \
    --bootstrap-server localhost:9092

# Read from a specific time (failure point)
kafka-consumer-groups.sh --reset-offsets \
    --group order-service \
    --topic orders \
    --to-datetime 2024-01-15T10:00:00.000 \
    --execute \
    --bootstrap-server localhost:9092

# Move to specific Offset
kafka-consumer-groups.sh --reset-offsets \
    --group order-service \
    --topic orders:0:1500 \
    --execute \
    --bootstrap-server localhost:9092

# Skip N messages from current position
kafka-consumer-groups.sh --reset-offsets \
    --group order-service \
    --topic orders \
    --shift-by 1000 \
    --execute \
    --bootstrap-server localhost:9092
```

#### Troubleshooting Checklist

**When Lag Spikes**

```bash
# 1. Check if Consumer is alive
kafka-consumer-groups.sh --describe --group order-service \
    --bootstrap-server localhost:9092 --members --verbose

# If CONSUMER-ID is empty, Consumer is down!

# 2. Check for Producer spike
kafka-get-offsets.sh --topic orders \
    --bootstrap-server localhost:9092

# 3. Check partition imbalance
# If only certain partitions have high LAG, it's a Hot Partition issue
# → Need to check Message Key distribution
```

If only specific Partitions have increasing LAG, it's a Hot Partition (Key skew) issue. Resolve by distributing Keys or adding Partitions. If overall LAG spikes, Consumer processing speed is insufficient - scale out instances. If LAG is 0 but messages are missing, auto commit + processing failure is likely the cause - switch to manual commit. If frequent rebalancing occurs, session.timeout is too short - increase timeout or apply Static Membership.

{{< callout type="info" title="Key Points" >}}
- Specific Partition Lag increase: Hot Partition issue, need to distribute Keys
- Overall Lag spike: Consumer processing speed insufficient, scale out instances
- Lag 0 but missing data: Auto commit + processing failure, switch to manual commit
{{< /callout >}}

#### Production Deployment Checklist

Before deploying a Consumer application to production, verify the following.

**Configuration Check**: Verify group.id naming convention ({service-name}-{purpose}), auto.offset.reset set as intended (usually earliest), enable.auto.commit=false (manual commit recommended), max.poll.interval.ms greater than maximum processing time, session.timeout.ms/heartbeat.interval.ms ratio (15:1 recommended), partition.assignment.strategy is CooperativeStickyAssignor.

**Monitoring Preparation**: Verify Consumer Lag metric collection setup, Lag threshold alerts configured (warning: 10,000 / critical: 50,000), rebalancing occurrence alerts configured, Consumer instance count monitoring.

**Incident Response Preparation**: Prepare DLQ (Dead Letter Queue) configuration, Offset reset procedure documentation, rollback plan, contact information and escalation path.

**Performance Verification**: Complete load testing at 2x expected TPS, verify Consumer instance count is less than or equal to Partition count, monitor memory usage (enable GC logs).

#### FAQ

**Q: Lag is consistently 0, is this normal?**

Yes, it's normal. If Producer speed is equal to or less than Consumer speed, Lag will be close to 0.

**Q: What should I do if rebalancing occurs frequently?**

Increase session.timeout.ms, use CooperativeStickyAssignor, or apply Static Group Membership.

**Q: How do I optimize a slow Consumer?**

Apply parallel processing (concurrency setting), batch processing (batch listener), or async external calls.

#### References

- [Confluent: Incremental Cooperative Rebalancing](https://www.confluent.io/blog/cooperative-rebalancing-in-kafka-streams-consumer-ksqldb/)
- [KIP-429: Consumer Group Protocol Redesign](https://cwiki.apache.org/confluence/display/KAFKA/KIP-429)
- [Kafka Consumer Configurations](https://kafka.apache.org/documentation/#consumerconfigs)

#### Next Steps

- [Producer Tuning](producer-tuning/) - Producer performance optimization
- [Transactions](transactions/) - Exactly-Once processing
