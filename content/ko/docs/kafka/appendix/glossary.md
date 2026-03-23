---
lastmod: "2026-01-15"
title: 용어 사전
description: "Kafka 핵심 용어를 정리합니다."
weight: 1
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

Kafka 관련 주요 용어를 정리합니다. 각 용어의 상세한 설명은 개념 이해 섹션을 참고하세요. 용어들은 알파벳 순서로 정렬되어 있으며, 관련 용어들은 상호 참조로 연결되어 있습니다.

{{< callout type="tip" title="TL;DR" >}}
- **핵심 구성요소**: Topic(논리적 채널), Partition(병렬 처리 단위), Broker(서버), Producer(발행자), Consumer(수신자)
- **신뢰성**: ACK(전송 확인), ISR(동기화 복제본), Replication Factor(복제 수)
- **Consumer 관리**: Consumer Group(병렬 처리), Offset(위치), Commit(저장), Rebalancing(재분배)
- **Spring Kafka**: KafkaTemplate(Producer), @KafkaListener(Consumer)
- **메타데이터 관리**: KRaft(신규 권장), Zookeeper(레거시)
{{< /callout >}}

#### A

**ACK (Acknowledgment)**

Producer가 메시지 전송 성공을 확인받는 방식입니다. acks=0은 확인 없이 전송하여 처리량이 가장 높지만 메시지 유실 가능성이 있습니다. acks=1은 Leader만 확인하는 중간 수준의 설정입니다. acks=all은 ISR(In-Sync Replicas) 전체가 확인해야 하므로 가장 안전하지만 지연 시간이 증가합니다. 프로덕션 환경에서는 데이터 중요도에 따라 적절한 값을 선택해야 합니다. 자세한 내용은 심화 개념 문서의 acks 섹션을 참고하세요.

**At-Least-Once**

메시지가 최소 한 번 전달되는 보장 수준입니다. 메시지 유실은 방지되지만 중복 전달이 발생할 수 있습니다. acks=all과 재시도 설정을 활성화하고, Consumer에서 처리 완료 후 Offset을 커밋하는 방식으로 구현합니다. 가장 일반적으로 사용되는 전달 보장 수준이며, 애플리케이션에서 멱등성 처리를 구현하면 중복 문제를 해결할 수 있습니다. 트랜잭션과 Exactly-Once 문서에서 자세히 다룹니다.

**At-Most-Once**

메시지가 최대 한 번 전달되는 보장 수준입니다. 메시지 유실이 발생할 수 있지만 중복은 없습니다. acks=0 또는 Consumer에서 처리 전에 Offset을 커밋하는 방식으로 구현합니다. 로그 수집이나 메트릭처럼 일부 유실이 허용되는 경우에 사용합니다. 트랜잭션과 Exactly-Once 문서에서 자세히 다룹니다.

**Auto Offset Reset**

Consumer Group이 처음 시작하거나 저장된 Offset 정보가 없을 때 읽기 시작할 위치를 결정하는 설정입니다. earliest는 가장 오래된 메시지부터 읽어 데이터 유실을 방지하고, latest는 최신 메시지부터 읽어 실시간 처리에 적합합니다. 새 Consumer Group에만 적용되며, 기존 그룹은 저장된 Offset을 사용합니다. Consumer Group & Offset 문서에서 자세히 다룹니다.

**Avro**

Apache Avro는 데이터 직렬화 형식으로, 스키마가 데이터와 함께 정의됩니다. JSON보다 컴팩트하고 빠르며, 스키마 진화(evolution)를 위한 규칙이 잘 정의되어 있습니다. Schema Registry와 함께 사용하면 Producer와 Consumer 간 데이터 호환성을 자동으로 검증할 수 있습니다. 생태계 문서에서 자세히 다룹니다.

#### B

**Backoff**

재시도 사이의 대기 시간을 의미합니다. FixedBackOff는 고정된 간격으로 재시도하고, ExponentialBackOff는 1초, 2초, 4초처럼 지수적으로 대기 시간을 증가시킵니다. 네트워크 오류나 일시적 장애 시 서버에 부하를 주지 않으면서 복구를 기다리는 데 사용됩니다. 에러 처리 심화 문서에서 자세히 다룹니다.

