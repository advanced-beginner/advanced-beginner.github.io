---
lastmod: "2026-01-07"
title: Aggregate 실전 패턴
weight: 4
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

# Aggregate 실전 패턴

Aggregate 설계를 위한 구현 패턴과 의사결정 가이드입니다.

> **선수 지식:** 먼저 [Aggregate 심화](../aggregate/)에서 핵심 개념을 읽어주세요.

> **이 페이지 예제의 공통 import:**
> ```java
> import java.util.*;
> import java.time.LocalDateTime;
> import javax.persistence.Version;
> import org.springframework.context.ApplicationEventPublisher;
> ```

## Aggregate 루트 설계

### 루트를 통한 모든 변경

```java
public class Order {
    private List<OrderLine> orderLines;

    // ✅ 루트를 통해 내부 객체 추가
    public void addOrderLine(ProductId productId, String name, Money price, int qty) {
        validateCanModify();

        OrderLine newLine = new OrderLine(
            OrderLineId.generate(),
            productId,
            name,
            price,
            qty
        );
        this.orderLines.add(newLine);
        recalculateTotal();
    }

    // ✅ 루트를 통해 내부 객체 수정
    public void changeQuantity(OrderLineId lineId, int newQuantity) {
        validateCanModify();

        OrderLine line = findOrderLine(lineId);
        line.changeQuantity(newQuantity);  // 내부에서만 변경 허용
        recalculateTotal();
    }

    // 내부 객체를 직접 노출하지 않음
    public List<OrderLine> getOrderLines() {
        return Collections.unmodifiableList(orderLines);
    }
}
```

### 불변식 검증

```java
public class Order {
    private static final int MAX_ORDER_LINES = 100;
    private static final Money MAX_ORDER_AMOUNT = Money.won(10_000_000);

    public void addOrderLine(OrderLine line) {
        // 불변식 1: 주문 항목 수 제한
        if (orderLines.size() >= MAX_ORDER_LINES) {
            throw new TooManyOrderLinesException(MAX_ORDER_LINES);
        }

        orderLines.add(line);
        recalculateTotal();

        // 불변식 2: 최대 주문 금액 제한
        if (totalAmount.isGreaterThan(MAX_ORDER_AMOUNT)) {
            orderLines.remove(line);  // 롤백
            recalculateTotal();
            throw new OrderAmountExceededException(MAX_ORDER_AMOUNT);
        }
    }
}
```

## 실전 패턴

### 패턴 1: 낙관적 락(Optimistic Locking)

```java
@Entity
public class OrderEntity {
    @Id
    private String id;

    @Version  // 낙관적 락
    private Long version;

    // ...
}
```

```java
// 동시 수정 시 예외 발생
try {
    order.confirm();
    orderRepository.save(order);
} catch (OptimisticLockingFailureException e) {
    // 재시도 로직
    throw new ConcurrentModificationException("주문이 다른 곳에서 수정되었습니다");
}
```

### 패턴 2: Aggregate 복원

```java
public class Order {
    // 저장된 상태에서 복원 (Factory 패턴)
    public static Order reconstitute(
        OrderId id,
        CustomerId customerId,
        OrderStatus status,
        List<OrderLine> orderLines,
        ShippingAddress address,
        LocalDateTime createdAt
    ) {
        Order order = new Order();
        order.id = id;
        order.customerId = customerId;
        order.status = status;
        order.orderLines = new ArrayList<>(orderLines);
        order.shippingAddress = address;
        order.createdAt = createdAt;
        return order;
    }

    // 새로 생성
    public static Order create(CustomerId customerId, List<OrderLine> orderLines) {
        Order order = new Order();
        order.id = OrderId.generate();
        order.customerId = customerId;
        order.status = OrderStatus.PENDING;
        order.orderLines = new ArrayList<>(orderLines);
        order.createdAt = LocalDateTime.now();

        order.registerEvent(new OrderCreatedEvent(order.id));
        return order;
    }
}
```

### 패턴 3: 도메인 이벤트 수집

```java
public abstract class AggregateRoot {
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}

public class Order extends AggregateRoot {

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
        registerEvent(new OrderConfirmedEvent(this.id));
    }

    public void cancel(CancellationReason reason) {
        this.status = OrderStatus.CANCELLED;
        registerEvent(new OrderCancelledEvent(this.id, reason));
    }
}
```

