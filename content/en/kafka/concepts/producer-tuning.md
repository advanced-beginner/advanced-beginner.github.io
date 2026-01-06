---
lastmod: "2026-01-06"
title: Producer Tuning
weight: 7
---

# Producer Tuning

Understanding key settings for optimizing Producer performance.

## Producer Internal Structure

```mermaid
flowchart LR
    subgraph Application["Application"]
        SEND[send]
    end

    subgraph Producer["Producer Internal"]
        SER[Serializer]
        PART[Partitioner]
        BATCH[Batch\nbuffer.memory]
        SENDER[Sender Thread]
    end

    subgraph Kafka["Kafka"]
        BROKER[Broker]
    end

    SEND --> SER --> PART --> BATCH
    BATCH -->|batch.size or\nlinger.ms| SENDER
    SENDER --> BROKER
```

## Key Settings Overview

| Setting | Default | Impact |
|---------|---------|--------|
| `batch.size` | 16KB | Batch size |
| `linger.ms` | 0ms | Batch wait time |
| `buffer.memory` | 32MB | Total buffer size |
| `compression.type` | none | Compression method |
| `max.in.flight.requests.per.connection` | 5 | Concurrent requests |

## batch.size

Maximum size of a message batch to send at once.

### How It Works

```mermaid
flowchart TB
    subgraph SmallBatch["batch.size = 1KB"]
        S1[1 message]
        S2[1 message]
        S3[1 message]
        SN["3 network requests"]
    end

    subgraph LargeBatch["batch.size = 16KB"]
        L1[Multiple messages]
        LN["1 network request"]
    end
```

### Configuration Guide

```yaml
spring:
  kafka:
    producer:
      batch-size: 16384  # 16KB (default)
      # batch-size: 65536  # 64KB (throughput focus)
      # batch-size: 1024   # 1KB (latency focus)
```

| Value | Effect | Suitable For |
|-------|--------|--------------|
| **Small** | Low latency, low throughput | Real-time requirements |
| **Large** | High throughput, high latency | Batch processing |

## linger.ms

Time to wait before sending even if batch isn't full.

### How It Works

```mermaid
sequenceDiagram
    participant A as Application
    participant P as Producer
    participant K as Kafka

    Note over P: linger.ms = 0 (default)
    A->>P: Message 1
    P->>K: Send immediately

    Note over P: linger.ms = 5
    A->>P: Message 1
    Note over P: Wait 5ms
    A->>P: Message 2
    A->>P: Message 3
    P->>K: Batch send (3 messages)
```

### Configuration Guide

```yaml
spring:
  kafka:
    producer:
      properties:
        linger.ms: 5  # Wait 5ms
```

| Value | Effect | Suitable For |
|-------|--------|--------------|
| **0 (default)** | Send immediately | Minimize latency |
| **5-10ms** | Moderate batching | General recommendation |
| **100ms+** | Maximum batching | High-volume batch processing |

### batch.size + linger.ms Combination

```mermaid
flowchart TB
    MSG[Message arrives]
    CHECK{batch.size\nreached?}
    WAIT{linger.ms\nexceeded?}
    SEND[Send batch]

    MSG --> CHECK
    CHECK -->|Yes| SEND
    CHECK -->|No| WAIT
    WAIT -->|Yes| SEND
    WAIT -->|No| MSG
```

Sends when either condition is met.

## buffer.memory

Total buffer memory available to the Producer.

### How It Works

```mermaid
flowchart TB
    subgraph Buffer["buffer.memory = 32MB"]
        B1[Partition 0\nBatch]
        B2[Partition 1\nBatch]
        B3[Partition 2\nBatch]
        FREE[Free space]
    end

    SEND[send] -->|Add to buffer| Buffer
    Buffer -->|Sender Thread| KAFKA[Kafka]
```

### When Buffer Is Full

```mermaid
sequenceDiagram
    participant A as Application
    participant P as Producer
    participant K as Kafka

    A->>P: send()
    Note over P: Buffer full!
    Note over P: Wait max.block.ms

    alt Space available
        K-->>P: ACK - buffer freed
        P->>K: Send new message
    else Timeout
        P-->>A: TimeoutException
    end
```

### Configuration Guide

```yaml
spring:
  kafka:
    producer:
      buffer-memory: 33554432  # 32MB (default)
      properties:
        max.block.ms: 60000  # Max buffer wait time
```

**Recommendation:** `buffer.memory` > `batch.size` × Partition count

## compression.type

Sets message compression method.

### Compression Methods Comparison