**Batch**

여러 메시지를 묶어서 한 번에 전송하는 단위입니다. batch.size와 linger.ms 설정으로 배치 크기와 대기 시간을 조절합니다. 배치 전송은 네트워크 오버헤드를 줄여 처리량을 높이지만, 지연 시간이 증가할 수 있습니다. Producer 튜닝 문서에서 배치 최적화를 자세히 다룹니다.

**Broker**

Kafka 서버 프로세스입니다. 메시지를 저장하고 Consumer에게 전달하는 역할을 담당합니다. 여러 Broker가 클러스터를 구성하여 고가용성과 확장성을 제공합니다. 각 Broker는 고유한 ID를 가지며, Topic의 Partition들을 분산 저장합니다. 핵심 구성요소 문서에서 Broker의 역할과 구성을 설명합니다.

**Bootstrap Servers**

Kafka 클러스터에 처음 연결할 때 사용하는 Broker 주소 목록입니다. localhost:9092와 같은 형태로 지정하며, 여러 개를 쉼표로 구분하여 나열할 수 있습니다. 클라이언트는 이 목록의 Broker 중 하나에 연결하여 전체 클러스터 메타데이터를 가져옵니다. 모든 Broker 주소를 나열할 필요는 없지만, 가용성을 위해 여러 개를 지정하는 것이 좋습니다.

#### C

**Commit (Offset Commit)**

Consumer가 특정 Offset까지 메시지를 성공적으로 처리했음을 Kafka에 알리는 작업입니다. 자동 커밋은 설정된 간격으로 자동 수행되고, 수동 커밋은 애플리케이션이 명시적으로 호출합니다. 처리 실패 시 재처리가 필요하다면 수동 커밋을 사용해야 합니다. Consumer Group & Offset 문서에서 커밋 전략을 자세히 설명합니다.

**Consumer**

Kafka에서 메시지를 읽어가는 클라이언트 애플리케이션입니다. Consumer Group에 소속되어 Partition을 분배받아 처리합니다. Spring Kafka에서는 @KafkaListener 어노테이션으로 선언적으로 구현합니다. 핵심 구성요소 문서에서 Consumer의 동작 원리를 다룹니다.

**Consumer Group**

같은 목적을 가진 Consumer들의 논리적 그룹입니다. 그룹 내 Consumer들은 Topic의 Partition을 분배받아 병렬로 처리합니다. Consumer가 추가되거나 제거되면 Rebalancing이 발생하여 Partition이 재분배됩니다. 서로 다른 Consumer Group은 동일한 메시지를 각각 독립적으로 수신합니다. Consumer Group & Offset 문서에서 상세히 설명합니다.

**Consumer Lag**

Producer가 발행한 메시지 중 Consumer가 아직 처리하지 않은 메시지 수입니다. Log End Offset에서 Consumer Offset을 뺀 값으로 계산됩니다. Lag이 0에 가까우면 실시간 처리 중이고, Lag이 지속적으로 증가하면 Consumer 처리 속도가 부족한 것입니다. kafka-consumer-groups.sh 명령어로 확인할 수 있으며, Kafka 모니터링에서 가장 중요한 지표입니다. 모니터링 기초 문서에서 자세히 다룹니다.

**Controller**

Kafka 클러스터의 메타데이터를 관리하는 특별한 Broker입니다. Leader 선출, Partition 재할당, Topic 생성/삭제 등의 관리 작업을 담당합니다. 클러스터에는 정확히 하나의 Controller만 존재해야 합니다. KRaft 모드에서는 일부 Broker가 Controller 역할을 겸합니다. Replication 문서에서 자세히 다룹니다.

**Cooperative Sticky Assignor**

Kafka 2.4부터 지원하는 점진적 리밸런싱 전략입니다. 기존 Eager Protocol과 달리 필요한 Partition만 재할당하여 리밸런싱 중에도 대부분의 Consumer가 계속 메시지를 처리할 수 있습니다. partition.assignment.strategy 설정으로 활성화합니다. Consumer 심화 운영 문서에서 자세히 다룹니다.

#### D

**Dead Letter Topic (DLT)**

