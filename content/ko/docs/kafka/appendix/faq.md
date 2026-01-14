---
lastmod: "2026-01-10"
title: 자주 묻는 질문
weight: 4
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

Kafka를 사용하면서 자주 받는 질문과 답변을 정리했습니다. 기본 개념부터 설정, 에러 처리, 성능 튜닝, 운영, Spring Kafka까지 주제별로 구성했습니다.

{{% notice style="tip" title="TL;DR" %}}
- **기본 개념**: Kafka는 메시지 큐가 아닌 분산 이벤트 스트리밍 플랫폼, Partition 단위 순서 보장
- **설정**: `acks=all`로 안정성 확보, `auto.offset.reset=earliest` 권장, 수동 커밋으로 메시지 유실 방지
- **에러 처리**: `@RetryableTopic`으로 재시도 후 Dead Letter Topic으로 이동
- **성능**: Producer는 배치/압축, Consumer는 인스턴스 수 증가 및 fetch 설정 조정
- **운영**: `replication.factor=3`, `min.insync.replicas=2` 권장
- **Spring Kafka**: `KafkaTemplate`과 `@KafkaListener`로 간편하게 구현
{{% /notice %}}

#### 기본 개념

**Q: Kafka는 메시지 큐인가요?**

Kafka는 메시지 큐가 아니라 분산 이벤트 스트리밍 플랫폼입니다. 전통적인 메시지 큐인 RabbitMQ와 비교하면 여러 차이점이 있습니다.

메시지 보관 측면에서 RabbitMQ는 Consumer가 메시지를 소비하면 즉시 삭제하지만, Kafka는 설정된 보존 기간까지 메시지를 유지합니다. 이 특성 덕분에 Kafka에서는 Offset을 이동하여 이미 처리한 메시지를 재처리할 수 있지만, 메시지 큐에서는 불가능합니다. 순서 보장도 다릅니다. 메시지 큐는 큐 전체에서 순서를 보장하지만 Kafka는 Partition 단위로만 순서를 보장합니다. 확장성 측면에서는 메시지 큐가 주로 수직 확장에 의존하는 반면, Kafka는 Partition 기반의 수평 확장이 용이합니다.

따라서 Kafka가 적합한 경우는 이벤트 소싱과 CQRS 아키텍처, 실시간 스트림 처리, 여러 시스템의 로그 집계, 메시지 재처리가 필요한 상황입니다.

**Q: Partition 수는 몇 개가 적당한가요?**

Partition 수는 처리량 요구와 Consumer 수를 고려하여 결정합니다. 대략적인 공식은 "Partition 수 = max(처리량 요구 / 단일 Partition 처리량, Consumer 수)"입니다.

일반적인 가이드라인으로 개발이나 테스트 환경의 소규모 시스템에서는 3~6개, 일반 프로덕션 환경의 중규모 시스템에서는 6~12개, 고처리량이 필요한 대규모 시스템에서는 12~50개를 권장합니다.

주의할 점이 있습니다. Partition은 늘릴 수 있지만 줄일 수 없으므로 처음에는 적게 시작하여 필요시 늘리는 것이 안전합니다. Partition이 많아지면 Leader Election 시간이 증가하고 메타데이터 관리 부하가 커집니다. Consumer 수보다 Partition이 적으면 일부 Consumer가 유휴 상태가 됩니다.

**Q: 메시지 순서는 어떻게 보장하나요?**

Kafka에서 메시지 순서는 같은 Partition 내에서만 보장됩니다. 따라서 순서가 중요한 메시지들은 같은 Key를 사용하여 동일한 Partition으로 전송해야 합니다.

```java
// 특정 키의 메시지는 항상 같은 Partition으로
kafkaTemplate.send("orders", orderId, orderEvent);
//                          ↑ Key
```

