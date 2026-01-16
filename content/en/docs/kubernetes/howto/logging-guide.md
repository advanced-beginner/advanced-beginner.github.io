---
lastmod: "2026-01-16"
title: Log Collection & Analysis
weight: 4
author:
  name: Advanced Beginner
  github: advanced-beginner
---

> **Objective**: Effectively collect and analyze logs to diagnose problems
> **Estimated Time**: 25 minutes

{{< callout type="info" title="Scope of This Guide" >}}
**Covers**: kubectl logs usage, multi-container logs, log filtering, stern tool

**Does not cover**: Central logging system (ELK, Loki) setup, long-term log retention
{{< /callout >}}

## Before You Begin

Verify the following prerequisites.

### 1. Verify kubectl Installation and Cluster Access

```bash
kubectl cluster-info
```

**Success output:**
```
Kubernetes control plane is running at https://xxx.xxx.xxx.xxx
```

### 2. Verify Log Access Permissions

```bash
kubectl auth can-i get pods/log
```

**Success output:**
```
yes
```

### 3. Verify Target Pods

Verify the Pods you want to check logs for exist.

```bash
kubectl get pods
```

**Success output:**
```
NAME                     READY   STATUS    RESTARTS   AGE
my-app-xxx-yyy           1/1     Running   0          5m
```

---

## Step 1: Basic Log Collection

### Check Current Logs

```bash
kubectl logs <pod-name>
```

**Expected output:**
```
2024-01-15T10:30:45.123Z INFO  Starting application...
2024-01-15T10:30:46.456Z INFO  Connected to database
2024-01-15T10:30:47.789Z INFO  Server listening on port 8080
```

### Check Only Last N Lines

Use this when the full log is too long.

```bash
kubectl logs <pod-name> --tail=100
```

**Success check:** Logs are displayed.

### Real-time Log Streaming

Monitor application behavior in real-time.

```bash
kubectl logs <pod-name> -f
```

{{< callout type="tip" title="How to Exit" >}}
Press `Ctrl+C` to stop streaming.
{{< /callout >}}

### Filter by Time Range

Check only logs from the last hour.

```bash
kubectl logs <pod-name> --since=1h
```

Or check logs after a specific time.

```bash
kubectl logs <pod-name> --since-time="2024-01-15T10:00:00Z"
```

**Success check:** Only logs from specified time range are displayed.

---

## Step 2: Logs from Restarted Containers

Check previous container logs when a Pod has restarted.

### Check Previous Container Logs

```bash
kubectl logs <pod-name> --previous
```

**Expected output:**
```
2024-01-15T10:25:00.000Z ERROR Connection timeout
2024-01-15T10:25:01.000Z ERROR Application crashed
```

{{< callout type="warning" title="Note" >}}
The `--previous` option only shows logs from the most recently terminated container. For older logs, you need a central logging system.
{{< /callout >}}

### Check Restart Count

```bash
kubectl get pod <pod-name> -o jsonpath='{.status.containerStatuses[0].restartCount}'
```

**Success check:** Previous container logs are displayed.

---

## Step 3: Multi-Container Pod Logs

When a Pod has multiple containers.

### List Containers

```bash
kubectl get pod <pod-name> -o jsonpath='{.spec.containers[*].name}'
```

**Expected output:**
```
app sidecar
```

### Check Specific Container Logs

```bash
kubectl logs <pod-name> -c <container-name>
```

**Example:**
```bash
kubectl logs my-app-xxx-yyy -c sidecar
```

### Check All Container Logs

```bash
kubectl logs <pod-name> --all-containers=true
```

**Success check:** Logs from each container are displayed separately.

---

## Step 4: Collect Logs from Multiple Pods

Collect logs from all Pods belonging to a Deployment.

### Collect Logs by Label

```bash
kubectl logs -l app=<app-name> --all-containers=true
```

{{< callout type="warning" title="Limitation" >}}
When collecting logs with label selector, you cannot use the `-f` (follow) option.
{{< /callout >}}

### Using stern Tool (Recommended)

stern is a tool for collecting logs from multiple Pods in real-time.

**Installation:**
{{< tabs "stern-install" >}}
{{< tab "macOS" >}}
```bash
brew install stern
```
{{< /tab >}}
{{< tab "Linux" >}}
```bash
# Download latest release
curl -Lo stern https://github.com/stern/stern/releases/latest/download/stern_linux_amd64
chmod +x stern
sudo mv stern /usr/local/bin/
```
{{< /tab >}}
{{< /tabs >}}

**Usage:**
```bash
# Collect logs by Pod name pattern
stern my-app

# Specify namespace
stern my-app -n production

# Specific container only
stern my-app -c app
```

**Expected output (color-coded):**
```
my-app-xxx-yyy app 2024-01-15T10:30:45Z INFO Starting...
my-app-xxx-zzz app 2024-01-15T10:30:46Z INFO Starting...
```