처리에 실패한 메시지를 저장하는 별도의 Topic입니다. 재시도 후에도 처리할 수 없는 메시지를 버리지 않고 보관하여 나중에 분석하거나 수동으로 처리할 수 있습니다. Spring Kafka의 @RetryableTopic과 @DltHandler를 사용하면 자동으로 DLT 처리를 구현할 수 있습니다. 기본 예제 문서에서 구현 방법을 설명합니다.

**Debezium**

CDC(Change Data Capture)를 위한 오픈소스 플랫폼입니다. MySQL, PostgreSQL, MongoDB, Oracle 등 다양한 데이터베이스의 변경 사항을 실시간으로 감지하여 Kafka로 스트리밍합니다. 데이터베이스의 바이너리 로그를 읽어 INSERT, UPDATE, DELETE 이벤트를 캡처합니다. Kafka Connect와 함께 사용됩니다. 생태계 문서에서 자세히 다룹니다.

**Deserializer**

Kafka에서 읽어온 바이트 배열을 객체로 변환하는 컴포넌트입니다. StringDeserializer는 문자열로, JsonDeserializer는 JSON을 객체로 변환합니다. Producer가 사용한 Serializer와 쌍을 이루어야 합니다. 잘못된 Deserializer를 사용하면 역직렬화 에러가 발생합니다.

#### E

**Exactly-Once Semantics (EOS)**

메시지가 정확히 한 번만 처리되는 보장 수준입니다. 유실도 중복도 없습니다. Idempotent Producer, Transactional API, read_committed 격리 수준을 함께 사용하여 구현합니다. 금융 트랜잭션이나 포인트 처리처럼 정확성이 중요한 경우에 사용하지만, 오버헤드가 있어 꼭 필요한 경우에만 사용합니다. 트랜잭션과 Exactly-Once 문서에서 자세히 다룹니다.

#### F

**Follower**

Leader의 데이터를 복제하는 Broker입니다. Leader Broker에 장애가 발생하면 ISR에 속한 Follower 중 하나가 Leader Election을 통해 새 Leader로 승격됩니다. Follower는 Leader로부터 데이터를 지속적으로 복제하여 동기화 상태를 유지합니다. Replication 문서에서 복제 메커니즘을 자세히 다룹니다.

#### G

**Group ID**

Consumer Group을 식별하는 고유 문자열입니다. Spring Boot에서는 spring.kafka.consumer.group-id 설정이나 @KafkaListener의 groupId 속성으로 지정합니다. 같은 Group ID를 가진 Consumer들은 하나의 Consumer Group으로 취급되어 Partition을 분배받습니다.

#### H

**Heartbeat**

Consumer가 Broker(Group Coordinator)에게 자신이 살아있음을 알리는 신호입니다. heartbeat.interval.ms 간격으로 전송되며, session.timeout.ms 시간 동안 Heartbeat가 도착하지 않으면 Consumer가 죽은 것으로 간주되어 리밸런싱이 시작됩니다. Consumer 튜닝 문서에서 자세히 다룹니다.

**High Watermark (HW)**

모든 ISR(In-Sync Replicas)에 복제가 완료된 Offset입니다. Consumer는 High Watermark까지만 메시지를 읽을 수 있습니다. 이를 통해 Leader 장애 시에도 데이터 일관성이 보장됩니다. LEO(Log End Offset)와 함께 복제 상태를 나타내는 핵심 지표입니다. Replication 문서에서 자세히 다룹니다.

**Hot Partition**

특정 Message Key에 메시지가 집중되어 해당 Partition만 과부하되는 현상입니다. 다른 Partition은 유휴 상태가 되어 병렬 처리의 이점을 활용할 수 없습니다. Key를 더 세분화하거나 Partition 수를 늘려서 해결합니다. 메시지 흐름 문서에서 자세히 다룹니다.

#### I

**Idempotent Producer**

네트워크 오류로 인한 재전송 시 중복 메시지를 방지하는 Producer 설정입니다. 각 메시지에 Producer ID(PID)와 시퀀스 번호를 부여하여 Broker가 중복을 감지합니다. Kafka 3.0부터 enable.idempotence=true가 기본값입니다. 단일 Partition 내에서만 중복 방지를 보장합니다. 심화 개념 문서에서 자세히 다룹니다.

