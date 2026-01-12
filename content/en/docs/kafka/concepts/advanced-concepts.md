---
lastmod: "2026-01-06"
title: Advanced Concepts
weight: 5
---

# Advanced Concepts

Understanding acks, Message Key, and Retention policies.

## acks (Acknowledgment)

Determines how Producer confirms successful message delivery.

### acks Options

```mermaid
flowchart TB
    subgraph acks0["acks=0"]
        P0[Producer] -->|Send| L0[Leader]
        P0 -->|Immediate| OK0[Success]
    end

    subgraph acks1["acks=1"]
        P1[Producer] -->|Send| L1[Leader]
        L1 -->|Stored| ACK1[ACK]
        ACK1 --> OK1[Success]
    end

    subgraph acksAll["acks=all"]
        P2[Producer] -->|Send| L2[Leader]
        L2 -->|Replicate| F1[Follower 1]
        L2 -->|Replicate| F2[Follower 2]
        F1 -->|Synced| ACK2[ACK]
        F2 -->|Synced| ACK2
        ACK2 --> OK2[Success]
    end
```

### Option Comparison

| acks | Behavior | Speed | Safety | Use Case |
|------|----------|-------|--------|----------|
| **0** | No response wait | Fastest | Lowest | Logs, metrics |
| **1** | Leader store confirmed | Medium | Medium | General events |
| **all** | All ISR replicated | Slowest | Highest | Payments, orders |

### ⚠️ Important: The acks=all Pitfall

> **`acks=all` alone doesn't guarantee data safety!**

`acks=all` confirms replication to "all replicas in ISR". But what if only the Leader remains in ISR?

```mermaid
flowchart TB
    subgraph Problem["acks=all but ISR=1"]
        P[Producer] -->|acks=all| L[Leader only in ISR]
        L -->|ACK| P
        F1[Follower 1]
        F2[Follower 2]
        L -.->|Sync lag| F1
        L -.->|Sync lag| F2
        NOTE[acks=all succeeds<br>with only Leader!]
    end
```

**Solution: Use with `min.insync.replicas`**

```yaml
# Topic configuration (recommended)
min.insync.replicas: 2  # Require at least 2 replicas

# Producer configuration
acks: all
```

| Configuration | ISR=3 | ISR=2 | ISR=1 |
|---------------|-------|-------|-------|
| `acks=all` only | ✅ Success | ✅ Success | ✅ Success (Risky!) |
| `acks=all` + `min.insync.replicas=2` | ✅ Success | ✅ Success | ❌ Failure (Safe) |

### Spring Kafka Configuration

```yaml
spring:
  kafka:
    producer:
      acks: all  # Recommended
      retries: 3
```

### Trade-off Diagram

```mermaid
flowchart LR
    subgraph Tradeoff["acks Trade-off"]
        SPEED[Speed]
        SAFE[Safety]
    end

    SPEED <-->|Inverse| SAFE

    A0["acks=0<br>Fast, Risky"] --> SPEED
    A1["acks=1<br>Balanced"] --> SPEED
    A1 --> SAFE
    AALL["acks=all<br>Slow, Safe"] --> SAFE
```

## Message Key

Used to route messages to specific Partitions.

### Role of Key

```mermaid
flowchart TB
    subgraph WithKey["With Key: 'user-123'"]
        M1[Message 1] -->|hash| P0[Partition 0]
        M2[Message 2] -->|hash| P0
        M3[Message 3] -->|hash| P0
    end

    subgraph WithoutKey["Without Key"]
        M4[Message 1] -->|round-robin| P1[Partition 0]
        M5[Message 2] -->|round-robin| P2[Partition 1]
        M6[Message 3] -->|round-robin| P3[Partition 2]
    end
```

### Order Guarantee

> **Same Key = Same Partition = Order Guaranteed**

```mermaid
sequenceDiagram
    participant P as Producer
    participant K as Kafka
    participant C as Consumer

    P->>K: Key="order-1", "Order Created"
    P->>K: Key="order-1", "Payment Complete"
    P->>K: Key="order-1", "Shipping Started"

    Note over K: All in same Partition

    K->>C: "Order Created"
    K->>C: "Payment Complete"
    K->>C: "Shipping Started"

    Note over C: Processed in order
```

### Use Cases

| Key Choice | Effect | Example |
|------------|--------|---------|
| **User ID** | Guarantee order per user | `user-123` |
| **Order ID** | Guarantee order status sequence | `order-456` |
| **Device ID** | Group IoT device data | `device-789` |

### Spring Kafka Code

```java
// With Key
kafkaTemplate.send("orders", orderId, orderJson);
//                  Topic    Key      Value

// Without Key (round-robin)
kafkaTemplate.send("logs", null, logMessage);
```

### Caution

```mermaid
flowchart TB
    subgraph Problem["When Partition Count Changes"]
        BEFORE["3 Partitions<br>Key 'A' → P0"]
        AFTER["5 Partitions<br>Key 'A' → P2"]
        WARN[Same Key goes to<br>different Partition!]
    end

    BEFORE -->|Danger| AFTER
    AFTER --> WARN
```

