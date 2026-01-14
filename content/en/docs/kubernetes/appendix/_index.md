---
bookCollapseSection: true
lastmod: "2026-01-11"
title: Appendix
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
| When encountering unfamiliar terms | [Glossary](glossary/) | "What's PVC?" → Check PersistentVolumeClaim definition |
| When stuck or encountering errors | [FAQ](faq/) | "Pod is in Pending state" → Check causes and solutions |
| When wanting to study in depth | [References](references/) | "Want to prepare for CKA?" → Check certification/learning resources |

## Appendix List

| Resource | Description | Target Audience |
|----------|-------------|-----------------|
| [Glossary](glossary/) | Quick reference for Kubernetes core terms | All learners |
| [FAQ](faq/) | Frequently asked questions and answers | Beginners, troubleshooting |
| [References](references/) | Official documentation and additional learning resources | Advanced learners |