Key 선택 기준은 비즈니스 엔티티의 식별자를 사용하는 것입니다. 주문 시스템에서는 orderId를, 사용자 활동 로그에서는 userId를, IoT 데이터에서는 deviceId를 Key로 사용합니다. 이렇게 하면 같은 주문, 같은 사용자, 같은 기기에 대한 이벤트들의 순서가 보장됩니다.

**Q: Consumer Group은 왜 필요한가요?**

Consumer Group은 병렬 처리와 장애 복구를 위해 필요합니다. 같은 Group ID를 가진 Consumer들은 Topic의 Partition을 나누어 처리합니다. 예를 들어 6개 Partition을 3개 Consumer가 처리하면 각 Consumer가 2개씩 담당합니다.

장점으로는 Partition을 분배하여 처리량을 높일 수 있고, Consumer에 장애가 발생하면 해당 Consumer의 Partition이 다른 Consumer에게 자동으로 재할당되며, 독립적인 Consumer Group은 동일한 메시지를 각자 처리할 수 있어 여러 서비스가 같은 이벤트를 활용할 수 있습니다.

{{< callout type="info" title="핵심 포인트" >}}
- Kafka는 메시지 큐가 아니라 **분산 이벤트 스트리밍 플랫폼**입니다
- 메시지 순서는 **같은 Partition 내에서만** 보장됩니다
- Partition 수는 처음에 적게 시작하고 필요시 늘리세요 (줄일 수 없음)
- Consumer Group으로 **병렬 처리**와 **장애 복구**를 구현합니다
{{< /callout >}}

#### 설정 관련

**Q: acks 설정은 어떻게 해야 하나요?**

acks 설정은 데이터 중요도에 따라 선택합니다. acks=0은 전송 후 확인하지 않아 처리량이 가장 높지만 메시지 유실 가능성이 있어 로그나 메트릭 같은 유실 허용 데이터에 적합합니다. acks=1은 Leader만 확인하여 처리량과 안정성의 균형을 맞추며 일반 이벤트에 사용합니다. acks=all은 모든 ISR이 확인해야 하므로 지연은 증가하지만 가장 안전하여 금융이나 주문 같은 중요 데이터에 사용합니다.

```yaml
# application.yml
spring:
  kafka:
    producer:
      acks: all                    # 권장
      properties:
        min.insync.replicas: 2     # 최소 2개 복제본 확인
```

**Q: auto.offset.reset은 어떤 값을 사용하나요?**

auto.offset.reset은 Consumer Group이 처음 시작하거나 저장된 Offset이 없을 때 어디서부터 읽을지 결정합니다. earliest는 처음부터 읽어 데이터 유실을 방지하며 대부분의 경우 권장됩니다. latest는 최신부터 읽어 실시간 처리만 필요한 경우에 사용합니다. none은 저장된 Offset이 없으면 예외를 발생시켜 엄격한 Offset 관리가 필요한 경우에 사용합니다.

중요한 점은 이 설정이 새 Consumer Group일 때만 적용된다는 것입니다. 이미 Offset을 커밋한 기존 그룹은 저장된 Offset을 사용합니다.

```yaml
spring:
  kafka:
    consumer:
      auto-offset-reset: earliest  # 권장
```

**Q: enable.auto.commit은 켜야 하나요?**

enable.auto.commit은 false로 설정하고 수동 커밋을 사용하는 것을 권장합니다. 자동 커밋은 설정된 간격으로 Offset을 커밋하므로 메시지 처리가 완료되기 전에 커밋될 수 있습니다. 이 경우 처리 실패 시 해당 메시지가 유실됩니다.

```java
// ❌ 자동 커밋: 처리 전 커밋될 수 있음
@KafkaListener(topics = "orders")
public void listen(String message) {
    processOrder(message);  // 실패해도 offset 이미 커밋됨
}

// ✅ 수동 커밋: 처리 성공 후 커밋
@KafkaListener(topics = "orders")
public void listen(String message, Acknowledgment ack) {
    processOrder(message);
    ack.acknowledge();  // 처리 성공 후 명시적 커밋
}
```

