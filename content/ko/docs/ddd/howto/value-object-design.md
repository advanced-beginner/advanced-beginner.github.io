---
title: Value Object 설계하기
weight: 3
lastmod: "2026-01-16"
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

> **해결하는 문제**: 모든 것을 Entity로 만들어 복잡도가 높아지거나, Primitive Obsession으로 타입 안정성이 낮은 상황
> **소요 시간**: 약 20분
> **전제 조건**: [전술적 설계]({{< relref "/docs/ddd/concepts/tactical-design" >}}) 문서를 읽었다고 가정

{{< callout type="warning" title="성공 기준" >}}
이 가이드를 완료하면 다음을 할 수 있습니다:
- 3가지 질문으로 Entity와 Value Object 구분
- Java Record를 활용한 Value Object 구현
- Primitive Obsession을 Value Object로 리팩토링
{{< /callout >}}

## 1. Entity vs Value Object 구분하기

### 1.1 3가지 판단 질문

다음 질문을 순서대로 적용하세요:

**질문 1: "이것은 고유한 식별자가 필요한가?"**

```
주문(Order) → 주문번호로 식별 → Entity
금액(Money) → 10000원은 그냥 10000원 → Value Object
```

**질문 2: "같은 속성 값을 가지면 같은 것인가?"**

```
두 개의 10000원 → 서로 교환 가능 → Value Object
주문번호 ORD-001과 ORD-002 → 다른 주문 → Entity
```

**질문 3: "시간이 지나도 동일성을 추적해야 하는가?"**

```
고객의 배송지 변경 이력 → 추적 필요 없음 → Value Object
고객의 주문 이력 → 추적 필요 → Entity
```

### 1.2 판단 결과표

| 대상 | Q1 식별자? | Q2 값 동등? | Q3 추적? | 결론 |
|------|-----------|-----------|---------|------|
| 주문 | Yes | No | Yes | Entity |
| 금액 | No | Yes | No | Value Object |
| 주소 | No | Yes | No | Value Object |
| 기간 | No | Yes | No | Value Object |
| 상품 | Yes | No | Yes | Entity |
| 전화번호 | No | Yes | No | Value Object |

### 1.3 Value Object로 만들어야 하는 것들

{{< callout type="tip" title="팁" >}}
다음 항목들은 거의 항상 Value Object로 만드세요. 원시 타입으로 두면 검증과 연산 로직이 여기저기 흩어집니다.
{{< /callout >}}

다음은 거의 항상 Value Object로 만들어야 합니다:

```
├── 금액 (Money)
├── 주소 (Address)
├── 기간/날짜 범위 (DateRange)
├── 좌표 (Coordinates)
├── 이메일 (Email)
├── 전화번호 (PhoneNumber)
├── 우편번호 (ZipCode)
├── 색상 (Color)
├── 수량 (Quantity)
└── 식별자 (OrderId, CustomerId) ← Entity의 ID도 Value Object!
```

## 2. Value Object 구현하기

### 2.1 Java Record 활용

Java 16+ Record를 사용하여 Value Object를 간결하게 구현하세요:

```java
// 기본 Value Object
public record Money(BigDecimal amount, Currency currency) {

    // Compact Constructor로 유효성 검증
    public Money {
        Objects.requireNonNull(amount, "금액은 필수입니다");
        Objects.requireNonNull(currency, "통화는 필수입니다");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다");
        }
    }

    // 팩토리 메서드
    public static Money won(long amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.KRW);
    }

    public static final Money ZERO = new Money(BigDecimal.ZERO, Currency.KRW);

    // 불변 연산 - 새 객체 반환
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

### 2.2 복합 Value Object

여러 값을 묶어야 할 때 복합 Value Object를 만드세요:

```java
public record Address(
    String zipCode,
    String city,
    String street,
    String detail
) {
    public Address {
        Objects.requireNonNull(zipCode, "우편번호는 필수입니다");
        Objects.requireNonNull(city, "도시는 필수입니다");
        Objects.requireNonNull(street, "도로명은 필수입니다");

        if (!zipCode.matches("\\d{5}")) {
            throw new InvalidAddressException("우편번호는 5자리 숫자여야 합니다");
        }
    }

    public String fullAddress() {
        String detailPart = detail != null ? " " + detail : "";
        return String.format("(%s) %s %s%s", zipCode, city, street, detailPart);
    }

    // 새 주소 생성 (상세주소만 변경)
    public Address withDetail(String newDetail) {
        return new Address(this.zipCode, this.city, this.street, newDetail);
    }
}
```

### 2.3 범위/구간 Value Object

시작과 끝이 있는 값을 범위 Value Object로 만드세요:

```java
public record DateRange(LocalDate startDate, LocalDate endDate) {

