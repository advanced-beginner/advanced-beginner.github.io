---
title: "Version Comparison"
description: "A summary of major changes from Kotlin 1.x to 2.x, including the K2 compiler, value class, Context Receivers, and Multiplatform stabilization."
weight: 2
lastmod: "2026-05-13"
---

> **Estimated time**: about 10 minutes

A summary of major changes across Kotlin versions. Provides the key changes and tips you should consider when migrating.

{{< callout type="tip" title="Key Summary" >}}
- **Kotlin 2.0**: K2 compiler stabilization — up to 2x faster compilation
- **Kotlin 2.0**: Official support for `data object`
- **Kotlin 1.9**: Added `Enum.entries` (replaces `.values()`)
- **Kotlin 1.7**: Improved builder inference and improved `by lazy` thread mode
- **Kotlin 1.5**: Stabilized `value class` (inline class) and added `sealed interface`
{{< /callout >}}

---

#### Kotlin Versions at a Glance

| Version | Major Changes | Minimum JVM |
|---------|---------------|-------------|
| 1.4.x | Improved SAM conversion, `fun interface` | JVM 8 |
| 1.5.x | `value class`, `sealed interface`, `Result` improvements | JVM 8 |
| 1.6.x | `sealed class` when-exhaustiveness warning, `Regex` improvements | JVM 8 |
| 1.7.x | K1 compiler improvements, `@OptIn` stabilization | JVM 8 |
| 1.8.x | `kotlin-reflect` slimmed down, Java 18-19 support | JVM 8 |
| 1.9.x | `Enum.entries`, `data object`, range function improvements | JVM 8 |
| 2.0.x | **K2 compiler stabilization**, smart-cast improvements | JVM 8 |
| 2.1.x | Guard conditions (`when` guard), non-local break/continue | JVM 8 |

---

#### Kotlin 2.0 — Core Changes

**K2 Compiler Stabilization**

K2 is a completely rewritten Kotlin compiler frontend. It was promoted to `Stable` in Kotlin 2.0.

| Item | Change |
|------|--------|
| Compile speed | Up to ~2x faster on average (depending on project size) |
| Type inference | More accurate inference, with some error fixes |
| IDE analysis | IntelliJ analysis engine switched to a K2 base |
| Plugin compatibility | Existing compiler plugins must be updated to the K2 API |

```kotlin
// Smart-cast improvement example in 2.0
class Container(val value: Any?) {
    fun printLength() {
        val v = value
        if (v is String) {
            println(v.length)   // Before: smart cast failed in some cases
                                // 2.0: smart cast works correctly
        }
    }
}
```

**data object (official in 2.0)**

Attaching `data` to an `object` declaration makes `toString()` return the class name. Useful for value-less cases in a `sealed` hierarchy.

```kotlin
sealed class ApiResult {
    data class Success(val body: String) : ApiResult()
    data class Error(val code: Int, val message: String) : ApiResult()
    data object Loading : ApiResult()     // toString() = "Loading"
    data object Empty : ApiResult()       // toString() = "Empty"
}

println(ApiResult.Loading)   // Loading (plain object would include the package path)
```

---

#### Kotlin 1.9 — Practical Improvements

**Enum.entries**

`.values()` created a new array on every call. `.entries` is a property that returns a pre-built immutable list.

```kotlin
enum class Direction { NORTH, SOUTH, EAST, WEST }

// Previously — array copy occurs
val old = Direction.values().toList()

// 1.9+ — pre-built list
val new = Direction.entries
println(new)   // [NORTH, SOUTH, EAST, WEST]
```

**Range Function Improvements**

The `..` operator and the `until` function gained `Float` and `Double` ranges, and the `rangeUntil` operator (`..<`) was introduced.

```kotlin
for (i in 0..<10) {     // 0 inclusive to 10 exclusive (1.9+)
    print("$i ")
}
// Previously: for (i in 0 until 10)
```

---

#### Kotlin 1.7 — Builders and Stabilization

**Builder Inference**

`buildList`, `buildMap`, and `buildSet` were promoted to `Stable`.

```kotlin
val list = buildList {
    add("A")
    addAll(listOf("B", "C"))
    add(if (condition) "D" else "E")
}   // returns List<String>
```

---

#### Kotlin 1.5 — value class and sealed interface

**value class (inline class)**

