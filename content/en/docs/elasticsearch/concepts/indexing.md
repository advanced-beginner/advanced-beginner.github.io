---
title: Indexing Strategy
weight: 6
lastmod: 2026-01-08
---

Learn Bulk indexing, Refresh, and Index Lifecycle Management for efficiently storing large volumes of data.

## Indexing Basics

### Indexing Process

```mermaid
flowchart LR
    A[Document Received] --> B[Analyze]
    B --> C[Create Inverted Index]
    C --> D[Memory Buffer]
    D --> E[Refresh]
    E --> F[Segment]
    F --> G[Flush]
    G --> H[Disk]
```

| Stage | Description |
|-------|-------------|
| Analyze | Split text into tokens |
| Memory Buffer | Temporary storage in memory |
| Refresh | Make searchable (default 1 second) |
| Segment | Immutable index piece |
| Flush | Permanent storage to disk |

---

## Single vs Bulk Indexing

### Single Document Indexing

```json
PUT /products/_doc/1
{
  "name": "MacBook Pro",
  "price": 2399
}
```

### Bulk Indexing

Process multiple documents at once:

```json
POST /_bulk
{"index": {"_index": "products", "_id": "1"}}
{"name": "MacBook Pro", "price": 2399}
{"index": {"_index": "products", "_id": "2"}}
{"name": "MacBook Air", "price": 1299}
{"index": {"_index": "products", "_id": "3"}}
{"name": "iPad", "price": 999}
```

> **NDJSON format**: Each line separated by newline (`\n`), including the last line

### Performance Comparison

| Method | Time for 10K docs | Network Requests |
|--------|-------------------|------------------|
| Single | ~30 seconds | 10,000 |
| Bulk (1000 per batch) | ~3 seconds | 10 |

### Recommended Bulk Settings

```json
POST /_bulk
// Recommended size: 5-15MB per request
// Recommended doc count: 1,000-5,000
```

---

## Refresh

### What is Refresh?

Operation that makes Memory Buffer data **searchable**.

### Refresh Interval

```json
PUT /products/_settings
{
  "index": {
    "refresh_interval": "30s"    // Default: 1s
  }
}
```

| Setting | Meaning | Use Case |
|---------|---------|----------|
| `1s` | Every 1 second (default) | Real-time search |
| `30s` | Every 30 seconds | Typical service |
| `-1` | Disabled | During bulk indexing |

### Optimization for Bulk Indexing

```json
// 1. Disable Refresh
PUT /products/_settings
{ "refresh_interval": "-1" }

// 2. Perform Bulk indexing
POST /_bulk
...

// 3. Manual Refresh
POST /products/_refresh

// 4. Restore Refresh
PUT /products/_settings
{ "refresh_interval": "1s" }
```

---

## Index Template

Settings automatically applied when creating new indices:

```json
PUT /_index_template/products_template
{
  "index_patterns": ["products-*"],
  "priority": 1,
  "template": {
    "settings": {
      "number_of_shards": 3,
      "number_of_replicas": 1,
      "refresh_interval": "5s"
    },
    "mappings": {
      "properties": {
        "name": { "type": "text" },
        "price": { "type": "integer" },
        "created_at": { "type": "date" }
      }
    }
  }
}
```

Now automatically applied when creating `products-2024`, `products-2025`, etc.

---

## Index Lifecycle Management (ILM)

Automatically manage the lifecycle of time-series data.

### Lifecycle Phases

```mermaid
flowchart LR
    A[Hot<br>Active write/read] --> B[Warm<br>Read-heavy]
    B --> C[Cold<br>Occasional reads]
    C --> D[Frozen<br>Rarely read]
    D --> E[Delete<br>Remove]
```

### Creating ILM Policy

```json
PUT /_ilm/policy/logs_policy
{
  "policy": {
    "phases": {
      "hot": {
        "min_age": "0ms",
        "actions": {
          "rollover": {
            "max_size": "50gb",
            "max_age": "7d"
          },
          "set_priority": { "priority": 100 }
        }
      },
      "warm": {
        "min_age": "7d",
        "actions": {
          "shrink": { "number_of_shards": 1 },
          "forcemerge": { "max_num_segments": 1 },
          "set_priority": { "priority": 50 }
        }
      },
      "cold": {
        "min_age": "30d",
        "actions": {
          "set_priority": { "priority": 0 }
        }
      },
      "delete": {
        "min_age": "90d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}
```

---

## Reindex

Copy/transform existing index to new index:

### Basic Reindex

```json
POST /_reindex
{
  "source": { "index": "products-old" },
  "dest": { "index": "products-new" }
}
```

### Filtered Reindex

```json
POST /_reindex
{
  "source": {
    "index": "products-old",
    "query": {
      "term": { "in_stock": true }
    }
  },
  "dest": { "index": "products-active" }
}
```

### Async Reindex

```json
POST /_reindex?wait_for_completion=false
{
  "source": { "index": "large-index" },
  "dest": { "index": "large-index-new" }
}
```

Check progress:
```json
GET /_tasks?actions=*reindex&detailed
```

---

## Alias

Give indices alternative names for flexible management:

### Create Alias

```json
POST /_aliases
{
  "actions": [
    { "add": { "index": "products-v1", "alias": "products" } }
  ]
}
```

### Zero Downtime Reindexing

```json
// 1. Create new index and copy data
PUT /products-v2
POST /_reindex
{
  "source": { "index": "products-v1" },
  "dest": { "index": "products-v2" }
}

// 2. Switch Alias (atomic)
POST /_aliases
{
  "actions": [
    { "remove": { "index": "products-v1", "alias": "products" } },
    { "add": { "index": "products-v2", "alias": "products" } }
  ]
}
```

Application uses only `products` alias → Zero-downtime switch

---

## Indexing Performance Optimization

### Bulk Indexing Checklist

```json
// 1. Disable Replicas
PUT /products/_settings
{ "number_of_replicas": 0 }

// 2. Disable Refresh
PUT /products/_settings
{ "refresh_interval": "-1" }

// 3. Perform Bulk indexing
POST /_bulk
...

// 4. Refresh
POST /products/_refresh

// 5. Restore settings
PUT /products/_settings
{
  "number_of_replicas": 1,
  "refresh_interval": "1s"
}
```

### Optimal Bulk Size

| Item | Recommended |
|------|-------------|
| Request size | 5-15 MB |
| Document count | 1,000-5,000 |
| Concurrent requests | 2-3 (per node) |

---

## Next Steps

| Goal | Recommended Document |
|------|---------------------|
| Cluster configuration | [Cluster Management](../cluster-management/) |
| Search optimization | [Performance Tuning](../performance-tuning/) |
| Failure response | [High Availability](../high-availability/) |
