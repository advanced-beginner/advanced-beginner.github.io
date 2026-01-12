---
lastmod: "2026-01-06"
title: Glossary
weight: 1
---

# Kafka Glossary

A compilation of key Kafka terminology.

## A

### ACK (Acknowledgment)
How a Producer receives confirmation of successful message delivery. Options are `acks=0`, `acks=1`, `acks=all`.
→ [Advanced Concepts](../../concepts/advanced-concepts/#acks-acknowledgment)

### Auto Offset Reset
Where to start reading when a Consumer Group first starts or has no offset information. `earliest` or `latest`.
→ [Consumer Group & Offset](../../concepts/consumer-group-offset/#autooffsetreset-configuration)

## B

### Broker
A Kafka server. Responsible for storing messages and delivering them to Consumers.
→ [Core Components](../../concepts/core-components/#3-broker)

### Bootstrap Servers
The list of Broker addresses used for initial connection to the Kafka cluster. Format: `localhost:9092`.

## C

### Commit (Offset Commit)
When a Consumer notifies Kafka that it has successfully processed messages up to a certain Offset.
→ [Consumer Group & Offset](../../concepts/consumer-group-offset/#offset-commit)

### Consumer
A client application that reads messages from Kafka.
→ [Core Components](../../concepts/core-components/#2-consumer)

### Consumer Group
A logical group of Consumers with the same purpose. Partitions are distributed within the group.
→ [Consumer Group & Offset](../../concepts/consumer-group-offset/)

## D

### Dead Letter Topic (DLT)
A separate Topic that stores messages that failed processing.
→ [Basic Examples](../../examples/basic/#dead-letter-topic-dlt)

### Deserializer
A component that converts byte arrays to objects. Examples: `StringDeserializer`, `JsonDeserializer`.

## F

### Follower
A Broker that replicates data from the Leader. Can be promoted to Leader if the Leader fails.
→ [Replication](../../concepts/replication/#leader-and-follower)

## G

### Group ID
A unique string that identifies a Consumer Group. Set with `spring.kafka.consumer.group-id`.

## I

### ISR (In-Sync Replicas)
The set of Followers synchronized with the Leader. Important for message durability.
→ [Replication](../../concepts/replication/#isr-in-sync-replicas)

## K

### KafkaListener
Spring Kafka's Consumer annotation. Receives messages from specific Topics.
→ [Basic Examples](../../examples/basic/#basic-kafkalistener)

### KafkaTemplate
Spring Kafka's Producer class. Used for sending messages.
→ [Basic Examples](../../examples/basic/#kafkatemplate-injection)

### KRaft
A mode where Kafka manages metadata itself without Zookeeper. Recommended for Kafka 3.3+.
→ [Replication](../../concepts/replication/#zookeeper-vs-kraft)

## L

### Leader
The primary Broker responsible for reads/writes for a Partition. Producers and Consumers connect only to the Leader.
→ [Replication](../../concepts/replication/#leader-and-follower)

### Leader Election
The process of electing a new Leader from ISR when the Leader Broker fails.
→ [Replication](../../concepts/replication/#leader-election)

### Log Compaction
A retention policy that keeps only the latest value for each Key.
→ [Advanced Concepts](../../concepts/advanced-concepts/#log-compaction)

## M

### Message Key
Used to route messages to specific Partitions. Same Key goes to the same Partition.
→ [Advanced Concepts](../../concepts/advanced-concepts/#message-key)

## O

### Offset
The sequential position number of a message within a Partition. Starts from 0 and increases.
→ [Consumer Group & Offset](../../concepts/consumer-group-offset/#what-is-offset)

## P

### Partition
A subdivision of a Topic. The basic unit of parallelism.
→ [Core Components](../../concepts/core-components/#5-partition)

### Producer
A client application that publishes messages to Kafka.
→ [Core Components](../../concepts/core-components/#1-producer)

### Pull Model
The method where Consumers fetch messages from Brokers. Kafka uses the Pull model.
→ [Message Flow](../../concepts/message-flow/#pull-vs-push)

## R

### Rebalancing
The process of redistributing Partitions within a Consumer Group. Occurs when Consumers are added/removed.
→ [Consumer Group & Offset](../../concepts/consumer-group-offset/#rebalancing)

### Replication Factor
The number of replicas for each Partition. 3 is recommended for production.
→ [Replication](../../concepts/replication/#replication-factor)

### Retention
Message retention policy. Can be time-based, size-based, or compaction.
→ [Advanced Concepts](../../concepts/advanced-concepts/#retention-policy)

## S

### Serializer
A component that converts objects to byte arrays. Examples: `StringSerializer`, `JsonSerializer`.

## T

### Topic
A logical channel for categorizing messages. Groups related messages together.
→ [Core Components](../../concepts/core-components/#4-topic)

## Z

### Zookeeper
An external service that manages Kafka cluster metadata. Being replaced by KRaft mode.
→ [Replication](../../concepts/replication/#zookeeper-vs-kraft)
