---
lastmod: "2026-01-08"
title: Consumer 심화 운영
weight: 4
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

# Consumer 심화 운영

리밸런싱 최적화, Consumer Lag 모니터링, 트러블슈팅을 다룹니다.

> **Kafka 버전**: 이 문서는 **Kafka 3.6.x** 기준으로 작성되었습니다.

| 검증 환경 | 버전 |
|----------|------|
| Kafka | 3.6.1 (KRaft) |
| Spring Boot | 3.2.x |
| Spring Kafka | 3.1.x |
| Java | 17 |
| Micrometer | 1.12.x |

> 이 문서의 코드 예제는 위 환경에서 컴파일 및 동작이 확인되었습니다.

## 선행 지식

- [Consumer Group & Offset](../consumer-group/) - 기본 개념 필수
- [Replication](../replication/) - ISR, Leader 개념

## Consumer Group 핵심 설정

리밸런싱과 장애 감지에 영향을 주는 설정들입니다.

### Session과 Heartbeat 설정

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

| 설정 | 기본값 (Kafka 3.6) | 역할 | 권장 |
|------|-------------------|------|------|
| `session.timeout.ms` | 45초 | Broker가 Consumer 장애로 판단하는 시간 | 네트워크 지연의 3배 |
| `heartbeat.interval.ms` | 3초 | Heartbeat 전송 주기 | session.timeout / 15 이하 |
| `max.poll.interval.ms` | 5분 | poll() 호출 사이 최대 간격 | 메시지 처리 시간 × 2 |

### max.poll.interval.ms 문제와 해결

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

    // ❌ 문제 상황: 동기 처리로 인한 타임아웃
    @KafkaListener(topics = "orders", groupId = "order-service-bad")
    public void consumeBad(String order) {
        // 외부 결제 API 호출 - 최대 3분 소요 가능
        PaymentResult result = paymentService.process(order);  // ⚠️ 위험!
        // max.poll.interval.ms(5분) 초과 시 리밸런싱 발생
    }

    // ✅ 해결책 1: 비동기 처리 + 수동 커밋
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

> **해결책 2**: `max.poll.records` 축소 - `spring.kafka.consumer.max-poll-records: 10` (기본값 500)

## 리밸런싱 심층 분석

### 리밸런싱이 성능에 미치는 영향

리밸런싱 중에는 **모든 Consumer가 일시 정지**됩니다 (Eager Protocol 기준):

```
리밸런싱 타임라인 (Eager Protocol):
├── 0ms: Consumer 3 장애 감지 (heartbeat 실패)
├── 0ms: 모든 Consumer Partition 해제 (Stop-the-World)
├── ~100ms: Group Coordinator가 새 할당 계산
├── ~200ms: 각 Consumer에게 새 Partition 할당
├── ~500ms: 각 Consumer가 마지막 Offset부터 재개
└── 총 소요: 500ms ~ 수 초 (Consumer 수에 비례)
```

**측정 참고 데이터** (Confluent 블로그, 2021):
- 10개 Consumer: ~1초 리밸런싱
- 100개 Consumer: ~10초 리밸런싱
- 대규모 클러스터: 분 단위 소요 가능

