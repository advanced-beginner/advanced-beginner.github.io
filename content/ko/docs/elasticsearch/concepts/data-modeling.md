---
title: 데이터 모델링
description: "Elasticsearch 데이터 모델링의 설계 원칙과 매핑 전략을 설명합니다."
weight: 2
lastmod: 2026-01-15
---

{{< callout type="tip" title="TL;DR" >}}
- **Mapping**: 문서 구조를 정의하는 스키마 (RDB의 테이블 정의와 유사)
- **text**: 풀텍스트 검색용, Analyzer로 토큰화됨
- **keyword**: 정확한 값 매칭, 정렬/집계용
- **Analyzer**: 텍스트를 검색 가능한 토큰으로 변환 (한글은 Nori 사용)
- **비정규화**: JOIN 없으므로 관련 데이터를 한 문서에 포함
{{< /callout >}}

**대상 독자**: Elasticsearch 검색 기능을 사용하려는 개발자
**선수 지식**: [핵심 구성요소](core-components/), JSON 기본 문법

**소요 시간**: 약 25-30분

## 전체 비유: 도서관의 도서 분류 시스템

데이터 모델링을 **도서관의 도서 분류 체계**에 비유하면 이해하기 쉽습니다:

| 도서관 비유 | Elasticsearch | 역할 |
|------------|---------------|------|
| 도서 분류 규칙 (DDC, KDC) | Mapping | 책을 어떻게 분류하고 정리할지 정의 |
| 책 내용으로 검색 | text 타입 | "인공지능"을 검색하면 관련 내용 포함 도서 반환 |
| 청구기호로 정확히 찾기 | keyword 타입 | "005.133-P99" 정확히 일치하는 책 찾기 |
| 색인 담당 사서 | Analyzer | 책 내용을 읽고 핵심 키워드 추출하여 색인 카드 작성 |
| 한 책에 관련 정보 모두 기록 | 비정규화 | 저자, 출판사, 분류 정보를 한 카드에 기재 |
| Object (저자 정보) | 단순 중첩 | 저자명, 생년 등 관련 정보 묶음 |
| Nested (시리즈 정보) | 관계 유지 | 1권-2024년, 2권-2025년 각각 독립적 관계 유지 |

이처럼 Mapping은 도서관에서 "어떤 기준으로 책을 분류하고 색인할 것인가"를 정하는 것과 같습니다.

Elasticsearch에서 데이터를 효과적으로 저장하고 검색하기 위한 Mapping, Field Type, Analyzer 설계를 다룹니다.

## Mapping이란?

**Mapping**은 문서와 필드가 어떻게 저장되고 인덱싱되는지 정의하는 스키마입니다.

### RDB vs Elasticsearch

| RDB | Elasticsearch |
|-----|---------------|
| CREATE TABLE | PUT /index (mapping) |
| 컬럼 타입 | Field Type |
| 스키마 필수 | Dynamic Mapping 가능 |
| ALTER TABLE | 제한적 (재인덱싱 필요) |

### Mapping 예시

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

{{< callout type="info" title="핵심 포인트" >}}
- Mapping은 인덱스 생성 시 정의하며, 이후 필드 타입 변경이 제한적입니다
- Dynamic Mapping으로 자동 타입 추론이 가능하지만, 프로덕션에서는 명시적 정의 권장
- 스키마 변경이 필요하면 재인덱싱(Reindex)이 필요합니다
{{< /callout >}}

---

## Field Types

### 문자열 타입

#### text vs keyword

| 특성 | text | keyword |
|------|------|---------|
| 용도 | 풀텍스트 검색 | 정확한 값 매칭 |
| 분석 | Analyzer로 토큰화 | 분석 안 함 |
| 검색 | match 쿼리 | term 쿼리 |
| 정렬/집계 | 불가 (기본) | 가능 |
| 예시 | 상품 설명, 게시글 본문 | 카테고리, 상태값, ID |

```json
{
  "properties": {
    "title": {
      "type": "text"          // "맥북 프로" → ["맥북", "프로"]
    },
    "category": {
      "type": "keyword"       // "노트북" → "노트북" (그대로)
    }
  }
}
```

#### Multi-field

하나의 필드를 여러 방식으로 인덱싱:

```json
{
  "properties": {
    "name": {
      "type": "text",
      "fields": {
        "keyword": {
          "type": "keyword"   // name.keyword로 접근
        }
      }
    }
  }
}
```

```bash
# 풀텍스트 검색
GET /products/_search
{ "query": { "match": { "name": "맥북" } } }

# 정확한 값 집계
GET /products/_search
{
  "aggs": {
    "names": { "terms": { "field": "name.keyword" } }
  }
}
```

