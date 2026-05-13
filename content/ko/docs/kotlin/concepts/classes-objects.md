---
title: "클래스와 객체"
description: "Kotlin의 class 정의, primary/secondary 생성자, init 블록, 프로퍼티, object 선언(싱글톤), companion object, 가시성 변경자를 설명합니다."
weight: 5
lastmod: "2026-05-13"
---

## 전체 비유: 설계도, 금형, 제품

Kotlin 클래스를 **설계도, 금형, 제품** 에 비유하면 이해하기 쉽습니다. `class`는 설계도이고, 생성자는 금형입니다. `new` 키워드 없이 금형을 직접 호출하면 제품(인스턴스)이 만들어집니다. `object`는 하나만 존재하는 특별 제품, `companion object`는 금형에 붙어있는 공용 도구함입니다.

| 비유 | Kotlin 개념 | 역할 |
|------|------------|------|
| 설계도 | `class` 선언 | 인스턴스의 구조와 행동 정의 |
| 금형 | 생성자 (primary/secondary) | 인스턴스 생성 방법 정의 |
| 제품 | 인스턴스 | 클래스의 구체적 실체 |
| 특별 한정판 (하나뿐) | `object` 선언 | 싱글톤 인스턴스 |
| 금형 옆 공용 도구함 | `companion object` | 클래스 수준 함수와 상수 |
| 접근 등급 | 가시성 변경자 | 누가 어디서 사용할 수 있는지 제어 |

---

> **대상 독자**: [Null Safety](../null-safety/)를 읽은 학습자
> **선수 지식**: Kotlin 기본 문법, 함수, null 안전
> **소요 시간**: 약 30분
> **이 문서를 읽으면**: Kotlin 클래스를 정의하고, 생성자와 프로퍼티를 활용하며, 싱글톤 패턴과 companion object를 적절히 사용할 수 있습니다.

{{< callout type="tip" title="TL;DR" >}}
- **Primary 생성자** 는 클래스 헤더에 직접 선언합니다
- **`val`/`var` 매개변수** 는 자동으로 프로퍼티가 됩니다
- **`object`** 는 싱글톤, **`companion object`** 는 클래스 수준 멤버입니다
- **가시성 변경자** 기본값은 `public`이고, `internal`은 모듈 단위입니다
{{< /callout >}}

#### 왜 Kotlin 클래스는 간결한가?

Java 클래스는 필드, 생성자, getter/setter를 모두 수동으로 작성해야 합니다. Kotlin은 primary 생성자에 `val`/`var`를 붙이면 이 모든 것이 자동으로 처리됩니다.

---

#### 클래스 기본 정의

```kotlin
// 가장 단순한 클래스
class Empty

// 프로퍼티가 있는 클래스
class Point(val x: Int, val y: Int)

// 인스턴스 생성 — new 키워드 없음
val origin = Point(0, 0)
val p = Point(3, 4)
println("x=${p.x}, y=${p.y}")
```

---

#### Primary 생성자

클래스 이름 다음에 괄호로 선언하는 **주 생성자** 입니다. `val`/`var`로 선언된 매개변수는 자동으로 프로퍼티가 됩니다.

```kotlin
class User(
    val name: String,
    val age: Int,
    var email: String = ""   // 기본값 지원
) {
    // 추가 로직은 클래스 바디에
    fun greet() = "안녕하세요, $name!"
    fun isAdult() = age >= 18
}

val user = User("홍길동", 30, "hong@example.com")
println(user.greet())     // 안녕하세요, 홍길동!
println(user.isAdult())   // true
user.email = "new@example.com"   // var이므로 변경 가능
```

**`constructor` 키워드**

어노테이션이나 가시성 변경자가 필요할 때만 `constructor` 키워드를 명시합니다.

```kotlin
class ApiClient @JvmOverloads constructor(
    val baseUrl: String,
    val timeout: Int = 30
)
```

---

#### init 블록

primary 생성자는 바디를 가질 수 없으므로, 초기화 로직은 `init` 블록에 작성합니다. 여러 개 선언할 수 있으며 위에서 아래 순서로 실행됩니다.

