---
title: Basic Examples
weight: 2
lastmod: 2026-01-10
---

{{< callout type="tip" title="TL;DR" >}}
- Implement Document CRUD and basic search using **Spring Data Elasticsearch**
- Handle simple queries with **Repository pattern**, complex queries with **ElasticsearchOperations**
- Choose field types based on purpose: `Text` (for search), `Keyword` (for filtering), `Integer` (for range queries)
- Total time required: approximately 20 minutes
{{< /callout >}}

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
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "products")
@Setting(settingPath = "elasticsearch/settings.json")  // Optional
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

    // Getter/Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getInStock() { return inStock; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }
}
```

### Annotation Explanation

| Annotation | Description |
|------------|-------------|
| `@Document` | Elasticsearch index mapping |
| `@Id` | Document unique ID field |
| `@Field` | Field type and analyzer settings |
| `@Setting` | Index settings file path |

### Why This Design?

**Field type selection rationale:**
- `name` is `Text` type: Search target field requiring morphological analysis
- `category` is `Keyword` type: For exact match filtering (no analysis needed)
- `price` is `Integer` type: Requires range queries (`range`)

**Comparison with alternatives:**
| Choice | Alternative | Rationale |
|--------|-------------|-----------|
| `String id` | `Long id` | ES document IDs are strings, UUID compatible |
| Explicit `@Field` | Auto mapping | Explicit types ensure predictable behavior |
| `standard` analyzer | `nori` | Basic example kept simple, see [Product Search](product-search/) for Korean search |

{{< callout type="info" title="Key Points" >}}
- Specify index name with `@Document`, document ID field with `@Id`
- `Text` type is for search (morphological analysis), `Keyword` type is for filtering/sorting
- Explicitly specifying field types ensures predictable behavior
{{< /callout >}}

---

## Repository

### ProductRepository.java

```java
package com.example.repository;

