---
lastmod: "2026-01-16"
title: Memory Troubleshooting
description: "How to diagnose and resolve Elasticsearch memory issues"
weight: 2
---

Learn how to diagnose and resolve OutOfMemoryError and GC issues.

**Duration**: Approximately 20-40 minutes (additional 10 minutes for GC log analysis)

{{< callout type="info" title="Scope of This Guide" >}}
**Covered**: Heap memory settings, Circuit Breaker, Field Data optimization, GC tuning

**Not Covered**: Adding nodes, hardware upgrades - see [Cluster Management]({{< relref "/docs/elasticsearch/concepts/cluster-management" >}})
{{< /callout >}}

{{< callout type="tip" title="TL;DR" >}}
- **Heap memory**: 50% or less of total memory, maximum 31GB
- **Circuit Breaker**: Check settings to prevent memory overuse
- **Field data**: Avoid aggregations on text fields, use doc_values
- **GC tuning**: Use G1GC, analyze logs to identify issues
{{< /callout >}}

---

## Before You Begin

Verify the following requirements:

| Item | Requirement | How to Check |
|------|-------------|--------------|
| Server access | SSH or console access | Able to log in to server |
| jvm.options edit permission | root or elasticsearch user | Check paths below |
| ES restart permission | Able to restart service | `systemctl restart elasticsearch` |

**jvm.options file locations**:

| Installation Method | Path |
|---------------------|------|
| Debian/Ubuntu (apt) | `/etc/elasticsearch/jvm.options` |
| RPM/CentOS (yum) | `/etc/elasticsearch/jvm.options` |
| tar.gz extraction | `{ES_HOME}/config/jvm.options` |
| Docker | Use environment variable `ES_JAVA_OPTS` |

```bash
# Find jvm.options file location
ls -la /etc/elasticsearch/jvm.options 2>/dev/null || \
ls -la $ES_HOME/config/jvm.options 2>/dev/null || \
echo "Cannot find jvm.options file"
```

{{< callout type="warning" title="Note" >}}
Elasticsearch must be restarted after changing jvm.options. Rolling restart is recommended for production environments.
{{< /callout >}}

---

## Symptoms

The following issues occur:

**OutOfMemoryError:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Circuit Breaker triggered:**
```json
{
  "error": {
    "type": "circuit_breaking_exception",
    "reason": "[parent] Data too large, data for [<query>] would be larger than limit of [xxx/yyy]"
  }
}
```

**GC overhead:**
```
GC overhead limit exceeded
```

---

## Step 1: Check Current Memory Status

### 1.1 Memory Usage by Node

```bash
# Heap memory status by node
curl -X GET "localhost:9200/_cat/nodes?v&h=name,heap.percent,heap.current,heap.max"

# Example output:
# name    heap.percent heap.current heap.max
# node-1  75           11.2gb       16gb
```

### 1.2 Detailed Memory Analysis

```bash
# Full node statistics
curl -X GET "localhost:9200/_nodes/stats/jvm?pretty"

# Circuit Breaker status
curl -X GET "localhost:9200/_nodes/stats/breaker?pretty"
```

**Key points to check:**
- `heap.percent` > 85%: Danger level
- High `fielddata` usage indicates aggregation query issues
- Frequent `request` breaker trips indicate need for query optimization

---

## Step 2: Optimize Heap Memory Settings

### 2.1 Appropriate Heap Size

```bash
# Edit jvm.options file
# Location: /etc/elasticsearch/jvm.options or config/jvm.options

# Recommended settings (example for 16GB system)
-Xms8g
-Xmx8g
```

**Heap memory guidelines:**

| System Memory | Recommended Heap | Remaining Memory For |
|---------------|------------------|---------------------|
| 8GB | 4GB | OS cache, Lucene |
| 16GB | 8GB | OS cache, Lucene |
| 32GB | 16GB | OS cache, Lucene |
| 64GB | 31GB | OS cache, Lucene |

{{< callout type="warning" title="Warning" >}}
Setting heap above 32GB disables Compressed OOPs, which actually degrades performance. Maximum recommended is 31GB.
{{< /callout >}}

### 2.2 Set Xms and Xmx Equal

```bash
# Incorrect: Variable heap size increases GC burden
-Xms4g
-Xmx16g

# Correct: Fixed size
-Xms8g
-Xmx8g
```

---

## Step 3: Adjust Circuit Breaker

### 3.1 Check Breaker Settings

```bash
curl -X GET "localhost:9200/_cluster/settings?include_defaults=true&filter_path=**.breaker"
```

### 3.2 Adjust Breaker Limits

```bash
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "persistent": {
    "indices.breaker.total.limit": "70%",
    "indices.breaker.fielddata.limit": "40%",
    "indices.breaker.request.limit": "40%"
  }
}'
```

| Breaker | Default | Role |
|---------|---------|------|
| `total` | 70% | Total memory limit |
| `fielddata` | 40% | Field data cache |
| `request` | 60% | Single request memory |
| `in_flight_requests` | 100% | Requests in transit |

---

## Step 4: Optimize Field Data

### 4.1 Problem Cause

Aggregations or sorting on text fields load fielddata into memory:

```json
// Dangerous: Aggregating on text field
{
  "aggs": {
    "categories": {
      "terms": { "field": "category" }  // Problem if category is text
    }
  }
}
```

### 4.2 Solutions

**Method 1: Use keyword field**

```json
// Mapping configuration
{
  "mappings": {
    "properties": {
      "category": {
        "type": "text",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      }
    }
  }
}

// Use keyword for aggregation
{
  "aggs": {
    "categories": {
      "terms": { "field": "category.keyword" }
    }
  }
}
```

