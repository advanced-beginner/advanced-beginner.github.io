---
title: Aggregation Operators
description: Summarize and analyze data with PromQL aggregation operators - sum, avg, count, topk
weight: 2
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

> **Target Audience**: Developers who know basic PromQL syntax
> **Prerequisites**: [Basic Syntax]({{< relref "/docs/observability/concepts/promql/syntax-basics" >}})
> **What You'll Learn**: Aggregate multiple time series to calculate meaningful metrics

## TL;DR

{{< callout type="info" >}}
**Key Summary:**
- **sum**: Total - total request count
- **avg**: Average - average response time
- **count**: Count - number of active instances
- **topk/bottomk**: Top/bottom N items
- **by/without**: Specify grouping criteria
{{< /callout >}}

## Aggregation Operators List

| Operator | Description | Returns |
|----------|-------------|---------|
| `sum` | Sum | Single value or per-group values |
| `avg` | Average | Single value or per-group values |
| `min` | Minimum | Single value or per-group values |
| `max` | Maximum | Single value or per-group values |
| `count` | Number of time series | Single value or per-group values |
| `stddev` | Standard deviation | Single value or per-group values |
| `stdvar` | Variance | Single value or per-group values |
| `topk` | Top K items | K time series |
| `bottomk` | Bottom K items | K time series |
| `count_values` | Count by value | One time series per value |
| `quantile` | Quantile | Single value or per-group values |

---

## sum (Total)

Calculates the sum of all time series values.

### Basic Usage

```promql
# Total HTTP requests (all instances, all statuses)
sum(http_requests_total)

# Total requests per second
sum(rate(http_requests_total[5m]))
```

### Aggregation by Group

```promql
# Request count by status code
sum by (status) (http_requests_total)

# Result:
# {status="200"} 15000
# {status="404"} 500
# {status="500"} 100

# Request count by service and status
sum by (service, status) (rate(http_requests_total[5m]))
```

### Practical Examples

```promql
# Total error count
sum(rate(http_requests_total{status=~"5.."}[5m]))

# Error rate by service
sum by (service) (rate(http_requests_total{status=~"5.."}[5m]))
/ sum by (service) (rate(http_requests_total[5m]))
```

---

## avg (Average)

Calculates the average of all time series values.

### Basic Usage

```promql
# Overall average CPU usage
avg(node_cpu_seconds_total{mode="idle"})

# Average memory usage per instance
avg by (instance) (
  node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes
) / avg by (instance) (node_memory_MemTotal_bytes) * 100
```

### Practical Examples

```promql
# Average response time by service
avg by (service) (
  rate(http_request_duration_seconds_sum[5m])
  / rate(http_request_duration_seconds_count[5m])
)

# Cluster average CPU usage
100 - avg(rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100
```

---

## count (Count)

Counts the number of time series.

### Basic Usage

```promql
# Number of monitored targets
count(up)

# Number of active targets (up=1)
count(up == 1)

# Number of down targets
count(up == 0)
```

### Aggregation by Group

```promql
# Number of instances per service
count by (job) (up)

# Result:
# {job="api-server"} 5
# {job="web-server"} 3
# {job="database"} 2
```

### Practical Examples

```promql
# Number of services with errors
count(
  sum by (service) (rate(http_requests_total{status=~"5.."}[5m])) > 0
)

# Number of endpoints exceeding SLA response time
count(
  histogram_quantile(0.99, sum by (le, path) (rate(http_request_duration_seconds_bucket[5m])))
  > 0.5
)
```

---

## min / max (Minimum / Maximum)

### Basic Usage

```promql
# Lowest memory usage
min(node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes * 100)

# Highest CPU usage
max(100 - rate(node_cpu_seconds_total{mode="idle"}[5m]) * 100)
```

### Aggregation by Group

```promql
# Maximum response time by service
max by (service) (
  rate(http_request_duration_seconds_sum[5m])
  / rate(http_request_duration_seconds_count[5m])
)
```

---

## topk / bottomk (Top / Bottom K)

### Basic Usage

```promql
# Top 5 endpoints with most requests
topk(5, sum by (path) (rate(http_requests_total[5m])))

# Top 10 instances with highest memory usage
topk(10, node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes)

# Bottom 3 disks with most available space
bottomk(3, node_filesystem_avail_bytes)
```

