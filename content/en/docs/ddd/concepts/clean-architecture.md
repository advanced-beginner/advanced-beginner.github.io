---
title: Clean Architecture
weight: 8
lastmod: "2026-01-13"
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

> **Target Audience**: Developers of large-scale projects requiring strict dependency management
> **Prerequisites**: Port/Adapter concept from [Hexagonal Architecture](hexagonal-architecture/)
> **Reading Time**: About 25 minutes

# Clean Architecture

An architecture proposed by Uncle Bob (Robert C. Martin) in 2012. The core is strictly following the **dependency rule**.

The background for Clean Architecture's emergence is the chronic problem of software development. When business logic is tightly coupled to frameworks or databases, changing tech stacks or writing tests becomes very difficult. For example, if JPA annotations are directly attached to domain objects, testing that domain logic necessarily requires database connections. Clean Architecture aims to **completely isolate business rules from technical details** to solve this problem.

## One-Line Summary

> **Dependencies always point inward**

```mermaid
flowchart TB
    subgraph Outer["Frameworks & Drivers"]
        subgraph Green["Interface Adapters"]
            subgraph Red["Use Cases"]
                subgraph Yellow["Entities"]
                    E["Enterprise<br>Business Rules"]
                end
                UC["Application<br>Business Rules"]
            end
            A["Controllers<br>Gateways<br>Presenters"]
        end
        F["Web, DB, Devices<br>External Interfaces"]
    end

    F --> A --> UC --> E
```

---

## Understanding Through "Concentric Circles"

### Analogy: Onion Layers

When you cut an onion, there are multiple layers:

```mermaid
flowchart TB
    subgraph Onion["Think of it like an onion"]
        L1["Outermost: Skin (disposable)"]
        L2["Middle layers"]
        L3["Inner layers"]
        L4["Core (most precious)"]

        L1 --> L2 --> L3 --> L4
    end
```

Software is the same:

| Layer | Onion Analogy | Software | Change Frequency |
|-------|---------------|----------|------------------|
| **Core** | Innermost | Business Rules | Rarely changes |
| **Inner** | Middle inside | Use Case | Sometimes changes |
| **Middle** | Middle outside | Adapter | Often changes |
| **Outer** | Skin | Framework | Can be swapped anytime |

**Core idea:** Put the most important things (business rules) at the innermost, and less important things (technical details) on the outside.

---

## The Dependency Rule

```
Rule: Inner circles know nothing about outer circles
```

```mermaid
flowchart TB
    subgraph Rule["Dependency Rule"]
        OUT["Outer Layer"]
        IN["Inner Layer"]

        OUT -->|"Can know"| IN
        IN -.->|"Cannot know"| OUT
    end
```

**Why is this important?**

```java
// ❌ Rule violation: Entity knows Framework
public class Order {
    @Entity  // JPA annotation = Framework!
    @Table(name = "orders")
    public void save() {
        // Using something from Spring...
    }
}

// ✅ Rule followed: Entity has only pure business logic
public class Order {
    private OrderId id;
    private Money totalAmount;

    public void confirm() {
        // Pure business logic only
        if (!canBeConfirmed()) {
            throw new IllegalStateException("Cannot be confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
    }
}
```

---

## 4 Layers in Detail

### 1. Entities - Innermost

Where the **core business rules** reside. Rules shared across the entire company.

```
Example: "Orders over 1 million won classify customer as VIP"
    → This is the same rule regardless of which system uses it
```

```java
// Entities Layer
public class Order {
    private OrderId id;
    private CustomerId customerId;
    private List<OrderLine> lines;
    private OrderStatus status;
    private Money totalAmount;

    // Core business rules
    public void confirm() {
        validateCanConfirm();
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        validateCanCancel();
        this.status = OrderStatus.CANCELLED;
    }

    public boolean isVipOrder() {
        return totalAmount.isGreaterThan(Money.of(1_000_000));
    }

    private void validateCanConfirm() {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Can only confirm from PENDING state");
        }
        if (lines.isEmpty()) {
            throw new EmptyOrderException("No order items");
        }
    }
}
```

