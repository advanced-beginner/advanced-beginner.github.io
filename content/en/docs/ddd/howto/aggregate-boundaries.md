---
title: Defining Aggregate Boundaries
description: "How to identify and define Aggregate boundaries"
weight: 1
lastmod: "2026-01-10"
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

> **Problem It Solves**: Performance issues from making Aggregates too large, or consistency violations from making them too small
> **Time Required**: ~30 minutes
> **Prerequisites**: Assumes you have read the [Aggregate Deep Dive]({{< relref "/docs/ddd/concepts/aggregate" >}}) document

{{< callout type="warning" title="Success Criteria" >}}
After completing this guide, you will be able to:
- Identify invariants to determine Aggregate boundaries
- Decide whether to include an Entity using 4 key questions
- Recognize signals when boundaries are wrong
{{< /callout >}}

## 1. Identify Core Invariants

An **Invariant** is a business rule that must always be true. It is the most important criterion for defining Aggregate boundaries.

### 1.1 List Business Rules

List all business rules for your target domain:

```
Example: Order Domain

1. An order must have at least one order line item
2. Order total must always match the sum of order line items
3. Shipping address can only be changed before order confirmation
4. Stock cannot go below zero
5. Discount rate varies by customer tier
```

### 1.2 Classify Consistency Requirements for Each Rule

Classify whether each rule requires **immediate consistency** or if **eventual consistency** is sufficient:

| Rule | Consistency Required | Reason |
|------|---------------------|--------|
| Order total = sum of items | Immediate | Prevent payment amount errors |
| At least 1 item per order | Immediate | Prevent empty orders |
| Stock deduction | Eventual | Order and stock can change independently |
| Discount rate application | Eventual | Customer info and orders are independent |

**Rules requiring immediate consistency → Include in the same Aggregate**

## 2. Determine Boundaries with 4 Questions

When deciding whether to include an Entity in an Aggregate, apply these 4 questions in order:

### Question 1: "Can this Entity exist without the Root?"

```
Can OrderLine have meaning without Order?
→ No → Include in Order Aggregate

Can Customer have meaning without Order?
→ Yes → Separate Aggregate
```

### Question 2: "Is this Entity directly referenced from elsewhere?"

```
Is Product referenced by multiple Orders?
→ Yes → Separate Aggregate, reference by ID

Is ShippingAddress referenced from elsewhere?
→ No → Can be included in Order Aggregate
```

### Question 3: "Is this Entity always changed together with the Root?"

```
Is OrderLine created/modified together with Order?
→ Yes → Include in Order Aggregate

Is Payment changed independently of Order?
→ Yes (cancellation, refunds, etc.) → Separate Aggregate
```

### Question 4: "Would including it make the transaction too large?"

```
What if we include OrderHistory (all status change history)?
→ Data keeps growing → Separate into different storage
```

### 2.1 Organize Question Results

Organize the results of applying the questions as follows:

```
Order Aggregate:
├── Order (Root)
├── OrderLine ✓ (Q1: No, Q2: No, Q3: Yes, Q4: OK)
└── ShippingAddress ✓ (Q1: No, Q2: No, Q3: Yes, Q4: OK)

Separate Aggregates:
├── Customer (Q1: Yes)
├── Product (Q2: Yes - referenced from multiple places)
├── Payment (Q3: No - independent lifecycle)
├── Stock (Q3: No - frequently changed independently)
└── OrderHistory (Q4: data growth)
```

## 3. Express Aggregate Boundaries in Code

### 3.1 Convert to ID References

Reference other Aggregates by ID instead of direct object references:

```java
// ❌ Wrong: Direct Aggregate reference
public class Order {
    private Customer customer;      // Direct Aggregate reference
    private List<Product> products; // Direct Aggregate reference
}

// ✅ Correct: ID reference
public class Order {
    private CustomerId customerId;
    private List<OrderLine> orderLines; // Only internal Entities included
}
```

### 3.2 Copy Necessary Information

Maintain copies of information needed for queries within the Aggregate:

```java
public class OrderLine {
    private OrderLineId id;
    private ProductId productId;     // Reference by ID
    private String productName;      // Copy for display
    private Money price;             // Copy of price at order time
    private int quantity;
}
```

### 3.3 Enforce Invariants at the Root

Implement the Aggregate Root to be responsible for invariants:

