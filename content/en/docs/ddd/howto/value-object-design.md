---
title: Designing Value Objects
description: "How to design and implement Value Objects"
weight: 3
lastmod: "2026-01-16"
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

> **Problem Solved**: High complexity from making everything an Entity, or low type safety due to Primitive Obsession
> **Time Required**: ~20 min
> **Prerequisites**: Assumes you have read the [Tactical Design]({{< relref "/docs/ddd/concepts/tactical-design" >}}) document

{{< callout type="warning" title="Success Criteria" >}}
After completing this guide, you will be able to:
- Distinguish Entity from Value Object using 3 questions
- Implement Value Objects using Java Records
- Refactor Primitive Obsession to Value Objects
{{< /callout >}}

## 1. Distinguishing Entity vs Value Object

### 1.1 Three Decision Questions

Apply the following questions in order:

**Question 1: "Does this need a unique identifier?"**

```
Order → Identified by order number → Entity
Money → 10,000 won is just 10,000 won → Value Object
```

**Question 2: "Are they the same if they have the same attribute values?"**

```
Two 10,000 won amounts → Interchangeable → Value Object
Order ORD-001 and ORD-002 → Different orders → Entity
```

**Question 3: "Do I need to track identity over time?"**

```
Customer's shipping address change history → No tracking needed → Value Object
Customer's order history → Tracking needed → Entity
```

### 1.2 Decision Matrix

| Subject | Q1 Identifier? | Q2 Value Equality? | Q3 Track? | Conclusion |
|---------|---------------|-------------------|-----------|------------|
| Order | Yes | No | Yes | Entity |
| Money | No | Yes | No | Value Object |
| Address | No | Yes | No | Value Object |
| Date Range | No | Yes | No | Value Object |
| Product | Yes | No | Yes | Entity |
| Phone Number | No | Yes | No | Value Object |

### 1.3 Things That Should Be Value Objects

{{< callout type="tip" title="Tip" >}}
Almost always make the following items as Value Objects. If left as primitive types, validation and operation logic will be scattered everywhere.
{{< /callout >}}

The following should almost always be Value Objects:

```
├── Money
├── Address
├── DateRange
├── Coordinates
├── Email
├── PhoneNumber
├── ZipCode
├── Color
├── Quantity
└── Identifiers (OrderId, CustomerId) ← Entity IDs are also Value Objects!
```

## 2. Implementing Value Objects

### 2.1 Using Java Records

Use Java 16+ Records to implement Value Objects concisely:

```java
// Basic Value Object
public record Money(BigDecimal amount, Currency currency) {

    // Validation with Compact Constructor
    public Money {
        Objects.requireNonNull(amount, "Amount is required");
        Objects.requireNonNull(currency, "Currency is required");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be 0 or greater");
        }
    }

    // Factory method
    public static Money usd(double amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.USD);
    }

    public static final Money ZERO = new Money(BigDecimal.ZERO, Currency.USD);

    // Immutable operation - returns new object
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int quantity) {
        return new Money(
            this.amount.multiply(BigDecimal.valueOf(quantity)),
            this.currency
        );
    }

    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new CurrencyMismatchException(this.currency, other.currency);
        }
    }
}
```

### 2.2 Composite Value Objects

Create composite Value Objects when you need to group multiple values:

```java
public record Address(
    String zipCode,
    String city,
    String street,
    String detail
) {
    public Address {
        Objects.requireNonNull(zipCode, "Zip code is required");
        Objects.requireNonNull(city, "City is required");
        Objects.requireNonNull(street, "Street is required");

        if (!zipCode.matches("\\d{5}")) {
            throw new InvalidAddressException("Zip code must be 5 digits");
        }
    }

    public String fullAddress() {
        String detailPart = detail != null ? " " + detail : "";
        return String.format("(%s) %s %s%s", zipCode, city, street, detailPart);
    }

    // Create new address (change detail only)
    public Address withDetail(String newDetail) {
        return new Address(this.zipCode, this.city, this.street, newDetail);
    }
}
```

### 2.3 Range Value Objects

Create range Value Objects for values with start and end:

```java
public record DateRange(LocalDate startDate, LocalDate endDate) {

    public DateRange {
        Objects.requireNonNull(startDate, "Start date is required");
        Objects.requireNonNull(endDate, "End date is required");

        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException(
                "Start date cannot be after end date: " + startDate + " > " + endDate
            );
        }
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public boolean overlaps(DateRange other) {
        return !this.endDate.isBefore(other.startDate)
            && !this.startDate.isAfter(other.endDate);
    }

    public long days() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
```

### 2.4 Identifier Value Objects

Implement Entity IDs as Value Objects. Use dedicated types instead of String or Long to increase type safety:

