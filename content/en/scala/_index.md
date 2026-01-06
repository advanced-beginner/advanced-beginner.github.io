---
lastmod: "2026-01-06"
title: Scala
weight: 3
---

## What is Scala?

Scala is a JVM-based language that combines **functional programming** and **object-oriented programming**. Short for "Scalable Language", it's designed to scale from small scripts to large distributed systems.

### Why Scala?

| Java's Limitations | Scala's Solutions |
|-------------------|-------------------|
| Verbose code | Concise and expressive syntax |
| Limited type system | Powerful type inference and advanced type features |
| Lack of functional programming support | First-class functions, immutability, pattern matching |
| Null reference problems | Safe null handling with Option type |

Scala is 100% compatible with Java while enabling safer and more expressive code.

### When Should You Use Scala?

**Suitable cases:**
- Large-scale data processing (Apache Spark)
- Distributed system development (Akka, Akka HTTP)
- When you want to apply functional programming
- Projects where type safety is important
- Systems requiring concurrency/parallelism

**May be overkill:**
- Simple CRUD web applications
- When the team has no Scala experience and deadlines are tight
- When Java ecosystem is sufficient

### Scala 2 vs Scala 3

This guide covers both **Scala 2.13** and **Scala 3**.

| Aspect | Scala 2.13 | Scala 3 |
|--------|-----------|---------|
| Status | Currently most widely used | Latest version, gradually spreading |
| Syntax | Brace-based | Indentation-based option |
| implicit | implicit keyword | Improved with given/using |
| Enums | sealed trait + case object | enum keyword |
| Type System | Powerful | Union, Intersection types added |

> **Recommendation:** Choose Scala 3 for new projects, Scala 2.13 for existing projects or when using Spark.

## What This Guide Covers

### [Quick Start](quick-start/)
Install Scala and run your first program in 5 minutes.

### [Concepts](concepts/)

**Fundamentals:**

| Topic | What You'll Learn |
|-------|-------------------|
| [Basic Syntax](concepts/basics/) | Variables, constants, basic types, type inference |
| [Control Structures](concepts/control-structures/) | if, for, while, match expressions |
| [Functions & Methods](concepts/functions-methods/) | def, lambdas, default values, varargs |
| [Classes & Objects](concepts/classes-objects/) | class, object, trait, enum |
| [Case Classes](concepts/case-classes/) | Immutable data modeling |
| [Pattern Matching](concepts/pattern-matching/) | Powerful use of match expressions |

**Intermediate:**

| Topic | What You'll Learn |
|-------|-------------------|
| [Collections](concepts/collections/) | List, Set, Map, Seq, Vector |
| [Higher-Order Functions](concepts/higher-order-functions/) | map, filter, fold, currying |
| [Generics](concepts/generics/) | Type parameters, type bounds |
| [For Comprehension](concepts/for-comprehensions/) | Elegant expression of monadic operations |
| [Implicit/Given](concepts/implicits/) | Implicit conversions and contextual abstraction |

**Advanced:**

| Topic | What You'll Learn |
|-------|-------------------|
| [Type Classes](concepts/type-classes/) | Ad-hoc polymorphism pattern |
| [Variance](concepts/variance/) | Covariance/contravariance of generic types |
| [Advanced Types](concepts/type-system-advanced/) | Union, Intersection, Match Types |
| [Macros](concepts/macros-metaprogramming/) | Compile-time metaprogramming |
| [Concurrency](concepts/concurrency/) | Future, Promise, ExecutionContext |
| [Functional Patterns](concepts/functional-patterns/) | Functor, Monad, referential transparency |

### [Hands-on Examples](examples/)
Executable example projects based on sbt.

- [Environment Setup](examples/setup/) - sbt, IDE configuration
- [Basic Examples](examples/basic/) - Core concept examples
- [Scala 2 vs 3 Comparison](examples/scala2-vs-scala3/) - Version-specific code comparison

### [Appendix](appendix/)
- [Glossary](appendix/glossary/) - Quick reference for Scala terms
- [Version Comparison](appendix/version-comparison/) - Scala 2 vs 3 differences summary
- [FAQ](appendix/faq/) - Frequently asked questions
- [References](appendix/references/) - Official docs and additional learning resources

## Try It Now

> 🚀 **No installation needed:** Try Scala directly in your browser at [Scastie](https://scastie.scala-lang.org/)!

```scala
// Try running this code in Scastie
@main def hello() =
  val name = "Scala"
  println(s"Hello, $name!")
```

## Prerequisites

- **Required**: Programming basics (variables, functions, conditionals, loops)
- **Helpful**: Java experience, OOP concepts, functional programming basics

## Suggested Learning Path

```
If you're new:      Quick Start → Basic Syntax → Control Structures → Functions → Classes
Data processing:    Collections → Higher-Order Functions → For Comprehension → Basic Examples
Advanced:           Type Classes → Variance → Functional Patterns → Concurrency
Scala 3 migration:  Implicit/Given → Version Comparison → Advanced Types
```

## Common Misconceptions

**"Scala is too difficult"** — Basic syntax is more concise than Java. Learn advanced features gradually as needed.

**"Scala is a dying language"** — Still critically used in major projects like Apache Spark, Kafka, and Akka. Scala 3 is actively evolving.

**"You must use only functional style"** — Scala is a multi-paradigm language. Mix OOP and FP as appropriate for the situation.