> **출처**: [Confluent Blog - Incremental Cooperative Rebalancing](https://www.confluent.io/blog/cooperative-rebalancing-in-kafka-streams-consumer-ksqldb/)

### 리밸런싱 최소화 전략

#### 1. Cooperative Sticky Assignor (Kafka 2.4+, **권장**)

기존 Eager Protocol과 달리 **필요한 Partition만** 재할당:

```yaml
spring:
  kafka:
    consumer:
      properties:
        partition.assignment.strategy: org.apache.kafka.clients.consumer.CooperativeStickyAssignor
```

**왜 2단계 프로토콜인가?**

Eager Protocol의 문제: 모든 Consumer가 동시에 Partition을 놓으면 **순간적으로 처리량이 0**이 됩니다. LinkedIn에서는 이 "Stop-the-World" 시간이 대규모 클러스터에서 분 단위로 발생하여 SLA 위반 원인이 되었습니다.

Cooperative Protocol의 해결책 (KIP-429):

```
2단계 리밸런싱 프로토콜:

[1단계: Revoke]
├── Group Coordinator: "C2의 P2를 해제해야 함"
├── C2: P2 해제, 다른 Consumer는 계속 처리
└── C1: P0, P1 처리 중 (중단 없음)

[2단계: Assign]
├── Group Coordinator: "P2를 C1에게 할당"
├── C1: P2 추가로 할당받음
└── 결과: 영향 받는 Partition만 잠시 중단
```

**핵심 원리**: "먼저 놓고, 나중에 받는다"가 아니라 "필요한 것만 놓고, 바로 받는다"

> **출처**: [KIP-429: Kafka Consumer Incremental Rebalance Protocol](https://cwiki.apache.org/confluence/display/KAFKA/KIP-429)

#### 2. Static Group Membership (Kafka 2.3+)

Consumer 재시작 시 리밸런싱 방지 (K8s Rolling Update에 유용):

```yaml
spring:
  kafka:
    consumer:
      properties:
        # 고정 ID 부여 - 재시작해도 같은 Consumer로 인식
        group.instance.id: ${HOSTNAME:consumer-1}
        session.timeout.ms: 300000  # 5분 (재시작 시간 확보)
```

| 전략 | 리밸런싱 시간 | 적합한 경우 |
|------|--------------|------------|
| Eager (기본) | 느림 (Stop-the-World) | 소규모 Consumer Group |
| **Cooperative Sticky** | 빠름 (증분) | 대규모 Consumer Group |
| Static Membership | 최소화 | K8s Rolling Update |

### 리밸런싱 모니터링 구현

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

**Spring Kafka에서 등록:**

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
            kafkaListenerContainerFactory(
                ConsumerFactory<String, String> consumerFactory,
                MeterRegistry meterRegistry) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties()
               .setConsumerRebalanceListener(new RebalanceMonitor(meterRegistry));
        return factory;
    }
}
```

**의존성 (build.gradle.kts):**
```kotlin
dependencies {
    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.micrometer:micrometer-core")
}
```

## Consumer Lag 모니터링

Consumer Lag = Producer가 보낸 메시지 수 - Consumer가 처리한 메시지 수

### Lag 확인 명령어

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

### Lag 해석 가이드

| LAG 수치 | 상태 | 조치 |
|----------|------|------|
| 0~100 | 정상 | 모니터링 유지 |
| 100~1,000 | 주의 | 처리 속도 확인 |
| 1,000~10,000 | 경고 | Consumer 증설 검토 |
| 10,000+ | 위험 | 즉시 대응 필요 |

> **핵심**: LAG 수치보다 **LAG 증가 추세**가 더 중요합니다. LAG 1000이 유지되면 문제없지만, LAG 100이 계속 증가하면 조치가 필요합니다.

### Prometheus + Grafana 모니터링

**1. kafka-exporter 설정:**

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

**2. Prometheus scrape config:**

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'kafka-exporter'
    static_configs:
      - targets: ['kafka-exporter:9308']
    scrape_interval: 15s
```

**3. 핵심 PromQL 쿼리:**

```promql
# Consumer Group Lag
kafka_consumergroup_lag{consumergroup="order-service"}

# Lag 증가율 (5분간) - 0보다 크면 적체 중
rate(kafka_consumergroup_lag{consumergroup="order-service"}[5m])

# Lag이 10000 이상인 파티션 수
count(kafka_consumergroup_lag > 10000)

# Consumer 처리율 (초당 메시지)
rate(kafka_consumergroup_current_offset{consumergroup="order-service"}[1m])
```

**4. Alerting Rules:**

```yaml
# alerting-rules.yml
groups:
  - name: kafka-consumer-alerts
    rules:
      - alert: HighConsumerLag
        expr: kafka_consumergroup_lag > 10000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Consumer Lag이 10,000 초과"
          description: "{{ $labels.consumergroup }}의 {{ $labels.topic }}:{{ $labels.partition }} LAG: {{ $value }}"

      - alert: ConsumerLagIncreasing
        expr: rate(kafka_consumergroup_lag[5m]) > 100
        for: 10m
        labels:
          severity: critical
        annotations:
          summary: "Consumer Lag이 지속적으로 증가 중"
          description: "{{ $labels.consumergroup }} LAG 증가율: {{ $value }}/초"

      - alert: ConsumerDown
        expr: kafka_consumergroup_members == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Consumer Group에 활성 멤버 없음"
```

### Offset 수동 리셋

기존 Offset을 무시하고 특정 위치부터 다시 읽어야 할 때:

