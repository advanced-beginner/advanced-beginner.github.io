---
title: Aggregate 경계 정하기
weight: 1
lastmod: "2026-01-10"
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

> **해결하는 문제**: Aggregate를 너무 크게 만들어 성능 이슈가 발생하거나, 너무 작게 만들어 일관성이 깨지는 상황
> **소요 시간**: 약 30분
> **전제 조건**: [Aggregate 심화](../concepts/aggregate/) 문서를 읽었다고 가정

{{< callout type="warning" title="성공 기준" >}}
이 가이드를 완료하면 다음을 할 수 있습니다:
- 불변식을 식별하여 Aggregate 경계를 결정
- 4가지 판단 질문으로 Entity 포함 여부를 결정
- 경계가 잘못되었을 때의 신호를 인식
{{< /callout >}}

## 1. 핵심 불변식 식별하기

<strong>불변식(Invariant)</strong>은 항상 참이어야 하는 비즈니스 규칙입니다. Aggregate 경계를 정하는 가장 중요한 기준입니다.

### 1.1 비즈니스 규칙 나열하기

대상 도메인의 비즈니스 규칙을 모두 나열하라:

```
예시: 주문 도메인

1. 주문에는 최소 1개 이상의 주문 항목이 있어야 한다
2. 주문 총액은 항상 주문 항목 합계와 일치해야 한다
3. 배송 주소는 주문 확정 전까지만 변경할 수 있다
4. 재고는 0 미만이 될 수 없다
5. 고객 등급에 따라 할인율이 달라진다
```

### 1.2 규칙의 일관성 요구사항 분류하기

각 규칙이 <strong>즉각적 일관성</strong>이 필요한지, <strong>결과적 일관성</strong>으로 충분한지 분류하라:

| 규칙 | 일관성 요구 | 이유 |
|------|------------|------|
| 주문 총액 = 항목 합계 | 즉각적 | 결제 금액 오류 방지 |
| 주문에 최소 1개 항목 | 즉각적 | 빈 주문 방지 |
| 재고 차감 | 결과적 | 주문과 재고는 독립적으로 변경 가능 |
| 할인율 적용 | 결과적 | 고객 정보와 주문은 독립적 |

**즉각적 일관성이 필요한 규칙들 → 같은 Aggregate에 포함**

## 2. 4가지 질문으로 경계 결정하기

Entity를 Aggregate에 포함할지 결정할 때 다음 4가지 질문을 순서대로 적용하라:

### 질문 1: "이 Entity가 Root 없이 존재할 수 있는가?"

```
OrderLine은 Order 없이 의미가 있는가?
→ No → Order Aggregate에 포함

Customer는 Order 없이 의미가 있는가?
→ Yes → 별도 Aggregate
```

### 질문 2: "이 Entity를 다른 곳에서 직접 참조하는가?"

```
Product는 여러 Order에서 참조되는가?
→ Yes → 별도 Aggregate, ID로 참조

ShippingAddress는 다른 곳에서 참조되는가?
→ No → Order Aggregate에 포함 가능
```

### 질문 3: "이 Entity가 Root와 항상 함께 변경되는가?"

```
OrderLine은 Order와 함께 생성/수정되는가?
→ Yes → Order Aggregate에 포함

Payment는 Order와 별개로 변경되는가?
→ Yes (결제 취소, 환불 등) → 별도 Aggregate
```

### 질문 4: "포함했을 때 트랜잭션이 너무 커지는가?"

```
OrderHistory(모든 상태 변경 이력)를 포함하면?
→ 데이터가 계속 증가 → 별도 저장소로 분리
```

### 2.1 질문 적용 결과 정리하기

질문을 적용한 결과를 다음과 같이 정리하라:

```
Order Aggregate:
├── Order (Root)
├── OrderLine ✓ (Q1: No, Q2: No, Q3: Yes, Q4: OK)
└── ShippingAddress ✓ (Q1: No, Q2: No, Q3: Yes, Q4: OK)

별도 Aggregate:
├── Customer (Q1: Yes)
├── Product (Q2: Yes - 여러 곳에서 참조)
├── Payment (Q3: No - 독립적 생명주기)
├── Stock (Q3: No - 자주 독립적 변경)
└── OrderHistory (Q4: 데이터 증가)
```

## 3. Aggregate 경계 코드로 표현하기

### 3.1 ID 참조로 변환하기

다른 Aggregate는 객체 참조 대신 ID로 참조하라:

```java
// ❌ 잘못된 예: 객체 직접 참조
public class Order {
    private Customer customer;      // Aggregate 직접 참조
    private List<Product> products; // Aggregate 직접 참조
}

// ✅ 올바른 예: ID 참조
public class Order {
    private CustomerId customerId;
    private List<OrderLine> orderLines; // 내부 Entity만 포함
}
```

### 3.2 필요한 정보는 복사하기

조회 시 필요한 정보는 Aggregate 내부에 복사본을 유지하라:

```java
public class OrderLine {
    private OrderLineId id;
    private ProductId productId;     // ID로 참조
    private String productName;      // 표시용 복사본
    private Money price;             // 주문 시점 가격 복사본
    private int quantity;
}
```

### 3.3 불변식을 Root에서 강제하기

Aggregate Root가 불변식을 책임지도록 구현하라:

