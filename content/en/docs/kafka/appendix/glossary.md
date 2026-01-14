---
lastmod: "2026-01-10"
title: Glossary
weight: 1
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

This glossary covers key Kafka terminology. For detailed explanations of each term, refer to the Concepts section. Terms are sorted alphabetically, with cross-references to related terms.

{{% notice style="tip" title="TL;DR" %}}
- **Core Components**: Topic (logical channel), Partition (parallel processing unit), Broker (server), Producer (publisher), Consumer (receiver)
- **Reliability**: ACK (delivery confirmation), ISR (synchronized replicas), Replication Factor (number of copies)
- **Consumer Management**: Consumer Group (parallel processing), Offset (position), Commit (save), Rebalancing (redistribution)
- **Spring Kafka**: KafkaTemplate (Producer), @KafkaListener (Consumer)
- **Metadata Management**: KRaft (recommended for new clusters), Zookeeper (legacy)
{{% /notice %}}

#### A

**ACK (Acknowledgment)**

The mechanism by which a Producer receives confirmation of successful message delivery. acks=0 sends without confirmation for maximum throughput but with potential message loss. acks=1 confirms with the Leader only, providing a middle-ground setting. acks=all requires confirmation from all ISR (In-Sync Replicas), which is safest but increases latency. For production environments, choose the appropriate value based on data importance. Refer to the Advanced Concepts document's acks section for details.

**Auto Offset Reset**

A setting that determines where to start reading when a Consumer Group first starts or has no saved Offset information. earliest reads from the oldest message to prevent data loss, while latest reads from the newest message for real-time processing. This setting only applies to new Consumer Groups; existing groups use their saved Offset. Covered in detail in the Consumer Group & Offset document.

#### B

**Broker**

A Kafka server process responsible for storing messages and delivering them to Consumers. Multiple Brokers form a cluster to provide high availability and scalability. Each Broker has a unique ID and stores Topic Partitions in a distributed manner. The Core Components document explains Broker roles and configuration.

**Bootstrap Servers**

The list of Broker addresses used for initial connection to a Kafka cluster. Specified in a format like localhost:9092, multiple addresses can be listed separated by commas. Clients connect to one of these Brokers to retrieve full cluster metadata. While you don't need to list all Broker addresses, specifying multiple is recommended for availability.

#### C

**Commit (Offset Commit)**

The operation where a Consumer notifies Kafka that it has successfully processed messages up to a specific Offset. Auto commit is performed automatically at configured intervals, while manual commit requires explicit application calls. If reprocessing is needed on failure, use manual commit. The Consumer Group & Offset document explains commit strategies in detail.

**Consumer**

A client application that reads messages from Kafka. Consumers belong to a Consumer Group and are assigned Partitions to process. In Spring Kafka, this is implemented declaratively with the @KafkaListener annotation. The Core Components document covers Consumer operation principles.

**Consumer Group**

A logical grouping of Consumers with the same purpose. Consumers in a group are assigned Topic Partitions to process in parallel. When Consumers are added or removed, Rebalancing occurs to redistribute Partitions. Different Consumer Groups independently receive the same messages. Explained in detail in the Consumer Group & Offset document.

#### D

**Dead Letter Topic (DLT)**

A separate Topic for storing messages that failed processing. Messages that cannot be processed after retries are saved here rather than discarded, allowing later analysis or manual processing. Spring Kafka's @RetryableTopic and @DltHandler enable automatic DLT handling implementation. The Basic Examples document explains implementation methods.

**Deserializer**

A component that converts byte arrays read from Kafka into objects. StringDeserializer converts to strings, JsonDeserializer converts JSON to objects. Must pair with the Serializer used by the Producer. Using the wrong Deserializer causes deserialization errors.

#### F

**Follower**

A Broker that replicates data from the Leader. When the Leader Broker fails, one of the Followers in the ISR is promoted to the new Leader through Leader Election. Followers continuously replicate data from the Leader to maintain synchronization. The Replication document covers the replication mechanism in detail.

#### G

**Group ID**

A unique string that identifies a Consumer Group. In Spring Boot, this is set via the spring.kafka.consumer.group-id configuration or the groupId attribute of @KafkaListener. Consumers with the same Group ID are treated as one Consumer Group and share Partition assignments.

#### I

**ISR (In-Sync Replicas)**

The set of Followers synchronized with the Leader. The Leader only considers a message committed after it's replicated to all replicas in the ISR. This is the core mechanism ensuring message safety with acks=all. Followers excluded from ISR are not eligible for Leader promotion. The Replication document explains ISR operation principles.

#### K

**KafkaListener**

An annotation for implementing Consumers in Spring Kafka. It automatically receives messages from specified Topics and designates the Consumer Group via groupId. Method parameters can receive message body or ConsumerRecord. The Basic Examples document explains various usage patterns.

**KafkaTemplate**

A class for implementing Producers in Spring Kafka. The send() method sends messages, specifying Topic, Key, and Value. Spring Boot automatically creates the Bean, so just inject the dependency. Pairs with KafkaListener to handle message sending and receiving.

