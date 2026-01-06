---
lastmod: "2026-01-06"
title: Replication
weight: 4
---

# Replication

Understanding data replication and high availability mechanisms.

## Why is Replication Needed?

Storing data on a single Broker results in data loss during failures.

```mermaid
flowchart TB
    subgraph Problem["Without Replication"]
        P1[Producer] --> B1[Broker 1]
        B1 -->|Failure!| X[Data Loss]
    end

    subgraph Solution["With Replication"]
        P2[Producer] --> B2[Broker 1\nLeader]
        B2 -->|Replicate| B3[Broker 2\nFollower]
        B2 -->|Replicate| B4[Broker 3\nFollower]
        B2 -->|Failure| B3
        B3 -->|Promoted| B3L[New Leader]
    end
```

## Leader and Follower

Each Partition consists of one **Leader** and multiple **Followers**.

```mermaid
flowchart TB
    subgraph Partition["Topic A - Partition 0"]
        L[Broker 1\nLeader]
        F1[Broker 2\nFollower]
        F2[Broker 3\nFollower]
    end

    P[Producer] -->|Write| L
    L -->|Replicate| F1
    L -->|Replicate| F2
    L -->|Read| C[Consumer]
```

### Role Distribution

| Role | Responsibility |
|------|----------------|
| **Leader** | Handles all reads/writes, replicates data to Followers |
| **Follower** | Replicates Leader data, waits for promotion on Leader failure |

> **Important:** Producers and Consumers connect **only to the Leader**.

## Replication Factor

**Replication Factor** is the number of replicas for each Partition.

```mermaid
flowchart LR
    subgraph RF1["RF=1 (No replication)"]
        RF1_B1[Broker 1\nPartition 0]
    end

    subgraph RF2["RF=2"]
        RF2_B1[Broker 1\nLeader]
        RF2_B2[Broker 2\nFollower]
    end

    subgraph RF3["RF=3 (Recommended)"]
        RF3_B1[Broker 1\nLeader]
        RF3_B2[Broker 2\nFollower]
        RF3_B3[Broker 3\nFollower]
    end
```

### Characteristics by RF

| RF | Fault Tolerance | Storage Cost | Recommended Use |
|----|-----------------|--------------|-----------------|
| 1 | None | 1x | Development/Testing |
| 2 | 1 Broker failure | 2x | General |
| 3 | 2 Broker failures | 3x | **Production recommended** |

## ISR (In-Sync Replicas)

**ISR** is the set of replicas synchronized with the Leader.

```mermaid
flowchart TB
    subgraph Healthy["Healthy State"]
        H_L[Leader\nOffset: 100]
        H_F1[Follower 1\nOffset: 100]
        H_F2[Follower 2\nOffset: 100]
        H_ISR["ISR: {Leader, F1, F2}"]
    end

    subgraph Lagging["Sync Lag"]
        L_L[Leader\nOffset: 100]
        L_F1[Follower 1\nOffset: 100]
        L_F2[Follower 2\nOffset: 80]
        L_ISR["ISR: {Leader, F1}"]
        L_NOTE[F2 removed from ISR]
    end
```

### ISR Conditions

For a Follower to be included in ISR:
- Must sync with Leader within `replica.lag.time.max.ms`
- Default: 30 seconds

```mermaid
sequenceDiagram
    participant L as Leader
    participant F1 as Follower 1
    participant F2 as Follower 2

    L->>L: Receive message (Offset 100)
    L->>F1: Replicate
    L->>F2: Replicate

    F1-->>L: Sync complete
    Note over L,F1: Remains in ISR

    Note over F2: Network delay
    Note over L,F2: Removed from ISR after 30s
```

## Leader Election

The process of electing a new Leader when the current one fails.

```mermaid
sequenceDiagram
    participant C as Controller
    participant L as Leader (Broker 1)
    participant F1 as Follower 1 (Broker 2)
    participant F2 as Follower 2 (Broker 3)

    Note over L: Leader failure!

    C->>C: Detect failure
    C->>C: Select new Leader from ISR
    C->>F1: Notify of Leader promotion
    F1->>F1: Promoted to Leader

    Note over F1: New Leader
    C->>F2: Propagate new Leader info
```

### Election Rules

1. **ISR Priority**: Elected from Followers within ISR
2. **Unclean Leader Election**: Elect out-of-sync Follower when ISR is empty (may cause data loss)

```yaml
# Topic configuration
unclean.leader.election.enable: false  # Recommended: prevent data loss
```

## min.insync.replicas

Minimum number of ISR members required for message writes.

```mermaid
flowchart TB
    subgraph Config["RF=3, min.insync.replicas=2"]
        L[Leader]
        F1[Follower 1]
        F2[Follower 2]
    end

    subgraph Scenario1["Normal: ISR=3"]
        S1[Write Success]
    end

    subgraph Scenario2["F2 Failure: ISR=2"]
        S2[Write Success]
    end

    subgraph Scenario3["F1,F2 Failure: ISR=1"]
        S3[Write Failure!]
    end
```

### Recommended Settings

| Environment | RF | min.insync.replicas |
|-------------|----|--------------------|
| Development | 1 | 1 |
| Production | 3 | 2 |

## Zookeeper vs KRaft

Comparison of Kafka's metadata management approaches:

```mermaid
flowchart TB
    subgraph Zookeeper["Zookeeper Mode (Legacy)"]
        ZK[Zookeeper Cluster]
        KB1[Kafka Broker 1]
        KB2[Kafka Broker 2]
        KB3[Kafka Broker 3]
        ZK <--> KB1
        ZK <--> KB2
        ZK <--> KB3
    end

    subgraph KRaft["KRaft Mode (New)"]
        KR1[Kafka Broker 1\nController]
        KR2[Kafka Broker 2]
        KR3[Kafka Broker 3]
        KR1 <--> KR2
        KR1 <--> KR3
    end
```

### Comparison

| Aspect | Zookeeper | KRaft |
|--------|-----------|-------|
| **External Dependency** | Required | Not needed |
| **Operational Complexity** | High | Low |
| **Partition Scalability** | Limited | Improved |
| **Recovery Time** | Slow | Fast |
| **Kafka Version** | 2.x and below | 3.3+ recommended |

> **Recommendation:** Use **KRaft mode** for new projects.

### KRaft Configuration Example

```yaml
# docker-compose.yml
environment:
  KAFKA_PROCESS_ROLES: broker,controller
  KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
```

## Summary

```mermaid
flowchart TB
    subgraph Replication["Replication Essentials"]
        R1[Leader/Follower Structure]
        R2[ISR - Synchronized Replicas]
        R3[Automatic Leader Election]
    end

    subgraph Config["Recommended Settings"]
        C1["RF=3"]
        C2["min.insync.replicas=2"]
        C3["KRaft Mode"]
    end

    Replication --> Config
```

| Concept | Role |
|---------|------|
| **Replication Factor** | Number of data copies |
| **ISR** | Set of synchronized replicas |
| **Leader Election** | Automatic failure recovery |
| **KRaft** | Simplified cluster management |

## Next Steps

- [Advanced Concepts](../advanced-concepts/) - acks, Message Key, Retention
