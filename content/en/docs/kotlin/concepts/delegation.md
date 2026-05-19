---
title: "Delegation"
description: "Explains the principles and real-world patterns of class delegation and property delegation (lazy, observable, Map delegation) using Kotlin's by keyword."
weight: 11
lastmod: "2026-05-13"
---

## Overall Analogy: Secretary and a Team of Experts

Delegation is easier to understand if you compare it to **a boss delegating work to a secretary**.

| Workplace Analogy | Kotlin Concept | Role |
|----------|-----------|------|
| Boss | Delegating class | Implements an interface but delegates the actual work |
| Secretary | Delegate object | Performs the actual work |
| "Ask the secretary" | `by` keyword | Compiler auto-generates the delegation code |
| Tasks the boss handles directly | Override | Process only some parts of the delegation directly |
| Expert called only when needed | `by lazy` | Initialize only on first use |
| Change notification service | `Delegates.observable` | Tracks property changes |

---

> **Target Audience**: Developers who understand Kotlin basic classes/interfaces
> **Prerequisites**: Interfaces, properties, basic generics
> **Time Required**: About 30 minutes
> **After Reading**: You will be able to freely use `by lazy`, `Delegates.observable`, Map delegation, and write your own Delegates.

{{< callout type="tip" title="TL;DR" >}}
- `class A(b: B) : Interface by b` — Delegate the interface implementation to b
- `val x by lazy { ... }` — Initialize only on first access (thread-safe)
- `var y by Delegates.observable(initial) { _, old, new -> }` — Detect changes
- `val z by map` — Delegate property value to a Map key
{{< /callout >}}

#### Class Delegation

Delegate interface implementations to another object using the `by` keyword. The compiler automatically generates the delegation code, reducing boilerplate.

```kotlin
interface Printer {
    fun print(text: String)
    fun printLine(text: String)
}

// Existing implementation
class ConsolePrinter : Printer {
    override fun print(text: String) = kotlin.io.print(text)
    override fun printLine(text: String) = println(text)
}

// Class delegation — delegates Printer implementation to ConsolePrinter
class PrefixedPrinter(
    private val prefix: String,
    private val delegate: Printer = ConsolePrinter()
) : Printer by delegate {
    // Override only printLine — print() is delegated
    override fun printLine(text: String) {
        delegate.printLine("[$prefix] $text")
    }
}

// Usage
val printer = PrefixedPrinter("INFO")
printer.print("Hello")          // Hello  (delegate call)
printer.printLine("World")      // [INFO] World  (override)
```

**Real-world Use of Class Delegation: The Decorator Pattern**

```kotlin
interface Cache<K, V> {
    fun get(key: K): V?
    fun put(key: K, value: V)
    fun remove(key: K)
    fun clear()
}

class SimpleCache<K, V> : Cache<K, V> {
    private val store = mutableMapOf<K, V>()
    override fun get(key: K) = store[key]
    override fun put(key: K, value: V) { store[key] = value }
    override fun remove(key: K) { store.remove(key) }
    override fun clear() = store.clear()
}

// Logging decorator — implemented concisely with delegation
class LoggingCache<K, V>(
    private val delegate: Cache<K, V>
) : Cache<K, V> by delegate {
    override fun put(key: K, value: V) {
        println("Cache put: $key")
        delegate.put(key, value)
    }

    override fun get(key: K): V? {
        val value = delegate.get(key)
        println("Cache ${if (value != null) "hit" else "miss"}: $key")
        return value
    }
}

// Usage
val cache: Cache<String, String> = LoggingCache(SimpleCache())
cache.put("user:1", "John Doe")   // Cache put: user:1
cache.get("user:1")               // Cache hit: user:1
cache.get("user:2")               // Cache miss: user:2
```

#### Property Delegation

Delegate the get/set behavior of a `val`/`var` property to another object. The object after `by` must provide `getValue` (and `setValue`).

```kotlin
// Basic structure of property delegation
class MyDelegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return "delegated value (${property.name})"
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        println("Storing $value to ${property.name}")
    }
}

class Example {
    val name: String by MyDelegate()
    var title: String by MyDelegate()
}
```

#### by lazy

**The most commonly used delegate.** It executes the initialization block only on first access, then returns the cached value. The default mode (`LazyThreadSafetyMode.SYNCHRONIZED`) is thread-safe.

```kotlin
class HeavyService {
    val client by lazy {
        println("Initializing client...")
        createExpensiveHttpClient()         // Expensive initialization
    }

    val config by lazy {
        loadConfigFromFile("config.yaml")   // File read
    }
}

val service = HeavyService()
// Not initialized yet
println("Service created")
service.client.get("/api/health")   // Initialization happens at this point
service.client.get("/api/users")    // Uses cached value
```

**Choosing a lazy Mode**

| Mode | Description | When to Use |
|------|------|---------|
| `SYNCHRONIZED` (default) | Thread-safe via locking | Multi-threaded environment |
| `PUBLICATION` | Multiple threads can initialize; only the first value is used | When initialization cost is low |
| `NONE` | No synchronization, fastest | When you are certain about a single thread |