```mermaid
flowchart LR
    subgraph NoComp["No compression"]
        NC1["100KB"] --> NC2["100KB"]
    end

    subgraph GZIP["gzip"]
        G1["100KB"] --> G2["~30KB"]
    end

    subgraph Snappy["snappy"]
        S1["100KB"] --> S2["~50KB"]
    end

    subgraph LZ4["lz4"]
        L1["100KB"] --> L2["~45KB"]
    end

    subgraph ZSTD["zstd"]
        Z1["100KB"] --> Z2["~25KB"]
    end
```

| Method | Ratio | CPU Usage | Speed | Recommendation |
|--------|-------|-----------|-------|----------------|
| **none** | 0% | Lowest | Fastest | Small messages |
| **gzip** | Highest | Highest | Slowest | Storage priority |
| **snappy** | Medium | Low | Fast | **General recommendation** |
| **lz4** | Medium | Low | Fastest | High performance |
| **zstd** | High | Medium | Fast | Kafka 2.1+ |

### Configuration

```yaml
spring:
  kafka:
    producer:
      compression-type: snappy  # General recommendation
      # compression-type: lz4   # High performance
      # compression-type: zstd  # High compression
```

### Benefits of Compression

```
Original data: 100MB
├── Network transfer: 100MB
├── Broker storage: 100MB
└── Replication: 200MB (RF=3)

snappy compressed: 50MB
├── Network transfer: 50MB (-50%)
├── Broker storage: 50MB (-50%)
└── Replication: 100MB (-50%)
```

## max.in.flight.requests.per.connection

Maximum requests waiting for ACK on a single connection.

### Ordering Problem

```mermaid
sequenceDiagram
    participant P as Producer
    participant K as Kafka

    Note over P,K: max.in.flight = 5
    P->>K: Request 1
    P->>K: Request 2
    P->>K: Request 3

    K--xP: Request 1 failed
    K-->>P: Request 2 success
    K-->>P: Request 3 success

    P->>K: Retry Request 1
    K-->>P: Request 1 success

    Note over K: Order: 2, 3, 1 (scrambled!)
```

### Solution

```yaml
# Method 1: Idempotent Producer (recommended)
spring:
  kafka:
    producer:
      properties:
        enable.idempotence: true  # Kafka 3.0+ default
        max.in.flight.requests.per.connection: 5  # Safe up to 5

# Method 2: Limit in-flight to 1 (performance impact)
spring:
  kafka:
    producer:
      properties:
        max.in.flight.requests.per.connection: 1
```

Idempotent Producer guarantees order through sequence numbers.

## Retry Settings

### Key Settings

```yaml
spring:
  kafka:
    producer:
      retries: 2147483647  # Integer.MAX_VALUE (default)
      properties:
        delivery.timeout.ms: 120000  # Total timeout
        retry.backoff.ms: 100  # Retry interval
        request.timeout.ms: 30000  # Single request timeout
```

### Timeout Relationship

```mermaid
flowchart LR
    subgraph DeliveryTimeout["delivery.timeout.ms (120s)"]
        R1[Request 1\n30s]
        W1[Wait\n100ms]
        R2[Retry\n30s]
        W2[Wait\n100ms]
        R3[Retry\n30s]
    end
```

**Rule:** `delivery.timeout.ms` >= `request.timeout.ms` + `linger.ms`

## Profile-based Configuration Examples

### Throughput Optimization

```yaml
spring:
  kafka:
    producer:
      acks: all
      batch-size: 65536  # 64KB
      compression-type: lz4
      properties:
        linger.ms: 50
        buffer.memory: 67108864  # 64MB
```

### Latency Optimization

```yaml
spring:
  kafka:
    producer:
      acks: 1  # or all
      batch-size: 1024  # 1KB
      compression-type: none
      properties:
        linger.ms: 0
```

### Balanced Configuration

```yaml
spring:
  kafka:
    producer:
      acks: all
      batch-size: 16384  # 16KB
      compression-type: snappy
      properties:
        linger.ms: 5
        enable.idempotence: true
```

## Tuning Guide

```mermaid
flowchart TB
    Q1{Throughput vs\nLatency?}
    Q2{Message size?}
    Q3{Order important?}

    Q1 -->|Throughput| TH[batch.size ↑\nlinger.ms ↑\ncompression: lz4]
    Q1 -->|Latency| LT[batch.size ↓\nlinger.ms=0]

    Q2 -->|Large messages| BIG[compression: zstd]
    Q2 -->|Small messages| SMALL[compression: none]

    Q3 -->|Yes| ORD[enable.idempotence=true]
```

## Summary

| Setting | Throughput ↑ | Latency ↓ |
|---------|--------------|-----------|
| `batch.size` | ↑ Larger | ↓ Smaller |
| `linger.ms` | ↑ Larger | = 0 |
| `compression.type` | lz4/snappy | none |
| `buffer.memory` | ↑ Larger | No impact |

## Next Steps

- [Consumer Tuning](../consumer-tuning/) - Consumer performance optimization
