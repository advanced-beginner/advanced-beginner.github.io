---
lastmod: "2026-01-07"
title: Strategic Design
weight: 1
---

# Strategic Design

High-level design for deciding how to divide and integrate complex domains.

## Overview

Strategic design is the process of drawing **"the big picture"**:

```mermaid
flowchart TB
    subgraph Strategic["Strategic Design Components"]
        SUB[Subdomain<br/>Domain Classification]
        UL[Ubiquitous Language<br/>Common Language]
        BC[Bounded Context<br/>Bounded Context]
        CM[Context Mapping<br/>Context Relationships]
    end

    SUB --> BC
    UL --> BC
    BC --> CM
```

| Component | Question | Output |
|-----------|----------|--------|
| **Subdomain** | What is core to the business? | Domain classification |
| **Ubiquitous Language** | What language will we communicate in? | Glossary |
| **Bounded Context** | How do we divide the system? | Context boundaries |
| **Context Mapping** | How do systems integrate? | Integration strategy |

## Subdomain

### Concept

Classify the business domain by **importance and characteristics**.

```mermaid
flowchart TB
    subgraph Domain["E-commerce Domain"]
        subgraph Core["Core Domain"]
            CD1["Order Management"]
            CD2["Pricing Policy"]
            CD3["Promotions"]
        end

        subgraph Supporting["Supporting Domain"]
            SD1["Inventory Management"]
            SD2["Customer Management"]
            SD3["Review Management"]
        end

        subgraph Generic["Generic Domain"]
            GD1["Payment Processing"]
            GD2["Notification Delivery"]
            GD3["Authentication/Authorization"]
        end
    end
```

### Subdomain Types

| Type | Characteristics | Investment | Example |
|------|----------------|------------|---------|
| **Core Domain** | Core business competency | Top priority, best developers | Delivery app's dispatch algorithm |
| **Supporting Domain** | Supports core but not differentiating | Adequate investment | Inventory management, customer management |
| **Generic Domain** | Common to all businesses | Use external solutions | Payment, authentication, email |

### Real-World Example: Coupang

```mermaid
flowchart TB
    subgraph Coupang["Coupang Domain Analysis"]
        subgraph Core["Core Domain 🔴"]
            C1["Rocket Delivery Logistics"]
            C2["Dynamic Pricing"]
            C3["Personalized Recommendations"]
        end

        subgraph Supporting["Supporting Domain 🟡"]
            S1["Product Catalog"]
            S2["Inventory Management"]
            S3["Seller Management"]
            S4["Review System"]
        end

        subgraph Generic["Generic Domain 🟢"]
            G1["Payment (PG)"]
            G2["Notifications (SMS/Push)"]
            G3["Member Authentication (OAuth)"]
        end
    end
```

**Analysis:**
- **Core:** Rocket delivery, pricing algorithm, recommendations → Build in-house, assign best talent
- **Supporting:** Catalog, inventory → Build in-house at practical level
- **Generic:** Payment, notifications → Integrate external services

### Subdomain Identification Guide

```mermaid
flowchart TB
    Q1{Can the business<br/>exist without this?}
    Q2{Does it differentiate<br/>from competitors?}
    Q3{Can a solution be<br/>purchased in the market?}

    Q1 -->|No| CORE["Core Domain"]
    Q1 -->|Yes| Q2

    Q2 -->|Yes| CORE
    Q2 -->|No| Q3

    Q3 -->|Yes| GENERIC["Generic Domain"]
    Q3 -->|No| SUPPORTING["Supporting Domain"]
```

## Ubiquitous Language

### Why is it needed?

Misunderstandings occur when developers and business experts use different terminology.

```mermaid
flowchart LR
    subgraph Problem["❌ Terminology Mismatch Problem"]
        direction TB
        BIZ1["Business: 'Gift Purchase'"]
        DEV1["Developer: 'gift_flag = true'"]
        QA1["QA: 'Gift option check'"]
        DOC1["Documentation: 'giftYn field'"]
    end

    subgraph Solution["✅ Ubiquitous Language"]
        direction TB
        ALL["Everyone: 'Gift Order'"]
    end

    Problem -->|Apply DDD| Solution
```

### How to Write a Glossary

**1. Nouns (Entity, Value Object)**

| Term | Definition | Code | Synonyms/Confusion |
|------|------------|------|-------------------|
| Order | A request created by a customer to purchase products | `Order` | Purchase, order |
| Order Line | Individual product and quantity within an order | `OrderLine` | Order item, detail |
| Shipping Address | Address where products will be delivered | `ShippingAddress` | Delivery address, destination |
| Money | Currency unit including currency and amount | `Money` | Price, cost |

