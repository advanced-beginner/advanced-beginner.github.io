---
title: 집계
weight: 5
lastmod: 2026-01-08
prerequisites:
  - title: Query DSL
    path: /docs/elasticsearch/concepts/query-dsl/
  - title: 데이터 모델링
    path: /docs/elasticsearch/concepts/data-modeling/
related_concepts:
  - title: 검색 관련성
    path: /docs/elasticsearch/concepts/search-relevance/
  - title: 성능 튜닝
    path: /docs/elasticsearch/concepts/performance-tuning/
---

{{% notice style="info" title="선수 개념" %}}
이 문서를 읽기 전에 다음 개념을 먼저 이해하세요:
- [Query DSL](query-dsl/) - 기본 쿼리 구조
- [데이터 모델링](data-modeling/) - keyword vs text 타입
{{% /notice %}}

Elasticsearch의 Aggregations를 사용하여 데이터를 분석하고 통계를 추출하는 방법을 배웁니다.

Elasticsearch는 검색 엔진이지만, 강력한 **실시간 분석 엔진**이기도 합니다. 쇼핑몰에서 "노트북"을 검색했을 때 왼쪽에 나타나는 브랜드별 필터, 가격대별 필터, 평점 분포 등이 바로 Aggregation의 결과물입니다. 이런 기능을 RDBMS로 구현하려면 복잡한 서브쿼리와 GROUP BY 조합이 필요하고, 대용량 데이터에서는 성능 문제가 심각해집니다.

Aggregation은 검색과 동시에 데이터를 그룹화하고 통계를 계산할 수 있어, **단일 요청으로 검색 결과와 필터 패싯(facet)을 모두 얻을 수 있습니다**. 이는 사용자 경험과 서버 효율성 모두를 향상시킵니다. 대시보드 구축, 비즈니스 인텔리전스, 로그 분석 등 다양한 분야에서 Aggregation은 핵심적인 역할을 합니다.

## Aggregation이란?

**Aggregation**은 검색 결과를 그룹화하고 통계를 계산하는 기능입니다.
SQL의 `GROUP BY`와 집계 함수(`COUNT`, `SUM`, `AVG`)와 유사합니다.

### 기본 구조

```json
GET /products/_search
{
  "size": 0,                    // 검색 결과는 필요 없음
  "aggs": {
    "집계_이름": {
      "집계_타입": {
        // 집계 설정
      }
    }
  }
}
```

---

## Bucket Aggregations

데이터를 그룹(버킷)으로 나눕니다. SQL의 `GROUP BY`와 유사합니다.

### terms

필드 값별로 그룹화:

```json
GET /products/_search
{
  "size": 0,
  "aggs": {
    "categories": {
      "terms": {
        "field": "category",
        "size": 10           // 상위 10개 버킷
      }
    }
  }
}
```

SQL: `SELECT category, COUNT(*) FROM products GROUP BY category`

응답:
```json
{
  "aggregations": {
    "categories": {
      "buckets": [
        { "key": "노트북", "doc_count": 150 },
        { "key": "태블릿", "doc_count": 80 },
        { "key": "스마트폰", "doc_count": 200 }
      ]
    }
  }
}
```

#### 정렬 옵션

```json
{
  "terms": {
    "field": "category",
    "order": { "_count": "asc" }     // 문서 수 오름차순
    // "order": { "_key": "desc" }   // 키 이름 내림차순
  }
}
```

### range

숫자 범위로 그룹화:

```json
GET /products/_search
{
  "size": 0,
  "aggs": {
    "price_ranges": {
      "range": {
        "field": "price",
        "ranges": [
          { "to": 500000, "key": "50만원 미만" },
          { "from": 500000, "to": 1000000, "key": "50-100만원" },
          { "from": 1000000, "to": 2000000, "key": "100-200만원" },
          { "from": 2000000, "key": "200만원 이상" }
        ]
      }
    }
  }
}
```

### date_range

날짜 범위로 그룹화:

```json
{
  "aggs": {
    "sales_periods": {
      "date_range": {
        "field": "created_at",
        "format": "yyyy-MM-dd",
        "ranges": [
          { "from": "2024-01-01", "to": "2024-04-01", "key": "1분기" },
          { "from": "2024-04-01", "to": "2024-07-01", "key": "2분기" }
        ]
      }
    }
  }
}
```

### histogram

고정 간격으로 그룹화:

```json
GET /products/_search
{
  "size": 0,
  "aggs": {
    "price_histogram": {
      "histogram": {
        "field": "price",
        "interval": 500000,      // 50만원 단위
        "min_doc_count": 1       // 최소 1개 이상인 버킷만
      }
    }
  }
}
```

### date_histogram

시간 간격으로 그룹화:

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

#### interval 옵션

| 타입 | 옵션 | 예시 |
|------|------|------|
| `calendar_interval` | day, week, month, quarter, year | 달력 기준 |
| `fixed_interval` | 1d, 12h, 30m | 고정 시간 |

### filter / filters

조건별로 그룹화:

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

숫자 데이터의 통계를 계산합니다.

### 기본 메트릭

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

한 번에 여러 통계:

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

응답:
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

표준편차 등 추가 통계:

```json
{
  "aggs": {
    "price_extended": {
      "extended_stats": { "field": "price" }
    }
  }
}
```

추가 필드: `variance`, `std_deviation`, `std_deviation_bounds`

### cardinality

고유값 개수 (DISTINCT):

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

백분위수:

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

## 중첩 집계

버킷 안에서 추가 집계를 수행합니다.

### Bucket + Metric

카테고리별 평균 가격:

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

### 다단계 중첩

카테고리 > 브랜드 > 통계:

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

다른 집계 결과를 입력으로 사용합니다.

### avg_bucket

버킷들의 평균:

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

변화량 계산:

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

누적 합계:

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

## 검색과 집계 결합

### 필터링된 데이터에 집계

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

### global 집계

쿼리 무시하고 전체 데이터에 집계:

```json
GET /products/_search
{
  "query": {
    "match": { "name": "맥북" }
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

## 실전 예제

### 대시보드 통계

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

### 상품 필터 패싯

```json
GET /products/_search
{
  "size": 10,
  "query": {
    "match": { "name": "노트북" }
  },
  "aggs": {
    "brands": {
      "terms": { "field": "brand", "size": 20 }
    },
    "price_ranges": {
      "range": {
        "field": "price",
        "ranges": [
          { "to": 1000000, "key": "100만원 미만" },
          { "from": 1000000, "to": 2000000, "key": "100-200만원" },
          { "from": 2000000, "key": "200만원 이상" }
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

## 성능 팁

### 1. size: 0 사용

집계만 필요하면 검색 결과 제외:

```json
{ "size": 0, "aggs": {...} }
```

### 2. 필요한 버킷만

```json
{
  "terms": {
    "field": "category",
    "size": 10,          // 필요한 만큼만
    "shard_size": 25     // 샤드별 수집량 (정확도 vs 성능)
  }
}
```

### 3. Composite Aggregation

대용량 데이터 페이지네이션:

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
        "after": { "category": "노트북", "brand": "Apple" }  // 다음 페이지
      }
    }
  }
}
```

---

## SQL과 비교

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

## 다음 단계

| 목표 | 추천 문서 |
|------|----------|
| 인덱싱 최적화 | [인덱싱 전략](indexing/) |
| 실전 구현 | [상품 검색 시스템](../examples/product-search/) |
| 성능 최적화 | [성능 튜닝](performance-tuning/) |
