---
title: FAQ
weight: 2
lastmod: 2026-01-08
---

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

**Cause:** Index automatically converted to read-only when disk usage exceeds 95%

**Solution:**
```json
// After freeing disk space
PUT /products/_settings
{
  "index.blocks.read_only_allow_delete": null
}
```

### 2. "Result window is too large" error

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
```

### 3. "mapper_parsing_exception" error

**Cause:** Data that doesn't match field type

**Solution:**
```json
// Wrong
{ "price": "one thousand" }     // String in integer field

// Correct
{ "price": 1000 }
```

### 4. "circuit_breaking_exception" error

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
```

### 5. "rejected execution" error

**Cause:** Search/indexing requests exceeded thread pool queue capacity

**Solution:**
```json
// Check thread pool status
GET /_cat/thread_pool?v&h=node_name,name,active,queue,rejected
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
