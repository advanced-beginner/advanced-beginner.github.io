---
title: 로그 분석 시스템
description: "Elasticsearch 기반 로그 분석 시스템을 단계별로 구현합니다."
weight: 4
lastmod: 2026-01-10
prerequisites:
  - title: 집계
    path: /docs/elasticsearch/concepts/aggregations/
  - title: 인덱싱 전략
    path: /docs/elasticsearch/concepts/indexing/
---

{{< callout type="warning" title="완전한 예제 프로젝트" >}}
이 문서의 코드를 바로 실행해보고 싶다면, **완전한 Spring Boot 프로젝트**를 사용하세요:
- 📁 [examples/elasticsearch/log-analysis/](https://github.com/advanced-beginner/advanced-beginner.github.io/tree/main/examples/elasticsearch/log-analysis)
- docker-compose로 Elasticsearch + Kibana 즉시 실행 가능
- 샘플 로그 생성 API 포함
{{< /callout >}}

{{< callout type="tip" title="TL;DR" >}}
- **Logback Appender**로 애플리케이션 로그를 Elasticsearch에 직접 전송합니다
- **MDC**로 요청 ID, 사용자 ID를 로그에 자동 포함하여 추적성을 확보합니다
- **집계(Aggregation)**로 에러율, 응답시간 백분위수 등을 분석합니다
- **ILM**으로 로그 수명주기(Hot→Warm→Cold→Delete)를 자동 관리합니다
- 전체 소요 시간: 약 40분
{{< /callout >}}

Elasticsearch를 사용하여 애플리케이션 로그를 수집, 저장, 분석하는 시스템을 구현합니다.

{{< callout type="info" title="버전 정보" >}}
- **Elasticsearch**: 8.11.x
- **Spring Boot**: 3.2.x
- **Logback**: Spring Boot 기본 포함
{{< /callout >}}

## 구현 목표

```mermaid
flowchart LR
    A[Spring Boot App] -->|Logback| B[Elasticsearch]
    B --> C[Kibana Dashboard]
    B --> D[알림/모니터링]
```

- **로그 수집**: 애플리케이션 로그를 Elasticsearch에 직접 저장
- **실시간 검색**: 에러 로그, 특정 사용자 요청 추적
- **대시보드**: 에러율, 응답시간 분포 시각화
- **알림**: 에러 급증 시 알림

---

## 1. 인덱스 설계

### 로그 인덱스 매핑

```json
PUT /_index_template/logs
{
  "index_patterns": ["logs-*"],
  "template": {
    "settings": {
      "number_of_shards": 2,
      "number_of_replicas": 1,
      "refresh_interval": "5s"
    },
    "mappings": {
      "properties": {
        "@timestamp": {
          "type": "date"
        },
        "level": {
          "type": "keyword"
        },
        "logger": {
          "type": "keyword"
        },
        "thread": {
          "type": "keyword"
        },
        "message": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "exception": {
          "type": "text"
        },
        "stack_trace": {
          "type": "text",
          "index": false
        },
        "application": {
          "type": "keyword"
        },
        "environment": {
          "type": "keyword"
        },
        "host": {
          "type": "keyword"
        },
        "request_id": {
          "type": "keyword"
        },
        "user_id": {
          "type": "keyword"
        },
        "duration_ms": {
          "type": "long"
        },
        "http_method": {
          "type": "keyword"
        },
        "http_path": {
          "type": "keyword"
        },
        "http_status": {
          "type": "integer"
        }
      }
    }
  }
}
```

### 일별 인덱스 패턴

```
logs-2024.01.15
logs-2024.01.16
logs-2024.01.17
```

> **장점**: 오래된 로그 삭제가 쉬움 (인덱스 단위 삭제)

{{< callout type="info" title="핵심 포인트" >}}
- `@timestamp`는 Date 타입으로 시계열 분석의 핵심 필드입니다
- `level`, `logger`는 Keyword 타입으로 정확한 필터링에 사용합니다
- 일별 인덱스 패턴(`logs-*`)으로 오래된 데이터를 쉽게 삭제할 수 있습니다
{{< /callout >}}

---

## 2. Spring Boot 설정

### build.gradle.kts

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")

    // Logback Elasticsearch Appender
    implementation("com.internetitem:logback-elasticsearch-appender:1.6")

    // JSON 로깅
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
}
```

### logback-spring.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <!-- 콘솔 출력 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>requestId</includeMdcKeyName>
            <includeMdcKeyName>userId</includeMdcKeyName>
        </encoder>
    </appender>

    <!-- Elasticsearch 전송 -->
    <appender name="ELASTIC" class="com.internetitem.logback.elasticsearch.ElasticsearchAppender">
        <url>http://localhost:9200/_bulk</url>
        <index>logs-%date{yyyy.MM.dd}</index>
        <type>_doc</type>
        <connectTimeout>30000</connectTimeout>
        <readTimeout>30000</readTimeout>
        <maxQueueSize>10000</maxQueueSize>
        <sleepTime>250</sleepTime>
        <maxRetries>3</maxRetries>

        <property>
            <name>application</name>
            <value>${spring.application.name:-app}</value>
        </property>
        <property>
            <name>environment</name>
            <value>${spring.profiles.active:-default}</value>
        </property>
        <property>
            <name>host</name>
            <value>${HOSTNAME:-unknown}</value>
        </property>
    </appender>

    <!-- 비동기 처리 (성능 향상) -->
    <appender name="ASYNC_ELASTIC" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="ELASTIC"/>
        <queueSize>10000</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <neverBlock>true</neverBlock>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_ELASTIC"/>
    </root>
</configuration>
```

