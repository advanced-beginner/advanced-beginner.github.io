---
title: 도메인 이벤트 설계하기
description: "도메인 이벤트를 설계하고 구현하는 방법을 안내합니다."
weight: 4
lastmod: "2026-01-16"
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

> **해결하는 문제**: Aggregate 간 강한 결합으로 트랜잭션이 복잡해지거나, 변경 전파가 어려운 상황
> **소요 시간**: 약 25분
> **전제 조건**: [Aggregate 심화]({{< relref "/docs/ddd/concepts/aggregate" >}}) 문서를 읽었다고 가정

{{< callout type="warning" title="성공 기준" >}}
이 가이드를 완료하면 다음을 할 수 있습니다:
- 도메인 이벤트가 필요한 상황 식별
- 이벤트 명명 규칙과 페이로드 설계
- Spring의 이벤트 발행/구독 패턴 구현
{{< /callout >}}

## 1. 도메인 이벤트가 필요한 상황 식별하기

### 1.1 이벤트가 필요한 3가지 신호

다음 3가지 신호를 확인하여 도메인 이벤트가 필요한지 판단하세요.

**신호 1: "~하면 ~해야 한다" 패턴**

비즈니스 규칙을 설명할 때 이 패턴이 나오면 이벤트 후보로 기록하세요:

```
"주문이 확정되면 재고를 차감해야 한다"
→ OrderConfirmedEvent → StockDecrease

"결제가 완료되면 포인트를 적립해야 한다"
→ PaymentCompletedEvent → PointAccumulation

"배송이 완료되면 리뷰 요청을 보내야 한다"
→ DeliveryCompletedEvent → ReviewRequest
```

**신호 2: 여러 Aggregate 수정이 필요한 트랜잭션**

```java
// ❌ 여러 Aggregate를 한 트랜잭션에서 수정
@Transactional
public void confirmOrder(OrderId orderId) {
    Order order = orderRepository.findById(orderId);
    order.confirm();

    Stock stock = stockRepository.findByProductId(productId);
    stock.decrease(quantity);  // 다른 Aggregate

    Customer customer = customerRepository.findById(customerId);
    customer.addPoints(points);  // 또 다른 Aggregate
}
```

**신호 3: 동기 호출 체인이 길어지는 경우**

```java
// ❌ 동기 호출 체인
public void confirmOrder(OrderId orderId) {
    orderService.confirm(orderId);
    stockService.decrease(productId, quantity);  // 동기 호출
    paymentService.capture(paymentId);           // 동기 호출
    notificationService.send(customerId);        // 동기 호출
    pointService.accumulate(customerId, points); // 동기 호출
}
// 하나라도 실패하면 전체 실패
```

### 1.2 이벤트 사용 판단 기준표

| 상황 | 이벤트 사용? | 이유 |
|------|------------|------|
| 같은 Aggregate 내 상태 변경 | No | 직접 메서드 호출 |
| 다른 Aggregate 알림 | Yes | 결합도 감소 |
| 외부 시스템 연동 | Yes | 비동기 처리 |
| 감사 로깅 | Yes | 관심사 분리 |
| 즉각적 일관성 필요 | No | 동기 처리 |
| 결과적 일관성 허용 | Yes | 비동기 처리 |

## 2. 도메인 이벤트 설계하기

### 2.1 이벤트 명명 규칙

다음 3가지 규칙을 따라 이벤트 이름을 정하세요.

**규칙 1: 과거형 동사 사용**

```
✅ OrderConfirmedEvent    (주문이 확정되었음)
✅ PaymentCompletedEvent  (결제가 완료되었음)
✅ StockDepletedEvent     (재고가 소진되었음)

❌ OrderConfirmEvent      (동사 원형)
❌ ConfirmOrderEvent      (명령형)
```

**규칙 2: 도메인 용어 사용**

```
✅ OrderConfirmedEvent    (도메인 용어: 확정)
✅ ShipmentDispatchedEvent (도메인 용어: 발송)

❌ OrderStatusChangedEvent (너무 일반적)
❌ OrderUpdatedEvent       (의미 불명확)
```