수동 커밋을 사용하면 메시지 처리가 성공한 후에만 커밋하므로 실패 시 재처리가 가능합니다. 설정은 enable-auto-commit을 false로, ack-mode를 manual로 지정합니다.

{{< callout type="info" title="핵심 포인트" >}}
- **acks=all**: 중요 데이터에는 모든 ISR 확인으로 안정성 확보
- **auto.offset.reset=earliest**: 데이터 유실 방지를 위해 처음부터 읽기 권장
- **enable.auto.commit=false**: 수동 커밋으로 처리 완료 후에만 Offset 저장
{{< /callout >}}

#### 에러 처리

**Q: Consumer에서 예외가 발생하면 어떻게 되나요?**

기본적으로 예외가 발생하면 무한히 재시도하고, 해결되지 않으면 애플리케이션이 중단됩니다. 이를 방지하려면 명시적인 에러 처리 전략이 필요합니다.

권장하는 방식은 @RetryableTopic을 사용하여 지정된 횟수만큼 재시도하고, 모든 재시도가 실패하면 Dead Letter Topic으로 이동시키는 것입니다.

```java
@RetryableTopic(
    attempts = "3",
    backoff = @Backoff(delay = 1000, multiplier = 2),
    dltTopicSuffix = "-dlt"
)
@KafkaListener(topics = "orders")
public void listen(OrderEvent event) {
    processOrder(event);
}
```

이 설정에서는 처음 실패하면 1초 후 재시도, 두 번째 실패하면 2초 후 재시도, 세 번째도 실패하면 orders-dlt Topic으로 메시지가 이동합니다.

**Q: Dead Letter Topic(DLT)은 어떻게 처리하나요?**

DLT에 도착한 메시지는 별도의 Consumer로 모니터링하고 수동으로 처리합니다. @DltHandler 어노테이션을 사용하면 DLT 메시지를 수신할 때 호출되는 핸들러를 정의할 수 있습니다.

```java
@DltHandler
public void handleDlt(OrderEvent event,
                      @Header(KafkaHeaders.ORIGINAL_TOPIC) String topic,
                      @Header(KafkaHeaders.EXCEPTION_MESSAGE) String error) {
    log.error("DLT 수신 - Topic: {}, Error: {}", topic, error);
    alertService.sendAlert(event, error);
    // 수동 검토 후 재처리 또는 폐기
}
```

DLT 운영 전략으로는 Slack이나 이메일로 알림을 설정하고, 주기적으로 DLT 메시지를 검토하며, 문제 해결 후 원본 Topic으로 재발행하거나 해결 불가능하면 폐기 처리합니다.

**Q: 멱등성(Idempotent)은 왜 중요한가요?**

네트워크 장애로 인해 중복 메시지가 발생할 수 있기 때문입니다. Producer가 메시지를 전송하고 Broker가 저장한 후 ACK를 보냈는데 네트워크 오류로 ACK가 유실되면, Producer는 전송 실패로 판단하고 재전송합니다. 이 경우 동일한 메시지가 두 번 저장됩니다.

해결 방법으로 Producer 측에서는 enable.idempotence를 true로 설정하여 Producer 수준의 중복 전송을 방지합니다.

```yaml
spring:
  kafka:
    producer:
      properties:
        enable.idempotence: true
```

Consumer 측에서도 멱등성을 보장해야 합니다. 이미 처리한 메시지 ID를 저장하고, 중복 메시지가 들어오면 건너뛰는 방식으로 구현합니다. 또는 비즈니스 로직 자체가 멱등하도록 설계합니다. 예를 들어 "잔액에서 1000원 차감" 대신 "잔액을 50000원으로 설정"과 같이 구현합니다.

{{< callout type="info" title="핵심 포인트" >}}
- **@RetryableTopic**: 재시도 횟수와 백오프 전략 설정 가능
- **Dead Letter Topic**: 처리 실패 메시지를 별도 Topic에 보관하여 수동 처리
- **멱등성**: Producer는 `enable.idempotence=true`, Consumer는 비즈니스 로직에서 중복 처리 방지
{{< /callout >}}

