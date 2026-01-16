---
title: Designing Domain Events
weight: 4
lastmod: "2026-01-16"
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

> **Problem Solved**: Complex transactions due to tight coupling between Aggregates, or difficulty propagating changes
> **Time Required**: ~25 min
> **Prerequisites**: Assumes you have read the [Aggregate Deep Dive]({{< relref "/docs/ddd/concepts/aggregate" >}}) document

{{< callout type="warning" title="Success Criteria" >}}
After completing this guide, you will be able to:
- Identify situations that need domain events
- Apply event naming conventions and payload design
- Implement Spring event publishing/subscribing patterns
{{< /callout >}}

## 1. Identifying Situations That Need Domain Events

### 1.1 Three Signals That Indicate Events Are Needed

Check the following 3 signals to determine if domain events are needed.

**Signal 1: "When X happens, Y must happen" Pattern**

When explaining business rules, record this pattern as event candidates:

```
"When an order is confirmed, inventory must be deducted"
→ OrderConfirmedEvent → StockDecrease

"When payment is completed, points must be accumulated"
→ PaymentCompletedEvent → PointAccumulation

"When delivery is completed, a review request must be sent"
→ DeliveryCompletedEvent → ReviewRequest
```

**Signal 2: Transaction Requiring Multiple Aggregate Modifications**

```java
// ❌ Modifying multiple Aggregates in one transaction
@Transactional
public void confirmOrder(OrderId orderId) {
    Order order = orderRepository.findById(orderId);
    order.confirm();

    Stock stock = stockRepository.findByProductId(productId);
    stock.decrease(quantity);  // Different Aggregate

    Customer customer = customerRepository.findById(customerId);
    customer.addPoints(points);  // Another Aggregate
}
```

**Signal 3: Long Synchronous Call Chains**

```java
// ❌ Synchronous call chain
public void confirmOrder(OrderId orderId) {
    orderService.confirm(orderId);
    stockService.decrease(productId, quantity);  // Sync call
    paymentService.capture(paymentId);           // Sync call
    notificationService.send(customerId);        // Sync call
    pointService.accumulate(customerId, points); // Sync call
}
// If any one fails, everything fails
```

### 1.2 Event Usage Decision Table

| Situation | Use Event? | Reason |
|-----------|-----------|--------|
| State change within same Aggregate | No | Direct method call |
| Notifying other Aggregates | Yes | Reduce coupling |
| External system integration | Yes | Asynchronous processing |
| Audit logging | Yes | Separation of concerns |
| Immediate consistency required | No | Synchronous processing |
| Eventual consistency acceptable | Yes | Asynchronous processing |

## 2. Designing Domain Events

### 2.1 Event Naming Conventions

Follow these 3 rules when naming events.

**Rule 1: Use Past Tense Verbs**

```
✅ OrderConfirmedEvent    (Order was confirmed)
✅ PaymentCompletedEvent  (Payment was completed)
✅ StockDepletedEvent     (Stock was depleted)

❌ OrderConfirmEvent      (Base verb)
❌ ConfirmOrderEvent      (Imperative)
```

**Rule 2: Use Domain Terms**

```
✅ OrderConfirmedEvent    (Domain term: confirmed)
✅ ShipmentDispatchedEvent (Domain term: dispatched)

❌ OrderStatusChangedEvent (Too generic)
❌ OrderUpdatedEvent       (Meaning unclear)
```

**Rule 3: Aggregate + Action + Event**

```
[AggregateName][PastTenseAction]Event

OrderConfirmedEvent      = Order + Confirmed + Event
PaymentCapturedEvent     = Payment + Captured + Event
StockReservedEvent       = Stock + Reserved + Event
CustomerRegisteredEvent  = Customer + Registered + Event
```

### 2.2 Event Payload Design

Include the following required attributes in all events.

**Required Attributes**:

```java
public abstract class DomainEvent {
    private final String eventId;          // Event unique ID
    private final LocalDateTime occurredAt; // Occurrence time
    private final String aggregateType;    // Aggregate type
    private final String aggregateId;      // Aggregate ID

    protected DomainEvent(String aggregateType, String aggregateId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
    }
}
```

**Concrete Event**:

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

    // Immutable DTO for event payload
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

### 2.3 Payload Design Principles

{{< callout type="info" title="Core Principle" >}}
Event payloads should include enough information so subscribers don't need to query again, but don't include the entire Aggregate.
{{< /callout >}}

**Principle 1: Include Only Necessary Information**

```java
// ❌ Including entire Aggregate
public class OrderConfirmedEvent {
    private final Order order;  // Circular reference, serialization issues
}

// ✅ Extract only necessary information
public class OrderConfirmedEvent {
    private final OrderId orderId;
    private final CustomerId customerId;
    private final Money totalAmount;
}
```

**Principle 2: Use Immutable Data**

```java
// ✅ Use immutable Record
public record OrderConfirmedEvent(
    String eventId,
    LocalDateTime occurredAt,
    OrderId orderId,
    CustomerId customerId,
    Money totalAmount,
    List<OrderLineInfo> orderLines
) implements DomainEvent {

    public OrderConfirmedEvent {
        orderLines = List.copyOf(orderLines);  // Defensive copy
    }
}
```

**Principle 3: Include Information Subscribers Need**

```java
// Include enough information so subscribers don't need to query
public class OrderConfirmedEvent extends DomainEvent {
    // Information stock service needs
    private final List<OrderLineInfo> orderLines;

    // Information notification service needs
    private final CustomerId customerId;
    private final String customerEmail;

    // Information billing service needs
    private final Money totalAmount;
}
```

