---
title: Basic Syntax
description: PromQL selectors, label matching, time ranges - master the fundamentals of queries
weight: 1
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

> **Target Audience**: Developers learning PromQL for the first time
> **Prerequisites**: [Metrics Fundamentals]({{< relref "/docs/observability/concepts/metrics-fundamentals" >}})
> **What You'll Learn**: Write basic PromQL queries and retrieve desired metrics

## TL;DR

{{< callout type="info" >}}
**Key Summary:**
- **Instant Vector**: Current point-in-time value `http_requests_total`
- **Range Vector**: Values over time range `http_requests_total[5m]`
- **Label Matching**: Filter using `=`, `!=`, `=~`, `!~`
- **Operations**: Arithmetic (`+`, `-`), Comparison (`>`, `<`), Logical (`and`, `or`)
{{< /callout >}}

## Why Do We Need PromQL?

Prometheus stores thousands of time series data points. But **having data doesn't automatically provide insights.** To answer questions like "Is the system healthy right now?", "How does it compare to yesterday?", or "Which service has problems?", you need **a language to query and transform data**. That's PromQL.

### Analogy: SQL and Databases

Even with data in a relational database, you can't find "customers over 30 who spent more than $1,000 this month" without SQL. Just as SQL is the query language for RDBMS, **PromQL is the query language for time series databases (Prometheus)**.

| SQL | PromQL |
|-----|--------|
| `SELECT * FROM users WHERE age > 30` | `http_requests_total{status="500"}` |
| `COUNT(*) GROUP BY status` | `sum by (status) (http_requests_total)` |
| `AVG(response_time)` | `avg(http_request_duration_seconds)` |
| Row-based data | Time series data |

While SQL handles "table rows", PromQL deals with **"metrics on a time axis"**.

### Problems PromQL Solves

| Question | PromQL Answer |
|----------|---------------|
| "What's the current error rate?" | `rate(http_requests_total{status="500"}[5m])` |
| "What's the P99 response time?" | `histogram_quantile(0.99, rate(http_request_duration_seconds_bucket[5m]))` |
| "How does traffic compare to yesterday?" | `rate(http_requests_total[5m]) / rate(http_requests_total[5m] offset 1d)` |
| "Which servers have CPU above 80%?" | `node_cpu_usage_percent > 80` |

Being able to answer these questions **in real-time** enables monitoring and alerting.

---

## Data Types

To understand PromQL, you first need to know **what form the data takes**. Every PromQL query returns one of three types:

### Instant Vector

**Definition**: A set of time series at a specific point in time.

**Why is it needed?**

It's used when you want to know "right now" system state. Most numbers displayed on dashboards are Instant Vectors.

**Analogy: Snapshot Photo**

A photo captures "that moment". An Instant Vector similarly returns all time series values at "query execution time". Each time series has **one value**.

```promql
# Current values of all http_requests_total time series
http_requests_total
```

**Result:**
```
http_requests_total{method="GET", status="200"} 1523
http_requests_total{method="GET", status="500"} 12
http_requests_total{method="POST", status="201"} 342
```

### Range Vector

**Definition**: Values within a time range. Specified with `[time]` format.

**Why is it needed?**

To know "how has it changed over the last 5 minutes?", you need **data from multiple points in time**. Functions like `rate()`, `increase()`, `avg_over_time()` take Range Vectors as input and calculate **trends or rates of change**.

**Analogy: Video**

While a photo is a moment, a video captures the flow of time. A Range Vector contains **multiple data points** like "frames from the last 5 minutes".

```promql
# Data points from the last 5 minutes
http_requests_total[5m]
```

**Result:**
```
http_requests_total{method="GET", status="200"}
  1500 @1704700500
  1510 @1704700515
  1523 @1704700530
```

### Time Units

| Unit | Meaning | Example |
|------|---------|---------|
| `s` | seconds | `[30s]` |
| `m` | minutes | `[5m]` |
| `h` | hours | `[1h]` |
| `d` | days | `[7d]` |
| `w` | weeks | `[2w]` |
| `y` | years | `[1y]` |

### Scalar

**Definition**: A single numeric value. Only **pure numbers** exist, without labels or time information.

**Why is it needed?**

Used as baseline values or thresholds for calculations. `80` in "if memory usage exceeds 80%" is a scalar. Also, results from functions like `count()`, `scalar()` are scalars.

```promql
# Numeric literal
100

# Aggregation result
count(up)
```

---

## Label Matching

Every Prometheus time series is identified by **metric name + label combination**. Even the same `http_requests_total`, `{method="GET", status="200"}` and `{method="POST", status="500"}` are **different time series**.

### Why Label Matching is Needed

Systems have hundreds or thousands of time series. Querying all of them results in a meaningless data pile. **Selecting only the needed time series** is the role of label matching.

**Analogy: Library Search**

