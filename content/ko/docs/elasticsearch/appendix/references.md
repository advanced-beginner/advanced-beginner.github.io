---
title: 참고 자료
weight: 3
lastmod: 2026-01-10
---

{{< callout type="tip" title="TL;DR" >}}
- **공식 문서**: Elastic 공식 가이드, Spring Data Elasticsearch 문서
- **한글 자료**: 우아한형제들, 카카오, 네이버 D2 기술 블로그의 실무 사례
- **클라우드 서비스**: Elastic Cloud(공식), Amazon OpenSearch(AWS 통합), Self-Managed(완전 통제)
- **학습 경로**: 공식 무료 교육 → 한글 블로그 → Elastic 인증
{{< /callout >}}

공식 문서, 추천 도서, 커뮤니티 리소스 링크입니다.

## 공식 문서

### Elasticsearch

- [Elasticsearch 공식 가이드](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Elasticsearch 클라이언트 문서](https://www.elastic.co/guide/en/elasticsearch/client/index.html)
- [REST API 레퍼런스](https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html)
- [Query DSL 레퍼런스](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl.html)

### Kibana

- [Kibana 공식 가이드](https://www.elastic.co/guide/en/kibana/current/index.html)
- [Dev Tools 사용법](https://www.elastic.co/guide/en/kibana/current/console-kibana.html)

### Spring Data Elasticsearch

- [Spring Data Elasticsearch 공식 문서](https://docs.spring.io/spring-data/elasticsearch/reference/)
- [Spring Data Elasticsearch GitHub](https://github.com/spring-projects/spring-data-elasticsearch)

## 추천 도서

### 입문서

| 도서 | 저자 | 특징 |
|------|------|------|
| Elasticsearch in Action (2nd Ed) | Madhusudhan Konda | 실전 예제 중심 |
| Learning Elastic Stack 8.0 | Pranav Shukla | ELK 스택 전체 학습 |

### 심화서

| 도서 | 저자 | 특징 |
|------|------|------|
| Elasticsearch: The Definitive Guide | Clinton Gormley | 원리 이해 (구버전이지만 개념 학습에 유용) |
| Relevant Search | Doug Turnbull | 검색 관련성 최적화 |

## 온라인 강의

### 무료

- [Elastic 공식 교육](https://www.elastic.co/training/free) - 기초 과정 무료
- [YouTube: Elastic 공식 채널](https://www.youtube.com/c/Aborla)

### 유료

- [Elastic 인증 과정](https://www.elastic.co/training/certification)
- Udemy, Coursera 등의 Elasticsearch 강의

## 커뮤니티

### 포럼 & Q&A

- [Elastic Discuss](https://discuss.elastic.co/) - 공식 포럼
- [Stack Overflow elasticsearch 태그](https://stackoverflow.com/questions/tagged/elasticsearch)

### 한국 커뮤니티

- [Elastic 한국 사용자 그룹](https://www.facebook.com/groups/elasticsearch.kr/)
- [한국 Elasticsearch 밋업](https://www.meetup.com/ko-KR/elasticsearch-korea/)

### 한글 학습 자료

#### 블로그 & 아티클

| 주제 | 링크 | 특징 |
|------|------|------|
| ES 기초 | [우아한형제들 기술블로그](https://techblog.woowahan.com/) | 실무 적용 사례 |
| 검색 최적화 | [카카오 기술블로그](https://tech.kakao.com/) | 대규모 검색 경험 |
| 로그 분석 | [네이버 D2](https://d2.naver.com/) | ELK 스택 운영 |
| 인프라 | [당근마켓 기술블로그](https://medium.com/daangn) | 스타트업 ES 도입기 |

#### 추천 한글 글

- "Elasticsearch 검색 품질 개선기" - 검색 관련성 튜닝 실전
- "1억 건 데이터 ES 마이그레이션" - 대용량 인덱싱 최적화
- "ES 클러스터 무중단 업그레이드" - 운영 중 버전 업그레이드
- "Nori 분석기 커스터마이징" - 한글 검색 품질 개선

#### 국내 도서

| 도서 | 특징 |
|------|------|
| 엘라스틱서치 실무 가이드 | 한글, 실무 중심 |
| 기초부터 다지는 ElasticSearch | 입문자 친화적 |

> **팁**: 영문 공식 문서와 한글 블로그를 병행하면 이해가 빠릅니다.

### GitHub

- [Elasticsearch GitHub](https://github.com/elastic/elasticsearch)
- [한글 형태소 분석기 Nori](https://github.com/elastic/elasticsearch/tree/main/plugins/analysis-nori)

## 블로그 & 아티클

### Elastic 공식

- [Elastic 블로그](https://www.elastic.co/blog/)
- [Elastic Engineering 블로그](https://www.elastic.co/blog/category/engineering)

### 기술 블로그 추천 글

- 검색 품질 개선 사례
- 대용량 인덱싱 최적화
- 클러스터 운영 경험

## 도구

### 개발/테스트

| 도구 | 용도 |
|------|------|
| Kibana Dev Tools | API 테스트, 쿼리 작성 |
| Elasticsearch Head | 클러스터 시각화 (Chrome 확장) |
| Cerebro | 클러스터 관리 UI |

### 모니터링

| 도구 | 용도 |
|------|------|
| Kibana Stack Monitoring | 공식 모니터링 |
| Grafana + Prometheus | 커스텀 대시보드 |
| Elastic APM | 애플리케이션 성능 모니터링 |

### 데이터 동기화

| 도구 | 용도 |
|------|------|
| Logstash | 다양한 소스에서 데이터 수집 |
| Debezium | CDC 기반 DB → Elasticsearch 동기화 |
| Kafka Connect | Kafka ↔ Elasticsearch 연동 |

## 클라우드 서비스 비교

직접 운영 대신 관리형 서비스를 고려할 수 있습니다.

### 주요 서비스 비교

| 서비스 | 제공사 | 특징 | 가격대 |
|--------|--------|------|--------|
| **Elastic Cloud** | Elastic | 공식 서비스, 최신 기능 먼저 지원 | 중상 |
| **Amazon OpenSearch** | AWS | AWS 통합, OpenSearch 기반 | 중 |
| **Azure Cognitive Search** | Microsoft | AI 기능 통합, Azure 연동 | 중상 |
| **Google Cloud Elasticsearch** | GCP | Elastic 파트너십 | 중 |

### Elastic Cloud vs Self-Managed

| 항목 | Elastic Cloud | Self-Managed |
|------|---------------|--------------|
| **초기 설정** | 분 단위 | 시간~일 단위 |
| **운영 부담** | 낮음 | 높음 |
| **비용** | 높음 (시간당 과금) | 인프라비만 |
| **커스터마이징** | 제한적 | 완전 자유 |
| **업그레이드** | 자동/간편 | 수동 계획 필요 |
| **보안 설정** | 기본 제공 | 직접 구성 |

### Amazon OpenSearch vs Elasticsearch

| 항목 | Amazon OpenSearch | Elasticsearch |
|------|-------------------|---------------|
| **기반** | ES 7.10 포크 | 원본 |
| **라이선스** | Apache 2.0 | Elastic License / SSPL |
| **최신 기능** | 지연될 수 있음 | 먼저 지원 |
| **AWS 통합** | 우수 | 별도 설정 필요 |
| **비용** | AWS 종량제 | 직접 운영 또는 Elastic Cloud |

### 선택 가이드

```
AWS 환경 + 비용 최적화 → Amazon OpenSearch
최신 기능 + 공식 지원 → Elastic Cloud
완전한 통제권 필요 → Self-Managed
Azure/GCP 환경 → 각 클라우드의 관리형 서비스
```

> **팁**: 소규모 시작은 Self-Managed, 규모 확대 시 관리형 서비스 검토

---

## 버전별 변경사항

### Elasticsearch 8.x 주요 변경

- 보안 기본 활성화
- Java API Client (기존 High-Level REST Client 대체)
- 벡터 검색 (kNN) 강화
- 새로운 라이선스 체계 (SSPL + Elastic License 2.0)

### Spring Data Elasticsearch 5.x

- Elasticsearch Java Client 사용 (기존 RestHighLevelClient 대체)
- Reactive 지원 강화
- Elasticsearch 8.x 호환

## 인증

### Elastic Certified Engineer

- [인증 안내](https://www.elastic.co/training/certification/engineer)
- 실습 기반 시험
- Elasticsearch 운영 능력 검증

### Elastic Certified Analyst

- [인증 안내](https://www.elastic.co/training/certification/analyst)
- Kibana 활용 능력 검증
- 데이터 시각화, 대시보드 구축
