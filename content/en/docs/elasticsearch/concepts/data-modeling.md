---
title: Data Modeling
description: "Data modeling design principles and mapping strategies"
weight: 2
lastmod: 2026-01-10
---

{{< callout type="tip" title="TL;DR" >}}
- **Mapping**: Schema defining document structure (similar to RDB table definitions)
- **text**: For full-text search, tokenized by Analyzer
- **keyword**: For exact value matching, sorting/aggregation
- **Analyzer**: Converts text into searchable tokens (use Nori for Korean)
- **Denormalization**: Include related data in one document since there's no JOIN
{{< /callout >}}

**Target Audience**: Developers looking to use Elasticsearch search features
**Prerequisites**: [Core Components](core-components/), basic JSON syntax

This document covers Mapping, Field Type, and Analyzer design for effectively storing and searching data in Elasticsearch.

## What is Mapping?

**Mapping** is a schema that defines how documents and fields are stored and indexed.

### RDB vs Elasticsearch

| RDB | Elasticsearch |
|-----|---------------|
| CREATE TABLE | PUT /index (mapping) |
| Column Type | Field Type |
| Schema Required | Dynamic Mapping possible |
| ALTER TABLE | Limited (requires reindexing) |

### Mapping Example

```json
PUT /products
{
  "mappings": {
    "properties": {
      "name": {
        "type": "text",
        "analyzer": "standard"
      },
      "category": {
        "type": "keyword"
      },
      "price": {
        "type": "integer"
      },
      "created_at": {
        "type": "date"
      },
      "in_stock": {
        "type": "boolean"
      }
    }
  }
}
```

{{< callout type="info" title="Key Points" >}}
- Mapping is defined when creating an index, and field type changes are limited afterward
- Dynamic Mapping allows automatic type inference, but explicit definition is recommended for production
- Schema changes require reindexing
{{< /callout >}}

---

## Field Types

### String Types

#### text vs keyword

| Property | text | keyword |
|----------|------|---------|
| Purpose | Full-text search | Exact value matching |
| Analysis | Tokenized by Analyzer | No analysis |
| Search | match query | term query |
| Sort/Aggregation | Not possible (by default) | Possible |
| Examples | Product description, post content | Category, status, ID |

```json
{
  "properties": {
    "title": {
      "type": "text"          // "MacBook Pro" → ["macbook", "pro"]
    },
    "category": {
      "type": "keyword"       // "Laptop" → "Laptop" (as-is)
    }
  }
}
```

#### Multi-field

Index a single field in multiple ways:

```json
{
  "properties": {
    "name": {
      "type": "text",
      "fields": {
        "keyword": {
          "type": "keyword"   // Access via name.keyword
        }
      }
    }
  }
}
```

```bash
# Full-text search
GET /products/_search
{ "query": { "match": { "name": "MacBook" } } }

# Exact value aggregation
GET /products/_search
{
  "aggs": {
    "names": { "terms": { "field": "name.keyword" } }
  }
}
```

### Numeric Types

| Type | Range | Use Case |
|------|-------|----------|
| `byte` | -128 ~ 127 | Small integers |
| `short` | -32,768 ~ 32,767 | Small integers |
| `integer` | -2³¹ ~ 2³¹-1 | General integers |
| `long` | -2⁶³ ~ 2⁶³-1 | Large integers, IDs |
| `float` | 32-bit floating point | Approximate values |
| `double` | 64-bit floating point | Precise calculations |
| `scaled_float` | Scaled value | Prices (scaling_factor: 100) |

```json
{
  "properties": {
    "price": {
      "type": "scaled_float",
      "scaling_factor": 100    // 23900.00 → 2390000 stored
    },
    "quantity": {
      "type": "integer"
    }
  }
}
```

### Date Type

```json
{
  "properties": {
    "created_at": {
      "type": "date",
      "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
    }
  }
}
```

Supported formats:
- `2024-01-15`
- `2024-01-15T10:30:00`
- `2024-01-15T10:30:00+09:00`
- `1705300200000` (epoch millis)

### Boolean Type

```json
{
  "properties": {
    "in_stock": {
      "type": "boolean"   // true, false, "true", "false" all accepted
    }
  }
}
```

### Complex Types

#### Object

Nested JSON objects:

```json
{
  "properties": {
    "seller": {
      "properties": {
        "name": { "type": "keyword" },
        "rating": { "type": "float" }
      }
    }
  }
}
```