**ISR (In-Sync Replicas)**

Leader와 동기화된 Follower들의 집합입니다. Leader는 ISR에 속한 모든 복제본에 메시지가 복제된 후에야 커밋으로 처리합니다. acks=all 설정 시 메시지 안정성을 보장하는 핵심 메커니즘입니다. ISR에서 제외된 Follower는 Leader 승격 대상에서 제외됩니다. Replication 문서에서 ISR의 동작 원리를 설명합니다.

#### J

**JMX (Java Management Extensions)**

Java 애플리케이션의 메트릭을 노출하고 관리하는 표준 기술입니다. Kafka Broker, Producer, Consumer의 성능 지표가 JMX를 통해 노출됩니다. Prometheus와 연동하려면 JMX Exporter를 사용하여 메트릭을 변환합니다. 모니터링 기초 문서에서 자세히 다룹니다.

#### K

**Kafka Connect**

외부 시스템과 Kafka 간에 데이터를 안정적으로 스트리밍하기 위한 프레임워크입니다. Source Connector는 외부 시스템에서 Kafka로, Sink Connector는 Kafka에서 외부 시스템으로 데이터를 전송합니다. 코딩 없이 JSON 설정만으로 데이터 파이프라인을 구축할 수 있습니다. 생태계 문서에서 자세히 다룹니다.

**KafkaListener**

Spring Kafka에서 Consumer를 구현하는 어노테이션입니다. 지정한 Topic의 메시지를 자동으로 수신하며, groupId로 Consumer Group을 지정합니다. 메서드 파라미터로 메시지 본문이나 ConsumerRecord를 받을 수 있습니다. 기본 예제 문서에서 다양한 사용법을 설명합니다.

**Kafka Streams**

Kafka 토픽의 데이터를 실시간으로 처리하는 Java 라이브러리입니다. 별도의 클러스터 없이 일반 Java 애플리케이션에 라이브러리로 추가하여 사용합니다. KStream(레코드 스트림)과 KTable(변경 로그 스트림)이라는 두 가지 추상화를 제공합니다. 생태계 문서에서 자세히 다룹니다.

**KStream**

Kafka Streams에서 레코드 스트림을 표현하는 추상화입니다. 각 레코드는 독립적인 이벤트로 취급됩니다. 클릭 로그, 주문 이벤트 등 시간에 따라 발생하는 이벤트를 처리할 때 적합합니다. 같은 키의 레코드도 각각 별개로 처리됩니다. 생태계 문서에서 자세히 다룹니다.

**KTable**

Kafka Streams에서 변경 로그(changelog) 스트림을 표현하는 추상화입니다. 각 키에 대해 최신 값만 유지됩니다. 사용자 프로필, 상품 재고 등 현재 상태를 나타내는 데이터에 적합합니다. 같은 키로 새 레코드가 들어오면 이전 값은 덮어씌워집니다. 생태계 문서에서 자세히 다룹니다.

**KafkaTemplate**

Spring Kafka에서 Producer를 구현하는 클래스입니다. send() 메서드로 메시지를 전송하며, Topic, Key, Value를 지정할 수 있습니다. Spring Boot가 자동으로 Bean을 생성하므로 의존성 주입만 받으면 됩니다. KafkaListener와 쌍을 이루어 메시지 송수신을 처리합니다.

**KRaft**

Zookeeper 없이 Kafka 자체적으로 메타데이터를 관리하는 모드입니다. Kafka 3.3 이상에서 프로덕션 사용이 가능하며, 새 클러스터에는 KRaft 모드를 권장합니다. 아키텍처가 단순해지고 확장성이 개선되었습니다. Replication 문서에서 KRaft와 Zookeeper의 차이를 설명합니다.

#### L

**Leader**

Partition의 읽기와 쓰기를 담당하는 주 Broker입니다. Producer와 Consumer는 Leader에만 연결하여 메시지를 주고받습니다. Follower들은 Leader로부터 데이터를 복제합니다. Leader에 장애가 발생하면 ISR 중에서 새 Leader가 선출됩니다.

**Leader Election**

