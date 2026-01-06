---
lastmod: "2026-01-06"
title: Order System
weight: 3
---

# Order System Example

Implementing an event-driven order system closer to real-world applications.

## System Architecture

```mermaid
flowchart TB
    subgraph Client["Client"]
        API[REST API Call]
    end

    subgraph OrderService["Order Service"]
        CTRL[OrderController]
        PROD[OrderProducer]
    end

    subgraph Kafka["Kafka"]
        TOPIC[order-events Topic]
    end

    subgraph Consumers["Event Processing"]
        CONS[OrderConsumer]
        LOGIC[Business Logic]
    end

    API --> CTRL
    CTRL --> PROD
    PROD -->|Publish event| TOPIC
    TOPIC -->|Receive event| CONS
    CONS --> LOGIC
```

## Event Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant A as API
    participant P as Producer
    participant K as Kafka
    participant O as Consumer

    C->>A: POST /api/orders
    A->>P: OrderEvent.created()
    P->>K: publish(orderId, event)
    A-->>C: {"orderId": "abc123"}

    K->>O: Deliver event
    O->>O: handleOrderCreated()

    C->>A: POST /orders/abc123/pay
    A->>P: OrderEvent.paid()
    P->>K: publish(orderId, event)

    K->>O: Deliver event
    O->>O: handleOrderPaid()
```

## Event Types

### OrderEvent

```java
public record OrderEvent(
    String orderId,
    String customerId,
    OrderStatus status,
    String description,
    LocalDateTime timestamp
) {}
```

### OrderStatus

| Status | Description | Next Status |
|--------|-------------|-------------|
| `CREATED` | Order created | PAID, CANCELLED |
| `PAID` | Payment complete | SHIPPED, CANCELLED |
| `SHIPPED` | Shipping started | DELIVERED |
| `DELIVERED` | Delivery complete | - |
| `CANCELLED` | Order cancelled | - |

```mermaid
stateDiagram-v2
    [*] --> CREATED
    CREATED --> PAID: Payment
    CREATED --> CANCELLED: Cancel
    PAID --> SHIPPED: Start shipping
    PAID --> CANCELLED: Cancel
    SHIPPED --> DELIVERED: Delivery complete
    DELIVERED --> [*]
    CANCELLED --> [*]
```

## Message Key Usage

### Why use orderId as Key?

```java
kafkaTemplate.send(TOPIC, event.orderId(), event);
//                 topic  key           value
```

Because **ordering** is required.

```mermaid
flowchart TB
    subgraph WithKey["Using Key (orderId)"]
        E1["CREATED"] --> P0["Partition 0"]
        E2["PAID"] --> P0
        E3["SHIPPED"] --> P0
        P0 --> C1["Consumer"]
        C1 --> OK["Processed in order ✓"]
    end

    subgraph WithoutKey["No Key"]
        E4["CREATED"] --> P1["Partition 0"]
        E5["PAID"] --> P2["Partition 1"]
        E6["SHIPPED"] --> P3["Partition 2"]
        P1 --> C2["Consumer 1"]
        P2 --> C3["Consumer 2"]
        P3 --> C4["Consumer 3"]
        C2 --> ERR["Order mixed up ✗"]
        C3 --> ERR
        C4 --> ERR
    end
```

### Event Order for Same Order

```
Order "abc123":
  Partition 2: [CREATED] → [PAID] → [SHIPPED] → [DELIVERED]
                   ↓          ↓         ↓            ↓
  Consumer:     Process 1  Process 2  Process 3   Process 4
                (Order guaranteed)
```

## Producer Implementation

```java
@Component
public class OrderProducer {

    private static final String TOPIC = "order-events";
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publish(OrderEvent event) {
        // Use orderId as Key for ordering
        kafkaTemplate.send(TOPIC, event.orderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Publish success - Partition: {}, Offset: {}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Publish failed", ex);
                    }
                });
    }
}
```

## Consumer Implementation

```java
@Component
public class OrderConsumer {

    @KafkaListener(topics = "order-events", groupId = "order-processor")
    public void consume(ConsumerRecord<String, OrderEvent> record) {
        OrderEvent event = record.value();

        // Events for same order arrive in order via Key(orderId)
        log.info("Received - OrderId: {}, Status: {}",
                record.key(), event.status());

        switch (event.status()) {
            case CREATED -> handleOrderCreated(event);
            case PAID -> handleOrderPaid(event);
            case SHIPPED -> handleOrderShipped(event);
            case DELIVERED -> handleOrderDelivered(event);
            case CANCELLED -> handleOrderCancelled(event);
        }
    }
}
```

## Running the Example

### 1. Start Kafka

```bash
cd docker
docker-compose up -d
```

### 2. Run Application

```bash
cd examples/order-system
./gradlew bootRun
```

### 3. Create Order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": "customer-123"}'
```

Response:
```json
{"orderId": "abc12345", "message": "Order has been created"}
```

### 4. Process Order

```bash
# Payment
curl -X POST "http://localhost:8080/api/orders/abc12345/pay?customerId=customer-123"

# Ship
curl -X POST "http://localhost:8080/api/orders/abc12345/ship?customerId=customer-123"

# Deliver
curl -X POST "http://localhost:8080/api/orders/abc12345/deliver?customerId=customer-123"
```

### 5. Check Logs

```
========================================
Event Received
  Partition: 0, Offset: 0
  Key (OrderId): abc12345
  Status: CREATED
========================================
[Processing] Order created - Checking inventory and awaiting payment

========================================
Event Received
  Partition: 0, Offset: 1
  Key (OrderId): abc12345
  Status: PAID
========================================
[Processing] Payment complete - Starting shipping preparation
```

## Extension Points

### Multiple Consumer Groups

```mermaid
flowchart TB
    TOPIC[order-events]

    subgraph Group1["order-processor"]
        C1[Order Processing]
    end

    subgraph Group2["notification-service"]
        C2[Send Notifications]
    end

    subgraph Group3["analytics-service"]
        C3[Analytics/Statistics]
    end

    TOPIC --> Group1
    TOPIC --> Group2
    TOPIC --> Group3
```

Each service processes the same events independently.

### Adding Error Handling

```java
@RetryableTopic(attempts = "3")
@KafkaListener(topics = "order-events")
public void consume(OrderEvent event) {
    // After 3 retry failures, moves to DLT
}
```

## Summary

| Pattern | Implementation |
|---------|----------------|
| **Event Publishing** | KafkaTemplate + JSON Serializer |
| **Event Consumption** | @KafkaListener + JSON Deserializer |
| **Ordering Guarantee** | Message Key (orderId) |
| **State Transition** | Event-based state machine |

## Full Source Code

The complete source code for this example is available at the link below. Check out the full implementation of Producer, Consumer, Controller, and domain objects.

- [**`examples/order-system`**](../../../../examples/order-system/)

## Next Steps

- [Appendix](../../appendix/) - Glossary and references