import com.example.domain.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends ElasticsearchRepository<Product, String> {

    // Query generation from method names
    List<Product> findByCategory(String category);

    List<Product> findByNameContaining(String name);

    List<Product> findByPriceBetween(Integer minPrice, Integer maxPrice);

    List<Product> findByCategoryAndInStock(String category, Boolean inStock);

    List<Product> findByNameContainingOrderByPriceAsc(String name);
}
```

### Query Method Rules

| Method Name | Generated Query |
|-------------|-----------------|
| `findByCategory` | `{ "term": { "category": ? } }` |
| `findByNameContaining` | `{ "match": { "name": ? } }` |
| `findByPriceBetween` | `{ "range": { "price": { "gte": ?, "lte": ? } } }` |
| `findByCategoryAndInStock` | `{ "bool": { "must": [...] } }` |

### Why ElasticsearchRepository?

**Repository pattern advantages:**
- Low learning curve with JPA-like approach
- Automatic query generation from method names
- Easy to test (mockable)

**Limitations and alternatives:**
- Use `ElasticsearchOperations` for complex queries → see Service below
- Use `@Query` annotation for native queries

{{< callout type="info" title="Key Points" >}}
- Extending `ElasticsearchRepository` provides basic CRUD methods automatically
- Queries are auto-generated from method name conventions (`findByXxx`, `findByXxxContaining`)
- Use Repository for simple queries, `ElasticsearchOperations` for complex queries
{{< /callout >}}

---

## Service

### ProductService.java

```java
package com.example.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.domain.Product;
import com.example.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public ProductService(ProductRepository productRepository,
                         ElasticsearchOperations elasticsearchOperations) {
        this.productRepository = productRepository;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    // ========== CRUD ==========

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public List<Product> saveAll(List<Product> products) {
        return (List<Product>) productRepository.saveAll(products);
    }

    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

    public void deleteById(String id) {
        productRepository.deleteById(id);
    }

    public void deleteAll() {
        productRepository.deleteAll();
    }

    // ========== Basic Search ==========

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> findByName(String name) {
        return productRepository.findByNameContaining(name);
    }

    public List<Product> findByPriceRange(Integer min, Integer max) {
        return productRepository.findByPriceBetween(min, max);
    }

    // ========== Complex Search (NativeQuery) ==========

    public List<Product> search(String keyword, String category,
                                Integer minPrice, Integer maxPrice,
                                int page, int size) {

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // Keyword search (optional)
        if (keyword != null && !keyword.isBlank()) {
            boolQuery.must(Query.of(q -> q
                .match(m -> m
                    .field("name")
                    .query(keyword)
                )
            ));
        }

        // Category filter (optional)
        if (category != null && !category.isBlank()) {
            boolQuery.filter(Query.of(q -> q
                .term(t -> t
                    .field("category")
                    .value(category)
                )
            ));
        }

        // Price range filter (optional)
        if (minPrice != null || maxPrice != null) {
            boolQuery.filter(Query.of(q -> q
                .range(r -> {
                    r.field("price");
                    if (minPrice != null) r.gte(co.elastic.clients.json.JsonData.of(minPrice));
                    if (maxPrice != null) r.lte(co.elastic.clients.json.JsonData.of(maxPrice));
                    return r;
                })
            ));
        }

        // In-stock items only
        boolQuery.filter(Query.of(q -> q
            .term(t -> t
                .field("inStock")
                .value(true)
            )
        ));

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

{{< callout type="info" title="Key Points" >}}
- Combine multiple conditions with `BoolQuery`: `must` (affects scoring), `filter` (no scoring, cacheable)
- Set pagination and sorting with `NativeQuery`
- Extract actual Document objects with `getContent()` from `SearchHits`
{{< /callout >}}

---

## Controller

### ProductController.java

```java
package com.example.controller;

import com.example.domain.Product;
import com.example.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ========== CRUD ==========

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.ok(productService.save(product));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable String id) {
        return productService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable String id,
                                          @RequestBody Product product) {
        product.setId(id);
        return ResponseEntity.ok(productService.save(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ========== Search ==========

    @GetMapping("/search")
    public ResponseEntity<List<Product>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<Product> results = productService.search(
            keyword, category, minPrice, maxPrice, page, size
        );
        return ResponseEntity.ok(results);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.findByCategory(category));
    }

    // ========== Initial Data ==========

    @PostMapping("/init")
    public ResponseEntity<String> initData() {
        List<Product> products = List.of(
            new Product("1", "MacBook Pro 14-inch", "Laptop", 2390000,
                "M3 Pro chip, 18GB memory", true),
            new Product("2", "MacBook Air 13-inch", "Laptop", 1390000,
                "M3 chip, 8GB memory", true),
            new Product("3", "iPad Pro 11-inch", "Tablet", 1499000,
                "M4 chip, 256GB", true),
            new Product("4", "Galaxy Book4 Pro", "Laptop", 1890000,
                "Intel Core Ultra, 16GB", true),
            new Product("5", "Galaxy Tab S9", "Tablet", 1199000,
                "Snapdragon 8 Gen 2", false)
        );

        productService.saveAll(products);
        return ResponseEntity.ok("Initial data created: " + products.size() + " items");
    }
}
```

---

## API Testing

### 1. Initialize Data

```bash
curl -X POST http://localhost:8080/api/products/init
```

### 2. Search All

```bash
curl "http://localhost:8080/api/products/search"
```

### 3. Keyword Search

```bash
curl "http://localhost:8080/api/products/search?keyword=MacBook"
```

### 4. Complex Search

```bash
curl "http://localhost:8080/api/products/search?keyword=Pro&category=Laptop&minPrice=1000000&maxPrice=2000000"
```

### 5. Category Lookup

```bash
curl "http://localhost:8080/api/products/category/Laptop"
```

### 6. Single Item Lookup

```bash
curl "http://localhost:8080/api/products/1"
```

### 7. Create

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "LG Gram 17",
    "category": "Laptop",
    "price": 1990000,
    "description": "Intel 13th Gen, 16GB",
    "inStock": true
  }'
```

### 8. Update

```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MacBook Pro 14-inch",
    "category": "Laptop",
    "price": 2290000,
    "description": "M3 Pro chip, 18GB memory - Sale price",
    "inStock": true
  }'
```

### 9. Delete

```bash
curl -X DELETE http://localhost:8080/api/products/1
```

{{< callout type="info" title="Key Points" >}}
- REST API endpoints follow standard CRUD patterns (POST/GET/PUT/DELETE)
- The `/init` endpoint quickly generates test sample data
- Search API accepts filter conditions via query parameters
{{< /callout >}}

---

## Key Patterns

### Repository vs ElasticsearchOperations

| Method | Pros | Cons | Use Case |
|--------|------|------|----------|
| Repository | Simple, query from method names | Complex queries not possible | Simple CRUD |
| ElasticsearchOperations | Flexible queries | Complex code | Complex search |

### Pagination

```java
// Repository approach
Page<Product> findByCategory(String category, Pageable pageable);

// Usage
Page<Product> page = repository.findByCategory("Laptop",
    PageRequest.of(0, 10, Sort.by("price").ascending()));
```

### Sorting

```java
NativeQuery query = NativeQuery.builder()
    .withQuery(...)
    .withSort(Sort.by(
        Sort.Order.desc("_score"),     // Relevance score descending
        Sort.Order.asc("price")        // Price ascending
    ))
    .build();
```

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

The default `standard` analyzer doesn't perform Korean morphological analysis.
Check Nori settings in [Product Search System](product-search/) example.

{{< callout type="info" title="Key Points" >}}
- If auto index creation fails, verify `@Document(createIndex = true)`
- For mapping conflicts, delete existing index and restart
- Nori analyzer configuration is essential for Korean search
{{< /callout >}}

---

## Next Steps

| Goal | Recommended Document |
|------|---------------------|
| Implement Korean search | [Product Search System](product-search/) |
| Improve search quality | [Search Relevance](../concepts/search-relevance/) |
| Data analysis | [Aggregations](../concepts/aggregations/) |
