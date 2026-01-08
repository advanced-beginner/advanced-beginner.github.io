---
lastmod: "2026-01-08"
title: Korean Search Optimization
weight: 11
---

This document covers how to optimize Korean language search in Elasticsearch. We'll implement the Nori analyzer, autocomplete, and initial consonant (chosung) search.

## Challenges of Korean Search

### English vs Korean

| Characteristic | English | Korean |
|----------------|---------|--------|
| **Word Separation** | Space-separated | Particles attached |
| **Stem Changes** | running → run | 먹었다 → 먹다 |
| **Synonyms** | car, automobile | 자동차, 차, 차량 |
| **Typos** | helo → hello | ㅎㅏㄴ글 → 한글 |

```
Example: "삼성전자 갤럭시를 구매했습니다" (Samsung Electronics Galaxy purchase)

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

| decompound_mode | Description | "삼성전자" Result |
|-----------------|-------------|-------------------|
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
  "name": "Samsung Galaxy S24 Ultra"
}

// Search with "Galax"
GET /products/_search
{
  "query": {
    "match": {
      "name": "Galax"
    }
  }
}

// Result: "Samsung Galaxy S24 Ultra" matched
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
  "name": "Samsung Galaxy S24 Ultra",
  "name_suggest": {
    "input": ["Samsung", "Galaxy", "S24", "Ultra", "Samsung Galaxy"],
    "contexts": {
      "category": "smartphone"
    }
  }
}

// Autocomplete query
POST /products/_search
{
  "suggest": {
    "product-suggest": {
      "prefix": "Galax",
      "completion": {
        "field": "name_suggest",
        "size": 5,
        "contexts": {
          "category": "smartphone"
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
            "car, automobile, vehicle",
            "laptop, notebook, portable computer",
            "cellphone, mobile phone, smartphone, phone",
            "air conditioner, AC, cooling",
            "tv, television"
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
car, automobile, vehicle
laptop, notebook, portable computer
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
            "query": "Samsung Galaxy",
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
        { "term": { "category": "smartphone" } },
        { "range": { "price": { "gte": 500, "lte": 1500 } } }
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

- [Query DSL](../query-dsl/) - Various search queries
- [Search Relevance](../search-relevance/) - Score adjustment
- [Performance Tuning](../performance-tuning/) - High-volume processing