**2. Verbs (Actions, Commands)**

| Term | Definition | Code | Preconditions | Result |
|------|------------|------|---------------|--------|
| Create Order | Creates a new order | `Order.create()` | Valid products, customer | Order created |
| Confirm Order | Changes order to processing status | `order.confirm()` | PENDING status | CONFIRMED status, inventory deducted |
| Cancel Order | Changes order to cancelled status | `order.cancel()` | PENDING/CONFIRMED | CANCELLED status, inventory restored |

**3. Events (Past occurrences)**

| Term | Definition | Code | Follow-up Actions |
|------|------------|------|-------------------|
| Order Created | The fact that a new order was created | `OrderCreatedEvent` | Reserve inventory, send notification |
| Order Confirmed | The fact that an order was confirmed | `OrderConfirmedEvent` | Request payment, start packing |
| Order Cancelled | The fact that an order was cancelled | `OrderCancelledEvent` | Restore inventory, refund |

### Reflecting in Code

```java
// ❌ Technical terms, abbreviations used
public class OrdSvc {
    public void updOrdSts(Long ordId, int sts) {
        OrdEntity ord = ordRepo.findById(ordId);
        ord.setSts(sts);
        ordRepo.save(ord);
    }
}

// ✅ Business terminology used
public class OrderService {
    public void confirmOrder(OrderId orderId) {
        Order order = orderRepository.findById(orderId);
        order.confirm();  // "Confirm the order"
        orderRepository.save(order);
    }

    public void cancelOrder(OrderId orderId, CancellationReason reason) {
        Order order = orderRepository.findById(orderId);
        order.cancel(reason);  // "Cancel the order"
        orderRepository.save(order);
    }
}
```

### Same Language in Tests

```java
@Nested
@DisplayName("Order Confirmation")
class OrderConfirmation {

    @Test
    @DisplayName("Confirming a pending order changes status to CONFIRMED")
    void pendingOrderConfirmationSuccess() {
        // given: When there is a pending order
        Order pendingOrder = createPendingOrder();

        // when: When the order is confirmed
        pendingOrder.confirm();

        // then: Status becomes CONFIRMED
        assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("An already confirmed order cannot be confirmed again")
    void alreadyConfirmedOrderCannotBeReconfirmed() {
        // given: An already confirmed order
        Order confirmedOrder = createConfirmedOrder();

        // when & then: Exception thrown when confirming again
        assertThatThrownBy(() -> confirmedOrder.confirm())
            .isInstanceOf(OrderCannotBeConfirmedException.class)
            .hasMessageContaining("already confirmed");
    }
}
```

## Bounded Context

### Concept

A Bounded Context is **an explicit boundary where a specific domain model applies**.

```mermaid
flowchart TB
    subgraph Ecommerce["E-commerce System"]
        subgraph Sales["Sales Context"]
            direction TB
            SP["Product"]
            SO["Order"]
            SC["Customer"]
        end

        subgraph Inventory["Inventory Context"]
            direction TB
            IP["Product"]
            IS["Stock"]
            IW["Warehouse"]
        end

        subgraph Shipping["Shipping Context"]
            direction TB
            SHP["Shipment"]
            SHD["Delivery"]
            SHA["Address"]
        end

        subgraph Billing["Billing Context"]
            direction TB
            BI["Invoice"]
            BP["Payment"]
            BS["Settlement"]
        end
    end

    Sales -.->|Event| Inventory
    Sales -.->|Event| Shipping
    Sales -.->|Event| Billing
```

### Same Term, Different Meaning (Homonyms)

**"Customer"** has different meanings in each Context:

```java
// Customer in Sales Context
// "Who is ordering?"
public class Customer {
    private CustomerId id;
    private String name;
    private Email email;
    private MembershipGrade grade;  // VIP, Gold, Silver
    private Money availablePoints;

    public Money getDiscount(Order order) {
        return grade.calculateDiscount(order.getTotalAmount());
    }
}

// Customer in Shipping Context (Recipient)
// "Who is receiving?"
public class Recipient {
    private String name;
    private PhoneNumber phone;
    private Address address;
    private DeliveryPreference preference;  // Door, Security desk

    public boolean canReceiveAt(TimeSlot slot) {
        return preference.isAvailable(slot);
    }
}

// Customer in Billing Context (Payer)
// "Who is paying?"
public class Payer {
    private String name;
    private TaxId taxId;
    private BillingAddress billingAddress;
    private List<PaymentMethod> paymentMethods;

    public boolean requiresTaxInvoice() {
        return taxId != null;
    }
}
```

