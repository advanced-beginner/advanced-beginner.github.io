---
title: 용어 사전
weight: 1
lastmod: 2026-01-08
---

Elasticsearch 핵심 용어를 빠르게 찾아볼 수 있습니다. 상세 설명은 [개념 이해](../../concepts/) 섹션을 참고하세요.

## A-E

### Aggregation (집계)
검색 결과를 그룹화하고 통계를 계산하는 기능. SQL의 `GROUP BY`와 유사. Bucket/Metric/Pipeline 세 종류.
→ [집계 개념](../../concepts/aggregations/) | [Query DSL](../../concepts/query-dsl/)

### Alias (별칭)
[Index](#index-인덱스)에 부여하는 별명. 무중단 인덱스 전환, 멀티 인덱스 검색에 유용. [ILM](#ilm-index-lifecycle-management)과 함께 사용.
→ [인덱싱 전략](../../concepts/indexing/)

### Analyzer (분석기)
텍스트를 [Term](#term)으로 분해하는 컴포넌트. Character Filter → [Tokenizer](#tokenizer) → Token Filter 순서로 처리. 한글에는 [Nori](#nori) 분석기 사용.
→ [데이터 모델링](../../concepts/data-modeling/) | [한글 검색](../../concepts/korean-search/)

### BM25 (Best Matching 25)
Elasticsearch의 기본 [Score](#score-점수) 계산 알고리즘. [TF](#tf-term-frequency)와 [IDF](#idf-inverse-document-frequency) 기반. [Boosting](#boosting)으로 조절 가능.
→ [검색 관련성](../../concepts/search-relevance/)

### Boosting
특정 필드나 조건의 [Score](#score-점수)에 가중치를 부여하는 기법. 제목에 높은 부스트 값 적용 등.
→ [검색 관련성](../../concepts/search-relevance/)

### Bulk API
여러 [Document](#document-문서)를 한 번에 인덱싱하는 API. 성능 향상에 필수. [Refresh](#refresh) 제어와 함께 사용.
→ [인덱싱 전략](../../concepts/indexing/)

### Cluster (클러스터)
하나 이상의 [Node](#node-노드)로 구성된 Elasticsearch 서버 그룹. [Master Node](#master-node)가 상태 관리.
→ [핵심 구성요소](../../concepts/core-components/) | [클러스터 관리](../../concepts/cluster-management/)

### Coordinating Node
검색 요청을 받아 [Data Node](#data-node)에 분배하고 결과를 병합하는 노드. 모든 노드가 기본적으로 역할 수행.
→ [클러스터 관리](../../concepts/cluster-management/)

### Data Node
실제 데이터를 저장하고 검색/[Aggregation](#aggregation-집계)을 수행하는 노드. [Shard](#shard-샤드)가 할당됨.
→ [클러스터 관리](../../concepts/cluster-management/)

### Document (문서)
Elasticsearch에 저장되는 JSON 형태의 데이터 단위. RDB의 Row와 유사. [Index](#index-인덱스) 내에 저장됨.
→ [핵심 구성요소](../../concepts/core-components/)

### DSL (Domain Specific Language)
Elasticsearch [Query](#query-context) 작성을 위한 JSON 기반 언어. [Bool Query](#filter-context), Match, Term 등 다양한 쿼리 제공.
→ [Query DSL](../../concepts/query-dsl/)

---

## F-M

### Field (필드)
[Document](#document-문서) 내의 개별 데이터 항목. RDB의 Column과 유사. [Mapping](#mapping-매핑)으로 타입 정의.
→ [데이터 모델링](../../concepts/data-modeling/)

### Filter Context
[Score](#score-점수) 계산 없이 조건 매칭만 수행. 캐싱되어 성능 우수. [Query Context](#query-context)와 함께 Bool 쿼리에서 사용.
→ [Query DSL](../../concepts/query-dsl/)

### Flush
메모리 버퍼의 데이터를 디스크에 영구 저장하는 작업. [Translog](#translog) 초기화. [Refresh](#refresh)와 구분.
→ [인덱싱 전략](../../concepts/indexing/)

### IDF (Inverse Document Frequency)
단어가 전체 [Document](#document-문서)에서 얼마나 희귀한지를 나타내는 지표. [BM25](#bm25-best-matching-25)의 구성 요소.
→ [검색 관련성](../../concepts/search-relevance/)

### ILM (Index Lifecycle Management)
[Index](#index-인덱스)의 생성부터 삭제까지 수명주기를 자동 관리. Hot → Warm → Cold → Delete 단계.
→ [인덱싱 전략](../../concepts/indexing/)

### Index (인덱스)
[Document](#document-문서)들의 모음. RDB의 Table과 유사. [Shard](#shard-샤드)로 분산 저장.
→ [핵심 구성요소](../../concepts/core-components/)

### Inverted Index (역색인)
[Term](#term) → [Document](#document-문서) 위치를 매핑한 자료구조. 빠른 검색의 핵심.
→ [핵심 구성요소](../../concepts/core-components/)

### kNN (k-Nearest Neighbors)
벡터 유사도 기반 검색. [Vector Search](#vector-search)에서 가장 가까운 k개 문서를 찾는 알고리즘.
→ [Vector Search](../../concepts/vector-search/)

### Mapping (매핑)
[Document](#document-문서)와 [Field](#field-필드)의 저장/인덱싱 방식을 정의. RDB의 Schema와 유사. Dynamic/Explicit 두 방식.
→ [데이터 모델링](../../concepts/data-modeling/)

### Master Node
[Cluster](#cluster-클러스터) 상태를 관리하고 [Index](#index-인덱스) 생성/삭제를 담당하는 노드. [Data Node](#data-node)와 분리 권장.
→ [클러스터 관리](../../concepts/cluster-management/)

---

## N-R

### Node (노드)
[Cluster](#cluster-클러스터)를 구성하는 단일 Elasticsearch 서버. [Master](#master-node), [Data](#data-node), [Coordinating](#coordinating-node) 등 역할 구분.
→ [핵심 구성요소](../../concepts/core-components/) | [클러스터 관리](../../concepts/cluster-management/)

### Nori
Elasticsearch 공식 한글 형태소 [Analyzer](#analyzer-분석기). `nori_tokenizer`, `nori_part_of_speech` 필터 제공. 자동완성, 초성 검색 구현에 활용.
→ [한글 검색 최적화](../../concepts/korean-search/)

### Primary Shard
원본 데이터가 저장되는 [Shard](#shard-샤드). 생성 후 개수 변경 불가. [Replica Shard](#replica-shard)의 원본.
→ [핵심 구성요소](../../concepts/core-components/)

### Query Context
검색어와 [Document](#document-문서)의 관련성 [Score](#score-점수)를 계산. [Filter Context](#filter-context)와 함께 Bool 쿼리에서 사용.
→ [Query DSL](../../concepts/query-dsl/)

### Refresh
메모리 버퍼의 데이터를 검색 가능하게 만드는 작업. 기본 1초. [Bulk API](#bulk-api) 사용 시 조절 권장. [Flush](#flush)와 구분.
→ [인덱싱 전략](../../concepts/indexing/) | [성능 튜닝](../../concepts/performance-tuning/)

### Reindex
기존 [Index](#index-인덱스)를 새 인덱스로 복사/변환. [Mapping](#mapping-매핑) 변경, 데이터 마이그레이션에 사용.
→ [인덱싱 전략](../../concepts/indexing/)

### Replica Shard
[Primary Shard](#primary-shard)의 복제본. 읽기 성능 향상과 장애 대비. [Cluster](#cluster-클러스터) 내 다른 [Node](#node-노드)에 배치.
→ [고가용성](../../concepts/high-availability/)

---

## S-Z

### Score (점수)
검색어와 [Document](#document-문서)의 관련성을 나타내는 숫자. [BM25](#bm25-best-matching-25) 알고리즘으로 계산. [Boosting](#boosting)으로 조절 가능.
→ [검색 관련성](../../concepts/search-relevance/)

### Segment
[Index](#index-인덱스)를 구성하는 불변의 파일 조각. [Refresh](#refresh) 시 생성. Merge로 통합.
→ [성능 튜닝](../../concepts/performance-tuning/)

### Shard (샤드)
[Index](#index-인덱스)를 수평 분할한 조각. 분산 저장과 병렬 처리 단위. [Primary](#primary-shard)와 [Replica](#replica-shard)로 구분.
→ [핵심 구성요소](../../concepts/core-components/)

### Snapshot
특정 시점의 [Index](#index-인덱스) 상태를 저장한 백업. 원격 저장소(S3, GCS 등)에 저장. [SLM](#slm-snapshot-lifecycle-management)으로 자동화.
→ [고가용성](../../concepts/high-availability/)

### TF (Term Frequency)
[Term](#term)이 [Document](#document-문서) 내에서 등장하는 빈도. [BM25](#bm25-best-matching-25)의 구성 요소.
→ [검색 관련성](../../concepts/search-relevance/)

### Term
[Analyzer](#analyzer-분석기) 처리 후 생성된 개별 토큰. [Inverted Index](#inverted-index-역색인)에 저장됨.
→ [데이터 모델링](../../concepts/data-modeling/)

### Tokenizer
텍스트를 토큰으로 분해하는 [Analyzer](#analyzer-분석기)의 구성 요소. Standard, Whitespace, [Nori](#nori) 등.
→ [데이터 모델링](../../concepts/data-modeling/)

### Translog
데이터 유실 방지를 위한 Write-Ahead Log. [Flush](#flush) 전까지 복구에 사용.
→ [고가용성](../../concepts/high-availability/)

### Vector Search
임베딩 벡터를 이용한 의미 기반 검색. [kNN](#knn-k-nearest-neighbors) 알고리즘 사용. 시맨틱 검색, 유사 상품 추천에 활용.
→ [Vector Search](../../concepts/vector-search/)

---

## 약어 정리

| 약어 | 풀네임 | 의미 | 참고 |
|------|--------|------|------|
| BM25 | Best Matching 25 | 기본 스코어링 알고리즘 | [검색 관련성](../../concepts/search-relevance/) |
| CCR | Cross-Cluster Replication | 클러스터 간 실시간 복제 | [고가용성](../../concepts/high-availability/) |
| DSL | Domain Specific Language | 쿼리 언어 | [Query DSL](../../concepts/query-dsl/) |
| IDF | Inverse Document Frequency | 단어 희소성 지표 | [검색 관련성](../../concepts/search-relevance/) |
| ILM | Index Lifecycle Management | 인덱스 수명주기 관리 | [인덱싱 전략](../../concepts/indexing/) |
| kNN | k-Nearest Neighbors | k-최근접 이웃 검색 | [Vector Search](../../concepts/vector-search/) |
| SLM | Snapshot Lifecycle Management | 스냅샷 수명주기 관리 | [고가용성](../../concepts/high-availability/) |
| TF | Term Frequency | 단어 빈도 지표 | [검색 관련성](../../concepts/search-relevance/) |

---

## 다음 단계

- [개념 이해](../../concepts/) - Elasticsearch 핵심 개념
- [Quick Start](../../quick-start/) - 빠른 시작 가이드
- [참고 자료](../references/) - 공식 문서, 블로그
- [FAQ](../faq/) - 자주 묻는 질문
