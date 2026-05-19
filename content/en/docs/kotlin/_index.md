---
title: Kotlin
bookCollapseSection: true
description: A guide to Kotlin, the concise and safe JVM language - from basic syntax and Null Safety to coroutines, Spring Boot integration, and Multiplatform
weight: 6
lastmod: "2026-05-13"
---

#### What is Kotlin?

Kotlin is a <strong>multi-paradigm statically typed language</strong> developed by JetBrains. It targets a wide range of platforms including JVM, Android, JavaScript, and Native, designed with the goal of being a "pragmatic and safe programming language." Since being adopted as the official Android language in 2017, its usage has rapidly expanded into backend development (Spring Boot), data engineering, and multiplatform development.

**Core Values of Kotlin**

Kotlin is designed around the following four core values.

| Value | Meaning |
|-------|---------|
| Safety | Eliminates null reference errors through the type system |
| Conciseness | Removes boilerplate, intent-focused code |
| Interoperability | 100% compatible with existing JVM libraries |
| Tooling | Powerful IDE support backed by IntelliJ |

**When Should You Use Kotlin?**

Decide whether to adopt Kotlin based on your project characteristics.

**Suitable cases:**
- Android application development (officially recommended)
- Spring Boot-based backends (Kotlin DSL, coroutine-friendly)
- Asynchronous processing in Kafka and reactive systems
- Multiplatform projects targeting iOS, Android, and Web from a single codebase
- Systems where null safety and type safety are critical

**May be overkill:**
- Simple scripts or one-off tools
- Teams with no JVM experience where Python or Go would be a better fit

#### What This Guide Covers

This guide is structured to help you learn Kotlin step by step, from language fundamentals to coroutines, Spring Boot/Kafka integration, and Kotlin Multiplatform.

**[Quick Start](quick-start/)**
Install Kotlin and run your first program in 5 minutes.

**[Concepts](concepts/)**

Learn Kotlin's core concepts categorized into basic, intermediate, and advanced levels.

**Basics:**

| Topic | What You'll Learn |
|-------|-------------------|
| [Basic Syntax](concepts/basics/) | Variables, constants, expressions, packages |
| [Variables and Types](concepts/variables-types/) | val/var, basic types, type inference |
| [Functions](concepts/functions/) | fun, default values, named arguments, lambdas |
| [Null Safety](concepts/null-safety/) | `?`, `!!`, `?.`, `?:` operators |
| [Classes and Objects](concepts/classes-objects/) | class, object, companion |
| [Data/Sealed Class](concepts/data-sealed-classes/) | Immutable data, closed hierarchies |
| [Collections](concepts/collections/) | List, Map, Set, Sequence |

**Intermediate:**

| Topic | What You'll Learn |
|-------|-------------------|
| [Extension Functions](concepts/extension-functions/) | Add methods to existing types |
| [Scope Functions](concepts/scope-functions/) | let, run, with, apply, also |
| [Generics and Variance](concepts/generics-variance/) | `<T>`, in/out, type bounds |
| [Delegation](concepts/delegation/) | by, lazy, observable |
| [Inline/Reified](concepts/inline-reified/) | Inline functions, reified type parameters |

**Advanced:**

| Topic | What You'll Learn |
|-------|-------------------|
| [Coroutines Basics](concepts/coroutines-basics/) | suspend, launch, async, await |
| [Flow and Async Streams](concepts/flow-async-streams/) | Flow, StateFlow, SharedFlow |
| [Advanced Coroutines](concepts/coroutines-advanced/) | Context, Scope, Channel, exceptions |
| [DSL Builders](concepts/dsl-builders/) | Type-safe builder pattern |
| [Multiplatform Overview](concepts/multiplatform-overview/) | KMP structure, expect/actual |

**[Hands-on Examples](examples/)**

Runnable example projects based on Gradle Kotlin DSL. Covers everything from basic Kotlin usage to Spring Boot/Kafka integration, real-world coroutine application, and a Kotlin Multiplatform mini project.

- [Environment Setup](examples/setup/) - JDK, Gradle Kotlin DSL, IDE
- [Basic Examples](examples/basic/) - Hello Kotlin, basic concept usage
- [Spring Boot Integration](examples/spring-boot-integration/) - Getting started with Kotlin + Spring Boot
- [Kafka Integration](examples/kafka-integration/) - Producer/Consumer in Kotlin
- [Coroutines in Practice](examples/coroutines-practical/) - Real-world async scenarios
- [Multiplatform Starter](examples/multiplatform-intro/) - KMP mini project

**[How-To Guide](howto/)**

Step-by-step guides for solving specific problems.

- [Coroutine Debugging](howto/coroutine-debugging/) - Tracing async code
- [Null Safety Migration](howto/null-safety-migration/) - Safely migrating Java code to Kotlin
- [Gradle Kotlin DSL Tips](howto/gradle-kotlin-dsl-tips/) - Practical build script know-how
- [Kotest vs JUnit](howto/kotest-vs-junit/) - Choosing a test framework
- [Performance Profiling](howto/performance-profiling/) - JVM performance tuning

**[Appendix](appendix/)**

- [Glossary](appendix/glossary/) - Core Kotlin terms
- [Version Comparison](appendix/version-comparison/) - Changes from Kotlin 1.x to 2.x
- [FAQ](appendix/faq/) - Frequently asked questions
- [References](appendix/references/) - Official docs and additional learning resources

#### Get Started Right Now

> 🚀 **Try Without Installing:** Run Kotlin directly in your browser at [Kotlin Playground](https://play.kotlinlang.org/).

```kotlin
// Try running this code in Kotlin Playground
fun main() {
    val name = "Kotlin"
    println("Hello, $name!")
}
```

#### Prerequisites

- **Required**: Programming fundamentals (variables, functions, conditionals, loops)
- **Helpful**: Experience with a JVM language (such as Java), object-oriented concepts, basic Gradle or Maven usage

#### Suggested Learning Paths

Recommended learning paths based on your goals.

```text
If you're new:        Quick Start -> Basic Syntax -> Variables and Types -> Functions -> Null Safety
For backend:          Classes -> Data Class -> Extension Functions -> Spring Boot Integration -> Kafka Integration
For async/streams:    Coroutines Basics -> Flow -> Advanced Coroutines -> Coroutines in Practice
For multiplatform:    Multiplatform Overview -> Multiplatform Starter
```

> 💡 **Related Reading**: Once you complete the backend learning path, expand into the [Kafka Guide]({{< relref "/docs/kafka" >}}) and [DDD Guide]({{< relref "/docs/ddd" >}}) to learn messaging-based domain modeling.

#### Common Misconceptions

Let's clear up a few common misconceptions about Kotlin.

**"Kotlin is just an Android language"** — Kotlin targets JVM backend, JavaScript, and Native. With official Spring Boot support, Ktor, and Kotlin Multiplatform, its scope is very wide.

**"If you know Java, you can just use Kotlin"** — The syntax is familiar, but Kotlin-specific paradigms like Null Safety, extension functions, coroutines, and scope functions require separate learning.

**"Coroutines are just a thread pool abstraction"** — Coroutines are an asynchronous programming model featuring structured concurrency, cooperative cancellation, and backpressure.
