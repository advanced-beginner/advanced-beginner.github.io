---
title: 용어 사전
weight: 1
lastmod: 2026-01-15
---

{{< callout type="tip" title="TL;DR" >}}
- **Index/Document/Field**: RDB의 Table/Row/Column에 대응
- **Shard/Replica**: 데이터 분산과 복제의 기본 단위
- **Analyzer/Tokenizer**: 텍스트를 검색 가능한 토큰으로 분해
- **Query/Filter Context**: 점수 계산 여부에 따른 검색 방식 구분
- 알파벳순으로 정렬, 각 용어에서 관련 개념 문서로 링크
{{< /callout >}}

> **빠른 이동**: [A](#a-e) | [B](#a-e) | [C](#a-e) | [D](#a-e) | [E](#a-e) | [F](#f-m) | [G](#f-m) | [H](#f-m) | [I](#f-m) | [J](#f-m) | [K](#f-m) | [L](#f-m) | [M](#f-m) | [N](#n-r) | [O](#n-r) | [P](#n-r) | [Q](#n-r) | [R](#n-r) | [S](#s-z) | [T](#s-z) | [U](#s-z) | [V](#s-z) | [W](#s-z) | [Z](#s-z)

Elasticsearch 핵심 용어를 빠르게 찾아볼 수 있습니다. 상세 설명은 [개념 이해](../concepts/) 섹션을 참고하세요.

## A-E

### Aggregation (집계)
검색 결과를 그룹화하고 통계를 계산하는 기능. SQL의 `GROUP BY`와 유사. Bucket/Metric/Pipeline 세 종류.
→ [집계 개념](../concepts/aggregations/) | [Query DSL](../concepts/query-dsl/)

### Alias (별칭)
[Index](#index-인덱스)에 부여하는 별명. 무중단 인덱스 전환, 멀티 인덱스 검색에 유용. [ILM](#ilm-index-lifecycle-management)과 함께 사용.
→ [인덱싱 전략](../concepts/indexing/)

### Analyzer (분석기)
텍스트를 [Term](#term)으로 분해하는 컴포넌트. Character Filter → [Tokenizer](#tokenizer) → Token Filter 순서로 처리. 한글에는 [Nori](#nori) 분석기 사용.
→ [데이터 모델링](../concepts/data-modeling/) | [한글 검색](../concepts/korean-search/)

### Apache Lucene
Elasticsearch 내부에서 사용하는 오픈소스 검색 엔진 라이브러리. 각 [Shard](#shard-샤드)가 하나의 Lucene 인덱스.
→ [핵심 구성요소](../concepts/core-components/)

### Auto-Expand Replicas
[Node](#node-노드) 수에 따라 [Replica](#replica-shard) 수를 자동 조정하는 설정. `0-2` 형식으로 최소-최대 지정.
→ [고가용성](../concepts/high-availability/)

### BM25 (Best Matching 25)
Elasticsearch의 기본 [Score](#score-점수) 계산 알고리즘. [TF](#tf-term-frequency)와 [IDF](#idf-inverse-document-frequency) 기반. [Boosting](#boosting)으로 조절 가능.
→ [검색 관련성](../concepts/search-relevance/)

### Bool Query
`must`, `should`, `must_not`, `filter` 절을 조합하는 복합 쿼리. AND/OR/NOT 논리 연산 구현.
→ [Query DSL](../concepts/query-dsl/)

### Boosting
특정 필드나 조건의 [Score](#score-점수)에 가중치를 부여하는 기법. 제목에 높은 부스트 값 적용 등.
→ [검색 관련성](../concepts/search-relevance/)

### Bucket Aggregation
데이터를 그룹(버킷)으로 나누는 집계. `terms`, `range`, `date_histogram` 등. SQL의 `GROUP BY`와 유사.
→ [집계 개념](../concepts/aggregations/)

### Bulk API
여러 [Document](#document-문서)를 한 번에 인덱싱하는 API. 성능 향상에 필수. [NDJSON](#ndjson) 형식 사용.
→ [인덱싱 전략](../concepts/indexing/)

### Cardinality (집계)
고유값 개수를 계산하는 [Metric Aggregation](#metric-aggregation). SQL의 `COUNT(DISTINCT)`와 유사.
→ [집계 개념](../concepts/aggregations/)

### CCR (Cross-Cluster Replication)
원격 클러스터로 데이터를 실시간 복제. 재해 복구(DR), 지역별 읽기에 활용.
→ [고가용성](../concepts/high-availability/)

### Character Filter
[Analyzer](#analyzer-분석기)의 첫 단계. HTML 태그 제거, 패턴 치환 등 텍스트 전처리.
→ [데이터 모델링](../concepts/data-modeling/)

### Cluster (클러스터)
하나 이상의 [Node](#node-노드)로 구성된 Elasticsearch 서버 그룹. [Master Node](#master-node)가 상태 관리.
→ [핵심 구성요소](../concepts/core-components/) | [클러스터 관리](../concepts/cluster-management/)

### Cluster Health
클러스터 전체 상태를 나타내는 지표. Green(정상), Yellow(Replica 미할당), Red(Primary 미할당).
→ [클러스터 관리](../concepts/cluster-management/)

### Commit Point
현재 활성 [Segment](#segment) 목록을 추적하는 메타데이터. 검색 대상 세그먼트 관리.
→ [핵심 구성요소](../concepts/core-components/)

### Completion Suggester
빠른 자동완성을 위한 특수 필드 타입. FST(Finite State Transducer) 기반.
→ [한글 검색](../concepts/korean-search/)

### Composite Aggregation
대용량 데이터의 페이지네이션 집계. `after` 파라미터로 다음 페이지 조회.
→ [집계 개념](../concepts/aggregations/)

### Coordinating Node
검색 요청을 받아 [Data Node](#data-node)에 분배하고 결과를 병합하는 노드. 모든 노드가 기본적으로 역할 수행.
→ [클러스터 관리](../concepts/cluster-management/)

### Cosine Similarity
두 벡터 간의 각도를 측정하는 유사도. [dense_vector](#dense_vector) 필드의 기본 유사도 방식.
→ [Vector Search](../concepts/vector-search/)

### Data Node
실제 데이터를 저장하고 검색/[Aggregation](#aggregation-집계)을 수행하는 노드. [Shard](#shard-샤드)가 할당됨.
→ [클러스터 관리](../concepts/cluster-management/)

### Data Tier (Hot/Warm/Cold)
데이터 수명주기에 따른 노드 계층. Hot(활발한 쓰기/읽기), Warm(읽기 위주), Cold(가끔 읽기).
→ [클러스터 관리](../concepts/cluster-management/) | [인덱싱 전략](../concepts/indexing/)

### date_histogram
시간 간격으로 데이터를 그룹화하는 [Bucket Aggregation](#bucket-aggregation). 일별/월별 트렌드 분석.
→ [집계 개념](../concepts/aggregations/)

### Decay Function
거리나 시간에 따라 점수를 감소시키는 함수. `linear`, `exp`, `gauss` 종류.
→ [검색 관련성](../concepts/search-relevance/)

### decompound_mode
[Nori](#nori) 분석기의 복합명사 분해 설정. `none`/`discard`/`mixed` 옵션.
→ [한글 검색](../concepts/korean-search/)

### dense_vector
벡터 데이터를 저장하는 필드 타입. [kNN](#knn-k-nearest-neighbors) 검색에 사용. `dims`, `similarity` 설정.
→ [Vector Search](../concepts/vector-search/)

### Document (문서)
Elasticsearch에 저장되는 JSON 형태의 데이터 단위. RDB의 Row와 유사. [Index](#index-인덱스) 내에 저장됨.
→ [핵심 구성요소](../concepts/core-components/)

### DSL (Domain Specific Language)
Elasticsearch [Query](#query-context) 작성을 위한 JSON 기반 언어. [Bool Query](#bool-query), Match, Term 등 다양한 쿼리 제공.
→ [Query DSL](../concepts/query-dsl/)

### Dynamic Mapping
[Mapping](#mapping-매핑)을 정의하지 않았을 때 Elasticsearch가 자동으로 타입을 추론하는 기능. `true`/`false`/`strict` 설정.
→ [데이터 모델링](../concepts/data-modeling/)

### Edge N-gram
단어의 앞부분부터 N글자씩 토큰을 생성. 자동완성 구현에 사용.
→ [한글 검색](../concepts/korean-search/)

### Embedding (임베딩)
텍스트나 이미지를 고차원 벡터로 변환하는 과정. [Vector Search](#vector-search)의 핵심.
→ [Vector Search](../concepts/vector-search/)

### exists (쿼리)
특정 필드가 존재하는 문서를 검색. null이 아닌 값을 가진 문서 필터링.
→ [Query DSL](../concepts/query-dsl/)

### Explain API
[Score](#score-점수)가 어떻게 계산되었는지 상세 분석하는 API.
→ [검색 관련성](../concepts/search-relevance/)

### extended_stats
평균, 합계, 분산, 표준편차 등 확장 통계를 계산하는 [Metric Aggregation](#metric-aggregation).
→ [집계 개념](../concepts/aggregations/)

---

## F-M

### Field (필드)
[Document](#document-문서) 내의 개별 데이터 항목. RDB의 Column과 유사. [Mapping](#mapping-매핑)으로 타입 정의.
→ [데이터 모델링](../concepts/data-modeling/)

### Field Data Cache
text 필드의 정렬/집계를 위해 메모리에 로드되는 캐시. 메모리 사용량 주의.
→ [성능 튜닝](../concepts/performance-tuning/)

### Field Length Normalization
짧은 필드에서 일치하면 더 높은 점수를 부여하는 [BM25](#bm25-best-matching-25)의 요소.
→ [검색 관련성](../concepts/search-relevance/)

### Filter Context
[Score](#score-점수) 계산 없이 조건 매칭만 수행. 캐싱되어 성능 우수. [Query Context](#query-context)와 함께 Bool 쿼리에서 사용.
→ [Query DSL](../concepts/query-dsl/)

### Flush
메모리 버퍼의 데이터를 디스크에 영구 저장하는 작업. [Translog](#translog) 초기화. [Refresh](#refresh)와 구분.
→ [인덱싱 전략](../concepts/indexing/)

### Function Score Query
복잡한 스코어링 로직을 구현하는 쿼리. `weight`, `field_value_factor`, [Decay Function](#decay-function) 등.
→ [검색 관련성](../concepts/search-relevance/)

### fuzzy (쿼리)
오타를 허용하는 검색. `fuzziness` 옵션으로 편집 거리 지정. `AUTO` 권장.
→ [Query DSL](../concepts/query-dsl/)

### Highlighting (하이라이팅)
검색 결과에서 검색어를 강조 표시. `pre_tags`, `post_tags`로 태그 지정.
→ [Query DSL](../concepts/query-dsl/)

### histogram (집계)
고정 간격으로 데이터를 그룹화하는 [Bucket Aggregation](#bucket-aggregation). 가격대별 분포 등.
→ [집계 개념](../concepts/aggregations/)

### HNSW (Hierarchical Navigable Small World)
[kNN](#knn-k-nearest-neighbors) 검색에 사용되는 근사 최근접 이웃 알고리즘. `m`, `ef_construction` 파라미터로 튜닝.
→ [Vector Search](../concepts/vector-search/)

### Hybrid Search (하이브리드 검색)
[kNN](#knn-k-nearest-neighbors) 벡터 검색과 키워드 검색을 결합. `boost`로 가중치 조절.
→ [Vector Search](../concepts/vector-search/)

### IDF (Inverse Document Frequency)
단어가 전체 [Document](#document-문서)에서 얼마나 희귀한지를 나타내는 지표. [BM25](#bm25-best-matching-25)의 구성 요소.
→ [검색 관련성](../concepts/search-relevance/)

### ILM (Index Lifecycle Management)
[Index](#index-인덱스)의 생성부터 삭제까지 수명주기를 자동 관리. Hot → Warm → Cold → Delete 단계.
→ [인덱싱 전략](../concepts/indexing/)

### Index (인덱스)
[Document](#document-문서)들의 모음. RDB의 Table과 유사. [Shard](#shard-샤드)로 분산 저장.
→ [핵심 구성요소](../concepts/core-components/)

### Index Template
새 인덱스 생성 시 자동 적용되는 설정 템플릿. `index_patterns`로 매칭.
→ [인덱싱 전략](../concepts/indexing/)

### Ingest Node
인덱싱 전 데이터 전처리를 수행하는 노드. 파이프라인으로 변환 정의.
→ [클러스터 관리](../concepts/cluster-management/)

### Inverted Index (역색인)
[Term](#term) → [Document](#document-문서) 위치를 매핑한 자료구조. 빠른 검색의 핵심.
→ [핵심 구성요소](../concepts/core-components/)

### JVM Heap
Elasticsearch가 사용하는 Java 힙 메모리. 총 메모리의 50% 권장 (최대 30-31GB).
→ [성능 튜닝](../concepts/performance-tuning/)

### keyword (타입)
분석 없이 정확한 값으로 저장되는 문자열 타입. 필터링, 정렬, 집계에 사용.
→ [데이터 모델링](../concepts/data-modeling/)

### kNN (k-Nearest Neighbors)
벡터 유사도 기반 검색. [dense_vector](#dense_vector)에서 가장 가까운 k개 문서를 찾는 알고리즘.
→ [Vector Search](../concepts/vector-search/)

### Mapping (매핑)
[Document](#document-문서)와 [Field](#field-필드)의 저장/인덱싱 방식을 정의. RDB의 Schema와 유사. Dynamic/Explicit 두 방식.
→ [데이터 모델링](../concepts/data-modeling/)

### Master Node
[Cluster](#cluster-클러스터) 상태를 관리하고 [Index](#index-인덱스) 생성/삭제를 담당하는 노드. [Data Node](#data-node)와 분리 권장.
→ [클러스터 관리](../concepts/cluster-management/)

### match (쿼리)
가장 일반적인 풀텍스트 검색. [Analyzer](#analyzer-분석기)로 분석 후 검색. `operator`, `minimum_should_match` 옵션.
→ [Query DSL](../concepts/query-dsl/)

### match_phrase (쿼리)
단어 순서까지 일치해야 하는 구문 검색. `slop` 옵션으로 단어 간격 허용.
→ [Query DSL](../concepts/query-dsl/)

### Metric Aggregation
숫자 데이터의 통계(sum, avg, min, max 등)를 계산하는 집계.
→ [집계 개념](../concepts/aggregations/)

### Multi-field
하나의 필드를 여러 방식으로 인덱싱. `name.keyword` 형태로 접근.
→ [데이터 모델링](../concepts/data-modeling/)

### multi_match (쿼리)
여러 필드에서 동시에 풀텍스트 검색. `fields`에 가중치(`^`) 지정 가능.
→ [Query DSL](../concepts/query-dsl/)

---

## N-R

### NDJSON (Newline Delimited JSON)
각 줄이 개별 JSON 객체인 형식. [Bulk API](#bulk-api)에서 사용.
→ [인덱싱 전략](../concepts/indexing/)

### Near Real-Time (NRT)
Elasticsearch의 검색 특성. 인덱싱 후 기본 1초([Refresh](#refresh)) 후 검색 가능.
→ [핵심 구성요소](../concepts/core-components/)

### Negative Boosting
특정 조건의 점수를 낮추는 기법. `boosting` 쿼리의 `negative_boost` 사용.
→ [검색 관련성](../concepts/search-relevance/)

### Nested (타입)
배열 내 객체의 관계를 유지하는 필드 타입. Object 타입의 평탄화 문제 해결.
→ [데이터 모델링](../concepts/data-modeling/)

### Node (노드)
[Cluster](#cluster-클러스터)를 구성하는 단일 Elasticsearch 서버. [Master](#master-node), [Data](#data-node), [Coordinating](#coordinating-node) 등 역할 구분.
→ [핵심 구성요소](../concepts/core-components/) | [클러스터 관리](../concepts/cluster-management/)

### Node Query Cache
[Filter Context](#filter-context) 결과를 캐싱하는 노드 레벨 캐시. [Refresh](#refresh) 시 무효화.
→ [성능 튜닝](../concepts/performance-tuning/)

### Nori
Elasticsearch 공식 한글 형태소 [Analyzer](#analyzer-분석기). `nori_tokenizer`, `nori_part_of_speech` 필터 제공. 자동완성, 초성 검색 구현에 활용.
→ [한글 검색 최적화](../concepts/korean-search/)

### nori_part_of_speech
품사 태그 기반 토큰 필터링. 조사, 어미 등 불필요한 품사 제거.
→ [한글 검색](../concepts/korean-search/)

### nori_readingform
한자를 한글 발음으로 변환하는 토큰 필터.
→ [한글 검색](../concepts/korean-search/)

### num_candidates
[kNN](#knn-k-nearest-neighbors) 검색에서 각 샤드별 후보 문서 수. 정확도와 속도의 트레이드오프.
→ [Vector Search](../concepts/vector-search/)

### Object (타입)
중첩된 JSON 객체를 저장하는 필드 타입. 배열 시 평탄화되어 관계 손실 주의.
→ [데이터 모델링](../concepts/data-modeling/)

### percentiles (집계)
백분위수를 계산하는 [Metric Aggregation](#metric-aggregation). P50, P95, P99 등.
→ [집계 개념](../concepts/aggregations/)

### Pipeline Aggregation
다른 집계 결과를 입력으로 사용하는 집계. `avg_bucket`, `derivative`, `cumulative_sum` 등.
→ [집계 개념](../concepts/aggregations/)

### prefix (쿼리)
접두사로 시작하는 값을 검색. 자동완성의 기본 방식.
→ [Query DSL](../concepts/query-dsl/)

### Primary Shard
원본 데이터가 저장되는 [Shard](#shard-샤드). 생성 후 개수 변경 불가. [Replica Shard](#replica-shard)의 원본.
→ [핵심 구성요소](../concepts/core-components/)

### Profile API
쿼리 실행 성능을 상세 분석하는 API. 각 단계별 소요 시간 확인.
→ [성능 튜닝](../concepts/performance-tuning/)

### Query Context
검색어와 [Document](#document-문서)의 관련성 [Score](#score-점수)를 계산. [Filter Context](#filter-context)와 함께 Bool 쿼리에서 사용.
→ [Query DSL](../concepts/query-dsl/)

### range (쿼리)
숫자/날짜 범위 검색. `gt`, `gte`, `lt`, `lte` 연산자. 상대 날짜(`now-7d`) 지원.
→ [Query DSL](../concepts/query-dsl/)

### Refresh
메모리 버퍼의 데이터를 검색 가능하게 만드는 작업. 기본 1초. [Bulk API](#bulk-api) 사용 시 조절 권장. [Flush](#flush)와 구분.
→ [인덱싱 전략](../concepts/indexing/) | [성능 튜닝](../concepts/performance-tuning/)

### Reindex
기존 [Index](#index-인덱스)를 새 인덱스로 복사/변환. [Mapping](#mapping-매핑) 변경, 데이터 마이그레이션에 사용.
→ [인덱싱 전략](../concepts/indexing/)

### Replica Shard
[Primary Shard](#primary-shard)의 복제본. 읽기 성능 향상과 장애 대비. [Cluster](#cluster-클러스터) 내 다른 [Node](#node-노드)에 배치.
→ [고가용성](../concepts/high-availability/)

### Repository (스냅샷)
[Snapshot](#snapshot)을 저장하는 위치. S3, GCS, 파일 시스템 등 지원.
→ [고가용성](../concepts/high-availability/)

### Rolling Restart
클러스터를 무중단으로 재시작하는 절차. 샤드 재할당 비활성화 후 노드별 순차 재시작.
→ [클러스터 관리](../concepts/cluster-management/)

---

## S-Z

### Score (점수)
검색어와 [Document](#document-문서)의 관련성을 나타내는 숫자. [BM25](#bm25-best-matching-25) 알고리즘으로 계산. [Boosting](#boosting)으로 조절 가능.
→ [검색 관련성](../concepts/search-relevance/)

### search_after
깊은 페이지네이션을 위한 방식. `from + size` 10,000건 제한 회피. 정렬 값 기반.
→ [Query DSL](../concepts/query-dsl/)

### Segment
[Index](#index-인덱스)를 구성하는 불변의 파일 조각. [Refresh](#refresh) 시 생성. Merge로 통합.
→ [성능 튜닝](../concepts/performance-tuning/)

### Segment Merge
여러 작은 [Segment](#segment)를 하나의 큰 세그먼트로 병합. 삭제된 문서 실제 제거. I/O 부하 발생.
→ [핵심 구성요소](../concepts/core-components/)

### Semantic Search (시맨틱 검색)
의미 기반 검색. 키워드가 달라도 의미가 유사한 문서를 찾음. [Vector Search](#vector-search)로 구현.
→ [Vector Search](../concepts/vector-search/)

### Shard (샤드)
[Index](#index-인덱스)를 수평 분할한 조각. 분산 저장과 병렬 처리 단위. [Primary](#primary-shard)와 [Replica](#replica-shard)로 구분.
→ [핵심 구성요소](../concepts/core-components/)

### Shard Allocation
[Shard](#shard-샤드)를 [Node](#node-노드)에 배치하는 과정. [Watermark](#watermark) 설정으로 제어.
→ [클러스터 관리](../concepts/cluster-management/)

### Shard Request Cache
집계 결과를 캐싱하는 샤드 레벨 캐시. 데이터 변경 시 무효화.
→ [성능 튜닝](../concepts/performance-tuning/)

### SLM (Snapshot Lifecycle Management)
[Snapshot](#snapshot) 생성/삭제를 자동화하는 정책. 보관 기간, 개수 제한 설정.
→ [고가용성](../concepts/high-availability/)

### Slow Log
지정 시간 이상 걸리는 쿼리/인덱싱을 기록하는 로그. 성능 병목 분석에 활용.
→ [성능 튜닝](../concepts/performance-tuning/)

### Snapshot
특정 시점의 [Index](#index-인덱스) 상태를 저장한 백업. 원격 저장소(S3, GCS 등)에 저장. [SLM](#slm-snapshot-lifecycle-management)으로 자동화.
→ [고가용성](../concepts/high-availability/)

### stats (집계)
count, min, max, avg, sum을 한 번에 계산하는 [Metric Aggregation](#metric-aggregation).
→ [집계 개념](../concepts/aggregations/)

### Synonym (동의어)
다른 단어지만 같은 의미로 검색되도록 설정. 동의어 필터로 구현.
→ [데이터 모델링](../concepts/data-modeling/) | [한글 검색](../concepts/korean-search/)

### term (쿼리)
분석 없이 정확한 값을 검색. [keyword](#keyword-타입) 필드에 사용. text 필드에 사용 금지.
→ [Query DSL](../concepts/query-dsl/)

### Term
[Analyzer](#analyzer-분석기) 처리 후 생성된 개별 토큰. [Inverted Index](#inverted-index-역색인)에 저장됨.
→ [데이터 모델링](../concepts/data-modeling/)

### terms (쿼리/집계)
여러 값 중 하나라도 일치하는 문서 검색. 집계에서는 값별 그룹화.
→ [Query DSL](../concepts/query-dsl/) | [집계](../concepts/aggregations/)

### text (타입)
[Analyzer](#analyzer-분석기)로 분석되는 문자열 타입. 풀텍스트 검색용. 정렬/집계 불가.
→ [데이터 모델링](../concepts/data-modeling/)

### TF (Term Frequency)
[Term](#term)이 [Document](#document-문서) 내에서 등장하는 빈도. [BM25](#bm25-best-matching-25)의 구성 요소.
→ [검색 관련성](../concepts/search-relevance/)

### Token Filter
[Tokenizer](#tokenizer) 출력을 변환하는 [Analyzer](#analyzer-분석기) 구성요소. 소문자 변환, 불용어 제거 등.
→ [데이터 모델링](../concepts/data-modeling/)

### Tokenizer
텍스트를 토큰으로 분해하는 [Analyzer](#analyzer-분석기)의 구성 요소. Standard, Whitespace, [Nori](#nori) 등.
→ [데이터 모델링](../concepts/data-modeling/)

### Translog
데이터 유실 방지를 위한 Write-Ahead Log. [Flush](#flush) 전까지 복구에 사용.
→ [고가용성](../concepts/high-availability/)

### User Dictionary (사용자 사전)
[Nori](#nori) 분석기에 커스텀 단어를 추가. 브랜드명, 신조어 등록.
→ [한글 검색](../concepts/korean-search/)

### Vector Search
[Embedding](#embedding-임베딩) 벡터를 이용한 의미 기반 검색. [kNN](#knn-k-nearest-neighbors) 알고리즘 사용. 시맨틱 검색, 유사 상품 추천에 활용.
→ [Vector Search](../concepts/vector-search/)

### Watcher
조건 기반 알림을 설정하는 기능 (Basic License+). 클러스터 상태 모니터링 등.
→ [고가용성](../concepts/high-availability/)

### Watermark (디스크)
디스크 사용량 임계값. low(85%), high(90%), flood_stage(95%)에서 샤드 할당 제한.
→ [클러스터 관리](../concepts/cluster-management/)

### wildcard (쿼리)
와일드카드(`*`, `?`) 패턴 검색. 앞에 `*`가 오면 매우 느림.
→ [Query DSL](../concepts/query-dsl/)

### Zone Awareness
Primary와 Replica를 다른 가용 영역(Zone)에 배치하여 장애 대비.
→ [고가용성](../concepts/high-availability/)

### 초성 검색
한글 자음(ㄱ, ㄴ, ㄷ...)으로 검색하는 기능. "ㄱㄹㅅ"로 "갤럭시" 검색. jaso-analyzer 플러그인 활용.
→ [한글 검색](../concepts/korean-search/)

### 형태소 분석
한글을 의미 단위로 분해하는 과정. 조사 분리, 어근 추출. [Nori](#nori) 분석기로 수행.
→ [한글 검색](../concepts/korean-search/)

---

## 약어 정리

| 약어 | 풀네임 | 의미 | 참고 |
|------|--------|------|------|
| BM25 | Best Matching 25 | 기본 스코어링 알고리즘 | [검색 관련성](../concepts/search-relevance/) |
| CCR | Cross-Cluster Replication | 클러스터 간 실시간 복제 | [고가용성](../concepts/high-availability/) |
| DSL | Domain Specific Language | 쿼리 언어 | [Query DSL](../concepts/query-dsl/) |
| HNSW | Hierarchical Navigable Small World | kNN 인덱싱 알고리즘 | [Vector Search](../concepts/vector-search/) |
| IDF | Inverse Document Frequency | 단어 희소성 지표 | [검색 관련성](../concepts/search-relevance/) |
| ILM | Index Lifecycle Management | 인덱스 수명주기 관리 | [인덱싱 전략](../concepts/indexing/) |
| kNN | k-Nearest Neighbors | k-최근접 이웃 검색 | [Vector Search](../concepts/vector-search/) |
| NDJSON | Newline Delimited JSON | Bulk API 형식 | [인덱싱 전략](../concepts/indexing/) |
| NRT | Near Real-Time | 준실시간 검색 | [핵심 구성요소](../concepts/core-components/) |
| SLM | Snapshot Lifecycle Management | 스냅샷 수명주기 관리 | [고가용성](../concepts/high-availability/) |
| TF | Term Frequency | 단어 빈도 지표 | [검색 관련성](../concepts/search-relevance/) |

---

## 다음 단계

- [개념 이해](../concepts/) - Elasticsearch 핵심 개념
- [Quick Start](../quick-start/) - 빠른 시작 가이드
- [참고 자료](../references/) - 공식 문서, 블로그
- [FAQ](../faq/) - 자주 묻는 질문
