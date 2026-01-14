---
title: FAQ
weight: 2
lastmod: 2026-01-10
---

{{% notice style="tip" title="TL;DR" %}}
- **ES vs RDB**: Use ES for full-text search/log analysis, RDB for transactions/JOINs
- **text vs keyword**: text is for search (analyzed), keyword is for filter/sort (not analyzed)
- **Performance issues**: Use Filter Context, return only needed fields, `search_after` pagination
- **Korean search**: Nori analyzer installation required
- **Common errors**: Most caused by disk shortage, memory shortage, or type mismatch
{{% /notice %}}

Frequently asked questions and answers.

## Basic Concepts

### Elasticsearch vs RDB, when should I use what?

| Situation | Recommendation |
|-----------|----------------|
| Full-text search | Elasticsearch |
| Transaction integrity needed | RDB |
| Complex JOINs | RDB |
| Real-time aggregation/analysis | Elasticsearch |
| General CRUD | RDB |
| High-volume log storage | Elasticsearch |

**Common pattern:** Use RDB as main store, Elasticsearch as search secondary store.

### What's the difference between text and keyword types?

| Property | text | keyword |
|----------|------|---------|
| Analysis | Yes (tokenized) | No |
| Search method | match query | term query |
| Sort/Aggregation | Not possible | Possible |
| Use case | Full-text search | Exact value matching, filters |

```json
// text: "Samsung Electronics" → ["samsung", "electronics"]
// keyword: "Samsung Electronics" → "Samsung Electronics"
```

### How do I determine the number of shards?

- **Rule of Thumb:** 20-40GB per shard
- Too few: Reduced parallel processing efficiency
- Too many: Increased overhead, memory shortage

```
# Example: 100GB data
Recommended Primary shards: 3-5
```

---

## Performance

### Search is slow, how do I optimize?

1. **Use Filter Context**: Put conditions not needing Score in `filter`
2. **Return only needed fields**: Specify `_source`
3. **Pagination optimization**: Use `search_after` for deep pages
4. **Use cache**: Frequently used filters are auto-cached
5. **Review index design**: Appropriate shard count, exclude unnecessary fields

### What's the impact of increasing Refresh Interval?

| Value | Impact |
|-------|--------|
| Short (1s) | Real-time search, increased indexing load |
| Long (30s) | Better indexing performance, search delay |
| -1 | Manual Refresh only, useful for bulk indexing |

### How should I set JVM Heap?

- 50% of system memory (max 30-31GB)
- Set minimum (-Xms) and maximum (-Xmx) to same value
- Leave the rest for file system cache

---

## Operations

### Cluster status is Yellow, is that okay?

**Development environment (single node):** Normal. Yellow because there's no other node to assign Replicas.

**Production:** Need to add nodes or reduce Replica count.

```json
// Set Replica to 0 (for development)
PUT /products/_settings
{ "number_of_replicas": 0 }
```

### What happens when disk space runs out?

| Usage | Behavior |
|-------|----------|
| 85% | Stop new shard allocation |
| 90% | Complete stop of shard allocation to that node |
| 95% | Index converted to read-only |

**Response:** Delete old data, add nodes, configure ILM

### I want to change Index Mapping

Existing field types cannot be changed. **Reindexing** required:

```json
// 1. Create new index (new Mapping)
PUT /products-v2

// 2. Copy data
POST /_reindex
{
  "source": { "index": "products-v1" },
  "dest": { "index": "products-v2" }
}

// 3. Switch Alias
POST /_aliases
{
  "actions": [
    { "remove": { "index": "products-v1", "alias": "products" } },
    { "add": { "index": "products-v2", "alias": "products" } }
  ]
}
```

---

## Search

### Korean search isn't working well

Default `standard` analyzer doesn't do Korean morphological analysis.
Install and configure the **Nori analyzer**.

```json
PUT /products
{
  "settings": {
    "analysis": {
      "analyzer": {
        "korean": {
          "type": "custom",
          "tokenizer": "nori_tokenizer"
        }
      }
    }
  }
}
```

### How do I allow typos in search?

Use **Fuzzy search**:

```json
{
  "query": {
    "match": {
      "name": {
        "query": "Macbok",
        "fuzziness": "AUTO"
      }
    }
  }
}
```

### How do I implement autocomplete?

1. **Edge N-gram**: Prefix matching (recommended)
2. **Completion Suggester**: Dedicated data structure (fast)
3. **match_phrase_prefix**: Simple but watch performance

---

## Errors and Troubleshooting

### 1. "index read-only / allow delete" error

**Symptoms:**
```
ClusterBlockException: index [products] blocked by: [FORBIDDEN/12/index read-only / allow delete (api)]
```

**Cause:** Index automatically converted to read-only when disk usage exceeds 95%

**Solution:**
```json
// 1. After freeing disk space
// 2. Release read-only
PUT /products/_settings
{
  "index.blocks.read_only_allow_delete": null
}

// Release all indices at once
PUT /_all/_settings
{
  "index.blocks.read_only_allow_delete": null
}
```

**Prevention:** Set alerts at 80% disk usage

### 2. "Result window is too large" error

**Symptoms:**
```
IllegalArgumentException: Result window is too large, from + size must be less than or equal to: [10000]
```

**Cause:** By default, `from + size` total cannot exceed 10,000

**Solution:**
```json
// Method 1: Use search_after (recommended)
GET /products/_search
{
  "size": 100,
  "sort": [{ "created_at": "desc" }, { "_id": "asc" }],
  "search_after": ["2024-01-15T10:00:00", "abc123"]
}

// Method 2: Scroll API (for bulk export)
POST /products/_search?scroll=1m
{ "size": 1000, "query": { "match_all": {} } }

// Method 3: Relax limit (not recommended - memory burden)
PUT /products/_settings
{ "index.max_result_window": 50000 }
```

