---
lastmod: "2026-01-15"
title: Consumer 심화 운영
description: "Consumer 고급 운영 전략과 장애 대응 방법을 설명합니다."
weight: 4
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

{{< callout type="info" title="TL;DR" >}}
- session.timeout.ms는 장애 감지, max.poll.interval.ms는 처리 시간 제한 설정
- Cooperative Sticky Assignor로 리밸런싱 영향 최소화 (Kafka 2.4+)
- Static Group Membership으로 재시작 시 리밸런싱 방지 가능
- Consumer Lag이 가장 중요한 모니터링 지표, 추세 관찰이 핵심
- kafka-consumer-groups.sh로 Offset 수동 리셋 가능 (Consumer 중지 필요)
{{< /callout >}}

**대상 독자**: Kafka Consumer를 프로덕션에서 운영하는 개발자 및 운영자

**선수 지식**: [Consumer Group & Offset](consumer-group/)의 기본 개념, [Replication](replication/)의 ISR과 Leader 개념

**소요 시간**: 약 25-30분

---

리밸런싱 최적화, Consumer Lag 모니터링, 트러블슈팅을 다룹니다. 이 문서는 Kafka 3.6.x 기준으로 작성되었으며, Spring Boot 3.2.x와 Spring Kafka 3.1.x, Micrometer 1.12.x, Java 17 환경에서 코드 예제가 검증되었습니다.

이 문서를 읽기 전에 [Consumer Group & Offset](consumer-group/)에서 기본 개념을, [Replication](replication/)에서 ISR과 Leader 개념을 먼저 이해하고 있어야 합니다.

#### Consumer Group 핵심 설정

리밸런싱과 장애 감지에 영향을 주는 핵심 설정들이 있습니다.

**Session과 Heartbeat 설정**

```yaml
# application.yml - Kafka 3.6 기본값 명시
spring:
  kafka:
    consumer:
      properties:
        session.timeout.ms: 45000      # 기본값: 45초 (Kafka 3.0+)
        heartbeat.interval.ms: 3000    # 기본값: 3초
        max.poll.interval.ms: 300000   # 기본값: 5분
```

`session.timeout.ms`는 Broker가 Consumer를 장애로 판단하는 시간입니다. 이 시간 동안 Heartbeat가 도착하지 않으면 Consumer가 죽은 것으로 간주하고 리밸런싱을 시작합니다. 네트워크 지연의 3배 정도로 설정하는 것이 권장됩니다.

`heartbeat.interval.ms`는 Heartbeat 전송 주기입니다. Consumer가 주기적으로 Broker에게 "살아있다"는 신호를 보내는 간격입니다. session.timeout의 1/15 이하로 설정하는 것이 권장됩니다.

`max.poll.interval.ms`는 poll() 호출 사이의 최대 허용 간격입니다. 이 시간 내에 다음 poll()을 호출하지 않으면 Consumer가 비정상으로 간주되어 그룹에서 제외됩니다. 메시지 처리 시간의 2배 정도로 설정하는 것이 권장됩니다.

{{< callout type="info" title="핵심 포인트" >}}
- session.timeout.ms: Heartbeat 기반 장애 감지 (기본 45초)
- max.poll.interval.ms: 처리 시간 제한 (기본 5분), 초과 시 리밸런싱
- heartbeat.interval.ms는 session.timeout.ms의 1/15 이하로 설정 권장
{{< /callout >}}

**max.poll.interval.ms 문제와 해결**

외부 API 호출이나 복잡한 처리로 인해 poll() 간격이 길어지면 리밸런싱이 발생할 수 있습니다.

```java
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final BlockingQueue<String> retryQueue = new LinkedBlockingQueue<>();

    public OrderConsumer(PaymentService paymentService, OrderRepository orderRepository) {
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
    }

    // 문제 상황: 동기 처리로 인한 타임아웃
    @KafkaListener(topics = "orders", groupId = "order-service-bad")
    public void consumeBad(String order) {
        // 외부 결제 API 호출 - 최대 3분 소요 가능
        PaymentResult result = paymentService.process(order);  // 위험!
        // max.poll.interval.ms(5분) 초과 시 리밸런싱 발생
    }

    // 해결책: 비동기 처리 + 수동 커밋
    @KafkaListener(topics = "orders", groupId = "order-service-async")
    public void consumeAsync(String order, Acknowledgment ack) {
        // DB에 먼저 저장 (빠름)
        orderRepository.saveForProcessing(order);
        ack.acknowledge();  // 즉시 커밋

        // 별도 스레드에서 처리 (poll() 블로킹 없음)
        CompletableFuture.runAsync(() -> paymentService.process(order))
            .exceptionally(ex -> {
                log.error("결제 처리 실패. 재처리 큐로 이동: {}", order, ex);
                retryQueue.add(order);
                return null;
            });
    }
}
```

