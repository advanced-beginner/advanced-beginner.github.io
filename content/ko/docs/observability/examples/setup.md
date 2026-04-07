---
title: 환경 구성
description: Docker Compose로 Prometheus, Grafana, Loki, Tempo를 구성합니다
weight: 1
author: "@advanced-beginner"
lastmod: "2026-01-12"
---

> **소요 시간**: 15분
> **선수 지식**: Docker, Docker Compose 기본
> **이 문서를 읽으면**: 로컬에 완전한 Observability 스택을 구축할 수 있습니다

## 전체 아키텍처

```mermaid
graph TB
    APP["Spring Boot App"] --> |"metrics"| PROM["Prometheus"]
    APP --> |"logs"| LOKI["Loki"]
    APP --> |"traces"| TEMPO["Tempo"]

    PROM --> GF["Grafana"]
    LOKI --> GF
    TEMPO --> GF

    PROM --> AM["Alertmanager"]
```

*Spring Boot 앱에서 Prometheus(메트릭), Loki(로그), Tempo(트레이스)로 데이터를 보내고 Grafana에서 통합 시각화하는 전체 아키텍처입니다.*

## Step 1: 디렉토리 구조 생성

```bash
mkdir -p observability-stack/{prometheus/rules,grafana/{provisioning/datasources,provisioning/dashboards,dashboards},loki,promtail}
cd observability-stack
```

## Step 2: Docker Compose 작성

```yaml
# docker-compose.yml
services:
  # 메트릭 수집
  prometheus:
    image: prom/prometheus:v2.50.0
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - ./prometheus/rules:/etc/prometheus/rules
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.enable-lifecycle'
      - '--web.enable-remote-write-receiver'

  # 알림 관리
  alertmanager:
    image: prom/alertmanager:v0.26.0
    container_name: alertmanager
    ports:
      - "9093:9093"
    volumes:
      - ./alertmanager/alertmanager.yml:/etc/alertmanager/alertmanager.yml

  # 시각화
  grafana:
    image: grafana/grafana:10.3.0
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_AUTH_ANONYMOUS_ENABLED=true
      - GF_AUTH_ANONYMOUS_ORG_ROLE=Viewer
    volumes:
      - ./grafana/provisioning:/etc/grafana/provisioning
      - ./grafana/dashboards:/var/lib/grafana/dashboards
      - grafana-data:/var/lib/grafana

  # 로그 수집
  loki:
    image: grafana/loki:2.9.0
    container_name: loki
    ports:
      - "3100:3100"
    volumes:
      - ./loki/loki.yml:/etc/loki/local-config.yaml
      - loki-data:/loki
    command: -config.file=/etc/loki/local-config.yaml

  # 로그 에이전트
  promtail:
    image: grafana/promtail:2.9.0
    container_name: promtail
    volumes:
      - ./promtail/promtail.yml:/etc/promtail/config.yml
      - /var/log:/var/log:ro
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
    command: -config.file=/etc/promtail/config.yml

  # 분산 추적
  tempo:
    image: grafana/tempo:2.3.0
    container_name: tempo
    ports:
      - "4317:4317"   # OTLP gRPC
      - "4318:4318"   # OTLP HTTP
      - "3200:3200"   # Tempo API
    volumes:
      - ./tempo/tempo.yml:/etc/tempo/config.yaml
      - tempo-data:/var/tempo
    command: -config.file=/etc/tempo/config.yaml

volumes:
  prometheus-data:
  grafana-data:
  loki-data:
  tempo-data:
```

## Step 3: Prometheus 설정

```yaml
# prometheus/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

rule_files:
  - /etc/prometheus/rules/*.yml

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'spring-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8080']
```

## Step 4: Loki 설정

```yaml
# loki/loki.yml
auth_enabled: false

server:
  http_listen_port: 3100

common:
  path_prefix: /loki
  storage:
    filesystem:
      chunks_directory: /loki/chunks
      rules_directory: /loki/rules
  replication_factor: 1
  ring:
    instance_addr: 127.0.0.1
    kvstore:
      store: inmemory

schema_config:
  configs:
    - from: 2020-10-24
      store: boltdb-shipper
      object_store: filesystem
      schema: v11
      index:
        prefix: index_
        period: 24h

limits_config:
  retention_period: 168h
```

