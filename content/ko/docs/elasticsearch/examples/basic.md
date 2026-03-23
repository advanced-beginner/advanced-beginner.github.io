---
title: 기본 예제
description: "Elasticsearch 기본 CRUD와 검색을 단계별로 학습합니다."
weight: 2
lastmod: 2026-01-10
---

{{< callout type="tip" title="TL;DR" >}}
- **Spring Data Elasticsearch**로 Document CRUD 및 기본 검색을 구현합니다
- **Repository 패턴**으로 간단한 쿼리, **ElasticsearchOperations**로 복합 쿼리를 처리합니다
- 필드 타입은 용도에 맞게 선택: `Text`(검색용), `Keyword`(필터용), `Integer`(범위 검색)
- 전체 소요 시간: 약 20분
{{< /callout >}}

Spring Data Elasticsearch를 사용하여 Document CRUD와 기본 검색을 구현합니다.

## 프로젝트 구조

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

## 도메인 클래스

### Product.java

```java
package com.example.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "products")
@Setting(settingPath = "elasticsearch/settings.json")  // 선택사항
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

    // 기본 생성자
    public Product() {}

    // 전체 필드 생성자
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

### 어노테이션 설명

| 어노테이션 | 설명 |
|-----------|------|
| `@Document` | Elasticsearch 인덱스 매핑 |
| `@Id` | 문서 고유 ID 필드 |
| `@Field` | 필드 타입 및 분석기 설정 |
| `@Setting` | 인덱스 설정 파일 경로 |

### 왜 이렇게 설계했나?

**필드 타입 선택 이유:**
- `name`은 `Text` 타입: 형태소 분석이 필요한 검색 대상 필드
- `category`는 `Keyword` 타입: 정확한 일치 필터링용 (분석 불필요)
- `price`는 `Integer` 타입: 범위 검색(`range`) 필요

**대안 비교:**
| 선택 | 대안 | 선택 이유 |
|------|------|----------|
| `String id` | `Long id` | ES 문서 ID는 문자열, UUID 사용 가능 |
| `@Field` 명시 | 자동 매핑 | 타입 명시로 예측 가능한 동작 보장 |
| `standard` analyzer | `nori` | 기본 예제는 단순하게, 한글 검색은 [상품 검색 예제](product-search/) 참조 |

{{< callout type="info" title="핵심 포인트" >}}
- `@Document`로 인덱스명 지정, `@Id`로 문서 ID 필드 지정
- `Text` 타입은 검색용(형태소 분석), `Keyword` 타입은 필터/정렬용
- 필드 타입을 명시적으로 지정하면 예측 가능한 동작을 보장합니다
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

    // 메서드 이름으로 쿼리 생성
    List<Product> findByCategory(String category);

    List<Product> findByNameContaining(String name);

    List<Product> findByPriceBetween(Integer minPrice, Integer maxPrice);

    List<Product> findByCategoryAndInStock(String category, Boolean inStock);

    List<Product> findByNameContainingOrderByPriceAsc(String name);
}
```

### 쿼리 메서드 규칙

| 메서드명 | 생성 쿼리 |
|---------|----------|
| `findByCategory` | `{ "term": { "category": ? } }` |
| `findByNameContaining` | `{ "match": { "name": ? } }` |
| `findByPriceBetween` | `{ "range": { "price": { "gte": ?, "lte": ? } } }` |
| `findByCategoryAndInStock` | `{ "bool": { "must": [...] } }` |

### 왜 ElasticsearchRepository인가?

**Repository 패턴의 장점:**
- JPA와 유사한 방식으로 학습 곡선 낮음
- 메서드 이름만으로 쿼리 자동 생성
- 테스트 용이성 (Mock 가능)

**한계와 대안:**
- 복잡한 쿼리는 `ElasticsearchOperations` 사용 → 아래 Service 참조
- 네이티브 쿼리가 필요하면 `@Query` 어노테이션 사용