**KRaft**

A mode where Kafka manages metadata internally without Zookeeper. Available for production use in Kafka 3.3 and above, recommended for new clusters. Architecture is simplified and scalability improved. The Replication document explains differences between KRaft and Zookeeper.

#### L

**Leader**

The primary Broker responsible for reads and writes on a Partition. Producers and Consumers connect only to the Leader to exchange messages. Followers replicate data from the Leader. When the Leader fails, a new Leader is elected from the ISR.

**Leader Election**

The process of selecting a new Leader from the ISR when a Leader Broker fails. During election, reads/writes to that Partition may be temporarily unavailable. If no Followers are in the ISR, the unclean.leader.election.enable setting determines whether an out-of-sync Follower can become Leader. The Replication document covers election mechanisms in detail.

**Log Compaction**

A retention policy that keeps only the latest value for each Message Key. Used when only the latest state is needed, similar to a key-value store. For example, activate Log Compaction on a user profile Topic to keep only the latest profile for each user. Explained in detail in the Advanced Concepts document.

#### M

**Message Key**

A value used to route messages to specific Partitions. Messages with the same Key are always sent to the same Partition, ensuring order. In order systems, orderId is typically used as the Key; for user activity logs, userId is common. The Advanced Concepts document covers Key usage patterns.

#### O

**Offset**

A sequential position number of a message within a Partition. Starts at 0 and increments with each added message. Consumers track how far they've read based on Offset and save this information through Commit. The Consumer Group & Offset document explains Offset management in detail.

#### P

**Partition**

A division unit of a Topic. The basic unit for parallel processing, where multiple Consumers handle different Partitions to increase throughput. Each Partition is replicated into Leader and Followers for high availability. Partition count can be increased but not decreased, making initial design important. The Core Components document explains Partition roles.

**Producer**

A client application that publishes messages to Kafka. In Spring Kafka, this is implemented with KafkaTemplate. Sends messages by specifying Topic, Key, and Value, receiving delivery confirmation based on ACK settings. The Core Components document covers Producer operation principles.

**Pull Model**

The method where Consumers fetch messages from Brokers. Kafka uses the Pull model, allowing Consumers to fetch messages at their own processing speed. Unlike Push model, this prevents Consumers from being overwhelmed and naturally controls backpressure. The Message Flow document explains differences between Pull and Push.

#### R

**Rebalancing**

The process of redistributing Partitions within a Consumer Group. Occurs automatically when Consumers are added/removed or when a Consumer becomes unresponsive. Message consumption may temporarily pause during Rebalancing. The Advanced Consumer Operations document covers rebalancing optimization methods.

**Replication Factor**

The number of replicas for each Partition. 3 is recommended for production environments. With Replication Factor of 3, there's 1 Leader and 2 Followers. Along with ISR, this is a key setting determining message safety. The Replication document explains replication settings in detail.

**Retention**

Message retention policy. Time-based (retention.ms) deletes after a specified time, while size-based (retention.bytes) deletes oldest messages when exceeding specified size. Log Compaction keeps only the latest value per Key. The Advanced Concepts document covers retention policy settings.

#### S

**Serializer**

A component that converts objects to byte arrays. StringSerializer converts strings, JsonSerializer converts objects to JSON bytes. Must pair with Consumer's Deserializer. Producer configuration specifies key-serializer and value-serializer.

#### T

**Topic**

A logical channel for categorizing messages. Groups related messages for management and consists of multiple Partitions. For example, order-related events go to the orders Topic, payment-related events to the payments Topic. The Core Components document explains Topic design methods.

#### Z

**Zookeeper**

An external service that managed Kafka cluster metadata. Handled Broker lists, Topic settings, Controller election, etc. From Kafka 3.3+, KRaft mode replaces Zookeeper, enabling operation without it. KRaft mode is recommended for new clusters. The Replication document explains differences between Zookeeper and KRaft.

{{< callout type="info" title="Key Points: Terms by Category" >}}
**Architecture Terms**
- **Broker**: Kafka server, **Topic**: Message channel, **Partition**: Parallel processing unit

**Message Flow Terms**
- **Producer**: Message publishing, **Consumer**: Message receiving, **Offset**: Position within Partition

**Reliability Terms**
- **ACK**: Delivery confirmation, **ISR**: Synchronized replicas, **Leader/Follower**: Primary/replica Broker

**Spring Kafka Terms**
- **KafkaTemplate**: Producer implementation, **@KafkaListener**: Consumer implementation

**Cluster Management Terms**
- **KRaft**: Recommended for new clusters, **Zookeeper**: Legacy mode
{{< /callout >}}

#### Next Steps

- [Concepts](../concepts/) - Kafka core concepts
- [Quick Start](../quick-start/) - Quick start guide
- [Microservices Example](../examples/microservices/) - Multi-service event flow
- [References](references/) - Official documentation, blogs
- [FAQ](faq/) - Frequently asked questions
