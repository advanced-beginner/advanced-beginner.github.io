---
title: Spring Boot 메트릭
description: Spring Boot Actuator + Micrometer로 애플리케이션 메트릭을 노출합니다
weight: 2
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

> **소요 시간**: 20분
> **선수 지식**: Spring Boot, [환경 구성](setup/)
> **이 문서를 읽으면**: Spring Boot 애플리케이션에 Observability를 적용할 수 있습니다

---

## Step 1/8: 의존성 추가 (2분)

```kotlin
// build.gradle.kts
dependencies {
    // Actuator + Prometheus
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Tracing
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")

    // Logging (Loki)
    implementation("com.github.loki4j:loki-logback-appender:1.4.2")
}
```

## Step 2/8: application.yml 설정 (3분)

```yaml
spring:
  application:
    name: order-service

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  endpoint:
    health:
      show-details: always
  metrics:
    tags:
      application: ${spring.application.name}
    distribution:
      percentiles-histogram:
        http.server.requests: true
      slo:
        http.server.requests: 50ms, 100ms, 200ms, 500ms, 1s
  tracing:
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces

logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```

## Step 3/8: Loki 로그 설정 (3분)

```xml
<!-- src/main/resources/logback-spring.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <springProperty scope="context" name="appName" source="spring.application.name"/>

    <!-- Console Appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{ISO8601} %5p [${appName},%X{traceId:-},%X{spanId:-}] --- [%t] %-40.40logger{39} : %m%n</pattern>
        </encoder>
    </appender>

    <!-- Loki Appender -->
    <appender name="LOKI" class="com.github.loki4j.logback.Loki4jAppender">
        <http>
            <url>http://localhost:3100/loki/api/v1/push</url>
        </http>
        <format>
            <label>
                <pattern>app=${appName},host=${HOSTNAME},level=%level</pattern>
            </label>
            <message>
                <pattern>{"timestamp":"%d{ISO8601}","level":"%level","logger":"%logger","message":"%msg","traceId":"%X{traceId:-}","spanId":"%X{spanId:-}"}</pattern>
            </message>
        </format>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="LOKI"/>
    </root>
</configuration>
```

### 중간 확인 (Step 1~3 완료)

- [ ] `build.gradle.kts`에 의존성 추가됨
- [ ] `application.yml`에 management 설정 완료
- [ ] `logback-spring.xml` 파일 생성됨
- [ ] `./gradlew bootRun` 실행 후 http://localhost:8080/actuator/prometheus 접속 시 메트릭 표시

---

## Step 4/8: 커스텀 메트릭 추가 (5분)

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final MeterRegistry meterRegistry;
    private final OrderService orderService;

    // Counter: 주문 수
    private Counter orderCounter(String status) {
        return Counter.builder("orders_total")
            .tag("status", status)
            .description("Total number of orders")
            .register(meterRegistry);
    }

    // Gauge: 처리 중인 주문
    private final AtomicInteger ordersInProgress = new AtomicInteger(0);

    @PostConstruct
    void registerGauge() {
        Gauge.builder("orders_in_progress", ordersInProgress, AtomicInteger::get)
            .description("Orders currently being processed")
            .register(meterRegistry);
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        ordersInProgress.incrementAndGet();
        try {
            // Timer로 처리 시간 측정
            return Timer.builder("order_processing_duration_seconds")
                .tag("type", request.getType())
                .publishPercentileHistogram()
                .register(meterRegistry)
                .record(() -> {
                    Order order = orderService.create(request);
                    orderCounter("success").increment();
                    return ResponseEntity.ok(order);
                });
        } catch (Exception e) {
            orderCounter("failed").increment();
            throw e;
        } finally {
            ordersInProgress.decrementAndGet();
        }
    }
}
```

## Step 5/8: 수동 트레이싱 (3분)

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final Tracer tracer;
    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;

    public Order create(OrderRequest request) {
        // 현재 Span에 태그 추가
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("order.type", request.getType());
            currentSpan.tag("order.items", String.valueOf(request.getItems().size()));
        }

        // 새 Span 생성
        Span inventorySpan = tracer.nextSpan().name("checkInventory").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(inventorySpan)) {
            inventoryClient.check(request.getItems());
            inventorySpan.event("Inventory checked");
        } finally {
            inventorySpan.end();
        }

        // 결제 처리
        Span paymentSpan = tracer.nextSpan().name("processPayment").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(paymentSpan)) {
            paymentClient.process(request.getPayment());
            paymentSpan.event("Payment processed");
        } finally {
            paymentSpan.end();
        }

        log.info("Order created: {}", request.getId());
        return new Order(request);
    }
}
```

