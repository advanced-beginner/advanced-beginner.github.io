---
title: Alerting Actions Guide
description: Response strategies after PromQL detection
weight: 3
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

Situational guide for responding after receiving alerts.

## Alert → Action Workflow

```mermaid
graph LR
    A["Receive Alert"] --> B["Assess Situation"]
    B --> C["Evaluate Impact"]
    C --> D["Immediate Action"]
    D --> E["Root Cause Analysis"]
    E --> F["Permanent Fix"]
    F --> G["Post-Incident Review"]
```

---

## Response by Situation

### 1. Service Down (TargetDown)

**Alert**
```yaml
alert: TargetDown
expr: up == 0
```

**Immediate Actions**
```bash
# 1. Check service status
kubectl get pods -l app=order-service
docker ps -f name=order-service

# 2. Check logs
kubectl logs -l app=order-service --tail=100
docker logs order-service --tail=100

# 3. Attempt restart
kubectl rollout restart deployment/order-service
docker restart order-service
```

**Root Cause Analysis**
- Check for OOM Kill
- Health check failure cause
- Dependency service status

**Automation Possible**
```yaml
# Kubernetes: Automatic restart
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  failureThreshold: 3
```

---

### 2. High Error Rate (HighErrorRate)

**Alert**
```yaml
alert: HighErrorRate
expr: error_rate > 0.05
```

**Immediate Actions**
1. **Check impact scope**
   ```promql
   sum by (uri) (rate(http_requests_total{status=~"5.."}[5m]))
   ```

2. **Check recent deployments**
   ```bash
   kubectl rollout history deployment/order-service
   ```

3. **Rollback (if caused by deployment)**
   ```bash
   kubectl rollout undo deployment/order-service
   ```

**Root Cause Analysis**
```logql
{app="order-service"} | json | level="ERROR"
```

---

### 3. High Latency (HighP99Latency)

**Alert**
```yaml
alert: HighP99Latency
expr: p99 > 0.5
```

**Immediate Actions**
1. **Identify bottleneck** (Tracing)
2. **Check resources**
   ```promql
   # CPU
   rate(process_cpu_seconds_total[5m])
   # Memory
   jvm_memory_used_bytes / jvm_memory_max_bytes
   # DB connections
   hikaricp_connections_active / hikaricp_connections_max
   ```

3. **Scale out (if resource shortage)**
   ```bash
   kubectl scale deployment/order-service --replicas=5
   ```

**Root Cause Analysis**
- Check slow queries
- External API response time
- GC logs

---

### 4. Resource Shortage (HighCPU/Memory/Disk)

**Alerts**
```yaml
alert: HighCPUUsage
expr: cpu_usage > 80

alert: HighMemoryUsage
expr: memory_usage > 85

alert: DiskSpaceLow
expr: disk_avail_percent < 10
```

**High CPU**
```bash
# Check processes
top -c

# Scale out
kubectl scale deployment/order-service --replicas=5
```

**Memory Shortage**
```bash
# Memory-consuming processes
ps aux --sort=-%mem | head

# JVM heap dump (Java)
jmap -dump:format=b,file=heap.bin <pid>
```

**Disk Shortage**
```bash
# Find large files
du -sh /* | sort -hr | head

# Clean up logs
journalctl --vacuum-time=7d
docker system prune -a
```

---

### 5. Kafka Consumer Lag

**Alert**
```yaml
alert: KafkaConsumerLagHigh
expr: kafka_consumer_lag > 10000
```

**Immediate Actions**
1. **Check consumer status**
   ```bash
   kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
     --describe --group order-consumer-group
   ```

2. **Restart consumer**
   ```bash
   kubectl rollout restart deployment/order-consumer
   ```

3. **Scale consumer**
   ```bash
   kubectl scale deployment/order-consumer --replicas=5
   ```

**Root Cause Analysis**
- Processing speed vs incoming speed
- Consumer error logs
- Partition imbalance

---

## Automated Actions

### 1. Alertmanager Webhook

```yaml
# alertmanager.yml
receivers:
  - name: 'auto-scale'
    webhook_configs:
      - url: 'http://auto-scaler:8080/scale'
        send_resolved: true
```

```python
# auto-scaler.py
@app.route('/scale', methods=['POST'])
def handle_alert():
    alert = request.json['alerts'][0]
    if alert['labels']['alertname'] == 'HighCPUUsage':
        scale_deployment(alert['labels']['service'], replicas=5)
    return 'OK'
```

### 2. Kubernetes HPA

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: order-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: order-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
```

### 3. Automatic Circuit Breaker Activation

```java
@CircuitBreaker(name = "payment", fallbackMethod = "fallback")
public Payment process(PaymentRequest request) {
    return paymentClient.process(request);
}

public Payment fallback(PaymentRequest request, Exception e) {
    return Payment.pending(request.getId());
}
```

---

## Runbook Template

```markdown
# Alert: HighErrorRate

## Symptoms
- Error rate exceeds 5%
- Affected service: order-service

## Checks
1. [ ] Check recent deployments
2. [ ] Check error logs
3. [ ] Check dependency service status

## Immediate Actions
1. Deployment cause: `kubectl rollout undo deployment/order-service`
2. Traffic surge: Scale out
3. Dependency failure: Check circuit breaker

## Escalation
- If not resolved within 15 minutes: Call team lead
- If not resolved within 30 minutes: Call on-call engineer

## Related Links
- Dashboard: https://grafana/d/order-service
- Logs: https://grafana/explore?datasource=loki
- Traces: https://grafana/explore?datasource=tempo
```

---

## Key Summary

| Alert Type | Immediate Action | Automation Possible |
|-----------|------------------|---------------------|
| Service Down | Restart | Liveness Probe |
| High Error Rate | Rollback | Canary Deployment |
| High Latency | Scale Out | HPA |
| Resource Shortage | Clean/Scale | HPA, Cleanup CronJob |
| Kafka Lag | Scale Consumer | KEDA |