{{< callout type="info" title="핵심 포인트" >}}
- `ElasticsearchRepository`를 상속하면 기본 CRUD 메서드가 자동 제공됩니다
- 메서드 이름 규칙(`findByXxx`, `findByXxxContaining`)으로 쿼리가 자동 생성됩니다
- 단순 조회/검색에는 Repository, 복잡한 쿼리에는 `ElasticsearchOperations` 사용
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

    // ========== 기본 검색 ==========

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> findByName(String name) {
        return productRepository.findByNameContaining(name);
    }

    public List<Product> findByPriceRange(Integer min, Integer max) {
        return productRepository.findByPriceBetween(min, max);
    }

    // ========== 복합 검색 (NativeQuery) ==========

    public List<Product> search(String keyword, String category,
                                Integer minPrice, Integer maxPrice,
                                int page, int size) {

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // 키워드 검색 (선택)
        if (keyword != null && !keyword.isBlank()) {
            boolQuery.must(Query.of(q -> q
                .match(m -> m
                    .field("name")
                    .query(keyword)
                )
            ));
        }

        // 카테고리 필터 (선택)
        if (category != null && !category.isBlank()) {
            boolQuery.filter(Query.of(q -> q
                .term(t -> t
                    .field("category")
                    .value(category)
                )
            ));
        }

        // 가격 범위 필터 (선택)
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

        // 재고 있는 상품만
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

{{< callout type="info" title="핵심 포인트" >}}
- `BoolQuery`로 여러 조건을 조합: `must`(필수 매칭), `filter`(필터링, 점수 영향 없음)
- `NativeQuery`로 페이지네이션과 정렬을 설정합니다
- `SearchHits`에서 `getContent()`로 실제 Document 객체를 추출합니다
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

    // ========== 검색 ==========

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

    // ========== 초기 데이터 ==========

    @PostMapping("/init")
    public ResponseEntity<String> initData() {
        List<Product> products = List.of(
            new Product("1", "맥북 프로 14인치", "노트북", 2390000,
                "M3 Pro 칩, 18GB 메모리", true),
            new Product("2", "맥북 에어 13인치", "노트북", 1390000,
                "M3 칩, 8GB 메모리", true),
            new Product("3", "아이패드 프로 11인치", "태블릿", 1499000,
                "M4 칩, 256GB", true),
            new Product("4", "갤럭시북4 프로", "노트북", 1890000,
                "인텔 코어 울트라, 16GB", true),
            new Product("5", "갤럭시 탭 S9", "태블릿", 1199000,
                "스냅드래곤 8 Gen 2", false)
        );

        productService.saveAll(products);
        return ResponseEntity.ok("초기 데이터 생성 완료: " + products.size() + "건");
    }
}
```

---

## API 테스트

### 1. 초기 데이터 생성

```bash
curl -X POST http://localhost:8080/api/products/init
```

### 2. 전체 검색

```bash
curl "http://localhost:8080/api/products/search"
```

### 3. 키워드 검색

```bash
curl "http://localhost:8080/api/products/search?keyword=맥북"
```

### 4. 복합 조건 검색

```bash
curl "http://localhost:8080/api/products/search?keyword=프로&category=노트북&minPrice=1000000&maxPrice=2000000"
```

### 5. 카테고리별 조회

```bash
curl "http://localhost:8080/api/products/category/노트북"
```

### 6. 단건 조회

```bash
curl "http://localhost:8080/api/products/1"
```

### 7. 생성

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "LG 그램 17",
    "category": "노트북",
    "price": 1990000,
    "description": "인텔 13세대, 16GB",
    "inStock": true
  }'
```

### 8. 수정

```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "맥북 프로 14인치",
    "category": "노트북",
    "price": 2290000,
    "description": "M3 Pro 칩, 18GB 메모리 - 할인가",
    "inStock": true
  }'
```

### 9. 삭제

```bash
curl -X DELETE http://localhost:8080/api/products/1
```

{{< callout type="info" title="핵심 포인트" >}}
- REST API 엔드포인트는 표준 CRUD 패턴을 따릅니다 (POST/GET/PUT/DELETE)
- `/init` 엔드포인트로 테스트용 샘플 데이터를 빠르게 생성할 수 있습니다
- 검색 API는 쿼리 파라미터로 필터 조건을 받습니다
{{< /callout >}}

---

## 주요 패턴

### Repository vs ElasticsearchOperations

| 방식 | 장점 | 단점 | 용도 |
|------|------|------|------|
| Repository | 간단, 메서드명으로 쿼리 | 복잡한 쿼리 불가 | 단순 CRUD |
| ElasticsearchOperations | 유연한 쿼리 | 코드 복잡 | 복합 검색 |

### 페이지네이션

```java
// Repository 방식
Page<Product> findByCategory(String category, Pageable pageable);

// 호출
Page<Product> page = repository.findByCategory("노트북",
    PageRequest.of(0, 10, Sort.by("price").ascending()));
```

### 정렬

```java
NativeQuery query = NativeQuery.builder()
    .withQuery(...)
    .withSort(Sort.by(
        Sort.Order.desc("_score"),     // 관련성 점수 내림차순
        Sort.Order.asc("price")        // 가격 오름차순
    ))
    .build();
```

---

## 트러블슈팅

### 인덱스가 생성되지 않음

Spring Data Elasticsearch는 기본적으로 인덱스를 자동 생성합니다.
자동 생성이 안 되면 `@Document`의 `createIndex` 확인:

```java
@Document(indexName = "products", createIndex = true)
```

### Mapping 충돌

기존 인덱스와 새 Mapping이 다르면 에러 발생:

```bash
# 기존 인덱스 삭제 후 재시작
curl -X DELETE http://localhost:9200/products
```

### 한글 검색 안 됨

기본 `standard` analyzer는 한글 형태소 분석을 하지 않습니다.
[상품 검색 시스템](product-search/) 예제에서 Nori 설정을 확인하세요.

{{< callout type="info" title="핵심 포인트" >}}
- 인덱스 자동 생성이 안 되면 `@Document(createIndex = true)` 확인
- Mapping 충돌 시 기존 인덱스를 삭제하고 재시작하세요
- 한글 검색을 위해서는 Nori 분석기 설정이 필수입니다
{{< /callout >}}

---

## 다음 단계

| 목표 | 추천 문서 |
|------|----------|
| 한글 검색 구현 | [상품 검색 시스템](product-search/) |
| 검색 품질 개선 | [검색 관련성](../concepts/search-relevance/) |
| 데이터 분석 | [집계](../concepts/aggregations/) |