### How to Identify Bounded Contexts

**1. Linguistic Clues**

```
"The customer..." → Which customer? Buyer? Recipient? Payer?
"The product..." → Which product? Sales product? Inventory item? Shipping item?
"The order..." → Which order? Sales order? Shipping instruction? Delivery request?
```

**2. Organizational Clues**

```mermaid
flowchart TB
    subgraph Teams["Team Structure"]
        T1["Sales Team"]
        T2["Logistics Team"]
        T3["Billing Team"]
    end

    subgraph Contexts["Bounded Context"]
        C1["Sales Context"]
        C2["Logistics Context"]
        C3["Billing Context"]
    end

    T1 --> C1
    T2 --> C2
    T3 --> C3
```

**Conway's Law:** "System structure follows organizational structure"

**3. Business Process Clues**

```mermaid
flowchart LR
    subgraph Process["Order Process"]
        P1["Order Receipt"] --> P2["Payment Processing"]
        P2 --> P3["Shipping Instruction"]
        P3 --> P4["Delivery"]
        P4 --> P5["Settlement"]
    end

    P1 -.-> C1["Sales"]
    P2 -.-> C2["Payment"]
    P3 -.-> C3["Inventory"]
    P4 -.-> C4["Shipping"]
    P5 -.-> C5["Billing"]
```

### Context Boundary Decision Checklist

```
✅ Should be grouped in the same Context:
- [ ] Strong transactional consistency is required
- [ ] Same team is responsible
- [ ] Same language (terminology) is used
- [ ] Must be deployed together

❌ Should be separated into different Contexts:
- [ ] Same term is used with different meanings
- [ ] Different teams are responsible
- [ ] Can be changed/deployed independently
- [ ] Eventual consistency is sufficient
```

## Context Mapping

### Concept

Defines relationships and integration methods between Contexts.

```mermaid
flowchart LR
    subgraph Upstream["Upstream (Provider)"]
        U[Product Catalog<br/>Service]
    end

    subgraph Downstream["Downstream (Consumer)"]
        D1[Order Service]
        D2[Inventory Service]
        D3[Search Service]
    end

    U -->|Product info| D1
    U -->|Product info| D2
    U -->|Product info| D3
```

### Integration Patterns in Detail

#### 1. Partnership

Two teams **work closely together** on integration.

```mermaid
flowchart LR
    subgraph TeamA["Order Team"]
        A[Order Context]
    end

    subgraph TeamB["Payment Team"]
        B[Payment Context]
    end

    A <-->|Close collaboration<br/>Joint planning| B
```

**Characteristics:**
- Both teams coordinate on API changes
- Regular integration meetings
- Joint testing

**Suitable for:**
- Different services within the same product team
- Strong dependencies

---

#### 2. Shared Kernel

Two Contexts **share some models**.

```mermaid
flowchart TB
    subgraph A["Order Context"]
        A1[Order Model]
        SK1[Money<br/>Address]
    end

    subgraph B["Payment Context"]
        B1[Payment Model]
        SK2[Money<br/>Address]
    end

    SK1 <-.->|Same module| SK2
```

```java
// shared-kernel module
public record Money(BigDecimal amount, Currency currency) {
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
}

public record Address(String zipCode, String city, String street, String detail) {
    public String fullAddress() {
        return String.format("(%s) %s %s %s", zipCode, city, street, detail);
    }
}
```

**Pros:** Eliminates duplication, consistency
**Cons:** Changes affect both sides, increased coupling

**Suitable for:**
- Truly identical concepts (Money, Address, etc.)
- Stable models that rarely change

---

#### 3. Customer-Supplier

Upstream provides the API, Downstream consumes it.

```mermaid
sequenceDiagram
    participant D as Order Service
    participant U as Product Service

    D->>U: GET /products/{id}
    U-->>D: Product info

    Note over D,U: Upstream leads API design
    Note over D: Downstream implements to match API
```

```java
// Downstream: Product Service Client
@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/products/{id}")
    ProductResponse getProduct(@PathVariable String id);

    @GetMapping("/products")
    List<ProductResponse> getProducts(@RequestParam List<String> ids);
}

// Usage in Downstream
@Service
public class OrderService {
    private final ProductServiceClient productClient;

    public Order createOrder(CreateOrderCommand command) {
        // Query product info from Upstream
        ProductResponse product = productClient.getProduct(command.getProductId());

        // Convert to Downstream model for use
        OrderLine orderLine = OrderLine.create(
            ProductId.of(product.id()),
            product.name(),
            Money.of(product.price()),
            command.getQuantity()
        );

        return Order.create(command.getCustomerId(), List.of(orderLine));
    }
}
```