Leader Broker에 장애가 발생했을 때 ISR 중에서 새 Leader를 선출하는 과정입니다. 선출 과정에서 일시적으로 해당 Partition의 읽기/쓰기가 불가능해질 수 있습니다. ISR에 Follower가 없으면 unclean.leader.election.enable 설정에 따라 동기화되지 않은 Follower가 Leader로 선출될 수 있습니다. Replication 문서에서 선출 메커니즘을 자세히 다룹니다.

**LEO (Log End Offset)**

Partition에서 가장 마지막 메시지의 다음 Offset입니다. 새 메시지가 추가될 위치를 나타냅니다. Leader의 LEO가 100이면 0부터 99까지의 메시지가 저장되어 있다는 의미입니다. High Watermark와 함께 복제 상태를 판단하는 핵심 지표입니다. Replication 문서에서 자세히 다룹니다.

**linger.ms**

Producer가 배치를 전송하기 전에 추가 메시지를 기다리는 시간입니다. 기본값 0은 메시지가 도착하면 즉시 전송합니다. 값을 5ms로 설정하면 5ms 동안 추가 메시지를 모아 배치로 전송하여 처리량이 증가합니다. batch.size와 OR 조건으로 동작합니다. Producer 튜닝 문서에서 자세히 다룹니다.

**Log Compaction**

같은 Message Key의 메시지 중 최신 값만 유지하는 보관 정책입니다. 키-값 저장소처럼 최신 상태만 필요한 경우에 사용합니다. 예를 들어 사용자 프로필 Topic에서 각 사용자의 최신 프로필만 유지하려면 Log Compaction을 활성화합니다. 심화 개념 문서에서 상세히 설명합니다.

#### M

**max.poll.interval.ms**

두 poll() 호출 사이의 최대 허용 시간입니다. 이 시간을 초과하면 Consumer가 비정상으로 간주되어 Consumer Group에서 제외되고 리밸런싱이 시작됩니다. 기본값은 5분(300000ms)입니다. 처리 시간이 긴 작업은 이 값을 늘리거나 비동기 처리로 전환해야 합니다. Consumer 심화 운영 문서에서 자세히 다룹니다.

**max.poll.records**

한 번의 poll() 호출로 가져오는 최대 레코드 수입니다. 기본값은 500입니다. 값이 작으면 빠르게 처리하고 자주 poll()을 호출하며, 값이 크면 배치 처리 효율이 높아지지만 처리 시간이 길어집니다. max.poll.interval.ms와 함께 조절해야 합니다. Consumer 튜닝 문서에서 자세히 다룹니다.

**min.insync.replicas**

ACK를 반환하기 위해 동기화되어 있어야 하는 최소 복제본 수입니다. acks=all과 함께 사용하여 데이터 안전성을 보장합니다. RF=3, min.insync.replicas=2 설정은 1대의 Broker 장애를 허용하면서도 데이터 유실을 방지합니다. ISR이 이 값보다 적으면 Producer 쓰기가 실패합니다. Replication 문서에서 자세히 다룹니다.

**Message Key**

메시지를 특정 Partition으로 라우팅하는 데 사용하는 값입니다. 같은 Key를 가진 메시지는 항상 같은 Partition으로 전송되어 순서가 보장됩니다. 주문 시스템에서는 orderId, 사용자 활동 로그에서는 userId를 Key로 사용하는 것이 일반적입니다. 심화 개념 문서에서 Key 사용 패턴을 다룹니다.

#### O

**Offset**

Partition 내 메시지의 순차적 위치 번호입니다. 0부터 시작하여 메시지가 추가될 때마다 증가합니다. Consumer는 Offset을 기준으로 어디까지 읽었는지 추적하고, Commit을 통해 이 정보를 저장합니다. Consumer Group & Offset 문서에서 Offset 관리를 자세히 설명합니다.

**Outbox 패턴**

DB와 Kafka를 원자적으로 처리하기 위한 패턴입니다. DB 트랜잭션 내에서 데이터와 함께 Outbox 테이블에 이벤트를 저장하고, 별도 프로세스가 Outbox에서 Kafka로 전송합니다. Kafka 트랜잭션만으로는 외부 시스템과의 원자성을 보장할 수 없을 때 사용합니다. 트랜잭션과 Exactly-Once 문서에서 자세히 다룹니다.

