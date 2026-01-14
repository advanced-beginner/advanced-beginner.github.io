---
bookCollapseSection: true
lastmod: "2026-01-11"
title: How-to Guides
weight: 4
author:
  name: Advanced Beginner
  github: advanced-beginner
---

This section provides step-by-step guides to solve specific problems you may encounter while operating Kubernetes. Each guide presents concrete solutions with clear objectives.

## Choosing the Right Guide

If you're unsure which guide to follow, use the flowchart below.

```mermaid
flowchart TD
    START[Problem Occurred] --> Q1{Is Pod<br>status normal?}

    Q1 -->|Pending/Error/CrashLoop| A[Pod Troubleshooting]
    Q1 -->|Running| Q2{Are there<br>performance issues?}

    Q2 -->|Slow/OOM/Throttling| B[Resource Optimization]
    Q2 -->|None| Q3{What kind of<br>issue?}

    Q3 -->|Network connectivity| C["See Concepts > Networking"]
    Q3 -->|Scaling| D["See Concepts > Scaling"]

    A --> A1["kubectl describe pod<br>Check kubectl logs"]
    B --> B1["kubectl top pods<br>Analyze metrics"]

    style A fill:#f9f,stroke:#333
    style B fill:#bbf,stroke:#333
```

| Symptom | Recommended Guide |
|---------|-------------------|
| Pod won't start, CrashLoopBackOff | [Pod Troubleshooting](pod-troubleshooting/) |
| Slow response, OOMKilled, CPU throttling | [Resource Optimization](resource-optimization/) |

#### Guide List

| Guide | Situation | Estimated Time |
|-------|-----------|----------------|
| [Pod Troubleshooting](pod-troubleshooting/) | When Pod fails to start or terminates abnormally | 30 min |
| [Resource Optimization](resource-optimization/) | When finding appropriate CPU/memory settings | 45 min |

#### How to Use These Guides

1. Select the guide that matches your current problem
2. Check the prerequisites of the guide
3. Follow the step-by-step instructions in order
4. Verify the expected results at each step