```java
public record OrderId(String value) {

    private static final String PREFIX = "ORD-";

    public OrderId {
        Objects.requireNonNull(value, "OrderId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("OrderId cannot be blank");
        }
    }

    public static OrderId generate() {
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new OrderId(PREFIX + uuid);
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
```

## 3. Refactoring Primitive Obsession

### 3.1 Identifying the Problem

{{< callout type="warning" title="Primitive Obsession Warning" >}}
Using primitive types directly causes validation to be omitted and type errors to be discovered only at runtime. Find the problems in the following code.
{{< /callout >}}

Find Primitive Obsession in the following code:

```java
// ❌ Code with Primitive Obsession
public class Order {
    private String orderId;           // String → OrderId
    private String customerId;        // String → CustomerId
    private int totalAmount;          // int → Money
    private String currency;          // Separate field → Include in Money
    private String shippingZipCode;   // String → Address
    private String shippingCity;      // String → Address
    private String shippingStreet;    // String → Address
    private String customerEmail;     // String → Email
    private String customerPhone;     // String → PhoneNumber
}
```

### 3.2 Step-by-Step Refactoring

**Step 1: Create Value Object Classes**

```java
public record Email(String value) {
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public Email {
        Objects.requireNonNull(value, "Email is required");
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException("Invalid email format: " + value);
        }
    }

    public String domain() {
        return value.substring(value.indexOf('@') + 1);
    }
}

public record PhoneNumber(String value) {
    public PhoneNumber {
        Objects.requireNonNull(value, "Phone number is required");
        String cleaned = value.replaceAll("[^0-9]", "");
        if (cleaned.length() < 10 || cleaned.length() > 11) {
            throw new InvalidPhoneNumberException("Invalid phone number: " + value);
        }
    }

    public String formatted() {
        String cleaned = value.replaceAll("[^0-9]", "");
        if (cleaned.length() == 11) {
            return cleaned.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        }
        return cleaned.replaceFirst("(\\d{2,3})(\\d{3,4})(\\d{4})", "$1-$2-$3");
    }
}
```

**Step 2: Apply to Entity**

```java
// ✅ After applying Value Objects
public class Order {
    private final OrderId id;
    private final CustomerId customerId;
    private Money totalAmount;
    private Address shippingAddress;
    private final Email customerEmail;
    private final PhoneNumber customerPhone;

    // Type-safe: invalid values cannot enter
    public Order(
        OrderId id,
        CustomerId customerId,
        Money totalAmount,
        Address shippingAddress,
        Email customerEmail,
        PhoneNumber customerPhone
    ) {
        this.id = Objects.requireNonNull(id);
        this.customerId = Objects.requireNonNull(customerId);
        this.totalAmount = Objects.requireNonNull(totalAmount);
        this.shippingAddress = Objects.requireNonNull(shippingAddress);
        this.customerEmail = Objects.requireNonNull(customerEmail);
        this.customerPhone = Objects.requireNonNull(customerPhone);
    }
}
```

**Step 3: Update Usage Code**

```java
// Before
Order order = new Order();
order.setOrderId("ORD-001");
order.setTotalAmount(10000);
order.setCurrency("USD");
order.setCustomerEmail("test@example.com");

// After
Order order = new Order(
    OrderId.of("ORD-001"),
    CustomerId.of("CUST-001"),
    Money.usd(100.00),
    new Address("12345", "New York", "5th Avenue", "Suite 101"),
    new Email("test@example.com"),
    new PhoneNumber("212-555-1234")
);
```

### 3.3 Refactoring Checklist

| Primitive Type | Convert to Value Object | Added Functionality |
|----------------|------------------------|---------------------|
| `String orderId` | `OrderId` | Format validation, auto-generation |
| `int amount` | `Money` | Currency handling, operations, comparisons |
| `String email` | `Email` | Format validation, domain extraction |
| `String zipCode` + `city` + `street` | `Address` | Full address composition, validation |
| `LocalDate start` + `end` | `DateRange` | Range validation, containment check |

## 4. Value Object Usage Patterns

### 4.1 Factory Method Pattern

Provide various creation methods through factory methods:

```java
public record Money(BigDecimal amount, Currency currency) {

    // USD specific
    public static Money usd(double amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.USD);
    }

    // EUR specific
    public static Money eur(double amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.EUR);
    }

    // Zero amount
    public static final Money ZERO = usd(0);

    // String parsing
    public static Money parse(String text) {
        // Parse "100.00 USD" format
        String[] parts = text.split(" ");
        BigDecimal amount = new BigDecimal(parts[0].replace(",", ""));
        Currency currency = Currency.valueOf(parts[1]);
        return new Money(amount, currency);
    }
}
```