```java
// Value Object (included in Entities Layer)
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be 0 or greater");
        }
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

{{< notice style="tip" >}}
**Entity Characteristics**
- No dependency on any framework
- Pure Java object (POJO)
- Business rules expressed as methods
- Easiest to test
{{< /notice >}}

---

### 2. Use Cases

Where the **application business rules** reside. Defines how things work "in this system."

```
Example: "For web orders: check inventory → payment → confirm order → notification"
    → This is a flow specific to our system
```

```java
// Use Case Interface (Input Port)
public interface CreateOrderUseCase {
    CreateOrderOutput execute(CreateOrderInput input);
}

// Input (Request Model)
public record CreateOrderInput(
    String customerId,
    List<OrderLineInput> lines
) {}

// Output (Response Model)
public record CreateOrderOutput(
    String orderId,
    String status,
    BigDecimal totalAmount
) {}
```

```java
// Use Case Implementation (Interactor)
public class CreateOrderInteractor implements CreateOrderUseCase {

    // Output Ports (connection to external via interfaces)
    private final OrderRepository orderRepository;
    private final InventoryGateway inventoryGateway;
    private final PaymentGateway paymentGateway;

    @Override
    public CreateOrderOutput execute(CreateOrderInput input) {
        // 1. Check inventory
        for (OrderLineInput line : input.lines()) {
            if (!inventoryGateway.checkAvailability(line.productId(), line.quantity())) {
                throw new InsufficientInventoryException(line.productId());
            }
        }

        // 2. Create Entity (core business logic is in Entity)
        Order order = Order.create(
            CustomerId.of(input.customerId()),
            toOrderLines(input.lines())
        );

        // 3. Save
        orderRepository.save(order);

        // 4. Create Output
        return new CreateOrderOutput(
            order.getId().getValue(),
            order.getStatus().name(),
            order.getTotalAmount().getAmount()
        );
    }
}
```

{{< notice style="warning" >}}
**Use Case Rules**

1. **Use only Entities** - Use Case calls Entity, and Entity performs the actual business logic
2. **Use Gateway interfaces** - External systems are abstracted through interfaces
3. **Input/Output objects** - Define objects for data exchange with external
{{< /notice >}}

---

### 3. Interface Adapters

Responsible for **data conversion**. Use Case desired format ↔ External system desired format.

```mermaid
flowchart LR
    subgraph External["External Format"]
        HTTP["HTTP Request"]
        DB["DB Row"]
    end

    subgraph Adapters["Interface Adapters"]
        CTRL["Controller<br>(request conversion)"]
        REPO["Repository<br>(data conversion)"]
    end

    subgraph UseCase["Use Case Format"]
        IN["Input"]
        OUT["Output"]
    end

    HTTP --> CTRL --> IN
    OUT --> CTRL --> HTTP
    DB <--> REPO <--> UseCase
