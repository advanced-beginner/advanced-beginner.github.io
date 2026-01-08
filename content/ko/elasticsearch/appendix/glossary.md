---
title: 용어 사전
weight: 1
lastmod: 2026-01-08
---

Elasticsearch 핵심 용어를 빠르게 찾아볼 수 있습니다.

## A-E

### Aggregation (집계)
검색 결과를 그룹화하고 통계를 계산하는 기능. SQL의 `GROUP BY`와 유사.
→ [집계 개념](../../concepts/aggregations/)

### Alias (별칭)
인덱스에 부여하는 별명. 무중단 인덱스 전환에 유용.

### Analyzer (분석기)
텍스트를 토큰으로 분해하는 컴포넌트. Character Filter → Tokenizer → Token Filter 순서로 처리.

### Bulk API
여러 문서를 한 번에 인덱싱하는 API. 성능 향상에 필수.

### Cluster (클러스터)
하나 이상의 노드로 구성된 Elasticsearch 서버 그룹.
→ [핵심 구성요소](../../concepts/core-components/)

### Coordinating Node
검색 요청을 받아 데이터 노드에 분배하고 결과를 병합하는 노드.

### Data Node
실제 데이터를 저장하고 검색/집계를 수행하는 노드.

### Document (문서)
Elasticsearch에 저장되는 JSON 형태의 데이터 단위. RDB의 Row와 유사.
→ [핵심 구성요소](../../concepts/core-components/)

### DSL (Domain Specific Language)
Elasticsearch 쿼리 작성을 위한 JSON 기반 언어.

---

## F-M

### Field (필드)
문서 내의 개별 데이터 항목. RDB의 Column과 유사.

### Filter Context
Score 계산 없이 조건 매칭만 수행. 캐싱됨.

### Flush
메모리 버퍼의 데이터를 디스크에 영구 저장하는 작업.

### ILM (Index Lifecycle Management)
인덱스의 생성부터 삭제까지 수명주기를 자동 관리.

### Index (인덱스)
문서들의 모음. RDB의 Table과 유사.

### Inverted Index (역색인)
단어 → 문서 위치를 매핑한 자료구조. 빠른 검색의 핵심.
→ [핵심 구성요소](../../concepts/core-components/)

### Mapping (매핑)
문서와 필드의 저장/인덱싱 방식을 정의. RDB의 Schema와 유사.
→ [데이터 모델링](../../concepts/data-modeling/)

### Master Node
클러스터 상태를 관리하고 인덱스 생성/삭제를 담당하는 노드.

---

## N-R

### Node (노드)
클러스터를 구성하는 단일 Elasticsearch 서버.

### Nori
Elasticsearch 공식 한글 형태소 분석기.

### Primary Shard
원본 데이터가 저장되는 샤드. 생성 후 개수 변경 불가.

### Query Context
검색어와 문서의 관련성 Score를 계산.

### Refresh
메모리 버퍼의 데이터를 검색 가능하게 만드는 작업. 기본 1초.

### Reindex
기존 인덱스를 새 인덱스로 복사/변환.

### Replica Shard
Primary Shard의 복제본. 읽기 성능 향상과 장애 대비.

---

## S-Z

### Score (점수)
검색어와 문서의 관련성을 나타내는 숫자. BM25 알고리즘으로 계산.

### Segment
인덱스를 구성하는 불변의 파일 조각.

### Shard (샤드)
인덱스를 수평 분할한 조각. 분산 저장과 병렬 처리 단위.
→ [핵심 구성요소](../../concepts/core-components/)

### Snapshot
특정 시점의 인덱스 상태를 저장한 백업.

### Term
분석 후 생성된 개별 토큰.

### Tokenizer
텍스트를 토큰으로 분해하는 컴포넌트.

### Translog
데이터 유실 방지를 위한 Write-Ahead Log.

---

## 약어 정리

| 약어 | 풀네임 | 의미 |
|------|--------|------|
| BM25 | Best Matching 25 | 기본 스코어링 알고리즘 |
| CCR | Cross-Cluster Replication | 클러스터 간 실시간 복제 |
| DSL | Domain Specific Language | 쿼리 언어 |
| IDF | Inverse Document Frequency | 단어 희소성 지표 |
| ILM | Index Lifecycle Management | 인덱스 수명주기 관리 |
| SLM | Snapshot Lifecycle Management | 스냅샷 수명주기 관리 |
| TF | Term Frequency | 단어 빈도 지표 |