```java
public class Order {
    private List<OrderLine> orderLines;
    private Money totalAmount;

    // Invariant: At least 1 item
    public void removeOrderLine(OrderLineId lineId) {
        if (orderLines.size() <= 1) {
            throw new BusinessRuleViolationException(
                "Order must have at least one item"
            );
        }
        orderLines.removeIf(line -> line.getId().equals(lineId));
        recalculateTotal();
    }

    // Invariant: Total must match
    private void recalculateTotal() {
        this.totalAmount = orderLines.stream()
            .map(OrderLine::getAmount)
            .reduce(Money.ZERO, Money::add);
    }
}
```

## 4. Validate Boundaries

Validate that your designed boundaries are correct using the following checklists:

### 4.1 Size Checklist

| Item | Healthy Level | Warning | Check |
|------|---------------|---------|-------|
| Number of internal Entities | 1-5 | More than 5 | ☐ |
| Average load time | < 10ms | > 50ms | ☐ |
| Average Aggregate size | < 10KB | > 50KB | ☐ |

### 4.2 Consistency Checklist

- ☐ All immediate consistency rules are within a single Aggregate
- ☐ Changes between Aggregates are synchronized via events
- ☐ External code cannot directly modify internal Entities

### 4.3 Validate with Tests

Aggregates should be independently testable:

```java
@Test
void order_status_changes_on_confirmation() {
    // Given - Testable with Aggregate alone
    Order order = Order.create(
        new OrderId("ORD-001"),
        new CustomerId("CUST-001"),
        List.of(new OrderLine(productId, "Product A", Money.won(10000), 2))
    );

    // When
    order.confirm();

    // Then
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
}
```

## Troubleshooting

### Problem: "Aggregate is too large, causing slow performance"

**Symptoms**: Load time over 50ms, frequent concurrency conflicts

**Solutions**:
1. Apply Question 4 again. Separate data that changes frequently but doesn't need to change together
2. Move history data (History, Log) to separate storage
3. Check if collection sizes grow indefinitely

```java
// Before: OrderHistory keeps growing
public class Order {
    private List<OrderStatusChange> history; // Unlimited growth
}

// After: History separated into separate Aggregate
public class Order {
    private OrderStatus currentStatus;
    // history removed
}

public class OrderAuditLog { // Separate Aggregate
    private OrderId orderId;
    private List<StatusChange> changes;
}
```

---

### Problem: "Consistency between Aggregates is broken"

**Symptoms**: Order is confirmed but stock is not deducted

**Solutions**:
1. Use Domain Events to synchronize changes between Aggregates
2. Implement retry mechanisms for event processing failures

```java
// Order Aggregate
public void confirm() {
    this.status = OrderStatus.CONFIRMED;
    registerEvent(new OrderConfirmedEvent(this.id, this.orderLines));
}

// Stock Aggregate - Event Handler
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

### Problem: "Same Entity is needed in multiple Aggregates"

**Symptoms**: Product information is needed in Order, Cart, and Wishlist

**Solutions**:
1. Keep Product as a separate Aggregate and reference by ID only
2. Store snapshots of necessary information in each Aggregate

```java
// Order holds product info snapshot at order time
public class OrderLine {
    private ProductId productId;          // ID reference
    private String productNameSnapshot;    // Name at order time
    private Money priceSnapshot;           // Price at order time
}

// Cart queries current price for display
public class CartItem {
    private ProductId productId;
    // Query current info from Product Aggregate when displaying
}
```

---

### Problem: "Need to modify multiple Aggregates in a transaction"

**Symptoms**: Must change Order, Stock, and Point simultaneously when confirming order

**Solutions**:
1. Reconsider whether immediate consistency is really needed (eventual consistency is usually sufficient)
2. If immediate consistency is essential, merge those Entities into one Aggregate
3. If eventual consistency is acceptable, separate using events

```java
// ❌ Modifying multiple Aggregates simultaneously
@Transactional
public void confirmOrder(OrderId orderId) {
    order.confirm();
    stock.decrease();  // Different Aggregate
    customer.addPoints();  // Another Aggregate
}

// ✅ Separated using events
@Transactional
public void confirmOrder(OrderId orderId) {
    Order order = orderRepository.findById(orderId);
    order.confirm();  // Publishes OrderConfirmedEvent
    orderRepository.save(order);
}
// Stock and Point are processed in separate transactions via event handlers
```

## Next Steps

- [Aggregate Deep Dive]({{< relref "/docs/ddd/concepts/aggregate" >}}) - More detailed design principles and patterns
- [Domain Events]({{< relref "/docs/ddd/concepts/domain-events" >}}) - Communication methods between Aggregates
- [Order Domain Example]({{< relref "/docs/ddd/examples/order-domain" >}}) - Actual implementation examples
