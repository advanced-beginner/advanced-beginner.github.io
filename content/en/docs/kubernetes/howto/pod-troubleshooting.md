---
lastmod: "2026-01-16"
title: Pod Troubleshooting
weight: 1
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **Objective**: Identify and resolve issues when Pods fail to start or terminate abnormally
> **Estimated Time**: 30 minutes

{{< callout type="info" title="Scope of This Guide" >}}
**Covers**: Pod startup failures, CrashLoopBackOff, ImagePullBackOff, Readiness failures

**Does not cover**: Network connectivity issues (see [Network Troubleshooting](network-troubleshooting/)), performance degradation due to resource constraints (see [Resource Optimization](resource-optimization/))
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

{{< callout type="warning" title="Version Compatibility" >}}
kubectl version must be within ±1 minor version of cluster version.
Example: Cluster 1.27 → kubectl 1.26~1.28 supported
{{< /callout >}}

### 2. Verify Cluster Access

```bash
kubectl cluster-info
```

**Success output:**
```
Kubernetes control plane is running at https://xxx.xxx.xxx.xxx
```

### 3. Verify Permissions

```bash
kubectl auth can-i get pods
kubectl auth can-i get events
```

**Success output (each):**
```
yes
```

---

## Step 1: Check Pod Status

First, check the current status of the Pod.

```bash
kubectl get pods
```

**Expected output:**
```
NAME                     READY   STATUS             RESTARTS   AGE
my-app-xxx-yyy           1/1     Running            0          5m
my-app-xxx-zzz           0/1     CrashLoopBackOff   3          2m
```

Based on Pod status, proceed to the appropriate step.