### MDC를 활용한 요청 추적

```java
@Component
public class RequestTrackingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String requestId = UUID.randomUUID().toString().substring(0, 8);
            String userId = extractUserId(request);

            MDC.put("requestId", requestId);
            MDC.put("userId", userId);
            MDC.put("httpMethod", request.getMethod());
            MDC.put("httpPath", request.getRequestURI());

            long startTime = System.currentTimeMillis();

            chain.doFilter(request, response);

            long duration = System.currentTimeMillis() - startTime;
            MDC.put("durationMs", String.valueOf(duration));
            MDC.put("httpStatus", String.valueOf(response.getStatus()));

            log.info("Request completed");
        } finally {
            MDC.clear();
        }
    }

    private String extractUserId(HttpServletRequest request) {
        // JWT나 세션에서 사용자 ID 추출
        return request.getHeader("X-User-Id");
    }
}
```

{{< callout type="info" title="핵심 포인트" >}}
- `AsyncAppender`로 로깅이 애플리케이션 성능에 영향을 주지 않도록 합니다
- `MDC`로 requestId, userId 등 컨텍스트 정보를 자동으로 로그에 포함합니다
- `neverBlock=true`로 큐가 가득 차도 애플리케이션이 멈추지 않습니다
{{< /callout >}}

---

## 3. 로그 검색 API

### LogSearchService.java

```java
@Service
public class LogSearchService {

    private final ElasticsearchOperations operations;

    public LogSearchService(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    /**
     * 에러 로그 검색
     */
    public SearchResult searchErrors(String query,
                                     LocalDateTime from,
                                     LocalDateTime to,
                                     int page, int size) {

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // 에러 레벨만
        boolQuery.filter(Query.of(q -> q
            .terms(t -> t
                .field("level")
                .terms(v -> v.value(List.of(
                    FieldValue.of("ERROR"),
                    FieldValue.of("WARN")
                )))
            )
        ));

        // 시간 범위
        boolQuery.filter(Query.of(q -> q
            .range(r -> r
                .field("@timestamp")
                .gte(JsonData.of(from.toString()))
                .lt(JsonData.of(to.toString()))
            )
        ));

        // 메시지 검색
        if (hasText(query)) {
            boolQuery.must(Query.of(q -> q
                .multiMatch(m -> m
                    .query(query)
                    .fields("message", "exception", "stack_trace")
                )
            ));
        }

        NativeQuery nativeQuery = NativeQuery.builder()
            .withQuery(Query.of(q -> q.bool(boolQuery.build())))
            .withSort(Sort.by("@timestamp").descending())
            .withPageable(PageRequest.of(page, size))
            .build();

        // 일별 인덱스 패턴으로 검색
        SearchHits<LogEntry> hits = operations.search(
            nativeQuery,
            LogEntry.class,
            IndexCoordinates.of("logs-*")
        );

        return toSearchResult(hits);
    }

    /**
     * 특정 요청 추적
     */
    public List<LogEntry> traceRequest(String requestId) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q
                .term(t -> t.field("request_id").value(requestId))
            ))
            .withSort(Sort.by("@timestamp").ascending())
            .withPageable(PageRequest.of(0, 1000))
            .build();

        SearchHits<LogEntry> hits = operations.search(
            query,
            LogEntry.class,
            IndexCoordinates.of("logs-*")
        );

        return hits.getSearchHits().stream()
            .map(SearchHit::getContent)
            .toList();
    }

    /**
     * 사용자별 로그 조회
     */
    public List<LogEntry> getUserLogs(String userId, LocalDateTime from, LocalDateTime to) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder()
            .filter(Query.of(q -> q.term(t -> t.field("user_id").value(userId))))
            .filter(Query.of(q -> q
                .range(r -> r
                    .field("@timestamp")
                    .gte(JsonData.of(from.toString()))
                    .lt(JsonData.of(to.toString()))
                )
            ));

        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q.bool(boolQuery.build())))
            .withSort(Sort.by("@timestamp").descending())
            .withPageable(PageRequest.of(0, 100))
            .build();

        return operations.search(query, LogEntry.class, IndexCoordinates.of("logs-*"))
            .getSearchHits().stream()
            .map(SearchHit::getContent)
            .toList();
    }
}
```

