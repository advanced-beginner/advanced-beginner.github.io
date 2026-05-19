---
bookCollapseSection: true
lastmod: "2026-05-13"
title: Concepts
description: "A learning guide and list of documents covering Kotlin's core concepts."
weight: 2
---

Learn Kotlin's core concepts systematically. From fundamentals to advanced topics, each concept is paired with examples you can immediately apply in real-world projects. Choose an appropriate order based on your level and goals for the most effective learning.

#### Basics

Learn the fundamental building blocks of programming in Kotlin. From variable declarations to data modeling, these are the concepts that form the foundation of Kotlin programming.

| Topic | Content | Key Keywords |
|------|------|-------------|
| [Basic Syntax](basics/) | Expressions, packages, comments | `package`, `import`, expressions |
| [Variables and Types](variables-types/) | Variable declarations, basic types | `val`, `var`, type inference |
| [Functions](functions/) | Function definitions, lambdas | `fun`, `=>`, default values, named arguments |
| [Null Safety](null-safety/) | Safe null handling | `?`, `!!`, `?.`, `?:` |
| [Classes and Objects](classes-objects/) | OOP basics | `class`, `object`, `companion` |
| [Data/Sealed Class](data-sealed-classes/) | Data modeling | `data class`, `sealed class` |
| [Collections](collections/) | Data structures | `List`, `Map`, `Set`, `Sequence` |

It is best to learn the basic concepts in order. Especially, understanding variable declarations and Null Safety first makes the subsequent concepts much easier to grasp.

#### Intermediate

Learn the distinctive features that define Kotlin's expressiveness. Mastering extension functions and scope functions greatly improves code readability.

| Topic | Content | Key Keywords |
|------|------|-------------|
| [Extension Functions](extension-functions/) | Adding methods to existing types | `fun Type.method()` |
| [Scope Functions](scope-functions/) | Object context handling | `let`, `run`, `with`, `apply`, `also` |
| [Generics and Variance](generics-variance/) | Type parameters | `<T>`, `in`, `out` |
| [Delegation](delegation/) | Delegation pattern | `by`, `lazy`, `Delegates.observable` |
| [Inline/Reified](inline-reified/) | Compile-time inlining | `inline`, `reified` |

Intermediate topics are key to making Kotlin code short and clear. You should be able to freely use extension functions and scope functions in order to naturally read and write library code.

#### Advanced

Advanced topics for professional Kotlin development. Covers coroutine-based asynchronous programming, DSL builders, and Kotlin Multiplatform.

| Topic | Content | Key Keywords |
|------|------|-------------|
| [Coroutines Basics](coroutines-basics/) | Suspending functions and builders | `suspend`, `launch`, `async` |
| [Flow and Async Streams](flow-async-streams/) | Reactive streams | `Flow`, `StateFlow`, `SharedFlow` |
| [Coroutines Advanced](coroutines-advanced/) | Context, channels, exceptions | `CoroutineContext`, `Channel` |
| [DSL Builders](dsl-builders/) | Type-safe builders | `@DslMarker`, lambda with receiver |
| [Multiplatform Overview](multiplatform-overview/) | KMP structure | `expect`, `actual`, common/jvm/native |

Advanced topics are best studied after you have a solid understanding of the intermediate material. Coroutines in particular are central to both backend and Android development, so it is worth investing enough time to master them.

#### Learning Guide

We recommend different paths depending on your learning goals.

**If you're just getting started**

Start with basic syntax, then proceed to variables and types, functions, and Null Safety. Understanding Kotlin's core safety mechanism, Null Safety, early on is important.

```text
Basic Syntax -> Variables and Types -> Functions -> Null Safety -> Classes and Objects
```

**If you're doing backend development**

Learn Data Class, collections, extension functions, and scope functions before moving on to coroutines. The patterns frequently used in Spring Boot are all part of this flow.

```text
Data Class -> Collections -> Extension Functions -> Scope Functions -> Coroutines Basics -> Flow
```

**If you're working on async/reactive systems**

Start with Coroutines Basics, then move on to Flow and Coroutines Advanced. These can be applied to Kafka consumers, WebFlux replacements, and Reactive systems.

```text
Coroutines Basics -> Flow -> Coroutines Advanced -> DSL Builders
```

**If you're writing libraries/DSLs**

Learn extension functions and scope functions, then proceed to Inline/Reified and DSL Builders. You'll be able to design type-safe builders on your own.

```text
Extension Functions -> Scope Functions -> Inline/Reified -> DSL Builders
```