## Step 5: Promtail 설정

```yaml
# promtail/promtail.yml
server:
  http_listen_port: 9080

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: containers
    static_configs:
      - targets:
          - localhost
        labels:
          job: containerlogs
          __path__: /var/lib/docker/containers/*/*log

    pipeline_stages:
      - json:
          expressions:
            output: log
            stream: stream
            attrs:
      - json:
          expressions:
            tag:
          source: attrs
      - regex:
          expression: (?P<container_name>(?:[^|]*[^|]))
          source: tag
      - labels:
          container_name:
          stream:
```

## Step 6: Tempo 설정

```yaml
# tempo/tempo.yml
server:
  http_listen_port: 3200

distributor:
  receivers:
    otlp:
      protocols:
        grpc:
          endpoint: 0.0.0.0:4317
        http:
          endpoint: 0.0.0.0:4318

storage:
  trace:
    backend: local
    local:
      path: /var/tempo/traces
    wal:
      path: /var/tempo/wal

metrics_generator:
  registry:
    external_labels:
      source: tempo
  storage:
    path: /var/tempo/generator/wal
    remote_write:
      - url: http://prometheus:9090/api/v1/write
        send_exemplars: true
```

## Step 7: Grafana 데이터소스 설정

```yaml
# grafana/provisioning/datasources/datasources.yml
apiVersion: 1
datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true

  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100

  - name: Tempo
    type: tempo
    access: proxy
    url: http://tempo:3200
    jsonData:
      tracesToLogsV2:
        datasourceUid: loki
        filterByTraceID: true
      tracesToMetrics:
        datasourceUid: prometheus
      serviceMap:
        datasourceUid: prometheus
```

## Step 8: Alertmanager 설정

```bash
mkdir -p alertmanager
```

```yaml
# alertmanager/alertmanager.yml
global:
  resolve_timeout: 5m

route:
  receiver: 'default'
  group_by: ['alertname', 'job']
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 4h

receivers:
  - name: 'default'
    webhook_configs:
      - url: 'http://localhost:5001/'
        send_resolved: true
```

## Step 9: 스택 실행

```bash
docker compose up -d
```

**예상 출력:**
```
[+] Running 7/7
 ✔ Container prometheus    Started
 ✔ Container alertmanager  Started
 ✔ Container grafana       Started
 ✔ Container loki          Started
 ✔ Container promtail      Started
 ✔ Container tempo         Started
```

## Step 10: 상태 확인

```bash
docker compose ps
```

**접속 URL:**

| 서비스 | URL | 용도 |
|--------|-----|------|
| Grafana | http://localhost:3000 | 대시보드 (admin/admin) |
| Prometheus | http://localhost:9090 | 메트릭 조회 |
| Alertmanager | http://localhost:9093 | 알림 상태 |
| Loki | http://localhost:3100 | 로그 API |
| Tempo | http://localhost:3200 | 트레이스 API |

## 확인 체크리스트

- [ ] 모든 컨테이너 `Up` 상태
- [ ] Grafana 로그인 성공
- [ ] Prometheus에서 `up` 쿼리 결과 확인
- [ ] Grafana → Explore → Loki 데이터소스 선택 가능
- [ ] Grafana → Explore → Tempo 데이터소스 선택 가능

## 정리

```bash
# 중지
docker compose stop

# 삭제 (볼륨 유지)
docker compose down

# 완전 삭제 (볼륨 포함)
docker compose down -v
```

---

## 다음 단계

| 추천 순서 | 문서 | 배우는 것 |
|----------|------|----------|
| 1 | [Spring Boot 메트릭](spring-boot-metrics/) | 애플리케이션 연동 |
| 2 | [Kafka 모니터링](kafka-monitoring/) | Kafka 관측성 |
