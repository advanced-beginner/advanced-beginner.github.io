---
title: Aggregations
weight: 5
lastmod: 2026-01-08
---

Learn how to analyze data and extract statistics using Elasticsearch Aggregations.

## What is Aggregation?

**Aggregation** is a feature for grouping search results and calculating statistics.
Similar to SQL's `GROUP BY` and aggregate functions (`COUNT`, `SUM`, `AVG`).

### Basic Structure

```json
GET /products/_search
{
  "size": 0,                    // Don't need search results
  "aggs": {
    "aggregation_name": {
      "aggregation_type": {
        // Aggregation settings
      }
    }
  }
}
```

---

## Bucket Aggregations

Divide data into groups (buckets). Similar to SQL's `GROUP BY`.

### terms

Group by field values:

```json
GET /products/_search
{
  "size": 0,
  "aggs": {
    "categories": {
      "terms": {
        "field": "category",
        "size": 10           // Top 10 buckets
      }
    }
  }
}
```

SQL: `SELECT category, COUNT(*) FROM products GROUP BY category`

Response:
```json
{
  "aggregations": {
    "categories": {
      "buckets": [
        { "key": "Laptop", "doc_count": 150 },
        { "key": "Tablet", "doc_count": 80 },
        { "key": "Smartphone", "doc_count": 200 }
      ]
    }
  }
}
```

### range

Group by numeric ranges:

```json
GET /products/_search
{
  "size": 0,
  "aggs": {
    "price_ranges": {
      "range": {
        "field": "price",
        "ranges": [
          { "to": 500, "key": "Under $500" },
          { "from": 500, "to": 1000, "key": "$500-$1000" },
          { "from": 1000, "to": 2000, "key": "$1000-$2000" },
          { "from": 2000, "key": "Over $2000" }
        ]
      }
    }
  }
}
```

### date_histogram

Group by time intervals:

```json
GET /orders/_search
{
  "size": 0,
  "aggs": {
    "orders_over_time": {
      "date_histogram": {
        "field": "order_date",
        "calendar_interval": "month",  // day, week, month, year
        "format": "yyyy-MM"
      }
    }
  }
}
```

---

## Metric Aggregations

Calculate statistics on numeric data.

### Basic Metrics

```json
GET /products/_search
{
  "size": 0,
  "aggs": {
    "avg_price": { "avg": { "field": "price" } },
    "max_price": { "max": { "field": "price" } },
    "min_price": { "min": { "field": "price" } },
    "total_price": { "sum": { "field": "price" } },
    "product_count": { "value_count": { "field": "price" } }
  }
}
```

### stats

Multiple statistics at once:

```json
GET /products/_search
{
  "size": 0,
  "aggs": {
    "price_stats": {
      "stats": { "field": "price" }
    }
  }
}
```

Response:
```json
{
  "aggregations": {
    "price_stats": {
      "count": 100,
      "min": 100,
      "max": 5000,
      "avg": 1500,
      "sum": 150000
    }
  }
}
```

### cardinality

Unique value count (DISTINCT):

```json
GET /orders/_search
{
  "size": 0,
  "aggs": {
    "unique_customers": {
      "cardinality": {
        "field": "customer_id"
      }
    }
  }
}
```

SQL: `SELECT COUNT(DISTINCT customer_id) FROM orders`

### percentiles

Percentile values:

```json
GET /products/_search
{
  "size": 0,
  "aggs": {
    "price_percentiles": {
      "percentiles": {
        "field": "price",
        "percents": [25, 50, 75, 90, 99]
      }
    }
  }
}
```

---

## Nested Aggregations

Perform additional aggregations within buckets.

### Bucket + Metric

Average price by category:

```json
GET /products/_search
{
  "size": 0,
  "aggs": {
    "categories": {
      "terms": { "field": "category" },
      "aggs": {
        "avg_price": { "avg": { "field": "price" } },
        "max_price": { "max": { "field": "price" } }
      }
    }
  }
}
```

SQL:
```sql
SELECT category, AVG(price), MAX(price)
FROM products
GROUP BY category
```

---

## Pipeline Aggregations

Use results from other aggregations as input.

### avg_bucket

Average of buckets:

