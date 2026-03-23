---
lastmod: "2026-01-16"
title: Resource Optimization
description: "How to analyze and optimize Kubernetes resources"
weight: 2
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **Objective**: Find appropriate CPU/memory settings to improve resource efficiency
> **Estimated Time**: 45 minutes

{{< callout type="info" title="Scope of This Guide" >}}
**Covers**: Measuring resource usage, determining appropriate requests/limits values, resolving throttling/OOM

**Does not cover**: Auto-scaling (see [Scaling]({{< relref "/docs/kubernetes/concepts/scaling" >}})), Pod startup issues (see [Pod Troubleshooting]({{< relref "/docs/kubernetes/howto/pod-troubleshooting" >}}))
{{< /callout >}}

## Before You Begin

Verify the following prerequisites.

### 1. Verify kubectl Installation and Version

```bash
kubectl version --client
```

**Success output:**
```
Client Version: v1.28.0
```

### 2. Verify Metrics Server Installation

```bash
kubectl top nodes
```

**Success output:**
```
NAME           CPU(cores)   CPU%   MEMORY(bytes)   MEMORY%
node-1         250m         12%    1024Mi          50%
```

**If you get an error, install Metrics Server.**

{{< tabs "metrics-server-install" >}}
{{< tab "Minikube" >}}
```bash
minikube addons enable metrics-server
```
{{< /tab >}}
{{< tab "Other Environments" >}}
```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```
{{< /tab >}}
{{< /tabs >}}

Wait 1-2 minutes after installation, then verify again.

### 3. Verify Target Pods

Verify the Pods you want to optimize are in Running status.

```bash
kubectl get pods -l app=<your-app>
```

**Success output:**
```
NAME                     READY   STATUS    RESTARTS   AGE
my-app-xxx-yyy           1/1     Running   0          5m
```

---

## Step 1: Measure Current Usage

{{< callout type="warning" title="Measurement Duration" >}}
Measure for at least 1 hour for accurate analysis. Including peak hours is recommended.
{{< /callout >}}

### Check Pod Resource Usage

```bash
kubectl top pods
```

**Expected output:**
```
NAME                    CPU(cores)   MEMORY(bytes)
my-app-xxx-yyy          50m          256Mi
my-app-xxx-zzz          45m          248Mi
```

To check by container, run:

```bash
kubectl top pods --containers
```

**Success check:** CPU(cores) and MEMORY(bytes) values are displayed.

### Track Usage Over Time

Start real-time monitoring that refreshes every 2 seconds.

```bash
watch -n 2 kubectl top pods
```

{{< callout type="tip" title="Tip" >}}
Record both peak time maximum values and normal operation values.
{{< /callout >}}

**Recording example:**

| Time Period | CPU | Memory |
|-------------|-----|--------|
| Normal | 50m | 256Mi |
| Peak | 200m | 400Mi |

---

## Step 2: Check Current Settings

Check the current resource settings of your Deployment.

```bash
kubectl get deployment <deployment-name> -o jsonpath='{.spec.template.spec.containers[0].resources}'
```

**Expected output:**
```json
{"limits":{"cpu":"1000m","memory":"2Gi"},"requests":{"cpu":"500m","memory":"1Gi"}}
```

### Problem Diagnosis Criteria

| Current State | Problem | Action |
|---------------|---------|--------|
| requests > 5x actual usage | Resource waste | Decrease requests |
| requests < actual usage | Throttling risk | Increase requests |
| limits < peak usage | OOM/throttling occurring | Increase limits |

**Success check:** You can view current settings and compare with actual usage.

---

## Step 3: Calculate Appropriate Values

### Recommended Calculation Formula

```
CPU requests = Normal usage × 1.2 (20% buffer)
CPU limits   = Peak usage × 1.5 or unset

Memory requests = Normal usage × 1.2
Memory limits   = requests × 1.5
```

### Calculation Example

**Measurements:**
- Normal CPU: 50m, Peak CPU: 200m
- Normal Memory: 256Mi, Peak Memory: 400Mi

**Calculations:**
```
CPU requests = 50m × 1.2 = 60m → 100m (rounded)
CPU limits   = 200m × 1.5 = 300m → 500m

Memory requests = 256Mi × 1.2 = 307Mi → 320Mi
Memory limits   = 320Mi × 1.5 = 480Mi → 512Mi
```

### Using VPA Recommender (Optional)

Installing VPA automatically calculates recommended values.

```yaml
# vpa.yaml
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
    updateMode: "Off"  # Only show recommendations, don't auto-apply
```

```bash
kubectl apply -f vpa.yaml
kubectl describe vpa my-app-vpa
```

**Success check:** Recommended values are displayed in the Recommendation section.

---

## Step 4: Apply Changes

{{< callout type="warning" title="Caution" >}}
In production environments, avoid making large changes at once. Adjust incrementally.
{{< /callout >}}

### Modify Deployment

```bash
kubectl edit deployment <deployment-name>
```

