---
lastmod: "2026-01-15"
title: 한글 검색 최적화
description: "한글 형태소 분석과 검색 최적화의 원리와 설정 방법을 설명합니다."
weight: 11
prerequisites:
  - title: 데이터 모델링
    path: /docs/elasticsearch/concepts/data-modeling/
  - title: Query DSL
    path: /docs/elasticsearch/concepts/query-dsl/
related_concepts:
  - title: 검색 관련성
    path: /docs/elasticsearch/concepts/search-relevance/
  - title: 상품 검색 시스템
    path: /docs/elasticsearch/examples/product-search/
---

{{< callout type="info" title="선수 개념" >}}
이 문서를 읽기 전에 다음 개념을 먼저 이해하세요:
- [데이터 모델링]({{< relref "/docs/elasticsearch/concepts/data-modeling" >}}) - Analyzer, text vs keyword 타입
- [Query DSL]({{< relref "/docs/elasticsearch/concepts/query-dsl" >}}) - match, multi_match 쿼리
{{< /callout >}}

**소요 시간**: 약 25-30분

## 전체 비유: 외국인을 위한 한글 도서 검색

한글 검색 최적화를 **외국인 사서가 한글 도서를 색인하는 상황**에 비유하면 이해하기 쉽습니다:

| 한글 검색 비유 | Elasticsearch | 역할 |
|---------------|---------------|------|
| 영어 사서: "삼성전자가"를 통째로 기록 | Standard Analyzer | 조사 분리 못함 |
| 한국어 사서: "삼성전자+가"로 분리 | Nori Analyzer | 형태소 분석으로 조사 분리 |
| "삼성전자" → "삼성 + 전자" | decompound_mode: mixed | 복합명사 분해 |
| 신조어/브랜드명 등록 | 사용자 사전 | 특수 단어 인식 |
| "갤럭" 입력 시 "갤럭시" 추천 | 자동완성 (Edge N-gram) | 접두어 매칭 |
| "ㄱㄹㅅ"로 "갤럭시" 찾기 | 초성 검색 | 자음 추출 후 매칭 |
| "노트북 = 랩탑" 등록 | 동의어 처리 | 유사어 확장 검색 |

이처럼 한글 검색은 "한글의 특성을 이해하는 전문 사서를 배치"하는 것과 같습니다.

Elasticsearch에서 한글 검색을 최적화하는 방법을 다룹니다. Nori 분석기, 자동완성, 초성 검색을 구현합니다.

한글은 영어와 근본적으로 다른 언어적 특성을 가지고 있어, 기본 영어 분석기로는 제대로 된 검색이 불가능합니다. "삼성전자"를 검색해도 "삼성전자가"나 "삼성전자를"이 검색되지 않고, "구매했습니다"에서 "구매"를 추출하지 못합니다. 사용자는 당연히 찾을 수 있다고 생각하는 결과를 못 찾게 되면, 검색 서비스에 대한 신뢰를 잃게 됩니다.

한글 검색의 핵심은 **형태소 분석**입니다. 조사를 분리하고, 복합명사를 적절히 분해하며, 어근을 추출해야 합니다. Elasticsearch는 이를 위해 **Nori 분석기**를 공식 플러그인으로 제공합니다. 여기에 자동완성, 초성 검색, 동의어 처리까지 구현하면 한국 사용자가 기대하는 수준의 검색 경험을 제공할 수 있습니다. 이 문서에서는 실제 서비스에서 사용할 수 있는 한글 검색 구현 방법을 단계별로 다룹니다.

## 한글 검색의 어려움

### 영어 vs 한글

| 특성 | 영어 | 한글 |
|------|------|------|
| **단어 구분** | 공백으로 분리 | 조사가 붙어있음 |
| **어근 변화** | running → run | 먹었다 → 먹다 |
| **동의어** | car, automobile | 자동차, 차, 차량 |
| **오타** | helo → hello | ㅎㅏㄴ글 → 한글 |

```
예시: "삼성전자 갤럭시를 구매했습니다"

영어 분석기: ["삼성전자", "갤럭시를", "구매했습니다"]  ❌ 검색 안됨
한글 분석기: ["삼성", "전자", "갤럭시", "구매"]  ✅ 개별 검색 가능
```

---

## Nori 분석기 설정

### Nori 플러그인 설치

