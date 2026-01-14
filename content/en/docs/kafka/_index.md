---
title: Apache Kafka
bookCollapseSection: true
description: Apache Kafka practical guide for distributed messaging - Producer/Consumer, transactions, replication, fault handling, and performance tuning
weight: 1
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

## What is Kafka?

Apache Kafka is a **distributed event streaming platform**. Simply put, it's a broker that safely and quickly delivers messages between systems.

### Why is Kafka Needed?

What problems arise when Service A directly calls Service B?

| Problems with Direct Calls | After Kafka Adoption |
|---------------------------|----------------------|
| A fails when B is down | A just sends messages and continues its work |
| A slows down when B is slow | Asynchronous processing allows A to respond immediately |
| Load explodes when C, D also call B | Kafka acts as a buffer, each consumes at its own pace |
| Strong coupling between A-B | Loose coupling with Producer/Consumer separation |

Kafka solves these problems while providing **no message loss**, **order guarantees**, and **high-volume processing**.

### When Should You Use Kafka?

**Suitable cases:**
- When asynchronous communication between services is needed
- When building event-driven architecture
- When high-volume log/event collection is needed
- When building real-time data pipelines

**May be overkill:**
- When simple request-response is sufficient
- When message volume is low and complexity is minimal
- When there's no capacity for operational infrastructure

## What This Guide Covers

### [Quick Start](quick-start/)
Send and receive Kafka messages in 5 minutes. See working code before concepts.

### [Concepts](concepts/)
Not just "use it this way", but explaining **why it works this way**.

| Topic | What You'll Learn |
|-------|-------------------|
| [Core Components](concepts/core-components/) | Roles and relationships of Broker, Topic, Partition, Producer, Consumer |
| [Message Flow](concepts/message-flow/) | The complete journey of a message from Producer to Consumer |
| [Consumer Group & Offset](concepts/consumer-group/) | Core of parallel processing and message position management |
| [Replication & Fault Tolerance](concepts/replication/) | How to survive failures without data loss |
| [Transactions](concepts/transactions/) | How to guarantee exactly-once processing |
| [Error Handling](concepts/error-handling/) | Real-world error scenarios and resolution patterns |
| [Performance Tuning](concepts/producer-tuning/) | Producer/Consumer optimization strategies |
| [Monitoring](concepts/monitoring/) | Understanding Kafka status in production |

### [Hands-on Examples](examples/)
Executable example code based on Spring Boot.

- [Environment Setup](examples/setup/) - Docker Kafka cluster configuration
- [Basic Examples](examples/basic/) - Producer/Consumer implementation
- [Order System](examples/order-system/) - Real business scenario example

### [Appendix](appendix/)
- [Glossary](appendix/glossary/) - Quick reference for Kafka terms
- [References](appendix/references/) - Official docs and additional learning resources

## Prerequisites

- **Required**: Java basics, Spring Boot experience
- **Helpful**: REST API development experience, basic distributed systems concepts

## Suggested Learning Path

```
If you're new:      Quick Start -> Core Components -> Message Flow -> Basic Examples
For production:     Consumer Group -> Error Handling -> Transactions -> Order System
For operations:     Replication & Fault Tolerance -> Performance Tuning -> Monitoring
```

Each document can be read independently, but if you're new, we recommend the order above.
