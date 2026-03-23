---
bookCollapseSection: true
title: Architecture Patterns
description: "A learning guide and document list for DDD architecture patterns."
weight: 5
lastmod: "2026-01-15"
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

> **Target Audience**: Developers and architects who need to choose a project architecture
> **Prerequisites**: Understanding of [Tactical Design]({{< relref "/docs/ddd/concepts/tactical-design" >}}) building blocks
> **Section Purpose**: Understand the philosophy of architecture patterns and make appropriate choices for your project

{{< callout type="info" title="What is the Architecture Section?" >}}
This section covers <strong>architecture patterns</strong> for effectively implementing DDD. Good architecture enables you to build systems that protect business logic, are flexible to change, and are easy to test.
{{< /callout >}}

## Overview Analogy Table

Here are analogies to understand architecture patterns at a glance.

| Architecture | Analogy | Core Philosophy | One-Line Summary |
|-------------|---------|-----------------|-----------------|
| **[Layered]({{< relref "/docs/ddd/concepts/architecture/layered-architecture" >}})** | Building floors | Simple structure flowing from top to bottom | "Separate layers by role" |
| **[Hexagonal]({{< relref "/docs/ddd/concepts/architecture/hexagonal-architecture" >}})** | Smartphone and adapters | Isolate external world with Ports/Adapters | "Protect the inside from the outside" |
| **[Clean]({{< relref "/docs/ddd/concepts/architecture/clean-architecture" >}})** | Concentric circles of a castle | Dependencies always point inward | "Business rules are king" |
| **[Onion]({{< relref "/docs/ddd/concepts/architecture/onion-architecture" >}})** | Onion layers | Domain model at the very center | "Domain is the center of everything" |
| **[CQRS]({{< relref "/docs/ddd/concepts/architecture/cqrs" >}})** | Separate read/write counters | Complete separation of commands and queries | "Optimize reads and writes separately" |
| **[Event-Driven]({{< relref "/docs/ddd/concepts/architecture/event-driven" >}})** | Company announcement system | Loose coupling and asynchronous communication | "Announce what happened and react" |

## Why Do You Need Architecture Patterns?

When you first start a project, it is fine to have all the code in one place. But as time passes and features are added, the code becomes increasingly complex and difficult to navigate. Architecture patterns provide methods to prevent this chaos and systematically organize your system.

{{< callout type="tip" title="Analogy: Building Design" >}}
You can compare software architecture to <strong>building design</strong>:

- **No architecture**: A studio apartment where all rooms are connected. Cooking in the kitchen makes the bedroom smell, and when guests come, you have to tidy up everything.
- **Layered**: A building with designated floors. Ground floor retail, second floor offices, third floor residential. Upper floors depend on lower floors.
- **Hexagonal**: A central hall with multiple entrances. Whether you enter from the front door, back door, or underground parking, you arrive at the same hall. Changing entrances (adapters) does not affect the internal structure.
- **Clean**: A castle with concentric circles. Moat (external world) -> walls (frameworks) -> inner keep (business logic) -> tower (core entities). You can only enter from outside to inside.

**Key question**: "How much do you need to protect your business logic (the tower) from external changes?"
{{< /callout >}}

## Core Principle: Dependency Direction

The most important principle shared by all architecture patterns is the direction of dependencies. Business logic (domain) should not depend on anything. Everything else should depend on the business logic.

```mermaid
flowchart TB
    subgraph Wrong["Incorrect Dependency Direction"]
        W1["Business Logic"]
        W2["Database"]
        W3["External API"]
        W1 --> W2
        W1 --> W3
    end

    subgraph Right["Correct Dependency Direction"]
        R1["Business Logic"]
        R2["Database Adapter"]
        R3["External API Adapter"]
        R2 --> R1
        R3 --> R1
    end
```

## Philosophy of Each Architecture Pattern

Each architecture pattern has its own philosophy and design goals.

### Layered Architecture

**Philosophy**: "Separate concerns so each layer focuses on its own role"

- **Background**: Solving the problem of unmaintainable mixed code
- **Core Idea**: Vertical separation, calls only flow from top to bottom
- **Values**: Simplicity, easy to understand, rapid development

### Hexagonal Architecture

**Philosophy**: "Completely isolate the application core from the external world"

- **Background**: Solving the problem of external system changes affecting business logic
- **Core Idea**: Define boundaries with Ports and Adapters
- **Values**: Testability, flexibility in technology replacement

### Clean Architecture

**Philosophy**: "Dependencies must always point inward"

- **Background**: Maintenance issues with framework-dependent code
- **Core Idea**: Strict dependency rules, concentric circle structure
- **Values**: Framework independence, long-term maintainability

### Onion Architecture

**Philosophy**: "The domain model is king. Everything else serves the domain"

- **Background**: Need for a structure to effectively implement DDD
- **Core Idea**: Place the domain model at the very center
- **Values**: Domain purity, DDD-friendliness

### CQRS

**Philosophy**: "Reads and writes are fundamentally different problems, so separate them"

- **Background**: Different optimization requirements for queries and commands
- **Core Idea**: Separate Command and Query models
- **Values**: Optimization tailored to each, scalability

### Event-Driven Architecture

**Philosophy**: "Announce what happened and let interested parties react"

- **Background**: Flexibility issues with tightly coupled systems
- **Core Idea**: Loose coupling through domain events
- **Values**: Scalability, loose coupling, asynchronous processing

## Feature Comparison by Pattern