비동기 처리 외에 `max.poll.records`를 축소하는 방법도 있습니다. 한 번에 가져오는 레코드 수를 줄이면 처리 시간이 단축됩니다. Spring Kafka에서는 `spring.kafka.consumer.max-poll-records: 10`과 같이 설정합니다(기본값 500).

{{< callout type="info" title="핵심 포인트" >}}
- 동기 외부 호출은 max.poll.interval.ms 초과로 리밸런싱 유발
- 해결책: 비동기 처리 + 수동 커밋, 또는 max.poll.records 축소
- DB에 먼저 저장 후 즉시 커밋, 별도 스레드에서 처리하는 패턴 권장
{{< /callout >}}

#### 리밸런싱 심층 분석

리밸런싱을 <strong>회사 좌석 재배치</strong>에 비유하면 이해하기 쉽습니다:

| 좌석 재배치 비유 | Kafka 리밸런싱 |
|-----------------|---------------|
| 전원 짐 싸서 대기 → 자리 배정 → 이동 | Eager Protocol (Stop-the-World) |
| 이동할 사람만 짐 싸기 → 자리 배정 | Cooperative Protocol (영향 최소화) |
| 고정석 부여 (이름표 붙은 자리) | Static Group Membership |

리밸런싱 중에는 모든 Consumer가 일시 정지됩니다. Eager Protocol 기준으로 리밸런싱이 시작되면 모든 Consumer가 Partition을 해제(Stop-the-World)하고, Group Coordinator가 새로운 할당을 계산한 후, 각 Consumer에게 새 Partition을 할당합니다. 10개 Consumer 환경에서는 약 1초, 100개 Consumer 환경에서는 약 10초가 소요됩니다. 대규모 클러스터에서는 분 단위로 소요될 수도 있습니다.

**Cooperative Sticky Assignor (Kafka 2.4+, 권장)**

기존 Eager Protocol과 달리 필요한 Partition만 재할당하는 방식입니다.

```yaml
spring:
  kafka:
    consumer:
      properties:
        partition.assignment.strategy: org.apache.kafka.clients.consumer.CooperativeStickyAssignor
```

Eager Protocol의 문제는 모든 Consumer가 동시에 Partition을 놓으면 순간적으로 처리량이 0이 된다는 점입니다. LinkedIn에서는 이 "Stop-the-World" 시간이 대규모 클러스터에서 분 단위로 발생하여 SLA 위반 원인이 되었습니다.

Cooperative Protocol(KIP-429)은 2단계로 리밸런싱을 수행합니다. 첫 번째 단계에서는 필요한 Partition만 해제하고, 영향받지 않는 Consumer는 계속 처리합니다. 두 번째 단계에서 해제된 Partition을 새 Consumer에게 할당합니다. 핵심 원리는 "먼저 놓고, 나중에 받는다"가 아니라 "필요한 것만 놓고, 바로 받는다"입니다.

**Static Group Membership (Kafka 2.3+)**

Consumer 재시작 시 리밸런싱을 방지하는 기능으로, Kubernetes Rolling Update에 유용합니다.

```yaml
spring:
  kafka:
    consumer:
      properties:
        # 고정 ID 부여 - 재시작해도 같은 Consumer로 인식
        group.instance.id: ${HOSTNAME:consumer-1}
        session.timeout.ms: 300000  # 5분 (재시작 시간 확보)
```

고정 ID를 부여하면 Consumer가 재시작되어도 같은 Consumer로 인식되어 불필요한 리밸런싱을 방지합니다. session.timeout.ms를 재시작에 필요한 시간보다 길게 설정해야 합니다.

{{< callout type="info" title="핵심 포인트" >}}
- Eager Protocol: 전체 Stop-the-World, 대규모 클러스터에서 분 단위 중단 발생
- Cooperative Sticky Assignor: 필요한 것만 재할당, 영향 최소화 (Kafka 2.4+)
- Static Group Membership: 재시작 시 리밸런싱 방지, K8s Rolling Update에 유용
{{< /callout >}}

**리밸런싱 모니터링 구현**