```bash
# Elasticsearch 컨테이너에서
bin/elasticsearch-plugin install analysis-nori

# Docker Compose
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    command: >
      bash -c "
        bin/elasticsearch-plugin install analysis-nori &&
        /usr/local/bin/docker-entrypoint.sh
      "
```

### 기본 Nori 분석기

```json
PUT /products
{
  "settings": {
    "analysis": {
      "analyzer": {
        "nori_analyzer": {
          "type": "custom",
          "tokenizer": "nori_tokenizer",
          "filter": ["nori_readingform", "lowercase"]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "name": {
        "type": "text",
        "analyzer": "nori_analyzer"
      }
    }
  }
}
```

### 분석 결과 확인

```json
POST /products/_analyze
{
  "analyzer": "nori_analyzer",
  "text": "삼성전자 갤럭시를 구매했습니다"
}

// 결과
{
  "tokens": [
    {"token": "삼성", "start_offset": 0, "end_offset": 2},
    {"token": "전자", "start_offset": 2, "end_offset": 4},
    {"token": "갤럭시", "start_offset": 5, "end_offset": 8},
    {"token": "구매", "start_offset": 10, "end_offset": 12}
  ]
}
```

---

## 고급 Nori 설정

### 복합명사 분해 (Decompound)

```json
PUT /products
{
  "settings": {
    "analysis": {
      "tokenizer": {
        "nori_mixed": {
          "type": "nori_tokenizer",
          "decompound_mode": "mixed",  // none, discard, mixed
          "discard_punctuation": true,
          "user_dictionary": "userdict_ko.txt"
        }
      },
      "analyzer": {
        "nori_mixed_analyzer": {
          "type": "custom",
          "tokenizer": "nori_mixed",
          "filter": [
            "nori_readingform",
            "nori_part_of_speech",
            "lowercase",
            "trim"
          ]
        }
      },
      "filter": {
        "nori_part_of_speech": {
          "type": "nori_part_of_speech",
          "stoptags": [
            "E", "IC", "J", "MAG", "MAJ", "MM",
            "SP", "SSC", "SSO", "SC", "SE",
            "XPN", "XSA", "XSN", "XSV",
            "UNA", "NA", "VSV"
          ]
        }
      }
    }
  }
}
```

| decompound_mode | 설명 | "삼성전자" 결과 |
|-----------------|------|----------------|
| `none` | 분해 안함 | ["삼성전자"] |
| `discard` | 원본 제거 | ["삼성", "전자"] |
| `mixed` | 둘 다 유지 | ["삼성전자", "삼성", "전자"] |

### 사용자 사전 (User Dictionary)

```text
# config/userdict_ko.txt
# 형식: 단어 품사 읽기

삼성전자 NNP
갤럭시 NNP
아이폰 NNP
에어팟 NNP
맥북프로 NNP 맥북프로
```

---

## 자동완성 (Autocomplete)

### Edge N-gram 방식

```json
PUT /products
{
  "settings": {
    "analysis": {
      "tokenizer": {
        "nori_tokenizer": {
          "type": "nori_tokenizer",
          "decompound_mode": "mixed"
        }
      },
      "filter": {
        "edge_ngram_filter": {
          "type": "edge_ngram",
          "min_gram": 1,
          "max_gram": 10
        }
      },
      "analyzer": {
        "autocomplete_index": {
          "type": "custom",
          "tokenizer": "nori_tokenizer",
          "filter": ["lowercase", "edge_ngram_filter"]
        },
        "autocomplete_search": {
          "type": "custom",
          "tokenizer": "nori_tokenizer",
          "filter": ["lowercase"]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "name": {
        "type": "text",
        "analyzer": "autocomplete_index",
        "search_analyzer": "autocomplete_search"
      }
    }
  }
}
```

### 자동완성 검색

```json
// 인덱싱
POST /products/_doc
{
  "name": "삼성 갤럭시 S24 울트라"
}

// "갤럭"으로 검색
GET /products/_search
{
  "query": {
    "match": {
      "name": "갤럭"
    }
  }
}

// 결과: "삼성 갤럭시 S24 울트라" 매칭됨
```

### Completion Suggester (더 빠른 방식)