| Pattern | Core Concept | Difficulty | Suitable For |
|---------|-------------|-----------|-------------|
| **[Layered]({{< relref "/docs/ddd/concepts/architecture/layered-architecture" >}})** | 4 layers flowing top to bottom | Easy | Getting started, simple projects |
| **[Hexagonal]({{< relref "/docs/ddd/concepts/architecture/hexagonal-architecture" >}})** | Isolate externals with Ports and Adapters | Medium | Projects with many external integrations |
| **[Clean]({{< relref "/docs/ddd/concepts/architecture/clean-architecture" >}})** | Strict dependency rules | Hard | Large-scale, long-term projects |
| **[Onion]({{< relref "/docs/ddd/concepts/architecture/onion-architecture" >}})** | Domain model centric | Medium | DDD projects |
| **[CQRS]({{< relref "/docs/ddd/concepts/architecture/cqrs" >}})** | Read/write separation | Hard | Complex queries/performance requirements |
| **[Event-Driven]({{< relref "/docs/ddd/concepts/architecture/event-driven" >}})** | Domain events | Medium~Hard | Microservices, asynchronous processing |

## Best Practice: Which System Fits Which Architecture?

The appropriate architecture varies depending on project characteristics.

### Recommended Architecture by System Type

| System Type | Recommended Architecture | Reason |
|------------|-------------------------|--------|
| **Startup MVP** | Layered | Speed is priority, minimize complexity |
| **Internal Admin System** | Layered or Hexagonal | Moderate complexity, easy maintenance |
| **E-commerce Platform** | Hexagonal + Event-Driven | Many external integrations, scalability needed |
| **Finance/Insurance System** | Onion or Clean | Complex domain, regulatory compliance |
| **Large Enterprise** | Clean + CQRS | Clear rules, multi-team collaboration |
| **Microservices** | Hexagonal + Event-Driven | Service boundaries, loose coupling |
| **Legacy Integration** | Hexagonal | Isolate legacy with ACL |
| **Reporting-focused** | CQRS | Complex query optimization |

### Selection Flowchart

```mermaid
flowchart TB
    Q1{"Does your team have<br>architecture experience?"}
    Q2{"Is the business logic<br>complex?"}
    Q3{"Are there many<br>external integrations?"}
    Q4{"Do you need read/write<br>optimization?"}
    Q5{"Are you<br>applying DDD?"}

    Q1 -->|No| L1["Start with Layered"]
    Q1 -->|Yes| Q2

    Q2 -->|No| Q3
    Q2 -->|Yes| Q5

    Q3 -->|No| L1
    Q3 -->|Yes| L2["Hexagonal"]

    Q5 -->|No| L3["Clean"]
    Q5 -->|Yes| L4["Onion"]

    Q4 -->|Yes| L5["Add CQRS"]

    L1 --> L6["Evolve gradually as needed"]
    L2 --> L6
    L3 --> L6
    L4 --> L6
```

## Gradual Evolution Path

You do not need to apply a complex architecture from the start. As your project grows, you can naturally evolve the architecture.

```mermaid
flowchart LR
    A["Step 1<br>Layered"]
    B["Step 2<br>Domain Separation"]
    C["Step 3<br>Port Extraction"]
    D["Step 4<br>Hexagonal/Clean"]
    E["Step 5<br>CQRS/Events"]

    A -->|"When domain grows complex"| B
    B -->|"When external integrations increase"| C
    C -->|"When testing/changing becomes difficult"| D
    D -->|"When query performance/scalability needed"| E
```

### Evolution Signals by Stage

| Current -> Next | Signal |
|----------------|--------|
| Layered -> Domain Separation | "Should this logic really be in the Controller?" |
| Domain Separation -> Port Extraction | "Changing the DB means modifying the domain too" |
| Port Extraction -> Hexagonal/Clean | "The team is growing and we need clear rules" |
| Hexagonal/Clean -> CQRS | "Query performance is the bottleneck" |
| -> Event-Driven | "We need to reduce coupling between services" |

## Learning Order

We recommend learning each architecture pattern in the following order.

### Foundational Architecture

| Order | Document | Key Question |
|-------|----------|-------------|
| 1 | [Layered Architecture]({{< relref "/docs/ddd/concepts/architecture/layered-architecture" >}}) | "How should you structure your code?" |
| 2 | [Hexagonal Architecture]({{< relref "/docs/ddd/concepts/architecture/hexagonal-architecture" >}}) | "How should you isolate externals?" |
| 3 | [Clean Architecture]({{< relref "/docs/ddd/concepts/architecture/clean-architecture" >}}) | "How should you manage dependencies?" |
| 4 | [Onion Architecture]({{< relref "/docs/ddd/concepts/architecture/onion-architecture" >}}) | "How should you protect the domain?" |

### Advanced Patterns

| Order | Document | Key Question |
|-------|----------|-------------|
| 5 | [CQRS]({{< relref "/docs/ddd/concepts/architecture/cqrs" >}}) | "Should you separate reads and writes?" |
| 6 | [Event-Driven Architecture]({{< relref "/docs/ddd/concepts/architecture/event-driven" >}}) | "How should components communicate?" |

## Next Steps

- **If you are just starting**: Begin with [Layered Architecture]({{< relref "/docs/ddd/concepts/architecture/layered-architecture" >}})
- **If you have many external integrations**: Check out [Hexagonal Architecture]({{< relref "/docs/ddd/concepts/architecture/hexagonal-architecture" >}})
- **If you are applying DDD**: [Onion Architecture]({{< relref "/docs/ddd/concepts/architecture/onion-architecture" >}}) is a good fit
- **If query performance matters**: Consider [CQRS]({{< relref "/docs/ddd/concepts/architecture/cqrs" >}})