Searching just "book" in a library returns all books. You need to **combine conditions** like "author is Hong Gil-dong, field is IT, published after 2020" to find the desired book. Label matching is like these "filter conditions".

```promql
# SQL analogy:
# SELECT * FROM http_requests_total WHERE method='GET' AND status LIKE '5%'

http_requests_total{method="GET", status=~"5.."}
```

### Basic Matchers

| Matcher | Meaning | Example | SQL Analogy |
|---------|---------|---------|-------------|
| `=` | Exact match | `{status="200"}` | `WHERE status = '200'` |
| `!=` | Not equal | `{status!="200"}` | `WHERE status != '200'` |
| `=~` | Regex match | `{status=~"2.."}` | `WHERE status LIKE '2__'` |
| `!~` | Regex not match | `{status!~"2.."}` | `WHERE status NOT LIKE '2__'` |

### Examples

```promql
# Only status 200
http_requests_total{status="200"}

# Not status 200
http_requests_total{status!="200"}

# Status 5xx (regex)
http_requests_total{status=~"5.."}

# Status 2xx or 3xx
http_requests_total{status=~"[23].."}

# Method GET or POST
http_requests_total{method=~"GET|POST"}
```

### Combining Multiple Conditions

```promql
# AND conditions (all must match)
http_requests_total{method="GET", status="200", path="/api"}

# OR conditions (use regex)
http_requests_total{status=~"200|201|204"}
```

{{< callout type="warning" >}}
**Caution**: Using metric name without label matchers returns **all time series** of that metric. This can cause performance issues with high cardinality metrics.
{{< /callout >}}

---

## Operators

Prometheus metrics are raw data. To **transform this data into meaningful indicators**, operations are needed.

### Why Operators are Needed

There are questions that raw metric values alone can't answer:

- "What's the memory usage percentage?" → used / total × 100 (arithmetic)
- "Which servers exceed 80% CPU?" → CPU > 80 (comparison)
- "Errors AND GET requests?" → condition AND condition (logical)

Operators **transform raw data into business-meaningful indicators**.

### Arithmetic Operators

**Purpose**: Unit conversion, ratio/percentage calculation, normalization

| Operator | Meaning | Use Case |
|----------|---------|----------|
| `+` | Addition | Sum calculation |
| `-` | Subtraction | Difference calculation |
| `*` | Multiplication | Percentage conversion |
| `/` | Division | Ratio calculation |
| `%` | Modulo | Cycle calculation |
| `^` | Power | Exponential calculation |

```promql
# Convert bytes to gigabytes
node_memory_MemTotal_bytes / 1024 / 1024 / 1024

# Calculate usage percentage
node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes * 100
```

**Analogy: Excel Formula**

Like calculating percentage with `=A1/B1*100` in Excel, you can calculate formulas between metrics in PromQL. However, PromQL automatically matches **time series with the same labels** for operations.

### Comparison Operators

**Purpose**: Threshold-based filtering, alert condition definition

| Operator | Meaning | Alert Example |
|----------|---------|---------------|
| `==` | Equal | Service down detection |
| `!=` | Not equal | Abnormal state detection |
| `>` | Greater than | Threshold exceeded |
| `<` | Less than | Resource shortage |
| `>=` | Greater or equal | SLA violation |
| `<=` | Less or equal | Normal range check |

**Key Concept**: Comparison operators **return only time series that satisfy the condition**. This is the basic principle of alerting.

```promql
# CPU usage over 80% → use as alert condition
node_cpu_seconds_total > 0.8

# Target down → up == 0 means "dead service"
up == 0

# bool modifier: return condition as 0/1 (returns true/false instead of filtering)
up == bool 1
```

### Logical Operators

**Purpose**: Complex condition combinations, set operations

Logical operators **combine time series from two vectors** similar to SQL's `INNER JOIN`, `UNION`, `EXCEPT`.

| Operator | Meaning | SQL Analogy |
|----------|---------|-------------|
| `and` | Intersection (exists in both) | `INNER JOIN` |
| `or` | Union (exists in either) | `UNION` |
| `unless` | Difference (only in left) | `LEFT JOIN ... WHERE B.id IS NULL` |

```promql
# Both conditions satisfied (time series that are errors AND GET requests)
http_requests_total{status="500"} and http_requests_total{method="GET"}

# Either condition satisfied (app or api service)
up{job="app"} or up{job="api"}

# In A but not in B (all non-200 requests)
http_requests_total unless http_requests_total{status="200"}
```

---

## Vector Matching

When operating between two vectors, PromQL operates on **time series with exactly matching labels**. However, in real environments, labels often don't match perfectly.

### Why Vector Matching is Needed

For example, to calculate "error rate = errors / total requests":

```promql
http_errors_total / http_requests_total  # No result if labels differ!
```

`http_errors_total{method="GET", instance="server1"}` and `http_requests_total{method="GET", instance="server1", path="/"}` have different labels, so they won't match. Vector matching **explicitly specifies which labels to use for matching**.

