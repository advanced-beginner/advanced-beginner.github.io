---
title: "FAQ"
description: "Answers to 15 frequently asked questions from developers new to Kotlin."
weight: 3
lastmod: "2026-05-13"
---

> **Estimated time**: about 10 minutes

A collection of questions that frequently come up when first encountering Kotlin or adopting it in production.

---

#### Q1. Can Kotlin and Java be used together in one project?

**A.** Yes, fully. Kotlin and Java compile to the same JVM bytecode, so you can freely mix them within the same project.

```kotlin
// Kotlin file
class KotlinService {
    fun getGreeting(): String = "Hello from Kotlin"
}
```

```java
// Using a Kotlin class from a Java file
KotlinService service = new KotlinService();
System.out.println(service.getGreeting());
```

{{< callout type="info" title="Java Interop Tips" >}}
- A Kotlin `data class` is used as a regular class in Java
- Use `@JvmOverloads` to expose default-argument functions as Java overloads
- Use `@JvmStatic` to call `companion object` members as Java static methods
- Top-level Kotlin functions are called as `FilenameKt.functionName()` in Java
{{< /callout >}}

---

#### Q2. What is the difference between a coroutine and a thread?

**A.** A coroutine is a lightweight execution unit that runs **cooperatively**. Threads are heavy OS-managed resources, while coroutines run inside threads and can be created by the thousands with negligible overhead.

| Item | Thread | Coroutine |
|------|--------|-----------|
| Resources | About 1MB stack | A few KB |
| Creation cost | Relatively high | Very low |
| Switching | Preemptive by OS | Cooperative suspension |
| Blocking | Thread blocking | suspend (releases the thread) |
| Concurrent count | Hundreds to thousands | Tens of thousands to hundreds of thousands |

```kotlin
import kotlinx.coroutines.*

// 1000 coroutines instead of 1000 threads
fun main() = runBlocking {
    repeat(1_000) {
        launch {
            delay(1000)
            print(".")
        }
    }
}
```

---

#### Q3. Can I use a `data class` as a JPA Entity?

**A.** Not recommended. JPA internally requires a **no-argument constructor** and **mutable state**, neither of which `data class` supports by default. In addition, `data class` `equals`/`hashCode` can conflict with proxy objects.

```kotlin
// Not recommended
@Entity
data class UserEntity(val id: Long, val name: String)

// Recommended pattern
@Entity
class UserEntity(
    @Id
    var id: Long? = null,
    var name: String = ""
) {
    // Implement equals/hashCode based on id directly,
    // or use the default reference equality
}

// Use data class for DTOs/value objects
data class UserDto(val id: Long, val name: String)
```

---

#### Q4. What is the difference between `lateinit var` and `by lazy`?

**A.** Both defer initialization, but their purpose and usage differ.

| Item | `lateinit var` | `by lazy { }` |
|------|----------------|---------------|
| Modifier | `var` | `val` |
| Initialization timing | Manual (set directly in code) | Automatic on first access |
| Allows null | No | No |
| Primitive types | Not allowed | Allowed |
| Init check | `::field.isInitialized` | Always initialized |
| Thread safety | Must be handled manually | Synchronized by default |
| Primary use | DI frameworks, test setup | Expensive computations, config loading |

```kotlin
class UserService {
    lateinit var repository: UserRepository  // injected externally (DI)

    val cache: Map<Long, User> by lazy {
        println("Initializing cache...")
        loadInitialCache()  // runs only on first access to cache
    }
}
```

---

#### Q5. How do extension functions look from Java?

**A.** An extension function compiles to a **static method that takes the receiver as its first parameter** in Java.

```kotlin
// Kotlin (StringExtensions.kt)
fun String.addPrefix(prefix: String): String = "$prefix$this"
```

```java
// Calling from Java
String result = StringExtensionsKt.addPrefix("World", "Hello, ");
// In Kotlin: "World".addPrefix("Hello, ")
```

To change the file name, use `@file:JvmName("StringUtils")`.

---

#### Q6. Does `when` always need an `else` branch?

**A.** When you use a `sealed class` or `enum class` as the `when` subject, all cases are handled and `else` is not required. If you omit `else`, the compiler warns about missing cases.

```kotlin
sealed class Status { object Active : Status(); object Inactive : Status() }

// else not needed — compiler checks exhaustiveness
fun describe(s: Status) = when (s) {
    is Status.Active   -> "active"
    is Status.Inactive -> "inactive"
}

// else required — Int has infinite cases
fun describe(n: Int) = when (n) {
    1 -> "one"
    2 -> "two"
    else -> "other"
}
```

---

#### Q7. How do I handle exceptions in coroutines?

**A.** `launch` and `async` differ in how they handle exceptions.

```kotlin
// launch — CoroutineExceptionHandler or try-catch
val handler = CoroutineExceptionHandler { _, exception ->
    println("Error handled: ${exception.message}")
}

CoroutineScope(Dispatchers.IO + handler).launch {
    throw RuntimeException("launch error")
}

// async — the exception is raised when await() is called
val scope = CoroutineScope(Dispatchers.IO)
val deferred = scope.async {
    throw RuntimeException("async error")
}
try {
    deferred.await()  // exception thrown here
} catch (e: RuntimeException) {
    println("Error handled: ${e.message}")
}
```

