---
title: 메트릭 카디널리티 최적화
description: Prometheus 시계열 폭발을 방지하고 비용을 절감합니다
weight: 2
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

> **대상 상황**: Prometheus 메모리/스토리지 급증, 쿼리 느림
> **목표**: 불필요한 시계열을 줄여 비용 최적화
> **소요 시간**: 30분~1시간 (분석 및 수정 복잡도에 따라 상이)
> **성공 기준**: 시계열 수가 목표치 이하로 감소하고 메모리 사용량이 안정화됨

## 문제 상황

```
Alert: PrometheusHighCardinality
Active Series: 2,500,000 (Threshold: 1,000,000)
Memory Usage: 32GB
```

## 카디널리티란?

**카디널리티 = 고유한 시계열 수**

```
http_requests_total{method="GET", status="200", path="/api/users"}     # 1개
http_requests_total{method="GET", status="200", path="/api/users/123"} # 또 1개!
http_requests_total{method="GET", status="200", path="/api/users/456"} # 또 1개!
```

**문제**: path에 user_id가 들어가면 사용자 수만큼 시계열 생성

## Step 1: 카디널리티 분석

### 현재 시계열 수

```promql
# 전체 시계열 수
count({__name__=~".+"})

# 메트릭별 시계열 수
count by (__name__) ({__name__=~".+"})

# 상위 10개 메트릭
topk(10, count by (__name__) ({__name__=~".+"}))
```

### 라벨별 카디널리티

```promql
# 라벨별 고유값 수
count(count by (path) (http_requests_total))

# 높은 카디널리티 라벨 찾기
count by (path) (http_requests_total)
```

### TSDB 상태 확인

```bash
curl http://localhost:9090/api/v1/status/tsdb | jq .
```

## Step 2: 문제 라벨 식별

### 위험한 패턴

| 패턴 | 문제 | 예상 시계열 |
|------|------|------------|
| `user_id` | 사용자 수만큼 | 수십만+ |
| `request_id` | 요청마다 | 무한 |
| `timestamp` | 시간마다 | 무한 |
| `session_id` | 세션마다 | 수만+ |
| URL 경로 (ID 포함) | ID마다 | 수만+ |

### 안전한 패턴

| 패턴 | 예상 시계열 |
|------|------------|
| `method` (GET/POST) | ~5 |
| `status` (2xx/4xx/5xx) | ~5 |
| `service` | ~100 |
| `endpoint` (정규화) | ~100 |

## Step 3: 해결 방법

### 1. 애플리케이션에서 수정

```java
// ❌ 나쁨: 동적 경로
Timer.builder("http_requests")
    .tag("path", "/users/123")  // user_id가 들어감
    .register(registry);

// ✅ 좋음: 정규화된 경로
Timer.builder("http_requests")
    .tag("path", "/users/{id}")  // 패턴화
    .register(registry);
```

### Spring Boot 설정

```java
@Configuration
public class MetricsConfig {
    @Bean
    public WebMvcTagsContributor webMvcTagsContributor() {
        return (exchange, response, handler) -> {
            // URL 정규화
            String pattern = getPattern(handler);
            return Tags.of("uri", pattern != null ? pattern : "UNKNOWN");
        };
    }
}
```

### 2. Prometheus Relabeling

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'app'
    metric_relabel_configs:
      # 특정 라벨 제거
      - action: labeldrop
        regex: 'user_id|request_id|session_id'

      # 특정 메트릭 제외
      - source_labels: [__name__]
        regex: 'go_.*'
        action: drop

      # 높은 카디널리티 메트릭 제외
      - source_labels: [__name__]
        regex: 'http_requests_total'
        action: drop
        # 대신 recording rule 사용
```

### 3. Recording Rules로 집계

```yaml
# 원본: 높은 카디널리티
# http_requests_total{method, status, path, instance, pod}

# Recording Rule: 필요한 라벨만
groups:
  - name: aggregation
    rules:
      - record: job:http_requests:rate5m
        expr: sum by (job, method, status) (rate(http_requests_total[5m]))
```

### 4. 수집 자체를 제한

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'app'
    sample_limit: 10000  # 샘플 수 제한
    metric_relabel_configs:
      - source_labels: [__name__]
        regex: 'go_.*|process_.*'  # 불필요한 메트릭
        action: drop
```

## Step 4: 확인

### 개선 전후 비교

```promql
# 시계열 수 변화
count({__name__=~".+"})

# 메모리 사용량
process_resident_memory_bytes{job="prometheus"}

# 쿼리 시간
prometheus_engine_query_duration_seconds
```

## 예방 가이드라인

### 라벨 설계 원칙

1. **라벨 값은 유한해야 함**
   - ✅ `method=GET|POST|PUT|DELETE`
   - ❌ `user_id=12345`

2. **라벨 조합 예측**
   ```
   총 시계열 = 메트릭 수 × 라벨1 × 라벨2 × ...

   예: 100 메트릭 × 5 method × 5 status × 100 path = 250,000
   ```

3. **동적 값은 메트릭 값으로**
   ```java
   // ❌ 라벨로
   Counter.builder("orders").tag("order_id", id).register(registry);

   // ✅ 로그로
   log.info("Order created: {}", orderId);
   ```

### 코드 리뷰 체크리스트

- [ ] 동적 라벨 값 없는가?
- [ ] 라벨 조합 수 예측했는가?
- [ ] ID, timestamp 포함 안 했는가?
- [ ] URL 패턴화 했는가?

---

## 빠른 참조

| 상황 | 해결책 |
|------|--------|
| user_id 라벨 | 제거 또는 로그로 |
| URL에 ID | 패턴화 (`/users/{id}`) |
| 불필요한 메트릭 | `action: drop` |
| 높은 카디널리티 메트릭 | Recording Rule로 집계 |
