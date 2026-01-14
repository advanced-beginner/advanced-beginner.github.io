---
lastmod: "2026-01-11"
title: FAQ
weight: 2
author:
  name: Advanced Beginner
  github: advanced-beginner
---

This page organizes frequently asked questions and answers in Kubernetes learning and operations.

## Before Getting Started

### Q: What should I know before learning Kubernetes?

**A:** Having the following knowledge will make learning easier:
- **Required**: Docker basics (building images, running containers)
- **Required**: Basic Linux commands (cd, ls, cat, grep)
- **Recommended**: YAML syntax, networking fundamentals (IP, ports, DNS)

### Q: How can I practice Kubernetes locally?

**A:** There are three methods:

| Tool | Features | Recommended For |
|------|----------|-----------------|
| Minikube | Easiest, extensive documentation | Beginners |
| Kind | Quick start, multi-node | CI/CD |
| Docker Desktop | Easy installation | macOS/Windows |

If you're a beginner, start with Minikube: `minikube start`

### Q: How long does it take to learn?

**A:** It depends on your goals:
- **Basic deployment** (Pod, Deployment, Service): 1-2 weeks
- **Operations basics** (resources, scaling, monitoring): 1 month
- **Production proficiency**: 3-6 months (requires actual project experience)

### Q: YAML syntax is difficult. Do I really need to know it?

**A:** Yes, Kubernetes resources are defined in YAML. Fortunately, complex syntax is rarely used:
- Indentation (2 spaces)
- Key-value pairs (`key: value`)
- Lists (`- item`)

You can view field descriptions with `kubectl explain deployment.spec`.

### Q: kubectl commands are too long. Are there shortcuts?

**A:** Common shortcut commands:

```bash
# Set aliases (~/.bashrc or ~/.zshrc)
alias k='kubectl'
alias kgp='kubectl get pods'
alias kgs='kubectl get services'
alias kgd='kubectl get deployments'
alias kd='kubectl describe'
alias kl='kubectl logs'
```

## Basic Concepts

### Q: What's the difference between a Pod and a container?

**A:** A container is an isolated environment for running an application, while a Pod is the smallest deployable unit in Kubernetes that contains one or more containers. Containers within the same Pod share network and storage. In most cases, a Pod contains only one container.

### Q: Can't I just create Pods directly instead of using Deployments?

**A:** You can create Pods directly, but it's not recommended. If a Pod is deleted, it won't be automatically recovered. Using a Deployment automatically maintains the desired number of replicas and also supports rolling updates and rollbacks.

### Q: Why do we need Services?

**A:** Pod IPs change whenever Pods are recreated. Services provide a stable IP and DNS name that doesn't change, allowing reliable access to Pods. They also automatically distribute traffic across multiple Pods.

## Configuration and Operations

### Q: What's the difference between requests and limits?

**A:** Requests are the minimum resources a Pod is guaranteed and serve as the scheduling criterion. Limits are the maximum resources a Pod can use. If CPU exceeds limits, it's throttled; if memory exceeds limits, it's OOMKilled.

### Q: What's the difference between ConfigMap and Secret?

**A:** Both store configuration, but ConfigMap is for general configuration and Secret is for sensitive information (passwords, API keys). Secrets are Base64 encoded, and can be stored more securely with etcd encryption enabled.

### Q: What's the difference between Liveness Probe and Readiness Probe?

**A:**
- **Liveness Probe**: Checks if the container is alive. Restarts on failure.
- **Readiness Probe**: Checks if ready to receive traffic. Removes from Service on failure.

Liveness is for unrecoverable situations like deadlocks, while Readiness is for temporary unavailability (like DB connection loss).

### Q: How do I determine the appropriate number of replicas?

**A:** Consider the following:
- Minimum of 2 or more (high availability)
- Expected traffic and throughput of a single Pod
- Number of available Pods during rolling updates
- HPA can automatically adjust

## Troubleshooting

### Q: My Pod is stuck in Pending state.

**A:** Check Events with `kubectl describe pod <pod-name>`. Main causes:
- Resource shortage: Node doesn't have requested CPU/memory
- Node selector mismatch: No nodes match the criteria
- PVC binding wait: Volume is not ready

### Q: I'm getting CrashLoopBackOff.

**A:** Check previous logs with `kubectl logs <pod-name> --previous`. Main causes:
- Application error (missing configuration, dependency issues)
- Out of memory (Exit Code 137 = OOMKilled)
- Incorrect command or environment variables

### Q: I can't connect to a Service.

**A:** Check in order:
1. `kubectl get endpoints <service-name>` - Are there Endpoints?
2. `kubectl get pods -l <selector>` - Are Pods Ready?
3. Inside Pod: `curl localhost:<port>` - Is the app responding?
4. Do Service selector and Pod labels match?

## Cloud and Tools

### Q: Which should I choose among EKS, GKE, and AKS?

**A:** If you're already using a cloud provider, choose that service:
- **Using AWS**: EKS
- **Using GCP**: GKE
- **Using Azure**: AKS

Features are generally similar. GKE has the most comprehensive default features, and EKS has good integration with AWS services.

### Q: Do I need Helm?

**A:** It's useful for complex applications or deploying the same app to multiple environments. For simple apps, kubectl and YAML alone are sufficient.

### Q: Minikube vs Kind for local development?

**A:**
- **Minikube**: Recommended for beginners, various addons, extensive documentation
- **Kind**: Fast cluster creation, multi-node, suitable for CI/CD

For learning purposes, use Minikube; for CI environments, use Kind.

## Best Practices

### Q: How should I manage YAML files?

**A:**
- Version control with Git
- Separate directories by environment (`/base`, `/overlays/dev`, `/overlays/prod`)
- Manage environment differences with Kustomize or Helm

### Q: How should I divide namespaces?

**A:** Common approaches:
- By environment: `dev`, `staging`, `prod`
- By team: `team-a`, `team-b`
- By function: `monitoring`, `logging`

For small scale, start with environment-based separation.

### Q: What should I be careful about in production?

**A:**
- Mandatory setting of resource requests/limits
- Configure Readiness/Liveness Probes
- Minimum of 2 or more replicas
- Configure PodDisruptionBudget
- Regular backups (etcd, PV)
- Configure monitoring and alerting