```json
PUT /products
{
  "mappings": {
    "properties": {
      "name": {
        "type": "text",
        "analyzer": "nori_analyzer"
      },
      "name_suggest": {
        "type": "completion",
        "analyzer": "nori_analyzer",
        "contexts": [
          {
            "name": "category",
            "type": "category"
          }
        ]
      }
    }
  }
}

// 인덱싱
POST /products/_doc
{
  "name": "삼성 갤럭시 S24 울트라",
  "name_suggest": {
    "input": ["삼성", "갤럭시", "S24", "울트라", "삼성 갤럭시"],
    "contexts": {
      "category": "스마트폰"
    }
  }
}

// 자동완성 쿼리
POST /products/_search
{
  "suggest": {
    "product-suggest": {
      "prefix": "갤럭",
      "completion": {
        "field": "name_suggest",
        "size": 5,
        "contexts": {
          "category": "스마트폰"
        }
      }
    }
  }
}
```

---

## 초성 검색

### 초성 필터 구현

```json
PUT /products
{
  "settings": {
    "analysis": {
      "char_filter": {
        "chosung_filter": {
          "type": "pattern_replace",
          "pattern": "([가-깋])",
          "replacement": "ㄱ"
        }
      },
      "tokenizer": {
        "chosung_tokenizer": {
          "type": "pattern",
          "pattern": ""
        }
      },
      "analyzer": {
        "chosung_analyzer": {
          "type": "custom",
          "char_filter": ["chosung_filter"],
          "tokenizer": "keyword"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "name": {
        "type": "text",
        "analyzer": "nori_analyzer",
        "fields": {
          "chosung": {
            "type": "text",
            "analyzer": "chosung_analyzer"
          }
        }
      }
    }
  }
}
```

### Elasticsearch 플러그인 사용 (권장)

```bash
# jaso-analyzer 플러그인 설치
bin/elasticsearch-plugin install https://github.com/.../jaso-analyzer.zip
```

```json
PUT /products
{
  "settings": {
    "analysis": {
      "analyzer": {
        "jaso_search": {
          "type": "custom",
          "tokenizer": "keyword",
          "filter": ["jaso_filter"]
        }
      },
      "filter": {
        "jaso_filter": {
          "type": "jaso",
          "chosung": true
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "name": {
        "type": "text",
        "analyzer": "nori_analyzer",
        "fields": {
          "jaso": {
            "type": "text",
            "analyzer": "jaso_search"
          }
        }
      }
    }
  }
}

// "ㄱㄹㅅ"로 "갤럭시" 검색
GET /products/_search
{
  "query": {
    "match": {
      "name.jaso": "ㄱㄹㅅ"
    }
  }
}
```

---

## 동의어 처리

### 동의어 필터

```json
PUT /products
{
  "settings": {
    "analysis": {
      "filter": {
        "korean_synonym": {
          "type": "synonym",
          "synonyms": [
            "자동차, 차, 차량, 카",
            "노트북, 랩탑, 휴대용컴퓨터",
            "핸드폰, 휴대폰, 스마트폰, 폰",
            "에어컨, 에어콘, 냉방기",
            "tv, 티비, 텔레비전"
          ]
        }
      },
      "analyzer": {
        "nori_synonym": {
          "type": "custom",
          "tokenizer": "nori_tokenizer",
          "filter": [
            "nori_readingform",
            "lowercase",
            "korean_synonym"
          ]
        }
      }
    }
  }
}
```

### 동의어 파일 사용

```text
# config/synonyms_ko.txt
자동차, 차, 차량, 카
노트북, 랩탑, 휴대용컴퓨터
```

```json
"filter": {
  "korean_synonym": {
    "type": "synonym",
    "synonyms_path": "synonyms_ko.txt"
  }
}
```

---

## 실전 매핑 예제

### 상품 검색 인덱스

