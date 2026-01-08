---
lastmod: "2026-01-07"
title: Core Concepts
weight: 2
---

A deep dive into DDD's strategic and tactical design patterns.

## Table of Contents

### Design Patterns

1. [Strategic Design](strategic-design/) - Subdomain, Bounded Context, Context Mapping, Ubiquitous Language
2. [Tactical Design](tactical-design/) - Entity, Value Object, Repository, Domain Service, Specification
3. [Aggregate Deep Dive](aggregate/) - Aggregate Design Principles, Transaction Boundaries, Size Decisions
4. [Domain Events](domain-events/) - Event-Driven Architecture, Event Sourcing

### Architecture

5. [Architecture Patterns](architecture/) - Hexagonal, Clean Architecture, Onion Architecture
6. [CQRS](cqrs/) - Command Query Responsibility Segregation

### Quality

7. [Testing Strategy](testing/) - Domain Model Testing, Integration Testing, E2E
8. [Anti-Patterns and Pitfalls](anti-patterns/) - Common Mistakes and Solutions

## Concept Relationships

```mermaid
flowchart TB
    subgraph Strategic["Strategic Design"]
        SUB[Subdomain]
        BC[Bounded Context]
        CM[Context Mapping]
        UL[Ubiquitous Language]
    end

    subgraph Tactical["Tactical Design"]
        E[Entity]
        VO[Value Object]
        AGG[Aggregate]
        REPO[Repository]
        DS[Domain Service]
        DE[Domain Event]
    end

    subgraph Architecture["Architecture"]
        HEX[Hexagonal]
        CLEAN[Clean Architecture]
        CQRS["CQRS/ES"]
    end

    SUB --> BC
    BC --> UL
    BC --> CM
    UL --> E
    UL --> VO
    E --> AGG
    VO --> AGG
    AGG --> REPO
    AGG --> DE
    DS --> AGG

    AGG --> HEX
    REPO --> HEX
    DE --> CQRS
```