```java
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

public class RebalanceMonitor implements ConsumerRebalanceListener {

    private static final Logger log = LoggerFactory.getLogger(RebalanceMonitor.class);
    private final MeterRegistry meterRegistry;
    private Instant rebalanceStart;

    public RebalanceMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        rebalanceStart = Instant.now();
        log.warn("파티션 해제됨: {}. 처리 중인 메시지 커밋 필요!", partitions);

        meterRegistry.counter("kafka.rebalance.revoked",
            "partitions", String.valueOf(partitions.size())).increment();
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        Duration duration = Duration.between(rebalanceStart, Instant.now());

        log.info("파티션 할당됨: {}. 리밸런싱 소요시간: {}ms",
                 partitions, duration.toMillis());

        Timer.builder("kafka.rebalance.duration")
            .description("Consumer rebalance duration")
            .register(meterRegistry)
            .record(duration);
    }
}
```

Spring Kafka에서 ConsumerRebalanceListener를 등록하려면 ContainerFactory 설정에서 `getContainerProperties().setConsumerRebalanceListener()`를 사용합니다.

#### Consumer Lag 모니터링

Consumer Lag는 Producer가 보낸 메시지 수에서 Consumer가 처리한 메시지 수를 뺀 값입니다. 가장 중요한 모니터링 지표입니다.

**Lag 확인 명령어**

```bash
# 전체 Consumer Group 목록
kafka-consumer-groups.sh --list --bootstrap-server localhost:9092

# 특정 Consumer Group 상세 정보
kafka-consumer-groups.sh --describe --group order-service \
    --bootstrap-server localhost:9092

# 출력 예시:
# GROUP           TOPIC    PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG      CONSUMER-ID
# order-service   orders   0          15234           15300           66       consumer-1-xxx
# order-service   orders   1          14892           15100           208      consumer-2-xxx
# order-service   orders   2          15001           15001           0        consumer-3-xxx
```

Lag 0~100은 정상 상태입니다. 100~1,000은 주의가 필요하며 처리 속도를 확인해야 합니다. 1,000~10,000은 경고 수준으로 Consumer 증설을 검토해야 합니다. 10,000 이상은 위험 상태로 즉시 대응이 필요합니다.

LAG 수치보다 LAG 증가 추세가 더 중요합니다. LAG 1000이 유지되면 문제없지만, LAG 100이 계속 증가하면 조치가 필요합니다.

{{< callout type="info" title="핵심 포인트" >}}
- Consumer Lag = 처리해야 할 메시지 수, 가장 중요한 모니터링 지표
- Lag 0~100: 정상, 100~1000: 주의, 1000~10000: 경고, 10000+: 위험
- 절대값보다 증가 추세가 더 중요, 지속적 증가 시 즉시 조치 필요
{{< /callout >}}

**Prometheus + Grafana 모니터링**

kafka-exporter를 사용하여 Consumer Lag를 Prometheus로 수집할 수 있습니다.

```yaml
# docker-compose.yml
services:
  kafka-exporter:
    image: danielqsj/kafka-exporter:v1.7.0
    command:
      - --kafka.server=kafka:9092
      - --web.listen-address=:9308
    ports:
      - "9308:9308"
    depends_on:
      - kafka
```

핵심 PromQL 쿼리는 다음과 같습니다. `kafka_consumergroup_lag{consumergroup="order-service"}`는 현재 Lag를 조회합니다. `rate(kafka_consumergroup_lag{consumergroup="order-service"}[5m])`는 5분간 Lag 증가율을 보여주며, 0보다 크면 적체 중입니다. `count(kafka_consumergroup_lag > 10000)`는 Lag이 10000 이상인 파티션 수를 나타냅니다.

알림 규칙 설정 예시입니다. Lag이 10,000을 5분간 초과하면 Warning 알림을 발생시키고, Lag 증가율이 100/초 이상으로 10분간 유지되면 Critical 알림을 발생시킵니다. Consumer Group에 활성 멤버가 없으면 1분 후 Critical 알림을 발생시킵니다.

**Offset 수동 리셋**

기존 Offset을 무시하고 특정 위치부터 다시 읽어야 할 때 사용합니다. Consumer가 중지된 상태에서만 가능합니다.

```bash
# 가장 처음부터 다시 읽기
kafka-consumer-groups.sh --reset-offsets \
    --group order-service \
    --topic orders \
    --to-earliest \
    --execute \
    --bootstrap-server localhost:9092

# 특정 시간 이후부터 읽기 (장애 발생 시점)
kafka-consumer-groups.sh --reset-offsets \
    --group order-service \
    --topic orders \
    --to-datetime 2024-01-15T10:00:00.000 \
    --execute \
    --bootstrap-server localhost:9092

# 특정 Offset으로 이동
kafka-consumer-groups.sh --reset-offsets \
    --group order-service \
    --topic orders:0:1500 \
    --execute \
    --bootstrap-server localhost:9092

# 현재 위치에서 N개 건너뛰기
kafka-consumer-groups.sh --reset-offsets \
    --group order-service \
    --topic orders \
    --shift-by 1000 \
    --execute \
    --bootstrap-server localhost:9092
```

