---
lastmod: "2026-01-08"
title: Korean Search Optimization
description: "Korean morphological analysis and search optimization"
weight: 11
prerequisites:
  - title: Data Modeling
    path: /docs/elasticsearch/concepts/data-modeling/
  - title: Query DSL
    path: /docs/elasticsearch/concepts/query-dsl/
related_concepts:
  - title: Search Relevance
    path: /docs/elasticsearch/concepts/search-relevance/
  - title: Product Search System
    path: /docs/elasticsearch/examples/product-search/
---

{{< callout type="info" title="Prerequisites" >}}
Before reading this document, understand these concepts first:
- [Data Modeling]({{< relref "/docs/elasticsearch/concepts/data-modeling" >}}) - Analyzer, text vs keyword types
- [Query DSL]({{< relref "/docs/elasticsearch/concepts/query-dsl" >}}) - match, multi_match queries
{{< /callout >}}

This document covers how to optimize Korean language search in Elasticsearch. We'll implement the Nori analyzer, autocomplete, and initial consonant (chosung) search.

Korean has fundamentally different linguistic characteristics from English, making proper search impossible with the default English analyzer. Searching "Samsung Electronics" (삼성전자) won't find "Samsung Electronics Inc." (삼성전자가) or "Samsung Electronics Co." (삼성전자를), and it can't extract "purchase" (구매) from "purchased" (구매했습니다). When users can't find results they expect to find, they lose trust in the search service.

The core of Korean search is **morphological analysis**. You must separate particles, properly decompose compound nouns, and extract stems. Elasticsearch provides the **Nori analyzer** as an official plugin for this purpose. By implementing autocomplete, initial consonant search, and synonym handling on top of this, you can provide the search experience Korean users expect. This document covers step-by-step implementation methods for Korean search that can be used in production services.

## Challenges of Korean Search

### English vs Korean

| Characteristic | English | Korean |
|----------------|---------|--------|
| **Word Separation** | Space-separated | Particles attached |
| **Stem Changes** | running → run | 먹었다 (ate) → 먹다 (eat) |
| **Synonyms** | car, automobile | 자동차, 차, 차량 (car, vehicle) |
| **Typos** | helo → hello | ㅎㅏㄴ글 → 한글 (Korean) |

```
Example: "삼성전자 갤럭시를 구매했습니다" (Purchased Samsung Electronics Galaxy)

English analyzer: ["삼성전자", "갤럭시를", "구매했습니다"]  ❌ Not searchable
Korean analyzer: ["삼성", "전자", "갤럭시", "구매"]  ✅ Individual terms searchable
```

---

## Nori Analyzer Configuration

### Nori Plugin Installation

```bash
# In Elasticsearch container
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

### Basic Nori Analyzer

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

### Verify Analysis Results

```json
POST /products/_analyze
{
  "analyzer": "nori_analyzer",
  "text": "삼성전자 갤럭시를 구매했습니다"
}

// Result
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

## Advanced Nori Configuration

### Compound Noun Decomposition

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

| decompound_mode | Description | "삼성전자" (Samsung Electronics) Result |
|-----------------|-------------|----------------------------------------|
| `none` | No decomposition | ["삼성전자"] |
| `discard` | Remove original | ["삼성", "전자"] |
| `mixed` | Keep both | ["삼성전자", "삼성", "전자"] |

### User Dictionary

```text
# config/userdict_ko.txt
# Format: word POS reading

삼성전자 NNP
갤럭시 NNP
아이폰 NNP
에어팟 NNP
맥북프로 NNP 맥북프로
```

---

## Autocomplete

### Edge N-gram Approach

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

### Autocomplete Search

```json
// Indexing
POST /products/_doc
{
  "name": "삼성 갤럭시 S24 울트라"
}

// Search with "갤럭" (Galax)
GET /products/_search
{
  "query": {
    "match": {
      "name": "갤럭"
    }
  }
}

// Result: "삼성 갤럭시 S24 울트라" matched
```

### Completion Suggester (Faster Approach)

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

// Indexing
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

// Autocomplete query
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

## Initial Consonant Search (Chosung)

### Chosung Filter Implementation

This is a Korean-specific feature that allows searching by initial consonants only. For example, searching "ㄱㄹㅅ" would find "갤럭시" (Galaxy).

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

### Using Elasticsearch Plugin (Recommended)

```bash
# Install jaso-analyzer plugin
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

// Search "ㄱㄹㅅ" to find "갤럭시"
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

## Synonym Handling

### Synonym Filter

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

### Using Synonym File

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

## Production Mapping Example

### Product Search Index

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

// Alias setup
POST /_aliases
{
  "actions": [
    { "add": { "index": "products_v1", "alias": "products" } }
  ]
}
```

### Combined Search Query

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

## Spring Data Elasticsearch Integration

### Repository Configuration

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

    // Autocomplete
    @Query("""
        {
          "match": {
            "name.autocomplete": "?0"
          }
        }
        """)
    List<Product> findByNameAutocomplete(String query);

    // Combined search
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

## Performance Optimization Tips

| Item | Recommended Setting |
|------|---------------------|
| `decompound_mode` | `mixed` (balance between precision and recall) |
| `edge_ngram min_gram` | 2 (single character search has too much noise) |
| Synonyms | Apply at search time rather than index time |
| Autocomplete | Use Completion Suggester (speed optimized) |
| Initial consonant search | Separate field (`name.chosung`) |

---

## Next Steps

- [Query DSL]({{< relref "/docs/elasticsearch/concepts/query-dsl" >}}) - Various search queries
- [Search Relevance]({{< relref "/docs/elasticsearch/concepts/search-relevance" >}}) - Score adjustment
- [Performance Tuning]({{< relref "/docs/elasticsearch/concepts/performance-tuning" >}}) - High-volume processing
