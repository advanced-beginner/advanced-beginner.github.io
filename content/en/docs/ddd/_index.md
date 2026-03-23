---
title: Domain-Driven Design
bookCollapseSection: true
description: A comprehensive DDD guide for systematically handling complex business logic - Strategic/Tactical Design, Aggregate, CQRS, Event Sourcing
weight: 2
lastmod: 2026-01-09
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

**Domain-Driven Design (DDD)** is a design methodology for systematically handling complex business logic. The core idea, introduced by Eric Evans in his 2003 book of the same name, is simple: Code should reflect the business. The business domain, not database tables or technical frameworks, becomes the center of design.

## When is DDD Needed?

Not every project needs DDD. When deciding whether to adopt DDD, carefully consider your project's characteristics.

**Situations Where DDD Helps**

DDD shines when business logic is complex. It's effective in domains with various rules, conditions, and calculations beyond simple CRUD. When collaboration with domain experts is needed—situations where developers alone can't fully understand the requirements—Ubiquitous Language helps bridge the communication gap. DDD is also suitable when the system will be maintained long-term or when multiple teams are developing one system and boundary/responsibility separation is needed.

**Situations Where DDD May Be Overkill**

On the other hand, for simple CRUD applications or when technical complexity outweighs business logic (e.g., high-performance data processing), DDD may add unnecessary complexity. The same applies to prototypes, short-term projects, or when the entire team isn't ready to understand and apply DDD.

> "DDD is a tool for managing complexity. Applying it where there's no complexity just creates complexity."

## What's Different from Traditional Approaches?

DDD takes a fundamentally different approach from traditional data-centric design. The table below shows the differences between the two approaches. While traditional approaches design database schemas first and write code in developer terminology, DDD designs business models first and writes code in business terminology (Ubiquitous Language). Instead of business logic being scattered across the service layer, it coheres in domain objects. Rather than the entire system using one model, models are separated by Bounded Context. Entities serve as subjects of business behavior, not mere data containers.

| Traditional Approach | DDD Approach |
|---------------------|--------------|
| Design from database schema first | Design from business model first |
| Write code in developer terminology | Write code in business terminology (Ubiquitous Language) |
| Business logic scattered in service layer | Logic cohesive in domain objects |
| Entire system has one model | Models separated by Bounded Context |
| Entity = data container | Entity = subject of business behavior |

## What This Guide Covers

**[Quick Start](quick-start/)**

A quick overview of DDD core concepts. Get the big picture before diving into details.

**[Concepts](concepts/)**

DDD is broadly divided into **Strategic Design** and **Tactical Design**. Strategic Design deals with the big picture of the system, including concepts like Bounded Context, Context Map, and Ubiquitous Language. Tactical Design deals with code-level patterns, providing concrete implementation patterns such as Entity, Value Object, Repository, Aggregate, and Domain Events. Advanced topics include separating commands and queries through CQRS, various architecture patterns (Layered, Hexagonal, Clean Architecture), domain model testing strategies, and anti-pattern guides to avoid common mistakes.

| Topic | What You'll Learn |
|-------|-------------------|
| [Strategic Design](concepts/strategic-design/) | Bounded Context, Context Map, Ubiquitous Language |
| [Tactical Design](concepts/tactical-design/) | Entity, Value Object, Repository patterns |
| [Aggregate](concepts/aggregate/) | Consistency boundaries and transaction scope design |
| [Domain Events](concepts/) | Event-based communication for loose coupling |
| [CQRS](concepts/) | Command Query Responsibility Segregation |
| [Architecture Patterns](concepts/architecture/) | Layered, Hexagonal, Clean Architecture |
| [Testing Strategy](concepts/testing/) | How to test domain models |
| [Anti-patterns](concepts/anti-patterns/) | Common mistakes and how to avoid them |

**[How-to Guides](howto/)**

Step-by-step guides to solving specific problems you encounter when applying DDD.

- [Defining Aggregate Boundaries](howto/aggregate-boundaries/) - How to design Aggregate boundaries based on invariants

**[Hands-on Examples](examples/)**

Apply learned concepts to practice through examples implementing an actual order domain with DDD. Starting from environment setup, you'll implement Entities, Value Objects, and Aggregates for the order domain, then write Use Cases and domain services in the application layer.

- [Environment Setup](examples/setup/) - Project structure and dependencies
- [Order Domain](examples/order-domain/) - Entity, Value Object, Aggregate implementation
- [Application Layer](examples/application-layer/) - Use Cases and domain services

**[Appendix](appendix/)**

Reference materials to consult during learning. The glossary provides quick access to DDD core terms, FAQ answers frequently asked questions, and references offer books and articles for further learning.

- [Glossary](appendix/glossary/) - Quick reference for DDD terms
- [FAQ](appendix/faq/) - Frequently asked questions
- [References](appendix/references/) - Additional learning resources

## Prerequisites

To effectively learn from this guide, basic knowledge of Java and Spring Boot, along with understanding of object-oriented programming, is essential. Basic database knowledge and understanding of design patterns will be helpful for learning.

## Suggested Learning Path

The following paths are recommended based on your learning goals. If you're new to DDD, start with Quick Start to get an overview, then proceed through Strategic Design and Tactical Design in order. If you want to deeply understand modeling, follow the sequence of Aggregate, Domain Events, and Order Domain examples. If you're interested in architecture, the sequence of CQRS, Architecture Patterns, and Testing Strategy is effective.

## Common Misconceptions

There are several common misconceptions about DDD.

**"DDD forces a specific architecture"** — This is not true. DDD is a design principle, not an implementation method. You can freely choose architecture that fits your project, whether Hexagonal or Layered.

**"Event sourcing is required for DDD"** — This is also a misconception. Event sourcing is optional, and you can fully apply DDD with traditional state persistence.

**"DDD is the same as microservices"** — These are different. While DDD's Bounded Context helps define microservice boundaries, DDD can be effectively applied in monolithic architectures as well.