```bash
# ⚠️ 주의: Consumer가 중지된 상태에서만 가능

# 1. 가장 처음부터 다시 읽기
kafka-consumer-groups.sh --reset-offsets \
    --group order-service \
    --topic orders \
    --to-earliest \
    --execute \
    --bootstrap-server localhost:9092

# 2. 특정 시간 이후부터 읽기 (장애 발생 시점)
kafka-consumer-groups.sh --reset-offsets \
    --group order-service \
    --topic orders \
    --to-datetime 2024-01-15T10:00:00.000 \
    --execute \
    --bootstrap-server localhost:9092

# 3. 특정 Offset으로 이동
kafka-consumer-groups.sh --reset-offsets \
    --group order-service \
    --topic orders:0:1500 \
    --execute \
    --bootstrap-server localhost:9092

# 4. 현재 위치에서 N개 건너뛰기
kafka-consumer-groups.sh --reset-offsets \
    --group order-service \
    --topic orders \
    --shift-by 1000 \
    --execute \
    --bootstrap-server localhost:9092
```

## 트러블슈팅 체크리스트

### Lag 급증 시

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

### 자주 발생하는 문제

| 증상 | 원인 | 해결 |
|------|------|------|
| 특정 파티션만 LAG 증가 | Hot Partition (Key 편중) | Key 분산 또는 Partition 추가 |
| 전체 LAG 급증 | Consumer 처리 속도 부족 | Consumer 인스턴스 증설 |
| LAG 0인데 메시지 누락 | 자동 커밋 + 처리 실패 | 수동 커밋으로 변경 |
| 잦은 리밸런싱 | session.timeout 너무 짧음 | timeout 증가 + Static Membership |

## 프로덕션 배포 체크리스트

Consumer 애플리케이션을 프로덕션에 배포하기 전 확인 사항:

### 설정 점검

- [ ] `group.id` 명명 규칙 준수 (`{서비스명}-{용도}`)
- [ ] `auto.offset.reset` 의도대로 설정 (보통 `earliest`)
- [ ] `enable.auto.commit=false` (수동 커밋 권장)
- [ ] `max.poll.interval.ms` > 최대 처리 시간
- [ ] `session.timeout.ms` / `heartbeat.interval.ms` 비율 확인 (15:1 권장)
- [ ] `partition.assignment.strategy` = `CooperativeStickyAssignor`

### 모니터링 준비

- [ ] Consumer Lag 메트릭 수집 설정
- [ ] Lag 임계값 알림 설정 (warning: 10,000 / critical: 50,000)
- [ ] Rebalancing 발생 알림 설정
- [ ] Consumer 인스턴스 수 모니터링

### 장애 대응 준비

- [ ] DLQ(Dead Letter Queue) 구성
- [ ] Offset 리셋 절차 문서화
- [ ] 롤백 계획 수립
- [ ] 담당자 연락처 및 에스컬레이션 경로

### 성능 검증

- [ ] 예상 TPS의 2배 부하 테스트 완료
- [ ] Consumer 인스턴스 수 ≤ Partition 수 확인
- [ ] 메모리 사용량 모니터링 (GC 로그 활성화)

## FAQ

**Q: Lag이 계속 0인데 정상인가요?**
> A: 정상입니다. Producer 속도 ≤ Consumer 속도이면 Lag은 0에 가깝습니다.

**Q: 리밸런싱이 자주 발생하면 어떻게 하나요?**
> A: 1) `session.timeout.ms` 증가, 2) `CooperativeStickyAssignor` 사용, 3) `static group membership` 적용

**Q: Consumer가 너무 느린데 어떻게 최적화하나요?**
> A: 1) 병렬 처리 (`concurrency` 설정), 2) 배치 처리 (`batch listener`), 3) 외부 호출 비동기화

## 참고 자료

- [Confluent: Incremental Cooperative Rebalancing](https://www.confluent.io/blog/cooperative-rebalancing-in-kafka-streams-consumer-ksqldb/)
- [KIP-429: Consumer Group Protocol Redesign](https://cwiki.apache.org/confluence/display/KAFKA/KIP-429)
- [Kafka Consumer Configurations](https://kafka.apache.org/documentation/#consumerconfigs)

## 다음 단계

- [Producer 튜닝](../producer-tuning/) - Producer 성능 최적화
- [트랜잭션](../transactions/) - Exactly-Once 처리
