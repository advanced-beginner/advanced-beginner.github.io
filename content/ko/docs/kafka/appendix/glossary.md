---
lastmod: "2026-01-10"
title: 용어 사전
weight: 1
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

Kafka 관련 주요 용어를 정리합니다. 각 용어의 상세한 설명은 개념 이해 섹션을 참고하세요. 용어들은 알파벳 순서로 정렬되어 있으며, 관련 용어들은 상호 참조로 연결되어 있습니다.

{{% notice style="tip" title="TL;DR" %}}
- **핵심 구성요소**: Topic(논리적 채널), Partition(병렬 처리 단위), Broker(서버), Producer(발행자), Consumer(수신자)
- **신뢰성**: ACK(전송 확인), ISR(동기화 복제본), Replication Factor(복제 수)
- **Consumer 관리**: Consumer Group(병렬 처리), Offset(위치), Commit(저장), Rebalancing(재분배)
- **Spring Kafka**: KafkaTemplate(Producer), @KafkaListener(Consumer)
- **메타데이터 관리**: KRaft(신규 권장), Zookeeper(레거시)
{{% /notice %}}

#### A

**ACK (Acknowledgment)**

Producer가 메시지 전송 성공을 확인받는 방식입니다. acks=0은 확인 없이 전송하여 처리량이 가장 높지만 메시지 유실 가능성이 있습니다. acks=1은 Leader만 확인하는 중간 수준의 설정입니다. acks=all은 ISR(In-Sync Replicas) 전체가 확인해야 하므로 가장 안전하지만 지연 시간이 증가합니다. 프로덕션 환경에서는 데이터 중요도에 따라 적절한 값을 선택해야 합니다. 자세한 내용은 심화 개념 문서의 acks 섹션을 참고하세요.

**Auto Offset Reset**

Consumer Group이 처음 시작하거나 저장된 Offset 정보가 없을 때 읽기 시작할 위치를 결정하는 설정입니다. earliest는 가장 오래된 메시지부터 읽어 데이터 유실을 방지하고, latest는 최신 메시지부터 읽어 실시간 처리에 적합합니다. 새 Consumer Group에만 적용되며, 기존 그룹은 저장된 Offset을 사용합니다. Consumer Group & Offset 문서에서 자세히 다룹니다.

#### B

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

#### D

**Dead Letter Topic (DLT)**

처리에 실패한 메시지를 저장하는 별도의 Topic입니다. 재시도 후에도 처리할 수 없는 메시지를 버리지 않고 보관하여 나중에 분석하거나 수동으로 처리할 수 있습니다. Spring Kafka의 @RetryableTopic과 @DltHandler를 사용하면 자동으로 DLT 처리를 구현할 수 있습니다. 기본 예제 문서에서 구현 방법을 설명합니다.

**Deserializer**

Kafka에서 읽어온 바이트 배열을 객체로 변환하는 컴포넌트입니다. StringDeserializer는 문자열로, JsonDeserializer는 JSON을 객체로 변환합니다. Producer가 사용한 Serializer와 쌍을 이루어야 합니다. 잘못된 Deserializer를 사용하면 역직렬화 에러가 발생합니다.

#### F

**Follower**

Leader의 데이터를 복제하는 Broker입니다. Leader Broker에 장애가 발생하면 ISR에 속한 Follower 중 하나가 Leader Election을 통해 새 Leader로 승격됩니다. Follower는 Leader로부터 데이터를 지속적으로 복제하여 동기화 상태를 유지합니다. Replication 문서에서 복제 메커니즘을 자세히 다룹니다.

#### G

**Group ID**

Consumer Group을 식별하는 고유 문자열입니다. Spring Boot에서는 spring.kafka.consumer.group-id 설정이나 @KafkaListener의 groupId 속성으로 지정합니다. 같은 Group ID를 가진 Consumer들은 하나의 Consumer Group으로 취급되어 Partition을 분배받습니다.

#### I

**ISR (In-Sync Replicas)**

Leader와 동기화된 Follower들의 집합입니다. Leader는 ISR에 속한 모든 복제본에 메시지가 복제된 후에야 커밋으로 처리합니다. acks=all 설정 시 메시지 안정성을 보장하는 핵심 메커니즘입니다. ISR에서 제외된 Follower는 Leader 승격 대상에서 제외됩니다. Replication 문서에서 ISR의 동작 원리를 설명합니다.

#### K

**KafkaListener**

