---
lastmod: "2026-01-16"
title: Slow Query Optimization
description: "How to diagnose and optimize slow queries"
weight: 1
---

Learn how to diagnose and improve slow search response times.

**Duration**: Approximately 15-30 minutes

{{< callout type="info" title="Scope of This Guide" >}}
**Covered**: Query-level performance optimization, Profile API analysis, cache utilization

**Not Covered**: Cluster-level tuning (adding nodes, hardware upgrades) - see [Performance Tuning]({{< relref "/docs/elasticsearch/concepts/performance-tuning" >}})
{{< /callout >}}

{{< callout type="tip" title="TL;DR" >}}
- Analyze query execution plans with **Profile API**
- **Remove unnecessary fields**: `_source` filtering, stored fields
- **Optimize query types**: Use `term` instead of `match`, avoid leading wildcards
- **Leverage caching**: Use filter context, enable request cache
{{< /callout >}}

---

## Before You Begin

Verify the following requirements:

| Item | Requirement | How to Check |
|------|-------------|--------------|
| Elasticsearch version | 7.x or higher | `curl -X GET "localhost:9200"` |
| Cluster status | green or yellow | `curl -X GET "localhost:9200/_cluster/health"` |
| Index permissions | Read + settings change | Test with command below |

```bash
# Check Elasticsearch status
curl -X GET "localhost:9200/_cluster/health?pretty"

# Expected response: "status": "green" or "yellow"
```

{{< callout type="warning" title="Note" >}}
Changing Slow Log settings requires index settings modification permissions. Contact your administrator if you don't have access.
{{< /callout >}}

---

## Symptoms

Search requests are slower than expected:

```bash
# Check the took value in the response
curl -X GET "localhost:9200/products/_search" -H 'Content-Type: application/json' -d'
{
  "query": { "match_all": {} }
}'

# Response: "took": 2500  <- 2.5 seconds, too slow
```

---

## Step 1: Measure Current State

### 1.1 Analyze Query with Profile API

Add `profile: true` to your query to see the execution plan:

```bash
curl -X GET "localhost:9200/products/_search" -H 'Content-Type: application/json' -d'
{
  "profile": true,
  "query": {
    "match": { "name": "samsung galaxy" }
  }
}'
```

**What to look for in the response:**
```json
{
  "profile": {
    "shards": [{
      "searches": [{
        "query": [{
          "type": "BooleanQuery",
          "time_in_nanos": 1234567,  // High value indicates a problem
          "breakdown": {
            "score": 100000,
            "build_scorer": 50000,
            "create_weight": 10000
          }
        }]
      }]
    }]
  }
}
```

### 1.2 Enable Slow Log

Configure automatic logging of slow queries:

```bash
curl -X PUT "localhost:9200/products/_settings" -H 'Content-Type: application/json' -d'
{
  "index.search.slowlog.threshold.query.warn": "1s",
  "index.search.slowlog.threshold.query.info": "500ms",
  "index.search.slowlog.threshold.fetch.warn": "500ms"
}'
```

Log location: `logs/{cluster-name}_index_search_slowlog.log`

---

## Step 2: Query Optimization

### 2.1 Use Appropriate Query Types

| Scenario | Slow Query | Fast Query |
|----------|------------|------------|
| Exact value search | `match` | `term` |
| One of multiple values | Multiple `should` | `terms` |
| Range search | `script` | `range` |
| Existence check | `script` | `exists` |

```json
// Slow: match goes through analyzer
{ "match": { "status": "active" } }

// Fast: term searches for exact value
{ "term": { "status": "active" } }
```

### 2.2 Optimize Wildcards

```json
// Very slow: Leading wildcard (full scan)
{ "wildcard": { "name": "*phone" } }

// Fast: Trailing wildcard only
{ "wildcard": { "name": "phone*" } }

// Faster: Use prefix query
{ "prefix": { "name": "phone" } }
```

### 2.3 Avoid Script Queries

```json
// Slow: Script runs on every document
{
  "script": {
    "script": "doc['price'].value * doc['quantity'].value > 1000"
  }
}

// Fast: Add computed field at indexing time
// After adding total_value field to mapping
{ "range": { "total_value": { "gt": 1000 } } }
```

---

## Step 3: Use Filter Context

### 3.1 filter vs query

`filter` skips scoring and is cached:

```json
// Slow: Scoring on all conditions
{
  "query": {
    "bool": {
      "must": [
        { "match": { "name": "galaxy" } },
        { "range": { "price": { "gte": 100000 } } },
        { "term": { "category": "phone" } }
      ]
    }
  }
}

// Fast: Separate filtering conditions to filter
{
  "query": {
    "bool": {
      "must": [
        { "match": { "name": "galaxy" } }
      ],
      "filter": [
        { "range": { "price": { "gte": 100000 } } },
        { "term": { "category": "phone" } }
      ]
    }
  }
}
```

