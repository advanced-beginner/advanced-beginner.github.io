---
title: Product Search System
weight: 3
lastmod: 2026-01-08
---

Implement a production-level product search system with Korean morphological analysis, autocomplete, and filtering.

## Implementation Goals

```mermaid
flowchart LR
    A[Search Input] --> B[Autocomplete]
    A --> C[Execute Search]
    C --> D[Apply Filters]
    D --> E[Return Results]
    E --> F[Highlighting]
```

- **Korean Search**: Searching "Samsung Electronics" matches both "Samsung" and "Electronics"
- **Autocomplete**: Typing "MacBook P" suggests "MacBook Pro"
- **Filtering**: Category, price range, brand filters
- **Highlighting**: Highlight search terms

---

## 1. Index Design

### Nori Analyzer Configuration

```json
PUT /products
{
  "settings": {
    "analysis": {
      "analyzer": {
        "korean_analyzer": {
          "type": "custom",
          "tokenizer": "nori_tokenizer",
          "filter": [
            "nori_readingform",
            "lowercase",
            "nori_part_of_speech_filter"
          ]
        },
        "autocomplete_analyzer": {
          "type": "custom",
          "tokenizer": "edge_ngram_tokenizer",
          "filter": ["lowercase"]
        },
        "autocomplete_search_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["lowercase"]
        }
      },
      "tokenizer": {
        "nori_tokenizer": {
          "type": "nori_tokenizer",
          "decompound_mode": "mixed"
        },
        "edge_ngram_tokenizer": {
          "type": "edge_ngram",
          "min_gram": 1,
          "max_gram": 20,
          "token_chars": ["letter", "digit"]
        }
      },
      "filter": {
        "nori_part_of_speech_filter": {
          "type": "nori_part_of_speech",
          "stoptags": [
            "E", "IC", "J", "MAG", "MAJ",
            "MM", "SP", "SSC", "SSO", "SC",
            "SE", "XPN", "XSA", "XSN", "XSV",
            "UNA", "NA", "VSV"
          ]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "name": {
        "type": "text",
        "analyzer": "korean_analyzer",
        "fields": {
          "keyword": {
            "type": "keyword"
          },
          "autocomplete": {
            "type": "text",
            "analyzer": "autocomplete_analyzer",
            "search_analyzer": "autocomplete_search_analyzer"
          }
        }
      },
      "description": {
        "type": "text",
        "analyzer": "korean_analyzer"
      },
      "category": {
        "type": "keyword"
      },
      "brand": {
        "type": "keyword"
      },
      "price": {
        "type": "integer"
      },
      "discount_price": {
        "type": "integer"
      },
      "rating": {
        "type": "float"
      },
      "review_count": {
        "type": "integer"
      },
      "in_stock": {
        "type": "boolean"
      },
      "tags": {
        "type": "keyword"
      },
      "created_at": {
        "type": "date"
      }
    }
  }
}
```

### Test Analyzer

```json
// Korean analysis
GET /products/_analyze
{
  "analyzer": "korean_analyzer",
  "text": "Samsung GalaxyBook Pro"
}
// Result: ["samsung", "galaxybook", "galaxy", "book", "pro"]

// Autocomplete analysis
GET /products/_analyze
{
  "analyzer": "autocomplete_analyzer",
  "text": "MacBook"
}
// Result: ["m", "ma", "mac", "macb", "macbo", "macboo", "macbook"]
```

---

## 2. Sample Data

```json
POST /_bulk
{"index": {"_index": "products", "_id": "1"}}
{"name": "MacBook Pro 14-inch M3 Pro", "description": "Apple M3 Pro chip, 18GB unified memory, 512GB SSD", "category": "Laptop", "brand": "Apple", "price": 2390, "discount_price": 2290, "rating": 4.8, "review_count": 1250, "in_stock": true, "tags": ["premium", "new-arrival"], "created_at": "2024-01-10"}
{"index": {"_index": "products", "_id": "2"}}
{"name": "MacBook Air 13-inch M3", "description": "Apple M3 chip, 8GB unified memory, 256GB SSD, Midnight", "category": "Laptop", "brand": "Apple", "price": 1390, "rating": 4.7, "review_count": 890, "in_stock": true, "tags": ["bestseller"], "created_at": "2024-01-15"}
{"index": {"_index": "products", "_id": "3"}}
{"name": "Galaxy Book4 Pro 16-inch", "description": "Intel Core Ultra 7, 16GB RAM, 512GB SSD", "category": "Laptop", "brand": "Samsung", "price": 1890, "rating": 4.5, "review_count": 456, "in_stock": true, "tags": ["new-arrival"], "created_at": "2024-01-20"}
{"index": {"_index": "products", "_id": "4"}}
{"name": "iPad Pro 11-inch M4", "description": "Apple M4 chip, 256GB, Space Black", "category": "Tablet", "brand": "Apple", "price": 1499, "rating": 4.9, "review_count": 2100, "in_stock": true, "tags": ["premium", "new-arrival"], "created_at": "2024-01-05"}
{"index": {"_index": "products", "_id": "5"}}
{"name": "Galaxy Tab S9 Ultra", "description": "Snapdragon 8 Gen 2, 12GB RAM, 256GB", "category": "Tablet", "brand": "Samsung", "price": 1599, "rating": 4.6, "review_count": 780, "in_stock": false, "tags": ["large-screen"], "created_at": "2024-01-08"}
```

