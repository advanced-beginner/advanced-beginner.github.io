---
title: 참고 자료
weight: 3
---

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