## 3. Implementing Event Publishing

### 3.1 Registering Events in Aggregates

Add event registration capability to the Aggregate Root:

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

        // Register event (not published yet)
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

### 3.2 Publishing Events from Application Service

Publish events after saving in the Application Service:

```java
@Service
@Transactional
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void confirmOrder(OrderId orderId) {
        // 1. Retrieve Aggregate
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 2. Execute business logic (events registered)
        order.confirm();

        // 3. Save
        orderRepository.save(order);

        // 4. Publish events
        order.getDomainEvents().forEach(eventPublisher::publishEvent);
        order.clearDomainEvents();
    }
}
```

### 3.3 Publishing Events After Transaction Commit

{{< callout type="warning" title="Warning" >}}
When using `@TransactionalEventListener`, events are published after transaction commit. If event processing fails, the original transaction is not rolled back.
{{< /callout >}}

To process events after transaction commit, implement as follows:

```java
@Component
public class DomainEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        // Executed after transaction commit
        // Even if event processing fails, order confirmation is not rolled back
    }
}
```

## 4. Implementing Event Subscription

### 4.1 Synchronous Event Handler

Use `@EventListener` for processing within the same transaction:

```java
@Component
public class OrderEventHandler {

    private final StockService stockService;

    @EventListener
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        // Executed in same transaction
        // If failed, order confirmation is also rolled back
        for (OrderLineInfo line : event.orderLines()) {
            stockService.reserve(line.productId(), line.quantity());
        }
    }
}
```

### 4.2 Asynchronous Event Handler

Combine `@Async` and `@TransactionalEventListener` to process in a separate transaction:

```java
@Component
public class NotificationEventHandler {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        // Executed in separate thread
        // Even if failed, order confirmation is maintained
        notificationService.sendOrderConfirmation(
            event.customerId(),
            event.orderId()
        );
    }
}
```

### 4.3 Registering Multiple Handlers

Register multiple handlers for one event. Each handler executes independently:

```java
// Stock processing
@Component
public class StockEventHandler {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserveStock(OrderConfirmedEvent event) {
        // Reserve stock
    }
}

// Point accumulation
@Component
public class PointEventHandler {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void accumulatePoints(OrderConfirmedEvent event) {
        // Accumulate points
    }
}

// Send notification
@Component
public class NotificationEventHandler {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendNotification(OrderConfirmedEvent event) {
        // Send notification
    }
}
```

## 5. Event Failure Handling

{{< callout type="warning" title="Always Handle Event Failures" >}}
Ignoring event handler failures causes data inconsistency. Always implement retry and final failure handling.
{{< /callout >}}

### 5.1 Retry Strategy

Use Spring Retry to handle transient errors:

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
        // Handle final failure
        log.error("Stock deduction final failure. Manual processing required. orderId={}",
            event.orderId(), e);
        alertService.sendAlert("Stock deduction failed", event);
    }
}
```

### 5.2 Outbox Pattern (Reliability Guarantee)

{{< callout type="tip" title="Recommended: Outbox Pattern" >}}
To prevent data loss when event publishing fails, use the Outbox pattern. Save events to DB first, then publish through a separate process.
{{< /callout >}}

Save events to DB before publishing:

```java
// Save events to DB before publishing
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

        // Save events to Outbox table
        order.getDomainEvents().forEach(event -> {
            outboxRepository.save(OutboxEvent.from(event));
        });
        order.clearDomainEvents();
    }
}

// Separate process polls Outbox and publishes
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

## 6. Event Testing

### 6.1 Unit Tests

```java
@Test
void event_is_registered_when_order_is_confirmed() {
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

### 6.2 Integration Tests

```java
@SpringBootTest
class OrderEventIntegrationTest {

    @Autowired
    private OrderCommandService orderCommandService;

    @MockBean
    private StockService stockService;

    @Test
    void stock_is_deducted_when_order_is_confirmed() {
        // given
        OrderId orderId = createTestOrder();

        // when
        orderCommandService.confirmOrder(orderId);

        // then
        verify(stockService, times(1)).decrease(any(), anyInt());
    }
}
```

## Troubleshooting

### Problem: "Events are not being published"

**Things to check**:

1. Verify `registerEvent()` is being called
2. Verify publishing before `clearDomainEvents()` is called
3. When using `@TransactionalEventListener`, verify transaction is committed

```java
// Add logging for debugging
@EventListener
public void logAllEvents(DomainEvent event) {
    log.debug("Event received: {}", event);
}
```

---

### Problem: "Exception in event handler rolls back the original transaction"

**Solution**:

Use `AFTER_COMMIT` and `REQUIRES_NEW`:

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void handleEvent(OrderConfirmedEvent event) {
    // Executed in new transaction separate from original
}
```

---

### Problem: "Event order is not guaranteed"

**Solution**:

Specify order with `@Order` annotation:

```java
@Order(1)
@EventListener
public void handleFirst(OrderConfirmedEvent event) {
    // Executed first
}

@Order(2)
@EventListener
public void handleSecond(OrderConfirmedEvent event) {
    // Executed second
}
```

## Next Steps

- [Domain Events]({{< relref "/docs/ddd/concepts/domain-events" >}}) - Event Sourcing, CQRS
- [CQRS]({{< relref "/docs/ddd/concepts/cqrs" >}}) - Command Query Separation
- [Event Sourcing Example]({{< relref "/docs/ddd/examples/event-sourcing" >}}) - Practical implementation example