#### 트러블슈팅 체크리스트

**Lag 급증 시**

```bash
# 1. Consumer가 살아있는지 확인
kafka-consumer-groups.sh --describe --group order-service \
    --bootstrap-server localhost:9092 --members --verbose

# CONSUMER-ID가 비어있으면 Consumer 장애!

# 2. Producer 급증 확인
kafka-get-offsets.sh --topic orders \
    --bootstrap-server localhost:9092

# 3. 파티션 불균형 확인
# 특정 파티션의 LAG만 높으면 Hot Partition 문제
# → Message Key 분포 확인 필요
```

특정 파티션만 LAG가 증가하면 Hot Partition(Key 편중) 문제입니다. Key 분산이나 Partition 추가로 해결합니다. 전체 LAG가 급증하면 Consumer 처리 속도가 부족한 것이므로 인스턴스를 증설합니다. LAG는 0인데 메시지가 누락되면 자동 커밋과 처리 실패가 원인일 수 있으므로 수동 커밋으로 변경합니다. 잦은 리밸런싱이 발생하면 session.timeout이 너무 짧은 것이므로 timeout을 증가시키거나 Static Membership을 적용합니다.

{{< callout type="info" title="핵심 포인트" >}}
- 특정 Partition Lag 증가: Hot Partition 문제, Key 분산 필요
- 전체 Lag 급증: Consumer 처리 속도 부족, 인스턴스 증설
- Lag 0인데 누락: 자동 커밋 + 처리 실패, 수동 커밋으로 변경
{{< /callout >}}

#### 프로덕션 배포 체크리스트

Consumer 애플리케이션을 프로덕션에 배포하기 전에 다음 사항을 확인해야 합니다.

<strong>설정 점검</strong>으로는 group.id 명명 규칙 준수({서비스명}-{용도}), auto.offset.reset 의도대로 설정(보통 earliest), enable.auto.commit=false(수동 커밋 권장), max.poll.interval.ms가 최대 처리 시간보다 큰지, session.timeout.ms/heartbeat.interval.ms 비율(15:1 권장), partition.assignment.strategy가 CooperativeStickyAssignor인지 확인합니다.

<strong>모니터링 준비</strong>로는 Consumer Lag 메트릭 수집 설정, Lag 임계값 알림 설정(warning: 10,000 / critical: 50,000), 리밸런싱 발생 알림 설정, Consumer 인스턴스 수 모니터링을 확인합니다.

<strong>장애 대응 준비</strong>로는 DLQ(Dead Letter Queue) 구성, Offset 리셋 절차 문서화, 롤백 계획 수립, 담당자 연락처 및 에스컬레이션 경로를 준비합니다.

<strong>성능 검증</strong>으로는 예상 TPS의 2배 부하 테스트 완료, Consumer 인스턴스 수가 Partition 수 이하인지 확인, 메모리 사용량 모니터링(GC 로그 활성화)을 확인합니다.

#### FAQ

**Q: Lag이 계속 0인데 정상인가요?**

정상입니다. Producer 속도가 Consumer 속도 이하이면 Lag은 0에 가깝습니다.

**Q: 리밸런싱이 자주 발생하면 어떻게 하나요?**

session.timeout.ms를 증가시키거나, CooperativeStickyAssignor를 사용하거나, Static Group Membership을 적용합니다.

**Q: Consumer가 너무 느린데 어떻게 최적화하나요?**

병렬 처리(concurrency 설정), 배치 처리(batch listener), 외부 호출 비동기화를 적용합니다.

#### 참고 자료

- [Confluent: Incremental Cooperative Rebalancing](https://www.confluent.io/blog/cooperative-rebalancing-in-kafka-streams-consumer-ksqldb/)
- [KIP-429: Consumer Group Protocol Redesign](https://cwiki.apache.org/confluence/display/KAFKA/KIP-429)
- [Kafka Consumer Configurations](https://kafka.apache.org/documentation/#consumerconfigs)

#### 다음 단계

- [Producer 튜닝](producer-tuning/) - Producer 성능 최적화
- [트랜잭션](transactions/) - Exactly-Once 처리
