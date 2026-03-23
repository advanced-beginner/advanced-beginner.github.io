---
bookCollapseSection: true
lastmod: "2026-01-11"
title: Appendix
description: "Kubernetes appendix resources and reference documents"
weight: 5
author:
  name: Advanced Beginner
  github: advanced-beginner
---

This section provides supplementary resources to help with Kubernetes learning and operations.

## Situation-based Appendix Usage Guide

If you're unsure which resource to consult, refer to the guide below.

```mermaid
flowchart LR
    Q[Question Arises] --> T{Question Type?}
    T -->|Term/Concept| G[Glossary]
    T -->|Error/Issue| F[FAQ]
    T -->|Advanced Learning| R[References]
```

| Situation | Recommended Resource | Usage Example |
|-----------|---------------------|---------------|
| When encountering unfamiliar terms | [Glossary]({{< relref "/docs/kubernetes/appendix/glossary" >}}) | "What's PVC?" → Check PersistentVolumeClaim definition |
| When stuck or encountering errors | [FAQ]({{< relref "/docs/kubernetes/appendix/faq" >}}) | "Pod is in Pending state" → Check causes and solutions |
| When wanting to study in depth | [References]({{< relref "/docs/kubernetes/appendix/references" >}}) | "Want to prepare for CKA?" → Check certification/learning resources |

## Appendix List

| Resource | Description | Target Audience |
|----------|-------------|-----------------|
| [Glossary]({{< relref "/docs/kubernetes/appendix/glossary" >}}) | Quick reference for Kubernetes core terms | All learners |
| [FAQ]({{< relref "/docs/kubernetes/appendix/faq" >}}) | Frequently asked questions and answers | Beginners, troubleshooting |
| [References]({{< relref "/docs/kubernetes/appendix/references" >}}) | Official documentation and additional learning resources | Advanced learners |
