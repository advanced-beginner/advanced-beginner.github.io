# 상품 검색 시스템 예제

한글 형태소 분석, 자동완성, 필터링을 포함한 Elasticsearch 기반 상품 검색 시스템입니다.

## 사전 요구사항

- Java 17+
- Docker & Docker Compose

## 실행 방법

### 1. Elasticsearch 시작 (Nori 플러그인 포함)

```bash
cd examples/elasticsearch/product-search/docker
docker-compose up -d
```

> Nori 플러그인 설치로 인해 첫 실행 시 1-2분 정도 소요됩니다.

### 2. Elasticsearch 상태 확인

```bash
curl http://localhost:9200/_cluster/health
```

`status`가 `green` 또는 `yellow`면 준비 완료입니다.

### 3. 애플리케이션 실행

```bash
cd examples/elasticsearch/product-search
./gradlew bootRun
```

애플리케이션이 시작되면 자동으로 인덱스를 생성하고 샘플 데이터를 추가합니다.

### 4. API 테스트

#### 기본 검색
```bash
curl "http://localhost:8080/api/products/search?keyword=맥북"
```

#### 필터 적용
```bash
curl "http://localhost:8080/api/products/search?keyword=프로&category=노트북&brands=Apple&minPrice=1000000&maxPrice=3000000"
```

#### 정렬
```bash
curl "http://localhost:8080/api/products/search?keyword=노트북&sortBy=price_asc"
```

#### 자동완성
```bash
curl "http://localhost:8080/api/products/autocomplete?q=맥북"
```

## 종료

```bash
# 애플리케이션: Ctrl+C

# Elasticsearch 종료
cd docker
docker-compose down

# 데이터도 삭제하려면
docker-compose down -v
```

## 프로젝트 구조

```
product-search/
├── build.gradle.kts              # Gradle 빌드 설정
├── docker/
│   └── docker-compose.yml        # Elasticsearch + Nori 플러그인
├── src/main/java/
│   └── com/example/productsearch/
│       ├── ProductSearchApplication.java  # 메인 클래스
│       ├── controller/
│       │   └── ProductController.java     # REST API
│       ├── service/
│       │   ├── ProductSearchService.java  # 검색 로직
│       │   └── DataInitializer.java       # 샘플 데이터 초기화
│       ├── domain/
│       │   └── Product.java               # 상품 엔티티
│       └── dto/
│           ├── SearchRequest.java         # 검색 요청
│           ├── SearchResult.java          # 검색 결과
│           └── ProductResponse.java       # 상품 응답
└── src/main/resources/
    ├── application.yml                    # 앱 설정
    └── elasticsearch/
        └── settings.json                  # Nori 분석기 설정
```

## 주요 기능

| 기능 | 설명 |
|------|------|
| 한글 검색 | Nori 분석기로 "삼성전자" → "삼성", "전자" 분리 |
| 자동완성 | match_phrase_prefix로 접두사 매칭 |
| 필터링 | 카테고리, 브랜드, 가격 범위 필터 |
| 패싯 | Aggregation으로 카테고리/브랜드별 개수 제공 |
| 하이라이팅 | 검색어 강조 표시 |

## 관련 문서

- [상품 검색 시스템 가이드](../../content/ko/docs/elasticsearch/examples/product-search.md)
- [한글 검색 최적화](../../content/ko/docs/elasticsearch/concepts/korean-search.md)
- [Query DSL](../../content/ko/docs/elasticsearch/concepts/query-dsl.md)