**Roles:**
- **Upstream:** Provides API, notifies Downstream of changes
- **Downstream:** Consumes API, communicates requirements

---

#### 4. Conformist

Downstream **follows the Upstream model as-is**.

```mermaid
flowchart LR
    subgraph External["External System (unchangeable)"]
        EXT[Legacy ERP]
    end

    subgraph Internal["Our System"]
        INT[Order Service]
    end

    EXT -->|"Accept as-is"| INT
```

**Characteristics:**
- Uses Upstream model without transformation
- Dependent on Upstream changes

**Suitable for:**
- External systems (unchangeable)
- No negotiating power
- Simple integrations

---

#### 5. Anti-Corruption Layer (ACL)

A **translation layer** prevents external models from corrupting internal ones.

```mermaid
flowchart LR
    subgraph External["Legacy/External System"]
        EXT[Legacy API<br/>Complex and inconsistent]
    end

    subgraph ACL["Anti-Corruption Layer"]
        TRANS[Translator<br/>Data conversion]
        ADAPT[Adapter<br/>Interface adaptation]
        FACADE[Facade<br/>Simplification]
    end

    subgraph Domain["Our Domain"]
        DOM[Clean Domain Model]
    end

    EXT -->|Legacy format| ACL
    ACL -->|Domain format| DOM
```

```java
// Legacy system response (unchangeable)
public class LegacyOrderResponse {
    private String ord_no;           // Different naming
    private int sts_cd;              // Magic numbers (0=pending, 1=confirmed, 9=cancelled)
    private String cust_nm;          // Abbreviations
    private long ord_amt;            // Amount as number
    private String dlv_addr1;        // Address1
    private String dlv_addr2;        // Address2
    private String rcv_nm;           // Recipient
    private String rcv_tel;          // Phone
}

// Anti-Corruption Layer: Translator
@Component
public class LegacyOrderTranslator {

    public Order translate(LegacyOrderResponse legacy) {
        return Order.reconstitute(
            OrderId.of(legacy.getOrd_no()),
            translateStatus(legacy.getSts_cd()),
            translateCustomer(legacy),
            translateShippingAddress(legacy),
            Money.won(legacy.getOrd_amt())
        );
    }

    private OrderStatus translateStatus(int statusCode) {
        return switch (statusCode) {
            case 0 -> OrderStatus.PENDING;
            case 1 -> OrderStatus.CONFIRMED;
            case 2 -> OrderStatus.SHIPPED;
            case 3 -> OrderStatus.DELIVERED;
            case 9 -> OrderStatus.CANCELLED;
            default -> throw new UnknownLegacyStatusException(statusCode);
        };
    }

    private ShippingAddress translateShippingAddress(LegacyOrderResponse legacy) {
        return new ShippingAddress(
            extractZipCode(legacy.getDlv_addr1()),
            extractCity(legacy.getDlv_addr1()),
            legacy.getDlv_addr1(),
            legacy.getDlv_addr2(),
            legacy.getRcv_nm(),
            formatPhoneNumber(legacy.getRcv_tel())
        );
    }
}

// Adapter: Repository implementation
@Repository
public class LegacyOrderAdapter implements OrderReader {
    private final LegacyOrderClient legacyClient;
    private final LegacyOrderTranslator translator;

    @Override
    public Optional<Order> findById(OrderId id) {
        try {
            LegacyOrderResponse response = legacyClient.getOrder(id.getValue());
            return Optional.of(translator.translate(response));
        } catch (LegacyNotFoundException e) {
            return Optional.empty();
        }
    }
}
```

**Pros:** Protects internal model, isolated from legacy changes
**Cons:** Additional complexity, performance overhead

---

#### 6. Open Host Service + Published Language

Integration through **standardized API and data formats**.

```mermaid
flowchart TB
    subgraph Provider["Product Service"]
        API["Open Host Service<br/>(REST API)"]
        SCHEMA["Published Language<br/>(JSON Schema)"]
    end

    subgraph Consumers["Multiple Consumers"]
        C1[Order Service]
        C2[Search Service]
        C3[Analytics Service]
        C4[External Partner]
    end

    API --> C1
    API --> C2
    API --> C3
    API --> C4
```