```java
public class Order {
    private List<OrderLine> orderLines;
    private Money totalAmount;

    // 불변식: 최소 1개 항목
    public void removeOrderLine(OrderLineId lineId) {
        if (orderLines.size() <= 1) {
            throw new BusinessRuleViolationException(
                "주문에는 최소 1개의 항목이 있어야 합니다"
            );
        }
        orderLines.removeIf(line -> line.getId().equals(lineId));
        recalculateTotal();
    }

    // 불변식: 총액 일치
    private void recalculateTotal() {
        this.totalAmount = orderLines.stream()
            .map(OrderLine::getAmount)
            .reduce(Money.ZERO, Money::add);
    }
}
```

## 4. 경계 검증하기

설계한 경계가 올바른지 다음 체크리스트로 검증하라:

### 4.1 크기 체크리스트

| 항목 | 건강한 수준 | 경고 | 확인 |
|------|-----------|------|------|
| 내부 Entity 개수 | 1-5개 | 5개 초과 | ☐ |
| 평균 로드 시간 | < 10ms | > 50ms | ☐ |
| 평균 Aggregate 크기 | < 10KB | > 50KB | ☐ |

### 4.2 일관성 체크리스트

- ☐ 모든 즉각적 일관성 규칙이 하나의 Aggregate 안에 있다
- ☐ Aggregate 간 변경은 이벤트로 동기화된다
- ☐ 외부에서 내부 Entity를 직접 수정할 수 없다

### 4.3 테스트로 검증하기

Aggregate가 독립적으로 테스트 가능해야 한다:

```java
@Test
void 주문_확정_시_상태가_변경된다() {
    // Given - Aggregate만으로 테스트 가능
    Order order = Order.create(
        new OrderId("ORD-001"),
        new CustomerId("CUST-001"),
        List.of(new OrderLine(productId, "상품A", Money.won(10000), 2))
    );

    // When
    order.confirm();

    // Then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
}
```

## 트러블슈팅

### 문제: "Aggregate가 너무 커서 성능이 느립니다"

**증상**: 로드 시간 50ms 이상, 동시성 충돌 빈번

**해결 방법**:
1. 질문 4를 다시 적용하라. 자주 변경되지만 함께 변경될 필요 없는 데이터를 분리하라
2. 이력 데이터(History, Log)는 별도 저장소로 분리하라
3. 컬렉션 크기가 무한정 증가하는지 확인하라

```java
// Before: OrderHistory가 계속 증가
public class Order {
    private List<OrderStatusChange> history; // 무한 증가
}

// After: 이력은 별도 Aggregate로 분리
public class Order {
    private OrderStatus currentStatus;
    // history 제거
}

public class OrderAuditLog { // 별도 Aggregate
    private OrderId orderId;
    private List<StatusChange> changes;
}
```

---

### 문제: "Aggregate 간 일관성이 깨집니다"

**증상**: 주문은 확정되었는데 재고가 차감되지 않음

**해결 방법**:
1. 도메인 이벤트를 사용하여 Aggregate 간 변경을 동기화하라
2. 이벤트 처리 실패에 대한 재시도 메커니즘을 구현하라

```java
// Order Aggregate
public void confirm() {
    this.status = OrderStatus.CONFIRMED;
    registerEvent(new OrderConfirmedEvent(this.id, this.orderLines));
}

// Stock Aggregate - 이벤트 핸들러
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void handle(OrderConfirmedEvent event) {
    for (OrderLineInfo line : event.getOrderLines()) {
        Stock stock = stockRepository.findByProductId(line.getProductId());
        stock.reserve(line.getQuantity());
        stockRepository.save(stock);
    }
}
```

---

### 문제: "같은 Entity가 여러 Aggregate에서 필요합니다"

**증상**: Product 정보가 Order, Cart, Wishlist에서 모두 필요함

**해결 방법**:
1. Product를 별도 Aggregate로 유지하고 ID로만 참조하라
2. 각 Aggregate에 필요한 정보의 스냅샷을 저장하라

```java
// Order는 주문 시점의 상품 정보 스냅샷 보유
public class OrderLine {
    private ProductId productId;          // ID 참조
    private String productNameSnapshot;    // 주문 시점 이름
    private Money priceSnapshot;           // 주문 시점 가격
}

// Cart는 현재 가격을 조회해서 표시
public class CartItem {
    private ProductId productId;
    // 표시할 때 Product Aggregate에서 현재 정보 조회
}
```

---

### 문제: "트랜잭션에서 여러 Aggregate를 수정해야 합니다"

**증상**: 주문 확정 시 Order, Stock, Point를 동시에 변경해야 함

**해결 방법**:
1. 정말 즉각적 일관성이 필요한지 다시 검토하라 (대부분 결과적 일관성으로 충분)
2. 즉각적 일관성이 필수라면 해당 Entity들을 하나의 Aggregate로 병합하라
3. 결과적 일관성이 가능하다면 이벤트로 분리하라

```java
// ❌ 여러 Aggregate 동시 수정
@Transactional
public void confirmOrder(OrderId orderId) {
    order.confirm();
    stock.decrease();  // 다른 Aggregate
    customer.addPoints();  // 또 다른 Aggregate
}

// ✅ 이벤트로 분리
@Transactional
public void confirmOrder(OrderId orderId) {
    Order order = orderRepository.findById(orderId);
    order.confirm();  // OrderConfirmedEvent 발행
    orderRepository.save(order);
}
// Stock과 Point는 이벤트 핸들러에서 별도 트랜잭션으로 처리
```

## 다음 단계

- [Aggregate 심화](../concepts/aggregate/) - 더 자세한 설계 원칙과 패턴
- [도메인 이벤트](../concepts/domain-events/) - Aggregate 간 통신 방법
- [주문 도메인 예제](../examples/order-domain/) - 실제 구현 예시