### 4.2 Immutable Operation Pattern

{{< callout type="info" title="Core Principle" >}}
All operations on Value Objects must return new objects without modifying the original. This is the essence of immutability.
{{< /callout >}}

Return new objects without modifying the original:

```java
public record Money(BigDecimal amount, Currency currency) {

    // Addition
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    // Subtraction
    public Money subtract(Money other) {
        validateSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativeMoneyException("Result would be negative");
        }
        return new Money(result, this.currency);
    }

    // Multiplication
    public Money multiply(int quantity) {
        return new Money(
            this.amount.multiply(BigDecimal.valueOf(quantity)),
            this.currency
        );
    }

    // Apply rate (e.g., discount)
    public Money applyRate(BigDecimal rate) {
        return new Money(
            this.amount.multiply(rate).setScale(2, RoundingMode.DOWN),
            this.currency
        );
    }
}

// Usage example
Money price = Money.usd(100.00);
Money discounted = price.applyRate(new BigDecimal("0.9"));  // 10% discount
Money total = discounted.multiply(3);  // Buy 3 items
```

### 4.3 Comparison Operation Pattern

```java
public record Money(BigDecimal amount, Currency currency)
    implements Comparable<Money> {

    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public int compareTo(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount);
    }
}
```

## 5. JPA Mapping

### 5.1 Mapping as Embedded

```java
@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private String id;

    @Embedded
    private MoneyVO totalAmount;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "zipCode", column = @Column(name = "ship_zip")),
        @AttributeOverride(name = "city", column = @Column(name = "ship_city")),
        @AttributeOverride(name = "street", column = @Column(name = "ship_street")),
        @AttributeOverride(name = "detail", column = @Column(name = "ship_detail"))
    })
    private AddressVO shippingAddress;
}

@Embeddable
public class MoneyVO {
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    // Convert to domain VO
    public Money toDomain() {
        return new Money(amount, currency);
    }

    public static MoneyVO from(Money money) {
        MoneyVO vo = new MoneyVO();
        vo.amount = money.amount();
        vo.currency = money.currency();
        return vo;
    }
}
```

### 5.2 Mapping with Converter

```java
@Converter(autoApply = true)
public class OrderIdConverter implements AttributeConverter<OrderId, String> {

    @Override
    public String convertToDatabaseColumn(OrderId orderId) {
        return orderId != null ? orderId.value() : null;
    }

    @Override
    public OrderId convertToEntityAttribute(String value) {
        return value != null ? OrderId.of(value) : null;
    }
}

@Converter(autoApply = true)
public class EmailConverter implements AttributeConverter<Email, String> {

    @Override
    public String convertToDatabaseColumn(Email email) {
        return email != null ? email.value() : null;
    }

    @Override
    public Email convertToEntityAttribute(String value) {
        return value != null ? new Email(value) : null;
    }
}
```

## Troubleshooting

### Problem: "I don't know when to make something a Value Object"

**Solution**:

Make it a Value Object if any of the following apply:

1. **Has validation logic**: Email format, phone number format
2. **Needs operations**: Money addition, date range calculation
3. **Multiple fields travel together**: Address (zip code + city + street)
4. **Has business meaning**: `int quantity` → `Quantity`

---

### Problem: "What if I'm on a Java version without Records?"

**Solution**:

Use Lombok's `@Value`:

```java
@Value
public class Money {
    BigDecimal amount;
    Currency currency;

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount), this.currency);
    }
}
```

Or implement as an immutable class manually:

```java
public final class Money {
    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    // Provide only getters, no setters
    public BigDecimal getAmount() { return amount; }
    public Currency getCurrency() { return currency; }

    // Implement equals, hashCode manually
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return Objects.equals(amount, money.amount)
            && currency == money.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
```

---

### Problem: "Are there cases where a Value Object needs an ID?"

**Solution**:

Value Objects themselves don't have IDs, but **technical IDs for persistence** may be needed:

```java
// Domain model - No ID
public record Address(String zipCode, String city, String street, String detail) {}

// JPA Entity - Technical ID (for ElementCollection)
@Embeddable
public class AddressEntity {
    // No @Id - Part of parent Entity
    private String zipCode;
    private String city;
    private String street;
    private String detail;
}
```

## Next Steps

- [Tactical Design]({{< relref "/docs/ddd/concepts/tactical-design" >}}) - Entity and Aggregate details
- [Defining Aggregate Boundaries]({{< relref "/docs/ddd/howto/aggregate-boundaries" >}}) - Deciding Value Object inclusion
- [Testing Strategy]({{< relref "/docs/ddd/concepts/testing" >}}) - How to test Value Objects
