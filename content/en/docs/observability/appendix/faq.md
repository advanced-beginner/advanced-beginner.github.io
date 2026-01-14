---
title: FAQ
description: Frequently asked questions about Observability
weight: 2
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

## General

### Q: What's the difference between Monitoring and Observability?

**Monitoring**: Watching predefined metrics ("Has this value exceeded the threshold?")

**Observability**: The ability to understand the internal state of a system from the outside ("Why did this problem occur?")

Monitoring is part of Observability. Observability includes the ability to analyze unexpected problems.

### Q: Which of the Three Pillars (Metrics, Logs, Traces) should I implement first?

**Recommended order**:
1. **Metrics** - Understand system state, set up alerts
2. **Logs** - Analyze error causes
3. **Traces** - Analyze distributed system flows

For a single service, Metrics + Logs may be sufficient. For microservices, Traces are essential.

---

## Prometheus

### Q: Which is better - Pull vs Push?

**Pull (Prometheus)**:
- Pros: Central control, built-in health checks, easy debugging
- Cons: Difficult to access targets behind firewalls

**Push (Datadog, StatsD)**:
- Pros: Firewall-friendly, suitable for short-lived jobs
- Cons: Difficult to determine target status

**Conclusion**: Pull model is better for operations in most cases. Use Pushgateway for short batch jobs.

### Q: What should I set for scrape_interval?

| Situation | Recommended Value |
|-----------|-------------------|
| General | 15-30 seconds |
| High-frequency change detection | 5-10 seconds |
| Low priority/cost reduction | 60 seconds |

**Note**: Too short increases Prometheus load, too long delays anomaly detection.

### Q: What problems occur with high cardinality?

- Memory usage surge
- Query speed degradation
- Storage cost increase
- Can cause OOM

**Solution**: See [Cardinality Optimization]({{< relref "/docs/observability/howto/reduce-cardinality" >}})

---

## PromQL

### Q: What's the difference between rate() and increase()?

```promql
# rate(): average per-second rate of increase
rate(http_requests_total[5m])  # → 10 (10 per second)

# increase(): total increase
increase(http_requests_total[5m])  # → 3000 (3000 in 5 minutes)
```

**rate()** = increase() / time(seconds)

- Dashboards: use rate()
- Period totals: use increase()

### Q: Why should I apply rate() to Counters?

Counters are cumulative values, so raw values are meaningless.

```promql
# ❌ Total requests since server start (varies by start time)
http_requests_total  # → 1,523,456

# ✅ Requests per second (current load)
rate(http_requests_total[5m])  # → 42.5
```

### Q: Why is the le label needed in histogram_quantile()?

Histograms store cumulative counts per bucket (le = less than or equal).

```
bucket{le="0.1"} 100   # 100 requests ≤ 0.1s
bucket{le="0.5"} 350   # 350 requests ≤ 0.5s
bucket{le="1.0"} 480   # 480 requests ≤ 1.0s
```

Without the `le` label, the bucket structure breaks and percentile calculation becomes impossible.

---

## Grafana

### Q: My dashboard is too slow, what should I do?

1. **Use Recording Rules**: Pre-calculate complex queries
2. **Limit time range**: Recent 1 hour → Recent 6 hours
3. **Limit samples**: Use `$__rate_interval` variable
4. **Remove unnecessary panels**: Lazy loading for panels requiring scrolling

### Q: How do I use Variables?

```yaml
# Service selection dropdown
- name: service
  query: label_values(http_requests_total, service)

# Use in query
rate(http_requests_total{service="$service"}[5m])
```

Variables allow viewing multiple services with a single dashboard.

---

## Alerting

### Q: Why should I set the for clause?

Prevents false positives from temporary spikes.

```yaml
# ❌ Alerts on momentary spikes
expr: error_rate > 0.05

# ✅ Alerts only after 5 minutes of sustained condition
expr: error_rate > 0.05
for: 5m
```

### Q: How do I reduce too many alerts?

1. **Adjust thresholds**: Not too sensitive
2. **Increase for time**: Filter temporary anomalies
3. **Grouping**: Batch similar alerts in Alertmanager
4. **Inhibition**: Suppress lower-level alerts when upper-level alerts fire
5. **Only actionable alerts**: Remove alerts that don't require action

---

## Distributed Tracing

### Q: What sampling rate should I set?

| Environment | Sampling Rate | Reason |
|------------|--------------|--------|
| Development | 100% | Trace all requests |
| Staging | 50% | Sufficient data |
| Production | 1-10% | Cost optimization |

**Tip**: Use Tail-based sampling to collect 100% of errors/slow requests

### Q: How do I link Trace IDs with logs?

Include Trace ID in logs.

```java
// Spring Boot (automatic)
// Log pattern: %X{traceId:-}

// Log output
2026-01-12 10:30:00 [order-service,abc123,span001] Order created
```

Set up Loki → Tempo connection in Grafana to jump directly from logs to traces.

---

## Cost

### Q: How do I reduce Observability costs?

**Metrics**:
- Optimize cardinality
- Drop unnecessary metrics
- Adjust scrape_interval

**Logs**:
- Long-term retention only for ERROR and above
- Short-term retention or no collection for DEBUG
- Sampling

**Traces**:
- Sampling (1-10%)
- Tail-based sampling

### Q: Open source vs Commercial service, which is better?

**Open source (Prometheus, Loki, Tempo)**:
- Cost: Infrastructure only
- Operations: Self-management required
- Suitable for: Teams with DevOps capability

**Commercial (Datadog, New Relic)**:
- Cost: Usage-based pricing
- Operations: No management needed
- Suitable for: Quick adoption, limited operations staff

**Hybrid**: Open source + Grafana Cloud (managed)
