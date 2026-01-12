---
lastmod: "2026-01-06"
title: Core Components
weight: 1
---

# Kafka Core Components

Understanding Kafka's five core components.

## Why is Kafka Needed?

Kafka solves three fundamental problems:

1. **Asynchronous Processing**: Decouples request-response to reduce coupling between systems
2. **High Volume Processing**: Processes large amounts of data in real-time
3. **High Availability**: Maintains service without data loss even during failures

```mermaid
flowchart TB
    subgraph Problem["Traditional Approach Problems"]
        A[Service A] -->|Sync Call| B[Service B]
        B -->|Sync Call| C[Service C]
    end

    subgraph Solution["After Kafka Adoption"]
        D[Service A] -->|Publish| E[Kafka]
        E -->|Subscribe| F[Service B]
        E -->|Subscribe| G[Service C]
    end
```

## Overall Architecture

```mermaid
flowchart LR
    subgraph Producers["Producer"]
        P1[Producer 1]
        P2[Producer 2]
    end

    subgraph Kafka["Kafka Cluster"]
        subgraph Broker1["Broker 1"]
            T1P0[Topic A<br>Partition 0]
        end
        subgraph Broker2["Broker 2"]
            T1P1[Topic A<br>Partition 1]
        end
    end

    subgraph Consumers["Consumer Group"]
        C1[Consumer 1]
        C2[Consumer 2]
    end

    P1 --> T1P0
    P2 --> T1P1
    T1P0 --> C1
    T1P1 --> C2
```

## 1. Producer

**Role:** Client that publishes messages to Kafka

```java
// Producer example in Spring Kafka
@Component
public class OrderProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendOrder(String orderId, String orderData) {
        kafkaTemplate.send("orders", orderId, orderData);
    }
}
```

**Key Concepts:**
- Decides which Topic to send messages to
- Can route to specific Partition via Message Key
- Can choose synchronous/asynchronous sending

## 2. Consumer

**Role:** Client that reads messages from Kafka

```java
// Consumer example in Spring Kafka
@Component
public class OrderConsumer {
    @KafkaListener(topics = "orders", groupId = "order-service")
    public void consume(String message) {
        // Process message
    }
}
```

**Key Concepts:**
- Belongs to a Consumer Group for parallel processing
- Tracks read position via Offset
- Fetches messages using Pull model

## 3. Broker

**Role:** Kafka server that stores and delivers messages

```mermaid
flowchart TB
    subgraph Cluster["Kafka Cluster"]
        B1[Broker 1<br>Leader]
        B2[Broker 2<br>Follower]
        B3[Broker 3<br>Follower]
    end

    B1 <-->|Replication| B2
    B1 <-->|Replication| B3
```

**Key Concepts:**
- Multiple Brokers form a cluster
- Data is persistently stored on disk
- Leader/Follower structure ensures high availability

> **Analogy:** A Broker is like a post office. It receives letters (messages), stores them, and delivers them to recipients (Consumers).

## 4. Topic

**Role:** Logical channel for categorizing messages

```mermaid
flowchart LR
    subgraph Topics["Topics"]
        T1[orders]
        T2[payments]
        T3[notifications]
    end

    OrderService --> T1
    PaymentService --> T2
    NotificationService --> T3
```

**Key Concepts:**
- Groups related messages together
- Composed of multiple Partitions
- Identified by name (e.g., `orders`, `user-events`)

> **Analogy:** A Topic is like a TV channel. Just like news channels and sports channels, it's organized by subject.

## 5. Partition

**Role:** Divides a Topic to enable parallel processing

```mermaid
flowchart TB
    subgraph Topic["orders Topic"]
        P0["Partition 0<br>#91;msg1, msg4, msg7#93;"]
        P1["Partition 1<br>#91;msg2, msg5, msg8#93;"]
        P2["Partition 2<br>#91;msg3, msg6, msg9#93;"]
    end

    subgraph Consumers["Consumer Group"]
        C1[Consumer 1] --> P0
        C2[Consumer 2] --> P1
        C3[Consumer 3] --> P2
    end
```

**Key Concepts:**
- Order is guaranteed within a single Partition
- Same Message Key routes to same Partition
- Number of Partitions = Maximum parallelism

> **Analogy:** Partitions are like checkout counters at a store. More counters means more customers can be served simultaneously.

## Relationships Between Components

```mermaid
flowchart TB
    P[Producer] -->|"1. Publish Message"| T[Topic]
    T -->|"2. Select Partition"| Part[Partition]
    Part -->|"3. Store"| B[Broker]
    B -->|"4. Replicate"| B2[Broker Replica]
    Part -->|"5. Deliver Message"| CG[Consumer Group]
    CG -->|"6. Process"| C[Consumer]
```

## Summary

| Component | Role | Analogy |
|-----------|------|---------|
| **Producer** | Publish messages | Person sending letters |
| **Consumer** | Consume messages | Person receiving letters |
| **Broker** | Store/deliver messages | Post office |
| **Topic** | Categorize messages | TV channel |
| **Partition** | Unit of parallel processing | Checkout counter |

## Next Steps

- [Message Flow](../message-flow/) - Learn how messages are delivered in detail