```json
// Document
{
  "seller": {
    "name": "Official Store",
    "rating": 4.8
  }
}

// Search
GET /products/_search
{
  "query": {
    "match": { "seller.name": "Official Store" }
  }
}
```

#### Nested

**Problem with Object type:**

```json
// Document
{
  "options": [
    { "color": "black", "size": "M" },
    { "color": "white", "size": "L" }
  ]
}
```

Object type flattens arrays:
```
options.color: ["black", "white"]
options.size: ["M", "L"]
```

→ Searching "black AND L" incorrectly matches!

**Use Nested type:**

```json
{
  "properties": {
    "options": {
      "type": "nested",
      "properties": {
        "color": { "type": "keyword" },
        "size": { "type": "keyword" }
      }
    }
  }
}
```

```json
// Accurate nested query
GET /products/_search
{
  "query": {
    "nested": {
      "path": "options",
      "query": {
        "bool": {
          "must": [
            { "term": { "options.color": "black" } },
            { "term": { "options.size": "M" } }
          ]
        }
      }
    }
  }
}
```

{{< callout type="info" title="Key Points" >}}
- **text**: For full-text search, use match query
- **keyword**: For exact values, sorting/aggregation, use term query
- **Multi-field**: Can index a single field as both text and keyword (name.keyword)
- **Nested**: Use when relationships between objects in an array need to be preserved (Object type flattens)
{{< /callout >}}

---

## Analyzer

An **Analyzer** converts text into searchable tokens.

### Analysis Process

```mermaid
flowchart LR
    A["Input Text<br>The Quick Brown Fox"]
    --> B["Character Filter<br>(HTML removal, etc.)"]
    --> C["Tokenizer<br>(word separation)"]
    --> D["Token Filter<br>(lowercase, etc.)"]
    --> E["Tokens<br>&#91;the, quick, brown, fox&#93;"]
```

*Diagram: The process of converting input text into final tokens through Character Filter, Tokenizer, and Token Filter.*

### Built-in Analyzers

| Analyzer | Behavior | Example Result |
|----------|----------|----------------|
| `standard` | Word separation + lowercase | "Quick Brown" → [quick, brown] |
| `simple` | Extract letters only + lowercase | "Quick-Brown" → [quick, brown] |
| `whitespace` | Split by whitespace | "Quick Brown" → [Quick, Brown] |
| `keyword` | No analysis | "Quick Brown" → [Quick Brown] |

### Testing Analyzers

```json
GET /_analyze
{
  "analyzer": "standard",
  "text": "The Quick Brown Fox"
}
```

```json
{
  "tokens": [
    { "token": "the", "position": 0 },
    { "token": "quick", "position": 1 },
    { "token": "brown", "position": 2 },
    { "token": "fox", "position": 3 }
  ]
}
```

### Korean Analyzer (Nori)

Korean text cannot be properly tokenized by whitespace alone.

```json
// Standard Analyzer
"삼성전자가 스마트폰을 출시했다"
→ ["삼성전자가", "스마트폰을", "출시했다"]

// Nori Analyzer
"삼성전자가 스마트폰을 출시했다"
→ ["삼성", "전자", "스마트폰", "출시"]
```

#### Nori Configuration

```json
PUT /products
{
  "settings": {
    "analysis": {
      "analyzer": {
        "korean": {
          "type": "custom",
          "tokenizer": "nori_tokenizer",
          "filter": ["nori_part_of_speech"]
        }
      },
      "tokenizer": {
        "nori_tokenizer": {
          "type": "nori_tokenizer",
          "decompound_mode": "mixed"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "name": {
        "type": "text",
        "analyzer": "korean"
      }
    }
  }
}
```

#### decompound_mode Options

| Mode | "삼성전자" Result |
|------|------------------|
| `none` | [삼성전자] |
| `discard` | [삼성, 전자] |
| `mixed` | [삼성전자, 삼성, 전자] |

> **Recommended:** `mixed` - Both compound words and separated words are searchable

### Custom Analyzer

```json
PUT /products
{
  "settings": {
    "analysis": {
      "analyzer": {
        "my_analyzer": {
          "type": "custom",
          "char_filter": ["html_strip"],
          "tokenizer": "standard",
          "filter": ["lowercase", "my_synonym"]
        }
      },
      "filter": {
        "my_synonym": {
          "type": "synonym",
          "synonyms": [
            "노트북, 랩탑",
            "핸드폰, 스마트폰, 휴대폰"
          ]
        }
      }
    }
  }
}
```

