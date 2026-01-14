---
title: Vector Search (kNN)
weight: 10
lastmod: 2026-01-10
---

{{< callout type="tip" title="TL;DR" >}}
- **Vector Search**: 의미(Semantic) 기반 검색으로 키워드가 달라도 유사한 내용 검색 가능
- **dense_vector**: 벡터를 저장하는 필드 타입, similarity로 유사도 계산 방식 지정
- **kNN 쿼리**: k개의 가장 가까운 이웃 문서를 찾는 검색 방식
- **하이브리드 검색**: kNN + 키워드 검색을 결합하여 최상의 결과 제공
- **임베딩 모델**: 텍스트/이미지를 벡터로 변환 (한글은 다국어 모델 권장)
{{< /callout >}}

**대상 독자**: 시맨틱 검색이나 추천 시스템을 구현하려는 개발자
**선수 지식**: [핵심 구성요소](core-components/), [Query DSL](query-dsl/), 기본적인 ML 개념

Elasticsearch의 벡터 검색(kNN)을 사용하여 시맨틱 검색과 유사 이미지 검색을 구현하는 방법을 배웁니다.

{{% notice style="info" title="버전 요구사항" %}}
- **Elasticsearch 8.0+** 필수 (네이티브 kNN 지원)
- 8.x 이전 버전은 script_score나 플러그인 필요
{{% /notice %}}

## Vector Search란?

기존 검색은 **키워드 매칭**입니다. "강아지"를 검색하면 "강아지"가 포함된 문서만 찾습니다.

**Vector Search**는 **의미(Semantic)** 기반 검색입니다:
- "강아지" 검색 → "퍼피", "반려견", "개"도 찾음
- 이미지 검색 → 비슷한 이미지 찾기
- 추천 시스템 → 유사한 상품 추천

### 동작 원리

```mermaid
flowchart LR
    A["텍스트/이미지"] --> B["임베딩 모델"]
    B --> C["벡터 변환"]
    C --> D["Elasticsearch 저장"]

    E["검색어"] --> F["임베딩 모델"]
    F --> G["쿼리 벡터"]
    G --> H["kNN 검색"]
    D --> H
    H --> I["유사 문서 반환"]
```

*다이어그램: 텍스트/이미지가 임베딩 모델을 통해 벡터로 변환되어 Elasticsearch에 저장되고, 검색어도 같은 방식으로 벡터화되어 kNN으로 유사 문서를 찾는 흐름입니다.*

1. **임베딩(Embedding)**: 텍스트/이미지를 고차원 벡터로 변환
2. **저장**: 벡터를 Elasticsearch dense_vector 필드에 저장
3. **검색**: 쿼리 벡터와 가장 가까운 문서를 kNN 알고리즘으로 검색

{{< callout type="info" title="핵심 포인트" >}}
- Vector Search는 키워드가 아닌 "의미"로 검색하므로 동의어, 유사 표현도 찾습니다
- 임베딩 모델이 텍스트를 벡터로 변환하며, 모델 선택이 검색 품질에 중요합니다
- 검색 시 쿼리도 같은 모델로 벡터화해야 합니다
{{< /callout >}}

---

## 인덱스 설정

### dense_vector 필드 정의

```json
PUT /products-vector
{
  "mappings": {
    "properties": {
      "name": {
        "type": "text"
      },
      "description": {
        "type": "text"
      },
      "description_vector": {
        "type": "dense_vector",
        "dims": 384,
        "index": true,
        "similarity": "cosine"
      }
    }
  }
}
```

### 주요 설정

| 옵션 | 설명 | 권장값 |
|------|------|--------|
| `dims` | 벡터 차원수 | 모델에 따라 다름 (384, 768, 1536 등) |
| `index` | kNN 인덱스 생성 | `true` (검색용) |
| `similarity` | 유사도 계산 방식 | `cosine` (정규화된 벡터), `dot_product`, `l2_norm` |

### similarity 옵션

| 방식 | 설명 | 사용 시점 |
|------|------|----------|
| `cosine` | 코사인 유사도 | 대부분의 텍스트 임베딩 (기본값) |
| `dot_product` | 내적 | 이미 정규화된 벡터 (빠름) |
| `l2_norm` | 유클리드 거리 | 거리 기반 유사도 |

{{< callout type="info" title="핵심 포인트" >}}
- `dims`는 사용하는 임베딩 모델의 차원수와 일치해야 합니다
- `index: true`로 설정해야 kNN 검색이 가능합니다
- 대부분의 텍스트 임베딩에는 `similarity: cosine` 사용
{{< /callout >}}

---

## 문서 인덱싱

### 임베딩 생성 (Python 예시)

```python
from sentence_transformers import SentenceTransformer

model = SentenceTransformer('sentence-transformers/all-MiniLM-L6-v2')

text = "Apple M3 Pro 칩이 탑재된 맥북 프로 14인치"
vector = model.encode(text).tolist()  # 384차원 벡터
```

### Elasticsearch에 저장