Spring Kafka에서 Consumer를 구현하는 어노테이션입니다. 지정한 Topic의 메시지를 자동으로 수신하며, groupId로 Consumer Group을 지정합니다. 메서드 파라미터로 메시지 본문이나 ConsumerRecord를 받을 수 있습니다. 기본 예제 문서에서 다양한 사용법을 설명합니다.

**KafkaTemplate**

Spring Kafka에서 Producer를 구현하는 클래스입니다. send() 메서드로 메시지를 전송하며, Topic, Key, Value를 지정할 수 있습니다. Spring Boot가 자동으로 Bean을 생성하므로 의존성 주입만 받으면 됩니다. KafkaListener와 쌍을 이루어 메시지 송수신을 처리합니다.

**KRaft**

Zookeeper 없이 Kafka 자체적으로 메타데이터를 관리하는 모드입니다. Kafka 3.3 이상에서 프로덕션 사용이 가능하며, 새 클러스터에는 KRaft 모드를 권장합니다. 아키텍처가 단순해지고 확장성이 개선되었습니다. Replication 문서에서 KRaft와 Zookeeper의 차이를 설명합니다.

#### L

**Leader**

Partition의 읽기와 쓰기를 담당하는 주 Broker입니다. Producer와 Consumer는 Leader에만 연결하여 메시지를 주고받습니다. Follower들은 Leader로부터 데이터를 복제합니다. Leader에 장애가 발생하면 ISR 중에서 새 Leader가 선출됩니다.

**Leader Election**

Leader Broker에 장애가 발생했을 때 ISR 중에서 새 Leader를 선출하는 과정입니다. 선출 과정에서 일시적으로 해당 Partition의 읽기/쓰기가 불가능해질 수 있습니다. ISR에 Follower가 없으면 unclean.leader.election.enable 설정에 따라 동기화되지 않은 Follower가 Leader로 선출될 수 있습니다. Replication 문서에서 선출 메커니즘을 자세히 다룹니다.

**Log Compaction**

같은 Message Key의 메시지 중 최신 값만 유지하는 보관 정책입니다. 키-값 저장소처럼 최신 상태만 필요한 경우에 사용합니다. 예를 들어 사용자 프로필 Topic에서 각 사용자의 최신 프로필만 유지하려면 Log Compaction을 활성화합니다. 심화 개념 문서에서 상세히 설명합니다.

#### M

**Message Key**

메시지를 특정 Partition으로 라우팅하는 데 사용하는 값입니다. 같은 Key를 가진 메시지는 항상 같은 Partition으로 전송되어 순서가 보장됩니다. 주문 시스템에서는 orderId, 사용자 활동 로그에서는 userId를 Key로 사용하는 것이 일반적입니다. 심화 개념 문서에서 Key 사용 패턴을 다룹니다.

#### O

**Offset**

Partition 내 메시지의 순차적 위치 번호입니다. 0부터 시작하여 메시지가 추가될 때마다 증가합니다. Consumer는 Offset을 기준으로 어디까지 읽었는지 추적하고, Commit을 통해 이 정보를 저장합니다. Consumer Group & Offset 문서에서 Offset 관리를 자세히 설명합니다.

#### P

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

**Retention**

메시지 보관 정책입니다. 시간 기반(retention.ms)은 지정된 시간이 지나면 삭제하고, 용량 기반(retention.bytes)은 지정된 크기를 초과하면 오래된 것부터 삭제합니다. Log Compaction을 사용하면 Key별 최신 값만 유지합니다. 심화 개념 문서에서 보관 정책 설정을 다룹니다.

#### S

**Serializer**

객체를 바이트 배열로 변환하는 컴포넌트입니다. StringSerializer는 문자열을, JsonSerializer는 객체를 JSON 바이트로 변환합니다. Consumer의 Deserializer와 쌍을 이루어야 합니다. Producer 설정에서 key-serializer와 value-serializer를 지정합니다.

#### T

**Topic**

메시지를 분류하는 논리적 채널입니다. 관련 메시지들을 그룹화하여 관리하며, 여러 Partition으로 구성됩니다. 예를 들어 주문 관련 이벤트는 orders Topic에, 결제 관련 이벤트는 payments Topic에 발행합니다. 핵심 구성요소 문서에서 Topic의 설계 방법을 설명합니다.

#### Z

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

- [개념 이해](../concepts/) - Kafka 핵심 개념
- [Quick Start](../quick-start/) - 빠른 시작 가이드
- [마이크로서비스 예제](../examples/microservices/) - 멀티 서비스 이벤트 흐름
- [참고 자료](references/) - 공식 문서, 블로그
- [FAQ](faq/) - 자주 묻는 질문
