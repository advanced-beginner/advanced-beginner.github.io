---
title: Advanced Beginner
description: Beyond basics to real-world practice - A technical guide that starts from core principles
---

## What is this site?

You've followed tutorials and built "Hello World", but when facing complex real-world situations, you're not sure **why things should be done a certain way** — this guide bridges that gap.

Rather than just "do it this way", we explain **why such design is necessary** from first principles. When you understand the principles, you can make the right decisions even in situations not covered by documentation.

## Who is this guide for?

| This guide is for you if... | This guide is NOT for you if... |
|---------------------|-------------------------|
| You know the basics but struggle with real-world application | You're a complete beginner (start with basic tutorials first) |
| You want to understand the "why" | You only want copy-paste solutions |
| You're a Java/Scala developer with Spring Boot experience | - |
| You want to understand design principles and trade-offs | You only need a quick reference |

## Available Guides

### [Apache Kafka](kafka/)

Practical usage of distributed messaging systems. From Producer/Consumer basics to transactions, replication, failure handling, and performance tuning.

**What you'll learn:**
- Core components of Kafka and message flow
- Consumer Group and Offset management strategies
- Achieving exactly-once delivery with transactions
- Real-world error handling patterns and monitoring

### [Domain-Driven Design](ddd/)

A design methodology for systematically handling complex business logic. From strategic design to tactical patterns, CQRS, and event sourcing.

**What you'll learn:**
- Dividing system boundaries with Bounded Contexts
- Designing consistent domain models with Aggregates
- Implementing loose coupling with Domain Events
- Practical order domain implementation examples

### [Scala](scala/)

A JVM language combining functional and object-oriented programming. From basic syntax to advanced type systems and functional patterns. Covers both Scala 2.13 and Scala 3 syntax.

**What you'll learn:**
- Scala basics and functional programming fundamentals
- Pattern Matching and Case Classes
- Type system: Generics, Variance, Type Classes
- Implicits/Given and principles of implicit conversions

## Characteristics of this guide

**First Principles** — We start from the fundamental problems the technology solves, not just surface-level usage. Understanding "why" before "how" builds adaptability.

**Executable Examples** — All code actually runs. We provide examples you can verify immediately after concept explanations.

**Real-world Perspective** — What's theoretically perfect and what works in production are different. We cover trade-offs and realistic choices together.

## Where should I start?

- **New to Kafka** → [Kafka Quick Start](kafka/quick-start/)
- **Know Kafka basics but want deeper understanding** → [Kafka Core Components](kafka/concepts/core-components/)
- **Curious about what DDD is** → [DDD Quick Start](ddd/quick-start/)
- **Want to learn domain model design** → [Tactical Design](ddd/concepts/tactical-design/)
- **New to Scala** → [Scala Quick Start](scala/quick-start/)
- **Want deep understanding of Scala's type system** → [Type Classes](scala/concepts/type-classes/)