```json
PUT /products-vector/_doc/1
{
  "name": "맥북 프로 14인치",
  "description": "Apple M3 Pro 칩이 탑재된 프리미엄 노트북",
  "description_vector": [0.12, -0.34, 0.56, ...]  // 384개 float
}
```

### Bulk 인덱싱

```json
POST /_bulk
{"index": {"_index": "products-vector", "_id": "1"}}
{"name": "맥북 프로", "description_vector": [0.12, -0.34, ...]}
{"index": {"_index": "products-vector", "_id": "2"}}
{"name": "갤럭시북", "description_vector": [0.08, -0.21, ...]}
```

{{< callout type="info" title="핵심 포인트" >}}
- 임베딩은 인덱싱 전에 외부 서비스나 라이브러리로 생성해야 합니다
- 벡터 필드에는 정확히 dims 개수만큼의 float 값이 필요합니다
- 대량 인덱싱 시 Bulk API 사용 권장
{{< /callout >}}

---

## kNN 검색

### 기본 kNN 쿼리

```json
GET /products-vector/_search
{
  "knn": {
    "field": "description_vector",
    "query_vector": [0.15, -0.30, 0.52, ...],  // 검색 벡터
    "k": 10,
    "num_candidates": 100
  }
}
```

| 파라미터 | 설명 |
|----------|------|
| `k` | 반환할 최근접 이웃 수 |
| `num_candidates` | 각 샤드에서 후보로 고려할 문서 수 (정확도↑ = 성능↓) |

### kNN + 필터 결합

```json
GET /products-vector/_search
{
  "knn": {
    "field": "description_vector",
    "query_vector": [0.15, -0.30, ...],
    "k": 10,
    "num_candidates": 100,
    "filter": {
      "bool": {
        "must": [
          { "term": { "category": "노트북" } },
          { "range": { "price": { "lte": 2000000 } } }
        ]
      }
    }
  }
}
```

### kNN + 키워드 검색 하이브리드

```json
GET /products-vector/_search
{
  "query": {
    "bool": {
      "should": [
        {
          "match": {
            "name": {
              "query": "맥북",
              "boost": 0.3
            }
          }
        }
      ]
    }
  },
  "knn": {
    "field": "description_vector",
    "query_vector": [0.15, -0.30, ...],
    "k": 10,
    "num_candidates": 100,
    "boost": 0.7
  }
}
```

> **하이브리드 검색**: 키워드 매칭(정확도)과 의미 검색(관련성)을 결합하여 최상의 결과 제공

{{< callout type="info" title="핵심 포인트" >}}
- `k`: 반환할 결과 수, `num_candidates`: 정확도와 속도의 트레이드오프
- `filter`로 kNN 검색 전에 조건 필터링 가능
- 하이브리드 검색으로 키워드 + 의미 검색 결합 시 `boost`로 가중치 조절
{{< /callout >}}

---

## Spring Boot 구현

### Product.java

```java
@Document(indexName = "products-vector")
public class Product {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Dense_Vector, dims = 384)
    private float[] descriptionVector;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Integer)
    private Integer price;

    // getters, setters
}
```

### VectorSearchService.java

```java
@Service
public class VectorSearchService {

    private final ElasticsearchOperations operations;
    private final EmbeddingService embeddingService;

    /**
     * 시맨틱 검색
     */
    public List<Product> semanticSearch(String query, int k) {
        // 1. 쿼리를 벡터로 변환
        float[] queryVector = embeddingService.embed(query);

        // 2. kNN 검색
        NativeQuery nativeQuery = NativeQuery.builder()
            .withKnnQuery(KnnQuery.builder()
                .field("description_vector")
                .queryVector(queryVector)
                .k(k)
                .numCandidates(100)
                .build()
            )
            .build();

        return operations.search(nativeQuery, Product.class)
            .getSearchHits().stream()
            .map(SearchHit::getContent)
            .toList();
    }

    /**
     * 하이브리드 검색 (kNN + 키워드)
     */
    public List<Product> hybridSearch(String query, int k) {
        float[] queryVector = embeddingService.embed(query);

        NativeQuery nativeQuery = NativeQuery.builder()
            .withQuery(Query.of(q -> q
                .bool(b -> b
                    .should(Query.of(sq -> sq
                        .match(m -> m
                            .field("name")
                            .query(query)
                            .boost(0.3f)
                        )
                    ))
                )
            ))
            .withKnnQuery(KnnQuery.builder()
                .field("description_vector")
                .queryVector(queryVector)
                .k(k)
                .numCandidates(100)
                .boost(0.7f)
                .build()
            )
            .build();

        return operations.search(nativeQuery, Product.class)
            .getSearchHits().stream()
            .map(SearchHit::getContent)
            .toList();
    }

    /**
     * 유사 상품 추천
     */
    public List<Product> findSimilar(String productId, int k) {
        // 기준 상품의 벡터 조회
        Product product = operations.get(productId, Product.class);
        if (product == null || product.getDescriptionVector() == null) {
            return List.of();
        }

        NativeQuery nativeQuery = NativeQuery.builder()
            .withKnnQuery(KnnQuery.builder()
                .field("description_vector")
                .queryVector(product.getDescriptionVector())
                .k(k + 1)  // 자기 자신 제외
                .numCandidates(100)
                .build()
            )
            .build();

        return operations.search(nativeQuery, Product.class)
            .getSearchHits().stream()
            .map(SearchHit::getContent)
            .filter(p -> !p.getId().equals(productId))  // 자기 자신 제외
            .limit(k)
            .toList();
    }
}
```