**Success check:** Logs from multiple Pods are displayed together.

---

## Step 5: Log Analysis Patterns

### Filter Error Logs

```bash
kubectl logs <pod-name> | grep -i error
```

Or find both ERROR and WARN.

```bash
kubectl logs <pod-name> | grep -iE "(error|warn)"
```

### Trace Specific Requests

Filter by Request ID or Trace ID.

```bash
kubectl logs <pod-name> | grep "request-id-12345"
```

### Analyze Log Frequency by Time

Check number of errors per minute.

```bash
kubectl logs <pod-name> | grep -i error | cut -d'T' -f1-2 | cut -d':' -f1-2 | uniq -c
```

**Expected output:**
```
   5 2024-01-15T10:30
  12 2024-01-15T10:31
   3 2024-01-15T10:32
```

### Parse JSON Logs (Using jq)

When application outputs logs in JSON format.

```bash
kubectl logs <pod-name> | jq 'select(.level == "error")'
```

Extract specific fields.

```bash
kubectl logs <pod-name> | jq '{time: .timestamp, msg: .message, err: .error}'
```

**Success check:** Logs with desired patterns are filtered.

---

## Common Errors

### "error: container not found"

**Cause:** Container name is incorrect or Pod doesn't have that container.

**Solution:**
```bash
# List Pod containers
kubectl get pod <pod-name> -o jsonpath='{.spec.containers[*].name}'
```

### "error: previous terminated container not found"

**Cause:** Pod has never restarted or previous logs were deleted.

**Solution:**
```bash
# Check restart count
kubectl describe pod <pod-name> | grep "Restart Count"
```

If restart count is 0, you cannot use `--previous` option.

### Logs Are Empty

**Cause 1:** Application doesn't output logs to stdout/stderr

**Solution:**
```bash
# If application writes to file, check inside container
kubectl exec <pod-name> -- cat /var/log/app.log
```

**Cause 2:** Container just started

**Solution:**
```bash
# Check container start time
kubectl describe pod <pod-name> | grep "Started:"
```

### Logs Are Truncated

**Cause:** Log buffer limit reached.

**Solution:**
```bash
# Get all logs without limit (caution: may increase memory usage)
kubectl logs <pod-name> --limit-bytes=0
```

Or specify time range.

```bash
kubectl logs <pod-name> --since=10m
```

---

## Useful Log Commands Collection

### Quick Debugging

```bash
# Last 50 error lines
kubectl logs <pod-name> --tail=500 | grep -i error | tail -50

# Monitor errors in real-time
kubectl logs <pod-name> -f | grep -i error

# Recent errors from all Pods
kubectl logs -l app=my-app --all-containers --tail=100 | grep -i error
```

### Save Logs

```bash
# Save to file
kubectl logs <pod-name> > pod-logs.txt

# Save with timestamps
kubectl logs <pod-name> --timestamps > pod-logs-with-time.txt
```

### Check with Container Events

```bash
# Pod events (information beyond logs)
kubectl describe pod <pod-name> | tail -20
```

---

## Environment-Specific Notes

{{< tabs "env-logging" >}}
{{< tab "Minikube" >}}
```bash
# Check logs directly on Minikube node
minikube ssh
sudo journalctl -u kubelet

# Container runtime logs
minikube logs
```
{{< /tab >}}
{{< tab "EKS" >}}
```bash
# CloudWatch Logs Insights query
# In AWS Console: CloudWatch > Logs Insights

# Example query:
fields @timestamp, @message
| filter @logStream like /my-app/
| filter @message like /error/i
| sort @timestamp desc
| limit 100
```
{{< /tab >}}
{{< tab "GKE" >}}
```bash
# Cloud Logging query
gcloud logging read 'resource.type="k8s_container" AND resource.labels.pod_name:"my-app"' --limit=50

# Filter errors only
gcloud logging read 'resource.type="k8s_container" AND severity>=ERROR' --limit=50
```
{{< /tab >}}
{{< /tabs >}}

---

## Checklist

### Basic Log Collection
- [ ] Can you view logs with `kubectl logs <pod-name>`?
- [ ] If restarted, did you check previous logs with `--previous`?
- [ ] For multi-container Pods, did you specify container with `-c`?

### Advanced Analysis
- [ ] Did you filter error patterns with grep?
- [ ] Did you specify time range with `--since`?
- [ ] For multiple Pod logs, did you use stern?

### Troubleshooting
- [ ] If logs are empty, did you verify stdout/stderr output?
- [ ] Did you verify container name is correct?

---

## Next Steps

| Goal | Recommended Document |
|------|---------------------|
| Resolve Pod issues | [Pod Troubleshooting](pod-troubleshooting/) |
| Resolve network issues | [Network Troubleshooting](network-troubleshooting/) |
| Configure health checks | [Health Checks]({{< relref "/docs/kubernetes/concepts/health-checks" >}}) |