{{< callout type="info" title="핵심 포인트" >}}
- `IndexCoordinates.of("logs-*")`로 모든 일별 인덱스를 한 번에 검색합니다
- `request_id`로 특정 요청의 전체 로그를 추적할 수 있습니다
- 시간 범위 필터는 `range` 쿼리로 구현합니다
{{< /callout >}}

---

## 4. 로그 분석 (집계)

### LogAnalyticsService.java

```java
@Service
public class LogAnalyticsService {

    private final ElasticsearchOperations operations;

    /**
     * 시간대별 에러 수 집계
     */
    public Map<String, Long> getErrorCountByHour(LocalDateTime from, LocalDateTime to) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q
                .bool(b -> b
                    .filter(Query.of(f -> f.term(t -> t.field("level").value("ERROR"))))
                    .filter(Query.of(f -> f
                        .range(r -> r
                            .field("@timestamp")
                            .gte(JsonData.of(from.toString()))
                            .lt(JsonData.of(to.toString()))
                        )
                    ))
                )
            ))
            .withAggregation("errors_by_hour", Aggregation.of(a -> a
                .dateHistogram(dh -> dh
                    .field("@timestamp")
                    .calendarInterval(CalendarInterval.Hour)
                    .format("yyyy-MM-dd HH:00")
                )
            ))
            .withMaxResults(0)
            .build();

        SearchHits<LogEntry> hits = operations.search(
            query, LogEntry.class, IndexCoordinates.of("logs-*"));

        // 결과 파싱
        return parseHistogramResult(hits, "errors_by_hour");
    }

    /**
     * 에러율 계산 (에러 수 / 전체 로그 수)
     */
    public ErrorRateResult getErrorRate(LocalDateTime from, LocalDateTime to) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q
                .range(r -> r
                    .field("@timestamp")
                    .gte(JsonData.of(from.toString()))
                    .lt(JsonData.of(to.toString()))
                )
            ))
            .withAggregation("total", Aggregation.of(a -> a.valueCount(v -> v.field("level"))))
            .withAggregation("errors", Aggregation.of(a -> a
                .filter(f -> f.term(t -> t.field("level").value("ERROR")))
            ))
            .withMaxResults(0)
            .build();

        SearchHits<LogEntry> hits = operations.search(
            query, LogEntry.class, IndexCoordinates.of("logs-*"));

        long total = extractCount(hits, "total");
        long errors = extractCount(hits, "errors");
        double errorRate = total > 0 ? (double) errors / total * 100 : 0;

        return new ErrorRateResult(total, errors, errorRate);
    }

    /**
     * HTTP 상태 코드 분포
     */
    public Map<Integer, Long> getStatusCodeDistribution(LocalDateTime from, LocalDateTime to) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q
                .bool(b -> b
                    .filter(Query.of(f -> f
                        .range(r -> r
                            .field("@timestamp")
                            .gte(JsonData.of(from.toString()))
                            .lt(JsonData.of(to.toString()))
                        )
                    ))
                    .filter(Query.of(f -> f.exists(e -> e.field("http_status"))))
                )
            ))
            .withAggregation("status_codes", Aggregation.of(a -> a
                .terms(t -> t.field("http_status").size(20))
            ))
            .withMaxResults(0)
            .build();

        SearchHits<LogEntry> hits = operations.search(
            query, LogEntry.class, IndexCoordinates.of("logs-*"));

        return parseTermsResult(hits, "status_codes");
    }

    /**
     * 느린 요청 Top N
     */
    public List<SlowRequest> getSlowRequests(int topN, long thresholdMs) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q
                .range(r -> r.field("duration_ms").gte(JsonData.of(thresholdMs)))
            ))
            .withSort(Sort.by("duration_ms").descending())
            .withPageable(PageRequest.of(0, topN))
            .build();

        return operations.search(query, LogEntry.class, IndexCoordinates.of("logs-*"))
            .getSearchHits().stream()
            .map(hit -> new SlowRequest(
                hit.getContent().getHttpPath(),
                hit.getContent().getDurationMs(),
                hit.getContent().getTimestamp()
            ))
            .toList();
    }

    /**
     * 응답 시간 백분위수
     */
    public LatencyPercentiles getLatencyPercentiles(LocalDateTime from, LocalDateTime to) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(Query.of(q -> q
                .bool(b -> b
                    .filter(Query.of(f -> f.exists(e -> e.field("duration_ms"))))
                    .filter(Query.of(f -> f
                        .range(r -> r
                            .field("@timestamp")
                            .gte(JsonData.of(from.toString()))
                            .lt(JsonData.of(to.toString()))
                        )
                    ))
                )
            ))
            .withAggregation("latency_percentiles", Aggregation.of(a -> a
                .percentiles(p -> p
                    .field("duration_ms")
                    .percents(50.0, 90.0, 95.0, 99.0)
                )
            ))
            .withMaxResults(0)
            .build();

        SearchHits<LogEntry> hits = operations.search(
            query, LogEntry.class, IndexCoordinates.of("logs-*"));

        return parsePercentilesResult(hits, "latency_percentiles");
    }
}
```