Or use the patch command.

```bash
kubectl patch deployment <deployment-name> -p '
{
  "spec": {
    "template": {
      "spec": {
        "containers": [{
          "name": "<container-name>",
          "resources": {
            "requests": {
              "memory": "320Mi",
              "cpu": "100m"
            },
            "limits": {
              "memory": "512Mi",
              "cpu": "500m"
            }
          }
        }]
      }
    }
  }
}'
```

**Success check:** New Pod is created and reaches Running status.

```bash
kubectl rollout status deployment <deployment-name>
```

---

## Step 5: Validate

Verify the following after making changes.

### Check for Throttling

Check if CPU throttling is occurring.

```bash
kubectl exec <pod-name> -- cat /sys/fs/cgroup/cpu/cpu.stat
```

**Check output:**
```
nr_throttled 0      # 0 means no throttling
throttled_time 0    # 0 means normal
```

{{< callout type="tip" title="cgroup v2 Environments" >}}
In cgroup v2 environments, use the following command:
```bash
kubectl exec <pod-name> -- cat /sys/fs/cgroup/cpu.stat
```
{{< /callout >}}

### Check for OOM

Verify no OOM events are occurring.

```bash
kubectl get events --field-selector reason=OOMKilling
```

**Success check:** No events returned.

### Check QoS Class

```bash
kubectl get pod <pod-name> -o jsonpath='{.status.qosClass}'
```

| QoS Class | Meaning | Recommended Scenario |
|-----------|---------|---------------------|
| Guaranteed | requests = limits | Critical workloads |
| Burstable | requests < limits | General workloads |
| BestEffort | No resources set | Test environments only |

**Success check:** Intended QoS class is applied.

---

## Common Errors

### "OOMKilled" Repeatedly Occurring

**Cause:** Memory limits are too low.

**Solution:**
```bash
# Check current limits
kubectl describe pod <pod-name> | grep -A 2 "Limits"

# Increase limits (e.g., 512Mi → 1Gi)
kubectl patch deployment <name> -p '{"spec":{"template":{"spec":{"containers":[{"name":"<container>","resources":{"limits":{"memory":"1Gi"}}}]}}}}'
```

### Response Delay Due to CPU Throttling

**Cause:** CPU limits are too low.

**Solution:**
1. Increase CPU limits, or
2. Remove CPU limits (set only requests)

```yaml
resources:
  requests:
    cpu: "100m"
  # limits.cpu omitted - can use node CPU
  limits:
    memory: "512Mi"
```

### "0/N nodes are available: Insufficient cpu/memory"

**Cause:** requests exceed available node resources.

**Solution:**
```bash
# Check node available resources
kubectl describe nodes | grep -A 5 "Allocatable"

# Adjust requests to be within available resources
```

### Metrics Server "error: Metrics API not available"

**Cause:** Metrics Server is not installed or not ready.

**Solution:**
```bash
# Check Metrics Server status
kubectl get pods -n kube-system | grep metrics-server

# Check logs
kubectl logs -n kube-system deployment/metrics-server
```

---

## Java Application Specific Settings

JVM heap memory configuration is critical for Java applications.

{{< callout type="warning" title="Caution" >}}
Setting JVM heap equal to container memory will cause OOM. JVM uses additional memory beyond heap (metaspace, thread stacks, etc.).
{{< /callout >}}

### Recommended Settings

```yaml
resources:
  requests:
    memory: "512Mi"
  limits:
    memory: "1Gi"
env:
- name: JAVA_OPTS
  value: "-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
```

### Heap Ratio by Container Memory

| Container Memory | Recommended Heap Ratio | Reason |
|-----------------|------------------------|--------|
| < 512Mi | 50-60% | Non-heap memory ratio is relatively higher |
| 512Mi - 2Gi | 65-75% | Typical settings |
| > 2Gi | 75-80% | Non-heap ratio decreases for large heaps |

**Success check:** Pod runs stably without OOM.

---

## Checklist

### Measurement
- [ ] Is Metrics Server installed?
- [ ] Have you measured usage for at least 1 hour?
- [ ] Did you check peak time usage?

### Configuration
- [ ] Are requests 100-120% of normal usage?
- [ ] Can limits accommodate peak usage?
- [ ] For Java apps, is JVM heap appropriately configured?

### Validation
- [ ] Is the Pod in Running status after changes?
- [ ] Is there no CPU throttling?
- [ ] Is there no OOM occurring?
- [ ] Is the intended QoS class applied?

---

## Next Steps

| Goal | Recommended Document |
|------|---------------------|
| Configure auto-scaling | [Scaling]({{< relref "/docs/kubernetes/concepts/scaling" >}}) |
| Resolve Pod issues | [Pod Troubleshooting]({{< relref "/docs/kubernetes/howto/pod-troubleshooting" >}}) |
| Resource management concepts | [Resource Management]({{< relref "/docs/kubernetes/concepts/resources" >}}) |