{{< callout type="info" title="Key Points" >}}
- Analyzer = Character Filter + Tokenizer + Token Filter
- Nori Analyzer is recommended for Korean (decompound_mode: mixed)
- Use `/_analyze` API to test analysis results
- Synonym handling is configured with Custom Analyzer
{{< /callout >}}

---

## Dynamic Mapping

If Mapping is not defined, Elasticsearch automatically infers types.

### Automatic Type Inference

| JSON Value | Inferred Type |
|------------|---------------|
| `"hello"` | text + keyword |
| `123` | long |
| `12.34` | float |
| `true` | boolean |
| `"2024-01-15"` | date |
| `{ "a": 1 }` | object |

### Controlling Dynamic Mapping

```json
PUT /products
{
  "mappings": {
    "dynamic": "strict",    // false: ignore, strict: error
    "properties": {
      "name": { "type": "text" }
    }
  }
}
```

| Setting | Behavior |
|---------|----------|
| `true` | Auto-add new fields (default) |
| `false` | Store new fields but don't index |
| `strict` | Error on new fields |

> **Production recommendation:** `strict` or explicit Mapping definition

{{< callout type="info" title="Key Points" >}}
- Dynamic Mapping is convenient during development, but risks unexpected type inference in production
- Setting `dynamic: strict` will throw an error when undefined fields are input
- `dynamic: false` stores new fields but doesn't index them (not searchable)
{{< /callout >}}

---

## Modeling Patterns

### Pattern 1: Denormalization

Elasticsearch doesn't support JOIN, so include related data in a single document.

```json
// RDB Normalized (2 tables)
// products: id, name, category_id
// categories: id, name

// Elasticsearch Denormalized (1 document)
{
  "name": "MacBook Pro",
  "category": {
    "id": 1,
    "name": "Laptop"
  }
}
```

**Pros:** Fast search, simple queries
**Cons:** All documents need updating when category changes

### Pattern 2: Application-Side Join

Manage frequently changing data in separate indices:

```java
// 1. Search products
List<Product> products = productRepository.search(query);

// 2. Fetch inventory info (separate index)
List<String> productIds = products.stream().map(Product::getId).toList();
Map<String, Stock> stocks = stockRepository.findByIds(productIds);

// 3. Combine
products.forEach(p -> p.setStock(stocks.get(p.getId())));
```

### Pattern 3: Nested vs Parent-Child

| Property | Nested | Parent-Child (Join) |
|----------|--------|---------------------|
| Performance | Fast | Slow |
| Update | Re-index entire document | Update child only |
| Query Complexity | Low | High |
| Recommended For | Rarely changing relations | Frequently changing 1:N |

{{< callout type="info" title="Key Points" >}}
- Denormalization is the default strategy since Elasticsearch doesn't support JOIN
- Consider Application-Side Join for frequently changing data
- Nested has good performance but requires full document re-indexing; Parent-Child allows individual updates
{{< /callout >}}

---

## Best Practices

### 1. Use text for search fields, keyword for filter/aggregation fields

```json
{
  "name": {
    "type": "text",
    "fields": { "keyword": { "type": "keyword" } }
  },
  "status": { "type": "keyword" }
}
```

### 2. Use keyword for numeric IDs

```json
{
  "user_id": { "type": "keyword" }  // Not long!
}
```
If no range queries needed, keyword is more efficient.

### 3. Exclude unnecessary fields from indexing

```json
{
  "raw_data": {
    "type": "object",
    "enabled": false    // Store only, not searchable
  }
}
```

### 4. Use Index Templates

```json
PUT /_index_template/logs
{
  "index_patterns": ["logs-*"],
  "template": {
    "mappings": {
      "properties": {
        "@timestamp": { "type": "date" },
        "message": { "type": "text" }
      }
    }
  }
}
```

{{< callout type="info" title="Key Points" >}}
- Configure search fields as text + keyword Multi-field
- Numeric IDs are more efficient as keyword if no range queries
- Exclude fields from indexing with `enabled: false` if not searching
- Apply consistent Mapping with index templates
{{< /callout >}}

---

## Next Steps

| Goal | Recommended Document |
|------|---------------------|
| Write search queries | [Query DSL](query-dsl/) |
| Improve search quality | [Search Relevance](search-relevance/) |
| Hands-on practice | [Basic Examples](../examples/basic/) |
