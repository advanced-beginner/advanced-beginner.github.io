---
lastmod: "2026-01-07"
title: Quick Start
weight: 1
---

# Understanding DDD in 5 Minutes

A quick overview of DDD's core concepts.

## The Problem DDD Solves

### Common Problems in Real Projects

```mermaid
flowchart TB
    subgraph Problems["Common Problems"]
        P1["Developers and planners<br/>speak different languages"]
        P2["Business logic<br/>scattered everywhere"]
        P3["Small changes affect<br/>the entire system"]
        P4["Can't understand business<br/>by reading code"]
    end
```

**Real conversation example:**

```
Planner: "When a customer cancels an order, refund their points"
Developer: "Oh, so I change the status to 9 in the order table,
           then INSERT into the point table with that user_id?"
Planner: "...What? What does status 9 mean?"
```

**This is where DDD comes in** — it bridges this communication gap.

## Core Idea

DDD can be summarized in one sentence:

> **"Reflect the business domain directly in your code"**

```mermaid
flowchart LR
    subgraph Business["Business World"]
        B1[Order]
        B2[Payment]
        B3[Shipping]
    end

    subgraph Code["Code World"]
        C1[Order]
        C2[Payment]
        C3[Shipping]
    end

    B1 -.->|1:1 Mapping| C1
    B2 -.->|1:1 Mapping| C2
    B3 -.->|1:1 Mapping| C3
```

## Before vs After: Real Code Comparison

### Scenario: Order Confirmation

**Business Rules:**
- Only pending orders can be confirmed
- Confirmation deducts inventory
- Confirmation sends notification to customer

### ❌ Traditional Approach: Data-Centric (Transaction Script)

```java
@Service
public class OrderService {

    public void confirmOrder(Long orderId) {
        // 1. Query data
        OrderEntity order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2. Validate status (magic number)
        if (order.getStatus() != 0) {  // What's 0? PENDING?
            throw new RuntimeException("Cannot confirm");
        }

        // 3. Update status
        order.setStatus(1);  // What's 1? CONFIRMED?
        order.setConfirmedAt(LocalDateTime.now());

        // 4. Deduct inventory (should this be here?)
        for (OrderItemEntity item : order.getItems()) {
            ProductEntity product = productRepository.findById(item.getProductId())
                .orElseThrow();
            int newStock = product.getStock() - item.getQuantity();
            if (newStock < 0) {
                throw new RuntimeException("Insufficient stock");
            }
            product.setStock(newStock);
            productRepository.save(product);
        }

        // 5. Notification (should this be here?)
        notificationService.send(order.getUserId(), "Order confirmed");

        orderRepository.save(order);
    }
}
```

**Problems:**

| Problem | Description |
|---------|-------------|
| **Magic numbers** | Unknown meaning of `status = 0, 1` |
| **Anemic model** | Entity is just a data container with getters/setters |
| **Scattered logic** | Validation, inventory, notification mixed in one method |
| **Hard to test** | Unit testing impossible due to DB, external service dependencies |
| **Risk of change** | Status can be directly modified from elsewhere |

### ✅ DDD Approach: Domain-Centric

```java
// Domain Model - Business logic inside the object
public class Order extends AggregateRoot<OrderId> {
    private OrderId id;
    private CustomerId customerId;
    private OrderStatus status;
    private List<OrderLine> orderLines;

    // Business behavior expressed as methods
    public void confirm() {
        // Invariant validation
        if (this.status != OrderStatus.PENDING) {
            throw new OrderCannotBeConfirmedException(
                "Only pending orders can be confirmed. Current status: " + this.status
            );
        }

        // State change
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();

        // Publish domain event (inventory, notification handled by event subscribers)
        registerEvent(new OrderConfirmedEvent(this));
    }

    public Money calculateTotal() {
        return orderLines.stream()
            .map(OrderLine::getAmount)
            .reduce(Money.ZERO, Money::add);
    }
}

// Application Service - Orchestrates flow only
@Service
@Transactional
public class OrderApplicationService {

    public void confirmOrder(OrderId orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.confirm();  // Delegate to domain object

        orderRepository.save(order);
        // Events are automatically published by infrastructure
    }
}

// Event Handlers - Separation of concerns
@Component
public class InventoryEventHandler {
    @EventListener
    public void on(OrderConfirmedEvent event) {
        inventoryService.reserveStock(event.getOrderLines());
    }
}

@Component
public class NotificationEventHandler {
    @EventListener
    public void on(OrderConfirmedEvent event) {
        notificationService.sendOrderConfirmation(event.getCustomerId());
    }
}
```

**Improvements:**

| Improvement | Description |
|-------------|-------------|
| **Clear intent** | `order.confirm()` expresses business intent |
| **Rich model** | Order protects its own invariants |
| **Separation of concerns** | Inventory, notification separated into event handlers |
| **Easy to test** | Order can be unit tested |
| **Safe to change** | Status can only be changed through `confirm()` method |

## Two Levels of Design in DDD

```mermaid
flowchart TB
    subgraph Strategic["Strategic Design"]
        direction LR
        S1[Bounded Context<br/>System Boundaries]
        S2[Context Mapping<br/>System Integration]
        S3[Ubiquitous Language<br/>Common Language]
        S4[Subdomain<br/>Domain Classification]
    end

    subgraph Tactical["Tactical Design"]
        direction LR
        T1[Entity<br/>Identity-based]
        T2[Value Object<br/>Value-based]
        T3[Aggregate<br/>Consistency Boundary]
        T4[Repository<br/>Persistence]
        T5[Domain Service<br/>Domain Logic]
        T6[Domain Event<br/>Event Expression]
    end

    Strategic -->|Concrete| Tactical
```

