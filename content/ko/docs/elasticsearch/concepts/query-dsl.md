---
title: Query DSL
description: "Query DSL의 작동 원리와 쿼리 작성 방법을 설명합니다."
weight: 3
lastmod: 2026-01-15
---

{{< callout type="tip" title="TL;DR" >}}
- **Query Context**: 관련성 점수(score)를 계산하는 풀텍스트 검색
- **Filter Context**: 점수 없이 조건만 확인, 캐싱되어 빠름
- **match/match_phrase**: 풀텍스트 검색에 사용 (text 필드)
- **term/terms/range**: 정확한 값 검색에 사용 (keyword 필드)
- **bool**: must/should/must_not/filter로 쿼리 조합
{{< /callout >}}

**대상 독자**: Elasticsearch 기본 개념을 이해한 개발자
**선수 지식**: [핵심 구성요소](core-components/), JSON 기본 문법

**소요 시간**: 약 25-30분

## 전체 비유: 도서관에서 책 찾기

Query DSL을 **도서관에서 책을 찾는 방법**에 비유하면 이해하기 쉽습니다:

| 도서관 비유 | Query DSL | 역할 |
|------------|-----------|------|
| "인공지능 관련 책 찾아주세요" | Query Context (match) | 내용 관련성으로 추천, 관련도 순 정렬 |
| "005.133 분류 책만 보여주세요" | Filter Context (term) | 조건 일치 여부만 확인, 빠름 |
| "머신러닝 입문" 내용 검색 | match | 단어 포함 여부로 검색 (순서 무관) |
| "머신러닝 입문" 정확히 | match_phrase | 정확한 문구 순서로 검색 |
| 청구기호 "005.133" 정확히 | term | 값이 정확히 일치하는 것만 |
| 2020~2024년 출판 도서 | range | 범위 내 값 검색 |
| 컴퓨터 분야 AND 2024년 출판 | bool (must) | 모든 조건 만족 |
| 프로그래밍 OR 데이터분석 | bool (should) | 하나 이상 만족 |
| 번역서 제외 | bool (must_not) | 해당 조건 제외 |

이처럼 Query DSL은 도서관 사서에게 "어떤 조건으로 책을 찾아달라"고 요청하는 것과 같습니다.

Elasticsearch의 Query DSL(Domain Specific Language)을 사용하여 다양한 검색 쿼리를 작성하는 방법을 배웁니다.

## 쿼리 기본 구조

```json
GET /products/_search
{
  "query": {
    // 검색 조건
  },
  "from": 0,           // 페이지네이션 시작점
  "size": 10,          // 반환할 문서 수
  "sort": [            // 정렬
    { "price": "asc" }
  ],
  "_source": ["name", "price"]  // 반환할 필드
}
```

---

## Query Context vs Filter Context

Elasticsearch 쿼리는 두 가지 맥락에서 실행됩니다.

| 구분 | Query Context | Filter Context |
|------|---------------|----------------|
| 질문 | "얼마나 잘 일치하나?" | "일치하나 안 하나?" |
| Score | 계산함 (관련성 점수) | 계산 안 함 |
| 캐싱 | 안 됨 | 됨 |
| 용도 | 풀텍스트 검색 | 정확한 값 필터링 |

```json
GET /products/_search
{
  "query": {
    "bool": {
      "must": [
        { "match": { "name": "맥북" } }      // Query context (score 계산)
      ],
      "filter": [
        { "term": { "category": "노트북" } }, // Filter context (캐싱)
        { "range": { "price": { "lte": 2000000 } } }
      ]
    }
  }
}
```

> **성능 팁:** 정확한 값 비교는 `filter`에 넣어 캐싱 효과를 얻으세요.

{{< callout type="info" title="핵심 포인트" >}}
- Query Context는 "얼마나 잘 일치하는가"를 계산하여 score를 부여합니다
- Filter Context는 "일치하는가/아닌가"만 판단하고 캐싱됩니다
- 정확한 값 필터링(category, status 등)은 filter에 넣어 성능을 높이세요
{{< /callout >}}

---

## 풀텍스트 쿼리

### match

가장 일반적인 풀텍스트 검색입니다.

```json
GET /products/_search
{
  "query": {
    "match": {
      "name": "맥북 프로"
    }
  }
}
```

"맥북 프로" → Analyzer → ["맥북", "프로"]
→ 둘 중 하나라도 포함된 문서 검색 (OR)

#### operator 옵션

```json
{
  "match": {
    "name": {
      "query": "맥북 프로",
      "operator": "and"    // 둘 다 포함 (기본값: or)
    }
  }
}
```

#### minimum_should_match

```json
{
  "match": {
    "name": {
      "query": "맥북 프로 14인치",
      "minimum_should_match": "2"  // 3개 중 2개 이상 일치
    }
  }
}
```

### match_phrase

단어 순서까지 일치해야 합니다.

```json
GET /products/_search
{
  "query": {
    "match_phrase": {
      "description": "M3 Pro 칩"
    }
  }
}
```