### Practical Examples

```promql
# Top 5 services with highest error rate
topk(5,
  sum by (service) (rate(http_requests_total{status=~"5.."}[5m]))
  / sum by (service) (rate(http_requests_total[5m]))
)

# Top 3 endpoints with longest latency
topk(3,
  histogram_quantile(0.99,
    sum by (le, path) (rate(http_request_duration_seconds_bucket[5m]))
  )
)
```

---

## by / without (Grouping)

### by: Keep only specified labels

```promql
# Aggregate keeping only status label
sum by (status) (http_requests_total)

# Keep multiple labels
sum by (service, status) (http_requests_total)
```

### without: Keep all labels except specified ones

```promql
# Aggregate excluding instance label
sum without (instance) (http_requests_total)

# Exclude multiple labels
sum without (instance, pod) (http_requests_total)
```

### by vs without

```promql
# These two queries produce identical results
# Labels: {service, instance, status}

# Using by
sum by (service, status) (http_requests_total)

# Using without
sum without (instance) (http_requests_total)
```

{{< callout type="info" >}}
**Selection Criteria:**
- Few labels to keep → Use `by`
- Few labels to remove → Use `without`
{{< /callout >}}

---

## count_values (Count by Value)

Counts how many times specific values appear.

```promql
# Number of instances by version
count_values("version", app_version)

# Result:
# {version="1.0.0"} 5
# {version="1.1.0"} 3
# {version="2.0.0"} 2
```

---

## quantile (Quantile)

Calculates a specific percentile among values.

```promql
# P90 CPU usage across all instances
quantile(0.9, node_cpu_seconds_total)

# P50 response time by service
quantile by (service) (0.5,
  rate(http_request_duration_seconds_sum[5m])
  / rate(http_request_duration_seconds_count[5m])
)
```

{{< callout type="warning" >}}
`quantile()` is used for Gauge or calculated values. To calculate percentiles from Histogram bucket data, use `histogram_quantile()`.
{{< /callout >}}

---

## Practical Patterns

### Error Rate Calculation

```promql
# Overall error rate
sum(rate(http_requests_total{status=~"5.."}[5m]))
/ sum(rate(http_requests_total[5m]))
* 100

# Error rate by service
sum by (service) (rate(http_requests_total{status=~"5.."}[5m]))
/ sum by (service) (rate(http_requests_total[5m]))
* 100
```

### Availability Calculation

```promql
# Success rate (availability)
sum(rate(http_requests_total{status!~"5.."}[5m]))
/ sum(rate(http_requests_total[5m]))
* 100

# Availability by service
(1 - sum by (service) (rate(http_requests_total{status=~"5.."}[5m]))
     / sum by (service) (rate(http_requests_total[5m])))
* 100
```

### Resource Utilization

```promql
# Overall memory usage
(sum(node_memory_MemTotal_bytes) - sum(node_memory_MemAvailable_bytes))
/ sum(node_memory_MemTotal_bytes) * 100

# Disk usage per instance
(sum by (instance) (node_filesystem_size_bytes)
 - sum by (instance) (node_filesystem_avail_bytes))
/ sum by (instance) (node_filesystem_size_bytes) * 100
```

### Traffic Analysis

```promql
# Traffic percentage by endpoint
sum by (path) (rate(http_requests_total[5m]))
/ sum(rate(http_requests_total[5m]))
* 100

# Top 10 endpoints
topk(10, sum by (path) (rate(http_requests_total[5m])))
```

---

## Key Takeaways

| Operator | Purpose | Example |
|----------|---------|---------|
| `sum` | Total | Total request count |
| `avg` | Average | Average CPU usage |
| `count` | Count | Number of instances |
| `min/max` | Extremes | Maximum memory |
| `topk` | Top N | Top 5 traffic |
| `by` | Grouping | Aggregate by service |
| `without` | Exclude labels | Aggregate excluding instance |

---

## Next Steps

| Recommended Order | Document | What You'll Learn |
|-------------------|----------|-------------------|
| 1 | [rate and increase](rate-and-increase/) | Core concepts for handling Counters |
| 2 | [histogram_quantile](histogram-quantile/) | Calculate P99 response time |