### 중간 확인 (Step 4~5 완료)

- [ ] `OrderController`에 Counter, Gauge, Timer 메트릭 추가됨
- [ ] `OrderService`에 수동 Span 생성 코드 추가됨
- [ ] 컴파일 오류 없이 빌드 성공: `./gradlew build`

---

## Step 6/8: 메트릭 확인 (2분)

### Prometheus 엔드포인트

```bash
curl http://localhost:8080/actuator/prometheus
```

**예상 출력:**
```
# HELP orders_total Total number of orders
# TYPE orders_total counter
orders_total{status="success"} 42.0
orders_total{status="failed"} 3.0

# HELP orders_in_progress Orders currently being processed
# TYPE orders_in_progress gauge
orders_in_progress 2.0

# HELP http_server_requests_seconds
# TYPE http_server_requests_seconds histogram
http_server_requests_seconds_bucket{method="POST",uri="/orders",le="0.05"} 35.0
http_server_requests_seconds_bucket{method="POST",uri="/orders",le="0.1"} 40.0
```

### Grafana에서 확인

1. http://localhost:3000 접속
2. Explore → Prometheus 선택
3. 쿼리 입력:

```promql
# 초당 요청 수
rate(http_server_requests_seconds_count{application="order-service"}[5m])

# P99 응답시간
histogram_quantile(0.99,
  sum by (le) (rate(http_server_requests_seconds_bucket{application="order-service"}[5m]))
)

# 에러율
sum(rate(http_server_requests_seconds_count{application="order-service",status=~"5.."}[5m]))
/ sum(rate(http_server_requests_seconds_count{application="order-service"}[5m]))
```

## Step 7/8: 로그 확인 (1분)

1. Grafana → Explore → Loki 선택
2. 쿼리:

```logql
{app="order-service"} |= "Order created"

{app="order-service"} | json | level="ERROR"

{app="order-service"} | json | traceId="abc123"
```

## Step 8/8: 트레이스 확인 (1분)

1. Grafana → Explore → Tempo 선택
2. Search 탭에서 Service 선택
3. 트레이스 ID로 검색

---

## Recording Rules 추가

```yaml
# prometheus/rules/spring.yml
groups:
  - name: spring_boot
    rules:
      - record: application:http_requests:rate5m
        expr: sum by (application) (rate(http_server_requests_seconds_count[5m]))

      - record: application:http_requests:p99
        expr: |
          histogram_quantile(0.99,
            sum by (application, le) (rate(http_server_requests_seconds_bucket[5m]))
          )

      - record: application:http_errors:ratio
        expr: |
          sum by (application) (rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
          / sum by (application) (rate(http_server_requests_seconds_count[5m]))
```

## 알림 규칙 추가

```yaml
# prometheus/rules/alerts.yml
groups:
  - name: spring_boot_alerts
    rules:
      - alert: HighErrorRate
        expr: application:http_errors:ratio > 0.05
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High error rate on {{ $labels.application }}"

      - alert: HighP99Latency
        expr: application:http_requests:p99 > 0.5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High P99 latency on {{ $labels.application }}"
```

---

## 확인 체크리스트

- [ ] `/actuator/prometheus` 엔드포인트 응답
- [ ] Prometheus에서 메트릭 조회 가능
- [ ] Grafana에서 그래프 표시
- [ ] Loki에서 로그 검색 가능
- [ ] Tempo에서 트레이스 확인 가능

---

## 다음 단계

| 추천 순서 | 문서 | 배우는 것 |
|----------|------|----------|
| 1 | [Kafka 모니터링](kafka-monitoring/) | Kafka 관측성 |
| 2 | [풀스택 예제](full-stack/) | 통합 예제 |