---

## 3. Search Implementation

### Basic Search

```json
GET /products/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "multi_match": {
            "query": "MacBook Pro",
            "fields": ["name^3", "description"],
            "type": "best_fields"
          }
        }
      ],
      "filter": [
        { "term": { "in_stock": true } }
      ]
    }
  }
}
```

### Autocomplete

```json
GET /products/_search
{
  "size": 5,
  "_source": ["name"],
  "query": {
    "match": {
      "name.autocomplete": {
        "query": "MacBook P",
        "operator": "and"
      }
    }
  }
}
```

### Filter + Search Combination

```json
GET /products/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "multi_match": {
            "query": "Pro",
            "fields": ["name^3", "description"]
          }
        }
      ],
      "filter": [
        { "term": { "category": "Laptop" } },
        { "terms": { "brand": ["Apple", "Samsung"] } },
        { "range": { "price": { "gte": 1000, "lte": 2500 } } },
        { "term": { "in_stock": true } }
      ]
    }
  },
  "sort": [
    { "_score": "desc" },
    { "review_count": "desc" }
  ]
}
```

### Search + Filter Facets

```json
GET /products/_search
{
  "size": 10,
  "query": {
    "bool": {
      "must": [
        { "match": { "name": "laptop" } }
      ],
      "filter": [
        { "term": { "in_stock": true } }
      ]
    }
  },
  "aggs": {
    "categories": {
      "terms": { "field": "category", "size": 10 }
    },
    "brands": {
      "terms": { "field": "brand", "size": 20 }
    },
    "price_ranges": {
      "range": {
        "field": "price",
        "ranges": [
          { "key": "Under $1000", "to": 1000 },
          { "key": "$1000-$1500", "from": 1000, "to": 1500 },
          { "key": "$1500-$2000", "from": 1500, "to": 2000 },
          { "key": "$2000+", "from": 2000 }
        ]
      }
    },
    "avg_rating": {
      "avg": { "field": "rating" }
    }
  }
}
```

### Highlighting

```json
GET /products/_search
{
  "query": {
    "match": { "description": "M3 chip" }
  },
  "highlight": {
    "fields": {
      "name": {
        "pre_tags": ["<em class='highlight'>"],
        "post_tags": ["</em>"]
      },
      "description": {
        "pre_tags": ["<em class='highlight'>"],
        "post_tags": ["</em>"],
        "fragment_size": 100,
        "number_of_fragments": 3
      }
    }
  }
}
```

---

## 4. Spring Boot Implementation

### Product.java

```java
@Document(indexName = "products")
public class Product {

    @Id
    private String id;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "korean_analyzer"),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword),
            @InnerField(suffix = "autocomplete", type = FieldType.Text,
                analyzer = "autocomplete_analyzer",
                searchAnalyzer = "autocomplete_search_analyzer")
        }
    )
    private String name;

    @Field(type = FieldType.Text, analyzer = "korean_analyzer")
    private String description;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String brand;

    @Field(type = FieldType.Integer)
    private Integer price;

    @Field(type = FieldType.Integer)
    private Integer discountPrice;

    @Field(type = FieldType.Float)
    private Float rating;

    @Field(type = FieldType.Integer)
    private Integer reviewCount;

    @Field(type = FieldType.Boolean)
    private Boolean inStock;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Date)
    private LocalDate createdAt;

    // Getters and Setters
}
```

### ProductSearchService.java