```kotlin
class DatabaseConfig(
    val host: String,
    val port: Int,
    val database: String
) {
    val connectionString: String

    init {
        // 유효성 검사
        require(port in 1..65535) { "포트 번호는 1~65535 사이여야 합니다" }
        require(database.isNotBlank()) { "데이터베이스 이름은 비어있을 수 없습니다" }

        // 파생 프로퍼티 초기화
        connectionString = "jdbc:postgresql://$host:$port/$database"
    }
}

val config = DatabaseConfig("localhost", 5432, "mydb")
println(config.connectionString)
// jdbc:postgresql://localhost:5432/mydb
```

---

#### Secondary 생성자

여러 가지 방법으로 인스턴스를 생성해야 할 때 `constructor` 키워드로 보조 생성자를 선언합니다. 반드시 `this(...)`로 primary 생성자를 호출해야 합니다.

```kotlin
class Rectangle(val width: Double, val height: Double) {
    // 정사각형 생성을 위한 보조 생성자
    constructor(side: Double) : this(side, side)

    // Int를 받는 보조 생성자
    constructor(width: Int, height: Int) : this(width.toDouble(), height.toDouble())

    val area: Double get() = width * height
}

val rect = Rectangle(3.0, 4.0)
val square = Rectangle(5.0)         // secondary 생성자
val intRect = Rectangle(6, 8)       // secondary 생성자
```

{{< callout type="info" title="보조 생성자보다 기본값 인자를 권장" >}}
대부분의 경우 기본값 인자로 보조 생성자를 대체할 수 있습니다. 보조 생성자는 완전히 다른 타입의 인자를 받거나 초기화 로직이 크게 다를 때 사용합니다.
{{< /callout >}}

---

#### 프로퍼티 (getter/setter)

Kotlin 프로퍼티는 필드와 getter/setter를 한꺼번에 처리합니다.

```kotlin
class Temperature(private var _celsius: Double) {
    // 커스텀 getter
    val fahrenheit: Double
        get() = _celsius * 9.0 / 5.0 + 32

    // 커스텀 getter와 setter
    var celsius: Double
        get() = _celsius
        set(value) {
            require(value >= -273.15) { "절대 영도 이하입니다" }
            _celsius = value
        }
}

val temp = Temperature(100.0)
println(temp.fahrenheit)   // 212.0
temp.celsius = 0.0
println(temp.fahrenheit)   // 32.0
```

**지연 초기화 — lateinit과 lazy**

```kotlin
class Service {
    // lateinit — 나중에 초기화 (var에만 사용, non-null)
    lateinit var repository: UserRepository

    fun init() {
        repository = UserRepository()
    }

    fun getUsers() = repository.findAll()   // init() 호출 전에 접근하면 UninitializedPropertyAccessException
}

class ExpensiveObject {
    // lazy — 처음 접근 시 초기화 (val에 사용)
    val config: AppConfig by lazy {
        println("설정 로딩 중...")
        AppConfig.load()
    }
}
```

| 비교 | `lateinit var` | `by lazy` |
|------|----------------|-----------|
| 대상 | `var` | `val` |
| 타입 | Non-null | Non-null |
| null 허용 | 불가 | 불가 |
| 초기화 시점 | 수동 초기화 | 첫 접근 시 |
| 초기화 확인 | `::field.isInitialized` | 자동 (항상 한 번만) |

---

#### object 선언 — 싱글톤

`object` 키워드로 클래스 정의와 동시에 유일한 인스턴스를 만듭니다. 싱글톤 패턴의 가장 간결한 표현입니다.

```kotlin
object AppConfig {
    val appName = "MyApp"
    val version = "1.0.0"

    fun loadFromEnv() {
        println("환경 변수에서 설정 로드")
    }
}

// 인스턴스 없이 바로 사용
println(AppConfig.appName)   // MyApp
AppConfig.loadFromEnv()
```

**object 표현식 — 익명 객체**

인터페이스를 즉석에서 구현할 때 사용합니다.

```kotlin
val comparator = object : Comparator<String> {
    override fun compare(a: String, b: String): Int = a.length - b.length
}

val sorted = listOf("banana", "apple", "kiwi").sortedWith(comparator)
println(sorted)   // [kiwi, apple, banana]
```

---

#### companion object

클래스 안에 선언하는 싱글톤입니다. 클래스 이름으로 접근할 수 있어 팩토리 메서드와 상수 정의에 활용합니다.

