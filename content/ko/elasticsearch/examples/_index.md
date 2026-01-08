---
title: 실습 예제
weight: 3
---

Spring Boot 기반의 실행 가능한 예제 코드입니다.

## 예제 목록

### [환경 설정](setup/)
Docker로 Elasticsearch + Kibana를 구성하고, Spring Boot 프로젝트를 설정합니다.

### [기본 예제](basic/)
Spring Data Elasticsearch를 사용한 Document CRUD와 기본 검색을 구현합니다.

### [상품 검색 시스템](product-search/)
실제 서비스에 가까운 상품 검색 기능을 구현합니다:
- 한글 형태소 분석 (nori)
- 자동완성
- 필터 + 검색 조합
- 검색 결과 하이라이팅