### EmbeddingService.java

```java
@Service
public class EmbeddingService {

    private final RestTemplate restTemplate;

    // 외부 임베딩 API 호출 (예: OpenAI, HuggingFace)
    public float[] embed(String text) {
        EmbeddingRequest request = new EmbeddingRequest(text);
        EmbeddingResponse response = restTemplate.postForObject(
            "http://embedding-service/embed",
            request,
            EmbeddingResponse.class
        );
        return response.getVector();
    }

    // 배치 임베딩
    public List<float[]> embedBatch(List<String> texts) {
        // ... 배치 처리
    }
}
```

{{< callout type="info" title="핵심 포인트" >}}
- 검색 시 쿼리를 벡터로 변환하는 EmbeddingService가 필요합니다
- 유사 상품 추천은 기존 상품의 벡터를 쿼리 벡터로 사용
- Spring Data Elasticsearch의 `@Field(type = FieldType.Dense_Vector)`로 매핑
{{< /callout >}}

---

## 임베딩 모델 선택

| 모델 | 차원 | 특징 | 용도 |
|------|------|------|------|
| `all-MiniLM-L6-v2` | 384 | 빠름, 가벼움 | 일반 텍스트 |
| `all-mpnet-base-v2` | 768 | 높은 품질 | 정밀 검색 |
| `text-embedding-ada-002` (OpenAI) | 1536 | 최고 품질 | 프로덕션 |
| `multilingual-e5-large` | 1024 | 다국어 지원 | 한글 검색 |

> **한글 검색 팁**: 다국어 모델(`multilingual-e5-*`)이나 한국어 특화 모델 사용 권장

{{< callout type="info" title="핵심 포인트" >}}
- 모델 선택 시 차원수, 품질, 속도, 비용을 고려하세요
- 한글 검색에는 다국어 모델(multilingual-e5-*) 또는 한국어 특화 모델 권장
- OpenAI API(text-embedding-ada-002)는 품질이 높지만 비용 발생
{{< /callout >}}

---

## 성능 최적화

### 인덱싱 성능

```json
PUT /products-vector
{
  "mappings": {
    "properties": {
      "description_vector": {
        "type": "dense_vector",
        "dims": 384,
        "index": true,
        "similarity": "dot_product",
        "index_options": {
          "type": "hnsw",
          "m": 16,
          "ef_construction": 100
        }
      }
    }
  }
}
```

| HNSW 파라미터 | 설명 | 트레이드오프 |
|---------------|------|-------------|
| `m` | 노드당 연결 수 | 높을수록 정확↑, 메모리↑ |
| `ef_construction` | 인덱스 구축 시 탐색 범위 | 높을수록 정확↑, 인덱싱 속도↓ |

### 검색 성능

```json
{
  "knn": {
    "field": "description_vector",
    "query_vector": [...],
    "k": 10,
    "num_candidates": 50  // 정확도 vs 속도 조절
  }
}
```

- `num_candidates` 낮춤 → 빠르지만 덜 정확
- `num_candidates` 높임 → 정확하지만 느림

{{< callout type="info" title="핵심 포인트" >}}
- HNSW 파라미터(m, ef_construction)로 인덱싱 정확도와 속도 조절
- `num_candidates`로 검색 정확도와 속도의 트레이드오프 조절
- 정규화된 벡터는 `dot_product`가 `cosine`보다 빠름
{{< /callout >}}

---

## 사용 사례

### 1. 시맨틱 검색

"가벼운 업무용 노트북" 검색 → 무게, 배터리, 성능 관련 상품 반환

### 2. 유사 상품 추천

상품 상세 페이지에서 "비슷한 상품" 표시

### 3. 이미지 검색

이미지 임베딩 → 비슷한 이미지 찾기 (패션, 인테리어)

### 4. FAQ 봇

질문 임베딩 → 가장 유사한 FAQ 답변 반환

{{< callout type="info" title="핵심 포인트" >}}
- 시맨틱 검색: 자연어 질의로 의미 기반 검색
- 유사 상품/이미지 추천: 기존 아이템의 벡터로 유사 아이템 검색
- FAQ/챗봇: 질문을 벡터화하여 가장 유사한 답변 반환
{{< /callout >}}

---

## 다음 단계

| 목표 | 추천 문서 |
|------|----------|
| 검색 품질 개선 | [검색 관련성](search-relevance/) |
| 기본 검색 | [Query DSL](query-dsl/) |
| 성능 최적화 | [성능 튜닝](performance-tuning/) |