```kotlin
val lazyValue by lazy(LazyThreadSafetyMode.NONE) {
    expensiveComputation()   // Used only in a single thread
}
```

#### Delegates.observable

Runs a callback every time a property value changes. Useful for UI binding, event logging, and validation.

```kotlin
import kotlin.properties.Delegates

class UserProfile {
    var name: String by Delegates.observable("") { property, oldValue, newValue ->
        println("${property.name}: '$oldValue' -> '$newValue'")
        // Change notification, validation, etc.
    }

    var age: Int by Delegates.observable(0) { _, old, new ->
        if (new < 0) throw IllegalArgumentException("Age cannot be negative")
    }
}

val profile = UserProfile()
profile.name = "John Doe"   // name: '' -> 'John Doe'
profile.name = "Jane Doe"   // name: 'John Doe' -> 'Jane Doe'
```

**vetoable — Can Reject Changes**

```kotlin
import kotlin.properties.Delegates

class BoundedValue {
    var score: Int by Delegates.vetoable(0) { _, _, newValue ->
        newValue in 0..100   // true allows the change, false rejects it
    }
}

val bv = BoundedValue()
bv.score = 85
println(bv.score)   // 85
bv.score = 150
println(bv.score)   // 85 — rejected
```

#### Delegates.notNull

An alternative to `lateinit`, usable with primitive types (`Int`, `Boolean`, etc.) or nullable types.

```kotlin
import kotlin.properties.Delegates

class Config {
    var maxRetries: Int by Delegates.notNull()
    var timeout: Long by Delegates.notNull()
}

val config = Config()
// config.maxRetries   // Accessing before initialization throws IllegalStateException
config.maxRetries = 3
config.timeout = 5000L
println("Max retries: ${config.maxRetries}")
```

#### Map Delegation

Use a `Map` (or `MutableMap`) to store property values. Useful for JSON parsing, configuration loading, and dynamic properties.

```kotlin
class UserSettings(private val map: Map<String, Any?>) {
    val name: String by map
    val age: Int by map
    val email: String by map
}

// When you receive the result of JSON parsing as a Map
val settings = UserSettings(mapOf(
    "name" to "John Doe",
    "age" to 30,
    "email" to "john@example.com"
))

println(settings.name)   // John Doe
println(settings.age)    // 30

// MutableMap for read/write
class MutableSettings(private val map: MutableMap<String, Any?>) {
    var theme: String by map
    var language: String by map
}

val ms = MutableSettings(mutableMapOf(
    "theme" to "dark",
    "language" to "en"
))
ms.theme = "light"
println(ms.theme)        // light
```

#### Writing Your Own Delegate

Implement the `ReadOnlyProperty` or `ReadWriteProperty` interface, or define `getValue`/`setValue` operator functions.

```kotlin
import kotlin.reflect.KProperty

// Read-only Delegate — store values encrypted
class EncryptedDelegate<T>(private val encrypt: (T) -> String,
                           private val decrypt: (String) -> T,
                           initialValue: T) {
    private var stored: String = encrypt(initialValue)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return decrypt(stored)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        stored = encrypt(value)
        println("${property.name} stored encrypted")
    }
}

// Usage example
fun simpleEncrypt(value: String) = value.reversed()
fun simpleDecrypt(value: String) = value.reversed()

class SecureData {
    var password: String by EncryptedDelegate(
        encrypt = ::simpleEncrypt,
        decrypt = ::simpleDecrypt,
        initialValue = ""
    )
}

val data = SecureData()
data.password = "secret123"    // password stored encrypted
println(data.password)         // secret123
```

**Reusable Validation Delegate**

```kotlin
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Validated<T>(
    initialValue: T,
    private val validator: (T) -> Boolean,
    private val errorMessage: String
) : ReadWriteProperty<Any?, T> {
    private var value: T = initialValue

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        require(validator(value)) { errorMessage }
        this.value = value
    }
}

// Convenient extension function
fun <T> validated(
    initialValue: T,
    validator: (T) -> Boolean,
    errorMessage: String
) = Validated(initialValue, validator, errorMessage)

class Order {
    var quantity: Int by validated(
        initialValue = 1,
        validator = { it > 0 },
        errorMessage = "Quantity must be at least 1"
    )

    var price: Long by validated(
        initialValue = 0L,
        validator = { it >= 0 },
        errorMessage = "Price cannot be negative"
    )
}

val order = Order()
order.quantity = 5
order.price = 10_000L
// order.quantity = -1  // IllegalArgumentException
```

{{< callout type="info" title="Key Takeaways" >}}
- `class A(b: B) : Interface by b` — Delegate the interface implementation and override only the methods you need
- `by lazy { }` — Initialize on first access; default mode is thread-safe
- `Delegates.observable` — Property change callback; `vetoable` can reject changes
- `Delegates.notNull` — Apply deferred initialization to primitive types
- Map delegation — Useful for dynamic properties or configuration loading
- Write your own Delegate by implementing `getValue`/`setValue` operator functions
{{< /callout >}}

#### Next Steps

- [Inline/Reified](inline-reified/) — How property Delegates are optimized with inline
- [Extension Functions](extension-functions/) — Extension function patterns used with Delegates
