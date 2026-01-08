---
title: Query DSL
weight: 3
lastmod: 2026-01-08
---

Learn how to write various search queries using Elasticsearch's Query DSL (Domain Specific Language).

## Basic Query Structure

```json
GET /products/_search
{
  "query": {
    // Search conditions
  },
  "from": 0,           // Pagination offset
  "size": 10,          // Number of documents to return
  "sort": [            // Sorting
    { "price": "asc" }
  ],
  "_source": ["name", "price"]  // Fields to return
}
```

---

## Query Context vs Filter Context

Elasticsearch queries execute in two contexts.

| Aspect | Query Context | Filter Context |
|--------|---------------|----------------|
| Question | "How well does it match?" | "Does it match or not?" |
| Score | Calculated (relevance score) | Not calculated |
| Caching | No | Yes |
| Use Case | Full-text search | Exact value filtering |

```json
GET /products/_search
{
  "query": {
    "bool": {
      "must": [
        { "match": { "name": "MacBook" } }      // Query context (score calculated)
      ],
      "filter": [
        { "term": { "category": "Laptop" } },   // Filter context (cached)
        { "range": { "price": { "lte": 2000 } } }
      ]
    }
  }
}
```

> **Performance Tip:** Put exact value comparisons in `filter` to benefit from caching.

---

## Full-Text Queries

### match

The most common full-text search.

```json
GET /products/_search
{
  "query": {
    "match": {
      "name": "MacBook Pro"
    }
  }
}
```

"MacBook Pro" → Analyzer → ["macbook", "pro"]
→ Search documents containing either (OR)

#### operator Option

```json
{
  "match": {
    "name": {
      "query": "MacBook Pro",
      "operator": "and"    // Must contain both (default: or)
    }
  }
}
```

#### minimum_should_match

```json
{
  "match": {
    "name": {
      "query": "MacBook Pro 14-inch",
      "minimum_should_match": "2"  // At least 2 of 3 must match
    }
  }
}
```

### match_phrase

Word order must also match.

```json
GET /products/_search
{
  "query": {
    "match_phrase": {
      "description": "M3 Pro chip"
    }
  }
}
```

- ✅ "M3 Pro chip included"
- ❌ "M3 chip and Pro display"

#### slop Option

```json
{
  "match_phrase": {
    "description": {
      "query": "M3 chip",
      "slop": 1          // Allow 1 word between
    }
  }
}
```
→ "M3 Pro chip" also matches

### multi_match

Search across multiple fields:

```json
GET /products/_search
{
  "query": {
    "multi_match": {
      "query": "MacBook",
      "fields": ["name^2", "description"]  // name has 2x weight
    }
  }
}
```

#### type Options

| type | Behavior |
|------|----------|
| `best_fields` | Use highest scoring field (default) |
| `most_fields` | Sum scores from all fields |
| `cross_fields` | Treat multiple fields as one |
| `phrase` | Search as match_phrase |

---

## Term Level Queries

Search for exact values without analysis.

### term

Search for exactly matching values:

```json
GET /products/_search
{
  "query": {
    "term": {
      "category": "Laptop"
    }
  }
}
```

> **Warning:** Don't use on `text` fields. It compares against analyzed tokens and won't work as expected.

```json
// ❌ Wrong usage
{ "term": { "name": "MacBook Pro" } }  // Won't work if name is text type

// ✅ Correct usage
{ "term": { "name.keyword": "MacBook Pro" } }  // Use keyword field
{ "term": { "category": "Laptop" } }           // keyword type field
```

### terms

Match any of multiple values:

```json
GET /products/_search
{
  "query": {
    "terms": {
      "category": ["Laptop", "Tablet", "Smartphone"]
    }
  }
}
```
SQL: `WHERE category IN ('Laptop', 'Tablet', 'Smartphone')`

### range

Range search:

```json
GET /products/_search
{
  "query": {
    "range": {
      "price": {
        "gte": 1000,   // >=
        "lte": 2000    // <=
      }
    }
  }
}
```

| Operator | Meaning |
|----------|---------|
| `gt` | > |
| `gte` | >= |
| `lt` | < |
| `lte` | <= |

#### Date Range

```json
{
  "range": {
    "created_at": {
      "gte": "2024-01-01",
      "lt": "2024-02-01",
      "format": "yyyy-MM-dd"
    }
  }
}
```

Relative dates:
```json
{
  "range": {
    "created_at": {
      "gte": "now-7d/d",    // From 7 days ago
      "lt": "now/d"         // Until today
    }
  }
}
```

### exists

Documents where field exists:

```json
GET /products/_search
{
  "query": {
    "exists": {
      "field": "discount_price"
    }
  }
}
```

### prefix

Prefix search:

```json
GET /products/_search
{
  "query": {
    "prefix": {
      "name.keyword": "Mac"
    }
  }
}
```

### wildcard

Wildcard pattern search:

```json
GET /products/_search
{
  "query": {
    "wildcard": {
      "sku": "PROD-*-2024"
    }
  }
}
```

> **Performance Warning:** Very slow when `*` comes first. Avoid if possible.

### fuzzy

Typo-tolerant search:

```json
GET /products/_search
{
  "query": {
    "fuzzy": {
      "name": {
        "value": "Macbok",     // Typo
        "fuzziness": "AUTO"    // Auto edit distance
      }
    }
  }
}
```