---

#### Q8. How do `object` and `companion object` differ?

**A.** `object` is a standalone singleton; `companion object` is a singleton bound to a specific class.

```kotlin
// Standalone singleton
object AppLogger {
    fun log(msg: String) = println("[LOG] $msg")
}
AppLogger.log("start")

// Singleton bound to a class — accessed via the class name
class User private constructor(val name: String) {
    companion object {
        fun create(name: String) = User(name)
    }
}
val user = User.create("Hong Gildong")
```

---

#### Q9. What is the difference between `==` and `===`?

**A.** In Kotlin, `==` is a **structural equality** check that calls `equals()`. `===` is a **referential identity** check.

```kotlin
val a = "hello"
val b = "hello"
val c = a

println(a == b)    // true — contents are equal
println(a === b)   // true — may be the same object in the JVM String pool

data class Point(val x: Int, val y: Int)
val p1 = Point(1, 2)
val p2 = Point(1, 2)

println(p1 == p2)    // true — data class equals
println(p1 === p2)   // false — different objects
```

---

#### Q10. What is the difference between `List` and `MutableList`?

**A.** `List` is a read-only view; `MutableList` is a mutable view. The object returned by `listOf()` may actually be an `ArrayList`, but through the `List` interface, no modification methods are exposed.

```kotlin
val readOnly: List<Int> = listOf(1, 2, 3)
// readOnly.add(4)  // compile error

val mutable: MutableList<Int> = mutableListOf(1, 2, 3)
mutable.add(4)  // OK

// Convert read-only to mutable
val converted = readOnly.toMutableList()
converted.add(4)   // OK (creates a new MutableList)
```

---

#### Q11. When should I use Kotlin `inline` functions?

**A.** Mainly for **higher-order functions that take lambdas as parameters**. A lambda creates an object on each call; with `inline`, the code is inserted at the call site so there is no object-creation overhead. It also enables `reified` type parameters.

```kotlin
// Without inline — a lambda object is created
fun <T> measure(block: () -> T): T {
    val start = System.currentTimeMillis()
    val result = block()
    println("Elapsed: ${System.currentTimeMillis() - start}ms")
    return result
}

// inline — the body is inserted at the call site
inline fun <T> measureInline(block: () -> T): T {
    val start = System.currentTimeMillis()
    val result = block()
    println("Elapsed: ${System.currentTimeMillis() - start}ms")
    return result
}
```

---

#### Q12. How do I implement the singleton pattern in Kotlin?

**A.** Use an `object` declaration. It is thread-safe and lazily initialized.

```kotlin
object DatabasePool {
    private val connections = mutableListOf<Connection>()

    fun getConnection(): Connection {
        return connections.firstOrNull() ?: createConnection()
    }

    private fun createConnection(): Connection {
        val conn = Connection()
        connections.add(conn)
        return conn
    }
}

// Usage
val conn = DatabasePool.getConnection()
```

---

#### Q13. What is the best way to convert `String?` to `String`?

**A.** It depends on the situation.

```kotlin
val nullable: String? = getMaybeNull()

// 1. Substitute with a default value (most common)
val safe = nullable ?: ""
val withDefault = nullable ?: "unknown"

// 2. Early-return from a function on null
fun process(s: String?) {
    val value = s ?: return
    println(value)
}

// 3. Throw on null (for required values)
val required = requireNotNull(nullable) { "Value is required" }

// 4. Different handling on null
nullable?.let { value ->
    println("Value: $value")
} ?: println("No value")
```

---

#### Q14. What is the difference between Flow and LiveData/RxJava?

**A.** Kotlin Flow is an asynchronous stream built on coroutines.

| Item | Flow | LiveData | RxJava |
|------|------|----------|--------|
| Platform | General Kotlin | Android only | General |
| Base | Coroutines | Android Lifecycle | Its own threading model |
| Subscription cancellation | Structured Concurrency | Automatic via Lifecycle | Manual via Disposable |
| Learning curve | Moderate | Low | High |
| Error handling | try-catch, catch operator | Handle directly | onError |

---

#### Q15. What is the difference between a Kotlin Script (.kts) and a regular .kt file?

**A.** A `.kts` file is a Kotlin script file: you can execute top-level code directly without a `main` function. A representative example is a Gradle build script (`build.gradle.kts`).

```kotlin
// script.kts — runs directly without main
val message = "Hello, Script!"
println(message)

// Run the script
// kotlinc -script script.kts
```

```kotlin
// Main.kt — requires a main function
fun main() {
    val message = "Hello, Kotlin!"
    println(message)
}
```

---

#### Next Steps

- [Glossary](glossary/) - Check core term definitions
- [Version Comparison](version-comparison/) - Understand changes across versions
- [References](references/) - Additional learning resources
