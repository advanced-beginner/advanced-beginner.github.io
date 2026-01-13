# 로그 분석 시스템 예제

Elasticsearch를 사용하여 애플리케이션 로그를 수집, 저장, 분석하는 시스템입니다.

## 사전 요구사항

- Java 17+
- Docker & Docker Compose

## 실행 방법

### 1. Elasticsearch + Kibana 시작

```bash
cd examples/elasticsearch/log-analysis/docker
docker-compose up -d
```

### 2. 서비스 상태 확인

```bash
# Elasticsearch
curl http://localhost:9200/_cluster/health

# Kibana (브라우저에서)
open http://localhost:5601
```

### 3. 애플리케이션 실행

```bash
cd examples/elasticsearch/log-analysis
./gradlew bootRun
```

### 4. 샘플 로그 생성

```bash
# 100개의 샘플 로그 생성
curl "http://localhost:8080/api/demo/generate-logs?count=100"

# 다양한 시나리오 테스트
curl http://localhost:8080/api/demo/success
curl "http://localhost:8080/api/demo/slow?delayMs=3000"
curl http://localhost:8080/api/demo/random
curl http://localhost:8080/api/demo/error  # 에러 발생
```

### 5. 로그 분석 API 테스트

#### 에러 로그 검색
```bash
curl "http://localhost:8080/api/logs/errors?from=2024-01-01T00:00:00&to=2024-12-31T23:59:59"
```

#### 요청 추적
```bash
curl "http://localhost:8080/api/logs/trace/{requestId}"
```

#### 시간대별 에러 수
```bash
curl "http://localhost:8080/api/logs/analytics/errors-by-hour?from=2024-01-01T00:00:00&to=2024-12-31T23:59:59"
```

#### 에러율 조회
```bash
curl "http://localhost:8080/api/logs/analytics/error-rate?from=2024-01-01T00:00:00&to=2024-12-31T23:59:59"
```

#### 느린 요청 Top N
```bash
curl "http://localhost:8080/api/logs/analytics/slow-requests?topN=10&thresholdMs=1000"
```

## Kibana 대시보드 설정

1. http://localhost:5601 접속
2. Management → Stack Management → Index Patterns
3. `logs-*` 패턴 생성, Time field: `@timestamp`
4. Discover에서 로그 확인
5. Visualize에서 차트 생성:
   - 에러율 추이 (Line Chart)
   - 로그 레벨 분포 (Pie Chart)
   - HTTP 상태 코드 (Bar Chart)

## 종료

```bash
# 애플리케이션: Ctrl+C

# Elasticsearch + Kibana 종료
cd docker
docker-compose down

# 데이터도 삭제하려면
docker-compose down -v
```

## 프로젝트 구조

```
log-analysis/
├── build.gradle.kts              # Gradle 빌드 설정
├── docker/
│   └── docker-compose.yml        # Elasticsearch + Kibana
├── src/main/java/
│   └── com/example/loganalysis/
│       ├── LogAnalysisApplication.java  # 메인 클래스
│       ├── controller/
│       │   ├── LogController.java       # 로그 검색/분석 API
│       │   └── DemoController.java      # 샘플 로그 생성
│       ├── service/
│       │   ├── LogSearchService.java    # 로그 검색 서비스
│       │   └── LogAnalyticsService.java # 로그 분석 서비스
│       ├── filter/
│       │   └── RequestTrackingFilter.java  # MDC 설정
│       └── domain/
│           └── LogEntry.java            # 로그 엔티티
└── src/main/resources/
    ├── application.yml                  # 앱 설정
    └── logback-spring.xml               # JSON 로깅 설정
```

## 주요 기능

| 기능 | 설명 |
|------|------|
| 요청 추적 | MDC로 requestId, userId 자동 포함 |
| 에러 검색 | 시간 범위, 메시지 검색 지원 |
| 로그 분석 | 시간대별 에러 수, 에러율, 느린 요청 |
| JSON 로깅 | Logstash 형식으로 Elasticsearch 연동 용이 |
| Kibana | 대시보드로 시각화 |

## 관련 문서

- [로그 분석 시스템 가이드](../../content/ko/docs/elasticsearch/examples/log-analysis.md)
- [집계](../../content/ko/docs/elasticsearch/concepts/aggregations.md)
- [고가용성](../../content/ko/docs/elasticsearch/concepts/high-availability.md)
