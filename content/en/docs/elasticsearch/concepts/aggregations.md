---
title: Aggregations
weight: 5
lastmod: 2026-01-08
prerequisites:
  - title: Query DSL
    path: /docs/elasticsearch/concepts/query-dsl/
  - title: Data Modeling
    path: /docs/elasticsearch/concepts/data-modeling/
related_concepts:
  - title: Search Relevance
    path: /docs/elasticsearch/concepts/search-relevance/
  - title: Performance Tuning
    path: /docs/elasticsearch/concepts/performance-tuning/
---

{{% notice style="info" title="Prerequisites" %}}
Before reading this document, understand these concepts first:
- [Query DSL](query-dsl/) - Basic query structure
- [Data Modeling](data-modeling/) - keyword vs text types
{{% /notice %}}

Learn how to analyze data and extract statistics using Elasticsearch Aggregations.

While Elasticsearch is a search engine, it's also a powerful **real-time analytics engine**. When you search for "laptop" on an e-commerce site, the brand filters, price range filters, and rating distributions that appear on the left side are all results of Aggregations. Implementing these features with RDBMS would require complex subqueries and GROUP BY combinations, and performance issues would be severe with large datasets.

Aggregations can group data and calculate statistics simultaneously with search queries, allowing you to **get both search results and filter facets in a single request**. This improves both user experience and server efficiency. Aggregations play a core role in various areas such as dashboard building, business intelligence, and log analysis.

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

#### Sorting Options

```json
{
  "terms": {
    "field": "category",
    "order": { "_count": "asc" }     // Ascending by document count
    // "order": { "_key": "desc" }   // Descending by key name
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
          { "to": 500000, "key": "Under $5,000" },
          { "from": 500000, "to": 1000000, "key": "$5,000-$10,000" },
          { "from": 1000000, "to": 2000000, "key": "$10,000-$20,000" },
          { "from": 2000000, "key": "Over $20,000" }
        ]
      }
    }
  }
}
```

### date_range

Group by date ranges:

```json
{
  "aggs": {
    "sales_periods": {
      "date_range": {
        "field": "created_at",
        "format": "yyyy-MM-dd",
        "ranges": [
          { "from": "2024-01-01", "to": "2024-04-01", "key": "Q1" },
          { "from": "2024-04-01", "to": "2024-07-01", "key": "Q2" }
        ]
      }
    }
  }
}
```

### histogram

Group by fixed intervals:

```json
GET /products/_search
{
  "size": 0,
  "aggs": {
    "price_histogram": {
      "histogram": {
        "field": "price",
        "interval": 500000,      // $5,000 intervals
        "min_doc_count": 1       // Only buckets with at least 1 doc
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

#### interval Options

| Type | Option | Example |
|------|--------|---------|
| `calendar_interval` | day, week, month, quarter, year | Calendar-based |
| `fixed_interval` | 1d, 12h, 30m | Fixed time |

### filter / filters

Group by conditions:

```json
GET /products/_search
{
  "size": 0,
  "aggs": {
    "stock_status": {
      "filters": {
        "filters": {
          "in_stock": { "term": { "in_stock": true } },
          "out_of_stock": { "term": { "in_stock": false } }
        }
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
      "min": 100000,
      "max": 5000000,
      "avg": 1500000,
      "sum": 150000000
    }
  }
}
```

### extended_stats

Additional statistics including standard deviation:

```json
{
  "aggs": {
    "price_extended": {
      "extended_stats": { "field": "price" }
    }
  }
}
```

Additional fields: `variance`, `std_deviation`, `std_deviation_bounds`

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

### Multi-level Nesting

Category > Brand > Statistics:

```json
GET /products/_search
{
  "size": 0,
  "aggs": {
    "by_category": {
      "terms": { "field": "category" },
      "aggs": {
        "by_brand": {
          "terms": { "field": "brand" },
          "aggs": {
            "price_stats": {
              "stats": { "field": "price" }
            }
          }
        }
      }
    }
  }
}
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

### cumulative_sum

Cumulative sum:

```json
{
  "aggs": {
    "daily_sales": {
      "date_histogram": {
        "field": "order_date",
        "calendar_interval": "day"
      },
      "aggs": {
        "sales": { "sum": { "field": "amount" } },
        "cumulative_sales": {
          "cumulative_sum": {
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

### global Aggregation

Ignore query and aggregate on all data:

```json
GET /products/_search
{
  "query": {
    "match": { "name": "MacBook" }
  },
  "aggs": {
    "filtered_count": {
      "value_count": { "field": "_id" }
    },
    "all_products": {
      "global": {},
      "aggs": {
        "total_count": {
          "value_count": { "field": "_id" }
        }
      }
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
          { "to": 1000000, "key": "Under $10,000" },
          { "from": 1000000, "to": 2000000, "key": "$10,000-$20,000" },
          { "from": 2000000, "key": "Over $20,000" }
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
| Indexing optimization | [Indexing Strategy](indexing/) |
| Practical implementation | [Product Search System](../examples/product-search/) |
| Performance optimization | [Performance Tuning](performance-tuning/) |
