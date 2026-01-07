---
lastmod: "2026-01-07"
title: Domain-Driven Design
weight: 2
---

## What is DDD?

**Domain-Driven Design (DDD)** is a design methodology for systematically handling complex business logic. It was introduced by Eric Evans in his 2003 book of the same name.

The core idea is simple: **Code should reflect the business.** The business domain, not database tables or technical frameworks, becomes the center of design.

## When is DDD Needed?

Not every project needs DDD. Answer these questions to decide:

### Situations Where DDD Helps

- **Is the business logic complex?** — If there are rules, conditions, calculations beyond CRUD
- **Do you need to collaborate with domain experts?** — If requirements are hard to understand with developers alone
- **Will the system be maintained long-term?** — If it's not a one-off project
- **Are multiple teams developing one system?** — If boundary and responsibility separation is needed

### Situations Where DDD May Be Overkill

- Simple CRUD applications
- When technical complexity outweighs business logic (e.g., high-performance data processing)
- Prototypes or short-term projects
- When the entire team isn't ready to understand and apply DDD

> "DDD is a tool for managing complexity. Applying it where there's no complexity just creates complexity."

## What's Different from Traditional Approaches?

| Traditional Approach | DDD Approach |
|---------------------|--------------|
| Design from database schema first | Design from business model first |
| Write code in developer terminology | Write code in business terminology (Ubiquitous Language) |
| Business logic scattered in service layer | Logic cohesive in domain objects |
| Entire system has one model | Models separated by Bounded Context |
| Entity = data container | Entity = subject of business behavior |

## What This Guide Covers

### [Quick Start](quick-start/)
A quick overview of DDD core concepts. Get the big picture before diving into details.

### [Concepts](concepts/)

DDD is broadly divided into **Strategic Design** and **Tactical Design**.

**Strategic Design** — Deals with the big picture:

| Topic | What You'll Learn |
|-------|-------------------|
| [Strategic Design](concepts/strategic-design/) | Bounded Context, Context Map, Ubiquitous Language |

**Tactical Design** — Deals with code-level patterns:

| Topic | What You'll Learn |
|-------|-------------------|
| [Tactical Design](concepts/tactical-design/) | Entity, Value Object, Repository patterns |
| [Aggregate](concepts/aggregate/) | Consistency boundaries and transaction scope design |
| [Domain Events](concepts/domain-events/) | Event-based communication for loose coupling |

**Advanced Topics:**

| Topic | What You'll Learn |
|-------|-------------------|
| [CQRS](concepts/cqrs/) | Command Query Responsibility Segregation |
| [Architecture Patterns](concepts/architecture/) | Layered, Hexagonal, Clean Architecture |
| [Testing Strategy](concepts/testing/) | How to test domain models |
| [Anti-patterns](concepts/anti-patterns/) | Common mistakes and how to avoid them |

### [Hands-on Examples](examples/)
Implementing an actual order domain with DDD.

- [Environment Setup](examples/setup/) - Project structure and dependencies
- [Order Domain](examples/order-domain/) - Entity, Value Object, Aggregate implementation
- [Application Layer](examples/application-layer/) - Use Cases and domain services

### [Appendix](appendix/)
- [Glossary](appendix/glossary/) - Quick reference for DDD terms
- [FAQ](appendix/faq/) - Frequently asked questions
- [References](appendix/references/) - Additional learning resources

## Prerequisites

- **Required**: Java/Spring Boot basics, Object-oriented programming
- **Helpful**: Basic database knowledge, design patterns

## Suggested Learning Path

```
If you're new:      Quick Start → Strategic Design → Tactical Design
Deep modeling:      Aggregate → Domain Events → Order Domain Example
Architecture:       CQRS → Architecture Patterns → Testing Strategy
```

## Common Misconceptions

**"DDD forces a specific architecture"** — No. DDD is a design principle, not an implementation method. You can choose Hexagonal or Layered.

**"Event sourcing is required for DDD"** — No. Event sourcing is optional. You can do DDD with traditional state persistence.

**"DDD is the same as microservices"** — Different. While DDD's Bounded Context helps define microservice boundaries, DDD can be applied in monoliths too.