```json
// Published Language: Standardized event schema
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "OrderConfirmedEvent",
  "type": "object",
  "properties": {
    "eventId": { "type": "string", "format": "uuid" },
    "eventType": { "const": "ORDER_CONFIRMED" },
    "occurredAt": { "type": "string", "format": "date-time" },
    "payload": {
      "type": "object",
      "properties": {
        "orderId": { "type": "string" },
        "customerId": { "type": "string" },
        "totalAmount": {
          "type": "object",
          "properties": {
            "amount": { "type": "number" },
            "currency": { "type": "string" }
          }
        },
        "orderLines": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "productId": { "type": "string" },
              "quantity": { "type": "integer" }
            }
          }
        }
      }
    }
  }
}
```

---

#### 7. Separate Ways

**Implement separately** without integration.

```mermaid
flowchart TB
    subgraph A["Context A"]
        A1[Own implementation]
    end

    subgraph B["Context B"]
        B1[Own implementation]
    end

    A1 -.-|No integration| B1
```

**Suitable for:**
- Integration cost > Duplication cost
- Simple functionality
- Different requirements

### Context Map Example: E-commerce

```mermaid
flowchart TB
    subgraph Core["Core Domain"]
        ORDER["Order<br/>Context"]
        PRICE["Pricing Policy<br/>Context"]
    end

    subgraph Supporting["Supporting"]
        CATALOG["Product Catalog<br/>Context"]
        INV["Inventory<br/>Context"]
        SHIP["Shipping<br/>Context"]
        MEMBER["Member<br/>Context"]
    end

    subgraph Generic["Generic"]
        PAY["Payment<br/>(External PG)"]
        NOTI["Notification<br/>(External Service)"]
        AUTH["Authentication<br/>(OAuth)"]
    end

    %% Relationship definitions
    ORDER -->|Customer-Supplier| CATALOG
    ORDER -->|Customer-Supplier| INV
    ORDER -->|Published Language| SHIP
    ORDER -->|ACL| PAY
    ORDER -->|Published Language| NOTI

    PRICE -->|Shared Kernel| ORDER
    MEMBER -->|Customer-Supplier| ORDER
    AUTH -->|Conformist| MEMBER
```

| Relationship | Description |
|--------------|-------------|
| **ORDER → CATALOG** | Query product info when creating order |
| **ORDER → INV** | Check/deduct inventory when confirming order |
| **ORDER → PAY** | External PG integration, protected by ACL |
| **ORDER → SHIP, NOTI** | Loose coupling via events |
| **PRICE ↔ ORDER** | Share pricing calculation logic (Shared Kernel) |

## EventStorming for Strategic Design

### What is EventStorming?

A workshop technique where domain experts and developers **come together** to explore the domain.

```mermaid
flowchart LR
    subgraph Workshop["EventStorming Workshop"]
        E[Domain Events<br/>Orange]
        C[Commands<br/>Blue]
        A[Aggregates<br/>Yellow]
        P[Policies<br/>Purple]
        BC[Bounded Context<br/>Boundary line]
    end

    E --> C --> A --> P --> BC
```

### EventStorming Results

```
┌─────────────────────────────────────────────────────────────────┐
│                          Order Context                          │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐  │
│  │ Create   │ => │ Order    │ => │ Confirm  │ => │ Order    │  │
│  │ Order    │    │          │    │ Order    │    │          │  │
│  │ Requested│    │(Aggregate│    │ Requested│    │(Aggregate│  │
│  │ (Command)│    │          │    │(Command) │    │          │  │
│  └──────────┘    └──────────┘    └──────────┘    └──────────┘  │
│        │              │               │               │         │
│        ▼              ▼               ▼               ▼         │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐  │
│  │ Order    │    │ Check    │    │ Order    │    │ Request  │  │
│  │ Created  │    │ Inventory│    │ Confirmed│    │ Payment  │  │
│  │ (Event)  │    │ (Policy) │    │ (Event)  │    │ (Policy) │  │
│  └──────────┘    └──────────┘    └──────────┘    └──────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Inventory Context                        │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐                  │
│  │ Deduct   │ => │ Stock    │ => │ Inventory│                  │
│  │ Inventory│    │          │    │ Deducted │                  │
│  │ Requested│    │(Aggregate│    │ (Event)  │                  │
│  │ (Command)│    │          │    │          │                  │
│  └──────────┘    └──────────┘    └──────────┘                  │
└─────────────────────────────────────────────────────────────────┘
```

## Next Steps

- [Tactical Design](../tactical-design/) - Entity, Value Object, Aggregate patterns
- [Architecture](../architecture/) - Hexagonal, Clean Architecture