### 숫자 타입

| 타입 | 범위 | 용도 |
|------|------|------|
| `byte` | -128 ~ 127 | 작은 정수 |
| `short` | -32,768 ~ 32,767 | 작은 정수 |
| `integer` | -2³¹ ~ 2³¹-1 | 일반 정수 |
| `long` | -2⁶³ ~ 2⁶³-1 | 큰 정수, ID |
| `float` | 32비트 부동소수점 | 근사값 |
| `double` | 64비트 부동소수점 | 정밀 계산 |
| `scaled_float` | 스케일 적용 | 가격 (scaling_factor: 100) |

```json
{
  "properties": {
    "price": {
      "type": "scaled_float",
      "scaling_factor": 100    // 23900.00 → 2390000으로 저장
    },
    "quantity": {
      "type": "integer"
    }
  }
}
```

### 날짜 타입

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

지원 형식:
- `2024-01-15`
- `2024-01-15T10:30:00`
- `2024-01-15T10:30:00+09:00`
- `1705300200000` (epoch millis)

### 불리언 타입

```json
{
  "properties": {
    "in_stock": {
      "type": "boolean"   // true, false, "true", "false" 모두 허용
    }
  }
}
```

### 복합 타입

#### Object

중첩된 JSON 객체:

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
// 문서
{
  "seller": {
    "name": "공식스토어",
    "rating": 4.8
  }
}

// 검색
GET /products/_search
{
  "query": {
    "match": { "seller.name": "공식스토어" }
  }
}
```

#### Nested

**Object의 문제점:**

```json
// 문서
{
  "options": [
    { "color": "black", "size": "M" },
    { "color": "white", "size": "L" }
  ]
}
```

Object 타입은 배열을 평탄화합니다:
```
options.color: ["black", "white"]
options.size: ["M", "L"]
```

→ "black AND L" 검색 시 잘못된 매칭 발생!

**Nested 타입 사용:**

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
// 정확한 nested 쿼리
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

{{< callout type="info" title="핵심 포인트" >}}
- **text**: 풀텍스트 검색용, match 쿼리 사용
- **keyword**: 정확한 값, 정렬/집계용, term 쿼리 사용
- **Multi-field**: 하나의 필드를 text와 keyword로 동시 인덱싱 가능 (name.keyword)
- **Nested**: 배열 내 객체 간 관계 유지가 필요할 때 사용 (Object는 평탄화됨)
{{< /callout >}}

---

## Analyzer

**Analyzer**는 텍스트를 검색 가능한 토큰으로 변환하는 과정입니다.

### 분석 과정

```mermaid
flowchart LR
    A["입력 텍스트<br>The Quick Brown Fox"]
    --> B["Character Filter<br>(HTML 제거 등)"]
    --> C["Tokenizer<br>(단어 분리)"]
    --> D["Token Filter<br>(소문자 변환 등)"]
    --> E["토큰<br>#91;the, quick, brown, fox#93;"]
```

*다이어그램: 입력 텍스트가 Character Filter, Tokenizer, Token Filter를 거쳐 최종 토큰으로 변환되는 과정입니다.*

### 기본 Analyzer

| Analyzer | 동작 | 예시 결과 |
|----------|------|----------|
| `standard` | 단어 분리 + 소문자 | "Quick Brown" → [quick, brown] |
| `simple` | 문자만 추출 + 소문자 | "Quick-Brown" → [quick, brown] |
| `whitespace` | 공백 기준 분리 | "Quick Brown" → [Quick, Brown] |
| `keyword` | 분석 안 함 | "Quick Brown" → [Quick Brown] |

### Analyzer 테스트

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

### 한글 Analyzer (Nori)

한글은 띄어쓰기만으로 의미 단위 분리가 어렵습니다.

```json
// Standard Analyzer
"삼성전자가 스마트폰을 출시했다"
→ ["삼성전자가", "스마트폰을", "출시했다"]