#### 성능 튜닝

**Q: Producer 처리량을 높이려면?**

배치와 압축을 활성화하여 처리량을 높일 수 있습니다.

```yaml
spring:
  kafka:
    producer:
      batch-size: 32768         # 32KB 배치
      properties:
        linger.ms: 20           # 20ms 대기 후 전송
        compression.type: lz4   # 압축
        buffer.memory: 67108864 # 64MB 버퍼
```

batch.size는 기본값 16KB에서 32KB 이상으로 늘리면 한 번에 더 많은 메시지를 전송합니다. linger.ms는 기본값 0에서 5~100ms로 설정하면 배치가 찰 때까지 기다려 효율이 높아집니다. compression.type은 lz4나 snappy를 사용하면 네트워크 부하가 줄어듭니다.

주의할 점은 linger.ms가 길어지면 지연 시간도 증가하므로 처리량과 지연의 균형을 맞춰야 합니다.

**Q: Consumer 처리량을 높이려면?**

Consumer 인스턴스 수를 늘리고 fetch 설정을 조정합니다.

```yaml
spring:
  kafka:
    consumer:
      properties:
        fetch.min.bytes: 50000      # 최소 50KB
        fetch.max.wait.ms: 500      # 최대 500ms 대기
        max.poll.records: 500       # poll당 최대 500개
```

병목 지점에 따라 전략이 달라집니다. Consumer CPU가 병목이면 Consumer 인스턴스를 추가합니다. Partition 수가 부족하면 Partition을 늘립니다. 네트워크가 병목이면 fetch 설정을 조정하여 한 번에 더 많은 데이터를 가져옵니다.

**Q: Consumer Lag이 계속 증가해요**

Consumer Lag이 증가한다는 것은 메시지 유입 속도가 처리 속도보다 빠르다는 의미입니다.

먼저 현재 상태를 확인합니다.

```bash
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group order-processor-group --describe
```

원인별 해결책은 다음과 같습니다. Consumer 수가 부족하면 인스턴스를 추가합니다. 외부 API 호출이 느리면 비동기 처리로 전환하고 타임아웃을 설정합니다. DB가 병목이면 배치 처리를 적용하고 인덱스를 최적화합니다. 비즈니스 로직이 비효율적이면 프로파일링으로 병목을 찾아 최적화합니다.

{{< callout type="info" title="핵심 포인트" >}}
- **Producer 튜닝**: `batch.size` 증가, `linger.ms` 설정, `compression.type=lz4` 적용
- **Consumer 튜닝**: 인스턴스 수 증가, `fetch.min.bytes`와 `max.poll.records` 조정
- **Consumer Lag**: 처리 속도 < 유입 속도일 때 발생, 병목 지점 파악 후 해결
{{< /callout >}}

#### 운영 관련

**Q: Kafka를 모니터링하려면?**

JMX 메트릭을 수집하고 주요 지표를 모니터링합니다. 핵심 지표로 Consumer Lag은 처리 지연을 나타내며 1000을 초과하면 경고를 설정합니다. Under-replicated Partitions는 복제 지연을 나타내며 0보다 크면 경고입니다. Request Latency는 요청 지연을 나타내며 100ms를 초과하면 경고입니다. Disk Usage는 80%를 초과하면 경고를 설정합니다.

Prometheus와 Grafana를 연동하면 시각화와 알림을 설정할 수 있습니다.

```yaml
# Prometheus AlertManager
- alert: KafkaConsumerLagHigh
  expr: kafka_consumer_lag > 10000
  for: 5m
  labels:
    severity: warning
```

**Q: Broker가 다운되면 어떻게 되나요?**

Replication 설정에 따라 자동으로 복구됩니다. Leader Broker에 장애가 발생하면 ISR에 속한 Follower 중 하나가 새 Leader로 선출되고, 나머지 Follower들은 새 Leader를 따릅니다.

