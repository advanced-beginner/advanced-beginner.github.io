---
bookCollapseSection: true
lastmod: "2026-05-13"
title: Appendix
description: "Kotlin appendix materials and reference document list."
weight: 5
---

Reference materials to support Kotlin learning. We have gathered supplementary resources you may need during the learning process, including a glossary, version comparison, FAQ, and reference links.

#### Appendix Index

The table below lists the documents provided in the appendix. Each document is useful for reference during learning or when you need quick information on a specific topic.

| Document | Description |
|----------|-------------|
| [Glossary](glossary/) | Definitions of core Kotlin terms |
| [Version Comparison](version-comparison/) | Summary of major changes from Kotlin 1.x to 2.x |
| [FAQ](faq/) | Frequently asked questions and answers |
| [References](references/) | Official docs, books, and course links |

#### Quick Reference

A collection of commands and code patterns frequently used in everyday Kotlin development.

**Frequently Used Gradle Commands**

Gradle Kotlin DSL is the standard build tool for Kotlin projects.

| Command | Description |
|---------|-------------|
| `./gradlew run` | Run the application |
| `./gradlew build` | Compile + test + package |
| `./gradlew test` | Run tests |
| `./gradlew clean` | Delete build artifacts |
| `./gradlew bootRun` | Run the Spring Boot application |
| `./gradlew run --continuous` | Auto-run on file changes |
| `./gradlew dependencies` | Print the dependency tree |

**Null Safety Operators**

Kotlin's core safety mechanisms — the null-related operators.

```kotlin
val nullable: String? = maybeNull()

// Safe call
val length = nullable?.length            // returns null if null

// Elvis operator
val safeLength = nullable?.length ?: 0   // default value if null

// Non-null assertion (avoid when possible)
val forced = nullable!!.length           // throws NullPointerException if null

// Safe cast
val asInt = (any as? Int) ?: -1
```

**Scope Functions — Quick Comparison**

| Function | Context Object | Return Value | Main Use |
|----------|----------------|--------------|----------|
| `let` | `it` | Lambda result | Nullable handling, transformation |
| `run` | `this` | Lambda result | Object configuration + result computation |
| `with` | `this` | Lambda result | Multiple calls on a non-null object |
| `apply` | `this` | The object itself | Object initialization |
| `also` | `it` | The object itself | Side effects (logging, etc.) |

**Key Collection Operations**

The core operations of Kotlin collections. You can transform, filter, and aggregate data in a functional style.

```kotlin
val nums = listOf(1, 2, 3, 4, 5)

nums.map { it * 2 }            // [2, 4, 6, 8, 10]
nums.filter { it % 2 == 0 }    // [2, 4]
nums.reduce { acc, n -> acc + n }   // 15
nums.fold(0) { acc, n -> acc + n }  // 15
nums.find { it > 3 }           // 4
nums.any { it > 3 }            // true
nums.all { it > 0 }            // true
nums.take(2)                   // [1, 2]
nums.drop(2)                   // [3, 4, 5]
nums.chunked(2)                // [[1, 2], [3, 4], [5]]
nums.groupBy { it % 2 }        // {1=[1, 3, 5], 0=[2, 4]}
```

**Coroutine Builders — Quick Reference**

```kotlin
import kotlinx.coroutines.*

// runBlocking - blocks the main thread to run a coroutine (tests, main function)
runBlocking {
    delay(1000)
    println("done")
}

// launch - asynchronous task that does not need a result (returns Job)
val job = CoroutineScope(Dispatchers.IO).launch {
    doWork()
}

// async - asynchronous task that needs a result (returns Deferred)
val deferred = CoroutineScope(Dispatchers.IO).async {
    fetchValue()
}
val result = deferred.await()
```