```kotlin
class User private constructor(
    val name: String,
    val role: String
) {
    companion object {
        const val ADMIN_ROLE = "ADMIN"
        const val USER_ROLE = "USER"

        // 팩토리 메서드
        fun createAdmin(name: String) = User(name, ADMIN_ROLE)
        fun createUser(name: String) = User(name, USER_ROLE)
    }

    override fun toString() = "$name ($role)"
}

// companion object 접근
val admin = User.createAdmin("관리자")
val user = User.createUser("홍길동")
println(admin)   // 관리자 (ADMIN)
println(User.ADMIN_ROLE)   // ADMIN
```

---

#### 가시성 변경자

Kotlin의 가시성 변경자는 4가지입니다. 기본값은 `public`입니다.

| 변경자 | 접근 범위 |
|--------|----------|
| `public` | 어디서든 접근 가능 (기본값) |
| `private` | 선언된 클래스/파일 내부에서만 |
| `protected` | 선언된 클래스와 하위 클래스에서만 |
| `internal` | 같은 모듈 내에서만 |

```kotlin
class BankAccount(private var balance: Double) {
    // public — 외부에서 접근 가능 (기본값)
    val accountNumber: String = generateAccountNumber()

    // internal — 같은 모듈에서만
    internal fun getBalanceForAudit() = balance

    // protected — 하위 클래스에서만
    protected fun addInterest(rate: Double) {
        balance += balance * rate
    }

    // private — 이 클래스 내부에서만
    private fun generateAccountNumber() = "ACC-${System.currentTimeMillis()}"

    fun deposit(amount: Double) {
        require(amount > 0) { "입금액은 0보다 커야 합니다" }
        balance += amount
    }
}
```

{{< callout type="info" title="internal 변경자" >}}
`internal`은 Kotlin 고유의 가시성 변경자입니다. 같은 Gradle 모듈(컴파일 단위) 안에서만 접근 가능합니다. 라이브러리에서 공개 API와 내부 구현을 구분할 때 유용합니다.
{{< /callout >}}

---

#### 코드 예제: 실전 클래스 설계

```kotlin
package com.example.classes

class OrderService {
    companion object {
        private const val MAX_ITEMS = 100
        fun create() = OrderService()
    }

    private val orders = mutableListOf<Order>()

    fun placeOrder(
        customerId: String,
        items: List<OrderItem>
    ): Order {
        require(items.isNotEmpty()) { "주문 항목은 비어있을 수 없습니다" }
        require(items.size <= MAX_ITEMS) { "주문 항목은 최대 $MAX_ITEMS개까지입니다" }

        val order = Order(
            id = generateId(),
            customerId = customerId,
            items = items
        )
        orders.add(order)
        return order
    }

    fun findOrder(id: String): Order? =
        orders.find { it.id == id }

    private fun generateId() = "ORD-${System.currentTimeMillis()}"
}

class Order(
    val id: String,
    val customerId: String,
    val items: List<OrderItem>
) {
    val totalAmount: Double
        get() = items.sumOf { it.price * it.quantity }

    override fun toString() = "Order(id=$id, total=$totalAmount)"
}

class OrderItem(
    val productId: String,
    val quantity: Int,
    val price: Double
)

fun main() {
    val service = OrderService.create()

    val order = service.placeOrder(
        customerId = "CUST-001",
        items = listOf(
            OrderItem("PROD-A", 2, 15000.0),
            OrderItem("PROD-B", 1, 8000.0)
        )
    )

    println(order)   // Order(id=ORD-..., total=38000.0)
    println("주문 금액: ${order.totalAmount}원")
}
```

---

#### 핵심 포인트

{{< callout type="info" title="핵심 정리" >}}
- **Primary 생성자** 에 `val`/`var`를 붙이면 자동으로 프로퍼티가 됩니다
- **`init` 블록** 에서 생성자 실행 후 초기화 로직을 처리합니다
- **`object`** 는 싱글톤, **`companion object`** 는 클래스 수준 팩토리/상수입니다
- **`lateinit var`** 는 나중에 초기화, **`by lazy`** 는 첫 접근 시 초기화입니다
- **가시성 기본값** 은 `public`, `internal`은 같은 모듈 내 공유입니다
{{< /callout >}}

#### 다음 단계

- [Data/Sealed Class](../data-sealed-classes/) - 불변 데이터 클래스와 닫힌 계층 모델링을 배웁니다
- [컬렉션](../collections/) - 클래스 인스턴스를 컬렉션으로 다루는 방법을 학습합니다
