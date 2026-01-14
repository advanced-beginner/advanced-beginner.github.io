---
title: Optimizing Metric Cardinality
description: Prevent Prometheus time series explosion and reduce costs
weight: 2
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

> **Target Scenario**: Prometheus memory/storage spike, slow queries
> **Goal**: Optimize costs by reducing unnecessary time series
> **Duration**: 30 minutes~1 hour (depending on analysis and fix complexity)
> **Success Criteria**: Time series count reduced below target and memory usage stabilized

## Problem Scenario

```
Alert: PrometheusHighCardinality
Active Series: 2,500,000 (Threshold: 1,000,000)
Memory Usage: 32GB
```

## What is Cardinality?

**Cardinality = Number of unique time series**

```
http_requests_total{method="GET", status="200", path="/api/users"}     # 1 series
http_requests_total{method="GET", status="200", path="/api/users/123"} # Another 1!
http_requests_total{method="GET", status="200", path="/api/users/456"} # Another 1!
```

**Problem**: If user_id is in path, time series created for each user

## Step 1: Cardinality Analysis

### Current Time Series Count

```promql
# Total time series count
count({__name__=~".+"})

# Time series count by metric
count by (__name__) ({__name__=~".+"})

# Top 10 metrics
topk(10, count by (__name__) ({__name__=~".+"}))
```

### Cardinality by Label

```promql
# Unique value count by label
count(count by (path) (http_requests_total))

# Find high cardinality labels
count by (path) (http_requests_total)
```

### Check TSDB Status

```bash
curl http://localhost:9090/api/v1/status/tsdb | jq .
```

## Step 2: Identify Problem Labels

### Dangerous Patterns

| Pattern | Problem | Expected Time Series |
|------|------|------------|
| `user_id` | One per user | Hundreds of thousands+ |
| `request_id` | One per request | Infinite |
| `timestamp` | One per time | Infinite |
| `session_id` | One per session | Tens of thousands+ |
| URL path (with ID) | One per ID | Tens of thousands+ |

### Safe Patterns

| Pattern | Expected Time Series |
|------|------------|
| `method` (GET/POST) | ~5 |
| `status` (2xx/4xx/5xx) | ~5 |
| `service` | ~100 |
| `endpoint` (normalized) | ~100 |

## Step 3: Solutions

### 1. Fix in Application

```java
// ❌ Bad: Dynamic path
Timer.builder("http_requests")
    .tag("path", "/users/123")  // user_id included
    .register(registry);

// ✅ Good: Normalized path
Timer.builder("http_requests")
    .tag("path", "/users/{id}")  // Pattern-based
    .register(registry);
```

### Spring Boot Configuration

```java
@Configuration
public class MetricsConfig {
    @Bean
    public WebMvcTagsContributor webMvcTagsContributor() {
        return (exchange, response, handler) -> {
            // Normalize URL
            String pattern = getPattern(handler);
            return Tags.of("uri", pattern != null ? pattern : "UNKNOWN");
        };
    }
}
```

### 2. Prometheus Relabeling

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'app'
    metric_relabel_configs:
      # Remove specific labels
      - action: labeldrop
        regex: 'user_id|request_id|session_id'

      # Exclude specific metrics
      - source_labels: [__name__]
        regex: 'go_.*'
        action: drop

      # Exclude high cardinality metrics
      - source_labels: [__name__]
        regex: 'http_requests_total'
        action: drop
        # Use recording rule instead
```

### 3. Aggregate with Recording Rules

```yaml
# Original: High cardinality
# http_requests_total{method, status, path, instance, pod}

# Recording Rule: Only necessary labels
groups:
  - name: aggregation
    rules:
      - record: job:http_requests:rate5m
        expr: sum by (job, method, status) (rate(http_requests_total[5m]))
```

### 4. Limit Collection Itself

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'app'
    sample_limit: 10000  # Limit sample count
    metric_relabel_configs:
      - source_labels: [__name__]
        regex: 'go_.*|process_.*'  # Unnecessary metrics
        action: drop
```

## Step 4: Verification

### Before/After Comparison

```promql
# Time series count change
count({__name__=~".+"})

# Memory usage
process_resident_memory_bytes{job="prometheus"}

# Query time
prometheus_engine_query_duration_seconds
```

## Prevention Guidelines

### Label Design Principles

1. **Label values must be finite**
   - ✅ `method=GET|POST|PUT|DELETE`
   - ❌ `user_id=12345`

2. **Predict label combinations**
   ```
   Total time series = Metric count × Label1 × Label2 × ...

   Example: 100 metrics × 5 methods × 5 statuses × 100 paths = 250,000
   ```

3. **Dynamic values as metric values**
   ```java
   // ❌ As label
   Counter.builder("orders").tag("order_id", id).register(registry);

   // ✅ As log
   log.info("Order created: {}", orderId);
   ```

### Code Review Checklist

- [ ] No dynamic label values?
- [ ] Predicted label combination count?
- [ ] No ID or timestamp included?
- [ ] URL pattern-based?

---

## Quick Reference

| Situation | Solution |
|------|--------|
| user_id label | Remove or use logs |
| ID in URL | Pattern-based (`/users/{id}`) |
| Unnecessary metrics | `action: drop` |
| High cardinality metrics | Aggregate with Recording Rule |