```json
GET /orders/_search
{
  "size": 0,
  "aggs": {
    "monthly_sales": {
      "date_histogram": {
        "field": "order_date",
        "calendar_interval": "month"
      },
      "aggs": {
        "total_sales": { "sum": { "field": "amount" } }
      }
    },
    "avg_monthly_sales": {
      "avg_bucket": {
        "buckets_path": "monthly_sales>total_sales"
      }
    }
  }
}
```

### derivative

Calculate change:

```json
{
  "aggs": {
    "monthly_sales": {
      "date_histogram": {
        "field": "order_date",
        "calendar_interval": "month"
      },
      "aggs": {
        "sales": { "sum": { "field": "amount" } },
        "sales_change": {
          "derivative": {
            "buckets_path": "sales"
          }
        }
      }
    }
  }
}
```

---

## Combining Search and Aggregations

### Aggregate on Filtered Data

```json
GET /products/_search
{
  "size": 0,
  "query": {
    "bool": {
      "filter": [
        { "term": { "in_stock": true } },
        { "range": { "created_at": { "gte": "2024-01-01" } } }
      ]
    }
  },
  "aggs": {
    "categories": {
      "terms": { "field": "category" }
    }
  }
}
```

---

## Practical Examples

### Dashboard Statistics

```json
GET /orders/_search
{
  "size": 0,
  "query": {
    "range": {
      "order_date": {
        "gte": "now-30d/d",
        "lt": "now/d"
      }
    }
  },
  "aggs": {
    "total_revenue": { "sum": { "field": "amount" } },
    "order_count": { "value_count": { "field": "_id" } },
    "avg_order_value": { "avg": { "field": "amount" } },
    "unique_customers": { "cardinality": { "field": "customer_id" } },
    "daily_trend": {
      "date_histogram": {
        "field": "order_date",
        "calendar_interval": "day"
      },
      "aggs": {
        "revenue": { "sum": { "field": "amount" } }
      }
    },
    "top_products": {
      "terms": {
        "field": "product_id",
        "size": 5
      },
      "aggs": {
        "revenue": { "sum": { "field": "amount" } }
      }
    }
  }
}
```

### Product Filter Facets

```json
GET /products/_search
{
  "size": 10,
  "query": {
    "match": { "name": "laptop" }
  },
  "aggs": {
    "brands": {
      "terms": { "field": "brand", "size": 20 }
    },
    "price_ranges": {
      "range": {
        "field": "price",
        "ranges": [
          { "to": 1000, "key": "Under $1000" },
          { "from": 1000, "to": 2000, "key": "$1000-$2000" },
          { "from": 2000, "key": "Over $2000" }
        ]
      }
    },
    "ratings": {
      "terms": { "field": "rating" }
    }
  }
}
```

---

## Performance Tips

### 1. Use size: 0

Exclude search results if only aggregations needed:

```json
{ "size": 0, "aggs": {...} }
```

### 2. Only Necessary Buckets

```json
{
  "terms": {
    "field": "category",
    "size": 10,          // Only as many as needed
    "shard_size": 25     // Per-shard collection (accuracy vs performance)
  }
}
```

### 3. Composite Aggregation

Pagination for large data:

```json
{
  "aggs": {
    "my_composite": {
      "composite": {
        "size": 1000,
        "sources": [
          { "category": { "terms": { "field": "category" } } },
          { "brand": { "terms": { "field": "brand" } } }
        ],
        "after": { "category": "Laptop", "brand": "Apple" }  // Next page
      }
    }
  }
}
```

---

## SQL Comparison

| SQL | Aggregation |
|-----|-------------|
| `COUNT(*)` | `value_count` |
| `COUNT(DISTINCT x)` | `cardinality` |
| `SUM(x)` | `sum` |
| `AVG(x)` | `avg` |
| `MIN(x)` | `min` |
| `MAX(x)` | `max` |
| `GROUP BY x` | `terms` |
| `GROUP BY CASE WHEN` | `range` / `filters` |

---

## Next Steps

| Goal | Recommended Document |
|------|---------------------|
| Indexing optimization | [Indexing Strategy](../indexing/) |
| Practical implementation | [Product Search System](../../examples/product-search/) |
| Performance optimization | [Performance Tuning](../performance-tuning/) |