**규칙 3: Aggregate + 행위 + Event**

```
[Aggregate이름][행위과거형]Event

OrderConfirmedEvent      = Order + Confirmed + Event
PaymentCapturedEvent     = Payment + Captured + Event
StockReservedEvent       = Stock + Reserved + Event
CustomerRegisteredEvent  = Customer + Registered + Event
```

### 2.2 이벤트 페이로드 설계

다음 필수 속성을 모든 이벤트에 포함하세요.

**필수 속성**:

```java
public abstract class DomainEvent {
    private final String eventId;          // 이벤트 고유 ID
    private final LocalDateTime occurredAt; // 발생 시각
    private final String aggregateType;    // Aggregate 유형
    private final String aggregateId;      // Aggregate ID

    protected DomainEvent(String aggregateType, String aggregateId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
    }
}
```

**구체적인 이벤트**:

```java
public class OrderConfirmedEvent extends DomainEvent {
    private final OrderId orderId;
    private final CustomerId customerId;
    private final Money totalAmount;
    private final List<OrderLineInfo> orderLines;
    private final LocalDateTime confirmedAt;

    public OrderConfirmedEvent(Order order) {
        super("Order", order.getId().value());
        this.orderId = order.getId();
        this.customerId = order.getCustomerId();
        this.totalAmount = order.getTotalAmount();
        this.orderLines = order.getOrderLines().stream()
            .map(OrderLineInfo::from)
            .toList();
        this.confirmedAt = LocalDateTime.now();
    }

    // 이벤트 페이로드용 불변 DTO
    public record OrderLineInfo(
        ProductId productId,
        String productName,
        int quantity,
        Money price
    ) {
        public static OrderLineInfo from(OrderLine line) {
            return new OrderLineInfo(
                line.getProductId(),
                line.getProductName(),
                line.getQuantity(),
                line.getPrice()
            );
        }
    }
}
```

### 2.3 페이로드 설계 원칙

{{< callout type="info" title="핵심 원칙" >}}
이벤트 페이로드는 구독자가 다시 조회하지 않도록 충분한 정보를 포함하되, 전체 Aggregate를 담지 마세요.
{{< /callout >}}

**원칙 1: 필요한 정보만 포함**

```java
// ❌ 전체 Aggregate 포함
public class OrderConfirmedEvent {
    private final Order order;  // 순환 참조, 직렬화 문제
}

// ✅ 필요한 정보만 추출
public class OrderConfirmedEvent {
    private final OrderId orderId;
    private final CustomerId customerId;
    private final Money totalAmount;
}
```

**원칙 2: 불변 데이터 사용**

```java
// ✅ 불변 Record 사용
public record OrderConfirmedEvent(
    String eventId,
    LocalDateTime occurredAt,
    OrderId orderId,
    CustomerId customerId,
    Money totalAmount,
    List<OrderLineInfo> orderLines
) implements DomainEvent {

    public OrderConfirmedEvent {
        orderLines = List.copyOf(orderLines);  // 방어적 복사
    }
}
```

**원칙 3: 구독자에게 필요한 정보 포함**

```java
// 구독자가 다시 조회하지 않도록 충분한 정보 포함
public class OrderConfirmedEvent extends DomainEvent {
    // 재고 서비스가 필요로 하는 정보
    private final List<OrderLineInfo> orderLines;

    // 알림 서비스가 필요로 하는 정보
    private final CustomerId customerId;
    private final String customerEmail;

    // 정산 서비스가 필요로 하는 정보
    private final Money totalAmount;
}
```

## 3. 이벤트 발행 구현하기

### 3.1 Aggregate에서 이벤트 등록

Aggregate Root에 이벤트 등록 기능을 추가하세요:

```java
public abstract class AggregateRoot {

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}

public class Order extends AggregateRoot {

    public void confirm() {
        validateConfirmable();
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();

        // 이벤트 등록 (아직 발행하지 않음)
        registerEvent(new OrderConfirmedEvent(this));
    }

    public void cancel(CancellationReason reason) {
        validateCancellable();
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;

        registerEvent(new OrderCancelledEvent(this, reason));
    }
}
```

