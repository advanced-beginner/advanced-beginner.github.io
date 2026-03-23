---
bookCollapseSection: true
title: How-To Guides
description: "DDD design problem-solving guides"
weight: 4
lastmod: "2026-01-13"
author: "@kimbenji"
author_url: "http://github.com/kimbenji"
---

> **Target Audience**: Developers who understand basic DDD concepts (Entity, Value Object, Aggregate)
> **Prerequisites**: Read the [Tactical Design]({{< relref "/docs/ddd/concepts/tactical-design" >}}) document or have basic knowledge of DDD building blocks
> **Purpose of This Section**: Solve specific problems step-by-step when applying DDD

{{< callout type="info" title="What is a How-To Guide?" >}}
How-to guides are practical instructions for **solving specific problems**. They focus on "how" rather than "why", and each guide starts with a clear problem situation and ends with a verifiable solution. If you need conceptual understanding first, please refer to the [Concepts]({{< relref "/docs/ddd/concepts" >}}) section.
{{< /callout >}}

## What This Section Covers

| Guide | Problem It Solves | Time Required |
|-------|-------------------|---------------|
| [Defining Aggregate Boundaries]({{< relref "/docs/ddd/howto/aggregate-boundaries" >}}) | "Should I group these Entities into one Aggregate?" | ~30 min |
| [Identifying Bounded Contexts]({{< relref "/docs/ddd/howto/bounded-context-identification" >}}) | "I don't know how to divide the system" | ~25 min |
| [Designing Value Objects]({{< relref "/docs/ddd/howto/value-object-design" >}}) | "How do I distinguish Entity from Value Object?" | ~20 min |
| [Designing Domain Events]({{< relref "/docs/ddd/howto/domain-event-design" >}}) | "How should Aggregates communicate?" | ~25 min |

## What This Section Does Not Cover

- DDD concept explanations → See [Concepts]({{< relref "/docs/ddd/concepts" >}})
- Full project implementation examples → See [Examples]({{< relref "/docs/ddd/examples" >}})
- Term definitions → See [Glossary]({{< relref "/docs/ddd/appendix/glossary" >}})

## How to Use These Guides

1. **Identify the Problem**: Select the guide most similar to your current issue
2. **Check Prerequisites**: Review the prerequisites at the top of each guide
3. **Follow Step-by-Step**: Follow the instructions in order and verify each checkpoint
4. **Apply**: Adapt and apply to your own project