- ✅ "M3 Pro 칩 탑재"
- ❌ "M3 칩과 Pro 디스플레이"

#### slop 옵션

```json
{
  "match_phrase": {
    "description": {
      "query": "M3 칩",
      "slop": 1          // 단어 사이 1개까지 허용
    }
  }
}
```
→ "M3 Pro 칩"도 매칭

### multi_match

여러 필드에서 동시 검색:

```json
GET /products/_search
{
  "query": {
    "multi_match": {
      "query": "맥북",
      "fields": ["name^2", "description"]  // name에 가중치 2배
    }
  }
}
```

#### type 옵션

| type | 동작 |
|------|------|
| `best_fields` | 가장 높은 점수의 필드 사용 (기본값) |
| `most_fields` | 모든 필드 점수 합산 |
| `cross_fields` | 여러 필드를 하나처럼 취급 |
| `phrase` | match_phrase로 검색 |

{{< callout type="info" title="핵심 포인트" >}}
- `match`: 기본 풀텍스트 검색, OR 조건 (operator로 AND 변경 가능)
- `match_phrase`: 단어 순서까지 일치해야 함, slop으로 허용 간격 조절
- `multi_match`: 여러 필드 동시 검색, 필드별 가중치(^) 설정 가능
{{< /callout >}}

---

## Term Level 쿼리

분석(Analyze) 과정 없이 정확한 값을 검색합니다.

### term

정확히 일치하는 값 검색:

```json
GET /products/_search
{
  "query": {
    "term": {
      "category": "노트북"
    }
  }
}
```

> **주의:** `text` 필드에는 사용하지 마세요. 분석된 토큰과 비교되어 의도대로 동작하지 않습니다.

```json
// ❌ 잘못된 사용
{ "term": { "name": "맥북 프로" } }  // name이 text 타입이면 안 됨

// ✅ 올바른 사용
{ "term": { "name.keyword": "맥북 프로" } }  // keyword 필드 사용
{ "term": { "category": "노트북" } }         // keyword 타입 필드
```

### terms

여러 값 중 하나라도 일치:

```json
GET /products/_search
{
  "query": {
    "terms": {
      "category": ["노트북", "태블릿", "스마트폰"]
    }
  }
}
```
SQL: `WHERE category IN ('노트북', '태블릿', '스마트폰')`

### range

범위 검색:

```json
GET /products/_search
{
  "query": {
    "range": {
      "price": {
        "gte": 1000000,   // >=
        "lte": 2000000    // <=
      }
    }
  }
}
```

| 연산자 | 의미 |
|--------|------|
| `gt` | > |
| `gte` | >= |
| `lt` | < |
| `lte` | <= |

#### 날짜 범위

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

상대 날짜:
```json
{
  "range": {
    "created_at": {
      "gte": "now-7d/d",    // 7일 전부터
      "lt": "now/d"         // 오늘까지
    }
  }
}
```

### exists

필드가 존재하는 문서:

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

접두사 검색:

```json
GET /products/_search
{
  "query": {
    "prefix": {
      "name.keyword": "맥북"
    }
  }
}
```

### wildcard

와일드카드 패턴 검색:

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

> **성능 주의:** 앞에 `*`가 오면 매우 느립니다. 가급적 피하세요.

### fuzzy

오타 허용 검색:

```json
GET /products/_search
{
  "query": {
    "fuzzy": {
      "name": {
        "value": "맥뷱",     // 오타
        "fuzziness": "AUTO"  // 자동 편집 거리
      }
    }
  }
}
```

| fuzziness | 동작 |
|-----------|------|
| `0` | 정확히 일치 |
| `1` | 1글자 차이 허용 |
| `2` | 2글자 차이 허용 |
| `AUTO` | 길이에 따라 자동 (권장) |

{{< callout type="info" title="핵심 포인트" >}}
- `term`: text 필드에 사용하면 안 됨! keyword 필드나 `.keyword` 서브필드에 사용
- `range`: gte/gt/lte/lt로 범위 검색, 날짜에 `now-7d` 같은 상대 표현 가능
- `wildcard`: 앞에 `*`가 오면 매우 느림, 가급적 피하세요
- `fuzzy`: fuzziness=AUTO로 오타 허용 검색 가능
{{< /callout >}}

---

## Bool 쿼리

여러 쿼리를 조합합니다.

```json
GET /products/_search
{
  "query": {
    "bool": {
      "must": [],          // AND - 반드시 일치, score에 영향
      "should": [],        // OR - 하나 이상 일치하면 점수 상승
      "must_not": [],      // NOT - 일치하면 제외
      "filter": []         // AND - 반드시 일치, score 무시, 캐싱
    }
  }
}
```

### 실전 예제: 상품 검색

"카테고리가 노트북이고, 100만원~200만원 사이, '맥북' 검색어, 품절 제외"

```json
GET /products/_search
{
  "query": {
    "bool": {
      "must": [
        { "match": { "name": "맥북" } }
      ],
      "filter": [
        { "term": { "category": "노트북" } },
        { "range": { "price": { "gte": 1000000, "lte": 2000000 } } }
      ],
      "must_not": [
        { "term": { "status": "sold_out" } }
      ]
    }
  }
}
```

