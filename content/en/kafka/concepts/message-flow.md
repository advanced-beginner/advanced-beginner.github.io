---
lastmod: "2026-01-06"
title: Message Flow
weight: 2
---

# Message Flow

Understanding the complete process of message publication and consumption in Kafka.

## Overview of the Complete Flow

```mermaid
sequenceDiagram
    participant P as Producer
    participant B as Broker
    participant Part as Partition
    participant C as Consumer

    P->>B: 1. Send message
    B->>Part: 2. Select Partition & store
    B-->>P: 3. ACK response
    C->>B: 4. Request messages (poll)
    B->>C: 5. Deliver messages
    C->>B: 6. Commit Offset
```

## Step 1: Message Publishing (Produce)

Producer sends messages to Kafka.

```mermaid
flowchart LR
    subgraph Producer
        MSG[Create Message]
        SER[Serialize]
        PART[Determine Partition]
    end

    subgraph Kafka
        B[Broker]
    end

    MSG --> SER --> PART --> B
```

### Publishing Process

1. **Create Message**: Compose message as Key-Value pair
2. **Serialize**: Convert object to byte array
3. **Determine Partition**: Key hash or round-robin
4. **Send**: Transmit to Broker over network

```java
// Producer code example
kafkaTemplate.send("orders", orderId, orderJson);
//                  Topic    Key      Value
```

### Partition Selection Methods

```mermaid
flowchart TB
    MSG[Message]
    KEY{Key exists?}
    HASH["Key hash mod Partition count"]
    RR[Round Robin]
    P0[Partition 0]
    P1[Partition 1]
    P2[Partition 2]

    MSG --> KEY
    KEY -->|Yes| HASH
    KEY -->|No| RR
    HASH --> P0
    HASH --> P1
    RR --> P0
    RR --> P1
    RR --> P2
```

- **With Key**: Same Key always goes to same Partition
- **Without Key**: Round-robin for even distribution

## Step 2: Message Storage (Store)

Broker stores messages in Partitions.

```mermaid
flowchart TB
    subgraph Partition["Partition 0"]
        direction LR
        O0["Offset 0\nmsg1"]
        O1["Offset 1\nmsg2"]
        O2["Offset 2\nmsg3"]
        O3["Offset 3\nmsg4"]
        NEW["Offset 4\nNew message"]
    end

    MSG[New Message] -->|Append| NEW
```

### Storage Characteristics

| Characteristic | Description |
|----------------|-------------|
| **Sequential Storage** | Messages appended to end of Partition (Append-only) |
| **Immutability** | Stored messages cannot be modified |
| **Offset Assignment** | Each message gets a unique sequence number |
| **Durability** | Stored on disk, survives restarts |

### What is an Offset?

```
Partition 0: [0] [1] [2] [3] [4] [5] [6] [7]
                              ↑           ↑
                       Current Offset    Latest Offset
                        (Read position)   (Newest message)
```

- Unique identifier for each message (sequential number)
- Consumer tracks read position based on Offset
- Starts at 0 and increases infinitely

## Step 3: Message Consumption (Consume)

Consumer fetches messages from Broker.

```mermaid
sequenceDiagram
    participant C as Consumer
    participant B as Broker
    participant P as Partition

    loop Polling Loop
        C->>B: poll() - Request messages
        B->>P: Read from Offset position
        P-->>B: Return messages
        B-->>C: Deliver messages
        C->>C: Process messages
        C->>B: Commit Offset
    end
```

### Consumption Process

1. **Poll**: Consumer requests messages from Broker
2. **Fetch**: Broker reads messages from Partition
3. **Process**: Consumer executes business logic
4. **Commit**: Save processed Offset

```java
// Consumer code example
@KafkaListener(topics = "orders", groupId = "order-service")
public void consume(ConsumerRecord<String, String> record) {
    String key = record.key();
    String value = record.value();
    long offset = record.offset();

    // Process business logic
    processOrder(value);

    // Offset is committed automatically (default setting)
}
```

### Pull vs Push

Kafka uses the **Pull model**:

| Model | Description | Advantage |
|-------|-------------|-----------|
| **Pull** | Consumer fetches when needed | Matches Consumer processing speed |
| Push | Broker pushes to Consumer | Can overload Consumer |

## Complete Flow Example

Message flow in an order system:

```mermaid
sequenceDiagram
    participant User as User
    participant Order as Order Service
    participant Kafka as Kafka
    participant Payment as Payment Service
    participant Noti as Notification Service

    User->>Order: Place order
    Order->>Kafka: Publish order event
    Order-->>User: Order received

    par Parallel Processing
        Kafka->>Payment: Deliver order event
        Payment->>Payment: Process payment
    and
        Kafka->>Noti: Deliver order event
        Noti->>Noti: Send notification
    end
```

## Message Delivery Guarantees

```mermaid
flowchart LR
    subgraph At-Most-Once["At-Most-Once"]
        A1[Send] --> A2[Commit] --> A3[Process]
    end

    subgraph At-Least-Once["At-Least-Once"]
        B1[Send] --> B2[Process] --> B3[Commit]
    end

    subgraph Exactly-Once["Exactly-Once"]
        C1[Begin Transaction] --> C2[Process] --> C3[Commit]
    end
```

| Level | Description | Use Case |
|-------|-------------|----------|
| **At-Most-Once** | At most once (may be lost) | Logs, metrics |
| **At-Least-Once** | At least once (may duplicate) | General events |
| **Exactly-Once** | Exactly once | Financial transactions |

## Next Steps

- [Consumer Group & Offset](../consumer-group-offset/) - Parallel processing and state management