#### P

**Page Cache**

운영체제가 디스크 데이터를 메모리에 캐싱하는 메커니즘입니다. Kafka Broker는 Page Cache를 적극 활용하여 자주 접근하는 데이터를 메모리에서 바로 제공합니다. 이를 통해 디스크 저장 방식임에도 높은 처리량을 달성합니다. 핵심 구성요소 문서에서 자세히 다룹니다.

**Partitioner**

Producer에서 메시지가 어떤 Partition으로 전송될지 결정하는 컴포넌트입니다. Message Key가 있으면 Key의 해시값을 기반으로 Partition을 선택하고, Key가 없으면 Sticky Partitioner(Kafka 2.4+) 방식으로 분배합니다. 커스텀 Partitioner를 구현할 수도 있습니다. 메시지 흐름 문서에서 자세히 다룹니다.

**Partition**

Topic을 분할한 단위입니다. 병렬 처리의 기본 단위로, 여러 Consumer가 각각 다른 Partition을 담당하여 처리량을 높입니다. 각 Partition은 Leader와 Follower로 복제되어 고가용성을 제공합니다. Partition 수는 늘릴 수 있지만 줄일 수 없으므로 초기 설계가 중요합니다. 핵심 구성요소 문서에서 Partition의 역할을 설명합니다.

**Producer**

Kafka에 메시지를 발행하는 클라이언트 애플리케이션입니다. Spring Kafka에서는 KafkaTemplate으로 구현합니다. Topic, Key, Value를 지정하여 메시지를 전송하며, ACK 설정에 따라 전송 확인을 받습니다. 핵심 구성요소 문서에서 Producer의 동작 원리를 다룹니다.

**Pull 방식**

Consumer가 Broker에서 메시지를 가져오는 방식입니다. Kafka는 Pull 방식을 사용하여 Consumer가 자신의 처리 속도에 맞게 메시지를 가져갈 수 있습니다. Push 방식과 달리 Consumer가 과부하에 빠지는 것을 방지하고 백프레셔를 자연스럽게 제어합니다. 메시지 흐름 문서에서 Pull과 Push의 차이를 설명합니다.

#### R

**Rebalancing**

Consumer Group 내에서 Partition을 재분배하는 과정입니다. Consumer가 추가되거나 제거되면, 또는 Consumer가 응답하지 않으면 자동으로 발생합니다. Rebalancing 동안 일시적으로 메시지 소비가 중단될 수 있습니다. Consumer 심화 운영 문서에서 리밸런싱 최적화 방법을 다룹니다.

**Replication Factor**

각 Partition의 복제본 수입니다. 프로덕션 환경에서는 3을 권장합니다. Replication Factor가 3이면 Leader 1개와 Follower 2개가 존재합니다. ISR과 함께 메시지 안정성을 결정하는 핵심 설정입니다. Replication 문서에서 복제 설정을 자세히 설명합니다.

**@RetryableTopic**

Spring Kafka 2.7부터 제공하는 선언적 재시도 및 DLT 처리 어노테이션입니다. 자동으로 재시도 Topic(topic-retry-0, retry-1, ...)을 생성하고, 최대 재시도 후에는 DLT(topic-dlt)로 메시지를 전송합니다. @DltHandler와 함께 사용하여 DLT 메시지를 처리합니다. 에러 처리 심화 문서에서 자세히 다룹니다.

**Retention**

메시지 보관 정책입니다. 시간 기반(retention.ms)은 지정된 시간이 지나면 삭제하고, 용량 기반(retention.bytes)은 지정된 크기를 초과하면 오래된 것부터 삭제합니다. Log Compaction을 사용하면 Key별 최신 값만 유지합니다. 심화 개념 문서에서 보관 정책 설정을 다룹니다.

#### S

**SASL (Simple Authentication and Security Layer)**

Kafka 클라이언트의 신원을 확인하는 인증 프레임워크입니다. PLAIN, SCRAM-SHA-256, SCRAM-SHA-512, GSSAPI(Kerberos), OAUTHBEARER 등 여러 메커니즘을 지원합니다. 프로덕션에서는 SCRAM-SHA-256을 권장하며, SSL과 함께 SASL_SSL 프로토콜로 사용합니다. 보안 문서에서 자세히 다룹니다.