| fuzziness | Behavior |
|-----------|----------|
| `0` | Exact match |
| `1` | Allow 1 character difference |
| `2` | Allow 2 character differences |
| `AUTO` | Auto based on length (recommended) |

---

## Bool Query

Combine multiple queries.

```json
GET /products/_search
{
  "query": {
    "bool": {
      "must": [],          // AND - Must match, affects score
      "should": [],        // OR - Match increases score
      "must_not": [],      // NOT - Exclude if matches
      "filter": []         // AND - Must match, no score, cached
    }
  }
}
```

### Practical Example: Product Search

"Category is Laptop, price $1000-$2000, 'MacBook' keyword, exclude out of stock"

```json
GET /products/_search
{
  "query": {
    "bool": {
      "must": [
        { "match": { "name": "MacBook" } }
      ],
      "filter": [
        { "term": { "category": "Laptop" } },
        { "range": { "price": { "gte": 1000, "lte": 2000 } } }
      ],
      "must_not": [
        { "term": { "status": "sold_out" } }
      ]
    }
  }
}
```

### should Behavior

If `must` or `filter` exists, `should` is optional (only boosts score):

```json
{
  "bool": {
    "must": [
      { "match": { "name": "MacBook" } }
    ],
    "should": [
      { "term": { "is_promotion": true } }  // Promotion items score higher
    ]
  }
}
```

If no `must` or `filter`, at least one `should` must match:

```json
{
  "bool": {
    "should": [
      { "term": { "category": "Laptop" } },
      { "term": { "category": "Tablet" } }
    ],
    "minimum_should_match": 1
  }
}
```

### Nested Bool Query

```json
GET /products/_search
{
  "query": {
    "bool": {
      "must": [
        { "match": { "name": "Pro" } }
      ],
      "should": [
        {
          "bool": {
            "must": [
              { "term": { "brand": "apple" } },
              { "range": { "price": { "gte": 2000 } } }
            ]
          }
        },
        {
          "bool": {
            "must": [
              { "term": { "brand": "samsung" } },
              { "range": { "price": { "gte": 1500 } } }
            ]
          }
        }
      ]
    }
  }
}
```

---

## Controlling Search Results

### Pagination

```json
GET /products/_search
{
  "from": 0,     // Start position (0-based)
  "size": 10,    // Number to fetch
  "query": { "match_all": {} }
}
```

> **Warning:** `from + size` is limited to 10,000 by default. Use `search_after` for large pagination.

### Sorting

```json
GET /products/_search
{
  "sort": [
    { "price": "asc" },
    { "created_at": "desc" },
    "_score"                    // Relevance score
  ],
  "query": { "match": { "name": "MacBook" } }
}
```

### Field Selection

```json
GET /products/_search
{
  "_source": ["name", "price"],   // Fields to include
  // or
  "_source": {
    "includes": ["name", "price"],
    "excludes": ["description"]
  },
  "query": { "match_all": {} }
}
```

### Highlighting

Highlight search terms:

```json
GET /products/_search
{
  "query": {
    "match": { "description": "M3 chip" }
  },
  "highlight": {
    "fields": {
      "description": {
        "pre_tags": ["<em>"],
        "post_tags": ["</em>"]
      }
    }
  }
}
```

Response:
```json
{
  "hits": [{
    "_source": { "description": "M3 Pro chip included..." },
    "highlight": {
      "description": ["<em>M3</em> Pro <em>chip</em> included..."]
    }
  }]
}
```

---

## SQL Comparison

| SQL | Query DSL |
|-----|-----------|
| `SELECT *` | `"_source": true` |
| `WHERE name = 'x'` | `"term": { "name": "x" }` |
| `WHERE name LIKE '%x%'` | `"match": { "name": "x" }` |
| `WHERE price > 100` | `"range": { "price": { "gt": 100 } }` |
| `WHERE a AND b` | `"bool": { "must": [a, b] }` |
| `WHERE a OR b` | `"bool": { "should": [a, b] }` |
| `WHERE NOT a` | `"bool": { "must_not": [a] }` |
| `ORDER BY price ASC` | `"sort": [{ "price": "asc" }]` |
| `LIMIT 10 OFFSET 20` | `"from": 20, "size": 10` |

---

## Common Mistakes

### 1. Using term query on text fields

```json
// ❌ Wrong - "MacBook Pro" is stored as ["macbook", "pro"]
{ "term": { "name": "MacBook Pro" } }

// ✅ Correct
{ "match": { "name": "MacBook Pro" } }
// or
{ "term": { "name.keyword": "MacBook Pro" } }
```

### 2. Using term query without filter

```json
// ⚠️ Unnecessary score calculation
{ "query": { "term": { "category": "Laptop" } } }

// ✅ Better with filter for caching
{
  "query": {
    "bool": {
      "filter": [
        { "term": { "category": "Laptop" } }
      ]
    }
  }
}
```

### 3. Large pagination

```json
// ❌ Error if exceeds 10000
{ "from": 10000, "size": 10 }

// ✅ Use search_after
{
  "size": 10,
  "sort": [{ "created_at": "desc" }, { "_id": "asc" }],
  "search_after": ["2024-01-15T10:00:00", "abc123"]
}
```

---

## Next Steps

| Goal | Recommended Document |
|------|---------------------|
| Improve search quality | [Search Relevance](../search-relevance/) |
| Data analysis | [Aggregations](../aggregations/) |
| Hands-on practice | [Basic Examples](../../examples/basic/) |