```json
PUT /products_v1
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1,
    "analysis": {
      "tokenizer": {
        "nori_mixed": {
          "type": "nori_tokenizer",
          "decompound_mode": "mixed",
          "user_dictionary": "userdict_ko.txt"
        }
      },
      "filter": {
        "edge_ngram_2_10": {
          "type": "edge_ngram",
          "min_gram": 2,
          "max_gram": 10
        },
        "korean_synonym": {
          "type": "synonym",
          "synonyms_path": "synonyms_ko.txt"
        },
        "nori_posfilter": {
          "type": "nori_part_of_speech",
          "stoptags": ["E", "J", "SC", "SE", "SF", "SP", "SSC", "SSO", "VCP", "XSN", "XSV"]
        }
      },
      "analyzer": {
        "korean_index": {
          "type": "custom",
          "tokenizer": "nori_mixed",
          "filter": ["nori_readingform", "nori_posfilter", "lowercase", "korean_synonym"]
        },
        "korean_search": {
          "type": "custom",
          "tokenizer": "nori_mixed",
          "filter": ["nori_readingform", "nori_posfilter", "lowercase"]
        },
        "autocomplete_index": {
          "type": "custom",
          "tokenizer": "nori_mixed",
          "filter": ["nori_readingform", "lowercase", "edge_ngram_2_10"]
        },
        "autocomplete_search": {
          "type": "custom",
          "tokenizer": "nori_mixed",
          "filter": ["nori_readingform", "lowercase"]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "name": {
        "type": "text",
        "analyzer": "korean_index",
        "search_analyzer": "korean_search",
        "fields": {
          "autocomplete": {
            "type": "text",
            "analyzer": "autocomplete_index",
            "search_analyzer": "autocomplete_search"
          },
          "keyword": {
            "type": "keyword"
          }
        }
      },
      "brand": {
        "type": "keyword",
        "fields": {
          "text": {
            "type": "text",
            "analyzer": "korean_index"
          }
        }
      },
      "category": {
        "type": "keyword"
      },
      "price": {
        "type": "integer"
      },
      "description": {
        "type": "text",
        "analyzer": "korean_index",
        "search_analyzer": "korean_search"
      },
      "created_at": {
        "type": "date"
      }
    }
  }
}

// 별칭 설정
POST /_aliases
{
  "actions": [
    { "add": { "index": "products_v1", "alias": "products" } }
  ]
}
```

### 통합 검색 쿼리

```json
GET /products/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "multi_match": {
            "query": "삼성 갤럭시",
            "fields": [
              "name^3",
              "name.autocomplete^2",
              "brand.text",
              "description"
            ],
            "type": "best_fields",
            "operator": "or",
            "minimum_should_match": "75%"
          }
        }
      ],
      "filter": [
        { "term": { "category": "스마트폰" } },
        { "range": { "price": { "gte": 500000, "lte": 1500000 } } }
      ]
    }
  },
  "highlight": {
    "fields": {
      "name": {},
      "description": {}
    },
    "pre_tags": ["<em>"],
    "post_tags": ["</em>"]
  },
  "sort": [
    { "_score": "desc" },
    { "created_at": "desc" }
  ]
}
```

---

## Spring Data Elasticsearch 연동

### Repository 설정

```java
@Document(indexName = "products")
public class Product {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "korean_index", searchAnalyzer = "korean_search")
    private String name;

    @Field(type = FieldType.Keyword)
    private String brand;

    @Field(type = FieldType.Integer)
    private Integer price;

    @Field(type = FieldType.Text, analyzer = "korean_index")
    private String description;
}

@Repository
public interface ProductRepository extends ElasticsearchRepository<Product, String> {

    // 자동완성
    @Query("""
        {
          "match": {
            "name.autocomplete": "?0"
          }
        }
        """)
    List<Product> findByNameAutocomplete(String query);

    // 통합 검색
    @Query("""
        {
          "bool": {
            "must": [
              {
                "multi_match": {
                  "query": "?0",
                  "fields": ["name^3", "brand.text", "description"],
                  "type": "best_fields"
                }
              }
            ],
            "filter": [
              { "term": { "brand": "?1" } }
            ]
          }
        }
        """)
    Page<Product> searchProducts(String query, String brand, Pageable pageable);
}
```

---

## 성능 최적화 팁

| 항목 | 권장 설정 |
|------|----------|
| `decompound_mode` | `mixed` (검색 정확도와 재현율 균형) |
| `edge_ngram min_gram` | 2 (1글자 검색은 노이즈가 많음) |
| 동의어 | 인덱스 타임보다 검색 타임에 적용 |
| 자동완성 | Completion Suggester 사용 (속도 최적화) |
| 초성 검색 | 별도 필드로 분리 (`name.chosung`) |

---

## 다음 단계

- [Query DSL]({{< relref "/docs/elasticsearch/concepts/query-dsl" >}}) - 다양한 검색 쿼리
- [검색 관련성]({{< relref "/docs/elasticsearch/concepts/search-relevance" >}}) - 점수 조정
- [성능 튜닝]({{< relref "/docs/elasticsearch/concepts/performance-tuning" >}}) - 대용량 처리
