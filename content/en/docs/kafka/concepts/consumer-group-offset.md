---
lastmod: "2026-01-06"
title: Consumer Group & Offset
weight: 3
---

# Consumer Group & Offset

Understanding the core concepts of parallel processing and progress tracking.

## What is a Consumer Group?

A **Consumer Group** is a logical group of Consumers with the same purpose.

```mermaid
flowchart TB
    subgraph Topic["orders Topic"]
        P0[Partition 0]
        P1[Partition 1]
        P2[Partition 2]
    end

    subgraph Group["Consumer Group: order-service"]
        C1[Consumer 1]
        C2[Consumer 2]
        C3[Consumer 3]
    end

    P0 --> C1
    P1 --> C2
    P2 --> C3
```

### Core Rule

> **One Partition can only be read by one Consumer within a Consumer Group**

Why this rule matters:
- **Order guarantee**: Messages from the same Partition are processed in order
- **Duplication prevention**: Same message isn't processed by multiple Consumers simultaneously

## Consumer Count vs Partition Count

```mermaid
flowchart TB
    subgraph Case1["Consumer ＜ Partition"]
        P1A[P0]
        P1B[P1]
        P1C[P2]
        C1A[Consumer 1]
        C1B[Consumer 2]
        P1A --> C1A
        P1B --> C1A
        P1C --> C1B
    end

    subgraph Case2["Consumer = Partition"]
        P2A[P0]
        P2B[P1]
        P2C[P2]
        C2A[Consumer 1]
        C2B[Consumer 2]
        C2C[Consumer 3]
        P2A --> C2A
        P2B --> C2B
        P2C --> C2C
    end

    subgraph Case3["Consumer ＞ Partition"]
        P3A[P0]
        P3B[P1]
        C3A[Consumer 1]
        C3B[Consumer 2]
        C3C["Consumer 3<br>(Idle)"]
        P3A --> C3A
        P3B --> C3B
    end
```

| Situation | Result |
|-----------|--------|
| Consumer < Partition | Some Consumers handle multiple Partitions |
| Consumer = Partition | Optimal (1:1 mapping) |
| Consumer > Partition | Some Consumers remain idle |

## Multiple Consumer Groups

Different Consumer Groups consume messages **independently**.

```mermaid
flowchart TB
    subgraph Topic["orders Topic"]
        P0[Partition 0]
        P1[Partition 1]
    end

    subgraph Group1["Group: order-service"]
        C1[Consumer]
    end

    subgraph Group2["Group: analytics-service"]
        C2[Consumer]
    end

    subgraph Group3["Group: notification-service"]
        C3[Consumer]
    end

    P0 --> C1
    P1 --> C1
    P0 --> C2
    P1 --> C2
    P0 --> C3
    P1 --> C3
```

Each group:
- Receives all messages independently
- Manages separate Offsets
- Processes in parallel without affecting each other

## What is an Offset?

An **Offset** is a sequential position number of a message within a Partition.

```
Partition 0:
┌─────┬─────┬─────┬─────┬─────┬─────┬─────┐
│  0  │  1  │  2  │  3  │  4  │  5  │  6  │
└─────┴─────┴─────┴─────┴─────┴─────┴─────┘
                    ↑           ↑
            Current Offset    Latest Offset
             (Read position)   (Newest message)
```

### Types of Offsets

```mermaid
flowchart LR
    START[Earliest<br>Offset 0]
    COMMIT[Committed<br>Offset 3]
    CURRENT[Current<br>Offset 5]
    END[Latest<br>Offset 7]

    START --> COMMIT --> CURRENT --> END
```

| Offset Type | Description |
|-------------|-------------|
| **Earliest** | Position of oldest message |
| **Committed** | Last committed position |
| **Current** | Position Consumer is currently reading |
| **Latest** | Position of newest message |

## Offset Commit

The process of informing Kafka that a Consumer successfully processed messages.

```mermaid
sequenceDiagram
    participant C as Consumer
    participant K as Kafka
    participant OS as Offset Storage

    C->>K: poll() - Request messages
    K-->>C: Messages at Offset 3, 4, 5
    C->>C: Process messages
    C->>OS: Commit Offset 5
    OS-->>C: Commit complete

    Note over C,OS: Resumes from Offset 6 on restart
```

### Auto Commit vs Manual Commit

```yaml
# application.yml
spring:
  kafka:
    consumer:
      enable-auto-commit: true   # Auto commit (default)
      auto-commit-interval: 5000 # Commit every 5 seconds
```

| Method | Pros | Cons |
|--------|------|------|
| **Auto Commit** | Simple implementation | Data loss possible on processing failure |
| **Manual Commit** | Precise control | Complex implementation |

### Manual Commit Example

```java
@KafkaListener(topics = "orders")
public void consume(ConsumerRecord<String, String> record,
                    Acknowledgment ack) {
    try {
        processOrder(record.value());
        ack.acknowledge();  // Commit on success
    } catch (Exception e) {
        // Don't commit - will be reprocessed
        log.error("Processing failed", e);
    }
}
```

## Failure Recovery Scenarios

### When a Consumer Fails

```mermaid
sequenceDiagram
    participant C1 as Consumer 1
    participant C2 as Consumer 2
    participant K as Kafka

    Note over C1,K: Normal state
    C1->>K: Processing Partition 0, 1

    Note over C1: Consumer 1 failure!

    K->>K: Begin rebalancing
    K->>C2: Reassign Partition 0, 1

    Note over C2,K: Recovery complete
    C2->>K: Resume from Committed Offset
```

### Rebalancing

Process of redistributing Partitions within a Consumer Group:

**Trigger conditions:**
- Consumer added/removed
- Consumer failure
- Partition count change

```mermaid
flowchart LR
    subgraph Before["Before Rebalancing"]
        B_P0[P0] --> B_C1[C1]
        B_P1[P1] --> B_C1
        B_P2[P2] --> B_C2[C2]
    end

    subgraph After["After C2 Failure"]
        A_P0[P0] --> A_C1[C1]
        A_P1[P1] --> A_C1
        A_P2[P2] --> A_C1
    end

    Before -->|Rebalancing| After
```

## auto.offset.reset Setting

Behavior when a Consumer Group starts for the first time or has no Offset information:

```yaml
spring:
  kafka:
    consumer:
      auto-offset-reset: earliest  # or latest
```

| Setting | Behavior |
|---------|----------|
| **earliest** | Read from oldest message |
| **latest** | Read only new messages |
| **none** | Error if no Offset exists |

## Summary

```mermaid
flowchart TB
    subgraph ConsumerGroup["Consumer Group"]
        CG[Consumers sharing<br>the same Group ID]
    end

    subgraph Rules["Core Rules"]
        R1[1 Partition = 1 Consumer]
        R2[Each Group is independent]
    end

    subgraph Offset["Offset"]
        O1[Message position tracking]
        O2[Failure recovery point]
    end

    ConsumerGroup --> Rules
    ConsumerGroup --> Offset
```

| Concept | Role |
|---------|------|
| **Consumer Group** | Parallel processing, load balancing |
| **Offset** | Progress tracking, failure recovery |
| **Rebalancing** | Automatic failure recovery |

## Next Steps

- [Replication](../replication/) - Data replication and high availability
