---
lastmod: "2026-01-08"
title: 용어 사전
weight: 1
---

# Kafka 용어 사전

Kafka 관련 주요 용어를 정리합니다. 상세 설명은 [개념 이해](../../concepts/) 섹션을 참고하세요.

## A

### ACK (Acknowledgment)
[Producer](#producer)가 메시지 전송 성공을 확인받는 방식. `acks=0`(확인 안 함), `acks=1`([Leader](#leader)만), `acks=all`([ISR](#isr-in-sync-replicas) 전체) 옵션이 있음.
→ [심화 개념](../../concepts/advanced-concepts/#acks-acknowledgment)

### Auto Offset Reset
[Consumer Group](#consumer-group)이 처음 시작하거나 [Offset](#offset) 정보가 없을 때 읽기 시작할 위치. `earliest` 또는 `latest`.
→ [Consumer Group & Offset](../../concepts/consumer-group-offset/#autooffsetreset-설정)

## B

### Broker
Kafka 서버. 메시지를 저장하고 [Consumer](#consumer)에게 전달하는 역할. 여러 Broker가 [클러스터](#bootstrap-servers)를 구성.
→ [핵심 구성요소](../../concepts/core-components/#3-broker-브로커)

### Bootstrap Servers
Kafka 클러스터에 처음 연결할 때 사용하는 [Broker](#broker) 주소 목록. `localhost:9092` 형태.

## C

### Commit (Offset Commit)
[Consumer](#consumer)가 특정 [Offset](#offset)까지 메시지를 성공적으로 처리했음을 Kafka에 알리는 것. 자동/수동 커밋 방식.
→ [Consumer Group & Offset](../../concepts/consumer-group-offset/#offset-커밋)

### Consumer
Kafka에서 메시지를 읽어가는 클라이언트 애플리케이션. [Consumer Group](#consumer-group)에 소속되어 [Partition](#partition)을 분배받음.
→ [핵심 구성요소](../../concepts/core-components/#2-consumer-소비자)

### Consumer Group
같은 목적을 가진 [Consumer](#consumer)들의 논리적 그룹. 그룹 내에서 [Partition](#partition)이 분배됨. [Rebalancing](#rebalancing)으로 동적 조정.
→ [Consumer Group & Offset](../../concepts/consumer-group-offset/)

## D

### Dead Letter Topic (DLT)
처리에 실패한 메시지를 저장하는 별도의 [Topic](#topic). 에러 분석과 재처리에 활용.
→ [기본 예제](../../examples/basic/#dead-letter-topic-dlt)

### Deserializer
바이트 배열을 객체로 변환하는 컴포넌트. `StringDeserializer`, `JsonDeserializer` 등. [Serializer](#serializer)의 반대.

## F

### Follower
[Leader](#leader)의 데이터를 복제하는 [Broker](#broker). Leader 장애 시 [Leader Election](#leader-election)으로 새 Leader로 승격될 수 있음.
→ [Replication](../../concepts/replication/#leader와-follower)

## G

### Group ID
[Consumer Group](#consumer-group)을 식별하는 고유 문자열. `spring.kafka.consumer.group-id`로 설정.

## I

### ISR (In-Sync Replicas)
[Leader](#leader)와 동기화된 [Follower](#follower) 집합. [ACK](#ack-acknowledgment)=all 설정 시 메시지 안정성 보장에 중요.
→ [Replication](../../concepts/replication/#isr-in-sync-replicas)

## K

### KafkaListener
Spring Kafka의 [Consumer](#consumer) 어노테이션. 특정 [Topic](#topic)의 메시지를 수신. [KafkaTemplate](#kafkatemplate)과 쌍.
→ [기본 예제](../../examples/basic/#기본-kafkalistener)

### KafkaTemplate
Spring Kafka의 [Producer](#producer) 클래스. 메시지 전송에 사용. [KafkaListener](#kafkalistener)와 쌍.
→ [기본 예제](../../examples/basic/#kafkatemplate-주입)

### KRaft
[Zookeeper](#zookeeper) 없이 Kafka 자체적으로 메타데이터를 관리하는 모드. Kafka 3.3+에서 권장.
→ [Replication](../../concepts/replication/#zookeeper-vs-kraft)

## L

### Leader
[Partition](#partition)의 읽기/쓰기를 담당하는 주 [Broker](#broker). [Producer](#producer)와 [Consumer](#consumer)는 Leader에만 연결.
→ [Replication](../../concepts/replication/#leader와-follower)

### Leader Election
[Leader](#leader) [Broker](#broker) 장애 시 [ISR](#isr-in-sync-replicas) 중에서 새 Leader를 선출하는 과정.
→ [Replication](../../concepts/replication/#leader-election)

### Log Compaction
같은 [Message Key](#message-key)의 메시지 중 최신 값만 유지하는 보관 정책. [Retention](#retention)의 한 방식.
→ [심화 개념](../../concepts/advanced-concepts/#log-compaction)

## M

### Message Key
메시지를 특정 [Partition](#partition)으로 라우팅하는 데 사용. 같은 Key는 같은 Partition으로 보장.
→ [심화 개념](../../concepts/advanced-concepts/#message-key)

## O

### Offset
[Partition](#partition) 내 메시지의 순차적 위치 번호. 0부터 시작하여 증가. [Consumer](#consumer)가 [Commit](#commit-offset-commit)으로 관리.
→ [Consumer Group & Offset](../../concepts/consumer-group-offset/#offset이란)

## P

### Partition
[Topic](#topic)을 분할한 단위. 병렬 처리의 기본 단위. [Leader](#leader)와 [Follower](#follower)로 복제.
→ [핵심 구성요소](../../concepts/core-components/#5-partition-파티션)

### Producer
Kafka에 메시지를 발행하는 클라이언트 애플리케이션. [KafkaTemplate](#kafkatemplate)으로 구현.
→ [핵심 구성요소](../../concepts/core-components/#1-producer-생산자)

### Pull 방식
[Consumer](#consumer)가 [Broker](#broker)에서 메시지를 가져오는 방식. Kafka는 Pull 방식 사용 (Push 방식 대비 백프레셔 제어 용이).
→ [메시지 흐름](../../concepts/message-flow/#pull-vs-push)

## R

### Rebalancing
[Consumer Group](#consumer-group) 내에서 [Partition](#partition)을 재분배하는 과정. [Consumer](#consumer) 추가/제거 시 발생.
→ [Consumer Group & Offset](../../concepts/consumer-group-offset/#리밸런싱-rebalancing)

### Replication Factor
각 [Partition](#partition)의 복제본 수. 프로덕션에서는 3 권장. [ISR](#isr-in-sync-replicas)과 연관.
→ [Replication](../../concepts/replication/#replication-factor)

### Retention
메시지 보관 정책. 시간 기반, 용량 기반, [Log Compaction](#log-compaction) 방식이 있음.
→ [심화 개념](../../concepts/advanced-concepts/#retention-보관-정책)

## S

### Serializer
객체를 바이트 배열로 변환하는 컴포넌트. `StringSerializer`, `JsonSerializer` 등. [Deserializer](#deserializer)의 반대.

## T

### Topic
메시지를 분류하는 논리적 채널. 관련 메시지들을 그룹화. 여러 [Partition](#partition)으로 구성.
→ [핵심 구성요소](../../concepts/core-components/#4-topic-토픽)

## Z

### Zookeeper
Kafka 클러스터의 메타데이터를 관리하는 외부 서비스. [KRaft](#kraft) 모드로 대체 중.
→ [Replication](../../concepts/replication/#zookeeper-vs-kraft)

---

## 다음 단계

- [개념 이해](../../concepts/) - Kafka 핵심 개념
- [Quick Start](../../quick-start/) - 빠른 시작 가이드
- [마이크로서비스 예제](../../examples/microservices/) - 멀티 서비스 이벤트 흐름
- [참고 자료](../references/) - 공식 문서, 블로그
- [FAQ](../faq/) - 자주 묻는 질문