### 패턴 4: Repository에서 이벤트 발행

```java
@Repository
public class JpaOrderRepository implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Order save(Order order) {
        OrderEntity entity = toEntity(order);
        jpaRepository.save(entity);

        // 저장 후 이벤트 발행
        order.getDomainEvents().forEach(eventPublisher::publishEvent);
        order.clearDomainEvents();

        return order;
    }
}
```

## Aggregate 경계 결정 가이드

### 질문 체크리스트

```mermaid
flowchart TB
    Q1{함께 변경되어야<br>하는가?}
    Q2{같은 트랜잭션이<br>필수인가?}
    Q3{진정한 불변식이<br>존재하는가?}

    Q1 -->|Yes| Q2
    Q1 -->|No| SEP[분리된 Aggregate]

    Q2 -->|Yes| Q3
    Q2 -->|No| SEP

    Q3 -->|Yes| SAME[같은 Aggregate]
    Q3 -->|No| SEP
```

### 예시: 주문과 결제

```
질문: 주문(Order)과 결제(Payment)는 같은 Aggregate?

1. 함께 변경되어야 하는가?
   → 주문 없이 결제는 없지만, 결제 실패해도 주문은 유지
   → No

2. 같은 트랜잭션이 필수인가?
   → 결제는 외부 PG 연동, 실패/재시도 많음
   → 분리해야 안전
   → No

3. 진정한 불변식이 있는가?
   → "주문 금액 = 결제 금액" 은 결과적 일관성으로 충분
   → No

결론: 분리된 Aggregate
```

```java
// 분리된 Aggregate
public class Order {
    private OrderId id;
    private PaymentId paymentId;  // ID로만 참조
    private PaymentStatus paymentStatus;  // 상태 복사
}

public class Payment {
    private PaymentId id;
    private OrderId orderId;  // ID로만 참조
    private Money amount;
    private PaymentStatus status;
}
```

## 안티패턴

### 1. God Aggregate

```java
// ❌ 모든 것을 포함하는 거대한 Aggregate
public class Order {
    private Customer customer;  // Customer 전체
    private List<Product> products;  // Product 전체
    private Payment payment;  // Payment 전체
    private Shipment shipment;  // Shipment 전체
    // 트랜잭션 범위가 너무 넓음
}
```

**문제점:**
- 동시 접근 시 경합 증가
- 거대한 객체 그래프로 로딩 지연
- 한 부분의 변경이 무관한 부분에 영향

**해결책:** ID 참조로 별도 Aggregate로 분리

### 2. Anemic Aggregate

```java
// ❌ 로직 없는 빈약한 Aggregate
public class Order {
    private OrderId id;
    private OrderStatus status;

    // getter/setter만 존재
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}

// 로직이 서비스에 분산
public class OrderService {
    public void confirm(Order order) {
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
        }
    }
}
```

**문제점:**
- 비즈니스 규칙이 서비스에 분산
- 불변식이 어디서든 위반 가능
- 도메인 로직 파악 어려움

**해결책:** 비즈니스 로직을 Aggregate로 이동

### 3. 외부 의존성을 가진 Aggregate

```java
// ❌ 외부 서비스를 호출하는 Aggregate
public class Order {
    @Autowired  // 의존성 주입 금지!
    private InventoryService inventoryService;

    public void confirm() {
        // Aggregate가 직접 외부 서비스 호출 - 피해야 함
        inventoryService.reserve(this.orderLines);
        this.status = OrderStatus.CONFIRMED;
    }
}
```

**해결책:** Domain Event를 통한 외부 통합

## 요약

| 패턴 | 사용 시점 |
|------|----------|
| **낙관적 락** | 동시 수정 가능성 있을 때 |
| **복원 패턴** | 생성과 DB 로딩 분리 시 |
| **이벤트 수집** | Aggregate 간 통신 시 |
| **경계 결정** | 새 Aggregate 설계 시 |

## 다음 단계

- [도메인 이벤트](../domain-events/) - 이벤트 기반 통합
- [안티패턴](../anti-patterns/) - 피해야 할 실수들