**Analogy: Excel VLOOKUP**

When combining data from two sheets in Excel, you specify "which column to match on". `on()` in vector matching means "match using only these labels".

### on / ignoring

```promql
# on(): match based on specific labels only
# "Match if method label is the same"
method_total / on(method) method_errors

# ignoring(): match excluding specific labels
# "Match if other labels are the same, ignoring instance label"
method_total / ignoring(instance) method_errors
```

### group_left / group_right

Used for many-to-one or one-to-many matching.

**When is it needed?**

Used to attach metadata metrics (e.g., version info) to other metrics. A metric like `app_info{job="api", version="2.0"}` has one per job, but `http_requests_total` has multiple time series per job.

```promql
# When left side (http_requests_total) has more time series
# Bring version label from app_info and attach it
http_requests_total
* on(job) group_left(version)
app_info
```

**Result**: The version label is added like `http_requests_total{job="api", method="GET", version="2.0"}`.

---

## Offset Modifier

### Why Offset is Needed

One of the key monitoring questions is **"How does it compare to the past?"**

- "Is current traffic higher than yesterday at this time?"
- "How did response time change before and after deployment?"
- "How does it compare to the same day last week?"

Offset queries data from **past points in time** instead of the current time.

**Analogy: Time Machine**

Offset is a time machine command: "Go back 1 hour from now and look at data from that time".

```promql
# Value from 1 hour ago
http_requests_total offset 1h

# 5-minute range from 1 hour ago (past time rate)
rate(http_requests_total[5m] offset 1h)

# Compare with yesterday at same time (current - yesterday)
rate(http_requests_total[5m])
- rate(http_requests_total[5m] offset 1d)
```

### Practical Use: Weekly Comparison

```promql
# Change rate compared to same day last week
(rate(http_requests_total[5m]) - rate(http_requests_total[5m] offset 7d))
/ rate(http_requests_total[5m] offset 7d) * 100
```

---

## @ Modifier

### Why @ Modifier is Needed

Offset specifies "relative time from now", but sometimes **an exact specific point in time** is needed.

- "What was the state at 2026-01-10 14:30:00 when the incident occurred?"
- "What were the metrics just before deployment?"

The @ modifier specifies **absolute time** using Unix timestamp.

```promql
# Specific point in time (Unix timestamp: 2026-01-10 10:00:00 UTC)
http_requests_total @ 1736503200

# Query start time (useful in Range Queries)
http_requests_total @ start()

# Query end time
http_requests_total @ end()
```

{{< callout type="info" >}}
**Offset vs @**: Offset is "relative time from now", @ is "absolute time". Offset is more intuitive in most cases, but @ is useful when analyzing specific incident times.
{{< /callout >}}

---

## Practical Examples

### Basic Queries

```promql
# All HTTP request counts
http_requests_total

# Only GET requests
http_requests_total{method="GET"}

# Only 5xx errors
http_requests_total{status=~"5.."}

# Last 5 minutes of specific service
http_requests_total{service="order-service"}[5m]
```

### Filtering

```promql
# Endpoints with 1000+ requests
http_requests_total > 1000

# Production environment only
http_requests_total{env="production"}

# Exclude specific paths
http_requests_total{path!~"/health|/metrics"}
```

### Time Comparison

```promql
# Current value
http_requests_total

# Value from 1 hour ago
http_requests_total offset 1h

# Value from 1 day ago
http_requests_total offset 1d
```

---

## Common Mistakes

### 1. Using Range Vector Directly in Graph

```promql
# ❌ Range Vector cannot be displayed directly in graph
http_requests_total[5m]

# ✅ Convert to Instant Vector with function
rate(http_requests_total[5m])
```

### 2. Direct Counter Comparison

```promql
# ❌ Counter is cumulative, meaningless
http_requests_total > 1000

# ✅ Calculate rate first, then compare
rate(http_requests_total[5m]) > 10
```

### 3. Missing Regex Anchor

```promql
# ❌ Matches anything containing "200" (e.g., "2001", "1200")
{status=~"200"}

# ✅ Exactly "200" only
{status=~"^200$"}
# Or just use =
{status="200"}
```

---

## Key Takeaways

| Concept | Syntax | Example |
|---------|--------|---------|
| **Instant Vector** | metric name | `up` |
| **Range Vector** | `[time]` | `up[5m]` |
| **Label Matching** | `{label="value"}` | `{job="api"}` |
| **Regex** | `=~` | `{status=~"5.."}` |
| **Offset** | `offset time` | `up offset 1h` |

---

## Next Steps

| Recommended Order | Document | What You'll Learn |
|-------------------|----------|-------------------|
| 1 | [Aggregation Operators](aggregation-operators/) | sum, avg, topk, by/without |
| 2 | [rate and increase](rate-and-increase/) | How to handle Counters |
