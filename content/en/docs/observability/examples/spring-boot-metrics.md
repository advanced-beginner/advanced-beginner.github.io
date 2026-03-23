---
title: Spring Boot Metrics
description: Expose application metrics with Spring Boot Actuator + Micrometer
weight: 2
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

> **Time Required**: 20 minutes
> **Prerequisites**: Spring Boot, [Environment Setup]({{< relref "/docs/observability/examples/setup" >}})
> **What You'll Learn**: Apply observability to Spring Boot applications

---

## Step 1/8: Add Dependencies (2 min)

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

## Step 2/8: application.yml Configuration (3 min)

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

## Step 3/8: Loki Log Configuration (3 min)

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

### Intermediate Verification (Steps 1~3 Complete)

- [ ] Dependencies added to `build.gradle.kts`
- [ ] Management configuration complete in `application.yml`
- [ ] `logback-spring.xml` file created
- [ ] After `./gradlew bootRun`, metrics displayed at http://localhost:8080/actuator/prometheus

---

## Step 4/8: Add Custom Metrics (5 min)

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final MeterRegistry meterRegistry;
    private final OrderService orderService;

    // Counter: Order count
    private Counter orderCounter(String status) {
        return Counter.builder("orders_total")
            .tag("status", status)
            .description("Total number of orders")
            .register(meterRegistry);
    }

    // Gauge: Orders in progress
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
            // Timer to measure processing time
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

## Step 5/8: Manual Tracing (3 min)

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final Tracer tracer;
    private final PaymentClient paymentClient;
    private final InventoryClient inventoryClient;

    public Order create(OrderRequest request) {
        // Add tags to current Span
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            currentSpan.tag("order.type", request.getType());
            currentSpan.tag("order.items", String.valueOf(request.getItems().size()));
        }

        // Create new Span
        Span inventorySpan = tracer.nextSpan().name("checkInventory").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(inventorySpan)) {
            inventoryClient.check(request.getItems());
            inventorySpan.event("Inventory checked");
        } finally {
            inventorySpan.end();
        }

        // Process payment
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

### Intermediate Verification (Steps 4~5 Complete)

- [ ] Counter, Gauge, Timer metrics added to `OrderController`
- [ ] Manual Span creation code added to `OrderService`
- [ ] Build successful without compilation errors: `./gradlew build`

---

## Step 6/8: Verify Metrics (2 min)

### Prometheus Endpoint

```bash
curl http://localhost:8080/actuator/prometheus
```

**Expected Output:**
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

### Verify in Grafana

1. Access http://localhost:3000
2. Explore → Select Prometheus
3. Enter queries:

```promql
# Requests per second
rate(http_server_requests_seconds_count{application="order-service"}[5m])

# P99 response time
histogram_quantile(0.99,
  sum by (le) (rate(http_server_requests_seconds_bucket{application="order-service"}[5m]))
)

# Error rate
sum(rate(http_server_requests_seconds_count{application="order-service",status=~"5.."}[5m]))
/ sum(rate(http_server_requests_seconds_count{application="order-service"}[5m]))
```

## Step 7/8: Verify Logs (1 min)

1. Grafana → Explore → Select Loki
2. Query:

```logql
{app="order-service"} |= "Order created"

{app="order-service"} | json | level="ERROR"

{app="order-service"} | json | traceId="abc123"
```

## Step 8/8: Verify Traces (1 min)

1. Grafana → Explore → Select Tempo
2. Select Service in Search tab
3. Search by trace ID

---

## Add Recording Rules

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

## Add Alert Rules

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

## Verification Checklist

- [ ] `/actuator/prometheus` endpoint responds
- [ ] Metrics queryable in Prometheus
- [ ] Graphs displayed in Grafana
- [ ] Logs searchable in Loki
- [ ] Traces viewable in Tempo

---

## Next Steps

| Recommended Order | Document | What You'll Learn |
|----------|------|----------|
| 1 | [Kafka Monitoring](kafka-monitoring/) | Kafka observability |
| 2 | [Full-Stack Example](full-stack/) | Integrated example |