{{< callout type="info" title="핵심 포인트" >}}
- `dateHistogram`으로 시간대별 에러 추이를 분석합니다
- `percentiles`로 응답시간 p50, p90, p95, p99를 계산합니다
- `withMaxResults(0)`으로 문서 없이 집계 결과만 가져옵니다
{{< /callout >}}

---

## 5. 인덱스 수명 관리 (ILM)

### ILM 정책 설정

```json
PUT /_ilm/policy/logs_policy
{
  "policy": {
    "phases": {
      "hot": {
        "min_age": "0ms",
        "actions": {
          "rollover": {
            "max_age": "1d",
            "max_size": "50gb"
          },
          "set_priority": {
            "priority": 100
          }
        }
      },
      "warm": {
        "min_age": "7d",
        "actions": {
          "shrink": {
            "number_of_shards": 1
          },
          "forcemerge": {
            "max_num_segments": 1
          },
          "set_priority": {
            "priority": 50
          }
        }
      },
      "cold": {
        "min_age": "30d",
        "actions": {
          "set_priority": {
            "priority": 0
          }
        }
      },
      "delete": {
        "min_age": "90d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}
```

### 인덱스 템플릿에 ILM 적용

```json
PUT /_index_template/logs
{
  "index_patterns": ["logs-*"],
  "template": {
    "settings": {
      "index.lifecycle.name": "logs_policy",
      "index.lifecycle.rollover_alias": "logs"
    }
  }
}
```

{{< callout type="info" title="핵심 포인트" >}}
- **Hot**: 최신 데이터, SSD에서 빠른 검색
- **Warm**: 7일 이후 데이터, 샤드 축소로 리소스 절약
- **Cold**: 30일 이후 데이터, 저비용 스토리지
- **Delete**: 90일 이후 자동 삭제로 스토리지 관리
{{< /callout >}}

---

## 6. Kibana 대시보드 설정

### 주요 시각화

1. **에러율 추이** (Line Chart)
   - X축: @timestamp
   - Y축: Count of level:ERROR

2. **로그 레벨 분포** (Pie Chart)
   - 필드: level

3. **HTTP 상태 코드** (Horizontal Bar)
   - 필드: http_status

4. **응답 시간 분포** (Histogram)
   - 필드: duration_ms

5. **최근 에러 목록** (Data Table)
   - 필드: @timestamp, level, message, http_path

### 알림 규칙 (Kibana Alerting)

```
조건: 5분간 ERROR 로그 > 100건
액션: Slack/Email 알림
```

---

## 7. 성능 최적화 팁

### 로깅 성능

```java
// ❌ 비효율 - 항상 문자열 연결
log.debug("Processing order: " + order.getId() + " for user: " + user.getName());

// ✅ 효율적 - 레벨 체크 후 처리
log.debug("Processing order: {} for user: {}", order.getId(), user.getName());

// ✅ 더 효율적 - 비용이 큰 연산
if (log.isDebugEnabled()) {
    log.debug("Order details: {}", toJson(order));
}
```

### 인덱스 설정

```json
{
  "settings": {
    "refresh_interval": "5s",      // 실시간성 vs 성능 트레이드오프
    "number_of_replicas": 0,       // 개발 환경
    "translog.durability": "async" // 대량 로그 시
  }
}
```

{{< callout type="info" title="핵심 포인트" >}}
- **로깅 성능**: `log.debug("{}", value)` 형식으로 불필요한 문자열 연결을 피합니다
- **refresh_interval**: 값을 늘리면 인덱싱 성능이 향상되지만 검색 지연이 발생합니다
- **대량 로그**: `translog.durability: async`로 쓰기 성능을 높일 수 있습니다
{{< /callout >}}

---

## 다음 단계

| 목표 | 추천 문서 |
|------|----------|
| 분산 로그 수집 | [분산 로그 수집 아키텍처]({{< relref "/docs/observability/concepts/log-aggregation" >}}) |
| 집계 심화 | [집계](../concepts/aggregations/) |
| 성능 최적화 | [성능 튜닝](../concepts/performance-tuning/) |
| 클러스터 관리 | [고가용성](../concepts/high-availability/) |