### 3.2 Optimize Filter Order

Place filters that exclude the most documents first:

```json
{
  "query": {
    "bool": {
      "filter": [
        { "term": { "is_deleted": false } },  // Filters most
        { "term": { "status": "active" } },   // Filters next
        { "range": { "price": { "gte": 100000 } } }  // Last
      ]
    }
  }
}
```

---

## Step 4: Optimize Result Size

### 4.1 _source Filtering

Return only required fields:

```json
// Slow: Returns entire document
{ "query": { "match_all": {} } }

// Fast: Only necessary fields
{
  "_source": ["name", "price"],
  "query": { "match_all": {} }
}

// Faster: Disable _source
{
  "_source": false,
  "fields": ["name", "price"],
  "query": { "match_all": {} }
}
```

### 4.2 Pagination Optimization

```json
// Slow: Deep pagination
{ "from": 10000, "size": 10 }  // Reads and discards 10,000

// Fast: Use search_after
{
  "size": 10,
  "sort": [{ "created_at": "desc" }, { "_id": "asc" }],
  "search_after": ["2024-01-01T00:00:00", "abc123"]
}
```

---

## Step 5: Index-Level Optimization

### 5.1 Adjust Shard Count

Too many shards per index increases overhead:

```bash
# Check current shard count
curl -X GET "localhost:9200/_cat/shards/products?v"

# Recommendation: 20-40GB per shard
# 10GB of data needs only 1 shard
```

### 5.2 Adjust Refresh Interval

{{< callout type="warning" title="Warning" >}}
Setting refresh_interval to `-1` means newly indexed documents won't be searchable. Always restore the original value after completing your task.
{{< /callout >}}

Reduce refresh during heavy indexing:

```bash
# Disable refresh during batch indexing
curl -X PUT "localhost:9200/products/_settings" -H 'Content-Type: application/json' -d'
{ "refresh_interval": "-1" }'

# Restore after completion
curl -X PUT "localhost:9200/products/_settings" -H 'Content-Type: application/json' -d'
{ "refresh_interval": "1s" }'

# Manual refresh
curl -X POST "localhost:9200/products/_refresh"
```

---

## Checklist

Items to check when optimizing slow queries:

- [ ] **Analyzed with Profile API?** - Identify bottlenecks
- [ ] **Using appropriate query types?** - term vs match, wildcard position
- [ ] **Using filter context?** - Separate conditions that don't need scoring
- [ ] **Applied _source filtering?** - Return only needed fields
- [ ] **Pagination appropriate?** - Use search_after for deep pages
- [ ] **Leveraging cache?** - Check filter cache, request cache

---

## Verify Success

Confirm optimization success with these methods:

1. **Compare took values**: Check if `took` value decreased by 50% or more for the same query
   ```bash
   # Compare took values before and after optimization
   curl -X GET "localhost:9200/products/_search?pretty" -H 'Content-Type: application/json' -d'
   {
     "query": { "match": { "name": "test" } }
   }' | grep took
   ```

2. **Compare Profile API**: Check if `time_in_nanos` decreased

3. **Check Slow Log**: Verify no more queries exceed the threshold (e.g., 500ms)

{{< callout type="tip" title="Success Criteria" >}}
- took value decreased by 50% or more
- No new warnings in Slow Log
- Improved perceived response time for users
{{< /callout >}}

---

## Common Errors

### "index_not_found_exception"

```json
{
  "error": {
    "type": "index_not_found_exception",
    "reason": "no such index [products]"
  }
}
```

**Cause**: Index name is incorrect or doesn't exist

**Solution**: Check the index list:
```bash
curl -X GET "localhost:9200/_cat/indices?v"
```

### "search_phase_execution_exception"

```json
{
  "error": {
    "type": "search_phase_execution_exception",
    "reason": "all shards failed"
  }
}
```

**Cause**: Query syntax error or query doesn't match mapping

**Solution**:
1. Validate query JSON syntax
2. Check index mapping: `curl -X GET "localhost:9200/products/_mapping?pretty"`

### Permission Error (403 Forbidden)

**Cause**: Insufficient permissions for index read or settings change

**Solution**: Request the following permissions from your cluster administrator:
- `read` - Execute searches
- `manage` - Change Slow Log settings

---

## Related Documentation

- [Query DSL]({{< relref "/docs/elasticsearch/concepts/query-dsl" >}}) - Query type characteristics
- [Performance Tuning]({{< relref "/docs/elasticsearch/concepts/performance-tuning" >}}) - Cluster-level tuning
- [Memory Troubleshooting]({{< relref "/docs/elasticsearch/howto/memory-troubleshooting" >}}) - Handling OOM issues
