---
title: Basic Examples
weight: 2
lastmod: 2026-01-08
---

Implement Document CRUD and basic search using Spring Data Elasticsearch.

## Project Structure

```
src/main/java/com/example/
├── ElasticsearchApplication.java
├── config/
│   └── ElasticsearchConfig.java
├── domain/
│   └── Product.java
├── repository/
│   └── ProductRepository.java
├── service/
│   └── ProductService.java
└── controller/
    └── ProductController.java
```

---

## Domain Class

### Product.java

```java
package com.example.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "products")
public class Product {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Integer)
    private Integer price;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Boolean)
    private Boolean inStock;

    // Default constructor
    public Product() {}

    // All-args constructor
    public Product(String id, String name, String category,
                   Integer price, String description, Boolean inStock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.inStock = inStock;
    }

    // Getters/Setters...
}
```

### Annotation Explanation

| Annotation | Description |
|------------|-------------|
| `@Document` | Elasticsearch index mapping |
| `@Id` | Document unique ID field |
| `@Field` | Field type and analyzer settings |

### Why This Design?

**Field type choices:**
- `name` is `Text` type: Search target field requiring analysis
- `category` is `Keyword` type: For exact match filtering (no analysis needed)
- `price` is `Integer` type: Range queries needed

---

## Repository

### ProductRepository.java

```java
package com.example.repository;

import com.example.domain.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductRepository extends ElasticsearchRepository<Product, String> {

    // Query creation from method names
    List<Product> findByCategory(String category);

    List<Product> findByNameContaining(String name);

    List<Product> findByPriceBetween(Integer minPrice, Integer maxPrice);

    List<Product> findByCategoryAndInStock(String category, Boolean inStock);
}
```

### Query Method Rules

| Method Name | Generated Query |
|-------------|-----------------|
| `findByCategory` | `{ "term": { "category": ? } }` |
| `findByNameContaining` | `{ "match": { "name": ? } }` |
| `findByPriceBetween` | `{ "range": { "price": { "gte": ?, "lte": ? } } }` |

---

## Service

### ProductService.java

```java
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    // Complex search using NativeQuery
    public List<Product> search(String keyword, String category,
                                Integer minPrice, Integer maxPrice,
                                int page, int size) {

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // Keyword search (optional)
        if (keyword != null && !keyword.isBlank()) {
            boolQuery.must(Query.of(q -> q
                .match(m -> m.field("name").query(keyword))
            ));
        }

        // Category filter (optional)
        if (category != null && !category.isBlank()) {
            boolQuery.filter(Query.of(q -> q
                .term(t -> t.field("category").value(category))
            ));
        }

        // Price range filter (optional)
        if (minPrice != null || maxPrice != null) {
            boolQuery.filter(Query.of(q -> q
                .range(r -> {
                    r.field("price");
                    if (minPrice != null) r.gte(JsonData.of(minPrice));
                    if (maxPrice != null) r.lte(JsonData.of(maxPrice));
                    return r;
                })
            ));
        }

        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q.bool(boolQuery.build())))
            .withPageable(PageRequest.of(page, size))
            .withSort(Sort.by(Sort.Direction.ASC, "price"))
            .build();

        SearchHits<Product> searchHits = elasticsearchOperations.search(query, Product.class);

        return searchHits.getSearchHits().stream()
            .map(SearchHit::getContent)
            .toList();
    }
}
```

---

## API Testing

### 1. Initialize Data

```bash
curl -X POST http://localhost:8080/api/products/init
```

### 2. Keyword Search

```bash
curl "http://localhost:8080/api/products/search?keyword=MacBook"
```

### 3. Complex Search

```bash
curl "http://localhost:8080/api/products/search?keyword=Pro&category=Laptop&minPrice=1000&maxPrice=2000"
```

### 4. Single Item Lookup

```bash
curl "http://localhost:8080/api/products/1"
```

### 5. Create

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "LG Gram 17",
    "category": "Laptop",
    "price": 1990,
    "description": "Intel 13th Gen, 16GB",
    "inStock": true
  }'
```

---

## Key Patterns

### Repository vs ElasticsearchOperations

| Method | Pros | Cons | Use Case |
|--------|------|------|----------|
| Repository | Simple, query from method names | Complex queries not possible | Simple CRUD |
| ElasticsearchOperations | Flexible queries | Complex code | Complex search |

---

## Troubleshooting

### Index Not Created

Spring Data Elasticsearch creates indices automatically by default.
If auto-creation fails, check `createIndex` in `@Document`:

```java
@Document(indexName = "products", createIndex = true)
```

### Mapping Conflict

Error occurs if existing index has different Mapping:

```bash
# Delete existing index and restart
curl -X DELETE http://localhost:9200/products
```

### Korean Search Not Working

Default `standard` analyzer doesn't do Korean morphological analysis.
Check Nori settings in [Product Search System](../product-search/).

---

## Next Steps

| Goal | Recommended Document |
|------|---------------------|
| Implement Korean search | [Product Search System](../product-search/) |
| Improve search quality | [Search Relevance](../../concepts/search-relevance/) |
| Data analysis | [Aggregations](../../concepts/aggregations/) |
