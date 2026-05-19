---
title: "Classes and Objects"
description: "Explains Kotlin's class definitions, primary/secondary constructors, init blocks, properties, object declarations (singletons), companion objects, and visibility modifiers."
weight: 5
lastmod: "2026-05-13"
---

## Overall Analogy: Blueprints, Molds, and Products

Kotlin classes are easier to understand if you compare them to **blueprints, molds, and products**. A `class` is a blueprint, and a constructor is a mold. Without a `new` keyword, calling the mold directly creates a product (instance). `object` is a special product that exists only once, and `companion object` is a shared toolbox attached to the mold.

| Analogy | Kotlin Concept | Role |
|------|------------|------|
| Blueprint | `class` declaration | Defines the structure and behavior of instances |
| Mold | Constructor (primary/secondary) | Defines how to create instances |
| Product | Instance | A concrete instance of the class |
| Special limited edition (only one) | `object` declaration | Singleton instance |
| Shared toolbox next to the mold | `companion object` | Class-level functions and constants |
| Access level | Visibility modifier | Controls who can use it from where |

---

> **Target Audience**: Learners who have read [Null Safety](../null-safety/)
> **Prerequisites**: Kotlin basic syntax, functions, null safety
> **Time Required**: About 30 minutes
> **After Reading**: You will be able to define Kotlin classes, use constructors and properties, and appropriately apply singleton patterns and companion objects.

{{< callout type="tip" title="TL;DR" >}}
- **Primary constructors** are declared directly in the class header
- **`val`/`var` parameters** automatically become properties
- **`object`** is a singleton, **`companion object`** is a class-level member
- **The default visibility modifier** is `public`, and `internal` is module-scoped
{{< /callout >}}

#### Why Are Kotlin Classes So Concise?

In Java classes, you must manually write fields, constructors, and getters/setters. In Kotlin, adding `val`/`var` in the primary constructor handles all of these automatically.

---

#### Basic Class Definition

```kotlin
// Simplest class
class Empty

// Class with properties
class Point(val x: Int, val y: Int)

// Instance creation — no new keyword
val origin = Point(0, 0)
val p = Point(3, 4)
println("x=${p.x}, y=${p.y}")
```

---

#### Primary Constructor

The **primary constructor** is declared in parentheses after the class name. Parameters declared with `val`/`var` automatically become properties.

```kotlin
class User(
    val name: String,
    val age: Int,
    var email: String = ""   // Default values supported
) {
    // Additional logic goes in the class body
    fun greet() = "Hello, $name!"
    fun isAdult() = age >= 18
}

val user = User("John Doe", 30, "john@example.com")
println(user.greet())     // Hello, John Doe!
println(user.isAdult())   // true
user.email = "new@example.com"   // Can be changed because it's a var
```

**The `constructor` Keyword**

Specify the `constructor` keyword explicitly only when annotations or visibility modifiers are needed.

```kotlin
class ApiClient @JvmOverloads constructor(
    val baseUrl: String,
    val timeout: Int = 30
)
```

---

#### init Block

The primary constructor cannot have a body, so initialization logic is written in `init` blocks. Multiple blocks can be declared and are executed in top-to-bottom order.

```kotlin
class DatabaseConfig(
    val host: String,
    val port: Int,
    val database: String
) {
    val connectionString: String

    init {
        // Validation
        require(port in 1..65535) { "Port number must be between 1 and 65535" }
        require(database.isNotBlank()) { "Database name cannot be blank" }

        // Initialize derived property
        connectionString = "jdbc:postgresql://$host:$port/$database"
    }
}

val config = DatabaseConfig("localhost", 5432, "mydb")
println(config.connectionString)
// jdbc:postgresql://localhost:5432/mydb
```

---

#### Secondary Constructor

When you need to create instances in multiple ways, declare a secondary constructor with the `constructor` keyword. It must call the primary constructor via `this(...)`.

```kotlin
class Rectangle(val width: Double, val height: Double) {
    // Secondary constructor for creating a square
    constructor(side: Double) : this(side, side)

    // Secondary constructor accepting Int
    constructor(width: Int, height: Int) : this(width.toDouble(), height.toDouble())

    val area: Double get() = width * height
}

val rect = Rectangle(3.0, 4.0)
val square = Rectangle(5.0)         // secondary constructor
val intRect = Rectangle(6, 8)       // secondary constructor
```

{{< callout type="info" title="Prefer Default Arguments Over Secondary Constructors" >}}
In most cases, default arguments can replace secondary constructors. Use secondary constructors only when you accept completely different argument types or when the initialization logic differs significantly.
{{< /callout >}}

---

#### Properties (getter/setter)

Kotlin properties combine fields and getter/setter into one.