### 3.2 Application Service에서 이벤트 발행

Application Service에서 저장 후 이벤트를 발행하세요:

```java
@Service
@Transactional
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void confirmOrder(OrderId orderId) {
        // 1. Aggregate 조회
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 2. 비즈니스 로직 실행 (이벤트 등록됨)
        order.confirm();

        // 3. 저장
        orderRepository.save(order);

        // 4. 이벤트 발행
        order.getDomainEvents().forEach(eventPublisher::publishEvent);
        order.clearDomainEvents();
    }
}
```

### 3.3 트랜잭션 커밋 후 이벤트 발행

{{< callout type="warning" title="주의" >}}
`@TransactionalEventListener`를 사용하면 트랜잭션 커밋 후에 이벤트가 발행됩니다. 이벤트 처리 실패 시 원래 트랜잭션은 롤백되지 않습니다.
{{< /callout >}}

트랜잭션 커밋 후 이벤트를 처리하려면 다음과 같이 구현하세요:

```java
@Component
public class DomainEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        // 트랜잭션 커밋 후 실행
        // 이벤트 처리 실패해도 주문 확정은 롤백되지 않음
    }
}
```

## 4. 이벤트 구독 구현하기

### 4.1 동기 이벤트 핸들러

같은 트랜잭션에서 처리해야 하는 경우 `@EventListener`를 사용하세요:

```java
@Component
public class OrderEventHandler {

    private final StockService stockService;

    @EventListener
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        // 같은 트랜잭션에서 실행
        // 실패하면 주문 확정도 롤백
        for (OrderLineInfo line : event.orderLines()) {
            stockService.reserve(line.productId(), line.quantity());
        }
    }
}
```

### 4.2 비동기 이벤트 핸들러

별도 트랜잭션에서 처리하려면 `@Async`와 `@TransactionalEventListener`를 조합하세요:

```java
@Component
public class NotificationEventHandler {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        // 별도 스레드에서 실행
        // 실패해도 주문 확정은 유지
        notificationService.sendOrderConfirmation(
            event.customerId(),
            event.orderId()
        );
    }
}
```

### 4.3 여러 핸들러 등록

하나의 이벤트에 여러 핸들러를 등록하세요. 각 핸들러는 독립적으로 실행됩니다:

```java
// 재고 처리
@Component
public class StockEventHandler {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserveStock(OrderConfirmedEvent event) {
        // 재고 예약
    }
}

// 포인트 적립
@Component
public class PointEventHandler {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void accumulatePoints(OrderConfirmedEvent event) {
        // 포인트 적립
    }
}

// 알림 발송
@Component
public class NotificationEventHandler {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendNotification(OrderConfirmedEvent event) {
        // 알림 발송
    }
}
```

## 5. 이벤트 실패 처리

{{< callout type="warning" title="이벤트 실패는 반드시 처리하세요" >}}
이벤트 핸들러 실패를 무시하면 데이터 불일치가 발생합니다. 재시도와 최종 실패 처리를 반드시 구현하세요.
{{< /callout >}}

### 5.1 재시도 전략

Spring Retry를 사용하여 일시적 오류를 처리하세요:

```java
@Component
public class StockEventHandler {

    private final StockService stockService;

    @Retryable(
        value = {OptimisticLockException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        for (OrderLineInfo line : event.orderLines()) {
            stockService.decrease(line.productId(), line.quantity());
        }
    }

    @Recover
    public void handleFailure(Exception e, OrderConfirmedEvent event) {
        // 최종 실패 시 처리
        log.error("재고 차감 최종 실패. 수동 처리 필요. orderId={}",
            event.orderId(), e);
        alertService.sendAlert("재고 차감 실패", event);
    }
}
```

### 5.2 Outbox 패턴 (신뢰성 보장)

{{< callout type="tip" title="권장: Outbox 패턴" >}}
이벤트 발행 실패 시 데이터 손실을 방지하려면 Outbox 패턴을 사용하세요. 이벤트를 먼저 DB에 저장하고, 별도 프로세스에서 발행합니다.
{{< /callout >}}