**Schema Registry**

Kafka 메시지의 스키마를 중앙에서 관리하는 서비스입니다. Producer가 메시지를 보낼 때 스키마를 등록하고, Consumer가 읽을 때 스키마를 조회합니다. 스키마 호환성을 자동으로 검증하여 런타임 오류를 방지합니다. Avro, Protobuf, JSON Schema를 지원합니다. 생태계 문서에서 자세히 다룹니다.

**Segment**

Partition을 구성하는 물리적 파일 단위입니다. 각 Segment는 로그 파일(.log)과 인덱스 파일(.index, .timeindex)로 구성됩니다. log.segment.bytes 크기에 도달하거나 log.roll.hours 시간이 지나면 새 Segment가 생성됩니다. 오래된 Segment는 retention 설정에 따라 자동 삭제됩니다. 메시지 흐름 문서에서 자세히 다룹니다.

**session.timeout.ms**

Broker(Group Coordinator)가 Consumer를 장애로 판단하는 시간입니다. 이 시간 동안 Heartbeat가 도착하지 않으면 Consumer가 죽은 것으로 간주하고 리밸런싱을 시작합니다. Kafka 3.0+ 기본값은 45초입니다. heartbeat.interval.ms의 3배 이상으로 설정하는 것이 권장됩니다. Consumer 심화 운영 문서에서 자세히 다룹니다.

**Sink Connector**

Kafka Connect에서 Kafka 토픽의 메시지를 외부 시스템으로 전송하는 커넥터입니다. Elasticsearch Sink Connector, S3 Sink Connector, JDBC Sink Connector 등이 있습니다. Source Connector와 반대 방향으로 데이터를 이동시킵니다. 생태계 문서에서 자세히 다룹니다.

**Source Connector**

Kafka Connect에서 외부 시스템의 데이터를 Kafka 토픽으로 수집하는 커넥터입니다. Debezium MySQL Connector, JDBC Source Connector, File Source Connector 등이 있습니다. CDC(Change Data Capture)를 구현할 때 주로 사용됩니다. 생태계 문서에서 자세히 다룹니다.

**SSL/TLS**

네트워크 통신을 암호화하여 데이터가 전송 중에 노출되는 것을 방지하는 보안 프로토콜입니다. Kafka에서는 Broker-Client, Broker-Broker 간 통신을 암호화하는 데 사용됩니다. ssl.client.auth=required 설정으로 양방향 TLS(클라이언트 인증서 검증)를 활성화할 수 있습니다. 보안 문서에서 자세히 다룹니다.

**Static Group Membership**

Kafka 2.3부터 지원하는 Consumer 재시작 시 리밸런싱을 방지하는 기능입니다. group.instance.id로 고정 ID를 부여하면 Consumer가 재시작되어도 같은 Consumer로 인식됩니다. Kubernetes Rolling Update 시 유용합니다. session.timeout.ms를 재시작 시간보다 길게 설정해야 합니다. Consumer 심화 운영 문서에서 자세히 다룹니다.

**Sticky Partitioner**

Kafka 2.4부터 기본으로 사용되는 Partition 분배 방식입니다. Key가 없는 메시지를 전송할 때 일정 기간 같은 Partition에 메시지를 모아 배치 효율을 높입니다. 기존 라운드 로빈 방식보다 처리량이 향상됩니다. 심화 개념 문서에서 자세히 다룹니다.

**Serializer**

객체를 바이트 배열로 변환하는 컴포넌트입니다. StringSerializer는 문자열을, JsonSerializer는 객체를 JSON 바이트로 변환합니다. Consumer의 Deserializer와 쌍을 이루어야 합니다. Producer 설정에서 key-serializer와 value-serializer를 지정합니다.

#### T

**Tombstone**

Log Compaction 환경에서 Key를 삭제하기 위해 전송하는 value가 null인 메시지입니다. Tombstone은 delete.retention.ms(기본 24시간) 동안 유지된 후 다음 Compaction 때 완전히 삭제됩니다. Consumer에서는 value가 null인 메시지를 받으면 해당 Key의 데이터를 삭제 처리해야 합니다. 심화 개념 문서에서 자세히 다룹니다.