| Status | Meaning | Next Step |
|--------|---------|-----------|
| `Pending` | Waiting for scheduling | [Step 2: Resolve Pending](#step-2-resolve-pending-status) |
| `ImagePullBackOff` | Image pull failure | [Step 3: Resolve Image Issues](#step-3-resolve-image-issues) |
| `CrashLoopBackOff` | Container repeatedly restarting | [Step 4: Resolve Crashes](#step-4-resolve-crashloopbackoff) |
| `Running` but not Ready | Health check failure | [Step 5: Resolve Readiness](#step-5-resolve-running-but-not-ready) |

---

## Step 2: Resolve Pending Status

The Pod is stuck in Pending status.

### Diagnose the Cause

```bash
kubectl describe pod <pod-name>
```

**Check the Events section.** Scroll down to the last portion.

### Common Causes and Solutions

#### Cause 1: Insufficient Resources

**Event message:**
```
Warning  FailedScheduling  Insufficient cpu/memory
```

**Solution:**

1. Check node resource status.
   ```bash
   kubectl top nodes
   ```

2. Reduce resource requests or add more nodes.
   ```yaml
   resources:
     requests:
       memory: "256Mi"  # reduce
       cpu: "100m"
   ```

#### Cause 2: Node Selector Mismatch

**Event message:**
```
Warning  FailedScheduling  0/3 nodes are available: node(s) didn't match node selector
```

**Solution:**

1. Check node labels.
   ```bash
   kubectl get nodes --show-labels
   ```

2. Verify Pod's nodeSelector matches available labels.

#### Cause 3: Waiting for PVC Binding

**Event message:**
```
Warning  FailedScheduling  persistentvolumeclaim "my-pvc" not found
```

**Solution:**

1. Check PVC status.
   ```bash
   kubectl get pvc
   ```

2. Verify PVC is in Bound status.

**Success check:** Status changes from `Pending` in `kubectl get pod <pod-name>`.

---

## Step 3: Resolve Image Issues

ImagePullBackOff or ErrImagePull status.

### Diagnose the Cause

```bash
kubectl describe pod <pod-name>
```

**Check Events section for image-related messages.**

### Common Causes and Solutions

#### Cause 1: Image Name/Tag Typo

**Event message:**
```
Failed to pull image "ngninx:latest": rpc error: code = NotFound
```

**Solution:**

1. Verify image name.
   ```bash
   kubectl get deployment <name> -o jsonpath='{.spec.template.spec.containers[0].image}'
   ```

2. Correct the image name and tag.

#### Cause 2: Private Registry Authentication Required

**Event message:**
```
Failed to pull image: unauthorized: authentication required
```

**Solution:**

1. Create imagePullSecret.
   ```bash
   kubectl create secret docker-registry my-registry-secret \
     --docker-server=registry.example.com \
     --docker-username=myuser \
     --docker-password=mypassword
   ```

2. Add secret to Deployment.
   ```yaml
   spec:
     imagePullSecrets:
     - name: my-registry-secret
   ```

**Success check:** Status changes to `Running` in `kubectl get pod <pod-name>`.

---

## Step 4: Resolve CrashLoopBackOff

Container terminates immediately after starting and keeps restarting.

### Diagnose the Cause

**First, check previous container logs.**

```bash
kubectl logs <pod-name> --previous
```

{{< callout type="tip" title="When Logs Are Empty" >}}
If the container terminates too quickly, logs may be empty. In this case, check the Exit Code.
{{< /callout >}}

**Check Exit Code:**

```bash
kubectl describe pod <pod-name>
```

**Find the Last State section and check the Exit Code:**
```
Last State:     Terminated
  Exit Code:    137
  Reason:       OOMKilled
```

### Solutions by Exit Code

| Exit Code | Meaning | Solution |
|-----------|---------|----------|
| **0** | Normal termination | Container command completed immediately. Verify ENTRYPOINT/CMD runs a long-running process. |
| **1** | Application error | Check logs for error messages. Review environment variables and configuration files. |
| **137** | OOM Killed | Increase memory limits. |
| **143** | SIGTERM | Normal termination signal. Check livenessProbe settings. |

#### Resolving OOM Killed

```yaml
resources:
  limits:
    memory: "512Mi"  # increase from previous value
```

**Success check:** RESTARTS stops increasing and `Running` status is maintained in `kubectl get pod <pod-name>`.

---

## Step 5: Resolve Running but not Ready

Pod is Running but Ready is 0/1.

### Diagnose the Cause

```bash
kubectl describe pod <pod-name>
```

**Check Conditions section for `Ready: False`.**

**Look for Readiness probe failure messages in Events section:**
```
Warning  Unhealthy  Readiness probe failed: Get "http://10.x.x.x:8080/health": connection refused
```

### Common Causes and Solutions

#### Cause 1: Incorrect Endpoint

**Solution:**

1. Test the endpoint directly inside the Pod.
   ```bash
   kubectl exec <pod-name> -- curl -s localhost:8080/health
   ```

2. Correct the endpoint path and port.

#### Cause 2: Insufficient Application Startup Time

**Solution:**

Increase initialDelaySeconds.

```yaml
readinessProbe:
  httpGet:
    path: /health
    port: 8080
  initialDelaySeconds: 30  # increase
  periodSeconds: 10
```

**Success check:** READY shows `1/1` in `kubectl get pod <pod-name>`.

---

## Common Errors

### "error: pod not found"

**Cause:** Pod name is incorrect or it's in a different namespace.

**Solution:**
```bash
# Search for Pod in all namespaces
kubectl get pods --all-namespaces | grep <pod-name>

# Specify correct namespace
kubectl get pod <pod-name> -n <namespace>
```

### "error: unable to upgrade connection"

**Cause:** Connection issue with API server during kubectl exec.

**Solution:**
- Check network connectivity
- Check proxy settings

### "OCI runtime create failed: container_linux.go"

**Cause:** Container runtime issue.

**Solution:**
- Verify image is compatible with current architecture (amd64 vs arm64)
- Check node's container runtime status

---

## Environment-Specific Notes

{{< tabs "env-specific" >}}
{{< tab "Minikube" >}}
```bash
# Debug directly via Minikube SSH
minikube ssh

# When using local images
eval $(minikube docker-env)
docker images
```
{{< /tab >}}
{{< tab "EKS" >}}
```bash
# Check logs in CloudWatch Logs
aws logs filter-log-events --log-group-name /aws/eks/<cluster>/cluster

# Check node status
kubectl get nodes -o wide
```
{{< /tab >}}
{{< tab "GKE" >}}
```bash
# Check logs in Cloud Logging
gcloud logging read "resource.type=k8s_container"

# Check node pool status
gcloud container node-pools list --cluster <cluster-name>
```
{{< /tab >}}
{{< /tabs >}}

---

## Checklist

### Startup Failures (Pending)
- [ ] Verified events with `kubectl describe pod`
- [ ] Verified node resources are sufficient (`kubectl top nodes`)
- [ ] Checked nodeSelector/tolerations settings

### Image Issues
- [ ] Verified image name and tag are correct
- [ ] Verified imagePullSecrets configuration (private registry)
- [ ] Verified registry accessibility

### Repeated Crashes
- [ ] Checked logs with `kubectl logs --previous`
- [ ] Checked Exit Code (137 = OOM)
- [ ] Verified environment variables/ConfigMap/Secret settings

### Not Ready
- [ ] Verified Readiness Probe endpoint and port
- [ ] Verified initialDelaySeconds is sufficient
- [ ] Checked external dependencies (DB, etc.) connectivity

---

## Next Steps

| Goal | Recommended Document |
|------|---------------------|
| Resolve network issues | [Network Troubleshooting](network-troubleshooting/) |
| Optimize resources | [Resource Optimization](resource-optimization/) |
| Configure health checks | [Health Checks]({{< relref "/docs/kubernetes/concepts/health-checks" >}}) |
| Practice deployment | [Spring Boot Deployment]({{< relref "/docs/kubernetes/examples/spring-boot" >}}) |
