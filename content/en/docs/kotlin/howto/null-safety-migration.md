---
title: "Null Safety Migration"
weight: 2
description: "Handle platform types, @Nullable/@NotNull annotations, and step-by-step incremental migration when porting Java code to Kotlin, plus common NPE patterns."
lastmod: "2026-05-13"
---

Step-by-step instructions for safely handling null-related issues when porting Java code to Kotlin.

**Estimated time**: about 15-20 minutes

{{< callout type="tip" title="TL;DR" >}}
- Types coming from Java are inferred in Kotlin as **platform types** (e.g., `String!`).
- Adding `@Nullable`/`@NotNull` annotations on Java code lets Kotlin infer the correct type.
- Handle platform types defensively with `?.` or `?: default` instead of `!!`.
- Migrate incrementally per class and verify with tests at each step.
{{< /callout >}}

---

## What This Guide Solves

Use this guide in the following situations:

- When you get `NullPointerException` while migrating Java code to Kotlin
- When you can't tell whether the return type of a Java method called from Kotlin is nullable
- When `!!` is overused and null safety loses its meaning
- When you need a safe way to use Java libraries from Kotlin

---

## Before You Start

| Item | Requirement |
|------|-------------|
| Kotlin | 1.9.x or later |
| Existing Java code | `@Nullable`/`@NotNull` annotations recommended |
| IntelliJ IDEA | Supports Java to Kotlin conversion |

**Annotation dependency:**

```kotlin
// build.gradle.kts
dependencies {
    // JSR-305 annotations (@Nullable, @Nonnull)
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    // Or the JetBrains annotations
    implementation("org.jetbrains:annotations:24.0.0")
}
```

---

## Step 1: Understand Platform Types

Kotlin treats Java return types as **platform types**. Platform types appear as `String!` and Kotlin cannot tell whether they're nullable.

```kotlin
// Java code (UserService.java)
public class UserService {
    public String getUserName(int id) {  // May return null
        if (id > 0) return "Alice";
        return null;
    }
}
```

```kotlin
// When called from Kotlin
val service = UserService()
val name = service.getUserName(0)  // Type: String! (platform type)

// Kotlin doesn't know whether it's null → runtime NPE risk
println(name.length)  // NPE if id=0!
```

**Danger of platform types:**

```kotlin
val name: String = service.getUserName(0)   // Compiles, runtime NPE possible
val name: String? = service.getUserName(0)  // Safer choice
```

---

## Step 2: Add Annotations to Java Code

Adding `@Nullable`/`@NotNull` to Java source lets Kotlin infer the exact type.

**Using JetBrains annotations (recommended):**

```java
// UserService.java (after fix)
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public class UserService {
    @Nullable  // May return null
    public String getUserName(int id) {
        if (id > 0) return "Alice";
        return null;
    }

    @NotNull  // Never returns null
    public String getDefaultName() {
        return "Guest";
    }

    public void processUser(@NotNull String name) {  // Parameters too
        // ...
    }
}
```

```kotlin
// Calling from Kotlin after annotations are added
val service = UserService()
val name = service.getUserName(0)  // Type: String? (clearly nullable)
val default = service.getDefaultName()  // Type: String (non-null)

// Compiler enforces null checks
println(name?.length ?: 0)  // Safely handled
println(default.length)     // No null check needed
```

**Using JSR-305 annotations:**

```java
import javax.annotation.Nullable;
import javax.annotation.Nonnull;

public class OrderService {
    @Nullable
    public Order findOrder(@Nonnull String orderId) {
        // orderId is non-null, return value is nullable
        return repository.findById(orderId);
    }
}
```

**Enable JSR-305 processing in Gradle:**

```kotlin
// build.gradle.kts (recommended for Kotlin 2.0+)
kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
```

> The `kotlinOptions { freeCompilerArgs += ... }` block from Kotlin 1.x is deprecated. From Kotlin 2.0, use `compilerOptions { freeCompilerArgs.addAll(...) }` as shown above.

---

## Step 3: Defensive Patterns for Platform Types

When using external libraries you can't annotate, handle nulls defensively.

**Pattern 1: safe-call operator `?.`**

```kotlin
// External library return value (platform type)
val result = externalService.getData()

// Use ?. instead of !!
val length = result?.length ?: 0
val upper  = result?.uppercase()
```

**Pattern 2: Elvis operator `?:` for defaults**

```kotlin
fun processUser(userId: String): String {
    val name = userRepository.findName(userId)  // String! (platform type)
    return name ?: "No name"  // Default if null
}
```

**Pattern 3: requireNotNull / checkNotNull**

```kotlin
fun processOrder(orderId: String) {
    val order = orderService.find(orderId)  // Platform type
    // Throws immediately if null (clearer error than an NPE)
    requireNotNull(order) { "Order not found: $orderId" }
    // After this line, order is smart-cast to non-null
    println(order.status)
}
```

**Pattern 4: null-safe block with let**

```kotlin
val user = userService.getUser(id)  // Platform type

// Only execute when non-null
user?.let { u ->
    println("Name: ${u.name}")
    println("Email: ${u.email}")
}
```

**Patterns to avoid:**

