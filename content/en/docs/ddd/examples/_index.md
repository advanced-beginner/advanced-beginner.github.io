---
bookCollapseSection: true
title: Hands-on Examples
description: "DDD hands-on examples learning guide and document index"
weight: 3
lastmod: "2026-01-13"
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

> **Target Audience**: Developers who understand DDD concepts and want to implement them in actual code
> **Prerequisites**: [Quick Start]({{< relref "/docs/ddd/quick-start" >}}) and Tactical Design from [Concepts]({{< relref "/docs/ddd/concepts" >}}) section
> **Purpose of this Section**: Implement DDD patterns with actual Spring Boot code through an order domain

{{< callout type="warning" title="Before You Start" >}}
- Java 17 or higher
- Gradle 8.x
- IDE (IntelliJ IDEA recommended)
- Docker (for Kafka, optional)
{{< /callout >}}

In this section, we implement an order domain with DDD patterns using Spring Boot. You will experience how each pattern interacts while converting theoretical concepts into actual code.

## Learning Path

Learning progresses from project setup to domain model implementation, application layer implementation, and infrastructure layer implementation. Each step builds upon the foundation established in previous steps.

| Step | Document | What You'll Learn | Duration |
|------|----------|-------------------|----------|
| 1 | [Project Setup](setup/) | Project structure, dependency configuration | ~15 min |
| 2 | [Order Domain](order-domain/) | Aggregate, Entity, Value Object implementation | ~40 min |
| 3 | [Application Layer](application-layer/) | Use Case, Domain Service implementation | ~30 min |
| 4 | [Event Sourcing Practice](event-sourcing/) | Event storage, snapshots, time travel | ~45 min |

## What You'll Build

After completing all exercises, you will have implemented:
- Order Aggregate (including Entity, Value Object)
- OrderApplicationService (Use Case orchestration)
- OrderRepository (persistence)
- Domain event publishing and handling