```

**Three types of Adapters:**

| Adapter | Role | Example |
|---------|------|---------|
| **Controller** | HTTP Request → Use Case Input | REST Controller |
| **Presenter** | Use Case Output → HTTP Response | View Model creation |
| **Gateway** | Use Case request → External system | Repository implementation |

```java
// Controller (HTTP → Use Case)
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody CreateOrderRequest request) {

        // 1. HTTP Request → Use Case Input conversion
        CreateOrderInput input = new CreateOrderInput(
            request.customerId(),
            request.items().stream()
                .map(this::toOrderLineInput)
                .toList()
        );

        // 2. Execute Use Case
        CreateOrderOutput output = createOrderUseCase.execute(input);

        // 3. Use Case Output → HTTP Response conversion
        OrderResponse response = new OrderResponse(
            output.orderId(),
            output.status(),
            output.totalAmount()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

// Gateway Implementation (Repository)
@Repository
public class JpaOrderRepository implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderDataMapper mapper;

    @Override
    public void save(Order order) {
        // Domain Entity → JPA Entity conversion
        JpaOrderEntity entity = mapper.toJpaEntity(order);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        // JPA Entity → Domain Entity conversion
        return jpaRepository.findById(id.getValue())
            .map(mapper::toDomain);
    }
}
```

---

### 4. Frameworks & Drivers - Outermost

Where the **external tools** reside. Can be swapped anytime.

```mermaid
flowchart TB
    subgraph FD["Frameworks & Drivers"]
        WEB["Spring MVC"]
        ORM["JPA/Hibernate"]
        MSG["Kafka Client"]
        HTTP["RestTemplate"]
        UI["React/Vue"]
    end
```

```java
// Framework Layer: Spring configuration
@Configuration
public class OrderConfig {

    @Bean
    public CreateOrderUseCase createOrderUseCase(
            OrderRepository orderRepository,
            InventoryGateway inventoryGateway) {
        return new CreateOrderInteractor(orderRepository, inventoryGateway);
    }
}

// Framework Layer: JPA Entity
@Entity
@Table(name = "orders")
public class JpaOrderEntity {
    @Id
    private String id;
    private String customerId;
    private String status;
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<JpaOrderLineEntity> lines;
}

// Framework Layer: Spring Data Repository
public interface OrderJpaRepository extends JpaRepository<JpaOrderEntity, String> {
}
```

{{< notice style="tip" >}}
**Framework Layer Role**
- Configuration of external tools like Spring, JPA, Kafka
- Dependency injection configuration (Bean registration)
- Only handles technical details
{{< /notice >}}

---

## Package Structure

```
com.example.order/
│
├── entity/                          # Entities (innermost)
│   ├── Order.java
│   ├── OrderLine.java
│   ├── OrderId.java
│   ├── OrderStatus.java
│   └── Money.java
│
├── usecase/                         # Use Cases
│   ├── port/
│   │   ├── in/                      # Input Ports (Use Case Interfaces)
│   │   │   ├── CreateOrderUseCase.java
│   │   │   ├── CreateOrderInput.java
│   │   │   └── CreateOrderOutput.java
│   │   └── out/                     # Output Ports (Gateway Interfaces)
│   │       ├── OrderRepository.java
│   │       ├── InventoryGateway.java
│   │       └── PaymentGateway.java
│   └── interactor/                  # Use Case Implementations
│       ├── CreateOrderInteractor.java
│       └── ConfirmOrderInteractor.java
│
├── adapter/                         # Interface Adapters
│   ├── controller/
│   │   ├── OrderController.java
│   │   ├── CreateOrderRequest.java
│   │   └── OrderResponse.java
│   ├── presenter/
│   │   └── OrderPresenter.java
│   └── gateway/
│       ├── persistence/
│       │   ├── JpaOrderRepository.java
│       │   └── OrderDataMapper.java
│       └── external/
│           └── InventoryGatewayImpl.java
│
└── framework/                       # Frameworks & Drivers (outermost)
    ├── config/
    │   └── OrderConfig.java
    └── persistence/
        ├── JpaOrderEntity.java
        └── OrderJpaRepository.java
```

---

## Detailed Dependency Flow

### Compile-time Dependencies

```mermaid
flowchart TB
    FW["framework"]
    AD["adapter"]
    UC["usecase"]
    EN["entity"]

    FW --> AD --> UC --> EN

    FW -.->|"Forbidden"| UC
    FW -.->|"Forbidden"| EN
    AD -.->|"Forbidden"| EN
```

### Runtime Flow (Actual Call Order)

```mermaid
sequenceDiagram
    participant HTTP as HTTP Request
    participant CTRL as Controller
    participant UC as Use Case
    participant ENT as Entity
    participant GW as Gateway
    participant DB as Database

    HTTP->>CTRL: POST /orders
    CTRL->>UC: execute(input)
    UC->>ENT: Order.create()
    ENT-->>UC: order
    UC->>GW: save(order)
    GW->>DB: INSERT
    DB-->>GW: OK
    GW-->>UC: void
    UC-->>CTRL: output
    CTRL-->>HTTP: 201 Created
```

---

## Difference Between Hexagonal and Clean

Both have similar goals, but different perspectives:

| Aspect | Hexagonal | Clean |
|--------|-----------|-------|
| **Shape** | Hexagon (in/out) | Concentric circles (layers) |
| **Emphasis** | Port/Adapter separation | Dependency rule |
| **Use Case** | Application Service | Interactor |
| **External connection** | Port | Gateway Interface |
| **Naming convention** | ~Port, ~Adapter | ~UseCase, ~Gateway |
| **Number of layers** | 3 (Adapter, Port, Core) | 4 (FW, Adapter, UC, Entity) |

```mermaid
flowchart LR
    subgraph Hex["Hexagonal"]
        H1["Adapter"]
        H2["Port"]
        H3["Core"]
        H1 --> H2 --> H3
    end

    subgraph Clean["Clean"]
        C1["Framework"]
        C2["Adapter"]
        C3["Use Case"]
        C4["Entity"]
        C1 --> C2 --> C3 --> C4
    end

    Hex -.->|"Practically the same"| Clean
```

---

## Common Mistakes

### 1. Framework Code in Entity

```java
// ❌ Wrong: Entity depends on JPA
@Entity  // JPA annotation!
public class Order {
    @Id
    @GeneratedValue
    private Long id;

    @Transactional  // Spring annotation!
    public void confirm() { ... }
}

// ✅ Correct: Pure Entity
public class Order {
    private OrderId id;
    private OrderStatus status;

    public void confirm() {
        // Pure business logic only
    }
}
```

### 2. Use Case Knows HTTP

```java
// ❌ Wrong: Use Case uses HTTP objects
public class CreateOrderInteractor {
    public ResponseEntity<?> execute(HttpServletRequest request) {
        // Use Case should not know HTTP!
    }
}

// ✅ Correct: Pure Input/Output
public class CreateOrderInteractor {
    public CreateOrderOutput execute(CreateOrderInput input) {
        // Pure objects unrelated to HTTP
    }
}
```

### 3. Interactor Directly Calls Presenter

```java
// ❌ Wrong: Interactor depends on Presenter
public class CreateOrderInteractor {
    private final OrderPresenter presenter;  // Depends on Adapter!

    public void execute(CreateOrderInput input) {
        Order order = ...;
        presenter.present(order);  // Not allowed!
    }
}

// ✅ Correct: Return Output object
public class CreateOrderInteractor {
    public CreateOrderOutput execute(CreateOrderInput input) {
        Order order = ...;
        return new CreateOrderOutput(order.getId(), ...);  // Return Output
    }
}
```

### 4. Using Gateway Implementation Directly in Use Case

```java
// ❌ Wrong: Depends on concrete class
public class CreateOrderInteractor {
    private final JpaOrderRepository repository;  // Concrete class!
}

// ✅ Correct: Depends on interface
public class CreateOrderInteractor {
    private final OrderRepository repository;  // Interface!
}
```

---

## Testing Strategy

### Testing by Layer

```mermaid
flowchart TB
    subgraph Tests["Test Types"]
        E2E["E2E Test"]
        INT["Integration Test"]
        UNIT["Unit Test"]
    end

    subgraph Layers["Applied Layers"]
        FW["Framework"]
        AD["Adapter"]
        UC["Use Case"]
        EN["Entity"]
    end

    E2E --> FW
    INT --> AD
    UNIT --> UC
    UNIT --> EN
```

### 1. Entity Tests (Most Pure)

```java
class OrderTest {

    @Test
    void order_confirm_success() {
        Order order = Order.create(
            CustomerId.of("c1"),
            List.of(new OrderLine(ProductId.of("p1"), 2, Money.of(10000)))
        );

        order.confirm();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void vip_order_determination() {
        Order order = createOrderWithTotal(Money.of(1_500_000));

        assertThat(order.isVipOrder()).isTrue();
    }
}
```

### 2. Use Case Tests (Mock Gateway)

```java
@ExtendWith(MockitoExtension.class)
class CreateOrderInteractorTest {

    @Mock private OrderRepository orderRepository;
    @Mock private InventoryGateway inventoryGateway;

    private CreateOrderInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new CreateOrderInteractor(orderRepository, inventoryGateway);
    }

    @Test
    void create_order_success() {
        // Given
        when(inventoryGateway.checkAvailability(any(), anyInt())).thenReturn(true);

        CreateOrderInput input = new CreateOrderInput(
            "customer-1",
            List.of(new OrderLineInput("product-1", 2, 10000))
        );

        // When
        CreateOrderOutput output = interactor.execute(input);

        // Then
        verify(orderRepository).save(any(Order.class));
        assertThat(output.orderId()).isNotNull();
        assertThat(output.status()).isEqualTo("PENDING");
    }

    @Test
    void insufficient_inventory_throws_exception() {
        when(inventoryGateway.checkAvailability(any(), anyInt())).thenReturn(false);

        CreateOrderInput input = new CreateOrderInput(
            "customer-1",
            List.of(new OrderLineInput("product-1", 100, 10000))
        );

        assertThrows(InsufficientInventoryException.class,
            () -> interactor.execute(input));
    }
}
```

### 3. Adapter Tests (Integration)

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CreateOrderUseCase createOrderUseCase;

    @Test
    void create_order_api() throws Exception {
        when(createOrderUseCase.execute(any()))
            .thenReturn(new CreateOrderOutput("order-123", "PENDING", BigDecimal.valueOf(20000)));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "customerId": "c1",
                        "items": [{"productId": "p1", "quantity": 2, "price": 10000}]
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").value("order-123"));
    }
}
```

---

## When to Use Clean Architecture?

### Suitable Cases

- Large-scale projects requiring long-term maintenance
- When multiple teams are collaborating (need clear boundaries)
- When business logic is complex
- When test coverage is important
- When tech stack changes are possible

### Unsuitable Cases

- Small, short-term projects → over-engineering
- Simple CRUD applications
- When team is unfamiliar with architecture → start with [Layered](layered-architecture/)
- MVP, prototype development

---

## File Count Comparison

Difference in file count when implementing the same feature:

```
Feature: Order creation API

=== Layered (5 files) ===
├── OrderController.java
├── OrderService.java
├── OrderRepository.java
├── Order.java
└── OrderDto.java

=== Clean (12+ files) ===
├── entity/
│   └── Order.java
├── usecase/
│   ├── port/in/
│   │   ├── CreateOrderUseCase.java
│   │   ├── CreateOrderInput.java
│   │   └── CreateOrderOutput.java
│   ├── port/out/
│   │   └── OrderRepository.java
│   └── interactor/
│       └── CreateOrderInteractor.java
├── adapter/
│   ├── controller/
│   │   ├── OrderController.java
│   │   └── OrderRequest.java
│   └── gateway/
│       └── JpaOrderRepository.java
└── framework/
    └── JpaOrderEntity.java
```

**What you gain from more files:**
- Clear responsibility for each file
- Easier to test
- Smaller impact scope for changes

---

## Gradual Adoption

No need to convert everything to Clean Architecture at once:

```mermaid
flowchart LR
    A["Stage 1<br>Separate Entity"]
    B["Stage 2<br>Extract Use Case"]
    C["Stage 3<br>Gateway Interface"]
    D["Stage 4<br>Full Application"]

    A --> B --> C --> D
```

### Stage 1: Separate Entity

```java
// Move business logic from Service to Entity
// Before
public class OrderService {
    public void confirm(Order order) {
        if (order.getStatus().equals("PENDING")) {
            order.setStatus("CONFIRMED");
        }
    }
}

// After
public class Order {
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException();
        }
        this.status = OrderStatus.CONFIRMED;
    }
}
```

### Stage 2: Extract Use Case

```java
// Abstract Service into Use Case interface
public interface ConfirmOrderUseCase {
    void execute(OrderId orderId);
}

public class ConfirmOrderInteractor implements ConfirmOrderUseCase {
    // ...
}
```

### Stage 3: Gateway Interface

```java
// Repository as Gateway interface
public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(OrderId id);
}
```

---

## Next Steps

- [Onion Architecture](onion-architecture/) - Domain model centric
- [Hexagonal Architecture](hexagonal-architecture/) - Port and Adapter perspective
- [CQRS](cqrs/) - Read/Write separation