### 3. "mapper_parsing_exception" error

**Symptoms:**
```
MapperParsingException: failed to parse field [price] of type [integer]
```

**Cause:** Data that doesn't match field type

**Solution:**
```json
// Wrong examples
{ "price": "one thousand" }     // String in integer field
{ "price": 1000.5 }     // Decimal in integer field

// Correct example
{ "price": 1000 }
```

**Prevention:** Use `dynamic: strict` setting to prevent unexpected fields

### 4. "circuit_breaking_exception" error

**Symptoms:**
```
CircuitBreakingException: [parent] Data too large, data for [<http_request>] would be [xxx/xxxgb]
```

**Cause:** Query trying to use too much memory

**Solution:**
```json
// 1. Optimize query (reduce aggregation size)
{
  "aggs": {
    "categories": {
      "terms": {
        "field": "category",
        "size": 100  // Reduce from 10000 → 100
      }
    }
  }
}

// 2. Increase Heap memory (jvm.options)
-Xms4g
-Xmx4g
```

### 5. "rejected execution" error

**Symptoms:**
```
EsRejectedExecutionException: rejected execution of search on EsThreadPoolExecutor
```

**Cause:** Search/indexing requests exceeded thread pool queue capacity

**Solution:**
```json
// 1. Add nodes to distribute load
// 2. Implement client-side retry logic
// 3. Adjust Bulk request size

// Check thread pool status
GET /_cat/thread_pool?v&h=node_name,name,active,queue,rejected
```

---

### 6. "ClusterBlockException: no master" error

**Symptoms:**
```
MasterNotDiscoveredException: null
ClusterBlockException: blocked by: [SERVICE_UNAVAILABLE/2/no master]
```

**Cause:** Master node election failure (network partition, node failure)

**Solution:**
```bash
# 1. Check node status
GET /_cat/nodes?v

# 2. Check cluster status
GET /_cluster/health

# 3. Verify master eligible nodes are majority
# Recommended: 3 or more Master eligible nodes
```

---

### 7. "shard failed" / "all shards failed" error

**Symptoms:**
```
SearchPhaseExecutionException: all shards failed
```

**Cause:** All search target shards failed (node down, shard corruption)

**Solution:**
```json
// 1. Check cluster status
GET /_cluster/health?level=shards

// 2. Check unassigned shard reasons
GET /_cluster/allocation/explain

// 3. Check specific index shard status
GET /_cat/shards/products?v&h=index,shard,prirep,state,node,unassigned.reason
```

---

### 8. "version conflict" error

**Symptoms:**
```
VersionConflictEngineException: [1]: version conflict, current version [5] is different than the one provided [4]
```

**Cause:** Optimistic Locking conflict - concurrent modification attempts

**Solution:**
```json
// Method 1: Use retry_on_conflict
POST /products/_update/1?retry_on_conflict=3
{
  "doc": { "price": 2000000 }
}

// Method 2: Use if_seq_no instead of explicit version
PUT /products/_doc/1?if_seq_no=10&if_primary_term=1
{
  "name": "MacBook Pro",
  "price": 2000000
}
```

---

### 9. "connection refused" / "connection timeout" error

**Symptoms:**
```
ConnectException: Connection refused: localhost:9200
java.net.SocketTimeoutException: connect timed out
```

**Cause:** Elasticsearch not running / network issues

**Solution:**
```bash
# 1. Check ES process
ps aux | grep elasticsearch
docker ps | grep elasticsearch

# 2. Check port listening
lsof -i :9200
netstat -tlnp | grep 9200

# 3. Check firewall
sudo iptables -L -n | grep 9200

# 4. Check ES logs
tail -f /var/log/elasticsearch/elasticsearch.log
docker logs elasticsearch
```

---

### 10. "OOM (OutOfMemoryError)" error

**Symptoms:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Cause:** JVM Heap memory shortage

**Solution:**
```bash
# 1. Increase Heap size (jvm.options)
-Xms8g
-Xmx8g

# Note: Keep below 50% of system memory, max 30-31GB

# 2. Check memory usage
GET /_nodes/stats/jvm

# 3. Check fielddata cache (issues when aggregating text fields)
GET /_nodes/stats/indices/fielddata

# 4. Limit fielddata cache
PUT /_cluster/settings
{
  "persistent": {
    "indices.fielddata.cache.size": "20%"
  }
}
```

---

### 11. "no such index" error

**Symptoms:**
```
IndexNotFoundException: no such index [products]
```

**Cause:** Accessing non-existent index

**Solution:**
```json
// 1. Check index existence
HEAD /products

// 2. Check index list
GET /_cat/indices?v

// 3. Check Alias (when accessing via Alias)
GET /_cat/aliases?v
```

---

### 12. "illegal_argument_exception: Text fields are not optimised for operations" error

**Symptoms:**
```
IllegalArgumentException: Text fields are not optimised for operations that require per-document field data like aggregations and sorting
```

**Cause:** Attempting to sort/aggregate on text type field

**Solution:**
```json
// Method 1: Use keyword subfield
GET /products/_search
{
  "sort": [{ "name.keyword": "asc" }],
  "aggs": {
    "names": { "terms": { "field": "name.keyword" } }
  }
}

// Method 2: Define as keyword type from the start
{
  "mappings": {
    "properties": {
      "category": { "type": "keyword" }
    }
  }
}
```

---

## Security

### Can I run production without security settings?

**Absolutely not!** Security is enabled by default since Elasticsearch 8.x.

Minimum configuration:
1. TLS/SSL encryption
2. User authentication
3. Role-based access control

### Can external access directly reach port 9200?

**No.**

- Place behind API Gateway or proxy
- Allow only internal network via firewall
- Authentication required