이벤트를 DB에 저장 후 발행하세요:

```java
// 이벤트를 DB에 저장 후 발행
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    @Id
    private String id;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String payload;
    private LocalDateTime createdAt;
    private boolean published;
}

@Service
@Transactional
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxRepository;

    public void confirmOrder(OrderId orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.confirm();
        orderRepository.save(order);

        // 이벤트를 Outbox 테이블에 저장
        order.getDomainEvents().forEach(event -> {
            outboxRepository.save(OutboxEvent.from(event));
        });
        order.clearDomainEvents();
    }
}

// 별도 프로세스에서 Outbox 폴링하여 발행
@Scheduled(fixedDelay = 1000)
public void publishOutboxEvents() {
    List<OutboxEvent> events = outboxRepository.findUnpublished();
    for (OutboxEvent event : events) {
        kafkaTemplate.send("domain-events", event.getPayload());
        event.markAsPublished();
        outboxRepository.save(event);
    }
}
```

## 6. 이벤트 테스트

### 6.1 단위 테스트

```java
@Test
void 주문_확정_시_이벤트가_등록된다() {
    // given
    Order order = createPendingOrder();

    // when
    order.confirm();

    // then
    List<DomainEvent> events = order.getDomainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(OrderConfirmedEvent.class);

    OrderConfirmedEvent event = (OrderConfirmedEvent) events.get(0);
    assertThat(event.orderId()).isEqualTo(order.getId());
    assertThat(event.totalAmount()).isEqualTo(order.getTotalAmount());
}
```

### 6.2 통합 테스트

```java
@SpringBootTest
class OrderEventIntegrationTest {

    @Autowired
    private OrderCommandService orderCommandService;

    @MockBean
    private StockService stockService;

    @Test
    void 주문_확정_시_재고가_차감된다() {
        // given
        OrderId orderId = createTestOrder();

        // when
        orderCommandService.confirmOrder(orderId);

        // then
        verify(stockService, times(1)).decrease(any(), anyInt());
    }
}
```

## 트러블슈팅

### 문제: "이벤트가 발행되지 않습니다"

**확인 사항**:

1. `registerEvent()`를 호출했는지 확인
2. `clearDomainEvents()` 호출 전에 발행했는지 확인
3. `@TransactionalEventListener` 사용 시 트랜잭션이 커밋되는지 확인

```java
// 디버깅용 로깅 추가
@EventListener
public void logAllEvents(DomainEvent event) {
    log.debug("이벤트 수신: {}", event);
}
```

---

### 문제: "이벤트 핸들러에서 예외가 발생하면 원래 트랜잭션이 롤백됩니다"

**해결 방법**:

`AFTER_COMMIT`과 `REQUIRES_NEW`를 사용하세요:

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void handleEvent(OrderConfirmedEvent event) {
    // 원래 트랜잭션과 분리된 새 트랜잭션에서 실행
}
```

---

### 문제: "이벤트 순서가 보장되지 않습니다"

**해결 방법**:

`@Order` 어노테이션으로 순서를 지정하세요:

```java
@Order(1)
@EventListener
public void handleFirst(OrderConfirmedEvent event) {
    // 먼저 실행
}

@Order(2)
@EventListener
public void handleSecond(OrderConfirmedEvent event) {
    // 나중에 실행
}
```

## 관련 문서

- [Kafka 토픽 설계 가이드]({{< relref "/docs/kafka/howto/topic-design" >}}) - 도메인 이벤트를 Kafka 토픽으로 발행할 때의 설계 원칙

## 다음 단계

- [이벤트 기반 아키텍처]({{< relref "/docs/ddd/concepts/architecture/event-driven" >}}) - 이벤트 소싱, CQRS
- [CQRS]({{< relref "/docs/ddd/concepts/architecture/cqrs" >}}) - 명령과 조회 분리
- [Event Sourcing 예제]({{< relref "/docs/ddd/examples/event-sourcing" >}}) - 실제 구현 예시