> **Warning:** Changing Partition count changes Key hash, causing existing and new messages to go to different Partitions.

## Retention Policy

Determines how long messages are kept.

### Policy Types

```mermaid
flowchart TB
    subgraph Policies["Retention Policies"]
        TIME["Time-based<br>e.g., 7 days"]
        SIZE["Size-based<br>e.g., 100GB"]
        COMPACT["Compaction<br>Keep latest only"]
    end

    TIME --> DELETE1[Delete old messages]
    SIZE --> DELETE2[Delete when over capacity]
    COMPACT --> KEEP[Keep latest per Key]
```

### Time-based (Default)

```yaml
# Topic configuration
retention.ms: 604800000  # 7 days (default)
```

```
Day 1    Day 2    Day 3    ...    Day 7    Day 8
[msg1]   [msg2]   [msg3]          [msg7]   [Deleted]
```

### Size-based

```yaml
retention.bytes: 107374182400  # 100GB
```

Deletes oldest segments when capacity exceeded.

### Log Compaction

Policy that **keeps only the last value per Key**.

```mermaid
flowchart LR
    subgraph Before["Before Compaction"]
        B1["K1:v1"]
        B2["K2:v1"]
        B3["K1:v2"]
        B4["K1:v3"]
        B5["K2:v2"]
    end

    subgraph After["After Compaction"]
        A1["K1:v3"]
        A2["K2:v2"]
    end

    Before -->|Compaction| After
```

### Recommended Settings by Use Case

| Use Case | Policy | Example Setting |
|----------|--------|-----------------|
| **Event logs** | Time-based | 7-day retention |
| **Audit logs** | Time-based | 1-year retention |
| **User state** | Compaction | Latest state only |
| **Session data** | Time-based | 24 hours |

### Compaction Configuration

```yaml
# Topic configuration
cleanup.policy: compact
min.cleanable.dirty.ratio: 0.5
```

## Idempotent Producer

Guarantees **no duplicate messages** during retransmission due to network errors.

### Problem Scenario

```mermaid
sequenceDiagram
    participant P as Producer
    participant B as Broker

    P->>B: Send message (seq=1)
    B->>B: Stored
    B--xP: ACK lost (network error)

    Note over P: No ACK → Retransmit
    P->>B: Retransmit same message (seq=1)
    B->>B: Duplicate stored! ❌
```

### Solution: Idempotent Producer

```mermaid
sequenceDiagram
    participant P as Producer (PID=100)
    participant B as Broker

    P->>B: Message (PID=100, seq=0)
    B->>B: Store, record seq=0
    B--xP: ACK lost

    P->>B: Retransmit (PID=100, seq=0)
    B->>B: seq=0 already processed → Ignore
    B->>P: ACK (duplicate prevented) ✅
```

### How It Works

| Concept | Description |
|---------|-------------|
| **Producer ID (PID)** | Producer identifier, assigned by broker |
| **Sequence Number** | Message sequence per Partition |
| **Duplicate Detection** | Same PID + seq is ignored |

### Configuration

```yaml
spring:
  kafka:
    producer:
      properties:
        enable.idempotence: true  # Default: true (Kafka 3.0+)
```

### Caution

```java
// Automatically configured when Idempotent Producer is enabled
acks = all                              // Required
retries = Integer.MAX_VALUE             // Infinite retries
max.in.flight.requests.per.connection = 5  // Max 5
```

> **Note:** `enable.idempotence=true` is the default since Kafka 3.0.

## Combined Configuration Examples

### High-Reliability Production Environment

```yaml
# Producer
spring:
  kafka:
    producer:
      acks: all
      retries: 3
      properties:
        enable.idempotence: true  # Kafka 3.0+ default
        max.in.flight.requests.per.connection: 5

# Topic creation
kafka-topics.sh --create \
  --topic orders \
  --partitions 6 \
  --replication-factor 3 \
  --config min.insync.replicas=2 \
  --config retention.ms=604800000
```

### High-Performance Logging Environment

```yaml
# Producer
spring:
  kafka:
    producer:
      acks: 0
      batch-size: 65536
      linger-ms: 10

# Topic
retention.ms: 86400000  # 1 day
```

## Summary

```mermaid
flowchart TB
    subgraph Concepts["Advanced Concepts"]
        ACKS["acks<br>Delivery guarantee level"]
        KEY["Message Key<br>Partitioning, ordering"]
        RET["Retention<br>Storage policy"]
    end

    subgraph Usage["Usage Guide"]
        U1["Critical data: acks=all"]
        U2["Order needed: Use Key"]
        U3["State storage: Compaction"]
    end

    ACKS --> U1
    KEY --> U2
    RET --> U3
```

| Concept | Key Question | Recommendation |
|---------|--------------|----------------|
| **acks** | How safe? | Production: `all` |
| **Message Key** | Order important? | Use Key when order matters |
| **Retention** | How long to keep? | Based on requirements |

## Next Steps

- [Transactions & Exactly-Once](../transactions/) - Message delivery guarantees and Transaction API
- [Hands-on Examples](../../examples/) - Apply learned concepts