```kotlin
class Temperature(private var _celsius: Double) {
    // Custom getter
    val fahrenheit: Double
        get() = _celsius * 9.0 / 5.0 + 32

    // Custom getter and setter
    var celsius: Double
        get() = _celsius
        set(value) {
            require(value >= -273.15) { "Below absolute zero" }
            _celsius = value
        }
}

val temp = Temperature(100.0)
println(temp.fahrenheit)   // 212.0
temp.celsius = 0.0
println(temp.fahrenheit)   // 32.0
```

**Lazy Initialization — lateinit and lazy**

```kotlin
class Service {
    // lateinit — initialize later (var only, non-null)
    lateinit var repository: UserRepository

    fun init() {
        repository = UserRepository()
    }

    fun getUsers() = repository.findAll()   // Accessing before init() throws UninitializedPropertyAccessException
}

class ExpensiveObject {
    // lazy — initialize on first access (used with val)
    val config: AppConfig by lazy {
        println("Loading configuration...")
        AppConfig.load()
    }
}
```

| Comparison | `lateinit var` | `by lazy` |
|------|----------------|-----------|
| Target | `var` | `val` |
| Type | Non-null | Non-null |
| Nullable | Not allowed | Not allowed |
| Initialization timing | Manual | On first access |
| Initialization check | `::field.isInitialized` | Automatic (always once) |

---

#### object Declaration — Singleton

The `object` keyword creates a unique instance at the same time as the class definition. It's the most concise expression of the singleton pattern.

```kotlin
object AppConfig {
    val appName = "MyApp"
    val version = "1.0.0"

    fun loadFromEnv() {
        println("Loading configuration from environment variables")
    }
}

// Use directly without an instance
println(AppConfig.appName)   // MyApp
AppConfig.loadFromEnv()
```

**Object Expression — Anonymous Object**

Used when implementing an interface on the spot.

```kotlin
val comparator = object : Comparator<String> {
    override fun compare(a: String, b: String): Int = a.length - b.length
}

val sorted = listOf("banana", "apple", "kiwi").sortedWith(comparator)
println(sorted)   // [kiwi, apple, banana]
```

---

#### companion object

A singleton declared inside a class. Accessible via the class name, it is used for factory methods and constant definitions.

```kotlin
class User private constructor(
    val name: String,
    val role: String
) {
    companion object {
        const val ADMIN_ROLE = "ADMIN"
        const val USER_ROLE = "USER"

        // Factory methods
        fun createAdmin(name: String) = User(name, ADMIN_ROLE)
        fun createUser(name: String) = User(name, USER_ROLE)
    }

    override fun toString() = "$name ($role)"
}

// companion object access
val admin = User.createAdmin("Administrator")
val user = User.createUser("John Doe")
println(admin)   // Administrator (ADMIN)
println(User.ADMIN_ROLE)   // ADMIN
```

---

#### Visibility Modifiers

Kotlin has 4 visibility modifiers. The default is `public`.

| Modifier | Access Scope |
|--------|----------|
| `public` | Accessible from anywhere (default) |
| `private` | Only within the declaring class/file |
| `protected` | Only within the declaring class and subclasses |
| `internal` | Only within the same module |

```kotlin
class BankAccount(private var balance: Double) {
    // public — accessible from outside (default)
    val accountNumber: String = generateAccountNumber()

    // internal — only within the same module
    internal fun getBalanceForAudit() = balance

    // protected — only in subclasses
    protected fun addInterest(rate: Double) {
        balance += balance * rate
    }

    // private — only inside this class
    private fun generateAccountNumber() = "ACC-${System.currentTimeMillis()}"

    fun deposit(amount: Double) {
        require(amount > 0) { "Deposit amount must be greater than 0" }
        balance += amount
    }
}
```

{{< callout type="info" title="internal Modifier" >}}
`internal` is a visibility modifier unique to Kotlin. It is accessible only within the same Gradle module (compilation unit). It is useful for distinguishing between public APIs and internal implementations in libraries.
{{< /callout >}}

---

#### Code Example: Real-world Class Design

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
        require(items.isNotEmpty()) { "Order items cannot be empty" }
        require(items.size <= MAX_ITEMS) { "Order items must be at most $MAX_ITEMS" }

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
    println("Order amount: ${order.totalAmount} KRW")
}
```

---

#### Key Points

{{< callout type="info" title="Key Takeaways" >}}
- Adding **`val`/`var` to primary constructor** parameters automatically makes them properties
- The **`init` block** handles initialization logic after the constructor runs
- **`object`** is a singleton, **`companion object`** is a class-level factory/constants holder
- **`lateinit var`** is initialized later, **`by lazy`** is initialized on first access
- **The default visibility** is `public`, and `internal` is shared within the same module
{{< /callout >}}

#### Next Steps

- [Data/Sealed Class](../data-sealed-classes/) - Learn immutable data classes and closed hierarchy modeling
- [Collections](../collections/) - Learn how to handle class instances as collections