| Category | Focus | Question | Key Deliverables |
|----------|-------|----------|-----------------|
| **Strategic Design** | Big picture, boundaries | "How do we divide the system?" | Context Map, Glossary |
| **Tactical Design** | Details, patterns | "How do we model the domain?" | Domain Model, Code |

## Key Terms at a Glance

### 1. Bounded Context

The same term can have different meanings depending on context.

```mermaid
flowchart TB
    subgraph Sales["Sales Context"]
        SP["Product<br/>━━━━━━━<br/>• id<br/>• name<br/>• price<br/>• discount"]
    end

    subgraph Inventory["Inventory Context"]
        IP["Product<br/>━━━━━━━<br/>• id<br/>• sku<br/>• quantity<br/>• location"]
    end

    subgraph Shipping["Shipping Context"]
        SHP["Package<br/>━━━━━━━<br/>• id<br/>• weight<br/>• dimension<br/>• fragile"]
    end
```

**The same "product" in each Context:**
- **Sales:** "How much to sell it for" (price, discount)
- **Inventory:** "How many do we have" (quantity, location)
- **Shipping:** "How to ship it" (weight, dimensions)

→ Each Context has its own model

### 2. Aggregate

A cluster of objects that maintain transactional consistency.

```mermaid
flowchart TB
    subgraph OrderAggregate["Order Aggregate"]
        Order["🔷 Order<br/>(Aggregate Root)"]
        OL1["OrderLine 1"]
        OL2["OrderLine 2"]
        ADDR["ShippingAddress"]

        Order --> OL1
        Order --> OL2
        Order --> ADDR
    end

    External["External Code"]
    External -->|"✅ order.addLine()"| Order
    External -.->|"❌ No direct access"| OL1
```

**Rules:**
- External access must go through the **Aggregate Root** (Order)
- **One transaction = One Aggregate** modification
- The Root is responsible for internal consistency

### 3. Ubiquitous Language

Developers and business experts use **the same terminology**.

```mermaid
flowchart LR
    subgraph Before["Before"]
        B1["Planning: 'Order Confirmation'"]
        B2["Dev: 'status = 1'"]
        B3["QA: 'Status Update'"]
    end

    subgraph After["After"]
        A1["Planning: 'Order Confirmation'"]
        A2["Dev: 'order.confirm()'"]
        A3["QA: 'Order Confirmation Test'"]
    end

    Before -->|Apply DDD| After
```

| Business Term | Code | Test |
|--------------|------|------|
| **Create** an order | `Order.create()` | `testOrderCreation()` |
| **Confirm** an order | `order.confirm()` | `testOrderConfirmation()` |
| **Cancel** an order | `order.cancel()` | `testOrderCancellation()` |
| **Change** shipping address | `order.changeShippingAddress()` | `testShippingAddressChange()` |

### 4. Domain Event

Represents significant occurrences in the domain.

```mermaid
sequenceDiagram
    participant O as Order
    participant E as Event Bus
    participant I as Inventory
    participant N as Notification
    participant A as Analytics

    O->>E: Publish OrderConfirmedEvent
    par Parallel Processing
        E->>I: Deduct inventory
    and
        E->>N: Send notification
    and
        E->>A: Update statistics
    end
```

**Event Characteristics:**
- **Past tense naming:** `OrderConfirmed` (was confirmed)
- **Immutable:** Cannot change after publishing
- **Self-contained:** Contains all information needed for processing

## When Should You Apply DDD?

```mermaid
flowchart TB
    Q1{Is the business logic<br/>complex?}
    Q2{Do you need to collaborate<br/>with domain experts?}
    Q3{Will this be maintained<br/>long-term?}

    Q1 -->|Yes| Q2
    Q1 -->|No| SIMPLE["Simple CRUD is sufficient"]

    Q2 -->|Yes| Q3
    Q2 -->|No| LAYER["Layered architecture is sufficient"]

    Q3 -->|Yes| DDD["✅ DDD Recommended"]
    Q3 -->|No| SIMPLE2["Start simple"]
```

### When DDD is Suitable

| Situation | Examples |
|-----------|----------|
| **Complex business rules** | Finance, insurance, logistics, healthcare |
| **Frequent requirement changes** | Startups, new business ventures |
| **Domain experts available** | Collaboration with business stakeholders |
| **Long-running systems** | Expected maintenance of 5+ years |

### When DDD is Overkill

| Situation | Alternative |
|-----------|-------------|
| **Simple CRUD** | Spring Data REST |
| **Prototype** | Prioritize fast implementation |
| **Small team** | Simple layered architecture |
| **Short-lived project** | Pragmatic approach |

## Benefits of DDD Adoption

### Real Case Comparison

```
📊 Before Adoption (Project A)
- New feature development: Average 2 weeks
- Bug fixes: Average 3 days (hard to identify side effects)
- New developer onboarding: 1 month
- Business change response: "We need to rewrite everything"

📊 After Adoption (Project B)
- New feature development: Average 1 week
- Bug fixes: Average 1 day (clear impact scope)
- New developer onboarding: 2 weeks (code serves as documentation)
- Business change response: "Just modify this Aggregate"
```

## Next Steps

Now that you understand the core concepts, let's dive deeper:

**Learning Path:** Quick Start → [Strategic Design](../concepts/strategic-design/) → [Tactical Design](../concepts/tactical-design/) → [Architecture](../concepts/architecture/) → Hands-on Examples

- [Strategic Design](../concepts/strategic-design/) - Bounded Context, Context Mapping, Subdomain
- [Tactical Design](../concepts/tactical-design/) - Entity, Value Object, Aggregate
- [Architecture](../concepts/architecture/) - Hexagonal, Clean Architecture