```kotlin
// Overusing !! — defeats null safety, runtime NPE risk
val name = service.getName()!!       // Risky!
val length = service.getData()!!.length  // Risky!

// Forcing a platform type to non-null
val name: String = service.getName()  // No compiler warning, but risky
```

---

## Step 4: Common NPE Patterns

**Pattern 1: nulls inside a collection**

```kotlin
// List from Java (elements may be null)
val javaList: List<String?> = javaService.getNames()  // List<String!>

// Filter out nulls safely
val safeNames: List<String> = javaList.filterNotNull()

// Or transform with mapNotNull
val lengths: List<Int> = javaList.mapNotNull { it?.length }
```

**Pattern 2: null from a Map lookup**

```kotlin
val map: Map<String, String> = javaService.getMap()

// Wrong: Map.get() returns nullable
val value = map["key"]!!  // NPE if key missing

// Right
val value = map["key"] ?: "default"
val value = map.getOrDefault("key", "default")
```

**Pattern 3: lateinit fields**

```kotlin
// Bad: lateinit throws UninitializedPropertyAccessException if accessed before init
class UserControllerLateinit {
    private lateinit var userService: UserService

    // You can check whether it's initialized in advance
    fun isInitialized() = ::userService.isInitialized
}

// Good: nullable + explicit check
class UserControllerNullable {
    private var userService: UserService? = null

    fun getService() = userService ?: throw IllegalStateException("Service not initialized")
}
```

**Pattern 4: nulls with generic types**

```kotlin
// Whether T is nullable or non-null isn't clear from the Java side
fun <T> process(value: T): String {
    // If T is String! and null is passed, value.toString() returns the "null" string
    return value?.toString() ?: "null"
}
```

---

## Step 5: Incremental Migration

Migrating the entire codebase at once is risky. Go class by class.

**Recommended order:**

```text
1. Start with dependency-free utility classes
2. Domain models (convert to data class)
3. Service layer
4. Controllers / entry points
```

**Stage 1: Add annotations to Java classes**

```java
// Add @Nullable/@NotNull to Java code first
@NotNull
public User findUser(@NotNull String id) {
    User user = repository.find(id);
    return Objects.requireNonNull(user, "User not found: " + id);
}
```

**Stage 2: Use IntelliJ's Java-to-Kotlin conversion**

1. Open the Java file to convert
2. `Code → Convert Java File to Kotlin File` (or `Ctrl+Alt+Shift+K`)
3. Review the auto-converted result

**Stage 3: Conversion review checklist**

```kotlin
// What to verify after auto-conversion:

// 1. Did platform types convert to appropriate nullable types?
var name: String? = null      // OK
var name: String = ""         // OK if Java side is non-null

// 2. Is `!!` used excessively?
val result = javaMethod()!!   // Review → replace with `?:` or requireNotNull

// 3. Is `lateinit` used appropriately?
lateinit var service: Service  // Only for DI targets

// 4. Are equals/hashCode generated correctly?
// Auto-generated for data class
```

**Stage 4: Verify with tests**

```kotlin
import kotlin.test.*

class UserServiceTest {

    @Test
    fun `handles null return case`() {
        val service = UserService()
        val name = service.getUserName(-1)  // returns null
        assertNull(name)

        // Verify Kotlin null handling
        val displayName = name ?: "Guest"
        assertEquals("Guest", displayName)
    }

    @Test
    fun `non-null return case`() {
        val service = UserService()
        val name = service.getUserName(1)
        assertNotNull(name)
        assertEquals("Alice", name)
    }
}
```

---

## Step 6: Caveats in Spring Boot

When using with Spring Boot, watch out for these:

```kotlin
// @RequestParam, @PathVariable are required=true by default
// Use String (not String?) in Kotlin to stay safer
@GetMapping("/users/{id}")
fun getUser(@PathVariable id: String): ResponseEntity<User> {
    // id is non-null (guaranteed by Spring)
    return ResponseEntity.ok(userService.findById(id))
}

// Optional parameters should be String?
@GetMapping("/users")
fun searchUsers(
    @RequestParam(required = false) name: String?
): List<User> {
    return if (name != null) {
        userService.findByName(name)
    } else {
        userService.findAll()
    }
}

// Prefer constructor injection over `lateinit var` for @Autowired
@Service
class UserService(
    private val userRepository: UserRepository,  // non-null, final
    private val cacheService: CacheService
)
```

---

## Checklist

Verify migration is complete:

- [ ] Are `@Nullable`/`@NotNull` annotations added to Java code?
- [ ] Is `-Xjsr305=strict` enabled in Gradle?
- [ ] Is `!!` usage minimized? (Use `requireNotNull` only where necessary)
- [ ] Are platform types from external libraries handled with `?.` and `?:`?
- [ ] Are nullable elements in collections handled with `filterNotNull()`?
- [ ] Did you run tests after each class transition to confirm no NPE?

---

## Related Documents

- [Null Safety](../concepts/null-safety/) — Kotlin null-handling concepts
- [Coroutines Basics](../concepts/coroutines-basics/) — null handling in suspend functions
- [Gradle Kotlin DSL Tips](gradle-kotlin-dsl-tips/) — build configuration for migration
