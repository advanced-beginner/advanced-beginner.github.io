---
title: Elasticsearch
description: 분산 검색 엔진 Elasticsearch 실무 가이드 - 검색, 집계, 데이터 모델링, 성능 튜닝까지
weight: 5
lastmod: 2026-01-08
---

{{% notice style="info" title="버전 정보" %}}
이 가이드는 다음 버전을 기준으로 작성되었습니다:
- **Elasticsearch**: 8.11.x
- **Kibana**: 8.11.x
- **Spring Boot**: 3.2.x
- **Spring Data Elasticsearch**: 5.2.x
- **Java**: 17+

다른 버전에서는 일부 API나 설정이 다를 수 있습니다. 특히 Elasticsearch 7.x와 8.x는 보안 설정, 클라이언트 API 등에서 큰 차이가 있습니다.
{{% /notice %}}

## Elasticsearch란?

Elasticsearch는 **분산 검색 및 분석 엔진**입니다. 대용량 데이터에서 빠르게 검색하고, 실시간으로 분석할 수 있게 해주는 도구입니다.

### 왜 Elasticsearch가 필요한가?

RDB의 `LIKE '%검색어%'`로 검색하면 어떤 문제가 생길까요?

| RDB 검색의 한계 | Elasticsearch 도입 후 |
|----------------|----------------------|
| 풀 테이블 스캔으로 느림 | 역색인(Inverted Index)으로 밀리초 검색 |
| 형태소 분석 불가 | "삼성전자" 검색 시 "삼성", "전자" 모두 매칭 |
| 오타 허용 안 됨 | Fuzzy 검색으로 "삼섬전자"도 찾음 |
| 연관도 정렬 불가 | 검색 관련성 점수로 정확한 결과 상위 노출 |
| 단일 서버 한계 | 샤딩으로 수평 확장, 수십억 건 처리 |

Elasticsearch는 이런 문제들을 해결하면서도 **실시간 검색**, **복잡한 집계**, **고가용성**을 제공합니다.

### 언제 Elasticsearch를 써야 할까?

**적합한 경우:**
- 상품, 게시글 등 **풀텍스트 검색**이 필요할 때
- 로그, 메트릭 등 **시계열 데이터 분석**이 필요할 때
- 대시보드용 **실시간 집계**가 필요할 때
- **자동완성**, 오타 교정, 동의어 처리가 필요할 때
- 데이터가 많아 RDB 검색이 느려졌을 때

**과할 수 있는 경우:**
- 단순 CRUD만 필요할 때 (RDB로 충분)
- 트랜잭션 무결성이 중요할 때 (Elasticsearch는 Eventually Consistent)
- 데이터가 적고 검색 요구사항이 단순할 때
- 운영 인프라를 갖출 여력이 없을 때

### Elasticsearch의 한계와 주의점

Elasticsearch를 도입하기 전에 알아야 할 **현실적인 단점**입니다:

| 한계 | 설명 | 대응 방안 |
|------|------|----------|
| **운영 복잡도** | 클러스터 관리, 샤드 리밸런싱, JVM 튜닝 필요 | 전담 인력 또는 관리형 서비스 고려 |
| **비용** | 메모리 집약적, 노드당 최소 4GB+ 권장 | 데이터 규모에 맞는 용량 산정 |
| **데이터 일관성** | Eventually Consistent, 실시간 반영 아님 (기본 1초 refresh) | 실시간성이 중요하면 refresh 설정 조정 |
| **스키마 변경** | 기존 필드 타입 변경 불가, 재인덱싱 필요 | 초기 Mapping 설계 중요 |
| **JOIN 미지원** | 테이블 간 JOIN 불가능, 비정규화 필수 | 데이터 모델 재설계, Application-side JOIN |
| **트랜잭션 미지원** | ACID 트랜잭션 없음 | RDB를 Primary로, ES는 검색용으로 분리 |
| **학습 곡선** | Query DSL, Mapping, 분석기 등 학습 필요 | 팀 역량 고려 |

> **실무 조언:** Elasticsearch는 "검색 전용 보조 저장소"로 사용하고, RDB를 메인 저장소로 유지하는 패턴이 가장 안전합니다. ES 장애 시에도 서비스 핵심 기능은 유지됩니다.

### 대안 기술 비교

Elasticsearch만이 유일한 선택지는 아닙니다:

| 기술 | 특징 | 적합한 경우 |
|------|------|------------|
| **Elasticsearch** | 풀스택 검색/분석, 가장 많은 기능 | 대규모 검색, 로그 분석, 복잡한 집계 |
| **OpenSearch** | ES 7.10 포크, AWS 관리형 제공 | AWS 환경, 라이선스 우려 시 |
| **Apache Solr** | 오래된 역사, 안정성 검증 | 전통적 엔터프라이즈 환경 |
| **Meilisearch** | 설정 간소, 빠른 시작 | 소규모, 프로토타입, 타이핑 실시간 검색 |
| **Typesense** | 쉬운 설정, 오타 허용 기본 내장 | 소규모 서비스, 빠른 구현 필요 |
| **PostgreSQL FTS** | 별도 시스템 불필요 | 단순 검색, 이미 PG 사용 중 |

> **선택 기준:** 데이터 규모 < 100만 건, 검색 요구사항이 단순하면 PostgreSQL FTS나 Meilisearch로 충분합니다. Elasticsearch는 대규모, 복잡한 요구사항에 적합합니다.

### RDB vs Elasticsearch

| 개념 | RDB | Elasticsearch |
|------|-----|---------------|
| 저장 단위 | Row | Document (JSON) |
| 스키마 | Table Schema | Mapping |
| 테이블 | Table | Index |
| 컬럼 | Column | Field |
| 인덱스 | B-Tree Index | Inverted Index |
| 조인 | JOIN | Nested, Parent-Child (제한적) |
| 트랜잭션 | ACID | Eventually Consistent |

> **핵심 차이:** RDB는 정규화된 데이터의 정확한 조회에, Elasticsearch는 비정규화된 데이터의 빠른 검색에 최적화되어 있습니다.

## 이 가이드에서 다루는 것

### [Quick Start](quick-start/)
5분 만에 Elasticsearch에 데이터를 넣고 검색해봅니다. 개념보다 먼저 동작하는 것을 확인하세요.

### [개념 이해](concepts/)
단순히 "이렇게 쓰세요"가 아닌, **왜 이렇게 동작하는지** 원리를 설명합니다.

| 주제 | 배우는 것 |
|------|----------|
| [핵심 구성요소](concepts/core-components/) | Cluster, Node, Index, Document, Shard의 역할과 관계 |
| [데이터 모델링](concepts/data-modeling/) | Mapping, Field Type, Analyzer 설계 |
| [Query DSL](concepts/query-dsl/) | Match, Term, Bool 등 검색 쿼리 작성법 |
| [검색 관련성](concepts/search-relevance/) | Score, BM25, Boosting으로 검색 품질 높이기 |
| [집계](concepts/aggregations/) | Bucket, Metric 집계로 데이터 분석하기 |
| [인덱싱 전략](concepts/indexing/) | Bulk 인덱싱, Refresh, ILM 설정 |
| [클러스터 관리](concepts/cluster-management/) | 노드 구성, 샤드 할당, 상태 모니터링 |
| [성능 튜닝](concepts/performance-tuning/) | 쿼리 최적화, 캐싱, JVM 설정 |
| [고가용성](concepts/high-availability/) | Replica, Snapshot, 장애 대응 |

### [실습 예제](examples/)
Spring Boot 기반의 실행 가능한 예제 코드입니다.

- [환경 설정](examples/setup/) - Docker로 Elasticsearch + Kibana 구성
- [기본 예제](examples/basic/) - Document CRUD와 기본 검색 구현
- [상품 검색 시스템](examples/product-search/) - 한글 검색, 자동완성, 필터링 구현

### [부록](appendix/)
- [용어 사전](appendix/glossary/) - Elasticsearch 용어 빠른 참조
- [FAQ](appendix/faq/) - 자주 묻는 질문
- [참고 자료](appendix/references/) - 공식 문서 및 추가 학습 자료

## 선수 지식

- **필수**: REST API 기본, JSON 형식 이해
- **도움됨**: Java/Spring Boot 경험, RDB 사용 경험

## 학습 경로 제안

```
처음이라면:     Quick Start → 핵심 구성요소 → 데이터 모델링 → 기본 예제
검색 구현:      Query DSL → 검색 관련성 → 상품 검색 시스템
데이터 분석:    집계 → 인덱싱 전략
운영 준비:      클러스터 관리 → 성능 튜닝 → 고가용성
```

각 문서는 독립적으로 읽을 수 있지만, 처음이라면 위 순서를 추천합니다.