A class that wraps a single property but does not create a wrapper object at runtime. Combines type safety with performance.

```kotlin
// Behaves like a Long at runtime, but remains type-safe
@JvmInline
value class UserId(val id: Long)

@JvmInline
value class Email(val address: String) {
    init {
        require(address.contains("@")) { "Invalid email format" }
    }
}

fun findUser(id: UserId): User = TODO("implement DB lookup")

val id = UserId(42L)
// findUser(42L)  // compile error — type-safe
findUser(id)      // OK
```

**sealed interface**

The `sealed` modifier can now also be applied to `interface`. Unlike classes, multiple `sealed interface` types can be implemented at the same time.

```kotlin
sealed interface Drawable { fun draw() }
sealed interface Resizable { fun resize(factor: Double) }

// You can implement two sealed interfaces simultaneously
class Image(val path: String) : Drawable, Resizable {
    override fun draw() { println("Drawing image: $path") }
    override fun resize(factor: Double) { println("Resizing by: $factor") }
}
```

---

#### Kotlin 1.4 — fun interface (SAM Conversion)

If you declare an interface with a single abstract method (SAM) using the `fun` keyword, you can implement it with a lambda.

```kotlin
fun interface Validator<T> {
    fun validate(value: T): Boolean
}

// Pass a lambda directly
val emailValidator = Validator<String> { it.contains("@") }
println(emailValidator.validate("user@example.com"))   // true
```

---

#### Kotlin Multiplatform — Path to Stability

| Version | Status |
|---------|--------|
| Before 1.4 | Experimental |
| 1.4 to 1.8 | Alpha/Beta |
| 1.9.20 | **Kotlin Multiplatform Stable** |
| 2.0+ | Android/iOS/JVM/Web targets stable |

Starting with Kotlin 1.9.20, Kotlin Multiplatform (KMP) reached Stable status. You can target iOS, Android, JVM, JavaScript, WASM, and more from a single Kotlin codebase.

---

#### Context Receivers → Context Parameters

Kotlin 1.6.20 introduced an experimental feature called **Context Receivers**. It allowed a function to require multiple receiver objects as context.

```kotlin
// Context Receivers (1.6.20 to 2.0, experimental) — enable with: -Xcontext-receivers
context(Logger, TransactionScope)
fun processOrder(order: Order) {
    log("Starting order processing: ${order.id}")    // Logger.log()
    beginTransaction()                                // TransactionScope.beginTransaction()
    // ...
}
```

{{< callout type="warning" title="Context Receivers Were Redesigned as Context Parameters" >}}
Context Receivers revealed limitations during stabilization and were **redesigned**. From Kotlin 2.1, they are replaced by a new design called **Context Parameters** (`-Xcontext-parameters`). It gives explicit names to parameters and passes the context using `with(...)` at the call site.

```kotlin
// Context Parameters (2.1+, experimental) — enable with: -Xcontext-parameters
context(logger: Logger, tx: TransactionScope)
fun processOrder(order: Order) {
    logger.log("Starting order processing: ${order.id}")
    tx.beginTransaction()
}
```

Existing Context Receivers-based code will need migration after Kotlin 2.x. See [KEEP-367](https://github.com/Kotlin/KEEP/issues/367) for details.
{{< /callout >}}

---

#### Migration Tips

**1.x → 2.0 Migration**

| Checklist | Action |
|-----------|--------|
| Enable K2 compiler | `languageVersion = "2.0"` in `build.gradle.kts` |
| Check compiler plugin compatibility | Verify K2 support for the plugins you use |
| `Enum.values()` → `.entries` | Performance improvement; the linter warns |
| Adopt `data object` | Apply to value-less cases in `sealed` hierarchies |

```kotlin
// build.gradle.kts — enable K2 compiler
kotlin {
    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_0
    }
}
```

**Default JVM Target Version**

From Kotlin 1.8, the default JVM target is **JVM 8**. Spring Boot 3.x requires JVM 17, so in Spring Boot projects you should specify it explicitly.

```kotlin
// build.gradle.kts
kotlin {
    jvmToolchain(17)   // use Java 17 toolchain
}
```

---

#### Next Steps

- [Glossary](glossary/) - Definitions of core Kotlin terms
- [FAQ](faq/) - Frequently asked questions and answers
- [References](references/) - Links to official release notes