**Method 2: Use doc_values**

```json
// Enable doc_values in mapping (enabled by default for keyword)
{
  "mappings": {
    "properties": {
      "status": {
        "type": "keyword",
        "doc_values": true
      }
    }
  }
}
```

### 4.3 Limit Field Data Cache

```bash
curl -X PUT "localhost:9200/_cluster/settings" -H 'Content-Type: application/json' -d'
{
  "persistent": {
    "indices.fielddata.cache.size": "20%"
  }
}'
```

---

## Step 5: GC Optimization

### 5.1 G1GC Settings (Recommended)

```bash
# jvm.options
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1ReservePercent=25
-XX:InitiatingHeapOccupancyPercent=30
```

### 5.2 GC Log Analysis

```bash
# Check GC log location
ls /var/log/elasticsearch/gc.log*

# Enable GC logging (jvm.options)
-Xlog:gc*,gc+age=trace,safepoint:file=/var/log/elasticsearch/gc.log:utctime,pid,tags:filecount=32,filesize=64m
```

**Analysis tools:**
- GCViewer: `java -jar gcviewer.jar gc.log`
- GCEasy: https://gceasy.io (online)

### 5.3 GC Problem Patterns

| Symptom | Cause | Solution |
|---------|-------|----------|
| Frequent Young GC | High object creation | Query optimization, adjust batch size |
| Long Full GC | Heap shortage | Increase heap or clean up data |
| Memory shortage after GC | Memory leak | Analyze heap dump |

---

## Step 6: Query-Level Optimization

### 6.1 Avoid Large Result Sets

```json
// Dangerous: Too many results
{ "size": 10000 }

// Safe: Use pagination
{ "size": 100, "from": 0 }

// For bulk data: Use Scroll API
curl -X POST "localhost:9200/products/_search?scroll=1m" -H 'Content-Type: application/json' -d'
{
  "size": 1000,
  "query": { "match_all": {} }
}'
```

### 6.2 Optimize Aggregations

```json
// Dangerous: High cardinality aggregation
{
  "aggs": {
    "all_users": {
      "terms": { "field": "user_id", "size": 1000000 }
    }
  }
}

// Safe: Appropriate size limit
{
  "aggs": {
    "top_users": {
      "terms": { "field": "user_id", "size": 100 }
    }
  }
}
```

---

## Checklist

Items to check when troubleshooting memory issues:

- [ ] **Is heap size appropriate?** - 50% of system memory, maximum 31GB
- [ ] **Are Xms and Xmx equal?** - Prevent heap size fluctuation
- [ ] **Not aggregating on text fields?** - Use keyword or doc_values
- [ ] **Circuit Breaker appropriate?** - Too high causes OOM, too low causes query failures
- [ ] **Analyzed GC logs?** - Identify patterns
- [ ] **Any unnecessary indices?** - Clean up old indices

---

## Verify Success

Confirm memory issues are resolved with these methods:

1. **Check heap usage**: Verify `heap.percent` stays stable below 75%
   ```bash
   # Monitor heap usage (10 times at 5-second intervals)
   for i in {1..10}; do
     curl -s "localhost:9200/_cat/nodes?v&h=name,heap.percent" && sleep 5
   done
   ```

2. **Check Circuit Breaker**: Verify breaker no longer trips
   ```bash
   curl -X GET "localhost:9200/_nodes/stats/breaker?pretty" | grep tripped
   ```

3. **Check OOM logs**: Verify no new OutOfMemoryError occurs
   ```bash
   # Search for OOM in recent logs
   grep -i "OutOfMemory" /var/log/elasticsearch/*.log | tail -5
   ```

{{< callout type="tip" title="Success Criteria" >}}
- `heap.percent` stable below 75%
- Circuit Breaker `tripped` count not increasing
- No OOM for 24 hours
{{< /callout >}}

---

## Common Errors

### jvm.options Syntax Error

**Symptom**: Elasticsearch won't start

```
Error: Could not create the Java Virtual Machine.
Error: A fatal exception has occurred. Program will exit.
```

**Cause**: Invalid option in jvm.options file

**Solution**:
1. Check jvm.options file syntax
2. Ensure each option is on a new line
3. Check for spaces or special characters

```bash
# Correct format
-Xms8g
-Xmx8g

# Incorrect format (contains spaces)
-Xms 8g
-Xmx=8g
```

### Elasticsearch Startup Failure (Insufficient Memory)

**Symptom**: Service won't start

```
[ERROR] bootstrap checks failed
max virtual memory areas vm.max_map_count [65530] is too low
```

**Solution**:
```bash
# Temporary setting
sudo sysctl -w vm.max_map_count=262144

# Permanent setting
echo "vm.max_map_count=262144" | sudo tee -a /etc/sysctl.conf
```

### Memory Limits in Docker Environment

**Symptom**: Container terminates due to OOM

**Solution**: Set both memory limit and ES_JAVA_OPTS when running Docker:
```bash
docker run -d \
  --memory="4g" \
  -e ES_JAVA_OPTS="-Xms2g -Xmx2g" \
  elasticsearch:8.x
```

---

## Related Documentation

- [Cluster Management]({{< relref "/docs/elasticsearch/concepts/cluster-management" >}}) - Node configuration and monitoring
- [Performance Tuning]({{< relref "/docs/elasticsearch/concepts/performance-tuning" >}}) - Overall performance optimization
- [Slow Query Optimization]({{< relref "/docs/elasticsearch/howto/slow-query-optimization" >}}) - Query-level optimization