    public DateRange {
        Objects.requireNonNull(startDate, "시작일은 필수입니다");
        Objects.requireNonNull(endDate, "종료일은 필수입니다");

        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException(
                "시작일이 종료일보다 늦을 수 없습니다: " + startDate + " > " + endDate
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

### 2.4 식별자 Value Object

Entity ID도 Value Object로 구현하세요. String이나 Long 대신 전용 타입을 사용하여 타입 안정성을 높이세요:

```java
public record OrderId(String value) {

    private static final String PREFIX = "ORD-";

    public OrderId {
        Objects.requireNonNull(value, "OrderId는 null일 수 없습니다");
        if (value.isBlank()) {
            throw new IllegalArgumentException("OrderId는 빈 값일 수 없습니다");
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

## 3. Primitive Obsession 리팩토링

### 3.1 문제 식별

{{< callout type="warning" title="Primitive Obsession 경고" >}}
원시 타입을 그대로 사용하면 유효성 검증이 누락되고, 타입 오류가 런타임에야 발견됩니다. 다음 코드에서 문제를 찾아보세요.
{{< /callout >}}

다음 코드에서 Primitive Obsession을 찾으세요:

```java
// ❌ Primitive Obsession이 있는 코드
public class Order {
    private String orderId;           // String → OrderId
    private String customerId;        // String → CustomerId
    private int totalAmount;          // int → Money
    private String currency;          // 별도 필드 → Money에 포함
    private String shippingZipCode;   // String → Address
    private String shippingCity;      // String → Address
    private String shippingStreet;    // String → Address
    private String customerEmail;     // String → Email
    private String customerPhone;     // String → PhoneNumber
}
```

### 3.2 단계별 리팩토링

**1단계: Value Object 클래스 생성**

```java
public record Email(String value) {
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public Email {
        Objects.requireNonNull(value, "이메일은 필수입니다");
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvalidEmailException("유효하지 않은 이메일 형식: " + value);
        }
    }

    public String domain() {
        return value.substring(value.indexOf('@') + 1);
    }
}

public record PhoneNumber(String value) {
    public PhoneNumber {
        Objects.requireNonNull(value, "전화번호는 필수입니다");
        String cleaned = value.replaceAll("[^0-9]", "");
        if (cleaned.length() < 10 || cleaned.length() > 11) {
            throw new InvalidPhoneNumberException("유효하지 않은 전화번호: " + value);
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

**2단계: Entity에 적용**

```java
// ✅ Value Object 적용 후
public class Order {
    private final OrderId id;
    private final CustomerId customerId;
    private Money totalAmount;
    private Address shippingAddress;
    private final Email customerEmail;
    private final PhoneNumber customerPhone;

    // 타입 안전: 잘못된 값이 들어올 수 없음
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

**3단계: 사용 코드 수정**

```java
// Before
Order order = new Order();
order.setOrderId("ORD-001");
order.setTotalAmount(10000);
order.setCurrency("KRW");
order.setCustomerEmail("test@example.com");

// After
Order order = new Order(
    OrderId.of("ORD-001"),
    CustomerId.of("CUST-001"),
    Money.won(10000),
    new Address("12345", "서울시", "강남구 테헤란로", "101호"),
    new Email("test@example.com"),
    new PhoneNumber("010-1234-5678")
);
```

### 3.3 리팩토링 체크리스트

| 원시 타입 | Value Object로 변환 | 추가되는 기능 |
|----------|-------------------|--------------|
| `String orderId` | `OrderId` | 형식 검증, 자동 생성 |
| `int amount` | `Money` | 통화 처리, 연산, 비교 |
| `String email` | `Email` | 형식 검증, 도메인 추출 |
| `String zipCode` + `city` + `street` | `Address` | 전체 주소 조합, 검증 |
| `LocalDate start` + `end` | `DateRange` | 범위 검증, 포함 여부 확인 |

## 4. Value Object 사용 패턴

### 4.1 팩토리 메서드 패턴

다양한 생성 방법을 팩토리 메서드로 제공하세요:

```java
public record Money(BigDecimal amount, Currency currency) {

    // 한국 원화 전용
    public static Money won(long amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.KRW);
    }

    // 미국 달러 전용
    public static Money dollar(double amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.USD);
    }

    // 0원
    public static final Money ZERO = won(0);

    // 문자열 파싱
    public static Money parse(String text) {
        // "10,000 KRW" 형식 파싱
        String[] parts = text.split(" ");
        BigDecimal amount = new BigDecimal(parts[0].replace(",", ""));
        Currency currency = Currency.valueOf(parts[1]);
        return new Money(amount, currency);
    }
}
```

### 4.2 불변 연산 패턴

{{< callout type="info" title="핵심 원칙" >}}
Value Object의 모든 연산은 원본을 변경하지 않고 새 객체를 반환해야 합니다. 이것이 불변성의 핵심입니다.
{{< /callout >}}

원본을 변경하지 않고 새 객체를 반환하세요:

```java
public record Money(BigDecimal amount, Currency currency) {

    // 덧셈
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    // 뺄셈
    public Money subtract(Money other) {
        validateSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new NegativeMoneyException("결과가 음수가 됩니다");
        }
        return new Money(result, this.currency);
    }

    // 곱셈
    public Money multiply(int quantity) {
        return new Money(
            this.amount.multiply(BigDecimal.valueOf(quantity)),
            this.currency
        );
    }

    // 비율 적용 (예: 할인)
    public Money applyRate(BigDecimal rate) {
        return new Money(
            this.amount.multiply(rate).setScale(0, RoundingMode.DOWN),
            this.currency
        );
    }
}

// 사용 예시
Money price = Money.won(10000);
Money discounted = price.applyRate(new BigDecimal("0.9"));  // 10% 할인
Money total = discounted.multiply(3);  // 3개 구매
```

### 4.3 비교 연산 패턴

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

## 5. JPA 매핑

### 5.1 Embedded로 매핑

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

    // 도메인 VO로 변환
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

### 5.2 Converter로 매핑

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

## 트러블슈팅

### 문제: "언제 Value Object로 만들어야 할지 모르겠습니다"

**해결 방법**:

다음 중 하나라도 해당되면 Value Object로 만드세요:

1. **검증 로직이 있다**: 이메일 형식, 전화번호 형식
2. **연산이 필요하다**: 금액 덧셈, 기간 계산
3. **여러 필드가 함께 다닌다**: 주소(우편번호 + 도시 + 거리)
4. **비즈니스 의미가 있다**: `int quantity` → `Quantity`

---

### 문제: "Record가 없는 Java 버전에서는 어떻게 하나요?"

**해결 방법**:

Lombok `@Value`를 사용하세요:

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

또는 직접 불변 클래스로 구현하세요:

```java
public final class Money {
    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

    // getter만 제공, setter 없음
    public BigDecimal getAmount() { return amount; }
    public Currency getCurrency() { return currency; }

    // equals, hashCode 직접 구현
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

### 문제: "Value Object에 ID가 필요한 경우가 있나요?"

**해결 방법**:

Value Object 자체에는 ID가 없지만, <strong>저장을 위한 기술적 ID</strong>가 필요할 수 있습니다:

```java
// 도메인 모델 - ID 없음
public record Address(String zipCode, String city, String street, String detail) {}

// JPA Entity - 기술적 ID 있음 (ElementCollection용)
@Embeddable
public class AddressEntity {
    // @Id 없음 - 부모 Entity의 일부
    private String zipCode;
    private String city;
    private String street;
    private String detail;
}
```

## 다음 단계

- [전술적 설계]({{< relref "/docs/ddd/concepts/tactical-design" >}}) - Entity와 Aggregate 상세
- [Aggregate 경계 정하기]({{< relref "/docs/ddd/howto/aggregate-boundaries" >}}) - Value Object 포함 결정
- [테스트 전략]({{< relref "/docs/ddd/concepts/testing" >}}) - Value Object 테스트 방법
