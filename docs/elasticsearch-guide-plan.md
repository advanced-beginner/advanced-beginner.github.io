# Elasticsearch 가이드 작성 계획

## 개요

기존 Kafka, Scala, Spark 가이드와 동일한 구조로 Elasticsearch 실무 가이드를 작성합니다.

- **대상 독자**: Java/Spring 개발자, 검색 기능 구현이 필요한 백엔드 개발자
- **목표**: 검색 엔진 개념부터 실무 적용까지 체계적으로 학습
- **스택**: Elasticsearch 8.x, Spring Data Elasticsearch, Docker

## 디렉토리 구조

```
content/ko/elasticsearch/
├── _index.md                    # 메인 페이지
├── quick-start/
│   └── _index.md               # 5분 만에 시작하기
├── concepts/
│   ├── _index.md               # 개념 목차
│   ├── core-components.md      # 핵심 구성요소
│   ├── data-modeling.md        # 데이터 모델링
│   ├── query-dsl.md            # Query DSL
│   ├── search-relevance.md     # 검색 관련성
│   ├── aggregations.md         # 집계
│   ├── indexing.md             # 인덱싱 전략
│   ├── cluster-management.md   # 클러스터 관리
│   ├── performance-tuning.md   # 성능 튜닝
│   └── high-availability.md    # 고가용성
├── examples/
│   ├── _index.md               # 예제 목차
│   ├── setup.md                # 환경 설정
│   ├── basic.md                # 기본 CRUD 예제
│   └── product-search.md       # 상품 검색 시스템
└── appendix/
    ├── _index.md               # 부록 목차
    ├── glossary.md             # 용어 사전
    ├── references.md           # 참고 자료
    └── faq.md                  # FAQ
```

## 문서별 내용 개요

### 1. 메인 페이지 (`_index.md`)

- Elasticsearch란?
- RDB vs Elasticsearch 비교
- 언제 Elasticsearch를 써야 할까?
- 이 가이드에서 다루는 것 (목차)
- 선수 지식
- 학습 경로 제안

### 2. Quick Start (`quick-start/_index.md`)

- Docker로 Elasticsearch 시작
- Kibana Dev Tools 접속
- 첫 번째 문서 인덱싱
- 간단한 검색 수행
- Spring Boot 연동 맛보기

### 3. 개념 이해 (`concepts/`)

#### 3.1 핵심 구성요소 (`core-components.md`)
| 주제 | 내용 |
|------|------|
| Cluster | 노드 그룹, 고가용성 |
| Node | Master, Data, Coordinating 역할 |
| Index | RDB의 테이블과 유사 |
| Document | JSON 형태의 데이터 단위 |
| Shard | 데이터 분산 단위 (Primary/Replica) |

#### 3.2 데이터 모델링 (`data-modeling.md`)
- Mapping (스키마 정의)
- Field Types (text, keyword, number, date, nested, object)
- Analyzer 기초 (Standard, Korean)
- Dynamic vs Explicit Mapping

#### 3.3 Query DSL (`query-dsl.md`)
- Match Query (풀텍스트 검색)
- Term Query (정확한 값 매칭)
- Bool Query (복합 조건)
- Range Query
- Multi-match, Wildcard, Fuzzy

#### 3.4 검색 관련성 (`search-relevance.md`)
- Score란?
- TF-IDF와 BM25
- Boosting (가중치)
- Function Score Query
- 검색 결과 튜닝 전략

#### 3.5 집계 (`aggregations.md`)
- Bucket Aggregations (terms, range, date_histogram)
- Metric Aggregations (avg, sum, min, max, cardinality)
- Pipeline Aggregations
- 중첩 집계
- 집계와 검색 결합

#### 3.6 인덱싱 전략 (`indexing.md`)
- 단건 vs Bulk 인덱싱
- Refresh Interval
- Flush와 Translog
- 인덱스 템플릿
- 인덱스 수명주기 관리 (ILM)

#### 3.7 클러스터 관리 (`cluster-management.md`)
- 클러스터 상태 (Green/Yellow/Red)
- 샤드 할당과 라우팅
- 노드 추가/제거
- Rolling Restart
- 클러스터 설정