### should의 동작

`must`나 `filter`가 있으면 `should`는 선택적 (score 부스팅만):

```json
{
  "bool": {
    "must": [
      { "match": { "name": "맥북" } }
    ],
    "should": [
      { "term": { "is_promotion": true } }  // 프로모션 상품 점수 상승
    ]
  }
}
```

`must`나 `filter`가 없으면 `should` 중 하나는 일치해야 함:

```json
{
  "bool": {
    "should": [
      { "term": { "category": "노트북" } },
      { "term": { "category": "태블릿" } }
    ],
    "minimum_should_match": 1
  }
}
```

### 중첩 Bool 쿼리

```json
GET /products/_search
{
  "query": {
    "bool": {
      "must": [
        { "match": { "name": "프로" } }
      ],
      "should": [
        {
          "bool": {
            "must": [
              { "term": { "brand": "apple" } },
              { "range": { "price": { "gte": 2000000 } } }
            ]
          }
        },
        {
          "bool": {
            "must": [
              { "term": { "brand": "samsung" } },
              { "range": { "price": { "gte": 1500000 } } }
            ]
          }
        }
      ]
    }
  }
}
```

{{< callout type="info" title="핵심 포인트" >}}
- `must`: AND 조건, score에 영향
- `filter`: AND 조건, score 무시, 캐싱됨 (성능 우수)
- `should`: OR 조건, must/filter가 있으면 선택적 (score 부스팅용)
- `must_not`: NOT 조건, 일치하면 제외
- Bool 쿼리는 중첩하여 복잡한 조건 표현 가능
{{< /callout >}}

---

## 검색 결과 제어

### 페이지네이션

```json
GET /products/_search
{
  "from": 0,     // 시작 위치 (0부터)
  "size": 10,    // 가져올 개수
  "query": { "match_all": {} }
}
```

> **주의:** `from + size`는 기본 10,000 제한. 대량 페이지네이션은 `search_after` 사용.

### 정렬

```json
GET /products/_search
{
  "sort": [
    { "price": "asc" },
    { "created_at": "desc" },
    "_score"                    // 관련성 점수
  ],
  "query": { "match": { "name": "맥북" } }
}
```

### 필드 선택

```json
GET /products/_search
{
  "_source": ["name", "price"],   // 포함할 필드
  // 또는
  "_source": {
    "includes": ["name", "price"],
    "excludes": ["description"]
  },
  "query": { "match_all": {} }
}
```

### 하이라이팅

검색어 강조:

```json
GET /products/_search
{
  "query": {
    "match": { "description": "M3 칩" }
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

응답:
```json
{
  "hits": [{
    "_source": { "description": "M3 Pro 칩 탑재..." },
    "highlight": {
      "description": ["<em>M3</em> Pro <em>칩</em> 탑재..."]
    }
  }]
}
```

{{< callout type="info" title="핵심 포인트" >}}
- `from + size`는 기본 10,000 제한, 대량 페이지네이션은 `search_after` 사용
- `sort`로 정렬, `_source`로 반환 필드 제한 가능
- `highlight`로 검색어 강조 표시 (pre_tags/post_tags 커스터마이즈)
{{< /callout >}}

---

## SQL과 비교

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

## 자주 하는 실수

### 1. text 필드에 term 쿼리

```json
// ❌ 잘못됨 - "맥북 프로"는 ["맥북", "프로"]로 분석되어 저장됨
{ "term": { "name": "맥북 프로" } }

// ✅ 올바름
{ "match": { "name": "맥북 프로" } }
// 또는
{ "term": { "name.keyword": "맥북 프로" } }
```

### 2. filter 없이 term 쿼리만 사용

```json
// ⚠️ score 계산 불필요
{ "query": { "term": { "category": "노트북" } } }

// ✅ filter로 캐싱 효과
{
  "query": {
    "bool": {
      "filter": [
        { "term": { "category": "노트북" } }
      ]
    }
  }
}
```

### 3. 대량 페이지네이션

```json
// ❌ 10000건 초과 시 에러
{ "from": 10000, "size": 10 }

// ✅ search_after 사용
{
  "size": 10,
  "sort": [{ "created_at": "desc" }, { "_id": "asc" }],
  "search_after": ["2024-01-15T10:00:00", "abc123"]
}
```

{{< callout type="info" title="핵심 포인트" >}}
- text 필드에 term 쿼리 사용 금지 → match 또는 `.keyword` 사용
- 정확한 값 필터링은 filter 안에 넣어 캐싱 효과 활용
- 10,000건 초과 페이지네이션은 `search_after` 사용
{{< /callout >}}

---

## 다음 단계

| 목표 | 추천 문서 |
|------|----------|
| 검색 품질 개선 | [검색 관련성](search-relevance/) |
| 데이터 분석 | [집계](aggregations/) |
| 실습 | [기본 예제](../examples/basic/) |