자동 복구가 가능하려면 replication.factor가 2 이상이어야 하고, min.insync.replicas가 2 이상이어야 하며, ISR에 살아있는 Broker가 존재해야 합니다.

프로덕션 환경에서는 replication.factor를 3으로, min.insync.replicas를 2로 설정하는 것을 권장합니다. 이렇게 하면 1대의 Broker가 다운되어도 서비스가 지속됩니다.

**Q: 메시지 보존 기간은 어떻게 설정하나요?**

Topic별로 retention 설정을 합니다.

```bash
# 7일 보존
kafka-configs.sh --bootstrap-server localhost:9092 \
  --alter --entity-type topics --entity-name orders \
  --add-config retention.ms=604800000
```

retention.ms는 시간 기반으로 7일은 604800000ms입니다. retention.bytes는 크기 기반으로 1GB는 1073741824bytes입니다.

권장 기간은 일반 이벤트는 7일, 감사 로그는 90일 이상, 디버깅용은 1~3일입니다. 디스크 용량과 비즈니스 요구사항을 고려하여 결정합니다.

{{< callout type="info" title="핵심 포인트" >}}
- **모니터링 지표**: Consumer Lag, Under-replicated Partitions, Request Latency, Disk Usage
- **고가용성**: `replication.factor=3`, `min.insync.replicas=2` 권장
- **Broker 장애**: ISR 기반 자동 Leader Election으로 복구
- **메시지 보존**: 비즈니스 요구에 따라 `retention.ms`와 `retention.bytes` 설정
{{< /callout >}}

#### Spring Kafka 관련

**Q: KafkaTemplate vs KafkaProducer 차이는?**

KafkaTemplate은 Spring이 제공하는 추상화 계층으로 더 편리합니다. Spring Boot의 자동 설정과 통합되어 별도 설정 없이 의존성 주입만 받으면 되고, 트랜잭션을 지원하며, 콜백 처리가 간소화되어 있습니다.

```java
// ✅ KafkaTemplate (Spring 추상화)
@Autowired
private KafkaTemplate<String, OrderEvent> template;

public void send(OrderEvent event) {
    template.send("orders", event.orderId(), event);
}

// ❌ KafkaProducer (저수준 API) - Spring에서는 비권장
Producer<String, OrderEvent> producer = new KafkaProducer<>(props);
producer.send(new ProducerRecord<>("orders", event));
```

KafkaProducer는 Kafka의 저수준 Java API로, Spring을 사용하지 않는 환경이나 세밀한 제어가 필요한 특수한 경우에만 사용합니다.

**Q: @KafkaListener는 몇 개의 스레드로 동작하나요?**

기본적으로 Partition 수만큼 스레드가 생성됩니다. concurrency 설정으로 최대 스레드 수를 지정할 수 있습니다.

```yaml
spring:
  kafka:
    listener:
      concurrency: 3  # 최대 3개 스레드
```

규칙은 다음과 같습니다. concurrency가 Partition 수 이하면 concurrency만큼 스레드가 생성됩니다. concurrency가 Partition 수를 초과하면 Partition 수만큼만 스레드가 생성되고 나머지는 유휴 상태가 됩니다.

예를 들어 Partition이 6개이고 concurrency가 3이면 3개 스레드가 각각 2개 Partition을 담당합니다. Partition이 3개이고 concurrency가 6이면 3개 스레드만 생성되고 각각 1개 Partition을 담당합니다.

{{< callout type="info" title="핵심 포인트" >}}
- **KafkaTemplate**: Spring 추상화 계층, 자동 설정 및 트랜잭션 지원
- **@KafkaListener**: 선언적 Consumer 구현, `concurrency`로 스레드 수 조정
- **스레드 규칙**: 실제 스레드 수 = min(concurrency, Partition 수)
{{< /callout >}}

#### 다음 단계

- [용어 사전](glossary/) - Kafka 용어 정리
- [참고 자료](references/) - 학습 자료