```java
@Service
public class ProductSearchService {

    private final ElasticsearchOperations operations;

    public ProductSearchService(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    public SearchResult search(SearchRequest request) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // Search keyword
        if (hasText(request.getKeyword())) {
            boolQuery.must(Query.of(q -> q
                .multiMatch(m -> m
                    .query(request.getKeyword())
                    .fields("name^3", "description")
                    .type(TextQueryType.BestFields)
                )
            ));
        }

        // Filters
        if (hasText(request.getCategory())) {
            boolQuery.filter(Query.of(q -> q
                .term(t -> t.field("category").value(request.getCategory()))
            ));
        }

        if (request.getBrands() != null && !request.getBrands().isEmpty()) {
            boolQuery.filter(Query.of(q -> q
                .terms(t -> t
                    .field("brand")
                    .terms(v -> v.value(
                        request.getBrands().stream()
                            .map(FieldValue::of)
                            .toList()
                    ))
                )
            ));
        }

        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            boolQuery.filter(Query.of(q -> q
                .range(r -> {
                    r.field("price");
                    if (request.getMinPrice() != null)
                        r.gte(JsonData.of(request.getMinPrice()));
                    if (request.getMaxPrice() != null)
                        r.lte(JsonData.of(request.getMaxPrice()));
                    return r;
                })
            ));
        }

        // Stock filter
        if (request.isInStockOnly()) {
            boolQuery.filter(Query.of(q -> q
                .term(t -> t.field("in_stock").value(true))
            ));
        }

        // Build query
        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q.bool(boolQuery.build())))
            .withPageable(PageRequest.of(
                request.getPage(),
                request.getSize()
            ))
            .withSort(buildSort(request.getSortBy()))
            .withHighlightQuery(buildHighlight())
            .withAggregation("categories", Aggregation.of(a -> a
                .terms(t -> t.field("category").size(10))
            ))
            .withAggregation("brands", Aggregation.of(a -> a
                .terms(t -> t.field("brand").size(20))
            ))
            .withAggregation("price_ranges", Aggregation.of(a -> a
                .range(r -> r
                    .field("price")
                    .ranges(
                        AggregationRange.of(ar -> ar.to("1000").key("Under $1000")),
                        AggregationRange.of(ar -> ar.from("1000").to("1500").key("$1000-$1500")),
                        AggregationRange.of(ar -> ar.from("1500").to("2000").key("$1500-$2000")),
                        AggregationRange.of(ar -> ar.from("2000").key("$2000+"))
                    )
                )
            ))
            .build();

        SearchHits<Product> hits = operations.search(query, Product.class);

        return SearchResult.builder()
            .products(hits.getSearchHits().stream()
                .map(this::toProductResponse)
                .toList())
            .total(hits.getTotalHits())
            .facets(extractFacets(hits))
            .build();
    }

    public List<String> autocomplete(String prefix) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q
                .match(m -> m
                    .field("name.autocomplete")
                    .query(prefix)
                    .operator(Operator.And)
                )
            ))
            .withPageable(PageRequest.of(0, 5))
            .withSourceFilter(new FetchSourceFilter(
                new String[]{"name"}, null
            ))
            .build();

        SearchHits<Product> hits = operations.search(query, Product.class);

        return hits.getSearchHits().stream()
            .map(h -> h.getContent().getName())
            .distinct()
            .toList();
    }

    private Sort buildSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by(
                Sort.Order.desc("_score"),
                Sort.Order.desc("review_count")
            );
        }
        return switch (sortBy) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "rating" -> Sort.by("rating").descending();
            case "newest" -> Sort.by("created_at").descending();
            default -> Sort.by("_score").descending();
        };
    }

    private HighlightQuery buildHighlight() {
        return new HighlightQuery(
            new Highlight(List.of(
                new HighlightField("name"),
                new HighlightField("description")
            )),
            Product.class
        );
    }

    private ProductResponse toProductResponse(SearchHit<Product> hit) {
        ProductResponse response = new ProductResponse(hit.getContent());
        if (hit.getHighlightFields().containsKey("name")) {
            response.setHighlightedName(
                String.join("", hit.getHighlightField("name"))
            );
        }
        return response;
    }
}
```

### ProductController.java

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductSearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<SearchResult> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> brands,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(defaultValue = "true") boolean inStockOnly,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        SearchRequest request = SearchRequest.builder()
            .keyword(keyword)
            .category(category)
            .brands(brands)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .inStockOnly(inStockOnly)
            .sortBy(sortBy)
            .page(page)
            .size(size)
            .build();

        return ResponseEntity.ok(searchService.search(request));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(
            @RequestParam String q) {
        return ResponseEntity.ok(searchService.autocomplete(q));
    }
}
```

---

## 5. API Testing

### Basic Search

```bash
curl "http://localhost:8080/api/products/search?keyword=MacBook"
```

### With Filters

```bash
curl "http://localhost:8080/api/products/search?keyword=Pro&category=Laptop&brands=Apple&minPrice=1000&maxPrice=3000"
```

### Sorting

```bash
curl "http://localhost:8080/api/products/search?keyword=laptop&sortBy=price_asc"
```

### Autocomplete

```bash
curl "http://localhost:8080/api/products/autocomplete?q=MacBook"
```

---

## 6. Optimization Tips

### Improve Search Quality

```json
// Add synonyms
"filter": {
  "synonym_filter": {
    "type": "synonym",
    "synonyms": [
      "notebook, laptop",
      "cellphone, smartphone, mobile phone"
    ]
  }
}
```

### Search Term Boosting

```json
{
  "function_score": {
    "query": { ... },
    "functions": [
      {
        "filter": { "term": { "tags": "bestseller" } },
        "weight": 1.5
      },
      {
        "field_value_factor": {
          "field": "review_count",
          "factor": 0.0001,
          "modifier": "log1p"
        }
      }
    ]
  }
}
```

---

## Next Steps

| Goal | Recommended Document |
|------|---------------------|
| Improve search quality | [Search Relevance](../../concepts/search-relevance/) |
| Performance optimization | [Performance Tuning](../../concepts/performance-tuning/) |
| Data analysis | [Aggregations](../../concepts/aggregations/) |
