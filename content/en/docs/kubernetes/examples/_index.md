---
bookCollapseSection: true
lastmod: "2026-01-11"
title: Practical Examples
description: "Kubernetes hands-on examples and document index"
weight: 3
author:
  name: Advanced Beginner
  github: advanced-beginner
---

This section provides practical examples to help you run Kubernetes core features hands-on. Each example can be executed independently and includes all necessary YAML files and commands.

## Choose Examples by Goal

**"Which example should I start with?"**

```mermaid
flowchart TD
    START[Start Practice] --> Q1{Is Kubernetes<br>installed?}

    Q1 -->|No| A[Environment Setup]
    Q1 -->|Yes| Q2{Do you have<br>Kubernetes experience?}

    Q2 -->|First time| B[Basic Example]
    Q2 -->|Some experience| Q3{Want to deploy<br>real app?}

    Q3 -->|Yes| C[Spring Boot Deployment]
    Q3 -->|No| Q4{Need state management /<br>operations?}

    Q4 -->|State management| D[StatefulSet Lab]
    Q4 -->|Access control| E[RBAC Configuration]
    Q4 -->|Scheduling| F[CronJob Lab]

    A -->|After completion| B
    B -->|After completion| C
    C -->|After completion| D

    style A fill:#90EE90
    style B fill:#87CEEB
    style C fill:#DDA0DD
    style D fill:#FFD700
    style E fill:#FFA07A
    style F fill:#98FB98
```

| Your Situation | Recommended Example | What You'll Learn |
|------------|----------|----------|
| New to Kubernetes | [Environment Setup]({{< relref "/docs/kubernetes/examples/setup" >}}) → [Basic Example]({{< relref "/docs/kubernetes/examples/basic" >}}) | Cluster setup, Pod/Service basics |
| Know concepts but lack hands-on | [Basic Example]({{< relref "/docs/kubernetes/examples/basic" >}}) | Run Pod, Deployment, Service directly |
| Want to deploy real apps | [Spring Boot Deployment]({{< relref "/docs/kubernetes/examples/spring-boot" >}}) | Apply ConfigMap, Secret, Probe |
| Need stateful apps | [StatefulSet Lab]({{< relref "/docs/kubernetes/examples/statefulset" >}}) | PVC templates, Headless Service |
| Want to manage access permissions | [RBAC Configuration Lab]({{< relref "/docs/kubernetes/examples/rbac" >}}) | Role, ServiceAccount, RoleBinding |
| Want to automate periodic tasks | [CronJob Lab]({{< relref "/docs/kubernetes/examples/cronjob" >}}) | Scheduling, concurrency policies |

#### Example List

| Example | Difficulty | Estimated Time | Topics Covered |
|------|--------|----------|-------------|
| [Environment Setup]({{< relref "/docs/kubernetes/examples/setup" >}}) | ⭐ Beginner | 30 min | Local environment with Minikube, Kind |
| [Basic Example]({{< relref "/docs/kubernetes/examples/basic" >}}) | ⭐⭐ Basic | 60 min | Pod, Deployment, Service hands-on |
| [Spring Boot Deployment]({{< relref "/docs/kubernetes/examples/spring-boot" >}}) | ⭐⭐⭐ Intermediate | 90 min | Real application deployment |
| [StatefulSet Lab]({{< relref "/docs/kubernetes/examples/statefulset" >}}) | ⭐⭐⭐ Intermediate | 60 min | MySQL StatefulSet, PVC, Headless Service |
| [RBAC Configuration Lab]({{< relref "/docs/kubernetes/examples/rbac" >}}) | ⭐⭐⭐ Intermediate | 45 min | Role, ServiceAccount, per-namespace access control |
| [CronJob Lab]({{< relref "/docs/kubernetes/examples/cronjob" >}}) | ⭐⭐⭐ Intermediate | 45 min | Periodic backup, concurrency policies, alert setup |

#### Prerequisites

All examples require the following environment:

- Docker 24.x or higher
- kubectl 1.29.x or higher
- Local Kubernetes cluster (Minikube or Kind)

If your environment is not set up, start with the [Environment Setup]({{< relref "/docs/kubernetes/examples/setup" >}}) example.
