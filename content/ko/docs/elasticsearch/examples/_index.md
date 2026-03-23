---
bookCollapseSection: true
title: 실습 예제
description: "Elasticsearch 실습 예제의 학습 가이드와 문서 목록입니다."
weight: 3
lastmod: 2026-01-08
---

Spring Boot 기반의 실행 가능한 예제 코드입니다.

{{< callout type="info" title="버전 정보" >}}
모든 예제는 **Elasticsearch 8.11.x**, **Spring Boot 3.2.x**, **Java 17+** 기준입니다.
{{< /callout >}}

## 예제 목록

### [환경 설정]({{< relref "/docs/elasticsearch/examples/setup" >}})
Docker로 Elasticsearch + Kibana를 구성하고, Spring Boot 프로젝트를 설정합니다.

### [기본 예제]({{< relref "/docs/elasticsearch/examples/basic" >}})
Spring Data Elasticsearch를 사용한 Document CRUD와 기본 검색을 구현합니다.

### [상품 검색 시스템]({{< relref "/docs/elasticsearch/examples/product-search" >}})
실제 서비스에 가까운 상품 검색 기능을 구현합니다:
- 한글 형태소 분석 (nori)
- 자동완성
- 필터 + 검색 조합
- 검색 결과 하이라이팅

### [로그 분석 시스템]({{< relref "/docs/elasticsearch/examples/log-analysis" >}})
애플리케이션 로그를 수집, 저장, 분석하는 시스템을 구현합니다:
- Logback → Elasticsearch 직접 전송
- 에러 로그 검색 및 요청 추적
- 에러율, 응답시간 분석 (집계)
- ILM을 사용한 로그 수명 관리