// Nori Analyzer
"삼성전자가 스마트폰을 출시했다"
→ ["삼성", "전자", "스마트폰", "출시"]
```

#### Nori 설정

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

#### decompound_mode 옵션

| 모드 | "삼성전자" 결과 |
|------|----------------|
| `none` | [삼성전자] |
| `discard` | [삼성, 전자] |
| `mixed` | [삼성전자, 삼성, 전자] |

> **추천:** `mixed` - 복합어와 분리된 단어 모두 검색 가능

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

{{< callout type="info" title="핵심 포인트" >}}
- Analyzer = Character Filter + Tokenizer + Token Filter
- 한글은 Nori Analyzer 사용 권장 (decompound_mode: mixed)
- `/_analyze` API로 분석 결과를 테스트할 수 있습니다
- 동의어(Synonym) 처리는 Custom Analyzer로 설정
{{< /callout >}}

---

## Dynamic Mapping

Mapping을 정의하지 않으면 Elasticsearch가 자동으로 타입을 추론합니다.

### 자동 타입 추론

| JSON 값 | 추론 타입 |
|---------|----------|
| `"hello"` | text + keyword |
| `123` | long |
| `12.34` | float |
| `true` | boolean |
| `"2024-01-15"` | date |
| `{ "a": 1 }` | object |

### Dynamic Mapping 제어

```json
PUT /products
{
  "mappings": {
    "dynamic": "strict",    // false: 무시, strict: 에러
    "properties": {
      "name": { "type": "text" }
    }
  }
}
```

| 설정 | 동작 |
|------|------|
| `true` | 새 필드 자동 추가 (기본값) |
| `false` | 새 필드 저장은 하지만 인덱싱 안 함 |
| `strict` | 새 필드 발견 시 에러 |

> **프로덕션 권장:** `strict` 또는 명시적 Mapping 정의

{{< callout type="info" title="핵심 포인트" >}}
- Dynamic Mapping은 개발 시 편리하지만, 프로덕션에서는 예기치 않은 타입 추론 위험
- `dynamic: strict`로 설정하면 정의되지 않은 필드 입력 시 에러 발생
- `dynamic: false`는 새 필드를 저장하지만 인덱싱하지 않음 (검색 불가)
{{< /callout >}}

---

## 모델링 패턴

### 패턴 1: 비정규화

Elasticsearch는 JOIN을 지원하지 않으므로, 관련 데이터를 한 문서에 포함합니다.

```json
// RDB 정규화 (2개 테이블)
// products: id, name, category_id
// categories: id, name

// Elasticsearch 비정규화 (1개 문서)
{
  "name": "맥북 프로",
  "category": {
    "id": 1,
    "name": "노트북"
  }
}
```

**장점:** 빠른 검색, 단순한 쿼리
**단점:** 카테고리 변경 시 모든 문서 업데이트 필요

### 패턴 2: Application-Side Join

자주 변경되는 데이터는 별도 인덱스로 관리:

```java
// 1. 상품 검색
List<Product> products = productRepository.search(query);

// 2. 재고 정보 조회 (별도 인덱스)
List<String> productIds = products.stream().map(Product::getId).toList();
Map<String, Stock> stocks = stockRepository.findByIds(productIds);

// 3. 조합
products.forEach(p -> p.setStock(stocks.get(p.getId())));
```

### 패턴 3: Nested vs Parent-Child

| 특성 | Nested | Parent-Child (Join) |
|------|--------|---------------------|
| 성능 | 빠름 | 느림 |
| 업데이트 | 전체 문서 재인덱싱 | 자식만 업데이트 |
| 쿼리 복잡도 | 낮음 | 높음 |
| 권장 상황 | 변경 적은 관계 | 변경 잦은 1:N |

{{< callout type="info" title="핵심 포인트" >}}
- Elasticsearch는 JOIN을 지원하지 않으므로 비정규화가 기본 전략
- 자주 변경되는 데이터는 Application-Side Join 고려
- Nested는 성능이 좋지만 전체 문서 재인덱싱 필요, Parent-Child는 개별 업데이트 가능
{{< /callout >}}

---

## 모범 사례

### 1. 검색 필드는 text, 필터/집계 필드는 keyword

```json
{
  "name": {
    "type": "text",
    "fields": { "keyword": { "type": "keyword" } }
  },
  "status": { "type": "keyword" }
}
```

### 2. 숫자 ID는 keyword

```json
{
  "user_id": { "type": "keyword" }  // long 아님!
}
```
숫자 범위 검색이 없다면 keyword가 더 효율적입니다.

### 3. 불필요한 필드는 인덱싱 제외

```json
{
  "raw_data": {
    "type": "object",
    "enabled": false    // 저장만, 검색 불가
  }
}
```

### 4. 인덱스 템플릿 활용

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

{{< callout type="info" title="핵심 포인트" >}}
- 검색용 필드는 text + keyword Multi-field로 설정
- 숫자 ID도 범위 검색이 없으면 keyword가 효율적
- 검색하지 않는 필드는 `enabled: false`로 인덱싱 제외
- 인덱스 템플릿으로 일관된 Mapping 적용
{{< /callout >}}

---

## 다음 단계

| 목표 | 추천 문서 |
|------|----------|
| 검색 쿼리 작성 | [Query DSL](query-dsl/) |
| 검색 품질 개선 | [검색 관련성](search-relevance/) |
| 실습 | [기본 예제](../examples/basic/) |
