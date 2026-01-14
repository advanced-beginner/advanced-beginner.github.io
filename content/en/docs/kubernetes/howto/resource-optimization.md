---
lastmod: "2026-01-11"
title: Resource Optimization
weight: 2
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **Objective**: Find appropriate CPU/memory settings to improve resource efficiency
> **Prerequisites**: Understanding of resource management concepts
> **Estimated Time**: 45 minutes

{{< callout type="tip" title="TL;DR" >}}
- Measure actual usage with Metrics Server
- Check recommendations with VPA Recommender
- Adjust incrementally while monitoring
{{< /callout >}}

## Why Resource Optimization?

| Configuration | Problem |
|---------------|---------|
| requests too high | Resource waste, scheduling difficulties |
| requests too low | Throttling, OOM risk |
| limits too high | Allowing excessive resource usage |
| limits too low | Frequent throttling, OOM |

## 1. Measure Current Usage

### Verify Metrics Server Installation

```bash
# Check Metrics Server operation
kubectl top nodes
kubectl top pods
```

If you get an error, install Metrics Server:

```bash
# Minikube
minikube addons enable metrics-server

# Other environments
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

### Check Usage

```bash
# Usage by Pod
kubectl top pods

# Usage by container
kubectl top pods --containers

# Specific namespace
kubectl top pods -n production
```

**Expected Output:**
```
NAME                    CPU(cores)   MEMORY(bytes)
my-app-xxx-yyy          50m          256Mi
my-app-xxx-zzz          45m          248Mi
```

### Track Usage Over Time

Use with watch for continuous monitoring:

```bash
# Refresh every 2 seconds
watch -n 2 kubectl top pods
```

## 2. Determine Appropriate Values

### Empirical Guidelines

| Item | Recommended Value |
|------|-------------------|
| CPU requests | 80-100% of normal usage |
| CPU limits | 2-4x requests or unset |
| Memory requests | 110-120% of normal usage |
| Memory limits | 1.2-1.5x requests |

### Using VPA Recommender

Installing VPA automatically calculates recommended values:

```bash
# Install VPA (components only)
git clone https://github.com/kubernetes/autoscaler.git
cd autoscaler/vertical-pod-autoscaler
./hack/vpa-up.sh
```

```yaml
# vpa.yaml - Check recommendations only (don't apply)
apiVersion: autoscaling.k8s.io/v1
kind: VerticalPodAutoscaler
metadata:
  name: my-app-vpa
spec:
  targetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: my-app
  updatePolicy:
    updateMode: "Off"  # Only recommend, don't apply
```

```bash
# Check VPA recommendations
kubectl describe vpa my-app-vpa
```

## 3. Adjust Resource Settings

### Incremental Adjustment Method

1. Measure current usage (at least 1 hour)
2. Set requests to normal usage + 20%
3. Set limits to 1.5x requests
4. Monitor after deployment
5. Repeat adjustments as needed

### Configuration Examples

**Before (Excessive settings):**
```yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1000m"
```

**Actual Usage:** CPU 50m, Memory 256Mi

**After (Optimized):**
```yaml
resources:
  requests:
    memory: "300Mi"
    cpu: "100m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

## 4. Monitor and Validate

### Check for Throttling

Check if CPU throttling is occurring:

```bash
# Check in Pod (cgroup v1)
kubectl exec <pod-name> -- cat /sys/fs/cgroup/cpu/cpu.stat
# If nr_throttled increases, throttling is occurring

# cgroup v2
kubectl exec <pod-name> -- cat /sys/fs/cgroup/cpu.stat
```

### Check for OOM

```bash
# Check OOM events
kubectl get events --field-selector reason=OOMKilling

# Check in Pod status
kubectl describe pod <pod-name> | grep -A 5 "Last State"
```

### Check QoS Class

```bash
kubectl get pod <pod-name> -o jsonpath='{.status.qosClass}'
```

## 5. Java Application Specific Settings

### JVM Heap and Container Memory

```yaml
resources:
  requests:
    memory: "512Mi"
  limits:
    memory: "1Gi"
env:
- name: JAVA_OPTS
  value: "-XX:MaxRAMPercentage=75.0"  # 75% of container memory for heap
```

JVM heap configuration guidelines:

| Container Memory | Recommended Heap Ratio |
|------------------|------------------------|
| < 512Mi | 50-60% |
| 512Mi - 2Gi | 65-75% |
| > 2Gi | 75-80% |

### Spring Boot Configuration

```yaml
env:
- name: JAVA_OPTS
  value: "-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
```

## Checklist

- [ ] Is Metrics Server installed?
- [ ] Have you measured usage for at least 1 hour?
- [ ] Are requests slightly higher than normal usage?
- [ ] Can limits accommodate peak usage?
- [ ] For Java apps, is JVM heap configured appropriately?
- [ ] After deployment, are there no throttling/OOM issues?

---

## Next Steps

| Goal | Recommended Document |
|------|---------------------|
| Auto-scaling | [Scaling](../concepts/scaling/) |
| Resolve Pod issues | [Pod Troubleshooting](pod-troubleshooting/) |
| Resource management concepts | [Resource Management](../concepts/resources/) |