#### 3.8 성능 튜닝 (`performance-tuning.md`)
- 인덱스 설계 최적화
- 쿼리 최적화 (Filter Context 활용)
- 캐싱 전략
- JVM 설정
- 느린 쿼리 분석

#### 3.9 고가용성 (`high-availability.md`)
- Replica Shard
- Snapshot과 Restore
- Cross-Cluster Replication
- 장애 시나리오와 대응

### 4. 실습 예제 (`examples/`)

#### 4.1 환경 설정 (`setup.md`)
- Docker Compose로 Elasticsearch + Kibana 실행
- Spring Boot 프로젝트 설정
- Spring Data Elasticsearch 의존성

#### 4.2 기본 예제 (`basic.md`)
- Document CRUD (Create, Read, Update, Delete)
- ElasticsearchRepository 사용
- ElasticsearchOperations 사용
- 기본 검색 구현

#### 4.3 상품 검색 시스템 (`product-search.md`)
- 상품 도메인 모델링
- 한글 검색 (nori analyzer)
- 자동완성 (Completion Suggester)
- 필터 + 검색 조합
- 페이지네이션과 정렬
- 검색 결과 하이라이팅

### 5. 부록 (`appendix/`)

#### 5.1 용어 사전 (`glossary.md`)
- Elasticsearch 핵심 용어 정리

#### 5.2 참고 자료 (`references.md`)
- 공식 문서 링크
- 추천 도서/강의
- 커뮤니티 리소스

#### 5.3 FAQ (`faq.md`)
- 자주 묻는 질문과 답변

## 예제 프로젝트 구조

```
examples/elasticsearch-quick-start/
├── build.gradle.kts
├── src/main/java/.../
│   ├── ElasticsearchQuickStartApplication.java
│   ├── config/ElasticsearchConfig.java
│   ├── domain/Product.java
│   ├── repository/ProductRepository.java
│   └── controller/ProductController.java
├── src/main/resources/
│   └── application.yml
└── docker/
    └── docker-compose.yml
```

## 작성 순서 (권장)

1. **Phase 1 - 기반 구축**
   - [ ] `_index.md` (메인 페이지)
   - [ ] `quick-start/_index.md`
   - [ ] `examples/setup.md`
   - [ ] Docker Compose 파일

2. **Phase 2 - 핵심 개념**
   - [ ] `concepts/core-components.md`
   - [ ] `concepts/data-modeling.md`
   - [ ] `concepts/query-dsl.md`
   - [ ] `examples/basic.md`

3. **Phase 3 - 심화 개념**
   - [ ] `concepts/search-relevance.md`
   - [ ] `concepts/aggregations.md`
   - [ ] `concepts/indexing.md`

4. **Phase 4 - 운영**
   - [ ] `concepts/cluster-management.md`
   - [ ] `concepts/performance-tuning.md`
   - [ ] `concepts/high-availability.md`

5. **Phase 5 - 실전 예제**
   - [ ] `examples/product-search.md`
   - [ ] 예제 프로젝트 코드

6. **Phase 6 - 부록**
   - [ ] `appendix/glossary.md`
   - [ ] `appendix/references.md`
   - [ ] `appendix/faq.md`

## 기술 스택 버전

| 구성요소 | 버전 |
|----------|------|
| Elasticsearch | 8.x |
| Kibana | 8.x |
| Spring Boot | 3.2.x |
| Spring Data Elasticsearch | 5.x |
| Java | 17 |

## 차별화 포인트

1. **RDB 비교**: RDB 개발자가 쉽게 이해할 수 있도록 SQL과 비교
2. **한글 검색**: nori analyzer를 활용한 한글 검색 상세 설명
3. **실무 중심**: 이론보다 실제 사용 패턴 강조
4. **트러블슈팅**: 흔한 문제와 해결 방법 포함

## 참고 사항

- 모든 문서는 한글로 작성 (기술 용어는 영어 허용)
- Mermaid.js로 아키텍처/흐름 다이어그램 작성
- 코드 예제는 실행 가능한 형태로 제공
- 기존 Kafka 가이드의 톤앤매너 유지