**Topic**

메시지를 분류하는 논리적 채널입니다. 관련 메시지들을 그룹화하여 관리하며, 여러 Partition으로 구성됩니다. 예를 들어 주문 관련 이벤트는 orders Topic에, 결제 관련 이벤트는 payments Topic에 발행합니다. 핵심 구성요소 문서에서 Topic의 설계 방법을 설명합니다.

**Transaction**

여러 Partition에 걸친 원자적 쓰기를 보장하는 Kafka 기능입니다. 모든 메시지가 성공하거나 모두 실패합니다(All or Nothing). transaction-id-prefix 설정으로 활성화하며, Transaction Coordinator가 트랜잭션 상태를 관리합니다. Consume-Transform-Produce 패턴에서 Exactly-Once를 구현하는 데 사용됩니다. 트랜잭션과 Exactly-Once 문서에서 자세히 다룹니다.

**Transaction Coordinator**

Kafka 트랜잭션의 상태를 관리하는 Broker 컴포넌트입니다. initTransactions(), beginTransaction(), commitTransaction(), abortTransaction() 요청을 처리하고, __transaction_state 내부 토픽에 트랜잭션 상태를 기록합니다. Producer ID(PID) 할당과 트랜잭션 마커 기록을 담당합니다. 트랜잭션과 Exactly-Once 문서에서 자세히 다룹니다.

#### U

**Under-replicated Partition**

복제가 부족한 상태의 Partition입니다. ISR에 있어야 할 복제본 중 일부가 동기화되지 않은 상태입니다. kafka-topics.sh --describe --under-replicated-partitions 명령으로 확인할 수 있으며, 이 값이 0이 아니면 Broker 장애나 네트워크 문제를 의심해야 합니다. Replication 문서에서 자세히 다룹니다.

#### Z

**Zero-Copy**

데이터를 커널 영역에서 네트워크 버퍼로 직접 복사하는 기술입니다. 일반적인 데이터 전송은 커널 → 사용자 공간 → 커널 경로를 거치지만, Zero-Copy는 사용자 공간을 거치지 않아 성능이 향상됩니다. Kafka Broker가 Consumer에게 데이터를 전송할 때 사용합니다. 메시지 흐름 문서에서 자세히 다룹니다.

**Zookeeper**

Kafka 클러스터의 메타데이터를 관리하는 외부 서비스입니다. Broker 목록, Topic 설정, Controller 선출 등을 담당했습니다. Kafka 3.3 이상에서는 KRaft 모드로 대체되어 Zookeeper 없이 운영이 가능합니다. 신규 클러스터는 KRaft 모드를 권장합니다. Replication 문서에서 Zookeeper와 KRaft의 차이를 설명합니다.

{{< callout type="info" title="핵심 포인트: 용어 카테고리별 정리" >}}
**아키텍처 용어**
- **Broker**: Kafka 서버, **Topic**: 메시지 채널, **Partition**: 병렬 처리 단위

**메시지 흐름 용어**
- **Producer**: 메시지 발행, **Consumer**: 메시지 수신, **Offset**: Partition 내 위치

**신뢰성 용어**
- **ACK**: 전송 확인, **ISR**: 동기화된 복제본, **Leader/Follower**: 주/복제 Broker

**Spring Kafka 용어**
- **KafkaTemplate**: Producer 구현, **@KafkaListener**: Consumer 구현

**클러스터 관리 용어**
- **KRaft**: 신규 권장, **Zookeeper**: 레거시 모드
{{< /callout >}}

#### 다음 단계

- [개념 이해]({{< relref "/docs/kafka/concepts" >}}) - Kafka 핵심 개념
- [Quick Start]({{< relref "/docs/kafka/quick-start" >}}) - 빠른 시작 가이드
- [마이크로서비스 예제]({{< relref "/docs/kafka/examples/microservices" >}}) - 멀티 서비스 이벤트 흐름
- [참고 자료]({{< relref "/docs/kafka/appendix/references" >}}) - 공식 문서, 블로그
- [FAQ]({{< relref "/docs/kafka/appendix/faq" >}}) - 자주 묻는 질문
